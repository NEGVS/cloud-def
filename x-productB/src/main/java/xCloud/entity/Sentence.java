package xCloud.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/16 17:27
 * @ClassName Sentence
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Sentence {
    private Long id;
    private String content;
    private float[] vector;
}
