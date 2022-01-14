为使浏览器设置更便利，本库扩展了 selenium.webdriver.chrome.options 的 Options 对象功能，创建了 DriverOptions 类，专门用于管理浏览器的启动配置。  
**注意：**

- DriverOptions 仅用于管理启动配置，浏览器启动后再修改无效。
- 若设置了 debugger_address 且浏览器未启动，则只有 arguments、driver_path、chrome_path 参数生效。
- 若设置了 debugger_address 且接管已有浏览器，只有 driver_path 参数生效。

# DriverOptions 类

DriverOptions 类继承自 Options 类，保留了原来所有功能，原生功能不在这里叙述，只介绍增加的功能。  
对象创建时，可从配置文件中读取配置来进行初始化，不从文件读取则创建一个空配置对象。  
该类绝大部分方法都支持链式操作。

初始化参数：

- read_file：是否从默认 ini 文件中读取配置信息
- ini_path：ini 文件路径，为 None 则读取默认 ini 文件

## driver_path

此属性返回 chromedriver 文件路径。

## chrome_path

此属性返回 Chrome 浏览器启动文件路径，即 binary_location。  
为空时程序会根据注册表或系统路径查找。

## set_paths()

该方法用于设置浏览器用到的几种路径信息。

参数：

- driver_path：chromedriver.exe 路径
- chrome_path：chrome.exe 路径
- local_port：本地端口号
- debugger_address：调试浏览器地址，会覆盖 local_port 设置，例：127.0.0.1:9222
- download_path：下载文件路径
- user_data_path：用户数据路径
- cache_path：缓存路径

返回：None

## save()

此方法用于保存当前配置对象的信息到配置文件。

参数：

- path：配置文件的路径，传入 None 保存到当前读取的配置文件，传入 'default' 保存到默认 ini 文件

返回：配置文件绝对路径

## remove_argument()

此方法用于移除一个 argument 项。

参数：

- value：设置项名称，带有值的设置项传入设置名称即可

返回：当前对象

## remove_experimental_option()

此方法用于移除一个实验设置，传入key值删除。

参数：

- key：实验设置的名称

返回：当前对象

## remove_all_extensions()

此方法用于移除所有插件。

参数：无

返回：当前对象

## set_argument()

此方法用于设置浏览器配置的 argument 属性。

参数：

- arg：属性名
- value：属性值，有值的属性传入值，没有的传入bool 类型表示开关

返回：当前对象

## set_timeouts()

此方法用于设置三种超时时间，selenium 4 以上版本有效。

参数：

- implicit：查找元素超时时间
- pageLoad：页面加载超时时间
- script：脚本运行超时时间

返回：当前对象

## set_headless()

该方法用于设置是否已无头模式启动浏览器。

参数：

- on_off：bool 类型，表示开或关

返回：None

## set_no_imgs()

该方法用于设置是否禁止加载图片。

参数：

- on_off：bool 类型，表示开或关

返回：None

## set_mute()

该方法用于设置是否静音。

参数：

- on_off：bool 类型，表示开或关

返回：None

## set_proxy()

该方法用于设置代理。

参数：

- proxy: 代理网址和端口，如 127.0.0.1:1080

返回：None

## set_user_agent()

该方法用于设置 user agent。

参数：

- user_agent：user agent文本

返回：None

## as_dict()

该方法以 dict 方式返回所有配置信息。

参数：无

返回：配置信息

# 简单示例

```python
from DrissionPage import MixPage
from DrissionPage.config import DriverOptions

# 创建配置对象（默认从 ini 文件中读取配置）
do = DriverOptions()
# 设置不加载图片、静音
do.set_no_imgs(True).set_mute(True)

# 以该配置创建页面对象
page = MixPage(driver_options=do)
```

