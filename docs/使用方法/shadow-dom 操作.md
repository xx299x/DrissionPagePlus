d 模式独有，支持获取 shadow-root 及内部元素，获取到的 shadow-root 元素类型为 ShadowRootElement，用法和正常元素类似，但功能有所简化。

**注意：**

- 只能获取 open 的 shadow-root
- 查找 shadow-root 内部元素不能使用 xpath 方式

获取依附在普通元素内的 shadow-root 元素

```python
shadow_root_element = element.shadow_root  # element 为含有 shadow-root 的普通元素
```

属性及方法

```python
shadow_root_element.tag  # 返回 'shadow-root'
shadow_root_element.html  # html 内容
shadow_root_element.parent  # 父元素
shadow_root_element.next  # 下一个兄弟元素

shadow_root_element.parents(num)  # 获取向上 num 级父元素
shadow_root_element.nexts(num)  # 获取向后 num 个兄弟元素
shadow_root_element.ele(loc_or_str)  # 获取第一个符合条件的内部元素
shadow_root_element.eles(loc_or_str)  # 获取全部符合条件的内部元素
shadow_root_element.run_scrpit(js_text)  # 运行 js 脚本
shadow_root_element.is_enabled()  # 返回元素是否可用
shadow_root_element.is_valid()  # 返回元素是否还在 dom 内
```

**Tips:** 以上属性或方法获取到的元素是普通的 DriverElement，用法参考上文所述。