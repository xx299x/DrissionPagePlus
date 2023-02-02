浏览器元素对应的对象是`ChromiumElement`和`ChromiumShadowRootElement`，本节介绍获取到元素对象后，如何获取其信息。

`ChromiumElement`拥有`SessionElement`所有属性，并有更多浏览器专属的信息。本节重点介绍如何获取浏览器专有的元素信息。

# ✔️ 与`SessionElement`共有信息

此处仅列出列表，具体用法请查看收发数据包部分的“获取元素信息”。

| 属性或方法        | 说明                               |
|:------------:| -------------------------------- |
| `html`       | 此属性返回元素的`outerHTML`文本            |
| `inner_html` | 此属性返回元素的`innerHTML`文本            |
| `tag`        | 此属性返回元素的标签名                      |
| `text`       | 此属性返回元素内所有文本组合成的字符串              |
| `raw_text`   | 此属性返回元素内原始文本                     |
| `texts()`    | 此方法返回元素内所有**直接**子节点的文本，包括元素和文本节点 |
| `comments`   | 此属性以列表形式返回元素内的注释                 |
| `attrs`      | 此属性以字典形式返回元素所有属性及值               |
| `attr()`     | 此方法返回元素某个`attribute`属性值          |
| `link`       | 此方法返回元素的 href 属性或 src 属性         |
| `page`       | 此属性返回元素所在的页面对象                   |
| `xpath`      | 此属性返回当前元素在页面中 xpath 的绝对路径        |
| `css_path`   | 此属性返回当前元素在页面中 css selector 的绝对路径 |

---

# ✔️ 获取大小和位置

## 📍 `size`

**类型：**`Tuple[int, int]`

此属性以元组形式返回元素的大小。

```python
size = ele.size
# 返回：(50, 50)
```

---

## 📍 `location`

**类型：**`Tuple[int, int]`

此属性以元组形式返回元素**左上角**在**整个页面**中的坐标。

```python
loc = ele.location
# 返回：(50, 50)
```

---

## 📍 `client_location`

**类型：**`Tuple[int, int]`

此属性以元组形式返回元素**左上角**在**当前视口**中的坐标。

```python
loc = ele.client_location
# 返回：(50, 50)
```

---

## 📍 `midpoint`

**类型：**`Tuple[int, int]`

此属性以元组形式返回元素**中点**在**整个页面**中的坐标。

```python
loc = ele.midpoint
# 返回：(55, 55)
```

---

## 📍 `client_midpoint`

**类型：**`Tuple[int, int]`

此属性以元组形式返回元素**中点**在**视口**中的坐标。

```python
loc = ele.client_midpoint
# 返回：(55, 55)
```

---

# ✔️ 获取属性和内容

## 📍 `pseudo_before`

**类型：**`str`

此属性以文本形式返回当前元素的`::before`伪元素内容。

```python
before_txt = ele.pseudo_before
```

---

## 📍 `pseudo_after`

**类型：**`str`

此属性以文本形式返回当前元素的`::after`伪元素内容。

```python
after_txt = ele.pseudo_after
```

---

## 📍 `style()`

该方法返回元素 css 样式属性值，可获取伪元素的属性。它有两个参数，`style`参数输入样式属性名称，`pseudo_ele`参数输入伪元素名称，省略则获取普通元素的 css 样式属性。

| 参数名称         | 类型    | 默认值  | 说明        |
|:------------:|:-----:|:----:| --------- |
| `style`      | `str` | 必填   | 样式名称      |
| `pseudo_ele` | `str` | `''` | 伪元素名称（如有） |

| 返回类型  | 说明    |
|:-----:| ----- |
| `str` | 样式属性值 |

**示例：**

```python
# 获取 css 属性的 color 值
prop = ele.style('color')

# 获取 after 伪元素的内容
prop = ele.style('content', 'after')
```

---

## 📍 `prop()`

此方法返回`property`属性值。它接收一个字符串参数，返回该参数的属性值。

| 参数名称   | 类型    | 默认值 | 说明   |
|:------:|:-----:|:---:| ---- |
| `prop` | `str` | 必填  | 属性名称 |

| 返回类型  | 说明  |
|:-----:| --- |
| `str` | 属性值 |

---

## 📍 `shadow_root`

**类型：**`ChromiumShadowRootElement`

此属性返回元素内的 shadow-root 对象，没有的返回`None`。

---

# ✔️ 获取状态信息

## 📍`is_in_viewport`

**类型：**`bool`

此属性以布尔值方式返回元素是否在视口中，以元素可以接受点击的点为判断。

---

## 📍`is_alive`

**类型：**`bool`

