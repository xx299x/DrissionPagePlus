package com.ll.DrissonPage.element;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.base.*;
import com.ll.DrissonPage.error.extend.*;
import com.ll.DrissonPage.functions.Keys;
import com.ll.DrissonPage.functions.Locator;
import com.ll.DrissonPage.functions.Settings;
import com.ll.DrissonPage.functions.Web;
import com.ll.DrissonPage.page.ChromiumFrame;
import com.ll.DrissonPage.page.ChromiumBase;
import com.ll.DrissonPage.units.Clicker;
import com.ll.DrissonPage.units.Coordinate;
import com.ll.DrissonPage.units.PicType;
import com.ll.DrissonPage.units.rect.ElementRect;
import com.ll.DrissonPage.units.scroller.ElementScroller;
import com.ll.DrissonPage.units.setter.ChromiumElementSetter;
import com.ll.DrissonPage.units.states.ElementStates;
import com.ll.DrissonPage.units.waiter.ElementWaiter;
import lombok.Getter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class ChromiumElement extends DrissionElement<ChromiumBase, ChromiumElement> {
    protected static final List<String> FRAME_ELEMENT = List.of("iframe", "frame");
    @Getter
    private final String docId;

    private String tag;
    @Getter
    private Integer nodeId;
    @Getter
    private String objId;
    @Getter
    private Integer backendId;
    private ElementScroller scroll;
    private Clicker clicker;
    private SelectElement select;
    private ElementWaiter wait;
    private ElementRect rect;
    private ChromiumElementSetter set;
    private ElementStates states;
    private Pseudo pseudo;

    public ChromiumElement(ChromiumBase page, Integer nodeId, String objId, Integer backendId) {
        super(page);
        this.scroll = null;
        this.select = null;
        this.rect = null;
        this.set = null;
        this.states = null;
        this.pseudo = null;
        this.clicker = null;
        this.tag = null;
        this.wait = null;
        this.setType("ChromiumElement");
        if (nodeId != null && nodeId != 0 && objId != null && backendId != null && backendId != 0) {
            this.nodeId = nodeId;
            this.objId = objId;
            this.backendId = backendId;
        } else if (nodeId != null && nodeId != 0) {
            this.nodeId = nodeId;
            this.objId = this.getObjId(this.nodeId, null);
            this.backendId = this.getBackendId(this.nodeId);
        } else if (objId != null) {
            this.nodeId = this.getNodeId(objId, null);
            this.objId = objId;
            this.backendId = this.getBackendId(this.nodeId);
        } else if (backendId != null && backendId != 0) {
            this.nodeId = this.getNodeId(null, backendId);
            this.objId = this.getObjId(null, backendId);
            this.backendId = backendId;
        } else {
            throw new ElementLostError();
        }
        Object doc = this.runJs("return this.ownerDocument;");
        this.docId = doc != null && !doc.toString().isEmpty() ? JSON.parseObject(doc.toString()).getString("objectId") : null;
    }


    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        this.attrs().forEach((k, v) -> stringBuilder.append(k).append("='").append(v).append("'"));
        return "<ChromiumElement " + this.tag + " " + stringBuilder + ">";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ChromiumElement && this.backendId.equals(((ChromiumElement) obj).backendId);
    }

    /**
     * @return 返回元素tag
     */
    @Override
    public String tag() {
        if (this.tag == null)
            this.tag = JSON.parseObject(this.getOwner().runCdp("DOM.describeNode", Map.of("backendNodeId", this.backendId)).toString()).getJSONObject("node").getString("localName").toLowerCase();
        return this.tag;
    }

    /**
     * @return 返回元素outerHTML文本
     */
    @Override
    public String html() {
        return JSON.parseObject(this.getOwner().runCdp("DOM.getOuterHTML", Map.of("backendNodeId", this.backendId)).toString()).getString("outerHTML");
    }

    /**
     * @return 返回元素innerHTML文本
     */
    public String innerHtml() {
        return this.runJs("return this.innerHTML;").toString();
    }

    /**
     * @return 返回元素所有attribute属性
     */
    @Override
    public Map<String, String> attrs() {
        try {
            JSONArray attrs = JSON.parseObject(this.getOwner().runCdp("DOM.getAttributes", Map.of("nodeId", this.nodeId)).toString()).getJSONArray("attributes");
            //0,1   1,2
            Map<String, String> map = new HashMap<>();
            for (int i = 0; i < attrs.size(); i += 2)
                map.put(attrs.get(i).toString(), attrs.get(i + 1).toString());
            return map;
        } catch (CDPError e) {
            //文档根元素不能调用此方法
            return new HashMap<>();
        }
    }

    /**
     * @return 返回元素内所有文本，文本已格式化
     */
    @Override
    public String text() {
        List<SessionElement> sessionElements = SessionElement.makeSessionEle(this.html(), By.NULL(), null);
        return sessionElements == null || sessionElements.isEmpty() ? null : Web.getEleTxt(sessionElements.get(0));
    }

    /**
     * @return 返回未格式化处理的元素内文本
     */
    @Override
    public String rawText() {
        return this.property("innerText");
    }
    // -----------------d模式独有属性-------------------

    /**
     * @return 返回用于设置元素属性的对象
     */
    public ChromiumElementSetter set() {
        if (set == null) set = new ChromiumElementSetter(this);
        return set;
    }

    /**
     * @return 返回用于获取元素状态的对象
     */
    public ElementStates states() {
        if (states == null) states = new ElementStates(this);
        return states;
    }

    /**
     * @return 返回用于获取伪元素内容的对象
     */
    public Pseudo pseudo() {
        if (pseudo == null) pseudo = new Pseudo(this);
        return pseudo;
    }

    /**
     * @return 返回用于获取元素位置的对象
     */
    public ElementRect rect() {
        if (rect == null) rect = new ElementRect(this);
        return rect;
    }

    /**
     * @return 返回当前元素的shadow_root元素对象
     */
    public ShadowRoot shadowRoot() {
        JSONObject info = JSON.parseObject(this.getOwner().runCdp("DOM.describeNode", Map.of("backendNodeId", this.backendId)).toString()).getJSONObject("node");
        return info.get("shadowRoots") == null || info.get("shadowRoots").toString().isEmpty() ? null : new ShadowRoot(this, null, info.getJSONArray("shadowRoots").getJSONObject(0).getInteger("backendNodeId"));
    }

    /**
     * @return 返回当前元素的shadow_root元素对象
     */
    public ShadowRoot sr() {
        return shadowRoot();
    }

    /**
     * @return 用于滚动滚动条的对象
     */
    public ElementScroller scroll() {
        if (this.scroll == null) this.scroll = new ElementScroller(this);
        return this.scroll;
    }

    /**
     * @return 返回用于点击的对象
     */
    public Clicker click() {
        if (this.clicker == null) this.clicker = new Clicker(this);
        return this.clicker;
    }

    /**
     * @return 返回用于等待的对象
     */
    public ElementWaiter waits() {
        if (this.wait == null) this.wait = new ElementWaiter(this.getOwner(), this);
        return this.wait;
    }

    /**
     * @return 返回专门处理下拉列表的Select类，非下拉列表元素返回null
     */
    public SelectElement select() {
        if (this.select == null) if (!Objects.equals(this.tag(), "select")) return null;
        else this.select = new SelectElement(this);
        return this.select;
    }

    public String value() {
        return this.property("value");
    }

    /**
     * 选中或取消选中当前元素
     */
    public void check() {
        this.check(false);
    }

    /**
     * 选中或取消选中当前元素
     *
     * @param uncheck 是否取消选中
     */
    public void check(boolean uncheck) {
        this.check(uncheck, false);
    }

    /**
     * 选中或取消选中当前元素
     *
     * @param uncheck 是否取消选中
     * @param byJs    是否用js执行
     */
    public void check(boolean uncheck, boolean byJs) {
        boolean checked = this.states.isChecked();
        if (byJs) {
            String js = null;
            if (checked && uncheck) js = "this.checked=false";
            else if (!checked && !uncheck) js = "this.checked=true";
            if (js != null) {
                this.runJs(js);
                this.runJs("this.dispatchEvent(new Event(\"change\", {bubbles: true}));");
            }
        } else {
            if ((checked && uncheck) || (!checked && !uncheck)) {
                this.click().click();
            }
        }
    }

    /**
     * 返回上面某一级父元素，可指定层数或用查询语法定位
     *
     * @param by    查询选择器
     * @param index 选择第几个结果
     * @return 上级元素对象
     */
    @Override
    public ChromiumElement parent(By by, Integer index) {
        return super.parent(by, index);
    }

    /**
     * 返回上面某一级父元素，可指定层数或用查询语法定位
     *
     * @param loc   定位符
     * @param index 选择第几个结果
     * @return 上级元素对象
     */
    @Override
    public ChromiumElement parent(String loc, Integer index) {
        return super.parent(loc, index);
    }

    /**
     * 返回上面某一级父元素，可指定层数或用查询语法定位
     *
     * @param level 第几级父元素
     * @return 上级元素对象
     */
    @Override
    public ChromiumElement parent(Integer level) {
        return super.parent(level);
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child(Integer index, Double timeout, Boolean eleOnly) {
        return super.child(index, timeout, eleOnly);
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child(Integer index, Double timeout) {
        return super.child(index, timeout);
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index 第几个查询结果，1开始
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child(Integer index) {
        return super.child(index);
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child() {
        return super.child();
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child(String loc, Integer index, Double timeout, Boolean eleOnly) {
        return super.child(loc, index, timeout, eleOnly);
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child(String loc, Integer index, Double timeout) {
        return super.child(loc, index, timeout);
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc   用于筛选的查询语法
     * @param index 第几个查询结果，1开始
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child(String loc, Integer index) {
        return super.child(loc, index);
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc 用于筛选的查询语法
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child(String loc) {
        return super.child(loc);
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child(By by, Integer index, Double timeout, Boolean eleOnly) {
        return super.child(by, index, timeout, eleOnly);
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child(By by, Integer index, Double timeout) {
        return super.child(by, index, timeout);
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param index 第几个查询结果，1开始
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child(By by, Integer index) {
        return super.child(by, index);
    }

    /**
     * 返回当前元素的一个符合条件的直接子元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @return 直接子元素
     */
    @Override
    public ChromiumElement child(By by) {
        return super.child(by);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement prev(String loc, Integer index, Double timeout) {
        return super.prev(loc, index, timeout);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc   用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement prev(String loc, Integer index) {
        return super.prev(loc, index);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc 用于筛选的查询语法
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement prev(String loc) {
        return super.prev(loc);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement prev() {
        return super.prev();
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement prev(By by, Integer index, Double timeout) {
        return super.prev(by, index, timeout);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by    用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement prev(By by, Integer index) {
        return super.prev(by, index);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by 用于筛选的查询语法
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement prev(By by) {
        return super.prev(by);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement prev(String loc, Integer index, Double timeout, Boolean eleOnly) {
        return super.prev(loc, index, timeout, eleOnly);
    }

    /**
     * 返回当前元素前面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement prev(By by, Integer index, Double timeout, Boolean eleOnly) {
        return super.prev(by, index, timeout, eleOnly);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement next(By by, Integer index, Double timeout) {
        return super.next(by, index, timeout);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by    用于筛选的查询语法
     * @param index 第几个查询结果，1开始
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement next(By by, Integer index) {
        return super.next(by, index);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by 用于筛选的查询语法
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement next(By by) {
        return super.next(by);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement next(String loc, Integer index, Double timeout) {
        return super.next(loc, index, timeout);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc   用于筛选的查询语法
     * @param index 第几个查询结果，1开始
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement next(String loc, Integer index) {
        return super.next(loc, index);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc 用于筛选的查询语法
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement next(String loc) {
        return super.next(loc);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement next() {
        return super.next();
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param loc     用于筛选的查询语法
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement next(String loc, Integer index, Double timeout, Boolean eleOnly) {
        return super.next(loc, index, timeout, eleOnly);
    }

    /**
     * 返回当前元素后面一个符合条件的同级元素，可用查询语法筛选，可指定返回筛选结果的第几个
     *
     * @param by      用于筛选的查询语法
     * @param index   第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本
     */
    @Override
    public ChromiumElement next(By by, Integer index, Double timeout, Boolean eleOnly) {
        return super.next(by, index, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 本元素前面的某个元素或节点
     */
    @Override
    public ChromiumElement before(By by) {
        return super.before(by);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by    用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素前面的某个元素或节点
     */
    @Override
    public ChromiumElement before(By by, Integer index) {
        return super.before(by, index);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的某个元素或节点
     */
    @Override
    public ChromiumElement before(By by, Integer index, Double timeout) {
        return super.before(by, index, timeout);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    @Override
    public ChromiumElement before(By by, Integer index, Double timeout, Boolean eleOnly) {
        return super.before(by, index, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 本元素前面的某个元素或节点
     */
    @Override
    public ChromiumElement before() {
        return super.before();
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素前面的某个元素或节点
     */
    @Override
    public ChromiumElement before(String loc) {
        return super.before(loc);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc   用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素前面的某个元素或节点
     */
    @Override
    public ChromiumElement before(String loc, Integer index) {
        return super.before(loc, index);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的某个元素或节点
     */
    @Override
    public ChromiumElement before(String loc, Integer index, Double timeout) {
        return super.before(loc, index, timeout);
    }

    /**
     * 返回文档中当前元素前面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的某个元素或节点
     */
    @Override
    public ChromiumElement before(String loc, Integer index, Double timeout, Boolean eleOnly) {
        return super.before(loc, index, timeout, eleOnly);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 本元素后面的某个元素或节点
     */
    @Override
    public ChromiumElement after(By by) {
        return super.after(by);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by    用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素后面的某个元素或节点
     */
    @Override
    public ChromiumElement after(By by, Integer index) {
        return super.after(by, index);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素后面的某个元素或节点
     */
    @Override
    public ChromiumElement after(By by, Integer index, Double timeout) {
        return super.after(by, index, timeout);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素后面的某个元素或节点
     */
    @Override
    public ChromiumElement after(By by, Integer index, Double timeout, Boolean eleOnly) {
        return super.after(by, index, timeout, eleOnly);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 本元素后面的某个元素或节点
     */
    @Override
    public ChromiumElement after() {
        return super.after();
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素后面的某个元素或节点
     */
    @Override
    public ChromiumElement after(String loc) {
        return super.after(loc);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc   用于筛选的查询语法
     * @param index 前面第几个查询结果，1开始
     * @return 本元素后面的某个元素或节点
     */
    @Override
    public ChromiumElement after(String loc, Integer index) {
        return super.after(loc, index);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素后面的某个元素或节点
     */
    @Override
    public ChromiumElement after(String loc, Integer index, Double timeout) {
        return super.after(loc, index, timeout);
    }

    /**
     * 返回文档中此当前元素后面符合条件的一个元素，可用查询语法筛选，可指定返回筛选结果的第几个
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param index   前面第几个查询结果，1开始
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素后面的某个元素或节点
     */
    @Override
    public ChromiumElement after(String loc, Integer index, Double timeout, Boolean eleOnly) {
        return super.after(loc, index, timeout, eleOnly);
    }

    /**
     * 返回当前元素符合条件的直接子元素或节点组成的列表，可用查询语法筛选
     *
     * @return 直接子元素或节点文本组成的列表
     */

    @Override
    public List<ChromiumElement> children() {
        return super.children();
    }

    /**
     * 返回当前元素符合条件的直接子元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return 直接子元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> children(String loc) {
        return super.children(loc);
    }

    /**
     * 返回当前元素符合条件的直接子元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 直接子元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> children(String loc, Double timeout) {
        return super.children(loc, timeout);
    }

    /**
     * 返回当前元素符合条件的直接子元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 直接子元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> children(String loc, Double timeout, Boolean eleOnly) {
        return super.children(loc, timeout, eleOnly);
    }

    /**
     * 返回当前元素符合条件的直接子元素或节点组成的列表，可用查询语法筛选
     *
     * @param by 用于筛选的查询语法
     * @return 直接子元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> children(By by) {
        return super.children(by);
    }

    /**
     * 返回当前元素符合条件的直接子元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 直接子元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> children(By by, Double timeout) {
        return super.children(by, timeout);
    }

    /**
     * 返回当前元素符合条件的直接子元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 直接子元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> children(By by, Double timeout, Boolean eleOnly) {
        return super.children(by, timeout, eleOnly);
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 查询元素
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> prevs(String loc) {
        return super.prevs(loc);
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     查询元素
     * @param timeout 等待时间
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> prevs(String loc, Double timeout) {
        return super.prevs(loc, timeout);
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     查询元素
     * @param timeout 等待时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> prevs(String loc, Double timeout, Boolean eleOnly) {
        return super.prevs(loc, timeout, eleOnly);
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by 查询元素
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> prevs(By by) {
        return super.prevs(by);
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      查询元素
     * @param timeout 等待时间
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> prevs(By by, Double timeout) {
        return super.prevs(by, timeout);
    }

    /**
     * 返回当前元素前面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      查询元素
     * @param timeout 等待时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> prevs(By by, Double timeout, Boolean eleOnly) {
        return super.prevs(by, timeout, eleOnly);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by 用于筛选的查询语法
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> nexts(By by) {
        return super.nexts(by);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> nexts(By by, Double timeout) {
        return super.nexts(by, timeout);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> nexts(By by, Double timeout, Boolean eleOnly) {
        return super.nexts(by, timeout, eleOnly);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> nexts() {
        return super.nexts();
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc 用于筛选的查询语法
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> nexts(String loc) {
        return super.nexts(loc);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> nexts(String loc, Double timeout) {
        return super.nexts(loc, timeout);
    }

    /**
     * 返回当前元素后面符合条件的同级元素或节点组成的列表，可用查询语法筛选
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 兄弟元素或节点文本组成的列表
     */
    @Override
    public List<ChromiumElement> nexts(String loc, Double timeout, Boolean eleOnly) {
        return super.nexts(loc, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 本元素前面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> befores(By by) {
        return super.befores(by);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> befores(By by, Double timeout) {
        return super.befores(by, timeout);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> befores(By by, Double timeout, Boolean eleOnly) {
        return super.befores(by, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 本元素前面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> befores() {
        return super.befores();
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素前面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> befores(String loc) {
        return super.befores(loc);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素前面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> befores(String loc, Double timeout) {
        return super.befores(loc, timeout);
    }

    /**
     * 返回文档中当前元素前面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素前面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> befores(String loc, Double timeout, Boolean eleOnly) {
        return super.befores(loc, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by 用于筛选的查询语法
     * @return 本元素后面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> afters(By by) {
        return super.afters(by);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素后面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> afters(By by, Double timeout) {
        return super.afters(by, timeout);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param by      用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素后面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> afters(By by, Double timeout, Boolean eleOnly) {
        return super.afters(by, timeout, eleOnly);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @return 本元素后面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> afters() {
        return super.afters();
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc 用于筛选的查询语法
     * @return 本元素后面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> afters(String loc) {
        return super.afters(loc);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @return 本元素后面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> afters(String loc, Double timeout) {
        return super.afters(loc, timeout);
    }

    /**
     * 返回文档中当前元素后面符合条件的元素或节点组成的列表，可用查询语法筛选
     * 查找范围不限同级元素，而是整个DOM文档
     *
     * @param loc     用于筛选的查询语法
     * @param timeout 查找节点的超时时间（秒）
     * @param eleOnly 是否只获取元素，为False时把文本、注释节点也纳入
     * @return 本元素后面的元素或节点组成的列表
     */
    @Override
    public List<ChromiumElement> afters(String loc, Double timeout, Boolean eleOnly) {
        return super.afters(loc, timeout, eleOnly);
    }

    @Override
    public String attr(String attr) {
        if (attr == null || attr.trim().isEmpty()) throw new NullPointerException();
        attr = attr.trim();
        Map<String, String> attrs = this.attrs();
        switch (attr) {
            case "href":
                String link = attrs.get("href");
                if (link == null || link.toLowerCase().startsWith("javascript:") || link.toLowerCase().startsWith("mailto:"))
                    return link;
                else return Web.makeAbsoluteLink(link, this.property("baseURI"));
            case "src": {
                Object o = attrs.get("src");
                return Web.makeAbsoluteLink(o == null ? "" : o.toString(), this.property("baseURI"));
            }
            case "text":
                return this.text();
            case "innerText":
                return this.rawText();
            case "html":
            case "outerHTML":
                return this.html();
            case "innerHTML":
                return this.innerHtml();
            default: {
                Object o = attrs.get(attr);
                return o == null ? null : o.toString();
            }
        }
    }

    /**
     * 删除元素一个attribute属性
     *
     * @param attr 属性名
     */
    public void removeAttr(String attr) {
        this.runJs("this.removeAttribute(" + attr + ");");
    }

    /**
     * 获取一个property属性值
     *
     * @param prop 属性名
     * @return 属性值文本
     */
    public String property(String prop) {
        try {
            Object o = this.runJs("return this." + prop + ";");
            return o instanceof String ? Web.formatHtml(o.toString()) : o.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js js文本可以是路径，文本中用this表示本元素
     * @return 运行的结果
     */
    public Object runJs(String js) {
        return runJs(js, new ArrayList<>());
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js     js文本可以是路径，文本中用this表示本元素
     * @param params 参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     * @return 运行的结果
     */
    public Object runJs(String js, List<Object> params) {
        return runJs(js, null, params);
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js      js文本可以是路径，文本中用this表示本元素
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
     * @param js      js文本可以是路径，文本中用this表示本元素
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
     * @param js js文本可以是路径，文本中用this表示本元素
     * @return 运行的结果
     */
    public Object runJs(Path js) {
        return runJs(js, new ArrayList<>());
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js     js文本可以是路径，文本中用this表示本元素
     * @param params 参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     * @return 运行的结果
     */
    public Object runJs(Path js, List<Object> params) {
        return runJs(js, null, params);
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js      js文本可以是路径，文本中用this表示本元素
     * @param timeout js超时时间（秒），为None则使用页面timeouts.script设置
     * @param params  参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     * @return 运行的结果
     */
    public Object runJs(Path js, Double timeout, List<Object> params) {
        return runJs(js, false, timeout, params);
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js      js文本可以是路径，文本中用this表示本元素
     * @param asExpr  是否作为表达式运行，为True时args无效
     * @param timeout js超时时间（秒），为None则使用页面timeouts.script设置
     * @param params  参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     * @return 运行的结果
     */
    public Object runJs(Path js, Boolean asExpr, Double timeout, List<Object> params) {
        timeout = timeout != null ? timeout : this.getOwner().getTimeouts().getScript();
        try {
            return ChromiumElement.runJs(this, js.toAbsolutePath().toString(), asExpr, timeout, params);
        } catch (IOError e) {
            return ChromiumElement.runJs(this, js.toString(), asExpr, timeout, params);
        }

    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js js文本可以是路径，文本中用this表示本元素
     */
    public void runAsyncJs(String js) {
        runAsyncJs(js, new ArrayList<>());
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js     js文本可以是路径，文本中用this表示本元素
     * @param params 参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     */
    public void runAsyncJs(String js, List<Object> params) {
        runAsyncJs(js, false, params);
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js     js文本可以是路径，文本中用this表示本元素
     * @param asExpr 是否作为表达式运行，为True时args无效
     * @param params 参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     */
    public void runAsyncJs(String js, Boolean asExpr, List<Object> params) {
        this.runJs(js, asExpr, 0.0, params);
    }


    /**
     * 对本元素执行javascript代码
     *
     * @param js js文本可以是路径，文本中用this表示本元素
     */
    public void runAsyncJs(Path js) {
        runAsyncJs(js, new ArrayList<>());
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js     js文本可以是路径，文本中用this表示本元素
     * @param params 参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     */
    public void runAsyncJs(Path js, List<Object> params) {
        runAsyncJs(js, false, params);
    }

    /**
     * 对本元素执行javascript代码
     *
     * @param js     js文本可以是路径，文本中用this表示本元素
     * @param asExpr 是否作为表达式运行，为True时args无效
     * @param params 参数，按顺序在js文本中对应arguments[0]、arguments[1]...
     */
    public void runAsyncJs(Path js, Boolean asExpr, List<Object> params) {
        this.runJs(js, asExpr, 0.0, params);
    }

    /**
     * 返回当前元素下级符合条件的一个元素、属性或节点文本
     *
     * @param by 查询元素
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement ele(By by) {
        return super.ele(by);
    }

    /**
     * 返回当前元素下级符合条件的一个元素、属性或节点文本
     *
     * @param by    查询元素
     * @param index 获取第几个元素，下标从1开始可传入负数获取倒数第几个
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement ele(By by, int index) {
        return super.ele(by, index);
    }

    /**
     * 返回当前元素下级符合条件的一个元素、属性或节点文本
     *
     * @param by      查询元素
     * @param index   获取第几个元素，下标从1开始可传入负数获取倒数第几个
     * @param timeout 查找元素超时时间（秒），默认与元素所在页面等待时间一致
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement ele(By by, int index, Double timeout) {
        return super.ele(by, index, timeout);
    }

    /**
     * 返回当前元素下级符合条件的一个元素、属性或节点文本
     *
     * @param loc 查询元素
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement ele(String loc) {
        return super.ele(loc);
    }

    /**
     * 返回当前元素下级符合条件的一个元素、属性或节点文本
     *
     * @param loc   查询元素
     * @param index 获取第几个元素，下标从1开始可传入负数获取倒数第几个
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement ele(String loc, int index) {
        return super.ele(loc, index);
    }

    /**
     * 返回当前元素下级符合条件的一个元素、属性或节点文本
     *
     * @param loc     查询元素
     * @param index   获取第几个元素，下标从1开始可传入负数获取倒数第几个
     * @param timeout 查找元素超时时间（秒），默认与元素所在页面等待时间一致
     * @return ChromiumElement对象
     */
    @Override
    public ChromiumElement ele(String loc, int index, Double timeout) {
        return super.ele(loc, index, timeout);
    }

    /**
     * 返回当前元素下级所有符合条件的子元素、属性或节点文本
     *
     * @param by 元素的定位信息，可以是loc元组，或查询字符串
     * @return ChromiumElement对象或属性
     */
    @Override
    public List<ChromiumElement> eles(By by) {
        return this.eles(by, null);
    }

    /**
     * 返回当前元素下级所有符合条件的子元素、属性或节点文本
     *
     * @param by      元素的定位信息，可以是loc元组，或查询字符串
     * @param timeout 查找元素超时时间（秒），默认与元素所在页面等待时间一致
     * @return ChromiumElement对象或属性
     */
    @Override
    public List<ChromiumElement> eles(By by, Double timeout) {
        return this._ele(by, timeout, null, null, null, null);
    }

    /**
     * 返回当前元素下级所有符合条件的子元素、属性或节点文本
     *
     * @param loc 元素的定位信息，可以是loc元组，或查询字符串
     * @return ChromiumElement对象或属性
     */
    @Override
    public List<ChromiumElement> eles(String loc) {
        return this.eles(loc, null);
    }

    /**
     * 返回当前元素下级所有符合条件的子元素、属性或节点文本
     *
     * @param loc     元素的定位信息，可以是loc元组，或查询字符串
     * @param timeout 查找元素超时时间（秒），默认与元素所在页面等待时间一致
     * @return ChromiumElement对象或属性
     */
    @Override
    public List<ChromiumElement> eles(String loc, Double timeout) {
        return this._ele(loc, timeout, null, null, null, null);

    }

    /**
     * 查找一个符合条件的元素，以SessionElement形式返回
     *
     * @param by    查询元素
     * @param index 获取第几个，从1开始，可传入负数获取倒数第几个
     * @return SessionElement对象或属性
     */
    @Override
    public SessionElement sEle(By by, Integer index) {
        try {
            return FRAME_ELEMENT.contains(this.tag) ? SessionElement.makeSessionEle(this.innerHtml(), by, index).get(0) : SessionElement.makeSessionEle(this, by, index).get(0);
        } catch (IndexOutOfBoundsException e) {
            if (Settings.raiseWhenEleNotFound) {
                throw new ElementNotFoundError(null, "s_ele()", Map.of("by", by.toString()));
            }
            return null;
        }
    }

    /**
     * 查找一个符合条件的元素，以SessionElement形式返回
     *
     * @param loc   定位符
     * @param index 获取第几个，从1开始，可传入负数获取倒数第几个
     * @return SessionElement对象或属性
     */
    @Override
    public SessionElement sEle(String loc, Integer index) {
        try {
            return FRAME_ELEMENT.contains(this.tag) ? SessionElement.makeSessionEle(this.innerHtml(), loc, index).get(0) : SessionElement.makeSessionEle(this, loc, index).get(0);
        } catch (IndexOutOfBoundsException e) {
            if (Settings.raiseWhenEleNotFound) {
                throw new ElementNotFoundError(null, "s_ele()", Map.of("loc", loc));
            }
            return null;
        }
    }

    /**
     * 查找所有符合条件的元素，以SessionElement列表形式返回
     *
     * @param by 查询元素
     * @return SessionElement或属性
     */
    @Override
    public List<SessionElement> sEles(By by) {
        return FRAME_ELEMENT.contains(this.tag) ? SessionElement.makeSessionEle(this.innerHtml(), by, null) : SessionElement.makeSessionEle(this, by, null);
    }

    /**
     * 查找所有符合条件的元素，以SessionElement列表形式返回
     *
     * @param loc 定位符
     * @return SessionElement或属性
     */
    @Override
    public List<SessionElement> sEles(String loc) {
        return FRAME_ELEMENT.contains(this.tag) ? SessionElement.makeSessionEle(this.innerHtml(), loc, null) : SessionElement.makeSessionEle(this, loc, null);
    }

    /**
     * 返回当前元素下级符合条件的子元素、属性或节点文本，默认返回第一个
     *
     * @param by       查询元素
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从1开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @return ChromiumElement对象或文本、属性或其组成的列表
     */
    @Override
    protected List<ChromiumElement> findElements(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        return ChromiumElement.findInChromiumEle(this, by, index, timeout, relative);
    }

    /**
     * 返回当前元素下级符合条件的子元素、属性或节点文本，默认返回第一个
     *
     * @param loc      定位符
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从1开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @return ChromiumElement对象或文本、属性或其组成的列表
     */
    @Override
    protected List<ChromiumElement> findElements(String loc, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        return ChromiumElement.findInChromiumEle(this, loc, index, timeout, relative);
    }

    /**
     * 返回元素样式属性值，可获取伪元素属性值
     *
     * @param style 样式属性名称
     * @return 样式属性的值
     */
    public String style(String style) {
        return style(style, "");
    }

    /**
     * 返回元素样式属性值，可获取伪元素属性值
     *
     * @param style     样式属性名称
     * @param pseudoEle 伪元素名称（如有）
     * @return 样式属性的值
     */
    public String style(String style, String pseudoEle) {
        if (pseudoEle != null && !pseudoEle.isEmpty())
            pseudoEle = pseudoEle.startsWith(":") ? ", \"" + pseudoEle + "\"" : ", \"::" + pseudoEle + "\"";
        return this.runJs("return window.getComputedStyle(this" + pseudoEle + ").getPropertyValue(\"" + style + "\");").toString();
    }

    /**
     * 返回元素src资源，base64的可转为bytes返回，其它返回str
     *
     * @return 资源内容
     */
    public Object src() {
        return src(true);
    }

    /**
     * 返回元素src资源，base64的可转为bytes返回，其它返回str
     *
     * @param base64ToBytes 为True时，如果是base64数据，转换为bytes格式
     * @return 资源内容
     */
    public Object src(boolean base64ToBytes) {
        return src(null, base64ToBytes);
    }

    /**
     * 返回元素src资源，base64的可转为bytes返回，其它返回str
     *
     * @param timeout       等待资源加载的超时时间（秒）
     * @param base64ToBytes 为True时，如果是base64数据，转换为bytes格式
     * @return 资源内容
     */
    public Object src(Double timeout, boolean base64ToBytes) {
        timeout = (timeout == 0) ? this.getOwner().timeout() : timeout;
        if (Objects.equals(this.tag(), "img")) {
            // 等待图片加载完成
            String js = "return this.complete && typeof this.naturalWidth !== 'undefined' " + "&& this.naturalWidth > 0 && typeof this.naturalHeight !== 'undefined' " + "&& this.naturalHeight > 0";
            long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
            while (this.runJs(js) != null && System.currentTimeMillis() < endTime) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        String src = this.attr("src");
        if (src.toLowerCase().startsWith("data:image")) {
            String[] split = src.split(",", 2);
            if (base64ToBytes) {
                return Base64.getDecoder().decode(split[split.length - 1]);
            } else {
                return split[split.length - 1];
            }
        }

        boolean isBlob = src.startsWith("blob");
        Object result = null;
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        while (System.currentTimeMillis() < endTime) {
            if (isBlob) {
                // Assuming getBlob method is defined elsewhere
                result = Web.getBlob(this.getOwner(), src, base64ToBytes);
                if (result != null) break;
            } else {
                src = this.property("currentSrc");
                if (src == null) continue;
                Object o = JSON.parseObject(this.getOwner().runCdp("DOM.describeNode", Map.of("backendNodeId", this.backendId)).toString()).getJSONObject("node").get("frameId");
                // Assuming getFrameId and getResourceContent methods are defined elsewhere
                Object frame = o != null ? o : this.getOwner().getFrameId();
                try {
                    result = this.getOwner().runCdp("Page.getResourceContent", Map.of("frameId", frame, "url", src));
                    break;
                } catch (CDPError e) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        if (result == null) return null;
        else if (isBlob) return result;
        else {
            JSONObject jsonObject = JSON.parseObject(result.toString());
            if (jsonObject.getBoolean("base64Encoded") && base64ToBytes)
                return Base64.getDecoder().decode(jsonObject.get("content").toString());
            else return jsonObject.get("content");
        }
    }

    /**
     * 保存图片或其它有src属性的元素的资源
     *
     * @return 返回保存路径
     */
    public String save() {
        return save(null);
    }

    /**
     * 保存图片或其它有src属性的元素的资源
     *
     * @param path 文件保存路径，为None时保存到当前文件夹
     * @return 返回保存路径
     */
    public String save(String path) {
        return save(path, null);
    }

    /**
     * 保存图片或其它有src属性的元素的资源
     *
     * @param path 文件保存路径，为None时保存到当前文件夹
     * @param name 文件名称，为None时从资源url获取
     * @return 返回保存路径
     */
    public String save(String path, String name) {
        return save(path, name, null);
    }

    /**
     * 保存图片或其它有src属性的元素的资源
     *
     * @param path    文件保存路径，为None时保存到当前文件夹
     * @param name    文件名称，为None时从资源url获取
     * @param timeout 等待资源加载的超时时间（秒）
     * @return 返回保存路径
     */
    public String save(String path, String name, Double timeout) {
        Object data = this.src(timeout, true);
        if (data == null) throw new NoResourceError();

        path = (path == null || path.isEmpty()) ? "." : path;
        if (name == null && Objects.equals(this.tag(), "img")) {
            String src = this.attr("src");
            if (src.toLowerCase().startsWith("data:image")) {
                String[] parts = src.split(",", 2);
                String extension = (parts.length > 1) ? parts[0].split("/")[1].split(";")[0] : null;
                name = (extension != null) ? "img." + extension : null;
            }
        }
        name = (name == null) ? Paths.get(this.property("currentSrc")).getFileName().toString() : name;
        Path filePath = com.ll.DataRecorder.Tools.getUsablePath(Paths.get(path, name).toFile().getPath()).toAbsolutePath();
        try {
            Files.write(filePath, (data instanceof byte[]) ? (byte[]) data : data.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            // Handle the exception appropriately
        }

        return filePath.toString();
    }

    /**
     * 对当前元素截图，可保存到文件，或以字节方式返回
     *
     * @param path     文件保存路径
     * @param name     完整文件名，后缀可选 'jpg','jpeg','png','webp'
     * @param asBytes  是否以字节形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数和as_base64参数无效
     * @param asBase64 是否以base64字符串形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数无效
     * @return 图片完整路径或字节文本
     */
    public Object getScreenshot(String path, String name, PicType asBytes, PicType asBase64) {
        return getScreenshot(path, name, asBytes, asBase64, true);
    }

    /**
     * 对当前元素截图，可保存到文件，或以字节方式返回
     *
     * @param path           文件保存路径
     * @param name           完整文件名，后缀可选 'jpg','jpeg','png','webp'
     * @param asBytes        是否以字节形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数和as_base64参数无效
     * @param asBase64       是否以base64字符串形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数无效
     * @param scrollToCenter 截图前是否滚动到视口中央
     * @return 图片完整路径或字节文本
     */
    public Object getScreenshot(String path, String name, PicType asBytes, PicType asBase64, boolean scrollToCenter) {
        if ("img".equals(this.tag())) {
            // 等待图片加载完成
            String js = "return this.complete && typeof this.naturalWidth !== 'undefined' && this.naturalWidth > 0 " + "&& typeof this.naturalHeight !== 'undefined' && this.naturalHeight > 0";
            long endTime = (long) (System.currentTimeMillis() + this.getOwner().timeout() * 1000);
            while (!((Boolean) this.runJs(js)) && System.currentTimeMillis() < endTime) try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (scrollToCenter) this.scroll.toSee(true);
        Coordinate location = this.rect().location();
        int left = location.getX();
        int top = location.getY();
        Coordinate size = this.rect().size();
        int width = size.getX();
        int height = size.getY();
        Coordinate leftTop = new Coordinate(left, top);
        Coordinate rightBottom = new Coordinate(left + width, top + height);
        if (name == null) name = this.tag() + ".jpg";
        return this.getOwner()._getScreenshot(path, name, asBytes, asBase64, false, leftTop, rightBottom, this);
    }

    /**
     * 输入文本或组合键，也可用于输入文件路径到input元素（路径间用\n间隔）
     *
     * @param value 文本值或按键组合
     */
    public void input(Object value) {
        input(value, true);
    }

    /**
     * 输入文本或组合键，也可用于输入文件路径到input元素（路径间用\n间隔）
     *
     * @param value 文本值或按键组合
     * @param clean 输入前是否清空文本框
     */
    public void input(Object value, boolean clean) {
        input(value, clean, false);
    }

    /**
     * 输入文本或组合键，也可用于输入文件路径到input元素（路径间用\n间隔）
     *
     * @param value 文本值或按键组合
     * @param clean 输入前是否清空文本框
     * @param byJs  是否用js方式输入，不能输入组合键
     */
    public void input(Object value, boolean clean, boolean byJs) {

        if (Objects.equals(this.tag(), "input") && Objects.equals(this.attr("type"), "file")) {
            this.setFileInput(value);
            return;
        }
        if (byJs) {
            if (clean) this.clear(true);
            StringBuilder v = new StringBuilder();
            if (value instanceof List || value instanceof String[])
                if (value instanceof List) ((List<?>) value).forEach(v::append);
                else for (String s : (String[]) value) v.append(s);
            this.set().prop("value", v.toString());
            this.runJs("this.dispatchEvent(new Event(\"change\", {bubbles: true}));");
            return;
        }
        if (clean && !String.valueOf(value).equals("\n") && !String.valueOf(value).equals("\ue007")) this.clear(false);
        else this.inputFocus();
        Keys.inputTextOrKeys(this.getOwner(), value);
    }

    /**
     * 清空元素文本
     */
    public void clear() {
        clear(false);
    }

    /**
     * 清空元素文本
     *
     * @param byJs 是否用js方式清空，为False则用全选+del模拟输入删除
     */
    public void clear(boolean byJs) {
        if (byJs) {
            this.runJs("this.value='';");
            this.runJs("this.dispatchEvent(new Event(\"change\", {bubbles: true}));");
            return;
        }
        this.inputFocus();
        this.input(new String[]{"\ue009", "a", "\ue017"}, false, false);
    }

    /**
     * 输入前使元素获取焦点
     */
    protected void inputFocus() {
        try {
            this.getOwner().runCdp("DOM.focus", Map.of("backendNodeId", this.getBackendId()));
        } catch (Exception e) {
            this.click().click();
        }
    }

    /**
     * 使元素获取焦点
     */
    protected void focus() {
        try {
            this.getOwner().runCdp("DOM.focus", Map.of("backendNodeId", this.getBackendId()));
        } catch (Exception e) {
            this.getOwner().runJs("this.focus();");
        }
    }

    /**
     * 鼠标悬停，可接受偏移量，偏移量相对于元素左上角坐标。不传入x或y值时悬停在元素中点
     */
    public void hover() {
        hover(null);
    }

    /**
     * 鼠标悬停，可接受偏移量，偏移量相对于元素左上角坐标。不传入x或y值时悬停在元素中点
     *
     * @param coordinate 相对元素左上角坐标
     */
    public void hover(Coordinate coordinate) {
        this.getOwner().scroll().toSee(this);
        coordinate = coordinate != null ? coordinate : new Coordinate(0, 0);
        coordinate = Web.offsetScroll(this, coordinate.getX(), coordinate.getY());
        this.getOwner().runCdp("Input.dispatchMouseEvent", Map.of("type", "mouseMoved", "x", coordinate.getX(), "y", coordinate.getY(), "_ignore", new AlertExistsError()));
    }

    /**
     * 拖拽当前元素到相对位置
     *
     * @param coordinate 另一个元素或坐标，坐标为元素中点的坐标
     */
    public void drag(Coordinate coordinate) {
        drag(coordinate, .5f);
    }

    /**
     * 拖拽当前元素到相对位置
     *
     * @param coordinate 另一个元素或坐标，坐标为元素中点的坐标
     * @param duration   拖动用时，传入0即瞬间到达
     */
    public void drag(Coordinate coordinate, double duration) {
        Coordinate midpoint = this.rect.midpoint();
        if (coordinate == null) coordinate = new Coordinate(0, 0);
        this.dragTo(coordinate, duration);
    }

    /**
     * 拖拽当前元素，目标为另一个元素
     *
     * @param coordinate 另一个元素或坐标，坐标为元素中点的坐标
     */
    public void dragTo(Coordinate coordinate) {
        dragTo(coordinate, 0.5);
    }

    /**
     * 拖拽当前元素，目标为另一个元素
     *
     * @param coordinate 另一个元素或坐标，坐标为元素中点的坐标
     * @param duration   拖动用时，传入0即瞬间到达
     */
    public void dragTo(Coordinate coordinate, double duration) {
        this.getOwner().actions().hold(this).moveTo(coordinate, null, duration).release();
    }

    /**
     * 拖拽当前元素，目标为另一个元素
     *
     * @param ele 另一个元素或坐标，坐标为元素中点的坐标
     */
    public void dragTo(ChromiumElement ele) {
        dragTo(ele, 0.5);
    }

    /**
     * 拖拽当前元素，目标为另一个元素
     *
     * @param ele      另一个元素或坐标，坐标为元素中点的坐标
     * @param duration 拖动用时，传入0即瞬间到达
     */
    public void dragTo(ChromiumElement ele, double duration) {
        this.getOwner().actions().hold(this).moveTo(ele, null, duration).release();
    }

    /**
     * 根据传入node id或backend id获取js中的object id
     *
     * @param nodeId    cdp中的node id
     * @param backendId backend id
     * @return js中的object id
     */
    private String getObjId(Integer nodeId, Integer backendId) {
        return nodeId != null ? JSON.parseObject(this.getOwner().runCdp("DOM.resolveNode", Map.of("nodeId", nodeId)).toString()).getJSONObject("object").getString("objectId") : JSON.parseObject(this.getOwner().runCdp("DOM.describeNode", Map.of("backendNodeId", backendId)).toString()).getJSONObject("object").getString("objectId");
    }

    /**
     * 根据传入object id或backend id获取cdp中的node id
     *
     * @param objId     js中的object id
     * @param backendId backend id
     * @return cdp中的node id
     */
    private Integer getNodeId(String objId, Integer backendId) {
        if (objId != null)
            return JSON.parseObject(this.getOwner().runCdp("DOM.requestNode", Map.of("objectId", objId)).toString()).getInteger("nodeId");
        else {
            JSONObject jsonObject = JSON.parseObject(this.getOwner().runCdp("DOM.describeNode", Map.of("backendNodeId", backendId)).toString()).getJSONObject("node");
            this.tag = jsonObject.getString("localName");
            return jsonObject.getInteger("nodeId");
        }
    }

    /**
     * 根据传入node id获取backend id
     *
     * @param nodeId js中的nodeId
     * @return backend id
     */
    private Integer getBackendId(Integer nodeId) {

        JSONObject jsonObject = JSON.parseObject(this.getOwner().runCdp("DOM.describeNode", Map.of("nodeId", nodeId)).toString()).getJSONObject("node");
        this.tag = jsonObject.getString("localName");
        return jsonObject.getInteger("backendNodeId");
    }

    /**
     * 返获取绝对的css路径或xpath路径
     *
     * @param mode 'css' 或 'xpath'
     * @return 绝对路径
     */
    @Override
    protected String getElePath(ElePathMode mode) {
        String txt1, txt3, txt4, txt5;

        if ("xpath".equals(mode.getMode())) {
            txt1 = "var tag = el.nodeName.toLowerCase();";
            txt3 = " && sib.nodeName.toLowerCase() == tag";
            txt4 = "if (nth > 1) { path = '/' + tag + '[' + nth + ']' + path; } else { path = '/' + tag + path; }";
            txt5 = "return path;";
        } else if ("css".equals(mode.getMode())) {
            txt1 = "";
            txt3 = "";
            txt4 = "path = '>' + el.tagName.toLowerCase() + \":nth-child(\" + nth + \")\" + path;";
            txt5 = "return path.substr(1);";
        } else {
            throw new IllegalArgumentException("mode参数只能是'xpath'或' css'，现在是：'" + mode + "'。");
        }

        String js = "function() {" + "    function e(el) {" + "        if (!(el instanceof Element)) return;" + "        var path = '';" + "        while (el.nodeType === Node.ELEMENT_NODE) {" + txt1 + "            var sib = el, nth = 0;" + "            while (sib) {" + "                if (sib.nodeType === Node.ELEMENT_NODE" + txt3 + ") {" + "                    nth += 1;" + "                }" + "                sib = sib.previousSibling;" + "            }" + txt4 + "            el = el.parentNode;" + "        }" + txt5 + "    }" + "    return e(this);" + "}";
        return this.runJs(js).toString();
    }

    protected void setFileInput(Object files) {
        if (files instanceof Integer || files instanceof Float || files instanceof Double) files = files.toString();
        if (!(files instanceof String) && !(files instanceof List) && !(files instanceof String[]))
            throw new ClassCastException("类型只能为字符串，字符串数组，字符串集合");
        if (files instanceof String) {
            String[] split = ((String) files).split("\n");
            files = new ArrayList<>(Arrays.asList(split));
        } else if (files instanceof String[]) {
            files = new ArrayList<>(Arrays.asList((String[]) files));
        }
        List<String> list = new ArrayList<>();
        for (String s : (List<String>) files) list.add(Paths.get(s).toFile().getAbsolutePath());
        this.getOwner().runCdp("DOM.setFileInputFiles", Map.of("files", list, "backendNodeId", this.getBackendId()));
    }


    /**
     * 返回当前元素下级符合条件的子元素、属性或节点文本，默认返回第一个
     *
     * @param ele      查询元素
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从1开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @return ChromiumElement对象或文本、属性或其组成的列表
     */
    protected static List<ChromiumElement> findInChromiumEle(ChromiumElement ele, String loc, Integer index, Double timeout, Boolean relative) {
        return _findInChromiumEle(ele, Locator.getLoc(loc), index, timeout, relative);
    }

    /**
     * 返回当前元素下级符合条件的子元素、属性或节点文本，默认返回第一个
     *
     * @param by       查询元素
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从1开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @return ChromiumElement对象或文本、属性或其组成的列表
     */
    protected static List<ChromiumElement> findInChromiumEle(ChromiumElement ele, By by, Integer index, Double timeout, Boolean relative) {
        return _findInChromiumEle(ele, Locator.getLoc(by), index, timeout, relative);
    }

    /**
     * 返回当前元素下级符合条件的子元素、属性或节点文本，默认返回第一个
     *
     * @param by       查询元素
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从1开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @return ChromiumElement对象或文本、属性或其组成的列表
     */
    private static List<ChromiumElement> _findInChromiumEle(ChromiumElement ele, By by, Integer index, Double timeout, Boolean relative) {
        if (by.getName().equals(BySelect.XPATH) && by.getValue().trim().startsWith("/")) {
            by.setValue("." + by.getValue());
        } else if (by.getName().equals(BySelect.CSS_SELECTOR) && by.getValue().trim().startsWith(">")) {
            by.setValue(ele.cssPath() + by.getValue());
        }
        timeout = timeout != null ? timeout : ele.getOwner().timeout();
        // ---------------执行查找-----------------
        return by.getName().equals(BySelect.XPATH) ? findByXpath(ele, by.getValue(), index, timeout, relative) : findByCss(ele, by.getValue(), index, timeout);
    }

    /**
     * 执行用xpath在元素中查找元素
     *
     * @param ele      在此元素中查找
     * @param xpath    查找语句
     * @param index    第几个结果，从1开始，可传入负数获取倒数第几个，为None返回所有
     * @param timeout  超时时间（秒）
     * @param relative 是否相对定位
     * @return ChromiumElement或其组成的列表
     */
    protected static List<ChromiumElement> findByXpath(ChromiumElement ele, String xpath, Integer index, Double timeout, Boolean relative) {
        String typeTxt = index != null && index == 1 ? "9" : "7";
        String nodeTxt = FRAME_ELEMENT.contains(ele.tag) && relative != null && !relative ? "this.contentDocument" : "this";
        String js = ChromiumElement.makeJsForFindEleByXPath(xpath, typeTxt, nodeTxt);
        ele.getOwner().waits().docLoaded();
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        Object result = doFindXpath(ele, xpath, index, js, nodeTxt);
        while (result != null && endTime > System.currentTimeMillis()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result = doFindXpath(ele, xpath, index, js, nodeTxt);
        }
        if (result == null) {
            return new ArrayList<>();
        } else if (result instanceof String) {
            try {
                throw new ElementNotFoundError(result.toString());
            } catch (Exception ignored) {
            }
        } else if (result instanceof List) {
            //noinspection unchecked
            return (List<ChromiumElement>) result;
        } else {
            try {
                throw new ElementNotFoundError(result.toString());
            } catch (Exception ignored) {
            }
        }
        return new ArrayList<>();

    }

    private static Object doFindXpath(ChromiumElement ele, String xpath, Integer index, String js, String nodeTxt) {
        JSONObject res = JSON.parseObject(ele.getOwner().runCdp("Runtime.callFunctionOn", Map.of("functionDeclaration", js, "objectId", ele.objId, "returnByValue", false, "awaitPromise", true, "userGesture", true)).toString());
        if (Objects.equals(res.getJSONObject("result").get("type").toString(), "string")) {
            return res.getJSONObject("result").get("value").toString();
        }
        if (res.get("exceptionDetails") != null) {
            if (res.getJSONObject("result").getString("description").contains("The result is not a node set")) {
                Object js1 = ChromiumElement.makeJsForFindEleByXPath(xpath, "1", nodeTxt);
                res = JSON.parseObject(ele.getOwner().runCdp("Runtime.callFunctionOn", Map.of("functionDeclaration", js1, "objectId", ele.objId, "returnByValue", false, "awaitPromise", true, "userGesture", true)).toString());
                return res.getJSONObject("result").getString("value");
            } else {
                throw new IllegalArgumentException("查询语句错误:\n" + res);
            }
        }
        if (Objects.equals(res.getJSONObject("result").get("subtype").toString(), "null") || List.of("NodeList(0)", "Array(0)").contains(res.getJSONObject("result").getString("description"))) {
            return null;
        }
        if (index != null && index == 1) {
            return ChromiumElement.makeChromiumEles(ele.getOwner(), res.getJSONObject("result").get("objectId"), 1, true);
        } else {
            JSONArray resA = JSON.parseObject(ele.getOwner().runCdp("Runtime.getProperties", Map.of("objectId", res.getJSONObject("result").get("objectId"), "ownProperties", true)).toString()).getJSONArray("result");
            resA.remove(resA.size() - 1);
            if (index == null) {
                List<ChromiumElement> list = new ArrayList<>();
                for (Object i : resA) {
                    JSONObject jsonObject = JSON.parseObject(i.toString());
                    if (Objects.equals(jsonObject.getJSONObject("value").getString("type"), "object")) {
                        List<ChromiumElement> list1 = makeChromiumEles(ele.getOwner(), jsonObject.getJSONObject("value").get("objectId"), 1, true);
                        if (list1 == null) return null;
                        if (!list1.isEmpty()) list.add(list1.get(0));
                    }
                }
                return list;
            } else {
                int elesCount = res.size();
                if (elesCount == 0 || Math.abs(index) > elesCount) return null;

                int index1 = ((index < 0 ? elesCount + index + 1 : index) - 1);
                index1 = index1 < 0 ? elesCount + index1 : index1;
                res = res.getJSONObject(index1 + "");
                return res.getJSONObject("value").getString("type").equals("object") ? makeChromiumEles(ele.getOwner(), res.getJSONObject("value").get("objectId"), null, true) : res.getJSONObject("result").getString("value");
            }
        }
    }

    /**
     * 执行用css selector在元素中查找元素
     *
     * @param ele      在此元素中查找
     * @param selector 查找语句
     * @param index    第几个结果，从1开始，可传入负数获取倒数第几个，为None返回所有
     * @param timeout  超时时间（秒）
     * @return ChromiumElement或其组成的列表
     */
    protected static List<ChromiumElement> findByCss(ChromiumElement ele, String selector, Integer index, Double timeout) {
        selector = selector.replace("\"", "\\\"");
        String findAll = index != null && index == 1 ? "" : "All";
        String nodeTxt = List.of("iframe", "frame", "shadow-root").contains(ele.tag) ? "this.contentDocument" : "this";
        String js = "function(){return " + nodeTxt + ".querySelector" + findAll + "(\"" + selector + "\");}";
        ele.getOwner().waits().docLoaded();

        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        Object result = doFindCss(ele, index, js);

        while (result != null && System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            result = doFindCss(ele, index, js);
        }

        if (result == null) {
            return new ArrayList<>();
        } else if (result instanceof String) {
            try {
                throw new ElementNotFoundError(result.toString());
            } catch (Exception ignored) {
            }
        } else if (result instanceof List) {
            //noinspection unchecked
            return (List<ChromiumElement>) result;
        } else {
            try {
                throw new ElementNotFoundError(result.toString());
            } catch (Exception ignored) {
            }
        }
        return new ArrayList<>();
    }

    private static Object doFindCss(ChromiumElement ele, Integer index, String js) {
        JSONObject res = JSON.parseObject(ele.getOwner().runCdp("Runtime.callFunctionOn", Map.of("functionDeclaration", js, "objectId", ele.objId, "returnByValue", false, "awaitPromise", true, "userGesture", true)).toString());
        if (res.containsKey("exceptionDetails")) {
            throw new IllegalArgumentException("查询语句错误:\n" + res);
        }
        String string = res.getJSONObject("result").getString("subtype");
        if (Objects.equals(string, null) || Objects.equals(string, "null") || List.of("NodeList(0)", "Array(0)").contains(res.getJSONObject("result").getString("description"))) {
            return null;
        }
        if (index == 1) {
            return makeChromiumEles(ele.getOwner(), res.getJSONObject("result").get("objectId"), 1, true);
        } else {
            JSONArray jsonArray = JSON.parseObject(ele.getOwner().runCdp("Runtime.getProperties", Map.of("objectId", res.getJSONObject("result").get("objectId"), "ownProperties", true)).toString()).getJSONArray("result");
            List<Object> objects = new ArrayList<>();
            for (int i = 0; i < jsonArray.size() - 1; i++)
                objects.add(JSON.parseObject(jsonArray.get(i).toString()).getJSONObject("value").get("objectId"));
            return makeChromiumEles(ele.getOwner(), objects, index, true);
        }
    }

    /**
     * 根据node id或object id生成相应元素对象
     *
     * @param page ChromiumPage对象
     * @param ids  元素的id列表
     * @return 浏览器元素对象或它们组成的列表，生成失败返回False
     */

    public static List<ChromiumElement> makeChromiumEles(ChromiumBase page, Object ids) {
        return makeChromiumEles(page, ids, 1);
    }

    //-----------------d模式独有属性-------------------

    /**
     * 根据node id或object id生成相应元素对象
     *
     * @param page  ChromiumPage对象
     * @param ids   元素的id列表
     * @param index 获取第几个，为None返回全部
     * @return 浏览器元素对象或它们组成的列表，生成失败返回False
     */

    public static List<ChromiumElement> makeChromiumEles(ChromiumBase page, Object ids, Integer index) {
        return makeChromiumEles(page, ids, index, false);
    }

    /**
     * 根据node id或object id生成相应元素对象
     *
     * @param page    ChromiumPage对象
     * @param ids     元素的id列表
     * @param index   获取第几个，为None返回全部
     * @param isObjId 传入的id是obj id还是node id
     * @return 浏览器元素对象或它们组成的列表，生成失败返回False
     */
    public static List<ChromiumElement> makeChromiumEles(ChromiumBase page, Object ids, Integer index, boolean isObjId) {

        List<Integer> list = new ArrayList<>();
        try {
            list.add(JSON.parseObject(ids.toString(), Integer.class));
        } catch (Exception e) {
            list.addAll(JSON.parseArray(ids.toString(), Integer.class));
        }
        Object obj;
        List<ChromiumElement> chromiumElements = new ArrayList<>();
        if (index != null) {
            index -= 1;
            if (index < 0) index = list.size() + index;
            Integer i = list.get(index);
            if (isObjId) {
                obj = getNodeByObjId(page, i);
            } else {
                obj = getNodeByNodeId(page, i);
            }
            if (Boolean.FALSE.equals(obj)) {
                return null;
            }
            if (obj instanceof ChromiumElement || obj instanceof ChromiumFrame) {
                chromiumElements.add((ChromiumElement) (BaseParser) obj);
            }
            return chromiumElements;
        } else {
            for (Integer i : list) {
                if (isObjId) {
                    obj = getNodeByObjId(page, i);
                } else {
                    obj = getNodeByNodeId(page, i);
                }
                if (Boolean.FALSE.equals(obj)) {
                    return null;
                }
                if (obj instanceof ChromiumElement || obj instanceof ChromiumFrame) {
                    chromiumElements.add((ChromiumElement) (BaseParser) obj);
                }
            }
        }
        return chromiumElements;
    }

    private static Map<String, Object> getNodeInfo(ChromiumBase page, String idType, Integer id) {
        if (id == null) return null;
        JSONObject jsonObject = JSON.parseObject(page.driver().run("DOM.describeNode", Map.of(idType, id)).toString());
        return jsonObject.containsKey("error") || jsonObject.containsValue("error") ? null : jsonObject;

    }

    /**
     * @return 返回类型为字符串或ele对象
     */
    private static Object getNodeByObjId(ChromiumBase page, Integer objId) {
        Map<String, Object> node = getNodeInfo(page, "objectId", objId);
        if (node == null) {
            return false;
        }
        JSONObject jsonObject = JSON.parseObject(node.get("node").toString());
        String o = jsonObject.getString("nodeName");
        if (Objects.equals(o, "#text") || Objects.equals(o, "#comment")) return null;
        return makeEle(page, objId, node);

    }

    private static Object getNodeByNodeId(ChromiumBase page, Integer objId) {
        Map<String, Object> node = getNodeInfo(page, "nodeId", objId);
        if (node == null) return false;
        JSONObject jsonObject = JSON.parseObject(node.get("node").toString());
        String o = jsonObject.getString("nodeName");
        if (Objects.equals(o, "#text") || Objects.equals(o, "#comment")) return jsonObject.get("nodeValue");
        else {
            JSONObject objIdMap = JSON.parseObject(page.driver().run("DOM.resolveNode", Map.of("nodeId", objId)).toString());
            if (objIdMap.containsKey("error")) return false;
            return makeEle(page, objIdMap.getJSONObject("object").get("objectId"), node);
        }
    }

    private static Object makeEle(ChromiumBase page, Object objId, Object node) {
        JSONObject jsonObject = JSON.parseObject(node.toString()).getJSONObject("node");
        ChromiumElement ele = new ChromiumElement(page, jsonObject.getInteger("nodeId"), objId.toString(), jsonObject.getInteger("backendNodeId"));
        if (FRAME_ELEMENT.contains(ele.tag())) {
            return new ChromiumFrame(page, ele, JSON.parseObject(node.toString()));
        } else {
            return ele;
        }
    }

    public static String makeJsForFindEleByXPath(String xpath, String typeTxt, String nodeTxt) {
        String forTxt = "";
        String returnTxt;
        switch (typeTxt) {
            case "9":
                returnTxt = "\n" + "if(e.singleNodeValue==null){return null;}\n" + "else if(e.singleNodeValue.constructor.name==\"Text\"){return e.singleNodeValue.data;}\n" + "else if(e.singleNodeValue.constructor.name==\"Attr\"){return e.singleNodeValue.nodeValue;}\n" + "else if(e.singleNodeValue.constructor.name==\"Comment\"){return e.singleNodeValue.nodeValue;}\n" + "else{return e.singleNodeValue;}";
                break;
            case "7":
                forTxt = "\n" + "var a=new Array();\n" + "for(var i = 0; i <e.snapshotLength ; i++){\n" + "if(e.snapshotItem(i).constructor.name==\"Text\"){a.push(e.snapshotItem(i).data);}\n" + "else if(e.snapshotItem(i).constructor.name==\"Attr\"){a.push(e.snapshotItem(i).nodeValue);}\n" + "else if(e.snapshotItem(i).constructor.name==\"Comment\"){a.push(e.snapshotItem(i).nodeValue);}\n" + "else{a.push(e.snapshotItem(i));}}";
                returnTxt = "return a;";
                break;
            case "2":
                returnTxt = "return e.stringValue;";
                break;
            case "1":
                returnTxt = "return e.numberValue;";
                break;
            default:
                returnTxt = "return e.singleNodeValue;";
        }
        xpath = xpath.replace("'", "\\'");
        return "function(){var e=document.evaluate('" + xpath + "'," + nodeTxt + ",null," + typeTxt + ",null);\n" + forTxt + "\n" + returnTxt + "}";
    }

    /**
     * 运行javascript代码
     *
     * @param pageOrEle 页面对象或元素对象
     * @param script    js文本
     * @param asExpr    是否作为表达式运行，为True时args无效
     * @param timeout   超时时间（秒）
     * @param params    参数
     * @return js执行结果
     */
    public static Object runJs(Object pageOrEle, String script, Boolean asExpr, Double timeout, List<Object> params) {
        if (!(pageOrEle instanceof ChromiumBase || pageOrEle instanceof ChromiumElement || pageOrEle instanceof ShadowRoot)) {
            throw new IllegalArgumentException("类型只能ChromiumPage ChromiumElement ShadowRoot");
        }
        ChromiumBase page;
        boolean isPage;
        String objId = null;

        if (pageOrEle instanceof ChromiumElement || pageOrEle instanceof ShadowRoot) {
            isPage = false;
            if (pageOrEle instanceof ChromiumElement) {
                page = ((ChromiumElement) pageOrEle).getOwner();
                objId = ((ChromiumElement) pageOrEle).getObjId();
            } else {
                page = ((ShadowRoot) pageOrEle).getOwner();
                objId = ((ShadowRoot) pageOrEle).getObjId();

            }
        } else {
            isPage = true;
            page = (ChromiumBase) pageOrEle;
            long endTime = System.currentTimeMillis() + 5000;
            while (System.currentTimeMillis() < endTime && objId == null) {
                objId = page.getRootId();
            }
            if (objId == null) {
                throw new RuntimeException("js运行环境出错。");
            }
        }
        try {
            File file = Paths.get(script).toFile();
            if (file.exists())
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) stringBuilder.append(line).append("\n");
                    if (stringBuilder.length() > 0) script = stringBuilder.toString();
                }
        } catch (IOException ignored) {

        }
        if (page.states().hasAlert()) throw new AlertExistsError();
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);

        Object res;
        try {
            if (asExpr != null && asExpr) {
                res = page.runCdp("Runtime.evaluate", Map.of("expression", script, "returnByValue", false, "awaitPromise", true, "userGesture", true, "_timeout", timeout, "_ignore", new AlertExistsError()));
            } else {
                params = params == null ? List.of() : params;
                if (!Web.isJsFunc(script)) script = "function(){" + script + "}";
                List<Object> objects = new ArrayList<>();
                params.forEach((p) -> objects.add(convertArgument(p)));
                res = page.runCdp("Runtime.callFunctionOn", Map.of("functionDeclaration", script, "objectId", objId, "arguments", objects, "returnByValue", false, "awaitPromise", true, "userGesture", true, "_timeout", timeout, "_ignore", new AlertExistsError()));
            }
        } catch (ContextLostError error) {
            if (isPage) {
                throw new ContextLostError("页面已被刷新，请尝试等待页面加载完成再执行操作。");
            } else {
                throw new ElementLostError("原来获取到的元素对象已不在页面内。");
            }
        }
        if (res == null && page.states().hasAlert()) {
            return null;
        }
        Object o = JSONObject.parseObject(Objects.requireNonNull(res).toString()).get("exceptionDetails");
        if (o != null) return new JavaScriptError("\njavascript运行错误：\n" + script + "\n错误信息：\n" + o);
        try {
            return parseJsResult(page, pageOrEle, JSONObject.parseObject(Objects.requireNonNull(res).toString()).getJSONObject("result"), endTime);
        } catch (Exception e) {
//            e.printStackTrace();
            return res;
        }

    }

    /**
     * @param endTime 毫秒
     * @return 解析js返回的结果
     */
    public static Object parseJsResult(ChromiumBase page, Object ele, JSONObject result, long endTime) {
        Object value = result.get("unserializableValue");
        if (value != null) return value;
        String theType = result.getString("type");
        if (theType.equals("object")) {
            String subtype = result.getString("subtype");
            if (subtype == null || subtype.equals("null")) {
                return null;
            } else if (subtype.equals("node")) {
                String className = result.getString("className");
                if (className.equals("ShadowRoot") && ele instanceof ChromiumElement) {
                    return new ShadowRoot((ChromiumElement) ele, result.getString("objectId"), null);
                } else if (className.equals("HTMLDocument")) {
                    return result;
                } else {
                    List<ChromiumElement> list = makeChromiumEles(page, result.get("objectId"));
                    if (list == null || list.isEmpty()) {
                        throw new ElementLostError();
                    }
                    return list;
                }
            } else if (subtype.equals("array")) {
                JSONArray objects = JSON.parseObject(page.runCdp("Runtime.getProperties", Map.of("objectId", result.get("objectId"), "ownProperties", true)).toString()).getJSONArray("result");
                List<Object> list = new ArrayList<>();
                for (int i = 0, objectsSize = objects.size(); i < objectsSize - 1; i++)
                    list.add(parseJsResult(page, ele, JSONObject.parseObject(objects.get(i).toString()).getJSONObject("value"), endTime));
                return list;
            } else if (result.getBoolean("objectId") && result.getString("className").equalsIgnoreCase("object")) {
                JSONArray objects = JSON.parseObject(page.runCdp("Runtime.getProperties", Map.of("objectId", result.get("objectId"), "ownProperties", true)).toString()).getJSONArray("result");
                Map<String, Object> map = new HashMap<>();
                for (Object object : objects) {
                    JSONObject jsonObject = JSON.parseObject(object.toString());
                    map.put(jsonObject.getString("name"), parseJsResult(page, ele, jsonObject.getJSONObject("value"), endTime));
                }
                return map;

            } else if (result.getBoolean("objectId")) {
                long timeout = endTime - System.currentTimeMillis();
                if (timeout < 0) {
                    return null;
                }
                String js = "function(){return JSON.stringify(this);}";
                JSONObject jsonObject = JSON.parseObject(page.runCdp("Runtime.callFunctionOn", Map.of("functionDeclaration", js, "objectId", result.get("objectId"), "returnByValue", false, "awaitPromise", true, "userGesture", true, "_ignore", new AlertExistsError(), "_timeout", timeout)).toString());
                return parseJsResult(page, ele, jsonObject.getJSONObject("result"), endTime);

            } else {
                Object o = result.get("value");
                return o == null ? result : o;
            }


        } else if (theType.equals("undefined")) {
            return null;
        } else {
            return result.get("value");
        }
    }

    /**
     * @param arg 转换对象
     * @return 把参数转换成js能够接收的形式
     */
    public static Map<String, Object> convertArgument(Object arg) {
        if (arg instanceof ChromiumElement) {
            return Map.of("objectId", ((ChromiumElement) arg).getObjId());
        } else if (arg instanceof Integer || arg instanceof Float || arg instanceof String || arg instanceof Boolean || arg instanceof Long) {
            return Map.of("value", arg);
        }
        if (Double.isInfinite((Double) arg)) {
            if ((Double) arg == Double.POSITIVE_INFINITY) {
                return Map.of("unserializableValue", "Infinity");
            } else if ((Double) arg == Double.NEGATIVE_INFINITY) {
                return Map.of("unserializableValue", "-Infinity");
            }
        }
        throw new TypeNotPresentException("不支持参数" + arg + "的类型:" + arg.getClass().getName(), new RuntimeException());

    }
}
