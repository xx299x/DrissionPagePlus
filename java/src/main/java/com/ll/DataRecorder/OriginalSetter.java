package com.ll.DataRecorder;

import lombok.AllArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class OriginalSetter<R extends OriginalRecorder> {
    protected final R recorder;


    /**
     * 设置缓存大小
     *
     * @param size 缓存大小
     */
    public void cacheSize(int size) {
        if (size < 0) return;
        this.recorder.cache = size;
    }

    /**
     * 设置文件路径
     *
     * @param path 文件路径
     */
    public void path(String path) {
        if (this.recorder.path() != null) this.recorder.record();
        this.recorder.path = path;
        this.recorder.data = new ArrayList<>();
    }

    /**
     * 设置文件路径
     *
     * @param path 文件路径
     */
    public void path(Path path) {
        if (this.recorder.path() != null) this.recorder.record();
        this.recorder.path = path.toString();
        this.recorder.data = new ArrayList<>();
    }

    /**
     * 设置是否显示运行信息
     *
     * @param onOff 开关
     */

    public void showMsg(boolean onOff) {
        this.recorder.showMsg = onOff;
    }
}
