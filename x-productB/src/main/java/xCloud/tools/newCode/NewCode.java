package xCloud.tools.newCode;

import cn.hutool.json.JSONUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/10/22 20:35
 * @ClassName NewCode
 */
public class NewCode {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();
        int m = in.nextInt();
        System.out.println(n);
        System.out.println(m);

        int[][] items = new int[m + 1][3];
        //价格，重要度，编号
        List<List<Integer>> attachments = new ArrayList<>();
        for (int i = 0; i <= m; i++) {
            attachments.add(new ArrayList<>());
        }
        for (int i = 1; i <= m; i++) {
            for (int j = 0; j < 3; j++) {
                items[i][j] = in.nextInt();
            }
            if (items[i][2] != 0) {
                attachments.get(items[i][2]).add(i);
            }
        }
        System.out.println(JSONUtil.toJsonStr(items));
        System.out.println(JSONUtil.toJsonStr(attachments));
    }
}
