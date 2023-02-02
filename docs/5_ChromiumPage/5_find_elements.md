浏览器页面对象获取元素对象的方式，与`SessionPage`获取元素对象的方法是一致的，但比后者有更多功能。本节重点介绍其独有功能，与`SessionPage`一致的部分，请查看“收发数据包》查找元素”章节。

# ✔️ 查找元素方法

## 📍 查找单个元素

🔸 `ele()`

页面对象和元素对象都拥有此方法，用于查找第一个匹配条件的元素。

页面对象和元素对象的`ele()`方法参数名称稍有不同，但用法一样。

查找元素内置了等待元素出现功能，默认跟随页面设置，也可以每次查找单独调整。

?>**Tips：**<br>在元素对象的`ele()`方法中使用 xpath 可直接获取后代元素的属性。

| 参数名称               | 类型                                              | 默认值    | 说明                                            |
|:------------------:|:-----------------------------------------------:|:------:| --------------------------------------------- |
| `loc_or_str`（元素对象） | `str`<br>`Tuple[str, str]`                      | 必填     | 元素的定位信息，可以是查询字符串，或 loc 元组                     |
| `loc_or_ele`（页面对象） | `str`<br>`ChromiumElement`<br>`Tuple[str, str]` | 必填     | 元素的定位信息，可以是查询字符串、loc 元组或一个`ChromiumElement`对象 |
| `timeout`          | `int`<br>`float`                                | `None` | 查找元素等待时间，为`None`则使用页面对象`timeout`属性值           |

| 返回类型              | 说明                                |
|:-----------------:| --------------------------------- |
| `ChromiumElement` | 返回查找到的第一个符合条件的元素对象                |
| `None`            | 限时内未找到符合条件的元素时返回`None`            |
| `str`             | 在元素的`ele()`中使用 xpath，可直接获取后代元素的属性 |

**示例：**

```python
from DrissionPage import ChromiumPage

page = ChromiumPage()

# 在页面内查找元素
ele1 = page.ele('#one')

# 在元素内查找后代元素
ele2 = ele1.ele('第二行')

# 使用 xpath 获取后代中第一个 div 元素的 class 属性（元素内查找可用）
ele_class = ele1.ele('xpath://p/@class')
```

---

## 📍 查找多个元素

🔸 `eles()`

此方法与`ele()`相似，但返回的是匹配到的所有元素组成的列表。

元素对象用 xpath 获取元素属性时，返回属性文本组成的列表。

| 参数名称         | 类型                         | 默认值    | 说明                                  |
|:------------:|:--------------------------:|:------:| ----------------------------------- |
| `loc_or_str` | `str`<br>`Tuple[str, str]` | 必填     | 元素的定位信息，可以是查询字符串，或 loc 元组           |
| `timeout`    | `int`<br>`float`           | `None` | 查找元素等待时间，为`None`则使用页面对象`timeout`属性值 |

| 返回类型                    | 说明                                 |
|:-----------------------:| ---------------------------------- |
| `List[ChromiumElement]` | 返回找到的所有`ChromiumElement`组成的列表      |
| `List[str]`             | 在元素的`eles()`中使用 xpath，可直接获取后代元素的属性 |

**示例：**

```python
# 获取ele元素内的所有p元素
p_eles = ele.eles('tag:p')
# 打印第一个p元素的文本
print(p_eles[0].text)  
```

---

## 📍 查找单个静态元素

静态元素即 s 模式的`SessionElement`
元素对象，是纯文本构造的，因此用它处理速度非常快。对于复杂的页面，要在成百上千个元素中采集数据时，转换为静态元素可把速度提升几个数量级。作者曾在采集的时候，用同一套逻辑，仅仅把元素转换为静态，就把一个要 30
秒才采集完成的页面，加速到零点几秒完成。   
我们甚至可以把整个页面转换为静态元素，再在其中提取信息。
当然，这种元素不能进行点击等交互。  
用`s_ele()`可在把查找到的动态元素转换为静态元素输出，或者获取元素或页面本身的静态元素副本。

🔸 `s_ele()`

页面对象和元素对象都拥有此方法，用于查找第一个匹配条件的元素，获取其静态版本。

页面对象和元素对象的`s_ele()`方法参数名称稍有不同，但用法一样。

| 参数名称               | 类型                                              | 默认值 | 说明                                            |
|:------------------:|:-----------------------------------------------:|:---:| --------------------------------------------- |
| `loc_or_str`（元素对象） | `str`<br>`Tuple[str, str]`                      | 必填  | 元素的定位信息，可以是查询字符串，或 loc 元组                     |
| `loc_or_ele`（页面对象） | `str`<br>`ChromiumElement`<br>`Tuple[str, str]` | 必填  | 元素的定位信息，可以是查询字符串、loc 元组或一个`ChromiumElement`对象 |

