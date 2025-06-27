package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/26
 * @ClassName Result
 */
@Schema(description = "Standard API response")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Result<T> {

    @Schema(description = "Response code (0 for success, 1 for error)", example = "0")
    private Integer code;

    @Schema(description = "Response message", example = "Success")
    private String message;

    @Schema(description = "Response data")
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "Success", data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(444, message, null);
    }
}
