# -*- coding:utf-8 -*-
"""
@Author   : g1879
@Contact  : g1879@qq.com
@Copyright: (c) 2024 by g1879, Inc. All Rights Reserved.
@License  : BSD 3-Clause.
"""
from typing import Union, Tuple, List, Any, Optional

from requests import Session, Response

from .chromium_frame import ChromiumFrame
from .chromium_page import ChromiumPage
from .chromium_tab import WebPageTab
from .session_page import SessionPage
from .._base.base import BasePage
from .._base.driver import Driver
from .._configs.chromium_options import ChromiumOptions
from .._configs.session_options import SessionOptions
from .._elements.chromium_element import ChromiumElement
from .._elements.none_element import NoneElement
from .._elements.session_element import SessionElement
from .._units.setter import WebPageSetter


class WebPage(SessionPage, ChromiumPage, BasePage):

    def __init__(self,
                 mode: str = 'd',
                 timeout: float = None,
                 chromium_options: Union[ChromiumOptions, bool] = None,
                 session_or_options: Union[Session, SessionOptions, bool] = None) -> None:
        self._mode: str = ...
        self._has_driver: bool = ...
        self._has_session: bool = ...
        self._session_options: Union[SessionOptions, None] = ...
        self._chromium_options: Union[ChromiumOptions, None] = ...

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str, ChromiumElement, SessionElement],
                 index: int = 0,
                 timeout: float = None) -> Union[ChromiumElement, SessionElement, NoneElement]: ...

    # -----------------共有属性和方法-------------------
    @property
    def url(self) -> Union[str, None]: ...

    @property
    def _browser_url(self) -> Union[str, None]: ...

    @property
    def title(self) -> str: ...

    @property
    def raw_data(self) -> Union[str, bytes]: ...

    @property
    def html(self) -> str: ...

    @property
    def json(self) -> dict: ...

    @property
    def response(self) -> Response: ...

    @property
    def mode(self) -> str: ...

    @property
    def cookies(self) -> dict: ...

    @property
    def user_agent(self) -> str: ...

    @property
    def session(self) -> Session: ...

    @property
    def _session_url(self) -> str: ...

    @property
    def timeout(self) -> float: ...

    @timeout.setter
    def timeout(self, second: float) -> None: ...

    def get(self,
            url: str,
            show_errmsg: bool = False,
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
            cert: Any | None = ...) -> Union[bool, None]: ...

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, ChromiumElement, SessionElement],
            timeout: float = None) -> Union[ChromiumElement, SessionElement, NoneElement]: ...

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None) -> List[Union[ChromiumElement, SessionElement]]: ...

    def s_ele(self, loc_or_ele: Union[Tuple[str, str], str] = None) -> Union[SessionElement, NoneElement]: ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str]) -> List[SessionElement]: ...

    def change_mode(self, mode: str = None, go: bool = True, copy_cookies: bool = True) -> None: ...

    def cookies_to_session(self, copy_user_agent: bool = True) -> None: ...

    def cookies_to_browser(self) -> None: ...

    def get_cookies(self,
                    as_dict: bool = False,
                    all_domains: bool = False,
                    all_info: bool = False) -> Union[dict, list]: ...

    def get_tab(self, id_or_num: Union[str, WebPageTab, int] = None) -> WebPageTab: ...

    def new_tab(self,
                url: str = None,
                new_window: bool = False,
                background: bool = False,
                new_context: bool = False) -> WebPageTab: ...

    def close_driver(self) -> None: ...

    def close_session(self) -> None: ...

    def close(self) -> None: ...

    # ----------------重写SessionPage的函数-----------------------
    def post(self,
             url: str,
             data: Union[dict, str, None] = None,
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
             cert: Any | None = ...) -> Union[bool, Response]: ...

    @property
    def set(self) -> WebPageSetter: ...

    def _find_elements(self,
                       loc_or_ele: Union[Tuple[str, str], str, ChromiumElement, SessionElement, ChromiumFrame],
                       timeout: float = None,
                       index: Optional[int] = 0,
                       relative: bool = False,
                       raise_err: bool = None) \
            -> Union[ChromiumElement, SessionElement, ChromiumFrame, NoneElement, List[SessionElement],
            List[Union[ChromiumElement, ChromiumFrame]]]: ...

    def _set_start_options(self,
                           dr_opt: Union[Driver, bool, None],
                           se_opt: Union[Session, SessionOptions, bool, None]) -> None: ...

    def quit(self, timeout: float = 5, force: bool = True) -> None: ...
