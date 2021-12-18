# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   base.py
"""
from abc import abstractmethod
from re import sub
from typing import Union, Tuple

from lxml.html import HtmlElement
from selenium.webdriver.remote.webelement import WebElement

from .common import format_html


class BaseParser(object):
    """所有页面、元素类的基类"""

    def __call__(self, loc_or_str):
        return self.ele(loc_or_str)

    def ele(self, loc_or_ele, timeout=None):
        return self._ele(loc_or_ele, timeout, True)

    def eles(self, loc_or_str: Union[Tuple[str, str], str], timeout=None):
        return self._ele(loc_or_str, timeout, False)

    # ----------------以下属性或方法待后代实现----------------
    @property
    def html(self) -> str:
        return ''

    @abstractmethod
    def s_ele(self, loc_or_ele):
        pass

    @abstractmethod
    def s_eles(self, loc_or_str):
        pass

    @abstractmethod
    def _ele(self, loc_or_ele, timeout=None, single=True):
        pass


class BaseElement(BaseParser):
    """各元素类的基类"""

    def __init__(self, ele: Union[WebElement, HtmlElement], page=None):
        self._inner_ele = ele
        self.page = page

    @property
    def inner_ele(self) -> Union[WebElement, HtmlElement]:
        return self._inner_ele

    def next(self, index: int = 1):
        """返回后面的一个兄弟元素，可指定第几个   \n
        :param index: 后面第几个兄弟元素
        :return: 兄弟元素
        """
        nexts = self.nexts(total=1, begin=index)
        return nexts[0] if nexts else None

    # ----------------以下属性或方法由后代实现----------------
    @property
    def tag(self):
        return

    def parent(self, level: int = 1):
        pass

    def prev(self, index: int = 1):
        return

    @property
    def is_valid(self):
        return True

    @abstractmethod
    def nexts(self, total: int = None, begin: int = 1):
        pass


class DrissionElement(BaseElement):
    """DriverElement 和 SessionElement的基类，但不是ShadowRootElement的基类"""

    @abstractmethod
    def parent(self, level: int = 1):
        """返回父级元素"""
        pass

    def prev(self, index: int = 1):
        """返回前面的一个兄弟元素，可指定第几个    \n
        :param index: 前面第几个
        :return: 兄弟元素
        """
        prevs = self.prevs(total=1, begin=index)
        return prevs[0] if prevs else None

    @property
    def link(self) -> str:
        """返回href或src绝对url"""
        return self.attr('href') or self.attr('src')

    @property
    def css_path(self) -> str:
        """返回css path路径"""
        return self._get_ele_path('css')

    @property
    def xpath(self) -> str:
        """返回xpath路径"""
        return self._get_ele_path('xpath')

    @property
    def comments(self) -> list:
        """返回元素注释文本组成的列表"""
        return self.eles('xpath:.//comment()')

    def texts(self, text_node_only: bool = False) -> list:
        """返回元素内所有直接子节点的文本，包括元素和文本节点   \n
        :param text_node_only: 是否只返回文本节点
        :return: 文本列表
        """
        if text_node_only:
            texts = self.eles('xpath:/text()')
        else:
            texts = [x if isinstance(x, str) else x.text for x in self.eles('xpath:./text() | *')]

        return [format_html(x.strip(' ').rstrip('\n')) for x in texts if x and sub('[\r\n\t ]', '', x) != '']

    def nexts(self, total: int = None, begin: int = 1, mode: str = 'ele'):
        """返回后面若干个兄弟元素或节点组成的列表，total为None返回所有            \n
        :param total: 获取多少个元素或节点
        :param begin: 从第几个开始获取，从1起
        :param mode: 'ele', 'node' 或 'text'，匹配元素、节点、或文本节点
        :return: SessionElement对象
        """
        return self._get_brothers(begin=begin, total=total, mode=mode, direction='next')

    def prevs(self, total: int = None, begin: int = 1, mode: str = 'ele'):
        """返回前面若干个兄弟元素或节点组成的列表，total为None返回所有            \n
        :param total: 获取多少个元素或节点
        :param begin: 从第几个开始获取，从1起
        :param mode: 'ele', 'node' 或 'text'，匹配元素、节点、或文本节点
        :return: SessionElement对象
        """
        return self._get_brothers(begin=begin, total=total, mode=mode, direction='prev')

    def _get_brothers(self, begin: int = 1, total: int = None, mode: str = 'ele', direction: str = 'next'):
        """按要求返回兄弟元素或节点组成的列表                                  \n
        :param begin: 从第几个兄弟节点或元素开始
        :param total: 获取多少个
        :param mode: 'ele', 'node' 或 'text'，匹配元素、节点、或文本节点
        :param direction: 'next' 或 'prev'，查找的方向
        :return: DriverElement对象或字符串
        """
        # 查找节点的类型
        node_txt = {'ele': '*', 'node': 'node()', 'text': 'text()'}.get(mode)
        if not node_txt:
            raise ValueError(f"mode参数只能是'node'、'ele'或'text'，现在是：'{mode}'。")

        # 查找节点的方向
        direction_txt = {'next': 'following', 'prev': 'preceding'}.get(direction)
        if not direction_txt:
            raise ValueError(f"direction参数只能是'next'或'prev'，现在是：'{direction}'。")

        timeout = 0 if direction == 'prev' else .5

        # 获取所有节点
        nodes = self._ele(f'xpath:./{direction_txt}-sibling::{node_txt}', timeout=timeout, single=False)

        if direction == 'next':
            end = None if not total or total >= len(nodes) else begin + total - 1
            begin -= 1
        else:
            begin = None if not total or total >= len(nodes) else begin - total - 1
            end = None

        return [e for e in nodes[begin:end] if not (isinstance(e, str) and sub('[ \n\t\r]', '', e) == '')]

    # ----------------以下属性或方法由后代实现----------------
    @property
    def attrs(self):
        return

    @property
    def text(self):
        return

    @property
    def raw_text(self):
        return

    # @abstractmethod
    # def parents(self, num: int = 1):
    #     pass

    @abstractmethod
    def attr(self, attr: str):
        return ''

    def _get_ele_path(self, mode):
        return ''


class BasePage(BaseParser):
    """页面类的基类"""

    def __init__(self, timeout: float = 10):
        """初始化函数"""
        self._url = None
        self.timeout = timeout
        self.retry_times = 3
        self.retry_interval = 2
        self._url_available = None

    @property
    def title(self) -> Union[str, None]:
        """返回网页title"""
        ele = self.ele('xpath:/html/head/title')
        return ele.text if ele else None

    @property
    def timeout(self) -> float:
        """返回查找元素时等待的秒数"""
        return self._timeout

    @timeout.setter
    def timeout(self, second: float) -> None:
        """设置查找元素时等待的秒数"""
        self._timeout = second

    @property
    def cookies(self) -> dict:
        """返回cookies"""
        return self.get_cookies(True)

    @property
    def url_available(self) -> bool:
        """返回当前访问的url有效性"""
        return self._url_available

    # ----------------以下属性或方法由后代实现----------------
    @property
    def url(self):
        return

    @property
    def json(self):
        return

    @abstractmethod
    def get_cookies(self, as_dict: bool = False):
        return {}

    @abstractmethod
    def get(self,
            url: str,
            go_anyway: bool = False,
            show_errmsg: bool = False,
            retry: int = None,
            interval: float = None):
        pass

    @abstractmethod
    def _try_to_connect(self,
                        to_url: str,
                        times: int = 0,
                        interval: float = 1,
                        show_errmsg: bool = False, ):
        pass
