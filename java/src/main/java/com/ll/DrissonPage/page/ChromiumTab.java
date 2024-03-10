package com.ll.DrissonPage.page;

import com.ll.DrissonPage.functions.Settings;
import com.ll.DrissonPage.units.setter.TabSetter;
import com.ll.DrissonPage.units.waiter.TabWaiter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 实现浏览器标签页的类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class ChromiumTab extends ChromiumBase {
    private static final Map<String, ChromiumTab> TAB = new ConcurrentHashMap<>();

    protected ChromiumTab(ChromiumPage page, String tabId) {
        this.page = page;
        this.setBrowser(page.getBrowser());
        super.init(page.getAddress(), tabId, page.timeout());
        super.rect = null;
        this.setType("ChromiumTab");
    }

    public static ChromiumTab getInstance(ChromiumPage page, String tabId) {
        ChromiumTab chromiumTab = TAB.get(tabId);
        if (Settings.singletonTabObj && chromiumTab != null) return chromiumTab;
        chromiumTab = new ChromiumTab(page, tabId);
        TAB.put(tabId, chromiumTab);
        return chromiumTab;
    }

    /***
     * 重写设置浏览器运行参数方法
     */
    @Override
    protected void dSetRuntimeSettings() {
        super.timeouts = this.page.getTimeouts().copy();
        super.setRetryTimes(this.page.getRetryTimes());
        super.setRetryInterval(this.page.getRetryInterval());
        super.setLoadMode(this.page.loadMode());
        super.setDownloadPath(this.page.downloadPath());
    }

    /**
     * 关闭当前标签页
     */
    public void close() {
        this.page.closeTabs(this.tabId());
    }

    /**
     * @return 返回总体page对象
     */
    public ChromiumPage page() {
        return this.page;
    }

    /**
     * @return 返回用于设置的对象
     */
    @Override
    public TabSetter set() {
        if (super.set == null) {
            super.set = new TabSetter(this);
        }
        return (TabSetter) super.set;
    }

    /**
     * @return 返回用于等待的对象
     */
    @Override
    public TabWaiter waits() {
        if (super.wait == null) this.wait = new TabWaiter(this);
        return (TabWaiter) super.wait;
    }

    /**
     * 把当前页面保存为文件，如果path和name参数都为null，只返回文本
     *
     * @param path 保存路径，为null且name不为null时保存在当前路径
     * @param name 文件名，为null且path不为null时用title属性值
     * @return asPdf为True时返回bytes，否则返回文件文本
     */

    public Object save(String path, String name) {
        return save(path, name, false);
    }

    /**
     * 把当前页面保存为文件，如果path和name参数都为null，只返回文本
     *
     * @param path  保存路径，为null且name不为null时保存在当前路径
     * @param name  文件名，为null且path不为null时用title属性值
     * @param asPdf 为Ture保存为pdf，否则为mhtml且忽略params参数
     * @return asPdf为True时返回bytes，否则返回文件文本
     */

    public Object save(String path, String name, boolean asPdf) {
        return save(path, name, asPdf, new HashMap<>());
    }

    /**
     * 把当前页面保存为文件，如果path和name参数都为null，只返回文本
     *
     * @param path   保存路径，为null且name不为null时保存在当前路径
     * @param name   文件名，为null且path不为null时用title属性值
     * @param asPdf  为Ture保存为pdf，否则为mhtml且忽略params参数
     * @param params pdf生成参数
     * @return asPdf为True时返回bytes，否则返回文件文本
     */

    public Object save(String path, String name, boolean asPdf, Map<String, Object> params) {
        return asPdf ? ChromiumBase.getPdf(this, path, name, params) : ChromiumBase.getMHtml(this, path, name);
    }

    @Override
    public String toString() {
        return "<ChromiumTab " + "browser_id=" + this.browser().getId() + " tab_id=" + this.tabId() + '>';
    }

    /**
     * 克隆
     *
     * @param cloneNumber 克隆数量
     * @return 集合
     */
    public List<ChromiumTab> copy(int cloneNumber) {
        return IntStream.range(0, cloneNumber < 0 ? 1 : cloneNumber).mapToObj(i -> copy()).collect(Collectors.toList());
    }

    /**
     * 克隆
     *
     * @return 单个
     */
    public ChromiumTab copy() {
        return this.page.newTab(this.url());
    }

    @Override
    public void onDisconnect() {
        ChromiumTab.TAB.remove(this.tabId());
    }
}
