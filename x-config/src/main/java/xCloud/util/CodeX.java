package xCloud.util;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.db.sql.Order;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.Claims;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import xCloud.entity.user.User;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * java工具栏集合
 * 重点搜索加*
 * 例如
 * 时间相关，time*
 * list相关，list*
 * map相关，map*
 * 多线程相关，thread*
 * 线程池相关，threadPool*
 */
@Slf4j
@Component
public class CodeX {
    /**
     * 数字返回true，否则false
     *
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        return str.matches("\\d+");
    }

    /**
     * 下划线转驼峰-UnderlineToCamelHump
     * check-employee-list
     */
    public static String UnderlineToCamelHump(String string) {
        StringBuilder sb = new StringBuilder();
        String[] split = string.replace("_", "-").split("-");
        for (String s : split) {
            sb.append(firstToUpper(s));
        }
        System.out.println(firstToSmall(sb.toString()));
        return firstToSmall(sb.toString());
    }

    //测试
    public static void testReadExcel() {
        String directoryPath = "/Users/andy_mac/Documents/CodeSpace/BackEnd/codeT/src/main/resources/file/111122.txt";

        CodeX codeX = new CodeX();
        List<String> list = codeX.readFileLines(directoryPath);
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder stringBuilder2 = new StringBuilder();
        StringBuilder stringBuilder3 = new StringBuilder();

        for (String string : list) {
            stringBuilder.append(string.trim());
        }
        System.out.println(stringBuilder);
        for (String string : stringBuilder.toString().split(",")) {
            //addressLongitude, addressLatitude,
            stringBuilder2.append(codeX.underlineToHump(string.trim()) + ", ");
            //#{ID}, #{ID},
            stringBuilder3.append("#{a." + codeX.underlineToHump(string.trim()) + "}, ");

        }
        System.out.println(stringBuilder2.substring(0, stringBuilder2.lastIndexOf(",")));
        System.out.println(stringBuilder3.substring(0, stringBuilder3.lastIndexOf(",")));
    }

    public static void testSubString() {
        final String fullFilePath = "finance/invoice/pdf/fileName" + ".pdf";
        //获取名字（包含后缀）(fileName.pdf)
        String resultNameAndSuffix = fullFilePath.substring(fullFilePath.lastIndexOf("/") + 1, fullFilePath.length());
        //获取名字（不包含后缀）(fileName)
        String resultName = fullFilePath.substring(fullFilePath.lastIndexOf("/") + 1, fullFilePath.lastIndexOf("."));
        //获取后缀（.pdf）
        String resultSuffix = fullFilePath.substring(fullFilePath.lastIndexOf("."));
        //获取文件所在目录（finance/invoice/pdf）
        String resulPath = fullFilePath.substring(0, fullFilePath.lastIndexOf("/"));
        System.out.println(fullFilePath);
        System.out.println(resultNameAndSuffix);
        System.out.println(resultName);
        System.out.println(resultSuffix);
        System.out.println(resulPath);
    }

    //is not null
    public void isNotNull() {
/**
 * ObjectUtil.isNotNull( "" );ObjectUtil.isNotEmpty( "" );------
 * 对象判断
 * 1-ObjectUtil.isNotNull()方法:
 * 该方法仅用于检查指定对象是否不为null。
 * 2.ObjectUtil.isNotEmpty()方法:
 *   该方法不仅检查对象是否为null，还会检查对象是否为空（例如空字符串、空集合等）。ObjectUtil.isNotEmpty()方法提供了更为全面的检查，因为它会检查null和空的状态，而ObjectUtil.isNotNull()方法只检查null状态。
 *
 * ——————字符串判断---使用--StrUtil.isNotBlank()，StrUtil.isBlank()，空格认为是null。
 *
 * StrUtil.isNotEmpty( "" );StrUtil.isNotBlank( "" );
 * StringUtils.isNotEmpty方法只会检查字符串是否不为空或null。如果字符串为null或空字符串，则返回false，否则返回true。
 *
 * 因此，StringUtils.isNotBlank方法比StringUtils.isNotEmpty方法更加严格，它不仅检查字符串是否为空或null，还要检查字符串中是否至少包含一个非空格字符。 在实际开发中，根据需求选择适当的方法。如果需要确保字符串中至少包含一个非空格字符，则应使用StringUtils.isNotBlank方法；如果只需要检查字符串是否为空或null，则可以使用StringUtils.isNotEmpty方法。
 * ————————
 */
        String strNull = null;
        String strSpace0 = "";
        String strSpace1 = " ";

        ObjectUtil.isNotNull("");
        ObjectUtil.isNotEmpty("");
        StrUtil.isNotEmpty("");
        StrUtil.isNotBlank("");
        StrUtil.isAllNotBlank("");

        if (ObjectUtil.isNotEmpty(strNull)) {
            System.out.println("strNull is not null");
        }
        if (ObjectUtil.isNotNull(strSpace0)) {

        }


    }

    public static void deleteTableField(String path) {

        CodeX codeX = new CodeX();
        List<String> list = codeX.readFileLines(path);
        for (String string : list) {
            if (string.contains("TableField")) {
                continue;
            }
            System.out.println(string);
        }
    }

    /**
     * 根据银行卡号获取银行卡信息
     *
     * @param cardNo 银行卡号
     * @return 信息
     */
    private static final String app_code = "4ef8cf7161fd4dbabf0fd95e26d59d37";
    private static final String app_url = "https:        //bkarea.market.alicloudapi.com/lundear/bankArea";
    private static final String app_key = "24926876";
    private static final String app_secret = "0eab01f69f9d886dd99ebebeca35ecea";

