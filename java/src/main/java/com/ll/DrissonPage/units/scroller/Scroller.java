package com.ll.DrissonPage.units.scroller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.page.ChromiumBase;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Scroller {
    @Setter
    private String t1;
    @Setter
    private String t2;

    /**
     * 和ele只能存活一个
     */
    @Getter
    private ChromiumBase driverPage;
    /**
     * 和page只能存活一个
     */
    @Getter
    private ChromiumElement driverEle;
    @Setter
    private boolean waitComplete;

    public Scroller(ChromiumBase driverPage) {
        this.t1 = this.t2 = "this";
        this.driverPage = driverPage;
        this.waitComplete = false;
    }

    public Scroller(ChromiumElement driverEle) {
        this.t1 = this.t2 = "this";
        this.driverEle = driverEle;
        this.waitComplete = false;
    }

    private void runJs(String js) {
        js = String.format(js, t1, t2, t2);
        if (this.driverPage != null) this.driverPage.runJs(js);
        else this.driverEle.runJs(js);
        this.waitScrolled();
    }

    /**
     * 滚动到顶端，水平位置不变
     */
    public void toTop() {
        this.runJs("{}.scrollTo({}.scrollLeft, 0);");
    }

    /**
     * 滚动到底端，水平位置不变
     */
    public void toBottom() {
        runJs("{}.scrollTo({}.scrollLeft, {}.scrollHeight);");
    }

    /**
     * 滚动到垂直中间位置，水平位置不变
     */
    public void toHalf() {
        runJs("{}.scrollTo({}.scrollLeft, {}.scrollHeight/2);");
    }

    /**
     * 滚动到最右边，垂直位置不变
     */
    public void toRightmost() {
        runJs("{}.scrollTo({}.scrollWidth, {}.scrollTop);");
    }

    /**
     * 滚动到最左边，垂直位置不变
     */
    public void toLeftmost() {
        runJs("{}.scrollTo(0, {}.scrollTop);");
    }

    /**
     * 滚动到指定位置
     *
     * @param x 水平距离
     * @param y 垂直距离
     */
    public void toLocation(int x, int y) {
        runJs("{}.scrollTo(" + x + ", " + y + ");");
    }

    /**
     * 向上滚动若干像素，水平位置不变
     *
     * @param pixel 滚动的像素
     */
    public void up(int pixel) {
        pixel = -pixel;
        runJs("{}.scrollBy(0, " + pixel + ");");
    }

    /**
     * 向下滚动若干像素，水平位置不变
     *
     * @param pixel 滚动的像素
     */
    public void down(int pixel) {
        runJs("{}.scrollBy(0, " + pixel + ");");
    }

    /**
     * 向左滚动若干像素，垂直位置不变
     *
     * @param pixel 滚动的像素
     */
    public void left(int pixel) {
        pixel = -pixel;
        runJs("{}.scrollBy(" + pixel + ", 0);");
    }

    /**
     * 向右滚动若干像素，垂直位置不变
     *
     * @param pixel 滚动的像素
     */
    public void right(int pixel) {
        runJs("{}.scrollBy(" + pixel + ", 0);");
    }

    protected void waitScrolled() {
        if (!waitComplete) {
            return;
        }
        JSONObject jsonObject = JSON.parseObject((driverPage != null ? driverPage.runCdp("Page.getLayoutMetrics") : driverEle.getOwner().runCdp("Page.getLayoutMetrics")).toString()).getJSONObject("layoutViewport");
        Object x = jsonObject.get("pageX");
        Object y = jsonObject.get("pageY");
        long timeout = (long) (System.currentTimeMillis() + (driverPage != null ? driverPage.timeout() * 1000 : driverEle.getOwner().timeout() * 1000));
        while (System.currentTimeMillis() < timeout) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            jsonObject = JSON.parseObject((driverPage != null ? driverPage.runCdp("Page.getLayoutMetrics") : driverEle.getOwner().runCdp("Page.getLayoutMetrics")).toString()).getJSONObject("layoutViewport");

            Object x1 = jsonObject.get("pageX");
            Object y1 = jsonObject.get("pageY");
            if (x == x1 && y == y1) break;

            x = x1;
            y = y1;
        }
    }


}
