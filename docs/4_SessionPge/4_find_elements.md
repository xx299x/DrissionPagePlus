本节介绍如何获取元素对象。

定位元素是自动化最重要的的技能，虽然可在开发者工具直接复制绝对路径，但这样做一来代码冗长，可读性低，二来难以应付动态变化的页面。  
因此本库提供一套简洁易用的语法，用于快速定位元素，并且内置等待功能、支持链式查找，减少了代码的复杂性。

定位元素大致分为三种方法：

- 在页面或元素内查找子元素
- 根据 DOM 结构相对定位
- 根据页面布局位置相对定位

# ✔️ 示例

先看一些示例，后面在详细讲解用法。

## 📍 简单示例

假设有这样一个页面，本节示例皆使用此页面：

```html
<html>
<body>
<div id="one">
    <p class="p_cls" name="row1">第一行</p>
    <p class="p_cls" name="row2">第二行</p>
    <p class="p_cls">第三行</p>
</div>
<div id="two">
    第二个div
</div>
</body>
</html>
```

我们可以用页面对象去获取其中的元素：

```python
# 获取 id 为 one 的元素
div1 = page.ele('#one')

# 获取 name 属性为 row1 的元素
p1 = page.ele('@name=row1')

# 获取包含“第二个div”文本的元素
div2 = page.ele('第二个div')

# 获取所有div元素
div_list = page.eles('tag:div')
```

也可以获取到一个元素，然后在它里面或周围查找元素：

```python
# 获取到一个元素div1
div1 = page.ele('#one')

# 在div1内查找所有p元素
p_list = div1.eles('tag:p')

# 获取div1后面一个元素
div2 = div1.next()
```

---

## 📍 实际示例

复制此代码可直接运行查看结果。

```python
from DrissionPage import SessionPage

page = SessionPage()
page.get('https://gitee.com/explore')

# 获取包含“全部推荐项目”文本的 ul 元素
ul_ele = page.ele('tag:ul@@text():全部推荐项目')  

# 获取该 ul 元素下所有 a 元素
titles = ul_ele.eles('tag:a')  

# 遍历列表，打印每个 a 元素的文本
for i in titles:  
    print(i.text)
```

**输出：**

```console
全部推荐项目
前沿技术
智能硬件
IOT/物联网/边缘计算
车载应用
...
```

---

# ✔️ 查找元素方法

## 📍 查找单个元素

🔸 `ele()`

页面对象和元素对象都拥有此方法，用于查找第一个匹配条件的元素。

页面对象和元素对象的`ele()`方法参数名称稍有不同，但用法一样。

?>**Tips：**<br>使用 xpath 可直接获取后代元素的属性。

| 参数名称               | 类型                                             | 默认值    | 说明                                           |
|:------------------:|:----------------------------------------------:|:------:| -------------------------------------------- |
| `loc_or_str`（元素对象） | `str`<br>`Tuple[str, str]`                     | 必填     | 元素的定位信息，可以是查询字符串，或 loc 元组                    |
| `loc_or_ele`（页面对象） | `str`<br>`SessionElement`<br>`Tuple[str, str]` | 必填     | 元素的定位信息，可以是查询字符串、loc 元组或一个`SessionElement`对象 |
| `timeout`          | `int`<br>`float`                               | `None` | 不起实际作用，用于和`ChromiumElement`对应，便于无差别调用        |

| 返回类型             | 说明                    |
|:----------------:| --------------------- |
| `SessionElement` | 返回查找到的第一个符合条件的元素对象    |
| `None`           | 未找到符合条件的元素时返回`None`   |
| `str`            | 使用 xpath，可直接获取后代元素的属性 |

**示例：**

