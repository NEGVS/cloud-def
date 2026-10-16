package xCloud.dto.recruitment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xCloud.entity.recruitment.Resume;

/**
 * 简历出参 VO
 * <p>
 * 相比实体 {@link Resume}：剔除大字段 rawContent（简历原文）与逻辑删除标记，
 * 并补充上传耗时、提示信息等接口层字段，避免直接暴露底层结构。
 *
 * @author Claude
 * @date 2026-10-12
 */
@Data
@Schema(description = "简历信息")
public class ResumeVO {

    @Schema(description = "简历ID")
    private Long id;

    @Schema(description = "候选人姓名")
    private String name;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别（M-男，F-女）")
    private String gender;

    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "学历")
    private String education;

    @Schema(description = "毕业院校")
    private String university;

    @Schema(description = "专业")
    private String major;

    @Schema(description = "工作年限")
    private Integer workYears;

    @Schema(description = "当前职位")
    private String currentPosition;

    @Schema(description = "当前公司")
    private String currentCompany;

    @Schema(description = "期望职位")
    private String expectedPosition;

    @Schema(description = "期望薪资（K/月）")
    private Integer expectedSalary;

    @Schema(description = "期望城市")
    private String expectedCity;

    @Schema(description = "技能标签（JSON数组字符串）")
    private String skills;

    @Schema(description = "工作经历（JSON数组字符串）")
    private String workExperience;

    @Schema(description = "项目经历（JSON数组字符串）")
    private String projectExperience;

    @Schema(description = "教育经历（JSON数组字符串）")
    private String educationHistory;

    @Schema(description = "自我评价")
    private String selfEvaluation;

    @Schema(description = "简历文件路径")
    private String filePath;

    @Schema(description = "文件类型（pdf、docx）")
    private String fileType;

    @Schema(description = "简历来源（upload-上传，crawler-爬虫，referral-内推）")
    private String source;

    @Schema(description = "解析状态（pending、parsing、success、failed）")
    private String parseStatus;

    @Schema(description = "解析失败原因")
    private String parseError;

    @Schema(description = "向量化状态（pending、success、failed）")
    private String vectorizeStatus;

    @Schema(description = "是否已索引到ES")
    private Integer indexed;

    /**
     * 上传解析耗时（毫秒），仅上传接口回填
     */
    @Schema(description = "解析耗时（毫秒）")
    private Long elapsedMs;

    /**
     * 提示信息，仅上传接口回填
     */
    @Schema(description = "提示信息")
    private String message;

    /**
     * 由实体转换（不拷贝 rawContent / deleted）
     */
    public static ResumeVO from(Resume resume) {
        if (resume == null) {
            return null;
        }
        ResumeVO vo = new ResumeVO();
        vo.setId(resume.getId());
        vo.setName(resume.getName());
        vo.setPhone(resume.getPhone());
        vo.setEmail(resume.getEmail());
        vo.setGender(resume.getGender());
        vo.setAge(resume.getAge());
        vo.setEducation(resume.getEducation());
        vo.setUniversity(resume.getUniversity());
        vo.setMajor(resume.getMajor());
        vo.setWorkYears(resume.getWorkYears());
        vo.setCurrentPosition(resume.getCurrentPosition());
        vo.setCurrentCompany(resume.getCurrentCompany());
        vo.setExpectedPosition(resume.getExpectedPosition());
        vo.setExpectedSalary(resume.getExpectedSalary());
        vo.setExpectedCity(resume.getExpectedCity());
        vo.setSkills(resume.getSkills());
        vo.setWorkExperience(resume.getWorkExperience());
        vo.setProjectExperience(resume.getProjectExperience());
        vo.setEducationHistory(resume.getEducationHistory());
        vo.setSelfEvaluation(resume.getSelfEvaluation());
        vo.setFilePath(resume.getFilePath());
        vo.setFileType(resume.getFileType());
        vo.setSource(resume.getSource());
        vo.setParseStatus(resume.getParseStatus());
        vo.setParseError(resume.getParseError());
        vo.setVectorizeStatus(resume.getVectorizeStatus());
        vo.setIndexed(resume.getIndexed());
        return vo;
    }
}
