package com.ll.DrissonPage.units;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
@AllArgsConstructor
public enum PicType {
    JPG("jpg"), JPEG("jpeg"), PNG("png"), WEBP("webp"), DEFAULT("png");
    final String value;

}