```python
from DrissionPage import SessionPage

page = SessionPage()

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

此方法与`ele()`相似，但返回的是匹配到的所有元素组成的列表，用 xpath 获取元素属性时，返回属性文本组成的列表。

| 参数名称         | 类型                         | 默认值    | 说明                                    |
|:------------:|:--------------------------:|:------:| ------------------------------------- |
| `loc_or_str` | `str`<br>`Tuple[str, str]` | 必填     | 元素的定位信息，可以是查询字符串，或 loc 元组             |
| `timeout`    | `int`<br>`float`           | `None` | 不起实际作用，用于和`ChromiumElement`对应，便于无差别调用 |

| 返回类型                   | 说明                           |
|:----------------------:| ---------------------------- |
| `List[SessionElement]` | 返回找到的所有`SessionElement`组成的列表 |
| `List[str]`            | 使用 xpath，可直接获取后代元素的属性        |

**示例：**

```python
# 获取ele元素内的所有p元素
p_eles = ele.eles('tag:p')
# 打印第一个p元素的文本
print(p_eles[0])  
```

---

# ✔️ 查找语法

我们使用一套简洁高效的语法去定位元素，大大简化了定位元素的代码量，增强了功能，也兼容 css selector、xpath、selenium 原生的 loc 元组。

🔸 **匹配模式** 指字符串是否完全匹配，有以下两种：

## 📍 精确匹配符 `=`

表示精确匹配，匹配完全符合的文本或属性。

---

## 📍 模糊匹配符 `:`

表示模糊匹配，匹配含有某个字符串的文本或属性。

---

🔸 **关键字** 是出现在定位语句最左边，用于指明该语句以哪种方式去查找元素，有以下这些：

## 📍 id 匹配符 `#`

表示`id`属性，只在语句最前面且单独使用时生效，可配合`=`或`:`。

```python
# 在页面中查找id属性为one的元素
ele1 = page.ele('#one')

# 在ele1元素内查找id属性包含ne文本的元素
ele2 = ele1.ele('#:ne')  
```

---

## 📍 class 匹配符 `.`

表示`class`属性，只在语句最前面且单独使用时生效，可配合`=`或`:`。

```python
# 查找class属性为p_cls的元素
ele2 = ele1.ele('.p_cls')

# 查找class属性包含_cls文本的元素
ele2 = ele1.ele('.:_cls')  
```

---

## 📍 单属性匹配符 `@`

表示某个属性，只匹配一个属性。  
`@`关键字只有一个简单功能，就是匹配`@`后面的内容，不再对后面的字符串进行解析。因此即使后面的字符串也存在`@`或`@@`，也作为要匹配的内容对待。

!> **注意：**  
如果属性中包含特殊字符，如包含`@`，用这个方式不能正确匹配到，须使用 css selector 方式查找。且特殊字符要用`\`转义。

```python
# 查找name属性为row1的元素
ele2 = ele1.ele('@name=row1')

# 查找name属性包含row文本的元素
ele2 = ele1.ele('@name:row')

# 查找有name属性的元素
ele2 = ele1.ele('@name')

# 查找没有任何属性的元素
ele2 = ele1.ele('@')

# 查找email属性为abc@def.com的元素，有多个@也不会重复处理
ele2 = ele1.ele('@email=abc@def.com')  

# 属性中有特殊字符的情形，匹配abc@def属性等于v的元素
ele2 = ele1.ele('css:div[abc\@def="v"]')
```

---

## 📍 多属性匹配符 `@@`

多属性匹配时使用，个数不限。还能匹配要忽略的元素，匹配文本时也和`@`不一样。  
`@@`后跟 - 时，表示 not。如：

- `@@-name`表示匹配没有`name`属性的元素

- `@@-name=ele_name`表示匹配`name`属性不为`ele_name`的元素

如有以下情况，不能使用此方式，须改用 xpath 的方式：

- 匹配文本或属性中出现`@@`

- 属性名本身以`-`开头

!> **注意：**：<br>如果属性中包含特殊字符，如包含`@`，用这个方式不能正确匹配到，须使用 css selector 方式查找。且特殊字符要用`\`转义。

```python
# 查找name属性为name且class属性包含cls文本的元素
ele2 = ele1.ele('@@name=row1@@class:cls')

# 查找没有class属性的元素
ele2 = ele1.ele('@@-class')

# 查找name属性不包含row1的元素
ele2 = ele1.ele('@@-name:row1')
```

---

## 📍 文本匹配符 `text`

要匹配的文本，查询字符串如开头没有任何关键字，也表示根据传入的文本作模糊查找。  
如果元素内有多个直接的文本节点，精确查找时可匹配所有文本节点拼成的字符串，模糊查找时可匹配每个文本节点。

没有任何匹配符时，默认匹配文本。

```python
# 查找文本为“第二行”的元素
ele2 = ele1.ele('text=第二行')

# 查找文本包含“第二”的元素
ele2 = ele1.ele('text:第二')

