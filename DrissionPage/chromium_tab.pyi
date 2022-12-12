# -*- coding:utf-8 -*-

from .chromium_base import ChromiumBase
from .chromium_page import ChromiumPage


class ChromiumTab(ChromiumBase):
    """实现浏览器标签页的类"""

    def __init__(self, page:ChromiumPage, tab_id: str = ...):
        self.page: ChromiumPage = ...

    def _set_options(self) -> None: ...
