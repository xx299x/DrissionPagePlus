package com.ll.DrissonPage.units.setter;

import com.alibaba.fastjson.JSON;
import com.ll.DrissonPage.base.MyRunnable;
import com.ll.DrissonPage.page.ChromiumBase;
import com.ll.DrissonPage.units.cookiesSetter.CookiesSetter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class ChromiumBaseSetter extends BasePageSetter<ChromiumBase> {
    protected CookiesSetter cookiesSetter;

    public ChromiumBaseSetter(ChromiumBase page) {
        super(page);
        this.cookiesSetter = null;
    }

    /**
     * @return 返回用于设置页面加载策略的对象
     */
    public LoadMode loadMode() {
        return new LoadMode(this.page);
    }

    /**
     * @return 返回用于设置页面滚动设置的对象
     */
    public PageScrollSetter scroll() {
        return new PageScrollSetter(this.page.scroll());
    }

    /**
     * @return 返回用于设置cookies的对象
     */
    public CookiesSetter cookies() {
        if (this.cookiesSetter == null) this.cookiesSetter = new CookiesSetter(this.page);
        return this.cookiesSetter;
    }

    /**
     * 设置连接失败重连次数
     */
    public void retryTimes(Integer times) {
        this.page.setRetryTimes(times);
    }

    /**
     * 设置连接失败重连间隔
     */
    public void retryInterval(Double times) {
        this.page.setRetryInterval(times);
    }

    /**
     * 设置超时时间，单位为秒
     */
    public void timeouts() {
        timeouts(null);
    }

    /**
     * 设置超时时间，单位为秒
     *
     * @param base 基本等待时间，除页面加载和脚本超时，其它等待默认使用
     */
    public void timeouts(Double base) {
        timeouts(base, null);
    }

    /**
     * 设置超时时间，单位为秒
     *
     * @param base     基本等待时间，除页面加载和脚本超时，其它等待默认使用
     * @param pageLoad 页面加载超时时间
     */
    public void timeouts(Double base, Double pageLoad) {
        timeouts(base, pageLoad, null);
    }

    /**
     * 设置超时时间，单位为秒
     *
     * @param base     基本等待时间，除页面加载和脚本超时，其它等待默认使用
     * @param pageLoad 页面加载超时时间
     * @param script   脚本运行超时时间
     */
    public void timeouts(Double base, Double pageLoad, Double script) {
        if (base != null) {
            this.page.getTimeouts().setBase(base);
            this.page.setTimeout(base);
        }
        if (pageLoad != null) this.page.getTimeouts().setPageLoad(pageLoad);
        if (script != null) this.page.getTimeouts().setScript(script);
    }

    /**
     * 为当前tab设置user agent，只在当前tab有效
     *
     * @param ua       user agent字符串
     * @param platform platform字符串
     */
    public void userAgent(String ua, String platform) {
        Map<String, Object> map = new HashMap<>();
        map.put("userAgent", ua);
        if (platform != null && !platform.isEmpty()) {
            map.put("platform", platform);
        }
        this.page.runCdp("Emulation.setUserAgentOverride", map);
    }

    /**
     * 设置或删除某项sessionStorage信息
     *
     * @param item  要设置的项
     * @param value 项的值，设置为False时，删除该项
     */
    public void sessionStorage(String item, Object value) {
        this.page.runCdpLoaded("DOMStorage.enable");
        Object i = JSON.parseObject(this.page.runCdp("Storage.getStorageKeyForFrame", Map.of("frameId", this.page.getFrameId())).toString()).get("storageKey");
        if (value.equals(false)) {
            this.page.runCdp("DOMStorage.removeDOMStorageItem", Map.of("storageId", Map.of("storageKey", i, "isLocalStorage", false), "key", item));
        } else {
            this.page.runCdp("DOMStorage.setDOMStorageItem", Map.of("storageId", Map.of("storageKey", i, "isLocalStorage", false), "key", item, "value", value));
        }
        this.page.runCdpLoaded("DOMStorage.disable");
    }


    /**
     * 设置或删除某项localStorage信息
     *
     * @param item  要设置的项
     * @param value 项的值，设置为False时，删除该项
     */
    public void localStorage(String item, Object value) {
        this.page.runCdpLoaded("DOMStorage.enable");
        Object i = JSON.parseObject(this.page.runCdp("Storage.getStorageKeyForFrame", Map.of("frameId", this.page.getFrameId())).toString()).get("storageKey");
        if (value.equals(false)) {
            this.page.runCdp("DOMStorage.removeDOMStorageItem", Map.of("storageId", Map.of("storageKey", i, "isLocalStorage", true), "key", item));
        } else {
            this.page.runCdp("DOMStorage.setDOMStorageItem", Map.of("storageId", Map.of("storageKey", i, "isLocalStorage", true), "key", item, "value", value));
        }
        this.page.runCdpLoaded("DOMStorage.disable");
    }

    /**
     * 等待上传的文件路径
     *
     * @param files 文件路径列表或字符串，字符串时多个文件用回车分隔
     */
    public void uploadFiles(String files) {
        uploadFiles(files.split("\n"));
    }

    /**
     * 等待上传的文件路径
     *
     * @param files 文件路径列表或字符串，字符串时多个文件用回车分隔
     */
    public void uploadFiles(Path files) {
        uploadFiles(files.toAbsolutePath().toString());
    }

    /**
     * 等待上传的文件路径
     *
     * @param files 文件路径列表或字符串，字符串时多个文件用回车分隔
     */
    public void uploadFiles(String[] files) {
        uploadFiles(Arrays.asList(files));
    }

    /**
     * 等待上传的文件路径
     *
     * @param files 文件路径列表或字符串，字符串时多个文件用回车分隔
     */
    public void uploadFiles(Collection<? extends String> files) {
        if (this.page.uploadList() == null) {
            this.page.driver().setCallback("Page.fileChooserOpened", new MyRunnable() {
                @Override
                public void run() {
                    page.onFileChooserOpened(getMessage());
                }
            });
            this.page.runCdp("Page.setInterceptFileChooserDialog", Map.of("enabled", true));
        }
        List<Object> list = files.stream().map(file -> Paths.get(file).toAbsolutePath().toFile()).collect(Collectors.toList());
        this.page.setUploadList(list);
    }

    /**
     * 设置固定发送的headers
     *
     * @param headers map
     */

    public void headers(Map<String, Object> headers) {
        if (headers != null && !headers.isEmpty()) {
            this.page.runCdp("Network.enable");
            this.page.runCdp("Network.setExtraHTTPHeaders", Map.of("headers", headers));
        }
    }

    /**
     * 设置是否启用自动处理弹窗
     */

    public void autoHandleAlert() {
        autoHandleAlert(true);
    }

    /**
     * 设置是否启用自动处理弹窗
     *
     * @param onOff 开或关
     */

    public void autoHandleAlert(boolean onOff) {
        autoHandleAlert(onOff, true);
    }

    /**
     * 设置是否启用自动处理弹窗
     *
     * @param onOff  开或关
     * @param accept 确定还是取消
     */

    public void autoHandleAlert(boolean onOff, boolean accept) {
        this.page.getAlert().setAuto(onOff ? accept : null);
    }

    /**
     * 设置要忽略的url
     *
     * @param urls 要忽略的url，可用*通配符，可输入多个，传入null时清空已设置的内容
     */

    public void blockedUrls(String urls) {
        blockedUrls(Collections.singletonList(urls));
    }

    /**
     * 设置要忽略的url
     *
     * @param urls 要忽略的url，可用*通配符，可输入多个，传入null时清空已设置的内容
     */

    public void blockedUrls(String[] urls) {
        blockedUrls(Arrays.asList(urls));
    }

    /**
     * 设置要忽略的url
     *
     * @param urls 要忽略的url，可用*通配符，可输入多个，传入null时清空已设置的内容
     */

    public void blockedUrls(Collection<String> urls) {
        if (urls == null) urls = new ArrayList<>();
        this.page.runCdp("Network.enable");
        this.page.runCdp("Network.setBlockedURLs", Map.of("urls", urls));
    }

}
