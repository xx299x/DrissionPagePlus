package com.ll.DrissonPage.error.extend;

import com.ll.DrissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class JavaScriptError extends BaseError {
    public JavaScriptError(String info) {
        super(info);
    }

    public JavaScriptError() {
        super("JavaScript运行错误.");
    }
}
