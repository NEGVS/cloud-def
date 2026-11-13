# 智能招聘系统

## 架构流程
```
用户输入 → IntentClassifier(意图识别) → 分支:
├─ 非求职 → LLM直接对话
└─ 求职 → HDBSCAN聚类 → Milvus向量检索 → LLM生成(含职位)
```

## 核心组件

### Java服务 (xCloud.openAiChatModel.*)
- `RecruitmentService` - 主流程编排,支持流式/非流式
- `RecruitmentQueryService` - 聚类+向量检索
- `JobService` - PDF向量化入库
- `LLMStreamService` - 流式LLM响应
- `RecruitmentController` - REST接口

### Python FastAPI (端口8000/8001)
- `intent_service.py` - 意图识别
- `cluster_service.py` - HDBSCAN聚类

## 启动步骤

1. 启动Milvus: `docker run -d -p 19530:19530 milvusdb/milvus`
2. 安装Python依赖: `cd python && pip install -r requirements.txt`
3. 启动Python服务: `./start_services.sh`
4. 启动Java: `mvn spring-boot:run`

## API接口

- POST `/api/recruitment/chat` - 普通对话
- POST `/api/recruitment/chat/stream` - 流式对话(SSE)

## 关键配置

- `hdbscan.service.url`: 聚类服务地址
- `llm.api.url`: LLM API地址
- `milvus.uri`: Milvus连接地址
