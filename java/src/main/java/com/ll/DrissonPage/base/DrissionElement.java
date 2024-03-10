package com.ll.DrissonPage.base;

import com.ll.DrissonPage.error.extend.ElementNotFoundError;
import com.ll.DrissonPage.functions.Locator;
import com.ll.DrissonPage.functions.Settings;
import com.ll.DrissonPage.functions.Web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * ChromiumElement 和 SessionElement的基类  但不是ShadowRoot的基类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public abstract class DrissionElement<P extends BasePage<?>, T extends DrissionElement<?, ?>> extends BaseElement<P, T> {

    public DrissionElement(P page) {
        super(page);
        this.setType("DrissionElement");
    }

    /**
     * 返回href或者src绝对的url
     */
    public String link() {
        String href = this.attr("href");
        return href == null ? this.attr("src") : href;
    }

    /**
     * 返回css path路径
     */
    public String cssPath() {
        return this.getElePath(ElePathMode.CSS);
    }

    /**
     * 返回xpath路径
     */
    public String xpath() {
        return this.getElePath(ElePathMode.XPATH);
    }

    /**
     * 返回元素注释文本组成的列表
     */
    public List<T> comments() {
        return this.eles("xpath:.//comment()");
    }

    /**
     * 返回元素内所有直接子节点的文本，包括元素和文本节点
     *
     * @return 文本列表
     */
    public List<String> texts() {
        return this.texts(false);
    }

    /**
     * 返回元素内所有直接子节点的文本，包括元素和文本节点
     *
     * @param textNodeOnly 是否只返回文本节点
     * @return 文本列表
     */
    public List<String> texts(boolean textNodeOnly) {
        List<String> texts = new ArrayList<>();
        if (textNodeOnly) {
            this.eles("xpath:/text()").forEach(a -> texts.add(a.text()));
        } else {
            this.eles("xpath:/text() | *").forEach(a -> texts.add(a.text()));
        }
        return texts.stream().filter(text -> text != null && !Pattern.compile("[\r\n\t ]").matcher(text).replaceAll("").isEmpty()).map(text -> Web.formatHtml(text.trim().replaceAll("[\r\n]", ""))).collect(Collectors.toList());
    }

    /**
     * 返回上面某一级父元素   用查询语法定位
     *
     * @param by    查询选择器
     * @param index 选择第几个结果
     * @return 上级元素对象
     */
    @Override
    public T parent(By by, Integer index) {
        by = Locator.getLoc(by, true, false);
        if (!by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath");
        String loc = "xpath:./ancestor::" + by.getValue().replaceAll("^[.\\s/]+", "") + "[" + index + "]";
        return _ele(loc, null, 1, true, false, "parent()").get(0);
    }

    /**
     * 返回上面某一级父元素，指定层数
     *
     * @param level 第几级父元素
     * @return 上级元素对象
     */
    @Override
    public T parent(Integer level) {
        String loc = "xpath:./ancestor::*[" + level + "]";
        return _ele(loc, null, 1, true, false, "parent()").get(0);
    }

    /**
     * 返回上面某一级父元素   用查询语法定位
     *
     * @param loc   定位符
     * @param index 选择第几个结果
     * @return 上级元素对象
     */
    @Override
    public T parent(String loc, Integer index) {
        By loc1 = Locator.getLoc(loc, true, false);
        if (!loc1.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath");
        loc = "xpath:./ancestor::" + loc1.getValue().replaceAll("^[.\\s/]+", "") + "[" + index + "]";
        return _ele(loc, null, 1, true, false, "parent()").get(0);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param index 第几个查询结果，1开始
     * @return 直接子元素或节点文本组成的列表
     */
    public T child(Integer index) {
        return child(index, null);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @return 直接子元素或节点文本组成的列表
     */
    public T child(Integer index, Double timeout) {
        return child(index, timeout, true);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 直接子元素或节点文本组成的列表
     */
    public T child(Integer index, Double timeout, Boolean eleOnly) {
        String loc = eleOnly ? "*" : "node()";
        List<T> ts = this._ele("xpath./" + loc, timeout, index, true, false, null);
        if (!ts.isEmpty()) {
            return ts.get(0);
        } else if (Settings.raiseWhenEleNotFound) {
            throw new ElementNotFoundError("child()", Map.of("loc", "", "index", index, "eleOnly", eleOnly));
        }
        return null;
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @return 直接子元素或节点文本组成的列表
     */
    public T child() {
        return child("");
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 查询语句
     * @return 直接子元素或节点文本组成的列表
     */
    public T child(String loc) {
        return child(loc.isEmpty() ? null : loc, 1);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc   查询语句
     * @param index 第几个查询结果，1开始
     * @return 直接子元素或节点文本组成的列表
     */
    public T child(String loc, Integer index) {
        return child(loc, index, null);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     查询语句
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @return 直接子元素或节点文本组成的列表
     */
    public T child(String loc, Integer index, Double timeout) {
        return child(loc, index, timeout, true);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     查询语句
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 直接子元素或节点文本组成的列表
     */
    public T child(String loc, Integer index, Double timeout, Boolean eleOnly) {
        if (loc == null || loc.isEmpty()) {
            loc = eleOnly ? "*" : "node()";
        } else {
            By by = Locator.getLoc(loc, true, false);
            if (by.getName().equals(BySelect.CSS_SELECTOR))
                throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
            else loc = by.getValue().replaceAll("^[.\\s/]+", "");
        }

        List<T> ts = this._ele("xpath./" + loc, timeout, index, true, false, null);
        if (!ts.isEmpty()) {
            return ts.get(0);
        } else if (Settings.raiseWhenEleNotFound) {
            throw new ElementNotFoundError("child()", Map.of("loc", loc, "index", index, "eleOnly", eleOnly));
        }
        return null;
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param by 查询语句
     * @return 直接子元素或节点文本组成的列表
     */
    public T child(By by) {
        return child(by, 1);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param by    查询语句
     * @param index 第几个查询结果，1开始
     * @return 直接子元素或节点文本组成的列表
     */
    public T child(By by, Integer index) {
        return child(by, index, null);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      查询语句
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @return 直接子元素或节点文本组成的列表
     */
    public T child(By by, Integer index, Double timeout) {
        return child(by, index, timeout, true);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      查询语句
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 直接子元素或节点文本组成的列表
     */
    public T child(By by, Integer index, Double timeout, Boolean eleOnly) {
        String loc;
        if (by == null) {
            loc = eleOnly ? "*" : "node()";
        } else {
            by = Locator.getLoc(by, true, false);
            if (by.getName().equals(BySelect.CSS_SELECTOR))
                throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
            else loc = by.getValue().replaceAll("^[.\\s/]+", "");
        }

        List<T> ts = this._ele("xpath./" + loc, timeout, index, true, false, null);
        if (!ts.isEmpty()) {
            return ts.get(0);
        } else if (Settings.raiseWhenEleNotFound) {
            throw new ElementNotFoundError("child()", Map.of("loc", loc, "index", index, "eleOnly", eleOnly));
        }
        return null;
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @return 兄弟元素
     */
    public T prev() {
        return prev("");
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc 用于筛选的查询语法
     * @return 兄弟元素
     */
    public T prev(String loc) {
        return prev(loc.isEmpty() ? null : loc, null);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc   用于筛选的查询语法
     * @param index 前面第几个查询结果，0开始
     * @return 兄弟元素
     */

    public T prev(String loc, Integer index) {
        return prev(loc, index, null);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，0开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 兄弟元素
     */
    public T prev(String loc, Integer index, Double timeout) {
        return prev(loc, index, timeout, true);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素
     */
    public T prev(String loc, Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("prev()", "preceding", true, loc, index, timeout, eleOnly);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by 用于筛选的元素
     * @return 兄弟元素
     */
    public T prev(By by) {
        return prev(by, 1);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by    用于筛选的元素
     * @param index 前面第几个查询结果，0开始
     * @return 兄弟元素
     */
    public T prev(By by, Integer index) {
        return prev(by, index, null);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的元素
     * @param index   前面第几个查询结果，0开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 兄弟元素
     */
    public T prev(By by, Integer index, Double timeout) {
        return prev(by, index, timeout, true);
    }

    public T prev(By by, Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("prev()", "preceding", true, by, index, timeout, eleOnly);
    }


    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index 前面第几个查询结果，0开始
     * @return 兄弟元素
     */
    public T prev(Integer index) {
        return prev(index, null);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   前面第几个查询结果，0开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 兄弟元素
     */
    public T prev(Integer index, Double timeout) {
        return prev(index, timeout, true);
    }

    public T prev(Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("prev()", "preceding", true, index, timeout, eleOnly);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素
     */
    @Override
    public T next(By by, Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("next()", "following", true, by, index, timeout, eleOnly);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素
     */

    @Override
    public T next(String loc, Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("next()", "following", true, loc, index, timeout, eleOnly);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   第几个查询结果，0开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本
     */
    @Override
    public T next(Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("next()", "following", true, index, timeout, eleOnly);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by 用于筛选的查询语法
     * @return 本元素前面的某个元素或节点
     */
    public T before(By by) {
        return before(by, null);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by    用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素前面的某个元素或节点
     */
    public T before(By by, Integer index) {
        return before(by, index, null);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的某个元素或节点
     */
    public T before(By by, Integer index, Double timeout) {
        return before(by, index, timeout, true);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    public T before(By by, Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("before()", "preceding", false, by, index, timeout, eleOnly);
    }


    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @return 本元素前面的某个元素或节点
     */
    public T before() {
        return before("");
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素前面的某个元素或节点
     */
    public T before(String loc) {
        return before(loc.isEmpty() ? null : loc, null);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc   用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素前面的某个元素或节点
     */
    public T before(String loc, Integer index) {
        return before(loc, index, null);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的某个元素或节点
     */
    public T before(String loc, Integer index, Double timeout) {
        return before(loc, index, timeout, true);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    public T before(String loc, Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("before()", "preceding", false, loc, index, timeout, eleOnly);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index 前面第几个查询结果，1开始
     * @return 本元素前面的某个元素或节点
     */
    public T before(Integer index) {
        return before(index, null);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的某个元素或节点
     */
    public T before(Integer index, Double timeout) {
        return before(index, timeout, true);
    }

    /**
     * 返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    public T before(Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("before()", "preceding", false, index, timeout, eleOnly);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by 用于筛选的查询语法
     * @return 本元素后面的某个元素或节点
     */
    public T after(By by) {
        return after(by, null);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by    用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素后面的某个元素或节点
     */
    public T after(By by, Integer index) {
        return after(by, index, null);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素后面的某个元素或节点
     */
    public T after(By by, Integer index, Double timeout) {
        return after(by, index, timeout, true);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素后面的某个元素或节点
     */
    public T after(By by, Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("after()", "following", false, by, index, timeout, eleOnly);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @return 本元素后面的某个元素或节点
     */
    public T after() {
        return after("");
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素后面的某个元素或节点
     */
    public T after(String loc) {
        return after(loc.isEmpty() ? null : loc, null);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc   用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素后面的某个元素或节点
     */
    public T after(String loc, Integer index) {
        return after(loc, index, null);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素后面的某个元素或节点
     */
    public T after(String loc, Integer index, Double timeout) {
        return after(loc, index, timeout, true);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素后面的某个元素或节点
     */
    public T after(String loc, Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("after()", "following", false, loc, index, timeout, eleOnly);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index 前面第几个查询结果，1开始
     * @return 本元素后面的某个元素或节点
     */
    public T after(Integer index) {
        return after(index, null);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素后面的某个元素或节点
     */
    public T after(Integer index, Double timeout) {
        return after(index, timeout, true);
    }

    /**
     * 返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素后面的某个元素或节点
     */
    public T after(Integer index, Double timeout, Boolean eleOnly) {
        return this.getRelative("after()", "following", false, index, timeout, eleOnly);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param by 用于筛选的查询语法
     * @return 本元素后面的某个元素或节点
     */
    public List<T> children(By by) {
        return children(by, null);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素后面的某个元素或节点
     */
    public List<T> children(By by, Double timeout) {
        return children(by, timeout, true);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素后面的某个元素或节点
     */
    public List<T> children(By by, Double timeout, Boolean eleOnly) {
        String loc;
        if (by == null) {
            loc = eleOnly ? "*" : "node()";
        } else {
            by = Locator.getLoc(by, true, false);
            if (by.getName().equals(BySelect.CSS_SELECTOR))
                throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
            else loc = by.getValue().replaceAll("^[.\\s/]+", "");
        }
        loc = "xpath:./" + loc;
        return this._ele(loc, timeout, null, true, null, null);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @return 本元素后面的某个元素或节点
     */
    public List<T> children() {
        return children("");
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素后面的某个元素或节点
     */
    public List<T> children(String loc) {
        return children(loc.isEmpty() ? null : loc, null);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素后面的某个元素或节点
     */
    public List<T> children(String loc, Double timeout) {
        return children(loc, timeout, true);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素后面的某个元素或节点
     */
    public List<T> children(String loc, Double timeout, Boolean eleOnly) {
        if (loc == null || loc.isEmpty()) {
            loc = eleOnly ? "*" : "node()";
        } else {
            By by = Locator.getLoc(loc, true, false);
            if (by.getName().equals(BySelect.CSS_SELECTOR))
                throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
            else {
                loc = by.getValue().replaceAll("^[.\\s/]+", "");
            }
        }
        loc = "xpath:./" + loc;
        return this._ele(loc, timeout, null, true, null, null);
    }


    /**
     * 获取前面符合条件的同级列表
     *
     * @return 同级元素或节点文本组成的列表
     */
    public List<T> prevs() {
        return prevs("", null);
    }

    /**
     * 获取前面符合条件的同级列表
     *
     * @param loc 查询元素
     * @return 同级元素或节点文本组成的列表
     */
    public List<T> prevs(String loc) {
        return prevs(loc.isEmpty() ? null : loc, null);
    }

    /**
     * 获取前面符合条件的同级列表
     *
     * @param loc     查询元素
     * @param timeout 等待时间
     * @return 同级元素或节点文本组成的列表
     */
    public List<T> prevs(String loc, Double timeout) {
        return prevs(loc, timeout, true);
    }

    /**
     * 回前面全部兄弟元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     查询元素
     * @param timeout 等待时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素的列表
     */
    public List<T> prevs(String loc, Double timeout, Boolean eleOnly) {
        return this.getRelatives(null, loc, "preceding", true, timeout, eleOnly);
    }

    /**
     * 获取前面符合条件的同级列表
     *
     * @param by 查询元素
     * @return 同级元素或节点文本组成的列表
     */
    public List<T> prevs(By by) {
        return prevs(by, null);
    }

    /**
     * 获取前面符合条件的同级列表
     *
     * @param by      查询元素
     * @param timeout 等待时间
     * @return 同级元素或节点文本组成的列表
     */
    public List<T> prevs(By by, Double timeout) {
        return prevs(by, timeout, true);
    }


    /**
     * 回前面全部兄弟元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      查询元素
     * @param timeout 等待时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素的列表
     */
    public List<T> prevs(By by, Double timeout, Boolean eleOnly) {
        return this.getRelatives(null, by, "preceding", true, timeout, eleOnly);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选
     *
     * @param by 用于筛选的查询语法
     * @return 兄弟元素或节点文本组成的列表
     */
    public List<T> nexts(By by) {
        return nexts(by, null);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间
     * @return 兄弟元素或节点文本组成的列表
     */
    public List<T> nexts(By by, Double timeout) {
        return nexts(by, timeout, true);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本组成的列表
     */
    public List<T> nexts(By by, Double timeout, Boolean eleOnly) {
        return this.getRelatives(null, by, "following", true, timeout, eleOnly);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选
     *
     * @return 兄弟元素或节点文本组成的列表
     */

    public List<T> nexts() {
        return nexts("");
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return 兄弟元素或节点文本组成的列表
     */
    public List<T> nexts(String loc) {
        return nexts(loc.isEmpty() ? null : loc, null);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间
     * @return 兄弟元素或节点文本组成的列表
     */
    public List<T> nexts(String loc, Double timeout) {
        return nexts(loc, timeout, true);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本组成的列表
     */
    public List<T> nexts(String loc, Double timeout, Boolean eleOnly) {
        return this.getRelatives(null, loc, "following", true, timeout, eleOnly);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param by 用于筛选的查询语法
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> befores(By by) {
        return this.befores(by, null);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> befores(By by, Double timeout) {
        return this.befores(by, timeout, true);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> befores(By by, Double timeout, Boolean eleOnly) {
        return this.getRelatives(null, by, "preceding", false, timeout, eleOnly);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> befores() {
        return this.befores("");
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> befores(String loc) {
        return this.befores(loc.isEmpty() ? null : loc, null);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> befores(String loc, Double timeout) {
        return this.befores(loc, timeout, true);
    }

    /**
     * 返回后面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> befores(String loc, Double timeout, Boolean eleOnly) {
        return this.getRelatives(null, loc, "preceding", false, timeout, eleOnly);
    }

    /**
     * 返回前面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param by 用于筛选的查询语法
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> afters(By by) {
        return this.afters(by, null);
    }


    /**
     * 返回前面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> afters(By by, Double timeout) {
        return this.afters(by, timeout, true);
    }

    /**
     * 返回前面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> afters(By by, Double timeout, Boolean eleOnly) {
        return this.getRelatives(null, by, "following", false, timeout, eleOnly);
    }


    /**
     * 返回前面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> afters() {
        return this.afters("");
    }

    /**
     * 返回前面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> afters(String loc) {
        return this.afters(loc.isEmpty() ? null : loc, null);
    }

    /**
     * 返回前面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> afters(String loc, Double timeout) {
        return this.afters(loc, timeout, true);
    }

    /**
     * 返回前面全部兄弟元素或节点组成的列表,可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的元素或节点组成的列表
     */
    public List<T> afters(String loc, Double timeout, Boolean eleOnly) {
        return this.getRelatives(null, loc, "following", false, timeout, eleOnly);
    }

    /**
     * *获取一个亲戚元素或节点，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param func:      方法名称
     * @param direction: 方向，'following' 或 'preceding'
     * @param brother    查找范围，在同级查找还是整个dom前后查找
     * @param loc:       用于筛选的查询语法
     * @param timeout:   查找节点的超时时间（秒）
     * @param eleOnly:   是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    private T getRelative(String func, String direction, Boolean brother, Integer loc, Double timeout, Boolean eleOnly) {
        List<T> relatives = this.getRelatives(loc, "", direction, brother, timeout, eleOnly);
        if (!relatives.isEmpty()) return relatives.get(0);
        if (Settings.raiseWhenEleNotFound)
            throw new ElementNotFoundError(func, Map.of("loc", "", "index", loc, "eleOnly", eleOnly));
        return null;
    }

    /**
     * *获取一个亲戚元素或节点，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param func:      方法名称
     * @param direction: 方向，'following' 或 'preceding'
     * @param brother    查找范围，在同级查找还是整个dom前后查找
     * @param loc:       用于筛选的查询语法
     * @param index:     前面第几个查询结果，1开始
     * @param timeout:   查找节点的超时时间（秒）
     * @param eleOnly:   是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    private T getRelative(String func, String direction, Boolean brother, String loc, Integer index, Double timeout, Boolean eleOnly) {
        List<T> relatives = this.getRelatives(index, loc, direction, brother, timeout, eleOnly);
        if (!relatives.isEmpty()) return relatives.get(0);
        if (Settings.raiseWhenEleNotFound)
            throw new ElementNotFoundError(func, Map.of("loc", "", "index", index, "eleOnly", eleOnly));
        return null;
    }

    /**
     * *获取一个亲戚元素或节点，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param func:      方法名称
     * @param direction: 方向，'following' 或 'preceding'
     * @param brother    查找范围，在同级查找还是整个dom前后查找
     * @param by:        用于筛选的查询语法
     * @param index:     前面第几个查询结果，1开始
     * @param timeout:   查找节点的超时时间（秒）
     * @param eleOnly:   是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    private T getRelative(String func, String direction, Boolean brother, By by, Integer index, Double timeout, Boolean eleOnly) {
        List<T> relatives = this.getRelatives(index, by, direction, brother, timeout, eleOnly);
        if (!relatives.isEmpty()) return relatives.get(0);
        if (Settings.raiseWhenEleNotFound)
            throw new ElementNotFoundError(func, Map.of("loc", "", "index", index, "eleOnly", eleOnly));
        return null;
    }

    /**
     * @param index     获取第几个，该参数不为null时只获取该编号的元素
     * @param loc       用于筛选的查询语法
     * @param direction 'following' 或 'preceding'，查找的方向
     * @param brother   查找范围，在同级查找还是整个dom前后查找
     * @param timeout   查找等待时间（秒）
     * @param eleOnly   是否只获取元素，为False时把文本、注释节点也纳入
     * @return 元素对象或字符串
     */
    private List<T> getRelatives(Integer index, String loc, String direction, Boolean brother, Double timeout, Boolean eleOnly) {
        //获取查到范围
        String brotherStr = brother ? "-sibling" : "";
        if (loc == null || loc.isEmpty()) {
            loc = eleOnly ? "*" : "node()";
        } else {
            By by = Locator.getLoc(loc, true, false);
            if (!by.getName().equals(BySelect.CSS_SELECTOR))
                throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
            else loc = by.getValue().replaceAll("^[.\\s/]+", "");
        }
        loc = "xpath:./" + direction + brotherStr + "::" + loc;
        if (index != null) index = "following".equals(direction) ? index : -index;
        return this._ele(loc, timeout, index, true, false, null);
    }

    /**
     * @param index     获取第几个，该参数不为null时只获取该编号的元素
     * @param by        用于筛选的查询语法
     * @param direction 'following' 或 'preceding'，查找的方向
     * @param brother   查找范围，在同级查找还是整个dom前后查找
     * @param timeout   查找等待时间（秒）
     * @param eleOnly   是否只获取元素，为False时把文本、注释节点也纳入
     * @return 元素对象或字符串
     */
    private List<T> getRelatives(Integer index, By by, String direction, Boolean brother, Double timeout, Boolean eleOnly) {
        //获取查到范围
        String brotherStr = brother ? "-sibling" : "";
        String loc;
        if (by == null) {
            loc = eleOnly ? "*" : "node()";
        } else {
            by = Locator.getLoc(by, true, false);
            if (!by.getName().equals(BySelect.CSS_SELECTOR))
                throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
            else loc = by.getValue().replaceAll("^[.\\s/]+", "");
        }
        loc = "xpath:./" + direction + brotherStr + "::" + loc;
        if (index != null) index = "following".equals(direction) ? index : -index;
        return this._ele(loc, timeout, index, true, false, null);
    }

    //----------------------------------------------

    /**
     * @return 返回元素所有属性及值
     */
    public abstract Map<String, String> attrs();

    /**
     * 返回处理的元素内文本
     */
    public abstract String text();

    /**
     * 返回未格式化处理的元素内文本
     */
    public abstract String rawText();

    /**
     * 返回attribute属性值
     *
     * @param attr 属性名
     * @return 属性值文本，没有该属性返回null
     */
    public abstract String attr(String attr);

    /**
     * 获取css路径或xpath路径
     *
     * @param mode 'css' 或 'xpath'
     * @return css路径或xpath路径
     */
    protected abstract String getElePath(ElePathMode mode);
}
