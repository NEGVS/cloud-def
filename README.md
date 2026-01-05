hello read me

System.out.println("Hello world! OrderApplication start...");
SpringApplication.run(OrderApplication.class, args);
System.out.println("Hello world! OrderApplication done...");
—— OpenAI API Key ————————————————————————————————————

发现refs/heads/kafka违反了存储库规则。 Repository rule violations found for refs/heads/kafka.
Push cannot contain secrets，推送不能包含秘密

若要推送，请从提交中删除secret，或按照此URL允许使用secret。 To push, remove secret from commit(s) or follow this URL to
allow the secret.

本项目版本:: Spring Boot (v3.4.3)

server:
port: 8091
tomcat:
max-threads: 10 #tomcat的最大并发值修改为10,默认是200

# maven官方仓库

https://mvnrepository.com/
mybatis-plus仓库
https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-boot-starter

# ABA

Elasticsearch 8.12.0

Spring Boot 3.4.3 匹配的swagger是什么版本？
springCloud
├── payment-service 支付模块
├── config-service 配置模块
├── order-service 订单模块
├── user-service 用户模块
├── product-service 商品模块 
├── productB-service 商品模块
└── merchant-service 商户模块

springCloud
├── payment-service 支付模块
├── config-service 配置模块
├── order-service 订单模块
├── user-service 用户模块   rabbitMQ
├── product-service 商品模块  kafka,es,redis
├── productB-service 商品模块 ，使用nacos,LangChain4j,milvus,PostgreSQL
├── x-PgSQL Pg模块 ，使用kafka,PostgreSQL,supabase
└── merchant-service 商户模块

技术栈
1-nacos ✅
2-负载均衡Ribbon✅
3-Feign ✅ order模块
4-Sentinel ✅......
5-gateway ✅
6-rabbitMQ、RocketMQ，
7-swagger，✅
8-elasticsearch 8.12.0 ✅ product 模块

9- kafka ✅ product 模块
10- seata
11-grpc  order模块，进行中...



