package com.ll.DrissonPage.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.Getter;
import org.ini4j.Wini;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */

@Getter
public class ChromiumOptions {
    private final String iniPath; //ini文件路径
    private final Map<String, Double> timeouts;  // 返回timeouts设置
    private final List<String> arguments;  // 返回浏览器命令行设置列表
    private final List<String> extensions;  // 以list形式返回要加载的插件路径
    private final Map<String, Object> pres;  // 返回用户首选项配置
    private final List<String> presToDel;    //删除用户配置文件中已设置的项
    private final Map<String, String> flags;  // 返回实验项配置
    private String downloadPath;  // 默认下载路径文件路径
    private String browserPath;  // 浏览器启动文件路径
    private String userDataPath;  // 返回用户数据文件夹路径
    private String tmpPath;  // 返回临时文件夹路径
    private String user;  // 返回用户配置文件夹名称
    private String loadMode;  // 返回页面加载策略，'normal', 'eager', 'none'
    private String proxy;  // 返回代理设置
    private String address;  // 返回浏览器地址，ip:port
    private boolean systemUserPath;  // 返回是否使用系统安装的浏览器所使用的用户数据文件夹

    private boolean existingOnly;  // 返回是否只接管现有浏览器方式

    private boolean autoPort;  // 返回是否使用自动端口和用户文件

    private int retryTimes;  // 返回连接失败时的重试次数

    private int retryInterval;  // 返回连接失败时的重试间隔（秒）
    private boolean clearFileFlags;// 删除浏览器配置文件中已设置的实验项
    private boolean headless;//设置是否隐藏浏览器界面

    public ChromiumOptions() {
        this(true, null);
    }

    /**
     * @param readFile 是否从默认ini文件中读取配置信息
     * @param iniPath  ini文件路径，为None则读取默认ini文件
     */
    public ChromiumOptions(boolean readFile, String iniPath) {
        // 构造方法实现部分
        this.userDataPath = null;
        this.user = "Default";
        this.presToDel = new ArrayList<>();
        this.clearFileFlags = false;
        this.headless = false;
        if (readFile) {
            // 从文件中读取配置信息的实现部分
            OptionsManager om = new OptionsManager(iniPath);
            this.iniPath = om.getIniPath();
            // 从OptionsManager获取配置信息
            Wini ini = om.getIni();
            this.downloadPath = ini.get("paths", "download_path");
            this.tmpPath = ini.get("paths", "tmp_path");
            this.arguments = JSON.parseArray(ini.get("chromium_options", "arguments"), String.class);
            this.browserPath = ini.get("chromium_options", "browser_path");
            this.extensions = JSON.parseArray(ini.get("chromium_options", "extensions"), String.class);
            this.pres = JSON.parseObject(ini.get("chromium_options", "prefs"), new TypeReference<>() {
            });
            this.flags = JSON.parseObject(ini.get("chromium_options", "flags"), new TypeReference<>() {
            });
            this.address = ini.get("chromium_options", "address");
            String s = ini.get("chromium_options", "load_mode");
            this.loadMode = s != null ? s : "normal";
            s = ini.get("chromium_options", "system_user_path");
            this.systemUserPath = Boolean.parseBoolean(s);
            s = ini.get("chromium_options", "existing_only");
            this.existingOnly = Boolean.parseBoolean(s);
            s = ini.get("proxies", "http");
            this.proxy = s != null ? s : ini.get("proxies", "https");

            boolean userPathSet = false;
            boolean userSet = false;

            for (String arg : this.arguments) {
                if (arg.startsWith("--user-data-dir=")) {
                    setPaths(arg.substring(16));
                    userPathSet = true;
                }
                if (arg.startsWith("--profile-directory=")) {
                    setUser(arg.substring(20));
                    userSet = true;
                }
                if (userSet && userPathSet) {
                    break;
                }
            }


            s = ini.get("timeouts", "base");
            String s1 = ini.get("timeouts", "page_load");
            String s2 = ini.get("timeouts", "script");
            this.timeouts = Map.of("base", s1 != null ? Double.parseDouble(s) : 10.0, "pageLoad", s1 != null ? Double.parseDouble(s1) : 20.0, "script", s2 != null ? Double.parseDouble(s2) : 30.0);
            s = ini.get("chromium_options", "auto_port");
            this.autoPort = Boolean.parseBoolean(s);
            if (this.autoPort) {
                // 使用自动端口和用户文件
                PortFinder.PortInfo portInfo = new PortFinder().getPort();
                this.address = "127.0.0.1:" + portInfo.getPort();
                setArgument("--user-data-dir", portInfo.getPath());
            }

            this.retryTimes = Integer.parseInt(ini.get("others").getOrDefault("retry_times", "3"));
            this.retryInterval = Integer.parseInt(ini.get("others").getOrDefault("retry_interval", "2"));

            return;
        }

        // 默认值初始化
        this.iniPath = null;
        this.browserPath = "chrome";
        this.arguments = new ArrayList<>();
        this.downloadPath = null;
        this.tmpPath = null;
        this.extensions = new ArrayList<>();
        this.pres = new HashMap<>();
        this.flags = new HashMap<>();
        this.timeouts = new HashMap<>();
        this.timeouts.put("base", 10.0);
        this.timeouts.put("pageLoad", 20.0);
        this.timeouts.put("script", 30.0);
        this.address = "127.0.0.1:9222";
        this.loadMode = "normal";
        this.proxy = null;
        this.autoPort = false;
        this.systemUserPath = false;
        this.existingOnly = false;
        this.retryTimes = 3;
        this.retryInterval = 2;
    }

