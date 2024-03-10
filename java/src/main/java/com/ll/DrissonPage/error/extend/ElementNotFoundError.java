package com.ll.DrissonPage.error.extend;

import com.ll.DrissonPage.error.BaseError;

import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
public class ElementNotFoundError extends BaseError {
    private String method;
    private Map<String, Object> arguments;

    public ElementNotFoundError(String method, Map<String, Object> arguments) {
        this();
        this.method = method;
        this.arguments = arguments;
    }

    public ElementNotFoundError(String errorInfo, String method, Map<String, Object> arguments) {
        super(errorInfo);
        this.method = method;
        this.arguments = arguments;
    }

    public ElementNotFoundError(String errorInfo) {
        super(errorInfo);
    }

    public ElementNotFoundError() {
        this("没有找到该元素");
    }

    @Override
    public String toString() {
        return super.toString() + ((method != null && !method.isEmpty()) ? "\nmethod: " + method : "") + ((arguments != null && !arguments.isEmpty()) ? "\nargs: " + arguments : "");
    }
}
