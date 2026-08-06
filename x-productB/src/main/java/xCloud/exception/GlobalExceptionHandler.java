package xCloud.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xCloud.entity.Result;

import java.util.stream.Collectors;

/**
 * @Description 全局异常处理器，统一捕获校验失败、业务异常等错误并返回标准格式
 * @Author Andy Fan
 * @Date 2026/5/22
 * @ClassName GlobalExceptionHandler
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕获 @Valid / @Validated 校验失败异常
     * 例如 @Pattern、@NotNull、@Size 等注解未通过时触发
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return Result.error(message);
    }

    /**
     * 兜底处理未预期的异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return Result.error("系统异常，请稍后重试");
    }
}
