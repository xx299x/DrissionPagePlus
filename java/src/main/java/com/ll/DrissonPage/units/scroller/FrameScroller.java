package com.ll.DrissonPage.units.scroller;

import com.ll.DrissonPage.base.By;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.page.ChromiumFrame;

import java.util.List;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class FrameScroller extends PageScroller {
    public FrameScroller(ChromiumFrame chromiumFrame) {
        super(chromiumFrame.getDocEle());
        //
        this.setT1("this.documentElement");
        this.setT2("this.documentElement");
    }

    /**
     * 滚动页面直到元素可见
     *
     * @param loc 元素的定位信息
     */
    @Override
    public void toSee(String loc) {
        toSee(loc, null);
    }

    /**
     * 滚动页面直到元素可见
     *
     * @param by 元素的定位信息
     */
    @Override
    public void toSee(By by) {
        toSee(by, null);
    }

    /**
     * 滚动页面直到元素可见
     *
     * @param loc    元素的定位信息
     * @param center 是否尽量滚动到页面正中，为None时如果被遮挡，则滚动到页面正中
     */
    @Override
    public void toSee(String loc, Boolean center) {
        List<ChromiumElement> list = this.getDriverEle() == null ? this.getDriverPage()._ele(loc, null, null, null, null, null) : this.getDriverEle()._ele(loc, null, null, null, null, null);
        if (list != null && !list.isEmpty()) toSee(list.get(0), center);
    }

    /**
     * 滚动页面直到元素可见
     *
     * @param by     元素的定位信息
     * @param center 是否尽量滚动到页面正中，为None时如果被遮挡，则滚动到页面正中
     */
    @Override
    public void toSee(By by, Boolean center) {
        List<ChromiumElement> list = this.getDriverEle() == null ? this.getDriverPage()._ele(by, null, null, null, null, null) : this.getDriverEle()._ele(by, null, null, null, null, null);
        if (list != null && !list.isEmpty()) toSee(list.get(0), center);
    }

    /**
     * 滚动页面直到元素可见
     *
     * @param ele 元素对象
     */
    public void toSee(ChromiumElement ele) {
        this.toSee(ele, null);
    }

    /**
     * 滚动页面直到元素可见
     *
     * @param ele    元素对象
     * @param center 是否尽量滚动到页面正中，为None时如果被遮挡，则滚动到页面正中
     */
    @Override
    public void toSee(ChromiumElement ele, Boolean center) {
        super.toSee(ele, center);
    }
}
