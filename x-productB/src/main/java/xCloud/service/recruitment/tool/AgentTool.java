package xCloud.service.recruitment.tool;

/**
 * Agent 工具接口
 *
 * 每个工具代表 Agent 可调用的一种能力。
 * ReAct 循环中，Agent 根据 getName()/getDescription() 决定调用哪个工具，
 * 然后调用 execute(input) 获取 Observation。
 */
public interface AgentTool {

    /** 工具名称（唯一标识，Agent 在 Action 中填写此名称） */
    String getName();

    /** 工具描述（告诉 Agent 何时使用此工具，以及 input 格式） */
    String getDescription();

    /**
     * 执行工具
     *
     * @param input Agent 提供的输入（自然语言或 JSON）
     * @return 工具执行结果（Observation）
     */
    String execute(String input);
}
