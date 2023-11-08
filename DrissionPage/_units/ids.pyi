# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from typing import Union

from .._pages.chromium_frame import ChromiumFrame
from .._elements.chromium_element import ChromiumElement, ChromiumShadowRoot


class Ids(object):
    def __init__(self, ele: Union[ChromiumElement, ChromiumShadowRoot]):
        self._ele: Union[ChromiumElement, ChromiumShadowRoot] = ...

    @property
    def node_id(self) -> str: ...

    @property
    def obj_id(self) -> str: ...

    @property
    def backend_id(self) -> str: ...


class ElementIds(Ids):
    @property
    def doc_id(self) -> str: ...


class FrameIds(object):
    def __init__(self, frame: ChromiumFrame):
        self._frame: ChromiumFrame = ...

    @property
    def tab_id(self) -> str: ...

    @property
    def backend_id(self) -> str: ...

    @property
    def obj_id(self) -> str: ...

    @property
    def node_id(self) -> str: ...
