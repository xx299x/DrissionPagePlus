package com.ll.DrissonPage.units.setter;

import com.ll.DrissonPage.page.WebPageTab;
import com.ll.DrissonPage.units.cookiesSetter.WebPageCookiesSetter;

import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class WebPageTabSetter extends TabSetter {
    private final WebPageTab pageTab;
    private final SessionPageSetter sessionPageSetter;
    private final ChromiumBaseSetter chromiumBaseSetter;

    public WebPageTabSetter(WebPageTab page) {
        super(page.getPage().getChromiumPage());
        pageTab = page;
        sessionPageSetter = new SessionPageSetter(pageTab.getPage().getSessionPage());
        chromiumBaseSetter = new ChromiumBaseSetter(pageTab.getPage().getChromiumPage());
    }

    /**
     * @return 返回用于设置cookies的对象
     */
    public WebPageCookiesSetter cookies() {
        if (super.cookiesSetter == null) super.cookiesSetter = new WebPageCookiesSetter(pageTab.getPage());
        return (WebPageCookiesSetter) super.cookiesSetter;
    }

    /**
     * 设置固定发送的headers
     *
     * @param headers map
     */
    public void headers(Map<String, Object> headers) {
        if (this.pageTab.isHasSession()) this.sessionPageSetter.headers(headers);
        if (this.pageTab.isHasDriver()) this.chromiumBaseSetter.headers(headers);
    }

    /**
     * 设置user agent，d模式下只有当前tab有效
     */
    public void userAgent(String ua, String platform) {
        if (this.pageTab.isHasSession()) sessionPageSetter.userAgent(ua);
        if (this.pageTab.isHasDriver()) this.chromiumBaseSetter.userAgent(ua, platform);
    }

}
