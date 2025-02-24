package xCloud.service;

import com.baomidou.mybatisplus.extension.service.IService;
import xCloud.domain.XOrders;

/**
 * @author andy_mac
 * @description 针对表【x_orders(订单表)】的数据库操作Service
 * @createDate 2025-02-21 17:26:18
 */
public interface XOrdersService extends IService<XOrders> {

    XOrders createOrder(String pid);
}
