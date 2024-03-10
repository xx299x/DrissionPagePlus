package com.ll.DrissonPage.base;

import com.alibaba.fastjson.JSONObject;
import com.ll.DownloadKit.DownloadKit;
import com.ll.DrissonPage.error.extend.ElementNotFoundError;
import com.ll.DrissonPage.functions.Settings;
import lombok.Getter;
import lombok.Setter;
import okhttp3.Cookie;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 页面类的基类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
//页面类的基类
public abstract class BasePage<T extends BaseParser<?>> extends BaseParser<T> {
    protected String url = null;
    /**
     * 查找元素时等待的秒数
     */
    private Double timeout = 10.0;
    /**
     * 当前访问的url有效性
     */
    @Setter
    private Boolean urlAvailable = null;
    /**
     * 重试次数
     */
    @Setter
    @Getter
    private Integer retryTimes = 3;
    /**
     * 重试间隔
     */
    @Setter
    @Getter
    private Double retryInterval = 2.0;
    /**
     * 默认下载路径
     */
    @Setter
    private String downloadPath = null;
    /**
     * 下载器对象
     */
    @Getter
    private DownloadKit downloadKit = null;
    /**
     * ele返回值
     */
    @Setter
    private Boolean noneEleReturnValue = Boolean.FALSE;
    @Setter
    private Object noneEleValue = null;

    public BasePage() {
        this.setType("BasePage");
    }

    /**
     * 返回网页的标题title
     */
    public String title() {
        T title = this._ele("xpath://title", null, null, null, false, "title").get(0);
        return title instanceof DrissionElement ? ((DrissionElement<?, ?>) title).text() : null;
    }

    /**
     * 返回查找元素时等待的秒数
     */
    public Double timeout() {
        return this.timeout;
    }

    /**
     * 设置查找元素时等待的秒数
     *
     * @param timeout 秒
     */
    public void setTimeout(Double timeout) {
        if (timeout != null && timeout >= 0) this.timeout = timeout;
    }


    /**
     * 连接前的准备
     *
     * @param url      要访问的url
     * @param retry    重试次数
     * @param interval 重试间隔
     * @return 重试次数和间隔
     */
    protected BeforeConnect beforeConnect(String url, Integer retry, Double interval) {
        boolean isFile = false;

        try {
            if (Paths.get(url).toFile().exists() || (!url.contains("://") && !url.contains(":\\\\"))) {
                Path p = Paths.get(url);
                if (p.toFile().exists()) {
                    url = p.toAbsolutePath().toString();
                    isFile = true;
                }
            }
        } catch (InvalidPathException ignored) {

        }
        this.url = url;
        retry = retry == null ? this.getRetryTimes() : retry;
        interval = interval == null ? this.getRetryInterval() : interval;
        return new BeforeConnect(retry, interval, isFile);
    }

    /**
     * @return 返回当前访问的url有效性
     */
    public Boolean urlAvailable() {
        return this.urlAvailable;
    }

    /**
     * @return 返回默认下载路径
     */
    public String downloadPath() {
        return this.downloadPath;
    }

    /**
     * 返回下载器对象
     */
    public DownloadKit download() {
        if (this.downloadKit == null) this.downloadKit = new DownloadKit(this.downloadPath, null, null, this);
        return this.downloadKit;
    }

    //----------------------------------------------------------------------------------------------------------------------

    /**
     * @return 返回当前访问url
     */
    public abstract String url();
    /**
     * @return 用于被WebPage覆盖
     */
    protected String browserUrl() {
        return this.url();
    }

    public abstract JSONObject json();

    /**
     * @return 返回user agent
     */
    public abstract String userAgent();

    /**
     * 返回cookies
     */
    public List<Cookie> cookies() {
        return cookies(false);
    }

    /**
     * 返回cookies
     *
     * @param asMap 为True时返回由{name: value}键值对组成的map，为false时返回list且allInfo无效
     */
    public List<Cookie> cookies(boolean asMap) {
        return cookies(asMap, false);
    }

