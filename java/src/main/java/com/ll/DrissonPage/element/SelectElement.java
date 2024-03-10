package com.ll.DrissonPage.element;

import com.ll.DrissonPage.base.By;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用于处理 select 标签
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class SelectElement {
    private final ChromiumElement ele;

    public SelectElement(ChromiumElement ele) {
        if (!Objects.equals(ele.tag(), "select"))
            throw new IllegalArgumentException("select方法只能在<select>元素使用。");
        this.ele = ele;
    }

    /**
     * 选定下拉列表中子元素
     *
     * @param textOrIndex 根据文本、值选或序号择选项，若允许多选，传入集合或数组可多选
     * @param timeout     超时时间，不输入默认实用页面超时时间
     * @return null
     */
    public boolean select(Object textOrIndex, Double timeout) {
        String paraType = textOrIndex instanceof Integer ? "index" : "text";
        timeout = timeout == null ? this.ele.getOwner().timeout() : timeout;
        return this.select(textOrIndex, paraType, false, timeout);
    }

    /**
     * @return 返回是否多选表单
     */
    public boolean isMulti() {
        String multiple = this.ele.attr("multiple");
        return multiple != null && !multiple.isEmpty();
    }

    /**
     * 返回所有选项元素组成的列表
     */
    public List<ChromiumElement> options() {
        return this.ele.eles("xpath://option");
    }

    /**
     * 返回第一个被选中的option元素
     *
     * @return ChromiumElement对象或null
     */
    public ChromiumElement selectOption() {
        Object o = this.ele.runJs("return this.options[this.selectedIndex];");
        if (o instanceof List<?>) {
            try {
                List<ChromiumElement> o1 = (List<ChromiumElement>) o;
                if (!o1.isEmpty()) return o1.get(0);
            } catch (ClassCastException ignored) {
            }
        } else if (o instanceof ChromiumElement) {
            return (ChromiumElement) o;
        }
        return null;

    }

    /**
     * 返回所有被选中的option元素列表
     *
     * @return ChromiumElement对象组成的列表
     */
    public List<ChromiumElement> selectOptions() {
        List<ChromiumElement> options = this.options();
        options.removeIf(option -> !option.states().isSelected());
        return options;
    }

    /**
     * 全选
     *
     * @return 是否成功
     */
    public boolean all() {
        if (!this.isMulti()) throw new IllegalArgumentException("只能在多选菜单执行此操作.");
        return this._byLoc("tag:option", 1.0, false);
    }

    /**
     * 反选
     */
    public void invert() {
        if (!this.isMulti()) throw new IllegalArgumentException("只能对多项选框执行反选.");
        boolean change = false;
        for (ChromiumElement option : this.options()) {
            change = true;
            String mode = option.states().isSelected() ? "false" : "true";
            option.runJs("this.selected=" + mode + ";");
        }
        if (change) this.dispatchChange();
    }

    /**
     * 清除所有已选项
     *
     * @return 是否成功
     */
    public boolean clear() {
        if (!this.isMulti()) throw new IllegalArgumentException("只能对多项选框执行反选.");
        return this._byLoc("tag:option", 1.0, true);
    }

    /**
     * 此方法用于根据text值选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param text text属性值，传入集合或数组 可选择多项
     * @return 是否选择成功
     */
    public boolean byText(Object text) {
        return this.byText(text, null);
    }


    /**
     * 此方法用于根据text值选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param text    text属性值，传入集合或数组 可选择多项
     * @param timeout 超时时间，为null默认使用页面超时时间
     * @return 是否选择成功
     */
    public boolean byText(Object text, Double timeout) {
        return this.select(text, "text", false, timeout);
    }


    /**
     * 此方法用于根据value值选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param text value属性值，传入集合或数组 可选择多项
     * @return 是否选择成功
     */
    public boolean byValue(Object text) {
        return this.byValue(text, null);
    }

    /**
     * 此方法用于根据value值选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param text    value属性值，传入集合或数组 可选择多项
     * @param timeout 超时时间，为null默认使用页面超时时间
     * @return 是否选择成功
     */
    public boolean byValue(Object text, Double timeout) {
        return this.select(text, "value", false, timeout);
    }

    /**
     * 此方法用于根据index值选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index 序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可选择多项
     * @return 是否选择成功
     */
    public boolean byIndex(int index) {
        return this.byIndex(index, null);
    }

    /**
     * 此方法用于根据index值选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index   序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可选择多项
     * @param timeout 超时时间，为null默认使用页面超时时间
     * @return 是否选择成功
     */
    public boolean byIndex(int index, Double timeout) {
        return this.byIndex(Integer.valueOf(index), timeout);
    }

    /**
     * 此方法用于根据index值选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index   序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可选择多项
     * @param timeout 超时时间，为null默认使用页面超时时间
     * @return 是否选择成功
     */
    private boolean byIndex(Integer index, Double timeout) {
        return this.select(index, "index", false, timeout);
    }


    /**
     * 此方法用于根据index值选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index 序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可选择多项
     * @return 是否选择成功
     */
    public boolean byIndex(int[] index) {
        return this.byIndex(index, null);
    }

    /**
     * 此方法用于根据index值选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index   序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可选择多项
     * @param timeout 超时时间，为null默认使用页面超时时间
     * @return 是否选择成功
     */
    public boolean byIndex(int[] index, Double timeout) {
        return byIndex(Arrays.stream(index).boxed().toArray(Integer[]::new), timeout);
    }

    /**
     * 此方法用于根据index值选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index   序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可选择多项
     * @param timeout 超时时间，为null默认使用页面超时时间
     * @return 是否选择成功
     */
    private boolean byIndex(Integer[] index, Double timeout) {
        return this.select(index, "index", false, timeout);
    }

    /**
     * 用定位符选择指定的项
     *
     * @param loc 定位符
     * @return 是否选择成功
     */
    public boolean byLoc(String loc) {
        return byLoc(loc, null);
    }

    /**
     * 用定位符选择指定的项
     *
     * @param loc     定位符
     * @param timeout 超时时间
     * @return 是否选择成功
     */
    public boolean byLoc(String loc, Double timeout) {
        return _byLoc(loc, timeout, false);
    }

    /**
     * 用定位符选择指定的项
     *
     * @param by 定位符
     * @return 是否选择成功
     */
    public boolean byLoc(By by) {
        return byLoc(by, null);
    }

    /**
     * 用定位符选择指定的项
     *
     * @param by      定位符
     * @param timeout 超时时间
     * @return 是否选择成功
     */
    public boolean byLoc(By by, Double timeout) {
        return _byLoc(by, timeout, false);
    }

    /**
     * 选中单个或多个option元素
     *
     * @param option option元素或它们组成的列表
     */
    public void byOption(ChromiumElement option) {
        ArrayList<ChromiumElement> option1 = new ArrayList<>();
        option1.add(option);
        byOption(option1);
    }

    /**
     * 选中单个或多个option元素
     *
     * @param option option元素或它们组成的列表
     */
    public void byOption(List<ChromiumElement> option) {
        this.selectOptions(option, "true");
    }

    /**
     * 此方法用于根据text值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param text 传入集合或数组可取消多项
     * @return 是否取消成功
     */
    public boolean cancelByText(String text) {
        return this.cancelByText(text, null);
    }

    /**
     * 此方法用于根据text值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param text    传入集合或数组可取消多项
     * @param timeout 超时时间，不输入默认实用页面超时时间
     * @return 是否取消成功
     */
    public boolean cancelByText(String text, Double timeout) {
        return this.select(text, "text", true, timeout);
    }

    /**
     * 此方法用于根据text值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param text 传入集合或数组可取消多项
     * @return 是否取消成功
     */
    public boolean cancelByText(String[] text) {
        return this.cancelByText(text, null);
    }

    /**
     * 此方法用于根据text值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param text    传入集合或数组可取消多项
     * @param timeout 超时时间，不输入默认实用页面超时时间
     * @return 是否取消成功
     */
    public boolean cancelByText(String[] text, Double timeout) {
        return this.select(text, "text", true, timeout);
    }

    /**
     * 此方法用于根据text值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param text 传入集合或数组可取消多项
     * @return 是否取消成功
     */
    public boolean cancelByText(List<String> text) {
        return this.cancelByText(text, null);
    }

    /**
     * 此方法用于根据text值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param text    传入集合或数组可取消多项
     * @param timeout 超时时间，不输入默认实用页面超时时间
     * @return 是否取消成功
     */
    public boolean cancelByText(List<String> text, Double timeout) {
        return this.select(text, "text", true, timeout);
    }


    /**
     * 此方法用于根据value值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param value value属性值，传入集合或数组可取消多项
     * @return 是否取消成功
     */
    public boolean cancelByValue(String value) {
        return this.cancelByText(value, null);
    }

    /**
     * 此方法用于根据value值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param value   value属性值，传入集合或数组可取消多项
     * @param timeout 超时时间，不输入默认实用页面超时时间
     * @return 是否取消成功
     */
    public boolean cancelByValue(String value, Double timeout) {
        return this.select(value, "value", true, timeout);
    }

    /**
     * 此方法用于根据value值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param value value属性值，传入集合或数组可取消多项
     * @return 是否取消成功
     */
    public boolean cancelByValue(String[] value) {
        return this.cancelByValue(value, null);
    }

    /**
     * 此方法用于根据value值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param value   value属性值，传入集合或数组可取消多项
     * @param timeout 超时时间，不输入默认实用页面超时时间
     * @return 是否取消成功
     */
    public boolean cancelByValue(String[] value, Double timeout) {
        return this.select(value, "value", true, timeout);
    }

    /**
     * 此方法用于根据value值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param value value属性值，传入集合或数组可取消多项
     * @return 是否取消成功
     */
    public boolean cancelByValue(List<String> value) {
        return this.cancelByValue(value, null);
    }

    /**
     * 此方法用于根据value值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param value   value属性值，传入集合或数组可取消多项
     * @param timeout 超时时间，不输入默认实用页面超时时间
     * @return 是否取消成功
     */
    public boolean cancelByValue(List<String> value, Double timeout) {
        return this.select(value, "value", true, timeout);
    }


    /**
     * 此方法用于根据index值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index 序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可取消多项
     * @return 是否取消成功
     */
    public boolean cancelByIndex(int index) {
        return this.cancelByIndex(index, null);
    }

    /**
     * 此方法用于根据index值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index   序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可取消多项
     * @param timeout 超时时间，不输入默认实用页面超时时间
     * @return 是否取消成功
     */
    public boolean cancelByIndex(int index, Double timeout) {
        return this.select(index, "index", true, timeout);
    }

    /**
     * 此方法用于根据index值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index 序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可取消多项
     * @return 是否取消成功
     */
    public boolean cancelByIndex(int[] index) {
        return this.cancelByIndex(index, null);
    }

    /**
     * 此方法用于根据index值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index   序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可取消多项
     * @param timeout 超时时间，不输入默认实用页面超时时间
     * @return 是否取消成功
     */
    public boolean cancelByIndex(int[] index, Double timeout) {
        return this.select(Arrays.stream(index).boxed().toArray(Integer[]::new), "index", true, timeout);
    }

    /**
     * 此方法用于根据index值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index 序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可取消多项
     * @return 是否取消成功
     */
    public boolean cancelByIndex(List<Integer> index) {
        return this.cancelByIndex(index, null);
    }

    /**
     * 此方法用于根据index值取消选择项。当元素是多选列表时，可以接收list或数组
     *
     * @param index   序号，从1开始，可传入负数获取倒数第几个，传入集合或数组可取消多项
     * @param timeout 超时时间，不输入默认实用页面超时时间
     * @return 是否取消成功
     */
    public boolean cancelByIndex(List<Integer> index, Double timeout) {
        return this.select(index, "index", true, timeout);
    }


    /**
     * 用定位符取消选择指定的项
     *
     * @param loc 定位符
     * @return 是否选择成功
     */
    public boolean cancelByLoc(String loc) {
        return cancelByLoc(loc, null);
    }

    /**
     * 用定位符取消选择指定的项
     *
     * @param loc     定位符
     * @param timeout 超时时间
     * @return 是否选择成功
     */
    public boolean cancelByLoc(String loc, Double timeout) {
        return _byLoc(loc, timeout, true);
    }

    /**
     * 用定位符取消选择指定的项
     *
     * @param by 定位符
     * @return 是否选择成功
     */
    public boolean cancelByLoc(By by) {
        return cancelByLoc(by, null);
    }

    /**
     * 用定位符取消选择指定的项
     *
     * @param by      定位符
     * @param timeout 超时时间
     * @return 是否选择成功
     */
    public boolean cancelByLoc(By by, Double timeout) {
        return _byLoc(by, timeout, true);
    }


    /**
     * 取消选中单个或多个option元 素
     *
     * @param option option元素或它们组成的列表
     */
    public void cancelByOption(ChromiumElement option) {
        ArrayList<ChromiumElement> option1 = new ArrayList<>();
        option1.add(option);
        cancelByOption(option1);
    }

    /**
     * 取消选中单个或多个option元 素
     *
     * @param option option元素或它们组成的列表
     */
    public void cancelByOption(ChromiumElement[] option) {
        this.selectOptions(List.of(option), "false");
    }

    /**
     * 取消选中单个或多个option元 素
     *
     * @param option option元素或它们组成的列表
     */
    public void cancelByOption(List<ChromiumElement> option) {
        this.selectOptions(option, "false");
    }

    /**
     * 用定位符取消选择指定的项
     *
     * @param loc     定位符
     * @param timeout 超时时间
     * @param cancel  是否取消选择
     * @return 是否选择成功
     */
    private boolean _byLoc(String loc, Double timeout, boolean cancel) {
        return __byLoc(cancel, this.ele.eles(loc, timeout));
    }


    /**
     * 用定位符取消选择指定的项
     *
     * @param by      定位符
     * @param timeout 超时时间
     * @param cancel  是否取消选择
     * @return 是否选择成功
     */
    private boolean _byLoc(By by, Double timeout, boolean cancel) {
        return __byLoc(cancel, this.ele.eles(by, timeout));
    }

    private boolean __byLoc(boolean cancel, List<ChromiumElement> elements) {
        if (elements == null || elements.isEmpty()) return false;
        this.selectOptions(this.isMulti() ? elements : Collections.singletonList(elements.get(0)), cancel ? "false" : "true");
        return true;
    }

    /**
     * 选定或取消选定下拉列表中子元素
     *
     * @param condition 根据文本、值选或序号择选项，若允许多选，传入集合或数组可多选
     * @param paraType  参数类型，可选 'text'、'value'、'index'
     * @param cancel    是否取消选择
     * @return 是否选择成功
     */

    private boolean select(Object condition, String paraType, boolean cancel, Double timeout) {
        if (!this.isMulti() && (condition instanceof Collection || condition instanceof String[] || condition instanceof Integer[]))
            throw new IllegalArgumentException("单选列表只能传入str格式.");
        String mode = cancel ? "false" : "true";
        timeout = timeout != null ? timeout : this.ele.getOwner().timeout();
        Collection<String> objects = new ArrayList<>();
        if (!(condition instanceof Collection || condition instanceof String[] || condition instanceof Integer[]))
            objects.add(condition.toString());
        else if (condition instanceof Collection) ((List<?>) condition).forEach(a -> objects.add(a.toString()));
        else if (condition instanceof String[]) Collections.addAll(objects, (String[]) condition);
        else Collections.addAll(objects, Arrays.toString(((Integer[]) condition)));
        if ("text".equals(paraType) || "value".equals(paraType))
            return this.textValue(Collections.singleton(objects), paraType, mode, timeout);
        else if ("index".equals(paraType)) return this.index(objects, mode, timeout, 0);
        return false;
    }

    /**
     * 执行text和value搜索
     *
     * @param condition 条件
     * @param paraType  参数类型，可选 'text'、'value'
     * @param mode      'true' 或 'false'
     * @param timeout   超时时间
     * @return 是否选择成功
     */
    private boolean textValue(Collection<Object> condition, String paraType, String mode, double timeout) {
        boolean ok = false;
        int textLen = condition.size();
        List<ChromiumElement> elements = new ArrayList<>();
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        while (System.currentTimeMillis() < endTime) {
            if (Objects.equals(paraType, "text"))
                this.options().stream().filter(i -> condition.toString().contains(i.text())).forEachOrdered(elements::add);
            else if (Objects.equals(paraType, "value"))
                this.options().stream().filter(i -> condition.toString().contains(i.attr("value"))).forEachOrdered(elements::add);
            if (elements.size() >= textLen) {
                ok = true;
                break;
            }
        }
        if (ok) {
            this.selectOptions(elements, mode);
            return true;
        }
        return false;
    }


    /**
     * 执行index搜索
     *
     * @param condition 条件
     * @param mode      'true' 或 'false'
     * @param timeout   超时时间
     * @return 是否选择成功
     */
    private boolean index(Collection<String> condition, String mode, double timeout, Integer ignored) {
        return index(condition.stream().map(Integer::parseInt).collect(Collectors.toList()), mode, timeout);
    }


    /**
     * 执行index搜索
     *
     * @param condition 条件
     * @param mode      'true' 或 'false'
     * @param timeout   超时时间
     * @return 是否选择成功
     */
    private boolean index(Collection<Integer> condition, String mode, double timeout) {
        boolean ok = false;
        int textLen = Math.abs(condition.stream().mapToInt(Math::abs).max().orElse(0));
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        while (System.currentTimeMillis() < endTime) {
            if (this.options().size() >= textLen) {
                ok = true;
                break;
            }
        }

        if (ok) {
            List<ChromiumElement> elements = options();
            selectOptions(condition.stream().mapToInt(i -> i).mapToObj(i -> i > 0 ? elements.get(i - 1) : elements.get(i)).collect(Collectors.toList()), mode);
            return true;
        }

        return false;
    }

    /**
     * 选中或取消某个选项
     *
     * @param option options元素对象
     * @param mode   选中还是取消
     */
    private void selectOptions(List<ChromiumElement> option, String mode) {
        if (this.isMulti() && option.size() > 1) option = List.of(option.get(0));
        for (ChromiumElement chromiumElement : option) {
            chromiumElement.runJs("this.selected=" + mode + ";");
            this.dispatchChange();
        }
    }

    /**
     * 触发修改动作
     */
    private void dispatchChange() {
        this.ele.runJs("this.dispatchEvent(new Event(\"change\", {bubbles: true}));");
    }
}