# 与上一行一致
ele2 = ele1.ele('第二')  
```

?> **Tips：** <br>若要查找的文本包含`text:` ，可下面这样写，即第一个`text:` 为关键字，第二个是要查找的内容：

```python
ele2 = page.ele('text:text:')
```

---

## 📍 文本匹配符 `text()`

作为查找属性时使用的文本关键字，必须与`@`或`@@`配合使用。

```python
# 查找文本为“第二行”的元素
ele2 = ele1.ele('@text()=第二行')

# 查找文本包含“第二行”的元素
ele2 = ele1.ele('@text():二行')

# 查找文本为“第二行”且class属性为p_cls的元素
ele2 = ele1.ele('@@text()=第二行@@class=p_cls')

# 查找文本为 some text 且没有任何属性的元素（因第一个 @@ 后为空）
ele2 = ele1.ele('@@@@text():some text')

# 查找直接子文本包含 some text 字符串的元素
ele = page.ele('@text():二行')

# 查找元素内部包含 some text 字符串的元素
ele = page.ele('@@text():二行')
```

须要注意的是，`'text=xxxx'`与`'@text()=xxxx'`使用上是有细微差别的。

`text=`表示在元素的直接子文本节点中匹配，`@text()=`会忽略一些文本标签，在整个元素的内容里匹配。

---

## 📍 类型匹配符 `tag`

表示元素的标签，只在语句最前面且单独使用时生效，可与`@`或`@@`配合使用。`tag:`与`tag=`效果一致。

```python
# 定位div元素
ele2 = ele1.ele('tag:div')

# 定位class属性为p_cls的p元素
ele2 = ele1.ele('tag:p@class=p_cls')

# 定位文本为"第二行"的p元素
ele2 = ele1.ele('tag:p@text()=第二行')

# 定位class属性为p_cls且文本为“第二行”的p元素
ele2 = ele1.ele('tag:p@@class=p_cls@@text()=第二行')

# 查找直接文本节点包含“二行”字符串的p元素
ele2 = ele1.ele('tag:p@text():二行')

# 查找内部文本节点包含“二行”字符串的p元素
ele2 = ele1.ele('tag:p@@text():二行')  
```

!> **注意：** <br>`tag:div@text():text` 和 `tag:div@@text():text` 是有区别的，前者只在`div`的直接文本节点搜索，后者搜索`div`的整个内部。

---

## 📍 css selector 匹配符 `css`

表示用 css selector 方式查找元素。`css:`与`css=`效果一致。

```python
# 查找 div 元素
ele2 = ele1.ele('css:.div')

# 查找 div 子元素元素，这个写法是本库特有，原生不支持
ele2 = ele1.ele('css:>div')  
```

---

## 📍 xpath 匹配符 `xpath`

表示用 xpath 方式查找元素。`xpath:`与`xpath=`效果一致。  
在元素中查找时，该方法支持完整的 xpath 语法，能使用 xpath 直接获取元素属性，selenium 不支持这种用法。

```python
# 查找 div 元素
ele2 = ele1.ele('xpath:.//div')

# 和上面一行一样，查找元素的后代时，// 前面的 . 可以省略
ele2 = ele1.ele('xpath://div')

# 获取 div 元素的 class 属性，返回字符串
txt = ele1.ele('xpath://div/@class')  
```

?> **Tips:**   <br>查找元素的后代时，selenium 原生代码要求 xpath 前面必须加`.`，否则会变成在全个页面中查找。作者觉得这个设计是画蛇添足，既然已经通过元素查找了，自然应该只查找这个元素内部的元素。所以，用
xpath 在元素下查找时，最前面`//`或`/`前面的`.`可以省略。

---

## 📍 selenium 的 loc 元组

查找方法能直接接收 selenium 原生定位元组进行查找。

```python
from selenium.webdriver.common.by import By

# 查找id为one的元素
loc1 = (By.ID, 'one')
ele = page.ele(loc1)

# 按 xpath 查找
loc2 = (By.XPATH, '//p[@class="p_cls"]')
ele = page.ele(loc2)  
```

---

# ✔️ 相对定位

以下方法可以以某元素为基准，在 DOM 中按照条件获取其兄弟元素、祖先元素、文档前后元素。  
除获取元素外，还能通过 xpath 获取任意节点内容，如文本节点、注释节点。这在处理元素和文本节点混排的时候非常有用。

!>**注意：**<br>如果元素在`<iframe>`中，相对定位不能超越`<iframe>`文档。

## 📍 获取父级元素

🔸 `parent()`

此方法获取当前元素某一级父元素，可指定筛选条件或层数。

