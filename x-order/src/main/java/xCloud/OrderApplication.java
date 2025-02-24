package xCloud;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
//@MapperScan("xCloud.mapper")
@SpringBootApplication
public class OrderApplication {
    public static void main(String[] args) {
        System.out.println("Hello world! OrderApplication start...");
        SpringApplication.run(OrderApplication.class, args);
        System.out.println("Hello world! OrderApplication done...");
    }

    /**
     * create restTemplate
     */
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}