# -*- coding:utf-8 -*-
from .chromium_element import ChromiumBase


class ChromiumTab(ChromiumBase):
    """实现浏览器标签页的类"""

    def __init__(self, page,
                 tab_id: str = None):
        """初始化                                                      \n
        :param page: 浏览器地址:端口、Tab对象或DriverOptions对象
        :param tab_id: 要控制的标签页id，不指定默认为激活的
        """
        self.page = page
        super().__init__(page.address, tab_id, page.timeout)

    def _set_options(self) -> None:
        self.set_timeouts(page_load=self.page.timeouts.page_load,
                          script=self.page.timeouts.script,
                          implicit=self.page.timeouts.implicit if self.timeout is None else self.timeout)
        self._page_load_strategy = self.page.page_load_strategy


class Timeout(object):
    """用于保存d模式timeout信息的类"""

    def __init__(self, page):
        self.page = page
        self.page_load = 30
        self.script = 30

    @property
    def implicit(self):
        return self.page.timeout