| 返回类型             | 说明                                |
|:----------------:| --------------------------------- |
| `SessionElement` | 返回查找到的第一个符合条件的元素对象的静态版本           |
| `None`           | 限时内未找到符合条件的元素时返回`None`            |
| `str`            | 在元素的`ele()`中使用 xpath，可直接获取后代元素的属性 |

!>**注意：**<br>页面对象和元素对象的`s_ele()`方法不能搜索到在`<iframe>`里的元素，页面对象的静态版本也不能搜索`<iframe>`里的元素。要使用`<iframe>`
里元素的静态版本，可先获取该元素，再转换。而使用`ChromiumFrame`对象，则可以直接用`s_ele()`查找元素，这在后面章节再讲述。

?>**Tips：**<br>从一个`ChromiumElement`元素获取到的`SessionElement`版本，依然能够使用相对定位方法定位祖先或兄弟元素。

```python
from DrissionPage import ChromiumPage

p = ChromiumPage()

# 在页面中查找元素，获取其静态版本
ele1 = page.s_ele('search text')

# 在动态元素中查找元素，获取其静态版本
ele = page.ele('search text')
ele2 = ele.s_ele()

# 获取页面元素的静态副本（不传入参数）
s_page = page.s_ele()

# 获取动态元素的静态副本
s_ele = ele.s_ele()

# 在静态副本中查询下级元素（因为已经是静态元素，用ele()查找结果也是静态）
ele3 = s_page.ele('search text')
ele4 = s_ele.ele('search text')
```

---

## 📍 查找多个静态元素

查找多个静态元素使用`s_eles()`方法。

此方法与`s_ele()`相似，但返回的是匹配到的所有元素组成的列表，或属性值组成的列表。

| 参数名称         | 类型                         | 默认值 | 说明                        |
|:------------:|:--------------------------:|:---:| ------------------------- |
| `loc_or_str` | `str`<br>`Tuple[str, str]` | 必填  | 元素的定位信息，可以是查询字符串，或 loc 元组 |

| 返回类型                   | 说明                                 |
|:----------------------:| ---------------------------------- |
| `List[SessionElement]` | 返回找到的所有元素的`SessionElement`版本组成的列表  |
| `List[str]`            | 在元素的`eles()`中使用 xpath，可直接获取后代元素的属性 |

**示例：**

```python
from DrissionPage import WebPage

p = WebPage()
for ele in p.s_eles('search text'):
    print(ele.text)
```

---

## 📍 获取当前焦点元素

使用`active_ele`属性获取页面上焦点所在元素。

```python
ele = page.active_ele
```

---

## 📍 获取 shadow_root

d 模式元素如果包含 shadow_root，可使用`shadow_root`属性获取。  
该属性返回的是一个`ChromiumShadowRootElement`对象，用法与`ChromiumElement`相似。也能使用各种元素查找方式，返回内部元素或相对位置元素，返回 `ChromiumElement`
元素。返回的`ChromiumElement`和普通的没有区别。

```python
# 获取一个元素下是 shadow root
shadow_ele = ele.shadow_root
# 获取该 shadow root 下的一个元素
ele1 = shadow_ele.ele('search text')
# 点击获取到的元素
ele1.click()  
```

---

# ✔️ 查找语法

查找语法与`SessionPage`一致，此处只放个汇总表，详情请查看“收发数据包》查找元素”章节。

| 匹配符      | 说明                  | 示例                          |
|:--------:| ------------------- | --------------------------- |
| `=`      | 精确匹配符               | `'@name=row1'`              |
| `:`      | 模糊匹配符               | `'@name:row'`               |
| `#`      | id 匹配符              | `'#one'`                    |
| `.`      | class 匹配符           | `'.p_cls'`                  |
| `@`      | 单属性匹配符              | `'@name=row1'`              |
| `@@`     | 多属性匹配符              | `'@@name:row@@class:p_cls'` |
| `text`   | 文本匹配符               | `'text=第一行'`                |
| `text()` | 与`@`或`@@`配合使用的文本匹配符 | `'@text()=第二行'`             |
| `tag`    | 类型匹配符               | `'tag:div'`                 |
| `css`    | css selector 匹配符    | `'css:.div'`                |
| `xpath`  | xpath 匹配符           | `'xpath://div'`             |
| loc 元组   | selenium 的定位元组      | `(By.ID, 'abc')`            |

---

# ✔️ 相对定位

