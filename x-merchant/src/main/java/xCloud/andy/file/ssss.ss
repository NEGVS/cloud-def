
B端 点开会话时，回调 告诉c端，该信息已读。 该信息应该时 from c to b;      目前回调的是 from b to c
--1--------syncMessage {"channelType":"PERSON","content":"{\"messageUId\":\"CPEN-GFGA-KC45-CB7A\",\"lastMessageSendTime\":1758619301930,\"type\":1}",
"fromUserId":"D_test_845933903","msgTimestamp":1758619309009,"msgUID":"CPEN-GH7K-ECA4-98EU","objectName":"RC:ReadNtf","sensitiveType":0,"source":"Websocket",
"toUserId":"C_test_845934022"}
17:21:49.031 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.r.A.selectByExample | ==>  Preparing: SELECT id,user_id,name,account,robot_wecom_account,avatar,member_rating,success_service_count,service_years,enterprise,service_fields,service_intro,personal_intro,delivery_id,delivery_sr_user_id,delivery_name,delivery_account,binding_status,created_at,updated_at,deleted_at FROM ai_robot WHERE ( ( deleted_at is null ) )
17:21:49.031 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.r.A.selectByExample | ==> Parameters:
17:21:49.031 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.r.A.selectByExample | <==      Total: 3
17:21:49.031 | http-nio-19203-exec-1 | INFO  | c.x.j.s.m.impl.RongCloudServiceImpl |


--2--------aiRobots_userIds ["845933845","845933903","845933904"]
17:21:49.032 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.a.I.selectByExample | ==>  Preparing: SELECT id,uid,im_uid,im_token,u_role,is_online,wechat_id,name,header_url,os,session_id,status_change_time,created_at,updated_at,deleted_at FROM im_user_info WHERE ( ( deleted_at is null ) and ( uid in ( ? , ? , ? ) ) )
17:21:49.032 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.a.I.selectByExample | ==> Parameters: 845933845(String), 845933903(String), 845933904(String)
17:21:49.032 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.a.I.selectByExample | <==      Total: 6
17:21:49.033 | http-nio-19203-exec-1 | INFO  | c.x.j.s.m.impl.RongCloudServiceImpl |


---3-------imUserInfos [{"createdAt":1756957932000,"headerUrl":"https://kbgz-public.oss-cn-shanghai.aliyuncs.com/qywx/video/20250904115223594_221839857419423744.png?x-oss-process=image/format,png","id":23707,"imToken":"Cdanynd47IxO8Jjh4i1BCoCr5/2Ogo5TFPIWlA4CdehRlTSuZ0vXkw==@2wzg.cn.rongnav.com;2wzg.cn.rongcfg.com","imUid":"C_test_845933845","isOnline":0,"name":"吴书晨","uRole":1,"uid":845933845,"updatedAt":1758102031000},{"createdAt":1756957935000,"headerUrl":"https://kbgz-public.oss-cn-shanghai.aliyuncs.com/qywx/video/20250904115223594_221839857419423744.png?x-oss-process=image/format,png","id":23708,"imToken":"QpMlG/V15qZO8Jjh4i1BCoCr5/2Ogo5TFPIWlA4Cdegak8ytcUr/pw==@2wzg.cn.rongnav.com;2wzg.cn.rongcfg.com","imUid":"D_test_845933845","isOnline":0,"name":"吴书晨","uRole":2,"uid":845933845,"updatedAt":1758102031000},{"createdAt":1757916344000,"headerUrl":"https://kbgz-public.oss-cn-shanghai.aliyuncs.com/qywx/video/20250915140632546_225859883692593152.png?x-oss-process=image/format,png","id":23830,"imToken":"Cdanynd47IxO8Jjh4i1BCohWRTOvBjBEFPIWlA4CdehKBdQFuS4Qqg==@2wzg.cn.rongnav.com;2wzg.cn.rongcfg.com","imUid":"C_test_845933903","isOnline":0,"name":"张俊伟","uRole":1,"uid":845933903,"updatedAt":1758101975000},{"createdAt":1757916349000,"headerUrl":"https://kbgz-public.oss-cn-shanghai.aliyuncs.com/qywx/video/20250915140632546_225859883692593152.png?x-oss-process=image/format,png","id":23831,"imToken":"QpMlG/V15qZO8Jjh4i1BCohWRTOvBjBEFPIWlA4Cdehj81eRUXSwBg==@2wzg.cn.rongnav.com;2wzg.cn.rongcfg.com","imUid":"D_test_845933903","isOnline":0,"name":"张俊伟","uRole":2,"uid":845933903,"updatedAt":1758279133000},{"createdAt":1757916551000,"headerUrl":"https://kbgz-public.oss-cn-shanghai.aliyuncs.com/qywx/video/20250915141052287_225860973125308416.png?x-oss-process=image/format,png","id":23832,"imToken":"Cdanynd47IxO8Jjh4i1BCkkScUrwALGVFPIWlA4CdegqGfQmHbDqAA==@2wzg.cn.rongnav.com;2wzg.cn.rongcfg.com","imUid":"C_test_845933904","isOnline":0,"name":"陈佳宁","uRole":1,"uid":845933904,"updatedAt":1758101993000},{"createdAt":1757916599000,"headerUrl":"https://kbgz-public.oss-cn-shanghai.aliyuncs.com/qywx/video/20250915141052287_225860973125308416.png?x-oss-process=image/format,png","id":23833,"imToken":"QpMlG/V15qZO8Jjh4i1BCkkScUrwALGVFPIWlA4CdeiajNbfPTQ75A==@2wzg.cn.rongnav.com;2wzg.cn.rongcfg.com","imUid":"D_test_845933904","isOnline":0,"name":"陈佳宁","uRole":2,"uid":845933904,"updatedAt":1758101993000}]
17:21:49.033 | http-nio-19203-exec-1 | INFO  | c.x.j.s.m.impl.RongCloudServiceImpl |


