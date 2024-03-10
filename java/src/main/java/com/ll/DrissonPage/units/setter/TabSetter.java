package com.ll.DrissonPage.units.setter;

import com.ll.DrissonPage.page.ChromiumBase;

import java.nio.file.Path;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class TabSetter extends ChromiumBaseSetter {
    public TabSetter(ChromiumBase page) {
        super(page);
    }

    protected WindowSetter windowSetter;

    /**
     * @return 返回用于设置浏览器窗口的对象
     */
    public WindowSetter window() {
        if (windowSetter == null) windowSetter = new WindowSetter(super.page);
        return windowSetter;
    }

    /**
     * 设置下载路径
     *
     * @param path 下载路径
     */
    public void downloadPath(Path path) {
        downloadPath(path.toAbsolutePath().toString());
    }

    /**
     * 设置下载路径
     *
     * @param path 下载路径
     */
    public void downloadPath(String path) {
        this.page.setDownloadPath(path);
        this.page.browser().getDlMgr().setPath(this.page, path);
        if (this.page.getDownloadKit() != null) this.page.getDownloadKit().set().goalPath(path);
    }

    /**
     * 设置下一个被下载文件的名称
     */
    public void downloadFileName() {
        this.downloadFileName(null);
    }

    /**
     * 设置下一个被下载文件的名称
     *
     * @param name 文件名，可不含后缀，会自动使用远程文件后缀
     */
    public void downloadFileName(String name) {
        this.downloadFileName(name, null);
    }

    /**
     * 设置下一个被下载文件的名称
     *
     * @param name   文件名，可不含后缀，会自动使用远程文件后缀
     * @param suffix 后缀名，显式设置后缀名，不使用远程文件后缀
     */
    public void downloadFileName(String name, String suffix) {
        this.page.browser().getDlMgr().setRename(this.page.tabId(), name, suffix);
    }

    /**
     * 设置当存在同名文件时的处理方式
     *
     * @param fileMode 可在 'rename', 'overwrite', 'skip',缩写 'r', 'o', 's'中选择
     */
    public void whenDownloadFileExists(FileMode fileMode) {
        Map<String, String> types = Map.of("rename", "rename", "overwrite", "overwrite", "skip", "skip", "r", "rename", "o", "overwrite", "s", "skip");
        this.page.browser().getDlMgr().setFileExists(this.page.tabId(), types.get(fileMode.mode));
    }

    /**
     * 使标签页处于最前面
     */
    public void activate() {
        this.page.browser().activateTab(this.page.tabId());
    }


    public enum FileMode {
        RENAME("rename"), OVERWRITE("overwrite"), SKIP("skip"), R("r"), O("o"), S("s");
        private final String mode;

        FileMode(String mode) {
            this.mode = mode;
        }
    }
}