    /**
     * 返回cookies
     *
     * @param asMap      为True时返回由{name: value}键值对组成的map，为false时返回list且allInfo无效
     * @param allDomains 是否返回所有域的cookies
     */
    public List<Cookie> cookies(boolean asMap, boolean allDomains) {
        return cookies(asMap, allDomains, false);
    }

    /**
     * 返回cookies
     *
     * @param asMap      为True时返回由{name: value}键值对组成的map，为false时返回list且allInfo无效
     * @param allDomains 是否返回所有域的cookies
     * @param allInfo    是否返回所有信息，为False时只返回name、value、domain
     */
    public abstract List<Cookie> cookies(boolean asMap, boolean allDomains, boolean allInfo);

    public Boolean get(String url) {
        return get(url, false);
    }


    public Boolean get(String url, boolean showErrMsg) {
        return get(url, showErrMsg, retryTimes, retryInterval, timeout);
    }

    public Boolean get(String url, boolean showErrMsg, Integer retry, Double interval, Double timeout) {
        return get(url, showErrMsg, retry, interval, timeout, null);
    }

    /**
     * 用get请求跳转到url，可输入文件路径
     *
     * @param url        目标url
     * @param showErrMsg 是否显示和抛出异常
     * @param retry      重试次数，为null时使用页面对象retryTimes属性值
     * @param interval   重试间隔（秒），为null时使用页面对象retryInterval属性值
     * @param timeout    连接超时时间（秒），为null时使用页面对象timeouts.pageLoad属性值
     * @param params     连接参数 s模式专用
     * @return url是否可用
     */
    public abstract Boolean get(String url, boolean showErrMsg, Integer retry, Double interval, Double timeout, Map<String, Object> params);

    /**
     * @param by       查询元素
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从0开始，可传入负数获取倒数第几个  如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @param method   方法名称
     * @return 元素对象组成的列表
     */
    @Override
    public List<T> _ele(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr, String method) {
        Map<String, Object> map = new HashMap<>();
        map.put("str", "");
        map.put("index", index);
        if (by == null) throw new ElementNotFoundError(null, method, map);
        List<T> elements = this.findElements(by, timeout, index, relative, raiseErr);
        //如果index为空则说明是查找多元素，如果不为空，则说明是单元素查找，直接判断是否为空，如果不为空则说明单元素找到了
        if (index == null || !elements.isEmpty()) return elements;
        //如果是单元素，是否抛出异常
        if (Settings.raiseWhenEleNotFound || raiseErr != null && raiseErr)
            throw new ElementNotFoundError(null, method, map);
        //如果不抛出异常则直接创建个空的
        return new ArrayList<>();
    }

    /**
     * @param loc      定位符
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从0开始，可传入负数获取倒数第几个  如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @param method   方法名称
     * @return 元素对象组成的列表
     */
    @Override
    public List<T> _ele(String loc, Double timeout, Integer index, Boolean relative, Boolean raiseErr, String method) {
        Map<String, Object> map = new HashMap<>();
        map.put("str", "");
        map.put("index", index);
        if (loc == null) throw new ElementNotFoundError(null, method, map);
        List<T> elements = this.findElements(loc, timeout, index, relative, raiseErr);
        //如果index为空则说明是查找多元素，如果不为空，则说明是单元素查找，直接判断是否为空，如果不为空则说明单元素找到了
        if (index == null || !elements.isEmpty()) return elements;
        //如果是单元素，是否抛出异常
        if (Settings.raiseWhenEleNotFound || raiseErr != null && raiseErr)
            throw new ElementNotFoundError(null, method, map);
        //如果不抛出异常则直接创建个空的
        return new ArrayList<>();
    }
    /**
     * 执行元素查找
     *
     * @param by       查询元素
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从1开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @return 元素对象组成的列表
     */
    protected abstract List<T> findElements(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr);


    /**
     * 执行元素查找
     *
     * @param loc      定位符
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从1开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @return 元素对象组成的列表
     */
    protected abstract List<T> findElements(String loc, Double timeout, Integer index, Boolean relative, Boolean raiseErr);
}
