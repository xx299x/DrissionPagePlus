Chrome 浏览器的配置繁琐且难以记忆，本库提供一些常用功能的快速设置方法，调用即可修改 ini 文件中该部分内容。

!> **注意：**  <br>easy_set 方法仅用于设置 ini 文件，浏览器或 Session 创建后再调用没有效果的。  <br>如果是接管已打开的浏览器，这些设置也没有用。<br>这些方法只是便于修改 ini 文件，不要写在正式代码中。

## 📍 简单示例

```python
# 导入
from DrissionPage.easy_set import set_headless

# 设置无头模式
set_headless(True)
```

## 📍 `show_settings()`

该方法用于打印 ini 文件内容。

**参数：**

- `ini_path`：ini 文件路径，默认读取默认 ini 文件

**返回：**`None`

## 📍 `set_paths()`

该方法用于设置浏览器用到的几种路径信息，设置后可检查 driver 是否和浏览器匹配。

**参数：**

- `driver_path`：chromedriver.exe 路径
- `chrome_path`：chrome.exe 路径
- `local_port`：本地端口号
- `debugger_address`：调试浏览器地址，会覆盖 local_port 设置，例：127.0.0.1:9222
- `download_path`：下载文件路径
- `tmp_path`：临时文件夹路径，暂时没有作用
- `user_data_path`：用户数据路径
- `cache_path`：缓存路径
- `ini_path`：要修改的 ini 文件路径，默认设置默认 ini 文件
- `check_version`：是否检查 chromedriver 和 Chrome 是否匹配

**返回：**`None`

## 📍 `set_headless()`

该方法用于设置是否已无头模式启动浏览器。

**参数：**

- `on_off`：`bool`类型，表示开或关
- `ini_path`：要修改的 ini 文件路径，默认设置默认 ini 文件

**返回：**`None`

## 📍 `set_no_imgs()`

该方法用于设置是否禁止加载图片。

**参数：**

- `on_off`：`bool`类型，表示开或关
- `ini_path`：要修改的 ini 文件路径，默认设置默认 ini 文件

**返回：**`None`

## 📍 `set_mute()`

该方法用于设置是否静音。

**参数：**

- `on_off`：`bool`类型，表示开或关
- `ini_path`：要修改的 ini 文件路径，默认设置默认 ini 文件

**返回：**`None`

## 📍 `set_proxy()`

该方法用于设置代理。

**参数：**

- `proxy`: 代理网址和端口，如 127.0.0.1:1080
- `ini_path`：要修改的 ini 文件路径，默认设置默认 ini 文件

**返回：**`None`

## 📍 `set_user_agent()`

该方法用于设置 user agent。

**参数：**

- `user_agent`：user agent 文本
- `ini_path`：要修改的 ini 文件路径，默认设置默认 ini 文件

**返回：**`None`

## 📍 `set_argument()`

该方法用于设置浏览器配置 argument 属性。

**参数：**

- `arg`：属性名
- `value`：属性值，有值的属性传入值。没有的传入`bool`表示开或关
- `ini_path`：要修改的 ini 文件路径，默认设置默认 ini 文件

**返回：**`None`

## 📍 `check_driver_version()`

该方法用于检查传入的 chrome 和 chromedriver 是否匹配。

**参数：**

- `driver_path`：chromedriver.exe 路径
- `chrome_path`：chrome.exe 路径

**返回：**`bool`类型，表示是否匹配

## 📍 `get_match_drive()`

该方法用于自动识别 chrome 版本并下载匹配的 driver。

**参数：**

- `ini_path`：要读取和修改的 ini 文件路径
- `save_path`：chromedriver 保存路径
- `chrome_path`：指定 chrome.exe 位置，不指定会自动依次在 ini 文件、注册表、系统路径中查找
- `show_msg`：是否打印信息
- `check_version`：是否检查版本匹配

**返回：** 成功返回 driver 路径，失败返回`None`
