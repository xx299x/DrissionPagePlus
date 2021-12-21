# class SessionElement()

session 模式的元素对象，包装了一个Element对象，并封装了常用功能。

参数说明：

- ele: HtmlElement - lxml 库的 HtmlElement 对象
- page: SessionPage - 元素所在页面对象

## inner_ele

被包装的 HTMLElement 对象。

返回： HtmlElement

## page

元素所在页面对象，如果是从 html 文本生成的元素，则为 None。

返回：SessionElement 或 None

## tag

返回元素标签名。

返回： str

## html

返回元素 outerHTML 文本。

返回： str

## inner_html

返回元素 innerHTML 文本。

返回： str

## attrs

以字典格式返回元素所有属性的名称和值。

返回： dict

## text

返回元素内文本，已格式化处理。

返回： str

## raw_text

返回元素内未格式化处理的原始文本。

返回： str

## comments

以 list 方式返回元素内所有注释文本。

返回： list

## link

返回元素 href 或 src 绝对 url。

返回： str

## css_path

返回元素 css selector 绝对路径。

返回： srt

## xpath

返回元素 xpath 绝对路径。

返回： srt

## parent

返回父级元素对象。

返回： SessionElement

## next

返回下一个兄弟元素对象。

返回： SessionElement

## prev

返回上一个兄弟元素对象。

返回： SessionElement

## parents()

返回第N层父级元素对象。

参数说明：

- num:int - 第几层父元素

返回： SessionElement

## nexts()

返回后面第 num 个兄弟元素或节点文本。

参数说明：

- num - 后面第几个兄弟元素
- mode: str - 'ele', 'node' 或 'text'，匹配元素、节点、或文本节点

返回： [SessionElement, str]

## prevs()

返回前 N 个兄弟元素对象。

参数说明：

- num - 前面第几个兄弟元素
- mode: str - 'ele', 'node' 或 'text'，匹配元素、节点、或文本节点

返回： [SessionElement, str]

## attr()

获取元素某个属性的值。

参数说明：

- attr: str - 属性名称

返回： str

## texts()

返回元素内所有直接子节点的文本，包括元素和文本节点。

参数说明：

- text_node_only: 是否只返回文本节点

返回： 文本组成的 list

## ele()

根据查询参数获取元素。  
如查询参数是字符串，可选 '@属性名:'、'tag:'、'text:'、'css:'、'xpath:'、'.'、'#' 方式。无控制方式时默认用 text 方式查找。  
如是loc，直接按照内容查询。

参数说明：

- loc_or_str:[Tuple[str, str], str]  - 查询条件参数

- timeout: float -不起实际作用，用于和父类对应

返回： [SessionElement, str]

## eles()

根据查询参数获取符合条件的元素列表。查询参数使用方法和ele方法一致。

参数说明：

- loc_or_str: [Tuple[str, str], str]        - 查询条件参数

- timeout: float -不起实际作用，用于和父类对应

返回： List[SessionElement or str]

## s_ele()

功能与 ele() 一致，这里仅用于和 DriverElement 匹配。

参数说明：

- loc_or_str:[Tuple[str, str], str]  - 查询条件参数

返回： [SessionElement, str]

## s_eles()

功能与 eles() 一致，这里仅用于和 DriverElement 匹配。

参数说明：

- loc_or_str: [Tuple[str, str], str]        - 查询条件参数

返回： List[SessionElement or str]