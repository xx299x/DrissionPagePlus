# -*- coding:utf-8 -*-
from typing import Union, List, Any, Tuple

from selenium.webdriver.remote.webdriver import WebDriver as RemoteWebDriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support.select import Select as SeleniumSelect

from .driver_page import DriverPage
from .mix_page import MixPage
from .shadow_root_element import ShadowRootElement
from .base import DrissionElement
from .session_element import SessionElement


class DriverElement(DrissionElement):
    """driver模式的元素对象，包装了一个WebElement对象，并封装了常用功能"""

    def __init__(self, ele: WebElement, page=...):
        self._inner_ele: WebElement = ...
        self._select: Select = ...
        self._scroll: Scroll = ...
        self.page: Union[DriverPage, MixPage] = ...

    def __repr__(self) -> str:
        ...

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str],
                 timeout: float = ...) -> Union['DriverElement', str, None]:
        ...

    # -----------------共有属性和方法-------------------
    @property
    def inner_ele(self) -> WebElement:
        ...

    @property
    def tag(self) -> str:
        ...

    @property
    def html(self) -> str:
        ...

    @property
    def inner_html(self) -> str:
        ...

    @property
    def attrs(self) -> dict:
        ...

    @property
    def text(self) -> str:
        ...

    @property
    def raw_text(self) -> str:
        ...

    def attr(self, attr: str) -> str:
        ...

    def ele(self,
            loc_or_str: Union[Tuple[str, str], str],
            timeout: float = ...) -> Union['DriverElement', str, None]:
        ...

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = ...) -> List[Union['DriverElement', str]]:
        ...

    def s_ele(self, loc_or_str: Union[Tuple[str, str], str] = ...) -> Union[SessionElement, str, None]:
        ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str] = ...) -> List[Union[SessionElement, str]]:
        ...

    def _ele(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = ...,
             single: bool = ...,
             relative: bool = ...) -> Union['DriverElement', str, None, List[Union['DriverElement', str]]]:
        ...

    def _get_ele_path(self, mode) -> str:
        ...

    # -----------------driver独有属性和方法-------------------
    @property
    def size(self) -> dict:
        ...

    @property
    def location(self) -> dict:
        ...

    @property
    def shadow_root(self) -> ShadowRootElement:
        ...

    @property
    def sr(self) -> ShadowRootElement:
        ...

    @property
    def pseudo_before(self) -> str:
        ...

    @property
    def pseudo_after(self) -> str:
        ...

    @property
    def select(self) -> Select:
        ...

    @property
    def scroll(self) -> Scroll:
        ...

    def parent(self, level_or_loc: Union[tuple, str, int] = ...) -> Union['DriverElement', None]:
        ...

    def prev(self,
             index: int = ...,
             filter_loc: Union[tuple, str] = ...,
             timeout: float = ...) -> Union['DriverElement', str, None]:
        ...

    def next(self,
             index: int = ...,
             filter_loc: Union[tuple, str] = ...,
             timeout: float = ...) -> Union['DriverElement', str, None]:
        ...

    def before(self,
               index: int = ...,
               filter_loc: Union[tuple, str] = ...,
               timeout: float = ...) -> Union['DriverElement', str, None]:
        ...

    def after(self,
              index: int = ...,
              filter_loc: Union[tuple, str] = ...,
              timeout: float = ...) -> Union['DriverElement', str, None]:
        ...

    def prevs(self,
              filter_loc: Union[tuple, str] = ...,
              timeout: float = ...) -> List[Union['DriverElement', str]]:
        ...

    def nexts(self,
              filter_loc: Union[tuple, str] = ...,
              timeout: float = ...) -> List[Union['DriverElement', str]]:
        ...

    def befores(self,
                filter_loc: Union[tuple, str] = ...,
                timeout: float = ...) -> List[Union['DriverElement', str]]:
        ...

    def afters(self,
               filter_loc: Union[tuple, str] = ...,
               timeout: float = ...) -> List[Union['DriverElement', str]]:
        ...

    def left(self, index: int = ..., filter_loc: Union[tuple, str] = ...) -> DriverElement:
        ...

    def right(self, index: int = ..., filter_loc: Union[tuple, str] = ...) -> 'DriverElement':
        ...

    def above(self, index: int = ..., filter_loc: Union[tuple, str] = ...) -> 'DriverElement':
        ...

    def below(self, index: int = ..., filter_loc: Union[tuple, str] = ...) -> 'DriverElement':
        ...

    def near(self, index: int = ..., filter_loc: Union[tuple, str] = ...) -> 'DriverElement':
        ...

    def lefts(self, filter_loc: Union[tuple, str] = ...) -> List['DriverElement']:
        ...

    def rights(self, filter_loc: Union[tuple, str] = ...) -> List['DriverElement']:
        ...

    def aboves(self, filter_loc: Union[tuple, str] = ...) -> List['DriverElement']:
        ...

    def belows(self, filter_loc: Union[tuple, str] = ...) -> List['DriverElement']:
        ...

    def nears(self, filter_loc: Union[tuple, str] = ...) -> List['DriverElement']:
        ...

    def wait_ele(self,
                 loc_or_ele: Union[str, tuple, DrissionElement, WebElement],
                 timeout: float = ...) -> 'ElementWaiter':
        ...

    def style(self, style: str, pseudo_ele: str = ...) -> str:
        ...

    def click(self, by_js: bool = ..., timeout: float = ...) -> bool:
        ...

    def click_at(self,
                 x: Union[int, str] = ...,
                 y: Union[int, str] = ...,
                 by_js: bool = ...) -> None:
        ...

    def r_click(self) -> None:
        ...

    def r_click_at(self, x: Union[int, str] = ..., y: Union[int, str] = ...) -> None:
        ...

    def input(self,
              vals: Union[str, tuple],
              clear: bool = ...,
              insure: bool = ...,
              timeout: float = ...) -> bool:
        ...

    def run_script(self, script: str, *args) -> Any:
        ...

    def submit(self) -> Union[bool, None]:
        ...

    def clear(self, insure: bool = ...) -> Union[None, bool]:
        ...

    def is_selected(self) -> bool:
        ...

    def is_enabled(self) -> bool:
        ...

    def is_displayed(self) -> bool:
        ...

    def is_valid(self) -> bool:
        ...

    def screenshot(self, path: str = ..., filename: str = ..., as_bytes: bool = ...) -> Union[str, bytes]:
        ...

    def prop(self, prop: str) -> str:
        ...

    def set_prop(self, prop: str, value: str) -> bool:
        ...

    def set_attr(self, attr: str, value: str) -> bool:
        ...

    def remove_attr(self, attr: str) -> bool:
        """删除元素attribute属性          \n
        :param attr: 属性名
        :return: 是否删除成功
        """
        try:
            self.run_script(f'arguments[0].removeAttribute("{attr}");')
            return True
        except Exception:
            return False

    def drag(self, x: int, y: int, speed: int = ..., shake: bool = ...) -> None:
        ...

    def drag_to(self,
                ele_or_loc: Union[tuple, WebElement, DrissionElement],
                speed: int = ...,
                shake: bool = ...) -> None:
        ...

    def hover(self, x: int = ..., y: int = ...) -> None:
        ...

    def _get_relative_eles(self,
                           mode: str,
                           loc: Union[tuple, str] = ...) -> Union[List['DriverElement'], 'DriverElement']:
        ...


