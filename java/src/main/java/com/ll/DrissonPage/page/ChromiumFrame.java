package com.ll.DrissonPage.page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.base.By;
import com.ll.DrissonPage.base.MyRunnable;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.error.extend.ContextLostError;
import com.ll.DrissonPage.error.extend.ElementLostError;
import com.ll.DrissonPage.error.extend.JavaScriptError;
import com.ll.DrissonPage.error.extend.PageDisconnectedError;
import com.ll.DrissonPage.units.Coordinate;
import com.ll.DrissonPage.units.PicType;
import com.ll.DrissonPage.units.listener.FrameListener;
import com.ll.DrissonPage.units.rect.FrameRect;
import com.ll.DrissonPage.units.scroller.FrameScroller;
import com.ll.DrissonPage.units.setter.ChromiumFrameSetter;
import com.ll.DrissonPage.units.states.FrameStates;
import com.ll.DrissonPage.units.waiter.FrameWaiter;
import lombok.Getter;
import okhttp3.Cookie;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
public class ChromiumFrame extends ChromiumBase {
    private final ChromiumBase targetPage;
    private final ChromiumTab tab;
    private final String tabId;
    private final int backendId;
    private ChromiumElement frameEle;
    private ChromiumElement docEle;
    private boolean isDiffDomain;
    private FrameStates states;
    private boolean reloading;
    private FrameRect rect;
    private FrameListener listener;

