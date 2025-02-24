package xcloud.xproduct;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("xcloud.xproduct.mapper")
public class XProductApplication {

    public static void main(String[] args) {
        System.out.println("x-product start...");
        SpringApplication.run(XProductApplication.class, args);
        System.out.println("x-product done...");

    }

}
