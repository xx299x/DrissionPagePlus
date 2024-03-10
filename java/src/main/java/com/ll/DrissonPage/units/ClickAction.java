package com.ll.DrissonPage.units;

import lombok.Getter;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
public enum ClickAction {
    LEFT("left"), RIGHT("right"), MIDDLE("middle"), BACK("back"), FORWARD("forward");
    private final String value;

    ClickAction(String value) {
        this.value = value;
    }
}