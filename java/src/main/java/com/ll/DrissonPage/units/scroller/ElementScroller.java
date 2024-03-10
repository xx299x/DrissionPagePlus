package com.ll.DrissonPage.units.scroller;

import com.ll.DrissonPage.element.ChromiumElement;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class ElementScroller extends Scroller {

    public ElementScroller(ChromiumElement driverEle) {
        super(driverEle);
    }

    /**
     * 滚动页面直到元素可见
     */
    public void toSee() {
        this.toSee(null);
    }

    /**
     * 滚动页面直到元素可见
     *
     * @param center 是否尽量滚动到页面正中，为null时如果被遮挡，则滚动到页面正中
     */
    public void toSee(Boolean center) {
        this.getDriverEle().getOwner().scroll().toSee(this.getDriverEle(), center);
    }

    /**
     * 元素尽量滚动到视口中间
     */
    public void toCenter() {
        this.getDriverEle().getOwner().scroll().toSee(this.getDriverEle(), true);
    }

}
