package xCloud.entity.recruitment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * HR反馈记录
 * 用途：记录HR对AI生成JD的反馈，用于迭代优化
 * @author Claude
 * @date 2026-08-18
 */
@Data
@Accessors(chain = true)
@TableName("jd_feedback")
public class JDFeedback {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的JD ID
     */
    private Long jdId;

    /**
     * JD版本号
     */
    private Integer jdVersion;

    /**
     * 反馈类型（adjust-调整优化，approve-通过，reject-拒绝）
     */
    private String feedbackType;

    /**
     * 反馈内容（HR的具体意见）
     */
    private String content;

    /**
     * 需要调整的字段（responsibilities,requirements,salary等）
     */
    private String adjustFields;

    /**
     * HR用户ID
     */
    private Long hrUserId;

    /**
     * 创建时间
     */
    private Date createdAt;
}
