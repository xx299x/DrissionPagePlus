# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from typing import Union, Tuple, List

from session_element import SessionElement
from .chromium_element import ChromiumElement
from .chromium_base import ChromiumBase


class ChromiumFrame(object):
    """frame元素的类。
    frame既是元素，也是页面，可以获取元素属性和定位周边元素，也能跳转到网址。
    同域和异域的frame处理方式不一样，同域的当作元素看待，异域的当作页面看待。"""

    def __init__(self, page: ChromiumBase, ele: ChromiumElement):
        self.frame_ele: ChromiumElement = ...
        self.frame_page: ChromiumBase = ...
        self.page: ChromiumBase = ...
        self.frame_id: str = ...
        self._is_diff_domain: bool = ...
        self.is_loading: bool = ...

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str],
                 timeout: float = ...) -> Union[ChromiumElement, ChromiumFrame, str, None]: ...

    def __repr__(self) -> str: ...

    @property
    def tag(self) -> str: ...

    @property
    def url(self) -> str: ...

    @property
    def html(self) -> str: ...

    @property
    def title(self) -> str: ...

    @property
    def cookies(self) -> dict: ...

    @property
    def inner_html(self) -> str: ...

    @property
    def attrs(self) -> dict: ...

    @property
    def frame_size(self) -> tuple: ...

    @property
    def size(self) -> tuple: ...

    @property
    def obj_id(self) -> str: ...

    @property
    def node_id(self) -> str: ...

    @property
    def location(self) -> dict: ...

    @property
    def is_displayed(self) -> bool: ...

    def get(self,
            url: str,
            show_errmsg: bool = ...,
            retry: int = ...,
            interval: float = ...,
            timeout: float = ...) -> Union[None, bool]: ...

    def refresh(self) -> None: ...

    def ele(self,
            loc_or_str: Union[Tuple[str, str], str, ChromiumElement, 'ChromiumFrame'],
            timeout: float = ...): ...

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = ...): ...

    def s_ele(self, loc_or_str: Union[Tuple[str, str], str, ChromiumElement] = ...) -> Union[
        SessionElement, str, None]: ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str] = ...) -> List[Union[SessionElement, str]]: ...

    def attr(self, attr: str) -> Union[str, None]: ...

    def set_attr(self, attr: str, value: str) -> None: ...

    def remove_attr(self, attr: str) -> None: ...

    def parent(self, level_or_loc: Union[tuple, str, int] = ...) -> Union[ChromiumElement, None]: ...

    def prev(self,
             filter_loc: Union[tuple, str] = ...,
             index: int = ...,
             timeout: float = ...) -> Union[ChromiumElement, str, None]: ...

    def next(self,
             filter_loc: Union[tuple, str] = ...,
             index: int = ...,
             timeout: float = ...) -> Union[ChromiumElement, str, None]: ...

    def before(self,
               filter_loc: Union[tuple, str] = ...,
               index: int = ...,
               timeout: float = ...) -> Union[ChromiumElement, str, None]: ...

    def after(self,
              filter_loc: Union[tuple, str] = ...,
              index: int = ...,
              timeout: float = ...) -> Union[ChromiumElement, str, None]: ...

    def prevs(self,
              filter_loc: Union[tuple, str] = ...,
              timeout: float = ...) -> List[Union[ChromiumElement, str]]: ...

    def nexts(self,
              filter_loc: Union[tuple, str] = ...,
              timeout: float = ...) -> List[Union[ChromiumElement, str]]: ...

    def befores(self,
                filter_loc: Union[tuple, str] = ...,
                timeout: float = ...) -> List[Union[ChromiumElement, str]]: ...
