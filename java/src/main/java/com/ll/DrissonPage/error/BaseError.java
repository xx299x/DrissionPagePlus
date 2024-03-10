package com.ll.DrissonPage.error;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class BaseError extends RuntimeException {
    private final String info;

    public BaseError(String info) {
        super(info);
        this.info = info;
    }

    @Override
    public String toString() {
        return info;
    }
}
