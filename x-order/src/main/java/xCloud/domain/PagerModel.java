package xCloud.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;


@Schema(name = "PagerModel", description = "PagerModel分页")
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PagerModel implements Serializable {

    @Schema(name = "current", description = "当前页")
    private long current;

    @Schema(name = "size", description = "当前页显示几条数据")
    private long size;

    //不在swagger文档中显示
    @Hidden
    @Schema(name = "hasNext", description = "当前页是否有下一页")
    private boolean hasNext;

    @Hidden
    @Schema(name = "hasPrevious", description = "当前页是否有上一页")
    private boolean hasPrevious;

    @Hidden
    @Schema(name = "startDate", description = "startDate")
    private String startDate;

    @Hidden
    @Schema(name = "endDate", description = "endDate")
    private String endDate;

    public PagerModel() {
    }
}

