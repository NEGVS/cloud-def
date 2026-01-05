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
    private static final String BASE_File_PATH = "/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/ruoyi-system/src/main/resources/mapper/andy";

    public static void main(String[] args) {
        //1-替换字符串--独立设置包
        replaceFileString();
    }

    public static void replaceFileString() {
        //1-获取所有文件
        Set<String> filePaths = CodeX.listFiles(BASE_File_PATH);
        System.out.println(filePaths);

        System.out.println(filePaths.size());

        for (String filePath : filePaths) {
            //完成包名替换
            CodeX.replaceString(filePath, "andy.", "andy..", true);

        }

    }
}
