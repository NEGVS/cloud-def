以下是 waitingSend 方法的完整执行逻辑分析：
                                                                                                        
---                                                                                                   
waitingSend 执行逻辑分析

整体目标

实现一个防抖 + 批量聚合机制：用户在短时间内连续发消息时，机器人不会每条立即回复，而是等用户"停止输入"
后再统一处理并回复。
                                                                                                        
---                                                                                                   
一、waitingSend(RongSyncMessageRequest) — 入口方法

每当融云回调收到用户发给机器人的消息时触发。

用户发消息 → 融云回调 → waitingSend()

核心逻辑：查找或创建 ImBatchWaitSend 记录，并设置/重置延迟时间。

┌──────────────────┬──────────────────────────────────────────────────────────────────────┐           
│       场景       │                                 操作                                 │
├──────────────────┼──────────────────────────────────────────────────────────────────────┤           
│ 记录不存在       │ 新建记录，status=UNSENT(0)，scheduledTime = 当前时间 + 随机 7~10 秒  │
├──────────────────┼──────────────────────────────────────────────────────────────────────┤
│ 状态=UNSENT(0)   │ 刷新 scheduledTime = 当前时间 + 随机 7~10 秒（防抖延后）             │           
├──────────────────┼──────────────────────────────────────────────────────────────────────┤           
│ 状态=ADD_IND(3)  │ 刷新 scheduledTime = 当前时间 + 随机 7~10 秒（防抖延后）             │           
├──────────────────┼──────────────────────────────────────────────────────────────────────┤           
│ 状态=SENT(已发)  │ 重置 status → UNSENT，scheduledTime = 当前时间 + 7~10 秒（重新触发） │
├──────────────────┼──────────────────────────────────────────────────────────────────────┤           
│ 状态=ING(处理中) │ 设置 status → ADD_IND(3)，scheduledTime = 当前时间 + 40 秒（保护期） │
└──────────────────┴──────────────────────────────────────────────────────────────────────┘

▎ 防抖效果：用户每发一条消息都会重置                                                                  
scheduledTime，只要用户持续发消息，定时任务就永远触发不到（因为时间一直被推后）。用户停止发消息 7~10
秒后，定时任务才能捡起这条记录。

▎ ING 保护期：如果机器人正在处理（ING）时用户又发消息了，不直接打断处理，而是标记 ADD_IND + 40        
秒后再处理，给当前处理留出完成时间。
                                                                                                        
---                                                       
二、processScheduledBatches() — 定时任务调度（每隔几秒轮询）

查询所有 status IN (0, 3) AND scheduledTime <= 当前时间 AND deletedAt IS NULL
→ 对每条记录异步执行 processBatch()

捡起所有到期的 UNSENT/ADD_IND 记录，并发异步处理。
                                                                                                        
---                                                                                                   
三、processBatch(ImBatchWaitSend) — 核心处理逻辑

1. 分布式锁（防并发重复处理）

redisUtil.setNX("SRZP_APP_AI_BATCH_SEND_{id}", key, 45秒)
同一批次只有一个线程能进入，45 秒后自动释放。

2. 重新读库校验状态

防止从查询到获取锁之间状态已经变化，如果状态不是 UNSENT/ADD_IND 则直接返回。

3. 设置状态 → ING，记录 lastSendTime

waitSend.setStatus(ING)                                                                               
waitSend.setLastSendTime(new Date())

4. 查询待处理消息

从 im_user_robot_msg 表中查询：
- userImUid + robotImUid 匹配
- conduct = 1（用户发的消息）
- id > lastMsgId（上次处理位置之后的新消息）

5. 消息为空 → 直接跳过（释放锁）

6. 查找 AI 会话关系

- 查 ImUserRobotList（融云会话关系表）
- 查 AiRobotUserRelation（AI 机器人绑定关系表）
- 若不存在 → 重置 UNSENT，释放锁，退出

7. 检查是否人工接管

if (aiRobotUserRelation.getIsTakeOver() == 1) → 状态设 SENT，备注"人工接管不处理"，退出

8. 检查并创建 AI 会话（checkAndAddSession）

9. 调用 AI（imChat）

- 过滤出文本/图片/地址消息（RC:TxtMsg、RC:ImgMsg、Message:LocationCustomMsg）
- 封装 ImChatRecordRequest，调用 autoClient.imChat()
- 返回结果：1=成功，2=调用服务失败，3=发送失败

10. AI 调用失败 → 重置 UNSENT 重试

11. 检查处理期间是否有新消息（ADD_IND 二次触发）

if (waitSend2.getStatus() == ADD_IND) {                                                               
→ 重置 UNSENT                                                                                     
→ 释放锁                                                                                          
→ 递归调用 processBatch(waitSend2)  // 立即再处理一次                                             
}                                                                                                     
这是处理"处理期间用户又发消息了"的情况。

12. 无新消息 → 设置 SENT，释放锁，完成

  ---                                                                                                   
四、状态流转总图

用户发消息                                                
↓                                                                                                 
waitingSend()                                                                                         
├── 不存在 → 新建 UNSENT，scheduledTime = +7~10s                                                  
├── UNSENT → 刷新 scheduledTime = +7~10s（防抖）                                                  
├── ADD_IND → 刷新 scheduledTime = +7~10s（防抖）                                                 
├── SENT → 重置 UNSENT，scheduledTime = +7~10s                                                    
└── ING → ADD_IND，scheduledTime = +40s（保护期）

定时轮询 processScheduledBatches()                                                                    
↓ 到期的 UNSENT/ADD_IND                                                                           
processBatch()                                                                                        
├── setNX 分布式锁（45s）                                                                         
├── 重新校验状态                                                                                  
├── → ING                                                                                         
├── 查新消息                                                                                      
├── 查会话/绑定关系                                                                               
├── 人工接管? → SENT（跳过）                                                                      
├── checkAndAddSession()                                                                          
├── imChat() → AI回复                                                                             
│

✻ Cooked for 3m 19s                                                                                     
