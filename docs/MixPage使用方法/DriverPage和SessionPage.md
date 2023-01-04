如果无须切换模式，可根据需要只使用 DriverPage 或 SessionPage。  
分别对应 d 和 s 模式，用法和 MixPage 相似。

# ✔️ SessionPage

```python
from DrissionPage.session_page import SessionPage
from DrissionPage import Drission

# 用配置文件信息创建 Drission，获取其中 Session 对象
session = Drission().session
# 传入 Session 对象创建页面对象
page = SessionPage(session)

# 使用页面对象
page.get('http://www.baidu.com')
# 输出：百度一下
print(page.ele('#su').text)  
```

# ✔️ DriverPage

```python
from DrissionPage.driver_page import DriverPage
from DrissionPage import Drission

# 用配置文件信息创建 Drission，获取其中 WebDriver 对象
driver = Drission().driver
# 传入 WebDriver 对象创建页面对象
page = DriverPage(driver)

# 使用页面对象
page.get('http://www.baidu.com')
# 输出：百度一下
print(page.ele('#su').text)  
```
