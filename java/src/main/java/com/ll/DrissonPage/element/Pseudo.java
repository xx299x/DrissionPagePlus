package com.ll.DrissonPage.element;

import lombok.AllArgsConstructor;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class Pseudo {
    private final ChromiumElement chromiumElement;

    /**
     * @return 返回当前元素的::before伪元素内容
     */
    public String before() {
        return chromiumElement.style("content", "before");
    }

    /**
     * @return 返回当前元素的::after伪元素内容
     */
    public String after() {
        return chromiumElement.style("content", "after");
    }
}
