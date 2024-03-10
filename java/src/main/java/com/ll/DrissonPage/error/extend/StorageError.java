package com.ll.DrissonPage.error.extend;

import com.ll.DrissonPage.error.BaseError;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */

public class StorageError extends BaseError {
    public StorageError(String info) {
        super(info);
    }

    public StorageError() {
        super("无法操作当前存储数据。");
    }
}
