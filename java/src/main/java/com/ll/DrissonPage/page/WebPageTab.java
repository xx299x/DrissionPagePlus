package com.ll.DrissonPage.page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.base.BasePage;
import com.ll.DrissonPage.base.By;
import com.ll.DrissonPage.base.DrissionElement;
import com.ll.DrissonPage.config.SessionOptions;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.element.SessionElement;
import com.ll.DrissonPage.functions.Web;
import com.ll.DrissonPage.units.setter.WebPageTabSetter;
import lombok.Getter;
import okhttp3.Cookie;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class WebPageTab extends BasePage<DrissionElement<?, ?>> {
    private WebMode mode;
    @Getter
    private WebPage page;
    @Getter
    private boolean hasDriver;
    @Getter
    private boolean hasSession;
    private final SessionPage sessionPage;
    private final ChromiumTab chromiumTab;
    private WebPageTabSetter setter;

    public WebPageTab(WebPage page, String tabId) {
        this.mode = WebMode.d;
        this.hasDriver = true;
        this.hasSession = true;
        sessionPage = new SessionPage(new SessionOptions(false, null).fromSession(page.session(), page.sessionPage.headers));
        chromiumTab = new ChromiumTab(page.chromiumPage, tabId);
    }

    /**
     * @return 返回用于设置的对象
     */
    public WebPageTabSetter set() {
        if (setter == null) setter = new WebPageTabSetter(this);
        return this.setter;
    }

    @Override
    public String url() {
        switch (mode) {
            case d:
                return this.browserUrl();
            case s:
                return this.sessionUrl();
            default:
                return null;
        }
    }

    /**
     * @return 返回浏览器当前url
     */
    protected String browserUrl() {
        return this.chromiumTab.url();
    }

    /**
     * @return 返回当前页面title
     */
    public String title() {
        switch (mode) {
            case d:
                return chromiumTab.title();
            case s:
                return sessionPage.title();
            default:
                return null;
        }
    }

    /**
     * @return 返回页码原始数据数据
     */
    public Object rawData() {
        switch (mode) {
            case d:
                if (this.hasDriver) {
                    return chromiumTab.html();
                } else {
                    return "";
                }
            case s:
                return sessionPage.rawData();
            default:
                return null;
        }
    }

    @Override
    public String html() {
        switch (mode) {
            case d:
                if (this.hasDriver) {
                    return chromiumTab.html();
                } else {
                    return "";
                }
            case s:
                return sessionPage.html();
            default:
                return null;
        }
    }

    @Override
    public JSONObject json() {
        switch (mode) {
            case d:
                return chromiumTab.json();
            case s:
                return sessionPage.json();
            default:
                return null;
        }
    }

    /**
     * @return 返回 s 模式获取到的 Response 对象
     */
    public Response response() {
        return sessionPage.response();
    }

    /**
     * @return 返回当前模式，'s'或'd'
     */
    public WebMode mode() {
        return this.mode;
    }

    /**
     * @return 返回user agent
     */
    public String ua() {
        return userAgent();

    }

    /**
     * @return 返回user agent
     */
    @Override
    public String userAgent() {
        switch (mode) {
            case d:
                return chromiumTab.userAgent();
            case s:
                return sessionPage.userAgent();
            default:
                return null;
        }
    }

    /**
     * @return 返回Session对象，如未初始化则按配置信息创建
     */
    public OkHttpClient session() {
        if (sessionPage.session == null) this.sessionPage.createSession();
        return sessionPage.session;
    }

    /**
     * @return 返回 session 保存的url
     */
    private String sessionUrl() {
        try (Response response = this.sessionPage.response()) {
            if (response == null) return null;
            return response.request().url().toString();
        }
    }

    /**
     * @return 返回通用timeout设置
     */
    public Double timeout() {
        return chromiumTab.timeouts.getBase();
    }

    /**
     * 设置通用超时时间
     *
     * @param second 秒
     */
    public void setTimeout(Double second) {
        this.set().timeouts(second, null, null);
    }


    @Override
    public Boolean get(String url, boolean showErrMsg, Integer retry, Double interval, Double timeout, Map<String, Object> params) {
        switch (mode) {
            case d:
                return chromiumTab.get(url, showErrMsg, retry, interval, timeout, params);
            case s:
                timeout = timeout == null ? this.hasDriver ? chromiumTab.timeouts.getPageLoad() : this.timeout() : timeout;
                return sessionPage.get(url, showErrMsg, retry, interval, timeout, params);
            default:
                return null;
        }
    }

    /**
     * 用post方式跳转到url 会切换到s模式
     *
     * @param url 目标url
     * @return s模式时返回url是否可用，d模式时返回获取到的Response对象
     */

    public Object post(@NotNull String url) {
        return this.post(url, null);

    }

    /**
     * 用post方式跳转到url 会切换到s模式
     *
     * @param url    目标url
     * @param params 连接参数
     * @return s模式时返回url是否可用，d模式时返回获取到的Response对象
     */

    public Object post(@NotNull String url, Map<String, Object> params) {
        return this.post(url, false, params);

    }

    /**
     * 用post方式跳转到url 会切换到s模式
     *
     * @param url        目标url
     * @param showErrMsg 是否显示和抛出异常
     * @param params     连接参数
     * @return s模式时返回url是否可用，d模式时返回获取到的Response对象
     */

    public Object post(@NotNull String url, boolean showErrMsg, Map<String, Object> params) {
        return this.post(url, showErrMsg, null, params);

    }

    /**
     * 用post方式跳转到url 会切换到s模式
     *
     * @param url        目标url
     * @param showErrMsg 是否显示和抛出异常
     * @param retry      重试次数，为None时使用页面对象retry_times属性值
     * @param params     连接参数
     * @return s模式时返回url是否可用，d模式时返回获取到的Response对象
     */
    public Object post(@NotNull String url, boolean showErrMsg, Integer retry, Map<String, Object> params) {
        return this.post(url, showErrMsg, retry, null, params);
    }

    /**
     * 用post方式跳转到url 会切换到s模式
     *
     * @param url        目标url
     * @param showErrMsg 是否显示和抛出异常
     * @param retry      重试次数，为None时使用页面对象retry_times属性值
     * @param interval   重试间隔（秒），为None时使用页面对象retry_interval属性值
     * @param params     连接参数
     * @return s模式时返回url是否可用，d模式时返回获取到的Response对象
     */

    public Object post(@NotNull String url, boolean showErrMsg, Integer retry, Double interval, Map<String, Object> params) {
        if (Objects.equals(mode, WebMode.d)) {
            this.cookiesToSession();
            sessionPage.post(url, showErrMsg, retry, interval, params);
            return this.response();
        } else {
            return sessionPage.post(url, showErrMsg, retry, interval, params);
        }
    }

    /**
     * 返回第一个符合条件的元素、属性或节点文本
     *
     * @param by      元素的定位信息，可以是元素对象，by，或查询字符串
     * @param index   获取第几个，从1开始，可传入负数获取倒数第几个
     * @param timeout 查找元素超时时间（秒），默认与页面等待时间一致
     * @return 元素对象
     */
    public DrissionElement<?, ?> ele(By by, int index, Double timeout) {
        switch (mode) {
            case d:
                return chromiumTab.ele(by, index, timeout);
            case s:
                return sessionPage.ele(by, index, timeout);
            default:
                return null;
        }
    }

    /**
     * 返回第一个符合条件的元素、属性或节点文本
     *
     * @param locator 元素的定位信息，可以是元素对象，by，或查询字符串
     * @param index   获取第几个，从1开始，可传入负数获取倒数第几个
     * @param timeout 查找元素超时时间（秒），默认与页面等待时间一致
     * @return 元素对象
     */
    public DrissionElement<?, ?> ele(String locator, int index, Double timeout) {
        switch (mode) {
            case d:
                return chromiumTab.ele(locator, index, timeout);
            case s:
                return sessionPage.ele(locator, index, timeout);
            default:
                return null;
        }
    }

    /**
     * 返回页面中所有符合条件的元素
     *
     * @param locator 元素的定位信息，可以是by，或查询字符串
     * @param timeout 查找元素超时时间（秒），默认与页面等待时间一致
     * @return 元素对象的列表
     */
    public List<DrissionElement<?, ?>> eles(String locator, Double timeout) {
        switch (mode) {
            case d:
                List<ChromiumElement> chromiumElements = chromiumTab.eles(locator, timeout);
                return chromiumElements != null ? new ArrayList<>(chromiumElements) : null;
            case s:
                List<SessionElement> sessionElements = sessionPage.eles(locator, timeout);
                return sessionElements != null ? new ArrayList<>(sessionElements) : null;
            default:
                return null;
        }
    }

    /**
     * 返回页面中所有符合条件的元素
     *
     * @param by      元素的定位信息，可以是by，或查询字符串
     * @param timeout 查找元素超时时间（秒），默认与页面等待时间一致
     * @return 元素对象的列表
     */
    public List<DrissionElement<?, ?>> eles(By by, Double timeout) {
        switch (mode) {
            case d:
                List<ChromiumElement> chromiumElements = chromiumTab.eles(by, timeout);
                return chromiumElements != null ? new ArrayList<>(chromiumElements) : null;
            case s:
                List<SessionElement> sessionElements = sessionPage.eles(by, timeout);
                return sessionElements != null ? new ArrayList<>(sessionElements) : null;
            default:
                return null;
        }
    }

    /**
     * 查找第一个符合条件的元素以SessionElement形式返回，d模式处理复杂页面时效率很高
     *
     * @param by    查询元素
     * @param index 获取第几个，从1开始，可传入负数获取倒数第几个
     * @return SessionElement对象
     */
    @Override
    public SessionElement sEle(By by, Integer index) {
        switch (mode) {
            case d:
                return chromiumTab.sEle(by, index);
            case s:
                return sessionPage.sEle(by, index);
            default:
                return null;
        }
    }

    /**
     * 查找第一个符合条件的元素以SessionElement形式返回，d模式处理复杂页面时效率很高
     *
     * @param loc   查询元素
     * @param index 获取第几个，从1开始，可传入负数获取倒数第几个
     * @return SessionElement对象
     */
    @Override
    public SessionElement sEle(String loc, Integer index) {
        switch (mode) {
            case d:
                return chromiumTab.sEle(loc, index);
            case s:
                return sessionPage.sEle(loc, index);
            default:
                return null;
        }
    }

    /**
     * 查找所有符合条件的元素以SessionElement形式返回，d模式处理复杂页面时效率很高
     *
     * @param by 元素的定位信息 查询元素
     * @return SessionElement对象集合
     */
    @Override
    public List<SessionElement> sEles(By by) {
        switch (mode) {
            case d:
                return chromiumTab.sEles(by);
            case s:
                return sessionPage.sEles(by);
            default:
                return null;
        }
    }

    /**
     * 查找所有符合条件的元素以SessionElement形式返回，d模式处理复杂页面时效率很高
     *
     * @param loc 元素的定位信息 查询元素
     * @return SessionElement对象集合
     */
    @Override
    public List<SessionElement> sEles(String loc) {
        switch (mode) {
            case d:
                return chromiumTab.sEles(loc);
            case s:
                return sessionPage.sEles(loc);
            default:
                return null;
        }
    }

    /**
     * 切换模式，接收's'或'd'，除此以外的字符串会切换为 d 模式
     * 如copy_cookies为True，切换时会把当前模式的cookies复制到目标模式
     * 切换后，如果go是True，调用相应的get函数使访问的页面同步
     *
     * @param mode 模式
     */
    public void changeMode(WebMode mode) {
        changeMode(mode, true);
    }

    /**
     * 切换模式，接收's'或'd'，除此以外的字符串会切换为 d 模式
     * 如copy_cookies为True，切换时会把当前模式的cookies复制到目标模式
     * 切换后，如果go是True，调用相应的get函数使访问的页面同步
     *
     * @param mode 模式
     * @param go   是否跳转到原模式的url
     */
    public void changeMode(WebMode mode, boolean go) {
        changeMode(mode, go, true);
    }

    /**
     * 切换模式，接收's'或'd'，除此以外的字符串会切换为 d 模式
     * 如copy_cookies为True，切换时会把当前模式的cookies复制到目标模式
     * 切换后，如果go是True，调用相应的get函数使访问的页面同步
     *
     * @param mode        模式
     * @param go          是否跳转到原模式的url
     * @param copyCookies 是否复制cookies到目标模式
     */
    public void changeMode(WebMode mode, boolean go, boolean copyCookies) {
        if (mode != null && Objects.equals(mode.mode, this.mode.mode)) return;
        this.mode = mode;
        //s模式转d模式
        if (this.mode.equals(WebMode.d)) {
            if (this.chromiumTab.driver == null) {
                this.chromiumTab.connectBrowser(null);
                this.url = this.hasDriver ? null : sessionPage.url();
                this.hasDriver = true;
            }
            String s = this.sessionUrl();
            if (s != null) {
                if (copyCookies) this.cookiesToBrowser();
                if (go) this.get(this.sessionUrl());
            }
            //d模式转s模式
        } else if (this.mode.equals(WebMode.s)) {
            this.hasSession = true;
            this.url = this.sessionUrl();
            if (this.hasDriver) {
                if (copyCookies) this.cookiesToSession();
                if (go && !this.get(sessionPage.url())) {
                    throw new IllegalArgumentException("s模式访问失败，请设置go=False，自行构造连接参数进行访问。");
                }
            }
        }
    }

    /**
     * 把driver对象的cookies复制到session对象
     */
    public void cookiesToSession() {
        cookiesToSession(true);
    }


    /**
     * 把driver对象的cookies复制到session对象
     *
     * @param copyUserAgent 是否复制ua信息
     */
    public void cookiesToSession(boolean copyUserAgent) {
        if (!this.hasSession) return;
        if (copyUserAgent) {
            String o = JSON.parseObject(this.chromiumTab.runCdp("Runtime.evaluate", Map.of("expression", "navigator.userAgent;")).toString()).getJSONObject("result").getString("value");
            this.sessionPage.headers.put("User-Agent", o);
        }
        Web.setBrowserCookies(this.chromiumTab.page(), chromiumTab.cookies());
    }

    /**
     * 把session对象的cookies复制到浏览器
     */
    public void cookiesToBrowser() {
        if (!this.hasDriver) return;
        Web.setBrowserCookies(this.chromiumTab.page(), sessionPage.cookies());

    }

    @Override
    public List<Cookie> cookies(boolean asMap, boolean allDomains, boolean allInfo) {
        switch (mode) {
            case d:
                return chromiumTab.cookies(asMap, allDomains, allInfo);
            case s:
                return sessionPage.cookies(asMap, allDomains, allInfo);
            default:
                return new ArrayList<>();
        }
    }

    /**
     * 关闭当前标签页
     */

    public void close() {
        this.chromiumTab.close();
        if (this.sessionPage.session != null) {
            Response response = this.sessionPage.response;
            if (response != null) try {
                response.close();
            } catch (Exception ignored) {
            }
        }

    }

    /**
     * 返回页面中符合条件的元素、属性或节点文本，默认返回第一个
     *
     * @param by       查询元素
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从1开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @return 元素对象
     */
    @Override
    protected List<DrissionElement<?, ?>> findElements(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        switch (mode) {
            case d:
                List<ChromiumElement> chromiumElements = chromiumTab.findElements(by, timeout, index, relative, raiseErr);
                return chromiumElements != null ? new ArrayList<>(chromiumElements) : null;
            case s:
                List<SessionElement> elements = sessionPage.findElements(by, timeout, index, relative, raiseErr);
                return elements != null ? new ArrayList<>(elements) : null;
            default:
                return null;
        }
    }

    /**
     * 返回页面中符合条件的元素、属性或节点文本，默认返回第一个
     *
     * @param loc      查询元素
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从1开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @return 元素对象
     */
    @Override
    protected List<DrissionElement<?, ?>> findElements(String loc, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        switch (mode) {
            case d:
                List<ChromiumElement> chromiumElements = chromiumTab.findElements(loc, timeout, index, relative, raiseErr);
                return chromiumElements != null ? new ArrayList<>(chromiumElements) : null;
            case s:
                List<SessionElement> elements = sessionPage.findElements(loc, timeout, index, relative, raiseErr);
                return elements != null ? new ArrayList<>(elements) : null;
            default:
                return null;
        }
    }

    /**
     * 克隆
     *
     * @param cloneNumber 克隆数量
     * @return 集合
     */
    public List<WebPageTab> copy(int cloneNumber) {
        return IntStream.range(0, cloneNumber < 0 ? 1 : cloneNumber).mapToObj(i -> copy()).collect(Collectors.toList());
    }

    /**
     * 克隆
     *
     * @return 单个
     */
    public WebPageTab copy() {
        return this.page.newTab(this.url());
    }
}
