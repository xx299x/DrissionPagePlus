# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   mix_page.py
"""
from typing import Union
from urllib import parse

from requests import Response
from requests_html import Element, HTMLSession
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement

from .drission import Drission
from .driver_page import DriverPage
from .session_page import SessionPage


class Null(object):
    """避免IDE警告未调用超类初始化函数而引入的无用类"""

    def __init__(self):
        pass


class MixPage(Null, SessionPage, DriverPage):
    """MixPage封装了页面操作的常用功能，可在selenium（d模式）和requests（s模式）间无缝切换。
    切换的时候会自动同步cookies，兼顾selenium的易用性和requests的高性能。
    获取信息功能为两种模式共有，操作页面元素功能只有d模式有。调用某种模式独有的功能，会自动切换到该模式。
    这些功能由DriverPage和SessionPage类实现。
    """

    def __init__(self, drission: Drission, locs=None, mode='d'):
        """初始化函数
        :param drission: 整合了driver和session的类
        :param locs: 提供页面元素地址的类
        :param mode: 默认使用selenium的d模式
        """
        super().__init__()
        self._drission = drission
        self._session = None
        self._driver = None
        self._url = None
        self._response = None
        self._locs = locs
        self._url_available = None
        self._mode = mode
        if mode == 's':
            self._session = self._drission.session
        elif mode == 'd':
            self._driver = self._drission.driver

    @property
    def url(self) -> str:
        """根据模式获取当前活动的url"""
        if self._mode == 'd':
            return super(SessionPage, self).url
        elif self._mode == 's':
            return self.session_url

    @property
    def session_url(self) -> str:
        return self._response.url if self._response else None

    @property
    def mode(self) -> str:
        """返回当前模式
        :return: 's'或'd'
        """
        return self._mode

    def change_mode(self, mode: str = None) -> None:
        """切换模式，接收字符串s或d，除此以外的字符串会切换为d模式
        切换后调用相应的get函数使访问的页面同步
        :param mode: 模式字符串
        """
        if mode == self._mode:
            return
        self._mode = 's' if self._mode == 'd' else 'd'
        if self._mode == 'd':  # s转d
            self._url = super(SessionPage, self).url
            self.get(self.session_url)
        elif self._mode == 's':  # d转s
            self._url = self.session_url
            self.get(super(SessionPage, self).url)

    @property
    def drission(self) -> Drission:
        """返回当前使用的Dirssion对象"""
        return self._drission

    @property
    def driver(self) -> WebDriver:
        """返回driver对象，如没有则创建
        每次访问时切换到d模式，主要用于独有函数及外部调用
        :return:selenium的WebDriver对象
        """
        if self._driver is None:
            self._driver = self._drission.driver
        self.change_mode('d')
        return self._driver

    @property
    def session(self) -> HTMLSession:
        """返回session对象，如没有则创建
        每次访问时切换到s模式，主要用于独有函数及外部调用
        :return:requests-html的HTMLSession对象
        """
        if self._session is None:
            self._session = self._drission.session
        self.change_mode('s')
        return self._session

    @property
    def response(self) -> Response:
        """返回response对象，切换到s模式"""
        self.change_mode('s')
        return self._response

    @property
    def cookies(self) -> Union[dict, list]:  # TODO:统一到一种类型
        """返回cookies，根据模式获取"""
        if self._mode == 's':
            return super().cookies
        elif self._mode == 'd':
            return super(SessionPage, self).cookies

    def check_driver_url(self) -> bool:
        """判断页面是否能访问，由子类依据不同的页面自行实现"""
        return True

    def cookies_to_session(self) -> None:
        """从driver复制cookies到session"""
        self._drission.cookies_to_session()

    def cookies_to_driver(self, url=None) -> None:
        """从session复制cookies到driver，chrome需要指定域才能接收cookies"""
        u = url if url else self.session_url
        self._drission.cookies_to_driver(u)

    # ----------------以下为共用函数-----------------------

    def get(self, url: str, params: dict = None, go_anyway=False, **kwargs) -> Union[bool, Response, None]:
        """跳转到一个url，跳转前先同步cookies，跳转后判断目标url是否可用"""
        to_url = f'{url}?{parse.urlencode(params)}' if params else url
        if not url or (not go_anyway and self.url == to_url):
            return
        if self._mode == 'd':
            if self.session_url:
                self.cookies_to_driver(self.session_url)
            super(SessionPage, self).get(url=to_url, go_anyway=go_anyway)
            if self._session:
                ua = {"User-Agent": "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.6) "}
                return True if self._session.get(to_url, headers=ua).status_code == 200 else False
            else:
                return self.check_driver_url()
        elif self._mode == 's':
            if self._session is None:
                self._session = self._drission.session
            if self._driver:
                self.cookies_to_session()
            super().get(url=to_url, go_anyway=go_anyway, **self.drission.session_options)
            return self._url_available

    def find(self, loc: tuple, mode=None, timeout: float = 10, show_errmsg: bool = True) -> Union[WebElement, Element]:
        """查找一个元素，根据模式调用对应的查找函数
        :param loc: 页面元素地址
        :param mode: 以某种方式查找元素，可选'single','all','visible'(d模式独有)
        :param timeout: 超时时间
        :param show_errmsg: 是否显示错误信息
        :return: 页面元素对象，s模式下返回Element，d模式下返回WebElement
        """
        if self._mode == 's':
            return super().find(loc, mode=mode, show_errmsg=show_errmsg)
        elif self._mode == 'd':
            return super(SessionPage, self).find(loc, mode=mode, timeout=timeout, show_errmsg=show_errmsg)

    def find_all(self, loc: tuple, timeout: float = 10, show_errmsg: bool = True) -> list:
        """查找符合条件的所有元素"""
        if self._mode == 's':
            return super().find_all(loc, show_errmsg)
        elif self._mode == 'd':
            return super(SessionPage, self).find_all(loc, timeout=timeout, show_errmsg=show_errmsg)

    def search(self, value: str, mode: str = None, timeout: float = 10) -> Union[WebElement, Element, None]:
        """根据内容搜索元素
        :param value: 搜索内容
        :param mode: 可选'single','all'
        :param timeout: 超时时间
        :return: 页面元素对象，s模式下返回Element，d模式下返回WebElement
        """
        if self._mode == 's':
            return super().search(value, mode=mode)
        elif self._mode == 'd':
            return super(SessionPage, self).search(value, mode=mode, timeout=timeout)

    def search_all(self, value: str, timeout: float = 10) -> list:
        """根据内容搜索元素"""
        if self._mode == 's':
            return super().search_all(value)
        elif self._mode == 'd':
            return super(SessionPage, self).search_all(value, timeout=timeout)

    def get_attr(self, loc_or_ele: Union[WebElement, Element, tuple], attr: str) -> str:
        """获取元素属性值"""
        if self._mode == 's':
            return super().get_attr(loc_or_ele, attr)
        elif self._mode == 'd':
            return super(SessionPage, self).get_attr(loc_or_ele, attr)

    def get_html(self, loc_or_ele: Union[WebElement, Element, tuple] = None) -> str:
        """获取元素innerHTML，如未指定元素则获取页面源代码"""
        if self._mode == 's':
            return super().get_html(loc_or_ele)
        elif self._mode == 'd':
            return super(SessionPage, self).get_html(loc_or_ele)

    def get_text(self, loc_or_ele) -> str:
        """获取元素innerText"""
        if self._mode == 's':
            return super().get_text(loc_or_ele)
        elif self._mode == 'd':
            return super(SessionPage, self).get_text(loc_or_ele)

    def get_title(self) -> str:
        """获取页面title"""
        if self._mode == 's':
            return super().get_title()
        elif self._mode == 'd':
            return super(SessionPage, self).get_title()

    def close_driver(self) -> None:
        """关闭driver及浏览器，切换到s模式"""
        self.change_mode('s')
        self._driver = None
        self.drission.close_driver()

    def close_session(self) -> None:
        """关闭session，切换到d模式"""
        self.change_mode('d')
        self._session = None
        self.drission.close_session()
