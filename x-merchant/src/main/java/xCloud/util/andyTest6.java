package xCloud.util;

import cn.hutool.core.date.DateUtil;
import xCloud.service.serviceImpl.CodeX;

import java.util.Date;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/23 19:32
 * @ClassName andyTest6
 */
public class andyTest6 {
    public static void main(String[] args) {


        System.out.println(String.valueOf(DateUtil.tomorrow()).split(" ")[0]);
        System.out.println(CodeX.getDate_yyyy_MM_dd());
        String dateStr = "2025-06-23";
        System.out.println(CodeX.getDate_yyyy_MM_dd(dateStr, -1129993));

        System.out.println();
        int i = DateUtil.dayOfYear(new Date());
        System.out.println(Math.random() * 100);
        System.out.println(Math.random() * 100);
        System.out.println(Math.random() * 100);
        System.out.println(Math.random() * 100);
        System.out.println(Math.random() * 100);
        System.out.println((long) (Math.random() * 100 * DateUtil.dayOfYear(new Date()) / 2));
        System.out.println((long) (Math.random() * 100 * DateUtil.dayOfYear(new Date()) / 2));
        System.out.println((long) (Math.random() * 100 * DateUtil.dayOfYear(new Date()) / 2));
        System.out.println((long) (Math.random() * 100 * DateUtil.dayOfYear(new Date()) / 2));
//        写一个方法，入参数为（datestr，n），返回值为datestr+n天，n为正数，返回datestr+n天，n为负数，返回datestr-n天。
    }
}