def make_driver_ele(page_or_ele,
                    loc: Union[str, Tuple[str, str]],
                    single: bool = ...,
                    timeout: float = ...) -> Union[DriverElement, str, None, List[Union[DriverElement, str]]]: ...


class ElementsByXpath(object):
    """用js通过xpath获取元素、节点或属性，与WebDriverWait配合使用"""

    def __init__(self, page, xpath: str = ..., single: bool = ..., timeout: float = ...):
        self.single: bool = ...
        self.xpath: str = ...
        self.page: Union[MixPage, DriverPage] = ...

    def __call__(self, ele_or_driver: Union[RemoteWebDriver, WebElement]) \
            -> Union[str, DriverElement, None, List[str or DriverElement]]: ...


class Select(object):
    """Select 类专门用于处理 d 模式下 select 标签"""

    def __init__(self, ele: DriverElement):
        self.select_ele: SeleniumSelect = ...
        self.inner_ele: DriverElement = ...

    def __call__(self, text_or_index: Union[str, int, list, tuple], timeout=...) -> bool: ...

    @property
    def is_multi(self) -> bool: ...

    @property
    def options(self) -> List[DriverElement]: ...

    @property
    def selected_option(self) -> Union[DriverElement, None]: ...

    @property
    def selected_options(self) -> List[DriverElement]: ...

    def clear(self) -> None: ...

    def select(self, text_or_index: Union[str, int, list, tuple], timeout=...) -> bool: ...

    def select_by_value(self, value: Union[str, list, tuple], timeout=...) -> bool: ...

    def deselect(self, text_or_index: Union[str, int, list, tuple], timeout=...) -> bool: ...

    def deselect_by_value(self, value: Union[str, list, tuple], timeout=...) -> bool: ...

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


class ElementWaiter(object):
    """等待元素在dom中某种状态，如删除、显示、隐藏"""

    def __init__(self,
                 page_or_ele,
                 loc_or_ele: Union[str, tuple, DriverElement, WebElement],
                 timeout: float = ...):
        self.target: Union[DriverElement, WebElement, tuple] = ...
        self.timeout: float = ...
        self.driver: Union[WebElement, RemoteWebDriver] = ...

    def delete(self) -> bool: ...

    def display(self) -> bool: ...

    def hidden(self) -> bool: ...

    def _wait_ele(self, mode: str) -> bool: ...


class Scroll(object):
    """用于滚动的对象"""

    def __init__(self, page_or_ele):
        self.driver: Union[DriverElement, DriverPage] = ...
        self.t1: str = ...
        self.t2: str = ...

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
