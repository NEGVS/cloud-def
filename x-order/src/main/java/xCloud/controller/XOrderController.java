package xCloud.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import xCloud.domain.XOrders;
import xCloud.entity.ResultEntity;
import xCloud.entity.XProducts;
import xCloud.service.ProductService;
import xCloud.service.XOrdersService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     */
    @Operation(summary = "listPage", description = "listPage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "success", content = @Content(schema = @Schema(implementation = XOrders.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @Parameters(value = {
            @Parameter(name = "id", description = "用户ID", required = false),
            @Parameter(name = "name", description = "商品名称", required = false)
    })
    @PostMapping("/list")
    public ResultEntity<List<XOrders>> list(@RequestBody XOrders order) {
        if (ObjectUtil.isNotNull(order)) {
            log.info("\n查询商品信息");
            //            Page<XOrders> page = new Page<>(order.getCurrent(), order.getSize());
            QueryWrapper<XOrders> wrapper = new QueryWrapper<>();
            //        wrapper.eq("age", age); // 条件：年龄等于指定值
            List<XOrders> list = orderService.list(wrapper);
            List<XOrders> xOrders = list.subList(0, 10);
            log.info("\n查询商品信息：{}", JSON.toJSONString(xOrders));
            log.info("\n查询商品信息：{}", JSON.toJSONString(ResultEntity.success(xOrders)));
            return ResultEntity.success(xOrders);
        }
        return ResultEntity.error("查询失败");
    }

    /**
     * 根据商品id查询商品信息
     *
     * @param id id
     * @return order
     */
    @Operation(summary = "findByPid", description = "Returns a single user based on their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found", content = @Content(schema = @Schema(implementation = XOrders.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @Parameters(value = {
            @Parameter(name = "id", description = "用户ID", required = false),
            @Parameter(name = "name", description = "商品名称", required = false)
    })
    @PostMapping("/find/{id}")
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
    @Operation(summary = "create", description = "createOrder")
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
    @Operation(summary = "Get user by ID", description = "Returns a single user based on their ID")
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


    @Operation(summary = "flask", description = "flask java Python")
    @PostMapping("/flask")
    public String message2(@RequestBody XOrders order) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:8088/add";
        if (order == null) {
            return "参数为空";
        }
        // 构造请求体
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Integer> map = new HashMap<>();
        map.put("a", order.getA() != null ? order.getA() : 0);
        map.put("b", order.getB() != null ? order.getB() : 0);

//        String json = "{\"a\": 99, \"b\": 222}";
        String json = JSON.toJSONString(map);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        // 发送请求
        String response = restTemplate.postForObject(url, entity, String.class);
        System.out.println("响应: " + response);
        return response;
    }

}
