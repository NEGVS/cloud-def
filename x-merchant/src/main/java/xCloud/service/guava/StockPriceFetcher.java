package xCloud.service.guava;


import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import xCloud.util.CodeX;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StockPriceFetcher {
    public static void main(String[] args) {
        String stockCode = "300793";  // 目标股票代码
        String apiUrl = "https://finance.pae.baidu.com/vapi/v1/getquotation" +
                "?all=1&srcid=5353&pointType=string" +
                "&group=quotation_fiveday_ab&market_type=ab" +
                "&new_Format=1&finClientType=pc&code=" + stockCode;
        try {
            // 1. 获取API数据
            String jsonData = fetchApiData(apiUrl);
            // 2. 解析JSON
            parseJsonData(jsonData, stockCode);
        } catch (Exception e) {
            System.err.println("数据处理失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static String fetchApiData(String apiUrl) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);
            request.addHeader("User-Agent", "Mozilla/5.0");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null && response.getStatusLine().getStatusCode() == 200) {
                    return EntityUtils.toString(entity);
                } else {
                    throw new RuntimeException("API请求失败: HTTP " + response.getStatusLine().getStatusCode());
                }
            }
        }
    }

    private static List<StockDataHeader> parseJsonData(String json, String stockCode) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);
        List<String> headerList = new ArrayList<>();
        List<String> keyList = new ArrayList<>();
        JsonNode newMarketData = rootNode.path("Result").path("newMarketData");
        JsonNode headers = newMarketData.path("headers");
        for (JsonNode header : headers) {
            headerList.add(header.asText());
        }
        JsonNode keys = newMarketData.path("keys");
        for (JsonNode key : keys) {
            keyList.add(key.asText());
        }
        JsonNode marketData = newMarketData.path("marketData");
        Map<String, List<StockDataHeader>> dateMap = new TreeMap<>();
        // 最终的数据
        for (JsonNode marketDatum : marketData) {
            String date = marketDatum.path("date").asText();
            String p = marketDatum.path("p").asText();

            // 将 p的值进行分割=每分钟的数据--["时间戳","时间","价格","均价","涨跌额","涨跌幅","成交量","成交额","累积成交量","累积成交额"]
            String[] split = p.split(";");
            List<StockDataHeader> stockDataHeaderList = new ArrayList<>(split.length);
            for (String temp : split) {
                StockDataHeader stockDataHeader = new StockDataHeader();
                String[] data = temp.split(",");
                Field[] declaredFields = StockDataHeader.class.getDeclaredFields();
                for (int i = 0; i < declaredFields.length; i++) {
                    Field declaredField = declaredFields[i];
                    declaredField.set(stockDataHeader, data[i]);
                }
                stockDataHeaderList.add(stockDataHeader);
            }
            dateMap.put(date, stockDataHeaderList);
        }

        for (Map.Entry<String, List<StockDataHeader>> entry : dateMap.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
        System.out.println(CodeX.getDate_yyyy_MM_dd());
        System.out.println(CodeX.getDate_yyyy_MM_dd(DateUtil.yesterday()));

        //今日最新数据
        List<StockDataHeader> stockDataHeaderList = dateMap.get(CodeX.getDate_yyyy_MM_dd());
        StockDataHeader stockDataHeader = stockDataHeaderList.get(stockDataHeaderList.size() - 1);
        BigDecimal bigDecimal = new BigDecimal(stockDataHeader.getPrice());
        System.out.println("今日价格：" + bigDecimal);
        //昨日最新数据

        List<StockDataHeader> stockDataHeaderList2 = dateMap.get(CodeX.getDate_yyyy_MM_dd(DateUtil.yesterday()));
        StockDataHeader stockDataHeader2 = stockDataHeaderList2.get(stockDataHeaderList2.size() - 1);
        BigDecimal bigDecimal2 = new BigDecimal(stockDataHeader2.getPrice());
        System.out.println("昨日价格：" + bigDecimal2);

        BigDecimal difference = bigDecimal.subtract(bigDecimal2);
        BigDecimal rate = bigDecimal.subtract(bigDecimal2).divide(bigDecimal2, 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

//        BigDecimal rate = ((bigDecimal.subtract(bigDecimal2)).divide(bigDecimal2).setScale(2, RoundingMode.HALF_UP)).setScale(2, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);

        System.out.println("Rate：" + rate);
        return null;
    }
}


