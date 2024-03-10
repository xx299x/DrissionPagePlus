package com.ll.DrissonPage.error.extend;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class loadFileError extends RuntimeException {
    public loadFileError() {
        super("load file error");
    }

    public loadFileError(String message, Throwable cause) {
        super(message, cause);
    }

    public loadFileError(Throwable cause) {
        super("load file error", cause);
    }

    public loadFileError(String message) {
        super(message);
    }
}
