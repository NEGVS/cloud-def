package xCloud.openAiChatModel.orchestrator.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import xCloud.openAiChatModel.service.JobService;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/5 15:09
 * @ClassName ToolExecutor
 * - 接收 `ToolCall`，执行相应业务逻辑。
 * - 例如：`apply_job` -> 调用 `jobService.apply()`；`schedule_interview` -> 发布 Kafka 事件。
 */
@RequiredArgsConstructor
@Service
public class ToolExecutor {

    private final JobService jobService;

    public Object execute(ToolCall call) {
        switch (call.getTool()) {
            case "apply_job":
                Long jobId = ((Number) call.getArgs().get("job_id")).longValue();
                Long resumeId = ((Number) call.getArgs().get("resume_id")).longValue();
                return jobService.apply(jobId, resumeId);
            case "schedule_interview":
                return "已为您预约面试，HR 将尽快联系您。";
            default:
                return "未知工具: " + call.getTool();
        }
    }
}
