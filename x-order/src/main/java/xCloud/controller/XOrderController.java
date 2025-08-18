package xCloud.controller;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import xCloud.feignClient.productClient.Products;
import xCloud.entity.ResultEntity;
import xCloud.entity.XOrders;
import xCloud.feignClient.productClient.ProductClient;
import xCloud.service.XOrdersService;

import java.math.BigDecimal;
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

    @Autowired
    private XOrdersService orderService;

    @Autowired
    private ProductClient productService;

    //DiscoveryClient是专门负责服务注册和发现的，可以通过它获取到注册到注册中心的所有服务
//    @Autowired
//    private PaymentFeignClient paymentClient;

    /**
     * 创建订单并支付
     *
     * @param orderId
     * @param amount
     * @param subject
     * @return
     */
//    public String createOrderAndPay(String orderId, String amount, String subject) {
//        return paymentClient.pay(orderId, amount, subject);
//    }

    /**
     * 更新订单状态--订单服务端点（示例）
     *
     * @param orderId
     * @param status
     * @param transactionId
     * @return
     */
    @PostMapping("/updateStatus")
    public String updateOrderStatus(
            @RequestParam("orderId") String orderId,
            @RequestParam("status") String status,
            @RequestParam("transactionId") String transactionId) {
        // TODO: 更新数据库订单状态
        System.out.println("更新订单: " + orderId + ", 状态: " + status + ", 交易号: " + transactionId);
        return "success"; // 假设更新成功
    }

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
     * 1-create order
     * 根据商品ID，进行创建订单
     *
     * @param pid pid
     * @return order
     */
    @Operation(summary = "create", description = "createOrder")
    @GetMapping("/create/{pid}")
    public XOrders createOrder(@PathVariable("pid") String pid) {
        log.info("\n>>客户下单，调用商品微服务查询商品信息---");
        XOrders order1 = orderService.createOrder(pid);
        log.info("\n>>创建订单成功：{}", JSON.toJSONString(order1));
        return order1;
    }

    /**
     * 1.1-模拟一个高并发的场景问题测试 1
     *
     * @param pid pid
     * @return order
     */
    @Operation(summary = "模拟高并发场景-创建订单", description = "模拟一个高并发的场景问题测试")
    @GetMapping("/create/prod/{pid}")
    public XOrders createOrder2(@PathVariable("pid") String pid) {
        log.info("\n>>客户下单，调用商品微服务查询商品信息---");

        log.info("\n>>----通过fegin调用商品微服务---");
        Products product = productService.findById(pid);

        log.info("\n查询到{}的商品信息内容是{}", pid, JSON.toJSONString(product));

        try {
            log.info("\n>>-模拟网络延迟，一个高并发的场景---sleep 2s---");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 创建订单
        XOrders order = new XOrders();
        order.setOrder_id(IdWorker.getId());
        order.setUser_id(1L);
        order.setMerchant_id(1);

        if (product != null && product.getPrice() != null) {
            order.setAmount(product.getPrice());
        } else {
            order.setAmount(new BigDecimal(12));
        }
        boolean save = orderService.save(order);
        if (save) {
            log.info("\n>>----下单成功---");
            log.info("\norder info:" + JSON.toJSONString(order));
        } else {
            log.info("\n>>----下单失败---");
        }
        return order;
    }

    /**
     * 1.3-模拟一个高并发的场景问题测试 1.2
     *
     * @return
     */
    @GetMapping("/message")
    public String message() {
        return "Hello World,模拟一个高并发的场景问题测试";
    }


    /**
     * 1.4-测试sentinel--/Users/andy_mac/Documents/CodeSpace/andy_softWare/sentinel/sentinel-dashboard-1.8.8.jar
     *
     * @return
     */
    @GetMapping("/message1")
    public String message1() {
        orderService.message();
        return "message1 测试sentinel";
    }

    /**
     * 1.4-测试sentinel
     *
     * @return
     */
    @GetMapping("/message2")
    public String message2() {
        orderService.message();
        return "message2 测试sentinel";
    }

    /**
     * 1.5-测试 sentinel
     *
     * @return
     */
    int i = 0;

    @GetMapping("/message3")
    @SentinelResource(value = "message3",
            blockHandler = "blockHandlerForMessage3",//发生BlockException时进入的方法
            fallback = "fallbackForMessage3")//发生Throwable时进入的方法
    public String message3() {
        i++;
        //异常比例为0.333
        if (i % 3 == 0) {
            throw new RuntimeException();
        }
        return "message3";
    }

    /**
     * 发生BlockException时进入的方法
     */
    public String blockHandlerForMessage3(BlockException blockException) {
        log.error("{}", blockException);
        return "message3 接口被限流或者降级了";
    }

    /**
     * 发生Throwable时进入的方法
     */
    public String fallbackForMessage3(Throwable throwable) {
        log.error("{}", throwable);
        return "message3 接口发生异常了";
    }

    /**
     * 1.6-测试sentinel热点规则
     *
     * @return
     */
    @GetMapping("/message4")
    @SentinelResource("message4")
    //注意这里必须使用这个注解标识,否则热点规则不生效
    public String message4(String name, Integer age) {
        return name + age;
    }

    /**
     * 调用python程序demo
     *
     * @param order order
     * @return String
     */
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
