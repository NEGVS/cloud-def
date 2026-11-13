### DeepSeek 官方 API 概述

DeepSeek 的官方 API 平台（https://platform.deepseek.com/）提供对 DeepSeek AI 模型的访问，主要聚焦于聊天和生成任务。它采用与 OpenAI API 兼容的格式，便于开发者迁移和集成。API 基地址为 `https://api.deepseek.com`（兼容模式下使用 `https://api.deepseek.com/v1`）。 支持非流式和流式响应（通过 `stream` 参数控制）。

#### 可用模型
根据官方文档，目前 API 支持以下两个聊天模型（均为 DeepSeek-V3.2-Exp 变体），无嵌入（embedding）模型支持：
- **deepseek-chat**：非思考模式，上下文长度 128K，最大输出 8K。支持 JSON 输出、函数调用、聊天前缀补全（Beta）和 FIM 补全（Beta）。
- **deepseek-reasoner**：思考模式，上下文长度 128K，最大输出 64K。支持 JSON 输出和聊天前缀补全（Beta），但函数调用会回退到 deepseek-chat 处理。

无嵌入模型；官方 FAQ 表示嵌入功能暂未支持，但计划中开发。

#### 定价（每 1M tokens）
| 模型              | 输入（缓存命中） | 输入（缓存未命中） | 输出    |
|-------------------|------------------|--------------------|---------|
| deepseek-chat    | $0.028          | $0.28             | $0.42  |
| deepseek-reasoner| $0.028          | $0.28             | $0.42  |

#### 认证与调用
1. **获取 API Key**：在 https://platform.deepseek.com/api_keys 申请免费密钥。
2. **认证**：在请求头中使用 `Authorization: Bearer ${DEEPSEEK_API_KEY}`。
3. **主要端点**：聊天/补全 - `POST /chat/completions`。

**示例（cURL，非流式）**：
```
curl https://api.deepseek.com/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${DEEPSEEK_API_KEY}" \
  -d '{
        "model": "deepseek-chat",
        "messages": [
          {"role": "system", "content": "You are a helpful assistant."},
          {"role": "user", "content": "Hello!"}
        ],
        "stream": false
      }'
```

#### Java 集成建议
使用 OpenAI Java SDK（com.openai:openai-java），只需修改 base URL 和模型：
```java
OpenAIClient client = OkHttpOpenAIClient.builder()
    .apiKey("your-deepseek-key")
    .baseUrl("https://api.deepseek.com/v1")
    .build();

// 示例：聊天调用
ChatCompletionRequest request = ChatCompletionRequest.builder()
    .model("deepseek-chat")
    .messages(List.of(new ChatMessage(ChatRole.USER, "Hello!")))
    .build();
ChatCompletionResponse response = client.chatCompletions().create(request);
```

#### 注意事项
- **嵌入替代**：API 无嵌入支持，建议使用 Hugging Face 的 DeepSeek 开源模型（如 deepseek-ai/deepseek-embeddings）本地运行，或 Ollama 部署。
- **限制**：免费额度有限，生产环境监控 token 使用。
- 完整文档：https://api-docs.deepseek.com/。

如果您需要嵌入具体集成或更多示例，请提供细节！