package xCloud.service.recruitment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import xCloud.dto.recruitment.ResumeParseResult;
import xCloud.entity.recruitment.Resume;
import xCloud.mapper.recruitment.ResumeMapper;
import xCloud.util.LlmJsonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 简历解析服务
 * 核心功能：
 * 1. PDF/Word文件解析 → 纯文本提取
 * 2. LLM结构化信息提取
 * 3. 保存到数据库
 * @author Claude
 * @date 2026-08-18
 */
@Slf4j
@Service
public class ResumeParseService {

    @Resource
    private ResumeMapper resumeMapper;

    /**
     * 事务模板：用于把"简历落库"收进短事务，避免长事务占用数据库连接
     */
    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RecruitmentLlmHelper llmHelper;

    @Value("${resume.upload.dir:/tmp/resumes}")
    private String uploadDir;

    /**
     * 上传并解析简历
     * <p>
     * 事务边界：文件落盘、PDF/Word 解析、LLM 结构化抽取均为耗时 IO（5~30s），
     * 一律放在事务【外】执行；仅最后的"插入简历记录"走短事务。
     * 否则一个请求会长时间占用数据库连接，高并发下直接打满连接池。
     *
     * @param file   简历文件（PDF或Word）
     * @param source 来源（upload、referral等）
     * @return 解析并落库后的简历实体（含自增ID）
     */
    public Resume uploadAndParseResume(MultipartFile file, String source) {
        Resume resume = new Resume();
        resume.setSource(source);
        resume.setParseStatus("parsing");
        resume.setVectorizeStatus("pending");
        resume.setIndexed(0);
        resume.setDeleted(0);
        resume.setCreatedAt(new Date());
        resume.setUpdatedAt(new Date());

        try {
            log.info("开始解析简历：filename={}, size={}", file.getOriginalFilename(), file.getSize());

            // ============1-保存文件到本地（事务外）============
            String filePath = saveFile(file);
            resume.setFilePath(filePath);
            resume.setFileType(getFileExtension(file.getOriginalFilename()));

            // ============2-提取纯文本（事务外）============
            String rawContent = extractText(filePath, resume.getFileType());
            resume.setRawContent(rawContent);

            // ============3-使用LLM进行结构化提取（事务外，耗时最长）============
            ResumeParseResult parseResult = parseWithLLM(rawContent);

            // ============4-填充结构化字段============
            fillResumeFromParseResult(resume, parseResult);
            resume.setParseStatus("success");

            // ============5-短事务落库============
            transactionTemplate.executeWithoutResult(status -> resumeMapper.insert(resume));

            log.info("✅ 简历解析成功：resumeId={}, name={}", resume.getId(), resume.getName());
            return resume;

        } catch (Exception e) {
            log.error("❌ 简历解析失败：filename={}", file.getOriginalFilename(), e);
            // ============失败记录用【独立事务】落库============
            // 若与成功路径共用同一事务，异常回滚会把失败记录一并抹掉，导致失败简历静默消失
            persistFailure(resume, e);
            throw new RuntimeException("简历解析失败：" + e.getMessage(), e);
        }
    }

    /**
     * 用独立事务持久化失败记录，保证失败简历可追溯
     */
    private void persistFailure(Resume resume, Exception e) {
        try {
            String message = e.getMessage() != null ? e.getMessage() : e.toString();
            // 避免超长异常信息撑爆字段
            resume.setParseError(message.length() > 2000 ? message.substring(0, 2000) : message);
            resume.setParseStatus("failed");
            resume.setUpdatedAt(new Date());
            transactionTemplate.executeWithoutResult(status -> resumeMapper.insert(resume));
            log.warn("已记录失败简历：resumeId={}, error={}", resume.getId(), message);
        } catch (Exception ex) {
            // 失败记录落库本身出错不能覆盖原始异常，仅记录日志
            log.error("失败简历记录落库异常：filename={}", resume.getFilePath(), ex);
        }
    }

