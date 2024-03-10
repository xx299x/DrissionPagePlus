package com.ll.DrissonPage.units.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Request {
    protected String url;
    protected CaseInsensitiveMap<String, Object> headers;
    protected String method;
    private final Map<String, Object> request;
    private final String rawPostData;
    private String postData;
    private final DataPacket dataPacket;

    public Request(DataPacket dataPacket, Map<String, Object> rawRequest, String postData) {
        this.dataPacket = dataPacket;
        this.request = rawRequest;
        this.rawPostData = postData;
        this.postData = null;
    }

    /**
     * @return 以大小写不敏感字符串返回headers数据
     */
    public Map<String, Object> headers() {
        if (this.headers == null)
            this.headers = new CaseInsensitiveMap<>(JSON.parseObject(JSON.toJSONString(this.request.get("headers"))));
        return this.headers;
    }

    /**
     * @return 返回postData数据 如果是其他类型则会格式化成string
     */
    public String postData() {
        if (this.postData == null) {
            Object postData;
            if (this.rawPostData != null) {
                postData = this.rawPostData;
            } else if (this.request.get("postData") != null) {
                postData = this.request.get("postData");
            } else {
                postData = false;
            }
            try {
                this.postData = JSON.parse(postData.toString()).toString();
            } catch (JSONException e) {
                this.postData = postData.toString();
            }
        }
        return this.postData;
    }

    public RequestExtraInfo extraInfo() {
        Map<String, Object> requestExtraInfo1 = this.dataPacket.requestExtraInfo;
        return new RequestExtraInfo(requestExtraInfo1 == null ? new HashMap<>() : requestExtraInfo1);
    }
}
