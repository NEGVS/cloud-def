package xCloud.controller.supabase;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/12/24 11:17
 * @ClassName UploadController
 */
@Tag(name = "UploadController", description = "UploadController")
@Slf4j
@RequestMapping("/genAI")
@RestController
public class UploadController {

    private final S3Client s3Client;

    public UploadController(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Value("${supabase.bucket:images}") // Bucket 名称 如果不配置这些值，代码会使用注解中的默认值（images 和 <project-ref>）
    private String bucketName;

    @Value("${supabase.project-ref:<project-ref>}")
    private String projectRef;


    @PostMapping("/api/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            // 生成唯一文件名
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String key = "products/" + fileName; // 可自定义文件夹

            // 上传
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // 生成 public URL（如果 bucket 是 public）
            String publicUrl = String.format("https://%s.supabase.co/storage/v1/object/public/%s/%s",
                    projectRef, bucketName, key);

            Map<String, String> response = new HashMap<>();
            response.put("url", publicUrl);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "上传失败: " + e.getMessage()));
        }
    }
}
