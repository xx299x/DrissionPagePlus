package com.ll.DrissonPage.units.listener;

import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class FailInfo {
    private final DataPacket dataPacket;
    private final Map<String, Object> failInfo;

    public FailInfo(DataPacket dataPacket, Map<String, Object> failInfo) {
        this.dataPacket = dataPacket;
        this.failInfo = failInfo;
    }

    public Object get(Object item) {
        if (failInfo != null && !failInfo.isEmpty()) return this.failInfo.get(item.toString());
        return null;
    }
}
