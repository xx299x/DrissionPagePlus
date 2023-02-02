本节介绍对浏览器标签页的管理及使用技巧。

与 selenium 不同，DrissionPage 能够用多个标签页对象同时操作多个标签页，而无须切入切出。并且，标签页无须在激活状态也可以控制。因此能够实现一些非常灵活的使用方式。

比如，多线程分别独立控制标签页，或一个总标签页，控制多个从标签页，或者配合插件实现浏览器随时更换代理等。

`ChromiumPage`和`WebPage`对象为浏览器标签页总管，可以控制标签页的增删。`ChromiumTab`对象为独立的标签页对象，可控制每个标签页内的操作。

# ✔️ 标签页总览

## 📍 `tabs_count`

**类型：**`int`

此属性返回标签页数量。

```python
print(page.tabs_count)
```

**输出：**

```console
2
```

---

## 📍 `tabs`

**类型：**`List[str]`

此属性以`list`方式返回所有标签页 id。

```python
print(page.tabs)
```

**输出：**

```console
['0B300BEA6F1F1F4D5DE406872B79B1AD', 'B838E91F38121B32940B47E8AC59D015']
```

---

# ✔️ 新建标签页

## 📍 `new_tab()`

该方法用于新建一个标签页，该标签页在最后面。

| 参数名称        | 类型              | 默认值    | 说明                   |
|:-----------:|:---------------:|:------:| -------------------- |
| `url`       | `str`<br>`None` | `None` | 新标签页访问的网址，不传入则新建空标签页 |
| `switch_to` | `bool`          | `True` | 新建标签页后是否把焦点移过去       |

**返回：**`None`

**示例：**

```python
page.new_tab(url='https://www.baidu.com')
```

---

# ✔️ 关闭标签页

## 📍 `close_tabs()`

此方法用于关闭指定的标签页，可关闭多个。默认关闭当前的。

如果被关闭的标签页包含当前页，会切换到剩下的第一个页面，但未必是视觉上第一个。

| 参数名称      | 类型                                             | 默认值    | 说明                                       |
|:---------:|:----------------------------------------------:|:------:| ---------------------------------------- |
| `tab_ids` | `str`<br>`None`<br>`List[str]`<br>`Tuple[str]` | `None` | 要处理的标签页 id，可传入 id 组成的列表或元组，为`None`时处理当前页 |
| `others`  | `bool`                                         | `True` | 是否关闭指定标签页之外的                             |

**返回：**`None`

**示例：**

```python
# 关闭当前标签页
page.close_tabs()

# 关闭第1、3个标签页
tabs = page.tabs
page.close_tabs(tab_ids=(tabs[0], tabs[2]))
```

---

## 📍 `close_other_tabs()`

此方法用于关闭传入的标签页以外标签页，默认保留当前页。可传入多个。

如果被关闭的标签页包含当前页，会切换到剩下的第一个页面，但未必是视觉上第一个。

| 参数名称      | 类型                                             | 默认值    | 说明                                       |
|:---------:|:----------------------------------------------:|:------:| ---------------------------------------- |
| `tab_ids` | `str`<br>`None`<br>`List[str]`<br>`Tuple[str]` | `None` | 要处理的标签页 id，可传入 id 组成的列表或元组，为`None`时处理当前页 |

**返回：**`None`

**示例：**

```python
# 关闭除当前标签页外的所有标签页
page.close_other_tabs()

# 关闭除第一个以外的标签页
page.close_other_tabs(page.tab[0])

# 关闭除指定id以外的标签页
reserve_list = ('0B300BEA6F...', 'B838E91...')
page.close_other_tabs(reserve_list)
```

---

# ✔️ 切换标签页

## 📍 `main_tab`

**类型：**`str`

日常使用时，经常会用一个标签页作为主标签页，产生众多临时标签页去进行操作。因此我们可以为每个`ChromiumPage`或`WebPage`对象设置一个标签页为主标签页，方便随时切换。

默认接管浏览器时活动标签页则为主标签页。

**示例：**

```python
print(page.main_tab)
```

**输出：**

```console
'0B300BEA6F1F1F4D5DE406872B79B1AD'
```

---

## 📍 `lastest_tab`

**类型：**`str`

此属性返回最新的标签页。最新的标签页是指最新出现或最新被激活的标签页。

**示例：**

```python
# 打开了一个标签页
ele.click()
# 切换到最新打开的标签页
page.to_tab(page.lastest_tab)
```

---

## 📍 `set_main_tab()`

此方法用于设置某个标签页为主标签页。

| 参数名称     | 类型              | 默认值    | 说明                     |
|:--------:|:---------------:|:------:| ---------------------- |
| `tab_id` | `str`<br>`None` | `None` | 要设置的标签页 id，默认设置当前标签页为主 |