----4------提取所有的aiRobots_ImUids ["C_test_845933845","D_test_845933845","C_test_845933903","D_test_845933903","C_test_845933904","D_test_845933904"]
17:21:49.033 | http-nio-19203-exec-1 | INFO  | c.x.j.s.m.impl.RongCloudServiceImpl |



--5--------处理机器人相关消息
17:21:49.033 | http-nio-19203-exec-1 | INFO  | c.x.j.s.m.impl.RongCloudServiceImpl |



----5.2------处理已读消息--只更新
17:21:49.033 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.r.I.updateByExampleSelective | ==>  Preparing: UPDATE im_user_robot_msg SET is_read = ?,updated_at = ? WHERE ( ( deleted_at is null ) and ( user_im_uid = ? and robot_im_uid = ? and conduct = ? and created_at < ? ) )
17:21:49.033 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.r.I.updateByExampleSelective | ==> Parameters: 1(Integer), 2025-09-23 17:21:49.033(Timestamp), C_test_845934022(String), D_test_845933903(String), 2(Integer), 2025-09-23 17:21:49(String)
17:21:49.034 | http-nio-19203-exec-6 | INFO  | com.xrl.java.extend.aop.WebLogAspect |
接口标题：before 获取AI顾问未读消息总数
请求标识：e0bc25e2-a896-44e5-812f-aa150e64a5ec
请求参数：{"deliveryIds":[51170325]}
请求方法：com.xrl.java.controller.robot.AiRobotController.unReadCount()

17:21:49.035 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.r.A.selectByExample | ==>  Preparing: SELECT id,user_id,name,account,robot_wecom_account,avatar,member_rating,success_service_count,service_years,enterprise,service_fields,service_intro,personal_intro,delivery_id,delivery_sr_user_id,delivery_name,delivery_account,binding_status,created_at,updated_at,deleted_at FROM ai_robot WHERE ( ( deleted_at is null ) and ( delivery_id in ( ? ) ) ) order by updated_at DESC
17:21:49.035 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.r.I.updateByExampleSelective | <==    Updates: 2
17:21:49.035 | http-nio-19203-exec-1 | INFO  | c.x.j.s.m.impl.RongCloudServiceImpl |

