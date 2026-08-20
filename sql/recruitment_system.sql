-- ============JD生成与优化相关表============

-- 1. JD岗位描述表
CREATE TABLE `job_description` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT 'JD标题',
    `responsibilities` TEXT COMMENT '岗位职责',
    `requirements` TEXT COMMENT '任职要求',
    `salary_range` VARCHAR(50) COMMENT '薪资范围',
    `location` VARCHAR(100) COMMENT '工作地点',
    `education` VARCHAR(20) COMMENT '学历要求',
    `experience_years` INT COMMENT '工作经验要求（年）',
    `skill_tags` VARCHAR(500) COMMENT '技能标签（逗号分隔）',
    `highlights` TEXT COMMENT '岗位亮点/福利',
    `company_intro` TEXT COMMENT '公司介绍',
    `status` VARCHAR(20) DEFAULT 'draft' COMMENT 'JD状态（draft-草稿，review-待审核，published-已发布，archived-已归档）',
    `version` INT DEFAULT 1 COMMENT '生成版本号',
    `original_requirement` TEXT COMMENT '原始需求输入',
    `feedback_history` TEXT COMMENT 'HR反馈历史（JSON）',
    `generation_strategy` VARCHAR(20) DEFAULT 'standard' COMMENT '生成策略',
    `creator_id` BIGINT COMMENT '创建人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `published_at` DATETIME COMMENT '发布时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    PRIMARY KEY (`id`),
    INDEX `idx_creator_id` (`creator_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='JD岗位描述表';

-- 2. JD反馈表
CREATE TABLE `jd_feedback` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `jd_id` BIGINT NOT NULL COMMENT '关联JD ID',
    `jd_version` INT NOT NULL COMMENT 'JD版本号',
    `feedback_type` VARCHAR(20) NOT NULL COMMENT '反馈类型（adjust-调整，approve-通过，reject-拒绝）',
    `content` TEXT COMMENT '反馈内容',
    `adjust_fields` VARCHAR(200) COMMENT '需要调整的字段',
    `hr_user_id` BIGINT COMMENT 'HR用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_jd_id` (`jd_id`),
    INDEX `idx_hr_user_id` (`hr_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='JD反馈表';

-- ============候选人评分相关表============

-- 3. 候选人评分表
CREATE TABLE `candidate_score` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `candidate_id` BIGINT NOT NULL COMMENT '候选人ID',
    `jd_id` BIGINT NOT NULL COMMENT 'JD ID',
    `total_score` DECIMAL(5,2) NOT NULL COMMENT '总分（0-100）',
    `skill_score` DECIMAL(5,2) COMMENT '技能匹配分',
    `experience_score` DECIMAL(5,2) COMMENT '经验匹配分',
    `education_score` DECIMAL(5,2) COMMENT '学历匹配分',
    `salary_score` DECIMAL(5,2) COMMENT '薪资匹配分',
    `location_score` DECIMAL(5,2) COMMENT '地点匹配分',
    `project_score` DECIMAL(5,2) COMMENT '项目经验分',
    `stability_score` DECIMAL(5,2) COMMENT '稳定性分',
    `match_reason` TEXT COMMENT '匹配理由（可解释性）',
    `risk_analysis` TEXT COMMENT '风险分析',
    `advantages` TEXT COMMENT '优势列表（JSON）',
    `disadvantages` TEXT COMMENT '劣势列表（JSON）',
    `recommendation` VARCHAR(20) DEFAULT 'consider' COMMENT '推荐度（highly_recommend-强烈推荐，recommend-推荐，consider-考虑，not_recommend-不推荐）',
    `hr_preference_applied` TINYINT DEFAULT 0 COMMENT '是否应用了HR偏好',
    `scoring_version` VARCHAR(20) DEFAULT 'v1.0' COMMENT '评分算法版本',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_candidate_jd` (`candidate_id`, `jd_id`),
    INDEX `idx_total_score` (`total_score`),
    INDEX `idx_recommendation` (`recommendation`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='候选人评分表';

-- 4. HR偏好设置表
CREATE TABLE `hr_preference` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `hr_user_id` BIGINT NOT NULL COMMENT 'HR用户ID',
    `jd_id` BIGINT COMMENT 'JD ID（为空表示全局偏好）',
    `preference_name` VARCHAR(100) NOT NULL COMMENT '偏好名称',
    `preference_type` VARCHAR(20) NOT NULL COMMENT '偏好类型（weight-权重，rule-规则，filter-过滤）',
    `preference_value` TEXT NOT NULL COMMENT '偏好值（JSON）',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_hr_user_id` (`hr_user_id`),
    INDEX `idx_jd_id` (`jd_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR偏好设置表';

-- 5. 评分规则表
CREATE TABLE `scoring_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(20) NOT NULL COMMENT '规则类型（dimension-维度，boost-加权，penalty-减分）',
    `dimension` VARCHAR(50) COMMENT '评分维度（skill,experience,education等）',
    `weight` DECIMAL(3,2) DEFAULT 1.0 COMMENT '权重（0-1）',
    `formula` TEXT COMMENT '计算公式',
    `description` TEXT COMMENT '规则描述',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
    `priority` INT DEFAULT 100 COMMENT '优先级（数字越小越优先）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_rule_type` (`rule_type`),
    INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评分规则表';

-- ============插入默认评分规则============
INSERT INTO `scoring_rule` (`rule_name`, `rule_type`, `dimension`, `weight`, `description`, `priority`) VALUES
('技能匹配', 'dimension', 'skill', 0.30, '技能标签匹配度评分', 10),
('工作经验', 'dimension', 'experience', 0.25, '工作年限匹配度评分', 20),
('学历匹配', 'dimension', 'education', 0.15, '学历要求匹配度评分', 30),
('项目经验', 'dimension', 'project', 0.15, '项目经验相关性评分', 40),
('薪资匹配', 'dimension', 'salary', 0.10, '期望薪资合理性评分', 50),
('稳定性', 'dimension', 'stability', 0.05, '工作稳定性评分', 60);
