# 智能招聘系统

## 架构流程

```
用户输入 → 意图识别 → 判断分支:
├─ 非求职 → LLM直接对话
└─ 求职相关 → HDBSCAN聚类 → 向量检索 → LLM生成回复(含职位)
```

## 核心组件

### Java服务
- `RecruitmentService`: 主流程编排
- `IntentService`: 调用Python意图识别
- `ClusterService`: 调用Python聚类
- `VectorSearchService`: Milvus向量检索
- `PDFVectorService`: PDF文档向量化
- `LLMService`: LLM对话生成
- `EmbeddingService`: 文本向量化

### Python API
- `intent_api.py`: 意图识别(端口5000)
- `cluster_api.py`: HDBSCAN聚类(端口5001)

## 启动步骤

1. 启动Milvus: `docker run -p 19530:19530 milvusdb/milvus`
2. 启动Python API: `python intent_api.py` 和 `python cluster_api.py`
3. 启动Java服务: `mvn spring-boot:run`
4. 测试: `curl -X POST http://localhost:8080/api/chat -d "我想找Java开发工作"`

## 关键逻辑

**是否返回岗位判断**: 相似度score > 0.75时返回职位列表
