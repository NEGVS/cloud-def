# Milvus 向量数据库接口文档

## 模块：`/milvus`

---

## 1. Collection 管理

### 1.1 测试连接
**接口**: `GET /milvus/test`  
**说明**: 测试Milvus连接状态，并检查目标Collection是否存在  
**入参**: 无  
**返回**: `String`
```
Milvus 连接成功！Collection [andy_vectorsV2] 存在: true
```

### 1.2 创建Collection
**接口**: `POST /milvus/collection/create`  
**说明**: 创建向量Collection（包含Schema、索引、加载到内存）  
**入参**: 无  
**返回**: `Result<String>`

### 1.3 删除Collection
**接口**: `DELETE /milvus/collection/drop`  
**说明**: 删除指定Collection（不可逆操作）  
**入参**: `DropCollectionRequest`
```json
{
  "name": "andy_vectorsV2"
}
```
**返回**: `Result<Object>`

### 1.4 查询所有Collection
**接口**: `GET /milvus/collection/list`  
**说明**: 查询所有Collection名称列表  
**入参**: 无  
**返回**: `Result<List<String>>`
```json
{
  "code": 200,
  "data": ["andy_vectorsV2", "test_collection"]
}
```

---

## 2. 向量插入

### 2.1 插入单条文本向量
**接口**: `POST /milvus/vector/insert`  
**说明**: 将文本转为向量并存储到Milvus，同步记录到MySQL日志表  
**入参**: `MilvusRequest`
```json
{
  "text": "5年Java后端开发经验，精通Spring Boot、MyBatis..."
}
```
**返回**: `Result<Object>`
```json
{
  "code": 200,
  "message": "插入成功",
  "data": {
    "id": 123456,
    "vectorDim": 1024
  }
}
```

---

## 3. 向量检索

### 3.1 向量相似度搜索
**接口**: `POST /milvus/vector/search`  
**说明**: 根据查询文本检索最相似的Top-K个向量（用于RAG检索增强生成）  
**入参**: `MilvusSearchReq`
```json
{
  "text": "需要3年以上Java开发经验，熟悉Spring Boot"
}
```
**返回**: `Result<Object>`
```json
{
  "code": 200,
  "data": [
    {
      "id": 123456,
      "text": "5年Java后端开发经验，精通Spring Boot...",
      "score": 0.95
    },
    {
      "id": 123457,
      "text": "3年Java开发经验，熟悉Spring全家桶...",
      "score": 0.89
    }
  ]
}
```

---

## 4. 向量查询

### 4.1 查询ID大于指定值的记录
**接口**: `GET /milvus/query/bigger?id=100`  
**说明**: 查询ID大于指定值的所有向量记录  
**入参**: `id` (Long)  
**返回**: `Result<Object>`

### 4.2 查询ID等于指定值的记录
**接口**: `GET /milvus/query/equal?id=123456`  
**说明**: 查询ID等于指定值的向量记录  
**入参**: `id` (Long)  
**返回**: `Result<Object>`

### 4.3 分页查询向量数据 ✨ **新增**
**接口**: `POST /milvus/vector/page`  
**说明**: 分页返回Milvus向量数据库中的数据，包括ID、文本、向量（文本超过5000字符自动截断）  
**入参**: `MilvusVectorPageRequest`
```json
{
  "pageNum": 1,                         // 页码（从1开始）
  "pageSize": 10,                       // 每页大小（最大100）
  "collectionName": "andy_vectorsV2"    // 集合名称（可选，默认使用配置的集合）
}
```
**返回**: `Result<MilvusVectorPageResponse>`
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 123456,
        "text": "5年Java后端开发经验，精通Spring Boot...",
        "vector": [0.123, 0.456, 0.789, ...],  // 1024维向量数组
        "originalTextLength": 8000,
        "truncated": true                       // 是否截断
      },
      {
        "id": 123457,
        "text": "3年Java开发经验...",
        "vector": [0.321, 0.654, 0.987, ...],
        "originalTextLength": 3000,
        "truncated": false
      }
    ],
    "pageNum": 1,
    "pageSize": 10,
    "total": 100,                              // 总记录数（估算值）
    "totalPages": 10,                          // 总页数
    "collectionName": "andy_vectorsV2"         // 集合名称
  }
}
```

**特性说明**：
- 支持自定义Collection，不指定则使用默认配置
- 文本长度超过5000字符自动截断，避免返回数据过大
- `originalTextLength`字段记录原始文本长度
- `truncated`字段标识是否截断
- 每页最大100条记录，防止内存溢出
- `total`为估算值（Milvus V2暂不支持高效count）

---

## 实体说明

### MilvusVectorPageRequest
| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| pageNum | Integer | 否 | 页码（从1开始），默认1 |
| pageSize | Integer | 否 | 每页大小，默认10，最大100 |
| collectionName | String | 否 | 集合名称，默认使用配置的collection |

### MilvusVectorPageResponse
| 字段 | 类型 | 说明 |
|---|---|---|
| records | List\<MilvusVectorVO\> | 向量数据列表 |
| pageNum | Integer | 当前页码 |
| pageSize | Integer | 每页大小 |
| total | Long | 总记录数（估算值） |
| totalPages | Integer | 总页数 |
| collectionName | String | 集合名称 |

### MilvusVectorVO
| 字段 | 类型 | 说明 |
|---|---|---|
| id | Long | 向量ID |
| text | String | 文本内容（超过5000字符会截断） |
| vector | List\<Float\> | 向量数据（维度1024的浮点数组） |
| originalTextLength | Integer | 文本原始长度 |
| truncated | Boolean | 是否截断 |