    public String findBankAreaByCardNo(String cardNo) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + app_code);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("cardno", cardNo);
        try {
//            return HttpUtils.doGet(app_url, headers, querys);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    /**
     * 导出excel
     * @param list 数据源
     * @param fileName 文件名
     * @param response response
     * @throws Exception 抛出异常
     */
    //   public static void exportExcel( Collection< ? > list, String fileName, HttpServletResponse response ) throws Exception
    //   {
    //              // 通过工具类创建writer，默认创建xls格式
    //      ExcelWriter writer = ExcelUtil.getWriter( true );
    //      writer.write( list, true );
    //
    //              // 设置浏览器响应格式
    //      response.setContentType( "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8" );
    //      fileName = URLEncoder.encode( fileName, StandardCharsets.UTF_8 );
    //      response.setHeader( "Content-Disposition", "attachment;filename=" + fileName + ".xlsx" );
    //
    //      ServletOutputStream out = response.getOutputStream();
    //
    //              //      CodeX.downLoadInputStreamToFile( out,"./sss.xlsx" );
    //      writer.flush( out, true );
    //      out.close();
    //      writer.close();
    //   }

    /**
     * BigDecimal测试
     */
    public static void BigDecimalTest() {

        BigDecimal bigDecimal1 = new BigDecimal("0.0");
        BigDecimal bigDecimal2 = new BigDecimal("5.0");
        BigDecimal bigDecimal3 = new BigDecimal("-2.0");
        BigDecimal bigDecimal4 = new BigDecimal("3.0");
        String str1 = "0.0";
        String str2 = "2.0";
        String str3 = "-2.0";
        //      加减乘除
        BigDecimal multiply = bigDecimal2.multiply(bigDecimal3);

        System.out.println(multiply);

        //str1转为BigDecimal
        BigDecimal bigDecimal5 = new BigDecimal(str1);
        BigDecimal bigDecimal6 = new BigDecimal(str2);
        BigDecimal bigDecimal7 = new BigDecimal(str3);
        System.out.println(bigDecimal7);
        if (bigDecimal7.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("bigDecimal bigger than  0");
        }

        //      正负判断，0值，是否大于0

    }

    /**
     * 生成唯一ID --idWorker
     *
     * @return
     */
    public static String generalIDByIDWorker() {
        return String.valueOf(IdWorker.getId());
    }

    /**
     * 生成唯一ID --uuid
     *
     * @return
     */
    public static String generalIDBySnowflake() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成唯一ID --uuid
     *
     * @return
     */
    public static String generalUUID() {

        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * downLoadInputStreamToFile-- InputStream 流转为文件
     *
     * @param inputStream
     * @param filePath    完整文件路径 例：./xxx.pdf
     */
    public static void downLoadInputStreamToFile(InputStream inputStream, String filePath) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        try {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            inputStream.close();
        }
    }

    /**
     * download url file return InputStream--method0，解决https证书问题「url，httpURL，request」--目前最好用
     *
     * @param fileURL
     * @return InputStream
     * @throws FileNotFoundException
     */
    public InputStream downLoadUrlToInputStream(final String fileURL) throws FileNotFoundException {
        try {
            // 创建一个信任所有证书的 TrustManager
            TrustManager[] trustAll = new TrustManager[]{new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};
            URL url = new URL(fileURL);
            HttpsURLConnection httpConn = (HttpsURLConnection) url.openConnection();
            // 初始化 SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAll, new java.security.SecureRandom());

            /**
             * 在Java中，使用TLS（Transport Layer Security）下载URL文件可以通过配置HttpURLConnection或使用其他支持TLS的HTTP客户端库来实现。以下是使用HttpURLConnection并设置特定TLS版本的示例：
             * 我们通过设置ssl-protocol请求属性来指定TLS版本。注意，不是所有的HTTP服务器都支持所有的TLS版本，因此可能需要根据服务器的配置进行调整。
             * 如果你需要更高级的功能，如自动TLS版本选择、主机名验证等，可以考虑使用第三方库，如Apache HttpClient或OkHttp，它们提供了更丰富的HTTP和TLS配置选项
             */

            // 获取 SSLSocketFactory
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // 设置自定义的 SSLSocketFactory
            httpConn.setSSLSocketFactory(sslSocketFactory);

            int responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = httpConn.getInputStream();
                //    inputStream.close();
                return inputStream;
            } else {
                log.info("download失败，服务器响应代码: " + responseCode);
            }

            httpConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * download file return InputStream----method1
     *
     * @param fileURL
     * @return InputStream
     * @throws FileNotFoundException
     */
    public InputStream downLoadUrlToInputStream001(final String fileURL) throws FileNotFoundException {
        return null;
    }

    /**
     * download文件返回InputStream-method2
     *
     * @param fileURL
     * @return InputStream
     * @throws FileNotFoundException
     */
    public InputStream downLoadUrlToInputStream002(final String fileURL) throws FileNotFoundException {
        //      String fileURL = "https:        //example.com/file.txt";         // 替换为你要下载的文件的URL
        String saveFilePath = "D:/DownloadedFiles/file.txt";         // 替换为你要保存文件的本地路径

        try {
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            // 设置TLS版本
            httpConn.setRequestProperty("ssl-protocol", "TLSv1.2");

            int responseCode = httpConn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream1 = httpConn.getInputStream();
                log.info("文件下载成功");
                return inputStream1;
                //    try (BufferedInputStream inputStream = new BufferedInputStream(  ); FileOutputStream outputStream = new FileOutputStream( saveFilePath ))
                //    {
                //       byte[] buffer = new byte[ 1024 ];
                //       int bytesRead;
                //       while ( ( bytesRead = inputStream.read( buffer ) ) != -1 )
                //       {
                //  outputStream.write( buffer, 0, bytesRead );
                //       }
                //    }
            } else {
                log.info("服务器响应错误： " + responseCode);
            }
            httpConn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * download文件返回InputStream-method3
     *
     * @param fileURL
     * @return InputStream
     * @throws FileNotFoundException
     */
    public InputStream downLoadUrlToInputStream003(final String fileURL) throws FileNotFoundException {
        return null;
    }

    /**
     * 验证字符串是否只包含字母、数字和下划线
     *
     * @param string
     * @return
     */
    public boolean stringValidate(String string) {
        if (ObjectUtil.isNotEmpty(string)) {
            // 只允许字母、数字和下划线的正则表达式
            String regex = "^[a-zA-Z0-9_]+$";
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(string).matches();
        } else {
            return false;
        }
    }

    /**
     * 验证手机号码
     *
     * @param phoneNumber
     * @return true or false
     */
    public boolean phoneNumberValidate(String phoneNumber) {
        if (ObjectUtil.isNotEmpty(phoneNumber)) {
            // 中国大陆手机号码正则表达式
            String regex = "^1[0-9]\\d{9}$";
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(phoneNumber).matches();
        } else {
            return false;
        }

    }

    /**
     * 导入excel--导入文件最好的method是 使用easyExcel
     * @param file
     * @return
     */
    //   public List< User > importExcel( MultipartFile file ) throws IOException
    //   {
    //      List< User > userlist = new ArrayList<>();
    //      Workbook workbook = new XSSFWorkbook( file.getInputStream() );
    //      Sheet sheet = workbook.getSheetAt( 0 );
    //      Iterator< Row > rowIterator = sheet.iterator();
    //
    //              //跳过标题行（if have）
    //      if ( rowIterator.hasNext() )
    //      {
    // rowIterator.next();
    //      }
    //      while ( rowIterator.hasNext() )
    //      {
    // Row row = rowIterator.next();
    // User user = new User();
    // user.setUserId( row.getCell( 0 ).getStringCellValue() );
    // user.setNickName( row.getCell( 1 ).getStringCellValue() );
    // user.setBalance( BigDecimal.valueOf( Long.parseLong( row.getCell( 2 ).getStringCellValue() ) ) );
    //      }
    //
    //      return null;
    //
    //   }
    //导出文件

    /**
     * 生成jwt令牌
     *
     * @return
     */
    public String genJwt(String userId, String userName) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("userName", userName);
//        String jwt = Jwts.builder().signWith(SignatureAlgorithm.HS256, "itheima")
        //签名算法 和密钥
//                .setClaims(claims)
//                //自定义内容
//                .setExpiration(new Date(System.currentTimeMillis() + 3600 * 1000))
//                //设置有效期为1小时
//                .compact();
//        System.out.println(jwt);
//
//        return jwt;
        return null;
    }


    /**
     * 随机生成指定长度的字符串
     *
     * @return 生成字符串
     */
    public static String generalComplexRandomString() {
        final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int length = 6;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    public static String generalComplexRandomString(int length) {
        if (ObjectUtil.isNotEmpty(length) && length > 1) {
            // 定义字符集，可以根据需要添加更多字符
            String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
            Random random = new Random();
            StringBuilder stringBuilder = new StringBuilder(length);
            for (int i = 0; i < length; i++) {
                int index = random.nextInt(characters.length());
                stringBuilder.append(characters.charAt(index));
            }

            return stringBuilder.toString();
        } else {
            throw new IllegalArgumentException("随机字符串长度必须大于等于1");

        }
    }

    /**
     * 生成1-10000之间的随机整数
     *
     * @return
     */
    public static int generalRandomInt() {
        Random random = new Random();
        return random.nextInt(10000) + 1;
    }

    //   加密为base64
    public void getBase64(String string) {
        //编码
        String encodeToString = Base64.getEncoder().encodeToString(string.getBytes());
        System.out.println(encodeToString);
        //解码
        byte[] decode = Base64.getDecoder().decode(encodeToString);
        String decodeString = new String(decode);
        System.out.println(decodeString);
    }

    /**
     * 压缩string
     *
     * @param string
     * @return
     */
    public String compress(String string) {
        if (ObjectUtil.isNotEmpty(string)) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
                gzipOutputStream.write(string.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());

        }
        return null;
    }

    //
    //   https:        //juejin.cn/post/7108217445831147556

    /**
     * 解压string
     *
     * @param compressString
     * @return
     */
    public String unCompress(String compressString) {
        String result = null;
        if (ObjectUtil.isNotEmpty(compressString)) {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] compressedByte = new byte[0];
            try {
                compressedByte = Base64.getDecoder().decode(compressString);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedByte); GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);) {
                byte[] buffer = new byte[1024];
                int offset = -1;
                while ((offset = gzipInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, offset);
                }
                result = outputStream.toString();

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    /**
     * md5 解密
     *
     * @param str
     * @return
     */
    public String getDecodeMD5(String str) {
        String result = null;
        if (ObjectUtil.isNotEmpty(str)) {
            //decode md5
            result = DigestUtil.md5Hex(str);
        }
        return result;
    }

    /**
     * md5 加密--md5 无法解密的，判断密码是否正确就把源文件再次加密，和加密后的文件进行比较
     *
     * @param str
     * @return
     */
    public static String getMD5(String str) {
        try {
            //创建一个MessageDigest实例，指定md5算法
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            //输入字符串，转换为字节数组，并加密
            byte[] bytes = messageDigest.digest(str.getBytes());
            //把字节数组转换为16进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                //将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制
                //    String hex = Integer.toHexString(b & 0xFF);
                //    if (hex.length() == 1)
                //    {
                //       hex = '0' + hex;
                //    }
                //    sb.append(hex);
                sb.append(String.format("%02x", b));
            }
            //返回加密后的字符串
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成二维码
     *
     * @param url
     * @param width
     * @param height
     * @param filePath
     * @throws Exception
     */


    /**
     * 每次取出10个数据的迭代
     */
    //      for (int i = 0; i < list1.size(); i += 10) {
    //   List<Integer> batchOf10 = list1.subList(i, Math.min(i + 10, list1.size()));
    //   System.out.println("Batch " + (i / 10 + 1) + ": " + batchOf10);
    //}

    /**
     * 两个list 的交集
     *
     * @param list1
     * @param list2
     * @return 1-两个list 的交集
     * 2-list1 独有的部分
     * 3-list2 独有的部分
     */

    public static List<List<String>> getListIntersection(List<String> list1, List<String> list2) {
        List<List<String>> result_list = new ArrayList<>();
        List<String> list_inter = new ArrayList<>();
        List<String> list_only1 = new ArrayList<>();
        for (String s : list1) {
            if (list2.contains(s)) {
                list_inter.add(s);
                list2.remove(s);
            } else {
                list_only1.add(s);
            }
        }
        result_list.add(list_inter);
        result_list.add(list_only1);
        result_list.add(list2);

        return result_list;

    }

    /**
     * 4-首字母大写 BaseUtil
     *
     * @param originalName
     * @return
     */
    public static String firstToUpper(String originalName) {
        if (originalName != null && originalName.length() > 1) {
            return originalName.substring(0, 1).toUpperCase() + originalName.substring(1);
        }
        return "";

    }

    /**
     * 3-首字母小写
     *
     * @param originalName
     * @return
     */
    public static String firstToSmall(String originalName) {
        if (originalName != null && originalName.length() > 1) {
            return originalName.substring(0, 1).toLowerCase() + originalName.substring(1);
        }
        return "";

    }

    //下划线转驼峰
    public String underlineToHump(String str) {
        //大写变小写
        str = str.toLowerCase();
        StringBuilder sb = new StringBuilder();
        if (str != null && str.contains("_")) {
            String[] split = str.split("_");
            int index = 0;
            for (String s : split) {
                if (index > 0) {
                    sb.append(firstToUpper(s));
                } else {
                    sb.append(s);
                }
                index++;
            }
            return sb.toString();
        }
        return str;
    }

    //驼峰转下划线
    public String humpToUnderline(String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append("_").append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * file-------------------------
     */
    /**
     * 判断文件夹是否存在 如果不存在就创建文件夹
     *
     * @param path path
     * @return true or false
     */
    public static boolean checkDirIsExist(String path) {
        //win--mac--system is different
        // 指定要检查的路径 .replace( "\\","\" )
        //get file name
        String file_name = path.substring(path.lastIndexOf("/") + 1, path.length());
        String directoryPath = "";
        if (path.contains("/")) {
            //获取文件夹路径
            directoryPath = path.substring(0, path.lastIndexOf("/"));
        } else if (path.contains("\\")) {
            directoryPath = path.substring(0, path.lastIndexOf("\\"));
        }
        Path parentPath = Paths.get(directoryPath);
        try {
            // 检查路径是否存在
            if (!Files.exists(parentPath)) {
                // 如果不存在，则创建目录
                Files.createDirectories(parentPath);
                System.out.println("目录已创建： " + directoryPath);
            } else {
                //    System.out.println( "目录已存在： " + directoryPath );
            }
        } catch (IOException e) {
            System.err.println("创建目录时发生错误： " + e.getMessage());
        }
        return true;

    }

    /**
     * 获取当前时间  2025-02-18 17:15:48
     */
    public static String getNowTime() {
        //2025-02-18 17:15:48
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 获取表结构
     *
     * @param tableName 表名字
     * @return 表结构： key:字段名，value:字段注释
     * @throws SQLException x
     */
    public static Map<String, String> getTableStructure(String tableName) throws SQLException {


        //rng -pre
//        final String URL = "jdbc:mysql://rm-uf655k9802vf7i07hso.mysql.rds.aliyuncs.com:3306/rng_hro_pre?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true";
//        final String USER = "hrosdba";
//        final String PASSWORD = "hros0427";


        //andy_ruoYi
        final String URL = "jdbc:mysql://1.92.111.247:3306/andy_ruoYi?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8";
        final String USER = "root";
        final String PASSWORD = "hy123456";

        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);

        Map<String, String> columnName_comment_map = new LinkedHashMap<>();

        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getColumns(null, null, tableName, null);
        while (resultSet.next()) {
            String columnName = resultSet.getString("COLUMN_NAME");
            String columnType = resultSet.getString("TYPE_NAME");
            int columnSize = resultSet.getInt("COLUMN_SIZE");
            boolean isNullable = resultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
            String remarks = resultSet.getString("REMARKS");

            //翻译类型
            columnType = columnTypeTranslate(columnType);
            System.out.println("Column Name: " + columnName + ", Type: " + columnType + ", Size: " + columnSize + ", Nullable: " + isNullable + ", Remarks: " + remarks);
            //remarks，记录 注释+type
            if (StrUtil.isNotBlank(remarks.trim())) {
                //去除注释中可能存着的@
                remarks = remarks.replace("@", "") + "@" + columnType;
            } else {
                remarks = columnType;
            }
            columnName_comment_map.put(columnName, remarks);
        }
        resultSet.close();
        return columnName_comment_map;
    }

    public static String columnTypeTranslate(String columnType) {
        //转为全部小写
        columnType = columnType.toLowerCase();
        if (columnType.contains("varchar")) {
            return "String";
        }
        if (columnType.contains("int")) {
            return "Integer";
        }
        if (columnType.contains("datetime")) {
            return "Date";
        }
        if (columnType.contains("decimal")) {
            return "BigDecimal";
        }
        if (columnType.contains("double")) {
            return "Double";
        }
        if (columnType.contains("float")) {
            return "Float";
        }
        if (columnType.contains("char")) {
            return "String";
        }
        if (columnType.contains("tinyint")) {
            return "Integer";
        }
        if (columnType.contains("longtext")) {
            return "String";
        }
        if (columnType.contains("text")) {
            return "String";
        }
        if (columnType.contains("date")) {
            return "Date";
        }
        if (columnType.contains("time")) {
            return "Date";
        }
        if (columnType.contains("year")) {
            return "Date";
        }
        if (columnType.contains("timestamp")) {
            return "Date";
        }
        if (columnType.contains("bit")) {
            return "Integer";
        }
        if (columnType.contains("mediumint")) {
            return "Integer";
        }
        if (columnType.contains("bigint")) {
            return "Long";
        }
        if (columnType.contains("tinytext")) {
            return "String";
        }
        if (columnType.contains("mediumtext")) {
            return "String";
        }
        if (columnType.contains("longblob")) {
            return "byte[]";
        }
        if (columnType.contains("blob")) {
            return "byte[]";
        }
        if (columnType.contains("mediumblob")) {
            return "byte[]";
        }
        if (columnType.contains("binary")) {
            return "byte[]";
        }
        if (columnType.contains("varbinary")) {
            return "byte[]";
        }
        if (columnType.contains("tinyblob")) {
            return "byte[]";
        }
        if (columnType.contains("json")) {
            return "String";
        }
        if (columnType.contains("set")) {
            return "String";
        }
        if (columnType.contains("enum")) {
            return "String";
        }
        if (columnType.contains("geometry")) {
            return "String";
        }
        if (columnType.contains("point")) {
            return "String";
        }
        if (columnType.contains("linestring")) {
            return "String";
        }
        if (columnType.contains("polygon")) {
            return "String";
        }
        if (columnType.contains("multipoint")) {
            return "String";
        }
        if (columnType.contains("multilinestring")) {
            return "String";
        }
        if (columnType.contains("multipolygon")) {
            return "String";
        }
        if (columnType.contains("geometrycollection")) {
            return "String";
        }
        if (columnType.contains("uuid")) {
            return "String";
        }
        if (columnType.contains("geography")) {
            return "String";
        }
        if (columnType.contains("jsonb")) {
            return "String";
        }
        if (columnType.contains("hstore")) {
            return "String";
        }
        if (columnType.contains("interval")) {
            return "String";
        }
        if (columnType.contains("cidr")) {
            return "String";
        }
        if (columnType.contains("inet")) {
            return "String";
        }
        if (columnType.contains("macaddr")) {
            return "String";
        }
        if (columnType.contains("bit")) {
            return "Integer";
        }
        if (columnType.contains("varbit")) {
            return "Integer";
        }
        if (columnType.contains("box")) {
            return "String";
        }
        if (columnType.contains("circle")) {
            return "String";
        }
        if (columnType.contains("path")) {
            return "String";
        }
        return "String";
    }

    public static void generalCodeTest() {

        String general_entity = "/Users/andy_mac/Documents/CodeSpace/BackEnd/codeT/src/main/java/generalCode/entity";
        String general_mapper = "/Users/andy_mac/Documents/CodeSpace/BackEnd/codeT/src/main/java/generalCode/mapper";
        String general_service = "/Users/andy_mac/Documents/CodeSpace/BackEnd/codeT/src/main/java/generalCode/service";
        String general_service_impl = "/Users/andy_mac/Documents/CodeSpace/BackEnd/codeT/src/main/java/generalCode/service/impl";

        Set<String> list = new TreeSet<>();
        list.add(general_entity);
        list.add(general_mapper);
        list.add(general_service);
        list.add(general_service_impl);

        String path = "/Users/andy_mac/Documents/CodeSpace/BackEnd/codeT/src/main/java/com/example/codet/";
        String controller_path = path + "controller/hc/";
        String service_path = path + "service/hc/";
        String service_impl_path = path + "service/impl/";
        String mapper_path = path + "mapper/";
        String entity_path = path + "entity/";

        for (String stringPath : list) {
            Set<String> allDirectoryNameList = CodeX.readFileNameList(stringPath);
            for (String str : allDirectoryNameList) {
                System.out.println(str);
            }
            System.out.println("-----------");
        }
        //      for ( String string : allDirectoryNameList )
        //      {
        // if ( string.contains( "Controller") )
        //      }
        //      System.out.println( allDirectoryNameList.size() );
    }

    /**
     * 复制文件到该目录下-方式1
     *
     * @param file
     * @param path
     * @return
     */
    public static boolean cp_file_to_path(String file, String path, String newName) {
        Path parentPath = Paths.get(path);

        try {
            // 检查路径是否存在
            if (!Files.exists(parentPath)) {
                // 如果不存在，则创建目录
                Files.createDirectories(parentPath);
                System.out.println("目录已创建： " + path);
            }
        } catch (IOException e) {
            System.err.println("创建目录时发生错误： " + e.getMessage());
        }
        if (!path.contains(".")) {
            path = path + file.substring(file.lastIndexOf("/") + 1, file.length());
        }
        String string = CodeX.firstToSmall(newName);

        //java复制文件到该目录下
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(path);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fis.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 复制文件到该目录下-方式1
     *
     * @param file
     * @param dest_file
     * @return
     */
    public static void copyFile(String file, String dest_file) throws IOException {
        Path parentPath = Paths.get(dest_file);
        try {
            // 检查路径是否存在
            if (!Files.exists(parentPath)) {
                // 如果不存在，则创建目录
                Files.createDirectories(parentPath);
                System.out.println("目录不存在，已创建： " + dest_file);
            }
        } catch (IOException e) {
            System.err.println("创建目录时发生错误： " + e.getMessage());
        }

        if (!dest_file.contains(".")) {
            dest_file = dest_file + file.substring(file.lastIndexOf("/") + 1, file.length());
        }
        File source = new File(file);
        File dest = new File(dest_file);

        try (FileChannel sourceChannel = new FileInputStream(source).getChannel(); FileChannel destChannel = new FileOutputStream(dest).getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 复制文件到该目录下-方式1
     *
     * @param file
     * @param path
     * @return
     */
    public static boolean cp_file_to_path(String file, String path) {
        if (!path.contains(".")) {
            path = path + file.substring(file.lastIndexOf("/") + 1, file.length());
        }

        //java复制文件到该目录下
        try {
            FileInputStream fis = new FileInputStream(file);
            FileOutputStream fos = new FileOutputStream(path);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fis.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static List<String> list_dir = new ArrayList<>();

    /**
     * 获取该路径下的所有文件夹绝对路径，只有文件夹，没有文件
     *
     * @return
     */
    public static List<String> getAllDirectoryNameList(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                list_dir.add(f.toString());
                getAllDirectoryNameList(f.toString());
            }
        }
        return list_dir;
    }

    public static String getRootDirectory() {
        return System.getProperty("user.dir");
    }

    final static Set<String> listResult = new TreeSet<>();

    /**
     * 输出该文件夹下的所有文件路径--文件绝对路径--会递归文件夹
     *
     * @param folderPath
     * @return
     */
    public static Set<String> readFileNameList(String folderPath) {
        File folder = new File(folderPath);
        Set<String> list1 = listFiles(folder.toString());
        listResult.addAll(list1);
        return listResult;
    }

    public static int count = 0;

    /**
     * 专供 readFileNameList 使用---返回该文件夹下所有的文件名字「绝对路径」，不包递归文件夹--
     *
     * @param folderName
     * @return
     */
    public static Set<String> listFiles(String folderName) {
        File folder = new File(folderName);
        List<String> list = new ArrayList<>();
        File[] fileList = folder.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                //如果是目录，继续递归下去
                if (file.isDirectory()) {
                    listFiles(file.toString());
                    //System.out.println( file.toString() );
                } else {
                    list.add(file.toString());
                    listResult.add(file.toString());
                    //       System.out.println( file.toString() );

                }
            }
        }
        return listResult;
    }

    /**
     * 读取该文件路径里面的文件
     *
     * @param path
     * @return
     */
    public List<String> readFile(final String path) {
        File file = new File(path);
        List<String> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        if (file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {

                    list.add(line.trim());

                }        //end while
                bufferedReader.close();
                inputStreamReader.close();
                fileInputStream.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("file not exists !");
        }
        return list;
    }

    /**
     * 清空文件
     *
     * @param string 文件路径
     */
    public static void clearFile(String string) {
        try {
            Path path2 = Paths.get(string);
            // 清空文件
            Files.write(path2, new byte[0]);         // 写入空字节数组以清空文件
            System.out.println("文件已清空。");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 替换单个文件里面的字符串{同时替换开头大写、小写的},
     *
     * @param path      文件路径
     * @param newString 新字符串
     * @param oldString 旧字符串
     *                  isMulti 是否替换多个 ，是
     */
    public static void replaceString(String path, String newString, String oldString, boolean isMulti) {
        //businessType---entity
        String string = CodeX.firstToSmall(newString);
        String string2 = CodeX.firstToSmall(oldString);

        //BusinessType---Entity
        String str = CodeX.firstToUpper(newString);
        String str2 = CodeX.firstToUpper(oldString);

        //BUSINESSTYPE---ENTITY
        String str_Upper = newString.toUpperCase();
        String str_Upper3 = oldString.toUpperCase();

        CodeX codeX = new CodeX();
        // 读取文件
        List<String> fileLines = codeX.readFileLines(path);
        System.out.println("读取文件...");
        try {
            Path path2 = Paths.get(path);
            //读取文件内容
            //String content = new String( Files.readAllBytes( path2 ) );
            //System.out.println( "文件内容：" );
            //System.out.println( content );
            //清空文件
            Files.write(path2, new byte[0]);         // 写入空字节数组以清空文件
            System.out.println("文件已清空!");
            System.out.println("开始写入新文件...");
            double count = 0;
            for (String s : fileLines) {
                count++;
                int totalLines = fileLines.size();
                double progress = (double) count / totalLines;
                System.out.printf("已经完成 %.2f%%\n", progress * 100);
                if (isMulti) {
                    CodeX.writeFile(path, s.replace(str2, str).replace(string2, string).replace(str_Upper3, str_Upper), true);
                } else {
                    CodeX.writeFile(path, s.replace(oldString, newString), true);
                }
            }
            System.out.println("done");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 全局替换该文件夹下的所有文件（并清除原文件）
     *
     * @param path，文件夹
     * @param newString 新字符串
     * @param oldString 旧的字符串
     */
    public static void replaceAllFileUnderDir(String path, String newString, String oldString) {
        Set<String> strings = CodeX.readFileNameList(path);
        for (String string : strings) {

        }
    }

    /**
     * 删除该路径下所有的文件和文件夹
     *
     * @param path
     */
    public static void deleteFiles(final String path) {
        File file = new File(path);
        if (file.exists()) {

            if (file.isFile()) {
                System.out.println("delete file");
                file.delete();
            } else {
                File[] files = file.listFiles();
                for (File f : files) {
                    deleteFiles(f.getAbsolutePath());
                }
                System.out.println("delete kkk");
                file.delete();
            }
        }
    }

    /**
     * 删除teslaCam文件夹下的部分文件，包含 back、json、png的文件
     *
     * @param path
     */
    private static AtomicInteger countTesla = new AtomicInteger();

    public static void deleteTeslaCamBackFiles(final String path) {
        CodeX codeX = new CodeX();
        Set<String> strings = readFileNameList(path);
        for (String string : strings) {
            System.out.println(string);
            if (string.contains("back")
                    || string.contains(".png")
                    || string.contains(".json")
                    || string.contains("right")
                    || string.contains("left")
            ) {
                System.out.println("teslaCam删除文件：" + countTesla.incrementAndGet());
                System.out.println("=======");
                System.out.println(string);

                deleteFiles(string);
            }
        }

    }

    /**
     * 删除teslaCam文件夹下的部分文件，包含left的文件
     *
     * @param string
     */
    public static void deleteTeslaCamLeftFiles(final String string) {
        if (string.contains("left")) {
            System.out.println("teslaCam删除文件：" + countTesla.incrementAndGet());
            System.out.println(string);
            deleteFiles(string);
        }
    }

    /**
     * 删除teslaCam文件夹下的部分文件，包含right的文件
     *
     * @param string
     */
    public static void deleteTeslaCamRightFiles(final String string) {
        if (string.contains("right")) {
            System.out.println("teslaCam删除文件：" + countTesla.incrementAndGet());
            System.out.println(string);
            deleteFiles(string);
        }
    }

    /**
     * 002 写入内容到该路径文件下--true追加,false覆盖
     *
     * @param path     path
     * @param content  content
     * @param isAppend true追加,false 覆盖
     */
    public static void writeFile(final String path, final String content, boolean isAppend) {
        //win--mac--system is different
        // 指定要检查的路径 .replace( "\\","\" )
        String directoryPath = "";
        if (path.contains("/")) {
            //获取文件夹路径
            directoryPath = path.substring(0, path.lastIndexOf("/"));
        } else if (path.contains("\\")) {
            directoryPath = path.substring(0, path.lastIndexOf("\\"));
        }
        Path parentPath = Paths.get(directoryPath);
        try {
            // 检查路径是否存在
            if (!Files.exists(parentPath)) {
                // 如果不存在，则创建目录
                Files.createDirectories(parentPath);
                System.out.println("目录已创建： " + directoryPath);
            } else {
                //    System.out.println( "目录已存在： " + directoryPath );
            }
        } catch (IOException e) {
            System.err.println("创建目录时发生错误： " + e.getMessage());
        }

        File file = new File(path);
        try {
            //true追加,false 覆盖
            FileOutputStream fileOutputStream = new FileOutputStream(file, isAppend);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            //写入
            bufferedWriter.write(content);
            bufferedWriter.write("\n");
            bufferedWriter.close();
            outputStreamWriter.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 读取Excel文件--只读取第一行的数据
     *
     * @param filePath
     */


    /**
     * 001 读取文件内容，每行存储到list，返回list
     *
     * @param path
     */
    public List<String> readFileLines(final String path) {

        List<String> list = new ArrayList<>();
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            System.out.println("读取文件");

            //read file
            try (FileReader fr = new FileReader(file); BufferedReader br = new BufferedReader(fr)) {
                String line;
                while ((line = br.readLine()) != null) {
                    list.add(line);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("不是文件");
            return null;
        }
        System.out.println("read over=============,all lines :" + list.size());
        return list;
    }

    //time* ----------------------------------------------------------------------------

    /**
     * 获取月份，返回格式‘yyyy/MM’---
     *
     * @param n，0:本月，1前月，-1后月
     * @return 返回格式‘yyyy/MM’
     */
    public static String getMonthly2(int n) {
        //获取当前日期
        LocalDate currentDate = LocalDate.now();

        //获取前一个月的日期
        LocalDate previousMonth = currentDate.minusMonths(n);

        //定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");

        //格式化前一个月的日期
        String formattedDate = previousMonth.format(formatter);

        //输出格式化后的日期
        return formattedDate;
    }

    public static String getMonthly() {
        //获取当前月份
        // 创建一个Calendar实例
        Calendar calendar = Calendar.getInstance();

        // 获取当前月份（注意：月份是从0开始的，所以需要加1）
        int month = calendar.get(Calendar.MONTH) + 1;

        // 输出当前月份-数字
        System.out.println("当前月份是：" + month);
        //----------------------------------------
        //----------------------------------------
        //----------------------------------------
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 定义日期格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM");

        // 格式化当前日期
        String formattedDate = currentDate.format(formatter);

        // 输出格式化后的日期
        System.out.println("当前月份是：" + formattedDate);

        return formattedDate;

    }

    /**
     * 获取当前时间的年月日
     */
    public static String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        //获取当前时间的前一天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);

        Date date = new Date();
        return sdf.format(date);
    }

    /**
     * 获取当前时间的年月日-前、后n日
     */
    public static String getCurrentDate(int n) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //获取当前时间的前一天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, n);
        Date date = new Date();
        return sdf.format(calendar.getTime());
    }

    /**
     * 获取两个日期中靠后的日期
     *
     * @param date1
     * @param date2
     * @return
     */
    public static String getLastDate(String date1, String date2) {
        return date1.compareTo(date2) > 0 ? date1 : date2;
    }

    /**
     * 获取两个日期中靠前的日期
     *
     * @param date1
     * @param date2
     * @return
     */
    public static String getPreDate(String date1, String date2) {
        return date1.compareTo(date2) < 0 ? date1 : date2;
    }

    public static String v6_general_search(LinkedHashMap<String, String> map, String prefix) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            StringBuilder stringBuilder = new StringBuilder();
            String en = entry.getKey();
            String zh = entry.getValue();
            stringBuilder.append("<div class=\"col-xs-12 col-sm-6 col-md-3 mb10\">\n" + "\t<label class=\"control-label\">" + zh + "</label>\n" + "\t<html:text property=\"" + en + "\" maxlength=\"10\" styleClass=\"" + prefix + "_" + en + " form-control\" />\n" + "   \t\t</div>");
            System.out.println(stringBuilder.toString());
        }

        return "";

    }

    public int getYear() {
        //返回当前时间的年份
        Calendar calendar = Calendar.getInstance();
        System.out.println("MONTH:");
        System.out.println(calendar.get(Calendar.MONTH));
        System.out.println(calendar.get(Calendar.DATE));
        System.out.println("DAY_OF_YEAR:");
        System.out.println(calendar.get(Calendar.DAY_OF_YEAR));
        System.out.println("DAY_OF_MONTH:");
        System.out.println(calendar.get(Calendar.DAY_OF_MONTH));
        System.out.println(calendar.get(Calendar.DAY_OF_WEEK));
        return calendar.get(Calendar.YEAR);
    }

    /**
     * 时间戳转yyyy-MM-dd日期格式
     *
     * @param timestamp
     * @return yyyy-MM-dd
     */
    public static String formatDay(Long timestamp) {
        //      long timestamp = System.currentTimeMillis();         // 获取当前时间戳
        Date date = new Date(timestamp);         // 将时间戳转换为 Date 对象---必须时long类型
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");         // 创建 SimpleDateFormat 对象，指定日期格式
        String formattedDate = sdf.format(date);         // 格式化日期
        log.info("转换后的日期： " + formattedDate);         // 输出转换后的日期
        return formattedDate;
    }

    /**
     * 时间格式转换
     *
     * @param timeStr
     * @return yyyy-MM-dd HH:mm:ss
     */
    //Thu Jun 13 00:00:00 CST 2024
    public static String formatDateTime(String timeStr) {
        String formattedTime = "";
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = inputFormat.parse(timeStr);
            formattedTime = outputFormat.format(date);
            System.out.println(formattedTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedTime;
    }

    /**
     * 获取时间戳-根据指定的时间
     */
    public static Long getTimestampFromTime(String timeStr) {
        long timestamp;
        if (ObjectUtil.isEmpty(timeStr)) {
            return null;
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");         // 设置日期格式
            Date date = dateFormat.parse(timeStr);         // 将日期字符串转换为Date对象
            timestamp = date.getTime();         // 获取时间戳
            System.out.println("时间戳： " + timestamp);
            return timestamp;
        } catch (Exception e) {
            if (e instanceof ParseException) {
                log.error("时间格式错误");
                throw new RuntimeException("时间格式错误");
            }
            log.error("有内鬼终止交易。。。");
            return null;
        }

    }
    //  * 秒：国际单位制中时间的基本单位，常用符号s表示。
    //    * 毫秒：千分之一秒，简称ms。
    //    * 微秒：百万分之一秒，简称μs。
    //    * 纳秒：十亿分之一秒，简称ns。
    //    * 皮秒：一万亿分之一秒，简称ps。
    //    * 飞秒：千万亿分之一秒，简称fs

    /**
     * 获取当天19:00时间戳
     * 时间戳的单位是毫秒。
     *
     * @return
     */
    public static long getTimestampFromTime19_00() {
        Calendar calendar = Calendar.getInstance();
        // 设置时间为当前日期和时间
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 设置小时为19，分钟和秒为0
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        // 获取时间戳+
        long timestamp = calendar.getTimeInMillis();
        log.info("当天19:00的时间戳： " + timestamp);
        return timestamp;
    }

    /**
     * 重要文档
     * https:        //blog.csdn.net/weixin_42710740/article/details/141134469
     * LocalDateTime 转为yyyy-MM-dd
     *
     * @param dateTimeStr
     * @return
     */
    public static String formatLocalDateTimeStrToYYMMDD(String dateTimeStr) {
        //      String dateTimeStr = "2023-07-04T15:30:00";         // LocalDateTime 类型的字符串
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");         // 创建 DateTimeFormatter 对象，指定输入日期格式
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, inputFormatter);         // 解析字符串为 LocalDateTime 对象
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");         // 创建 DateTimeFormatter 对象，指定输出日期格式
        String formattedDate = dateTime.format(outputFormatter);         // 格式化日期
        System.out.println("转换后的日期：" + formattedDate);         // 输出转换后的日期
        return formattedDate;
    }

    /**
     * ZonedDateTime 转为yyyy-MM-dd
     *
     * @param dateTimeStr
     * @return
     */
    public static String formatZoneDateTimeStrToYYMMDD(String dateTimeStr) {
        if (ObjectUtil.isEmpty(dateTimeStr) || !dateTimeStr.contains("T")) {
            return dateTimeStr;
        }
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeStr);
        LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = localDateTime.format(formatter);
        return formattedDate;
    }

    public String getNowDateTime() {
        return cn.hutool.core.date.DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    //   mybatis* 继承自 AbstractWrapper ,自身的内部属性 entity 也用于生成 where 条件
    //及 LambdaQueryWrapper, 可以通过 new QueryWrapper().lambda() 方法获取.
    //
    //queryWrapper.lt（）——小于
    //queryWrapper.le（）——小于等于
    //queryWrapper.gt（）——大于
    //queryWrapper.ge（）——大于等于
    //queryWrapper.eq（）——等于
    //queryWrapper.ne（）——不等于
    //queryWrapper.betweeen（“age”,10,20）——age在值10到20之间
    //queryWrapper.notBetweeen（“age”,10,20）——age不在值10到20之间
    //queryWrapper.like（“属性”,“值”）——模糊查询匹配值‘%值%’
    //queryWrapper.notLike（“属性”,“值”）——模糊查询不匹配值‘%值%’
    //queryWrapper.likeLeft（“属性”,“值”）——模糊查询匹配最后一位值‘%值’
    //queryWrapper.likeRight（“属性”,“值”）——模糊查询匹配第一位值‘值%’
    //queryWrapper.isNull（）——值为空或null
    //queryWrapper.isNotNull（）——值不为空或null
    //queryWrapper.in（“属性”，条件，条件 ）——符合多个条件的值
    //queryWrapper.notIn(“属性”，条件，条件 )——不符合多个条件的值
    //queryWrapper.or（）——或者
    //queryWrapper.and（）——和
    //queryWrapper.orderByAsc(“属性”)——根据属性升序排序
    //queryWrapper.orderByDesc(“属性”)——根据属性降序排序
    //queryWrapper.inSql(“sql语句”)——符合sql语句的值
    //queryWrapper.notSql(“sql语句”)——不符合SQL语句的值
    //queryWrapper.esists（“SQL语句”）——查询符合SQL语句的值
    //queryWrapper.notEsists（“SQL语句”）——查询不符合SQL语句的值
    //   plus --------------------------------------------
    //
    //   map* --------------------------------------------
    public static void mapTest() {
        Map<String, Integer> map = new HashMap<>();
//        map.put("one", 1);
//        map.put("two", 2);
//        map.put("three", 3);
        List<Order> orders = new ArrayList<>();
        List<User> users = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setId(String.valueOf(1));
//            user.setName(String.valueOf(i));
//            user.setAge(1);
            users.add(user);
        }
        System.out.println(JSON.toJSONString(users));
/**
 * 3-List<VO> 转为Map<id,name>, 如果key重复，则覆盖
 * return Map<String, string>
 */
        //perfect
        Map<String, String> collect1 = users.stream().collect(Collectors.toMap(User::getId, User::getUser_name, (oldValue, newValue) -> newValue));
        //会报错，重复key
        //Map<String, String> collect11 = users.stream().collect(Collectors.toMap(User::getId, User::getName));
        //会报错，重复key
        //Map<String, String> collect12 = users.stream().collect(Collectors.toMap(User::getId, User::getName));
        System.out.println(JSON.toJSONString(collect1));

        //
/**
 * 2-List<VO> 转为Map，key 为Id，value 为User, 如果key重复，则覆盖。
 * return Map<String, User>
 */
        Map<String, User> userMap = users.stream().collect(Collectors.toMap(User::getId, Function.identity(), (oldValue, newValue) -> newValue));
        //会报错的
        //Map<String, User> userMap2 = users.stream().distinct().collect(Collectors.toMap(User::getId, Function.identity()));

        //Function.identity() 作为 key 和 value 的映射函数，返回输入本身。当需要将 Stream 元素直接作为 key 或 value 时，避免显式写 x -> x。

/**
 * 1-提取List<VO> 中的id，返回idList，并去重。
 */
        //返回Set<id>
        Set<String> idList = users.stream().map(User::getId).collect(Collectors.toSet());
        //返回List<id>，需要加.distinct()
        List<String> idList222 = users.stream().distinct().map(User::getId).collect(Collectors.toList());

        System.out.println(JSON.toJSONString(idList));
        System.out.println(JSON.toJSONString(idList222));

        System.out.println(JSON.toJSONString(userMap));
        System.out.println("---------");
        System.out.println(JSON.toJSONString(orders));
        //List<VO> 转为Map，key 为Id，value 为VO
        System.out.println("=======---------");

        Map<String, Order> collect = orders.stream().collect(Collectors.toMap(Order::getField, Function.identity()));
        System.out.println(collect);
        System.out.println("=======--------end-");
        if (true) {
//            return;
        }

        //map输出方式--推荐
        Iterator<Map.Entry<String, Integer>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
/**
 *  方式1: 使用Map.Entry---10. 【推荐】使用 entrySet 遍历 Map 类集合 KV，而不是 keySet 方式进行遍历。
 */
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        // 方式2: 遍历keySet()--坚决不推荐
        for (String key : map.keySet()) {
            System.out.println("Key: " + key + ", Value: " + map.get(key));
        }

        // 方式3: 遍历values()
        for (Integer value : map.values()) {
            System.out.println("Value: " + value);
        }

        // 方式4: 使用forEach (Java 8及以上)
        map.forEach((key, value) -> System.out.println("Key: " + key + ", Value: " + value));
    }

    /**
     * map重置value为0
     *
     * @param map
     * @return
     */
    public static void resetMapValueZero(Map<String, Integer> map) {
        String jsonStr1 = JSONUtil.toJsonStr(map);
        String jsonString = JSON.toJSONString(map);
        String jsonString1 = JSONObject.toJSONString(map);
        if (ObjectUtil.isNotEmpty(map)) {
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                entry.setValue(0);
            }
        }
    }

    //   json* ---------------------------------------
    //   public void jsonTest() throws JsonProcessingException
    //   {
    //
    //      String kk = "iddd";
    //      int aa = 1;
    //              //把kk，aa封装到json中
    //      Map< String, Integer > map = new HashMap<>();
    //      map.put( kk, aa );
    //      String jsonStr1 = JSONUtil.toJsonStr( map );
    //      System.out.println( jsonStr1 );
    //
    //      String json = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
    //      JSONObject jsonObject = JSONObject.parseObject( json );
    //      System.out.println( jsonObject.getString( "name" ) );
    //      System.out.println( jsonObject.getIntValue( "age" ) );
    //      System.out.println( jsonObject.getString( "city" ) );
    //
    //              //str to jsonStr
    //              //   1-   import com.alibaba.fastjson.JSON;
    //      String jsonString = JSON.toJSONString( map );
    //
    //              // 2-   String jsonStr = JSONUtil.toJsonStr( objectDailyOrderCount);
    //
    //              //jsonStr to Map
    //              //Map< String, Integer > mapDailyOrderCount = JSONUtil.toBean( ( String ) objectDailyOrderCount, Map.class );
    //              //此方法已经过期不建议使用
    //      Object parse = JSON.parse( "" );
    //
    //              //      1-对象 --jsonStr
    //
    //              //      2- jsonStr--对象
    //      LambdaQueryWrapper< TradingData > lambdaQueryWrapper = new LambdaQueryWrapper< TradingData >();
    //      lambdaQueryWrapper.in( TradingData::getId, 1, 2, 3, 4, 5 );
    //
    //      List< TradingData > list = tradingDataService.list( lambdaQueryWrapper );
    //
    //      String jsonStr = JSONUtil.toJsonStr( list );
    //              //      Map< String, Object > map;
    //      ObjectMapper mapper = new ObjectMapper();
    //      list.clear();
    //              // 将JSON数据解析成  List< TradingData >,   ( map ->json)json-->map
    //      list = mapper.readValue( jsonStr, new TypeReference< List< TradingData > >()
    //      {
    //      } );
    //   }

    /**
     * asyn*----------对于耗时较长的查询操作，异步处理是一个非常好的选择，可以有效避免阻塞主线程，提升应用的响应性。
     * Java 提供了多种方式来实现异步处理，如 CompletableFuture 和 Spring 的 @Async 注解。下面是两种实现异步处理的方式。
     * ---------------------------------------
     * 异步执行
     */
    public static void asyncTest() throws ExecutionException, InterruptedException {

        long l = System.currentTimeMillis();
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> performQuery1("SELECT * FROM users-----performQuery1"));
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> performQuery2("SELECT * FROM orders-----performQuery2"));
        CompletableFuture<String> future3 = CompletableFuture.supplyAsync(() -> performQuery3("SELECT * FROM products--------performQuery3"));
        //      System.out.println( "allFutures-----获取每个任务的结果111" );
        //      System.out.println( "耗时：" + ( System.currentTimeMillis() - l ) );
        //      System.out.println( future1.get() );
        //      System.out.println( "耗时：" + ( System.currentTimeMillis() - l ) );
        //
        //      System.out.println( future2.get() );
        //      System.out.println( "耗时：" + ( System.currentTimeMillis() - l ) );
        //      System.out.println( future3.get() );
        //              // 异步处理查询结果
        //      System.out.println( "耗时：" + ( System.currentTimeMillis() - l ) );

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(future1, future2, future3);
        //      System.out.println( "allFutures-----获取每个任务的结果222" );
        //      System.out.println( "耗时：" + ( System.currentTimeMillis() - l ) );
        //
        //              // 获取每个任务的结果
        //      System.out.println( future1.get() );
        //      System.out.println( future2.get() );
        //      System.out.println( future3.get() );
        //      System.out.println( "耗时：" + ( System.currentTimeMillis() - l ) );

        allFutures.thenRun(() -> {
            try {
                // 获取每个任务的结果
                System.out.println(future1.get());
                System.out.println(future2.get());
                System.out.println(future3.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }).join();         // 等待所有任务完成
        System.out.println("allFutures-----获取每个任务的结果,耗时" + (System.currentTimeMillis() - l));

/**
 * 代码说明
 * supplyAsync: 提交一个异步任务，该任务由 ForkJoinPool.commonPool() 线程池执行。performQuery 方法用于模拟一个耗时查询操作。
 *
 * CompletableFuture.allOf: 用于等待所有异步任务完成。thenRun 方法在所有任务完成后执行。
 *
 * join(): 等待所有异步任务完成，并获取结果。
 */
    }

    /**
     * 普通方法按照顺序执行
     */
    public static void asyncTest2() {
        long l = System.currentTimeMillis();
        performQuery1("SELECT * FROM users");
        performQuery2("SELECT * FROM orders");
        performQuery3("SELECT * FROM products");
        long l1 = System.currentTimeMillis();
        System.out.println("asyncTest2------------耗时：" + (l1 - l));

/**
 * 代码说明
 * supplyAsync: 提交一个异步任务，该任务由 ForkJoinPool.commonPool() 线程池执行。performQuery 方法用于模拟一个耗时查询操作。
 *
 * CompletableFuture.allOf: 用于等待所有异步任务完成。thenRun 方法在所有任务完成后执行。
 *
 * join(): 等待所有异步任务完成，并获取结果。
 */
    }

    // 模拟一个耗时的查询操作
    private static String performQuery1(String query) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Result of query: " + query;
    }

    private static String performQuery2(String query) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Result of query: " + query;
    }

    private static String performQuery3(String query) {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "Result of query: " + query;
    }

//    public static void main(String[] args) throws Exception {
//
//        String general_sssentity = "/Volumes/Elements";
//        CodeX codeX07 = new CodeX();
//        codeX07.deleteTeslaCamBackFiles(general_sssentity);
//        codeX07.deleteTeslaCamRightFiles(general_sssentity);
//        codeX07.deleteTeslaCamLeftFiles(general_sssentity);
//        if (true) {
//            return;
//        }
//
//        //      Thread threadA = new Thread( () -> printABC( "A", 0, conditionA, conditionB ) );
//        //      Thread threadB = new Thread( () -> printABC( "B", 1, conditionB, conditionC ) );
//        //      Thread threadC = new Thread( () -> printABC( "C", 2, conditionC, conditionA ) );
//        Thread threadA = new Thread(() -> printABC_synchronized("A", 0));
//        Thread threadB = new Thread(() -> printABC_synchronized("B", 1));
//        Thread threadC = new Thread(() -> printABC_synchronized("C", 2));
//        threadA.start();
//        threadB.start();
//        threadC.start();
//    }

    //   thread*---------------------------------------
    public static void threadTest() {
        // 创建一个线程
        Thread thread = new Thread(() -> {
            System.out.println("Hello, World!");
        });

        // 启动线程
        thread.start();
    }

    private static final int print_count = 10;
    private static int state = 0;        //状态值
    private static final Lock lock = new ReentrantLock();
    private static final Condition conditionA = lock.newCondition();
    private static final Condition conditionB = lock.newCondition();
    private static final Condition conditionC = lock.newCondition();

    /**
     * 要实现多个线程按顺序严格交替输出（如线程 1 输出 a，线程 2 输出 b，线程 3 输出 c），可以使用 Java 的线程间通信机制，例如 ReentrantLock 和 Condition 或 synchronized 和 wait/notify。
     * 以下是使用 ReentrantLock 和 Condition 的实现方法：
     *
     * @param letter
     * @param targetState
     * @param currentCondition
     * @param nextCondition
     */
    private static void printABC(String letter, int targetState, Condition currentCondition, Condition nextCondition) {
        for (int i = 0; i < print_count; i++) {
            lock.lock();
            try {
                while (state != targetState) {
                    currentCondition.await();
                }
                // 输出字母
                System.out.println(letter);
                // 修改状态
                state = (state + 1) % 3;
                // 唤醒下一个线程
                nextCondition.signal();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

    }

    private static final Object lockObject = new Object();        // 共享锁

    private static void printABC_synchronized(String letter, int targetState) {
        for (int i = 0; i < print_count; i++) {
            System.out.println("--------------当前线程：" + Thread.currentThread().getName() + "，i = ：" + i);
            synchronized (lockObject) {
                while (state != targetState) {
                    try {
                        lockObject.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        e.printStackTrace();
                    }
                }
                System.out.println("当前线程：" + Thread.currentThread().getName());

                System.out.println(letter);
                state = (state + 1) % 3;
                System.out.println("state:" + state);
                lockObject.notifyAll();        //唤醒所有等待的线程
                System.out.println("唤醒其他线程");

            }
        }
    }

    //   Algorithm*---------------------------------------
    public static void popSort(String[] arr) {
        System.out.println("排序前：" + Arrays.asList(arr));
        for (int i = 0; i < arr.length; i++) {
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[i].compareTo(arr[j]) > 0) {
                    String temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                }
            }
        }
        //数组转为list
        System.out.println("排序后：" + Arrays.asList(arr));
    }

    public static void bucketSort(int[] arr) {
        System.out.println("排序前：");
        for (int i1 : arr) {
            System.out.print(i1 + ", ");
        }
        //桶排序是一种分布式排序算法，它将待排序的数据分布到多个有序的桶中，然后对每个桶进行排序，最后将所有桶中的数据合并成一个有序序列
        //      首先找到待排序数组的最大值和最小值，然后根据最大值和最小值的差值以及数组长度计算出桶的数量。接着，将数组中的每个元素放入对应的桶中。最后，对每个桶中的元素进行排序，并将排序后的元素依次放回原数组
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        //确定最大、最小值
        for (int i : arr) {
            max = Math.max(max, i);
            min = Math.min(min, i);
        }
        System.out.println("\n最大值：" + max + ", 最小值：" + min);
        //确定创建桶的个数
        int bucketCount = (max - min) / arr.length + 1;
        System.out.println("桶的数量：" + bucketCount);
        List<List<Integer>> buckets = new ArrayList<>();
        for (int i = 0; i < bucketCount; i++) {
            buckets.add(new ArrayList<>());
        }
        //放入桶中
        for (int i : arr) {
            int index = (i - min) / arr.length;
            buckets.get(index).add(i);
        }
        int index = 0;
        for (List<Integer> bucket : buckets) {
            //对每个桶排序
            Collections.sort(bucket);
            //将排序后的元素放回原数组
            for (Integer i : bucket) {
                arr[index++] = i;
            }
        }
        System.out.println("排序后：");
        for (int i1 : arr) {
            System.out.print(i1 + ", ");
        }
    }

    public static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }

        }
        swap(arr, i + 1, high);
        return i + 1;
    }

    public static void swap(int[] arr, int low, int high) {
        int temp = arr[low];
        arr[low] = arr[high];
        arr[high] = temp;
    }

    public static void quickSort(int[] arr, int low, int high) {
        System.out.println("排序前：");
        for (int i1 : arr) {
            System.out.print(i1 + ", ");
        }
        //使用递归来实现快速排序算法。快速排序是一种非常高效的排序算法，它的基本思想是选择一个基准元素，然后将数组分为两部分：一部分的元素都比基准小，
        //另一部分的元素都比基准大，然后对这两部分再分别进行快速排序。
        //      int low = 0;
        //      int high = arr.length - 1;
        if (low < high) {
            int pivot = partition(arr, low, high);
            quickSort(arr, low, pivot - 1);
            quickSort(arr, pivot + 1, high);
        }
        System.out.println("排序后：");
        for (int i1 : arr) {
            System.out.print(i1 + ", ");
        }
    }

    public static void insertSort(int[] arr) {
        System.out.println("排序前：");
        for (int i1 : arr) {
            System.out.print(i1 + ", ");
        }
        //从第二个开始-在已排序序列中从后向前扫描，找到相应位置并插入
        for (int i = 1; i < arr.length; i++) {
            int key = arr[i];
            int j = i - 1;
            //将arr[i]插入到arr[0..i-1]中正确的位置
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = key;
            for (int i1 : arr) {
                System.out.print(i1 + ", ");
            }

        }
        //数组转为list
        System.out.println("排序后：");

        for (int i1 : arr) {
            System.out.print(i1 + ", ");
        }
    }

    /**
     * 栈--------
     */
    /**
     * 将中缀表达式转换为后缀表达式
     *
     * @param s 中缀表达式
     * @return String字符串 后缀表达式
     * 存在中缀表达式：(2*(3-4))*5，通过下面的代码将其转换为后缀表达式，则当扫描到字符4时，栈ops中所存元素为（）
     */
    public static String postfix(String s) {
        // 后缀表达式
        StringBuilder sb = new StringBuilder();
        Stack<Character> ops = new Stack<>();
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i++);
            if (c == '(' || c == '+' || c == '-' || c == '*') {
                // 加一个空格是为了将操作数之间隔开
                sb.append(" ");
                pushOP(sb, c, ops);
                continue;
            }
            if (c == ')') {
                // 弹出操作符直到(
                while (ops.peek() != '(') {
                    sb.append(ops.pop());
                }
                ops.pop();
                continue;
            }
            sb.append(c);
        }
        // 弹出栈中元素
        while (!ops.isEmpty()) {
            sb.append(ops.pop());
        }
        return sb.toString();
    }

    public static void pushOP(StringBuilder sb, char op, Stack<Character> ops) {
        // 栈空，或者栈顶元素为(,操作符直接放入栈中
        if (ops.isEmpty() || ops.peek() == '(' || op == '(') {
            ops.add(op);
            return;
        }
        char c = ops.peek();
        // 栈顶操作符的优先级低于当前操作符，直接压入栈中
        if (c != '*' && op == '*') {
            ops.add(op);
            return;
        }
        // 否则，弹出栈顶元素，继续比较
        c = ops.pop();
        sb.append(c);
        pushOP(sb, op, ops);
    }

    /**
     * 递归求解i
     *
     * @param i
     * @return 1,=2
     * 2,= x+2,2+2=4
     * 3,= f2+f1+2,= 8
     * 4,= f3+f2+f1+2,= 16
     * Fn=F(n-1)+F(n-2)+...+F1+2===2^2,
     */
    public static int calculateI(int i) {
        if (i <= 0) {
            return 0;
        } else {
            return calculateI(i - 1) + 2;
        }
    }

    //           //   日内急涨不追多，日内急跌不追空;
    //   低位判定条件:以半年为判断周期，最近半年该品种处于跌势，最高点到最低点下跌超过 20%，日当前价格距离最低点低于该品种最近半年最高点到最低点跌幅的 1/3.高位判定条件:以半年为判断周期，最近半年该品种处于涨势，最低点到最高点上涨超过 20%，且当前价格距离最高点低于该品种最近半年最低点到最高点涨幅的 1/3.

    //   低位判定条件:以半年为判断周期，最近半年该品种处于跌势，最高点到最低点下跌超过 20%，日当前价格距离最低点低于该品种最近半年最高点到最低点跌幅的 1/3.高位判定条件:以半年为判断周期，最近半年该品种处于涨势，最低点到最高点上涨超过 20%，且当前价格距离最高点低于该品种最近半年最低点到最高点涨幅的 1/3.请用java实现，输入近半年的最高价和最低价，求低位范围和高位范围

    public static void isDdd(float currentPrice) {

    }

    public static void isGgg() {

    }

    /**
     *
     */
    public static int fibonacci(int n) {
        if (n == 1 || n == 2) {
            return 1;
        } else {
            return fibonacci(n - 2) + fibonacci(n - 1);
        }
    }

    /**
     * 斐波那契数列
     * 前100项无法正常输出
     *
     * @param n 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144, 233, 377, 610, 987, 1597, 2584, 4181, 6765, 10946, 17711, 28657, 46368, 75025, 121393, 196418, 317811, 514229, 832040, 1346269, 2178309, 3524578, 5702887, 9227465, 14930352, 24157817, 39088169, 63245986, 102334155, 165580141, 267914296, 433494437, 701408733, 1134903170, 1836311903, -1323752223, 512559680, -811192543, -298632863,
     */
    public static void fibonacciStart(int n) {
        for (int i = 1; i < n; i++) {
            System.out.print(fibonacci(i) + ", ");
        }
    }

    //   -----
    public static void fibonacciStartX(int n) {
        for (int i = 1; i < n; i++) {
            System.out.print(fibonacci(i) + ", ");
            System.out.println("index " + i);
        }
    }

    public static BigInteger fibonacciY(int n) {

        BigInteger a = BigInteger.valueOf(n);
        if (n == 1 || n == 2) {
            return BigInteger.valueOf(1);
        } else {
            return fibonacciY(n - 2).add(fibonacciY(n - 1));
        }
    }

    //   -------
    public static void fibonacciZ(int n) {
        BigInteger[] fib = new BigInteger[n];

        // 计算并输出前100项

        System.out.println("斐波那契数列前" + n + "项：");
        // 初始化前两项
        fib[1] = BigInteger.ONE;
        fib[0] = BigInteger.ONE;

        for (int i = 0; i < n; i++) {
            if (i == 1 || i == 0) {
                System.out.println("index" + (i + 1) + " : " + fib[1]);
                continue;
            }
            fib[i] = fib[i - 1].add(fib[i - 2]);
            System.out.println("index" + (i + 1) + " : " + fib[i]);
        }
    }

    /**
     * 问题描述：给定一个无序的整数数组，找到其中最长上升子序列的长度。
     * 最长上升子序列长度
     */
    public static int lastLarge(Integer[] nums) {
        int n = nums.length;
        int[] result = new int[n];
        //{ 10, 9, 2, 5, 3, 7, 101, 18 };
        for (int i = 0; i < n; i++) {
            int pre = nums[i];
            int count = 1;
            for (int j = i + 1; j < n; j++) {
                if (nums[j] <= pre) {
                    result[i] = count;
                    break;
                } else {
                    count++;
                    pre = nums[j];

                }
            }
            System.out.println("----for");
        }
        result[n - 1] = 1;

        Arrays.sort(nums, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        return result[0];
    }

    /**
     * java正则表达式匹配左右括号直接的所有字符 @RequiresPermissions("system:merchants:query")
     *
     * @param permission x
     * @return x
     */
    public static String replacePermission(String permission) {
        // 定义正则表达式模式
        String pattern = "\\(\"(.*?)\"\\)";

        // 编译正则表达式
        Pattern r = Pattern.compile(pattern);
        // 创建匹配器对象
        Matcher m = r.matcher(permission);
        if (m.find() && permission.contains("RequiresPermissions")) {
            // 提取匹配的子字符串

            // System.out.println( "Matched Substring: " + matchedSubstring );
            String matched = m.group(1);

            String matchedString = "@PreAuthorize(\"@ss.hasPermi('" + matched + "')\")";
            System.out.println("Matched: " + matched);
            return matchedString;
        }

        return permission;
    }

    public static void demiReplacePermission(String path) {

        Set<String> strings = listFiles(path);
        for (String string : strings) {
            CodeX codeX = new CodeX();
            List<String> strings1 = codeX.readFileLines(string);
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (String s : strings1) {
                i++;
                if (i == 2) {
                    //       sb.append( "import com.ruoyi.common.annotation.Log;" );
                }
                if (s.contains("core.web")) {
                    continue;
                }
                sb.append(replacePermission(s));
                sb.append("\n");
            }
            writeFile(string, sb.toString(), true);
        }
    }
    //   end andy

    /**
     * v0-------
     */
    /**
     * v0--生成 @Parameter(name = "detailDescription", description = "保额说明", required = false)
     *
     * @param path
     */
    public static void V0_Controller(String path) {
        CodeX codeX = new CodeX();
        List<String> strings9 = codeX.readFileLines(path);
        for (String s : strings9) {
            if (s.contains("@Schema") && !s.contains("DTO")) {
                String[] s1 = s.split(",");
                String s11 = s1[0].split("=")[1].replace("\"", "").trim();
                String s22 = s1[1].split("=")[1].replace("\"", "").replace(")", "").trim();
                System.out.println("@Parameter(name = \"" + s11 + "\", description = \"" + s22 + "\", required = false)");
            }
        }
    }

}
