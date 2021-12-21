## class DriverElement()

driver 模式的元素对象，包装了一个 WebElement 对象，并封装了常用功能。

参数说明：

- ele: WebElement - WebElement 对象
- page: DriverPage - 元素所在的页面对象

## inner_ele

被包装的 WebElement 对象。

返回： WebElement

## html

返回元素 outerHTML 文本。

返回： str

## json

当返回内容是json格式时，返回对应的字典。

返回： dict

## inner_html

返回元素 innerHTML 文本。

返回： str

## tag

返回元素标签名。

返回： str

## attrs

以字典方式返回元素所有属性及值。

返回： dict

## text

返回元素内的文本，已格式化处理。

返回： str

## raw_text

返回元素内的文本，未格式化处理。

返回： str

## comments

返回元素内注释列表。

返回： list

## link

返回元素 href 或 src 绝对 url。

返回： str

## css_path

返回元素 css selector 绝对路径。

返回： str

## xpath

返回元素 xpath 绝对路径。

返回： str

## parent

返回父级元素对象。

返回： DriverElement

## next

返回下一个兄弟元素对象。

返回： DriverElement

## prev

返回上一个兄弟元素对象。

返回： DriverElement

## size

以字典方式返回元素大小。

返回： dict

## location

以字典方式放回元素坐标。

返回： dict

## shadow_root

返回当前元素的 shadow_root 元素对象

返回： ShadowRoot

## before

返回当前元素的 ::before 伪元素内容

返回： str

## after

返回当前元素的 ::after 伪元素内容

返回： str

## select

如果是 select 元素，返回 Select 对象，否则返回 None。

返回：Union[Select, None]

## wait__ele

等待子元素从dom删除、显示、隐藏。

参数说明：

- loc_or_ele:Union[str, tuple, DrissionElement, WebElement]  - 可以是元素、查询字符串、loc元组

- mode:str - 等待方式，可选：'del', 'display', 'hidden'

- timeout:float - 等待超时时间

返回： 等待是否成功

## texts()

返回元素内所有直接子节点的文本，包括元素和文本节点

参数说明：

- text_node_only:bool - 是否只返回文本节点

返回： List[str]

## parents()

返回第 N 层父级元素对象。

参数说明：

- num: int - 第几层父元素

返回： DriverElement

## nexts()

返回后面第 num 个兄弟元素或节点文本。

参数说明：

- num: int - 后面第几个兄弟元素或节点
- mode: str - 'ele', 'node' 或 'text'，匹配元素、节点、或文本节点

返回： [DriverElement, str]

## prevs()

返回前面第 num 个兄弟元素或节点文本。

参数说明：

- num: int - 前面第几个兄弟元素或节点
- mode: str - 'ele', 'node' 或 'text'，匹配元素、节点、或文本节点

返回： [DriverElement, str]

## attr()

获取元素某个属性的值。

参数说明：

- attr: str - 属性名称

返回： str

## prop()

获取元素某个property属性的值。

参数说明：

- prop: str - 属性名称

返回： str

## ele()

返回当前元素下级符合条件的子元素、属性或节点文本。  
如查询参数是字符串，可选 '@属性名:'、'tag:'、'text:'、'css:'、'xpath:'、'.'、'#' 方式。无控制方式时默认用 text 方式查找。  
如是loc，直接按照内容查询。

参数说明：

- loc_or_str: [Tuple[str, str], str]         - 元素的定位信息，可以是 loc 元组，或查询字符串
- mode: str - 'single' 或 'all'，对应查找一个或全部
- timeout: float - 查找元素超时时间

返回： [DriverElement, str]

## eles()

根据查询参数获取符合条件的元素列表。查询参数使用方法和 ele 方法一致。

参数说明：

- loc_or_str: [Tuple[str, str], str]        - 查询条件参数
- timeout: float - 查找元素超时时间

返回： List[DriverElement or str]

## s_ele()

查找第一个符合条件的元素以SessionElement形式返回。

