package com.ll.DrissonPage.element;

import com.alibaba.fastjson.JSON;
import com.ll.DrissonPage.base.BaseElement;
import com.ll.DrissonPage.base.By;
import com.ll.DrissonPage.base.BySelect;
import com.ll.DrissonPage.error.extend.ElementNotFoundError;
import com.ll.DrissonPage.functions.Locator;
import com.ll.DrissonPage.functions.Settings;
import com.ll.DrissonPage.page.ChromiumBase;
import com.ll.DrissonPage.units.states.ShadowRootStates;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class ShadowRoot extends BaseElement<ChromiumBase, ChromiumElement> {
    @Getter
    private String objId;
    @Getter
    private Integer backendId;
    private Integer nodeId;
    private final ChromiumElement parentEle;
    private ShadowRootStates states;

    /**
     * @param parentEle shadow root 所在父元素
     * @param objId     js中的object id
     * @param backendId cdp中的backend id
     */
    public ShadowRoot(ChromiumElement parentEle, String objId, Integer backendId) {
        super(parentEle.getOwner());
        this.parentEle = parentEle;
        if (backendId != null) {
            this.backendId = backendId;
            this.objId = this.getObjId(backendId);
            this.nodeId = this.getNodeId(this.objId);
        } else if (objId != null) {
            this.objId = objId;
            this.nodeId = this.getNodeId(objId);
            this.backendId = this.getBackendId(this.nodeId);
        }
        this.states = null;
        super.setType("ShadowRoot");

    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ShadowRoot || obj instanceof ChromiumElement)
            if (obj instanceof ShadowRoot) return Objects.equals(this.backendId, ((ShadowRoot) obj).getBackendId());
            else return Objects.equals(this.backendId, ((ChromiumElement) obj).getBackendId());
        return false;
    }

    /**
     * @return 返回元素标签名
     */
    @Override
    public String tag() {
        return "shadow-root";
    }

    /**
     * @return 返回outerHTML文本
     */
    @Override
    public String html() {
        return "<shadow_root>" + this.innerHtml() + "</shadow_root>";
    }

    /**
     * @return 返回内部的html文本
     */
    public String innerHtml() {
        return this.runJs("return this.innerHTML;").toString();
    }

    /**
     * @return 返回用于获取元素状态的对象
     */
    public ShadowRootStates states() {
        if (this.states == null) this.states = new ShadowRootStates(this);
        return this.states;
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js js文本，文本中用this表示本元素
     * @return 运行的结果
     */
    public Object runJs(String js) {
        return runJs(js, new ArrayList<>());
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js     js文本，文本中用this表示本元素
     * @param params 参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     * @return 运行的结果
     */
    public Object runJs(String js, List<Object> params) {
        return runJs(js, null, params);
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js      js文本，文本中用this表示本元素
     * @param timeout js超时时间（秒），为None则使用页面timeouts.script设置
     * @param params  参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     * @return 运行的结果
     */
    public Object runJs(String js, Double timeout, List<Object> params) {
        return runJs(js, false, timeout, params);
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js      js文本，文本中用this表示本元素
     * @param asExpr  是否作为表达式运行，为True时args无效
     * @param timeout js超时时间（秒），为None则使用页面timeouts.script设置
     * @param params  参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     * @return 运行的结果
     */
    public Object runJs(String js, Boolean asExpr, Double timeout, List<Object> params) {
        return ChromiumElement.runJs(this, js, asExpr, timeout != null ? timeout : this.getOwner().getTimeouts().getScript(), params);
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js js文本，文本中用this表示本元素
     */
    public void runAsyncJs(String js) {
        runAsyncJs(js, new ArrayList<>());
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js     js文本，文本中用this表示本元素
     * @param params 参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     */
    public void runAsyncJs(String js, List<Object> params) {
        runAsyncJs(js, 0.0, params);
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js      js文本，文本中用this表示本元素
     * @param params  参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     * @param timeout js超时时间（秒），为None则使用页面timeouts.script设置
     */
    public void runAsyncJs(String js, Double timeout, List<Object> params) {
        runAsyncJs(js, false, params, timeout);
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js      js文本，文本中用this表示本元素
     * @param asExpr  是否作为表达式运行，为True时args无效
     * @param params  参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     * @param timeout js超时时间（秒），为None则使用页面timeouts.script设置
     */
    public void runAsyncJs(String js, Boolean asExpr, List<Object> params, Double timeout) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ChromiumElement.runJs(this, js, asExpr, timeout == null ? 0 : timeout, params);
            }
        }).start();
    }

    @Override
    public ChromiumElement parent(By by, Integer index) {
        by = Locator.getLoc(by, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        String loc = "xpath:./ancestor-or-self::" + by.getValue().replaceFirst("^[.\\s/]+", "") + "[" + index + "]";
        return this.parentEle._ele(loc, 0.0, null, true, false, "parent()").get(0);
    }

    @Override
    public ChromiumElement parent(Integer level) {
        String loc = "xpath:./ancestor-or-self::*[" + level + "]";
        return this.parentEle._ele(loc, 0.0, null, true, false, "parent()").get(0);
    }

    @Override
    public ChromiumElement parent(String loc, Integer index) {
        By by = Locator.getLoc(loc, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        loc = "xpath:./ancestor-or-self::" + by.getValue().replaceFirst("^[.\\s/]+", "") + "[" + index + "]";
        return this.parentEle._ele(loc, 0.0, null, true, false, "parent()").get(0);

    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @return 直接子元素
     */

    public ChromiumElement child() {
        return this.child("");
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return 直接子元素
     */

    public ChromiumElement child(String loc) {
        return this.child(loc == null || loc.isEmpty() ? null : loc, 1);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc   用于筛选的查询语法
     * @param index 第几个查询结果，1开始
     * @return 直接子元素
     */

    public ChromiumElement child(String loc, Integer index) {
        String value;
        if (loc == null || loc.isEmpty()) value = "*";
        else {
            By by = Locator.getLoc(loc, true, false);
            if (by.getName().equals(BySelect.CSS_SELECTOR))
                throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
            value = by.getValue().replaceAll("^[.\\s/]+", "");
        }
        value = "xpath:./" + value;
        List<ChromiumElement> list = this._ele(value, null, index, true, null, null);
        if (list != null && !list.isEmpty()) return list.get(0);
        if (Settings.raiseWhenEleNotFound)
            throw new ElementNotFoundError("child()", Map.of("filter_loc", loc == null ? "" : loc, "index", index));
        else return null;
    }


    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return 直接子元素
     */

    public ChromiumElement child(By loc) {
        return this.child(loc, 1);
    }

    /**
     * 返回直接子元素元素或节点组成的列表，可用查询语法筛选
     *
     * @param index 第几个查询结果，1开始
     * @return 直接子元素
     */

    public ChromiumElement child(By by, Integer index) {
        String loc = by.getName().getName() + ":" + by.getValue();
        by = Locator.getLoc(by, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./" + value;
        List<ChromiumElement> list = this._ele(value, null, index, true, null, null);
        if (list != null && !list.isEmpty()) return list.get(0);
        if (Settings.raiseWhenEleNotFound) {
            throw new ElementNotFoundError("child()", Map.of("filter_loc", loc, "index", index));
        } else return null;
    }

    /**
     * @param by    用于筛选的查询语法
     * @param index 第几个查询结果，0开始
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement next(By by, Integer index) {
        return this.next(by, index, null);
    }

    /**
     * @param by      用于筛选的查询语法
     * @param index   第几个查询结果，0开始
     * @param timeout 无效参数
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement next(By by, Integer index, Double timeout) {
        return this.next(by, index, null, null);
    }

    /**
     * @param by      用于筛选的查询语法
     * @param index   第几个查询结果，0开始
     * @param timeout 无效参数
     * @param eleOnly 无效参数
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement next(By by, Integer index, Double timeout, Boolean eleOnly) {
        String loc = by.getName().getName() + ":" + by.getValue();
        by = Locator.getLoc(by, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./" + value;
        List<ChromiumElement> list = this.parentEle._ele(value, null, index, true, null, null);
        if (list != null && !list.isEmpty()) return list.get(0);
        if (Settings.raiseWhenEleNotFound) {
            throw new ElementNotFoundError("next()", Map.of("filter_loc", loc, "index", index));
        } else return null;
    }

    /**
     * @param loc   用于筛选的查询语法
     * @param index 第几个查询结果，0开始
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement next(String loc, Integer index) {
        return this.next(loc, index, null);
    }

    /**
     * @param loc     用于筛选的查询语法
     * @param index   第几个查询结果，0开始
     * @param timeout 无效参数
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement next(String loc, Integer index, Double timeout) {
        return this.next(loc, index, null, null);
    }

    /**
     * @param loc     用于筛选的查询语法
     * @param index   第几个查询结果，0开始
     * @param timeout 无效参数
     * @param eleOnly 无效参数
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement next(String loc, Integer index, Double timeout, Boolean eleOnly) {
        By by = Locator.getLoc(loc, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./" + value;
        List<ChromiumElement> list = this.parentEle._ele(value, null, index, true, false, null);
        if (list != null && !list.isEmpty()) return list.get(0);
        if (Settings.raiseWhenEleNotFound) {
            throw new ElementNotFoundError("next()", Map.of("filter_loc", loc, "index", index));
        } else return null;
    }

    /**
     * @param index 第几个查询结果，0开始
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement next(Integer index) {
        return next(index, null);
    }

    /**
     * @param index   第几个查询结果，0开始
     * @param timeout 无效参数
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement next(Integer index, Double timeout) {
        return next(index, null, null);
    }

    /**
     * @param index   第几个查询结果，0开始
     * @param timeout 无效参数
     * @param eleOnly 无效参数
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement next(Integer index, Double timeout, Boolean eleOnly) {
        return next("", index);
    }


    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 直接子元素
     */

    public ChromiumElement before() {
        return this.before("");
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 直接子元素
     */

    public ChromiumElement before(String loc) {
        return this.before(loc, 1);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc   用于筛选的查询语法
     * @param index 第几个查询结果，1开始
     * @return 直接子元素
     */

    public ChromiumElement before(String loc, Integer index) {
        String value;
        if (loc == null) loc = "";
        By by = Locator.getLoc(loc, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./preceding::" + value;
        List<ChromiumElement> list = this.parentEle._ele(value, null, index, true, null, null);
        if (list != null && !list.isEmpty()) return list.get(0);
        if (Settings.raiseWhenEleNotFound)
            throw new ElementNotFoundError("before()", Map.of("filter_loc", loc == null ? "" : loc, "index", index));
        else return null;
    }


    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 直接子元素
     */

    public ChromiumElement before(By by) {
        return this.before(by, 1);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param index 第几个查询结果，1开始
     * @return 直接子元素
     */

    public ChromiumElement before(By by, Integer index) {
        String loc = by.getName().getName() + ":" + by.getValue();
        by = Locator.getLoc(by, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./preceding::" + value;
        List<ChromiumElement> list = this.parentEle._ele(value, null, index, true, null, null);
        if (list != null && !list.isEmpty()) return list.get(0);
        if (Settings.raiseWhenEleNotFound)
            throw new ElementNotFoundError("child()", Map.of("filter_loc", loc, "index", index));
        else return null;
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 本元素后面的某个元素
     */
    public ChromiumElement after(By by) {
        return after(by, 1);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by    用于筛选的查询语法
     * @param index 后面第几个查询结果，1开始
     * @return 本元素后面的某个元素
     */
    public ChromiumElement after(By by, Integer index) {
        String loc = by.getName().getName() + ":" + by.getValue();
        List<ChromiumElement> afters = this.afters(by);
        if (afters != null && !afters.isEmpty()) {
            index -= 1;
            if (index < 0) index = afters.size() + index;
            return afters.get(index);
        }
        if (Settings.raiseWhenEleNotFound)
            throw new ElementNotFoundError("after(()", Map.of("filter_loc", loc, "index", index));
        return null;
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 本元素后面的某个元素
     */
    public ChromiumElement after() {
        return after("");
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素后面的某个元素
     */
    public ChromiumElement after(String loc) {
        return after(loc, 1);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc   用于筛选的查询语法
     * @param index 后面第几个查询结果，1开始
     * @return 本元素后面的某个元素
     */
    public ChromiumElement after(String loc, Integer index) {
        List<ChromiumElement> afters = this.afters(loc);
        if (afters != null && !afters.isEmpty()) {
            index -= 1;
            if (index < 0) index = afters.size() + index;
            return afters.get(index);
        }
        if (Settings.raiseWhenEleNotFound)
            throw new ElementNotFoundError("after(()", Map.of("filter_loc", loc, "index", index));
        return null;
    }

    /**
     * 返回当前元素符合条件的直接子元素或节点组成的列表，可用查询语法筛选
     *
     * @param by 用于筛选的查询语法
     * @return ChromiumElement对象组成的列表
     */
    public List<ChromiumElement> children(By by) {
        by = Locator.getLoc(by, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./" + value;
        return this._ele(value, null, null, true, null, null);
    }

    /**
     * 返回当前元素符合条件的直接子元素或节点组成的列表，可用查询语法筛选
     *
     * @return ChromiumElement对象组成的列表
     */
    public List<ChromiumElement> children() {
        return this.children("");
    }

    /**
     * 返回当前元素符合条件的直接子元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return ChromiumElement对象组成的列表
     */
    public List<ChromiumElement> children(String loc) {
        By by = Locator.getLoc(loc, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./" + value;
        return this._ele(value, null, null, true, null, null);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by 用于筛选的查询语法
     * @return ChromiumElement对象组成的列表
     */
    public List<ChromiumElement> nexts(By by) {
        by = Locator.getLoc(by, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./" + value;
        return this.parentEle._ele(value, null, null, true, null, null);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @return ChromiumElement对象组成的列表
     */
    public List<ChromiumElement> nexts() {
        return nexts("");
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return ChromiumElement对象组成的列表
     */
    public List<ChromiumElement> nexts(String loc) {
        By by = Locator.getLoc(loc, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./" + value;
        return this.parentEle._ele(value, null, null, true, null, null);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 本元素前面的元素
     */
    public List<ChromiumElement> befores(By by) {
        by = Locator.getLoc(by, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./preceding::" + value;
        return this.parentEle._ele(value, null, null, true, null, null);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 本元素前面的元素
     */
    public List<ChromiumElement> befores() {
        return this.befores("");
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素前面的元素
     */
    public List<ChromiumElement> befores(String loc) {
        By by = Locator.getLoc(loc, true, false);
        if (by.getName().equals(BySelect.CSS_SELECTOR))
            throw new IllegalArgumentException("此css selector语法不受支持，请换成xpath。");
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./preceding::" + value;
        return this.parentEle._ele(value, null, null, true, null, null);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 本元素后面的元素
     */
    public List<ChromiumElement> afters(By by) {
        List<ChromiumElement> next = this.nexts(by);
        by = Locator.getLoc(by, true, false);
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./following::" + value;
        List<ChromiumElement> list = this.parentEle._ele(value, null, null, true, null, null);
        List<ChromiumElement> returns = new ArrayList<>();
        if (next != null) returns.addAll(next);
        if (list != null) returns.addAll(list);
        return returns;
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 本元素后面的元素
     */
    public List<ChromiumElement> afters() {
        return this.afters("");
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素后面的元素
     */
    public List<ChromiumElement> afters(String loc) {
        List<ChromiumElement> next = this.nexts(loc);
        By by = Locator.getLoc(loc, true, false);
        String value = by.getValue().replaceAll("^[.\\s/]+", "");
        value = "xpath:./following::" + value;
        List<ChromiumElement> list = this.parentEle._ele(value, null, null, true, null, null);
        List<ChromiumElement> returns = new ArrayList<>();
        if (next != null) returns.addAll(next);
        if (list != null) returns.addAll(list);
        return returns;
    }


    public ChromiumElement ele(By by, int index, Double timeout) {
        List<ChromiumElement> list = this._ele(by, timeout, index, null, null, "ele()");
        return !list.isEmpty() ? list.get(0) : null;
    }

    public ChromiumElement ele(String loc, int index, Double timeout) {
        List<ChromiumElement> list = this._ele(loc, timeout, index, null, null, "ele()");
        return !list.isEmpty() ? list.get(0) : null;
    }

    @Override
    public List<ChromiumElement> eles(By by, Double timeout) {
        return this._ele(by, timeout, null, null, null, "ele()");
    }

    @Override
    public List<ChromiumElement> eles(String loc, Double timeout) {
        return this._ele(loc, timeout, null, null, null, "ele()");
    }

    @Override
    public SessionElement sEle(By by, Integer index) {
        List<SessionElement> sessionElements = SessionElement.makeSessionEle(this, by, index);
        return sessionElements != null && !sessionElements.isEmpty() ? sessionElements.get(0) : null;
    }

    @Override
    public SessionElement sEle(String loc, Integer index) {
        List<SessionElement> sessionElements = SessionElement.makeSessionEle(this, loc, index);
        return sessionElements != null && !sessionElements.isEmpty() ? sessionElements.get(0) : null;
    }

    @Override
    public List<SessionElement> sEles(By by) {
        return SessionElement.makeSessionEle(this, by, null);
    }

    @Override
    public List<SessionElement> sEles(String loc) {
        return SessionElement.makeSessionEle(this, loc, null);
    }

    @Override
    protected List<ChromiumElement> findElements(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        return _findElement(Locator.getLoc(by, false, false), timeout, index);
    }

    @Override
    protected List<ChromiumElement> findElements(String loc, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        return _findElement(Locator.getLoc(loc, false, false), timeout, index);
    }

    private List<ChromiumElement> _findElement(By by, Double timeout, Integer index) {
        if (by.getName().equals(BySelect.CSS_SELECTOR) && by.getValue().startsWith(":root")) {
            by.setValue(by.getValue().substring(5));
        }
        timeout = timeout == null ? this.getOwner().timeout() : timeout;
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        List<ChromiumElement> result = doFind(by, index);
        while (result == null && System.currentTimeMillis() <= endTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result = doFind(by, index);
        }
        if (result != null) return result;
        return new ArrayList<>();
    }

    private List<ChromiumElement> doFind(By by, Integer index) {
        if (by.getName().equals(BySelect.CSS_SELECTOR)) {
            if (index != null && index == 1) {
                Object nodeId = JSON.parseObject(this.getOwner().runCdp("DOM.querySelector", Map.of("nodeId", this.nodeId, "selector", by.getValue())).toString()).get("nodeId");
                if (nodeId != null && !nodeId.toString().isEmpty()) {
                    return ChromiumElement.makeChromiumEles(this.getOwner(), nodeId, 1, false);
                }
            } else {
                Object nodId = JSON.parseObject(this.getOwner().runCdp("DOM.querySelectorAll", Map.of("nodeId", this.nodeId, "selector", by.getValue())).toString()).get("nodeId");
                return ChromiumElement.makeChromiumEles(this.getOwner(), nodId, 1, false);
            }
        } else {
            List<SessionElement> sessionElements = SessionElement.makeSessionEle(this.html(), "", 1).get(0).eles(by);
            if (sessionElements == null || sessionElements.isEmpty()) return null;
            List<String> css = new ArrayList<>();
            for (SessionElement ele : sessionElements) css.add(ele.cssPath().substring(0, 61));
            if (index != null) {
                Object nodeId;
                try {
                    index -= 1;
                    if (index < 0) index = css.size() + index;
                    nodeId = JSON.parseObject(this.getOwner().runCdp("DOM.querySelector", Map.of("nodeId", this.nodeId, "selector", css.get(index))).toString()).get("nodeId");
                } catch (IndexOutOfBoundsException e) {
                    return null;
                }
                return ChromiumElement.makeChromiumEles(this.getOwner(), nodeId, 1, false);
            } else {
                List<Object> nodeIds = new ArrayList<>();
                for (String s : css)
                    nodeIds.add(JSON.parseObject(this.getOwner().runCdp("DOM.querySelector", Map.of("nodeId", this.nodeId, "selector", s)).toString()).get("nodeId"));
                if (nodeIds.contains(0)) return null;
                return ChromiumElement.makeChromiumEles(this.getOwner(), nodeId, 1, false);
            }
        }
        return null;
    }

    /**
     * 根据传入object id或backend id获取cdp中的node id
     *
     * @param objId backend id
     * @return cdp中的node id
     */
    private Integer getNodeId(String objId) {
        return JSON.parseObject(this.getOwner().runCdp("DOM.requestNode", Map.of("objectId", objId)).toString()).getInteger("nodeId");
    }

    /**
     * 根据传入node id或backend id获取js中的object id
     *
     * @param backendId backend id
     * @return js中的object id
     */
    private String getObjId(Integer backendId) {
        return JSON.parseObject(this.getOwner().runCdp("DOM.describeNode", Map.of("backendNodeId", backendId)).toString()).getJSONObject("object").getString("objectId");
    }

    /**
     * 根据传入node id获取backend id
     *
     * @param nodeId js中的nodeId
     * @return backend id
     */
    private Integer getBackendId(Integer nodeId) {
        return JSON.parseObject(this.getOwner().runCdp("DOM.describeNode", Map.of("nodeId", nodeId)).toString()).getJSONObject("node").getInteger("backendNodeId");
    }

}
