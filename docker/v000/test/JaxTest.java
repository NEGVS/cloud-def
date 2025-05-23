package com.jan.base.util.bankOfCommunications.test;


import com.alibaba.fastjson.JSON;
import com.jan.base.util.bankOfCommunications.BankOfCommunicationsConstants;
import com.jan.base.util.bankOfCommunications.BankOfCommunicationsUtil;
import com.jan.base.util.bankOfCommunications.PaymentTypeEnum;
import com.jan.base.util.bankOfCommunications.request.body.AccountInfoReqBody;
import com.jan.base.util.bankOfCommunications.request.body.HistoryTransactionReqBody;
import com.jan.base.util.bankOfCommunications.request.body.PayAgencyDetail;
import com.jan.base.util.bankOfCommunications.request.AccountInfoRequest;
import com.jan.base.util.bankOfCommunications.request.HistoryTransactionRequest;
import com.jan.base.util.bankOfCommunications.request.PayAgencyRequest;
import com.jan.base.util.bankOfCommunications.request.QueryTransactionRequest;
import com.jan.base.util.bankOfCommunications.request.body.PayAgencyReqBody;
import com.jan.base.util.bankOfCommunications.request.body.PayAgencyReqTran;
import com.jan.base.util.bankOfCommunications.request.body.QueryTransactionReqBody;
import com.jan.base.util.bankOfCommunications.request.head.ReqHead;
import com.jan.base.util.bankOfCommunications.request.head.RequestHeader;
import com.jan.base.util.bankOfCommunications.response.AccountInfoResponse;
import com.jan.base.util.bankOfCommunications.response.HistoryTransactionResponse;
import com.jan.base.util.bankOfCommunications.response.PayAgencyResponse;
import com.jan.base.util.bankOfCommunications.response.QueryTransactionResponse;
import com.jan.base.util.bankOfCommunications.response.serialRecord.AccountInfoSerialRecord;
import com.jan.base.util.bankOfCommunications.response.body.AccountInfoResBody;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description JAXB 使用案例测试，包含 XML 与 Java 对象的互相转换（序列化与反序列化），使用 Jakarta EE 的 jakarta.xml.bind 包（适用于现代 Java 项目）。
 * @Author Andy Fan
 * @Date 2025/5/16 11:41
 * @ClassName JaxTest
 * <p>
 * <p>
 * 1-间隔31天，是否包含31？
 * 5.1---5.31，是间隔30天，允许
 * 5.1---6.1，正好是间隔31天， 6.1是否允许？
 * 5.1---6.2，间隔32天， 不允许
 */
public class JaxTest {
    public static void main(String[] args) {

        try {
            List<PayAgencyDetail> rcds = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                PayAgencyDetail payAgencyDetail = new PayAgencyDetail();
                if (i > 0) {
                    payAgencyDetail.setBusummary(String.valueOf(i));
                }else {
                    payAgencyDetail.setBusummary("");
                }
                rcds.add(payAgencyDetail);
            }
            boolean allNonEmpty = rcds.stream()
                    .noneMatch(detail -> detail.getBusummary() == null || detail.getBusummary().isEmpty());

            String trCode = allNonEmpty ? "330012" : "330008";


            System.out.println(trCode);
            // 3. 将XML转回对象（反序列化）
//            testResponse();

//            AccountInfoResponse accountInfoResponse = new AccountInfoResponse();
//            AccountInfoResponse testeeee = testeeee(accountInfoResponse, AccountInfoResponse.class);
//
//            System.out.println(JSON.toJSONString(testeeee));

            String sss = "adsf\n\t\t";
            System.out.println(sss.replaceAll("\\s", ""));
            System.out.println(sss.trim());
            System.out.println(sss.replace("\n", "").replace("\t", ""));
//            testResponse1();
//            ttttest1();
            System.out.println("-------------------------1--ok");
//            ttttest2();
            System.out.println("-------------------------2--ok");
//            body.setStartDate(String.valueOf(LocalDate.parse("2025-05-01")));
//
//            body.setStartDate(startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

            // 定义格式（年月日无分隔符）
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

            // 解析字符串
            LocalDate endDat2e = LocalDate.parse("20250501", formatter);
            System.out.println(endDat2e); // 输出：2025-05-01
//            ttttest3();
            System.out.println("-------------------------3--ok");

//            ttttest4();
            System.out.println("-------------------------4--ok");

            ttttest5();

        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 比较时间
     *
     * @param startDate
     * @param endDate
     * @return
     */
    public static String compareDates(LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("参数不能为null");
        }

        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("结束时间必须大于开始时间");
        }

