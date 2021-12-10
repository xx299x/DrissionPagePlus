> 创建驱动器的步骤不是必须，若想快速上手，可跳过本节。MixPage 会自动创建该对象。

Drission 对象用于管理 driver 和 session 对象。在多个页面协同工作时，Drission 对象用于传递驱动器，使多个页面类可控制同一个浏览器或 Session 对象。  
可直接读取 ini 文件配置信息创建，也可以在初始化时传入配置信息。

```python
# 由默认 ini 文件创建
drission = Drission()  

# 由其它 ini 文件创建
drission = Drission(ini_path='D:\\settings.ini')  

# 不从 ini 文件创建
drission = Drission(read_file=False)
```

若要手动传入配置（不使用 ini 文件）：

```python
from DrissionPage.config import DriverOptions

# 创建 driver 配置对象，read_file = False 表示不读取 ini 文件
do = DriverOptions(read_file=False)  

# 设置路径，若已在系统变量设置，可忽略
do.set_paths(chrome_path='D:\\chrome\\chrome.exe',
             driver_path='D:\\chrome\\chromedriver.exe')  

# 用于 s 模式的设置
session_options = {'headers': {'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6)'}}

# 代理设置，可选
proxy = {'http': '127.0.0.1:1080', 'https': '127.0.0.1:1080'}

# 传入配置，driver_options 和 session_options 都是可选的，须要使用对应模式才须要传入
drission = Drission(driver_options, session_options, proxy=proxy)  


# 关闭浏览器，debug 模式下须要显式调用这句，浏览器才能关掉
drission.kill_browser()
```

DriverOptions 和 SessionOptions 用法详见下文。