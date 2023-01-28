`SessionOptions`对象用于管理`Session`对象连接配置。  
其使用逻辑与`ChromiumOptions`相似。

!> **注意：** <br>`SessionOptions`仅用于管理启动配置，程序启动后再修改无效。

# ✔️ `SessionOptions`类

`SessionOptions`对象创建时默认读取默认 ini 文件配置信息，也可手动设置所需信息。  
该类的方法支持链式操作。

**初始化参数：**

- `read_file`：是否从默认 ini 文件中读取配置信息
- `ini_path`：ini 文件路径，为`None`则读取默认 ini 文件

## 📍 `headers`

该属性返回`headers`设置信息，可传入字典赋值。

## 📍 `set_headers()`

该方法与`headers`参数赋值功能一致。

**参数：**

- `headers`：`headers`字典

**返回：** 当前对象

## 📍 `set_a_header()`

该方法用于设置`headers`中的一个项。

**参数：**

- `attr`：设置项名称
- `value`：设置值

**返回：** 当前对象

```python
so = SessionOptions()
so.set_a_header('accept', 'text/html')
so.set_a_header('Accept-Charset', 'GB2312')

print(so.headers)
```

输出：

```
{'accept': 'text/html', 'accept-charset': 'GB2312'}
```

## 📍 `remove_a_header()`

此方法用于从`headers`中移除一个设置项。

**参数：**

- `attr`：要删除的设置名称

**返回：** 当前对象

## 📍 `set_timeout()`

此方法用于设置超时属性。

**参数：**

- `second`：秒数

**返回：** 当前对象

## 📍 `cookies`

此属性返回`cookies`设置信息，可赋值。  
可接收`dict`、`list`、`tuple`、`str`、`RequestsCookieJar`等格式的信息。

## 📍 `proxies`

此属性返回代理信息，可赋值。可传入字典类型。  
**格式：**{'http': 'http://xx.xx.xx.xx:xxxx', 'https': 'http://xx.xx.xx.xx:xxxx'}

## 📍 `set_proxies()`

此方法与`proxies`属性赋值功能一致。

**参数：**

- `proxies`：`dict`格式的代理参数

**返回：** 当前对象

## 📍 `auth`

此属性用于返回和设置`auth`参数，接收`tuple`类型参数。

## 📍 `hooks`

此属性用于返回和设置`hooks`参数，接收`dict`类型参数。

## 📍 `params`

此属性用于返回和设置`params`参数，接收`dict`类型参数。

## 📍 `verify`

此属性用于返回和设置`verify`参数，接收`bool`类型参数。

## 📍 `cert`

此属性用于返回和设置`cert`参数，接收`str`或`tuple`类型参数。

## 📍 `adapters`

此属性用于返回和设置`adapters`参数。

## 📍 `stream`

此属性用于返回和设置`stream`参数，接收`bool`类型参数。

## 📍 `trust_env`

此属性用于返回和设置`trust_env`参数，接收`bool`类型参数。

## 📍 `max_redirects`

此属性用于返回和设置`max_redirects`参数，接收`int`类型参数。

## 📍 `save()`

此方法用于保存当前配置对象的信息到配置文件。

**参数：**

- `path`：配置文件的路径，默认保存到当前读取的配置文件，传入`'default'`保存到默认 ini 文件

**返回：** 配置文件绝对路径

## 📍 `save_to_default()`

此方法用于保存当前配置对象的信息到默认 ini 文件。

**参数：** 无

**返回：** 配置文件绝对路径

## 📍 `as_dict()`

该方法以`dict`方式返回所有配置信息。

**参数：** 无

**返回：** 配置信息

# ✔️ 简单示例

```python
from DrissionPage import WebPage, SessionOptions

# 创建配置对象（默认从 ini 文件中读取配置）
so = SessionOptions()
# 设置 cookies
so.cookies = ['key1=val1; domain=xxxx', 'key2=val2; domain=xxxx']
# 设置 headers 一个参数
so.set_a_header('Connection', 'keep-alive')

# 以该配置创建页面对象
page = WenPage(mode='s', session_or_options=so)
```
