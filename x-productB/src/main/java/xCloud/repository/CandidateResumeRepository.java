package xCloud.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import xCloud.entity.es.CandidateResumeDocument;

import java.util.List;

/**
 * 候选人简历ES Repository
 * 提供基础CRUD操作
 * @author Claude
 * @date 2026-08-18
 */
@Repository
public interface CandidateResumeRepository extends ElasticsearchRepository<CandidateResumeDocument, String> {

    /**
     * 根据姓名查询
     */
    List<CandidateResumeDocument> findByName(String name);

    /**
     * 根据技能查询
     */
    List<CandidateResumeDocument> findBySkillsContaining(String skill);

    /**
     * 根据期望职位查询
     */
    List<CandidateResumeDocument> findByExpectedPosition(String position);

    /**
     * 根据工作经验范围查询
     */
    List<CandidateResumeDocument> findByExperienceBetween(Integer minExp, Integer maxExp);

    /**
     * 根据状态查询
     */
    List<CandidateResumeDocument> findByStatus(String status);
}