    /**
     * 设置连接失败时的重试操作
     *
     * @param times    重试次数
     * @param interval 重试间隔
     * @return 当前对象
     */
    public ChromiumOptions setRetry(Integer times, Integer interval) {
        if (times != null && times >= 0) this.retryTimes = times;
        if (interval != null && interval >= 0) this.retryInterval = interval;
        return this;
    }


    /**
     * 设置浏览器配置的 argument 属性
     *
     * @param arg 属性名
     */
    public ChromiumOptions setArgument(String arg) {
        // 返回当前对象
        return setArgument(arg, null);
    }

    /**
     * 设置浏览器配置的 argument 属性
     *
     * @param arg   属性名
     * @param value 属性值，有值的属性传入值，没有的传入 null，如传入 false，删除该项
     * @return 当前对象
     */
    public ChromiumOptions setArgument(String arg, String value) {
        // 调用 removeArgument 方法删除已有的同名属性
        removeArgument(arg);
        if (value == null && arg.equals("--headless")) {
            // 如果属性是 "--headless" 且值为 null，则将 "--headless=new" 添加到 _arguments 列表中
            arguments.add("--headless=new");
        } else {
            // 否则，根据是否有值构造属性字符串，添加到 _arguments 列表中
            arguments.add(value != null ? arg + "=" + value : arg);
        }

        // 返回当前对象
        return this;
    }

    /**
     * 移除一个 argument 项
     *
     * @param value 设置项名，有值的设置项传入设置名称即可
     * @return 本身
     */
    public ChromiumOptions removeArgument(String value) {
        List<String> delList = new ArrayList<>();
        for (String argument : arguments)
            if (argument.equals(value) || argument.startsWith(value + "=")) delList.add(argument);
        arguments.removeAll(delList);
        return this;
    }

    /**
     * 添加插件
     *
     * @param path 插件路径，可指向文件夹
     */
    public ChromiumOptions addExtension(String path) throws IOException {
        Path extensionPath = Paths.get(path);
        if (!Files.exists(extensionPath)) throw new IOException("插件路径不存在。");
        extensions.add(extensionPath.toString());
        return this;
    }

    /**
     * 移除所有插件
     */
    public ChromiumOptions removeExtensions() {
        extensions.clear();
        return this;
    }

    /**
     * 设置Preferences文件中的用户设置项
     *
     * @param key   设置项名称
     * @param value 设置项值
     */
    public ChromiumOptions setPref(String key, Object value) {
        this.pres.put(key, value);
        return this;
    }

    /**
     * 删除用户首选项设置，不能删除已设置到文件中的项
     *
     * @param key 设置项名称
     * @return 当前对象
     */
    public ChromiumOptions removePref(String key) {
        this.pres.remove(key);
        return this;
    }

    /**
     * 删除用户配置文件中已设置的项
     *
     * @param arg 设置项名称
     * @return 当前对象
     */
    public ChromiumOptions removePrefFromFile(String arg) {
        this.presToDel.add(arg);
        return this;
    }

