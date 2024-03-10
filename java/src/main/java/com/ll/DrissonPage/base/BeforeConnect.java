package com.ll.DrissonPage.base;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
@Getter
public  class BeforeConnect {
    private Integer retry;
    private Double interval;
    private boolean isFile;
}
