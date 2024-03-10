package com.ll.DrissonPage.base;

import lombok.Getter;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
public enum BySelect {
    NAME("name"), ID("id"), CLASS_NAME("class name"), TAG_NAME("tag name"), CSS_SELECTOR("css selector"),
    XPATH("xpath"), LINK_TEXT("link text"), PARTIAL_LINK_TEXT("partial link text"), TEXT("text"), PARTIAL_TEXT("partial text");
    private final String name;

    BySelect(String name) {
        this.name = name;
    }
}
