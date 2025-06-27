package xCloud.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/26 17:31
 * @ClassName PageRequest
 */
@Schema(description = "Pagination request with product search criteria")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageRequest<T> {

    @Schema(description = "Current page number (1-based)", example = "1")
    private Integer currentPage;

    @Schema(description = "Number of items per page", example = "10")
    private Integer pageSize;

    @Schema(description = "Product search criteria")
    private T data;
}
