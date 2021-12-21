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
举例场景：我在本地项目 要覆盖默认的configs.ini文件相关内容,具体写法流程。
    1:新建一个py文件
    2:导入easy_set
        from DrissionPage.easy_set import set_paths
    3:调set_path方法设置相关的参数值
        set_paths(
                    driver_path=r"E:\flying-soft-package\chrome75\chrome75\chromedriver75.exe",
                    chrome_path=r"E:\flying-soft-package\chrome75\chrome75\chrome.exe",
                    user_data_path=r"E:\flying-soft-package\chrome75\chrome75\user_data",
                    debugger_address='127.0.0.1:9222',
                    check_version=True
                  )
         参数含义:
             driver_path:chromedriver.exe路径
             chrome_path: chrome.exe路径
             user_data_path: 用户数据路径
             debugger_address: 调试浏览器地址，例：127.0.0.1:9222
             check_version: 是否检查chromedriver和chrome是否匹配(若不设置,默认是true)
    4:执行这个py文件 右击run。
             
