# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from pathlib import Path
from typing import Union, Tuple, List, Any

from .base import DrissionElement, BaseElement
from .chromium_base import ChromiumBase
from .chromium_frame import ChromiumFrame
from .chromium_page import ChromiumPage
from .session_element import SessionElement
from .web_page import WebPage


class ChromiumElement(DrissionElement):
    """ChromePage页面对象中的元素对象"""

    def __init__(self,
                 page: ChromiumBase,
                 node_id: str = ..., obj_id: str = ..., backend_id: str = ...):
        self._tag: str = ...
        self.page: Union[ChromiumPage, WebPage] = ...
        self._node_id: str = ...
        self._obj_id: str = ...
        self._backend_id: str = ...
        self._doc_id: str = ...
        self._scroll: ChromeScroll = ...
        self._select: ChromeSelect = ...

    def __repr__(self) -> str: ...

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str],
                 timeout: float = ...) -> Union[ChromiumElement, ChromiumFrame, str, None]: ...

    @property
    def tag(self) -> str: ...

    @property
    def html(self) -> str: ...

    @property
    def inner_html(self) -> str: ...

    @property
    def attrs(self) -> dict: ...

    @property
    def text(self) -> str: ...

    @property
    def raw_text(self) -> str: ...

    # -----------------d模式独有属性-------------------
    @property
    def obj_id(self) -> str: ...

    @property
    def node_id(self) -> str: ...

    @property
    def backend_id(self) -> str: ...

    @property
    def doc_id(self) -> str: ...

    @property
    def size(self) -> tuple: ...

    @property
    def client_location(self) -> tuple: ...

    @property
    def client_midpoint(self) -> tuple: ...

    @property
    def location(self) -> tuple: ...

    @property
    def midpoint(self) -> tuple: ...

    @property
    def _client_click_point(self) -> tuple: ...

    @property
    def _click_point(self) -> tuple: ...

    @property
    def shadow_root(self) -> Union[None, ChromiumShadowRootElement]: ...

    @property
    def sr(self) -> Union[None, ChromiumShadowRootElement]: ...

    @property
    def pseudo_before(self) -> str: ...

    @property
    def pseudo_after(self) -> str: ...

    @property
    def scroll(self) -> ChromeScroll: ...

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

    def wait_ele(self,
                 loc_or_ele: Union[str, tuple, ChromiumElement],
                 timeout: float = ...) -> 'ChromiumElementWaiter': ...

    @property
    def select(self) -> 'ChromeSelect': ...

    @property
    def is_selected(self) -> bool: ...

    @property
    def is_displayed(self) -> bool: ...

    @property
    def is_enabled(self) -> bool: ...

    @property
    def is_alive(self) -> bool: ...

    @property
    def is_in_viewport(self) -> bool: ...

    def attr(self, attr: str) -> Union[str, None]: ...

    def set_attr(self, attr: str, value: str) -> None: ...

    def remove_attr(self, attr: str) -> None: ...

    def prop(self, prop: str) -> Union[str, int, None]: ...

    def set_prop(self, prop: str, value: str) -> None: ...

    def set_innerHTML(self, html: str) -> None: ...

    def run_script(self, script: str, as_expr: bool = ..., *args: Any) -> Any: ...

    def run_async_script(self, script: str, as_expr: bool = ..., *args: Any) -> None: ...

    def ele(self,
            loc_or_str: Union[Tuple[str, str], str],
            timeout: float = ...) -> Union[ChromiumElement, ChromiumFrame, str, None]: ...

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = ...) -> List[Union[ChromiumElement, ChromiumFrame, str]]: ...

    def s_ele(self, loc_or_str: Union[Tuple[str, str], str] = ...) -> Union[SessionElement, str, None]: ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str] = ...) -> List[Union[SessionElement, str]]: ...

    def _ele(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = ..., single: bool = ..., relative: bool = ...) \
            -> Union[ChromiumElement, ChromiumFrame, str, None, List[Union[ChromiumElement, ChromiumFrame, str]]]: ...

    def style(self, style: str, pseudo_ele: str = ...) -> str: ...

    def get_src(self) -> Union[bytes, str, None]: ...

    def save(self, path: [str, bool] = ..., rename: str = ...) -> None: ...

    def get_screenshot(self, path: [str, Path] = ..., as_bytes: [bool, str] = ...) -> Union[str, bytes]: ...

    def input(self, vals: Union[str, tuple, list], clear: bool = ...) -> None: ...

    def _set_file_input(self, files: Union[str, list, tuple]) -> None: ...

    def clear(self, by_js: bool = ...) -> None: ...

    def click(self, by_js: bool = ..., retry: bool = ..., timeout: float = ..., wait_loading: bool = ...) -> bool: ...

    def click_at(self, offset_x: Union[int, str] = ..., offset_y: Union[int, str] = ..., button: str = ...) -> None: ...

    def r_click(self) -> None: ...

    def r_click_at(self, offset_x: Union[int, str] = ..., offset_y: Union[int, str] = ...) -> None: ...

    def _click(self, client_x: int, client_y: int, button: str = ...) -> None: ...

    def hover(self, offset_x: int = ..., offset_y: int = ...) -> None: ...

    def drag(self, offset_x: int = ..., offset_y: int = ..., speed: int = ..., shake: bool = ...) -> None: ...

    def drag_to(self,
                ele_or_loc: Union[tuple, ChromiumElement],
                speed: int = ...,
                shake: bool = ...) -> None: ...

    def _get_obj_id(self, node_id: str = ..., backend_id: str = ...) -> str: ...

    def _get_node_id(self, obj_id: str = ..., backend_id: str = ...) -> str: ...

    def _get_backend_id(self, node_id: str) -> str: ...

    def _get_ele_path(self, mode: str) -> str: ...

    def _get_client_rect(self, quad: str) -> Union[list, None]: ...

    def _get_absolute_rect(self, x: int, y: int) -> tuple: ...


