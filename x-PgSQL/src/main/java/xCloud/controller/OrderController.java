package xCloud.controller;

import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.entity.Order;
import xCloud.entity.request.CreateOrderRequest;
import xCloud.mapper.OrderMapper;
import xCloud.service.OrderService;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/15 20:05
 * @ClassName OrderController
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    @Resource
    private OrderService orderService;

    @Resource
    private OrderMapper orderMapper;
    /**
     * 创建订单，触发完整流程：下单 → 发送Kafka事件 → 资金扣款 → 更新订单状态
     * 测试示例：POST /api/orders
     * Body: {"userId": 123, "amount": 100.00}
     */
    @PostMapping("add")
    public ResponseEntity<String> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            String orderNo = orderService.createOrder(request);
            return ResponseEntity.ok("订单创建成功，订单号: " + orderNo + " (状态: PENDING，等待支付处理)");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("订单创建失败: " + e.getMessage());
        }
    }
    /**
     * 查询订单状态，验证支付流程
     * 测试示例：GET /api/orders/{orderNo}
     * 预期：如果支付成功，返回PAID；否则PENDING或FAILED
     */
    @GetMapping("/{orderNo}")
    public ResponseEntity<Order> getOrderStatus(@PathVariable String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }
}
