package xCloud.openAiChatModel.ali;

/**
 * @Description 文本 转 向量
 * @Author Andy Fan
 * @Date 2025/11/5 19:56
 * @ClassName Main
 * 模型选择
 * 选择合适的模型取决于您的输入数据类型和应用场景。
 * <p>
 * 处理纯文本或代码：推荐使用 text-embedding-v4。它是当前性能最强的模型，支持任务指令（instruct）、稀疏向量等高级功能，能覆盖绝大多数文本处理场景。
 * <p>
 * 处理多模态内容：
 * <p>
 * 统一多模态向量：若要将单模态或混合模态输入表征为统一向量，适用于跨模态检索、图搜等场景，可使用 qwen2.5-vl-embedding。例如，输入一张衬衫图片并附加文本“找相似风格但更显年轻的款式”，模型能将图像和文本指令融合成一个向量进行理解。
 * <p>
 * 独立向量：若要为每个输入（如图片和其对应的文字标题）生成独立的向量，可选择 tongyi-embedding-vision-plus 、 tongyi-embedding-vision-flash或通用多模态模型multimodal-embedding-v1为每个输入部分（图片、文字）生成一个独立的向量。
 * <p>
 * 处理大规模数据：若您需要处理大规模、非实时的文本数据，建议使用 text-embedding-v4 并结合 OpenAI兼容-Batch调用，以显著降低成本。
 * <p>
 * 下表包含所有可用向量化模型的详细规格。
 *
 * text-embedding-v4
 * 是通义实验室基于Qwen3训练的多语言文本统一向量模型，相较V3版本在文本检索、聚类、分类性能大幅提升；在MTEB多语言、中英、Code检索等评测任务上效果提升15%~40%；支持64~2048维用户自定义向量维度。
 *
 */

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.Constants;

import java.util.Collections;

public class Main {
    /**
     * 若使用新加坡地域的模型，请取消以下注释
     * static {
     * Constants.baseHttpApiUrl="https://dashscope-intl.aliyuncs.com/api/v1";
     * }
     */
    public static void main(String[] args) {
        // 手动从环境变量获取并设置 API Key
        String apiKey = System.getenv("DASHSCOPE_API_KEY");
        System.out.println(apiKey);

        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("环境变量 DASHSCOPE_API_KEY 未设置");
        }
        System.out.println(apiKey);

        String inputTexts = "衣服的质量杠杠的";
        try {
            // 构建请求参数
            TextEmbeddingParam param = TextEmbeddingParam
                    .builder()
                    .model("text-embedding-v4")
                    // 输入文本
                    .texts(Collections.singleton(inputTexts))
                    .build();

            // 创建模型实例并调用
            TextEmbedding textEmbedding = new TextEmbedding();
            TextEmbeddingResult result = textEmbedding.call(param);

            // 输出结果
            System.out.println(result);

        } catch (NoApiKeyException e) {
            // 捕获并处理API Key未设置的异常
            System.err.println("调用 API 时发生异常: " + e.getMessage());
            System.err.println("请检查您的 API Key 是否已正确配置。");
            e.printStackTrace();
        }
    }
}