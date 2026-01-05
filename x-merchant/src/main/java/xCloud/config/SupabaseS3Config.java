package xCloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * @Description 配置 S3Client（创建一个 Config 类）： 后端验证并使用 AWS SDK 将文件上传到 Supabase（Supabase Storage 支持 S3 协议）。优点：隐藏密钥、添加文件验证/压缩、控制权限。
 * @Author Andy Fan
 * @Date 2025/12/24 11:04
 * @ClassName SupabaseS3Config
 */
@Configuration
public class SupabaseS3Config {
    private String accessKey = "7d160bb06bae96b8ace8c08aa9bdbc7e";
    private String secretKey = "94adf05ecc2839e6e1835f960ac499afd80d43cbd8e920a3f1f7ce83fcd80abf";
    private String endpoint = "https://mlbypadufgwqslopkqwy.storage.supabase.co/storage/v1/s3";
    private String region = "us-east-2";

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(region))
                .build();
    }
}
