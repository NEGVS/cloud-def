package com.jan.base.util.bankOfCommunications.test;

import com.jan.base.service.task.BankOfCommunicationsTask;
import com.jan.base.util.JANException;
import com.jan.base.util.JANUtil;
import com.jan.base.util.codeGeneration.CodeX;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/5/22 17:14
 * @ClassName ttttest
 */
public class ttttest {

    public static void main(String[] args) {
        CodeX codeX = new CodeX();


        try {

            String estimatePayDate = "2024-07-25 17:00:00";
            BankOfCommunicationsTask task = new BankOfCommunicationsTask();
            task.executePayAgency();

            System.out.println("----------end-= ");
        } catch (DateTimeParseException e) {
            System.err.println("日期格式错误！");
        } catch (JANException e) {
            throw new RuntimeException(e);
        }

    }
}
