# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from pathlib import Path
from typing import Union, Tuple, List, Any

from .none_element import NoneElement
from .._base.base import DrissionElement, BaseElement
from .._elements.session_element import SessionElement
from .._pages.chromium_base import ChromiumBase
from .._pages.chromium_frame import ChromiumFrame
from .._pages.chromium_page import ChromiumPage
from .._pages.web_page import WebPage
from .._units.clicker import Clicker
from .._units.rect import ElementRect
from .._units.scroller import ElementScroller
from .._units.selector import SelectElement
from .._units.setter import ChromiumElementSetter
from .._units.states import ShadowRootStates, ElementStates
from .._units.waiter import ElementWaiter


class ChromiumElement(DrissionElement):

    def __init__(self, page: ChromiumBase, node_id: str = None, obj_id: str = None, backend_id: str = None):
        self._tag: str = ...
        self.page: Union[ChromiumPage, WebPage] = ...
        self._node_id: str = ...
        self._obj_id: str = ...
        self._backend_id: str = ...
        self._doc_id: str = ...
        self._scroll: ElementScroller = ...
        self._clicker: Clicker = ...
        self._select: SelectElement = ...
        self._wait: ElementWaiter = ...
        self._rect: ElementRect = ...
        self._set: ChromiumElementSetter = ...
        self._states: ElementStates = ...
        self._pseudo: Pseudo = ...

    def __repr__(self) -> str: ...

    def __call__(self, loc_or_str: Union[Tuple[str, str], str],
                 timeout: float = None) -> Union[ChromiumElement, str, None]: ...

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
    def set(self) -> ChromiumElementSetter: ...

    @property
    def states(self) -> ElementStates: ...

    @property
    def rect(self) -> ElementRect: ...

    @property
    def pseudo(self) -> Pseudo: ...

    @property
    def shadow_root(self) -> Union[None, ChromiumShadowRoot]: ...

    @property
    def sr(self) -> Union[None, ChromiumShadowRoot]: ...

    @property
    def scroll(self) -> ElementScroller: ...

    @property
    def click(self) -> Clicker: ...

    def parent(self, level_or_loc: Union[tuple, str, int] = 1, index: int = 1) -> Union[ChromiumElement, None]: ...

    def child(self, filter_loc: Union[tuple, str, int] = '',
              index: int = 1,
              timeout: float = 0,
              ele_only: bool = True) -> Union[ChromiumElement, str, None]: ...

    def prev(self, filter_loc: Union[tuple, str, int] = '',
             index: int = 1,
             timeout: float = 0,
             ele_only: bool = True) -> Union[ChromiumElement, str, None]: ...

    def next(self, filter_loc: Union[tuple, str, int] = '',
             index: int = 1,
             timeout: float = 0,
             ele_only: bool = True) -> Union[ChromiumElement, str, None]: ...

    def before(self, filter_loc: Union[tuple, str, int] = '',
               index: int = 1,
               timeout: float = None,
               ele_only: bool = True) -> Union[ChromiumElement, str, None]: ...

    def after(self, filter_loc: Union[tuple, str, int] = '',
              index: int = 1,
              timeout: float = None,
              ele_only: bool = True) -> Union[ChromiumElement, str, None]: ...

    def children(self, filter_loc: Union[tuple, str] = '',
                 timeout: float = 0,
                 ele_only: bool = True) -> List[Union[ChromiumElement, str]]: ...

    def prevs(self, filter_loc: Union[tuple, str] = '',
              timeout: float = 0,
              ele_only: bool = True) -> List[Union[ChromiumElement, str]]: ...

    def nexts(self, filter_loc: Union[tuple, str] = '',
              timeout: float = 0,
              ele_only: bool = True) -> List[Union[ChromiumElement, str]]: ...

    def befores(self, filter_loc: Union[tuple, str] = '',
                timeout: float = None,
                ele_only: bool = True) -> List[Union[ChromiumElement, str]]: ...

    def afters(self, filter_loc: Union[tuple, str] = '',
               timeout: float = None,
               ele_only: bool = True) -> List[Union[ChromiumElement, str]]: ...

    @property
    def wait(self) -> ElementWaiter: ...

    @property
    def select(self) -> SelectElement: ...

    def check(self, uncheck: bool = False) -> None: ...

    def attr(self, attr: str) -> Union[str, None]: ...

    def remove_attr(self, attr: str) -> None: ...

    def prop(self, prop: str) -> Union[str, int, None]: ...

    def run_js(self, script: str, *args, as_expr: bool = False, timeout: float = None) -> Any: ...

    def run_async_js(self, script: str, *args, as_expr: bool = False, timeout: float = None) -> None: ...

    def ele(self, loc_or_str: Union[Tuple[str, str], str],
            timeout: float = None) -> Union[ChromiumElement, str]: ...

    def eles(self, loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None) -> List[Union[ChromiumElement, str]]: ...

    def s_ele(self, loc_or_str: Union[Tuple[str, str], str] = None) -> Union[SessionElement, str, NoneElement]: ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str] = None) -> List[Union[SessionElement, str]]: ...

    def _find_elements(self, loc_or_str: Union[Tuple[str, str], str], timeout: float = None,
                       single: bool = True, relative: bool = False, raise_err: bool = False) \
            -> Union[ChromiumElement, ChromiumFrame, str, NoneElement,
            List[Union[ChromiumElement, ChromiumFrame, str]]]: ...

    def style(self, style: str, pseudo_ele: str = '') -> str: ...

    def get_src(self, timeout: float = None, base64_to_bytes: bool = True) -> Union[bytes, str, None]: ...

    def save(self, path: [str, bool] = None, name: str = None, timeout: float = None) -> str: ...

    def get_screenshot(self, path: [str, Path] = None, name: str = None, as_bytes: [bool, str] = None,
                       as_base64: [bool, str] = None, scroll_to_center: bool = True) -> Union[str, bytes]: ...

    def input(self, vals: Any, clear: bool = True, by_js: bool = False) -> None: ...

    def _set_file_input(self, files: Union[str, list, tuple]) -> None: ...

    def clear(self, by_js: bool = False) -> None: ...

    def _input_focus(self) -> None: ...

    def focus(self) -> None: ...

    def hover(self, offset_x: int = None, offset_y: int = None) -> None: ...

    def drag(self, offset_x: int = 0, offset_y: int = 0, duration: float = 0.5) -> None: ...

    def drag_to(self, ele_or_loc: Union[tuple, ChromiumElement], duration: float = 0.5) -> None: ...

    def _get_obj_id(self, node_id: str = None, backend_id: str = None) -> str: ...

    def _get_node_id(self, obj_id: str = None, backend_id: str = None) -> str: ...

    def _get_backend_id(self, node_id: str) -> str: ...

    def _get_ele_path(self, mode: str) -> str: ...


