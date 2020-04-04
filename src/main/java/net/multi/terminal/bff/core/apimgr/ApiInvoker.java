package net.multi.terminal.bff.core.apimgr;

import lombok.extern.slf4j.Slf4j;
import net.multi.terminal.bff.constant.MsgCode;
import net.multi.terminal.bff.exception.ApiException;
import net.multi.terminal.bff.model.ApiReq;
import net.multi.terminal.bff.model.ApiRsp;
import org.springframework.beans.BeanUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * Api执行器,用于调用Api方法
 * 处理Api方法调用前后的通用逻辑
 */
@Slf4j
public class ApiInvoker {
    private ApiRunContext runContext;

    public ApiInvoker(ApiRunContext runContext) {
        this.runContext = runContext;
    }

    public final ApiRsp doInvoke(ApiReq inputMessage) throws ApiException {
        Object input = convertInput(inputMessage);
        //参数校验
        try {
            return invoke(input);
        } catch (Throwable e) {
            throw new ApiException(e, MsgCode.E_11009);
        }
    }

    private ApiRsp invoke(Object input) throws Throwable {
        return (ApiRsp) runContext.getMethodHandle().bindTo(runContext.getInstance()).invoke(input);
    }


    private Object convertInput(ApiReq inputMessage) {
        Object bodyObj = inputMessage.getBody().toJavaObject(runContext.getArgType());
        BeanUtils.copyProperties(inputMessage, bodyObj);
        return bodyObj;
    }

    public void checkParameter(Object t) throws ApiException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(t);
        if (!constraintViolations.isEmpty()) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(MsgCode.E_11011.getMessage());
            constraintViolations.forEach((s) -> {
                buffer.append("," + s.getMessage());
            });
            ApiException be = new ApiException(MsgCode.E_11011, buffer.toString());
            throw be;
        }
    }
}
