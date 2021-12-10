## class MixPage()

MixPage 封装了页面操作的常用功能，可在 driver 和 session 模式间无缝切换。切换的时候会自动同步 cookies。  
获取信息功能为两种模式共有，操作页面元素功能只有 d 模式有。调用某种模式独有的功能，会自动切换到该模式。  
它继承自 DriverPage 和 SessionPage 类，这些功能由这两个类实现，MixPage 作为调度的角色存在。

参数说明：

- drission: Drission - Drission 对象，如没传入则创建一个。传入 's' 或 'd' 时快速配置相应模式
- mode: str - 模式，可选 'd' 或 's'，默认为'd'
- timeout: float - 超时时间，driver 模式为查找元素时间，session 模式为连接等待时间

## url

返回 MixPage 对象当前访问的 url。

返回： str

## mode

返回当前模式（ 's' 或 'd' ）。

返回： str

## drission

返回当前使用的 Dirssion 对象。

返回： Drission

## driver

返回driver对象，如没有则创建，调用时会切换到 driver 模式。

返回： WebDriver

## session

返回 session 对象，如没有则创建。

返回： Session

## response

返回s模式获取到的 Response 对象，调用时会切换到s模式。

返回： Response

## cookies

返回 cookies，从当前模式获取。

返回： [dict, list]

## html

返回页面 html 文本。

返回： str

## json

当返回内容是json格式时，返回对应的字典。

返回： dict

## title

返回页面 title。

返回： str

## url_available

返回当前 url 有效性。

返回： bool

## retry_times

连接失败时重试次数。

返回： int

## retry_interval

重试连接的间隔。

返回： int

## wait_object

返回WebDriverWait对象，重用避免每次新建对象。

返回： WebDriverWait

## set_cookies()

设置 cookies。

参数说明：

- cookies: Union[RequestsCookieJar, list, tuple, str, dict]  - cookies 信息，可为CookieJar, list, tuple, str, dict
- refresh: bool - 设置cookies后是否刷新页面

返回： None

## get_cookies()

返回 cookies。

参数说明：

- as_dict: bool - 是否以 dict 方式返回，默认以 list 返回完整的 cookies
- all_domains: bool - 是否返回所有域名的 cookies，只有 s 模式下生效

返回：cookies 字典或列表

## change_mode()

切换模式，'d' 或 's'。切换时会把当前模式的 cookies 复制到目标模式。

参数说明：

- mode: str - 指定目标模式，'d' 或 's'。
- go: bool - 切换模式后是否跳转到当前 url

返回： None

## ele()

返回页面中符合条件的元素，默认返回第一个。  
​如查询参数是字符串，可选 '@属性名:'、'tag:'、'text:'、'css:'、'xpath:'、'.'、'#' 方式。无控制方式时默认用 text 方式查找。  
​如是loc，直接按照内容查询。

参数说明：

- loc_or_str: [Tuple[str, str], str, DriverElement, SessionElement, WebElement]  - 元素的定位信息，可以是元素对象，loc 元组，或查询字符串
- timeout: float - 查找元素超时时间，driver 模式下有效

返回： [DriverElement, SessionElement, str]  - 元素对象或属性、文本节点文本

## eles()

根据查询参数获取符合条件的元素列表。查询参数使用方法和 ele 方法一致。

参数说明：

- loc_or_str: [Tuple[str, str], str]        - 查询条件参数
- timeout: float - 查找元素超时时间，driver 模式下有效

返回： [List[DriverElement or str], List[SessionElement or str]]  - 元素对象或属性、文本节点文本组成的列表

## s_ele()

查找第一个符合条件的元素以SessionElement形式返回。

参数说明：

- loc_or_ele: [Tuple[str, str], str]         - 元素的定位信息，可以是 loc 元组，或查询字符串

返回： [SessionElement, str]

## s_eles()

查找所有符合条件的元素以SessionElement列表形式返回。

参数说明：

- loc_or_ele: [Tuple[str, str], str]        - 查询条件参数

返回： List[SessionElement or str]

## cookies_to_session()

从 WebDriver 对象复制 cookies 到 Session 对象。

参数说明：

- copy_user_agent: bool - 是否同时复制 user agent

返回： None

## cookies_to_driver()

从 Session 对象复制 cookies 到 WebDriver 对象。

参数说明：

- url: str - cookies 的域或 url

返回： None

## get()

跳转到一个url，跳转前先同步 cookies，跳转后返回目标 url 是否可用。

参数说明：

- url: str - 目标 url
- go_anyway: bool - 是否强制跳转。若目标 url 和当前 url 一致，默认不跳转。
- show_errmsg: bool - 是否显示和抛出异常
- retry: int - 连接出错时重试次数
- interval: float - 重试间隔（秒）
- **kwargs - 用于 requests 的连接参数

返回： [bool, None]  - url 是否可用

## post()

以 post 方式跳转，调用时自动切换到 session 模式。

参数说明：

- url: str - 目标 url
- data: dict - 提交的数据
- go_anyway: bool - 是否强制跳转。若目标 url 和当前 url 一致，默认不跳转。
- show_errmsg: bool - 是否显示和抛出异常
- retry: int - 连接出错时重试次数
- interval: float - 重试间隔（秒）
- **kwargs - 用于 requests 的连接参数

