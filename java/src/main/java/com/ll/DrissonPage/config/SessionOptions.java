package com.ll.DrissonPage.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ll.DrissonPage.units.HttpClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.ini4j.Profile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * requests的Session对象配置类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
@Getter
@Setter
public class SessionOptions {
    private String iniPath;
    /**
     * 返回默认下载路径属性信息
     */
    private String downloadPath;
    /**
     * 返回timeout属性信息
     */
    private Double timeout = 10.0;
    /**
     * 记录要从ini文件删除的参数
     */
    private Set<String> delSet = new HashSet<>();
    /**
     * 返回headers设置信息
     */
    private Map<String, String> headers;
    /**
     * 以list形式返回cookies
     */
    private List<Cookie> cookies;
    /**
     * 返回认证设置信息
     */
    private List<Object> auth;
    /**
     * 返回proxies设置信息
     */
    private Map<String, String> proxies;
    /**
     * 返回回调方法
     */
    private Map<String, Object> hooks;
    /**
     * 返回连接参数设置信息
     */
    private Map<String, String> params;
    /**
     * 返回是否验证SSL证书设置
     */
    private Boolean verify;
    /**
     * 返回SSL证书设置信息
     */
    private String cert;
    /**
     * 返回适配器设置信息
     */
    private List<String> adapters;
    /**
     * 返回是否使用流式响应内容设置信息
     */
    private Boolean stream;
    /**
     * 返回是否信任环境设置信息
     */
    private Boolean trustEnv;
    /**
     * 返回最大重定向次数
     */
    private Integer maxRedirects;
    /**
     * 返回连接失败时的重试次数
     */
    private int retryTimes = 3;
    /**
     * 返回连接失败时的重试间隔（秒）
     */
    private int retryInterval = 2;

    public SessionOptions(boolean readFile, String iniPath) {
        headers = new CaseInsensitiveMap<>();
        auth = new ArrayList<>();
        cookies = new ArrayList<>();
        proxies = new HashMap<>();
        hooks = new HashMap<>();
        params = new HashMap<>();
        if (!readFile) {
            return;
        }

        iniPath = iniPath != null ? iniPath : "";
        OptionsManager om = new OptionsManager(iniPath);
        this.iniPath = om.getIniPath();

        Profile.Section options = om.getIni().get("session_options");
        if (options.get("headers") != null) {
            setHeaders(JSON.parseObject(options.get("headers"), new TypeReference<>() {
            }));
        }

        if (options.containsKey("cookies")) {
            setCookies(JSON.parseObject(options.get("cookies"), new TypeReference<>() {
            }));
        }

        if (options.containsKey("auth")) {
            this.auth = JSON.parseArray(options.get("auth"));
        }

        if (options.containsKey("params")) {
            this.params = JSON.parseObject(options.get("params"), new TypeReference<>() {
            });
        }

        if (options.containsKey("verify")) {
            this.verify = Boolean.parseBoolean(options.get("verify"));
        }

        if (options.containsKey("cert")) {
            this.cert = options.get("cert");
        }

        if (options.containsKey("stream")) {
            this.stream = Boolean.parseBoolean(options.get("stream"));
        }

        if (options.containsKey("trust_env")) {
            this.trustEnv = Boolean.parseBoolean(options.get("trust_env"));
        }

        if (options.containsKey("max_redirects")) {
            this.maxRedirects = Integer.parseInt("max_redirects");
        }

        setProxies(om.getIni().get("proxies", "http"), om.getIni().get("proxies", "https"));
        String s = om.getIni().get("timeouts", "base");
        if (s != null) this.timeout = Double.parseDouble(om.getIni().get("timeouts", "base"));
        this.downloadPath = om.getIni().get("paths", "download_path");
        Profile.Section others = om.getIni().get("others");
        s = others.get("retry_times");
        this.retryTimes = s != null ? Integer.parseInt(s) : 3;
        s = others.get("retry_interval");
        this.retryInterval = s != null ? Integer.parseInt(s) : 2;
    }

