package xcloud.xproduct.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xcloud.xproduct.config.kafka.KafkaProducer;
import xcloud.xproduct.domain.XProducts;
import xcloud.xproduct.service.XProductsService;

import java.net.http.HttpResponse;
import java.util.List;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/2/21 14:11
 * @ClassName ProductController
 */
@RestController
@Slf4j
@RequestMapping("/product")
@Tag(name = "商品服务", description = "商品服务")
public class XProductController {

    @Resource
    XProductsService xProductsService;
    @Resource
    KafkaProducer kafkaProducer;

    @GetMapping("/find/{id}")
    public XProducts find(@PathVariable("id") String id) {

        XProducts productById = xProductsService.getProductById(Long.valueOf(id));

        return productById;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "查询失败")
    })
    @Operation(summary = "查询商品列表", description = "查询商品列表")
    @Parameters({
            @Parameter(name = "product_id", description = "商品ID", required = false),
            @Parameter(name = "name", description = "商品名称", required = false)
    })
    @PostMapping("/find/list")
    public HttpRestResult<List<XProducts>> selectList(@RequestBody XProducts xProducts) {

        QueryWrapper<XProducts> queryWrapper = new QueryWrapper<>();
        //sendMessage
        String message = "andy";
        if (ObjectUtil.isNotEmpty(xProducts.getName())) {
            message = "andy" + xProducts.getName();
        }
        //sendMessage
        kafkaProducer.sendMessage(message);

        if (xProducts.getProduct_id() != null) {
            queryWrapper.eq("product_id", xProducts.getProduct_id());
        }
        if (xProducts.getName() != null) {
            queryWrapper.like("name", xProducts.getName());
        }
        queryWrapper.orderByDesc("created_time");
        List<XProducts> list = xProductsService.list(queryWrapper);
        log.info("------------");
        log.info(JSONUtil.toJsonStr(list));
        HttpRestResult<List<XProducts>> httpRestResult2 = new HttpRestResult<>();
        httpRestResult2.setData(list);
        httpRestResult2.setCode(200);
        httpRestResult2.setMessage("查询成功");
        return httpRestResult2;
    }
}
