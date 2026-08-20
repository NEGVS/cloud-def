package xCloud.mapper.recruitment;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import xCloud.entity.recruitment.JDFeedback;

import java.util.List;

/**
 * JD反馈 Mapper
 * @author Claude
 * @date 2026-08-18
 */
@Mapper
public interface JDFeedbackMapper extends BaseMapper<JDFeedback> {

    /**
     * 根据JD ID查询所有反馈
     */
    List<JDFeedback> selectByJdId(Long jdId);
}
