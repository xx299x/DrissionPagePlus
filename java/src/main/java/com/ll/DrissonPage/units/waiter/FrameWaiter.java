package com.ll.DrissonPage.units.waiter;

import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.error.extend.NoRectError;
import com.ll.DrissonPage.error.extend.WaitTimeoutError;
import com.ll.DrissonPage.functions.Settings;
import com.ll.DrissonPage.page.ChromiumFrame;
import com.ll.DrissonPage.units.Coordinate;
import com.ll.DrissonPage.units.states.ElementStates;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class FrameWaiter extends BaseWaiter {
    //----------------------------多继承ElementWaiter-----------------------------
    private final ChromiumElement ele;


    public FrameWaiter(ChromiumFrame chromiumBase) {
        super(chromiumBase);
        this.ele = chromiumBase.frameEle();
    }

    /**
     * 等待若干秒
     *
     * @param second 秒
     */
    public void sleep(Double second) {
        try {
            Thread.sleep((long) (second * 1000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 等待元素从dom删除
     *
     * @return 是否等待成功
     */
    public boolean deleted() {
        return deleted(null);
    }

    /**
     * 等待元素从dom删除
     *
     * @param timeout 超时时间，为null使用元素所在页面timeout属性
     * @return 是否等待成功
     */
    public boolean deleted(Double timeout) {
        return deleted(timeout, null);
    }

    /**
     * 等待元素从dom删除
     *
     * @param timeout  超时时间，为null使用元素所在页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean deleted(Double timeout, Boolean raiseErr) {
        return this.waitState("isAlive", false, timeout, raiseErr, "等待元素被删除失败。");
    }


    /**
     * 等待元素从dom显示
     *
     * @return 是否等待成功
     */
    public boolean displayed() {
        return displayed(null);
    }

    /**
     * 等待元素从dom显示
     *
     * @param timeout 超时时间，为null使用元素所在页面timeout属性
     * @return 是否等待成功
     */
    public boolean displayed(Double timeout) {
        return displayed(timeout, null);
    }

    /**
     * 等待元素从dom显示
     *
     * @param timeout  超时时间，为null使用元素所在页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean displayed(Double timeout, Boolean raiseErr) {
        return this.waitState("isDisplayed", true, timeout, raiseErr, "等待元素显示失败。");
    }


    /**
     * 等待元素从dom隐藏
     *
     * @return 是否等待成功
     */
    public boolean hidden() {
        return hidden(null);
    }

    /**
     * 等待元素从dom隐藏
     *
     * @param timeout 超时时间，为null使用元素所在页面timeout属性
     * @return 是否等待成功
     */
    public boolean hidden(Double timeout) {
        return hidden(timeout, null);
    }

    /**
     * 等待元素从dom隐藏
     *
     * @param timeout  超时时间，为null使用元素所在页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean hidden(Double timeout, Boolean raiseErr) {
        return this.waitState("isDisplayed", false, timeout, raiseErr, "等待元素隐藏失败。");
    }


    /**
     * 等待当前元素被遮盖
     *
     * @return 是否等待成功
     */
    public boolean covered() {
        return covered(null);
    }

    /**
     * 等待当前元素被遮盖
     *
     * @param timeout 超时时间，为null使用元素所在页面timeout属性
     * @return 是否等待成功
     */
    public boolean covered(Double timeout) {
        return covered(timeout, null);
    }

    /**
     * 等待当前元素被遮盖
     *
     * @param timeout  超时时间，为null使用元素所在页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean covered(Double timeout, Boolean raiseErr) {
        return this.waitState("isCovered", true, timeout, raiseErr, "等待元素被覆盖失败。");
    }


    /**
     * 等待当前元素不被遮盖
     *
     * @return 是否等待成功
     */
    public boolean notCovered() {
        return notCovered(null);
    }

    /**
     * 等待当前元素不被遮盖
     *
     * @param timeout 超时时间，为null使用元素所在页面timeout属性
     * @return 是否等待成功
     */
    public boolean notCovered(Double timeout) {
        return notCovered(timeout, null);
    }

    /**
     * 等待当前元素不被遮盖
     *
     * @param timeout  超时时间，为null使用元素所在页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean notCovered(Double timeout, Boolean raiseErr) {
        return this.waitState("isCovered", false, timeout, raiseErr, "等待元素不被覆盖失败。");
    }


    /**
     * 等待当前元素变成可用
     *
     * @return 是否等待成功
     */
    public boolean enabled() {
        return enabled(null);
    }

    /**
     * 等待当前元素变成可用
     *
     * @param timeout 超时时间，为null使用元素所在页面timeout属性
     * @return 是否等待成功
     */
    public boolean enabled(Double timeout) {
        return enabled(timeout, null);
    }

    /**
     * 等待当前元素变成可用
     *
     * @param timeout  超时时间，为null使用元素所在页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean enabled(Double timeout, Boolean raiseErr) {
        return this.waitState("isEnabled", true, timeout, raiseErr, "等待元素变成可用失败。");
    }

    /**
     * 等待当前元素变成不可用
     *
     * @return 是否等待成功
     */
    public boolean disabled() {
        return disabled(null);
    }

    /**
     * 等待当前元素变成不可用
     *
     * @param timeout 超时时间，为null使用元素所在页面timeout属性
     * @return 是否等待成功
     */
    public boolean disabled(Double timeout) {
        return disabled(timeout, null);
    }

    /**
     * 等待当前元素变成不可用
     *
     * @param timeout  超时时间，为null使用元素所在页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean disabled(Double timeout, Boolean raiseErr) {
        return this.waitState("isEnabled", false, timeout, raiseErr, "等待元素变成不可用失败。");
    }

    /**
     * 等待当前元素变成不可用或从DOM移除
     *
     * @return 是否等待成功
     */
    public boolean disabledOrDeleted() {
        return disabledOrDeleted(null);
    }

    /**
     * 等待当前元素变成不可用或从DOM移除
     *
     * @param timeout 超时时间，为null使用元素所在页面timeout属性
     * @return 是否等待成功
     */
    public boolean disabledOrDeleted(Double timeout) {
        return disabledOrDeleted(timeout, null);
    }

    /**
     * 等待当前元素变成不可用或从DOM移除
     *
     * @param timeout  超时时间，为null使用元素所在页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean disabledOrDeleted(Double timeout, Boolean raiseErr) {
        timeout = timeout == null ? super.driver.timeout() : timeout;
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        while (System.currentTimeMillis() < endTime) {
            if (!this.ele.states().isEnabled() || !this.ele.states().isAlive()) {
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (raiseErr == Boolean.TRUE || Settings.raiseWhenWaitFailed)
            throw new WaitTimeoutError("等待元素隐藏或被删除失败（等待" + timeout + "秒）。");
        else return false;

    }

    /**
     * 等待当前元素停止运动
     *
     * @return 是否等待成功
     */
    public boolean stopMoving() {
        return stopMoving(.1);
    }

    /**
     * 等待当前元素停止运动
     *
     * @param gap 检测间隔时间
     * @return 是否等待成功
     */
    public boolean stopMoving(double gap) {
        return stopMoving(gap, null);
    }

    /**
     * 等待当前元素停止运动
     *
     * @param gap     检测间隔时间
     * @param timeout 超时时间，为null 使用元素所在页面timeout属性
     * @return 是否等待成功
     */
    public boolean stopMoving(double gap, Double timeout) {
        return stopMoving(gap, timeout, null);
    }

    /**
     * 等待当前元素停止运动
     *
     * @param gap      检测间隔时间
     * @param timeout  超时时间，为null 使用元素所在页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean stopMoving(double gap, Double timeout, Boolean raiseErr) {
        timeout = timeout == null ? super.driver.timeout() : timeout;
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        Object size = null;
        Coordinate location = null;
        while (System.currentTimeMillis() < endTime) {
            try {
                size = this.ele.states().hasRect();
                location = this.ele.rect().location();
                break;
            } catch (NoRectError ignored) {
            }
        }
        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep((long) gap * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (Objects.equals(this.ele.rect().size(), size) && Objects.equals(this.ele.rect().location(), location))
                return true;
            size = this.ele.rect().size();
            location = this.ele.rect().location();
        }
        if (raiseErr == Boolean.TRUE || Settings.raiseWhenWaitFailed)
            throw new WaitTimeoutError("等待元素停止运动失败（等待" + timeout + "秒）。");
        else return false;
    }

    /**
     * 等待元素某个元素状态到达指定状态
     *
     * @param attr     状态名称
     * @param mode     true或false
     * @param timeout  超时时间，为None使用元素所在页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @param errText  抛出错误时显示的信息
     * @return 是否等待成功
     */
    private boolean waitState(String attr, boolean mode, Double timeout, Boolean raiseErr, String errText) {
        errText = errText == null ? "等待元素状态改变失败（等待%s秒）。" : errText;
        timeout = timeout == null ? super.driver.timeout() : timeout;
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        while (System.currentTimeMillis() < endTime) {
            ElementStates states = this.ele.states();
            try {
                if (Objects.equals(states.getClass().getMethod(attr).invoke(states), mode)) return true;
                Thread.sleep(50);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                     InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (raiseErr == Boolean.TRUE || Settings.raiseWhenWaitFailed)
            throw new WaitTimeoutError(String.format(errText, timeout));
        else return false;
    }
}
