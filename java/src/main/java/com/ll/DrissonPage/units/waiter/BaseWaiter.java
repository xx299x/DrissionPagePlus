package com.ll.DrissonPage.units.waiter;

import com.ll.DrissonPage.base.By;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.error.extend.WaitTimeoutError;
import com.ll.DrissonPage.functions.Settings;
import com.ll.DrissonPage.page.ChromiumBase;

import java.util.List;
import java.util.Random;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class BaseWaiter {
    protected final ChromiumBase driver;

    public BaseWaiter(ChromiumBase chromiumBase) {
        this.driver = chromiumBase;
    }

    /**
     * 待若干秒，如传入两个参数，等待时间为这两个数间的一个随机数
     *
     * @param second 秒数
     */
    public void sleep(int second) {
        sleep(second, null);
    }

    /**
     * 待若干秒，如传入两个参数，等待时间为这两个数间的一个随机数
     *
     * @param second  秒数
     * @param second2 第二个秒数
     */
    public void sleep(int second, Integer second2) {
        if (second2 != null) second = new Random().nextInt(second2) + second;
        try {
            if (second > 0) Thread.sleep(second);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 等待元素从DOM中删除
     *
     * @param by 要等待的元素，可以是已有元素、定位符
     * @return 是否等待成功
     */
    public boolean eleDeleted(By by) {
        return eleDeleted(by, null);
    }

    /**
     * 等待元素从DOM中删除
     *
     * @param by      要等待的元素，可以是已有元素、定位符
     * @param timeout 超时时间，默认读取页面超时时间
     * @return 是否等待成功
     */
    public boolean eleDeleted(By by, Double timeout) {
        return eleDeleted(by, timeout, null);
    }

    /**
     * 等待元素从DOM中删除
     *
     * @param by       要等待的元素，可以是已有元素、定位符
     * @param timeout  超时时间，默认读取页面超时时间
     * @param raiseErr 等待失败时是否报错，为None时根据Settings设置
     * @return 是否等待成功
     */
    public boolean eleDeleted(By by, Double timeout, Boolean raiseErr) {
        List<ChromiumElement> list = this.driver._ele(by, timeout, 1, raiseErr, null, null);
        return list == null || list.isEmpty() || eleDeleted(list.get(0), timeout, raiseErr);
    }

    /**
     * 等待元素从DOM中删除
     *
     * @param loc 要等待的元素，可以是已有元素、定位符
     * @return 是否等待成功
     */
    public boolean eleDeleted(String loc) {
        return eleDeleted(loc, null);
    }

    /**
     * 等待元素从DOM中删除
     *
     * @param loc     要等待的元素，可以是已有元素、定位符
     * @param timeout 超时时间，默认读取页面超时时间
     * @return 是否等待成功
     */
    public boolean eleDeleted(String loc, Double timeout) {
        return eleDeleted(loc, timeout, null);
    }

    /**
     * 等待元素从DOM中删除
     *
     * @param loc      要等待的元素，可以是已有元素、定位符
     * @param timeout  超时时间，默认读取页面超时时间
     * @param raiseErr 等待失败时是否报错，为None时根据Settings设置
     * @return 是否等待成功
     */
    public boolean eleDeleted(String loc, Double timeout, Boolean raiseErr) {
        List<ChromiumElement> list = this.driver._ele(loc, timeout, 1, raiseErr, null, null);
        return list == null || list.isEmpty() || eleDeleted(list.get(0), timeout, raiseErr);
    }

    /**
     * 等待元素从DOM中删除
     *
     * @param ele 要等待的元素，可以是已有元素、定位符
     * @return 是否等待成功
     */
    public boolean eleDeleted(ChromiumElement ele) {
        return eleDeleted(ele, null);
    }

    /**
     * 等待元素从DOM中删除
     *
     * @param ele     要等待的元素，可以是已有元素、定位符
     * @param timeout 超时时间，默认读取页面超时时间
     * @return 是否等待成功
     */
    public boolean eleDeleted(ChromiumElement ele, Double timeout) {
        return eleDeleted(ele, timeout, null);
    }


    /**
     * 等待元素从DOM中删除
     *
     * @param ele      要等待的元素，可以是已有元素、定位符
     * @param timeout  超时时间，默认读取页面超时时间
     * @param raiseErr 等待失败时是否报错，为None时根据Settings设置
     * @return 是否等待成功
     */
    public boolean eleDeleted(ChromiumElement ele, Double timeout, Boolean raiseErr) {
        return ele == null || ele.waits().deleted(timeout, raiseErr);
    }

    /**
     * 等待元素变成显示状态
     *
     * @param by 要等待的元素，可以是已有元素、定位符
     * @return 是否等待成功
     */
    public boolean eleDisplayed(By by) {
        return eleDisplayed(by, null);
    }

    /**
     * 等待元素变成显示状态
     *
     * @param by      要等待的元素，可以是已有元素、定位符
     * @param timeout 超时时间，默认读取页面超时时间
     * @return 是否等待成功
     */
    public boolean eleDisplayed(By by, Double timeout) {
        return eleDisplayed(by, timeout, null);
    }

    /**
     * 等待元素变成显示状态
     *
     * @param by       要等待的元素，可以是已有元素、定位符
     * @param timeout  超时时间，默认读取页面超时时间
     * @param raiseErr 等待失败时是否报错，为None时根据Settings设置
     * @return 是否等待成功
     */
    public boolean eleDisplayed(By by, Double timeout, Boolean raiseErr) {
        timeout = timeout != null ? timeout : this.driver.timeout();
        long endTime = (long) (System.currentTimeMillis() + timeout);
        List<ChromiumElement> list = this.driver._ele(by, timeout, 1, false, null, null);
        if (list == null || list.isEmpty()) return false;
        timeout = (double) (endTime - System.currentTimeMillis());
        if (timeout <= 0) {
            if (raiseErr.equals(true) || Settings.raiseWhenWaitFailed)
                throw new WaitTimeoutError("待元素显示失败（等待" + timeout + "秒）。");
            else return false;
        }
        return eleDisplayed(list.get(0), timeout, raiseErr);
    }

    /**
     * 等待元素变成显示状态
     *
     * @param loc 要等待的元素，可以是已有元素、定位符
     * @return 是否等待成功
     */
    public boolean eleDisplayed(String loc) {
        return eleDisplayed(loc, null);
    }

    /**
     * 等待元素变成显示状态
     *
     * @param loc     要等待的元素，可以是已有元素、定位符
     * @param timeout 超时时间，默认读取页面超时时间
     * @return 是否等待成功
     */
    public boolean eleDisplayed(String loc, Double timeout) {
        return eleDisplayed(loc, timeout, null);
    }

    /**
     * 等待元素变成显示状态
     *
     * @param ele      要等待的元素，可以是已有元素、定位符
     * @param timeout  超时时间，默认读取页面超时时间
     * @param raiseErr 等待失败时是否报错，为None时根据Settings设置
     * @return 是否等待成功
     */
    public boolean eleDisplayed(String ele, Double timeout, Boolean raiseErr) {
        timeout = timeout != null ? timeout : this.driver.timeout();
        long endTime = (long) (System.currentTimeMillis() + timeout);
        List<ChromiumElement> list = this.driver._ele(ele, timeout, 1, false, null, null);
        if (list == null || list.isEmpty()) return false;
        timeout = (double) (endTime - System.currentTimeMillis());
        if (timeout <= 0) {
            if (raiseErr.equals(true) || Settings.raiseWhenWaitFailed)
                throw new WaitTimeoutError("待元素显示失败（等待" + timeout + "秒）。");
            else return false;
        }
        return eleDisplayed(list.get(0), timeout, raiseErr);
    }

    /**
     * 等待元素变成显示状态
     *
     * @param ele 要等待的元素，可以是已有元素、定位符
     * @return 是否等待成功
     */
    public boolean eleDisplayed(ChromiumElement ele) {
        return eleDisplayed(ele, null);
    }

    /**
     * 等待元素变成显示状态
     *
     * @param ele     要等待的元素，可以是已有元素、定位符
     * @param timeout 超时时间，默认读取页面超时时间
     * @return 是否等待成功
     */
    public boolean eleDisplayed(ChromiumElement ele, Double timeout) {
        return eleDisplayed(ele, timeout, null);
    }

    /**
     * 等待元素变成显示状态
     *
     * @param ele      要等待的元素，可以是已有元素、定位符
     * @param timeout  超时时间，默认读取页面超时时间
     * @param raiseErr 等待失败时是否报错，为None时根据Settings设置
     * @return 是否等待成功
     */
    public boolean eleDisplayed(ChromiumElement ele, Double timeout, Boolean raiseErr) {
        if (ele == null) return false;
        return ele.waits().displayed(timeout != null ? timeout : this.driver.timeout(), raiseErr);
    }


    /**
     * 等待元素变成隐藏状态
     *
     * @param by 要等待的元素，可以是已有元素、定位符
     * @return 是否等待成功
     */
    public boolean eleHidden(By by) {
        return eleHidden(by, null);
    }

    /**
     * 等待元素变成隐藏状态
     *
     * @param by      要等待的元素，可以是已有元素、定位符
     * @param timeout 超时时间，默认读取页面超时时间
     * @return 是否等待成功
     */
    public boolean eleHidden(By by, Double timeout) {
        return eleHidden(by, timeout, null);
    }

    /**
     * 等待元素变成隐藏状态
     *
     * @param by       要等待的元素，可以是已有元素、定位符
     * @param timeout  超时时间，默认读取页面超时时间
     * @param raiseErr 等待失败时是否报错，为None时根据Settings设置
     * @return 是否等待成功
     */
    public boolean eleHidden(By by, Double timeout, Boolean raiseErr) {
        timeout = timeout != null ? timeout : this.driver.timeout();
        long endTime = (long) (System.currentTimeMillis() + timeout);
        List<ChromiumElement> list = this.driver._ele(by, timeout, 1, false, null, null);
        if (list == null || list.isEmpty()) return false;
        timeout = (double) (endTime - System.currentTimeMillis());
        if (timeout <= 0) {
            if (raiseErr.equals(true) || Settings.raiseWhenWaitFailed)
                throw new WaitTimeoutError("待元素隐藏失败（等待" + timeout + "秒）。");
            else return false;
        }
        return eleHidden(list.get(0), timeout, raiseErr);
    }

    /**
     * 等待元素变成隐藏状态
     *
     * @param loc 要等待的元素，可以是已有元素、定位符
     * @return 是否等待成功
     */
    public boolean eleHidden(String loc) {
        return eleHidden(loc, null);
    }

    /**
     * 等待元素变成隐藏状态
     *
     * @param loc     要等待的元素，可以是已有元素、定位符
     * @param timeout 超时时间，默认读取页面超时时间
     * @return 是否等待成功
     */
    public boolean eleHidden(String loc, Double timeout) {
        return eleHidden(loc, timeout, null);
    }

    /**
     * 等待元素变成隐藏状态
     *
     * @param ele      要等待的元素，可以是已有元素、定位符
     * @param timeout  超时时间，默认读取页面超时时间
     * @param raiseErr 等待失败时是否报错，为None时根据Settings设置
     * @return 是否等待成功
     */
    public boolean eleHidden(String ele, Double timeout, Boolean raiseErr) {
        timeout = timeout != null ? timeout : this.driver.timeout();
        long endTime = (long) (System.currentTimeMillis() + timeout);
        List<ChromiumElement> list = this.driver._ele(ele, timeout, 1, false, null, null);
        if (list == null || list.isEmpty()) return false;
        timeout = (double) (endTime - System.currentTimeMillis());
        if (timeout <= 0) {
            if (raiseErr.equals(true) || Settings.raiseWhenWaitFailed)
                throw new WaitTimeoutError("待元素隐藏失败（等待" + timeout + "秒）。");
            else return false;
        }
        return eleHidden(list.get(0), timeout, raiseErr);
    }

    /**
     * 等待元素变成隐藏状态
     *
     * @param ele 要等待的元素，可以是已有元素、定位符
     * @return 是否等待成功
     */
    public boolean eleHidden(ChromiumElement ele) {
        return eleHidden(ele, null);
    }

    /**
     * 等待元素变成隐藏状态
     *
     * @param ele     要等待的元素，可以是已有元素、定位符
     * @param timeout 超时时间，默认读取页面超时时间
     * @return 是否等待成功
     */
    public boolean eleHidden(ChromiumElement ele, Double timeout) {
        return eleHidden(ele, timeout, null);
    }

    /**
     * 等待元素变成隐藏状态
     *
     * @param ele      要等待的元素，可以是已有元素、定位符
     * @param timeout  超时时间，默认读取页面超时时间
     * @param raiseErr 等待失败时是否报错，为None时根据Settings设置
     * @return 是否等待成功
     */
    public boolean eleHidden(ChromiumElement ele, Double timeout, Boolean raiseErr) {
        if (ele == null) return false;
        return ele.waits().hidden(timeout != null ? timeout : this.driver.timeout(), raiseErr);
    }


    /**
     * 等待元素加载到DOM
     *
     * @param by 要等待的元素，可以是已有元素、定位符
     * @return 是否等待成功
     */
    public ChromiumElement eleLoaded(By by) {
        return eleLoaded(by, null);
    }

    /**
     * 等待元素加载到DOM
     *
     * @param by      要等待的元素，可以是已有元素、定位符
     * @param timeout 超时时间，默认读取页面超时时间
     * @return 是否等待成功
     */
    public ChromiumElement eleLoaded(By by, Double timeout) {
        return eleLoaded(by, timeout, null);
    }

    /**
     * 等待元素加载到DOM
     *
     * @param by       要等待的元素，可以是已有元素、定位符
     * @param timeout  超时时间，默认读取页面超时时间
     * @param raiseErr 等待失败时是否报错，为None时根据Settings设置
     * @return 是否等待成功
     */
    public ChromiumElement eleLoaded(By by, Double timeout, Boolean raiseErr) {
        timeout = timeout != null ? timeout : this.driver.timeout();
        long endTime = (long) (System.currentTimeMillis() + timeout);
        List<ChromiumElement> list = this.driver._ele(by, timeout, 1, false, null, null);
        if (list == null || list.isEmpty()) {
            timeout = (double) (endTime - System.currentTimeMillis());
            if (timeout <= 0) {
                if (raiseErr.equals(true) || Settings.raiseWhenWaitFailed)
                    throw new WaitTimeoutError("待元素加载失败（等待" + timeout + "秒）。");
                else return null;
            }
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * 等待元素加载到DOM
     *
     * @param loc 要等待的元素，可以是已有元素、定位符
     * @return 是否等待成功
     */
    public ChromiumElement eleLoaded(String loc) {
        return eleLoaded(loc, null);
    }

    /**
     * 等待元素加载到DOM
     *
     * @param loc     要等待的元素，可以是已有元素、定位符
     * @param timeout 超时时间，默认读取页面超时时间
     * @return 是否等待成功
     */
    public ChromiumElement eleLoaded(String loc, Double timeout) {
        return eleLoaded(loc, timeout, null);
    }

    /**
     * 等待元素加载到DOM
     *
     * @param ele      要等待的元素，可以是已有元素、定位符
     * @param timeout  超时时间，默认读取页面超时时间
     * @param raiseErr 等待失败时是否报错，为None时根据Settings设置
     * @return 是否等待成功
     */
    public ChromiumElement eleLoaded(String ele, Double timeout, Boolean raiseErr) {
        timeout = timeout != null ? timeout : this.driver.timeout();
        long endTime = (long) (System.currentTimeMillis() + timeout);
        List<ChromiumElement> list = this.driver._ele(ele, timeout, 1, false, null, null);
        if (list == null || list.isEmpty()) {
            timeout = (double) (endTime - System.currentTimeMillis());
            if (timeout <= 0) {
                if (raiseErr.equals(true) || Settings.raiseWhenWaitFailed)
                    throw new WaitTimeoutError("待元素加载失败（等待" + timeout + "秒）。");
                else return null;
            }
            return null;
        } else {
            return list.get(0);
        }
    }


    /**
     * 等待页面开始加载
     *
     * @return 是否等待成功
     */
    public boolean loadStart() {
        return loadStart(null);
    }

    /**
     * 等待页面开始加载
     *
     * @param timeout 超时时间，为null时使用页面timeout属性
     * @return 是否等待成功
     */
    public boolean loadStart(Double timeout) {
        return loadStart(timeout, null);
    }


    /**
     * 等待页面开始加载
     *
     * @param timeout  超时时间，为null时使用页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean loadStart(Double timeout, Boolean raiseErr) {
        return false;
    }

    /**
     * 等待页面加载完成
     *
     * @return 是否等待成功
     */
    public boolean docLoaded() {
        return docLoaded(null);
    }

    /**
     * 等待页面加载完成
     *
     * @param timeout 超时时间，为null时使用页面timeout属性
     * @return 是否等待成功
     */
    public boolean docLoaded(Double timeout) {
        return docLoaded(timeout, null);
    }

    /**
     * 等待页面加载完成
     *
     * @param timeout  超时时间，为null时使用页面timeout属性
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean docLoaded(Double timeout, Boolean raiseErr) {
        return this.loading(timeout, false, 0.01, raiseErr);
    }

    /**
     * 等待自动填写上传文件路径
     *
     * @return 是否等待成功
     */
    public boolean uploadPathsInputted() {
        long endTime = (long) (System.currentTimeMillis() + this.driver.timeout() * 1000);
        while (System.currentTimeMillis() < endTime) {
            if (this.driver.getUploadList() == null || this.driver.getUploadList().isEmpty()) {
                return true;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * 等待浏览器下载开始，可将其拦截
     *
     * @return 成功返回任务对象，失败返回false
     */
    public Object downloadBegin() {
        return downloadBegin(null);
    }

    /**
     * 等待浏览器下载开始，可将其拦截
     *
     * @param timeout 超时时间，null使用页面对象超时时间
     * @return 成功返回任务对象，失败返回false
     */
    public Object downloadBegin(Double timeout) {
        return downloadBegin(timeout, false);
    }

    /**
     * 等待浏览器下载开始，可将其拦截
     *
     * @param timeout  超时时间，null使用页面对象超时时间
     * @param cancelIt 是否取消该任务
     * @return 成功返回任务对象，失败返回false
     */
    public Object downloadBegin(Double timeout, boolean cancelIt) {
        this.driver.browser().getDlMgr().setFlag(this.driver.tabId(), !cancelIt);
        timeout = timeout == null ? this.driver.timeout() : timeout;
        Object r = false;
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        while (System.currentTimeMillis() < endTime) {
            Object flag = this.driver.browser().getDlMgr().getFlag(this.driver.tabId());
            if (!(flag instanceof Boolean)) {
                r = flag;
                break;
            }
        }
        this.driver.browser().getDlMgr().setFlag(this.driver.tabId(), null);
        return r;
    }

    /**
     * 等待url变成包含或不包含指定文本
     *
     * @param text 用于识别的文本
     * @return 是否等待成功
     */
    public boolean urlChange(String text) {
        return urlChange(text, false);
    }

    /**
     * 等待url变成包含或不包含指定文本
     *
     * @param text    用于识别的文本
     * @param exclude 是否排除，为True时当url不包含text指定文本时返回True
     * @return 是否等待成功
     */
    public boolean urlChange(String text, boolean exclude) {
        return urlChange(text, exclude, null);
    }

    /**
     * 等待url变成包含或不包含指定文本
     *
     * @param text    用于识别的文本
     * @param exclude 是否排除，为True时当url不包含text指定文本时返回True
     * @param timeout 超时时间
     * @return 是否等待成功
     */
    public boolean urlChange(String text, boolean exclude, Double timeout) {
        return urlChange(text, exclude, timeout, null);
    }

    /**
     * 等待url变成包含或不包含指定文本
     *
     * @param text     用于识别的文本
     * @param exclude  是否排除，为True时当url不包含text指定文本时返回True
     * @param timeout  超时时间
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean urlChange(String text, boolean exclude, Double timeout, Boolean raiseErr) {
        return this.change("url", text, exclude, timeout, raiseErr);
    }

    /**
     * 等待title变成包含或不包含指定文本
     *
     * @param text 用于识别的文本
     * @return 是否等待成功
     */
    public boolean titleChange(String text) {
        return titleChange(text, false);
    }

    /**
     * 等待title变成包含或不包含指定文本
     *
     * @param text    用于识别的文本
     * @param exclude 是否排除，为True时当title不包含text指定文本时返回True
     * @return 是否等待成功
     */
    public boolean titleChange(String text, boolean exclude) {
        return titleChange(text, exclude, null);
    }

    /**
     * 等待title变成包含或不包含指定文本
     *
     * @param text    用于识别的文本
     * @param exclude 是否排除，为True时当title不包含text指定文本时返回True
     * @param timeout 超时时间
     * @return 是否等待成功
     */
    public boolean titleChange(String text, boolean exclude, Double timeout) {
        return titleChange(text, exclude, timeout, null);
    }

    /**
     * 等待title变成包含或不包含指定文本
     *
     * @param text     用于识别的文本
     * @param exclude  是否排除，为True时当title不包含text指定文本时返回True
     * @param timeout  超时时间
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    public boolean titleChange(String text, boolean exclude, Double timeout, Boolean raiseErr) {
        return this.change("title", text, exclude, timeout, raiseErr);

    }

    /**
     * 等待指定属性变成包含或不包含指定文本
     *
     * @param arg      要被匹配的属性
     * @param text     用于识别的文本
     * @param exclude  为True时当属性不包含text指定文本时返回True
     * @param timeout  超时时间
     * @param raiseErr 等待失败时是否报错，为null时根据Settings设置
     * @return 是否等待成功
     */
    protected boolean change(String arg, String text, boolean exclude, Double timeout, Boolean raiseErr) {
        timeout = timeout == null ? this.driver.timeout() : timeout;
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        String val;
        while (System.currentTimeMillis() < endTime) {
            if (arg.equals("url")) {
                val = this.driver.url();
            } else if (arg.equals("title")) {
                val = this.driver.title();
            } else {
                throw new IllegalArgumentException();
            }
            if ((!exclude && val.contains(text)) || (exclude && !val.contains(text))) {
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (raiseErr != null && raiseErr || Settings.raiseWhenWaitFailed) {
            throw new WaitTimeoutError("等待" + arg + "改变失败（等待" + timeout + "秒）。");
        }
        return false;
    }

    protected boolean loading(Double timeout, boolean start, double gap, Boolean raiseErr) {
        timeout = timeout == null || timeout != 0 ? this.driver.timeout() : timeout;
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        while (System.currentTimeMillis() < endTime) {
            if (this.driver.getIsLoading() == start) {
                return true;
            }
            try {
                Thread.sleep((long) (gap * 1000));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (raiseErr != null && raiseErr || Settings.raiseWhenWaitFailed) {
            throw new WaitTimeoutError("等待页面加载失败（等待" + timeout + "秒）。");
        }
        return false;
    }


}
