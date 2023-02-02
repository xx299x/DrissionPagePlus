本节介绍`Webpage`独有的功能。

`WebPage`是`ChromiumPage`和`SessionPage`的集成，因此拥有这两者全部功能。这些功能具体查看相关章节，这里只介绍`WebPage`独有的功能。

# ✔️ cookies 处理

## 📍 `cookies_to_session()`

此方法把浏览器的 cookies 复制到`session`对象。

| 参数名称              | 类型     | 默认值    | 说明                 |
|:-----------------:|:------:|:------:| ------------------ |
| `copy_user_agent` | `bool` | `True` | 是否复制 user agent 信息 |

**返回：**`None`

---

## 📍 `cookies_to_session()`

此方法把`session`对象的 cookies 复制到浏览器。

**参数：** 无

**返回：**`None`

---

# ✔️ 关闭对象

## 📍 `close_driver()`

此方法关闭内置`ChromiumDriver`对象及浏览器，并切换到 s 模式。

**参数：** 无

**返回：**`None`

---

## 📍 `close_driver()`

此方法关闭内置`Session`对象及浏览器，并切换到 d 模式。

**参数：** 无

**返回：**`None`

---

## 📍 `quit()`

此方法彻底关闭内置的`Session`对象和`ChromiumDriver`对象，并关闭浏览器（如已打开）。

**参数：** 无

**返回：**`None`
