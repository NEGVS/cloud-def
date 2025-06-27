package xCloud.tools;

import com.alibaba.nacos.api.config.ConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/27 11:25
 * @ClassName NacosConfigTest
 */
@Slf4j
//@Configuration
public class NacosConfigTest {

    @Resource
    private ConfigService configService;

    @Bean
    public String getNacosConfig() throws Exception {
        String serverStatus = configService.getServerStatus();
        log.info("\n\n\nNacos Server Status: \n" + serverStatus);

        // (String dataId, String group, long timeoutMs)
        String config = configService.getConfig("x-productB.yml", "ANDY_GROUP", 5000);
        log.info("\n\n\nNacos Config: \n" + config);
        return config;
    }


}
