package xCloud.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.entity.PageRequest;
import xCloud.entity.Result;
import xCloud.entity.XProductsB;
import xCloud.service.XProductsBService;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/2/21 14:11
 * @ClassName ProductController
 */
@Tag(name = "Product Management", description = "APIs for managing products")
@RestController
@Slf4j
@RequestMapping("/product")
public class XProductController {

    @Resource
    XProductsBService xProductsService;

    @GetMapping("/find/{id}")
    public XProductsB findGet(@PathVariable("id") String id) {
        XProductsB productById = xProductsService.getProductById(Long.valueOf(id));
        return productById;
    }

    @Operation(
            summary = "Search products with pagination",
            description = "Retrieve a paginated list of products based on search criteria",
//            requestBody = @RequestBody(description = "Search parameters including pagination and product filters",
//                    required = true, content = @Content(schema = @Schema(implementation = PageRequest.class))
//            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful retrieval of products",
                            content = @Content(schema = @Schema(implementation = Result.class))
                    ),
                    @ApiResponse(
                            responseCode = "444",
                            description = "Invalid request parameters",
                            content = @Content
                    )
            }
    )
    @PostMapping("/find")
    public Result<Page<XProductsB>> findPost(@RequestBody PageRequest<XProductsB> request) {
        log.info("Search request for products: {}, page: {}, size: {}",
                request.getData(), request.getCurrentPage(), request.getPageSize());
        // Initialize pagination parameters
        Page<XProductsB> page = new Page<>(
                request.getCurrentPage() != null ? request.getCurrentPage() : 1,
                request.getPageSize() != null ? request.getPageSize() : 10
        );
        // Build query conditions
        LambdaQueryWrapper<XProductsB> queryWrapper = new LambdaQueryWrapper<>();
        // Execute paginated query
        Page<XProductsB> resultPage = xProductsService.page(page, queryWrapper);
        return Result.success(resultPage);
    }
}
