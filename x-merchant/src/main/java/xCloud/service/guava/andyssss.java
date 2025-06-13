package xCloud.service.guava;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import xCloud.util.CodeX;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/12 19:35
 * @ClassName andyssss
 */
public class andyssss {
    public static void main(String[] args) {

        System.out.println((17.38 - 16.31) / (16.31));

        DateTime date = DateUtil.date();
        System.out.println(DateUtil.yesterday());
        System.out.println(CodeX.getDate_yyyy_MM_dd());
        System.out.println(CodeX.getDate_yyyy_MM_dd(DateUtil.yesterday()));
//今日价格：16.72
//昨日价格：17.38
        BigDecimal bigDecimal = new BigDecimal("16.72");
        BigDecimal bigDecimal2 = new BigDecimal("17.38");
        BigDecimal difference = bigDecimal.subtract(bigDecimal2);
        BigDecimal rate = difference.divide(bigDecimal2, 10, RoundingMode.UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.UP);


        BigDecimal rate2 = bigDecimal.subtract(bigDecimal2).divide(bigDecimal2, 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
        System.out.println(difference);
        System.out.println(rate);
        System.out.println(rate2);

    }
}
