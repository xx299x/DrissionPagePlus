package com.ll.DrissonPage.units.states;

import com.ll.DrissonPage.error.extend.PageDisconnectedError;
import com.ll.DrissonPage.page.ChromiumBase;
import lombok.AllArgsConstructor;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class PageStates {
    private final ChromiumBase page;

    /**
     * @return 返回页面是否在加载状态
     */
    public boolean isLoading() {
        return this.page.getIsLoading();
    }

    /**
     * @return 返回页面对象是否仍然可用
     */
    public boolean isAlive() {
        try {
            this.page.runCdp("Page.getLayoutMetrics");
            return true;
        } catch (PageDisconnectedError e) {
            return false;
        }
    }

    /**
     * @return 返回当前页面加载状态，'connecting' 'loading' 'interactive' 'complete'
     */
    public String readyState() {
        return this.page.getReadyState();
    }

    /**
     * @return 返回当前页面是否存在弹窗
     */
    public boolean hasAlert() {
        return this.page.getHasAlert();
    }
}
