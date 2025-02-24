package xCloud.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import xCloud.domain.XOrders;
import xCloud.entity.XProducts;
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
public class XOrderController {
    @Resource
    private XOrdersService orderService;

    //   DiscoveryClient是专门负责服务注册和发现的，我们可以通过它获取到注册到注册中心的所有服务

    /**
     * 根据商品id查询商品信息
     *
     * @param id id
     * @return order
     */
    @GetMapping("/find/{id}")
    public XOrders findByPid(@PathVariable("id") String id) {
        log.info("查询商品信息");

        XOrders order = orderService.getById(id);
        log.info("查询商品信息：{}", JSON.toJSONString(order));
        return order;
    }

    /**
     * create order
     *
     * @param pid pid
     * @return order
     */
    @GetMapping("/create/{pid}")
    public XOrders createOrder(@PathVariable("pid") String pid) {
        log.info(">>客户下单，调用商品微服务查询商品信息---");
        XOrders order1 = orderService.createOrder(pid);
        return order1;
    }

}
