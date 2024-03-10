package com.ll.DrissonPage.error.extend;


import com.ll.DrissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */

public class PageDisconnectedError extends BaseError {

    public PageDisconnectedError() {
        super("与页面的连接已断开");
    }

    public PageDisconnectedError(String info) {
        super(info);
    }
}
