package xCloud.dto.recruitment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 简历解析结果DTO
 * @author Claude
 * @date 2026-08-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeParseResult {

    /**
     * 基本信息
     */
    private BasicInfo basicInfo;

    /**
     * 工作经历列表
     */
    private List<WorkExperience> workExperiences;

    /**
     * 项目经历列表
     */
    private List<ProjectExperience> projectExperiences;

    /**
     * 教育经历列表
     */
    private List<EducationHistory> educations;

    /**
     * 技能列表
     */
    private List<String> skills;

    /**
     * 自我评价
     */
    private String selfEvaluation;

    /**
     * 原始文本
     */
    private String rawContent;

    /**
     * 基本信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicInfo {
        private String name;
        private String phone;
        private String email;
        private String gender;
        private Integer age;
        private String education;        // 最高学历
        private String university;       // 毕业院校
        private String major;            // 专业
        private Integer workYears;       // 工作年限
        private String currentPosition;  // 当前职位
        private String currentCompany;   // 当前公司
        private String expectedPosition; // 期望职位
        private Integer expectedSalary;  // 期望薪资
        private String expectedCity;     // 期望城市
    }

    /**
     * 工作经历
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkExperience {
        private String company;          // 公司名称
        private String position;         // 职位
        private String startDate;        // 开始时间（yyyy-MM）
        private String endDate;          // 结束时间（yyyy-MM 或 "至今"）
        private String description;      // 工作描述
        private List<String> achievements; // 工作成果
    }

    /**
     * 项目经历
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectExperience {
        private String projectName;      // 项目名称
        private String role;             // 担任角色
        private String startDate;        // 开始时间
        private String endDate;          // 结束时间
        private String description;      // 项目描述
        private List<String> technologies; // 使用技术
        private List<String> achievements; // 项目成果
    }

    /**
     * 教育经历
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationHistory {
        private String university;       // 学校名称
        private String major;            // 专业
        private String degree;           // 学位（本科、硕士、博士）
        private String startDate;        // 开始时间（yyyy-MM）
        private String endDate;          // 结束时间（yyyy-MM）
    }
}
