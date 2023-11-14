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
    def location(self) -> Tuple[float, float]: ...

    @property
    def midpoint(self) -> Tuple[float, float]: ...

    @property
    def click_point(self) -> Tuple[float, float]: ...

    @property
    def viewport_location(self) -> Tuple[float, float]: ...

    @property
    def viewport_midpoint(self) -> Tuple[float, float]: ...

    @property
    def viewport_click_point(self) -> Tuple[float, float]: ...

    @property
    def screen_location(self) -> Tuple[float, float]: ...

    @property
    def screen_midpoint(self) -> Tuple[float, float]: ...

    @property
    def screen_click_point(self) -> Tuple[float, float]: ...

    @property
    def rect(self) -> List[Tuple[float, float], ...]: ...

    @property
    def viewport_rect(self) -> List[Tuple[float, float], ...]: ...

    def _get_viewport_rect(self, quad: str) -> Union[list, None]: ...

    def _get_page_coord(self, x: float, y: float) -> Tuple[float, float]: ...
