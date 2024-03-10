package com.ll.DrissonPage.utils;


import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Collection;

/**
 * 可关闭连接请求工具
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class CloseableHttpClientUtils {
    private static final CloseableHttpClient closeableHttpClient;

    static {
        closeableHttpClient = reconnectCloseableHttpClient();
    }

    private CloseableHttpClientUtils() {

    }

    public static CloseableHttpClient closeableHttpClient() {
        return closeableHttpClient;
    }

    public static synchronized CloseableHttpClient reconnectCloseableHttpClient() {
        return reconnectCloseableHttpClient(null);
    }

    public static synchronized CloseableHttpClient reconnectCloseableHttpClient(Collection<? extends Header> defaultHeaders) {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(false).setSoReuseAddress(true).setSoTimeout(120_000).build();
        httpClientBuilder.setDefaultHeaders(defaultHeaders);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(120_000).setSocketTimeout(120_000).setConnectionRequestTimeout(30).build();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);
        httpClientBuilder.setDefaultSocketConfig(socketConfig);
        return httpClientBuilder.build();
    }

    /**
     * @param request 发送get或者post请求
     * @return 返回请求体
     * @throws IOException 异常
     */
    public static HttpEntity sendRequest(HttpUriRequest request) throws IOException {
        CloseableHttpResponse execute;
        CloseableHttpClient client = CloseableHttpClientUtils.closeableHttpClient();
        try {
            execute = client.execute(request);
        } catch (Exception e) {
            CloseableHttpClient closeableHttpClient1=null;
            try {
                closeableHttpClient1 = CloseableHttpClientUtils.reconnectCloseableHttpClient();
            } finally {
                if (closeableHttpClient1 != null) {
                    closeableHttpClient1.close();
                }
            }
            execute = client.execute(request);
        }
        return execute.getEntity();
    }


    /**
     * @param request 发送get或者post请求
     * @return 返回请求体
     */
    public static String sendRequestJson(HttpUriRequest request) {
        HttpEntity entity;
        String json;
        try {
            entity = CloseableHttpClientUtils.sendRequest(request);
        } catch (IOException e) {
//            System.out.println("发送请求失败->错误原因" + e);
            return null;
        }
        try {
            json = EntityUtils.toString(entity);
        } catch (IOException e) {
            System.out.println("解析请求失败->错误原因" + e);

            return null;

        }
        try {
            EntityUtils.consume(entity);//释放连接
        } catch (Exception e) {
            System.out.println("释放请求失败->错误原因" + e);
        }
        return json;
    }
}
