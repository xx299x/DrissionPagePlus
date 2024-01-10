# -*- coding:utf-8 -*-
"""
@Author   : g1879
@Contact  : g1879@qq.com
@Copyright: (c) 2024 by g1879, Inc. All Rights Reserved.
@License  : BSD 3-Clause.
"""
from abc import abstractmethod
from typing import Union, Tuple, List, Any, Optional

from DownloadKit import DownloadKit

from .._elements.none_element import NoneElement


class BaseParser(object):

    def __call__(self, loc_or_str: Union[Tuple[str, str], str], index: int = 1): ...

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, BaseElement],
            index: int = 1,
            timeout: float = None): ...

    def eles(self, loc_or_str: Union[Tuple[str, str], str], timeout=None): ...

    # ----------------以下属性或方法待后代实现----------------
    @property
    def html(self) -> str: ...

    def s_ele(self, loc_or_ele: Union[Tuple[str, str], str, BaseElement], index: int = 1): ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str]): ...

    def _ele(self,
             loc_or_ele,
             timeout: float = None,
             index: Optional[int] = 1,
             raise_err: bool = None,
             method: str = None): ...

    @abstractmethod
    def _find_elements(self,
                       loc_or_ele,
                       timeout: float = None,
                       index: Optional[int] = 1,
                       raise_err: bool = None): ...


class BaseElement(BaseParser):

    def __init__(self, page: BasePage = None):
        self.page: BasePage = ...

    # ----------------以下属性或方法由后代实现----------------
    @property
    def tag(self) -> str: ...

    def _ele(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None,
             index: Optional[int] = 1,
             relative: bool = False,
             raise_err: bool = None,
             method: str = None): ...

    @abstractmethod
    def _find_elements(self, loc_or_str,
                       timeout: float = None,
                       index: Optional[int] = 1,
                       relative: bool = False,
                       raise_err: bool = None): ...

    def parent(self, level_or_loc: Union[tuple, str, int] = 1): ...

    def prev(self, index: int = 1) -> None: ...

    def prevs(self) -> None: ...

    def next(self, index: int = 1): ...

    def nexts(self): ...


class DrissionElement(BaseElement):

    def __init__(self,
                 page: BasePage = ...):
        self.page: BasePage = ...

    @property
    def link(self) -> str: ...

    @property
    def css_path(self) -> str: ...

    @property
    def xpath(self) -> str: ...

    @property
    def comments(self) -> list: ...

    def texts(self, text_node_only: bool = False) -> list: ...

    def parent(self,
               level_or_loc: Union[tuple, str, int] = 1,
               index: int = 1) -> Union[DrissionElement, None]: ...

    def child(self,
              filter_loc: Union[tuple, str, int] = '',
              index: int = 1,
              timeout: float = None,
              ele_only: bool = True) -> Union[DrissionElement, str, NoneElement]: ...

    def prev(self,
             filter_loc: Union[tuple, str, int] = '',
             index: int = 1,
             timeout: float = None,
             ele_only: bool = True) -> Union[DrissionElement, str, NoneElement]: ...

    def next(self,
             filter_loc: Union[tuple, str, int] = '',
             index: int = 1,
             timeout: float = None,
             ele_only: bool = True) -> Union[DrissionElement, str, NoneElement]: ...

    def before(self,
               filter_loc: Union[tuple, str, int] = '',
               index: int = 1,
               timeout: float = None,
               ele_only: bool = True) -> Union[DrissionElement, str, NoneElement]: ...

    def after(self,
              filter_loc: Union[tuple, str, int] = '',
              index: int = 1,
              timeout: float = None,
              ele_only: bool = True) -> Union[DrissionElement, str, NoneElement]: ...

    def children(self,
                 filter_loc: Union[tuple, str] = '',
                 timeout: float = None,
                 ele_only: bool = True) -> List[Union[DrissionElement, str]]: ...

    def prevs(self,
              filter_loc: Union[tuple, str] = '',
              timeout: float = None,
              ele_only: bool = True) -> List[Union[DrissionElement, str]]: ...

    def nexts(self,
              filter_loc: Union[tuple, str] = '',
              timeout: float = None,
              ele_only: bool = True) -> List[Union[DrissionElement, str]]: ...

    def befores(self,
                filter_loc: Union[tuple, str] = '',
                timeout: float = None,
                ele_only: bool = True) -> List[Union[DrissionElement, str]]: ...

    def afters(self,
               filter_loc: Union[tuple, str] = '',
               timeout: float = None,
               ele_only: bool = True) -> List[Union[DrissionElement, str]]: ...

    def _get_relative(self,
                      func: str,
                      direction: str,
                      brother: bool,
                      filter_loc: Union[tuple, str] = '',
                      index: int = 1,
                      timeout: float = None,
                      ele_only: bool = True) -> DrissionElement: ...

    def _get_relatives(self,
                       index: int = None,
                       filter_loc: Union[tuple, str] = '',
                       direction: str = 'following',
                       brother: bool = True,
                       timeout: float = 0.5,
                       ele_only: bool = True) -> List[Union[DrissionElement, str]]: ...

    # ----------------以下属性或方法由后代实现----------------
    @property
    def attrs(self) -> dict: ...

    @property
    def text(self) -> str: ...

    @property
    def raw_text(self) -> str: ...

    @abstractmethod
    def attr(self, attr: str) -> str: ...

    def _get_ele_path(self, mode) -> str: ...


class BasePage(BaseParser):

    def __init__(self):
        self._url_available: bool = ...
        self.retry_times: int = ...
        self.retry_interval: float = ...
        self._timeout: float = ...
        self._download_path: str = ...
        self._DownloadKit: DownloadKit = ...
        self._none_ele_return_value: bool = ...
        self._none_ele_value: Any = ...

    @property
    def title(self) -> Union[str, None]: ...

    @property
    def timeout(self) -> float: ...

    @timeout.setter
    def timeout(self, second: float) -> None: ...

    @property
    def cookies(self) -> dict: ...

    @property
    def url_available(self) -> bool: ...

    @property
    def download_path(self) -> str: ...

    @property
    def download(self) -> DownloadKit: ...

    # ----------------以下属性或方法由后代实现----------------
    @property
    def url(self) -> str: ...

    @property
    def json(self) -> dict: ...

    @property
    def user_agent(self) -> str: ...

    @abstractmethod
    def get_cookies(self, as_dict: bool = False, all_info: bool = False) -> Union[list, dict]: ...

    @abstractmethod
    def get(self, url: str, show_errmsg: bool = False, retry: int = None, interval: float = None): ...

    def _ele(self,
             loc_or_ele,
             timeout: float = None,
             index: Optional[int] = 1,
             raise_err: bool = None,
             method: str = None): ...

    @abstractmethod
    def _find_elements(self,
                       loc_or_ele,
                       timeout: float = None,
                       index: Optional[int] = 1,
                       raise_err: bool = None): ...
