package xCloud.entity.recruitment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 简历实体
 * @author Claude
 * @date 2026-08-18
 */
@Data
@Accessors(chain = true)
@TableName("resume")
public class Resume {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 候选人姓名
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别（M-男，F-女）
     */
    private String gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 学历（本科、硕士、博士）
     */
    private String education;

    /**
     * 毕业院校
     */
    private String university;

    /**
     * 专业
     */
    private String major;

    /**
     * 工作年限
     */
    private Integer workYears;

    /**
     * 当前职位
     */
    private String currentPosition;

    /**
     * 当前公司
     */
    private String currentCompany;

    /**
     * 期望职位
     */
    private String expectedPosition;

    /**
     * 期望薪资（单位：K/月）
     */
    private Integer expectedSalary;

    /**
     * 期望城市
     */
    private String expectedCity;

    /**
     * 技能标签（JSON数组，如：["Java","Spring Boot"]）
     */
    private String skills;

    /**
     * 工作经历（JSON数组）
     */
    private String workExperience;

    /**
     * 项目经历（JSON数组）
     */
    private String projectExperience;

    /**
     * 教育经历（JSON数组）
     */
    private String educationHistory;

    /**
     * 自我评价
     */
    private String selfEvaluation;

    /**
     * 简历原文（纯文本）
     */
    private String rawContent;

    /**
     * 简历文件路径（OSS/本地）
     */
    private String filePath;

    /**
     * 文件类型（pdf、docx）
     */
    private String fileType;

    /**
     * 简历来源（upload-上传，crawler-爬虫，referral-内推）
     */
    private String source;

    /**
     * 解析状态（pending-待解析，parsing-解析中，success-成功，failed-失败）
     */
    private String parseStatus;

    /**
     * 解析失败原因
     */
    private String parseError;

    /**
     * 向量化状态（pending-待向量化，success-成功，failed-失败）
     */
    private String vectorizeStatus;

    /**
     * 是否已索引到ES
     */
    private Integer indexed;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 是否删除
     */
    private Integer deleted;
}
