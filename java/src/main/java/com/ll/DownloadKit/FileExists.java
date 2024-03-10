package com.ll.DownloadKit;

import lombok.AllArgsConstructor;

/**
 * 用于设置存在同名文件时处理方法
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class FileExists {
    private final Setter setter;

    public void set(FileMode fileMode) {
        this.setter.downloadKit.fileMode = fileMode;
    }

    /**
     * 设为跳过
     */
    public void skip() {
        this.setter.downloadKit.fileMode = FileMode.SKIP;
    }

    /**
     * 设为重命名，文件名后加序号
     */
    public void rename() {
        this.setter.downloadKit.fileMode = FileMode.rename;
    }

    /**
     * 设为覆盖
     */
    public void overwrite() {
        this.setter.downloadKit.fileMode = FileMode.overwrite;
    }

    /**
     * 设为追加
     */
    public void add() {
        this.setter.downloadKit.fileMode = FileMode.add;
    }
}
