MixPage 支持获取和设置`cookies`，具体使用方法如下：

```python
# 以字典形式返回 cookies，只会返回当前域名可用的 cookies
page.cookies  

# 以列表形式返回当前域名可用 cookies，每个 cookie 包含所有详细信息
page.get_cookies(as_dict=False)  

# 以列表形式返回所有 cookies，只有 s 模式有效
page.get_cookies(all_domains=True)  

# 设置 cookies，可传入 RequestsCookieJar, list, tuple, str, dict
page.set_cookies(cookies)  
```

?> **Tips:**  <br>d 模式设置`cookies`后要刷新页面才能看到效果。  <br>s 模式可在 ini 文件、`SessionOptions`、配置字典中设置`cookies`，在`MixPage`初始化时即可传入，d 模式只能用`set_cookies()`函数设置。
