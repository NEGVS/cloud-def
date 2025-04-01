package xCloud.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.domain.XOrders;
import xCloud.entity.XProducts;
import xCloud.service.ProductService;
import xCloud.service.XOrdersService;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/2/17 16:51
 * @ClassName OrderController
 */
@RestController
@Slf4j
@RequestMapping("/order")
@Tag(name = "订单服务", description = "订单服务")
public class XOrderController {

    @Resource
    private XOrdersService orderService;

    @Resource
    private ProductService productService;

    //   DiscoveryClient是专门负责服务注册和发现的，我们可以通过它获取到注册到注册中心的所有服务

    /**
     * 根据商品id查询商品信息
     *
     * @param id id
     * @return order
     */
    @Operation(summary = "Get user by ID", description = "Returns a single user based on their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found", content = @Content(schema = @Schema(implementation = XOrders.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @Parameters(value = {
            @Parameter(name = "id", description = "用户ID", required = false),
            @Parameter(name = "name", description = "商品名称", required = false)
    })
    @GetMapping("/find/{id}")
    public ResponseEntity<XOrders> findByPid(@PathVariable("id") String id) {
        log.info("\n查询商品信息");
        XOrders order = orderService.getById(id);
        log.info("\n查询商品信息：{}", JSON.toJSONString(order));
        return ResponseEntity.ok().body(order);
    }

    /**
     * create order
     *
     * @param pid pid
     * @return order
     */
    @GetMapping("/create/{pid}")
    public XOrders createOrder(@PathVariable("pid") String pid) {
        log.info("\n>>客户下单，调用商品微服务查询商品信息---");
        XOrders order1 = orderService.createOrder(pid);
        return order1;
    }

    /**
     * create order by Fegin
     *
     * @param pid pid
     * @return order
     */
    @GetMapping("/create/fegin/{pid}")
    public XOrders createOrder2(@PathVariable("pid") String pid) {
        log.info("\n>>客户下单，调用商品微服务查询商品信息---");
        log.info("\n>>----通过fegin调用商品微服务---");
        XProducts product = productService.findById(pid);

        log.info("\n查询结果: " + JSON.toJSONString(product));
        try {
            log.info("\n>>-模拟一个高并发的场景---sleep 2s---");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        XOrders order = new XOrders();
        order.setOrder_id(IdWorker.getId());
        order.setUser_id(1L);
        assert product != null;
        //System.out.println( "------product.getProductId() = " + product.getProductId() );
        order.setMerchant_id(1);
        order.setAmount(product.getPrice());
        boolean save = orderService.save(order);
        if (save) {
            log.info("\n>>----下单成功---");
            log.info("\norder info:" + JSON.toJSONString(order));
        } else {
            log.info("\n>>----下单失败---");
        }
        return order;
    }

    @GetMapping("/message")
    public String message() {
        return "Hello World,模拟一个高并发的场景问题测试";
    }


    @GetMapping("/message2")
    public String message2() {
        return "Hello World22222,模拟一个高并发的场景问题测试";
    }

}
