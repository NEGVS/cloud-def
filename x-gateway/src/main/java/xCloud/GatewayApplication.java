package xCloud;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {MybatisPlusAutoConfiguration.class, DataSourceAutoConfiguration.class})
public class GatewayApplication {
    public static void main(String[] args) {
        System.out.println("Hello world! GatewayApplication start...----------------------");
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("Hello world! GatewayApplication done...----------------------------");

    }
}