class ChromiumShadowRootElement(BaseElement):
    """ChromiumShadowRootElement是用于处理ShadowRoot的类，使用方法和ChromiumElement基本一致"""

    def __init__(self,
                 parent_ele: ChromiumElement,
                 obj_id: str = ...,
                 backend_id: str = ...):
        self._obj_id: str = ...
        self._node_id: str = ...
        self._backend_id: str = ...
        self.page: ChromiumPage = ...
        self.parent_ele: ChromiumElement = ...

    def __repr__(self) -> str: ...

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str],
                 timeout: float = ...) -> Union[ChromiumElement, ChromiumFrame, None]: ...

    @property
    def is_enabled(self) -> bool: ...

    @property
    def is_alive(self) -> bool: ...

    @property
    def node_id(self) -> str: ...

    @property
    def obj_id(self) -> str: ...

    @property
    def backend_id(self) -> str: ...

    @property
    def tag(self) -> str: ...

    @property
    def html(self) -> str: ...

    @property
    def inner_html(self) -> str: ...

    def run_script(self, script: str, as_expr: bool = ..., *args: Any) -> Any: ...

    def run_async_script(self, script: str, as_expr: bool = ..., *args: Any) -> None: ...

    def parent(self, level_or_loc: Union[str, int] = ...) -> ChromiumElement: ...

    def next(self,
             filter_loc: Union[tuple, str] = ...,
             index: int = ...) -> Union[ChromiumElement, str, None]: ...

    def before(self,
               filter_loc: Union[tuple, str] = ...,
               index: int = ...) -> Union[ChromiumElement, str, None]: ...

    def after(self,
              filter_loc: Union[tuple, str] = ...,
              index: int = ...) -> Union[ChromiumElement, str, None]: ...

    def nexts(self, filter_loc: Union[tuple, str] = ...) -> List[Union[ChromiumElement, str]]: ...

    def befores(self, filter_loc: Union[tuple, str] = ...) -> List[Union[ChromiumElement, str]]: ...

    def afters(self, filter_loc: Union[tuple, str] = ...) -> List[Union[ChromiumElement, str]]: ...

    def ele(self,
            loc_or_str: Union[Tuple[str, str], str],
            timeout: float = ...) -> Union[ChromiumElement, ChromiumFrame, None]: ...

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = ...) -> List[Union[ChromiumElement, ChromiumFrame]]: ...

    def s_ele(self, loc_or_str: Union[Tuple[str, str], str] = ...) -> Union[SessionElement, str, None]: ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str]) -> List[Union[SessionElement, str]]: ...

    def _ele(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = ...,
             single: bool = ..., relative: bool = ...) \
            -> Union[ChromiumElement, ChromiumFrame, None, str, List[Union[ChromiumElement, ChromiumFrame, str]]]: ...

    def _get_node_id(self, obj_id: str) -> str: ...

    def _get_obj_id(self, back_id: str) -> str: ...

    def _get_backend_id(self, node_id: str) -> str: ...


def find_in_chromium_ele(ele: ChromiumElement,
                         loc: Union[str, Tuple[str, str]],
                         single: bool = ...,
                         timeout: float = ...,
                         relative: bool = ...) -> Union[
    ChromiumElement, str, None, List[Union[ChromiumElement, str]]]: ...


