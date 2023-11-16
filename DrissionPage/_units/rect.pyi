# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from typing import Tuple, Union

from .._pages.chromium_frame import ChromiumFrame
from .._pages.chromium_page import ChromiumPage
from .._pages.chromium_tab import ChromiumTab, WebPageTab
from .._pages.web_page import WebPage


class TabRect(object):
    def __init__(self, page: Union[ChromiumPage, ChromiumTab, WebPage, WebPageTab]):
        self._page: Union[ChromiumPage, ChromiumTab, WebPage, WebPageTab] = ...

    @property
    def window_state(self) -> str: ...

    @property
    def window_location(self) -> Tuple[int, int]: ...

    @property
    def page_location(self) -> Tuple[int, int]: ...

    @property
    def viewport_location(self) -> Tuple[int, int]: ...

    @property
    def window_size(self) -> Tuple[int, int]: ...

    @property
    def page_size(self) -> Tuple[int, int]: ...

    @property
    def viewport_size(self) -> Tuple[int, int]: ...

    @property
    def viewport_size_with_scrollbar(self) -> Tuple[int, int]: ...

    def _get_page_rect(self) -> dict: ...

    def _get_window_rect(self) -> dict: ...


class FrameRect(object):
    def __init__(self, frame: ChromiumFrame):
        self._frame: ChromiumFrame = ...

    @property
    def viewport_location(self) -> Tuple[float, float]: ...

    @property
    def size(self) -> Tuple[float, float]: ...

    @property
    def page_size(self) -> Tuple[float, float]: ...

    @property
    def viewport_size(self) -> Tuple[float, float]: ...