    public ChromiumFrame(ChromiumBase page, ChromiumElement ele, Map<String, Object> info) {
        if ("ChromiumPage".equals(page.getType()) || "WebPage".equals(page.getType())) {
            this.page = (ChromiumPage) page;
            this.targetPage = page;
            this.tab = ((ChromiumPage) page).getTab();
            this.setBrowser(page.browser());
        } else {
            this.page = ((ChromiumTab) page).page();
            this.setBrowser(this.page.browser());
            this.targetPage = page;
            this.tab = "ChromiumFrame".equals(page.getType()) ? ((ChromiumFrame) page).getTab() : (ChromiumTab) page;
        }
        this.address = page.getAddress();
        this.tabId = page.tabId();
        this.backendId = ele.getBackendId();
        this.frameEle = ele;
        this.states = null;
        this.reloading = false;
        JSONObject node = JSON.parseObject(JSON.toJSONString(info != null ? info.get("node") : JSON.parseObject(page.runCdp("DOM.describeNode", Map.of("backendNodeId", ele.getBackendId())).toString()).get("node")));
        this.frameId = node.getString("frameId");
        if (this.isInnerFrame()) {
            this.isDiffDomain = false;
            this.docEle = new ChromiumElement(this.targetPage, null, null, node.getJSONObject("contentDocument").getInteger("backendNodeId"));
            super.init(page.getAddress(), page.tabId(), page.timeout());
        } else {
            this.isDiffDomain = true;
            this.frameId = null;
            super.init(page.getAddress(), node.getString("frameId"), page.timeout());
            this.docEle = new ChromiumElement(this, null, JSON.parseObject(super.runJs("document;", true, null, null).toString()).getString("objectId"), null);
        }
        this.rect = null;
        this.setType("ChromiumFrame");
        long endTime = System.currentTimeMillis() + 2000L;
        while (System.currentTimeMillis() < endTime) {
            String url = this.url();
            if (!(url == null || url.trim().isEmpty() || url.equals("about:blank"))) break;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ChromiumFrame && this.getFrameId().equals(((ChromiumFrame) obj).getFrameId());
    }


    /**
     * 重写设置浏览器运行参数方法
     */
    @Override
    protected void dSetRuntimeSettings() {
        if (this.getTimeouts() == null) {
            this.timeouts = this.targetPage.getTimeouts().copy();
            this.setRetryTimes(this.targetPage.getRetryTimes());
            this.setRetryInterval(this.targetPage.getRetryInterval());
            this.setDownloadPath(this.targetPage.downloadPath());
        }
        this.setLoadMode(!this.isDiffDomain ? this.targetPage.loadMode() : "normal");
    }

    /**
     * 避免出现服务器 500 错误
     *
     * @param tabId 要跳转到的标签页id
     */
    @Override
    protected void driverInit(String tabId) {
        try {
            super.driverInit(tabId);
        } catch (Exception e) {
            this.browser().getDriver().get("http://" + this.address + "/json");
            super.driverInit(tabId);
        }
        this.driver().setCallback("Inspector.detached", new MyRunnable() {
            @Override
            public void run() {
                onInspectorDetached(this.getMessage());
            }
        }, true);
        this.driver().setCallback("Page.frameDetached", null);
        this.driver().setCallback("Inspector.frameDetached", new MyRunnable() {
            @Override
            public void run() {
                onFrameDetached(this.getMessage());
            }
        }, true);
    }

    /**
     * 重新获取document
     */
    private void reload() {
        this.isLoading = true;
        this.reloading = true;
        this.docGot = false;
        this.driver().stop();
        JSONObject node = null;
        try {
            this.frameEle = new ChromiumElement(this.targetPage, null, null, this.backendId);
            long endTime = System.currentTimeMillis() + 2000L;
            while (System.currentTimeMillis() < endTime) {
                node = JSON.parseObject(this.targetPage.runCdp("DOM.describeNode", Map.of("backendNodeId", this.frameEle.getBackendId())).toString()).getJSONObject("node");
                if (node.containsKey("frameId")) break;
            }
            if (node == null) return;
        } catch (ElementLostError | PageDisconnectedError e) {
            return;
        }
        String frameId1 = node.getString("frameId");
        if (this.isInnerFrame()) {
            this.isDiffDomain = false;
            this.docEle = new ChromiumElement(this.targetPage, null, null, node.getJSONObject("contentDocument").getInteger("backendNodeId"));
            this.frameId = frameId1;
            if (this.listener != null) this.listener.toTarget(this.targetPage.tabId(), this.address, this);
            super.init(this.address, this.targetPage.tabId(), this.targetPage.timeout());
        } else {
            this.isDiffDomain = true;
            if (this.listener != null) this.listener.toTarget(frameId1, this.address, this);

            long endTime = (long) (System.currentTimeMillis() + this.timeouts.getPageLoad() * 1000);
            super.init(this.address, frameId1, this.targetPage.timeout());
            long timeout = endTime - System.currentTimeMillis();
            if (timeout <= 0) timeout = 500;
            this.waitLoaded(timeout / 1000.0);
        }
        this.isLoading = false;
        this.reloading = false;

    }

    /**
     * 刷新cdp使用的document数据
     *
     * @param timeout 超时时间
     * @return 是否获取成功
     */
    @Override
    protected Boolean getDocument(Double timeout) {
        if (super.isReading != null && super.isReading) return false;
        super.isReading = true;
        try {
            if (!this.isDiffDomain) {
                JSONObject node = JSON.parseObject(this.targetPage.runCdp("DOM.describeNode", Map.of("backendNodeId", this.backendId)).toString()).getJSONObject("node");
                this.docEle = new ChromiumElement(this.targetPage, null, null, node.getJSONObject("contentDocument").getInteger("backendNodeId"));
            } else {
                timeout = timeout >= .5 ? timeout : Double.valueOf(.5);
                Integer bId = JSON.parseObject(this.runCdp("DOM.getDocument", Map.of("_timeout", timeout)).toString()).getJSONObject("root").getInteger("backendNodeId");
                this.docEle = new ChromiumElement(this, null, null, bId);
            }
            this.rootId = this.docEle.getObjId();
            String r = this.runCdp("Page.getFrameTree").toString();
            // 定义正则表达式模式
            Pattern pattern = Pattern.compile("'id': '(.*?)'");
            Matcher matcher = pattern.matcher(r);
            // 使用循环匹配所有符合条件的字符串
            if (matcher.find()) {
                String match = matcher.group(1); // 获取匹配到的值
                this.browser().getFrames().put(match, this.tabId);
                return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (!this.reloading) this.isLoading = false;
            this.isReading = false;
        }
        return false;
    }

    /**
     * 异域转同域或退出
     *
     * @param ignoredParams 无效参数
     */

    private void onInspectorDetached(Object ignoredParams) {
        this.reload();
    }

    /**
     * 同域变异域
     */
    private void onFrameDetached(Object params) {
        String frameId1 = JSON.parseObject(params.toString()).getString("frameId");
        this.browser().getFrames().remove(frameId1);
        if (Objects.equals(frameId1, this.frameId)) this.reload();
    }
    //------------挂件-----------------


    /**
     * @return 返回用于滚动的对象
     */
    public FrameScroller scroll() {
        this.waits().docLoaded();
        if (this.scroll == null) this.scroll = new FrameScroller(this);
        return (FrameScroller) this.scroll;
    }

    /**
     * @return 返回用于设置的对象
     */
    public ChromiumFrameSetter set() {
        if (this.set == null) this.set = new ChromiumFrameSetter(this);
        return (ChromiumFrameSetter) this.set;
    }

    /**
     * @return 返回用于获取状态信息的对象
     */
    public FrameStates states() {
        if (this.states == null) this.states = new FrameStates(this);
        return this.states;
    }

    /**
     * @return 返回用于等待的对象
     */
    public FrameWaiter waits() {
        if (this.wait == null) this.wait = new FrameWaiter(this);
        return (FrameWaiter) this.wait;
    }

    /**
     * @return 返回获取坐标和大小的对象
     */
    public FrameRect rect() {
        if (rect == null) this.rect = new FrameRect(this);
        return this.rect;
    }

    /**
     * @return 返回用于聆听数据包的对象
     */
    public FrameListener listen() {
        if (this.listener == null) this.listener = new FrameListener(this);
        return this.listener;
    }


    //----------挂件----------
    public ChromiumPage page() {
        return this.page;
    }

    /**
     * @return 返回总页面上的frame元素
     */
    public ChromiumElement frameEle() {
        return this.frameEle;
    }

    /**
     * @return 返回元素tag
     */
    public String tag() {
        return this.frameEle().tag();
    }

    /**
     * @return 返回frame当前访问的url
     */
    public String url() {
        try {
            return this.docEle.runJs("return this.location.href;").toString();
        } catch (JavaScriptError | NullPointerException e) {
            return null;
        }
    }

    /**
     * @return 返回元素outerHTML文本
     */
    public String html() {
        String tag = this.tag();
        String outHtml = JSON.parseObject(this.targetPage.runCdp("DOM.getOuterHTML", Map.of("backendNodeId", this.frameEle.getBackendId())).toString()).getString("outerHTML");
        Pattern pattern = Pattern.compile("<" + tag + ".*?>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(outHtml);
        if (matcher.find()) {
            String sign = matcher.group(0);
            return sign + this.innerHtml() + "</" + tag + ">";
        }
        return ""; // 根据你的实际需求返回值可能会有所不同
    }

    /**
     * @return 返回元素innerHTML文本
     */
    public String innerHtml() {
        return this.docEle.runJs("return this.documentElement.outerHTML;").toString();
    }

    /**
     * @return 返回页面title
     */
    public String title() {
        List<ChromiumElement> list = this._ele("t:title", null, null, null, false, null);
        return !list.isEmpty() ? list.get(0).text() : null;
    }

    /**
     * @return 返回cookies
     */
    public List<Cookie> cookies() {
        Object o = this.docEle.runJs("return this.cookie;");
        List<Cookie> list = new ArrayList<>();

        JSONObject jsonObject;
        try {
            for (Object object : JSON.parseArray(this.docEle.runJs("return this.cookie;").toString())) {
                jsonObject = JSON.parseObject(object.toString());
                Cookie.Builder builder = new Cookie.Builder();
                builder.name(jsonObject.getString("name"));
                builder.value(jsonObject.getString("value"));
                builder.domain(jsonObject.getString("domain"));
                builder.hostOnlyDomain(jsonObject.getString("domain"));
                builder.expiresAt(jsonObject.getInteger("expiresAt"));
                builder.path(jsonObject.getString("path"));
                list.add(builder.build());
            }
        } catch (Exception e) {
            jsonObject = JSON.parseObject(this.docEle.runJs("return this.cookie;").toString());
            Cookie.Builder builder = new Cookie.Builder();
            builder.name(jsonObject.getString("name"));
            builder.value(jsonObject.getString("value"));
            builder.domain(jsonObject.getString("domain"));
            builder.hostOnlyDomain(jsonObject.getString("domain"));
            builder.expiresAt(jsonObject.getInteger("expiresAt"));
            builder.path(jsonObject.getString("path"));
            list.add(builder.build());
        }
        return this.isDiffDomain ? super.cookies() : list;
    }

    /**
     * @return 返回frame元素所有attribute属性
     */
    public Map<String, String> attrs() {
        return this.frameEle.attrs();
    }

    /**
     * @return 返回当前焦点所在元素（需要测试一下）
     */
    public ChromiumElement actionEle() {
        Object o = this.docEle.runJs("return this.activeElement;");
        System.out.println(o);
        return null;
//        return new ChromiumElement(this.targetPage,);
    }

    /**
     * @return 返回frame的xpath绝对路径
     */

    public String xpath() {
        return this.frameEle.xpath();
    }

    /**
     * @return 返回frame的css selector绝对路径
     */

    public String cssPath() {
        return this.frameEle.cssPath();
    }

    /**
     * @return 返回frame所在tab的id
     */
    public String tabId() {
        return this.tabId;
    }

    public String downloadPath() {
        return super.downloadPath();
    }

    /**
     * @return 返回当前页面加载状态，'loading' 'interactive' 'complete'
     */
    protected String jsReadyState() {
        if (this.isDiffDomain) {
            return super.jsReadyState();
        } else {
            try {
                return this.docEle.runJs("return this.readyState;").toString();
            } catch (ContextLostError e) {
                try {
                    JSONObject node = JSON.parseObject(this.runCdp("DOM.describeNode", Map.of("backendNodeId", this.frameEle.getBackendId())).toString()).getJSONObject("node");
                    ChromiumElement chromiumElement = new ChromiumElement(this.targetPage, null, null, node.getJSONObject("contentDocument").getInteger("backendNodeId"));
                    return chromiumElement.runJs("return this.readyState;").toString();
                } catch (Exception i) {
                    return null;
                }
            } catch (NullPointerException e) {
                return null;
            }
        }

    }

    /**
     * 刷新frame页面
     */
    @Override
    public boolean refresh() {
        this.docEle.runJs("this.location.reload();");
        return true;
    }

    /**
     * 返回frame元素attribute属性值
     *
     * @param attr 属性名
     * @return 属性值文本，没有该属性返回null
     */
    public String attr(String attr) {
        return this.frameEle.attr(attr);
    }

    /**
     * 删除frame元素attribute属性
     *
     * @param attr 属性名
     */
    public void remove(String attr) {
        this.frameEle.removeAttr(attr);
    }

    /**
     * 运行javascript代码
     *
     * @param js      js文本
     * @param asExpr  是否作为表达式运行，为True时args无效
     * @param timeout js超时时间（秒），为null则使用页面timeouts.script设置
     * @param params  参数
     */
    @Override
    public Object runJs(String js, Boolean asExpr, Double timeout, List<Object> params) {
        return js.startsWith("this.scrollIntoView") ? this.frameEle.runJs(js, asExpr, timeout, params) : this.docEle.runJs(js, asExpr, timeout, params);
    }

    /**
     * 运行javascript代码
     *
     * @param js      js文本
     * @param asExpr  是否作为表达式运行，为True时args无效
     * @param timeout js超时时间（秒），为null则使用页面timeouts.script设置
     * @param params  参数
     */
    @Override
    public Object runJs(Path js, Boolean asExpr, Double timeout, List<Object> params) {
        return js.startsWith("this.scrollIntoView") ? this.frameEle.runJs(js, asExpr, timeout, params) : this.docEle.runJs(js, asExpr, timeout, params);
    }


    /**
     * 返回上面某一级父元素，可指定层数或用查询语法定位
     *
     * @param index 第几级父元素，1开始，或定位符
     * @return 上级元素对象
     */
    public ChromiumElement parent(int index) {
        return this.frameEle.parent(index);
    }


    /**
     * 返回上面某一级父元素，可指定层数或用查询语法定位
     *
     * @return 上级元素对象
     */
    public ChromiumElement parent() {
        return this.frameEle.parent(1);
    }

    /**
     * 返回上面某一级父元素，可指定层数或用查询语法定位
     *
     * @param loc   第几级父元素，1开始，或定位符
     * @param index 使用此参数选择第几个结果，1开始
     * @return 上级元素对象
     */
    public ChromiumElement parent(String loc, int index) {
        return this.frameEle.parent(loc, index);
    }

    /**
     * 返回上面某一级父元素，可指定层数或用查询语法定位
     *
     * @param loc 第几级父元素，1开始，或定位符
     * @return 上级元素对象
     */
    public ChromiumElement parent(String loc) {
        return parent(loc, 1);
    }

    /**
     * 返回上面某一级父元素，可指定层数或用查询语法定位
     *
     * @param by    第几级父元素，1开始，或定位符
     * @param index 使用此参数选择第几个结果，1开始
     * @return 上级元素对象
     */
    public ChromiumElement parent(By by, int index) {
        return this.frameEle.parent(by, index);
    }

    /**
     * 返回上面某一级父元素，可指定层数或用查询语法定位
     *
     * @param by 第几级父元素，1开始，或定位符
     * @return 上级元素对象
     */
    public ChromiumElement parent(By by) {
        return parent(by, 1);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素或节点
     */
    public ChromiumElement prev(String loc, int index, Double timeout, boolean eleOnly) {
        return this.frameEle.prev(loc, index, timeout, eleOnly);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素或节点
     */
    public ChromiumElement prev(String loc, int index, Double timeout) {
        return this.prev(loc, index, timeout, true);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc   用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 同级元素或节点
     */
    public ChromiumElement prev(String loc, int index) {
        return this.prev(loc, index, 0.0);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc 用于筛选的查询语法
     * @return 同级元素或节点
     */
    public ChromiumElement prev(String loc) {
        return this.prev(loc, 1);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @return 同级元素或节点
     */
    public ChromiumElement prev() {
        return this.prev("");
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素或节点
     */
    public ChromiumElement prev(By by, int index, Double timeout, boolean eleOnly) {
        return this.frameEle.prev(by, index, timeout, eleOnly);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素或节点
     */
    public ChromiumElement prev(By by, int index, Double timeout) {
        return this.prev(by, index, timeout, true);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by    用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 同级元素或节点
     */
    public ChromiumElement prev(By by, int index) {
        return this.prev(by, index, 0.0);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by 用于筛选的查询语法
     * @return 同级元素或节点
     */
    public ChromiumElement prev(By by) {
        return this.prev(by, 1);
    }


    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素或节点
     */
    public ChromiumElement next(String loc, int index, Double timeout, boolean eleOnly) {
        return this.frameEle.next(loc, index, timeout, eleOnly);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素或节点
     */
    public ChromiumElement next(String loc, int index, Double timeout) {
        return this.next(loc, index, timeout, true);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc   用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 同级元素或节点
     */
    public ChromiumElement next(String loc, int index) {
        return this.next(loc, index, 0.0);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc 用于筛选的查询语法
     * @return 同级元素或节点
     */
    public ChromiumElement next(String loc) {
        return this.next(loc, 1);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @return 同级元素或节点
     */
    public ChromiumElement next() {
        return this.next("");
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素或节点
     */
    public ChromiumElement next(By by, int index, Double timeout, boolean eleOnly) {
        return this.frameEle.next(by, index, timeout, eleOnly);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素或节点
     */
    public ChromiumElement next(By by, int index, Double timeout) {
        return this.next(by, index, timeout, true);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by    用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 同级元素或节点
     */
    public ChromiumElement next(By by, int index) {
        return this.next(by, index, 0.0);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by 用于筛选的查询语法
     * @return 同级元素或节点
     */
    public ChromiumElement next(By by) {
        return this.next(by, 1);
    }


    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement before(String loc, int index, Double timeout, boolean eleOnly) {
        return this.frameEle.before(loc, index, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement before(String loc, int index, Double timeout) {
        return this.before(loc, index, timeout, true);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc   用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement before(String loc, int index) {
        return this.before(loc, index, null);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement before(String loc) {
        return this.before(loc, 1);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement before() {
        return this.before("");
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement before(By by, int index, Double timeout, boolean eleOnly) {
        return this.frameEle.before(by, index, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement before(By by, int index, Double timeout) {
        return this.before(by, index, timeout, true);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by    用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement before(By by, int index) {
        return this.before(by, index, null);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement before(By by) {
        return this.before(by, 1);
    }


    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement after(String loc, int index, Double timeout, boolean eleOnly) {
        return this.frameEle.after(loc, index, timeout, eleOnly);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement after(String loc, int index, Double timeout) {
        return this.after(loc, index, timeout, true);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc   用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement after(String loc, int index) {
        return this.after(loc, index, null);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement after(String loc) {
        return this.after(loc, 1);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement after() {
        return this.after("");
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement after(By by, int index, Double timeout, boolean eleOnly) {
        return this.frameEle.after(by, index, timeout, eleOnly);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement after(By by, int index, Double timeout) {
        return this.after(by, index, timeout, true);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by    用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement after(By by, int index) {
        return this.after(by, index, null);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 本元素前面的某个元素或节点
     */
    public ChromiumElement after(By by) {
        return this.after(by, 1);
    }


    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> prevs(String loc, Double timeout, boolean eleOnly) {
        return this.frameEle.prevs(loc, timeout, eleOnly);
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> prevs(String loc, Double timeout) {
        return this.prevs(loc, timeout, true);
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> prevs(String loc) {
        return this.prevs(loc, 0.0);
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> prevs() {
        return this.prevs("");
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> prevs(By by, Double timeout, boolean eleOnly) {
        return this.frameEle.prevs(by, timeout, eleOnly);
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> prevs(By by, Double timeout) {
        return this.prevs(by, timeout, true);
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by 用于筛选的查询语法
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> prevs(By by) {
        return this.prevs(by, 0.0);
    }


    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> nexts(String loc, Double timeout, boolean eleOnly) {
        return this.frameEle.nexts(loc, timeout, eleOnly);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> nexts(String loc, Double timeout) {
        return this.nexts(loc, timeout, true);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> nexts(String loc) {
        return this.nexts(loc, 0.0);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> nexts() {
        return this.nexts("");
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> nexts(By by, Double timeout, boolean eleOnly) {
        return this.frameEle.nexts(by, timeout, eleOnly);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> nexts(By by, Double timeout) {
        return this.nexts(by, timeout, true);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by 用于筛选的查询语法
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> nexts(By by) {
        return this.nexts(by, 0.0);
    }


    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> befores(String loc, Double timeout, boolean eleOnly) {
        return this.frameEle.befores(loc, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> befores(String loc, Double timeout) {
        return this.befores(loc, timeout, true);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> befores(String loc) {
        return this.befores(loc, null);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> befores() {
        return this.befores("");
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> befores(By by, Double timeout, boolean eleOnly) {
        return this.frameEle.befores(by, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> befores(By by, Double timeout) {
        return this.befores(by, timeout, true);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> befores(By by) {
        return this.befores(by, null);
    }


    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> afters(String loc, Double timeout, boolean eleOnly) {
        return this.frameEle.afters(loc, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> afters(String loc, Double timeout) {
        return this.afters(loc, timeout, true);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> afters(String loc) {
        return this.afters(loc, null);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> afters() {
        return this.afters("");
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> afters(By by, Double timeout, boolean eleOnly) {
        return this.frameEle.afters(by, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> afters(By by, Double timeout) {
        return this.afters(by, timeout, true);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 同级元素组成的列表
     */
    public List<ChromiumElement> afters(By by) {
        return this.afters(by, null);
    }

    public Object getScreenshot(String path, String name, PicType asBytes, PicType asBase64) {
        return this.frameEle.getScreenshot(path, name, asBytes, asBase64, true);
    }


    /**
     * 实现截图
     *
     * @param path     文件保存路径
     * @param name     完整文件名，后缀可选 'jpg','jpeg','png','webp'
     * @param asBytes  是否以字节形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数和as_base64参数无效
     * @param asBase64 是否以base64字符串形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数无效
     * @param fullPage 是否整页截图，为True截取整个网页，为False截取可视窗口
     * @param leftTop  截取范围左上角坐标
     * @param rightTop 截取范围右下角角坐标
     * @param ele      为异域iframe内元素截图设置
     * @return 图片完整路径或字节文本
     */
    public Object _getScreenshot(String path, String name, PicType asBytes, PicType asBase64, Boolean fullPage, Coordinate leftTop, Coordinate rightTop, ChromiumElement ele) {
        if (!this.isDiffDomain) return super.getScreenshot(path, name, asBytes, asBase64, fullPage, leftTop, rightTop);

        String picType;
        if (asBytes != null) {
            if (asBytes.equals(PicType.DEFAULT)) {
                picType = PicType.PNG.getValue();
            } else {
                picType = (asBytes.equals(PicType.JPG) ? PicType.JPEG : asBytes).getValue();
            }
        } else if (asBase64 != null) {
            if (asBase64.equals(PicType.DEFAULT)) {
                picType = PicType.PNG.getValue();
            } else {
                picType = (asBytes.equals(PicType.JPG) ? PicType.JPEG : asBytes).getValue();
            }
        } else {
            path = path != null ? path.replaceAll("[\\\\/]+$", "") : ".";
            if (!(path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".png") || path.endsWith(".webp"))) {
                if (name == null) {
                    name = this.title() + ".jpg";
                } else if (!(name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp"))) {
                    name = name + ".jpg";
                }
                path = path + FileSystems.getDefault().getSeparator() + name;
            }

            Path imagePath = Paths.get(path);
            path = imagePath.toString();
            String suffix = imagePath.getFileName().toString().toLowerCase();
            String substring = suffix.substring(1);


            picType = ".jpg".equals(substring) ? PicType.JPEG.getValue() : substring;
        }
        this.frameEle.scroll().toSee(true);
        this.scroll().toSee(ele, true);
        Coordinate c = ele.rect().viewportLocation();
        Coordinate s = ele.rect().size();
        String imeData = "data:image/" + picType + ";base64," + this.frameEle.getScreenshot(null, null, null, PicType.PNG, true);
        ChromiumElement body = this.tab.ele("t:body");
        ChromiumElement firstChild = body.ele("c::first-child");
        String js = " img = document.createElement('img');\n" + "        img.src = " + imeData + ";\n" + "        img.style.setProperty(\"z-index\",9999999);\n" + "        img.style.setProperty(\"position\",\"fixed\");\n" + "        arguments[0].insertBefore(img, this);\n" + "        return img;";
        //这里可能有问题
//        Object o = firstChild.runJs(js, List.of(body));
        return null;
    }

    @Override
    protected List<ChromiumElement> findElements(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        this.waits().docLoaded();
        return index != null ? this.docEle._ele(by, timeout, index, relative, raiseErr, null) : this.docEle.eles(by, timeout);
    }

    @Override
    protected List<ChromiumElement> findElements(String loc, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        this.waits().docLoaded();
        return index != null ? this.docEle._ele(loc, timeout, index, relative, raiseErr, null) : this.docEle.eles(loc, timeout);
    }


    /**
     * @return 返回当前frame是否同域
     */
    private boolean isInnerFrame() {
        return JSON.parseObject(this.targetPage.runCdp("Page.getFrameTree").toString()).getString("frameTree").contains(this.frameId);
    }

}
