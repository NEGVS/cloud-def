package xCloud.service.impl;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.fastjson.JSON;
import com.alipay.api.domain.Product;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import xCloud.domain.XOrders;
import xCloud.entity.XProducts;
import xCloud.mapper.XOrdersMapper;
import xCloud.service.ProductService;
import xCloud.service.XOrdersService;

import java.util.List;
import java.util.Random;

/**
 * @author andy_mac
 * @description 针对表【x_orders(订单表)】的数据库操作Service实现
 * @createDate 2025-02-21 17:26:18
 */
@Slf4j
@Service
public class XOrdersServiceImpl extends ServiceImpl<XOrdersMapper, XOrders>
        implements XOrdersService {

    @Resource
    RestTemplate restTemplate;
    @Resource
    DiscoveryClient discoveryClient;

    @Resource
    ProductService productService;

    @Override
    public XOrders createOrder(String pid) {


        return createOrder4(pid);
    }

    /**
     * SentinelResource链路流控模式
     */

//    @SentinelResource(value = "message", fallback = "messageFallback")
    @SentinelResource("message")
    @Override
    public void message(){
        log.info("\n>>message--");
    }

    /**
     * 方式5-创建订单-fegin调用商品微服务
     *
     * @param pid 商品ID
     * @return XOrders
     */
    public XOrders createOrder5_feign(String pid) {
        XProducts byId = productService.findById(pid);
        log.info("\n>>客户下单，调用商品微服务查询商品信息---");
        //直接使用微服务名字， 从nacos中获取服务地址
//        List<ServiceInstance> instances = discoveryClient.getInstances("shop-product");
//        int index = new Random().nextInt(instances.size());
//        ServiceInstance serviceInstance = instances.get(index);
//        String host = serviceInstance.getHost();
//        int port = serviceInstance.getPort();
//        String server_name = "shop-product";
//        String url = "http://" + server_name + "/product/find/" + pid;
//        XProducts product = restTemplate.getForObject(url, XProducts.class);
        //通过fegin调用商品微服务
        XProducts product = productService.findById(pid);

        log.info(">>商品信息,查询结果：{}", JSON.toJSONString(product));
        XOrders order = new XOrders();
        order.setOrder_id(IdWorker.getId());
        order.setUser_id(1L);
        order.setMerchant_id(1);
        order.setAmount(product.getPrice());
        return order;
    }

    /**
     * 方式4-创建订单-LoadBalanced负载均衡
     *
     * @param pid 商品ID
     * @return XOrders
     */
    public XOrders createOrder4(String pid) {
        log.info("\n>>客户下单，调用商品微服务查询商品信息---");
        //直接使用微服务名字， 从nacos中获取服务地址
//        List<ServiceInstance> instances = discoveryClient.getInstances("shop-product");
//        int index = new Random().nextInt(instances.size());
//        ServiceInstance serviceInstance = instances.get(index);
//        String host = serviceInstance.getHost();
//        int port = serviceInstance.getPort();
        String server_name = "shop-product";
        String url = "http://" + server_name + "/product/find/" + pid;
        XProducts product = restTemplate.getForObject(url, XProducts.class);

        log.info(">>商品信息,查询结果：{}", JSON.toJSONString(product));
        XOrders order = new XOrders();
        order.setOrder_id(IdWorker.getId());
        order.setUser_id(1L);
        order.setMerchant_id(1);
        order.setAmount(product.getPrice());
        return order;
    }

    /**
     * 方式3-创建订单-负载均衡 从nacos中 随机获取服务地址url
     *
     * @param pid 商品ID
     * @return XOrders
     */
    public XOrders createOrder3(String pid) {
        log.info("\n>>客户下单，调用商品微服务查询商品信息---");
        //从nacos中获取服务地址-自定义规则实现随机挑选服务
        List<ServiceInstance> instances = discoveryClient.getInstances("shop-product");
        int index = new Random().nextInt(instances.size());
        ServiceInstance serviceInstance = instances.get(index);
        String host = serviceInstance.getHost();
        int port = serviceInstance.getPort();
        String url = "http://" + host + ":" + port + "/product/find/" + pid;
        XProducts product = restTemplate.getForObject(url, XProducts.class);

        log.info(">>商品信息,查询结果：{}", JSON.toJSONString(product));
        XOrders order = new XOrders();
        order.setOrder_id(IdWorker.getId());
        order.setUser_id(1L);
        order.setMerchant_id(1);
        order.setAmount(product.getPrice());
        return order;
    }

    /**
     * 方式2-创建订单-从nacos中获取服务地址url
     *
     * @param pid 商品ID
     * @return XOrders
     */
    public XOrders createOrder2(String pid) {

        log.info("\n>>客户下单，调用商品微服务查询商品信息---");
        //从nacos中获取服务地址
        ServiceInstance serviceInstance = discoveryClient.getInstances("shop-prduct").get(0);

        String host = serviceInstance.getHost();
        int port = serviceInstance.getPort();

        String url = "http://" + host + ":" + port + "/product/find/" + pid;
        log.info("-----get url from nacos：{}", url);
        //通过restTemplate调用商品微服务
        XProducts product = restTemplate.getForObject(url, XProducts.class);
        log.info(">>商品信息,查询结果：{}", JSON.toJSONString(product));
        XOrders order = new XOrders();
        order.setOrder_id(IdWorker.getId());
        order.setUser_id(1L);
        order.setMerchant_id(1);
        order.setAmount(product.getPrice());
        return order;
    }

    /**
     * 方式1-创建订单-restTemplate-固定url
     *
     * @param pid 商品ID
     * @return XOrders
     */
    public XOrders createOrder1(String pid) {

        //method 1,use restTemplate to call product service
        XProducts product = restTemplate.getForObject("http://localhost:8072/product/find/" + pid, XProducts.class);

        log.info(">>商品信息,查询结果：{}", JSON.toJSONString(product));
        XOrders order = new XOrders();
        order.setOrder_id(IdWorker.getId());
        order.setUser_id(1L);
        order.setMerchant_id(1);
        order.setAmount(product.getPrice());
        return order;
    }
}




