package xCloud.util;

import io.swagger.v3.oas.annotations.Operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/5/6 13:52
 * @ClassName andyTest
 */
public class andyTest {

    public static void main(String[] args) {
///Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue _boot/ruoyi-admin/src/main/java/com/ruoyi/web/controller/hc
        lengthOfLongestSubstring("abcddadsx");

        if (true) {
            return;
        }
        String root = "/Users/andy_mac/Documents/CodeSpace/andyProject0/demi_vue_boot/ruoyi-admin/src/main/java/com/ruoyi/web";
        Set<String> strings2 = CodeX.readFileNameList(root);
        for (String path : strings2) {
            System.out.println(path);
            CodeX codeX = new CodeX();
            List<String> strings = codeX.readFileLines(path);

            StringBuilder stringBuilder = new StringBuilder();
            for (String string : strings) {
                if (string.contains("@Schema") && string.contains("description") && !string.contains("name")) {
                    String replace = string.replace("Schema", "ApiModelProperty").replace("description", "value");
                    stringBuilder.append(replace + "\n");
                    System.out.println("****************swagger v3 to v2 ****\n" + path);
                    continue;
                }
                if (string.contains("@Schema") && string.contains("name") && string.contains("description")) {
                    List<String> strings1 = CodeX.regexGetContentByDoubleQuotes(string);
                    string = "@ApiModel(description =\"" + strings1 + "\")";
                    stringBuilder.append(string + "\n");
                    continue;
                }
                if (string.contains("v3.oas")) {
                    continue;
                }
                if (string.contains("@Operation")) {
                    String replace = string.replace("Operation", "ApiOperation").replace("description", "value");
                    stringBuilder.append(replace + "\n");
                    continue;
                }
                stringBuilder.append(string + "\n");
            }

            CodeX.overWrite(path, stringBuilder);

        }
        if (true) {
            return;
        }


    }


    public static int lengthOfLongestSubstring(String s) {
        Map<Character, Integer> map = new HashMap<>(); // 记录字符最后出现的位置
        int maxLen = 0; // 最长子串长度
        int left = 0; // 窗口左边界

        for (int right = 0; right < s.length(); right++) {
            char c = s.charAt(right);
            // 如果字符重复且在窗口内，移动左指针
            if (map.containsKey(c) && map.get(c) >= left) {
                left = map.get(c) + 1;
            }
            // 更新最大长度
            maxLen = Math.max(maxLen, right - left + 1);
            map.put(c, right); // 更新字符位置
        }
        map.entrySet().forEach(System.out::println);
        return maxLen;
    }
}
