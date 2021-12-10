如果无须切换模式，可根据需要只使用 DriverPage 或 SessionPage，用法和 MixPage 一致。

```python
from DrissionPage.session_page import SessionPage
from DrissionPage.drission import Drission

session = Drission().session
page = SessionPage(session)  # 传入 Session 对象
page.get('http://www.baidu.com')
print(page.ele('@id:su').text)  # 输出：百度一下

driver = Drission().driver
page = DriverPage(driver)  # 传入 Driver 对象
page.get('http://www.baidu.com')
print(page.ele('@id:su').text)  # 输出：百度一下
```