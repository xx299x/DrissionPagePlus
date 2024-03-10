package com.ll.DrissonPage.units;

import com.ll.DrissonPage.base.By;
import com.ll.DrissonPage.base.Driver;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.error.extend.AlertExistsError;
import com.ll.DrissonPage.functions.Keys;
import com.ll.DrissonPage.functions.Web;
import com.ll.DrissonPage.page.ChromiumBase;

import java.util.*;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Actions {
    private final ChromiumBase page;
    private final Driver dr;
    /**
     * 修饰符，Alt=1, Ctrl=2, Meta/Command=4, Shift=8
     */
    private int modifier;
    /**
     * 视图坐标
     */
    private Coordinate curr;

    public Actions(ChromiumBase page) {
        this.page = page;
        this.dr = page.driver();
        this.modifier = 0;
        this.curr = new Coordinate(0, 0);
    }

    /**
     * @return 绝对坐标转换为视口坐标
     */
    protected static Coordinate locationToClient(ChromiumBase page, Coordinate l) {
        String x = page.runJs("return document.documentElement.scrollLeft;").toString();
        String y = page.runJs("return document.documentElement.scrollTop;").toString();
        return new Coordinate(l.getX() - Integer.parseInt(x), l.getY() - Integer.parseInt(y));

    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param loc 元素对象、绝对坐标或文本定位符
     * @return this
     */
    public Actions moveTo(String loc) {
        return moveTo(loc, new Coordinate(0, 0));
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param loc      元素对象、绝对坐标或文本定位符
     * @param duration 拖动用时，传入0即瞬间到达
     * @return this
     */
    public Actions moveTo(String loc, Double duration) {
        return moveTo(loc, new Coordinate(0, 0), duration);
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param loc  元素对象、绝对坐标或文本定位符
     * @param curr 偏移量
     * @return this
     */
    public Actions moveTo(String loc, Coordinate curr) {
        return moveTo(loc, curr, 0.5);
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param loc      元素对象、绝对坐标或文本定位符
     * @param curr     偏移量
     * @param duration 拖动用时，传入0即瞬间到达
     * @return this
     */
    public Actions moveTo(String loc, Coordinate curr, Double duration) {
        if (curr == null) curr = new Coordinate(0, 0);
        ChromiumElement ele = this.page.ele(loc);
        this.page.scroll().toSee(ele);
        Coordinate coordinate = curr.getY() > 0 || curr.getX() > 0 ? ele.rect().location() : ele.rect().midpoint();
        coordinate = new Coordinate(coordinate.getX() + curr.getX(), coordinate.getY() + curr.getY());
        if (!Web.locationInViewport(this.page, coordinate)) {
            //把坐标滚动到页面中间
            int w = Integer.parseInt(this.page.runJs("return document.body.clientWidth;").toString());
            int h = Integer.parseInt(this.page.runJs("return document.body.clientHeight;").toString());
            this.page.scroll().toLocation((int) (coordinate.getX() - w / 2), (int) (coordinate.getY() - h / 2));
        }
        coordinate = curr.getY() > 0 || curr.getX() > 0 ? ele.rect().viewportLocation() : ele.rect().viewportMidpoint();
        coordinate = new Coordinate(coordinate.getX() + curr.getX(), coordinate.getY() + curr.getY());
        coordinate = new Coordinate(coordinate.getX() - this.curr.getX(), coordinate.getY() - this.curr.getY());
        this.move(coordinate, duration);
        return this;
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param by 元素对象、绝对坐标或文本定位符
     * @return this
     */
    public Actions moveTo(By by) {
        return moveTo(by, new Coordinate(0, 0));
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param by       元素对象、绝对坐标或文本定位符
     * @param duration 拖动用时，传入0即瞬间到达
     * @return this
     */
    public Actions moveTo(By by, Double duration) {
        return moveTo(by, new Coordinate(0, 0), duration);
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param by   元素对象、绝对坐标或文本定位符
     * @param curr 偏移量
     * @return this
     */
    public Actions moveTo(By by, Coordinate curr) {
        return moveTo(by, curr, 0.5);
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param by       元素对象、绝对坐标或文本定位符
     * @param curr     偏移量
     * @param duration 拖动用时，传入0即瞬间到达
     * @return this
     */
    public Actions moveTo(By by, Coordinate curr, Double duration) {
        if (curr == null) curr = new Coordinate(0, 0);

        ChromiumElement ele = this.page.ele(by);
        this.page.scroll().toSee(ele);
        Coordinate coordinate = curr.getY() > 0 || curr.getX() > 0 ? ele.rect().location() : ele.rect().midpoint();
        coordinate = new Coordinate(coordinate.getX() + curr.getX(), coordinate.getY() + curr.getY());
        if (!Web.locationInViewport(this.page, coordinate)) {
            //把坐标滚动到页面中间
            int w = Integer.parseInt(this.page.runJs("return document.body.clientWidth;").toString());
            int h = Integer.parseInt(this.page.runJs("return document.body.clientHeight;").toString());
            this.page.scroll().toLocation((int) (coordinate.getX() - w / 2), (int) (coordinate.getY() - h / 2));
        }
        coordinate = curr.getY() > 0 || curr.getX() > 0 ? ele.rect().viewportLocation() : ele.rect().viewportMidpoint();
        coordinate = new Coordinate(coordinate.getX() + curr.getX(), coordinate.getY() + curr.getY());
        coordinate = new Coordinate(coordinate.getX() - this.curr.getX(), coordinate.getY() - this.curr.getY());
        this.move(coordinate, duration);
        return this;
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param ele 元素对象、绝对坐标或文本定位符
     * @return this
     */
    public Actions moveTo(ChromiumElement ele) {
        return moveTo(ele, new Coordinate(0, 0));
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param ele      元素对象、绝对坐标或文本定位符
     * @param duration 拖动用时，传入0即瞬间到达
     * @return this
     */
    public Actions moveTo(ChromiumElement ele, Double duration) {
        return moveTo(ele, new Coordinate(0, 0), duration);
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param ele  元素对象、绝对坐标或文本定位符
     * @param curr 偏移量
     * @return this
     */
    public Actions moveTo(ChromiumElement ele, Coordinate curr) {
        return moveTo(ele, curr, 0.5);
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param ele      元素对象、绝对坐标或文本定位符
     * @param curr     偏移量
     * @param duration 拖动用时，传入0即瞬间到达
     * @return this
     */
    public Actions moveTo(ChromiumElement ele, Coordinate curr, Double duration) {
        if (curr == null) curr = new Coordinate(0, 0);
        this.page.scroll().toSee(ele);
        Coordinate coordinate = curr.getY() > 0 || curr.getX() > 0 ? ele.rect().location() : ele.rect().midpoint();
        coordinate = new Coordinate(coordinate.getX() + curr.getX(), coordinate.getY() + curr.getY());
        if (!Web.locationInViewport(this.page, coordinate)) {
            //把坐标滚动到页面中间
            int w = Integer.parseInt(this.page.runJs("return document.body.clientWidth;").toString());
            int h = Integer.parseInt(this.page.runJs("return document.body.clientHeight;").toString());
            this.page.scroll().toLocation((int) (coordinate.getX() - w / 2), (int) (coordinate.getY() - h / 2));
        }
        coordinate = curr.getY() > 0 || curr.getX() > 0 ? ele.rect().viewportLocation() : ele.rect().viewportMidpoint();
        coordinate = new Coordinate(coordinate.getX() + curr.getX(), coordinate.getY() + curr.getY());
        coordinate = new Coordinate(coordinate.getX() - this.curr.getX(), coordinate.getY() - this.curr.getY());
        this.move(coordinate, duration);
        return this;
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param curr 坐标
     * @return this
     */
    public Actions moveTo(Coordinate curr) {
        return moveTo(curr, new Coordinate(0, 0));
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param curr     坐标
     * @param duration 拖动用时，传入0即瞬间到达
     * @return this
     */
    public Actions moveTo(Coordinate curr, Double duration) {
        return moveTo(curr, new Coordinate(0, 0), duration);
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param curr      坐标
     * @param currDrift 偏移量
     * @return this
     */
    public Actions moveTo(Coordinate curr, Coordinate currDrift) {
        return moveTo(curr, currDrift, 0.5);
    }

    /**
     * 鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量
     * 当带偏移量时，偏移量相对于元素左上角坐标
     *
     * @param curr      坐标
     * @param currDrift 偏移量
     * @param duration  拖动用时，传入0即瞬间到达
     * @return this
     */
    public Actions moveTo(Coordinate curr, Coordinate currDrift, Double duration) {
        if (currDrift == null) currDrift = new Coordinate(0, 0);
        curr = new Coordinate(curr.getX() + currDrift.getX(), curr.getY() + currDrift.getY());
        if (!Web.locationInViewport(this.page, curr)) {
            //把坐标滚动到页面中间
            int w = Integer.parseInt(this.page.runJs("return document.body.clientWidth;").toString());
            int h = Integer.parseInt(this.page.runJs("return document.body.clientHeight;").toString());
            this.page.scroll().toLocation(curr.getX() - w / 2, curr.getY() - h / 2);
        }
        curr = locationToClient(page, curr);
        this.move(curr, duration);
        return this;
    }

    /**
     * 鼠标相对当前位置移动若干位置
     *
     * @return this
     */
    public Actions move() {
        return move(new Coordinate(0, 0));
    }

    /**
     * 鼠标相对当前位置移动若干位置
     *
     * @param curr 偏移量
     * @return this
     */
    public Actions move(Coordinate curr) {
        return move(curr, 0.5);
    }

    /**
     * 鼠标相对当前位置移动若干位置
     *
     * @param curr     偏移量
     * @param duration 拖动用时，传入0即瞬间到达
     * @return this
     */
    public Actions move(Coordinate curr, Double duration) {
        duration = duration == null || duration < 0.02 ? 0.02 : duration;
        int num = (int) (duration * 50);
        List<Coordinate> points = new ArrayList<>();
        for (int i = 1; i < num; i++) {
            points.add(new Coordinate(this.curr.getX() + i * (curr.getX() / num), this.curr.getY() + i * (curr.getY() / num)));
        }
        points.add(new Coordinate(this.curr.getX() + curr.getX(), this.curr.getY() + curr.getY()));
        for (Coordinate point : points) {
            long t = System.currentTimeMillis();
            this.curr = point;
            this.dr.run("Input.dispatchMouseEvent", Map.of("type", "mouseMoved", "x", this.curr.getX(), "y", this.curr.getY(), "modifiers", this.modifier));
            long t1 = 20 - System.currentTimeMillis() + t;
            if (t1 > 0) try {
                Thread.sleep(t1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return this;
    }

    /**
     * 点击鼠标左键，可先移动到元素上
     *
     * @return this
     */
    public Actions click() {
        return this._hold("", ClickAction.LEFT).wait(0.05)._release(ClickAction.LEFT);
    }

    /**
     * 点击鼠标左键，可先移动到元素上
     *
     * @param loc ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions click(String loc) {
        return this._hold(loc, ClickAction.LEFT).wait(0.05)._release(ClickAction.LEFT);
    }

    /**
     * 点击鼠标左键，可先移动到元素上
     *
     * @param by ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions click(By by) {
        return this._hold(by, ClickAction.LEFT).wait(0.05)._release(ClickAction.LEFT);
    }

    /**
     * 点击鼠标左键，可先移动到元素上
     *
     * @param ele ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions click(ChromiumElement ele) {
        return this._hold(ele, ClickAction.LEFT).wait(0.05)._release(ClickAction.LEFT);
    }

    /**
     * 点击鼠标右键，可先移动到元素上
     *
     * @return this
     */
    public Actions r_click() {
        return this._hold("", ClickAction.RIGHT).wait(0.05)._release(ClickAction.RIGHT);
    }

    /**
     * 点击鼠标右键，可先移动到元素上
     *
     * @param loc ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions r_click(String loc) {
        return this._hold(loc, ClickAction.RIGHT).wait(0.05)._release(ClickAction.RIGHT);
    }

    /**
     * 点击鼠标右键，可先移动到元素上
     *
     * @param by ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions r_click(By by) {
        return this._hold(by, ClickAction.RIGHT).wait(0.05)._release(ClickAction.RIGHT);
    }

    /**
     * 点击鼠标右键，可先移动到元素上
     *
     * @param ele ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions r_click(ChromiumElement ele) {
        return this._hold(ele, ClickAction.RIGHT).wait(0.05)._release(ClickAction.RIGHT);
    }

    /**
     * 点击鼠标中键，可先移动到元素上
     *
     * @return this
     */
    public Actions m_click() {
        return this._hold("", ClickAction.MIDDLE).wait(0.05)._release(ClickAction.MIDDLE);
    }

    /**
     * 点击鼠标中键，可先移动到元素上
     *
     * @param loc ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions m_click(String loc) {
        return this._hold(loc, ClickAction.MIDDLE).wait(0.05)._release(ClickAction.MIDDLE);
    }

    /**
     * 点击鼠标中键，可先移动到元素上
     *
     * @param by ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions m_click(By by) {
        return this._hold(by, ClickAction.MIDDLE).wait(0.05)._release(ClickAction.MIDDLE);
    }

    /**
     * 点击鼠标中键，可先移动到元素上
     *
     * @param ele ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions m_click(ChromiumElement ele) {
        return this._hold(ele, ClickAction.MIDDLE).wait(0.05)._release(ClickAction.MIDDLE);
    }

    /**
     * 双击鼠标左键，可先移动到元素上
     *
     * @return this
     */
    public Actions db_click() {
        return this._hold("", ClickAction.LEFT, 2).wait(0.05)._release(ClickAction.LEFT);
    }

    /**
     * 双击鼠标左键，可先移动到元素上
     *
     * @param loc ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions db_click(String loc) {
        return this._hold(loc, ClickAction.LEFT, 2).wait(0.05)._release(ClickAction.LEFT);
    }

    /**
     * 双击鼠标左键，可先移动到元素上
     *
     * @param by ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions db_click(By by) {
        return this._hold(by, ClickAction.LEFT, 2).wait(0.05)._release(ClickAction.LEFT);
    }

    /**
     * 双击鼠标左键，可先移动到元素上
     *
     * @param ele ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions db_click(ChromiumElement ele) {
        return this._hold(ele, ClickAction.LEFT, 2).wait(0.05)._release(ClickAction.LEFT);
    }

    /**
     * 按住鼠标左键，可先移动到元素上
     *
     * @return this
     */
    public Actions hold() {
        return this._hold("", ClickAction.LEFT);
    }

    /**
     * 按住鼠标左键，可先移动到元素上
     *
     * @param loc ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions hold(String loc) {
        return this._hold(loc, ClickAction.LEFT);
    }

    /**
     * 按住鼠标左键，可先移动到元素上
     *
     * @param by ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions hold(By by) {
        return this._hold(by, ClickAction.LEFT);
    }

    /**
     * 按住鼠标左键，可先移动到元素上
     *
     * @param ele ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions hold(ChromiumElement ele) {
        return this._hold(ele, ClickAction.LEFT);
    }

    /**
     * 释放鼠标左键，可先移动到元素上
     *
     * @return this
     */
    public Actions release() {
        return this._hold("", ClickAction.LEFT);
    }

    /**
     * 释放鼠标左键，可先移动到元素上
     *
     * @param loc ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions release(String loc) {
        return this.moveTo(loc, 0.0)._hold(loc, ClickAction.LEFT);
    }

    /**
     * 释放鼠标左键，可先移动到元素上
     *
     * @param by ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions release(By by) {
        return this.moveTo(by, 0.0)._hold(by, ClickAction.LEFT);
    }

    /**
     * 释放鼠标左键，可先移动到元素上
     *
     * @param ele ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions release(ChromiumElement ele) {
        return this.moveTo(ele, 0.0)._hold(ele, ClickAction.LEFT);
    }

    /**
     * 按住鼠标右键，可先移动到元素上
     *
     * @return this
     */
    public Actions rHold() {
        return this._hold("", ClickAction.RIGHT);
    }

    /**
     * 按住鼠标右键，可先移动到元素上
     *
     * @param loc ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions rHold(String loc) {
        return this._hold(loc, ClickAction.RIGHT);
    }

    /**
     * 按住鼠标右键，可先移动到元素上
     *
     * @param by ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions rHold(By by) {
        return this._hold(by, ClickAction.RIGHT);
    }

    /**
     * 按住鼠标右键，可先移动到元素上
     *
     * @param ele ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions rHold(ChromiumElement ele) {
        return this._hold(ele, ClickAction.RIGHT);
    }

    /**
     * 释放鼠标右键，可先移动到元素上
     *
     * @return this
     */
    public Actions rRelease() {
        return this._hold("", ClickAction.RIGHT);
    }

    /**
     * 释放鼠标右键，可先移动到元素上
     *
     * @param loc ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions rRelease(String loc) {
        return this.moveTo(loc, 0.0)._hold(loc, ClickAction.RIGHT);
    }

    /**
     * 释放鼠标右键，可先移动到元素上
     *
     * @param by ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions rRelease(By by) {
        return this.moveTo(by, 0.0)._hold(by, ClickAction.RIGHT);
    }

    /**
     * 释放鼠标右键，可先移动到元素上
     *
     * @param ele ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions rRelease(ChromiumElement ele) {
        return this.moveTo(ele, 0.0)._hold(ele, ClickAction.RIGHT);
    }

    /**
     * 按住鼠标中键，可先移动到元素上
     *
     * @return this
     */
    public Actions mHold() {
        return this._hold("", ClickAction.MIDDLE);
    }

    /**
     * 按住鼠标中键，可先移动到元素上
     *
     * @param loc ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions mHold(String loc) {
        return this._hold(loc, ClickAction.MIDDLE);
    }

    /**
     * 按住鼠标中键，可先移动到元素上
     *
     * @param by ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions mHold(By by) {
        return this._hold(by, ClickAction.MIDDLE);
    }

    /**
     * 按住鼠标中键，可先移动到元素上
     *
     * @param ele ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions mHold(ChromiumElement ele) {
        return this._hold(ele, ClickAction.MIDDLE);
    }

    /**
     * 释放鼠标中键，可先移动到元素上
     *
     * @return this
     */
    public Actions mRelease() {
        return this._hold("", ClickAction.MIDDLE);
    }

    /**
     * 释放鼠标中键，可先移动到元素上
     *
     * @param loc ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions mRelease(String loc) {
        return this.moveTo(loc, 0.0)._hold(loc, ClickAction.MIDDLE);
    }

    /**
     * 释放鼠标中键，可先移动到元素上
     *
     * @param by ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions mRelease(By by) {
        return this.moveTo(by, 0.0)._hold(by, ClickAction.MIDDLE);
    }

    /**
     * 释放鼠标中键，可先移动到元素上
     *
     * @param ele ChromiumElement元素或文本定位符
     * @return this
     */
    public Actions mRelease(ChromiumElement ele) {
        return this.moveTo(ele, 0.0)._hold(ele, ClickAction.MIDDLE);
    }

    /**
     * 按下鼠标按键
     *
     * @param loc    元素或文本定位符
     * @param button 要按下的按键
     * @return this
     */
    private Actions _hold(String loc, ClickAction button) {
        return _hold(loc == null || loc.isEmpty() ? null : loc, button, 1);
    }

    /**
     * 按下鼠标按键
     *
     * @param loc    元素或文本定位符
     * @param button 要按下的按键
     * @param count  点击次数
     * @return this
     */
    private Actions _hold(String loc, ClickAction button, int count) {
        if (loc != null) this.moveTo(loc, 0.0);
        return _hold(button, count);
    }

    /**
     * 按下鼠标按键
     *
     * @param by     元素或文本定位符
     * @param button 要按下的按键
     * @return this
     */
    private Actions _hold(By by, ClickAction button) {
        return _hold(by, button, 1);
    }

    /**
     * 按下鼠标按键
     *
     * @param by     元素或文本定位符
     * @param button 要按下的按键
     * @param count  点击次数
     * @return this
     */
    private Actions _hold(By by, ClickAction button, int count) {
        if (by != null) this.moveTo(by, 0.0);
        return _hold(button, count);
    }

    /**
     * 按下鼠标按键
     *
     * @param ele    元素或文本定位符
     * @param button 要按下的按键
     * @return this
     */
    private Actions _hold(ChromiumElement ele, ClickAction button) {
        return _hold(ele, button, 1);
    }

    /**
     * 按下鼠标按键
     *
     * @param ele    元素或文本定位符
     * @param button 要按下的按键
     * @param count  点击次数
     * @return this
     */
    private Actions _hold(ChromiumElement ele, ClickAction button, int count) {
        if (ele != null) this.moveTo(ele, 0.0);
        return _hold(button, count);
    }

    /**
     * 按下鼠标按键
     *
     * @param button 要按下的按键
     * @param count  点击次数
     * @return this
     */
    private Actions _hold(ClickAction button, int count) {
        this.dr.run("Input.dispatchMouseEvent", Map.of("type", "mousePressed", "button", button.getValue(), "clickCount", count, "x", this.curr.getX(), "y", this.curr.getY(), "modifiers", this.modifier));
        return this;
    }

    /**
     * 释放鼠标按键
     *
     * @param button 要释放的按键
     * @return this
     */
    private Actions _release(ClickAction button) {
        this.dr.run("Input.dispatchMouseEvent", Map.of("type", "mousePressed", "button", button.getValue(), "clickCount", 1, "x", this.curr.getX(), "y", this.curr.getY(), "modifiers", this.modifier));
        return this;
    }

    /**
     * 滚动鼠标滚轮，可先移动到元素上
     *
     * @return this
     */
    public Actions scroll() {
        return scroll(new Coordinate(0, 0));
    }

    /**
     * 滚动鼠标滚轮，可先移动到元素上
     *
     * @param loc ChromiumElement元素 或查询元素
     * @return this
     */
    public Actions scroll(String loc) {
        return scroll(new Coordinate(0, 0), loc);
    }

    /**
     * 滚动鼠标滚轮，可先移动到元素上
     *
     * @param curr 滚动值
     * @return this
     */
    public Actions scroll(Coordinate curr) {
        return scroll(curr, "");
    }

    /**
     * 滚动鼠标滚轮，可先移动到元素上
     *
     * @param curr 滚动值
     * @param loc  ChromiumElement元素 或查询元素
     * @return this
     */
    public Actions scroll(Coordinate curr, String loc) {
        if (loc != null && !loc.isEmpty()) this.moveTo(loc, 0.0);
        this.dr.run("Input.dispatchMouseEvent", Map.of("type", "mouseWheel", "x", curr.getX(), "y", curr.getY(), "modifiers", this.modifier));
        return this;
    }

    /**
     * 滚动鼠标滚轮，可先移动到元素上
     *
     * @param by ChromiumElement元素 或查询元素
     * @return this
     */
    public Actions scroll(By by) {
        return scroll(new Coordinate(0, 0), by);
    }

    /**
     * 滚动鼠标滚轮，可先移动到元素上
     *
     * @param curr 滚动值
     * @param by   ChromiumElement元素 或查询元素
     * @return this
     */
    public Actions scroll(Coordinate curr, By by) {
        if (by != null) this.moveTo(by, 0.0);
        this.dr.run("Input.dispatchMouseEvent", Map.of("type", "mouseWheel", "x", curr.getX(), "y", curr.getY(), "modifiers", this.modifier));
        return this;
    }

    /**
     * 滚动鼠标滚轮，可先移动到元素上
     *
     * @param ele ChromiumElement元素 或查询元素
     * @return this
     */
    public Actions scroll(ChromiumElement ele) {
        return scroll(new Coordinate(0, 0), ele);
    }

    /**
     * 滚动鼠标滚轮，可先移动到元素上
     *
     * @param curr 滚动值
     * @param ele  ChromiumElement元素 或查询元素
     * @return this
     */
    public Actions scroll(Coordinate curr, ChromiumElement ele) {
        if (ele != null) this.moveTo(ele, 0.0);
        this.dr.run("Input.dispatchMouseEvent", Map.of("type", "mouseWheel", "x", curr.getX(), "y", curr.getY(), "modifiers", this.modifier));
        return this;
    }

    /**
     * 鼠标向上移动若干像素
     *
     * @param pixel 鼠标移动的像素值
     * @return this
     */
    public Actions up(int pixel) {
        return this.move(new Coordinate(0, -pixel));
    }

    /**
     * 鼠标向下移动若干像素
     *
     * @param pixel 鼠标移动的像素值
     * @return this
     */
    public Actions down(int pixel) {
        return this.move(new Coordinate(0, pixel));
    }

    /**
     * 鼠标向左移动若干像素
     *
     * @param pixel 鼠标移动的像素值
     * @return this
     */
    public Actions left(int pixel) {
        return this.move(new Coordinate(-pixel, 0));
    }

    /**
     * 鼠标向右移动若干像素
     *
     * @param pixel 鼠标移动的像素值
     * @return this
     */
    public Actions right(int pixel) {
        return this.move(new Coordinate(pixel, 0));
    }

    /**
     * 按下键盘上的按键
     *
     * @param key 使用Keys获取的按键，或"DEL"形式按键名称
     * @return this
     */
    public Actions keyDown(Object key) {
        return keyDown(String.valueOf(key));
    }

    /**
     * 按下键盘上的按键
     *
     * @param key 使用Keys获取的按键，或"DEL"形式按键名称
     * @return this
     */
    public Actions keyDown(String key) {
        key = Keys.K.getOrDefault(key.toUpperCase(Locale.ROOT), key);

        if (List.of("\ue009", "\ue008", "\ue00a", "\ue03d").contains(key)) {
            this.modifier |= Keys.modifierBit.getOrDefault(key, 0);
            return this;
        }
        Map<String, Object> keyDown = this.getKeyData(key, "keyDown");
        keyDown.put("_ignore", new AlertExistsError());
        this.page.runCdp("Input.dispatchKeyEvent", keyDown);

        return this;
    }

    /**
     * 按下键盘上的按键
     *
     * @return this
     */
    public Actions keyUp(Object key) {
        return keyUp(String.valueOf(key));
    }

    /**
     * 按下键盘上的按键
     *
     * @param key 按键，特殊字符见Keys
     * @return this
     */
    public Actions keyUp(String key) {
        key = Keys.K.getOrDefault(key.toUpperCase(Locale.ROOT), key);
        if (List.of("\ue009", "\ue008", "\ue00a", "\ue03d").contains(key)) {
            this.modifier |= Keys.modifierBit.getOrDefault(key, 0);
            return this;
        }
        Map<String, Object> keyDown = this.getKeyData(key, "keyUp");
        keyDown.put("_ignore", new AlertExistsError());
        this.page.runCdp("Input.dispatchKeyEvent", keyDown);

        return this;
    }

    /**
     * 用模拟键盘按键方式输入文本，可输入字符串，也可输入组合键，只能输入键盘上有的字符
     *
     * @param key 要按下的按键，特殊字符和多个文本可用list
     * @return this
     */
    public Actions type(Object key) {
        List<String> modifiers = new ArrayList<>();
        String s = String.valueOf(key);
        for (int i = 0; i < s.length(); i++) {
            String c = String.valueOf(s.charAt(i));
            this.keyDown(c);
            if (List.of('\ue009', '\ue008', '\ue00a', '\ue03d').contains(c)) modifiers.add(c);
            else this.keyUp(c);
        }
        for (String c : modifiers) this.keyUp(c);
        return this;
    }

    /**
     * 输入文本，也可输入组合键，组合键用list形式输入
     *
     * @param text 文本值或按键组合
     * @return this
     */
    public Actions input(Object text) {
        Keys.inputTextOrKeys(this.page, text);
        return this;
    }

    /**
     * 等待若干秒
     *
     * @param second 秒
     * @return this
     */
    public Actions wait(Double second) {
        try {
            Thread.sleep((long) (second * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * 获取用于发送的按键信息
     *
     * @param key    按键
     * @param action 'keyDown' 或 'keyUp'
     * @return 按键信息
     */
    private Map<String, Object> getKeyData(String key, String action) {
        Map<String, Object> map = Keys.keyDescriptionForString(this.modifier, key);
        Object text = map.get("text");
        if (!Objects.equals(action, "keyUp"))
            action = text != null && !text.toString().isEmpty() ? "keyDown" : "rawKeyDown";
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("type", action);
        stringObjectHashMap.put("modifiers", this.modifier);
        stringObjectHashMap.put("windowsVirtualKeyCode", map.get("keyCode"));
        stringObjectHashMap.put("code", map.get("code"));
        stringObjectHashMap.put("key", map.get("key"));
        stringObjectHashMap.put("text", text);
        stringObjectHashMap.put("autoRepeat", false);
        stringObjectHashMap.put("unmodifiedText", text);
        stringObjectHashMap.put("location", map.get("location"));
        stringObjectHashMap.put("isKeypad", Objects.equals(map.get("location"), 3));
        return stringObjectHashMap;
    }


}
