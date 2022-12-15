目前只在 Windows 系统 Chrome 浏览器上进行过完整功能测试，如要使用其它系统或浏览器，在启动和设置方面可能遇到问题。  
这个时候可使用 selenium 原生方法创建 driver，然后用 Drission 对象接收即可。

!> **注意：** <br>本库所有功能暂时只在 Chrome 上做了完整测试。

```python
from selenium import webdriver
from DrissionPage import Drission, MixPage

# 用 selenium 原生代码创建 WebDriver 对象
driver = webdriver.Firefox()
# 把 WebDriver 对象传入 Drission 对象
dr = Drission(driver_or_options=driver)

page = MixPage(drission=dr)
page.get('https://www.baidu.com')
```