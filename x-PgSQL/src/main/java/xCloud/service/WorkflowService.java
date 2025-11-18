package xCloud.service;

import jakarta.annotation.Resource;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/17 14:12
 * @ClassName WorkflowService
 */
@Service
public class WorkflowService {
    @Resource
    private RepositoryService repositoryService;

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    public void deployProcess(String bpmnClasspathLocation) {
        repositoryService.createDeployment()
                .addClasspathResource(bpmnClasspathLocation)
                .deploy();
    }

    /**
     * Required type:
     * org.activiti.api.process.model.ProcessInstance
     * Provided:
     * org.activiti.engine.runtime.ProcessInstance
     *
     * @param processKey
     * @param applyUser
     * @param days
     * @return
     */
    public ProcessInstance startProcess(String processKey, String applyUser, Integer days) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("applyUser", applyUser);
        vars.put("days", days);
//        return runtimeService.startProcessInstanceByKey(processKey, vars);
        return runtimeService.startProcessInstanceByKey(processKey, vars);
    }

    public List<Task> getTasksForAssignee(String assignee) {
        return taskService.createTaskQuery().taskAssignee(assignee).list();
    }
    public void completeTask(String taskId, Map<String, Object> vars) {
        taskService.complete(taskId, vars);
    }
    public List<HistoricTaskInstance> getHistoryTasks(String processInstanceId) {
//        return org.activiti.engine.Activiti.getHistoryService() == null ? null : taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        return null;
    }
//    注：上面 getHistoryTasks 示例仅示意，实际可使用 HistoryService 查询历史任务。

}
