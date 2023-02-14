# ✨️ 概述

DrissionPage 是一个基于 python 的网页自动化工具。

它既能控制浏览器，也能收发数据包，甚至能把两者合而为一。

可兼顾浏览器自动化的便利性和 requests 的高效率。

它功能强大，内置无数人性化设计和便捷功能。

它的语法简洁而优雅，代码量少，对新手友好。

---

<a href='https://gitee.com/g1879/DrissionPage/stargazers'><img src='https://gitee.com/g1879/DrissionPage/badge/star.svg?theme=dark' alt='star'></img></a> <a href='https://gitee.com/g1879/DrissionPage/members'><img src='https://gitee.com/g1879/DrissionPage/badge/fork.svg?theme=dark' alt='fork'></img></a>

项目地址：[gitee](https://gitee.com/g1879/DrissionPage)    |    [github](https://github.com/g1879/DrissionPage) 

您的星星是对我最大的支持💖

--- 

支持系统：Windows、Linux、Mac

python 版本：3.6 及以上

支持浏览器：Chromium 内核浏览器（如 Chrome 和 Edge）

---

**📖 使用文档：**  [点击查看](http://g1879.gitee.io/drissionpagedocs)

**交流QQ群：**  897838127

---

# 📕 背景

用 requests 做数据采集面对要登录的网站时，要分析数据包、JS 源码，构造复杂的请求，往往还要应付验证码、JS 混淆、签名参数等反爬手段，门槛较高。若数据是由 JS 计算生成的，还须重现计算过程，体验不好，开发效率不高。
使用浏览器，可以很大程度上绕过这些坑，但浏览器运行效率不高。

因此，这个库设计初衷，是将它们合而为一，能够在不同须要时切换相应模式，并提供一种人性化的使用方法，提高开发和运行效率。  
除了合并两者，本库还以网页为单位封装了常用功能，提供非常简便的操作和语句，在用于网页自动化操作时，减少考虑细节，专注功能实现，使用更方便。  一切从简，尽量提供简单直接的使用方法，使代码更优雅。

以前的版本是对 selenium 进行重新封装实现的。从 3.0 开始，作者另起炉灶，对底层进行了重新开发，摆脱对 selenium 的依赖，增强了功能，提升了运行效率。

--- 

# 💡 理念

简洁！易用 ！方便！

--- 

# ☀️ 特性和亮点

作者经过长期实践，踩过无数坑，总结出的经验全写到这个库里了。

## 🎇 强大的自研内核

本库采用全自研的内核，内置了 N 多实用功能，对常用功能作了整合和优化，对比 selenium，有以下优点：

- 无 webdriver 特征，不会被网站识别

- 无需为不同版本的浏览器下载不同的驱动

- 运行速度更快

- 可以跨`<iframe>`查找元素，无需切入切出

- 把`<iframe>`看作普通元素，获取后可直接在其中查找元素，逻辑更清晰

- 可以同时操作浏览器中的多个标签页，即使标签页为非激活状态，无需切换

- 可以直接读取浏览器缓存来保存图片，无需用 GUI 点击另存

- 可以对整个网页截图，包括视口外的部分（90以上版本浏览器支持）

- 可处理非`open`状态的 shadow-root

## 🎇 亮点功能

除了以上优点，本库还内置了无数人性化设计。

- 极简的语法规则。集成大量常用功能，代码更优雅

- 定位元素更加容易，功能更强大稳定

- 无处不在的等待和自动重试功能。使不稳定的网络变得易于控制，程序更稳定，编写更省心

- 提供强大的下载工具。操作浏览器时也能享受快捷可靠的下载功能

- 允许反复使用已经打开的浏览器。无须每次运行从头启动浏览器，调试超方便

- 使用 ini 文件保存常用配置，自动调用，提供便捷的设置，远离繁杂的配置项

- 内置 lxml 作为解析引擎，解析速度成几个数量级提升

- 使用 POM 模式封装，可直接用于测试，便于扩展

- 高度集成的便利功能，从每个细节中体现

- 还有很多细节，这里不一一列举，欢迎实际使用中体验：）

--- 

# 🌟 简单演示

## ⭐ **与 selenium 代码对比**

以下代码实现一模一样的功能，对比两者的代码量：

🔸 用显性等待方式定位第一个文本包含`some text`的元素

```python
# 使用 selenium：
element = WebDriverWait(driver).until(ec.presence_of_element_located((By.XPATH, '//*[contains(text(), "some text")]')))

# 使用 DrissionPage：
element = page('some text')
```

🔸 跳转到第一个标签页

```python
# 使用 selenium：
driver.switch_to.window(driver.window_handles[0])

# 使用 DrissionPage：
page.to_tab(page.tabs[0])
```

🔸 按文本选择下拉列表

```python
# 使用 selenium：
from selenium.webdriver.support.select import Select

select_element = Select(element)
select_element.select_by_visible_text('text')

# 使用 DrissionPage：
element.select('text')
```

🔸 拖拽一个元素

```python
# 使用 selenium：
ActionChains(driver).drag_and_drop(ele1, ele2).perform()

# 使用 DrissionPage：
ele1.drag_to(ele2)
```

🔸 滚动窗口到底部（保持水平滚动条不变）

```python
# 使用 selenium：
driver.execute_script("window.scrollTo(document.documentElement.scrollLeft, document.body.scrollHeight);")

# 使用 DrissionPage：
page.scroll.to_bottom()
```

🔸 获取伪元素内容

```python
# 使用 selenium：
text = webdriver.execute_script('return window.getComputedStyle(arguments[0], "::after").getPropertyValue("content");',
                                element)

# 使用 DrissionPage：
text = element.pseudo_after
```

🔸 shadow-root 操作

```python
# 使用 selenium：
shadow_element = webdriver.execute_script('return arguments[0].shadowRoot', element)

# 使用 DrissionPage：
shadow_element = element.sr

# 在 shadow_root 下可继续执行查找，获取普通元素
ele = shadow_element.ele('tag:div')
ele.click()
```

🔸 随时让浏览器窗口消失和显示（Windows系统）

```python
# selenium 无此功能

# 使用 DrissionPage
page.hide_browser()  # 让浏览器窗口消失
page.show_browser()  # 重新显示浏览器窗口
```

## ⭐ **与 requests 代码对比**

以下代码实现一模一样的功能，对比两者的代码量：

🔸 获取元素内容

```python
url = 'https://baike.baidu.com/item/python'

# 使用 requests：
from lxml import etree

headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36'}
response = requests.get(url, headers=headers)
html = etree.HTML(response.text)
element = html.xpath('//h1')[0]
title = element.text

# 使用 DrissionPage：
page = WebPage('s')
page.get(url)
title = page('tag:h1').text
```

Tips: DrissionPage 自带默认`headers`

🔸 下载文件

```python
url = 'https://www.baidu.com/img/flexible/logo/pc/result.png'
save_path = r'C:\download'

# 使用 requests：
r = requests.get(url)
with open(f'{save_path}\\img.png', 'wb') as fd:
   for chunk in r.iter_content():
       fd.write(chunk)

# 使用 DrissionPage：
page.download(url, save_path, 'img')  # 支持重命名，处理文件名冲突，自动创建目标文件夹
```

## ⭐ **模式切换**

用浏览器登录网站，然后切换到 requests 读取网页。两者会共享登录信息。

```python
page = WebPage()  # 创建页面对象，默认 driver 模式
page.get('https://gitee.com/profile')  # 访问个人中心页面（未登录，重定向到登录页面）

page.ele('@id:user_login').input('your_user_name')  # 使用 selenium 输入账号密码登录
page.ele('@id:user_password').input('your_password\n')
sleep(1)

page.change_mode()  # 切换到 session 模式
print('登录后title：', page.title, '\n')  # 登录后 session 模式的输出
```

输出：

```
登录后title： 个人资料 - 码云 Gitee.com
```

**获取并显示元素属性**

```python
# 接上段代码
foot = page.ele('@id:footer-left')  # 用 id 查找元素
first_col = foot.ele('css:>div')  # 使用 css selector 在元素的下级中查找元素（第一个）
lnk = first_col.ele('text:命令学')  # 使用文本内容查找元素
text = lnk.text  # 获取元素文本
href = lnk.attr('href')  # 获取元素属性值

print(text, href, '\n')

# 简洁模式串联查找
text = page('@id:footer-left')('css:>div')('text:命令学').text
print(text)
```

输出：

```
Git 命令学习 https://oschina.gitee.io/learn-git-branching/

Git 命令学习
```

--- 

# 🛠 使用方法

[点击跳转到使用文档](http://g1879.gitee.io/drissionpage)

--- 

# 🔖 版本历史

[点击查看版本历史](http://g1879.gitee.io/drissionpagedocs/10_history/)

--- 

# 🖐🏻 免责声明

请勿将 DrissionPage 应用到任何可能会违反法律规定和道德约束的工作中,请友善使用 DrissionPage，遵守蜘蛛协议，不要将 DrissionPage 用于任何非法用途。如您选择使用 DrissionPage
即代表您遵守此协议，作者不承担任何由于您违反此协议带来任何的法律风险和损失，一切后果由您承担。

---  

# ☕ 请我喝咖啡

如果本项目对您有所帮助，不妨请作者我喝杯咖啡 ：）

![](https://gitee.com/g1879/DrissionPage/raw/master/docs/imgs/code.jpg)
