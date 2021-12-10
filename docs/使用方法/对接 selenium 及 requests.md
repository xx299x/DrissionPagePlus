DrissionPage 代码可与 selenium 及 requests 代码无缝拼接。既可直接使用 selenium 的 WebDriver 对象，也可导出自身的 WebDriver 给 selenium 代码使用。requests 的
Session 对象也可直接传递。使已有项目的迁移非常方便。

# selenium 转 DrissionPage

```python
driver = webdriver.Chrome()
driver.get('https://www.baidu.com')

page = MixPage(Drission(driver))  # 把 driver 传递给 Drission，创建 MixPage 对象
print(page.title)  # 打印结果：百度一下，你就知道
```

# DrissionPage 转 selenium

```python
page = MixPage()
page.get('https://www.baidu.com')

driver = page.driver  # 从 MixPage 对象中获取 WebDriver 对象
print(driver.title)  # 打印结果：百度一下，你就知道
element = driver.find_element_by_xpath('//div')  # 使用 selenium 原生功能
```

# requests 转 DrissionPage

``` python
session = requets.Session()
drission = Drission(session_or_options=session)
page = MixPage(drission, mode='s')

page.get('https://www.baidu.com')
```

# DrissionPage 转 requests

```python
page = MixPage('s')
session = page.session

response = session.get('https://www.baidu.com')
```