    /**
     * 保存文件到本地
     */
    private String saveFile(MultipartFile file) throws IOException {
        // 创建上传目录
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + extension;
        Path filePath = Paths.get(uploadDir, filename);

        // 保存文件
        Files.copy(file.getInputStream(), filePath);
        return filePath.toString();
    }

    /**
     * 提取文件纯文本
     */
    private String extractText(String filePath, String fileType) throws IOException {
        if ("pdf".equalsIgnoreCase(fileType)) {
            return extractTextFromPDF(filePath);
        } else if ("docx".equalsIgnoreCase(fileType) || "doc".equalsIgnoreCase(fileType)) {
            return extractTextFromWord(filePath);
        } else {
            throw new IllegalArgumentException("不支持的文件类型：" + fileType);
        }
    }

    /**
     * 从PDF提取文本
     */
    private String extractTextFromPDF(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 从Word提取文本
     */
    private String extractTextFromWord(String filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    /**
     * 使用LLM进行结构化提取（温度 0.1，追求稳定准确）
     */
    private ResumeParseResult parseWithLLM(String rawContent) {
        try {
            String prompt = buildParsePrompt(rawContent);
            // 低温度，更准确；统一走 RecruitmentLlmHelper，避免每次 new Generation()
            String llmResponse = llmHelper.syncChat(prompt, 0.1f, 3000);
            return parseResumeFromLLM(llmResponse);

        } catch (Exception e) {
            log.error("LLM结构化提取失败", e);
            throw new RuntimeException("简历结构化提取失败", e);
        }
    }

    /**
     * 构建简历解析Prompt
     */
    private String buildParsePrompt(String rawContent) {
        return String.format(
                "你是一名专业的简历解析助手，请从以下简历文本中提取结构化信息。\n\n" +
                        "【简历原文】\n%s\n\n" +
                        "【提取要求】\n" +
                        "请严格按照以下JSON格式输出（如果某个字段无法提取，设置为null或空数组）：\n" +
                        "{\n" +
                        "  \"basicInfo\": {\n" +
                        "    \"name\": \"姓名\",\n" +
                        "    \"phone\": \"手机号\",\n" +
                        "    \"email\": \"邮箱\",\n" +
                        "    \"gender\": \"M或F\",\n" +
                        "    \"age\": 年龄数字,\n" +
                        "    \"education\": \"最高学历（本科/硕士/博士）\",\n" +
                        "    \"university\": \"毕业院校\",\n" +
                        "    \"major\": \"专业\",\n" +
                        "    \"workYears\": 工作年限数字,\n" +
                        "    \"currentPosition\": \"当前职位\",\n" +
                        "    \"currentCompany\": \"当前公司\",\n" +
                        "    \"expectedPosition\": \"期望职位\",\n" +
                        "    \"expectedSalary\": 期望薪资数字（单位K）,\n" +
                        "    \"expectedCity\": \"期望城市\"\n" +
                        "  },\n" +
                        "  \"workExperiences\": [\n" +
                        "    {\n" +
                        "      \"company\": \"公司名称\",\n" +
                        "      \"position\": \"职位\",\n" +
                        "      \"startDate\": \"2020-01\",\n" +
                        "      \"endDate\": \"2023-06或至今\",\n" +
                        "      \"description\": \"工作描述\",\n" +
                        "      \"achievements\": [\"成果1\", \"成果2\"]\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"projectExperiences\": [\n" +
                        "    {\n" +
                        "      \"projectName\": \"项目名称\",\n" +
                        "      \"role\": \"担任角色\",\n" +
                        "      \"startDate\": \"2020-01\",\n" +
                        "      \"endDate\": \"2021-06\",\n" +
                        "      \"description\": \"项目描述\",\n" +
                        "      \"technologies\": [\"Java\", \"Spring Boot\"],\n" +
                        "      \"achievements\": [\"成果1\"]\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"educations\": [\n" +
                        "    {\n" +
                        "      \"university\": \"学校名称\",\n" +
                        "      \"major\": \"专业\",\n" +
                        "      \"degree\": \"本科/硕士/博士\",\n" +
                        "      \"startDate\": \"2016-09\",\n" +
                        "      \"endDate\": \"2020-06\"\n" +
                        "    }\n" +
                        "  ],\n" +
                        "  \"skills\": [\"Java\", \"Spring Boot\", \"MySQL\"],\n" +
                        "  \"selfEvaluation\": \"自我评价内容\"\n" +
                        "}",
                rawContent.length() > 4000 ? rawContent.substring(0, 4000) + "..." : rawContent
        );
    }

    /**
     * 解析LLM返回的JSON
     * <p>
     * 统一走 {@link LlmJsonUtil#extractJson(String)} 剥离 Markdown 围栏，避免短文本 substring 越界。
     */
    private ResumeParseResult parseResumeFromLLM(String llmResponse) {
        JSONObject json = JSON.parseObject(LlmJsonUtil.extractJson(llmResponse));

        ResumeParseResult result = new ResumeParseResult();

        // 基本信息
        JSONObject basicInfoJson = json.getJSONObject("basicInfo");
        if (basicInfoJson != null) {
            ResumeParseResult.BasicInfo basicInfo = new ResumeParseResult.BasicInfo();
            basicInfo.setName(basicInfoJson.getString("name"));
            basicInfo.setPhone(basicInfoJson.getString("phone"));
            basicInfo.setEmail(basicInfoJson.getString("email"));
            basicInfo.setGender(basicInfoJson.getString("gender"));
            basicInfo.setAge(basicInfoJson.getInteger("age"));
            basicInfo.setEducation(basicInfoJson.getString("education"));
            basicInfo.setUniversity(basicInfoJson.getString("university"));
            basicInfo.setMajor(basicInfoJson.getString("major"));
            basicInfo.setWorkYears(basicInfoJson.getInteger("workYears"));
            basicInfo.setCurrentPosition(basicInfoJson.getString("currentPosition"));
            basicInfo.setCurrentCompany(basicInfoJson.getString("currentCompany"));
            basicInfo.setExpectedPosition(basicInfoJson.getString("expectedPosition"));
            basicInfo.setExpectedSalary(basicInfoJson.getInteger("expectedSalary"));
            basicInfo.setExpectedCity(basicInfoJson.getString("expectedCity"));
            result.setBasicInfo(basicInfo);
        }

        // 工作经历
        result.setWorkExperiences(json.getObject("workExperiences", List.class));

        // 项目经历
        result.setProjectExperiences(json.getObject("projectExperiences", List.class));

        // 教育经历
        result.setEducations(json.getObject("educations",List.class));

        // 技能
        result.setSkills(json.getObject("skills", List.class));

        // 自我评价
        result.setSelfEvaluation(json.getString("selfEvaluation"));

        return result;
    }

    /**
     * 填充Resume实体
     */
    private void fillResumeFromParseResult(Resume resume, ResumeParseResult parseResult) {
        // 基本信息
        if (parseResult.getBasicInfo() != null) {
            ResumeParseResult.BasicInfo info = parseResult.getBasicInfo();
            resume.setName(info.getName());
            resume.setPhone(info.getPhone());
            resume.setEmail(info.getEmail());
            resume.setGender(info.getGender());
            resume.setAge(info.getAge());
            resume.setEducation(info.getEducation());
            resume.setUniversity(info.getUniversity());
            resume.setMajor(info.getMajor());
            resume.setWorkYears(info.getWorkYears());
            resume.setCurrentPosition(info.getCurrentPosition());
            resume.setCurrentCompany(info.getCurrentCompany());
            resume.setExpectedPosition(info.getExpectedPosition());
            resume.setExpectedSalary(info.getExpectedSalary());
            resume.setExpectedCity(info.getExpectedCity());
        }

        // JSON字段
        resume.setSkills(JSON.toJSONString(parseResult.getSkills()));
        resume.setWorkExperience(JSON.toJSONString(parseResult.getWorkExperiences()));
        resume.setProjectExperience(JSON.toJSONString(parseResult.getProjectExperiences()));
        resume.setEducationHistory(JSON.toJSONString(parseResult.getEducations()));
        resume.setSelfEvaluation(parseResult.getSelfEvaluation());
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
