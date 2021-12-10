chrome 的配置很繁琐，为简化使用，本库提供了常用配置的设置方法。

# DriverOptions 对象

DriverOptions 对象继承自 selenium.webdriver.chrome.options 的 Options 对象，在其基础上增加了以下方法：

```python
options.remove_argument(value)  # 删除某 argument 值
options.remove_experimental_option(key)  # 删除某 experimental_option 设置
options.remove_all_extensions()  # 删除全部插件
options.save()  # 保存当前打开的 ini 文件
options.save('D:\\settings.ini')  # 保存到指定路径 ini 文件
options.save('default')  # 保存当前设置到默认 ini 文件
options.set_argument(arg, value)  # 设置 argument 属性
options.set_headless(on_off)  # 设置是否使用无界面模式
options.set_no_imgs(on_off)  # 设置是否加载图片
options.set_no_js(on_off)  # 设置是否禁用 js
options.set_mute(on_off)  # 设置是否静音
options.set_user_agent(user_agent)  # 设置 user agent
options.set_proxy(proxy)  # 设置代理地址
options.set_paths(driver_path, chrome_path, debugger_address, download_path, user_data_path, cache_path)  # 设置浏览器相关的路径
```

# 使用方法

```python
do = DriverOptions()  # 读取默认 ini 文件创建 DriverOptions 对象
do = DriverOptions('D:\\settings.ini')  # 读取指定 ini 文件创建 DriverOptions 对象
do = DriverOptions(read_file=False)  # 不读取 ini 文件，创建空的 DriverOptions 对象

do.set_headless(False)  # 显示浏览器界面
do.set_no_imgs(True)  # 不加载图片
do.set_paths(driver_path='D:\\chromedriver.exe', chrome_path='D:\\chrome.exe')  # 设置路径
do.set_headless(False).set_no_imgs(True)  # 支持链式操作

drission = Drission(driver_options=do)  # 用配置对象创建 Drission 对象
page = MixPage(driver_options=do)  # 用配置对象创建 MixPage 对象

do.save()  # 保存当前打开的 ini 文件
do.save('D:\\settings.ini')  # 保存到指定的 ini 文件
do.save('default')  # 保存当前设置到默认 ini 文件
```