相对语法与`SessionPage`一致，此处只放个汇总表，详情请查看“收发数据包》查找元素”章节。

注意有以下两点差异：

- `timeout`参数是有效的，会等待目标元素出现

- 返回的类型是`ChromiumElement`而非`SessionElement`

| 方法          | 说明                                                                  |
|:-----------:| ------------------------------------------------------------------- |
| `parent()`  | 此方法获取当前元素某一级父元素，可指定筛选条件或层数                                          |
| `next()`    | 此方法返回当前元素后面的某一个同级元素，可指定筛选条件和第几个                                     |
| `nexts()`   | 此方法返回当前元素后面全部符合条件的同级元素或节点组成的列表，可用查询语法筛选                             |
| `prev()`    | 此方法返回当前元素前面的某一个同级元素，可指定筛选条件和第几个                                     |
| `prevs()`   | 此方法返回当前元素前面全部符合条件的同级元素或节点组成的列表，可用查询语法筛选                             |
| `after()`   | 此方法返回当前元素后面的某一个元素，可指定筛选条件和第几个。这个方法查找范围不局限在兄弟元素间，而是整个 DOM 文档         |
| `afters()`  | 此方法返回当前元素后面符合条件的全部元素或节点组成的列表，可用查询语法筛选。这个方法查找范围不局限在兄弟元素间，而是整个 DOM 文档 |
| `before()`  | 此方法返回当前元素前面的某一个元素，可指定筛选条件和第几个。这个方法查找范围不局限在兄弟元素间，而是整个 DOM 文档         |
| `befores()` | 此方法返回当前元素前面全部符合条件的元素或节点组成的列表，可用查询语法筛选。这个方法查找范围不局限在兄弟元素间，而是整个 DOM 文档 |

---

# ✔️ 等待

## 📍 等待元素加载

由于网络、js 运行时间的不确定性等因素，经常须要等待元素加载到 DOM 中才能使用。

l浏览器所有查找元素操作都自带等待，时间默认跟随元素所在页面`timeout`属性（默认 10 秒），也可以在每次查找时单独设置，单独设置的等待时间不会改变页面原来设置。

```python
# 页面初始化时设置查找元素超时时间为 15 秒
page = ChromiumPage(timeout=15)
# 设置查找元素超时时间为 5 秒
page.timeout = 5

# 使用页面超时时间来查找元素（5 秒）
ele1 = page.ele('search text')
# 为这次查找页面独立设置等待时间（1 秒）
ele1 = page.ele('search text', timeout=1)
# 查找后代元素，使用页面超时时间（5 秒）
ele2 = ele1.ele('search text')
# 查找后代元素，使用单独设置的超时时间（1 秒）
ele2 = ele1.ele('some text', timeout=1)  
```

---

## 📍 等待元素状态改变

有时候我们须要等待元素到达某种状态，如显示、隐藏、删除。页面对象和元素对象都内置了`wait_ele()`方法，用于等待元素状态变化。

该方法可接收现成的`ChromiumElement`对象，或定位符，再调用内置方法实现等待。

默认等待时间为页面对象的`timeout`值，也可以单独设定。

🔸 `wait_ele()`方法

| 参数名称         | 类型                                              | 默认值    | 说明                |
|:------------:|:-----------------------------------------------:|:------:| ----------------- |
| `loc_or_ele` | `str`<br>`Tuple[str, str]`<br>`ChromiumElement` | 必填     | 要等待的元素，可以是元素或定位符  |
| `timeout`    | `int`<br>`float`                                | `None` | 等待超时时间，默认使用页面超时时间 |

| 内置方法        | 参数  | 返回             | 功能           |
|:-----------:|:---:|:--------------:| ------------ |
| `display()` | 无   | `bool`表示是否等待成功 | 等待元素从 DOM 显示 |
| `hidden()`  | 无   | `bool`表示是否等待成功 | 等待元素从 DOM 隐藏 |
| `delete()`  | 无   | `bool`表示是否等待成功 | 等待元素从 DOM 删除 |

**示例：**

```python
# 等待 id 为 div1 的元素显示，超时使用页面设置
page.wait_ele('#div1').display()

# 等待 id 为 div1 的元素被删除（使用 loc 元组），设置超时3秒
ele.wait_ele('#div1', timeout=3).delete()

# 等待已获取到的元素被隐藏
ele2 = ele1.ele('#div1')
ele1.wait_ele(ele2).hidden()
```

---

# ✔️ 查找 iframe 里的元素

## 📍 在页面下跨级查找

