package com.ll.DataRecorder;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class BaseSetter<R extends BaseRecorder> extends OriginalSetter<R> {
    public BaseSetter(R recorder) {
        super(recorder);
    }

    /**
     * 设置默认表名
     *
     * @param name 表名
     */
    public void table(String name) {
        recorder.table = name;
    }

    /**
     * 设置在数据前面补充的列
     *
     * @param before 列表、数组或字符串，为字符串时则补充一列
     */
    public void before(List<String> before) {
        before(before, false);
    }

    /**
     * 设置在数据前面补充的列
     *
     * @param before 列表、数组或字符串，为字符串时则补充一列
     */
    public void before(Map<?, ?> before) {
        before(before, false);
    }

    /**
     * 设置在数据前面补充的列
     *
     * @param before 列表、数组或字符串，为字符串时则补充一列
     */
    public void before(String before) {
        before(before, true);
    }

    /**
     * 设置在数据前面补充的列
     *
     * @param before 列表、数组或字符串，为字符串时则补充一列
     */
    public void before(String[] before) {
        before(before, false);
    }

    /**
     * 设置在数据前面补充的列
     *
     * @param before 列表、数组或字符串，为字符串时则补充一列
     */
    private void before(Object before, boolean ignoredI) {
        if (before == null || "".equals(before)) {
            this.recorder.before = null;
        } else if (before instanceof List) {
            this.recorder.before = before;
        } else if (before instanceof Map) {
            this.recorder.before = before;

        } else if (before instanceof String[]) {
            this.recorder.before = before;
        } else {
            this.recorder.before = Collections.singletonList(before);
        }
    }

    /**
     * 设置在数据后面补充的列
     *
     * @param after 列表、数组或字符串，为字符串时则补充一列
     */
    public void after(List<String> after) {
        after(after, false);
    }

    /**
     * 设置在数据后面补充的列
     *
     * @param after 列表、数组或字符串，为字符串时则补充一列
     */
    public void after(Map<?, ?> after) {
        after(after, false);
    }

    /**
     * 设置在数据后面补充的列
     *
     * @param after 列表、数组或字符串，为字符串时则补充一列
     */
    public void after(String after) {
        after(after, true);
    }

    /**
     * 设置在数据后面补充的列
     *
     * @param after 列表、数组或字符串，为字符串时则补充一列
     */
    public void after(String[] after) {
        after(after, false);
    }

    /**
     * 设置在数据后面补充的列
     *
     * @param after 列表、数组或字符串，为字符串时则补充一列
     */
    private void after(Object after, boolean ignoredI) {
        if (after == null || "".equals(after)) {
            this.recorder.after = null;
        } else if (after instanceof List) {
            this.recorder.after = after;
        } else if (after instanceof Map) {
            this.recorder.after = after;

        } else if (after instanceof String[]) {
            this.recorder.after = after;
        } else {
            this.recorder.after = Collections.singletonList(after);
        }
    }

    /**
     * 设置编码
     *
     * @param encoding 编码格式
     */
    public void encoding(String encoding) {
        this.recorder.encoding = Charset.forName(encoding).name();
    }

    /**
     * 设置编码
     *
     * @param encoding 编码格式
     */
    public void encoding(Charset encoding) {
        this.recorder.encoding = encoding.name();
    }
}