此属性以布尔值形式返回当前元素是否仍可用。用于判断 d 模式下是否因页面刷新而导致元素失效。

---

## 📍 `is_selected`

**类型：**`bool`

此属性以布尔值返回元素是否选中。

---

## 📍 `is_enabled`

**类型：**`bool`

此属性以布尔值返回元素是否可用。

---

## 📍 `is_displayed`

**类型：**`bool`

此属性以布尔值返回元素是否可见。

---

# ✔️ 保存和截图

保存功能是本库一个特色功能，可以直接读取浏览器缓存，无须依赖另外的 ui 库或重新下载就可以保存页面资源。

作为对比，selenium 无法自身实现图片另存，往往须要通过使用 ui 工具进行辅助，不仅效率和可靠性低，还占用键鼠资源。

## 📍 `get_src()`

此方法用于返回元素`src`属性所使用的资源。base64 的会转为`bytes`返回，其它的以`str`返回。无资源的返回`None`。

例如，可获取页面上图片字节数据，用于识别内容，或保存到文件。`<script>`标签也可获取 js 文本。

!> **注意：**<br>无法获取 Blob 内容。

**参数：** 无

| 返回类型   | 说明           |
|:------:| ------------ |
| `str`  | 资源字符串        |
| `None` | 无资源的返回`None` |

**示例：**

```python
img = page('tag:img')
src = img.get_src()
```

---

## 📍 `save()`

此方法用于保存`get_src()`方法获取到的资源到文件。

| 参数名称     | 类型                        | 默认值    | 说明                            |
|:--------:|:-------------------------:|:------:| ----------------------------- |
| `path`   | `str`<br>`Path`<br>`None` | `None` | 文件保存路径，为`None`时保存到当前文件夹       |
| `rename` | `str`<br>`None`           | `None` | 文件名称，须包含后缀，为`None`时从资源 url 获取 |

**返回：**`None`

**示例：**

```python
img = page('tag:img')
img.save('D:\\img.png')
```

---

## 📍 `get_screenshot()`

此方法用于对元素进行截图。若截图时元素在视口外，须 90 以上版本内核支持。

**参数：**

- `path`：图片完整路径，后缀可选`'jpg'`、`'jpeg'`、`'png'`、`'webp'`

- `as_bytes`：是否已字节形式返回图片，可选`'jpg'`、`'jpeg'`、`'png'`、`'webp'`。为`True`时以`'png'`输出。生效时`path`参数无效。

**返回：** 图片完整路径或字节文本

| 参数名称       | 类型                        | 默认值    | 说明                                                                                                        |
|:----------:|:-------------------------:|:------:| --------------------------------------------------------------------------------------------------------- |
| `path`     | `str`<br>`Path`<br>`None` | `None` | 保存图片的完整路径，文件后缀可选`'jpg'`、`'jpeg'`、`'png'`、`'webp'`<br>为`None`时以 jpg 格式保存在当前文件夹                             |
| `as_bytes` | `str`<br>`None`<br>`True` | `None` | 是否已字节形式返回图片，可选`'jpg'`、`'jpeg'`、`'png'`、`'webp'`、`None`、`True`<br>不为`None`时`path`参数无效<br>为`True`时选用 jpg 格式 |

| 返回类型    | 说明                         |
|:-------:| -------------------------- |
| `bytes` | `as_bytes`生效时返回图片字节        |
| `str`   | `as_bytes`为`None`时返回图片完整路径 |

**示例：**

```python
img = page('tag:img')
img.get_screenshot()
bytes_str = img.get_screenshot(as_bytes='png')  # 返回截图二进制文本
```

---

# ✔️ `ChromiumShadowRootElement`属性

本库把 shadow dom 的`root`看作一个元素处理，可以获取属性，也可以执行其下级的查找，使用逻辑与`ChromiumElement`一致，但属性较之少，有如下这些：

## 📍 `tag`

**类型：**`str`

此属性返回元素标签名，即`'shadow-root'`。

---

## 📍 `html`

**类型：**`str`

此属性返回`shadow_root`的 html 文本，由`<shadow_root></shadow_root>` 标签包裹。

---

## 📍 `inner_html`

**类型：**`str`

此属性返回`shadow_root`内部的 html 文本。

---

## 📍 `page`

**类型：**`ChromiumPage`、`ChromiumTab`、`ChromiumFrame`、`WebPage`

此属性返回元素所在页面对象。

---

## 📍 `parent_ele`

**类型：**`ChromiumElement`

此属性返回所依附的普通元素对象。

---

## 📍 `is_enabled`

**类型：**`bool`

与`ChromiumElement`一致。

---

## 📍 `is_alive`

**类型：**`bool`

与`ChromiumElement`一致。
