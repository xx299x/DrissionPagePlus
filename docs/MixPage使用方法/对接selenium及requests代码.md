DrissionPage 代码可无缝拼接 selenium 及 requests 代码。既可直接使用 selenium 的`WebDriver`对象，也可导出自身的`WebDriver`给 selenium 代码使用。requests 的
`Session`对象也可直接传递。便于已有项目的迁移。

# ✔️ selenium 转 DrissionPage

```python
from selenium import webdriver

driver = webdriver.Chrome()
driver.get('https://www.baidu.com')

# 把 driver 传递给 Drission，创建 MixPage 对象
drission = Drission(driver_or_options=driver)
page = MixPage(drission=drission)  

# 打印结果：百度一下，你就知道
print(page.title)  
```

# ✔️ DrissionPage 转 selenium

```python
page = MixPage()
page.get('https://www.baidu.com')

# 从 MixPage 对象中获取 WebDriver 对象
driver = page.driver  
# 打印结果：百度一下，你就知道
print(driver.title)  
# 使用 selenium 原生功能
element = driver.find_element(By.XPATH, '//div')  
```

# ✔️ requests 转 DrissionPage

```python
from requests import Session

session = requets.Session()

# 把 session 传递给 Drission，创建 MixPage 对象
drission = Drission(session_or_options=session)
page = MixPage('s', drission=drission)

page.get('https://www.baidu.com')
```

# ✔️ DrissionPage 转 requests

```python
from DrissionPage import MixPage

page = MixPage('s')

# 提取 MixPage 中的 Session 对象
session = page.session

response = session.get('https://www.baidu.com')
```