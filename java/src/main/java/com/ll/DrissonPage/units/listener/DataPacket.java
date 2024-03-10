package com.ll.DrissonPage.units.listener;

import com.alibaba.fastjson.JSONObject;

import java.util.Map;

/**
 * 返回的数据包管理类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class DataPacket {
    private String tabId;
    private String target;
    private boolean isFailed;

    protected JSONObject rawRequest;
    protected JSONObject rawResponse;

    protected String rawPostData;
    protected String rawBody;
    protected Map<String, Object> rawFailInfo;
    protected Boolean base64Body;
    private Request request;
    private Response response;
    private FailInfo failInfo;

    protected String resourceType;
    protected Map<String, Object> requestExtraInfo;
    protected Map<String, Object> responseExtraInfo;

    /**
     * @param tabId  产生这个数据包的tab的id
     * @param target 监听目标
     */
    public DataPacket(String tabId, String target) {
        this.tabId = tabId;
        this.target = target;
    }

    public String url() {
        return this.request.url;
    }

    public String method() {
        return this.request.method;
    }

    public String frameId() {
        return this.rawRequest.getString("frameId");
    }

    public Request request() {
        if (this.request == null)
            this.request = new Request(this, this.rawRequest.getJSONObject("request"), this.rawPostData);
        return this.request;
    }

    public Response response() {
        if (this.response == null) this.response = new Response(this, this.rawResponse, this.rawBody, this.base64Body);
        return this.response;
    }

    public FailInfo failInfo() {
        if (this.failInfo == null) this.failInfo = new FailInfo(this, this.rawFailInfo);
        return this.failInfo;
    }

    /**
     * 等待额外的信息加载完成
     *
     * @return 是否等待成功
     */
    public boolean waitExtraInfo() {
        return waitExtraInfo(null);
    }

    /**
     * 等待额外的信息加载完成
     *
     * @param timeout 超时时间，null为无限等待
     * @return 是否等待成功
     */
    public boolean waitExtraInfo(Double timeout) {
        if (timeout == null) {
            while (this.requestExtraInfo == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        } else {
            long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
            while (System.currentTimeMillis() < endTime) {
                if (this.requestExtraInfo != null) return true;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return false;
        }
    }
}
