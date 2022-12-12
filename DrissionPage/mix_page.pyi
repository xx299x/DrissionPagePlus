# -*- coding:utf-8 -*-
from typing import Union, List, Tuple

from DownloadKit import DownloadKit
from requests import Response, Session
from requests.cookies import RequestsCookieJar
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement

from .base import BasePage
from .config import DriverOptions, SessionOptions
from .drission import Drission
from .driver_element import DriverElement
from .driver_page import DriverPage
from .session_element import SessionElement
from .session_page import SessionPage


class MixPage(SessionPage, DriverPage, BasePage):
    """MixPage整合了DriverPage和SessionPage，封装了对页面的操作，
    可在selenium（d模式）和requests（s模式）间无缝切换。
    切换的时候会自动同步cookies。
    获取信息功能为两种模式共有，操作页面元素功能只有d模式有。
    调用某种模式独有的功能，会自动切换到该模式。
    """

    def __init__(self,
                 mode: str = ...,
                 drission: Union[Drission, str] = ...,
                 timeout: float = ...,
                 driver_options: Union[Options, DriverOptions, bool] = ...,
                 session_options: Union[dict, SessionOptions, bool] = ...) -> None:
        self._mode: str = ...
        self._drission: Drission = ...

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str, DriverElement, SessionElement, WebElement],
                 timeout: float = ...) -> Union[DriverElement, SessionElement, str, None]: ...

    # -----------------共有属性和方法-------------------
    @property
    def url(self) -> Union[str, None]: ...

    @property
    def title(self) -> str: ...

    @property
    def html(self) -> str: ...

    @property
    def json(self) -> dict: ...

    def get(self,
            url: str,
            show_errmsg: bool = ...,
            retry: int = ...,
            interval: float = ...,
            **kwargs) -> Union[bool, None]: ...

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, DriverElement, SessionElement, WebElement],
            timeout: float = ...) -> Union[DriverElement, SessionElement, str, None]: ...

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = ...) -> List[Union[DriverElement, SessionElement, str]]: ...

    def s_ele(self, loc_or_ele: Union[Tuple[str, str], str, DriverElement, SessionElement] = ...) \
            -> Union[SessionElement, str, None]: ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str] = ...) -> List[Union[SessionElement, str]]: ...

    def _ele(self,
             loc_or_ele: Union[Tuple[str, str], str, DriverElement, SessionElement, WebElement],
             timeout: float = ..., single: bool = ...) \
            -> Union[DriverElement, SessionElement, str, None, List[Union[SessionElement, str]], List[
                Union[DriverElement, str]]]: ...

    def get_cookies(self, as_dict: bool = ..., all_domains: bool = ...) -> Union[dict, list]: ...

    # ----------------MixPage独有属性和方法-----------------------
    @property
    def drission(self) -> Drission: ...

    @property
    def driver(self) -> WebDriver: ...

    @property
    def session(self) -> Session: ...

    @property
    def response(self) -> Response: ...

    @property
    def mode(self) -> str: ...

    @property
    def _session_url(self) -> str: ...

    def change_mode(self, mode: str = ..., go: bool = ..., copy_cookies: bool = ...) -> None: ...

    def set_cookies(self, cookies: Union[RequestsCookieJar, list, tuple, str, dict], refresh: bool = ...) -> None: ...

    def cookies_to_session(self, copy_user_agent: bool = ...) -> None: ...

    def cookies_to_driver(self, url: str = ...) -> None: ...

    def check_page(self, by_requests: bool = ...) -> Union[bool, None]: ...

    def close_driver(self) -> None: ...

    def close_session(self) -> None: ...

    # ----------------重写SessionPage的函数-----------------------
    def post(self,
             url: str,
             data: Union[dict, str] = ...,
             show_errmsg: bool = ...,
             retry: int = ...,
             interval: float = ...,
             **kwargs) -> bool: ...

    @property
    def download(self) -> DownloadKit: ...

    def chrome_downloading(self, path: str = ...) -> list: ...

    # ----------------MixPage独有函数-----------------------
    def hide_browser(self) -> None: ...

    def show_browser(self) -> None: ...
