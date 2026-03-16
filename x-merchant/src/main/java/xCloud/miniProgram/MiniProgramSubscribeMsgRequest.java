package xCloud.miniProgram;

import lombok.Data;

import java.util.Map;

/**
 * @Description 微信订阅消息请求参数实体
 * @Author Andy Fan
 * @Date 2026/1/15 19:28
 * @ClassName MiniProgramSubscribeMsgRequest
 */

@Data
public class MiniProgramSubscribeMsgRequest {
    /**
     * 接收者openid
     */
    private String touser;
    /**
     * 模板ID
     */
    private String template_id;
    /**
     * 点击模板卡片后的跳转页面（小程序内路径）
     */
    private String page;
    /**
     * 模板数据（如thing1、time4、thing3）
     */
    private Map<String, MiniProgramTemplateData> data;

    /**
     * 模板数据子项
     */
    @Data
    public static class MiniProgramTemplateData {
        /**
         * 模板值
         */
        private String value;
    }
}
