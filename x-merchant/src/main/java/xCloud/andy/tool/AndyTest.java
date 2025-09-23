package xCloud.andy.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/23 11:35
 * @ClassName andyTest
 */
public class AndyTest {
    public static void main(String[] args) throws JsonProcessingException {
        Integer switchValue = 10;
        System.out.println(switchValue == null || switchValue == 0 ? 1 : switchValue);
        if (true) {
            return;
        }

        int size = 3;
        System.out.println(ThreadLocalRandom.current().nextInt(size));

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree("{\"channelType\":\"PERSON\",\"content\":\"{\\\"content\\\":\\\"{\\\\\\\"id\\\\\\\":\\\\\\\"845933904\\\\\\\",\\\\\\\"name\\\\\\\":\\\\\\\"陈佳宁\\\\\\\",\\\\\\\"tag\\\\\\\":\\\\\\\"官方职业顾问\\\\\\\",\\\\\\\"serviceField\\\\\\\":\\\\\\\"零售·餐饮·物流配送·安保·工厂\\\\\\\",\\\\\\\"dess\\\\\\\":\\\\\\\"免费一对一求职咨询-精准岗位推荐（含未公开机会）-求职指导+面试指导-职业规划建议-岗位真实免费杜绝虚假招聘\\\\\\\",\\\\\\\"desc\\\\\\\":\\\\\\\"【顾问卡片】\\\\\\\"}\\\",\\\"extra\\\":{\\\"configCode\\\":\\\"IM_MSG_CONSULTANT_CARD\\\",\\\"parameter\\\":{},\\\"systemSend\\\":1,\\\"title\\\":\\\"顾问卡片\\\",\\\"urlType\\\":1},\\\"user\\\":{\\\"id\\\":\\\"845934006\\\",\\\"name\\\":\\\"真实姓名\\\",\\\"portrait\\\":\\\"https://job-onsite.oss-cn-shanghai.aliyuncs.com/used/%E9%BB%98%E8%AE%A4%E5%A4%B4%E5%83%8F%E5%BA%93/%E5%A4%B4%E5%83%8F%EF%BC%8Fc%E7%AB%AF%EF%BC%8F4%402x.png\\\"}}\",\"fromUserId\":\"D_test_845933904\",\"msgTimestamp\":1758607163340,\"msgUID\":\"CPEK-JRVJ-4MQ4-98D4\",\"objectName\":\"Message:ConsultantCustomMsg\",\"sensitiveType\":0,\"source\":\"Server\",\"toUserId\":\"C_test_845934006\"}");

// 检查字段是否存在，避免NullPointerException

        if (jsonNode.has("content")) {
            String desc = jsonNode.get("content").asText();
            System.out.println("1 desc");
            System.out.println(desc);
            JsonNode jsonNode2 = objectMapper.readTree(desc);
            if (jsonNode2.has("content")) {
                String desc2 = jsonNode2.get("content").asText();
                System.out.println(desc2);
                System.out.println("2 desc");
                JsonNode jsonNode3 = objectMapper.readTree(desc2);
                if (jsonNode3.has("dess")) {
                    String desc3 = jsonNode3.get("dess").asText();
                    System.out.println("3 dess");
                    System.out.println(desc3);
                }
            }
        }
    }
}
