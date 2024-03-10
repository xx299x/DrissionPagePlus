package com.ll.DrissonPage.page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.base.BasePage;
import com.ll.DrissonPage.base.BeforeConnect;
import com.ll.DrissonPage.base.By;
import com.ll.DrissonPage.config.SessionOptions;
import com.ll.DrissonPage.element.SessionElement;
import com.ll.DrissonPage.units.HttpClient;
import com.ll.DrissonPage.units.setter.SessionPageSetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * SessionPage封装了页面操作的常用功能，使用requests来获取、解析网页
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class SessionPage extends BasePage<SessionElement> {
    @Getter
    @Setter
    protected Map<String, String> headers;
    @Setter
    protected OkHttpClient session;
    protected SessionOptions sessionOptions;
    protected Response response;
    private double timeout;
    private int retryTimes;
    private float retryInterval;
    private SessionPageSetter set;
    @Setter
    private Charset encoding;

    /**
     * @param request 请求工厂
     */
    public SessionPage(OkHttpClient request) {
        this(request, null);
    }

    /**
     * @param request 请求工厂
     * @param timeout 连接超时时间
     */
    public SessionPage(OkHttpClient request, Double timeout) {
        this(request, timeout, false);
    }

    public SessionPage() {
        this(new SessionOptions(true, null));
    }

    /**
     * @param option 配置
     */
    public SessionPage(SessionOptions option) {
        this(option, null);
    }

    /**
     * @param option  配置
     * @param timeout 连接超时时间
     */
    public SessionPage(SessionOptions option, Double timeout) {
        this(option, timeout, false);
    }

    private SessionPage(Object requestOrOption, Double timeout, boolean ignoredFlag) {
        this.setType("SessionPage");
        this.sSetStartOptions(requestOrOption);
        this.sSetRunTimeSettings(timeout);
        this.createSession();
        if (timeout != null) this.timeout = timeout;
        this.headers = new CaseInsensitiveMap<>();
        ;
    }

    /**
     * 启动配置
     */
    private void sSetStartOptions(Object sessionOrOptions) {
        if (sessionOrOptions == null || sessionOrOptions instanceof SessionOptions) {
            this.sessionOptions = sessionOrOptions == null ? new SessionOptions(true, null) : (SessionOptions) sessionOrOptions;
        } else if (sessionOrOptions instanceof OkHttpClient) {
            this.sessionOptions = new SessionOptions(true, null);
            this.headers = new CaseInsensitiveMap<>(this.sessionOptions.getHeaders());
            this.sessionOptions.setHeaders(null);
            this.session = (OkHttpClient) sessionOrOptions;
        }
    }

    /**
     * 设置运行时用到的属性
     */
    private void sSetRunTimeSettings(Double timeout) {
        this.timeout = timeout == null || timeout <= 0 ? this.sessionOptions.getTimeout() : timeout;
        this.setDownloadPath(this.sessionOptions.getDownloadPath() == null ? null : Paths.get(this.sessionOptions.getDownloadPath()).toAbsolutePath().toString());
        this.retryTimes = this.sessionOptions.getRetryTimes();
        this.retryInterval = this.sessionOptions.getRetryInterval();
    }

    /**
     * 创建内建Session对象
     */
    protected void createSession() {
        if (this.session == null) {
            HttpClient httpClient = this.sessionOptions.makeSession();
            this.session = httpClient.getClient();
            this.headers = new CaseInsensitiveMap<>(this.sessionOptions.getHeaders());
        }
    }

    //-----------------共有属性和方法-------------------
    @Override
    public String title() {
        List<SessionElement> sessionPages = this._ele("xpath://title", null, null, null, false, null);
        if (!sessionPages.isEmpty()) return sessionPages.get(0).text();
        return null;
    }

    /**
     * @return 返回当前访问url
     */
    @Override
    public String url() {
        return this.url;
    }

    /**
     * @return 返回页面原始数据
     */
    public byte[] rawData() {
        ResponseBody body = this.response.body();
        if (body != null) {
            try {
                return body.bytes();
            } catch (IOException e) {
                return new byte[0];
            }
        }
        return new byte[0];
    }

    @Override
    public String html() {
        if (this.response == null) return "";
        ResponseBody body = this.response.body();
        if (body != null) {
            try {
                return body.string();
            } catch (IOException e) {
                return "";
            }
        }
        return "";
    }

    /**
     * @return 当返回内容是json格式则返回JSONObject，非json格式时返回None
     */

    @Override
    public JSONObject json() {
        if (this.response == null) return null;
        ResponseBody body = this.response.body();
        if (body != null) {
            try {
                return JSON.parseObject(body.string());
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * @return 返回user agent
     */
    @Override
    public String userAgent() {
        String ua = "";
        for (Map.Entry<String, String> entry : this.headers.entrySet()) {
            String k = entry.getKey();
            Object v = entry.getValue();
            if (Objects.equals(k.toUpperCase(Locale.ROOT), "user-agent")) {
                ua = v.toString();
                break;
            } else if ("useragent".equalsIgnoreCase(k.toUpperCase(Locale.ROOT))) {
                ua = v.toString();
                break;
            }
        }

        return ua;
    }

    public OkHttpClient session() {
        return this.session;
    }

    /**
     * @return 返回访问url得到的Response对象
     */
    public Response response() {
        return this.response;
    }

    /**
     * @return 返回设置的编码
     */
    public String encoding() {
        return this.encoding.name();
    }

    /**
     * @return 返回用于设置的对象
     */
    public SessionPageSetter set() {
        if (this.set == null) this.set = new SessionPageSetter(this);
        return this.set;
    }

    /**
     * 用get方式跳转到url，可输入文件路径
     *
     * @param url 目标url，可指定本地文件路径
     * @return url是否可用
     */
    public Boolean get(Path url) {
        return get(url, false);
    }

    /**
     * 用get方式跳转到url，可输入文件路径
     *
     * @param url        目标url，可指定本地文件路径
     * @param showErrMsg 是否显示和抛出异常
     * @return url是否可用
     */
    public Boolean get(Path url, boolean showErrMsg) {
        return get(url, showErrMsg, null, null, null);
    }

    /**
     * 用get方式跳转到url，可输入文件路径
     *
     * @param url        目标url，可指定本地文件路径
     * @param showErrMsg 是否显示和抛出异常
     * @param retry      重试次数，为None时使用页面对象retry_times属性值
     * @param interval   重试间隔（秒），为None时使用页面对象retry_interval属性值
     * @param timeout    连接超时时间（秒），为None时使用页面对象timeout属性值
     * @return url是否可用
     */
    public Boolean get(Path url, boolean showErrMsg, Integer retry, Double interval, Double timeout) {
        return get(url, showErrMsg, retry, interval, timeout, null);
    }

    /**
     * 用get方式跳转到url，可输入文件路径
     *
     * @param url        目标url，可指定本地文件路径
     * @param showErrMsg 是否显示和抛出异常
     * @param retry      重试次数，为None时使用页面对象retry_times属性值
     * @param interval   重试间隔（秒），为None时使用页面对象retry_interval属性值
     * @param timeout    连接超时时间（秒），为None时使用页面对象timeout属性值
     * @param params     连接参数
     * @return url是否可用
     */

    public Boolean get(@NotNull Path url, boolean showErrMsg, Integer retry, Double interval, Double timeout, Map<String, Object> params) {
        return get(url.toAbsolutePath().toString(), showErrMsg, retry, interval, timeout, params);
    }

    /**
     * 用get方式跳转到url，可输入文件路径
     *
     * @param url        目标url，可指定本地文件路径
     * @param showErrMsg 是否显示和抛出异常
     * @param retry      重试次数，为None时使用页面对象retry_times属性值
     * @param interval   重试间隔（秒），为None时使用页面对象retry_interval属性值
     * @param timeout    连接超时时间（秒），为None时使用页面对象timeout属性值
     * @param params     连接参数
     * @return url是否可用
     */

    @Override
    public Boolean get(@NotNull String url, boolean showErrMsg, Integer retry, Double interval, Double timeout, Map<String, Object> params) {
        if (!url.toLowerCase().startsWith("http")) {
            if (url.startsWith("file:///")) {
                url = url.substring(8);
            }
            File file = Paths.get(url).toFile();
            if (file.exists()) {
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    String string = Arrays.toString(fileInputStream.readAllBytes());
                    Response.Builder builder = new Response.Builder();
                    builder.setMessage$okhttp("get");
                    builder.setCode$okhttp(200);
                    builder.setBody$okhttp(new RealResponseBody(string, string.length(), new Buffer()));
                    this.response = builder.build();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        }
        return this.sConnect(url, "get", showErrMsg, retry, interval, params);
    }

    /**
     * 用post方式跳转到url
     *
     * @param url 目标url
     * @return url是否可用
     */

    public Boolean post(@NotNull String url) {
        return this.post(url, null);

    }

    /**
     * 用post方式跳转到url
     *
     * @param url    目标url
     * @param params 连接参数
     * @return url是否可用
     */

    public Boolean post(@NotNull String url, Map<String, Object> params) {
        return this.post(url, false, params);

    }

    /**
     * 用post方式跳转到url
     *
     * @param url        目标url
     * @param showErrMsg 是否显示和抛出异常
     * @param params     连接参数
     * @return url是否可用
     */

    public Boolean post(@NotNull String url, boolean showErrMsg, Map<String, Object> params) {
        return this.post(url, showErrMsg, null, params);

    }

    /**
     * 用post方式跳转到url
     *
     * @param url        目标url
     * @param showErrMsg 是否显示和抛出异常
     * @param retry      重试次数，为None时使用页面对象retry_times属性值
     * @param params     连接参数
     * @return url是否可用
     */

    public Boolean post(@NotNull String url, boolean showErrMsg, Integer retry, Map<String, Object> params) {
        return this.post(url, showErrMsg, retry, null, params);
    }

    /**
     * 用post方式跳转到url
     *
     * @param url        目标url
     * @param showErrMsg 是否显示和抛出异常
     * @param retry      重试次数，为None时使用页面对象retry_times属性值
     * @param interval   重试间隔（秒），为None时使用页面对象retry_interval属性值
     * @param params     连接参数
     * @return url是否可用
     */

    public Boolean post(@NotNull String url, boolean showErrMsg, Integer retry, Double interval, Map<String, Object> params) {
        return this.sConnect(url, "post", showErrMsg, retry, interval, params);
    }

    @Override
    public SessionElement sEle(By by, Integer index) {
        if (by == null) {
            List<SessionElement> sessionElements = SessionElement.makeSessionEle(this.html(), By.NULL(), null);
            if (!sessionElements.isEmpty()) return sessionElements.get(0);
            return null;
        }
        List<SessionElement> sessionElements = this._ele(by, null, index, null, null, "s_ele()");
        if (!sessionElements.isEmpty()) return sessionElements.get(0);
        return null;
    }

    @Override
    public SessionElement sEle(String loc, Integer index) {
        if (loc == null) {
            List<SessionElement> sessionElements = SessionElement.makeSessionEle(this.html(), By.NULL(), null);
            if (!sessionElements.isEmpty()) return sessionElements.get(0);
            return null;
        }
        List<SessionElement> sessionElements = this._ele(loc, null, index, null, null, "s_ele()");
        if (!sessionElements.isEmpty()) return sessionElements.get(0);
        return null;
    }

    @Override
    public List<SessionElement> sEles(By by) {
        return this._ele(by, null, null, null, null, null);
    }

    @Override
    public List<SessionElement> sEles(String loc) {
        return this._ele(loc, null, null, null, null, null);
    }

    @Override
    protected List<SessionElement> findElements(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        return SessionElement.makeSessionEle(this, by, index);
    }

    @Override
    protected List<SessionElement> findElements(String loc, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        return SessionElement.makeSessionEle(this, loc, index);

    }

    @Override
    public List<Cookie> cookies(boolean asMap, boolean allDomains, boolean allInfo) {
        List<Cookie> list;
        {
            final var cookies = new List[]{new ArrayList<>()};
            this.session.newBuilder().setCookieJar$okhttp(new CookieJar() {
                @Override
                public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                    if (url != null) {
                        ArrayList<Cookie> src = new ArrayList<>();
                        Collections.copy(list, src);
                        src.removeIf(cookie -> !cookie.domain().isEmpty() || !cookie.domain().contains(url));
                        cookies[0] = src;
                    } else {
                        cookies[0] = list;
                    }
                }

                @NotNull
                @Override
                public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                    return new ArrayList<>();
                }
            });
            list = new ArrayList(cookies[0]);
        }

        return list;
    }


    public void close() {
        if (this.response != null) try {
            this.response.close();
        } catch (Exception ignored) {

        }
    }

    private boolean sConnect(String url, String mode, boolean showErrMsg, Integer retry, Double interval, Map<String, Object> params) {
        BeforeConnect beforeConnect = this.beforeConnect(url, retry, interval);
        ResponseWrapper responseReturn = this.makResponse(this.url(), mode, beforeConnect.getRetry(), beforeConnect.getInterval(), showErrMsg, params);
        boolean urlAvailable;
        if (responseReturn.getResponse() == null) urlAvailable = false;
        else if (this.response.code() == 200) urlAvailable = true;
        else {
            if (showErrMsg) try {
                throw new ConnectException("状态码：" + this.response.code());
            } catch (ConnectException e) {
                throw new RuntimeException(e);
            }
            urlAvailable = false;

        }
        return urlAvailable;
    }

    public ResponseWrapper makResponse(String url, String mode, Integer retry, Double interval, boolean showErrMsg, Map<String, Object> params) {
        Map<String, Object> headersMap = new CaseInsensitiveMap<>();
        if (params.containsKey("headers"))
            headersMap.putAll(JSON.parseObject(JSON.toJSONString(params.get("headers"))));

        // Set referer and host values
        URI uri = URI.create(url);
        String hostname = uri.getHost();
        String scheme = uri.getScheme();
        if (notCheckHeaders(headersMap, headers, "Referer")) {
            headersMap.put("Referer", this.headers.get("Referer"));
            if (headersMap.get("Referer") == null) {
                headersMap.put("Referer", (this.headers.get("scheme") != null ? this.headers.get("scheme") : scheme) + "://" + hostname);
            }
        }
        if (!headersMap.containsKey("Host")) {
            headersMap.put("Host", hostname);
        }

        // Set timeout
        if (notCheckHeaders(params, headers, "timeout")) {
            params.put("timeout", this.timeout);
        }

        // Merge headers
        headersMap.putAll(this.headers);

        // Build request
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        for (Map.Entry<String, Object> entry : headersMap.entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue().toString());
        }

        Response response = null;
        retry = retry == null ? this.retryTimes : retry;
        interval = interval == null ? this.retryInterval : interval;
        IOException exception = null;
        for (int i = 0; i <= retry; i++) {
            try {
                if (mode.equals("get")) {
                    response = this.session.newCall(requestBuilder.build()).execute();
                } else if (mode.equals("post")) {
                    RequestBody requestBody = RequestBody.create(params.get("data").toString(), MediaType.parse("application/json"));
                    requestBuilder.post(requestBody);
                    response = this.session.newCall(requestBuilder.build()).execute();
                }

                if (response != null && response.body() != null) {
                    MediaType mediaType = response.body().contentType();
                    if (mediaType != null && this.encoding != null) {
                        mediaType.charset(this.encoding);
                        return new ResponseWrapper(response, "Success");

                    }
                    return new ResponseWrapper(setCharset(response), "Success");
                }

            } catch (IOException e) {
                exception = e;
            }
            long millis = (long) (interval * 1000);
            if (i < retry) {
                try {
                    Thread.sleep(millis);
                    if (showErrMsg) {
                        System.out.println("重试 " + url);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (showErrMsg) {
            if (exception != null) {
                throw new RuntimeException(exception);
            } else if (response != null) {
                try {
                    throw new ConnectException("状态码：" + response.code());
                } catch (ConnectException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    throw new ConnectException("连接失败");
                } catch (ConnectException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (response != null) {
                return new ResponseWrapper(response, "状态码：" + response.code());
            } else {
                return new ResponseWrapper(null, "连接失败" + (exception != null ? exception.getMessage() : ""));
            }
        }
    }

    private boolean notCheckHeaders(Map<String, Object> map, Map<String, String> headers, String key) {
        return !map.containsKey(key) && !headers.containsKey(key);
    }

    public static Response setCharset(Response response) {
        // 在headers中获取编码
        String s = response.headers().get("content-type");
        s = s == null ? "" : s;
        String contentType = s.toLowerCase();
        if (!contentType.endsWith(";")) contentType += ";";
        String charset = searchCharset(contentType);

        ResponseBody body = response.body();
        if (charset != null && !charset.isEmpty()) {
            if (body != null) {
                MediaType mediaType = body.contentType();
                if (mediaType != null) {
                    mediaType.charset(Charset.forName(charset));
                }
            }
        } else if (contentType.replace(" ", "").startsWith("text/html")) {
            String content = "";
            try {
                if (body != null) {
                    content = body.string();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            charset = searchCharsetInMeta(content);
            if (charset == null || !charset.isEmpty()) {
                MediaType mediaType = null;
                if (body != null) mediaType = body.contentType();
                if (mediaType != null) charset = mediaType.type();
            }
            Response.Builder builder = new Response.Builder();
            builder.setBody$okhttp(ResponseBody.create(content, MediaType.get(charset == null ? "utf-8" : charset)));
            response.close();
            try (Response r = builder.build()) {
                response = r;
            }
        }

        return response;
    }

    private static String searchCharset(String contentType) {
        Pattern pattern = Pattern.compile("charset[=: ]*(.*?);?");
        Matcher matcher = pattern.matcher(contentType);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "utf-8";
    }

    private static String searchCharsetInMeta(String content) {
        Pattern pattern = Pattern.compile("<meta.*?charset=[ \\\\'\"]*([^\"\\\\' />]+).*?>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "utf-8";
    }

    /**
     * 克隆
     *
     * @param cloneNumber 克隆数量
     * @return 集合
     */
    public List<SessionPage> copy(int cloneNumber) {
        return IntStream.range(0, cloneNumber < 0 ? 1 : cloneNumber).mapToObj(i -> copy()).collect(Collectors.toList());
    }

    /**
     * 克隆
     *
     * @return 单个
     */
    public SessionPage copy() {
        return new SessionPage(this.sessionOptions.copy());
    }

    @Getter
    @AllArgsConstructor
    public static class ResponseWrapper {
        private Response response;
        private String message;
    }
}
