package xCloud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xCloud.entity.XOrders;

/**
 * @author andy_mac
 * @description 针对表【x_orders(订单表)】的数据库操作Service
 * @createDate 2025-02-21 17:26:18
 */
public interface XOrdersService extends IService<XOrders> {
    /**
     * 根据商品ID，进行创建订单
     *
     * @param pid
     * @return
     */
    XOrders createOrder(String pid);

    /**
     *  测试sentinel
     */
    void message();
}
