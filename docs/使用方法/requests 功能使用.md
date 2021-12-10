# 连接参数

除了在创建时传入配置信息及连接参数，如有必要，s 模式下也可在每次访问网址时设置连接参数。

```python
headers = {'User-Agent': '......', }
cookies = {'name': 'value', }
proxies = {'http': '127.0.0.1:1080', 'https': '127.0.0.1:1080'}
page.get(url, headers=headers, cookies=cookies, proxies=proxies)
```

Tips：

- 如果连接参数内没有指定，s 模式会根据当前域名自动填写 Host 和 Referer 属性
- 在创建 MixPage 时传入的 Session 配置是全局有效的

# Response 对象

requests 获取到的 Response 对象存放在 page.response，可直接使用。如：

```python
print(page.response.status_code)
print(page.response.headers)
```