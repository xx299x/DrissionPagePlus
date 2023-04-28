# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from http.cookiejar import Cookie
from typing import Union

from requests import Session
from requests.cookies import RequestsCookieJar
from requests.structures import CaseInsensitiveDict

from DrissionPage.base import DrissionElement, BasePage
from DrissionPage.chromium_element import ChromiumElement
from DrissionPage.chromium_base import ChromiumBase


class DataPacket(object):
    """返回的数据包管理类"""

    def __init__(self, tab: str, target: str, raw_info: dict):
        self.tab: str = ...
        self.target: str = ...
        self._raw_info: dict = ...
        self._raw_post_data: str = ...
        self._raw_body: str = ...
        self._base64_body: bool = ...
        self._request: Request = ...
        self._response: Response = ...

    def __repr__(self): ...

    @property
    def requestId(self) -> str: ...

    @property
    def url(self) -> str: ...

    @property
    def method(self) -> str: ...

    @property
    def frameId(self) -> str: ...

    @property
    def resourceType(self) -> str: ...

    @property
    def request(self) -> Request: ...

    @property
    def response(self) -> Response: ...


class Request(object):
    url: str = ...
    urlFragment: str = ...
    postDataEntries: list = ...
    mixedContentType: str = ...
    initialPriority: str = ...
    referrerPolicy: str = ...
    isLinkPreload: bool = ...
    trustTokenParams: dict = ...
    isSameSite: bool = ...

    def __init__(self, raw_request: dict, post_data: str):
        self._request: dict = ...
        self._raw_post_data: str = ...
        self._postData: str = ...

    @property
    def headers(self) -> dict: ...

    @property
    def postData(self) -> Union[str, dict]: ...


class Response(object):
    responseErrorReason: str = ...
    responseStatusCod: int = ...
    responseStatusText: str = ...

    def __init__(self, raw_response: dict, raw_body: str, base64_body: bool):
        self._response: dict = ...
        self._raw_body: str = ...
        self._is_base64_body: bool = ...
        self._body: Union[str, dict] = ...
        self._headers: dict = ...

    @property
    def headers(self) -> CaseInsensitiveDict: ...

    @property
    def body(self) -> Union[str, dict, bool]: ...


def get_ele_txt(e: DrissionElement) -> str: ...


def format_html(text: str) -> str: ...


def location_in_viewport(page, loc_x: int, loc_y: int) -> bool: ...


def offset_scroll(ele: ChromiumElement, offset_x: int, offset_y: int) -> tuple: ...


def make_absolute_link(link, page: BasePage = None) -> str: ...


def is_js_func(func: str) -> bool: ...


def cookie_to_dict(cookie: Union[Cookie, str, dict]) -> dict: ...


def cookies_to_tuple(cookies: Union[RequestsCookieJar, list, tuple, str, dict]) -> tuple: ...


def set_session_cookies(session: Session, cookies: Union[RequestsCookieJar, list, tuple, str, dict]) -> None: ...


def set_browser_cookies(page: ChromiumBase, cookies: Union[RequestsCookieJar, list, tuple, str, dict]) -> None: ...


def is_cookie_in_driver(page: ChromiumBase, cookie: dict) -> bool: ...
