# -*- coding:utf-8 -*-
from http.cookiejar import Cookie
from typing import Union

from requests.cookies import RequestsCookieJar

from .._pages.session_page import SessionPage
from .._pages.chromium_base import ChromiumBase


class CookiesSetter(object):
    def __init__(self, page: ChromiumBase):
        self._page: ChromiumBase = ...

    def __call__(self, cookies: Union[RequestsCookieJar, Cookie, list, tuple, str, dict]) -> None: ...

    def remove(self, name: str, url: str = None, domain: str = None, path: str = None) -> None: ...

    def clear(self) -> None: ...


class SessionCookiesSetter(object):
    def __init__(self, page: SessionPage):
        self._page: SessionPage = ...

    def __call__(self, cookies: Union[RequestsCookieJar, Cookie, list, tuple, str, dict]) -> None: ...

    def remove(self, name: str) -> None: ...

    def clear(self) -> None: ...
