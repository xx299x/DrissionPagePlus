package com.ll.DataRecorder;

import java.nio.file.Path;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public abstract class BaseRecorder extends OriginalRecorder {
    protected String encoding;
    protected Object before;
    protected Object after;
    protected String table;

    public BaseRecorder() {
    }

    public BaseRecorder(Integer cacheSize) {
        super(cacheSize);
    }

    public BaseRecorder(Path path) {
        super(path);
    }

    public BaseRecorder(Path path, Integer cacheSize) {
        super(path, cacheSize);
    }

    public BaseRecorder(String path) {
        super(path);
    }

    public BaseRecorder(String path, Integer cacheSize) {
        super(path, cacheSize);
    }

    /**
     * @return 返回用于设置属性的对象
     */
    @Override
    public BaseSetter<?> set() {
        if (super.setter == null) super.setter = new BaseSetter<>(this);
        return (BaseSetter<?>) super.setter;
    }

    /**
     * @return 返回当前before内容
     */
    public Object before() {
        return this.before;
    }


    /**
     * @return 返回当前before内容
     */
    public Object after() {
        return this.after;
    }

    /**
     * @return 返回默认表名
     */
    public String table() {
        return this.table;
    }

    /**
     * @return 返回编码格式
     */
    public String encoding() {
        return this.encoding;
    }

}
