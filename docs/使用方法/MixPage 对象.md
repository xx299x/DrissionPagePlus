MixPage 页面对象封装了常用的网页操作，并实现 driver 和 session 模式之间的切换。  
MixPage 须控制一个 Drission 对象并使用其中的 driver 或 session，如没有传入，MixPage 会自己创建一个（使用传入的配置信息或从默认 ini 文件读取）。

Tips: 多对象协同工作时，可将一个 MixPage 中的 Drission 对象传递给另一个，使多个对象共享登录信息或操作同一个页面。

## 创建对象

创建对象方式有3种：简易、传入 Drission 对象、传入配置。可根据实际需要选择。
参数说明：
    drission: Drission对象,如没传入则创建一个。
    mode: 'd' 或 's' 即driver模式和session模式,默认是d模式
    timeout: 超时时间。d模式时为寻找元素时间,s模式时为连接时间
    driver_options: 浏览器设置，没有传入drission参数时会用这个设置新建Drission对象
    session_options: requests设置，没有传入drission参数时会用这个设置新建Drission对象

```python
# 简易创建方式，以 ini 文件默认配置自动创建 Drission 对象
page = MixPage()
page = MixPage('s')

# 以传入 Drission 对象创建
page = MixPage(drission=drission)
page = MixPage(mode='s', drission=drission, timeout=5)  # session 模式，等待时间5秒（默认10秒）

# 传入配置信息，MixPage 根据配置在内部创建 Drission
page = MixPage(driver_options=do, session_options=so)  # 默认 d 模式
```

## 访问网页

```python
# 默认方式
page.get(url)
# 指定重试次数和间隔
page.get(url, retry=5, interval=0.5) #若连接出错，程序会自动重试若干次，可指定重试次数和等待间隔

page.post(url, data, **kwargs)  # 只有 session 模式才有 post 方法
```

## 切换模式
在 s 和 d 模式之间切换，切换时会自动同步 cookies 和正在访问的 url。

```python
page.change_mode(mode,go)
切换模式，'d' 或 's'。切换时会把当前模式的 cookies 复制到目标模式。
参数说明：
    mode: str - 指定目标模式，'d' 或 's'。
    go: bool - 切换模式后是否跳转到当前 url
返回： None
```
Tips：使用某种模式独有的方法时会自动跳转到该模式。

## 页面属性

```python
page.url  # 当前访问的 url
page.mode  # 当前模式
page.drission  # 当前使用的 Dirssion 对象
page.driver  # 当前使用的 WebDirver 对象
page.session  # 当前使用的 Session 对象
page.cookies  # 获取 cookies 信息
page.html  # 页面源代码
page.json  # 当返回内容是json格式时，返回对应的字典
page.title  # 当前页面标题

# d 模式独有：
page.tabs_count  # 返回标签页数量
page.tab_handles  # 返回所有标签页 handle 列表
page.current_tab_num  # 返回当前标签页序号
page.current_tab_handle  # 返回当前标签页 handle
```

## 页面操作

调用只属于 d 模式的方法，会自动切换到 d 模式。

