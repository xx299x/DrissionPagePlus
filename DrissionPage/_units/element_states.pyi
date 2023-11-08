# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from typing import Union, Tuple, List

from .._elements.chromium_element import ChromiumShadowRoot, ChromiumElement


class ElementStates(object):
    def __init__(self, ele: ChromiumElement):
        self._ele: ChromiumElement = ...

    @property
    def is_selected(self) -> bool: ...

    @property
    def is_checked(self) -> bool: ...

    @property
    def is_displayed(self) -> bool: ...

    @property
    def is_enabled(self) -> bool: ...

    @property
    def is_alive(self) -> bool: ...

    @property
    def is_in_viewport(self) -> bool: ...

    @property
    def is_whole_in_viewport(self) -> bool: ...

    @property
    def is_covered(self) -> bool: ...

    @property
    def has_rect(self) -> Union[bool, List[Tuple[float, float]]]: ...


class ShadowRootStates(object):
    def __init__(self, ele: ChromiumShadowRoot):
        """
        :param ele: ChromiumElement
        """
        self._ele: ChromiumShadowRoot = ...

    @property
    def is_enabled(self) -> bool: ...

    @property
    def is_alive(self) -> bool: ...
