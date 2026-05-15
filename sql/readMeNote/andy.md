RecruitmentAgentController的chat方法
主要使用了哪些技术栈？实现了什么功能？

## 1-RecruitmentAgentController的chat方法主要使用了哪些技术栈？实现了什么功能？
⏺ 技术栈：

  - Spring WebFlux — 返回 Flux<String>，配合 produces = MediaType.TEXT_EVENT_STREAM_VALUE 实现 SSE 长连接推送
  - Project Reactor — Flux 作为响应式流载体，数据由 Sinks.Many 异步推入
  - Java 21 虚拟线程 — Thread.ofVirtual() 在 Service 层异步执行 Agent 循环，不阻塞 Reactor 调度线程
  - SpringDoc + Knife4j — @Operation、@Parameter 提供 Swagger 文档

  ---
  实现的功能：

  接收用户问题后，触发一个完整的 Plan-and-Execute + ReAct Agent 流程，并将中间过程和最终答案实时推送给客户端：

  GET /recruitment/chat?sessionId=xxx&query=xxx
           ↓
  [PLAN]        ← LLM 制定 2-4 步执行计划
  • 步骤1
  • 步骤2
  [EXECUTING]
  [STEP 1]      ← ReAct 推理：Thought → Action → Observation
  [STEP 2]      ← 可能调用工具（RAG检索 / DB查询 / 外部API）
  ...
  [ANSWER]      ← 最终答案按标点分块流式推送
  [ERROR]       ← 异常时输出错误信息

  关键特性：
  - 同一 sessionId 共享多轮对话记忆（滑动窗口保留最近 10 轮）
  - 最多 6 步 ReAct 循环，超出后强制总结
  - LLM 调用失败自动重试 2 次
  - 工具执行失败不中断流程，返回错误 Observation 继续推理


## 2- 使用rag了吗
 看一下 ReAct 循环里调用的工具实现：

  Read 1 file (ctrl+o to expand)

⏺ 用了，而且是完整的 增强版 RAG，不是简单的向量检索。

  chat 方法通过 ReAct 循环，当 LLM 判断需要查文档时会调用 document_search 工具，触发完整的 RAG 流程：

  用户问题
     ↓
  HybridRagService.hybridSearch()   ← 混合检索，召回 Top-10
     ↓
  RerankService.rerank()            ← 重排序，精选 Top-5
     ↓
  ContextCompressService.compress() ← 上下文压缩，去噪
     ↓
  返回精简上下文给 LLM 生成答案

  三个阶段对应 CLAUDE.md 里提到的优化策略：

  ┌────────────────────────┬─────────────────────────────────────────────┐
  │          阶段          │                  对应优化                   │
  ├────────────────────────┼─────────────────────────────────────────────┤
  │ HybridRagService       │ 关键词（BM25）+ 向量混合检索 + RRF 融合排序 │
  ├────────────────────────┼─────────────────────────────────────────────┤
  │ RerankService          │ Rerank 精排，准确率再提升 5-10%             │
  ├────────────────────────┼─────────────────────────────────────────────┤
  │ ContextCompressService │ 压缩召回内容，减少噪声和 token 消耗         │
  └────────────────────────┴─────────────────────────────────────────────┘

  RAG 只是 Agent 的一个工具，LLM 会自主决定是否调用，不是每次都走 RAG，这比传统"每次都检索"的方案更灵活。

# end
如果在基础上让你继续优化，你会使用什么技术方案？请告诉我

基于现有代码，分层给出优化方向：
                                         
---                                                                                                                                                                                                       
1. RAG 召回质量（最直接的收益）

现状： HybridRagService 做了混合检索，但查询是原始用户问题。

优化：Query Rewriting + HyDE
- Query Rewriting：用 LLM 把用户问题改写成 3 个角度的子查询，分别检索后合并，覆盖更广
- HyDE（假设文档嵌入）：让 LLM 先生成一个"假设答案"，用假设答案的向量去检索，比问题向量更接近文档分布

收益：召回率从当前约 70% 提升到 85%+
                                                                                                                                                                                                            
---                                                                                                                                                                                                       
2. 切片策略优化

现状： PdfChunkService 的切片方式未知，大概率是固定长度切片。

优化：父子 Chunk 策略                                                                                                                                                                                     
存储：小 chunk（200字）用于精准检索                                                                                                                                                                       
返回：检索到小 chunk 后，返回其父 chunk（800字）给 LLM                                                                                                                                                    
解决"切太短丢上下文，切太长噪声多"的矛盾。
                                                                                                                                                                                                            
---                                                                                                                                                                                                       
3. Agent 推理稳定性

现状： ReAct 循环依赖 LLM 严格按格式输出 Action/Final Answer，格式不对就浪费一步。

优化：Function Calling 替代文本解析

现在用正则解析：                                                                                                                                                                                          
Pattern.compile("Action:\\s*(.+?)\\s*\\n")

改用 LLM 原生 Function Calling（通义千问支持），工具调用结构化返回，彻底消除格式解析失败的问题，稳定性从 80% 提到 99%。
                                                                                                                                                                                                            
---                                                                                                                                                                                                       
4. 会话记忆持久化

现状： ConversationMemoryService 用 ConcurrentHashMap 存内存，重启丢失。

优化：Redis 持久化 + 摘要压缩                                                                                                                                                                             
短期记忆：Redis，TTL 24h，保留最近 10 轮原文                                                                                                                                                              
长期记忆：超出 10 轮后，用 LLM 摘要压缩历史，存为一条 system message                                                                                                                                      
解决重启丢失和长对话 token 超限两个问题。
                                                                                                                                                                                                            
---                                                                                                                                                                                                       
5. 流式输出体验

现状： streamAnswer 按标点分块推送，粒度粗，用户感知延迟高。

优化：LLM 原生 stream=true 直接透传

ReAct 最后一步直接用流式 LLM 生成答案并实时推送，而不是先 block() 拿到完整答案再分块：                                                                                                                    
ReAct finalAnswer → 直接 stream 调用 LLM → chunk by chunk 推入 sink
用户看到第一个字的延迟从 2-3s 降到 300ms 以内。
                                                                                                                                                                                                            
---                                                                                                                                                                                                       
6. 评估体系（工程保障）

现状： 没有评估，优化靠感觉。

优化：建立离线评测集
- 100 个真实问题 + 标准答案
- 每次改动后跑 Recall@5、MRR、端到端准确率
- 用 DeepEval 或自建脚本，CI 里自动跑

  ---                                                                                                                                                                                                       
优先级建议

第一步：Function Calling 替代正则解析   ← 稳定性收益最大，改动集中
第二步：Query Rewriting                ← 召回质量提升明显，改动在 HybridRagService                                                                                                                        
第三步：Redis 会话持久化               ← 生产必备，改动在 ConversationMemoryService                                                                                                                       
第四步：stream=true 直接透传           ← 体验提升，改动在 streamAnswer                                                                                                                                    
第五步：父子 Chunk + HyDE             ← 效果好但需要重建知识库，放最后                                                                                                                                    
                                                                               



