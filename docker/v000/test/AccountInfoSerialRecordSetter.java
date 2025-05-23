package com.jan.base.util.bankOfCommunications.test;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/5/21 15:29
 * @ClassName AccountInfoSerialRecordSetter
 */

import com.jan.base.util.bankOfCommunications.request.body.PayAgencyDetail;
import com.jan.base.util.bankOfCommunications.response.serialRecord.AccountInfoSerialRecord;
import com.jan.base.util.codeGeneration.CodeX;

import java.lang.reflect.Field;

public class AccountInfoSerialRecordSetter {
    public static String generateSetterCode(String AccountInfoSerialRecord, Class clazz) {
        // 创建 StringBuilder 用于拼接代码
        StringBuilder code = new StringBuilder();

        code.append(AccountInfoSerialRecord+" " + CodeX.firstToSmall(AccountInfoSerialRecord) + "  = new " + AccountInfoSerialRecord + "();\n");

        // 获取 AccountInfoSerialRecord 类的所有字段
        Field[] fields = clazz.getDeclaredFields();
        int count = 0;
        // 遍历所有字段
        for (Field field : fields) {
            // 获取字段名
            String fieldName = field.getName();
            // 将字段名首字母大写，用于生成 setter 方法名
            String setterName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

            // 根据字段类型设置默认值
//            String defaultValue = getDefaultValueForType(field.getType());
            String defaultValue = "split[" + (((++count) + fields.length) - 1) + "]";

            // 生成 setter 方法调用代码
            code.append(String.format(CodeX.firstToSmall(AccountInfoSerialRecord) + ".%s(%s);\n", setterName, defaultValue));
        }

        return code.toString();
    }

    // 根据字段类型返回默认值
    private static String getDefaultValueForType(Class<?> type) {
        if (type == String.class) {
            return "\"\""; // 字符串类型的默认值为空字符串
        } else if (type == int.class || type == Integer.class) {
            return "0";
        } else if (type == double.class || type == Double.class) {
            return "0.0";
        } else if (type == boolean.class || type == Boolean.class) {
            return "false";
        } else {
            return "null"; // 其他类型的默认值为 null
        }
    }

    // 主方法用于测试
    public static void main(String[] args) {
        System.out.println(generateSetterCode("PayAgencyDetail", PayAgencyDetail.class));
    }
}