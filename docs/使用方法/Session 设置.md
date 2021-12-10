# SessionOPtions 对象

SessionOptions 对象用于管理 Session 的配置信息。它创建时默认读取默认 ini 文件配置信息，也可手动设置所需信息。

可配置的属性：

headers、cookies、auth、proxies、hooks、params、verify、cert、adapters、stream、trust_env、max_redirects。

**Tips:** cookies 可接收 dict、list、tuple、str、RequestsCookieJar 等格式的信息。

# 使用方法

```python
so = SessionOptions()  # 读取默认 ini 文件创建 SessionOptions 对象
so = SessionOptions('D:\\settings.ini')  # 读取指定 ini 文件创建 SessionOptions 对象
so = SessionOptions(read_file=False)  # 不读取 ini 文件，创建空的 SessionOptions 对象

so.cookies = ['key1=val1; domain=xxxx', 'key2=val2; domain=xxxx']  # 设置 cookies
so.headers = {'User-Agent': 'xxxx', 'Accept-Charset': 'xxxx'}
so.set_a_header('Connection', 'keep-alive')

drission = Drission(session_options=so)  # 用配置对象创建 Drission 对象
page = MixPage(session_options=so)  # 用配置对象创建 MixPage 对象

so.save()  # 保存当前打开的 ini 文件
so.save('D:\\settings.ini')  # 保存到指定的 ini 文件
so.save('default')  # 保存当前设置到默认 ini 文件
```