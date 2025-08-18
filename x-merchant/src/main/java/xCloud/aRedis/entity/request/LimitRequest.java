package xCloud.aRedis.entity.request;

import lombok.Data;

/**
 * @author 樊迎宾
 * @description LimitRequest
 * @date 2025/08/14
 **/

@Data
public class LimitRequest {
    /**
     * b端用户ID
     */
    private Integer bUserId;
    /**
     * C端用户ID
     */
    private Integer cUserId;
}
