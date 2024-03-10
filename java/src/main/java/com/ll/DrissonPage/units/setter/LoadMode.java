package com.ll.DrissonPage.units.setter;

import com.ll.DrissonPage.page.ChromiumBase;
import lombok.AllArgsConstructor;

/**
 * 用于设置页面加载策略的类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class LoadMode {
    private final ChromiumBase page;

    /**
     * 设置加载策略  如果传入错误则是normal
     *
     * @param mode value: 可选 'normal', 'eager', 'none'
     */
    public void load(String mode) {
        mode = mode != null ? mode.trim().toLowerCase() : "normal";
        if (!mode.equals("normal") && !mode.equals("eager") && !mode.equals("none")) mode = "normal";
        this.page.setLoadMode(mode);
    }

    /**
     * 设置页面加载策略为normal
     */
    public void normal() {
        this.page.setLoadMode("normal");
    }

    /**
     * 设置页面加载策略为eager
     */
    public void eager() {
        this.page.setLoadMode("eager");
    }

    /**
     * 设置页面加载策略为none
     */
    public void none() {
        this.page.setLoadMode("none");
    }

}
