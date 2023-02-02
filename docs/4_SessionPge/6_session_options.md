本节介绍`SessionPage`的启动配置。

我们用`SessionOptions`对象管理`Session`对象初始配置。

!> **注意：** <br>`SessionOptions`仅用于管理启动配置，程序启动后再修改无效。

# ✔️ 创建对象

## 📍 导入

```python
from DrissionPage import SessionOptions
```

---

## 📍 `SessionOptions`

`SessionOptions`对象用于管理`Session`对象的初始化配置。可从配置文件中读取配置来进行初始化。

| 初始化参数       | 类型              | 默认值    | 说明                                 |
|:-----------:|:---------------:|:------:| ---------------------------------- |
| `read_file` | `bool`          | `True` | 是否从 ini 文件中读取配置信息，为`False`则用默认配置创建 |
| `ini_path`  | `Path`<br>`str` | `None` | 指定 ini 文件路径，为`None`则读取内置 ini 文件    |

创建配置对象：

```python
from DrissionPage import SessionOptions

so = SessionOptions()
```

默认情况下，`SessionOptions`对象会从 ini 文件中读取配置信息，当指定`read_file`参数为`False`时，则以默认配置创建。

---

# ✔️ 使用方法

创建配置对象后，可调整配置内容，然后在页面对象创建时以参数形式把配置对象传递进去。

```python
from DrissionPage import SessionPage, SessionOptions

# 创建配置对象（默认从 ini 文件中读取配置）
so = SessionOptions()
# 设置代理
so.set_proxies('http://localhost:1080')
# 设置 cookies
cookies = ['key1=val1; domain=xxxx', 'key2=val2; domain=xxxx']
so.set_cookies(cookies)

# 以该配置创建页面对象
page = SessionPage(session_or_options=so)
```

---

# ✔️ 用于设置的方法

## 📍 `set_headers()`

该方法用于设置整个 headers 参数，传入值会覆盖原来的 headers。

| 参数名称     | 类型     | 默认值 | 说明             |
|:--------:|:------:|:---:| -------------- |
| `headers | `dict` | 必填  | 完整的 headers 字典 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

**示例：**

```python
so.set_headers = {'user-agent': 'Mozilla/5.0 (Macint...', 'connection': 'keep-alive' ...}
```

---

## 📍 `set_a_header()`

该方法用于设置`headers`中的一个项。

| 参数名称    | 类型    | 默认值 | 说明   |
|:-------:|:-----:|:---:| ---- |
| `attr`  | `str` | 必填  | 设置名称 |
| `value` | `str` | 必填  | 设置值  |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

**示例：**

```python
so.set_a_header('accept', 'text/html')
so.set_a_header('Accept-Charset', 'GB2312')
```

**输出：**

```
{'accept': 'text/html', 'accept-charset': 'GB2312'}
```

---

## 📍 `remove_a_header()`

此方法用于从`headers`中移除一个设置项。

| 参数名称   | 类型    | 默认值 | 说明     |
|:------:|:-----:|:---:| ------ |
| `attr` | `str` | 必填  | 要删除的设置 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

**示例：**

```python
so.remove_a_header('accept')
```

---

## 📍 `set_cookies()`

此方法用于设置 cookies 信息，每次设置会覆盖之前所有 cookies 信息。

| 参数名称      | 类型                                                          | 默认值 | 说明      |
|:---------:|:-----------------------------------------------------------:|:---:| ------- |
| `cookies` | `RequestsCookieJar`<br>`list`<br>`tuple`<br>`str`<br>`dict` | 必填  | cookies |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

**示例：**

```python
cookies = ['key1=val1; domain=xxxx', 'key2=val2; domain=xxxx']
so.set_cookies(cookies)
```

---

## 📍 `set_timeout()`

此方法用于设置连接超时属性。

| 参数名称     | 类型               | 默认值 | 说明     |
|:--------:|:----------------:|:---:| ------ |
| `second` | `int`<br>`float` | 必填  | 连接等待秒数 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

---

## 📍 `set_proxies()`

此方法用于设置代理信息。

| 参数名称    | 类型    | 默认值    | 说明                              |
|:-------:|:-----:|:------:| ------------------------------- |
| `http`  | `str` | 必填     | http 代理地址                       |
| `https` | `str` | `None` | https 代理地址，为`None`时使用`http`参数的值 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

**示例：**

```python
so.set_proxies('http://127.0.0.1:1080')
```

---

## 📍 `set_paths()`

此方法用于设置路径信息，目前只有`download_path`一种。

| 参数名称            | 类型              | 默认值 | 说明       |
|:---------------:|:---------------:|:---:| -------- |
| `download_path` | `str`<br>`Path` | 必填  | 默认下载保存路径 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

---

## 📍 `set_auth()`

此方法用于设置认证元组信息。

| 参数名称   | 类型                         | 默认值 | 说明      |
|:------:|:--------------------------:|:---:| ------- |
| `auth` | `tuple`<br>`HTTPBasicAuth` | 必填  | 认证元组或对象 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

---

## 📍 `set_hooks()`

此方法用于设置回调方法。

| 参数名称    | 类型     | 默认值 | 说明   |
|:-------:|:------:|:---:| ---- |
| `hooks` | `dict` | 必填  | 回调方法 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

---

## 📍 `set_params()`

此方法用于设置查询参数。

| 参数名称     | 类型     | 默认值 | 说明     |
|:--------:|:------:|:---:| ------ |
| `params` | `dict` | 必填  | 查询参数字典 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

---

## 📍 `set_cert()`

此方法用于设置 SSL 客户端证书文件的路径（.pem格式），或 ('cert', 'key') 元组。

| 参数名称   | 类型               | 默认值 | 说明      |
|:------:|:----------------:|:---:| ------- |
| `cert` | `str`<br>`tuple` | 必填  | 证书路径或元组 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

---

## 📍 `set_verify()`

此方法用于设置是否验证SSL证书。

| 参数名称     | 类型     | 默认值 | 说明          |
|:--------:|:------:|:---:| ----------- |
| `on_off` | `bool` | 必填  | `bool`表示开或关 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

---

## 📍 `add_adapter()`

此方法用于添加适配器。

| 参数名称      | 类型            | 默认值 | 说明        |
|:---------:|:-------------:|:---:| --------- |
| `url`     | `str`         | 必填  | 适配器对应 url |
| `adapter` | `HTTPAdapter` | 必填  | 适配器对象     |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

---

## 📍 `set_stream()`

此方法用于设置是否使用流式响应内容。

| 参数名称     | 类型     | 默认值 | 说明          |
|:--------:|:------:|:---:| ----------- |
| `on_off` | `bool` | 必填  | `bool`表示开或关 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

---

## 📍 `set_trust_env()`

此方法用于设置是否信任环境。

| 参数名称     | 类型     | 默认值 | 说明          |
|:--------:|:------:|:---:| ----------- |
| `on_off` | `bool` | 必填  | `bool`表示开或关 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

---

## 📍 `set_max_redirects()`

此方法用于设置最大重定向次数。

| 参数名称    | 类型    | 默认值 | 说明      |
|:-------:|:-----:|:---:| ------- |
| `times` | `int` | 必填  | 最大重定向次数 |

| 返回类型             | 说明     |
| ---------------- | ------ |
| `SessionOptions` | 配置对象本身 |

---

# ✔️ 保存设置到文件

您可以把不同的配置保存到各自的 ini 文件，以便适应不同的场景。

!>**注意：**<br>`hooks`和`adapters`配置是不会保存到文件中的。

## 📍 `save()`

此方法用于保存配置项到一个 ini 文件。

| 参数名称   | 类型              | 默认值    | 说明                              |
|:------:|:---------------:|:------:| ------------------------------- |
| `path` | `str`<br>`Path` | `None` | ini 文件的路径， 传入`None`保存到当前读取的配置文件 |

| 返回类型  | 说明             |
| ----- | -------------- |
| `str` | 保存的 ini 文件绝对路径 |

**示例：**

```python
# 保存当前读取的ini文件
so.save()

