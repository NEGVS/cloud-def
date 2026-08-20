package xCloud.mapper.recruitment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import xCloud.entity.recruitment.CandidateScore;

import java.util.List;

/**
 * 候选人评分 Mapper
 * @author Claude
 * @date 2026-08-18
 */
@Mapper
public interface CandidateScoreMapper extends BaseMapper<CandidateScore> {

    /**
     * 根据JD ID查询所有候选人评分（按总分降序）
     */
    List<CandidateScore> selectByJdIdOrderByScore(@Param("jdId") Long jdId);

    /**
     * 根据候选人ID查询所有评分记录
     */
    List<CandidateScore> selectByCandidateId(@Param("candidateId") Long candidateId);

    /**
     * 查询候选人在指定JD下的评分
     */
    CandidateScore selectByCandidateAndJd(@Param("candidateId") Long candidateId, @Param("jdId") Long jdId);

    /**
     * 查询高分候选人（总分>=阈值）
     */
    List<CandidateScore> selectHighScoreCandidates(@Param("jdId") Long jdId, @Param("threshold") Integer threshold);
}
