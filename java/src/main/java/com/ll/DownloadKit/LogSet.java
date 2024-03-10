package com.ll.DownloadKit;

import com.ll.DataRecorder.Recorder;
import lombok.AllArgsConstructor;

import java.nio.file.Path;

/**
 * 用于设置信息打印和记录日志方式
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class LogSet {
    private final Setter setter;

    /**
     * 设置日志文件路径
     *
     * @param path 文件路径，可以是str或Path
     */
    public void path(String path) {
        if (this.setter.downloadKit.getLogger() != null) this.setter.downloadKit.getLogger().record();
        this.setter.downloadKit.logger= new Recorder(path);
    }

    /**
     * 设置日志文件路径
     *
     * @param path 文件路径，可以是str或Path
     */

    public void path(Path path) {
        if (this.setter.downloadKit.getLogger() != null) this.setter.downloadKit.getLogger().record();
        this.setter.downloadKit.logger= new Recorder(path);
    }

    /**
     * 打印所有信息
     */
    public void printAll() {
        this.setter.downloadKit.printMode= "all";
    }

    /**
     * 只有在下载失败时打印信息
     */
    public void printFailed() {
        this.setter.downloadKit.printMode= "failed";
    }


    /**
     * 不打印任何信息
     */
    public void printNull() {
        this.setter.downloadKit.printMode= null;
    }

    /**
     * 记录所有信息
     */
    public void logAll() {
        if (this.setter.downloadKit.getLogger() == null) throw new RuntimeException("请先用logPath()设置log文件路径。");
        this.setter.downloadKit.logMode= "all";
    }

    /**
     * 只记录下载失败的信息
     */
    public void logFailed() {
        if (this.setter.downloadKit.getLogger() == null) throw new RuntimeException("请先用logPath()设置log文件路径。");
        this.setter.downloadKit.logMode= "failed";
    }

    /**
     * 不进行记录
     */
    public void logNull() {
        if (this.setter.downloadKit.getLogger() == null) throw new RuntimeException("请先用logPath()设置log文件路径。");
        this.setter.downloadKit.logMode= null;
    }


}
