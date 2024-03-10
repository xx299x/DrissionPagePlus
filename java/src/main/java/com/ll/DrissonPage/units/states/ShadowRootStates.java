package com.ll.DrissonPage.units.states;

import com.ll.DrissonPage.element.ShadowRoot;
import lombok.AllArgsConstructor;

import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class ShadowRootStates {
    private final ShadowRoot ele;

    /**
     * @return 返回元素是否可用
     */
    public boolean isEnabled() {
        return Boolean.parseBoolean(this.ele.runJs("return this.disabled;").toString());
    }

    /**
     * @return 返回元素是否仍在DOM中
     */
    public boolean isAlive() {
        try {
            this.ele.getOwner().runCdp("DOM.describeNode", Map.of("backendNodeId", this.ele.getBackendId()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
