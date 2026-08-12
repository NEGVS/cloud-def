package xCloud.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xCloud.entity.ResultEntity;

/**
 * @Description 全局异常处理：统一返回 ResultEntity，避免异常堆栈直接暴露给前端
 * @Author Andy Fan
 * @Date 2026/08/08
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==========【业务异常：按业务码返回】==========
    @ExceptionHandler(BusinessException.class)
    public ResultEntity<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return ResultEntity.error(e.getMessage());
    }

    // ==========【参数校验异常：取第一条校验提示】==========
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResultEntity<Void> handleValid(Exception e) {
        FieldError fieldError = e instanceof MethodArgumentNotValidException ? ((MethodArgumentNotValidException) e).getBindingResult().getFieldError() : ((BindException) e).getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return ResultEntity.error(msg);
    }

    // ==========【兜底异常：不向前端暴露细节，仅记录日志】==========
    @ExceptionHandler(Exception.class)
    public ResultEntity<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ResultEntity.error("系统繁忙，请稍后重试");
    }
}
