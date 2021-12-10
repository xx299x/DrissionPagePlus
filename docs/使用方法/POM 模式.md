MixPage 封装了常用的页面操作，可方便地用于扩展。

例：扩展一个列表页面读取类

```python
import re
from time import sleep
from DrissionPage import *

class ListPage(MixPage):
    """本类封装读取列表页面的方法，根据必须的4个元素，可读取同构的列表页面
    （中文变量真香）"""
    def __init__(self, drission: Drission, url: str = None, **xpaths):
        super().__init__(drission)
        self._url = url
        self.xpath_栏目名 = xpaths['栏目名']  # [xpath字符串, 正则表达式]
        self.xpath_下一页 = xpaths['下一页']
        self.xpath_行s = xpaths['行']
        self.xpath_页数 = xpaths['页数']  # [xpath字符串, 正则表达式]
        self.总页数 = self.get_总页数()
        if url:
            self.get(url)

    def get_栏目名称(self) -> str:
        if self.xpath_栏目名[1]:
            s = self.ele(f'xpath:{self.xpath_栏目名[0]}').text
            r = re.search(self.xpath_栏目名[1], s)
            return r.group(1)
        else:
            return self.ele(f'xpath:{self.xpath_栏目名[0]}').text

    def get_总页数(self) -> int:
        if self.xpath_页数[1]:
            s = self.ele(f'xpath:{self.xpath_页数[0]}').text
            r = re.search(self.xpath_页数[1], s)
            return int(r.group(1))
        else:
            return int(self.ele(f'xpath:{self.xpath_页数[0]}').text)

    def click_下一页(self, wait: float = None):
        self.ele(f'xpath:{self.xpath_下一页}').click()
        if wait:
            sleep(wait)

    def get_当前页列表(self, 待爬内容: list) -> list:
        """
        待爬内容格式：[[xpath1,参数1],[xpath2,参数2]...]
        返回列表格式：[[参数1,参数2...],[参数1,参数2...]...]
        """
        结果列表 = []
        行s = self.eles(f'xpath:{self.xpath_行s}')
        for 行 in 行s:
            行结果 = []
            for j in 待爬内容:
                行结果.append(行.ele(f'xpath:{j[0]}').attr(j[1]))
            结果列表.append(行结果)
            print(行结果)
        return 结果列表

    def get_列表(self, 待爬内容: list,  wait: float = None) -> list:
        列表 = self.get_当前页列表(待爬内容)
        for _ in range(self.总页数 - 1):
            self.click_下一页(wait)
            列表.extend(self.get_当前页列表(待爬内容))
        return 列表
```