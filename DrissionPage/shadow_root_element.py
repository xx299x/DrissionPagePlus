#!/usr/bin/env python
# -*- coding:utf-8 -*-
from html import unescape
from typing import Union, Any

from selenium.webdriver.remote.webelement import WebElement

from common import DrissionElement


# from driver_element import DriverElement


class ShadowRootElement(DrissionElement):
    def __init__(self, inner_ele: WebElement, parent_ele):
        super().__init__(inner_ele)
        self.parent_ele = parent_ele

    def ele(self, loc: Union[tuple, str], mode: str = None, show_errmsg: bool = True):
        pass

    def eles(self, loc: Union[tuple, str], show_errmsg: bool = True):
        pass

    def attr(self, attr: str):
        return self.html if attr == 'innerHTML' else None

    def run_script(self, script: str, *args) -> Any:
        """执行js代码，传入自己为第一个参数  \n
        :param script: js文本
        :param args: 传入的参数
        :return: js执行结果
        """
        return self.inner_ele.parent.execute_script(script, self.inner_ele, *args)

    @property
    def html(self):
        return unescape(self.attr('innerHTML')).replace('\xa0', ' ')

    @property
    def parent(self):
        return self.parent_ele

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
