package com.ll.DrissonPage.units.setter;

import com.ll.DrissonPage.page.ChromiumFrame;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class ChromiumFrameSetter extends ChromiumBaseSetter {
    public ChromiumFrameSetter(ChromiumFrame page) {
        super(page);
    }

    /**
     * 设置frame元素attribute属性
     *
     * @param name  属性名
     * @param value 属性值
     */
    public void attr(String name, String value) {
        ((ChromiumFrame) this.page).frameEle().set().attr(name, value);
    }
}
