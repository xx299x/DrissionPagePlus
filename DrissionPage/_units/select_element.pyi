# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from typing import Union, Tuple, List

from .._elements.chromium_element import ChromiumElement


class SelectElement(object):
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

    def all(self) -> None: ...

    def by_text(self, text: Union[str, list, tuple], timeout: float = None) -> bool: ...

    def by_value(self, value: Union[str, list, tuple], timeout: float = None) -> bool: ...

    def by_index(self, index: Union[int, list, tuple], timeout: float = None) -> bool: ...

    def by_loc(self, loc: Union[str, Tuple[str, str]], timeout: float = None) -> bool: ...

    def cancel_by_text(self, text: Union[str, list, tuple], timeout: float = None) -> bool: ...

    def cancel_by_value(self, value: Union[str, list, tuple], timeout: float = None) -> bool: ...

    def cancel_by_index(self, index: Union[int, list, tuple], timeout: float = None) -> bool: ...

    def cancel_by_loc(self, loc: Union[str, Tuple[str, str]], timeout: float = None) -> bool: ...

    def invert(self) -> None: ...

    def _by_loc(self, loc: Union[str, Tuple[str, str]], timeout: float = None, cancel: bool = False) -> bool: ...

    def _select(self,
                condition: Union[str, int, list, tuple] = None,
                para_type: str = 'text',
                cancel: bool = False,
                timeout: float = None) -> bool: ...

    def _text_value(self, condition: Union[list, set], para_type: str, mode: str, timeout: float) -> bool: ...

    def _index(self, condition: set, mode: str, timeout: float) -> bool: ...

    def _dispatch_change(self) -> None: ...