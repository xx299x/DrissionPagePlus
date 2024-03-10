package com.ll.DrissonPage.units.downloader;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
@Setter
public class TabDownloadSettings {
    protected static final Map<String, TabDownloadSettings> TAB_DOWNLOAD_SETTINGS_MAP = new HashMap<>();
    protected String rename;
    protected String suffix;
    protected String path;
    protected String whenFileExists;
    private String tabId;

    private TabDownloadSettings(String tabId) {
        this.tabId = tabId;
        this.rename = null;
        this.suffix = null;
        this.path = "";
        this.whenFileExists = "rename";
    }

    public static TabDownloadSettings getInstance(String tabId) {
        return TAB_DOWNLOAD_SETTINGS_MAP.computeIfAbsent(tabId, TabDownloadSettings::new);
    }
}
