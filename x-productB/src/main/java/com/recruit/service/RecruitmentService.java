package com.recruit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class RecruitmentService {

    @Autowired
    private IntentService intentService;

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private LLMService llmService;

    /**
     * 主流程:处理用户输入
     */
    public String handleUserInput(String userInput) {
        // 1. 意图识别
        boolean isJobRelated = intentService.isJobIntent(userInput);

        if (!isJobRelated) {
            // 非求职问题,直接对话
            return llmService.chat(userInput, null);
        }

        // 2. 聚类获取职位分类ID
        Integer categoryId = clusterService.getJobCategory(userInput);

        // 3. 向量检索匹配职位
        List<Map<String, Object>> jobs = vectorSearchService.searchJobs(userInput, categoryId);

        // 4. 判断是否需要返回岗位(相似度阈值)
        boolean shouldReturnJobs = jobs.stream()
            .anyMatch(job -> (float) job.get("score") > 0.75f);

        // 5. LLM生成回复
        return llmService.chat(userInput, shouldReturnJobs ? jobs : null);
    }
}
