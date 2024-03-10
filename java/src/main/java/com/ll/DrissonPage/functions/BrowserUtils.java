package com.ll.DrissonPage.functions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.config.ChromiumOptions;
import com.ll.DrissonPage.config.OptionsManager;
import com.ll.DrissonPage.error.extend.BrowserConnectError;
import com.ll.DrissonPage.utils.CloseableHttpClientUtils;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class BrowserUtils {
    /**
     * 连接或启动浏览器
     *
     * @param options ChromiumOptions对象
     * @return 返回是否接管的浏览器
     */
    public static boolean connectBrowser(ChromiumOptions options) {
        if (options == null) throw new NullPointerException("必须传入ChromiumOptions对象");
        String address = options.getAddress().replace("localhost", "127.0.0.1").replace("http://", "").replace("https://", "");
        String browserPath = options.getBrowserPath();
        String[] split = address.split(":");
        String ip = split[0];
        String port = split[1];
        if (!ip.equals("127.0.0.1") || Tools.portIsUsing(ip, Integer.parseInt(port)) || options.isExistingOnly()) {
            testConnect(ip, port);
            for (String argument : options.getArguments()) {
                if (argument.startsWith("--headless") && argument.endsWith("=false")) {
                    options.headless(true);
                    break;
                }
            }
            return true;
        }
        //   ----------创建浏览器进程----------
        List<String> args = getLaunchArgs(options);
        setPrefs(options);
        setFlags(options);
        try {
            runBrowser(port, browserPath, args);
        } catch (Exception e) {
            browserPath = getChromePath();
            if (browserPath == null) {
                throw new RuntimeException("未找到浏览器，请手动指定浏览器可执行文件路径。");
            }
            runBrowser(port, browserPath, args);
        }
        testConnect(ip, port);
        return false;
    }

    /**
     * 从ChromiumOptions获取命令行启动参数
     *
     * @param options ChromiumOptions
     * @return 启动参数列表
     */
    public static List<String> getLaunchArgs(ChromiumOptions options) {
        Set<String> result = new HashSet<>();
        boolean hasUserPath = false;
        Boolean headless = null;

        for (String arg : options.getArguments()) {
            if (arg.startsWith("--load-extension=") || arg.startsWith("--remote-debugging-port=")) {
                continue;
            } else if (arg.startsWith("--user-data-dir") && !options.isSystemUserPath()) {
                result.add("--user-data-dir=" + Path.of(arg.substring(16)).toAbsolutePath());
                hasUserPath = true;
                continue;
            } else if (arg.startsWith("--headless")) {
                if ("--headless=false".equals(arg)) {
                    headless = false;
                    continue;
                } else if ("--headless".equals(arg)) {
                    arg = "--headless=new";
                    headless = true;
                } else {
                    headless = true;
                }
            }

            result.add(arg);
        }

        if (!hasUserPath && !options.isSystemUserPath()) {
            String port = options.getAddress() != null ? options.getAddress().split(":")[1] : "0";
            Path p = options.getTmpPath() != null && !options.getTmpPath().isEmpty() ? Path.of(options.getTmpPath()) : Paths.get(System.getProperty("java.io.tmpdir")).resolve("DrissionPage");
            Path path = p.resolve("userData_" + port);
            path.toFile().mkdirs();
            options.setUserDataPath(path.toString());
            result.add("--user-data-dir=" + path);
        }

        if (headless == null && System.getProperty("os.name").toLowerCase().contains("linux")) {
            try {
                Process process = Runtime.getRuntime().exec("systemctl list-units | grep graphical.target");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                if (reader.lines().noneMatch(line -> line.contains("graphical.target"))) {
                    headless = true;
                    result.add("--headless=new");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        List<String> resultList = new ArrayList<>(result);
        options.headless(Boolean.TRUE.equals(headless));

        // 处理插件extensions
        List<String> extensions = options.getExtensions().stream().map(e -> Path.of(e).toAbsolutePath().toString()).distinct().collect(Collectors.toList());

        if (!extensions.isEmpty()) {
            String ext = String.join(",", extensions);
            result.add("--load-extension=" + ext);
        }

        return resultList;
    }


    /**
     * 处理启动配置中的prefs项，目前只能对已存在文件夹配置
     *
     * @param options ChromiumOptions
     */
    public static void setPrefs(ChromiumOptions options) {
        if (options == null) throw new NullPointerException("必须传入ChromiumOptions对象");
        if (options.getUserDataPath() == null || options.getPres() == null && options.getPresToDel() == null) return;
        Map<String, Object> pres = options.getPres();
        List<String> delList = options.getPresToDel();
        String user = "Default";
        for (String argument : options.getArguments())
            if (argument.startsWith("--profile-directory")) {
                String[] split = argument.split("=");
                user = split[split.length - 1].strip();
                break;
            }
        Path prefsFile = Paths.get(options.getUserDataPath()).resolve(user).resolve("Preferences");
        if (!Files.exists(prefsFile)) {
            try {
                // 创建父目录（如果不存在）
                Files.createDirectories(prefsFile.getParent());
                // 创建文件并写入空的 JSON 对象
                Files.write(prefsFile, "{}".getBytes(), StandardOpenOption.CREATE);
            } catch (Exception e) {
                e.printStackTrace(); // 处理异常，例如文件创建失败
            }
        }

        // 读取属性文件
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(prefsFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            builder = new StringBuilder("{}");
        }
        JSONObject targetDict = JSON.parseObject(builder.toString());
        // 设置新的属性值
        for (String pref : pres.keySet()) {
            Object value = pres.get(pref);
            String[] prefArray = pref.split("\\.");

            List<String> list = Arrays.asList(prefArray);
            makeLeaveInMap(targetDict, list, 0, prefArray.length);
            setValueToMap(targetDict, list, value);
        }
        // 删除属性
        for (String pref : delList) {
            removeArgFromDict(targetDict, pref);
        }

        // 写入属性文件
        try (BufferedWriter writer = Files.newBufferedWriter(prefsFile, StandardCharsets.UTF_8)) {
            writer.write(targetDict.toJSONString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 处理启动配置中的prefs项，目前只能对已存在文件夹配置
     *
     * @param options ChromiumOptions
     */
    public static void setFlags(ChromiumOptions options) {
        if (options == null) throw new NullPointerException("必须传入ChromiumOptions对象");
        if (options.getUserDataPath() == null || options.getPres() == null && options.getPresToDel() == null) return;
        Path stateFile = Paths.get(options.getUserDataPath()).resolve("Local State");
        if (!Files.exists(stateFile)) {
            try {
                // 创建父目录（如果不存在）
                Files.createDirectories(stateFile.getParent());
                // 创建文件并写入空的 JSON 对象
                Files.write(stateFile, "{}".getBytes(), StandardOpenOption.CREATE);
            } catch (Exception e) {
                e.printStackTrace(); // 处理异常，例如文件创建失败
            }
        }
        // 读取属性文件
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(stateFile, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            builder = new StringBuilder("{}");
        }
        JSONObject jsonObject;
        try {
            jsonObject = JSON.parseObject(builder.toString());

        } catch (Exception e) {
            jsonObject = JSON.parseObject("{}");

        }
        if (!options.isClearFileFlags()) {
            jsonObject.put("browser", Objects.requireNonNullElseGet(jsonObject.get("browser"), HashMap::new));
            jsonObject.put("enabled_labs_experiments", Objects.requireNonNullElseGet(jsonObject.get("enabled_labs_experiments"), HashMap::new));
        }
        Map<String, Object> flagsMap = new HashMap<>(options.getFlags());
        List<String> value = new ArrayList<>();
        flagsMap.forEach((k, v) -> value.add(v == null ? k : k + "@" + v));
        jsonObject.getJSONObject("browser").put("enabled_labs_experiments", value);
        // 写入属性文件
        try (BufferedWriter writer = Files.newBufferedWriter(stateFile, StandardCharsets.UTF_8)) {
            writer.write(jsonObject.toJSONString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试浏览器是否可用默认三十秒
     *
     * @param ip   浏览器ip
     * @param port 浏览器端口
     */
    public static void testConnect(String ip, String port) {
        testConnect(ip, port, 30);
    }

    /**
     * 测试浏览器是否可用
     *
     * @param ip      浏览器ip
     * @param port    浏览器端口
     * @param timeout 超时时间（秒）
     */
    public static void testConnect(String ip, String port, Integer timeout) {
        long endTime = System.currentTimeMillis() + (long) (timeout * 1000);
        while (System.currentTimeMillis() < endTime) {
            try {
                HttpGet request = new HttpGet("http://" + ip + ":" + port + "/json");
                request.setHeader("Connection", "close");
                request.setConfig(RequestConfig.custom().setConnectTimeout(20).build());
                String text = CloseableHttpClientUtils.sendRequestJson(request);
                if (text != null) {
                    for (Object o : JSON.parseArray(text)) {
                        String type = JSON.parseObject(o.toString()).get("type").toString();
                        if ("page".contains(type) || "webview".contains(type)) return;
                    }
                } else {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            } catch (Exception e) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        throw new BrowserConnectError("\n" + ip + ":" + port + "浏览器无法链接。\n请确认：\n1、该端口为浏览器\n" + "2、已添加'--remote-debugging-port=" + port + "'启动项\n" + "3、用户文件夹没有和已打开的浏览器冲突\n" + "4、如为无界面系统，请添加'--headless=new '参数\n" + "5、如果是Linux系统，可能还要添加'--no-sandbox '启动参数\n" + "可使用ChromiumOptions设置端口和用户文件夹路径。");
    }

    /**
     * 创建chrome进程
     *
     * @param port 端口号
     * @param path 浏览器路径
     * @param args 启动参数
     */
    private static void runBrowser(String port, String path, List<String> args) {
        File executable = new File(path);
        ProcessBuilder processBuilder = new ProcessBuilder();
        // 设置命令和参数
        processBuilder.command(executable.getAbsolutePath(), "--remote-debugging-port=" + port);
        processBuilder.command().addAll(args);

        // 设置工作目录
        processBuilder.directory(executable.getParentFile());
        try {
            // 启动进程
            processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("未找到浏览器，请手动指定浏览器可执行文件路径。", e);
        }
    }

    /**
     * 把prefs中a.b.c形式的属性转为a['b']['c']形式
     *
     * @param targetDict 要处理的map
     * @param src        属性层级列表[a, b, c]
     * @param num        当前处理第几个
     * @param end        src长度
     */
    public static void makeLeaveInMap(JSONObject targetDict, List<String> src, int num, int end) {
        if (num == end) {
            return;
        }

        String key = src.get(num);

        if (!targetDict.containsKey(key)) {
            targetDict.put(key, new JSONObject());
        }

        num++;
        try {
            makeLeaveInMap(targetDict.getJSONObject(key), src, num, end);
        } catch (Exception e) {
            return;
        }

    }

    private static void setValueToMap(JSONObject targetDict, List<String> src, Object value) {
        setNestedValue(targetDict, src, value, 0);
    }

    private static void setNestedValue(JSONObject currentDict, List<String> src, Object value, int index) {
        // 当前层级是最后一层，设置值
        if (index == src.size() - 1) currentDict.put(src.get(index), value);
        else {
            // 当前层级不是最后一层，继续递归
            JSONObject nestedDict = JSON.parseObject(currentDict.getOrDefault(src.get(index), new HashMap<>()).toString());
            currentDict.put(src.get(index), nestedDict);
            setNestedValue(nestedDict, src, value, index + 1);
        }
    }

    private static void removeArgFromDict(JSONObject targetDict, String arg) {
        List<String> args = List.of(arg.split("\\."));
        removeNestedArg(targetDict, args, 0);
    }

    private static void removeNestedArg(JSONObject currentDict, List<String> args, int index) {
        if (index == args.size() - 1) {
            // 当前层级是最后一层，删除值
            currentDict.remove(args.get(index));
        } else {
            // 当前层级不是最后一层，继续递归
            JSONObject nestedDict = JSON.parseObject(currentDict.get(args.get(index)).toString());
            if (nestedDict != null) {
                removeNestedArg(nestedDict, args, index + 1);
            }
        }
    }

    /**
     * @return 从ini文件或系统变量中获取chrome可执行文件的路径
     */
    public static String getChromePath() {
        // 从ini文件中获取
        String path = new OptionsManager().getValue("chromium_options", "browser_path");
        if (path != null && new File(path).isFile()) return path;

        // 使用which获取
        String[] candidates = {"chrome", "chromium", "google-chrome", "google-chrome-stable", "google-chrome-unstable", "google-chrome-beta"};
        for (String candidate : candidates) {
            path = findExecutable(candidate);
            if (path != null) return path;
        }

        // 从MAC和Linux默认路径获取
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) {
            path = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
            return new File(Paths.get(path).toString()).exists() ? path : null;
        } else if (os.contains("linux")) {
            String[] paths = {"/usr/bin/google-chrome", "/opt/google/chrome/google-chrome", "/usr/lib/chromium-browser/chromium-browser"};
            for (String p : paths) {
                if (new File(p).exists()) {
                    return p;
                }
            }
            return null;
        } else if (!os.contains("windows")) {
            return null;
        }

        // 从注册表中获取
        String registryPath = getRegistryPath();
        if (registryPath != null) {
            return registryPath;
        }
        // 从系统变量中获取
        return getPathFromSystemVariable();
    }

    private static String findExecutable(String executable) {
        try {
            Process process = Runtime.getRuntime().exec("which " + executable);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * @return 获取注册表中谷歌浏览器的地址
     */
    public static String getRegistryPath() {
        try {
            // 使用 Advapi32Util.registryGetStringValue 获取注册表项的字符串值
            String chromePath = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\App Paths\\chrome.exe", "");

            if (chromePath != null && !chromePath.isEmpty()) {
                return chromePath;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return 获取系统变量谷歌浏览器的地址
     */
    private static String getPathFromSystemVariable() {
        String paths = System.getenv("PATH").toLowerCase();
        Pattern pattern = Pattern.compile("[^;]*chrome[^;]*");
        Matcher matcher = pattern.matcher(paths.toLowerCase());
        if (matcher.find()) {
            String path = matcher.group(0);
            if (path.contains("chrome.exe")) {
                return Paths.get(path).toString();
            }
        }
        return null;
    }

}
