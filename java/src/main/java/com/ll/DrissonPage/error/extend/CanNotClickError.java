package com.ll.DrissonPage.error.extend;

import com.ll.DrissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class CanNotClickError extends BaseError {
    public CanNotClickError(String info) {
        super(info);
    }

    public CanNotClickError() {
        super("该元素无法滚动到视口或被遮挡，无法点击。");
    }
}