**返回：**`None`

**示例：**

```python
# 指定一个标签页为主标签页
page.set_main_tab(tab_id='0B300BEA6F1F1F4D5DE406872B79B1AD')

# 设置当前控制的标签页为主标签页
page.set_main_tab()
```

---

## 📍`to_main_tab()`

此方法用于把焦点定位到主标签页，使当前对象控制目标改为主标签页。

**参数：** 无

**返回：**`None`

**示例：**

```python
page.to_main_tab()
```

---

## 📍 `to_tab()`

此方法把焦点定位到某个标签页，使当前对象控制目标改为该标签页。

| 参数名称       | 类型              | 默认值    | 说明                      |
|:----------:|:---------------:|:------:| ----------------------- |
| `tab_id`   | `str`<br>`None` | `None` | 标签页 id，默认为`None`切换到主标签页 |
| `activate` | `bool`          | `True` | 切换后是否变为活动状态             |

**返回：**`None`

**示例：**

```python
# 切换到主标签页
page.to_tab()

# 切换到第一个标签页
page.to_tab(page.tabs[0])

# 切换到id为该字符串的标签页
page.to_tab('0B300BEA6F1F1F4D5DE406872B79B1AD')
```

---

## 📍 `to_front()`

此方法用于激活当前标签页使其处于最前面。标签页无须在活动状态程序也能操控。

**参数：** 无

**返回：**`None`

---

# ✔️ 多标签页协同

## 📍 获取标签页对象

可以用`WebPage`或`ChromiumPage`的`get_tab()`方法获取标签页对象，然后可以使用这个对象对标签页进行操作。

🔸 `get_tab()`

| 参数名称     | 类型              | 默认值    | 说明                          |
|:--------:|:---------------:|:------:| --------------------------- |
| `tab_id` | `str`<br>`None` | `None` | 要获取的标签页 id，默认为`None`获取当前标签页 |

| 返回类型          | 说明    |
|:-------------:| ----- |
| `ChromiumTab` | 标签页对象 |

**示例：**

```python
tab = page.get_tab()  # 获取当前标签页对象
```

---

## 📍 使用标签页对象

每个`ChromiumTab`对象控制一个浏览器标签页，方法和直接使用`ChromiumPage`一致，只比`ChromiumPage`少了控制标签页功能。

```python
tab.get('https://www.baidu.com')  # 使用标签页对象
```

---

## 📍 控制多标签页协作示例

做自动化的时候，我们经常会遇到这样一种场景：我们有一个列表页，须要逐个点开里面的链接，获取新页面的内容，每个链接会打开一个新页面。

如果用 selenium 来做，点击一个链接后必须把焦点切换到新标签页，采集信息后再回到原来的页面，点击下一个链接，但由于焦点的切换，原来的元素信息已丢失，我们只能重新获取所有链接，以计数方式点击下一个，非常不优雅。

而用`ChromiumPage`，点开标签页后焦点无须移动，可直接生成一个新标签页的页面对象，对新页面进行采集，而原来列表页的对象可以继续操作下一个链接。甚至可以用多线程控制多个标签页，实现各种黑科技。

我们用 gitee 的推进项目页面做个演示：[最新推荐项目 - Gitee.com](https://gitee.com/explore/all)

```python
from DrissionPage import ChromiumPage

page = ChromiumPage()
page.get('https://gitee.com/explore/all')

links = page.eles('t:h3')
for link in links[:-1]:
    # 点击链接
    link.click()
    # 获取新标签页对象
    new_tab = page.get_tab(page.latest_tab)
    # 等待新标签页加载
    new_tab.wait_loading()
    # 打印标签页标题
    print(new_tab.title)
    # 关闭除列表页外所有标签页
    page.close_other_tabs()
```

**输出：**

```console
wx-calendar: 原生小程序日历组件(可滑动，可标记，可禁用)
thingspanel-go: 开源插件化物联网平台，Go语言开发。支持MQTT、Modbus多协议、多类型设备接入与可视化、自动化、告警、规则引擎等功能。 QQ群：371794256。
APITable: vika.cn维格表社区版，地表至强的开源低代码、多维表格工具，Airtable的开源免费替代。
ideaseg: 基于 NLP 技术实现的中文分词插件，准确度比常用的分词器高太多，同时提供 ElasticSearch 和 OpenSearch 插件。
vue-plugin-hiprint: hiprint for Vue2/Vue3 ⚡打印、打印设计、可视化设计器、报表设计、元素编辑、可视化打印编辑
ExDUIR.NET: Windows平台轻量DirectUI框架

后面省略。。。
```
