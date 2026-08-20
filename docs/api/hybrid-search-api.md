
---

## 混合检索模块 `/api/search`

### 1. 混合检索（完整链路）

- **接口**：`POST /api/search/hybrid`
- **说明**：BM25关键词检索 + 向量语义检索 + RRF融合 + Rerank精排。提供最高准确率的候选人检索。
- **入参**：Query参数

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| query | String | 是 | 查询文本（如：需要3年以上Java开发经验，熟悉Spring Boot） |
| topK | Integer | 否 | 返回Top-K结果，默认10 |
| useRerank | Boolean | 否 | 是否使用Rerank精排，默认true（关闭可提升速度） |

- **返回**：`Map<String, Object>`

```json
{
  "query": "需要3年以上Java开发经验",
  "total": 10,
  "elapsed_ms": 1523,
  "useRerank": true,
  "results": [
    {
      "documentId": "resume_001",
      "name": "张三",
      "contentSnippet": "5年Java后端开发经验，精通Spring Boot、MyBatis...",
      "skills": "Java,Spring Boot,MySQL,Redis,Kafka",
      "experience": 5,
      "expectedPosition": "高级Java工程师",
      "bm25Score": 12.5,
      "vectorScore": 0.89,
      "finalScore": 9.2,
      "rank": 1,
      "matchReason": "候选人具备5年Java开发经验，技能栈完全匹配岗位要求"
    }
  ]
}
```

---

### 2. 快速检索

- **接口**：`POST /api/search/fast`
- **说明**：仅使用BM25 + 向量 + RRF融合，不使用Rerank。适用于对延迟敏感的场景（响应时间<500ms）。
- **入参**：Query参数

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| query | String | 是 | 查询文本 |
| topK | Integer | 否 | 返回Top-K结果，默认10 |

- **返回**：`Map<String, Object>`（同上，但无matchReason字段）

---

### 3. 健康检查

- **接口**：`GET /api/search/health`
- **说明**：检查Elasticsearch和Milvus连接状态
- **入参**：无
- **返回**：`Map<String, Object>`

```json
{
  "status": "ok",
  "timestamp": 1724000000000
}
```

---

