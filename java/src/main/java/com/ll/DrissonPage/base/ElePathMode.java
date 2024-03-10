package com.ll.DrissonPage.base;

import lombok.Getter;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
public enum ElePathMode {
    C("css"), CSS("css"), X("xpath"), XPATH("xpath");
    private final String mode;

    ElePathMode(String mode) {
        this.mode = mode;
    }
}
