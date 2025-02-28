 hello read me

System.out.println("Hello world! OrderApplication start...");
SpringApplication.run(OrderApplication.class, args);
System.out.println("Hello world! OrderApplication done...");


1-nacos ✅
2-负载均衡Ribbon,✅
3-Feign ✅
4-Sentinel ✅......
5-gateway ✅
6-rabbitMQ、RocketMQ，

server:
port: 8091
tomcat:
max-threads: 10 #tomcat的最大并发值修改为10,默认是200