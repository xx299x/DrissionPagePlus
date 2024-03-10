package com.ll.DrissonPage.error.extend;

import com.ll.DrissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class ElementLostError extends BaseError {
    public ElementLostError(String info) {
        super(info);
    }

    public ElementLostError() {
        super("元素对象已失效。可能是页面整体刷新，或js局部刷新把元素替换或去除了。");
    }
}
