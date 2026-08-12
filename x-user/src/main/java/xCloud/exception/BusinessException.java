package xCloud.exception;

import lombok.Getter;

/**
 * @Description 业务异常：用于向上抛出可预期的业务错误（如登录失败、账号停用）
 * @Author Andy Fan
 * @Date 2026/08/08
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
