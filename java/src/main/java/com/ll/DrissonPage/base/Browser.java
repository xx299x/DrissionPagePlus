package com.ll.DrissonPage.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.config.ChromiumOptions;
import com.ll.DrissonPage.error.extend.PageDisconnectedError;
import com.ll.DrissonPage.functions.Tools;
import com.ll.DrissonPage.page.ChromiumPage;
import com.ll.DrissonPage.page.ChromiumBase;
import com.ll.DrissonPage.units.downloader.DownloadManager;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 浏览器
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */

public class Browser implements Occupant {
    public static final String __ERROR__ = "error";
    private static final Map<String, Browser> BROWSERS = new ConcurrentHashMap<>();
    @Getter
    private final ChromiumBase page;
    private final Map<String, Driver> drivers;
    private final Map<String, Set<Driver>> allDrivers;
    @Getter
    private BrowserDriver driver;
    @Getter
    private String id;
    @Getter
    private String address;
    @Getter
    private Map<String, String> frames;
    @Getter
    private DownloadManager dlMgr;
    /**
     * 浏览器进程id
     */
    @Getter
    private Integer processId;
    private boolean connected;

    /**
     * 浏览器
     *
     * @param address   浏览器地址
     * @param browserId 浏览器id
     * @param page      ChromiumPage对象
     */
    private Browser(String address, String browserId, ChromiumBase page) {
        this.page = page;
        this.address = address;
        this.driver = BrowserDriver.getInstance(browserId, "browser", address, this);
        this.id = browserId;
        this.frames = new HashMap<>();
        this.drivers = new HashMap<>();
        this.allDrivers = new HashMap<>();
        this.connected = false;
        this.processId = null;
        JSONArray processInfoArray = JSON.parseObject(runCdp("SystemInfo.getProcessInfo")).getJSONArray("processInfo");
        if (processInfoArray == null) processInfoArray = new JSONArray();
        for (Object processInfoObject : processInfoArray) {
            JSONObject processInfo = JSON.parseObject(processInfoObject.toString());
            if ("browser".equals(processInfo.getString("type"))) {
                this.processId = processInfo.getInteger("id");
                break;
            }
        }
        this.runCdp("Target.setDiscoverTargets", Map.of("discover", true));
        driver.setCallback("Target.targetDestroyed", new MyRunnable() {
            @Override
            public void run() {
                onTargetDestroyed(getMessage());
            }
        });
        driver.setCallback("Target.targetCreated", new MyRunnable() {
            @Override
            public void run() {
                onTargetCreated(getMessage());
            }
        });
    }

    /**
     * 单例模式
     *
     * @param address   浏览器地址
     * @param browserId 浏览器id
     * @param page      ChromiumPage对象
     */
    public static Browser getInstance(String address, String browserId, ChromiumBase page) {
        return BROWSERS.computeIfAbsent(browserId, key -> new Browser(address, browserId, page));
    }

    /**
     * 获取对应tab id的Driver
     *
     * @param tabId 标签页id
     * @return Driver对象
     */
    public Driver getDriver(String tabId) {
        return getDriver(tabId, null);
    }

    /**
     * 获取对应tab id的Driver
     *
     * @param tabId    标签页id
     * @param occupant 使用该驱动的对象
     * @return Driver对象
     */
    public Driver getDriver(String tabId, Occupant occupant) {
        Driver driver = Objects.requireNonNullElseGet(drivers.remove(tabId), () -> new Driver(tabId, "page", this.getAddress(), occupant));
        HashSet<Driver> value = new HashSet<>();
        value.add(driver);
        this.allDrivers.put(tabId, value);
        return driver;
    }

