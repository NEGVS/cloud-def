package xCloud.aRedis.entity.vo;

import lombok.Data;

/**
 * @author 樊迎宾
 * @description LimitVo
 * @date 2025/08/14
 **/
@Data
public class LimitVo {

    /**
     * B联系C次数限制 1：不限制 0：限制
     */
    private Integer isLimit;
}
