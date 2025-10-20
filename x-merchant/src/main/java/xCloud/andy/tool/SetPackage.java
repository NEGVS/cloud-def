package xCloud.andy.tool;

import xCloud.service.serviceImpl.CodeX;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/18 20:12
 * @ClassName setPackage
 */
public class SetPackage {


    // Java源文件的基础路径（根据实际项目结构调整）
    // 例如：如果文件完整路径是 /xxx/ruoyi-system/src/main/java/com/xxx/AiRobotVO.java
    // 则基础路径应为 /xxx/ruoyi-system/src/main/java/
    private static final String BASE_JAVA_PATH = "/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/ruoyi-system/src/main/java/";

    public static void main(String[] args) {
        System.out.println("xCloud.andy.tool");
        //该路径下时所有文件
        String packageName2 = "/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/ruoyi-system/src/main/java/com/ruoyi/demi/stock";

        Set<String> filePaths = CodeX.listFiles(packageName2);
        System.out.println(filePaths);
        for (String filePath : filePaths) {
            ///filePath = Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/ruoyi-system/src/main/java/com/ruoyi/hro/srzp/andyCodeGeneration/entity/vo/AiRobotVO.java
            System.out.println(filePath);
            //读取文件
            //判断第一行是否倒入包：包含关键字 package com
            //如果没有，添加倒入包：com.ruoyi.hro.srzp.andyCodeGeneration.entity.vo
            //注意：倒入包是根据文件路径来判断的
            //再写入文件
        }
        for (String filePath : filePaths) {
            System.out.println("处理文件: " + filePath);

            try {
                // 读取文件所有行
                List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

                // 检查是否已包含package声明
                boolean hasPackage = !lines.isEmpty() && lines.get(0).trim().startsWith("package com");
                boolean isXml = !lines.isEmpty() && lines.get(0).trim().contains("xml");
                //无包，非xml文件
                if (!hasPackage && !isXml) {
                    // 根据文件路径生成对应的包名
                    String packageName = generatePackageName(filePath);
                    if (packageName != null) {
                        String packageStatement = "package " + packageName + ";";

                        // 在列表开头插入package声明
                        lines.add(0, packageStatement);
                        lines.add(1, ""); // 添加空行保持格式

                        // 写回文件
                        Files.write(Paths.get(filePath), lines, StandardCharsets.UTF_8);
                        System.out.println("已添加包声明: " + packageStatement + " 到文件: " + filePath);
                    } else {
                        System.out.println("无法生成包名，跳过文件: " + filePath);
                    }
                } else {
                    System.out.println("文件已包含package声明，无需处理: " + filePath);
                }

            } catch (IOException e) {
                System.err.println("处理文件时发生错误: " + filePath);
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据文件路径生成对应的包名
     *
     * @param filePath 文件完整路径
     * @return 包名，如com.ruoyi.hro.srzp.andyCodeGeneration.entity.vo
     */
    private static String generatePackageName(String filePath) {
        // 找到基础路径在文件路径中的位置
//        int baseIndex = filePath.indexOf(BASE_JAVA_PATH);
//        if (baseIndex == -1) {
//            return null;
//        }

        // 提取基础路径之后的部分（包路径+文件名）
//        String packagePathPart = filePath.substring(baseIndex + BASE_JAVA_PATH.length());
        String packagePathPart = filePath.replace(BASE_JAVA_PATH, "");

        // 找到最后一个斜杠的位置（分割目录和文件名）
        int lastSlashIndex = packagePathPart.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            lastSlashIndex = packagePathPart.lastIndexOf('\\'); // 处理Windows路径
        }

        if (lastSlashIndex == -1) {
            return null; // 没有目录结构，无法生成包名
        }

        // 提取包路径部分并替换为点分隔符
        String packagePath = packagePathPart.substring(0, lastSlashIndex);
        return packagePath.replace('/', '.').replace('\\', '.');
    }
}
