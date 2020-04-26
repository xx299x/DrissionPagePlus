#!/usr/bin/env python
# -*- coding:utf-8 -*-
"""
旧版MixPage，已弃用
在MixPage类中使用DriverPage和SessionPage对象，使用时根据模式调用相应对象的函数
问题是须要在MixPage类中为这两个类中的函数写一一对应的调用函数
新版中直接继承这两个类，只须要为这两个类共有的函数写调用函数即可
"""
from abc import abstractmethod
from typing import Union
from urllib import parse

from requests_html import Element
from selenium.webdriver.remote.webelement import WebElement

from DrissionPage.drission import Drission
from DrissionPage.driver_page import DriverPage
from DrissionPage.session_page import SessionPage


class MixPage:
    def __init__(self, drission: Drission, locs=None, mode='d'):
        self._drission = drission
        self._session = None
        self._driver = None
        self._session_page = None
        self._driver_page = None
        self._url = None
        self._session_url = None
        self._locs = locs
        self._mode = mode
        if mode == 's':
            self._session_page = self.s_page
        else:
            self._driver_page = self.d_page
        self._open_self_url()

    @abstractmethod
    def _open_self_url(self):
        pass

    @property
    def mode(self):
        return self._mode

    @mode.setter
    def mode(self, value):
        self._mode = value

    def change_mode(self, mode=None):
        if mode == self.mode:
            return
        self.mode = 's' if self.mode == 'd' else 'd'

    @property
    def drission(self):
        return self._drission

    @property
    def response(self):
        return self.s_page.response

    @property
    def session(self):
        if self._session is None:
            self._session = self._drission.session
        return self._session

    @property
    def driver(self):
        if self._driver is None:
            self._driver = self._drission.driver
        return self._driver

    @property
    def d_page(self):
        if self._driver_page is None:
            self._driver_page = DriverPage(self.driver)
            if self._url:
                self._init_page()
        return self._driver_page

    @property
    def s_page(self):
        if self._session_page is None:
            self._session_page = SessionPage(self.session)
            if self._url:
                self._init_page()
        self.refresh_url()  # 每次调用session页面时，使url和driver页面保持一致
        return self._session_page

    @property
    def url(self):
        if self.mode == 'd':
            return self.d_page.url
        else:
            return self._url

    def _init_page(self):
        if self._session_page:
            self.cookies_to_driver(self._url)
            self.d_page.get(self._url)
        elif self._driver_page:
            self.cookies_to_session()
            self.s_page.get(self._url)

    def goto(self, url: str, url_data: dict = None):
        """跳转到一个url"""
        to_url = f'{url}?{parse.urlencode(url_data)}' if url_data else url
        if self._url == to_url:
            return
        now_url = self._url
        self._url = to_url
        if self._driver_page:
            if self._session_page:
                self.cookies_to_driver(now_url)
            self._driver_page.get(to_url, url_data)
            if not self._session_page:
                return self.check_driver_url()
        if self._session_page:
            self._session_url = to_url
            if self._session_page:
                self.cookies_to_session()
            return self.s_page.goto(to_url, url_data)

    def check_driver_url(self) -> bool:
        """由子类依据不同的页面自行实现"""
        return True

    def refresh_url(self):
        """使session的url与driver当前保持一致，并复制cookies到session"""
        if self._driver and (self._url != self._driver.current_url or self._session_url != self._driver.current_url):
            self._url = self._driver.current_url
            self._session_url = self._driver.current_url
            self.cookies_to_session()
            self._session_page.get(self._url)

    def cookies_to_session(self):
        self._drission.cookies_to_session()

    def cookies_to_driver(self, url=None):
        u = url if url else self._url
        self._drission.cookies_to_driver(u)

    # ----------------以下为共用函数-----------------------
    def find(self, loc, timeout=10, show_errmsg=True) -> Union[WebElement, Element]:
        if self._mode == 's':
            return self.s_page.find(loc, show_errmsg)
        elif self._mode == 'd':
            return self.d_page.find(loc, timeout, show_errmsg)

    def find_all(self, loc, timeout=10, show_errmsg=True) -> list:
        if self._mode == 's':
            return self.s_page.find_all(loc, show_errmsg)
        elif self._mode == 'd':
            return self.d_page.find_all(loc, timeout, show_errmsg)

    def get_attr(self, loc_or_ele, attr) -> str:
        if self._mode == 's':
            return self.s_page.get_attr(loc_or_ele, attr)
        elif self._mode == 'd':
            return self.d_page.get_attr(loc_or_ele, attr)

    def get_html(self, loc_or_ele) -> str:
        if self._mode == 's':
            return self.s_page.get_html(loc_or_ele)
        elif self._mode == 'd':
            return self.d_page.get_html(loc_or_ele)

    def get_text(self, loc_or_ele) -> str:
        if self._mode == 's':
            return self.s_page.get_text(loc_or_ele)
        elif self._mode == 'd':
            return self.d_page.get_text(loc_or_ele)

    def get_source(self):
        if self._mode == 's':
            return self.s_page.get_html()
        elif self._mode == 'd':
            return self.d_page.get_html()

    def get_cookies(self):
        if self._mode == 's':
            return self.s_page.cookies
        elif self._mode == 'd':
            return self.d_page.cookies

    # ----------------以下为driver page专用函数-----------------
    def input(self, loc_or_ele, value: str, clear=True) -> bool:
        return self.d_page.input(loc_or_ele, value, clear)

    def click(self, loc_or_ele) -> bool:
        return self.d_page.click(loc_or_ele)

    def set_attr(self, loc_or_ele, attribute: str, value: str) -> bool:
        return self.d_page.set_attr(loc_or_ele, attribute, value)

    def run_script(self, loc_or_ele, script: str):
        return self.d_page.run_script(loc_or_ele, script)

    def get_tabs_sum(self) -> int:
        return self.d_page.get_tabs_sum()

    def get_tab_num(self) -> int:
        return self.d_page.get_tab_num()

    def to_tab(self, index: int = 0):
        return self.d_page.to_tab(index)

    def close_current_tab(self):
        return self.d_page.close_current_tab()

    def close_other_tabs(self, tab_index: int = None):
        return self.d_page.close_other_tabs(tab_index)

    def to_iframe(self, loc_or_ele):
        return self.d_page.to_iframe(loc_or_ele)

    def get_screen(self, loc_or_ele, path: str, file_name: str = None) -> str:
        return self.d_page.get_screen(loc_or_ele, path, file_name)

    def choose_select_list(self, loc_or_ele, text):
        return self.d_page.choose_select_list(loc_or_ele, text)

    def refresh(self):
        return self.d_page.refresh()

    def back(self):
        return self.d_page.back()

    def set_window_size(self, x: int = None, y: int = None):
        return self.d_page.set_window_size(x, y)
