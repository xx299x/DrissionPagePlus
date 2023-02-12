# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from pathlib import Path
from typing import Any, Union, Tuple, List

from DownloadKit import DownloadKit
from requests import Session, Response
from requests.cookies import RequestsCookieJar
from requests.structures import CaseInsensitiveDict

from .base import BasePage
from .chromium_page import ChromiumPage
from .configs.session_options import SessionOptions
from .session_element import SessionElement
from .web_page import WebPage


class SessionPage(BasePage):
    def __init__(self,
                 session_or_options: Union[Session, SessionOptions] = None,
                 timeout: float = None):
        self._session: Session = ...
        self._session_options: SessionOptions = ...
        self._url: str = ...
        self._response: Response = ...
        self._download_path: str = ...
        self._download_set: DownloadSetter = ...
        self._url_available: bool = ...
        self.timeout: float = ...
        self.retry_times: int = ...
        self.retry_interval: float = ...

    def _set_start_options(self, session_or_options, none) -> None: ...

    def _create_session(self) -> None: ...

    def _set_session(self, opt: SessionOptions) -> None: ...

    def _set_runtime_settings(self) -> None: ...

    def set_cookies(self, cookies: Union[RequestsCookieJar, list, tuple, str, dict]) -> None: ...

    def set_headers(self, headers: dict) -> None: ...

    def set_user_agent(self, ua: str) -> None: ...

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str, SessionElement],
                 timeout: float = None) -> Union[SessionElement, str, None]: ...

    # -----------------共有属性和方法-------------------
    @property
    def title(self) -> str: ...

    @property
    def url(self) -> str: ...

    @property
    def html(self) -> str: ...

    @property
    def json(self) -> Union[dict, None]: ...

    @property
    def download_path(self) -> str: ...

    @property
    def download_set(self) -> DownloadSetter: ...

    def get(self,
            url: str,
            show_errmsg: bool | None = False,
            retry: int | None = None,
            interval: float | None = None,
            timeout: float | None = None,
            params: dict | None = ...,
            data: Union[dict, str, None] = ...,
            json: Union[dict, str, None] = ...,
            headers: dict | None = ...,
            cookies: Any | None = ...,
            files: Any | None = ...,
            auth: Any | None = ...,
            allow_redirects: bool = ...,
            proxies: dict | None = ...,
            hooks: Any | None = ...,
            stream: Any | None = ...,
            verify: Any | None = ...,
            cert: Any | None = ...) -> bool: ...

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, SessionElement],
            timeout: float = None) -> Union[SessionElement, str, None]: ...

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None) -> List[Union[SessionElement, str]]: ...

    def s_ele(self,
              loc_or_ele: Union[Tuple[str, str], str, SessionElement] = None) -> Union[SessionElement, str, None]: ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str]) -> List[Union[SessionElement, str]]: ...

    def _ele(self,
             loc_or_ele: Union[Tuple[str, str], str, SessionElement],
             timeout: float = None,
             single: bool = True) -> Union[SessionElement, str, None, List[Union[SessionElement, str]]]: ...

    def get_cookies(self,
                    as_dict: bool = False,
                    all_domains: bool = False) -> Union[dict, list]: ...

    # ----------------session独有属性和方法-----------------------
    @property
    def session(self) -> Session: ...

    @property
    def response(self) -> Response: ...

    @property
    def download(self) -> DownloadKit: ...

    def post(self,
             url: str,
             data: Union[dict, str, None] = ...,
             show_errmsg: bool = False,
             retry: int | None = None,
             interval: float | None = None,
             timeout: float | None = ...,
             params: dict | None = ...,
             json: Union[dict, str, None] = ...,
             headers: dict | None = ...,
             cookies: Any | None = ...,
             files: Any | None = ...,
             auth: Any | None = ...,
             allow_redirects: bool = ...,
             proxies: dict | None = ...,
             hooks: Any | None = ...,
             stream: Any | None = ...,
             verify: Any | None = ...,
             cert: Any | None = ...) -> bool: ...

    def _s_connect(self,
                   url: str,
                   mode: str,
                   data: Union[dict, str, None] = None,
                   show_errmsg: bool = False,
                   retry: int = None,
                   interval: float = None,
                   **kwargs) -> bool: ...

    def _make_response(self,
                       url: str,
                       mode: str = 'get',
                       data: Union[dict, str] = None,
                       retry: int = None,
                       interval: float = None,
                       show_errmsg: bool = False,
                       **kwargs) -> tuple: ...


class DownloadSetter(object):
    def __init__(self, page: Union[SessionPage, WebPage, ChromiumPage]):
        self._page: SessionPage = ...
        self._DownloadKit: DownloadKit = ...

    @property
    def DownloadKit(self) -> DownloadKit: ...

    @property
    def if_file_exists(self) -> FileExists: ...

    def split(self, on_off: bool) -> None: ...

    def save_path(self, path: Union[str, Path]): ...


class FileExists(object):
    def __init__(self, setter: DownloadSetter):
        self._setter: DownloadSetter = ...

    def __call__(self, mode: str) -> None: ...

    def skip(self) -> None: ...

    def rename(self) -> None: ...

    def overwrite(self) -> None: ...


def check_headers(kwargs: Union[dict, CaseInsensitiveDict], headers: Union[dict, CaseInsensitiveDict],
                  arg: str) -> bool: ...


def set_charset(response: Response) -> Response: ...
