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
    @GetMapping("/find/{id}")
    public XOrders findByPid(@PathVariable("id") String id) {
        log.info("\n查询商品信息");
        XOrders order = orderService.getById(id);
        log.info("\n查询商品信息：{}", JSON.toJSONString(order));
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
//        白领职场女性我见的开的比较多的是奔驰、宝马、保时捷、蔚来、特斯拉，智己，小米su7还没见过

//        小米su7感觉白领女生开的不多，大学刚毕业的女生买不起，赚几年钱能买的起的也不会自己去买车，要结婚了是基本是要求男方有车，自己很少主动给自己买车。
//        所以大多是那些早早在社会打拼并且脱颖而出经济独立的女性，做生意 服装 美容 餐饮 直播赚些钱的，富家千金我看到的基本也是特斯拉要么更贵的豪车
//        过年相亲我就认识一个这样的女生，自己开的店，化妆化的不像正常人，猛喷我的特斯拉，非常喜欢小米su7，我直接让她滚了
//
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
