package com.ll.DrissonPage.units.cookiesSetter;

import com.ll.DrissonPage.page.WebMode;
import com.ll.DrissonPage.page.WebPage;

import java.util.Objects;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class WebPageCookiesSetter extends CookiesSetter {
    private final WebPage page;
    private final SessionCookiesSetter sessionCookiesSetter;

    public WebPageCookiesSetter(WebPage page) {
        super(page.getChromiumPage());
        this.page = page;
        sessionCookiesSetter = new SessionCookiesSetter(page.getSessionPage());
    }

    /**
     * 删除一个cookie
     *
     * @param name   cookie的name字段
     * @param url    cookie的url字段，可选 d模式时才有效
     * @param domain cookie的domain字段，可选 d模式时才有效
     * @param path   cookie的path字段，可选 d模式时才有效
     */

    public void remove(String name, String url, String domain, String path) {
        if (Objects.equals(this.page.mode(), WebMode.d) && this.page.isHasDriver())
            super.remove(name, url, domain, path);
        else if (Objects.equals(this.page.mode(), WebMode.s) && this.page.isHasSession())
            sessionCookiesSetter.remove(name);
    }

    /**
     * 清除cookies
     */
    public void clear() {
        if (Objects.equals(this.page.mode(), WebMode.d) && this.page.isHasDriver()) super.clear();
        else if (Objects.equals(this.page.mode(), WebMode.s) && this.page.isHasSession()) sessionCookiesSetter.clear();
    }
}