# 把当前配置保存到指定的路径
so.save(path=r'D:\tmp\settings.ini')
```

---

## 📍 `save_to_default()`

此方法用于保存配置项到固定的默认 ini 文件。默认 ini 文件是指随 DrissionPage 内置的那个。

**参数：** 无

| 返回类型  | 说明             |
| ----- | -------------- |
| `str` | 保存的 ini 文件绝对路径 |

**示例：**

```python
so.save_to_default()
```

---

# ✔️ `SessionOptions`属性

## 📍 `headers`

该属性返回 headers 设置信息。

**类型：**`dict`

---

## 📍 `cookies`

此属性以`list`方式返回 cookies 设置信息。  

**类型：**`list`

---

## 📍 `proxies`

此属性返回代理信息。  

**类型：**`dict`
**格式：**{'http': 'http://xx.xx.xx.xx:xxxx', 'https': 'http://xx.xx.xx.xx:xxxx'}

---

## 📍 `auth`

此属性返回认证设置。

**类型：**`tuple`、`HTTPBasicAuth`

---

## 📍 `hooks`

此属性返回回调方法设置。

**类型：**`dict`

---

## 📍 `params`

此属性返回查询参数设置。

**类型：**`dict`

---

## 📍 `verify`

此属性返回是否验证 SSL 证书设置。

**类型：**`bool`

---

## 📍 `cert`

此属性返回 SSL 证书设置。

**类型：**`str`、`tuple`

---

## 📍 `adapters`

此属性返回适配器设置。

**类型：**`List[HTTPAdapter]`

---

## 📍 `stream`

此属性返回是否使用流式响应设置。

**类型：**`bool`

---

## 📍 `trust_env`

此属性返回是否信任环境设置。

**类型：**`bool`

---

## 📍 `max_redirects`

此属性返回`max_redirects`设置。

**类型：**`int`

---

## 📍 `timeout`

此属性返回连接超时设置。

**类型：**`int`、`float`

---

## 📍 `download_path`

此属性返回默认下载路径设置。

**类型：**`str`
