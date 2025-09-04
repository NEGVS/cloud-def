package xCloud.util;

import cn.hutool.json.JSONUtil;
import com.sun.source.tree.MemberReferenceTree;
import xCloud.entity.Merchants;
import xCloud.service.serviceImpl.CodeX;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/6/23 19:32
 * @ClassName andyTest6
 */
public class andyTest6 {
    public static void main(String[] args) {
        try {

            List<Merchants> businessUsers = new ArrayList<>();
            businessUsers.add(new Merchants(1, "1", "1", "1", "1", "1", 1, 1, new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("1"), "1", "1", new Date(), new Date()));
            businessUsers.add(new Merchants(2, "1", "1", "1", "1", "1", 1, 1, new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("1"), "1", "1", new Date(), new Date()));
            businessUsers.add(new Merchants(null, "1", "1", "1", "1", "1", 1, 1, new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("1"), "1", "1", new Date(), new Date()));
            businessUsers.add(new Merchants(2, "1", "1", "1", "1", "1", 1, 1, new BigDecimal("1"), new BigDecimal("1"), new BigDecimal("1"), "1", "1", new Date(), new Date()));
//            businessUsers=null;
            List<Integer> collect = Optional.ofNullable(businessUsers).orElse(Collections.emptyList())
                    .stream().distinct().filter(Objects::nonNull)
                    .map(Merchants::getMerchant_id)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            System.out.println(collect);
            System.out.println(JSONUtil.toJsonStr(collect));


            if (true) {
                return;
            }
            BigDecimal bigDecimal = CodeX.calculateFinalAmount(BigDecimal.valueOf(100000), true, 10, BigDecimal.valueOf(0.1));
            System.out.println("本金：" + BigDecimal.valueOf(100000));
            System.out.println("年化利率：" + BigDecimal.valueOf(0.1));
            System.out.println(bigDecimal);
            System.out.println("净赚：" + bigDecimal.subtract(BigDecimal.valueOf(100000)));
            if (true) {
                return;
            }
            URL url = new URL("https://space.robotcy.cn:18082/mini_api/wxRoom/selectRoomUser");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("token", "17435595079786232611");
            connection.setDoOutput(true);

            String jsonInputString = "{\"pageSize\": 250, \"pageNum\": 1, \"wxRoomId\": 7940, \"type\": 0, \"hytype\": \"\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read the response if needed
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("Response Body: " + response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
