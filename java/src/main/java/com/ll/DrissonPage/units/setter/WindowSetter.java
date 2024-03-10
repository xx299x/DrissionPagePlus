package com.ll.DrissonPage.units.setter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.page.ChromiumBase;
import com.ll.DrissonPage.units.Coordinate;

import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class WindowSetter {
    protected final ChromiumBase page;
    private final Integer windowId;

    /**
     * @param page 页面对象
     */
    public WindowSetter(ChromiumBase page) {
        this.page = page;
        this.windowId = this.getInfo().getInteger("windowId");
    }

    /**
     * 窗口最大化
     */
    public void max() {
        String s = this.getInfo().getJSONObject("bounds").getString("windowState");
        if ("fullscreen".equals(s) || "minimized".equals(s)) this.perform(Map.of("windowState", "normal"));
        this.perform(Map.of("windowState", "maximized"));
    }

    /**
     * 窗口最小化
     */
    public void min() {
        String s = this.getInfo().getJSONObject("bounds").getString("windowState");
        if ("fullscreen".equals(s)) this.perform(Map.of("windowState", "normal"));
        this.perform(Map.of("windowState", "minimized"));
    }

    /**
     * 设置窗口为全屏
     */
    public void full() {
        String s = this.getInfo().getJSONObject("bounds").getString("windowState");
        if ("minimized".equals(s)) this.perform(Map.of("windowState", "normal"));
        this.perform(Map.of("windowState", "fullscreen"));
    }

    /**
     * 设置窗口为常规模式
     */
    public void normal() {
        String s = this.getInfo().getJSONObject("bounds").getString("windowState");
        if ("fullscreen".equals(s)) this.perform(Map.of("windowState", "normal"));
        this.perform(Map.of("windowState", "normal"));
    }

    /**
     * @return 获取窗口位置及大小信息
     */
    private JSONObject getInfo() {
        for (int i = 0; i < 50; i++) {
            try {
                return JSON.parseObject(this.page.runCdp("Browser.getWindowForTarget").toString());
            } catch (Exception e) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return new JSONObject();
    }

    /**
     * 设置窗口大小 (其中一个可以设置为null)
     *
     * @param coordinate 窗口宽度+高度
     */
    public void size(Coordinate coordinate) {
        if (coordinate != null) size(coordinate.getX(), coordinate.getY());
    }

    /**
     * 设置窗口大小 (其中一个可以设置为null)
     *
     * @param width  窗口宽度
     * @param height 窗口高度
     */
    public void size(Integer width, Integer height) {
        if (width != null || height != null) {
            String s = this.getInfo().getJSONObject("bounds").getString("windowState");
            if (!"normal".equals(s)) this.perform(Map.of("windowState", "normal"));
            JSONObject info = this.getInfo().getJSONObject("bounds");
            width = width != null ? width - 16 : info.getInteger("width");
            height = height != null ? height - 7 : info.getInteger("height");
            this.perform(Map.of("width", width, "height", height));
        }
    }
    /**
     * 设置窗口在屏幕中的位置，相对左上角坐标
     *
     * @param coordinate 距离顶部距离和 距离左边距离
     */
    public void location(Coordinate coordinate) {
        if (coordinate != null) location(coordinate.getX(), coordinate.getY());
    }
    /**
     * 设置窗口在屏幕中的位置，相对左上角坐标
     *
     * @param x 距离顶部距离
     * @param y 距离左边距离
     */
    public void location(Integer x, Integer y) {
        if (x != null || y != null) {
            this.normal();
            JSONObject info = this.getInfo().getJSONObject("bounds");
            x = x != null ? x : info.getInteger("left");
            y = y != null ? y : info.getInteger("top");
            this.perform(Map.of("left", x - 8, "top", y));
        }
    }

    /**
     * 执行改变窗口大小操作
     *
     * @param bounds 控制数据
     */
    private void perform(Object bounds) {
        try {
            this.page.runCdp("Browser.setWindowBounds", Map.of("windowId", this.windowId, "bounds", bounds));
        } catch (Exception e) {
            throw new RuntimeException("浏览器全屏或最小化状态时请先调用set.window.normal()恢复正常状态。");
        }
    }
}
