package com.ll.DrissonPage.units.states;

import com.alibaba.fastjson.JSON;
import com.ll.DrissonPage.error.extend.ElementLostError;
import com.ll.DrissonPage.error.extend.PageDisconnectedError;
import com.ll.DrissonPage.page.ChromiumFrame;

import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class FrameStates extends PageStates {
    private final ChromiumFrame frame;

    public FrameStates(ChromiumFrame page) {
        super(page.getTargetPage());
        frame = page;
    }


    /**
     * @return 返回页面是否在加载状态
     */
    public boolean isLoading() {
        return this.frame.getIsLoading();
    }

    /**
     * @return 返回页面对象是否仍然可用
     */
    public boolean isAlive() {
        try {
            return JSON.parseObject(this.frame.getTargetPage().runCdp("DOM.describeNode", Map.of("backendNodeId", this.frame.getFrameEle().getBackendId())).toString()).get("node").toString().contains("frameId");
        } catch (PageDisconnectedError | ElementLostError e) {
            return false;
        }
    }

    /**
     * @return 返回当前页面加载状态，'connecting' 'loading' 'interactive' 'complete'
     */
    public String readyState() {
        return this.frame.getTargetPage().getReadyState();
    }

    /**
     * @return 返回元素是否显示
     */
    public boolean isDisplayed() {
        return !(this.frame.getFrameEle().style("visibility").equals("hidden") || Boolean.parseBoolean(this.frame.getFrameEle().runJs("return this.offsetParent === null;").toString()) || this.frame.getFrameEle().style("display").equals("none"));
    }

    /**
     * @return 返回当前页面是否存在弹窗
     */
    public boolean hasAlert() {
        return this.frame.getHasAlert();
    }
}