    /**
     * 设置实验项
     *
     * @param flag  设置项名称
     * @param value 设置项的值，为null则删除该项
     * @return 当前对象
     */
    public ChromiumOptions setFlag(String flag, String value) {
        if (value == null) flags.remove(flag);
        else flags.put(flag, value);
        return this;
    }

    /**
     * 删除浏览器配置文件中已设置的实验项
     *
     * @return 返回当前对象
     */
    public ChromiumOptions clearFlagsInFile() {
        clearFileFlags = true;
        return this;
    }

    /**
     * 清空本对象已设置的argument参数
     *
     * @return 当前对象
     */
    public ChromiumOptions clearArguments() {
        this.arguments.clear();
        return this;
    }

    /**
     * 清空本对象已设置的pref参数
     *
     * @return 当前对象
     */
    public ChromiumOptions clearPrefs() {
        this.pres.clear();
        ;
        return this;
    }

    /**
     * 设置超时时间，单位为秒
     *
     * @param base     默认超时时间
     * @param pageLoad 页面加载超时时间
     * @param script   脚本运行超时时间
     * @return 当前对象
     */
    public ChromiumOptions setTimeouts(Double base, Double pageLoad, Double script) {
        // 设置超时时间，单位为秒
        if (base != null && base >= 0) timeouts.put("base", base);
        if (pageLoad != null && pageLoad >= 0) timeouts.put("pageLoad", pageLoad);
        if (script != null && script >= 0) timeouts.put("script", script);

        // 返回当前对象
        return this;
    }

    /**
     * 设置使用哪个用户配置文件夹
     *
     * @param user 用户文件夹名称
     * @return 当前对象
     */
    public ChromiumOptions setUser(String user) {
        setArgument("--profile-directory", user);
        this.user = user;

        // 返回当前对象
        return this;
    }

    public ChromiumOptions headless() {
        return headless(true);
    }

    /**
     * 设置是否隐藏浏览器界面
     *
     * @param onOff 是否开启
     * @return 当前对象
     */
    public ChromiumOptions headless(boolean onOff) {
        this.headless = onOff;
        String value = onOff ? "new" : null;
        return setArgument("--headless", value);
    }

    public ChromiumOptions noImg() {
        return noImg(true);
    }

    /**
     * 设置是否加载图片
     *
     * @param onOff 是否开启
     * @return 当前对象
     */
    public ChromiumOptions noImg(boolean onOff) {
        return onOff ? setArgument("--blink-settings=imagesEnabled=false") : this;
    }

    public ChromiumOptions noJs() {
        return noJs(true);
    }

    /**
     * 设置是否禁用js
     *
     * @param onOff 是否开启
     * @return 当前对象
     */
    public ChromiumOptions noJs(boolean onOff) {
        return onOff ? setArgument("--disable-javascript") : this;
    }

    public ChromiumOptions mute() {
        return mute(true);
    }

    /**
     * 设置是否静音
     *
     * @param onOff 是否开启
     * @return 当前对象
     */
    public ChromiumOptions mute(boolean onOff) {
        return onOff ? setArgument("--mute-audio") : this;
    }

    public ChromiumOptions incognito() {
        return incognito(true);
    }


    /**
     * 设置是否使用无痕模式启动
     *
     * @param onOff 是否开启
     * @return 当前对象
     */
    public ChromiumOptions incognito(boolean onOff) {
        return onOff ? setArgument("--incognito") : this;
    }

    public ChromiumOptions ignoreCertificateErrors() {
        return ignoreCertificateErrors(true);
    }

    /**
     * 设置是否忽略证书错误
     *
     * @param onOff 是否开启
     * @return 当前对象
     */
    public ChromiumOptions ignoreCertificateErrors(boolean onOff) {
        return onOff ? setArgument("--ignore-certificate-errors") : this;
    }

    /**
     * 设置user agent
     *
     * @param userAgent user agent文本
     * @return 当前对象
     */
    public ChromiumOptions setUserAgent(String userAgent) {
        return setArgument("--user-agent", userAgent);
    }

