# API 文档

---

## 岗位管理模块 `/job`

### 1. 分页查询岗位列表

- **接口**：`POST /job/page`
- **说明**：多条件分页查询岗位，不返回 `job_detail` 大字段，提升性能。支持名称模糊、类型、状态、分类等过滤。
- **入参**：`JobQueryDTO`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| currentPage | Integer | 否 | 当前页，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| name | String | 否 | 岗位名称（模糊） |
| jobNature | Integer | 否 | 岗位类型：1全职 2兼职 |
| jobType | Integer | 否 | 岗位属性：1商户岗位 2自营岗位 |
| recruitmentType | Integer | 否 | 招聘类型：1直招 2派遣 3代招 |
| oneCategoryId | Integer | 否 | 一级分类ID |
| categoryId | Integer | 否 | 二级分类ID |
| companyId | Integer | 否 | 门店（公司）ID |
| status | Integer | 否 | 状态：1招聘中 4停招 |
| isDisable | Integer | 否 | 是否禁用：0否 1是 |
| auditType | Integer | 否 | 审核状态：0未审核 1通过 2不通过 100待提交 |
| isHot | Integer | 否 | 是否热门：1是 0否 |

- **返回**：`Result<Page<JobVO>>`，分页岗位列表

---

### 2. 查询岗位详情

- **接口**：`GET /job/detail/{id}`
- **说明**：根据ID查询岗位完整信息，含 `job_detail`、审核记录、道具信息等全部字段。
- **入参**：Path 参数 `id`（岗位ID）
- **返回**：`Result<Job>`，岗位完整实体

---

### 3. 新增岗位

- **接口**：`POST /job/add`
- **说明**：创建一条新岗位，默认 `status=4`（停招）、`auditType=100`（待提交审核）、`source=3`（app）。
- **入参**：`JobDTO`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| name | String | 是 | 岗位名称 |
| jobNature | Integer | 否 | 岗位类型：1全职 2兼职 |
| jobType | Integer | 否 | 岗位属性：1商户岗位 2自营岗位 |
| oneCategoryId | Integer | 否 | 一级分类ID |
| categoryId | Integer | 否 | 二级分类ID |
| recruitmentType | Integer | 否 | 招聘类型：1直招 2派遣 3代招 |
| userId | Integer | 否 | 商户端用户id |
| bonusSubsidy | String | 否 | 奖金补贴，逗号分隔数字 |
| socialSecurityFund | Integer | 否 | 社保公积金：0无 1五险 2五险一金 3五险二金 |
| welfares | String | 否 | 企业福利，逗号分隔数字 |
| accommodation | Integer | 否 | 食宿：1包吃包住 2包吃 3包住 4不包 |
| jobDetail | String | 否 | 岗位详情（富文本） |
| ageMin | Integer | 否 | 年龄下限，0不限 |
| ageMax | Integer | 否 | 年龄上限，0不限 |
| genderRequire | Integer | 否 | 性别：0不限 1男 2女 |
| educationRequire | Integer | 否 | 学历：0不限 1-6依次提升 |
| otherRequire | String | 否 | 其他要求 |
| companyId | Integer | 否 | 门店（公司）ID |
| brandId | Integer | 否 | 品牌ID |
| groupId | Integer | 否 | 群ID |
| hireAll | Integer | 否 | 招聘总人数 |
| imgIds | String | 否 | 岗位图片ID，逗号分隔 |
| videoId | Integer | 否 | 视频ID |
| videoImageId | Integer | 否 | 视频封面图ID |
| jobTag | String | 否 | 自定义标签 |
| isOpenPhone | Integer | 否 | 是否开放手机：1开放 2未开放 |

- **返回**：`Result<Void>`

---

### 4. 编辑岗位

- **接口**：`POST /job/update`
- **说明**：更新岗位基本信息，`id` 必传。状态、审核状态、来源、创建人等管理字段不受此接口影响。
- **入参**：`JobDTO`（同新增，`id` 必传）
- **返回**：`Result<Void>`

---

### 5. 删除岗位（软删除）

- **接口**：`DELETE /job/delete/{id}`
- **说明**：软删除，设置 `deleted_at`，数据不物理删除，后续查询自动过滤。
- **入参**：Path 参数 `id`（岗位ID）
- **返回**：`Result<Void>`

---

### 6. 修改岗位状态（启停招）

- **接口**：`POST /job/status/{id}/{status}`
- **说明**：切换岗位招聘状态，设为招聘中时同步更新 `release_time`。
- **入参**：

| 参数 | 位置 | 说明 |
|---|---|---|
| id | Path | 岗位ID |
| status | Path | 1=招聘中，4=停招 |

- **返回**：`Result<Void>`

---

### 7. 禁用/启用岗位

- **接口**：`POST /job/disable/{id}/{isDisable}`
- **说明**：禁用时需传 `reason`（禁用理由），启用时 reason 可不传，同时清空禁用理由。
- **入参**：

| 参数 | 位置 | 说明 |
|---|---|---|
| id | Path | 岗位ID |
| isDisable | Path | 0=启用，1=禁用 |
| reason | Query | 禁用理由（禁用时建议传，启用时可不传） |

- **返回**：`Result<Void>`

---

## 通用返回结构 `Result<T>`

```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
```

