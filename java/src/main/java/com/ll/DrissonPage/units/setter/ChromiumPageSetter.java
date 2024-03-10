package com.ll.DrissonPage.units.setter;

import com.ll.DrissonPage.page.ChromiumPage;
import com.ll.DrissonPage.page.ChromiumTab;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */

public class ChromiumPageSetter extends TabSetter {
    public ChromiumPageSetter(ChromiumPage page) {
        super(page);
    }

    /**
     * 激活标签页使其处于最前面
     */
    public void tabToFront() {
        tabToFront("");
    }

    /**
     * 激活标签页使其处于最前面
     */
    public void tabToFront(ChromiumTab chromiumTab) {
        tabToFront(chromiumTab.tabId());
    }

    /**
     * 激活标签页使其处于最前面
     */
    public void tabToFront(String tabOrId) {
        this.page.browser().activateTab(tabOrId == null || tabOrId.isEmpty() ? this.page.tabId() : tabOrId);
    }


    /**
     * @return 返回用于设置浏览器窗口的对象
     */
    public PageWindowSetter window() {
        if (this.windowSetter == null) this.windowSetter = new PageWindowSetter((ChromiumPage) this.page);
        return (PageWindowSetter) this.windowSetter;
    }
}
