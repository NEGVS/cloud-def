package xCloud.entity;

import lombok.Data;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/4/7 14:32
 * @ClassName ResultEntity
 */
@Data
public class ResultEntity<T> {

    private int code; // 状态码

    private String message; // 消息

    private T data; // 数据

    // 成功返回
    public static <T> ResultEntity<T> success(T data) {
        ResultEntity<T> result = new ResultEntity<>();
        result.setCode(200);
        result.setMessage("成功");
        result.setData(data);
        return result;
    }

    // 失败返回
    public static <T> ResultEntity<T> error(String message) {
        ResultEntity<T> result = new ResultEntity<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    // 成功返回
    public static <T> ResultEntity<T> success(T data,String message) {
        ResultEntity<T> result = new ResultEntity<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

}
