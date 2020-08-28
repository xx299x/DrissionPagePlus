#!/usr/bin/env python
# -*- coding:utf-8 -*-
from html import unescape
from typing import Union, Any

from selenium.webdriver.remote.webelement import WebElement

from .common import DrissionElement, get_loc_from_str
from .driver_element import execute_driver_find


class ShadowRootElement(DrissionElement):
    def __init__(self, inner_ele: WebElement, parent_ele, timeout: float = 10):
        super().__init__(inner_ele)
        self.parent_ele = parent_ele
        self.timeout = timeout
        self._driver = inner_ele.parent

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

    # @property
    # def next(self):
    #     """返回后一个兄弟元素"""
    #     return
    #
    # def nexts(self, num: int = 1):
    #     """返回后面第num个兄弟元素      \n
    #     :param num: 后面第几个兄弟元素
    #     :return: DriverElement对象
    #     """
    #     # loc = 'xpath', f'./following-sibling::*[{num}]'
    #     return

    def ele(self,
            loc_or_str: Union[tuple, str],
            mode: str = None,
            timeout: float = None,
            show_errmsg: bool = False):
        if isinstance(loc_or_str, str):
            loc_or_str = get_loc_from_str(loc_or_str)
        elif isinstance(loc_or_str, tuple) and len(loc_or_str) == 2:
            pass
        else:
            raise ValueError('Argument loc_or_str can only be tuple or str.')

        if loc_or_str[0] == 'xpath':
            # 确保查询语句最前面是.
            # loc_str = loc_or_str[1] if loc_or_str[1].startswith(('.', '/')) else f'.//{loc_or_str[1]}'
            # loc_str = loc_str if loc_str.startswith('.') else f'.{loc_str}'
            loc_str = loc_or_str[1]
            # print(self.inner_ele)
            # print(loc_str)
            js = f'''return document.evaluate('{loc_str}', arguments[0]).iterateNext()'''  #
            print(js)
            return self.inner_ele.parent.execute_script(js, self.inner_ele)
            # return self.run_script(js)
        # else:
        #     if loc_or_str[1].lstrip().startswith('>'):
        #         loc_or_str = loc_or_str[0], f'{self.css_path}{loc_or_str[1]}'

        timeout = timeout or self.timeout

        return execute_driver_find(self.inner_ele, loc_or_str, mode, show_errmsg, timeout)

    def eles(self, loc: Union[tuple, str], show_errmsg: bool = True):
        pass

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
