package com.ll.DrissonPage.units.listener;

import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class ExtraInfo {
    Map<String, Object> extraInfo;

    /**
     * @return 以map形式返回所有额外信息
     */
    public Map<String, Object> allInfo() {
        return extraInfo;
    }

    /**
     * @param item 获取单独的额外信息
     */
    public Object getInfo(Object item) {
        return extraInfo.get(item.toString());
    }
}