返回： [bool, None]  - url 是否可用

## download()

下载一个文件，返回是否成功和下载信息字符串。改方法会自动避免和目标路径现有文件重名。

参数说明：

- file_url: str - 文件 url
- goal_path: str - 存放路径，默认为 ini 文件中指定的临时文件夹
- rename: str - 重命名文件，不改变扩展名
- file_exists: str - 若存在同名文件，可选择 'rename', 'overwrite', 'skip' 方式处理
- post_data: dict - post 方式时提交的数据
- show_msg: bool - 是否显示下载信息
- show_errmsg: bool - 是否显示和抛出异常
- **kwargs - 用于 requests 的连接参数

返回： Tuple[bool, str]  - 下载是否成功（bool）和状态信息（成功时信息为文件路径）的元组

以下方法和属性只有 driver 模式下生效，调用时会自动切换到 driver 模式

***

## tabs_count

返回标签页数量。

返回： int

## tab_handles

返回所有标签页 handle 列表。

返回： list

## current_tab_num

返回当前标签页序号。

返回： int

## current_tab_handle

返回当前标签页 handle。

返回： str

## wait_ele()

等待元素从 dom 删除、显示、隐藏。

参数说明：

- loc_or_ele: [str, tuple, DriverElement, WebElement]  - 元素查找方式，与ele()相同
- mode: str - 等待方式，可选：'del', 'display', 'hidden'
- timeout: float - 等待超时时间

返回： bool - 等待是否成功

## check_page()

d 模式时检查网页是否符合预期。默认由 response 状态检查，可重载实现针对性检查。

参数说明：

- by_requests:bool - 强制使用内置 response 进行检查

返回： [bool, None]  - bool 为是否可用，None 为未知

## run_script()

执行JavaScript代码。

参数说明：

- script: str - JavaScript 代码文本
- *args - 传入的参数

返回： Any

## create_tab()

新建并定位到一个标签页,该标签页在最后面。

参数说明：

- url: str - 新标签页跳转到的网址

返回： None

## close_current_tab()

关闭当前标签页。

返回： None

## close_other_tabs()

关闭传入的标签页以外标签页，默认保留当前页。可传入列表或元组。

参数说明：

- num_or_handles:[int, str]  - 要保留的标签页序号或 handle，可传入 handle 组成的列表或元组

返回： None

## to_tab()

跳转到标签页。

参数说明：

- num_or_handle:[int, str]  - 标签页序号或handle字符串，序号第一个为0，最后为-1

返回： None

## to_iframe()

跳转到 iframe，默认跳转到最高层级，兼容 selenium 原生参数。

参数说明：

- loc_or_ele: [int, str, tuple, WebElement, DriverElement] - 查找 iframe 元素的条件，可接收 iframe 序号（0开始）、id 或
  name、查询字符串、loc参数、WebElement对象、DriverElement 对象，传入 'main' 跳到最高层，传入 'parent' 跳到上一层

示例：

- to_iframe('tag:iframe')          - 通过传入 iframe 的查询字符串定位
- to_iframe('iframe_id')           - 通过 iframe 的 id 属性定位
- to_iframe('iframe_name')     - 通过 iframe 的 name 属性定位
- to_iframe(iframe_element)  - 通过传入元素对象定位
- to_iframe(0)                         - 通过 iframe 的序号定位
- to_iframe('main')                  - 跳到最高层
- to_iframe('parent')                - 跳到上一层

返回： None

## scroll_to_see()

滚动直到元素可见。

参数说明：

- loc_or_ele: [str, tuple, WebElement, DriverElement]  - 查找元素的条件，和 ele() 方法的查找条件一致。

返回： None

## scroll_to()

滚动页面，按照参数决定如何滚动。

参数说明：

- mode: str - 滚动的方向，top、bottom、rightmost、leftmost、up、down、left、right
- pixel: int - 滚动的像素

返回： None

## refresh()

刷新页面。

返回： None

## back()

页面后退。

返回： None

## set_window_size()

设置窗口大小，默认最大化。

参数说明：

- x: int - 目标宽度
- y: int - 目标高度

返回： None

## screenshot()

网页截图，返回截图文件路径。

参数说明：

- path: str - 截图保存路径，默认为 ini 文件中指定的临时文件夹
- filename: str - 截图文件名，默认为页面 title 为文件名

返回： str

## chrome_downloading()

返回浏览器下载中的文件列表。

参数说明：

- download_path: str - 下载文件夹路径

返回：list

## process_alert()

处理提示框。

参数说明：

- mode: str - 'ok' 或 'cancel'，若输入其它值，不会按按钮但依然返回文本值
- text: str - 处理 prompt 提示框时可输入文本

返回： [str, None]  - 提示框内容文本

## close_driver()

关闭 driver 及浏览器。

返回： None

## close_session()

关闭 session。

返回： None

## hide_browser()

隐藏浏览器窗口。

返回： None

## show_browser()

显示浏览器窗口。

返回： None