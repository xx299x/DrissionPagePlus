package com.ll.DrissonPage.units.screencast;

import lombok.AllArgsConstructor;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class ScreencastMode {
    private final Screencast screencast;

    /**
     * 持续视频模式，生成的视频没有声音
     */
    public void videoMode() {
        this.screencast.mode = "video";
    }

    /**
     * 持续视频模式，生成的视频没有声音
     */
    public void frugalVideoMode() {
        this.screencast.mode = "frugal_video";
    }

    /**
     * 设置使用js录制视频模式，可生成有声音的视频，但需要手动启动
     */
    public void jsVideoMode() {
        this.screencast.mode = "js_video";
    }

    /**
     * 设置节俭视频模式，页面有变化时才截图
     */
    public void frugalImgMode() {
        this.screencast.mode = "frugal_imgs";
    }

    /**
     * 设置图片模式，持续对页面进行截图
     */
    public void imgMode() {
        this.screencast.mode = "imgs";
    }
}