class ChromiumShadowRoot(BaseElement):

    def __init__(self, parent_ele: ChromiumElement, obj_id: str = None, backend_id: str = None):
        self._obj_id: str = ...
        self._node_id: str = ...
        self._backend_id: str = ...
        self.page: ChromiumPage = ...
        self.parent_ele: ChromiumElement = ...
        self._states: ShadowRootStates = ...

    def __repr__(self) -> str: ...

    def __call__(self, loc_or_str: Union[Tuple[str, str], str],
                 timeout: float = None) -> ChromiumElement: ...

    @property
    def states(self) -> ShadowRootStates: ...

    @property
    def tag(self) -> str: ...

    @property
    def html(self) -> str: ...

    @property
    def inner_html(self) -> str: ...

    def run_js(self, script: str, *args, as_expr: bool = False, timeout: float = None) -> Any: ...

    def run_async_js(self, script: str, *args, as_expr: bool = False, timeout: float = None) -> None: ...

    def parent(self, level_or_loc: Union[str, int] = 1, index: int = 1) -> ChromiumElement: ...

    def child(self, filter_loc: Union[tuple, str] = '',
              index: int = 1) -> Union[ChromiumElement, str, None]: ...

    def next(self, filter_loc: Union[tuple, str] = '',
             index: int = 1) -> Union[ChromiumElement, str, None]: ...

    def before(self, filter_loc: Union[tuple, str] = '',
               index: int = 1) -> Union[ChromiumElement, str, None]: ...

    def after(self, filter_loc: Union[tuple, str] = '',
              index: int = 1) -> Union[ChromiumElement, str, None]: ...

    def children(self, filter_loc: Union[tuple, str] = '') -> List[Union[ChromiumElement, str]]: ...

    def nexts(self, filter_loc: Union[tuple, str] = '') -> List[Union[ChromiumElement, str]]: ...

    def befores(self, filter_loc: Union[tuple, str] = '') -> List[Union[ChromiumElement, str]]: ...

    def afters(self, filter_loc: Union[tuple, str] = '') -> List[Union[ChromiumElement, str]]: ...

    def ele(self, loc_or_str: Union[Tuple[str, str], str],
            timeout: float = None) -> Union[ChromiumElement]: ...

    def eles(self, loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None) -> List[ChromiumElement]: ...

    def s_ele(self, loc_or_str: Union[Tuple[str, str], str] = None) -> Union[SessionElement, str, NoneElement]: ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str]) -> List[Union[SessionElement, str]]: ...

    def _find_elements(self, loc_or_str: Union[Tuple[str, str], str], timeout: float = None,
                       single: bool = True, relative: bool = False, raise_err: bool = None) \
            -> Union[ChromiumElement, ChromiumFrame, NoneElement, str, List[Union[ChromiumElement,
            ChromiumFrame, str]]]: ...

    def _get_node_id(self, obj_id: str) -> str: ...

    def _get_obj_id(self, back_id: str) -> str: ...

    def _get_backend_id(self, node_id: str) -> str: ...


def find_in_chromium_ele(ele: ChromiumElement, loc: Union[str, Tuple[str, str]],
                         single: bool = True, timeout: float = None, relative: bool = True) \
        -> Union[ChromiumElement, str, NoneElement, List[Union[ChromiumElement, str]]]: ...


def find_by_xpath(ele: ChromiumElement, xpath: str, single: bool, timeout: float,
                  relative: bool = True) -> Union[ChromiumElement, List[ChromiumElement], NoneElement]: ...


def find_by_css(ele: ChromiumElement, selector: str, single: bool,
                timeout: float) -> Union[ChromiumElement, List[ChromiumElement], NoneElement]: ...


def make_chromium_ele(page: ChromiumBase, node_id: str = ..., obj_id: str = ...) \
        -> Union[ChromiumElement, ChromiumFrame, str]: ...


def make_js_for_find_ele_by_xpath(xpath: str, type_txt: str, node_txt: str) -> str: ...


def run_js(page_or_ele: Union[ChromiumBase, ChromiumElement, ChromiumShadowRoot], script: str,
           as_expr: bool = False, timeout: float = None, args: tuple = ...) -> Any: ...


def parse_js_result(page: ChromiumBase, ele: ChromiumElement, result: dict): ...


def convert_argument(arg: Any) -> dict: ...


def send_enter(ele: ChromiumElement) -> None: ...


def send_key(ele: ChromiumElement, modifier: int, key: str) -> None: ...


class Pseudo(object):
    def __init__(self, ele: ChromiumElement):
        self._ele: ChromiumElement = ...

    @property
    def before(self) -> str: ...

    @property
    def after(self) -> str: ...
