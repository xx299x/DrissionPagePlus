package com.ll.DrissonPage.page;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.base.*;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.element.SessionElement;
import com.ll.DrissonPage.error.extend.CDPError;
import com.ll.DrissonPage.error.extend.ContextLostError;
import com.ll.DrissonPage.error.extend.PageDisconnectedError;
import com.ll.DrissonPage.functions.Locator;
import com.ll.DrissonPage.functions.Tools;
import com.ll.DrissonPage.functions.Web;
import com.ll.DrissonPage.units.Actions;
import com.ll.DrissonPage.units.Coordinate;
import com.ll.DrissonPage.units.PicType;
import com.ll.DrissonPage.units.listener.Listener;
import com.ll.DrissonPage.units.rect.TabRect;
import com.ll.DrissonPage.units.screencast.Screencast;
import com.ll.DrissonPage.units.scroller.PageScroller;
import com.ll.DrissonPage.units.scroller.Scroller;
import com.ll.DrissonPage.units.setter.ChromiumBaseSetter;
import com.ll.DrissonPage.units.states.PageStates;
import com.ll.DrissonPage.units.waiter.BaseWaiter;
import lombok.Getter;
import lombok.Setter;
import okhttp3.Cookie;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 标签页、frame、页面基类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public abstract class ChromiumBase extends BasePage<ChromiumElement> implements Occupant {
    @Getter
    protected String address;
    @Getter
    protected String frameId;
    @Getter
    protected ChromiumPage page;
    protected Boolean isReading;
    @Getter
    protected Timeout timeouts;
    @Getter
    protected Boolean isLoading;
    protected Scroller scroll;
    @Getter
    protected String rootId;
    protected BaseWaiter wait;
    protected ChromiumBaseSetter set;
    protected Boolean docGot;
    protected TabRect rect;
    @Getter
    @Setter
    private Browser browser;
    /**
     * 返回用于控制浏览器的Driver对象
     */
    protected Driver driver;
    @Setter
    private String loadMode;
    @Getter
    @Setter
    private List<Object> uploadList;
    private Screencast screencast;
    private Actions actions;
    private Listener listener;
    private PageStates states;
    @Getter
    private Alert alert;
    @Getter
    private Boolean hasAlert;
    private Double loadEndTime;
    private List<String> initJss = null;
    @Getter
    private String readyState;

    protected ChromiumBase() {

    }

    public ChromiumBase(String address, String tabId, Double timeout) {
        init(address, tabId, timeout);
    }

    public ChromiumBase(String address, String tabId) {
        this(address, tabId, null);
    }


    public ChromiumBase(String address) {
        this(address, null);

    }

    public ChromiumBase(Integer address, String tabId, Double timeout) {
        this("127.0.0.1:" + String.format("%04d", Math.max(address, 1000)), tabId, timeout);
    }

    public ChromiumBase(Integer address, String tabId) {
        this(address, tabId, null);
    }

    public ChromiumBase(Integer address) {
        this(address, null);
    }

    /**
     * 初始化全部参数，可在子类中初始化
     *
     * @param address 链接地址
     * @param tabId   浏览器唯一id
     * @param timeout 超时时间 秒
     */
    protected void init(String address, String tabId, Double timeout) {
        this.isLoading = null;
        this.rootId = null;
        this.set = null;
        this.screencast = null;
        this.actions = null;
        this.states = null;
        this.hasAlert = false;
        this.readyState = null;
        this.rect = null;
        this.wait = null;
        this.scroll = null;
        this.url = null;
        this.uploadList = null;
        this.docGot = false;//用于在LoadEventFired和FrameStoppedLoading间标记是否已获取doc
        this.loadEndTime = 0.0;
        this.initJss = new ArrayList<>();
        this.setType("ChromiumBase");
        this.listener = null;
        this.address = address;
        this.dSetStartOptions(this.address);
        this.dSetRuntimeSettings();
        this.connectBrowser(tabId);
        if (timeout != null) this.setTimeout(timeout);
    }

    /**
     * 设置浏览器启动属性
     *
     * @param address 'ip:port'
     */
    private void dSetStartOptions(String address) {
        this.address = address.replace("localhost", "127.0.0.1").replaceFirst("^(http://|https://)", "");
    }

    protected void dSetRuntimeSettings() {
        this.timeouts = new Timeout(this);
        this.loadMode = "normal";
    }

    /**
     * 连接浏览器，在第一次时运行
     *
     * @param tabId 要控制的标签页id，不指定默认为激活的
     */
    protected void connectBrowser(String tabId) {
        this.isReading = false;
        if (tabId == null || tabId.isEmpty()) {
            JSONArray jsonArray = (JSONArray) this.browser.getDriver().get("http://" + this.address + "/json");
            List<List<String>> tabs = new ArrayList<>();
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                String type = jsonObject.getString("type");
                String url = jsonObject.getString("url");
                if (("page".contains(type) || "webview".contains(type)) && !url.startsWith("devtools://"))
                    tabs.add(List.of(jsonObject.getString("id"), jsonObject.getString("url")));
            }

            Integer dialog = null;
            if (tabs.size() > 1) {
                for (int i = 0, tabsSize = tabs.size(); i < tabsSize; i++) {
                    List<String> tab = tabs.get(i);
                    if (tab.get(1).equals("chrome://privacy-sandbox-dialog/notice")) {
                        dialog = i;
                    } else if (tabId == null) {
                        tabId = tab.get(0);
                    }
                    if (tabId != null && dialog != null) break;
                }

                if (dialog != null) {
                    ChromiumBase.closePrivacyDialog(this, tabs.get(dialog).get(0));
                }
            } else if (!tabs.isEmpty()) {
                tabId = tabs.get(0).get(0);
            }

        }
        this.driverInit(tabId);
        if ("complete".equals(this.jsReadyState()) && this.readyState == null) {
            this.getDocument();
            this.readyState = "complete";
        }
    }

    /**
     * 新建页面、页面刷新、切换标签页后要进行的cdp参数初始化
     *
     * @param tabId 要跳转到的标签页id
     */
    protected void driverInit(String tabId) {
        this.isLoading = true;
        this.driver = this.browser.getDriver(tabId, this);
        this.alert = new Alert();
        this.driver.setCallback("Page.javascriptDialogOpening", new MyRunnable() {
            @Override
            public void run() {
                onAlterOpen(getMessage());
            }
        }, true);

        this.driver.setCallback("Page.javascriptDialogClosed", new MyRunnable() {
            @Override
            public void run() {
                onAlertClose(getMessage());
            }
        });
        this.driver.run("DOM.enable");
        this.driver.run("Page.enable");
        this.driver.run("Emulation.setFocusEmulationEnabled", Map.of("enabled", true));
        Object r = this.runCdp("Page.getFrameTree");
        Matcher matcher = Pattern.compile("'id': '(.*?)'").matcher(r.toString());
        while (matcher.find()) this.browser.getFrames().put(matcher.group(1), this.tabId());
        if (this.frameId == null)
            this.frameId = JSON.parseObject(r.toString()).getJSONObject("frameTree").getJSONObject("frame").getString("id");

        this.driver.setCallback("Page.frameStartedLoading", new MyRunnable() {
            @Override
            public void run() {
                onFrameStartedLoading(getMessage());
            }
        });
        this.driver.setCallback("Page.frameNavigated", new MyRunnable() {
            @Override
            public void run() {
                onFrameNavigated(getMessage());
            }
        });
        this.driver.setCallback("Page.domContentEventFired", new MyRunnable() {
            @Override
            public void run() {
                onDomContentEventFired(getMessage());
            }
        });
        this.driver.setCallback("Page.loadEventFired", new MyRunnable() {
            @Override
            public void run() {
                onLoadEventFired(getMessage());
            }
        });
        this.driver.setCallback("Page.frameStoppedLoading", new MyRunnable() {
            @Override
            public void run() {
                onFrameStoppedLoading(getMessage());
            }
        });
        this.driver.setCallback("Page.frameAttached", new MyRunnable() {
            @Override
            public void run() {
                onFrameAttached(getMessage());
            }
        });
        this.driver.setCallback("Page.frameDetached", new MyRunnable() {
            @Override
            public void run() {
                onFrameDetached(getMessage());
            }
        });
    }

    private void getDocument() {
        getDocument(10.0);
    }

    /**
     * 获取页面文档
     *
     * @param timeout 超时时间
     * @return 是否获取成功
     */
    protected Boolean getDocument(Double timeout) {
        if (this.isReading != null && this.isReading) return null;
        this.isReading = true;
        timeout = timeout != null && timeout >= .5 ? timeout : .5;
        long end_time = (long) (System.currentTimeMillis() + timeout * 1000);
        Object bId;
        Boolean result = null;
        while (System.currentTimeMillis() < end_time) {
            try {
                bId = JSON.parseObject(this.runCdp("DOM.getDocument", Map.of("_timeout", timeout)).toString()).getJSONObject("root").get("backendNodeId");
                timeout = (double) (end_time - System.currentTimeMillis());
                timeout = timeout <= 0 ? .5 : timeout;
                this.rootId = JSON.parseObject(this.runCdp("DOM.resolveNode", Map.of("backendNodeId", bId, "_timeout", timeout)).toString()).getJSONObject("object").getString("objectId");
                result = true;
                break;
            } catch (Exception e) {
//                e.printStackTrace();
                timeout = (double) (end_time - System.currentTimeMillis());
                timeout = timeout <= 0 ? .5 : timeout;
            }
        }
        if (result == null) result = false;
        if (result) {
            Matcher matcher = Pattern.compile("'id': '(.*?)'").matcher(this.runCdp("Page.getFrameTree").toString());
            while (matcher.find()) this.browser.getFrames().put(matcher.group(1), this.tabId());
        }
        this.isLoading = false;
        this.isReading = false;
        return result;
    }

    private void onFrameDetached(Object message) {
        this.browser.getFrames().remove(JSON.parseObject(message.toString()).getString("frameId"));
    }

    private void onFrameAttached(Object message) {
        this.browser.getFrames().put(JSON.parseObject(message.toString()).getString("frameId"), this.tabId());
    }

    /**
     * 页面开始加载时执行
     */
    private void onFrameStartedLoading(Object message) {
        String id = JSON.parseObject(message.toString()).getString("frameId");
        this.browser.getFrames().put(id, this.tabId());
        if (Objects.equals(this.frameId, id)) {
            this.docGot = false;
            this.readyState = "connecting";
            this.isLoading = true;
            this.loadEndTime = System.currentTimeMillis() + this.timeouts.getPageLoad() * 1000;
            if (Objects.equals(this.loadMode, "eager")) {
                Thread thread = new Thread(this::waitToStop);
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    /**
     * 页面跳转时执行
     */
    private void onFrameNavigated(Object message) {
        if (Objects.equals(this.frameId, JSON.parseObject(message.toString()).getJSONObject("frame").getString("id"))) {
            this.docGot = false;
            this.readyState = "loading";
            this.isLoading = true;
        }
    }

    /**
     * 在页面刷新、变化后重新读取页面内容
     */
    private void onDomContentEventFired(Object ignoredMessage) {
        if (Objects.equals("eager", this.loadMode)) this.runCdp("Page.stopLoading");
        if (Boolean.TRUE.equals(this.getDocument(this.loadEndTime - System.currentTimeMillis() - .1)))
            this.docGot = true;
        this.readyState = "interactive";
    }

    /**
     * 在页面刷新、变化后重新读取页面内容
     */
    private void onLoadEventFired(Object ignoredMessage) {
        if (!this.docGot && Boolean.TRUE.equals(this.getDocument(this.loadEndTime - System.currentTimeMillis() - .1f)))
            this.docGot = true;
        this.readyState = "complete";
    }

    //----------挂件----------

    /**
     * 页面加载完成后执行
     */
    private void onFrameStoppedLoading(Object message) {
        String id = JSON.parseObject(message.toString()).getString("frameId");
        this.browser.getFrames().put("id", this.tabId());
        if (Objects.equals(id, this.frameId)) {
            if (!this.docGot) this.getDocument(this.loadEndTime - System.currentTimeMillis() - .1);
            this.readyState = "complete";
        }
    }

    /**
     * 文件选择框打开时执行
     */
    public void onFileChooserOpened(Object message) {
        JSONObject jsonObject = JSON.parseObject(message.toString());
        if (this.uploadList != null && !this.uploadList.isEmpty()) {
            if (!jsonObject.containsKey("backendNodeId"))
                throw new IllegalArgumentException("该输入框无法接管，请改用对<input>元素输入路径的方法设置。");
            List<Object> files = Objects.equals("selectMultiple", jsonObject.getString("mode")) ? this.uploadList : List.of(this.uploadList.get(0));
            this.runCdp("DOM.setFileInputFiles", Map.of("files", files, "backendNodeId", jsonObject.get("backendNodeId")));
            this.driver.setCallback("Page.fileChooserOpened", null);
            this.runCdp("Page.setInterceptFileChooserDialog", Map.of("enabled", false));
            this.uploadList = null;
        }
    }

    /**
     * eager策略超时时使页面停止加载
     */
    private void waitToStop() {
        long endTime = (long) (System.currentTimeMillis() + this.timeouts.getPageLoad() * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (("interactive".equals(this.readyState) || "complete".equals(this.readyState)) && this.isLoading != null && this.isLoading) {
            this.stopLoading();
        }
    }

    /**
     * @return 返回用于等待的对象
     */
    public BaseWaiter waits() {
        if (this.wait == null) this.wait = new BaseWaiter(this);
        return this.wait;
    }

    /**
     * @return 返回用于设置的对象
     */
    public ChromiumBaseSetter set() {
        if (this.set == null) this.set = new ChromiumBaseSetter(this);
        return this.set;
    }

    /**
     * @return 返回用于录屏的对象
     */
    public Screencast screencast() {
        if (this.screencast == null) this.screencast = new Screencast(this);
        return this.screencast;
    }

    /**
     * @return 返回用于执行动作链的对象
     */
    public Actions actions() {
        if (this.actions == null) this.actions = new Actions(this);
        return this.actions;
    }

    /**
     * @return 返回用于聆听数据包的对象
     */
    public Listener listen() {
        if (this.listener == null) this.listener = new Listener(this);
        return this.listener;
    }


    //----------挂件----------

    /**
     * @return 返回用于获取状态信息的对象
     */

    public PageStates states() {
        if (this.states == null) this.states = new PageStates(this);
        return this.states;
    }

    /**
     * @return 返回用于滚动滚动条的对象
     */

    public PageScroller scroll() {
        this.wait.docLoaded();
        if (this.scroll == null) this.scroll = new PageScroller(this);
        return (PageScroller) this.scroll;
    }

    /**
     * @return 返回获取窗口坐标和大小的对象
     */
    public TabRect rect() {
        if (this.rect == null) this.rect = new TabRect(this);

        return this.rect;
    }

    /**
     * @return 返回用于控制浏览器cdp的driver
     */
    public Browser browser() {
        return this.browser;
    }

    /**
     * @return 返回用于控制浏览器的Driver对象
     */
    public Driver driver() {
        if (this.driver == null) throw new RuntimeException("浏览器已关闭或链接已断开.");
        return driver;
    }

    /**
     * @return 返回当前页面title
     */
    public String title() {
        return JSON.parseObject(this.runCdpLoaded("Target.getTargetInfo", Map.of("targetId", this.targetId())).toString()).getJSONObject("targetInfo").getString("title");
    }

    /**
     * @return 返回当前页面url
     */
    public String url() {
        Object o = this.runCdpLoaded("Target.getTargetInfo", Map.of("targetId", this.targetId()));
        return o == null ? null : JSON.parseObject(o.toString()).getJSONObject("targetInfo").getString("url");
    }

    /**
     * @return 用于被WebPage覆盖
     */
    protected String browserUrl() {
        return this.url();
    }

    /**
     * @return 返回当前页面html文本
     */
    public String html() {
        this.wait.docLoaded();
        return JSON.parseObject(this.runCdp("DOM.getOuterHTML", Map.of("objectId", this.rootId)).toString()).getString("outerHTML");
    }

    /**
     * @return 当返回内容是json格式时，返回对应的字典，非json格式时返回null
     */
    public JSONObject json() {
        try {
            return JSON.parseObject(ele("t:pre", 1, 0.5).text());
        } catch (JSONException e) {
            return null;
        }

    }

    /**
     * @return 返回当前标签页id
     */
    public String tabId() {
        return this.targetId();
    }

    /**
     * @return 返回当前标签页id
     */
    private String targetId() {
        return !this.driver.getStopped().get() ? this.driver.getId() : "";
    }

    /**
     * @return 返回当前焦点所在元素
     */
    public ChromiumElement activeEle() {
        return (ChromiumElement) this.runJsLoaded("return document.activeElement;");
    }

    /**
     * @return 返回页面加载策略，有3种：'null'、'normal'、'eager'
     */
    public String loadMode() {
        return this.loadMode;
    }

    /**
     * @return 返回user agent
     */
    public String userAgent() {
        return JSON.parseObject(this.runCdp("Runtime.evaluate", Map.of("expression", "navigator.userAgent;")).toString()).getJSONObject("result").getString("value");
    }

    /**
     * @return 返回user agent
     */
    public String ua() {
        return userAgent();
    }

    /**
     * @return 返回等待上传文件列表
     */
    public List<Object> uploadList() {
        return this.uploadList;
    }

    /**
     * @return 返回js获取的ready state信息
     */
    protected String jsReadyState() {
        try {
            return JSON.parseObject(this.runCdp("Runtime.evaluate", Map.of("expression", "document.readyState;", "_timeout", 3)).toString()).getJSONObject("result").getString("value");
        } catch (ContextLostError e) {
            return null;
        } catch (Exception e) {
            return "other" + e.getMessage();
        }
    }

    /**
     * 执行Chrome DevTools Protocol语句
     *
     * @param cmd 协议项目
     * @return 执行的结果
     */
    public Object runCdp(String cmd) {
        return runCdp(cmd, new HashMap<>());
    }

    /**
     * 执行Chrome DevTools Protocol语句
     *
     * @param cmd    协议项目
     * @param params 参数
     * @return 执行的结果
     */
    public Object runCdp(String cmd, Map<String, Object> params) {
        params = params == null ? new HashMap<>() : params;
        params = new HashMap<>(params);
        Object ignore = params.remove("_ignore");
        Object run = this.driver.run(cmd, params);
        if (JSON.parseObject(run.toString()).containsKey(Browser.__ERROR__)) {
            try {
                Tools.raiseError(JSON.parseObject(run.toString()), ignore);
            } catch (JSONException e) {
                Tools.raiseError(JSON.parseObject(JSON.toJSONString(run)), ignore);
            }
            return null;
        }
        return run;
    }

    /**
     * 执行Chrome DevTools Protocol语句，执行前等待页面加载完毕
     *
     * @param cmd 协议项目
     * @return 执行的结果
     */

    public Object runCdpLoaded(String cmd) {
        return this.runCdpLoaded(cmd, new HashMap<>());
    }

    /**
     * 执行Chrome DevTools Protocol语句，执行前等待页面加载完毕
     *
     * @param cmd    协议项目
     * @param params 参数
     * @return 执行的结果
     */

    public Object runCdpLoaded(String cmd, Map<String, Object> params) {
        this.wait.docLoaded();
        return this.runCdp(cmd, params);
    }

    /**
     * 运行javascript代码
     *
     * @param js js文本可以是路径
     */
    public Object runJs(String js) {
        return runJs(js, new ArrayList<>());
    }

    /**
     * 运行javascript代码
     *
     * @param js     js文本可以是路径
     * @param params 参数
     */
    public Object runJs(String js, List<Object> params) {
        return runJs(js, null, params);
    }

    /**
     * 运行javascript代码
     *
     * @param js      js文本可以是路径
     * @param timeout js超时时间（秒），为null则使用页面timeouts.script设置
     * @param params  参数
     */
    public Object runJs(String js, Double timeout, List<Object> params) {
        return runJs(js, false, timeout, params);
    }

    /**
     * 运行javascript代码
     *
     * @param js      js文本可以是路径
     * @param asExpr  是否作为表达式运行，为True时args无效
     * @param timeout js超时时间（秒），为null则使用页面timeouts.script设置
     * @param params  参数
     */
    public Object runJs(String js, Boolean asExpr, Double timeout, List<Object> params) {
        return ChromiumElement.runJs(this, js, asExpr, timeout == null ? this.timeouts.getScript() : timeout, params);
    }

    /**
     * 运行javascript代码
     *
     * @param js js文本可以是路径
     */
    public Object runJs(Path js) {
        return runJs(js, new ArrayList<>());
    }

    /**
     * 运行javascript代码
     *
     * @param js     js文本可以是路径
     * @param params 参数
     */
    public Object runJs(Path js, List<Object> params) {
        return runJs(js, null, params);
    }

    /**
     * 运行javascript代码
     *
     * @param js      js文本可以是路径
     * @param timeout js超时时间（秒），为null则使用页面timeouts.script设置
     * @param params  参数
     */
    public Object runJs(Path js, Double timeout, List<Object> params) {
        return runJs(js, false, timeout, params);
    }

    /**
     * 运行javascript代码
     *
     * @param js      js文本可以是路径
     * @param asExpr  是否作为表达式运行，为True时args无效
     * @param timeout js超时时间（秒），为null则使用页面timeouts.script设置
     * @param params  参数
     */
    public Object runJs(Path js, Boolean asExpr, Double timeout, List<Object> params) {
        timeout = timeout == null ? this.timeouts.getScript() : timeout;
        try {
            return ChromiumElement.runJs(this, js.toAbsolutePath().toString(), asExpr, timeout, params);
        } catch (IOError e) {
            return ChromiumElement.runJs(this, js.toString(), asExpr, timeout, params);
        }
    }

    /**
     * 运行javascript代码
     *
     * @param script js文本
     */
    public Object runJsLoaded(String script) {
        return runJsLoaded(script, new ArrayList<>());
    }

    /**
     * 运行javascript代码
     *
     * @param script js文本
     * @param params 参数
     */
    public Object runJsLoaded(String script, List<Object> params) {
        return runJsLoaded(script, null, params);
    }


    /**
     * 运行javascript代码
     *
     * @param script js文本
     * @param asExpr 是否作为表达式运行，为True时args无效
     * @param params 参数
     */
    public Object runJsLoaded(String script, Boolean asExpr, List<Object> params) {
        return runJsLoaded(script, asExpr, null, params);
    }

    /**
     * 运行javascript代码
     *
     * @param script  js文本
     * @param asExpr  是否作为表达式运行，为True时args无效
     * @param timeout js超时时间（秒），为null则使用页面timeouts.script设置
     * @param params  参数
     */
    public Object runJsLoaded(String script, Boolean asExpr, Double timeout, List<Object> params) {
        this.wait.docLoaded();
        return ChromiumElement.runJs(this, script, asExpr, timeout == null ? this.timeouts.getScript() : timeout, params);
    }


    /**
     * 运行javascript代码
     *
     * @param js js文本可以是路径
     */
    public Object runJsLoaded(Path js) {
        return runJsLoaded(js, new ArrayList<>());
    }

    /**
     * 运行javascript代码
     *
     * @param js     js文本可以是路径
     * @param params 参数
     */
    public Object runJsLoaded(Path js, List<Object> params) {
        return runJsLoaded(js, null, params);
    }

    /**
     * 运行javascript代码
     *
     * @param js      js文本可以是路径
     * @param timeout js超时时间（秒），为null则使用页面timeouts.script设置
     * @param params  参数
     */
    public Object runJsLoaded(Path js, Double timeout, List<Object> params) {
        return runJsLoaded(js, false, timeout, params);
    }

    /**
     * 运行javascript代码
     *
     * @param js      js文本可以是路径
     * @param asExpr  是否作为表达式运行，为True时args无效
     * @param timeout js超时时间（秒），为null则使用页面timeouts.script设置
     * @param params  参数
     */
    public Object runJsLoaded(Path js, Boolean asExpr, Double timeout, List<Object> params) {
        this.wait.docLoaded();
        timeout = timeout == null ? this.timeouts.getScript() : timeout;
        try {
            return ChromiumElement.runJs(this, js.toAbsolutePath().toString(), asExpr, timeout, params);
        } catch (IOError e) {
            return ChromiumElement.runJs(this, js.toString(), asExpr, timeout, params);
        }
    }

    /**
     * 以异步方式执行js代码
     *
     * @param script js文本
     */
    public void runAsyncJs(String script) {
        runAsyncJs(script, new ArrayList<>());
    }

    /**
     * 以异步方式执行js代码
     *
     * @param script js文本
     * @param params 参数
     */
    public void runAsyncJs(String script, List<Object> params) {
        runAsyncJs(script, false, params);
    }

    /**
     * 以异步方式执行js代码
     *
     * @param script js文本
     * @param asExpr 是否作为表达式运行，为True时args无效
     * @param params 参数
     */
    public void runAsyncJs(String script, boolean asExpr, List<Object> params) {
        runJs(script, asExpr, 0.0, params);
    }


    /**
     * 以异步方式执行js代码
     *
     * @param script js文本
     */
    public void runAsyncJs(Path script) {
        runAsyncJs(script, new ArrayList<>());
    }

    /**
     * 以异步方式执行js代码
     *
     * @param script js文本
     * @param params 参数
     */
    public void runAsyncJs(Path script, List<Object> params) {
        runAsyncJs(script, false, params);
    }

    /**
     * 以异步方式执行js代码
     *
     * @param script js文本
     * @param asExpr 是否作为表达式运行，为True时args无效
     * @param params 参数
     */
    public void runAsyncJs(Path script, boolean asExpr, List<Object> params) {
        runJs(script, asExpr, 0.0, params);
    }

    @Override
    public Boolean get(String url, boolean showErrMsg, Integer retry, Double interval, Double timeout, Map<String, Object> params) {
        BeforeConnect beforeConnect = this.beforeConnect(url, retry, interval);
        Boolean urlAvailable = this.dConnect(this.url, beforeConnect.getRetry(), beforeConnect.getInterval(), showErrMsg, timeout);
        this.setUrlAvailable(urlAvailable);
        return this.urlAvailable();
    }

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
    public List<Cookie> cookies(boolean asMap, boolean allDomains, boolean allInfo) {
        String txt = allDomains ? "Storage" : "Network";
        JSONArray objects = JSON.parseObject(this.runCdpLoaded(txt + ".getCookies").toString()).getJSONArray("cookies");
        if (asMap) {
            List<Cookie> mapList = new ArrayList<>();
            for (Object object : objects) {
                JSONObject jsonObject = JSON.parseObject(object.toString());
                Cookie.Builder builder = new Cookie.Builder();
                builder.name(jsonObject.getString("name"));
                builder.value(jsonObject.getString("value"));
                mapList.add(builder.build());
            }
            return mapList;
        } else if (allInfo) {
            List<Cookie> mapList = new ArrayList<>();
            for (Object object : objects) {
                JSONObject jsonObject = JSON.parseObject(object.toString());
                Cookie.Builder builder = new Cookie.Builder();
                builder.name(jsonObject.getString("name"));
                builder.value(jsonObject.getString("value"));
                builder.path(jsonObject.getString("path"));
                builder.domain(jsonObject.getString("domain"));
                Integer integer = jsonObject.getInteger("expiresAt");
                integer = integer == null ? jsonObject.getInteger("expiresat") : integer;
                builder.expiresAt(integer);
                mapList.add(builder.build());
            }
            return mapList;
        } else {
            List<Cookie> mapList = new ArrayList<>();
            for (Object object : objects) {
                JSONObject jsonObject = JSON.parseObject(object.toString());
                Cookie.Builder builder = new Cookie.Builder();
                builder.name(jsonObject.getString("name"));
                builder.value(jsonObject.getString("value"));
                builder.domain(jsonObject.getString("domain"));
                mapList.add(builder.build());
            }
            return mapList;
        }
    }

    /**
     * ChromiumElement对象组成的列表
     *
     * @param by 定位符或元素对象
     * @return ChromiumElement对象组成的列表
     */
    @Override
    public List<ChromiumElement> eles(By by) {
        return eles(by, null);
    }

    /**
     * ChromiumElement对象组成的列表
     *
     * @param by      定位符或元素对象
     * @param timeout 查找超时时间（秒）
     * @return ChromiumElement对象组成的列表
     */
    public List<ChromiumElement> eles(By by, Double timeout) {
        return super.eles(by, timeout);
    }

    /**
     * ChromiumElement对象组成的列表
     *
     * @param str 定位符或元素对象
     * @return ChromiumElement对象组成的列表
     */
    @Override
    public List<ChromiumElement> eles(String str) {
        return eles(str, null);
    }

    /**
     * ChromiumElement对象组成的列表
     *
     * @param str     定位符或元素对象
     * @param timeout 查找超时时间（秒）
     * @return ChromiumElement对象组成的列表
     */
    @Override
    public List<ChromiumElement> eles(String str, Double timeout) {
        return new ArrayList<>(super.eles(str, timeout));
    }

    /**
     * @param by 查询元素
     * @return SessionElement
     */
    @Override
    public SessionElement sEle(By by) {
        return this.sEle(by, 1);
    }

    /**
     * @param by    查询元素
     * @param index 获取第几个，从1开始，可传入负数获取倒数第几个
     * @return SessionElement
     */
    @Override
    public SessionElement sEle(By by, Integer index) {
        List<SessionElement> sessionElements = SessionElement.makeSessionEle(this, by, index);
        return sessionElements != null ? sessionElements.get(0) : null;
    }

    /**
     * @param str 定位符
     * @return SessionElement
     */
    @Override
    public SessionElement sEle(String str) {
        return this.sEle(str, 1);
    }

    /**
     * @param loc   定位符
     * @param index 获取第几个，从1开始，可传入负数获取倒数第几个
     * @return SessionElement
     */
    @Override
    public SessionElement sEle(String loc, Integer index) {
        List<SessionElement> sessionElements = SessionElement.makeSessionEle(this, loc, index);
        return sessionElements != null ? sessionElements.get(0) : null;
    }

    /**
     * @param by 查询元素
     * @return List<SessionElement>
     */
    @Override
    public List<SessionElement> sEles(By by) {
        return SessionElement.makeSessionEle(this, by, null);
    }

    /**
     * @param loc 定位符
     * @return List<SessionElement>
     */
    @Override
    public List<SessionElement> sEles(String loc) {
        return SessionElement.makeSessionEle(this, loc, null);
    }

    /**
     * 执行元素查找
     *
     * @param by       查询元素
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从0开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @return ChromiumElement对象或元素对象组成的列表
     */
    @Override
    protected List<ChromiumElement> findElements(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        return _findElement(Locator.getLoc(by), timeout, index);
    }

    /**
     * 执行元素查找
     *
     * @param loc      定位符
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从0开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @return ChromiumElement对象或元素对象组成的列表
     */
    @Override
    protected List<ChromiumElement> findElements(String loc, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        return _findElement(Locator.getLoc(loc), timeout, index);
    }

    private List<ChromiumElement> _findElement(By by, Double timeout, Integer index) {
        this.waits().docLoaded();
        timeout = timeout != null ? timeout : this.timeout();
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        List<String> searchIds = new ArrayList<>();
        timeout = timeout <= 0 ? 0.5 : timeout;
        JSONObject result = JSON.parseObject(this.driver.run("DOM.performSearch", Map.of("query", by.getValue(), "_timeout", timeout, "includeUserAgentShadowDOM", true)).toString());
        int num;
        if (result == null || result.toString().isEmpty() || result.containsKey(Browser.__ERROR__)) {
            num = 0;
        } else {
            num = result.getInteger("resultCount");
            searchIds.add(result.getString("searchId"));
        }
        List<ChromiumElement> list;
        while (true) {
            if (num > 0) {
                int fromIndex = 0;
                Integer indexArg = 0;
                int endIndex;
                if (index == null) {
                    endIndex = num;
                    indexArg = null;
                } else if (index < 0) {
                    fromIndex = index + num;
                    endIndex = fromIndex + 1;
                } else {
                    fromIndex = index - 1;
                    endIndex = fromIndex + 1;
                }
                if (fromIndex <= num - 1) {
                    if (result != null) {
                        JSONObject nIds = JSON.parseObject(this.driver.run("DOM.getSearchResults", Map.of("searchId", result.getString("searchId"), "fromIndex", fromIndex, "toIndex", endIndex)).toString());
                        if (!nIds.containsKey(Browser.__ERROR__)) {
                            list = ChromiumElement.makeChromiumEles(this, nIds.get("nodeIds"), indexArg, false);
                            if (list != null) {
                                break;
                            }
                        }
                    } else throw new IllegalArgumentException("缺少参数searchId");

                }
            }
            if (System.currentTimeMillis() >= endTime) return new ArrayList<>();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            timeout = (double) (endTime - System.currentTimeMillis());
            timeout = timeout <= 0 ? 0.5 : timeout;
            result = JSON.parseObject(this.driver.run("DOM.performSearch", Map.of("query", by.getValue(), "_timeout", timeout, "includeUserAgentShadowDOM", true)).toString());
            if (result != null && !result.containsKey(Browser.__ERROR__)) {
                num = result.getInteger("resultCount");
                searchIds.add(result.getString("searchId"));
            }
        }
        for (String searchId : searchIds) {
            this.driver.run("DOM.discardSearchResults", Map.of("searchId", searchId));
        }
        return new ArrayList<>(list);
    }

    /**
     * 刷新当前页面
     */
    public boolean refresh() {
        return refresh(false);
    }

    /**
     * 刷新当前页面
     *
     * @param ignoreCache 是否忽略缓存
     */
    public boolean refresh(boolean ignoreCache) {
        this.isLoading = true;
        this.runCdp("Page.reload", Map.of("ignoreCache", ignoreCache));
        return this.wait.loadStart();
    }

    /**
     * 在浏览历史中前进1
     */
    public void forward() {
        forward(1);
    }

    /**
     * 在浏览历史中前进若干步
     *
     * @param steps 前进步数
     */
    public void forward(int steps) {
        this.forwardOrBack(steps);
    }

    /**
     * 在浏览历史中后退1
     */
    public void back() {
        back(1);
    }

    /**
     * 在浏览历史中后退若干步
     *
     * @param steps 后退步数
     */
    public void back(int steps) {
        this.forwardOrBack(-steps);
    }

    /**
     * 执行浏览器前进或后退，会跳过url相同的历史记录
     *
     * @param steps 步数
     */
    private void forwardOrBack(int steps) {
        if (steps == 0) return;
        JSONObject history = JSON.parseObject(this.runCdp("Page.getNavigationHistory").toString());
        Integer index = history.getInteger("currentIndex");
        history = history.getJSONObject("entries");
        int direction = steps > 0 ? 1 : -1;
        Object currUrl = history.getJSONObject(index + "").get("url");
        Object nid = null;
        for (int num = 0; num < Math.abs(steps); num++) {
            for (int i = index; i < history.size() && i >= 0; i += direction) {
                index += direction;
                JSONObject entry = history.getJSONObject(i + "");
                if (!Objects.equals(entry.get("url"), currUrl)) {
                    nid = entry.get("id");
                    currUrl = entry.get("url");
                    break;
                }
            }
        }
        if (nid != null) {
            this.isLoading = true;
            this.runCdp("Page.navigateToHistoryEntry", Map.of("entryId", nid));
        }

    }

    /**
     * 页面停止加载
     */
    public void stopLoading() {
        try {
            this.runCdp("Page.stopLoading");
        } catch (PageDisconnectedError | CDPError ignored) {

        }
        long endTime = (long) (System.currentTimeMillis() + this.timeouts.getPageLoad());
        while (!Objects.equals(this.readyState, "complete") && System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 从页面上删除一个元素
     *
     * @param by 元素对象或定位符
     */
    public void removeEle(By by) {
        if (by == null) return;
        List<ChromiumElement> list = this._ele(by, null, null, null, false, null);
        if (!list.isEmpty()) this.runCdp("DOM.removeNode", Map.of("nodeId", list.get(0).getNodeId()));
    }

    /**
     * 从页面上删除一个元素
     *
     * @param loc 元素对象或定位符
     */
    public void removeEle(String loc) {
        if (loc == null || loc.isEmpty()) return;
        List<ChromiumElement> list = this._ele(loc, null, null, null, false, null);
        if (!list.isEmpty()) this.runCdp("DOM.removeNode", Map.of("nodeId", list.get(0).getNodeId()));
    }

    /**
     * 新建一个元素
     *
     * @param outerHTML 新元素的html文本
     * @return 元素对象
     */
    public ChromiumElement addEle(String outerHTML) {
        return addEle(outerHTML, By.NULL());
    }

    /**
     * 新建一个元素
     *
     * @param outerHTML 新元素的html文本
     * @param insertTo  插入到哪个元素中，可接收元素对象和定位符，为null添加到body
     * @return 元素对象
     */
    public ChromiumElement addEle(String outerHTML, By insertTo) {
        return addEle(outerHTML, this.ele(insertTo), null);
    }

    /**
     * 新建一个元素
     *
     * @param outerHTML 新元素的html文本
     * @param insertTo  插入到哪个元素中，可接收元素对象和定位符，为null添加到body
     * @param before    在哪个子节点前面插入，可接收对象和定位符，为null插入到父元素末尾
     * @return 元素对象
     */
    public ChromiumElement addEle(String outerHTML, By insertTo, By before) {
        return addEle(outerHTML, this.ele(insertTo), this.ele(before));
    }

    /**
     * 新建一个元素
     *
     * @param outerHTML 新元素的html文本
     * @param insertTo  插入到哪个元素中，可接收元素对象和定位符，为null添加到body
     * @return 元素对象
     */
    public ChromiumElement addEle(String outerHTML, String insertTo) {
        return addEle(outerHTML, this.ele(insertTo), null);
    }

    /**
     * 新建一个元素
     *
     * @param outerHTML 新元素的html文本
     * @param insertTo  插入到哪个元素中，可接收元素对象和定位符，为null添加到body
     * @param before    在哪个子节点前面插入，可接收对象和定位符，为null插入到父元素末尾
     * @return 元素对象
     */
    public ChromiumElement addEle(String outerHTML, String insertTo, String before) {
        return addEle(outerHTML, this.ele(insertTo), this.ele(before));
    }


    /**
     * 新建一个元素
     *
     * @param outerHTML 新元素的html文本
     * @param insertTo  插入到哪个元素中，可接收元素对象和定位符，为null添加到body
     * @return 元素对象
     */
    public ChromiumElement addEle(String outerHTML, ChromiumElement insertTo) {
        return addEle(outerHTML, insertTo, null);
    }

    /**
     * 新建一个元素
     *
     * @param outerHTML 新元素的html文本
     * @param insertTo  插入到哪个元素中，可接收元素对象和定位符，为null添加到body
     * @param before    在哪个子节点前面插入，可接收对象和定位符，为null插入到父元素末尾
     * @return 元素对象
     */
    public ChromiumElement addEle(String outerHTML, ChromiumElement insertTo, ChromiumElement before) {
        String string = insertTo == null ? this.ele("t:body").toString() : insertTo.toString();
        List<Object> args = new ArrayList<>();
        args.add(outerHTML);
        args.add(string);
        String js;
        if (before != null) {
            args.add(before.toString());
            js = "ele = document.createElement(null);\n" + "arguments[1].insertBefore(ele, arguments[2]);\n" + "ele.outerHTML = arguments[0];\n" + "return arguments[2].previousElementSibling;";
        } else {
            js = "ele = document.createElement(null);\n" + "arguments[1].appendChild(ele);\n" + "ele.outerHTML = arguments[0];\n" + "return arguments[1].lastElementChild;";
        }
        return (ChromiumElement) this.runJs(js, args);
    }

    /**
     * 获取页面中一个frame对象
     *
     * @param loc 定位符、iframe序号、ChromiumFrame对象，序号从1开始，可传入负数获取倒数第几个
     * @return ChromiumFrame对象
     */
    public ChromiumFrame getFrame(String loc) {
        return getFrame(loc, null);
    }

    /**
     * 获取页面中一个frame对象
     *
     * @param loc     定位符、iframe序号、ChromiumFrame对象，序号从1开始，可传入负数获取倒数第几个
     * @param timeout 查找元素超时时间（秒）
     * @return ChromiumFrame对象
     */
    public ChromiumFrame getFrame(String loc, Double timeout) {
        String xpath = !Locator.isLoc(loc) ? "xpath://*[(name()=\"iframe\" or name()=\"frame\") and (@name=\"" + loc + "\" or @id=\"" + loc + "\")]" : loc;
        return getFrame(this._ele(xpath, timeout, null, null, null, null));
    }

    /**
     * 获取页面中一个frame对象
     *
     * @param by 定位符、iframe序号、ChromiumFrame对象，序号从1开始，可传入负数获取倒数第几个
     * @return ChromiumFrame对象
     */
    public ChromiumFrame getFrame(By by) {
        return getFrame(by, null);
    }

    /**
     * 获取页面中一个frame对象
     *
     * @param by      定位符、iframe序号、ChromiumFrame对象，序号从1开始，可传入负数获取倒数第几个
     * @param timeout 查找元素超时时间（秒）
     * @return ChromiumFrame对象
     */
    public ChromiumFrame getFrame(By by, Double timeout) {
        return getFrame(this._ele(by, timeout, null, null, null, null));
    }

    /**
     * 获取页面中一个frame对象
     *
     * @return ChromiumFrame对象
     */
    public ChromiumFrame getFrame() {
        return getFrame(1);
    }

    /**
     * 获取页面中一个frame对象
     *
     * @param index 定位符、iframe序号、ChromiumFrame对象，序号从1开始，可传入负数获取倒数第几个
     * @return ChromiumFrame对象
     */
    public ChromiumFrame getFrame(int index) {
        return getFrame(index, null);
    }

    /**
     * 获取页面中一个frame对象
     *
     * @param index   定位符、iframe序号、ChromiumFrame对象，序号从1开始，可传入负数获取倒数第几个
     * @param timeout 查找元素超时时间（秒）
     * @return ChromiumFrame对象
     */
    public ChromiumFrame getFrame(int index, Double timeout) {
        String str = null;
        if (index == 0) index = 1;
        else if (index < 0) str = "last()+" + index + "+1";
        str = "xpath:(//*[name()=\"frame\" or name()=\"iframe\"])[" + (str == null ? index : str) + "]";
        return this.getFrame(this._ele(str, timeout, null, null, null, null));
    }

    @Nullable
    private ChromiumFrame getFrame(List<ChromiumElement> chromiumElements) {
        ChromiumFrame frame = null;
        if (!chromiumElements.isEmpty()) {
            try {
                ChromiumElement chromiumElement = chromiumElements.get(0);
                if (chromiumElement.getType().equals("ChromiumFrame")) {
                    return (ChromiumFrame) (BaseParser) chromiumElement;
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("无法转换成frame元素");
            }
        }
        return frame;
    }

    /**
     * 获取所有符合条件的frame对象
     *
     * @return ChromiumFrame对象组成的列表
     */
    public List<ChromiumFrame> getFrames() {
        return getFrames("");
    }

    /**
     * 获取所有符合条件的frame对象
     *
     * @param loc 定位符，为null时返回所有
     * @return ChromiumFrame对象组成的列表
     */
    public List<ChromiumFrame> getFrames(String loc) {
        return getFrames(loc == null || loc.isEmpty() ? null : loc, null);
    }

    /**
     * 获取所有符合条件的frame对象
     *
     * @param loc     定位符，为null时返回所有
     * @param timeout 查找超时时间（秒）
     * @return ChromiumFrame对象组成的列表
     */
    public List<ChromiumFrame> getFrames(String loc, Double timeout) {
        loc = loc == null ? "xpath://*[name()=\"iframe\" or name()=\"frame\"]" : loc;
        return _getFrames(this._ele(loc, timeout, null, null, false, null));
    }

    /**
     * 获取所有符合条件的frame对象
     *
     * @param by 定位符，为null时返回所有
     * @return ChromiumFrame对象组成的列表
     */
    public List<ChromiumFrame> getFrames(By by) {
        return getFrames(by, null);
    }

    /**
     * 获取所有符合条件的frame对象
     *
     * @param by      定位符，为null时返回所有
     * @param timeout 查找超时时间（秒）
     * @return ChromiumFrame对象组成的列表
     */
    public List<ChromiumFrame> getFrames(By by, Double timeout) {
        return _getFrames(this._ele(by, timeout, null, null, false, null));
    }

    @NotNull
    private List<ChromiumFrame> _getFrames(List<ChromiumElement> chromiumElements) {
        List<ChromiumFrame> frames = new ArrayList<>();
        chromiumElements.forEach(baseParser -> {
            try {
                if (baseParser.getType().equals("ChromiumFrame")) {
                    frames.add((ChromiumFrame) (BaseParser) baseParser);
                }
            } catch (Exception ignored) {
            }
        });
        return frames;
    }

    /**
     * 获取sessionStorage信息，不设置item则获取全部
     *
     * @return sessionStorage一个或所有项内容
     */
    public Object sessionStorage() {
        return sessionStorage(null);
    }

    /**
     * 获取sessionStorage信息，不设置item则获取全部
     *
     * @param item 要获取的项，不设置则返回全部
     * @return sessionStorage一个或所有项内容
     */
    public Object sessionStorage(String item) {
        if (item != null && !item.trim().isEmpty())
            return this.runJsLoaded("sessionStorage.getItem(\"" + item.trim() + "\");");
        else {
            String js = "var dp_ls_len = sessionStorage.length;\n" + "var dp_ls_arr = new Array();\n" + "for(var i = 0; i < dp_ls_len; i++) {\n" + "   var getKey = sessionStorage.key(i);\n" + "   var getVal = sessionStorage.getItem(getKey);\n" + "   dp_ls_arr[i] = {'key': getKey, 'val': getVal}\n" + "}\n" + "return dp_ls_arr;";
            return JSON.parseArray(this.runJsLoaded(js).toString()).stream().map(o -> JSON.parseObject(o.toString())).collect(Collectors.toMap(jsonObject -> jsonObject.getString("key"), jsonObject -> jsonObject.get("val"), (a, b) -> b));
        }
    }

    /**
     * 获取localStorage信息，不设置item则获取全部
     *
     * @return localStorage一个或所有项内容
     */
    public Object localStorage() {
        return localStorage(null);
    }

    /**
     * 获取localStorage信息，不设置item则获取全部
     *
     * @param item 要获取的项，不设置则返回全部
     * @return localStorage一个或所有项内容
     */
    public Object localStorage(String item) {
        if (item != null && !item.trim().isEmpty())
            return this.runJsLoaded("localStorage.getItem(\"" + item.trim() + "\");");
        else {
            String js = "var dp_ls_len = localStorage.length;\n" + "var dp_ls_arr = new Array();\n" + "for(var i = 0; i < dp_ls_len; i++) {\n" + "    var getKey = localStorage.key(i);\n" + "    var getVal = localStorage.getItem(getKey);\n" + "    dp_ls_arr[i] = {'key': getKey, 'val': getVal}\n" + "}\n" + "return dp_ls_arr;";
            return JSON.parseArray(this.runJsLoaded(js).toString()).stream().map(o -> JSON.parseObject(o.toString())).collect(Collectors.toMap(jsonObject -> jsonObject.getString("key"), jsonObject -> jsonObject.get("val"), (a, b) -> b));
        }
    }

    /**
     * 对页面进行截图，可对整个网页、可见网页、指定范围截图。对可视范围外截图需要90以上版本浏览器支持
     *
     * @param path     保存路径
     * @param name     完整文件名，后缀可选 'jpg','jpeg','png','webp'
     * @param asBytes  是否以字节形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数和as_base64参数无效
     * @param asBase64 是否以base64字符串形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数无效
     * @param fullPage 是否整页截图，为True截取整个网页，为False截取可视窗口
     * @param leftTop  截取范围左上角坐标
     * @param rightTop 截取范围右下角角坐标
     * @return 图片完整路径或字节文本
     */
    public Object getScreenshot(String path, String name, PicType asBytes, PicType asBase64, boolean fullPage, Coordinate leftTop, Coordinate rightTop) {
        return this._getScreenshot(path, name, asBytes, asBase64, fullPage, leftTop, rightTop, null);
    }

    /**
     * 添加初始化脚本，在页面加载任何脚本前执行
     *
     * @param script js文本
     * @return 添加的脚本的id
     */
    public String addInitJs(String script) {
        if (script == null || script.isEmpty()) return null;
        String o = JSON.parseObject(this.runCdp("Page.addScriptToEvaluateOnNewDocument", Map.of("source", script, "includeCommandLineAPI", true)).toString()).getString("identifier");
        this.initJss.add(o);
        return o;
    }

    /**
     * 删除初始化脚本，jsId传入null时删除所有
     *
     * @param scriptId 脚本的id
     */
    public void removeInitJs(String scriptId) {
        if (scriptId == null || scriptId.isEmpty()) {
            this.initJss.forEach(id -> this.runCdp("Page.removeScriptToEvaluateOnNewDocument", Map.of("identifier", id)));
            this.initJss.clear();
        } else {
            for (String id : this.initJss)
                if (id.equals(scriptId))
                    this.runCdp("Page.removeScriptToEvaluateOnNewDocument", Map.of("identifier", scriptId));
            this.initJss.remove(scriptId);
        }
    }

    /**
     * 清除缓存，可选要清除的项
     */
    public void clearCache() {
        clearCache(true);
    }

    /**
     * 清除缓存，可选要清除的项
     *
     * @param cache 是否清除cache
     */
    public void clearCache(boolean cache) {
        clearCache(cache, true);
    }

    /**
     * 清除缓存，可选要清除的项
     *
     * @param cache   是否清除cache
     * @param cookies 是否清除cookies
     */
    public void clearCache(boolean cache, boolean cookies) {
        clearCache(true, cache, cookies);
    }

    /**
     * 清除缓存，可选要清除的项
     *
     * @param localStorage 是否清除localStorage
     * @param cache        是否清除cache
     * @param cookies      是否清除cookies
     */
    public void clearCache(boolean localStorage, boolean cache, boolean cookies) {
        clearCache(true, localStorage, cache, cookies);
    }

    /**
     * 清除缓存，可选要清除的项
     *
     * @param sessionStorage 是否清除sessionStorage
     * @param localStorage   是否清除localStorage
     * @param cache          是否清除cache
     * @param cookies        是否清除cookies
     */
    public void clearCache(boolean sessionStorage, boolean localStorage, boolean cache, boolean cookies) {
        if (sessionStorage || localStorage) {
            this.runCdpLoaded("DOMStorage.enable");
            Object i = JSON.parseObject(this.runCdp("Storage.getStorageKeyForFrame", Map.of("frameId", this.frameId)).toString()).get("storageKey");
            if (sessionStorage)
                this.runCdp("DOMStorage.clear", Map.of("storageId", Map.of("storageKey", i, "isLocalStorage", false)));
            if (localStorage)
                this.runCdp("DOMStorage.clear", Map.of("storageId", Map.of("storageKey", i, "isLocalStorage", true)));
            this.runCdpLoaded("DOMStorage.disable");
        }
        if (cache) this.runCdpLoaded("Network.clearBrowserCache");
        if (cookies) this.runCdpLoaded("Network.clearBrowserCookies");
    }

    /**
     * 断开与页面的连接，不关闭页面
     */
    public void stop() {
        this.disconnect();
    }

    /**
     * 断开与页面的连接，不关闭页面
     */
    public void disconnect() {
        if (this.driver != null) this.browser.stopDiver(this.driver);
    }


    /**
     * 断开与页面原来的页面，重新建立连接
     */
    public void reconnect() {
        reconnect(0);
    }

    /**
     * 断开与页面原来的页面，重新建立连接
     *
     * @param wait 断开后等待若干秒再连接
     */
    public void reconnect(int wait) {
        String s = this.targetId();
        this.disconnect();
        if (wait > 0) try {
            Thread.sleep(wait * 1000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        this.browser.reconnect();
        this.driver = this.browser.getDriver(s, this);
    }

    /**
     * 处理提示框，可以自动等待提示框出现
     *
     * @return 提示框内容文本，未等到提示框则返回null
     */
    public String handleAlert() {
        return handleAlert(true);
    }

    /**
     * 处理提示框，可以自动等待提示框出现
     *
     * @param send 处理prompt提示框时可输入文本
     * @return 提示框内容文本，未等到提示框则返回null
     */
    public String handleAlert(String send) {
        return handleAlert(true, send);
    }

    /**
     * 处理提示框，可以自动等待提示框出现
     *
     * @param accept True表示确认，False表示取消，其它值不会按按钮但依然返回文本值
     * @return 提示框内容文本，未等到提示框则返回null
     */
    public String handleAlert(boolean accept) {
        return handleAlert(accept, null);
    }

    /**
     * 处理提示框，可以自动等待提示框出现
     *
     * @param accept True表示确认，False表示取消，其它值不会按按钮但依然返回文本值
     * @param send   处理prompt提示框时可输入文本
     * @return 提示框内容文本，未等到提示框则返回null
     */
    public String handleAlert(boolean accept, String send) {
        return handleAlert(accept, send, null);
    }

    /**
     * 处理提示框，可以自动等待提示框出现
     *
     * @param accept  True表示确认，False表示取消，其它值不会按按钮但依然返回文本值
     * @param send    处理prompt提示框时可输入文本
     * @param timeout 等待提示框出现的超时时间（秒），为null则使用self.timeout属性的值
     * @return 提示框内容文本，未等到提示框则返回null
     */
    public String handleAlert(boolean accept, String send, Double timeout) {
        return handleAlert(accept, send, timeout, false);
    }

    /**
     * 处理提示框，可以自动等待提示框出现
     *
     * @param accept  True表示确认，False表示取消，其它值不会按按钮但依然返回文本值
     * @param send    处理prompt提示框时可输入文本
     * @param timeout 等待提示框出现的超时时间（秒），为null则使用self.timeout属性的值
     * @param nextOne 是否处理下一个出现的提示框，为True时timeout参数无效
     * @return 提示框内容文本，未等到提示框则返回null
     */
    public String handleAlert(boolean accept, String send, Double timeout, boolean nextOne) {
        String s = this._handleAlert(accept, send, timeout, nextOne);
        while (this.hasAlert) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return s;
    }

    /**
     * 处理提示框，可以自动等待提示框出现
     *
     * @param accept  True表示确认，False表示取消，其它值不会按按钮但依然返回文本值
     * @param send    处理prompt提示框时可输入文本
     * @param timeout 等待提示框出现的超时时间（秒），为null则使用self.timeout属性的值
     * @param nextOne 是否处理下一个出现的提示框，为True时timeout参数无效
     * @return 提示框内容文本，未等到提示框则返回null
     */
    private String _handleAlert(boolean accept, String send, Double timeout, boolean nextOne) {
        if (nextOne) {
            this.alert.setHandleNext(accept);
            this.alert.setNextText(send);
            return null;
        }
        timeout = timeout == null ? this.timeout() : timeout;
        timeout = timeout <= 0 ? 0.1 : timeout;
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        while (!this.alert.getActivated() && System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (!this.alert.getActivated()) {
            return null;
        }
        String resText = this.alert.getText();
        HashMap<String, Object> map = new HashMap<>();
        map.put("accept", accept);
        map.put("_timeout", 0);
        if (Objects.equals(this.alert.getType(), "prompt") && send != null && !send.isEmpty())
            map.put("promptText", send);
        this.driver.run("Page.handleJavaScriptDialog", map);
        return resText;
    }

    /**
     * alert出现时触发的方法
     */
    private void onAlterOpen(Object message) {
        JSONObject jsonObject = JSON.parseObject(message.toString());
        this.alert.setActivated(true);
        this.alert.setText(jsonObject.getString("message"));
        this.alert.setType(jsonObject.getString("message"));
        this.alert.setDefaultPrompt(jsonObject.getString("defaultPrompt"));
        this.alert.setResponseAccept(null);
        this.alert.setResponseText(null);
        this.hasAlert = true;
        if (this.alert.getAuto() != null) {
            this.handleAlert(this.alert.getAuto(), null, null, false);
        } else if (this.alert.getHandleNext() != null) {
            this.handleAlert(this.alert.getHandleNext(), this.alert.getNextText(), null, false);
            this.alert.setHandleNext(null);
        }
    }

    /**
     * alert关闭时触发的方法
     */
    private void onAlertClose(Object params) {
        JSONObject jsonObject = JSON.parseObject(params.toString());
        this.alert.setActivated(false);
        this.alert.setText(null);
        this.alert.setType(null);
        this.alert.setDefaultPrompt(null);
        this.alert.setResponseAccept(jsonObject.getString("result"));
        this.alert.setResponseText(jsonObject.getString("userInput"));
        this.hasAlert = false;
    }

    /**
     * 待页面加载完成，超时触发停止加载
     *
     * @return 是否成功，超时返回False
     */
    protected boolean waitLoaded() {
        return waitLoaded(null);
    }

    /**
     * 待页面加载完成，超时触发停止加载
     *
     * @param timeout 超时时间（秒）
     * @return 是否成功，超时返回False
     */
    protected boolean waitLoaded(Double timeout) {
        timeout = timeout == null ? this.timeouts.getPageLoad() : timeout;
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        while (System.currentTimeMillis() < endTime) {
            if (Objects.equals(this.readyState, "complete")) {
                return true;
            } else if (Objects.equals(this.loadMode, "eager") && Objects.equals(this.readyState, "interactive") && !this.isLoading) {
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            this.stopLoading();
        } catch (CDPError ignored) {

        }
        return false;
    }


    /**
     * 尝试连接，重试若干次
     *
     * @param toUrl      要访问的url
     * @param times      重试次数
     * @param interval   重试间隔（秒）
     * @param showErrMsg 是否抛出异常
     * @param timeout    连接超时时间（秒）
     * @return 是否成功，返回null表示不确定
     */
    private Boolean dConnect(String toUrl, int times, double interval, boolean showErrMsg, Double timeout) {
        Exception err = null;
        this.isLoading = true;
        timeout = timeout != null ? timeout : this.timeouts.getPageLoad();
        for (int i = 0; i < times + 1; i++) {
            err = null;
            long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
            try {
                String string = this.runCdp("Page.navigate", Map.of("frameId", this.frameId, "url", toUrl, "_timeout", timeout)).toString();
                JSONObject result = JSON.parseObject(string);
                if (result.containsKey("errorText")) err = new ConnectException(result.get("errorText").toString());
            } catch (Exception e) {
                e.printStackTrace();
                err = new TimeoutException("页面连接超时（等待" + timeout + "秒）。");
            }
            if (err != null) {
                if (i < times) {
                    try {
                        Thread.sleep((long) (interval * 1000));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (showErrMsg) System.out.println("重试" + (i + 1) + toUrl);
                }
                long endTime1 = (long) (System.currentTimeMillis() + timeout * 1000);
                while ((!Objects.equals(this.readyState, "loading") || !Objects.equals(this.readyState, "complete")) && System.currentTimeMillis() < endTime1) {// 等待出错信息显示
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                this.stopLoading();
                continue;
            }
            if (Objects.equals(this.loadMode, null) || Objects.equals(this.loadMode, "null")) return true;
            double yu = endTime - System.currentTimeMillis();
            boolean ok = this.waitLoaded(yu <= 0 ? 1 : yu / 1000);
            if (!ok) {
                err = new TimeoutException("页面连接超时（等待" + timeout + "秒）。");
                if (i < times) {
                    try {
                        Thread.sleep((long) (interval * 1000));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    if (showErrMsg) System.out.println("重试" + (i + 1) + toUrl);
                }
                continue;
            }
            break;
        }
        if (err != null) if (showErrMsg) throw new RuntimeException(new ConnectException("连接异常。"));
        else return false;
        return true;
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
        Coordinate size = this.rect.size();
        Map<String, Object> vp;
        Object png;
        if (fullPage) {
            vp = Map.of("x", 0, "y", 0, "width", size.getX(), "height", size.getY(), "scale", 1);
            png = JSONObject.parseObject(this.runCdpLoaded("Page.captureScreenshot", Map.of("format", picType, "captureBeyondViewport", true, "clip", vp)).toString()).get("data");
        } else {
            if (leftTop != null && rightTop != null) {
                int x = leftTop.getX();
                int y = leftTop.getY();
                int w = rightTop.getX() - x;
                int h = rightTop.getY() - y;
                boolean v = !Web.locationInViewport(this, x, y) && Web.locationInViewport(this, rightTop.getX(), rightTop.getY());
                if (v && Boolean.parseBoolean(this.runJs("return document.body.scrollHeight > window.innerHeight;").toString()) && !Boolean.parseBoolean(this.runJs("return document.body.scrollWidth > window.innerWidth;").toString())) {
                    x += 10;
                }
                vp = Map.of("x", x, "y", y, "width", w, "height", h, "scale", 1);
                png = JSONObject.parseObject(this.runCdpLoaded("Page.captureScreenshot", Map.of("format", picType, "captureBeyondViewport", v, "clip", vp)).toString()).get("data");
            } else {
                png = JSONObject.parseObject(this.runCdpLoaded("Page.captureScreenshot", Map.of("format", picType)).toString()).get("data");
            }
        }
        if (asBase64 != null) return png;
        byte[] decodedBytes = Base64.getDecoder().decode(png.toString());
        if (asBytes != null) return decodedBytes;
        try {
            Path file = new File(path).toPath();
            // 创建父目录（如果不存在）
            Files.createDirectories(file.getParent());
            // 写入文件
            Files.write(file, decodedBytes, StandardOpenOption.CREATE);
            // 返回文件的绝对路径
            return file.toAbsolutePath().toString();
        } catch (IOException e) {
            e.printStackTrace(); // 处理异常，例如文件写入失败
            return null;
        }
    }

    public static void closePrivacyDialog(ChromiumBase page, String tabId) {
        try {
            Driver driver = page.browser().getDriver(tabId);
            driver.run("Runtime.enable");
            driver.run("DOM.enable");
            driver.run("DOM.getDocument");
            Object sid = JSON.parseObject(driver.run("DOM.performSearch", Map.of("query", "//*[name()=\"privacy-sandbox-notice-dialog-app\"]", "includeUserAgentShadowDOM", true)).toString()).get("searchId");
            JSONObject jsonObject = JSON.parseObject(driver.run("DOM.getSearchResults", Map.of("searchId", sid, "fromIndex", 0, "toIndex", 1)).toString());
            Object r;
            try {
                r = jsonObject.getJSONArray("nodeIds").get(0);
            } catch (Exception e) {
                r = jsonObject.get("nodeIds");
            }
            long endTime = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < endTime) {
                try {
                    r = JSON.parseObject(JSON.parseObject(driver.run("DOM.describeNode", Map.of("nodeId", r)).toString()).getJSONObject("node").getJSONArray("shadowRoots").get(0).toString()).get("backendNodeId");
                    break;
                } catch (Exception ignored) {
                }
            }

            driver.run("DOM.discardSearchResults", Map.of("searchId", sid));
            r = JSON.parseObject(driver.run("DOM.resolveNode", Map.of("backendNodeId", r)).toString()).getJSONObject("object").get("objectId");
            r = JSON.parseObject(driver.run("Runtime.callFunctionOn", Map.of("objectId", r, "functionDeclaration", "function(){return this.getElementById(\"ackButton\");}")).toString()).getJSONObject("result").get("objectId");
            driver.run("Runtime.callFunctionOn", Map.of("objectId", r, "functionDeclaration", "function(){return this.click();}"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 把当前页面保存为mhtml文件，如果path和name参数都为null，只返回mhtml文本
     *
     * @param page 要保存的页面对象
     * @param path 保存路径，为null且name不为null时保存在当前路径
     * @param name 文件名，为null且path不为null时用title属性值
     * @return mhtml文本
     */
    public static String getMHtml(ChromiumBase page, String path, String name) {
        String string = JSON.parseObject(page.runCdp("Page.captureSnapshot").toString()).getString("data");
        if (path == null && name == null) return string;
        path = path == null ? "." : path;
        Paths.get(path).toFile().mkdirs();
        name = name == null ? page.title() : name;
        name = com.ll.DataRecorder.Tools.makeValidName(name);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(path + FileSystems.getDefault().getSeparator() + name + ".mhtml"), StandardCharsets.UTF_8)) {
            writer.write(string);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return string;
    }

    /**
     * 把当前页面保存为pdf文件，如果path和name参数都为null，只返回字节
     *
     * @param page   要保存的页面对象
     * @param path   保存路径，为null且name不为null时保存在当前路径
     * @param name   文件名，为null且path不为null时用title属性值
     * @param params 参数
     * @return pdf文本
     */
    public static Object getPdf(ChromiumBase page, String path, String name, Map<String, Object> params) {
        params = params == null ? new HashMap<>() : new HashMap<>(params);
        params.put("transferMode", "ReturnAsBase64");
        if (!params.toString().contains("printBackground")) params.put("printBackground", true);
        Object data;
        try {
            data = JSON.parseObject(page.runCdp("Page.printToPDF", params).toString()).get("data");
        } catch (Exception e) {
            throw new RuntimeException("保存失败，可能浏览器版本不支持。");
        }

        // 使用 Java 的 Base64 解码
        byte[] decodedBytes = java.util.Base64.getDecoder().decode(String.valueOf(data));
        // 如果需要返回字节数组，则直接返回
        if (path == null && name == null) return decodedBytes;
        path = path == null ? "." : path;
        try {
            // 创建父目录（如果不存在）
            Paths.get(path).toFile().mkdirs();
            name = com.ll.DataRecorder.Tools.makeValidName(name);
            // 写入文件
            Files.write(Paths.get(path + FileSystems.getDefault().getSeparator() + name + ".pdf"), decodedBytes, StandardOpenOption.CREATE);
        } catch (Exception e) {
            // 处理异常，例如文件写入失败
            e.printStackTrace();
            return null;
        }
        return decodedBytes;
    }

}
