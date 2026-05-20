package xCloud.service.vector;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import xCloud.entity.TextVectorLog;
import xCloud.mapper.TextVectorLogMapper;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2026/5/20 16:01
 * @ClassName TextVectorLogServiceImpl
 */
@Service
public class TextVectorLogServiceImpl extends ServiceImpl<TextVectorLogMapper, TextVectorLog>
        implements TextVectorLogService {
}
