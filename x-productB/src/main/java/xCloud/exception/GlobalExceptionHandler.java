package xCloud.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import xCloud.entity.Result;

import java.io.IOException;
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
     * 处理SSE客户端断开异常（正常情况，不记录错误日志）
     * 当客户端主动关闭SSE连接时，服务端会抛出此异常
     */
    @ExceptionHandler({AsyncRequestNotUsableException.class, ClientAbortException.class})
    public void handleClientAbortException(Exception ex) {
        // 判断是否为Broken pipe异常
        if (ex.getMessage() != null && ex.getMessage().contains("Broken pipe")) {
            log.debug("客户端主动断开SSE连接（正常行为）: {}", ex.getMessage());
        } else {
            log.warn("客户端异常断开: {}", ex.getMessage());
        }
        // 不返回任何响应，因为连接已断开
    }

    /**
     * 处理IO异常（包括客户端断开）
     */
    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException ex) {
        // Broken pipe 是客户端主动断开连接，属于正常情况
        if (ex.getMessage() != null && ex.getMessage().contains("Broken pipe")) {
            log.debug("客户端断开连接: {}", ex.getMessage());
        } else {
            log.error("IO异常", ex);
        }
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
