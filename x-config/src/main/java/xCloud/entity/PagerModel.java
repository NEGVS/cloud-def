package xCloud.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 分页
 **/
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PagerModel implements Serializable {
    /**
     * 获取当前页
     */
    private long current;
    /**
     * 当前页显示几条数据
     */

    private long size;
    /**
     * 当前页是否有下一页
     */

    private boolean hasNext;
    /**
     * 当前页是否有上一页
     */

    private boolean hasPrevious;

    public PagerModel() {
    }
}