    /**
     * 设置代理
     *
     * @param proxy 代理
     * @return 当前对象
     */
    public ChromiumOptions setProxy(String proxy) {
        if (Pattern.matches(".*?:.*?@.*?\\..*", proxy)) {
            System.out.println("你似乎在设置使用账号密码的代理，暂时不支持这种代理，可自行用插件实现需求。");
        }
        if (proxy.toLowerCase().startsWith("socks")) {
            System.out.println("你似乎在设置使用socks代理，暂时不支持这种代理，可自行用插件实现需求。");
        }
        this.proxy = proxy;
        return setArgument("--proxy-server", proxy);
    }

    /**
     * 设置load_mode  可接收 'normal', 'eager', 'none'
     * normal：默认情况下使用, 等待所有资源下载完成
     * eager：DOM访问已准备就绪, 但其他资源 (如图像) 可能仍在加载中
     * none：完全不阻塞
     *
     * @param value 可接收 'normal', 'eager', 'none'
     * @return 当前对象
     */
    public ChromiumOptions setLoadMode(String value) {
        //
        String lowerCase = value == null ? null : value.trim().toLowerCase();
        if (!List.of("normal", "eager", "none").contains(lowerCase)) {
            throw new IllegalArgumentException("只能选择 'normal', 'eager', 'none'。");
        }
        this.loadMode = lowerCase;
        return this;
    }

    public ChromiumOptions setPaths(String browserPath) {
        return setPaths(browserPath, null, null, null, null, null, null);
    }

    /**
     * 快捷的路径设置函数
     *
     * @param browserPath     浏览器可执行文件路径
     * @param localPort       本地端口号
     * @param address         调试浏览器地址，例：127.0.0.1:9222
     * @param downloadPath    下载文件路径
     * @param userDataPath    用户数据路径
     * @param cachePath       缓存路径
     * @param debuggerAddress 调试浏览器地址
     * @return 当前对象
     */
    public ChromiumOptions setPaths(String browserPath, Integer localPort, String address, String downloadPath, String userDataPath, String cachePath, String debuggerAddress) {
        // 快捷的路径设置函数
        address = (address != null) ? address : debuggerAddress;
        if (browserPath != null) {
            setBrowserPath(browserPath);
        }

        if (localPort != null) {
            setLocalPort(localPort);
        }

        if (address != null) {
            setAddress(address);
        }

        if (downloadPath != null) {
            setDownloadPath(downloadPath);
        }

        if (userDataPath != null) {
            setUserDataPath(userDataPath);
        }

        if (cachePath != null) {
            setCachePath(cachePath);
        }

        return this;
    }

    /**
     * 设置本地启动端口
     *
     * @param port 端口号
     * @return 当前对象
     */
    public ChromiumOptions setLocalPort(Integer port) {
        this.address = String.format("127.0.0.1:%04d", port);
        this.autoPort = false;
        return this;
    }

    /**
     * 设置浏览器地址，格式'ip:port'
     *
     * @param address 浏览器地址
     * @return 当前对象
     */
    public ChromiumOptions setAddress(String address) {
        address = address.replace("localhost", "127.0.0.1").replace("http://", "").replace("https://", "");
        this.address = address;
        return this;
    }

    /**
     * 设置浏览器可执行文件路径
     *
     * @param path 浏览器路径
     * @return 当前对象
     */
    public ChromiumOptions setBrowserPath(String path) {
        if (path != null && !path.isEmpty()) {
            // 设置浏览器可执行文件路径
            this.browserPath = path;
            this.autoPort = false;
        }
        return this;
    }

    /**
     * 设置下载文件保存路径
     *
     * @param path 下载路径
     * @return 当前对象
     */
    public ChromiumOptions setDownloadPath(String path) {
        if (path != null && !path.isEmpty()) this.downloadPath = path;
        return this;
    }

    /**
     * 设置临时文件文件保存路径
     *
     * @param path 用户文件夹路径
     * @return 当前对象
     */
    public ChromiumOptions setTmpPath(String path) {
        if (path != null && !path.isEmpty()) this.tmpPath = path;
        return this;
    }

    /**
     * 设置用户文件夹路径
     *
     * @param path 用户文件夹路径
     * @return 当前对象
     */
    public ChromiumOptions setUserDataPath(String path) {
        // 设置用户文件夹路径
        if (path != null && !path.isEmpty()) {
            setArgument("--user-data-dir", path);
            this.userDataPath = path;
            this.autoPort = false;
        }
        return this;
    }


