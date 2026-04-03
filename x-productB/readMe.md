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