    public static Map<String, Object> sessionOptionsToMap(Map<String, Object> options) {
        if (options == null) return new SessionOptions(false, null).asMap();
        if (!options.isEmpty()) return options;
        String[] attrs = {"headers", "cookies", "proxies", "params", "verify", "stream", "trustEnv", "cert", "maxRedirects", "timeout", "downloadPath"};
        options = new HashMap<>();
        for (String attr : attrs) {
            Object val;
            try {
                val = options.getClass().getField(attr).get(options);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            if (val != null) options.put(attr, val);
        }
        return options;
    }

    public void setProxies(String http, String https) {
        this.proxies.put("http", http);
        this.proxies.put("https", https);
    }

    /**
     * 设置连接失败时的重试操作
     *
     * @param times    重试次数
     * @param interval 重试间隔
     * @return 当前对象
     */
    public SessionOptions setRetry(Integer times, Integer interval) {
        if (times != null) this.retryTimes = times;
        if (interval != null) this.retryInterval = interval;
        return this;
    }

    /**
     * 设置headers参数
     *
     * @param headers 参数值，传入null可在ini文件标记删除
     * @return 返回当前对象
     */
    public SessionOptions setHeaders(Map<String, String> headers) {
        if (headers == null) {
            this.headers = null;
            this.delSet.add("headers");
        } else {
            this.headers = new CaseInsensitiveMap<>(headers.size());
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                this.headers.put(entry.getKey().toLowerCase(), entry.getValue());
            }
        }
        return this;
    }

    /**
     * 设置headers中一个项
     *
     * @param attr  设置名称
     * @param value 设置值
     * @return 返回当前对象
     */
    public SessionOptions setHeader(String attr, String value) {
        if (this.headers == null) this.headers = new CaseInsensitiveMap<>();
        this.headers.put(attr.toLowerCase(), value);
        return this;
    }

    /**
     * 从headers中删除一个设置
     *
     * @param attr 要删除的设置
     * @return 返回当前对象
     */
    public SessionOptions removeHeader(String attr) {
        if (this.headers != null) {
            this.headers.remove(attr);
        }
        return this;
    }

    public List<String> adapters() {
        if (this.adapters == null) this.adapters = new ArrayList<>();
        return this.adapters;
    }

