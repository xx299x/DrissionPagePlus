# -*- coding:utf-8 -*-
from typing import Union, Tuple

from .._elements.chromium_element import ChromiumShadowRoot, ChromiumElement


class ChromiumElementStates(object):
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
    def has_rect(self) -> Union[bool, Tuple[int, int]]: ...


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
