package com.ll.DrissonPage.units.cookiesSetter;

import com.ll.DrissonPage.page.SessionPage;
import lombok.AllArgsConstructor;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class SessionCookiesSetter {
    private final SessionPage page;

    /**
     * 删除一个cookie
     *
     * @param name cookie的name字段
     */
    public void remove(String name) {
        if (name != null && !name.isEmpty()) {
            OkHttpClient.Builder builder = this.page.session().newBuilder();
            builder.setCookieJar$okhttp(new CookieJar() {
                @Override
                public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                    list.stream().filter(cookie -> cookie.name().equals(name)).findFirst().ifPresent(list::remove);
                }

                @NotNull
                @Override
                public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                    return new ArrayList<>();
                }
            });
            this.page.setSession(builder.build());
        }

    }

    /**
     * 清除cookies
     */
    public void clear() {
        OkHttpClient.Builder builder = this.page.session().newBuilder();
        builder.setCookieJar$okhttp(new CookieJar() {
            @Override
            public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                list.clear();
            }

            @NotNull
            @Override
            public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                return new ArrayList<>();
            }
        });
        this.page.setSession(builder.build());
    }
}
