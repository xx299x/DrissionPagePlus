package com.ll.DownloadKit;

import lombok.Getter;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
public enum FileMode {
    ADD("add"), add(ADD.value), SKIP("skip"), skip(SKIP.value), RENAME("rename"), rename(RENAME.value), OVERWRITE("overwrite"), overwrite(OVERWRITE.value), a(ADD.value), A(ADD.value), S(SKIP.value), s(SKIP.value), r(RENAME.value), R(RENAME.value), o(OVERWRITE.value), O(OVERWRITE.value);
    private final String value;

    FileMode(String value) {
        this.value = value;
    }

}
