### class ShadowRootElement()

元素内 shadow-root 元素。

参数说明：

- inner_ele: WebElement - selenium 获取到的 shadow-root 元素
- parent_ele: DriverElement - shadow-root 所依附的元素

### tag

元素标签名。

返回：'shadow-root' 字符串。

### html

内部html文本。

返回：str

### parent

shadow-root 所依赖的父元素。

返回：DriverElement

### next

返回后一个兄弟元素。

返回：DriverElement

### parents()

返回上面第 num 级父元素

参数说明：

- num: int - 第几层父元素

返回：DriverElement

### nexts()

返回后面第 num 个兄弟元素

参数说明：

- num: int - 第几个兄弟元素

返回：DriverElement

### ele()

返回第一个符合条件的子元素。

参数说明：

- loc_or_str: Union[Tuple[str, str], str]  - 元素定位条件
- mode: str - 'single' 或 'all'，对应获取一个和全部
- timeout: float - 超时时间

返回：DriverElement - 第一个符合条件的元素

### eles()

返回所有符合条件的子元素。

参数说明：

- loc_or_str: Union[Tuple[str, str], str]  - 元素定位条件
- timeout: float - 超时时间

返回：List[DriverElement]  - 所有符合条件的元素组成的列表

### run_script()

对元素执行 js 代码。

参数说明：

- scrpit: str - js 代码
- *args - 传入的对象

### is_enabled()

返回元素是否可用。

返回：bool

### is_valid()

返回元素是否仍在 dom 内。

返回：bool