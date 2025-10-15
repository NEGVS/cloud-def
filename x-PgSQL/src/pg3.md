### 测试Controller实现

为了测试完整的订单-支付流程，我设计了一个简单的REST Controller（`OrderController`）。它提供两个端点：
- **POST /api/orders**：创建订单，触发整个流程（订单插入 → Kafka发送 → 资金扣款 → 订单状态更新）。
- **GET /api/orders/{orderNo}**：查询订单状态，验证支付是否成功（PENDING → PAID）。

**注意**：
- 该Controller依赖`OrderService`、`OrderMapper`（用于查询）。
- 测试时，确保Kafka、PostgreSQL、Redis运行正常。创建订单后，可通过GET查询状态（需等待Kafka异步处理，实际测试中可添加延迟或轮询）。
- 假设项目包为`xCloud.controller`，实体/请求类已定义（如`CreateOrderRequest`、`Order`）。
- 异常处理简化，使用全局异常处理器或直接返回。

#### 完整Controller代码（OrderController.java）

```java
package xCloud.controller;

import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xCloud.entity.Order;
import xCloud.entity.request.CreateOrderRequest;
import xCloud.service.OrderService;
import xCloud.mapper.OrderMapper;

/**
 * @Description: 订单测试Controller
 * @Author: Andy Fan
 * @Date: 2025/10/15
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
    @PostMapping
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
```

#### 使用说明与测试步骤
1. **启动应用**：确保Spring Boot应用启动，Kafka主题（`order-paid-topic`、`payment-success-topic`、`payment-failed-topic`）存在，数据库有测试用户资金记录（e.g., INSERT INTO funds (user_id, balance) VALUES (123, 1000.00);）。
2. **创建订单**（使用Postman或curl）：
   ```
   curl -X POST http://localhost:8080/api/orders \
   -H "Content-Type: application/json" \
   -d '{"userId": 123, "amount": 100.00}'
   ```
   响应：`订单创建成功，订单号: xxx (状态: PENDING，等待支付处理)`
3. **查询订单**（等待5-10秒，让Kafka处理）：
   ```
   curl -X GET http://localhost:8080/api/orders/xxx
   ```
   响应示例（JSON）：
   ```json
   {
     "id": 1,
     "orderNo": "xxx",
     "userId": 123,
     "amount": 100.00,
     "status": "PAID",  // 成功后变为PAID
     "version": 2,
     "createdAt": "2025-10-15T17:30:00",
     "updatedAt": "2025-10-15T17:31:00"
   }
   ```
4. **异常测试**：
    - 余额不足：amount=2000，预期FundsService抛异常，订单状态保持PENDING，日志更新为FAILED。
    - 并发测试：多线程调用创建订单，验证乐观锁（version）冲突处理。
5. **日志监控**：查看应用日志（FundsService的log.info/error），确认Kafka事件流动和补偿。

此Controller简单高效，便于集成测试（如JUnit + MockMvc模拟Kafka）。如果需要添加认证、更多端点（如模拟支付失败）或Swagger文档，请提供细节！
