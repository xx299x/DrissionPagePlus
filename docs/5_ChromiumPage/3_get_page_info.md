成功访问网页后，可使用`ChromiumPage`自身属性和方法获取页面信息。

操控浏览器除了`ChromiumPage`，还有`ChromiumTab`和`ChromiumFrame`两种页面对象分别对应于标签页对象和`<iframe>`元素对象，后面会有单独章节介绍。

# ✔️ 运行状态信息

## 📍 `url`

此属性返回当前访问的 url。

**返回类型：**`str`

---

## 📍 `address`

此属性返回当前对象控制的页面地址和端口。

**返回类型：**`str`

```python
print(page.address)
```

**输出：**

```
127.0.0.1:9222
```

---

## 📍 `tab_id`

**返回类型：**`str`

此属性返回当前标签页的 id。

---

## 📍 `process_id`

此属性返回浏览器进程 id。

**返回类型：**`int`、`None`

---

## 📍 `is_loading`

此属性返回页面是否正在加载状态。

**返回类型：**`bool`

---

## 📍 `ready_state`

此属性返回页面当前加载状态，有 3 种：

- `'loading'`：表示文档还在加载中

- `'interactive'`：DOM 已加载，但资源未加载完成

- `'complete'`：所有内容已完成加载

**返回类型：**`str`

---

## 📍 `url_available`

此属性以布尔值返回当前链接是否可用。

**返回类型：**`bool`

---

# ✔️ 窗口及页面信息

## 📍 `size`

此属性以`tuple`返回页面尺寸，格式：(宽, 高)。

**返回类型：**`Tuple[int, int]`

---

## 📍 `tabs_count`

此属性返回当前浏览器标签页数量。

**返回类型：**`int`

---

## 📍 `tabs`

此属性以列表形式返回当前浏览器所有标签页 id。

**返回类型：**`List[str]`

---

## 📍 `html`

此属性返回当前页面 html 文本。

**返回类型：**`str`

---

## 📍 `json`

此属性把请求内容解析成 json。

假如用浏览器访问会返回 json 数据的 url，浏览器会把 json 数据显示出来，这个参数可以把这些数据转换为`dict`格式。

**返回类型：**`dict`

---

## 📍 `title`

此属性返回当前页面`title`文本。

**返回类型：**`str`

---

# ✔️ 配置参数信息

## 📍 `timeout`

此属性为整体默认超时时间，包括元素查找、点击、处理提示框、列表选择等须要用到超时设置的地方，都以这个数据为默认值。  
默认为 10，可对其赋值。

**返回类型：**`int`、`float`

```python
# 创建页面对象时指定
page = ChromiumPage(timeout=5)

# 修改 timeout
page.timeout = 20
```

---

## 📍 `timeouts`

此属性以字典方式返回三种超时时间。

- `'implicit'`：与`timeout`属性是同一个值

- `'page_load'`：用于等待页面加载

- `'script'`：用于等待脚本执行

**返回类型：**`dict`

```python
print(page.timeouts)
```

**输出：**

```
{'implicit': 10, 'pageLoad': 30.0, 'script': 30.0}
```

---

## 📍 `retry_times`

此属性为网络连接失败时的重试次数。默认为 3，可对其赋值。

**返回类型：**`int`

```python
# 修改重试次数
page.retry_times = 5
```

---

## 📍 `retry_interval`

此属性为网络连接失败时的重试等待间隔秒数。默认为 2，可对其赋值。

**返回类型：**`int`、`float`

```python
# 修改重试等待间隔时间
page.retry_interval = 1.5
```

---

## 📍 `page_load_strategy`

此属性返回页面加载策略，有 3 种：

- `'normal'`：等待页面所有资源完成加载

- `'eager'`：DOM 加载完成即停止

- `'none'`：页面完成连接即停止

**返回类型：**`str`

---

# ✔️ cookies 和缓存信息

## 📍 `cookies`

此属性以`dict`方式返回当前页面所使用的 cookies。

**返回类型：**`dict`

---

## 📍 `get_cookies()`

此方法获取 cookies 并以 cookie 组成的`list`形式返回。

| 参数名称      | 类型     | 默认值     | 说明                                      |
|:---------:|:------:|:-------:| --------------------------------------- |
| `as_dict` | `bool` | `False` | 是否以字典方式返回结果，为`False`返回 cookie 组成的`list` |

| 返回类型   | 说明            |
|:------:| ------------- |
| `dict` | cookies 字典    |
| `list` | cookies 组成的列表 |

**示例：**

```python
from DrissionPage import ChromiumPage

p = ChromiumPage()
p.get('http://www.baidu.com')

for i in p.get_cookies(as_dict=False):
    print(i)
```

**输出：**

```
{'domain': '.baidu.com', 'domain_specified': True, ......}
......
```

---

## 📍 `get_session_storage()`

此方法用于获取 sessionStorage 信息，可获取全部或单个项。

| 参数名称   | 类型    | 默认值    | 说明                         |
|:------:|:-----:|:------:| -------------------------- |
| `item` | `str` | `None` | 要获取的项目，为`None`则返回全部项目组成的字典 |

| 返回类型   | 说明                     |
|:------:| ---------------------- |
| `dict` | `item`参数为`None`时返回所有项目 |
| `str`  | 指定`item`时返回该项目内容       |

---

## 📍 `get_local_storage()`

此方法用于获取 localStorage 信息，可获取全部或单个项。

| 参数名称   | 类型    | 默认值    | 说明                         |
|:------:|:-----:|:------:| -------------------------- |
| `item` | `str` | `None` | 要获取的项目，为`None`则返回全部项目组成的字典 |

| 返回类型   | 说明                     |
|:------:| ---------------------- |
| `dict` | `item`参数为`None`时返回所有项目 |
| `str`  | 指定`item`时返回该项目内容       |

---

# ✔️ 内嵌对象

## 📍 `driver`

此属性返回当前页面对象使用的`ChromiumDriver`对象。

**返回类型：**`ChromiumDriver`

---

# ✔️ 页面截图

## 📍 `get_screenshot()`

此方法用于对页面进行截图，可对整个网页、可见网页、指定范围截图。对可视范围外截图需要 90 以上版本浏览器支持。

| 参数名称           | 类型                        | 默认值     | 说明                                                                                                        |
|:--------------:|:-------------------------:|:-------:| --------------------------------------------------------------------------------------------------------- |
| `path`         | `str`<br>`Path`<br>`None` | `None`  | 保存图片的完整路径，文件后缀可选`'jpg'`、`'jpeg'`、`'png'`、`'webp'`<br>为`None`时以 jpg 格式保存在当前文件夹                             |
| `as_bytes`     | `str`<br>`None`<br>`True` | `None`  | 是否已字节形式返回图片，可选`'jpg'`、`'jpeg'`、`'png'`、`'webp'`、`None`、`True`<br>不为`None`时`path`参数无效<br>为`True`时选用 jpg 格式 |
| `full_page`    | `bool`                    | `False` | 是否整页截图，为`True`截取整个网页，为`False`截取可视窗口                                                                       |
| `left_top`     | `Tuple[int, int]`         | `None`  | 截取范围左上角坐标                                                                                                 |
| `right_bottom` | `Tuple[int, int]`         | `None`  | 截取范围右下角坐标                                                                                                 |

| 返回类型    | 说明                         |
|:-------:| -------------------------- |
| `bytes` | `as_bytes`生效时返回图片字节        |
| `str`   | `as_bytes`为`None`时返回图片完整路径 |

```python
# 对整页截图并保存
page.get_screenshot(path='D:\\page.png', full_page=True)
```
