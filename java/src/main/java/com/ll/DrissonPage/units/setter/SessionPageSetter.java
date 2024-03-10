package com.ll.DrissonPage.units.setter;

import com.ll.DrissonPage.page.SessionPage;
import com.ll.DrissonPage.units.cookiesSetter.SessionCookiesSetter;
import okhttp3.Authenticator;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import javax.net.ssl.HostnameVerifier;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class SessionPageSetter extends BasePageSetter<SessionPage> {
    private SessionCookiesSetter cookiesSetter;

    /**
     * @param page SessionPage对象
     */
    public SessionPageSetter(SessionPage page) {
        super(page);
        this.cookiesSetter = null;
    }

    public SessionCookiesSetter cookies() {
        if (cookiesSetter == null) cookiesSetter = new SessionCookiesSetter(this.page);
        return cookiesSetter;
    }

    /**
     * 设置连接失败时重连次数
     *
     * @param times 次数
     */
    public void retryTimes(Integer times) {
        this.page.setRetryTimes(times);
    }

    /**
     * 设置连接失败时重连间隔
     *
     * @param interval 秒
     */
    public void retryInterval(Double interval) {
        this.page.setRetryInterval(interval);
    }

    /**
     * 设置下载路径
     *
     * @param path 下载路径
     */
    public void downloadPath(String path) {
        downloadPath(Paths.get(path));
    }

    /**
     * 设置下载路径
     *
     * @param path 下载路径
     */
    public void downloadPath(Path path) {
        String string = path.toAbsolutePath().toString();
        this.page.setDownloadPath(string);
        if (this.page.getDownloadKit() != null) {
            this.page.getDownloadKit().set().goalPath(string);
        }
    }

    /**
     * 设置连接超时时间
     *
     * @param second 秒数
     */

    public void timeout(Double second) {
        this.page.setTimeout(second);
    }

    public void encoding(Charset encoding) {
        encoding(encoding, true);
    }

    /**
     * 设置编码
     *
     * @param encoding 编码
     * @param setAll   是否设置对象参数，为False则只设置当前Response
     */
    public void encoding(Charset encoding, boolean setAll) {
        if (setAll) {
            if (encoding == null) encoding = StandardCharsets.US_ASCII;
            this.page.setEncoding(encoding);
        }
        try (Response response = this.page.response()) {
            if (response != null && response.body() != null) {
                MediaType mediaType = response.body().contentType();
                if (mediaType != null) mediaType.charset(encoding);
            }
        }
    }

    /**
     * 设置通用的headers
     *
     * @param headers map
     */
    public void headers(Map<String, Object> headers) {
        this.page.setHeaders(new CaseInsensitiveMap<>(headers));
    }

    /**
     * 设置headers中一个项
     *
     * @param name  名称
     * @param value 值
     */
    public void header(String name, Object value) {
        this.page.getHeaders().put(name, value);
    }


    /**
     * 设置user agent
     *
     * @param ua user agent
     */
    public void userAgent(String ua) {
        this.page.getHeaders().put("user-agent", ua);
    }

    /***
     * 设置proxies参数
     * @param http http代理地址
     * @param https https代理地址
     */

    public void proxies(String http, String https) {
        OkHttpClient.Builder builder = this.page.session().newBuilder();
        if (http != null) {
            int i = http.lastIndexOf(":");
            String hp;
            int port = 80;
            if (i != -1) {
                hp = http.substring(0, i);
                try {
                    port = Integer.parseInt(http.substring(i + 1));
                } catch (NumberFormatException e) {
                    hp = http;
                }
            } else {
                hp = http;
            }
            Proxy httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hp, port));
            builder.setProxy$okhttp(httpProxy);
        }
        if (https != null) {
            int i = https.lastIndexOf(":");
            String hp;
            int port = 80;
            if (i != -1) {
                hp = https.substring(0, i);
                try {
                    port = Integer.parseInt(https.substring(i + 1));
                } catch (NumberFormatException e) {
                    hp = https;
                }
            } else {
                hp = https;
            }
            Proxy httpProxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(hp, port));
            builder.setProxy$okhttp(httpProxy);
        }
        this.page.setSession(builder.build());
    }

    /**
     * 设置认证元组或对象
     *
     * @param authenticator 认证
     */
    public void auth(Authenticator authenticator) {
        OkHttpClient.Builder builder = this.page.session().newBuilder();
        if (authenticator != null) {
            builder.setAuthenticator$okhttp(authenticator);
        }
        this.page.setSession(builder.build());
    }

    /**
     * 设置是否验证SSL证书
     *
     * @param hostnameVerifier 验证 SSL 证书
     */
    public void verify(HostnameVerifier hostnameVerifier) {
        OkHttpClient.Builder builder = this.page.session().newBuilder();
        builder.setHostnameVerifier$okhttp(hostnameVerifier);
        this.page.setSession(builder.build());
    }


}
