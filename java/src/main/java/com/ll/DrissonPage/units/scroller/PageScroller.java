package com.ll.DrissonPage.units.scroller;

import com.ll.DrissonPage.base.By;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.page.ChromiumBase;

import java.util.List;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class PageScroller extends Scroller {
    public PageScroller(ChromiumBase driverPage) {
        super(driverPage);
        this.setT1("window");
        this.setT2("document.documentElement");
    }

    public PageScroller(ChromiumElement driverPage) {
        super(driverPage);
        this.setT1("window");
        this.setT2("document.documentElement");
    }

    /**
     * 滚动页面直到元素可见
     *
     * @param loc 元素的定位信息
     */
    public void toSee(String loc) {
        toSee(loc, null);
    }

    /**
     * 滚动页面直到元素可见
     *
     * @param by 元素的定位信息
     */
    public void toSee(By by) {
        toSee(by, null);
    }

    /**
     * 滚动页面直到元素可见
     *
     * @param loc    元素的定位信息
     * @param center 是否尽量滚动到页面正中，为None时如果被遮挡，则滚动到页面正中
     */
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
    public void toSee(ChromiumElement ele, Boolean center) {
        String txt = center != null && center ? "true" : "false";
        ele.runJs("this.scrollIntoViewIfNeeded(" + txt + ");");
        if (center != null && center || (!Boolean.FALSE.equals(center) && ele.states().isChecked())) {
            ele.runJs(
                    "function getWindowScrollTop() {var scroll_top = 0;\n" +
                    "                    if (document.documentElement && document.documentElement.scrollTop) {\n" +
                    "                      scroll_top = document.documentElement.scrollTop;\n" +
                    "                    } else if (document.body) {scroll_top = document.body.scrollTop;}\n" +
                    "                    return scroll_top;}\n" + "            const { top, height } = this.getBoundingClientRect();\n" +
                    "                    const elCenter = top + height / 2;\n" +
                    "                    const center = window.innerHeight / 2;\n" +
                    "                    window.scrollTo({top: getWindowScrollTop() - (center - elCenter),\n" +
                    "                    behavior: 'instant'});"
            );
        }
        this.waitScrolled();
    }

}
