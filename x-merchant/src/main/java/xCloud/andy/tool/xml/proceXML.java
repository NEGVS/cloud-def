package xCloud.andy.tool.xml;

import xCloud.service.serviceImpl.CodeX;

import java.util.Set;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/24 00:27
 * @ClassName proceXML
 */
public class proceXML {
    public static void main(String[] args) {
        String basePath = "/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/ruoyi-system/src/main/java/";;
        Set<String> set = CodeX.listFiles("/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/ruoyi-system/src/main/java/com/ruoyi/hro/srzp/andyCodeGeneration/mapper");
        for (String filePath : set) {
            System.out.println(filePath.replace(basePath, "")
                    .replace(".java", "")
                    .replace("/", ".")
            );
        }
    }
}
