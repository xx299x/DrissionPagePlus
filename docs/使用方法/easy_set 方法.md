可快速地修改常用设置的方法。全部用于 driver 模式的设置。调用 easy_set 方法会修改默认 ini 文件相关内容。

```python
get_match_driver()  # 识别chrome版本并自动下载匹配的chromedriver.exe
show_settings()  # 打印所有设置
set_headless(True)  # 开启 headless 模式
set_no_imgs(True)  # 开启无图模式
set_no_js(True)  # 禁用 JS
set_mute(True)  # 开启静音模式
set_user_agent('Mozilla/5.0 (Macintosh; Int......')  # 设置 user agent
set_proxy('127.0.0.1:8888')  # 设置代理
set_paths(paths)  # 见 [初始化] 一节
set_argument(arg, value)  # 设置属性，若属性无值（如'zh_CN.UTF-8'），value 为 bool 表示开关；否则value为str，当 value为''或 False，删除该属性项
check_driver_version()  # 检查chrome和chromedriver版本是否匹配
```
