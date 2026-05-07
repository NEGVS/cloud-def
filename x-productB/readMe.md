# x-productB 技术栈总览

## 核心框架
| 层次 | 技术 | 版本 |
|------|------|------|
| 基础框架 | Spring Boot | 3.2.9 |
| 服务注册/配置 | Spring Cloud Alibaba Nacos | 2023.0.1.0 |
| 服务调用 | Spring Cloud OpenFeign | 4.1.3 |
| 响应式 | Spring WebFlux / Reactor | 3.6.9 |
| 动态线程池 | DynamicTP | 1.1.8 |

## 数据存储
| 组件 | 用途 | 版本 |
|------|------|------|
| MySQL 8.0 + Druid | 关系型数据库，关键词检索、文档块存储 | 1.2.20 |
| MyBatis-Plus | ORM | 3.5.5 |
| Milvus | 向量数据库，语义检索 | SDK 2.6.6 |
| Redis + Redisson | 缓存、分布式锁 | 3.27.2 |

## AI / LLM
| 组件 | 用途 |
|------|------|
| 阿里云 DashScope (qwen-plus) | 主力 LLM，对话生成、上下文压缩 |
| 阿里云 text-embedding-v4 | 向量化（1024维） |
| DashScope gte-rerank | 二次排序（Rerank） |
| DeepSeek API | 备用 LLM |
| LangChain4j 1.4.0 | AI 应用框架，RAG 流程编排 |
| OpenAI Java SDK 4.3.0 | OpenAI 兼容接口调用 |
| DJL HuggingFace Tokenizers | 本地 Token 处理 |

## RAG 架构
```
用户输入
  ↓
意图识别（Python FastAPI / LLM）
  ├─ 非求职 → LLM 直接对话
  └─ 求职相关
       ↓
     HDBSCAN 聚类（Python, 端口8001）→ 职位分类ID
       ↓
     Hybrid RAG 多路召回
       ├─ Milvus 向量检索（语义相似度）
       └─ MySQL BM25 关键词检索
       ↓ RRF 融合排序
     Rerank 二次排序（gte-rerank）
       ↓
     上下文压缩（LLM 提取关键片段）
       ↓
     LLM 生成回复（流式 SSE / 非流式）
```

## Python 微服务（FastAPI）
| 服务 | 端口 | 功能 |
|------|------|------|
| intent_service.py | 8000 | 意图识别 |
| cluster_service.py | 8001 | HDBSCAN 聚类 |

## PDF 处理
- Apache PDFBox 2.0.30
- Recursive Character Splitter（段落→句子→字符，100字 Overlap）
- 并行 Embedding（25条/批，CompletableFuture 异步）

## 工程能力
| 能力 | 实现 |
|------|------|
| 多轮对话记忆 | 滑动窗口10轮，按 sessionId 隔离 |
| 流式输出 | SSE（Flux\<String\>） |
| ReAct Agent | Thought→Action→Observation，最多6步，含重试 |
| 高并发 PDF 上传 | 同步/异步双接口，Embedding 批量并行 |
| 端口自动清理 | 启动前 kill 占用进程（ApplicationEnvironmentPreparedEvent）|
| 接口文档 | Knife4j + SpringDoc OpenAPI 3 (doc.html) |
| SQL 监控 | Druid StatViewServlet (/druid) |

---

完成智能招聘系统
主要代码我写在了x-productB，请继续优化，完善整个项目。
这是一个智能招聘系统，请帮我实现，这是我的大概思路，不对的你尽管修改。
主要目的：根据用户的输入，进行回复或者返回最匹配的职位信息

目前大部份代码写在了x-productB，                                                                                                                                      
1-把pdf文档，向量化       【 完成 】                                                                                                                                   
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

