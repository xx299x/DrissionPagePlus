# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from typing import Tuple, Union

from .._pages.chromium_page import ChromiumPage
from .._pages.chromium_tab import ChromiumTab


class ChromiumTabRect(object):
    def __init__(self, page: Union[ChromiumPage, ChromiumTab]):
        self._page: Union[ChromiumPage, ChromiumTab] = ...

    @property
    def window_state(self) -> str: ...

    @property
    def browser_location(self) -> Tuple[int, int]: ...

    @property
    def page_location(self) -> Tuple[int, int]: ...

    @property
    def viewport_location(self) -> Tuple[int, int]: ...

    @property
    def browser_size(self) -> Tuple[int, int]: ...

    @property
    def page_size(self) -> Tuple[int, int]: ...

    @property
    def viewport_size(self) -> Tuple[int, int]: ...

    @property
    def viewport_size_with_scrollbar(self) -> Tuple[int, int]: ...

    def _get_page_rect(self) -> dict: ...

    def _get_browser_rect(self) -> dict: ...
