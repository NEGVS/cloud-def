package xCloud.service.impl;

import com.alibaba.fastjson.JSON;
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

    @Override
    public XOrders createOrder(String pid) {

//        method 4 ,use load balance
        //直接使用微服务名字， 从nacos中获取服务地址
        String serviceName = "shop-product";
        String url4 = "http://" + serviceName + "/product/find/" + pid;

        //end
        //method 3,use random service
        List<ServiceInstance> instances = discoveryClient.getInstances("shop-product");
        int index = new Random().nextInt(instances.size());
        ServiceInstance serviceInstance = instances.get(index);

        //method 3 end

        //method 2 ,use nacos
        //get url from nacos
        //ServiceInstance serviceInstance = discoveryClient.getInstances("shop-product").get(0);
        String host = serviceInstance.getHost();
        int port = serviceInstance.getPort();

        String url = "http://" + serviceInstance.getHost() + ":" + serviceInstance.getPort() + "/product/find/" + pid;
        //UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUri(URI.create(url));

        System.out.println(url4);
        XProducts product = restTemplate.getForObject(url4, XProducts.class);
        log.info("-----get url from nacos：{}", url);
        //method 2 end

        //method 1,use restTemplate to call product service
        //XProducts product = restTemplate.getForObject("http://localhost:8072/product/find/" + pid, XProducts.class);
        //method 1 end

        log.info(">>商品信息,查询结果：{}", JSON.toJSONString(product));
        XOrders order = new XOrders();
        order.setOrder_id(IdWorker.getId());
        order.setUser_id(1L);
        assert product != null;
        //System.out.println( "------product.getProductId() = " + product.getProductId() );
        //order.setMerchantId( 1L );
        //order.setAmount( product.getPrice() );
        return order;
    }
}




