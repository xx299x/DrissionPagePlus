package com.ll.DrissonPage.error.extend;

import com.ll.DrissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class CDPError extends BaseError {
    public CDPError(String info) {
        super(info);
    }

    public CDPError() {
        super("方法调用错误.");
    }
}
