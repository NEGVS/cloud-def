package xCloud.dto.recruitment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批量操作结果 VO
 * <p>
 * 供批量上传简历 / 批量评分等接口统一返回：总量、成功数、失败数、耗时以及失败明细。
 *
 * @author Claude
 * @date 2026-10-12
 */
@Data
@Schema(description = "批量操作结果")
public class BatchResultVO {

    @Schema(description = "总条数")
    private Integer total;

    @Schema(description = "成功数")
    private Integer successCount;

    @Schema(description = "失败数")
    private Integer failCount;

    @Schema(description = "总耗时（毫秒）")
    private Long elapsedMs;

    @Schema(description = "失败明细")
    private List<FailItem> failures;

    /**
     * 失败明细项
     */
    @Data
    @Schema(description = "批量操作失败明细")
    public static class FailItem {

        @Schema(description = "标识（如文件名、候选人ID）")
        private String name;

        @Schema(description = "失败原因")
        private String error;

        public FailItem() {
        }

        public FailItem(String name, String error) {
            this.name = name;
            this.error = error;
        }
    }
}
