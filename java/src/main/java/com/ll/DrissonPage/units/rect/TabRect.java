package com.ll.DrissonPage.units.rect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.page.ChromiumBase;
import com.ll.DrissonPage.units.Coordinate;
import lombok.AllArgsConstructor;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class TabRect {
    private final ChromiumBase page;

    /**
     * @return 返回窗口状态：normal、fullscreen、maximized、 minimized
     */
    public String windowState() {
        return JSONObject.parseObject(this.getWindowRect().toString()).getString("windowState");
    }

    /**
     * @return 返回窗口在屏幕上的坐标，左上角为(0, 0)
     */
    public Coordinate windowLocation() {
        JSONObject jsonObject = JSONObject.parseObject(this.getWindowRect().toString());
        String string = jsonObject.getString("windowState");
        return string.equals("maximized") || string.equals("fullscreen") ? new Coordinate(0, 0) : new Coordinate(jsonObject.getInteger("left") + 7, jsonObject.getInteger("top"));
    }

    /**
     * @return 返回窗口大小
     */
    public Coordinate windowSize() {
        JSONObject jsonObject = JSONObject.parseObject(this.getWindowRect().toString());
        String state = jsonObject.getString("windowState");
        if (state.equals("fullscreen")) {
            return new Coordinate(jsonObject.getInteger("width"), jsonObject.getInteger("height"));
        } else if (state.equals("maximized")) {
            return new Coordinate(jsonObject.getInteger("width") - 16, jsonObject.getInteger("height") - 16);
        } else {
            return new Coordinate(jsonObject.getInteger("width") - 16, jsonObject.getInteger("height") - 7);
        }

    }

    /**
     * @return 返回页面左上角在屏幕中坐标，左上角为(0, 0)
     */
    public Coordinate pageLocation() {
        Coordinate coordinate = this.viewportLocation();
        JSONObject jsonObject = JSONObject.parseObject(this.getPageRect().toString()).getJSONObject("layoutViewport");
        return new Coordinate(coordinate.getX() - jsonObject.getInteger("pageX"), coordinate.getY() - jsonObject.getInteger("pageY"));
    }

    /**
     * @return 返回视口在屏幕中坐标，左上角为(0, 0)
     */
    public Coordinate viewportLocation() {
        Coordinate bl = this.windowLocation();
        Coordinate bs = this.windowSize();
        Coordinate vs = this.viewportSizeWithScrollbar();
        return new Coordinate(bl.getX() + bs.getX() - vs.getX(), bl.getY() + bs.getY() - vs.getY());
    }

    /**
     * @return 返回页面总宽高，格式：(宽, 高)
     */
    public Coordinate size() {
        JSONObject r = JSON.parseObject(this.getPageRect().toString()).getJSONObject("contentSize");
        return new Coordinate(r.getInteger("width"), r.getInteger("height"));
    }

    /**
     * @return 返回视口宽高，不包括滚动条，格式：(宽, 高)
     */
    public Coordinate viewportSize() {
        JSONObject r = JSON.parseObject(this.getPageRect().toString()).getJSONObject("visualViewport");
        return new Coordinate(r.getInteger("clientWidth"), r.getInteger("clientHeight"));
    }

    /**
     * @return 返回视口宽高，包括滚动条，格式：(宽, 高)
     */
    public Coordinate viewportSizeWithScrollbar() {
        String[] split = this.page.runJs("return window.innerWidth.toString() + \" \" + window.innerHeight.toString();").toString().split(" ");
        return new Coordinate(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
    }


    /**
     * @return 获取页面范围信息
     */
    private Object getPageRect() {
        return page.runCdpLoaded("Page.getLayoutMetrics");
    }

    /**
     * @return 获取窗口范围信息
     */
    private Object getWindowRect() {
        return page.browser().getWindowBounds(page.tabId());
    }
}
