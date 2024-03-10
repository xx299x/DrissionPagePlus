package com.ll.DrissonPage.units.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Response {
    private final DataPacket dataPacket;

    private final Map<String, Object> response;
    private String rawBody;
    private boolean isBase64Body;
    private Object body;
    private Map<String, Object> headers;

    public Response(DataPacket dataPacket, Map<String, Object> rawResponse, String rawBody, boolean base64Body) {
        this.dataPacket = dataPacket;
        this.response = rawResponse;
        this.rawBody = rawBody;
        this.isBase64Body = base64Body;
        this.body = null;
        this.headers = null;
    }

    /**
     * @return 以大小写不敏感字符串返回headers数据
     */
    public Map<String, Object> headers() {
        if (this.headers == null)
            this.headers = new CaseInsensitiveMap<>(JSON.parseObject(JSON.toJSONString(this.response.get("headers"))));
        return this.headers;
    }

    /**
     * @return 返回未被处理的body文本
     */
    public String rawBody() {
        return this.rawBody;
    }

    /**
     * @return 返回body内容，如果是json格式，自动进行转换，如果时图片格式，进行base64转换，其它格式直接返回文本
     */
    public Object body() {
        if (body == null) {
            if (this.isBase64Body) {
                byte[] decodedBytes = Base64.getDecoder().decode(this.rawBody);
                this.body = new String(decodedBytes, StandardCharsets.UTF_8);
            } else {
                try {
                    this.body = JSON.parse(this.rawBody);
                } catch (JSONException e) {
                    this.body = this.rawBody;
                }
            }
        }
        return this.body;
    }
}