参数说明：

- loc_or_str: [Tuple[str, str], str]         - 元素的定位信息，可以是 loc 元组，或查询字符串

返回： [SessionElement, str]

## s_eles()

查找所有符合条件的元素以SessionElement列表形式返回。

参数说明：

- loc_or_str: [Tuple[str, str], str]        - 查询条件参数

返回： List[SessionElement or str]

## style()

返回元素样式属性值。

参数说明：

- style: str - 样式属性名称
- pseudo_ele: str - 伪元素名称

返回： str

## click()

点击元素，如不成功则用 js 方式点击，可指定是否用 js 方式点击。

参数说明：

- by_js: bool - 是否用js方式点击

返回： bool

## click_at()

带偏移量点击本元素，相对于左上角坐标。不传入x或y值时点击元素中点。

参数说明：

- x: Union[int, str]  - 相对元素左上角坐标的x轴偏移量
- y: Union[int, str]  - 相对元素左上角坐标的y轴偏移量
- by_js: bool - 是否用js方式点击

返回： None

## r_click()

右键单击。

返回： None

## r_click_at()

带偏移量右键单击本元素，相对于左上角坐标。不传入x或y值时点击元素中点。

参数说明：

- x: Union[int, str]  - 相对元素左上角坐标的x轴偏移量
- y: Union[int, str]  - 相对元素左上角坐标的y轴偏移量

返回： None

## input()

输入文本或组合键，返回是否成功输入。insure_input 为 False 时始终返回 True。

参数说明：

- vals: Union[str, tuple]    - 文本值或按键组合
- clear: bool - 输入前是否清除文本框
- insure_input: bool - 是否自动检测并确保输入正确
- timeout: folat - 尝试输入的超时时间，不指定则使用父页面的超时时间，只在insure_input为True时生效

返回： bool，是否成功输入。insure_input 为 False 时始终返回 True。

## run_script()

执行 js 代码，传入自己为第一个参数。

参数说明：

- script: str - JavaScript文本
- *args - 传入的参数

返回： Any

## submit()

提交表单。

返回： None

## clear()

清空文本框。

参数说明：

- insure_clear: bool - 是否确保清空

返回： bool - 是否清空成功，不能清空的元素返回None

## is_selected()

元素是否被选中。

返回： bool

## is_enabled()

元素在页面中是否可用。

返回： bool

## is_displayed()

元素是否可见。

返回： bool

## is_valid()

元素是否还在 DOM 内。该方法用于判断页面跳转元素不能用的情况

返回： bool

## screenshot()

网页截图，返回截图文件路径。

参数说明：

- path: str - 截图保存路径，默认为 ini 文件中指定的临时文件夹
- filename: str - 截图文件名，默认为页面 title 为文件名

返回： str

## select()

在下拉列表中选择。

参数说明：

- text: str - 选项文本

返回： bool - 是否成功

## set_prop()

设置元素property属性。

参数说明：

- prop: str - 属性名
- value: str - 属性值

返回： bool -是否成功

## set_attr()

设置元素attribute参数。

参数说明：

- attr: str - 参数名
- value: str - 参数值

返回： bool -是否成功

## remove_attr()

删除元素属性。

参数说明：

- attr: str - 参数名

返回： bool -是否成功

## drag()

拖拽当前元素一段距离，返回是否拖拽成功。

参数说明：

- x: int - 拖拽x方向距离
- y: int - 拖拽y方向距离
- speed: int - 拖拽速度
- shake: bool - 是否随机抖动

返回： bool

## drag_to()

拖拽当前元素，目标为另一个元素或坐标元组，返回是否拖拽成功。

参数说明：

- ele_or_loc[tuple, WebElement, DrissionElement]  - 另一个元素或相对当前位置，坐标为元素中点坐标。
- speed: int - 拖拽速度
- shake: bool - 是否随机抖动

返回： bool

## hover()

在元素上悬停鼠标。

返回： None