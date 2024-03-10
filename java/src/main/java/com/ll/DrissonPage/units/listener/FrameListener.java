package com.ll.DrissonPage.units.listener;

import com.alibaba.fastjson.JSON;
import com.ll.DrissonPage.page.ChromiumBase;
import com.ll.DrissonPage.page.ChromiumFrame;

import java.util.Objects;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class FrameListener extends Listener {
    public FrameListener(ChromiumFrame chromiumBase) {
        super(chromiumBase);
    }

    public void toTarget(String targetId, String address, ChromiumBase page) {
        super.toTarget(targetId, address, page);
    }

    /**
     * 接收到请求时的回调函数
     */
    @Override
    protected void requestWillBeSent(Object params) {
        if (!((ChromiumFrame) super.page).isDiffDomain() && Objects.equals(JSON.parseObject(params.toString()).get("frameId"), this.page.getFrameId()))
            return;
        super.requestWillBeSent(params);
    }

    /**
     * 接收到返回信息时处理方法
     */
    @Override
    protected void responseReceived(Object params) {
        if (!((ChromiumFrame) super.page).isDiffDomain() && Objects.equals(JSON.parseObject(params.toString()).get("frameId"), this.page.getFrameId()))
            return;
        super.responseReceived(params);
    }
}
