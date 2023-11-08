# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from typing import Tuple, Union, List

from .._elements.chromium_element import ChromiumElement


class Locations(object):
    def __init__(self, ele: ChromiumElement):
        self._ele: ChromiumElement = ...

    @property
    def location(self) -> Tuple[int, int]: ...

    @property
    def midpoint(self) -> Tuple[int, int]: ...

    @property
    def click_point(self) -> Tuple[int, int]: ...

    @property
    def viewport_location(self) -> Tuple[int, int]: ...

    @property
    def viewport_midpoint(self) -> Tuple[int, int]: ...

    @property
    def viewport_click_point(self) -> Tuple[int, int]: ...

    @property
    def screen_location(self) -> Tuple[int, int]: ...

    @property
    def screen_midpoint(self) -> Tuple[int, int]: ...

    @property
    def screen_click_point(self) -> Tuple[int, int]: ...

    @property
    def rect(self) -> List[Tuple[int, int], ...]: ...

    @property
    def viewport_rect(self) -> List[Tuple[int, int], ...]: ...

    def _get_viewport_rect(self, quad: str) -> Union[list, None]: ...

    def _get_page_coord(self, x: int, y: int) -> Tuple[int, int]: ...
