package com.ll.DrissonPage.units.setter;

import com.ll.DrissonPage.base.BasePage;
import lombok.AllArgsConstructor;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class BasePageSetter<P extends BasePage<?>> {
    protected final P page;

    /**
     * 设置空元素是否返回设定值
     *
     * @param value 返回的设定值
     * @param onOff 是否启用
     */
    public void NoneElementValue(String value, boolean onOff) {
        this.page.setNoneEleValue(value);
        this.page.setNoneEleReturnValue(onOff);
    }

}
