package com.ll.DrissonPage.units.rect;

import com.ll.DrissonPage.page.ChromiumFrame;
import com.ll.DrissonPage.units.Coordinate;

import java.util.List;


/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class FrameRect extends TabRect {
    private final ChromiumFrame frame;

    public FrameRect(ChromiumFrame frame) {
        super(frame.getTargetPage());
        this.frame = frame;
    }


    /**
     * @return 无效方法只为兼容
     */
    @Override
    public String windowState() {
        return null;
    }

    /**
     * @return 无效方法只为兼容
     */
    @Override
    public Coordinate windowLocation() {
        return null;

    }

    /**
     * @return 无效方法只为兼容
     */
    @Override
    public Coordinate windowSize() {
        return null;

    }

    /**
     * @return 无效方法只为兼容
     */
    @Override
    public Coordinate pageLocation() {
        return null;

    }

    /**
     * @return 无效方法只为兼容
     */
    @Override
    public Coordinate viewportSizeWithScrollbar() {
        return null;
    }

    /**
     * @return 返回iframe元素左上角的绝对坐标
     */
    public Coordinate location() {
        return this.frame.getFrameEle().rect().location();
    }

    /**
     * @return 返回元素在视口中坐标，左上角为(0, 0)
     */
    public Coordinate viewportLocation() {
        return this.frame.getFrameEle().rect().viewportLocation();
    }

    /**
     * @return 返回元素左上角在屏幕上坐标，左上角为(0, 0)
     */
    public Coordinate screenLocation() {
        return this.frame.getFrameEle().rect().screenLocation();
    }

    /**
     * @return 返回frame内页面尺寸，格式：(宽, 高)
     */
    public Coordinate size() {
        String w = this.frame.getDocEle().runJs("return this.body.scrollWidth").toString();
        String h = this.frame.getDocEle().runJs("return this.body.scrollHeight").toString();
        return new Coordinate(Integer.parseInt(w), Integer.parseInt(h));
    }

    /**
     * @return 返回视口宽高，格式：(宽, 高)
     */
    public Coordinate viewportSize() {
        return this.frame.getFrameEle().rect().size();
    }

    /**
     * @return 返回元素四个角坐标，顺序：坐上、右上、右下、左下
     */
    public List<Coordinate> corners() {
        return this.frame.getFrameEle().rect().corners();
    }

    /**
     * @return 返回iframe元素左上角的绝对坐标
     */
    public List<Coordinate> viewportCorners() {
        return this.frame.getFrameEle().rect().viewportCorners();
    }
}
