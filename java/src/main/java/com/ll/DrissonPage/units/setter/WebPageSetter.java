package com.ll.DrissonPage.units.setter;

import com.ll.DrissonPage.page.WebMode;
import com.ll.DrissonPage.page.WebPage;
import com.ll.DrissonPage.units.cookiesSetter.WebPageCookiesSetter;

import java.util.Map;
import java.util.Objects;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class WebPageSetter extends ChromiumPageSetter {
    private final WebPage page;
    private final SessionPageSetter sessionPageSetter;
    private final ChromiumPageSetter chromiumPageSetter;

    public WebPageSetter(WebPage page) {
        super(page.getChromiumPage());
        this.page = page;
        sessionPageSetter = new SessionPageSetter(page.getSessionPage());
        chromiumPageSetter = new ChromiumPageSetter(page.getChromiumPage());
    }

    /**
     * @return 返回用于设置cookies的对象
     */
    public WebPageCookiesSetter cookies() {
        if (super.cookiesSetter == null) super.cookiesSetter = new WebPageCookiesSetter(this.page);
        return (WebPageCookiesSetter) super.cookiesSetter;
    }

    /**
     * 设置固定发送的headers
     */
    public void header(Map<String, Object> headers) {
        if (Objects.requireNonNull(this.page.mode()) == WebMode.s) sessionPageSetter.headers(headers);
        else this.chromiumPageSetter.headers(headers);
    }

    /**
     * 设置user agent，d模式下只有当前tab有效
     */
    public void userAgent(String ua, String platform) {
        if (Objects.requireNonNull(this.page.mode()) == WebMode.s) sessionPageSetter.userAgent(ua);
        else this.chromiumPageSetter.userAgent(ua, platform);
    }
}
