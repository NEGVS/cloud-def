package xCloud.tools.newCode;

import org.apache.catalina.User;
import xCloud.entity.dto.StockAllDTO;

import java.util.Objects;
import java.util.Scanner;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/11/17 14:48
 * @ClassName NewCode11
 */
public class NewCode11 {
    public static void main(String[] args) {
        StockAllDTO stockAllDTO = new StockAllDTO();
        stockAllDTO.setId(null);
        Integer ddd = null;
        //不推荐
        System.out.println(stockAllDTO.getId() != 3);
        //推荐使用
        System.out.println(Objects.equals(3, stockAllDTO.getId()));

        if (true) {
            return;
        }

        System.out.println("----22------");

        Integer aa = null;
        if (3 != aa) {
            System.out.println("aa==3");
        }
        System.out.println("----------");

        if (true) {
            return;
        }
        Scanner in = new Scanner(System.in);
        // 注意 hasNext 和 hasNextLine 的区别
        //
        //order 服务订单 status==3的，
        //
        //
        while (in.hasNextInt()) { // 注意 while 处理多个 case
            int a = in.nextInt();
            int b = in.nextInt();
            System.out.println(a + b);
        }
    }

    /**
     * 对于给定的由大小写字母、数字和空格混合构成的字符串 S，给定字符c，按要求统计：
     * 若c为大写或者小写字母，统计其大小写形态出现的次数和；
     * 若c为数字，统计其出现的次数。
     * 保证字符c要么为字母、要么为数字。
     *
     * @param s
     * @param c
     * @return
     */
    public int getSum(String s, String c) {

        for (int i = 0; i < s.length(); i++) {
            char a = s.charAt(i);
        }

        return 1;
    }
}
