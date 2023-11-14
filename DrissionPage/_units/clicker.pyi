# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from typing import Union

from .._elements.chromium_element import ChromiumElement


class Clicker(object):
    def __init__(self, ele: ChromiumElement):
        self._ele: ChromiumElement = ...

    def __call__(self, by_js: Union[None, bool] = False, timeout: float = 2, wait_stop: bool = True) -> bool: ...

    def left(self, by_js: Union[None, bool] = False, timeout: float = 2, wait_stop: bool = True) -> bool: ...

    def right(self) -> None: ...

    def middle(self) -> None: ...

    def at(self, offset_x: float = None, offset_y: float = None, button: str = 'left', count: int = 1) -> None: ...

    def twice(self, by_js: bool = False) -> None: ...

    def _click(self, client_x: float, client_y: float, button: str = 'left', count: int = 1) -> None: ...
