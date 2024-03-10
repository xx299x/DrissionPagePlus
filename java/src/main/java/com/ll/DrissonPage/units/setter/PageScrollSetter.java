package com.ll.DrissonPage.units.setter;

import com.ll.DrissonPage.units.scroller.PageScroller;
import lombok.AllArgsConstructor;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class PageScrollSetter {
    private final PageScroller scroller;

    /**
     * 设置滚动命令后是否等待完成
     */
    public void waitComplete() {
        this.waitComplete(false);
    }

    /**
     * 设置滚动命令后是否等待完成
     *
     * @param onOff 开或关
     */
    public void waitComplete(boolean onOff) {
        this.scroller.setWaitComplete(onOff);
    }

    /**
     * 设置页面滚动是否平滑滚动
     */
    public void smooth() {
        smooth(false);
    }

    /**
     * 设置页面滚动是否平滑滚动
     *
     * @param onOff 开或关
     */
    public void smooth(boolean onOff) {
        String b = onOff ? "smooth" : "auto";
        if (this.scroller.getDriverPage() != null)
            this.scroller.getDriverPage().runJs("document.documentElement.style.setProperty(\"scroll-behavior\",\"" + b + "\");");
        else
            this.scroller.getDriverEle().runJs("document.documentElement.style.setProperty(\"scroll-behavior\",\"" + b + "\");");
        this.scroller.setWaitComplete(onOff);
    }
}