def _find_by_xpath(ele: ChromiumElement,
                   xpath: str,
                   single: bool,
                   timeout: float,
                   relative: bool = ...) -> Union[ChromiumElement, List[ChromiumElement], None]: ...


def _find_by_css(ele: ChromiumElement,
                 selector: str,
                 single: bool,
                 timeout: float) -> Union[ChromiumElement, List[ChromiumElement], None]: ...


def make_chromium_ele(page: ChromiumBase, node_id: str = ..., obj_id: str = ...) -> Union[
    ChromiumElement, ChromiumFrame]: ...


def _make_js_for_find_ele_by_xpath(xpath: str, type_txt: str, node_txt: str) -> str: ...


def run_script(page_or_ele: Union[ChromiumBase, ChromiumElement, ChromiumShadowRootElement], script: str,
               as_expr: bool = ..., timeout: float = ..., args: tuple = ..., not_change: bool = ...) -> Any: ...


def _parse_js_result(page: ChromiumBase, ele: ChromiumElement, result: dict): ...


def _convert_argument(arg: Any) -> dict: ...


def _send_enter(ele: ChromiumElement) -> None: ...


def _send_key(ele: ChromiumElement, modifier: int, key: str) -> None: ...


def _offset_scroll(ele: ChromiumElement, offset_x: int, offset_y: int) -> tuple: ...


class ChromeScroll(object):
    """用于滚动的对象"""

    def __init__(self, page_or_ele: Union[ChromiumBase, ChromiumElement]):
        self.t1: str = ...
        self.t2: str = ...
        self.obj_id: str = ...
        self.page: ChromiumPage = ...

    def _run_script(self, js: str): ...

    def to_top(self) -> None: ...

    def to_bottom(self) -> None: ...

    def to_half(self) -> None: ...

    def to_rightmost(self) -> None: ...

    def to_leftmost(self) -> None: ...

    def to_location(self, x: int, y: int) -> None: ...

    def up(self, pixel: int = ...) -> None: ...

    def down(self, pixel: int = ...) -> None: ...

    def left(self, pixel: int = ...) -> None: ...

    def right(self, pixel: int = ...) -> None: ...


class ChromeSelect(object):
    """ChromeSelect 类专门用于处理 d 模式下 select 标签"""

    def __init__(self, ele: ChromiumElement):
        self._ele: ChromiumElement = ...

    def __call__(self, text_or_index: Union[str, int, list, tuple], timeout: float = None) -> bool: ...

    @property
    def is_multi(self) -> bool: ...

    @property
    def options(self) -> List[ChromiumElement]: ...

    @property
    def selected_option(self) -> Union[ChromiumElement, None]: ...

    @property
    def selected_options(self) -> List[ChromiumElement]: ...

    def clear(self) -> None: ...

    def by_text(self, text: Union[str, list, tuple], timeout: float = ...) -> bool: ...

    def by_value(self, value: Union[str, list, tuple], timeout: float = ...) -> bool: ...

    def by_index(self, index: Union[int, list, tuple], timeout: float = ...) -> bool: ...

    def cancel_by_text(self, text: Union[str, list, tuple], timeout: float = ...) -> bool: ...

    def cancel_by_value(self, value: Union[str, list, tuple], timeout: float = ...) -> bool: ...

    def cancel_by_index(self, index: Union[int, list, tuple], timeout: float = ...) -> bool: ...

    def invert(self) -> None: ...

    def _select(self,
                text_value_index: Union[str, int, list, tuple] = ...,
                para_type: str = ...,
                deselect: bool = ...,
                timeout: float = ...) -> bool: ...

    def _select_multi(self,
                      text_value_index: Union[list, tuple] = ...,
                      para_type: str = ...,
                      deselect: bool = ...) -> bool: ...


class ChromiumElementWaiter(object):
    """等待元素在dom中某种状态，如删除、显示、隐藏"""

    def __init__(self,
                 page_or_ele: Union[ChromiumBase, ChromiumElement],
                 loc_or_ele: Union[str, tuple, ChromiumElement],
                 timeout: float = ...):
        self.loc_or_ele: Union[str, tuple, ChromiumElement] = ...
        self.timeout: float = ...
        self.driver: Union[ChromiumPage, ChromiumPage] = ...

    def delete(self) -> bool: ...

    def display(self) -> bool: ...

    def hidden(self) -> bool: ...

    def _wait_ele(self, mode: str) -> Union[None, bool]: ...