    /**
     * 设置缓存路径
     *
     * @param path 缓存路径
     * @return 当前对象
     */
    public ChromiumOptions setCachePath(String path) {
        if (path != null && !path.isEmpty()) setArgument("--disk-cache-dir", path);
        return this;
    }

    public ChromiumOptions useSystemUserPath() {
        return useSystemUserPath(true);
    }

    /**
     * 设置是否使用系统安装的浏览器默认用户文件夹
     *
     * @param onOff 开或关
     * @return 当前对象
     */
    public ChromiumOptions useSystemUserPath(boolean onOff) {
        //
        this.systemUserPath = onOff;
        return this;
    }

    /**
     * 自动获取可用端口
     *
     * @return 当前对象
     */
    public ChromiumOptions autoPort() {
        return autoPort(null);
    }

    public ChromiumOptions autoPort(String tmpPath) {
        return autoPort(true, tmpPath);
    }

    /**
     * 自动获取可用端口
     *
     * @param onOff 开或关
     * @return 当前对象
     */
    public ChromiumOptions autoPort(boolean onOff, String tmpPath) {
        if (onOff) {
            this.autoPort = true;
            if (tmpPath != null && !tmpPath.isEmpty()) this.tmpPath = tmpPath;
        } else {
            this.autoPort = false;
        }
        return this;
    }

    public ChromiumOptions existingOnly() {
        return existingOnly(true);
    }

    /**
     * 设置只接管已有浏览器，不自动启动新的
     *
     * @param onOff 开或关
     * @return 当前对象
     */
    public ChromiumOptions existingOnly(boolean onOff) {
        //
        this.existingOnly = onOff;
        return this;
    }

    /**
     * 保存当前配置到默认ini文件
     *
     * @param path ini文件的路径， None 保存到当前读取的配置文件，传入 'default' 保存到默认ini文件
     * @return 保存文件的绝对路径
     */
    public String save(String path) throws IOException, NoSuchFieldException, IllegalAccessException {
        // 保存设置到文件
        URL resource = getClass().getResource("/configs.ini");
        if (resource == null) throw new FileNotFoundException();
        String pathStr = Paths.get(resource.getPath()).toAbsolutePath().toString();


        if ("default".equals(path)) {
            path = pathStr;
        } else if (path == null) {
            if (this.iniPath != null) {
                path = Paths.get(this.iniPath).toAbsolutePath().toString();
            } else {
                path = pathStr;
            }
        } else {
            path = Paths.get(path).toAbsolutePath().toString();
        }
        path = path + File.separator + "config.ini";
        OptionsManager om;
        if (new File(path).exists()) {
            om = new OptionsManager(path);
        } else {
            om = new OptionsManager(this.iniPath != null ? this.iniPath : pathStr);
        }
        // 设置chromium_options
        String[] attrs = {"address", "browserPath", "arguments", "extensions", "user", "loadMode", "autoPort", "systemUserPath", "existingOnly", "flags"};
        for (String i : attrs) {
            om.setItem("chromium_options", i, this.getClass().getDeclaredField("_" + i).get(this));
        }

        // 设置代理
        om.setItem("proxies", "http", this.proxy);
        om.setItem("proxies", "https", this.proxy);

        // 设置路径
        om.setItem("paths", "downloadPath", this.downloadPath != null ? this.downloadPath : "");
        om.setItem("paths", "tmpPath", this.tmpPath != null ? this.tmpPath : "");

        // 设置timeout
        om.setItem("timeouts", "base", this.timeouts.get("base"));
        om.setItem("timeouts", "pageLoad", this.timeouts.get("pageLoad"));
        om.setItem("timeouts", "script", this.timeouts.get("script"));

        // 设置重试
        om.setItem("others", "retryTimes", this.retryTimes);
        om.setItem("others", "retryInterval", this.retryInterval);

        // 设置prefs
        om.setItem("chromium_options", "prefs", this.pres);

        om.save(path);

        return path;
    }

    /**
     * 保存当前配置到默认ini文件
     *
     * @return 保存文件的绝对路径
     */
    public String saveToDefault() throws IOException, NoSuchFieldException, IllegalAccessException {
        return this.save("default");
    }
    public ChromiumOptions copy(){
        return JSON.parseObject(JSON.toJSONString(this),ChromiumOptions.class);
    }
}
