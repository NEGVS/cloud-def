完成智能招聘系统
主要代码我写在了x-productB，请继续优化，完善整个项目。
这是一个智能招聘系统，请帮我实现，这是我的大概思路，不对的你尽管修改。

主要目的：根据用户的输入，进行回复或者返回最匹配的职位信息



{"title":"我想和你交换电话","info":"您是否同意?","agreeBtn":"同意","refuseBtn":"不同意","agreeResultWords":"已同意","refuseResultWords":"未同意","msgType":"","msgId":"2","callback":"","jobId":"50001006"}



INSERT INTO srzp_application.ai_robot (user_id, name, account, robot_wecom_account, avatar) VALUES (58655, '陈佳宁', '13162091125', 'LiWuYou', '');
INSERT INTO srzp_application.ai_robot (user_id, name, account, robot_wecom_account, avatar) VALUES (57868, '陆小鹿', '13338629982', 'luxiaolu', '');
INSERT INTO srzp_application.ai_robot (user_id, name, account, robot_wecom_account, avatar) VALUES (847964, '张俊伟', '18321447952', '18321447952', '');


{"info":"我已留下微信，你可以加我微信聊","leftBtnType":"","rightBtnType":"","centerBtnType":"addWechat","isSendRealPhone":"0","callRealPhone":"","desc":"发送了【微信号】","senderUserId":"847855"}
{"headInfo":"好的收到！感谢你留下联系方式并报名我们的岗位，我会尽快处理，如果合适的话会立即联系你~","footInfo":"平台消息可能查看不及时，你可以直接打我电话联系我哦","desc":"【自动回复消息】"}



目前大部份代码写在了x-productB，                                                                                                                                      
1-把pdf文档，向量化                                                                                                                                                   
写方法实现                                                                                                                                                            
2-调用python的意图识别 方法，判断用户输入是求职问题，还是其他问题

2-调用python的Hdbscan聚类方法,获取最匹配的职位分类ID

3-根据职位分类ID，进行向量检索

4-把检索结果发给llm                                                                                                                                                   
中间如何判断是否需要发送岗位？                                                                                                                                        
5-llm返回结果给用户   

用户输入 → IntentClassifier → 求职问题 → HDBSCAN聚类 → Milvus检索 → LLM生成回复

用户输入 → 意图识别 → 判断分支:
7 ├─ 非求职 → LLM直接对话
8 └─ 求职相关 → HDBSCAN聚类 → 向量检索 → LLM生成回复(含职位)

调用python使用FastApi,webclient,分别支持，一次性回答，和流失回答。已经存在的代码就在基础上修改完善，没有的再新建。要求：快速，无误，优化完善流程，使用先进技术



完成清单

新建文件（共 11 个）

service/recruitment/
├── PdfChunkService.java           PDF 上传 + 智能切分 + 向量化存储                                                            
├── HybridRagService.java          多路召回（向量 + 关键词 + RRF 融合）                                                        
├── RerankService.java             二次排序（DashScope gte-rerank）                                                            
├── ContextCompressService.java    上下文压缩（LLM 提取关键片段）                                                              
├── ConversationMemoryService.java 多轮对话记忆（滑动窗口，10轮）                                                              
└── tool/                                                                                                                      
├── AgentTool.java             工具接口                                                                                    
├── RagTool.java               文档检索工具                                                                                
├── SqlQueryTool.java          数据库查询工具                                                                              
└── ExternalApiTool.java       外部业务 API 工具                                                                           
RecruitmentAgentService.java       ★ Agent 主服务

entity/recruitment/                                                                                                              
└── DocumentChunk.java             文档块实体

mapper/
└── DocumentChunkMapper.java       关键词检索 Mapper

controller/
└── RecruitmentAgentController.java HTTP 接口
                                                                                                                                   
---
核心能力对照

