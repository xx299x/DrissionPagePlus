# 简介

***

DrissionPage，即 driver 和 session 组合而成的 page。  
是个基于 python 的 Web 自动化操作集成工具。  
它实现了 selenium 和 requests 之间的无缝切换。  
可以兼顾 selenium 的便利性和 requests 的高效率。  
它集成了页面常用功能，两种模式系统一致的 API，使用便捷。  
它用 POM 模式封装了页面元素常用的方法，适合自动化操作功能扩展。  
更棒的是，它的使用方式非常简洁和人性化，代码量少，对新手友好。

**项目地址：**

- https://github.com/g1879/DrissionPage
- https://gitee.com/g1879/DrissionPage

**示例地址：** [使用DrissionPage的网页自动化及爬虫示例](https://gitee.com/g1879/DrissionPage-demos)

**联系邮箱：**  g1879@qq.com

**交流QQ群：**  897838127

**理念**

**简洁、易用 、可扩展**

**背景**

requests 爬虫面对要登录的网站时，要分析数据包、JS 源码，构造复杂的请求，往往还要应付验证码、JS 混淆、签名参数等反爬手段，门槛较高。若数据是由 JS 计算生成的，还须重现计算过程，体验不好，开发效率不高。  
使用 selenium，可以很大程度上绕过这些坑，但 selenium 效率不高。因此，这个库将 selenium 和 requests 合而为一，不同须要时切换相应模式，并提供一种人性化的使用方法，提高开发和运行效率。  
除了合并两者，本库还以网页为单位封装了常用功能，简化了 selenium 的操作和语句，在用于网页自动化操作时，减少考虑细节，专注功能实现，使用更方便。  
一切从简，尽量提供简单直接的使用方法，对新手更友好。

# 特性

***

- 以简洁的代码为第一追求。
- 允许在 selenium 和 requests 间无缝切换，共享 session。
- 两种模式提供一致的 API，使用体验一致。
- 人性化的页面元素操作方式，减轻页面分析工作量和编码量。
- 对常用功能作了整合和优化，更符合实际使用需要。
- 兼容 selenium 代码，便于项目迁移。
- 使用 POM 模式封装，便于扩展。
- 统一的文件下载方法，弥补浏览器下载的不足。
- 简易的配置方法，摆脱繁琐的浏览器配置。

# 项目结构

***

**结构图**

![](https://gitee.com/g1879/DrissionPage-demos/raw/master/pics/20201118170751.jpg)

**Drission 类**

管理负责与网页通讯的 WebDriver 对象和 Session 对象，相当于驱动器的角色。

**MixPage 类**

MixPage 封装了页面操作的常用功能，它调用 Drission 类中管理的驱动器，对页面进行访问、操作。可在 driver 和 session 模式间切换。切换的时候会自动同步登录状态。

**DriverElement 类**

driver 模式下的页面元素类，可对元素进行点击、输入文本、修改属性、运行 js 等操作，也可在其下级搜索后代元素。

**SessionElement 类**

session 模式下的页面元素类，可获取元素属性值，也可在其下级搜索后代元素。

# 简单演示

***

**与 selenium 代码对比**

以下代码实现一模一样的功能，对比两者的代码量：

- 用显性等待方式查找第一个文本包含 some text 的元素

```python
# 使用 selenium：
element = WebDriverWait(driver).until(ec.presence_of_element_located((By.XPATH, '//*[contains(text(), "some text")]')))

# 使用 DrissionPage：
element = page('some text')
```

- 跳转到第一个标签页

```python
# 使用 selenium：
driver.switch_to.window(driver.window_handles[0])

# 使用 DrissionPage：
page.to_tab(0)
```

- 按文本选择下拉列表

```python
# 使用 selenium：
from selenium.webdriver.support.select import Select
select_element = Select(element)
select_element.select_by_visible_text('text')

# 使用 DrissionPage：
element.select('text')
```

- 拖拽一个元素

```python
# 使用 selenium：
ActionChains(driver).drag_and_drop(ele1, ele2).perform()

# 使用 DrissionPage：
ele1.drag_to(ele2)
```

- 滚动窗口到底部（保持水平滚动条不变）

```python
# 使用 selenium：
driver.execute_script("window.scrollTo(document.documentElement.scrollLeft, document.body.scrollHeight);")

# 使用 DrissionPage：
page.scroll_to('bottom')
```

- 设置 headless 模式

```python
# 使用 selenium：
options = webdriver.ChromeOptions()
options.add_argument("--headless")

# 使用 DrissionPage：
set_headless()
```

- 获取伪元素内容

```python
# 使用 selenium：
text = webdriver.execute_script('return window.getComputedStyle(arguments[0], "::after").getPropertyValue("content");', element)

# 使用 DrissionPage：
text = element.after
```

- 获取 shadow-root

```python
# 使用 selenium：
shadow_element = webdriver.execute_script('return arguments[0].shadowRoot', element)

# 使用 DrissionPage：
shadow_element = element.shadow_root
```

- 用 xpath 直接获取属性或文本节点（返回文本）

```python
# 使用 selenium：
相当复杂

# 使用 DrissionPage：
class_name = element('xpath://div[@id="div_id"]/@class')
text = element('xpath://div[@id="div_id"]/text()[2]')
```

**与 requests 代码对比**

以下代码实现一模一样的功能，对比两者的代码量：

- 获取元素内容

```python
url = 'https://baike.baidu.com/item/python'

# 使用 requests：
from lxml import etree
headers = {'User-Agent':'Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36'}
response = requests.get(url, headers = headers)
html = etree.HTML(response.text)
element = html.xpath('//h1')[0]
title = element.text

# 使用 DrissionPage：
page = MixPage('s')
page.get(url)
title = page('tag:h1').text
```

Tips: DrissionPage 自带默认 headers

- 下载文件

```python
url = 'https://www.baidu.com/img/flexible/logo/pc/result.png'
save_path = r'C:\download'

# 使用 requests：
r = requests.get(url)
with open(f'{save_path}\\img.png', 'wb') as fd:
   for chunk in r.iter_content():
       fd.write(chunk)
        
# 使用 DrissionPage：
page.download(url, save_path, 'img')  # 支持重命名，处理文件名冲突
```

**模式切换**

用 selenium 登录网站，然后切换到 requests 读取网页。两者会共享登录信息。

```python
page = MixPage()  # 创建页面对象，默认 driver 模式
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

**获取并打印元素属性**

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

**下载文件**

```python
url = 'https://www.baidu.com/img/flexible/logo/pc/result.png'
save_path = r'C:\download'
page.download(url, save_path)
```

# 使用方法

***

请在 Wiki中查看：[点击跳转到wiki](https://gitee.com/g1879/DrissionPage/wikis/%E5%AE%89%E8%A3%85%E4%B8%8E%E5%AF%BC%E5%85%A5?sort_id=3201408)

# 版本历史

***

请在 Wiki中查看：[点击查看版本历史](https://gitee.com/g1879/DrissionPage/wikis/%E7%89%88%E6%9C%AC%E5%8E%86%E5%8F%B2?sort_id=3201403)

# APIs

***

请在 Wiki中查看：[点击查看APIs](https://gitee.com/g1879/DrissionPage/wikis/Drission%20%E7%B1%BB?sort_id=3159323)

