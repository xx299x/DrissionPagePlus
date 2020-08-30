#!/usr/bin/env python
# -*- coding:utf-8 -*-
from html import unescape
from re import split as re_SPLIT
from typing import Union, Any

from selenium.webdriver.remote.webelement import WebElement

from .common import DrissionElement
from .driver_element import execute_driver_find


class ShadowRootElement(DrissionElement):
    def __init__(self, inner_ele: WebElement, parent_ele, timeout: float = 10):
        super().__init__(inner_ele)
        self.parent_ele = parent_ele
        self.timeout = timeout
        self._driver = inner_ele.parent

    def __repr__(self):
        return f'<ShadowRootElement in {self.parent_ele} >'

    @property
    def driver(self):
        """返回控制元素的WebDriver对象"""
        return self._driver

    @property
    def tag(self):
        return 'shadow-root'

    @property
    def html(self):
        return unescape(self.inner_ele.get_attribute('innerHTML')).replace('\xa0', ' ')

    @property
    def parent(self):
        return self.parent_ele

    def parents(self, num: int = 1):
        """返回上面第num级父元素              \n
        :param num: 第几级父元素
        :return: DriverElement对象
        """
        loc = 'xpath', f'.{"/.." * (num - 1)}'
        return self.parent_ele.ele(loc, timeout=0.01, show_errmsg=False)

    @property
    def next(self):
        """返回后一个兄弟元素"""
        return self.nexts()

    def nexts(self, num: int = 1):
        """返回后面第num个兄弟元素      \n
        :param num: 后面第几个兄弟元素
        :return: DriverElement对象
        """
        loc = 'css selector', f':nth-child({num})'
        return self.parent_ele.ele(loc)

    def ele(self,
            loc_or_str: Union[tuple, str],
            mode: str = None,
            timeout: float = None,
            show_errmsg: bool = False):
        if isinstance(loc_or_str, str):
            loc_or_str = get_css_from_str(loc_or_str)
        elif isinstance(loc_or_str, tuple) and len(loc_or_str) == 2:
            pass
        else:
            raise ValueError('Argument loc_or_str can only be tuple or str.')
        if loc_or_str[0] == 'xpath':
            raise ValueError('不支持xpath')
        timeout = timeout or self.timeout
        return execute_driver_find(self.inner_ele, loc_or_str, mode, show_errmsg, timeout)

    def eles(self,
             loc_or_str: Union[tuple, str],
             timeout: float = None,
             show_errmsg: bool = False):
        return self.ele(loc_or_str, mode='all', show_errmsg=show_errmsg, timeout=timeout)

    def run_script(self, script: str, *args) -> Any:
        """执行js代码，传入自己为第一个参数  \n
        :param script: js文本
        :param args: 传入的参数
        :return: js执行结果
        """
        return self.inner_ele.parent.execute_script(script, self.inner_ele, *args)

    def is_enabled(self) -> bool:
        """是否可用"""
        return self.inner_ele.is_enabled()

    def is_valid(self) -> bool:
        """用于判断元素是否还能用，应对页面跳转元素不能用的情况"""
        try:
            self.is_enabled()
            return True
        except:
            return False


def get_css_from_str(loc: str) -> tuple:
    """处理元素查找语句                                              \n
    查找方式：属性、tag name及属性、css selector                      \n
    =表示精确匹配，:表示模糊匹配，无控制字符串时默认搜索该字符串           \n
    示例：                                                          \n
        @class:ele_class - class含有ele_class的元素                  \n
        @class=ele_class - class等于ele_class的元素                  \n
        @class - 带class属性的元素                                   \n
        tag:div - div元素                                            \n
        tag:div@class:ele_class - class含有ele_class的div元素        \n
        tag:div@class=ele_class - class等于ele_class的div元素        \n
        css:div.ele_class                                           \n
    """
    if loc.startswith('@'):  # 根据属性查找
        r = re_SPLIT(r'([:=])', loc[1:], maxsplit=1)
        if len(r) == 3:
            mode = '=' if r[1] == '=' else '*='
            loc_str = f'*[{r[0]}{mode}{r[2]}]'
        else:
            loc_str = f'*[{loc[1:]}]'
    elif loc.startswith(('tag=', 'tag:')):  # 根据tag name查找
        if '@' not in loc[4:]:
            loc_str = f'{loc[4:]}'
        else:
            at_lst = loc[4:].split('@', maxsplit=1)
            r = re_SPLIT(r'([:=])', at_lst[1], maxsplit=1)
            if len(r) == 3:
                if r[0] == 'text()':
                    raise ValueError('不支持按文本查找')
                mode = '=' if r[1] == '=' else '*='
                loc_str = f'{at_lst[0]}[{r[0]}{mode}"{r[2]}"]'
            else:
                loc_str = f'{at_lst[0]}[{r[0]}]'
    elif loc.startswith(('css=', 'css:')):  # 用css selector查找
        loc_str = loc[4:]
    elif loc.startswith(('text=', 'text:')):  # 根据文本查找
        raise ValueError('不支持按文本查找')
    elif loc.startswith(('xpath=', 'xpath:')):  # 用xpath查找
        raise ValueError('不支持xpath')
    else:
        raise ValueError('不支持的查询语句')
    return 'css selector', loc_str
