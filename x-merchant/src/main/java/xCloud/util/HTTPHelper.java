package xCloud.util;

/**
 * @Description
 * @Author Andy Fan
 * @Date 2025/7/7 13:35
 * @ClassName HTTPHelper
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPHelper {
    // slf4j日志记录器
    private static final Logger LOG = LoggerFactory.getLogger(HTTPHelper.class);

    /***
     * 向指定URL发送GET方法的请求
     *
     * @param apiUrl
     * @param data
     * @param projectId
     * @param signature
     * @param encoding
     * @return
     * @throws Exception
     */
    public static String sendGet(String apiUrl, HashMap<String, Object> param,
                                 LinkedHashMap<String, String> headers, String encoding) throws Exception {
        // 获得响应内容
        String http_RespContent = null;
        HttpURLConnection httpURLConnection = null;
        int http_StatusCode = 0;
        String http_RespMessage = null;
        try {
            // 实际请求完整Url
            StringBuffer fullUrl = new StringBuffer();
            if (null != param) {
                if (0 != param.size()) {
                    StringBuffer params = new StringBuffer();
                    for (Entry<String, Object> entry : param.entrySet()) {
                        params.append(entry.getKey());
                        params.append("=");
                        params.append(entry.getValue().toString());
                        params.append("&");
                    }
                    if (params.length() > 0) {
                        params.deleteCharAt(params.lastIndexOf("&"));
                    }
                    fullUrl.append(apiUrl).append((params.length() > 0) ? "?" + params.toString() : "");
                } else {
                    fullUrl.append(apiUrl);
                }
            } else {
                fullUrl.append(apiUrl);
            }

            LOG.info(">>>> 实际请求Url: " + fullUrl.toString());

            // 建立连接
            URL url = new URL(fullUrl.toString());
            httpURLConnection = (HttpURLConnection) url.openConnection();
            // 需要输出
            httpURLConnection.setDoOutput(true);
            // 需要输入
            httpURLConnection.setDoInput(true);
            // 不允许缓存
            httpURLConnection.setUseCaches(false);
            // HTTP请求方式
            httpURLConnection.setRequestMethod("GET");
            // 设置Headers
            if (null != headers) {
                for (String key : headers.keySet()) {
                    httpURLConnection.setRequestProperty(key, headers.get(key));
                }
            }
            // 连接会话
            httpURLConnection.connect();
            // 获得响应状态(HTTP状态码)
            http_StatusCode = httpURLConnection.getResponseCode();
            // 获得响应消息(HTTP状态码描述)
            http_RespMessage = httpURLConnection.getResponseMessage();
            // 获得响应内容
            if (HttpURLConnection.HTTP_OK == http_StatusCode) {
                // 返回响应结果
                http_RespContent = getResponseContent(httpURLConnection);
            } else {
                // 返回非200状态时响应结果
                http_RespContent = getErrorResponseContent(httpURLConnection);
                String msg =
                        MessageFormat.format("请求失败: Http状态码 = {0} , {1}", http_StatusCode, http_RespMessage);
                LOG.info(msg);
            }
        } catch (UnknownHostException e) {
            String message = MessageFormat.format("网络请求时发生异常: {0}", e.getMessage());
            Exception ex = new Exception(message);
            ex.initCause(e);
            throw ex;
        } catch (MalformedURLException e) {
            String message = MessageFormat.format("格式错误的URL: {0}", e.getMessage());
            Exception ex = new Exception(message);
            ex.initCause(e);
            throw ex;
        } catch (IOException e) {
            String message = MessageFormat.format("网络请求时发生异常: {0}", e.getMessage());
            Exception ex = new Exception(message);
            ex.initCause(e);
            throw ex;
        } catch (Exception e) {
            String message = MessageFormat.format("网络请求时发生异常: {0}", e.getMessage());
            Exception ex = new Exception(message);
            ex.initCause(e);
            throw ex;
        } finally {
            if (null != httpURLConnection) {
                httpURLConnection.disconnect();
            }
        }
        return http_RespContent;
    }

    /***
     * 向指定URL发送POST方法的请求
     *
     * @param apiUrl
     * @param data
     * @param projectId
     * @param signature
     * @param encoding
     * @return
     * @throws Exception
     */
    public static String sendPOST(String apiUrl, String data, LinkedHashMap<String, String> headers,
                                  String encoding) throws Exception {
        // 获得响应内容
        String http_RespContent = null;
        HttpURLConnection httpURLConnection = null;
        int http_StatusCode = 0;
        String http_RespMessage = null;
        try {
            // 建立连接
            URL url = new URL(apiUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            // 需要输出
            httpURLConnection.setDoOutput(true);
            // 需要输入
            httpURLConnection.setDoInput(true);
            // 不允许缓存
            httpURLConnection.setUseCaches(false);
            // HTTP请求方式
            httpURLConnection.setRequestMethod("POST");
            // 设置Headers
            if (null != headers) {
                for (String key : headers.keySet()) {
                    httpURLConnection.setRequestProperty(key, headers.get(key));
                }
            }
            // 连接会话
            httpURLConnection.connect();
            // 建立输入流，向指向的URL传入参数
            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            // 设置请求参数
            dos.write(data.getBytes(encoding));
            dos.flush();
            dos.close();
            // 获得响应状态(HTTP状态码)
            http_StatusCode = httpURLConnection.getResponseCode();
            // 获得响应消息(HTTP状态码描述)
            http_RespMessage = httpURLConnection.getResponseMessage();
            // 获得响应内容
            if (HttpURLConnection.HTTP_OK == http_StatusCode) {
                // 返回响应结果
                http_RespContent = getResponseContent(httpURLConnection);
            } else {
                // 返回非200状态时响应结果
                http_RespContent = getErrorResponseContent(httpURLConnection);
                String msg =
                        MessageFormat.format("请求失败: Http状态码 = {0} , {1}", http_StatusCode, http_RespMessage);
                LOG.info(msg);
            }
        } catch (UnknownHostException e) {
            String message = MessageFormat.format("网络请求时发生异常: {0}", e.getMessage());
            Exception ex = new Exception(message);
            ex.initCause(e);
            throw ex;
        } catch (MalformedURLException e) {
            String message = MessageFormat.format("格式错误的URL: {0}", e.getMessage());
            Exception ex = new Exception(message);
            ex.initCause(e);
            throw ex;
        } catch (IOException e) {
            String message = MessageFormat.format("网络请求时发生异常: {0}", e.getMessage());
            Exception ex = new Exception(message);
            ex.initCause(e);
            throw ex;
        } catch (Exception e) {
            String message = MessageFormat.format("网络请求时发生异常: {0}", e.getMessage());
            Exception ex = new Exception(message);
            ex.initCause(e);
            throw ex;
        } finally {
            if (null != httpURLConnection) {
                httpURLConnection.disconnect();
            }
        }
        return http_RespContent;
    }

    /***
     * 读取HttpResponse响应内容
     *
     * @param httpURLConnection
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static String getResponseContent(HttpURLConnection httpURLConnection)
            throws UnsupportedEncodingException, IOException {
        StringBuffer contentBuffer = null;
        BufferedReader responseReader = null;
        try {
            contentBuffer = new StringBuffer();
            String readLine = null;
            responseReader =
                    new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), "UTF-8"));
            while ((readLine = responseReader.readLine()) != null) {
                contentBuffer.append(readLine);
            }
        } finally {
            if (null != responseReader) {
                responseReader.close();
            }
        }
        return contentBuffer.toString();
    }

    /***
     * 读取HttpResponse响应内容
     *
     * @param httpURLConnection
     * @return
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private static String getErrorResponseContent(HttpURLConnection httpURLConnection)
            throws UnsupportedEncodingException, IOException {
        StringBuffer contentBuffer = null;
        BufferedReader responseReader = null;
        try {
            contentBuffer = new StringBuffer();
            String readLine = null;
            responseReader =
                    new BufferedReader(new InputStreamReader(httpURLConnection.getErrorStream(), "UTF-8"));
            while ((readLine = responseReader.readLine()) != null) {
                contentBuffer.append(readLine);
            }
        } finally {
            if (null != responseReader) {
                responseReader.close();
            }
        }
        return contentBuffer.toString();
    }
}