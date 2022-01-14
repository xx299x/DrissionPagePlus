Drission 对象用于管理 driver 和 session 对象。在多个页面协同工作时，Drission 对象用于传递驱动器，使多个页面类可控制同一个浏览器或 Session 对象。  
可直接读取 ini 文件配置信息创建，也可以在初始化时传入配置信息。  
在“使用方法->创建页面对象”章节已经涉及过 Drission 的用法，这里介绍属性和方法。

# Drission 类

初始化参数：

- driver_or_options：driver 对象或 DriverOptions、Options 类，传入 False 则创建空配置对象
- session_or_options：Session 对象或设置字典，传入 False 则创建空配置对象
- ini_path：ini 文件路径
- proxy：代理设置，dict 类型。格式：{'http': '127.0.0.1:1080', 'https': '127.0.0.1:1080'}

前两个参数可直接接收 WebDriver 和 Session 对象，这时后面两个参数无效。  
若接收配置对象，则按照配置创建 WebDriver 和 Session 对象。

用 ini 文件信息创建：

```python
# 由默认 ini 文件创建
drission = Drission()  

# 由其它 ini 文件创建
drission = Drission(ini_path='D:\\settings.ini')  
```

传入配置创建：

```python
from DrissionPage.config import DriverOptions

# 创建 driver 配置对象
do = DriverOptions()  
# 传入配置，driver_options 和 session_options 都是可选的，须要使用对应模式才须要传入
drission = Drission(driver_options=do)  
```

## session

此属性返回该对象管理的 Session 对象。

## driver

此属性返回该对象管理的 WebDriver 对象。

## driver_options

此属性返回用于创建 WebDriver 对象的 DriverOptions 对象。

## session_options

此属性以 dict 形式返回用于创建 Session 对象的配置参数。可传入 dict 或 SessionOptions 赋值。

## proxy

此属性返回代理信息，dict 形式。可传入 dict 赋值。格式：{'http': '127.0.0.1:1080', 'https': '127.0.0.1:1080'}

## debugger_progress

此属性返回浏览器进程（如有）。

## kill_browser()

此方法用于关闭浏览器进程。

参数：无

返回： None

## get_browser_progress_id()

此方法用于获取浏览器进程id。

参数：无

返回： None

## hide_browser()

此方法用于隐藏浏览器进程窗口。

参数：无

返回： None

## show_browser()

此方法用于显示浏览器进程窗口。

参数：无

返回： None

## set_cookies()

此方法用于设置cookies。可选择对某个对象设置。

参数：

- cookies：cookies信息，可为 CookieJar, list, tuple, str, dict
- set_session：是否设置 Session 对象的 cookies
- set_driver：是否设置浏览器的 cookies

返回：None

## cookies_to_session()

此方法用于把 WebDriver 对象的 cookies 复制到 Session 对象。

参数：

- copy_user_agent：是否复制 user agent 信息

返回：None

## cookies_to_driver()

此方法用于把 Session 对象的 cookies 复制到 WebDriver 对象。  
复制 cookies 到浏览器必须指定域名。

参数：

- url：作用域

返回：None

## close_driver()

此方法用于关闭 WebDriver 对象，可选择是否关闭浏览器进程。

参数：

- kill：是否关闭浏览器进程

返回：None

## close_session()

此方法用于关闭 Session 对象。

参数：无

返回：None

## close()

此方法用于关闭 Session 和 WebDriver 对象。

参数：无

返回：None