```python
page.set_cookies()  # 设置cookies
page.get_cookies()  # 获取 cookies，可以 list 或 dict 方式返回
page.change_mode()  # 切换模式，会自动复制 cookies
page.cookies_to_session()  # 从 WebDriver 对象复制 cookies 到 Session 对象
page.cookies_to_driver()  # 从 Session 对象复制 cookies 到 WebDriver 对象
page.get(url, retry, interval, **kwargs)  # 用 get 方式访问网页，可指定重试次数及间隔时间
page.ele(loc_or_ele, timeout)  # 返回页面中符合条件的元素，默认返回第一个。​如查询参数是字符串，可选 '@属性名:'、'tag:'、'text:'、'css:'、'xpath:'、'.'、'#' 方式。无控制方式时默认用 text 方式查找。​如是loc，直接按照内容查询。
    参数说明：
        loc_or_ele: [Tuple[str, str], str, DriverElement, SessionElement, WebElement] - 元素的定位信息，可以是元素对象，loc 元组，或查询字符串
        timeout: float - 查找元素超时时间，driver 模式下有效
    返回： [DriverElement, SessionElement, str] - 元素对象或属性、文本节点文本
page.eles(loc_or_ele, timeout)  # 获取所有符合条件的元素、节点或属性 
page.download(url, save_path, rename, file_exists, **kwargs)  # 下载一个文件，返回是否成功和下载信息字符串。改方法会自动避免和目标路径现有文件重名。
    参数说明：
        url: str - 文件 url
        save_path: str - 存放路径，默认为 ini 文件中指定的临时文件夹
        rename: str - 重命名文件，不改变扩展名
        file_exists: str - 若存在同名文件，可选择 'rename', 'overwrite', 'skip' 方式处理
        **kwargs - 用于 requests 的连接参数
    返回： Tuple[bool, str] - 下载是否成功（bool）和状态信息（成功时信息为文件路径）的元组

以下方法和属性只有 driver 模式下生效，调用时会自动切换到 driver 模式
page.close_driver()  # 关闭 WebDriver 对象
page.close_session()  # 关闭 Session 对象

# s 模式独有：
page.post(url, data, retry, interval, **kwargs) ##以 post 方式跳转，调用时自动切换到 session 模式。
    参数说明：
        url: str - 目标 url
        data: dict - 提交的数据
        go_anyway: bool - 是否强制跳转。若目标 url 和当前 url 一致，默认不跳转。
        show_errmsg: bool - 是否显示和抛出异常
        retry: int - 连接出错时重试次数
        interval: float - 重试间隔（秒）
        **kwargs - 用于 requests 的连接参数
    返回： [bool, None] - url 是否可用
# d 模式独有：
page.wait_ele(loc_or_ele, mode, timeout)  # 等待元素从 dom 删除、显示、隐藏
    参数说明：
        loc_or_ele: [str, tuple, DriverElement, WebElement] - 元素查找方式，与ele()相同
        mode: str - 等待方式，可选：'del', 'display', 'hidden'
        timeout: float - 等待超时时间
    返回： bool - 等待是否成功
page.run_script(js, *args) 执行JavaScript代码。
    参数说明：
        script: str - JavaScript 代码文本
        *args - 传入的参数
    返回： Any
page.create_tab(url)  # 新建并定位到一个标签页,该标签页在最后面。
    参数说明：
        url: str - 新标签页跳转到的网址
    返回： None
page.to_tab(num_or_handle)  # 跳转到标签页
    参数说明：
        num_or_handle:[int, str] - 标签页序号或handle字符串，序号第一个为0，最后为-1
    返回： None
page.close_current_tab()  # 关闭当前标签页
page.close_other_tabs(num_or_handles)  # 关闭传入的标签页以外标签页，默认保留当前页。可传入列表或元组。
page.to_iframe(loc_or_ele)  # 跳转到 iframe，默认跳转到最高层级，兼容 selenium 原生参数。
    参数说明:
        loc_or_ele: [int, str, tuple, WebElement, DriverElement] - 查找 iframe 元素的条件，可接收 iframe 序号（0开始）、id 或
        name、查询字符串、loc参数、WebElement对象、DriverElement 对象，传入 'main' 跳到最高层，传入 'parent' 跳到上一层   
    示例:
        to_iframe('tag:iframe') - 通过传入 iframe 的查询字符串定位
        to_iframe('iframe_id') - 通过 iframe 的 id 属性定位
        to_iframe('iframe_name') - 通过 iframe 的 name 属性定位
        to_iframe(iframe_element) - 通过传入元素对象定位
        to_iframe(0) - 通过 iframe 的序号定位
        to_iframe('main') - 跳到最高层
        to_iframe('parent') - 跳到上一层
    返回: None   
page.screenshot(path,filename)  # 网页截图，返回截图文件路径。
    参数说明：
        path: str - 截图保存路径，默认为 ini 文件中指定的临时文件夹
        filename: str - 截图文件名，默认为页面 title 为文件名
    返回： str
page.scroll_to_see(loc_or_ele)  # 滚动直到某元素可见
    参数说明：
        loc_or_ele: [str, tuple, WebElement, DriverElement] - 查找元素的条件，和 ele() 方法的查找条件一致。
    返回： None
page.scroll_to(mode, pixel)  # 按参数指示方式滚动页面，可选滚动方向：'top', 'bottom', 'rightmost', 'leftmost', 'up', 'down', 'left', 'right', 'half'
page.refresh()  # 刷新当前页面
page.back()  # 浏览器后退
page.set_window_size(x, y)  # 设置浏览器窗口大小，默认最大化
page.check_page()  # 检测页面是否符合预期
page.chrome_downloading(download_path)  # 获取 chrome 正在下载的文件列表
    参数说明：
        download_path: str - 下载文件夹路径
    返回：list
page.process_alert(mode, text)  # 处理提示框。
    参数说明：
        mode: str - 'ok' 或 'cancel'，若输入其它值，不会按按钮但依然返回文本值
        text: str - 处理 prompt 提示框时可输入文本
    返回： [str, None] - 提示框内容文本
```

## cookies 的使用

MixPage 支持获取和设置 cookies，具体使用方法如下：

```python
page.cookies  # 以字典形式返回 cookies，只会返回当前域名可用的 cookies
page.get_cookies(as_dict=False)  # 以列表形式返回当前域名可用 cookies，每个 cookie 包含所有详细信息
page.get_cookies(all_domains=True)  # 以列表形式返回所有 cookies，只有 s 模式有效
page.set_cookies(cookies)  # 设置 cookies，可传入 RequestsCookieJar, list, tuple, str, dict
```

Tips:

- d 模式设置 cookies 后要刷新页面才能看到效果。
- s 模式可在 ini 文件、SessionOptions、配置字典中设置 cookies，在 MixPage 初始化时即可传入，d 模式只能用 set_cookies() 函数设置。