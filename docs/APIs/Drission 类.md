## class Drission()

Drission 类用于管理 WebDriver 对象和 Session 对象，是驱动器的角色。

参数说明：

- driver_or_options: [WebDriver, dict, Options, DriverOptions]     - WebDriver 对象或 chrome 配置参数。
- session_or_options: [Session, dict]                                            - Session 对象配置参数
- ini_path: str - ini 文件路径，默认为 DrissionPage 文件夹下的ini文件
- proxy: dict - 代理设置

## session

返回 Session 对象，自动按配置信息初始化。

返回： Session - 管理的 Session 对象

## driver

返回 WebDriver 对象，自动按配置信息初始化。

返回： WebDriver - 管理的 WebDriver 对象

## driver_options

返回或设置 driver 配置。

返回： dict

## session_options

返回 session 配置。

返回： dict

## proxy

返回代理配置。

返回： dict

## debugger_progress

调试浏览器进程，当浏览器是自动创建时才能返回，否则返回 None。

返回：浏览器进程

## session_options()

设置 session 配置。

返回： None

## cookies_to_session()

把 driver 对象的 cookies 复制到 session 对象。

参数说明：

- copy_user_agent: bool - 是否复制 user_agent 到 session
- driver: WebDriver - 复制 cookies 的 WebDriver 对象
- session: Session - 接收 cookies 的 Session 对象

返回： None

## cookies_to_driver()

把 cookies 从 session 复制到 driver。

参数说明：

- url: str - cookies 的域
- driver: WebDriver - 接收 cookies 的 WebDriver 对象
- session: Session - 复制 cookies 的 Session 对象

返回： None

## user_agent_to_session()

把 user agent 从 driver 复制到 session。

参数说明：

- driver: WebDriver - WebDriver 对象，复制 user agent
- session: Session - Session 对象，接收 user agent

返回： None

## close_driver()

关闭浏览器，driver 置为 None。

返回： None

## close_session()

关闭 session 并置为 None。

返回： None

## close()

关闭 driver 和 session。

返回： None

## kill_browser()

关闭浏览器进程（如果可以）。

返回： None