    /**
     * 给属性赋值或标记删除
     *
     * @param arg 属性名称
     * @param val 参数值
     */
    private void sets(String arg, Object val) {
        try {
            Field field = this.getClass().getDeclaredField(arg);
            field.setAccessible(true);

            if (val == null) {
                field.set(this, null);
                delSet.add(arg);
            } else {
                field.set(this, val);
                delSet.remove(arg);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(); // Handle the exception according to your needs
        }
    }


    public String save(String path) throws URISyntaxException, IOException {
        if ("default".equals(path)) {
            path = Path.of(Objects.requireNonNull(getClass().getResource("configs.ini")).toURI()).toAbsolutePath().toString();
        } else if (path == null) {
            path = iniPath != null ? Path.of(iniPath).toAbsolutePath().toString() : Path.of(Objects.requireNonNull(getClass().getResource("configs.ini")).toURI()).toAbsolutePath().toString();
        } else {
            path = Path.of(path).toAbsolutePath().toString();
        }

        Path filePath = path.endsWith("config.ini") ? Path.of(path) : Path.of(path, "config.ini");

        OptionsManager om = filePath.toFile().exists() ? new OptionsManager(filePath.toString()) : new OptionsManager(iniPath != null ? iniPath : getClass().getResource("configs.ini").toURI().toString());

        Map<String, Object> options = sessionOptionsToMap(JSON.parseObject(JSON.toJSONString(this)));

        for (Map.Entry<String, Object> entry : options.entrySet()) {
            String i = entry.getKey();
            if (!List.of("downloadPath", "timeout", "proxies").contains(i)) {
                om.setItem("sessionOptions", i, entry.getValue());
            }
        }

        om.setItem("paths", "downloadPath", downloadPath != null ? downloadPath : "");
        om.setItem("timeouts", "base", timeout);
        om.setItem("proxies", "http", proxies.get("http") != null ? proxies.get("http") : "");
        om.setItem("proxies", "https", proxies.get("https") != null ? proxies.get("https") : "");
        om.setItem("others", "retryTimes", retryTimes);
        om.setItem("others", "retryInterval", retryInterval);

        for (String i : delSet) {
            if ("downloadPath".equals(i)) {
                om.setItem("paths", "downloadPath", "");
            } else if ("proxies".equals(i)) {
                om.setItem("proxies", "http", "");
                om.setItem("proxies", "https", "");
            } else {
                om.removeItem("sessionOptions", i);
            }
        }

        om.save(filePath.toString());

        return filePath.toString();
    }

    public String saveToDefault() throws URISyntaxException, IOException {
        return save("default");
    }

    public Map<String, Object> asMap() {
        return sessionOptionsToMap(JSON.parseObject(JSON.toJSONString(this)));
    }

    public HttpClient makeSession() {
        List<Header> headers = new ArrayList<>();
        this.headers.forEach((a, b) -> headers.add(new BasicHeader(a, b)));
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder().readTimeout(120, TimeUnit.SECONDS);

        builder.addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
                Request request = chain.request();
                Request.Builder builder1 = request.newBuilder();
                if (!headers.isEmpty()) headers.forEach((a) -> builder1.addHeader(a.getName(), a.getValue()));
                return chain.proceed(request);
            }
        });
        //设置缓存
        if (!this.cookies.isEmpty()) {
            builder.setCookieJar$okhttp(new CookieJar() {
                @Override
                public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                    list.addAll(cookies);
                }

                @NotNull
                @Override
                public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                    return new ArrayList<>();
                }
            });
        }
        //设置代理
        if (!this.proxies.isEmpty()) {
            String https = this.proxies.get("https");
            String http = this.proxies.get("http");
            if (!https.isEmpty()) {
                String[] split = https.split(":");
                builder.setProxy$okhttp(split.length == 2 ? new Proxy(Proxy.Type.HTTP, new InetSocketAddress(split[0], Integer.parseInt(split[1]))) : new Proxy(Proxy.Type.HTTP, new InetSocketAddress(https, 80)));
            }
            if (!http.isEmpty()) {
                String[] split = http.split(":");
                builder.setProxy$okhttp(split.length == 2 ? new Proxy(Proxy.Type.HTTP, new InetSocketAddress(split[0], Integer.parseInt(split[1]))) : new Proxy(Proxy.Type.HTTP, new InetSocketAddress(http, 80)));
            }
        }


        if (this.verify != null) {
            builder.setHostnameVerifier$okhttp((s, sslSession) -> this.verify);
        }
        if (this.maxRedirects != null) {
            builder.setConnectionPool$okhttp(new ConnectionPool(this.maxRedirects, 5, TimeUnit.MINUTES));
        }
        return new HttpClient(builder.build(), headers);
    }

    /**
     * 从Session对象中读取配置
     *
     * @param session Session对象
     * @param headers headers
     * @return 当前对象
     */
    public SessionOptions fromSession(OkHttpClient session, Map<String, String> headers) {
        headers = headers == null ? new CaseInsensitiveMap<>() : new CaseInsensitiveMap<>(headers);

        Map<String, String> finalHeaders = headers;
        OkHttpClient.Builder builder = session.newBuilder();
        builder.addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
                Request request = chain.request();
                Headers headers1 = request.headers();
                for (String name : headers1.names()) {
                    finalHeaders.put(name, headers1.get(name));
                }
                return chain.proceed(request);
            }
        });
        this.headers = headers;
        builder.setCookieJar$okhttp(new CookieJar() {
            @Override
            public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                cookies = list;
            }

            @NotNull
            @Override
            public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                return new ArrayList<>();
            }
        });
        Proxy proxy$okhttp = builder.getProxy$okhttp();
        if (proxy$okhttp != null) {
            this.proxies = new HashMap<>();
            this.proxies.put(proxy$okhttp.type().toString(), proxy$okhttp.address().toString());
        }
        this.maxRedirects = builder.getConnectionPool$okhttp().connectionCount();

        return this;
    }

    public SessionOptions copy() {
        return JSON.parseObject(JSON.toJSONString(this), SessionOptions.class);
    }

    @AllArgsConstructor
    public static class Adapter {
        private String url;
    }
}
