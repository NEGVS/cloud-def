package xCloud.controller;

import jakarta.annotation.Resource;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xCloud.entity.request.StartRequest;
import xCloud.service.WorkflowService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/17 14:12
 * @ClassName WorkflowController
 */


@RestController
@RequestMapping("/workflow")
public class WorkflowController {
    @Resource
    private WorkflowService workflowService;


    @PostMapping("/deploy")
    public String deploy() {
        workflowService.deployProcess("processes/leave-process.bpmn20.xml");
        return "deployed";
    }

    @PostMapping("/start")
    public Map<String, Object> startProcess(@RequestBody StartRequest req) {
        ProcessInstance pi = workflowService.startProcess("leaveProcess", req.getApplyUser(), req.getDays());
        Map<String, Object> m = new HashMap<>();
        m.put("processInstanceId", pi.getId());
        return m;
    }

    @GetMapping("/tasks/{assignee}")
    public List<Task> getTasks(@PathVariable String assignee) {
        return workflowService.getTasksForAssignee(assignee);
    }

    @PostMapping("/complete/{taskId}")
    public String complete(@PathVariable String taskId, @RequestBody(required = false) Map<String, Object> vars) {
        if (vars == null) vars = new HashMap<>();
        workflowService.completeTask(taskId, vars);
        return "completed";
    }

}
