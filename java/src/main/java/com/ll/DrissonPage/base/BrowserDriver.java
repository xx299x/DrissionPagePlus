package com.ll.DrissonPage.base;

import com.alibaba.fastjson.JSON;
import com.ll.DrissonPage.utils.CloseableHttpClientUtils;
import org.apache.http.client.methods.HttpGet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 浏览器驱动
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */

public class BrowserDriver extends Driver {
    protected static final Map<String, BrowserDriver> BROWSERS = new ConcurrentHashMap<>();


    private BrowserDriver(String tabId, String tabType, String address, Occupant occupant) {
        super(tabId, tabType, address, occupant);
    }

    public static BrowserDriver getInstance(String tabId, String tabType, String address, Occupant occupant) {

        return BROWSERS.computeIfAbsent(tabId, key -> new BrowserDriver(tabId, tabType, address, occupant));
    }

    @Override
    public String toString() {
        return "<BrowserDriver " + this.getId() + ">";
    }

    /**
     * 发送请求
     *
     * @param url 请求地址
     * @return 返回值可能是List<Map> 或者Map
     */
    public Object get(String url) {
        HttpGet request = new HttpGet(url);
        request.setHeader("Connection", "close");
        String text = CloseableHttpClientUtils.sendRequestJson(request);
        return JSON.parse(text);
    }


}