    /**
     * 标签页创建时执行
     *
     * @param message 回调参数
     */
    private void onTargetCreated(Object message) {
        try {
            JSONObject jsonObject = JSON.parseObject(message.toString());
            String type = jsonObject.getJSONObject("targetInfo").getString("type");
            if ("page".equals(type) || "webview".equals(type) && !jsonObject.getJSONObject("targetInfo").getString("url").startsWith("devtools://")) {
                String targetId = jsonObject.getJSONObject("targetInfo").getString("targetId");
                Driver driver = new Driver(targetId, "page", address);
                drivers.put(targetId, driver);
                this.allDrivers.computeIfAbsent(targetId, k -> new HashSet<>()).add(driver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 标签页关闭时执行
     *
     * @param message 回调参数
     */
    private void onTargetDestroyed(Object message) {
        JSONObject jsonObject = JSON.parseObject(message.toString());
        String tabId = jsonObject.getString("targetId");
        if (this.dlMgr != null) this.dlMgr.clearTabInfo(tabId);
        if (frames != null) frames.values().removeIf(value -> value.equals(tabId));
        allDrivers.forEach((a, b) -> {
            if (a.equals(tabId)) b.forEach(Driver::stop);
        });
        allDrivers.remove(tabId);
        drivers.forEach((a, b) -> {
            if (a.equals(tabId)) b.stop();
        });
        drivers.remove(tabId);

    }

    /**
     * 执行page相关的逻辑
     */
    public void connectToPage() {
        if (!connected) {
            this.dlMgr = new DownloadManager(this);
            connected = true;
        }
    }

    public String runCdp(String cmd) {
        return runCdp(cmd, new HashMap<>());
    }

    /**
     * 执行Chrome DevTools Protocol语句
     *
     * @param cmd     协议项目
     * @param cmdArgs 参数
     * @return 执行的结果
     */
    public String runCdp(String cmd, Map<String, Object> cmdArgs) {
        cmdArgs = new HashMap<>(cmdArgs == null ? new HashMap<>() : cmdArgs);
        Object ignore = cmdArgs.remove("_ignore");
        String result = driver.run(cmd, cmdArgs).toString();
        JSONObject result1 = JSONObject.parseObject(result);
        if (result1.containsKey(__ERROR__)) Tools.raiseError(result1, ignore);
        return result;
    }

    /**
     * 返回标签页数量
     */
    public int tabsCount() {
        JSONArray targetInfos = JSON.parseObject(this.runCdp("Target.getTargets")).getJSONArray("targetInfos");
        return (int) targetInfos.stream().filter(targetInfo -> {
            JSONObject jsonObject = JSON.parseObject(targetInfo.toString());
            String type = jsonObject.getString("type");
            String url = jsonObject.getString("url");
            return ("page".equals(type) || "webview".equals(type)) && !url.startsWith("devtools://");
        }).count();
    }

    /**
     * 返回所有标签页id组成的列表
     */
    public List<String> tabs() {
        JSONArray jsonArray = JSON.parseArray(JSONObject.toJSONString(driver.get("http://" + address + "/json")));
        return jsonArray.stream().filter(targetInfo -> {
            JSONObject jsonObject = JSON.parseObject(targetInfo.toString());
            String type = jsonObject.getString("type");
            String url = jsonObject.getString("url");
            return ("page".equals(type) || "webview".equals(type)) && !url.startsWith("devtools://");
        }).map(obj -> ((JSONObject) obj).getString("id")).collect(Collectors.toList());
    }

    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @return tab id或tab列表
     */
    public List<String> findTabs() {
        return findTabs(null);
    }

    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @param title 要匹配title的文本
     * @return tab id或tab列表
     */
    public List<String> findTabs(String title) {
        return findTabs(title, null);
    }

    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @param title 要匹配title的文本
     * @param url   要匹配url的文本
     * @return tab id或tab列表
     */
    public List<String> findTabs(String title, String url) {
        return findTabs(title, url, null);
    }

    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @param title   要匹配title的文本
     * @param url     要匹配url的文本
     * @param tabType tab类型，可用列表输入多个
     * @return tab id或tab列表
     */
    public List<String> findTabs(String title, String url, List<String> tabType) {
        return findTabs(title, url, tabType, true);
    }

    /**
     * 查找符合条件的tab，返回它们的id组成的列表
     *
     * @param title   要匹配title的文本
     * @param url     要匹配url的文本
     * @param tabType tab类型，可用列表输入多个
     * @param single  是否返回首个结果的id，为False返回所有信息
     * @return tab id或tab列表
     */
    public List<String> findTabs(String title, String url, List<String> tabType, boolean single) {
        Object parse = JSON.parse(JSONObject.toJSONString(this.driver.get("http://" + this.address + "/json")));
        JSONArray tabs;
        if (parse instanceof String) {
            tabs = new JSONArray(List.of(parse));
        } else if (parse instanceof List || parse instanceof Set || parse instanceof String[]) {
            tabs = JSON.parseArray(parse.toString());
        } else {
            throw new IllegalArgumentException("tab_type类型不对" + parse.toString());
        }

        List<String> result = tabs.stream().filter(targetInfo -> {
            JSONObject jsonObject = JSON.parseObject(targetInfo.toString());
            return (title == null || jsonObject.getString("title").contains(title)) && (url == null || jsonObject.getString("url").contains(url)) && (tabType == null || tabType.contains(jsonObject.getString("type")));
        }).map(tab -> ((JSONObject) tab).getString("id")).collect(Collectors.toList());
        return single ? (result.isEmpty() ? null : List.of(result.get(0))) : result;
    }

    /**
     * 关闭标签页
     *
     * @param tabId 标签页id
     */
    public void closeTab(String tabId) {
        this.onTargetDestroyed(JSON.toJSONString(Map.of("targetId", tabId)));
        this.driver.run("Target.closeTarget", Map.of("targetId", tabId));
    }

    /**
     * 停止一个Driver
     *
     * @param driver Driver对象
     */
    public void stopDiver(Driver driver) {
        driver.stop();
        Set<Driver> set = this.allDrivers.get(driver.getId());
        if (set != null) set.remove(driver);
    }

    /**
     * 使标签页变为活动状态
     *
     * @param tabId 标签页id
     */
    public void activateTab(String tabId) {
        this.runCdp("Target.activateTarget", Map.of("targetId", tabId));
    }

    /**
     * 返回浏览器窗口位置和大小信息
     *
     * @return 窗口大小字典
     */
    public JSONObject getWindowBounds() {
        return getWindowBounds(null);
    }

    /**
     * 返回浏览器窗口位置和大小信息
     *
     * @param tabId 标签页id  不传入默认使用本身的id
     */
    public JSONObject getWindowBounds(String tabId) {
        return JSON.parseObject(runCdp("Browser.getWindowForTarget", Map.of("targetId", tabId == null || tabId.isEmpty() ? this.id : tabId))).getJSONObject("bounds");
    }

    /**
     * 断开重连
     */
    public void reconnect() {
        this.driver.stop();
        BrowserDriver.BROWSERS.remove(this.id);
        this.driver = BrowserDriver.getInstance(this.id, "browser", this.address, this);
        this.runCdp("Target.setDiscoverTargets", Map.of("discover", true));
        this.driver.setCallback("Target.targetDestroyed", new MyRunnable() {
            @Override
            public void run() {
                onTargetDestroyed(getMessage());
            }
        });
        this.driver.setCallback("Target.targetCreated", new MyRunnable() {
            @Override
            public void run() {
                onTargetCreated(getMessage());
            }
        });
    }

    /**
     * 关闭浏览器
     */
    public void quit() {
        quit(5.0);
    }

    /**
     * 关闭浏览器
     *
     * @param timeout 等待浏览器关闭超时时间
     */
    public void quit(double timeout) {
        quit(timeout, false);
    }

    /**
     * 关闭浏览器
     *
     * @param timeout 等待浏览器关闭超时时间
     * @param force   是否立刻强制终止进程
     */
    public void quit(double timeout, boolean force) {
        List<Integer> pids = JSON.parseArray(this.runCdp("SystemInfo.getProcessInfo")).stream().map(o -> JSON.parseObject(o.toString()).getInteger("id")).collect(Collectors.toList());


        for (Set<Driver> value : this.allDrivers.values()) for (Driver driver1 : value) driver1.stop();

        if (force) {
            for (Integer pid : pids)
                try {
                    ProcessHandle.allProcesses().filter(process -> process.info().command().isPresent()).filter(process -> process.info().command().map(s -> s.contains(Integer.toString(pid))).orElse(false)).forEach(process -> {
                        if (process.isAlive()) process.destroy();
                    });
                } catch (SecurityException ignored) {
                }

        } else {
            try {
                this.runCdp("Browser.close");
                this.driver.stop();
            } catch (PageDisconnectedError e) {
                this.driver.stop();
                return;
            }
        }


        if (force) {
            String[] ipPort = address.split(":");
            if (!("127.0.0.1".equals(ipPort[0]) || "localhost".equals(ipPort[0]))) {
                return;
            }
            Tools.stopProcessOnPort(Integer.parseInt(ipPort[1]));
            return;
        }

        if (processId != null) {
            String txt = System.getProperty("os.name").toLowerCase().contains("win") ? "tasklist | findstr " + processId : "ps -ef | grep " + processId;
            long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
            while (System.currentTimeMillis() < endTime) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ProcessBuilder(txt.split("\\s+")).start().getInputStream()))) {
                    try {
                        if (!reader.readLine().contains(processId.toString())) {
                            return;
                        }
                    } catch (NullPointerException e) {
                        // Handle null pointer exception, if needed
                    }
                    Thread.sleep(100);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDisconnect() {
        this.page.onDisconnect();
        BROWSERS.remove(id);
        if (page instanceof ChromiumPage) {
            ChromiumOptions chromiumOptions = ((ChromiumPage) page).getChromiumOptions();
            if (chromiumOptions.isAutoPort() && chromiumOptions.getUserDataPath() != null) {
                Path path = Paths.get(chromiumOptions.getUserDataPath());
                long endTime = System.currentTimeMillis() + 7000;
                while (System.currentTimeMillis() < endTime) {
                    if (!Files.exists(path)) break;
                    try (Stream<Path> walk = Files.walk(path, FileVisitOption.FOLLOW_LINKS)) {
                        walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }
}