        if (startDate.isBefore(LocalDate.now().minusYears(2))) {
            throw new IllegalArgumentException("只能查询2年内的纪录");
        }
        System.out.println(ChronoUnit.DAYS.between(startDate, endDate));
        if (ChronoUnit.DAYS.between(startDate, endDate) > 31) {
            throw new IllegalArgumentException("时间段不能超过31天");
        }
        return "时间段有效";
    }


    /**
     * 返回实体测试 1-账户信息查询
     */
    public static <T> T testeeee(T t, Class<?> clazz) throws Exception {
        String sstr = "户名|账号|币种|余额|可用余额|开户日期|账户类型|开户行|错误信息|成功标志|峡致禽些柯坯谦潞殷虑|110060576018150007238|CNY|749461855.49|749461855.49|20040616|2|110999|账户信息查询成功|0|";
        String[] split = sstr.split("\\|");
        //获取所有字段
        Field[] declaredFields = clazz.getDeclaredFields();
        int count = 0;
        int length = declaredFields.length;
        for (Field field : declaredFields) {
            //给字段赋值
            if (field.getType().getName().contains("BigDecimal")) {
                field.set(t, new BigDecimal(split[(++count + length) - 1]));
                continue;
            }
            field.set(t, split[(++count + length) - 1]);
        }
        return t;
    }

    /**
     * 多个
     *
     * @return
     * @throws Exception
     */
    public static int testResponse() throws Exception {

        String responseXml = "户名|账号|币种|余额|可用余额|开户日期|账户类型|开户行|错误信息|成功标志|峡致禽些柯坯谦潞殷虑|110060576018150007238|CNY|749461855.49|749461855.49|20040616|2|110999|账户信息查询成功|0||110899999601003000358|||||||校验账户失败或未查询到对应授权账户，账户信息查询失败|1|";
        //解析多域串
        List<AccountInfoSerialRecord> accountInfoSerialRecords = new ArrayList<>();
        //获取多域串
        String[] split = responseXml.split("\\|");
        //获取所有字段
        Field[] declaredFields = AccountInfoSerialRecord.class.getDeclaredFields();
        int count = 0;

        //参数总数
        int split_length = split.length;
        //实体字段总数
        int length = declaredFields.length;
//        户名|账号|币种|余额|可用余额|开户日期|账户类型|开户行|错误信息|成功标志|
//        峡致禽些柯坯谦潞殷虑|110060576018150007238|CNY|749461855.49|749461855.49|20040616|2|110999|账户信息查询成功|0|
//        |110899999601003000358|||||||校验账户失败或未查询到对应授权账户，账户信息查询失败|1|
        int sum = split_length / length - 1;
        for (int i = 0; i < sum; i++) {
            AccountInfoSerialRecord accountInfoSerialRecord = new AccountInfoSerialRecord();

            for (Field field : declaredFields) {

                String val = split[(++count + length) - 1].trim().replace("\\\n", "").replace("\\\t", "");
                //给字段赋值
                if (field.getType().getName().contains("BigDecimal")) {
                    if (val.isEmpty()) {
                        val = "0.00";
                    }
                    field.set(accountInfoSerialRecord, new BigDecimal(val));
                    continue;
                }

                field.set(accountInfoSerialRecord, val);
            }
            accountInfoSerialRecords.add(accountInfoSerialRecord);
        }
        System.out.println("----------AccountInfoSerialRecord:\n" + JSON.toJSONString(accountInfoSerialRecords));

        return 1;
    }

    public static int testResponse1() throws Exception {

        String kk = "<ap><head><tr_code>310101</tr_code><corp_no>9000007196</corp_no><req_no>5796a12a829b4409b31c</req_no><serial_no>\n" +
                "\t\t</serial_no><ans_no>\n" +
                "\t\t</ans_no><next_no>\n" +
                "\t\t</next_no><tr_acdt>20250522</tr_acdt><tr_time>093308</tr_time><ans_code>0</ans_code><ans_info>\n" +
                "\t\t</ans_info><particular_code>0000</particular_code><particular_info>交易成功</particular_info><atom_tr_count>1</atom_tr_count><reserved></reserved></head><body><serial_record>户名|账号|币种|余额|可用余额|开户日期|账户类型|开户行|错误信息|成功标志|峡致禽些柯坯谦潞殷虑|110060576018150007238|CNY|749461855.49|749461855.49|20040616|2|110999|账户信息查询成功|0|</serial_record><field_num>10</field_num><record_num>1</record_num><file_flag>0</file_flag><filename></filename></body></ap>";
        String responseStr = "<ap><head><tr_code>310101</tr_code><corp_no>9000007196</corp_no><req_no>0612d38816cd4692934a</req_no><serial_no>\n" +
                "\t\t</serial_no><ans_no>\n" +
                "\t\t</ans_no><next_no>\n" +
                "\t\t</next_no><tr_acdt>20250521</tr_acdt><tr_time>131840</tr_time><ans_code>0</ans_code><ans_info>\n" +
                "\t\t</ans_info><particular_code>0000</particular_code><particular_info>交易成功</particular_info><atom_tr_count>1</atom_tr_count><reserved></reserved></head><body><serial_record>户名|账号|币种|余额|可用余额|开户日期|账户类型|开户行|错误信息|成功标志|峡致禽些柯坯谦潞殷虑|110060576018150007238|CNY|749461855.49|749461855.49|20040616|2|110999|账户信息查询成功|0|</serial_record><field_num>10</field_num><record_num>1</record_num><file_flag>0</file_flag><filename></filename></body></ap>";
        try {
            BankOfCommunicationsUtil bankOfCommunicationsUtil = new BankOfCommunicationsUtil();
            AccountInfoResponse accountInfoResponse = bankOfCommunicationsUtil.fromXml(responseStr, AccountInfoResponse.class);
            System.out.println("************start**********");
            System.out.println(JSON.toJSONString(accountInfoResponse));
            AccountInfoResBody body = accountInfoResponse.getBody();

            accountInfoResponse.getHead();


            System.out.println("-------end------");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 请求体测试 1-账户信息查询-1,多账号，ok
     */
    public static int ttttest1() throws Exception {

        System.out.println("* *********************");
        List<String> accountNos = new ArrayList<>();
        accountNos.add("110060576018150007238");
        accountNos.add("110899999601003000358");
        AccountInfoRequest accountInfoRequest = new AccountInfoRequest();
        List<AccountInfoReqBody> bodies = new ArrayList<>();

        for (String accountNo : accountNos) {
            AccountInfoReqBody body = new AccountInfoReqBody();
            body.setAcno(accountNo);
            body.setType(PaymentTypeEnum.ALIPAY.getCode());
            bodies.add(body);
        }
        accountInfoRequest.setBodies(bodies);

        BankOfCommunicationsUtil bankOfCommunicationsUtil = new BankOfCommunicationsUtil();
        System.out.println("-------send------");
        AccountInfoResponse accountInfoResponse = bankOfCommunicationsUtil.queryAccountInfo(accountInfoRequest);
        System.out.println("-------返回值------");
        System.out.println(JSON.toJSONString(accountInfoResponse));
        System.out.println("-------end------");
        return 1;
    }

    /**
     * 请求体测试 2-账户信息查询-1,单个账号，ok
     */
    public static int ttttest2() throws Exception {
        System.out.println("* *********************");
        List<String> accountNos = new ArrayList<>();
        accountNos.add("110060576018150007238");
        AccountInfoRequest accountInfoRequest = new AccountInfoRequest();
        List<AccountInfoReqBody> bodies = new ArrayList<>();
        for (String accountNo : accountNos) {
            AccountInfoReqBody body = new AccountInfoReqBody();
            body.setAcno(accountNo);
            body.setType(PaymentTypeEnum.ALIPAY.getCode());
            bodies.add(body);
        }
        accountInfoRequest.setBodies(bodies);
        BankOfCommunicationsUtil bankOfCommunicationsUtil = new BankOfCommunicationsUtil();

        AccountInfoResponse accountInfoResponse = bankOfCommunicationsUtil.queryAccountInfo(accountInfoRequest);
        System.out.println("-------返回值------");
        System.out.println(JSON.toJSONString(accountInfoResponse));
        System.out.println("-------end------");
        return 1;
    }

    /**
     * 请求体测试 3-历史明细查询-单账号
     */
    public static int ttttest3() throws JAXBException {

        System.out.println("* *********************");
        try {
            HistoryTransactionRequest historyTransactionRequest = new HistoryTransactionRequest();

            HistoryTransactionReqBody body = new HistoryTransactionReqBody();
            body.setAcNo("110060576018150007238");
            body.setStartDate(String.valueOf(LocalDate.parse("2025-05-01")));
            body.setEndDate(String.valueOf(LocalDate.parse("2025-05-11")));
            historyTransactionRequest.setBody(body);
            BankOfCommunicationsUtil bankOfCommunicationsUtil = new BankOfCommunicationsUtil();
            HistoryTransactionResponse response = bankOfCommunicationsUtil.queryHistoricalTransactions(historyTransactionRequest);
            System.out.println("-------返回值------");
            System.out.println(JSON.toJSONString(response));
            System.out.println("-------end------");
        } catch (Exception e) {
            System.out.println("异常信息:" + e.getMessage());
        }
        return 1;
    }

    /**
     * 请求体测试 4 代发查询
     */
    public static int ttttest4() throws Exception {
        System.out.println("* *********************");
        BankOfCommunicationsUtil bankOfCommunicationsUtil = new BankOfCommunicationsUtil();
        PayAgencyRequest payAgencyRequest = new PayAgencyRequest();

        PayAgencyReqBody body = new PayAgencyReqBody();
        PayAgencyReqTran tran = new PayAgencyReqTran();
        List<PayAgencyDetail> details = new ArrayList<>();
        details.add(new PayAgencyDetail("110060576018150007238", "张三", "A", new BigDecimal(100.20).setScale(2, BigDecimal.ROUND_HALF_UP), "2025xxad0526", null));
        details.add(new PayAgencyDetail("110060576018150007238", "张三", "B", new BigDecimal(1000.03).setScale(2, BigDecimal.ROUND_HALF_UP), "202ssdqweq50528", "批次明细摘要"));
        tran.setRcds(details);
        body.setCertNo("certNo");
        body.setPayAcno("110060576018150007238");
        body.setType(PaymentTypeEnum.ALIPAY.getCode());
        body.setSum(details.size());
        body.setSumAmt(details.stream().map(d -> d.amt).reduce(BigDecimal.ZERO, BigDecimal::add));
        body.setPayMonth("2025-05".replace("-", ""));
        body.setSummary("附言");
        body.setBusiNo("协议编号222");
        body.setSelsecFlg("0");
        body.setMailflg(BankOfCommunicationsConstants.MAIL_FLG_Y);
        body.setBusiNo("certNo");
        body.setTran(tran);
        payAgencyRequest.setBody(body);
        System.out.println("-------发送------");
        PayAgencyResponse payAgencyResponse = bankOfCommunicationsUtil.executePayAgency(payAgencyRequest);
        System.out.println("-------返回值------");
        System.out.println(JSON.toJSONString(payAgencyResponse));
        System.out.println("-------end------");

        return 1;
    }

    /**
     * 请求体测试 5 代发结果查询
     */
    public static int ttttest5() throws Exception {

        System.out.println("* ************ttttest5*********");
        QueryTransactionRequest queryTransactionRequest = new QueryTransactionRequest();
        QueryTransactionReqBody body = new QueryTransactionReqBody();
        body.setOglSerialNo("2023456789dfghjk1");
        body.setQueryFlag(BankOfCommunicationsConstants.QUERY_FLAG_1);

        queryTransactionRequest.setBody(body);

        System.out.println(JSON.toJSONString(queryTransactionRequest));
        BankOfCommunicationsUtil bankOfCommunicationsUtil = new BankOfCommunicationsUtil();
        System.out.println("* ******************发送***");
        QueryTransactionResponse queryTransactionResponse = bankOfCommunicationsUtil.queryPayAgencyResult(queryTransactionRequest);
        System.out.println("-------返回值------");
        System.out.println(JSON.toJSONString(queryTransactionResponse));
        System.out.println("-------end------");
        return 1;
    }

    /**
     * 公共头测试
     *
     * @return
     * @throws JAXBException
     */
    public static int ttttest() throws JAXBException {

        System.out.println("* *********************");
        RequestHeader requestHeader = new RequestHeader();

        System.out.println(JSON.toJSONString(requestHeader));
        BankOfCommunicationsUtil bankOfCommunicationsUtil = new BankOfCommunicationsUtil();

        String xml1 = bankOfCommunicationsUtil.toXml(requestHeader);

        System.out.println("生成的XML:\n" + xml1);

        // 3. 将XML转回对象（反序列化）
        RequestHeader requestHeader1 = bankOfCommunicationsUtil.fromXml(xml1, RequestHeader.class);
        System.out.println("解析后的对象:\n" + JSON.toJSONString(requestHeader1));
        return 1;
    }

    /**
     * 对象-->xml
     *
     * @param object obj
     * @return string
     * @throws JAXBException jaxb异常
     */
    public static String marshalToXml(Object object) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = context.createMarshaller();
        //配置输出格式
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        // 设置 JAXB_FRAGMENT 为 true，移除 XML 声明
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();
    }


    /**
     * xml → 对象
     *
     * @param xml xml
     * @param <T> T
     * @return T
     * @throws JAXBException jaxb异常
     */
    public static <T> T unmarshalFromXml(String xml) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Person.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        StringReader reader = new StringReader(xml);
        return (T) unmarshaller.unmarshal(reader);
    }
}