| code | 含义 |
|---|---|
| 200 | 成功 |
| 444 | 业务异常，message 中有说明 |

---

## 智能招聘 Agent 模块 `/recruitment`

### 1. 异步上传 PDF

- **接口**：`POST /recruitment/upload/async`
- **说明**：立即返回，后台处理 PDF 解析与向量化，适合大文件或高并发场景
- **入参**：multipart/form-data

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| file | File | 是 | PDF 文件 |
| docType | String | 否 | 文档类型：company_doc（默认）/ job_info |

- **返回**：`Result<String>`，提交确认消息

---

### 2. 同步上传 PDF

- **接口**：`POST /recruitment/upload`
- **说明**：阻塞等待解析与向量化完成后返回结果，适合小文件；返回入库块数及跳过块数
- **入参**：同异步上传
- **返回**：`Result<String>`，入库/跳过块数统计

---

### 3. Agent 流式对话（SSE）

- **接口**：`GET /recruitment/chat`
- **说明**：基于 ReAct 框架的多轮对话，SSE 流式返回。输出标签：[PLAN] [STEP N] [ANSWER] [ERROR]
- **入参**：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| sessionId | String | 是 | 会话ID，相同ID共享上下文 |
| query | String | 是 | 用户问题 |

- **返回**：`text/event-stream`，流式文本

---

### 4. 清除会话记忆

- **接口**：`DELETE /recruitment/session/{sessionId}`
- **说明**：清除指定会话的全部对话上下文，下次对话重新开始
- **入参**：Path 参数 `sessionId`
- **返回**：`Result<String>`

---

### 5. 初始化 Milvus Collection

- **接口**：`POST /recruitment/collection/init`
- **说明**：首次部署时调用，创建向量数据库 Collection 及索引；幂等操作，已存在则跳过
- **入参**：无
- **返回**：`Result<String>`

---

## 文本向量日志模块 `/text/vector/log`

### 1. 分页查询向量日志

- **接口**：`POST /text/vector/log/list`
- **说明**：支持按 text 模糊搜索，按创建时间倒序分页返回向量日志列表
- **入参**：`PageRequest<TextVectorLog>`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| currentPage | Integer | 否 | 当前页，默认1 |
| pageSize | Integer | 否 | 每页条数，默认10 |
| data.text | String | 否 | 文本内容（模糊匹配） |

- **返回**：`Result<Page<TextVectorLog>>`

---

### 2. 新增向量日志

- **接口**：`POST /text/vector/log/save`
- **说明**：新增一条文本向量日志记录
- **入参**：`TextVectorLog`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| id | Long | 否 | 主键（对应 Milvus 主键） |
| text | String | 是 | 原始文本内容 |
| vector | String | 否 | 向量数据 |
| source | String | 否 | 来源标识 |
| remark | String | 否 | 备注 |

- **返回**：`Result<Boolean>`

---

### 3. 更新向量日志

- **接口**：`POST /text/vector/log/update`
- **说明**：根据 id 更新文本向量日志记录，id 必传
- **入参**：`TextVectorLog`（同新增，id 必传）
- **返回**：`Result<Boolean>`

---

### 4. 删除向量日志

- **接口**：`POST /text/vector/log/delete/{id}`
- **说明**：根据 id 物理删除一条向量日志记录
- **入参**：Path 参数 `id`（日志ID）
- **返回**：`Result<Boolean>`

---

## 用户管理模块 `/biz/user`（x-user 服务）

### 1. 用户登录

- **接口**：`POST /biz/user/login`
- **说明**：校验账号密码（BCrypt 比对），成功签发 JWT 令牌。用户不存在与密码错误返回统一提示，防账号探测。
- **入参**：`LoginDTO`

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| userName | String | 是 | 用户账号 |
| password | String | 是 | 密码（明文，服务端BCrypt比对） |

- **返回**：`ResultEntity<LoginVO>`（token、userId、userName、nickName、avatar）

---

### 2. 获取用户详情

- **接口**：`GET /biz/user/{id}`
- **说明**：根据用户ID查询详情，不返回 password。
- **入参**：Path 参数 `id`（用户ID）
- **返回**：`ResultEntity<UserVO>`

---

### 3. 新增用户

- **接口**：`POST /biz/user/add`
- **说明**：新增用户，账号唯一校验，密码 BCrypt 加密存储。
- **入参**：`UserDTO`（userName、password 必填，含 nickName、email、phonenumber、sex 等）
- **返回**：`ResultEntity<Long>`（新增用户ID）

---

### 4. 删除用户

- **接口**：`POST /biz/user/delete/{id}`
- **说明**：逻辑删除（del_flag=2）。
- **入参**：Path 参数 `id`（用户ID）
- **返回**：`ResultEntity<Boolean>`

---

### 5. 更新用户

- **接口**：`POST /biz/user/update`
- **说明**：更新用户信息，userId 必传；此接口不允许修改密码。
- **入参**：`UserDTO`（userId 必填）
- **返回**：`ResultEntity<Boolean>`

---

### 6. 分页查询用户列表

- **接口**：`POST /biz/user/list`
- **说明**：多条件分页查询，仅返回未删除用户，不含 password。支持账号/昵称模糊、手机号、状态、部门过滤。
- **入参**：`UserDTO`（current、size 分页 + 过滤字段）
- **返回**：`ResultEntity<IPage<UserVO>>`
