package com.ll.DrissonPage.units.downloader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.base.Browser;
import com.ll.DrissonPage.base.MyRunnable;
import com.ll.DrissonPage.page.ChromiumBase;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class DownloadManager {
    private final Browser browser;
    private ChromiumBase page;
    /**
     * 返回所有未完成的下载任务
     */
    @Getter
    private Map<String, DownloadMission> missions;
    private Map<String, List<DownloadMission>> tabMissions;
    private Map<String, Object> flags;
    private boolean running;
    private String savePath;
    private String whenDownloadFileExists;


    public DownloadManager(Browser browser) {
        this.browser = browser;
        this.page = browser.getPage();
        this.whenDownloadFileExists = "rename";
        TabDownloadSettings tabDownloadSettings = TabDownloadSettings.getInstance(this.page.tabId());
        tabDownloadSettings.path = this.page.downloadPath();
        this.missions = new HashMap<>();
        this.tabMissions = new HashMap<>();
        this.flags = new HashMap<>();
        String s = this.page.downloadPath();
        if (s != null && !s.isEmpty()) {
            this.browser.getDriver().setCallback("Browser.downloadProgress", new MyRunnable() {
                @Override
                public void run() {
                    onDownloadProgress(getMessage());
                }
            });
            this.browser.getDriver().setCallback("Browser.downloadWillBegin", new MyRunnable() {
                @Override
                public void run() {
                    onDownloadWillBegin(getMessage());
                }
            });
            String string = this.browser.runCdp("Browser.setDownloadBehavior", Map.of("downloadPath", this.page.downloadPath(), "behavior", "allowAndName", "eventsEnabled", true)).toString();
            if (JSON.parseObject(string).containsKey("error")) {
                System.out.println("浏览器版本太低无法使用下载管理功能。");
            }
            this.running = true;
        } else {
            this.running = false;
        }
    }

    /**
     * 设置某个tab的下载路径
     *
     * @param tab  页面对象
     * @param path 下载路径（绝对路径str）
     */
    public void setPath(ChromiumBase tab, String path) {
        TabDownloadSettings.getInstance(tab.tabId()).path = path;
        if (this.page.equals(tab) || !this.running) {
            this.browser.getDriver().setCallback("Browser.downloadProgress", new MyRunnable() {
                @Override
                public void run() {
                    onDownloadProgress(getMessage());
                }
            });
            this.browser.getDriver().setCallback("Browser.downloadWillBegin", new MyRunnable() {
                @Override
                public void run() {
                    onDownloadWillBegin(getMessage());
                }
            });
            String string = this.browser.runCdp("Browser.setDownloadBehavior", Map.of("downloadPath", this.page.downloadPath(), "behavior", "allowAndName", "eventsEnabled", true)).toString();
            this.savePath = path;
            if (JSON.parseObject(string).containsKey("error")) {
                System.out.println("浏览器版本太低无法使用下载管理功能。");
            }
        }
        this.running = true;
    }

    /**
     * 设置某个tab的重命名文件名
     *
     * @param tabId tab id
     */
    public void setRename(String tabId) {
        setRename(tabId, null);
    }

    /**
     * 设置某个tab的重命名文件名
     *
     * @param tabId  tab id
     * @param rename 文件名，可不含后缀，会自动使用远程文件后缀
     */
    public void setRename(String tabId, String rename) {
        setRename(tabId, rename, null);
    }

    /**
     * 设置某个tab的重命名文件名
     *
     * @param tabId  tab id
     * @param rename 文件名，可不含后缀，会自动使用远程文件后缀
     * @param suffix 后缀名，显式设置后缀名，不使用远程文件后缀
     */
    public void setRename(String tabId, String rename, String suffix) {
        TabDownloadSettings instance = TabDownloadSettings.getInstance(tabId);
        instance.rename = rename;
        instance.suffix = suffix;
    }

    /**
     * 设置某个tab下载文件重名时执行的策略
     *
     * @param tabId 下载路径
     * @param mode  下载路径
     */
    public void setFileExists(String tabId, Path mode) {
        setFileExists(tabId, mode.toAbsolutePath().toString());
    }

    /**
     * 设置某个tab下载文件重名时执行的策略
     *
     * @param tabId 下载路径
     * @param mode  下载路径
     */
    public void setFileExists(String tabId, String mode) {
        if (mode != null && !mode.isEmpty()) TabDownloadSettings.getInstance(tabId).whenFileExists = mode;
    }


    /**
     * 设置某个tab的重命名文件名
     *
     * @param tabId tab id
     * @param flag  等待标志
     */
    public void setFlag(String tabId, boolean flag) {
        this.flags.put(tabId, flag);
    }

    /**
     * 设置某个tab的重命名文件名
     *
     * @param tabId tab id
     * @param flag  等待标志
     */
    public void setFlag(String tabId, DownloadMission flag) {
        this.flags.put(tabId, flag);
    }

    /**
     * 获取tab下载等待标记
     *
     * @param tabId tab id
     * @return 任务对象或False
     */
    public Object getFlag(String tabId) {
        return this.flags.get(tabId);
    }

    /**
     * 获取某个tab正在下载的任务
     *
     * @param tabId tab id
     * @return 下载任务组成的列表
     */
    public List<DownloadMission> getTabMissions(String tabId) {
        return this.tabMissions.getOrDefault(tabId, new ArrayList<>());
    }

    /**
     * 设置任务结束
     *
     * @param mission 任务对象
     * @param state   任务状态
     */
    public void setDone(DownloadMission mission, String state) {
        setDone(mission, state, null);
    }

    /**
     * 设置任务结束
     *
     * @param mission   任务对象
     * @param state     任务状态
     * @param finalPath 最终路径
     */
    public void setDone(DownloadMission mission, String state, String finalPath) {
        if (!"canceled".equals(mission.state) && !"skipped".equals(mission.state)) mission.state = state;
        mission.finalPath = finalPath;
    }

    /**
     * 取消任务
     *
     * @param mission 任务对象
     */
    public Boolean cancel(DownloadMission mission) {
        mission.state = "canceled";
        try {
            this.browser.runCdp("Browser.cancelDownload", Map.of("guid", mission.id));
        } catch (Exception ignored) {

        }
        if (mission.finalPath != null) {
            return Paths.get(mission.finalPath).toFile().delete();
        }
        return null;
    }

    /**
     * 跳过任务
     *
     * @param mission 任务对象
     */
    public void skip(DownloadMission mission) {
        mission.state = "skipped";
        try {
            this.browser.runCdp("Browser.cancelDownload", Map.of("guid", mission.id));
        } catch (Exception ignored) {

        }
    }

    /**
     * 当tab关闭时清除有关信息
     *
     * @param tabId 标签页id
     */
    public void clearTabInfo(String tabId) {
        this.tabMissions.remove(tabId);
        this.flags.remove(tabId);
        TabDownloadSettings.TAB_DOWNLOAD_SETTINGS_MAP.remove(tabId);
    }

    /**
     * 用于获取弹出新标签页触发的下载任务
     */
    private void onDownloadWillBegin(Object params) {
        JSONObject kwargs = JSON.parseObject(params.toString());
        String guid = (String) kwargs.get("guid");
        String tabId = this.browser.getFrames().getOrDefault(kwargs.getString("frameId"), this.page.tabId());
        TabDownloadSettings settings = TabDownloadSettings.getInstance(tabId);
        String name;
        if (settings.getRename() != null) {
            if (settings.getSuffix() != null) {
                name = settings.getRename() + (settings.getSuffix() != null ? "." + settings.getSuffix() : "");
            } else {
                String[] tmp = ((String) kwargs.get("suggestedFilename")).split("\\.");
                String extName = tmp.length > 1 ? tmp[tmp.length - 1] : "";
                tmp = settings.getRename().split("\\.");
                String extRename = tmp.length > 1 ? tmp[tmp.length - 1] : "";
                name = (extRename.equals(extName) ? settings.getRename() : settings.getRename() + "." + extName);
            }

            settings.setRename(null);
            settings.setSuffix(null);

        } else if (settings.getSuffix() != null) {
            name = ((String) kwargs.get("suggestedFilename")).split("\\.")[0];
            if (settings.getSuffix() != null) {
                name += "." + settings.getSuffix();
            }
            settings.setSuffix(null);

        } else {
            name = (String) kwargs.get("suggestedFilename");
        }

        boolean skip = false;
        Path goalPath = Paths.get(settings.getPath()).resolve(name);
        if (goalPath.toFile().exists()) {
            if ("skip".equals(settings.getWhenFileExists())) {
                skip = true;
            } else if ("overwrite".equals(settings.getWhenFileExists())) {
                goalPath.toFile().delete();
            }
        }

        DownloadMission m = new DownloadMission(this, tabId, guid, settings.getPath(), name, (String) kwargs.get("url"), this.savePath);
        this.missions.put(guid, m);

        if (this.getFlag(tabId).equals(false)) {
            cancel(m);
        } else if (skip) {
            skip(m);
        } else {
            this.tabMissions.computeIfAbsent(tabId, k -> new ArrayList<>()).add(m);
        }

        if (getFlag(tabId) != null) flags.put(tabId, m);
    }

    /**
     * 下载状态变化时执行
     */
    private void onDownloadProgress(Object kwargs) {
        JSONObject jsonObject = JSON.parseObject(kwargs.toString());

        if (jsonObject.containsKey("guid") && this.missions.containsKey(jsonObject.getString("guid"))) {
            DownloadMission mission = this.missions.get(jsonObject.getString("guid"));

            if (jsonObject.containsKey("state")) {
                String state = jsonObject.getString("state");
                switch (state) {
                    case "inProgress":
                        mission.receiveBytes = jsonObject.getInteger("receivedBytes");
                        mission.totalBytes = jsonObject.getInteger("totalBytes");
                        break;
                    case "completed":
                        if ("skipped".equals(mission.getState())) {
                            // Perform cleanup for skipped mission
                            // Note: This assumes there is a method like `Path.unlink(true)` for cleanup
                            Path formPath = Paths.get(mission.getSavePath(), mission.getId());
                            formPath.toFile().delete(); // Change to your cleanup logic
                            setDone(mission, "skipped");
                            return;
                        }

                        mission.receiveBytes = jsonObject.getInteger("receivedBytes");
                        mission.totalBytes = jsonObject.getInteger("totalBytes");

                        // Assuming there are methods like `move` and `getUsablePath` in your code
                        String formPath = mission.getSavePath() + File.separator + mission.getId();
                        String toPath = String.valueOf(com.ll.DataRecorder.Tools.getUsablePath(mission.getPath() + File.separator + mission.getName()));
                        try {
                            Files.move(Path.of(formPath), Path.of(toPath));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        setDone(mission, "completed", toPath);
                        break;

                    case "canceled":
                        setDone(mission, "canceled");
                        break;
                    default:
                        // Handle other states if needed
                        break;
                }
            }
        }
    }

}
