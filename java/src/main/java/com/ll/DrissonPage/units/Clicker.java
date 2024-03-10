package com.ll.DrissonPage.units;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.error.extend.AlertExistsError;
import com.ll.DrissonPage.error.extend.CDPError;
import com.ll.DrissonPage.error.extend.CanNotClickError;
import com.ll.DrissonPage.error.extend.NoRectError;
import com.ll.DrissonPage.functions.Settings;
import com.ll.DrissonPage.functions.Web;
import com.ll.DrissonPage.page.ChromiumTab;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Clicker {
    private final ChromiumElement ele;

    public Clicker(ChromiumElement ele) {
        this.ele = ele;
    }

    /**
     * 点击元素
     *
     * @return 是否点击成功
     */
    public boolean click() {
        return click(1.5);
    }

    /**
     * 点击元素
     * 如果遇到遮挡，可选择是否用js点击
     *
     * @param timeout 模拟点击的超时时间（秒），等待元素可见、可用、进入视口
     * @return 是否点击成功
     */
    public boolean click(Double timeout) {
        return click(timeout, true);
    }

    /**
     * 点击元素
     * 如果遇到遮挡，可选择是否用js点击
     *
     * @param timeout  模拟点击的超时时间（秒），等待元素可见、可用、进入视口
     * @param waitStop 是否等待元素运动结束再执行点击
     * @return 是否点击成功
     */
    public boolean click(Double timeout, boolean waitStop) {
        return left(false, timeout, waitStop);
    }

    /**
     * 点击元素
     * 如果遇到遮挡，可选择是否用js点击
     *
     * @param byJs     是否用js点击，为None时先用模拟点击，遇到遮挡改用js，为True时直接用js点击，为False时只用模拟点击
     * @param timeout  模拟点击的超时时间（秒），等待元素可见、可用、进入视口
     * @param waitStop 是否等待元素运动结束再执行点击
     * @return 是否点击成功
     */
    public boolean click(Boolean byJs, Double timeout, boolean waitStop) {
        return left(byJs, timeout, waitStop);
    }

    /**
     * 点击元素
     *
     * @return 是否点击成功
     */
    public boolean left() {
        return left(1.5);
    }

    /**
     * 点击元素
     * 如果遇到遮挡，可选择是否用js点击
     *
     * @param timeout 模拟点击的超时时间（秒），等待元素可见、可用、进入视口
     * @return 是否点击成功
     */
    public boolean left(Double timeout) {
        return left(timeout, true);
    }

    /**
     * 点击元素
     * 如果遇到遮挡，可选择是否用js点击
     *
     * @param timeout  模拟点击的超时时间（秒），等待元素可见、可用、进入视口
     * @param waitStop 是否等待元素运动结束再执行点击
     * @return 是否点击成功
     */
    public boolean left(Double timeout, boolean waitStop) {
        return left(false, timeout, waitStop);
    }

    /**
     * 点击元素
     * 如果遇到遮挡，可选择是否用js点击
     *
     * @param byJs     是否用js点击，为null时先用模拟点击，遇到遮挡改用js，为True时直接用js点击，为False时只用模拟点击
     * @param timeout  模拟点击的超时时间（秒），等待元素可见、可用、进入视口
     * @param waitStop 是否等待元素运动结束再执行点击
     * @return 是否点击成功 如果是select选择器则不返回值
     */
    public Boolean left(Boolean byJs, Double timeout, boolean waitStop) {
        if (Objects.equals(this.ele.tag(), "option")) {
            if (this.ele.states().isSelected()) {
                this.ele.parent("t:select").select().cancelByOption(this.ele);
            } else {
                this.ele.parent("t:select").select().byOption(this.ele);
            }
            return null;
        }
        if (byJs == null || !byJs) {
            boolean can_click = false;
            timeout = timeout == null ? this.ele.getOwner().timeout() : timeout;
            List<Coordinate> rect = null;
            if (timeout == 0) try {
                this.ele.scroll().toSee();
                if (this.ele.states().isEnabled() && this.ele.states().isDisplayed()) {
                    rect = this.ele.rect().viewportCorners();
                    can_click = true;
                }
            } catch (NoRectError e) {
                if (Boolean.FALSE.equals(byJs)) throw e;
            }
            else {
                rect = this.ele.states().hasRect();
                long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
                while (rect == null && endTime > System.currentTimeMillis()) {
                    rect = this.ele.states().hasRect();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (waitStop && rect != null && !rect.isEmpty()) {
                    this.ele.waits().stopMoving(.1, (double) (endTime - System.currentTimeMillis()));
                }
                if (rect != null && !rect.isEmpty()) {
                    this.ele.scroll().toSee();
                    rect = this.ele.rect().corners();
                    while (endTime > System.currentTimeMillis()) {
                        if (this.ele.states().isEnabled() && this.ele.states().isDisplayed()) {
                            can_click = true;
                            break;
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                } else if (Boolean.FALSE.equals(byJs)) throw new NoRectError();

            }
            if (can_click && !this.ele.states().isInViewport()) {
                byJs = true;
            } else if (can_click && (Boolean.FALSE.equals(byJs) || this.ele.states().isCovered() == null)) {
                Coordinate coordinate = new Coordinate(rect.get(1).getX() - (rect.get(1).getX() - rect.get(0).getX()) / 2, rect.get(0).getX() + 3);
                try {
                    JSONObject r = JSON.parseObject(this.ele.getOwner().runCdp("DOM.getNodeForLocation", Map.of("x", coordinate.getX(), "y", coordinate.getY(), "includeUserAgentShadowDOM", true, "ignorePointerEventsNone", true)).toString());
                    if (!Objects.equals(r.getInteger("backendNodeId"), this.ele.getBackendId())) {
                        coordinate = this.ele.rect().viewportMidpoint();
                    } else {
                        coordinate = this.ele.rect().viewportClickPoint();
                    }
                } catch (CDPError e) {
                    coordinate = this.ele.rect().viewportMidpoint();
                }
                this._click(coordinate, ClickAction.LEFT, 1);
                return true;
            }
        }
        if (Boolean.TRUE.equals(byJs)) {
            this.ele.runJs("this.click();");
            return true;
        }
        if (Settings.raiseWhenClickFailed) {
            throw new CanNotClickError();
        }
        return false;
    }

    /**
     * 右键单击
     */
    public void right() {
        middle(1);
    }

    /**
     * 右键点击
     *
     * @param count 次数
     */
    public void right(int count) {
        this.ele.getOwner().scroll().toSee(this.ele);
        Coordinate coordinate = this.ele.rect().viewportClickPoint();
        this._click(coordinate, ClickAction.RIGHT, count);
    }

    /**
     * 中键单击
     */
    public void middle() {
        middle(1);
    }

    /**
     * 中键点击
     *
     * @param count 次数
     */
    public void middle(int count) {
        this.ele.getOwner().scroll().toSee(this.ele);
        Coordinate coordinate = this.ele.rect().viewportClickPoint();
        this._click(coordinate, ClickAction.LEFT, count);
    }

    /**
     * 带偏移量点击本元素，相对于左上角坐标。不传入x或y值时点击元素中间点
     */
    public void at() {
        at(1);
    }

    /**
     * 带偏移量点击本元素，相对于左上角坐标。不传入x或y值时点击元素中间点
     *
     * @param count 点击次数
     */
    public void at(int count) {
        at(ClickAction.LEFT, count);
    }

    /**
     * 带偏移量点击本元素，相对于左上角坐标。不传入x或y值时点击元素中间点
     *
     * @param button 点击哪个键，可选 left, middle, right, back, forward
     * @param count  点击次数
     */
    public void at(ClickAction button, int count) {
        at(null, null, button, count);
    }

    /**
     * 带偏移量点击本元素，相对于左上角坐标。不传入x或y值时点击元素中间点
     *
     * @param offset_x 相对元素左上角坐标的x轴偏移量
     * @param offset_y 相对元素左上角坐标的y轴偏移量
     */
    public void at(Integer offset_x, Integer offset_y) {
        at(offset_x, offset_y, 1);
    }

    /**
     * 带偏移量点击本元素，相对于左上角坐标。不传入x或y值时点击元素中间点
     *
     * @param offset_x 相对元素左上角坐标的x轴偏移量
     * @param offset_y 相对元素左上角坐标的y轴偏移量
     * @param count    点击次数
     */
    public void at(Integer offset_x, Integer offset_y, int count) {
        at(offset_x, offset_y, ClickAction.LEFT, count);
    }

    /**
     * 带偏移量点击本元素，相对于左上角坐标。不传入x或y值时点击元素中间点
     *
     * @param offset_x 相对元素左上角坐标的x轴偏移量
     * @param offset_y 相对元素左上角坐标的y轴偏移量
     * @param button   点击哪个键，可选 left, middle, right, back, forward
     * @param count    点击次数
     */
    public void at(Integer offset_x, Integer offset_y, ClickAction button, int count) {
        this.ele.getOwner().scroll().toSee(this.ele);
        if (offset_x == null && offset_y == null) {
            Coordinate size = this.ele.rect().size();
            offset_x = size.getX() / 2;
            offset_y = size.getY() / 2;
        }
        this._click(Web.offsetScroll(this.ele, offset_x, offset_y), button, count);
    }

    /**
     * 多次点击
     */
    public void multi() {
        multi(2);
    }

    /**
     * 多次点击
     *
     * @param times 默认双击
     */
    public void multi(int times) {
        this.at(null, null, ClickAction.LEFT, times);
    }

    /**
     * 触发上传文件选择框并自动填入指定路径
     *
     * @param filePaths 文件路径，如果上传框支持多文件，可传入列表或字符串，字符串时多个文件用回车分隔
     */
    public void toUpload(Path filePaths) {
        this.toUpload(filePaths, false);
    }

    /**
     * 触发上传文件选择框并自动填入指定路径
     *
     * @param filePaths 文件路径，如果上传框支持多文件，可传入列表或字符串，字符串时多个文件用回车分隔
     * @param byJs      是否用js方式点击，逻辑与click()一致
     */
    public void toUpload(Path filePaths, boolean byJs) {
        this.ele.getOwner().set().uploadFiles(filePaths);
        this.left(1.5, byJs);
        this.ele.getOwner().waits().uploadPathsInputted();
    }

    /**
     * 触发上传文件选择框并自动填入指定路径
     *
     * @param filePaths 文件路径，如果上传框支持多文件，可传入列表或字符串，字符串时多个文件用回车分隔
     */
    public void toUpload(String filePaths) {
        this.toUpload(filePaths, false);
    }

    /**
     * 触发上传文件选择框并自动填入指定路径
     *
     * @param filePaths 文件路径，如果上传框支持多文件，可传入列表或字符串，字符串时多个文件用回车分隔
     * @param byJs      是否用js方式点击，逻辑与click()一致
     */
    public void toUpload(String filePaths, boolean byJs) {
        this.ele.getOwner().set().uploadFiles(filePaths);
        this.left(1.5, byJs);
        this.ele.getOwner().waits().uploadPathsInputted();
    }

    /**
     * 触发上传文件选择框并自动填入指定路径
     *
     * @param filePaths 文件路径，如果上传框支持多文件，可传入列表或字符串，字符串时多个文件用回车分隔
     */
    public void toUpload(String[] filePaths) {
        this.toUpload(filePaths, false);
    }

    /**
     * 触发上传文件选择框并自动填入指定路径
     *
     * @param filePaths 文件路径，如果上传框支持多文件，可传入列表或字符串，字符串时多个文件用回车分隔
     * @param byJs      是否用js方式点击，逻辑与click()一致
     */
    public void toUpload(String[] filePaths, boolean byJs) {
        this.ele.getOwner().set().uploadFiles(filePaths);
        this.left(1.5, byJs);
        this.ele.getOwner().waits().uploadPathsInputted();
    }

    /**
     * 触发上传文件选择框并自动填入指定路径
     *
     * @param filePaths 文件路径，如果上传框支持多文件，可传入列表或字符串，字符串时多个文件用回车分隔
     */
    public void toUpload(Collection<String> filePaths) {
        this.toUpload(filePaths, false);
    }

    /**
     * 触发上传文件选择框并自动填入指定路径
     *
     * @param filePaths 文件路径，如果上传框支持多文件，可传入列表或字符串，字符串时多个文件用回车分隔
     * @param byJs      是否用js方式点击，逻辑与click()一致
     */
    public void toUpload(Collection<String> filePaths, boolean byJs) {
        this.ele.getOwner().set().uploadFiles(filePaths);
        this.left(1.5, byJs);
        this.ele.getOwner().waits().uploadPathsInputted();
    }


    /**
     * 点击后等待新tab出现并返回其对象
     *
     * @return 新标签页对象，如果没有等到新标签页出现则抛出异常
     */
    public ChromiumTab forNewTab() {
        return forNewTab(false);
    }

    /**
     * 点击后等待新tab出现并返回其对象
     *
     * @param byJs 是否使用js点击，逻辑与click()一致
     * @return 新标签页对象，如果没有等到新标签页出现则抛出异常
     */
    public ChromiumTab forNewTab(boolean byJs) {
        this.left(1.5, byJs);
        String tid = this.ele.getOwner().getPage().waits().newTab();
        if (tid == null) throw new RuntimeException("没有出现新标签页。");
        return this.ele.getOwner().getPage().getTab(tid);
    }

    /**
     * 实施点击
     *
     * @param coordinate 视口中的坐标
     * @param button     'left' 'right' 'middle'  'back' 'forward'
     * @param count      点击次数
     */

    private void _click(Coordinate coordinate, ClickAction button, int count) {
        this.ele.getOwner().runCdp("Input.dispatchMouseEvent", Map.of("type", "mousePressed", "x", coordinate.getX(), "y", coordinate.getY(), "button", button.getValue(), "clickCount", count, "_ignore", new AlertExistsError()));
        this.ele.getOwner().runCdp("Input.dispatchMouseEvent", Map.of("type", "mouseReleased", "x", coordinate.getX(), "y", coordinate.getY(), "button", button.getValue(), "_ignore", new AlertExistsError()));
    }


}
