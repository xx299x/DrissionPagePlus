package com.ll.DrissonPage.base;

import com.ll.DrissonPage.element.SessionElement;
import com.ll.DrissonPage.error.extend.ElementNotFoundError;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * 所有页面、元素类的基类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */

@Getter
public abstract class BaseParser<T extends BaseParser<?>> {
    @Setter
    private String type;


    /**
     * 返回当前元素下级符合条件的一个元素、属性或节点文本
     *
     * @param by 查询元素
     */
    public T ele(By by) {
        return ele(by, 1);
    }

    /**
     * 返回当前元素下级符合条件的一个元素、属性或节点文本
     *
     * @param by    查询元素
     * @param index 获取第几个元素，下标从1开始可传入负数获取倒数第几个
     */
    public T ele(By by, int index) {
        return ele(by, index, null);
    }

    /**
     * 返回当前元素下级符合条件的一个元素、属性或节点文本
     *
     * @param by      查询元素
     * @param timeout 查找元素超时时间（秒），默认与元素所在页面等待时间一致
     */
    public T ele(By by, Double timeout) {
        return ele(by, 1, timeout);
    }

    /**
     * 返回当前元素下级符合条件的一个元素、属性或节点文本
     *
     * @param by      查询元素
     * @param index   获取第几个元素，下标从1开始可传入负数获取倒数第几个
     * @param timeout 查找元素超时时间（秒），默认与元素所在页面等待时间一致
     */
    public T ele(By by, int index, Double timeout) {
        try {
            return this._ele(by, timeout, index, null, null, "ele()").get(0);
        } catch (IndexOutOfBoundsException e) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("by", by);
            map.put("index", index);
            new ElementNotFoundError("ele()", map).printStackTrace();
            return null;
        }
    }

    /**
     * 获取单个元素
     *
     * @param loc 参数，字符串
     */
    public T ele(String loc) {
        return ele(loc, 1);
    }

    /**
     * 获取单个元素
     *
     * @param loc   参数，字符串
     * @param index 获取第几个元素，下标从0开始可传入负数获取倒数第几个
     */
    public T ele(String loc, int index) {
        return ele(loc, index, null);
    }


    /**
     * 返回当前元素下级符合条件的一个元素、属性或节点文本
     *
     * @param loc      参数，字符串
     * @param timeout 查找元素超时时间（秒），默认与元素所在页面等待时间一致
     */
    public T ele(String loc, Double timeout) {
        return ele(loc, 1, timeout);
    }

    /**
     * 获取单个元素
     *
     * @param loc     参数，字符串
     * @param index   获取第几个元素，下标从0开始可传入负数获取倒数第几个
     * @param timeout 查找元素超时时间（秒），默认与元素所在页面等待时间一致
     */
    public T ele(String loc, int index, Double timeout) {
        try {
            List<T> ts = this._ele(loc, timeout, index, null, null, "ele()");
            if (ts == null) return null;
            return ts.get(0);
        } catch (IndexOutOfBoundsException e) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("loc", loc);
            map.put("index", index);
            new ElementNotFoundError("ele()", map).printStackTrace();
            return null;
        }
    }


    public List<T> eles(By by) {
        return eles(by, null);
    }

    public List<T> eles(By by, Double timeout) {
        return this._ele(by, timeout, null, null, null, null);
    }

    public List<T> eles(String loc) {
        return eles(loc, null);
    }

    public List<T> eles(String loc, Double timeout) {
        return this._ele(loc, timeout, null, null, null, null);
    }

    /**
     * 获取当前页面数据
     */
    public abstract String html();

    /**
     * @param by 查询元素
     * @return 元素对象
     */
    public SessionElement sEle(By by) {
        return sEle(by, 1);
    }

    /**
     * @param by    查询元素
     * @param index 获取第几个，从1开始，可传入负数获取倒数第几个
     * @return 元素对象
     */
    public abstract SessionElement sEle(By by, Integer index);

    /**
     * @param loc 定位符
     * @return 元素对象
     */
    public SessionElement sEle(String loc) {
        return sEle(loc, 1);
    }

    /**
     * @param loc   定位符
     * @param index 获取第几个，从1开始，可传入负数获取倒数第几个
     * @return 元素对象
     */
    public abstract SessionElement sEle(String loc, Integer index);

    /**
     * @param by 查询元素
     * @return 元素对象组成的列表
     */
    public abstract List<SessionElement> sEles(By by);

    /**
     * @param loc 定位符
     * @return 元素对象组成的列表
     */
    public abstract List<SessionElement> sEles(String loc);


    /**
     * @param by       查询元素
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从0开始，可传入负数获取倒数第几个  如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @param method   方法名称
     * @return 元素对象组成的列表
     */
    protected abstract List<T> _ele(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr, String method);

    /**
     * @param loc      定位符
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从0开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @param method   方法名称
     * @return 元素对象组成的列表
     */
    protected abstract List<T> _ele(String loc, Double timeout, Integer index, Boolean relative, Boolean raiseErr, String method);

    /**
     * 执行元素查找
     *
     * @param by       查询元素
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从0开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @return 元素对象组成的列表
     */
    protected abstract List<T> findElements(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr);


    /**
     * 执行元素查找
     *
     * @param loc      定位符
     * @param timeout  查找超时时间（秒）
     * @param index    获取第几个，从0开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置
     * @return 元素对象组成的列表
     */
    protected abstract List<T> findElements(String loc, Double timeout, Integer index, Boolean relative, Boolean raiseErr);

}