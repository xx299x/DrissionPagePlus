package com.ll.DrissonPage.units.setter;

import com.ll.DrissonPage.functions.Tools;
import com.ll.DrissonPage.page.ChromiumPage;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class PageWindowSetter extends WindowSetter {
    public PageWindowSetter(ChromiumPage page) {
        super(page);
    }

    /**
     * 隐藏浏览器窗口，只在Windows系统可用
     */
    public void hide() {
        Tools.showOrHideBrowser((ChromiumPage) page, true);
    }

    /**
     * 显示浏览器窗口，只在Windows系统可用
     */
    public void show() {
        Tools.showOrHideBrowser((ChromiumPage) page, false);
    }
}