┌───────────────────────┬────────────────────────────────────────────────────────────────────────────────────┐
│         需求          │                                        实现                                        │                   
├───────────────────────┼────────────────────────────────────────────────────────────────────────────────────┤
│ PDF 上传 + 最优切分   │ PdfChunkService — Recursive Character Splitter（段落→句子→字符，含 100字 Overlap） │
├───────────────────────┼────────────────────────────────────────────────────────────────────────────────────┤
│ 多路召回 Hybrid RAG   │ HybridRagService — Milvus 向量检索 + MySQL 关键词检索 + RRF 融合                   │                   
├───────────────────────┼────────────────────────────────────────────────────────────────────────────────────┤                   
│ Re-ranking 二次排序   │ RerankService — DashScope gte-rerank API                                           │                   
├───────────────────────┼────────────────────────────────────────────────────────────────────────────────────┤                   
│ 上下文压缩            │ ContextCompressService — Qwen LLM 提取相关片段                                     │
├───────────────────────┼────────────────────────────────────────────────────────────────────────────────────┤                   
│ 多轮对话记忆          │ ConversationMemoryService — 滑动窗口，按 sessionId 隔离                            │
├───────────────────────┼────────────────────────────────────────────────────────────────────────────────────┤                   
│ 流式输出              │ RecruitmentAgentService.chat() → Flux<String> SSE                                  │
├───────────────────────┼────────────────────────────────────────────────────────────────────────────────────┤                   
│ 自动工具选择          │ ReAct 循环解析 Action: tool_name 自动路由                                          │
├───────────────────────┼────────────────────────────────────────────────────────────────────────────────────┤                   
│ 多步推理 Multi-step   │ ReAct Thought→Action→Observation 最多 6 步                                         │
├───────────────────────┼────────────────────────────────────────────────────────────────────────────────────┤                   
│ Tool 组合 RAG+SQL+API │ RagTool / SqlQueryTool / ExternalApiTool 三路并用                                  │
├───────────────────────┼────────────────────────────────────────────────────────────────────────────────────┤                   
│ 失败重试              │ 工具执行 & LLM 调用各有最多 2 次重试                                               │
├───────────────────────┼────────────────────────────────────────────────────────────────────────────────────┤                   
│ Plan-and-Execute      │ Agent 先 makePlan() 出步骤列表，再按计划 executeReAct()                            │
└───────────────────────┴────────────────────────────────────────────────────────────────────────────────────┘
                                                            
---                                                                                                                              
需要手动做一件事（建表）

在 MySQL 执行：
CREATE TABLE recruitment_chunk (                                                                                                 
id           BIGINT       PRIMARY KEY,                  
content      TEXT         NOT NULL,                                                                                            
source       VARCHAR(500) NOT NULL,                     
doc_type     VARCHAR(100) NOT NULL,                                                                                            
chunk_index  INT          NOT NULL,
page_num     INT          NOT NULL DEFAULT 0,                                                                                  
create_time  DATETIME     NOT NULL                                                                                             
);

然后调用一次初始化接口创建 Milvus Collection：                                                                                   
POST /recruitment/collection/init

之后就可以正常上传 PDF 和对话了：
# 上传岗位 PDF
POST /recruitment/upload?docType=job_info   (multipart file)

# 流式对话
GET  /recruitment/chat?sessionId=u001&query=Java工程师有哪些要求

✻ Worked for 2m 11s                             


本次针对高并发做了 5 处优化：

  ---                                                                                                                              
优化清单

┌─────┬─────────────────────────┬─────────────────────────────────────────────────────┬──────────────────────────────────────┐   
│  #  │          问题           │                      优化方案                       │                 效果                 │   
├─────┼─────────────────────────┼─────────────────────────────────────────────────────┼──────────────────────────────────────┤   
│ 1   │ Embedding 逐条串行调用  │ 按 25 条分批，每批 CompletableFuture.supplyAsync    │ 100 块从串行 20s → 并行 ~1-2s        │   
│     │                         │ 并行提交，allOf 汇总                                │                                      │   
├─────┼─────────────────────────┼─────────────────────────────────────────────────────┼──────────────────────────────────────┤   
│ 2   │ embeddingB 单条调用     │ AliEmbeddingUtil 新增                               │ 减少 N-1 次 HTTP 往返                │   
│     │                         │ embeddingBatch(List)，批量调用 DashScope API        │                                      │   
├─────┼─────────────────────────┼─────────────────────────────────────────────────────┼──────────────────────────────────────┤   
│ 3   │ ensureCollectionExists  │ volatile boolean collectionReady + synchronized DCL │ 并发时只有一个线程创建，其余快速返回 │   
│     │ 无锁竞态                │  双重检查锁                                         │                                      │   
├─────┼─────────────────────────┼─────────────────────────────────────────────────────┼──────────────────────────────────────┤   
│ 4   │ MySQL 逐条 insert       │ DocumentChunkMapper 新增 insertBatch（单 SQL 多     │ 减少 N-1 次数据库连接开销            │   
│     │                         │ VALUES）                                            │                                      │   
├─────┼─────────────────────────┼─────────────────────────────────────────────────────┼──────────────────────────────────────┤   
│     │                         │ 新增 uploadPdfAsync 返回 CompletableFuture，PDF     │                                      │   
│ 5   │ uploadPdf 阻塞请求线程  │ 解析完即返回，Embedding+写库在 taskExecutor         │ 高并发时请求线程不被占满             │   
│     │                         │ 异步执行；Controller 同时提供 /upload（同步）和     │                                      │   
│     │                         │ /upload/async（异步）两个接口                       │                                      │   
└─────┴─────────────────────────┴─────────────────────────────────────────────────────┴──────────────────────────────────────┘   