更新已读成功:handleReadNotificationToUser
17:21:49.035 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.r.A.selectByExample | ==> Parameters: 51170325(Integer)
17:21:49.036 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.a.I.updateByExampleSelective | ==>  Preparing: UPDATE im_user_msg SET is_read = ?,updated_at = ? WHERE ( ( deleted_at is null ) and ( from_im_uid = ? and to_im_uid = ? and push_time <= ? and is_read = ? and object_name in ( ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? , ? ) ) )
17:21:49.036 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.r.A.selectByExample | <==      Total: 1
17:21:49.036 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.a.I.updateByExampleSelective | ==> Parameters: 1(Integer), 2025-09-23 17:21:49.035(Timestamp), C_test_845934022(String), D_test_845933903(String), 2025-09-23 17:21:41.93(Timestamp), 0(Integer), RC:TxtMsg(String), RC:HQVCMsg(String), RC:ImgMsg(String), RC:GIFMsg(String), RC:ImgTextMsg(String), RC:LBSMsg(String), RC:SightMsg(String), Message:TipsInfoCustomMsg(String), Message:GetWechatMobileCustomMsg(String), Message:ApplyCustomMsg(String), Message:JobInfoCustomMsg(String), Message:ResumeInfoCustomMsg(String)
17:21:49.037 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.r.I.selectByExample | ==>  Preparing: SELECT id,user_im_uid,user_uid,robot_uid,robot_im_uid,message,msg_uid,push_time,source,is_read,conduct,created_at,updated_at,deleted_at FROM im_user_robot_msg WHERE ( ( deleted_at is null ) and ( is_read = ? and conduct = ? and robot_uid in ( ? ) ) )
17:21:49.037 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.r.I.selectByExample | ==> Parameters: 0(Integer), 1(Integer), 845933903(Integer)
17:21:49.038 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.r.I.selectByExample | <==      Total: 13
17:21:49.038 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.a.I.updateByExampleSelective | <==    Updates: 1
17:21:49.039 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.r.A.selectByExample | ==>  Preparing: SELECT id,im_relation_id,robot_user_id,robot_id,robot_name,user_id,is_take_over,take_over_at,created_at,updated_at,deleted_at FROM ai_robot_user_relation WHERE ( ( deleted_at is null ) and ( robot_user_id in ( ? ) ) )
17:21:49.039 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.r.A.selectByExample | ==> Parameters: 845933903(Integer)
17:21:49.039 | http-nio-19203-exec-1 | INFO  | c.x.j.s.m.impl.RongCloudServiceImpl | 更新的条数 1
17:21:49.039 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.a.ImUserMsgMapper.insert | ==>  Preparing: INSERT INTO im_user_msg ( id,from_im_uid,to_im_uid,from_uid,to_uid,from_role,to_role,object_name,content,im_content,msg_uid,uid_str,push_time,source,channel_type,is_read,recovered,code,conduct,created_at,updated_at,deleted_at,im_uid_str ) VALUES( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )
17:21:49.040 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.r.A.selectByExample | <==      Total: 8
17:21:49.040 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.a.ImUserMsgMapper.insert | ==> Parameters: null, D_test_845933903(String), C_test_845934022(String), 845933903(Integer), 845934022(Integer), 2(Integer), 1(Integer), RC:ReadNtf(String), {"messageUId":"CPEN-GFGA-KC45-CB7A","lastMessageSendTime":1758619301930,"type":1}(String), null, CPEN-GH7K-ECA4-98EU(String), 845933903,845934022(String), 2025-09-23 17:21:49.0(Timestamp), Websocket(String), PERSON(String), 0(Integer), 0(Integer), null, 1(Integer), 2025-09-23 17:21:49.03(Timestamp), 2025-09-23 17:21:49.03(Timestamp), null, D_test_845933903,C_test_845934022(String)
17:21:49.040 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.a.I.selectByExample | ==>  Preparing: SELECT id,uid,im_uid,im_token,u_role,is_online,wechat_id,name,header_url,os,session_id,status_change_time,created_at,updated_at,deleted_at FROM im_user_info WHERE ( ( deleted_at is null ) and ( uid in ( ? ) and u_role = ? ) )
17:21:49.040 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.a.I.selectByExample | ==> Parameters: 845933903(Integer), 2(Integer)
17:21:49.041 | http-nio-19203-exec-6 | DEBUG | c.x.j.m.a.I.selectByExample | <==      Total: 1
17:21:49.041 | http-nio-19203-exec-1 | DEBUG | c.x.j.m.a.ImUserMsgMapper.insert | <==    Updates: 1
17:21:49.041 | http-nio-19203-exec-6 | INFO  | com.xrl.java.extend.aop.WebLogAspect |
接口标题：after 获取AI顾问未读消息总数