| 参数名称           | 类型                                  | 默认值 | 说明                    |
|:--------------:|:-----------------------------------:|:---:| --------------------- |
| `level_or_loc` | `int`<br>`str`<br>`Tuple[str, str]` | `1` | 第几级父元素，或定位符用于在祖先元素中查找 |

| 返回类型             | 说明              |
|:----------------:| --------------- |
| `SessionElement` | 获取到的元素对象        |
| `None`           | 未获取到结果时返回`None` |

**示例：**

```python
# 获取 ele1 的第二层父元素
ele2 = ele1.parent(2)

# 获取 ele1 父元素中 id 为 id1 的元素
ele2 = ele1.parent('#id1')
```

---

## 📍 获取后面的兄弟元素

🔸 `next()`

此方法返回当前元素后面的某一个同级元素，可指定筛选条件和第几个。

| 参数名称         | 类型                         | 默认值    | 说明          |
|:------------:|:--------------------------:|:------:| ----------- |
| `filter_loc` | `str`<br>`Tuple[str, str]` | `''`   | 用于筛选元素的查询语法 |
| `index`      | `int`                      | `1`    | 查询结果中的第几个   |
| `timeout`    | `int`<br>`float`           | `None` | 无实际作用       |

| 返回类型             | 说明              |
|:----------------:| --------------- |
| `SessionElement` | 获取到的元素对象        |
| `None`           | 未获取到结果时返回`None` |

**示例：**

```python
# 获取 ele1 后面第一个兄弟元素
ele2 = ele1.next()

# 获取 ele1 后面第 3 个兄弟元素
ele2 = ele1.next(3)

# 获取 ele1 后面第 3 个 div 兄弟元素
ele2 = ele1.next('tag:div', 3)

# 获取 ele1 后面第一个文本节点的文本
txt = ele1.next('xpath:text()', 1)
```

---

🔸 `nexts()`

此方法返回当前元素后面全部符合条件的同级元素或节点组成的列表，可用查询语法筛选。

| 参数名称         | 类型                         | 默认值    | 说明          |
|:------------:|:--------------------------:|:------:| ----------- |
| `filter_loc` | `str`<br>`Tuple[str, str]` | `''`   | 用于筛选元素的查询语法 |
| `timeout`    | `int`<br>`float`           | `None` | 无实际作用       |

| 返回类型                   | 说明       |
|:----------------------:| -------- |
| `List[SessionElement]` | 获取到的元素对象 |

**示例：**

```python
# 获取 ele1 后面所有兄弟元素
eles = ele1.nexts()

# 获取 ele1 后面所有 div 兄弟元素
divs = ele1.nexts('tag:div')

# 获取 ele1 后面的所有文本节点
txts = ele1.nexts('xpath:text()')
```

---

## 📍 获取前面的兄弟元素

🔸 `prev()`

此方法返回当前元素前面的某一个同级元素，可指定筛选条件和第几个。

| 参数名称         | 类型                         | 默认值    | 说明          |
|:------------:|:--------------------------:|:------:| ----------- |
| `filter_loc` | `str`<br>`Tuple[str, str]` | `''`   | 用于筛选元素的查询语法 |
| `index`      | `int`                      | `1`    | 查询结果中的第几个   |
| `timeout`    | `int`<br>`float`           | `None` | 无实际作用       |

| 返回类型             | 说明              |
|:----------------:| --------------- |
| `SessionElement` | 获取到的元素对象        |
| `None`           | 未获取到结果时返回`None` |

**示例：**

```python
# 获取 ele1 前面第一个兄弟元素
ele2 = ele1.prev()

# 获取 ele1 前面第 3 个兄弟元素
ele2 = ele1.prev(3)

# 获取 ele1 前面第 3 个 div 兄弟元素
ele2 = ele1.prev(3, 'tag:div')

# 获取 ele1 前面第一个文本节点的文本
txt = ele1.prev(1, 'xpath:text()')
```

---

🔸 `prevs()`

此方法返回当前元素前面全部符合条件的同级元素或节点组成的列表，可用查询语法筛选。

| 参数名称         | 类型                         | 默认值    | 说明          |
|:------------:|:--------------------------:|:------:| ----------- |
| `filter_loc` | `str`<br>`Tuple[str, str]` | `''`   | 用于筛选元素的查询语法 |
| `timeout`    | `int`<br>`float`           | `None` | 无实际作用       |

| 返回类型                   | 说明       |
|:----------------------:| -------- |
| `List[SessionElement]` | 获取到的元素对象 |

