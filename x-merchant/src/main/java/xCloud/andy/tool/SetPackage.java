package xCloud.andy.tool;

import cn.hutool.json.JSONUtil;
import xCloud.service.serviceImpl.CodeX;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/9/18 20:12
 * @ClassName setPackage
 */
public class SetPackage {


    //1-获取该路径下所有文件
    private static final String BASE_File_PATH = "/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/ruoyi-system/src/main/resources/mapper/demi/stock/stockB";

    //2-【不能修改】Java源文件的基础路径（根据实际项目结构调整）
    // 例如：如果文件完整路径是 /xxx/ruoyi-system/src/main/java/com/xxx/AiRobotVO.java
    // 则基础路径应为 /xxx/ruoyi-system/src/main/java/
    private static final String BASE_JAVA_PATH = "/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/ruoyi-admin/src/main/java/";
//    private static final String BASE_JAVA_PATH = "/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/ruoyi-system/src/main/java/";

    public static void main(String[] args) {
        replaceFileString();
//        getEntityPackages();
        //运行 生成包导入
//        generatePackage();
    }

    public static void replaceFileString() {
        //1-获取所有文件
        Set<String> filePaths = CodeX.listFiles(BASE_File_PATH);
        System.out.println(filePaths);
        System.out.println(filePaths.size());
        for (String filePath : filePaths) {
            CodeX.replaceString(filePath, "ruoyi.demi.stock.stockB", "xCloud.system", true);
        }
    }
    public static void getEntityPackages() {
        //1-获取所有文件
        Set<String> filePaths = CodeX.listFiles(BASE_File_PATH);
        System.out.println(filePaths);
        System.out.println(filePaths.size());
        List<String> lines_final = new ArrayList<>();

        int count = 0;
        for (String filePath : filePaths) {
            try {
                /*
                 *2-读取文件所有行
                 */
                List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    lines_final.add(lines.get(0).split(" ")[1].replace(";", ".") + CodeX.getFileNameA(filePath));
                }
            } catch (IOException e) {
                System.err.println("处理文件时发生错误: " + filePath);
            }
        }
        System.out.println(JSONUtil.toJsonStr(lines_final));
        lines_final.forEach(System.out::println);
    }

    /**
     * 生成包导入
     */
    private static void generatePackage() {
        //1-获取所有文件
        Set<String> filePaths = CodeX.listFiles(BASE_File_PATH);
        System.out.println(filePaths);
        System.out.println(filePaths.size());
        int count = 0;
        for (String filePath : filePaths) {
            System.out.println();
            System.out.println(++count + "处理文件: " + filePath);
            try {
                /*
                 *2-读取文件所有行
                 */
                List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
                /*
                 *3-清除元素，检查列表非空后再删除第一个元素，避免索引越界异常
                 */
                if (!lines.isEmpty()) {
                    lines.remove(0); // 删除第一个元素（索引为0）
                }
                // 关键代码：删除所有包含"domain"的元素（不区分大小写）
//                lines.removeIf(line -> line.contains("domain"));
                lines.removeIf(line -> line.contains(".xCloud."));
                // 若需不区分大小写（如删除"Domain"、"DOMAIN"等），可修改为：
                // lines.removeIf(line -> line.toLowerCase().contains("domain"));


                // 检查是否已包含package声明
//                boolean hasPackage = !lines.isEmpty() && lines.get(0).trim().startsWith("package com");
                // 判断是否是xml文件
                boolean isXml = !lines.isEmpty() && lines.get(0).trim().contains("xml");
                //无包，非xml文件
                if (!isXml) {
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
        // 提取基础路径之后的部分（包路径+文件名）
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
