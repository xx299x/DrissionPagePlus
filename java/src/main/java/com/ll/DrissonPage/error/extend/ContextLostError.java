package com.ll.DrissonPage.error.extend;

import com.ll.DrissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */

public class ContextLostError extends BaseError {
    public ContextLostError(String info) {
        super(info);
    }

    public ContextLostError() {
        super("页面被刷新，请操作前尝试等待页面刷新或加载完成。");
    }
}