**示例：**

```python
# 获取 ele1 前面所有兄弟元素
eles = ele1.prevs()

# 获取 ele1 前面所有 div 兄弟元素
divs = ele1.prevs('tag:div')
```

---

## 📍 在后面文档中查找元素

🔸 `after()`

此方法返回当前元素后面的某一个元素，可指定筛选条件和第几个。这个方法查找范围不局限在兄弟元素间，而是整个 DOM 文档。

| 参数名称         | 类型                         | 默认值    | 说明          |
|:------------:|:--------------------------:|:------:| ----------- |
| `filter_loc` | `str`<br>`Tuple[str, str]` | `''`   | 用于筛选元素的查询语法 |
| `index`      | `int`                      | `1`    | 查询结果中的第几个   |
| `timeout`    | `int`<br>`float`           | `None` | 无实际作用       |

| 返回类型             | 说明              |
|:----------------:| --------------- |
| `SessionElement` | 获取到的元素对象        |
| `None`           | 未获取到结果时返回`None` |

**示例：**

```python
# 获取 ele1 后面第 3 个元素
ele2 = ele1.after(3)

# 获取 ele1 后面第 3 个 div 元素
ele2 = ele1.after('tag:div', 3)

# 获取 ele1 后面第一个文本节点的文本
txt = ele1.after('xpath:text()', 1)
```

---

🔸 `afters()`

此方法返回当前元素后面符合条件的全部元素或节点组成的列表，可用查询语法筛选。这个方法查找范围不局限在兄弟元素间，而是整个 DOM 文档。

| 参数名称         | 类型                         | 默认值    | 说明          |
|:------------:|:--------------------------:|:------:| ----------- |
| `filter_loc` | `str`<br>`Tuple[str, str]` | `''`   | 用于筛选元素的查询语法 |
| `timeout`    | `int`<br>`float`           | `None` | 无实际作用       |

| 返回类型                   | 说明       |
|:----------------------:| -------- |
| `List[SessionElement]` | 获取到的元素对象 |

**示例：**

```python
# 获取 ele1 后所有元素
eles = ele1.afters()

# 获取 ele1 前面所有 div 元素
divs = ele1.afters('tag:div')
```

---

## 📍 在前面文档中查找元素

🔸 `before()`

此方法返回当前元素前面的某一个元素，可指定筛选条件和第几个。这个方法查找范围不局限在兄弟元素间，而是整个 DOM 文档。

| 参数名称         | 类型                         | 默认值    | 说明          |
|:------------:|:--------------------------:|:------:| ----------- |
| `filter_loc` | `str`<br>`Tuple[str, str]` | `''`   | 用于筛选元素的查询语法 |
| `index`      | `int`                      | `1`    | 查询结果中的第几个   |
| `timeout`    | `int`<br>`float`           | `None` | 无实际作用       |

| 返回类型             | 说明              |
|:----------------:| --------------- |
| `SessionElement` | 获取到的元素对象        |
| `None`           | 未获取到结果时返回`None` |

**示例：**

```python
# 获取 ele1 前面第 3 个元素
ele2 = ele1.before(3)

# 获取 ele1 前面第 3 个 div 元素
ele2 = ele1.before('tag:div', 3)

# 获取 ele1 前面第一个文本节点的文本
txt = ele1.before('xpath:text()', 1)
```

---

🔸 `befores()`

此方法返回当前元素前面全部符合条件的元素或节点组成的列表，可用查询语法筛选。这个方法查找范围不局限在兄弟元素间，而是整个 DOM 文档。

| 参数名称         | 类型                         | 默认值    | 说明          |
|:------------:|:--------------------------:|:------:| ----------- |
| `filter_loc` | `str`<br>`Tuple[str, str]` | `''`   | 用于筛选元素的查询语法 |
| `timeout`    | `int`<br>`float`           | `None` | 无实际作用       |

| 返回类型                   | 说明       |
|:----------------------:| -------- |
| `List[SessionElement]` | 获取到的元素对象 |

**示例：**

```python
# 获取 ele1 前面所有元素
eles = ele1.befores()

# 获取 ele1 前面所有 div 元素
divs = ele1.befores('tag:div')
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

| 原写法      | 简化写法   |
|:--------:|:------:|
| `text`   | `tx`   |
| `text()` | `tx()` |
| `tag`    | `t`    |
| `xpath`  | `x`    |
| `css`    | `c`    |
