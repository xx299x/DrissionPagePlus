chrome 配置太复杂，所以把常用的配置写成简单的方法，调用会修改 ini 文件相关内容。

### get_match_driver()

自动识别 chrome 版本并下载匹配的driver。获取 ini 文件记录的 chrome.exe 路径，若没有则获取系统变量中的。

参数说明：

- ini_path: str - 要读取和修改的 ini 文件路径
- save_path: str - chromedriver 保存路径

返回： None

### show_settings()

打印 ini 文件中所有配置信息。

参数说明：

- ini_path: str - ini 文件路径，为 None 则读取默认 ini 文件

返回： None

### set_paths()

便捷的设置路径方法，把传入的路径保存到 ini 文件，并检查 chrome 和 chromedriver 版本是否匹配。

参数说明：

- driver_path: str - chromedriver.exe 路径
- chrome_path: str - chrome.exe 路径
- debugger_address: str - 调试浏览器地址，例：127.0.0.1:9222
- download_path: str - 下载文件路径
- tmp_path: str - 临时文件夹路径
- user_data_path: str - 用户数据路径
- cache_path: str - 缓存路径
- ini_path: str - ini 文件路径，为 None 则保存到默认 ini 文件
- check_version: bool - 是否检查 chromedriver 和 chrome 是否匹配

返回： None

### set_argument()

设置属性。若属性无值（如 'zh_CN.UTF-8' ），value 传入 bool 表示开关；否则把 value 赋值给属性，当 value 为 '' 或 False，删除该属性项。

参数说明：

- arg: str - 属性名
- value: [bool, str]  - 属性值，有值的属性传入值，没有的传入 bool
- ini_path: str - ini 文件路径，为 None 则保存到默认 ini 文件

返回： None

### set_headless()

开启或关闭 headless 模式。

参数说明：

- on_off: bool - 是否开启 headless 模式
- ini_path: str - ini 文件路径，为 None 则保存到默认 ini 文件

返回： None

### set_no_imgs()

开启或关闭图片显示。

参数说明：

- on_off: bool - 是否开启无图模式
- ini_path: str - ini 文件路径，为 None 则保存到默认 ini 文件

返回： None

### set_no_js()

开启或关闭禁用 JS 模式。

参数说明：

- on_off: bool - 是否开启禁用 JS 模式
- ini_path: str - ini 文件路径，为 None 则保存到默认 ini 文件

返回： None

### set_mute()

开启或关闭静音模式。

参数说明：

- on_off: bool - 是否开启静音模式
- ini_path: str - ini 文件路径，为 None 则保存到默认 ini 文件

返回： None

### set_user_agent()

设置 user_agent。

参数说明：

- user_agent: str - user_agent 值
- ini_path: str - ini 文件路径，为 None 则保存到默认 ini 文件

返回： None

### set_proxy()

设置代理。

参数说明：

- proxy: str - 代理值
- ini_path: str - ini 文件路径，为 None 则保存到默认 ini 文件

返回： None

### check_driver_version()

检查 chrome 与 chromedriver 版本是否匹配。

参数说明：

- driver_path: bool - chromedriver.exe 路径
- chrome_path: bool - chrome.exe 路径

返回： bool