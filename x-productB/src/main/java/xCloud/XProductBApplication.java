package xCloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("xCloud.mapper")
public class XProductBApplication {

    public static void main(String[] args) {
        System.out.println("x-product-B start...");
        SpringApplication.run(XProductBApplication.class, args);
        System.out.println("x-product-B done...");

    }

}