与 selenium 不同，本库可以直接查找`<iframe>`里面的元素，而且无视层级，可以直接获取到多层`<iframe>`里的元素。无需切入切出，大大简化了程序逻辑，使用更便捷。

假设在页面中有个两级`<iframe>`，其中有个元素`<div id='abc'></div>`，可以这样获取：

```python
page = ChromiumPage()
ele = page('#abc')
```

获取前后无须切入切出，也不影响获取页面上其它元素。

如果用 selenium，要这样写：

```python
driver = webdriver.Chrome()
driver.switch_to.frame(0)
driver.switch_to.frame(0)
ele = driver.find_element(By.ID, 'abc')
driver.switch_to.default_content()
```

显然比较繁琐，而且切入到`<iframe>`后无法对`<iframe>`外的元素进行操作。

!>**注意：**<br>- 跨级查找只是页面对象支持，元素对象不能直接查找内部 iframe 里的元素。<br>- 跨级查找只能用于与主框架同域名的`<iframe>`，不同域名的请用下面的方法。

---

## 📍 在 iframe 元素下查找

本库把`<iframe>`看作一个特殊元素/页面对象看待，可以实现同时操作多个`<iframe>`，而无须来回切换。

对于跨域名的`<iframe>`，我们无法通过页面直接查找里面的元素，可以先获取到`<iframe>`元素，再在其下查找。当然，非跨域`<iframe>`也可以这样操作。

假设一个`<iframe>`的 id 为 `'iframe1'`，要在其中查找一个 id 为`'abc'`的元素：

```python
page = ChromiumPage()
iframe = page('#iframe1')
ele = iframe('#abc')
```

这个`<iframe>`元素是一个页面对象，因此可以继续在其下进行跨`<iframe>`查找（相对这个`<iframe>`不跨域的）。

---

# ✔️ `ShadowRootElement`相关查找

本库把 shadow-root 也作为元素对象看待，是为`ChromiumShadowRootElement`对象。该对象可与普通元素一样查找下级元素和 DOM 内相对定位。  
对`ChromiumShadowRootElement`对象进行相对定位时，把它看作其父对象内部的第一个对象，其余定位逻辑与普通对象一致。

!> **注意：**  <br>如果`ChromiumShadowRootElement`元素的下级元素中有其它`ChromiumShadowRootElement`元素，那这些下级`ChromiumShadowRootElement`
元素内部是无法直接通过定位语句查找到的，只能先定位到其父元素，再用`shadow-root`属性获取。

```python
# 获取一个 shadow-root 元素
sr_ele = page.ele('#app').shadow_root

# 在该元素下查找下级元素
ele1 = sr_ele.ele('tag:div')

# 用相对定位获取其它元素
ele1 = sr_ele.parent(2)
ele1 = sr_ele.next('tag:div', 1)
ele1 = sr_ele.after('tag:div', 1)
eles = sr_ele.nexts('tag:div')

# 定位下级元素中的 shadow+-root 元素
sr_ele2 = sr_ele.ele('tag:div').shadow_root
```

由于 shadow-root 不能跨级查找，链式操作非常常见，所以设计了一个简写：`sr`，功能和`shadow_root`一样，都是获取元素内部的`ChromiumShadowRootElement`。

多级 shadow-root 链式操作示例：

以下这段代码，可以打印浏览器历史第一页，可见是通过多级 shadow-root 来获取的。

```python
from DrissionPage import ChromiumPage

page = ChromiumPage()
page.get('chrome://history/')

items = page('#history-app').sr('#history').sr.eles('t:history-item')
for i in items:
    print(i.sr('#item-container').text.replace('\n', ''))
```

---

# ✔️ 简化写法

为进一步精简代码，对语法进行了精简

- 定位语法都有其简化形式。
- 页面和元素对象都实现了`__call__()`方法，可直接调用。
- 所有查找方法都支持链式操作

示例：

```python
# 定位到页面中 id 为 table_id 的元素，然后获取它的所有 tr 元素
eles = page('#table_id').eles('t:tr')

# 定位到 class 为 cls 的元素，然后在它里面查找文本为 text 的元素
ele2 = ele1('.cls1')('tx=text')

# 获取 ele1 的 shadow_root 元素，再在里面查找 class 属性为 cls 的元素
ele2 = ele1.sr('.cls')

# 按xpath 查找元素
ele2 = ele1('x://div[@class="ele_class"]')  
```

简化写法对应列表

| 原写法           | 简化写法   |
|:-------------:|:------:|
| `text`        | `tx`   |
| `text()`      | `tx()` |
| `tag`         | `t`    |
| `xpath`       | `x`    |
| `css`         | `c`    |
| `shadow_root` | `sr`   |
