package xCloud.config.exception;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 19:25
 * @ClassName InsufficientBalanceException
 */
public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
