package com.ll.DrissonPage.units.states;

import com.alibaba.fastjson.JSON;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.error.extend.CDPError;
import com.ll.DrissonPage.error.extend.NoRectError;
import com.ll.DrissonPage.functions.Web;
import com.ll.DrissonPage.units.Coordinate;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class ElementStates {
    private final ChromiumElement ele;

    /**
     * @return 返回元素是否被选择
     */
    public boolean isSelected() {
        return Boolean.parseBoolean(this.ele.runJs("return this.selected;").toString());
    }

    /**
     * @return 返回元素是否被点击
     */
    public boolean isChecked() {
        return Boolean.parseBoolean(this.ele.runJs("return this.checked;").toString());
    }

    /**
     * @return 返回元素是否显示
     */
    public boolean isDisplayed() {
        return !(this.ele.style("visibility").equals("hidden") ||
                 Boolean.parseBoolean(this.ele.runJs("return this.offsetParent === null;").toString()) || this.ele.style("display").equals("none") || Boolean.parseBoolean(this.ele.property("hidden")));
    }

    /**
     * @return 返回元素是否可用
     */
    public boolean isEnabled() {
        return !Boolean.parseBoolean(this.ele.runJs("return this.disabled;").toString());
    }

    /**
     * @return 返回元素是否仍在DOM中
     */
    public boolean isAlive() {
        try {
            return !this.ele.attrs().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return 返回元素是否出现在视口中，以元素click_point为判断
     */
    public boolean isInViewport() {
        Coordinate coordinate = this.ele.rect().clickPoint();
        return coordinate != null && Web.locationInViewport(this.ele.getOwner(), coordinate);
    }

    /**
     * @return 返回元素是否整个都在视口内
     */
    public boolean isWholeInViewport() {
        Coordinate location = this.ele.rect().location();
        Coordinate size = this.ele.rect().size();
        return Web.locationInViewport(this.ele.getOwner(), location) && Web.locationInViewport(this.ele.getOwner(), new Coordinate(location.getX() + size.getX(), location.getY() + size.getX()));
    }

    /**
     * @return 返回元素是否被覆盖，与是否在视口中无关，如被覆盖返回覆盖元素的backend id，否则返回null
     */
    public Integer isCovered() {
        Coordinate coordinate = this.ele.rect().clickPoint();
        try {
            Integer integer = JSON.parseObject(this.ele.getOwner().runCdp("DOM.getNodeForLocation", Map.of("x", coordinate.getX(), "y", coordinate.getY())).toString()).getInteger("backendNodeId");
            if (!Objects.equals(integer, this.ele.getBackendId())) return integer;
            return null;
        } catch (CDPError c) {
            return null;
        }
    }

    /**
     * @return 回元素是否拥有位置和大小，没有返回null，有返回四个角在页面中坐标组成的列表
     */
    public List<Coordinate> hasRect() {
        try {
            return this.ele.rect().corners();
        } catch (NoRectError e) {
            return null;
        }
    }
}
