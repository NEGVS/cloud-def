package xCloud.entity;

import cn.hutool.json.JSONUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/29 10:01
 * @ClassName Order
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
class Order {
    private String id;
    private String product;
    private double price;
}

// 生产者线程：模拟添加订单
class OrderProducer implements Runnable {

    private final BlockingQueue<Order> queue;

    private final int numOrders;

    public OrderProducer(BlockingQueue<Order> queue, int numOrders) {
        this.queue = queue;
        this.numOrders = numOrders;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < numOrders; i++) {
                Order order = new Order("Order-" + i, "Product-" + i, Math.random() * 100);
                queue.put(order);// 将订单放入队列,阻塞添加，线程安全
                System.out.println("queue size : " + queue.size());
                System.out.println("\n\n-----OrderProducer-----" + Thread.currentThread().getName() + " 生产订单: " + JSONUtil.toJsonStr(order));
                Thread.sleep(50);  // 模拟生产延迟
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Producer interrupted");
        }
    }
}

// 消费者线程：模拟处理订单
class OrderConsumer implements Runnable {
    private final BlockingQueue<Order> queue;

    public OrderConsumer(BlockingQueue<Order> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.println("===消费者  queue size : " + queue.size());
                Order order = queue.take();// 从队列中获取订单，阻塞获取，线程安全
                System.out.println(Thread.currentThread().getName() + " 处理订单: " + JSONUtil.toJsonStr(order));
                Thread.sleep(100);  // 模拟处理延迟
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Consumer interrupted");
        }
    }
}

// 主类：启动生产者和消费者
public class ThreadSafeOrderQueue {
    public static void main(String[] args) {
        //创建线程安全的订单队列，无界队列
        BlockingQueue<Order> orderQueue = new LinkedBlockingDeque<>();
        //创建线程池
        ExecutorService executor = Executors.newFixedThreadPool(3);//1 生产者，2消费者
        System.out.println(Thread.currentThread().getName() + " name:0000 ");

        //启动生产者，生产5个订单
        executor.submit(new OrderProducer(orderQueue, 0));

        //启动2个消费者，处理订单
        executor.submit(new OrderConsumer(orderQueue));
//        executor.submit(new OrderConsumer(orderQueue));
        System.out.println(Thread.currentThread().getName() + " name:1111 ");
        System.out.println(orderQueue.size());


        for (int i = 0; i < 10; i++) {
            System.out.println("*************消费者 size : " + orderQueue.size());
        }

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println(Thread.currentThread().getName() + " name:222 end ");
        System.out.println("-----------Main thread finished");
        executor.shutdown();
    }
}