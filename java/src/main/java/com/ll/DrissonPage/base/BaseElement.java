package com.ll.DrissonPage.base;

import com.ll.DrissonPage.error.extend.ElementNotFoundError;
import com.ll.DrissonPage.functions.Settings;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 各元素类的基类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
public abstract class BaseElement<P extends BasePage<?>, T extends BaseElement<?, ?>> extends BaseParser<T> {
    private final P owner;

    public BaseElement(P page) {
        this.owner = page;
        this.setType("BaseElement");
    }

    /**
     * @return 返回元素标签名
     */
    public abstract String tag();

    /**
     * 返回上面某一级父元素   用查询语法定位
     *
     * @param by 查询选择器
     * @return 上级元素对象
     */
    public T parent(By by) {
        return parent(by, 1);
    }

    /**
     * 返回上面某一级父元素   用查询语法定位
     *
     * @param by    查询选择器
     * @param index 选择第几个结果
     * @return 上级元素对象
     */
    public abstract T parent(By by, Integer index);

    /**
     * 获取上级父元素
     *
     * @return 上级元素对象
     */
    public T parent() {
        return parent(1);
    }

    /**
     * 返回上面某一级父元素，指定层数
     *
     * @param level 第几级父元素
     * @return 上级元素对象
     */
    public abstract T parent(Integer level);

    /**
     * 获取指定定位的第一个父元素
     *
     * @param loc 定位语法
     * @return 上级元素对象
     */
    public T parent(String loc) {
        return parent(loc, 1);
    }

    /**
     * 返回上面某一级父元素   用查询语法定位
     *
     * @param loc   定位符
     * @param index 选择第几个结果
     * @return 上级元素对象
     */
    public abstract T parent(String loc, Integer index);


    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by 用于筛选的查询语法
     * @return 兄弟元素或节点文本
     */
    public T next(By by) {
        return next(by, 1);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by    用于筛选的查询语法
     * @param index 第几个查询结果，0开始
     * @return 兄弟元素或节点文本
     */
    public T next(By by, Integer index) {
        return next(by, index, null);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   第几个查询结果，0开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 兄弟元素或节点文本
     */
    public T next(By by, Integer index, Double timeout) {
        return next(by, index, timeout, true);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   第几个查询结果，0开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本
     */
    public abstract T next(By by, Integer index, Double timeout, Boolean eleOnly);

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @return 兄弟元素或节点文本
     */
    public T next() {
        return next("");
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc 用于筛选的查询语法
     * @return 兄弟元素或节点文本
     */
    public T next(String loc) {
        return next(loc.isEmpty() ? null : loc, 1);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc   用于筛选的查询语法
     * @param index 第几个查询结果，0开始
     * @return 兄弟元素或节点文本
     */
    public T next(String loc, Integer index) {
        return next(loc, index, null);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   第几个查询结果，0开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 兄弟元素或节点文本
     */
    public T next(String loc, Integer index, Double timeout) {
        return next(loc, index, timeout, true);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   第几个查询结果，0开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本
     */
    public abstract T next(String loc, Integer index, Double timeout, Boolean eleOnly);

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index 第几个查询结果，0开始
     * @return 兄弟元素或节点文本
     */

    public T next(Integer index) {
        return next(index, null);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   第几个查询结果，0开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 兄弟元素或节点文本
     */
    public T next(Integer index, Double timeout) {
        return next(index, timeout, true);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   第几个查询结果，0开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本
     */
    public abstract T next(Integer index, Double timeout, Boolean eleOnly);


    @Override
    public List<T> _ele(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr, String method) {
        return __ele(this.findElements(by, timeout, index, relative, raiseErr), by.getValue(), index, raiseErr, method);
    }

    @Override
    public List<T> _ele(String str, Double timeout, Integer index, Boolean relative, Boolean raiseErr, String method) {
        return __ele(this.findElements(str, timeout, index, relative, raiseErr), str, index, raiseErr, method);
    }

    private List<T> __ele(List<T> elements, String str, Integer index, Boolean raiseErr, String method) {
        //如果index为空则说明是查找多元素，如果不为空，则说明是单元素查找，直接判断是否为空，如果不为空则说明单元素找到了
        if (index == null || !elements.isEmpty()) return elements;
        //如果是单元素，是否抛出异常
        if (Settings.raiseWhenEleNotFound || raiseErr != null && raiseErr) {
            Map<String, Object> locOrStr = new HashMap<>();
            locOrStr.put("loc_or_str", str);
            locOrStr.put("index", index);
            throw new ElementNotFoundError(null, method, locOrStr);
        }
        //如果不抛出异常则直接创建个空的
        return new ArrayList<>();
    }

}
