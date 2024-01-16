# -*- coding:utf-8 -*-
"""
@Author   : g1879
@Contact  : g1879@qq.com
@Copyright: (c) 2024 by g1879, Inc. All Rights Reserved.
@License  : BSD 3-Clause.
"""
from typing import List, Optional, Union

from .driver import BrowserDriver, Driver
from .._pages.chromium_page import ChromiumPage
from .._units.downloader import DownloadManager


class Browser(object):
    BROWSERS: dict = ...
    page: ChromiumPage = ...
    _driver: BrowserDriver = ...
    id: str = ...
    address: str = ...
    _frames: dict = ...
    _drivers: dict = ...
    _process_id: Optional[int] = ...
    _dl_mgr: DownloadManager = ...
    _connected: bool = ...

    def __new__(cls, address: str, browser_id: str, page: ChromiumPage): ...

    def __init__(self, address: str, browser_id: str, page: ChromiumPage): ...

    def _get_driver(self, tab_id: str, owner=None) -> Driver: ...

    def run_cdp(self, cmd, **cmd_args) -> dict: ...

    @property
    def driver(self) -> BrowserDriver: ...

    @property
    def tabs_count(self) -> int: ...

    @property
    def tabs(self) -> List[str]: ...

    @property
    def process_id(self) -> Optional[int]: ...

    def find_tabs(self, title: str = None, url: str = None,
                  tab_type: Union[str, list, tuple] = None, single: bool = True) -> Union[str, List[str]]: ...

    def close_tab(self, tab_id: str) -> None: ...

    def activate_tab(self, tab_id: str) -> None: ...

    def get_window_bounds(self, tab_id: str = None) -> dict: ...

    def connect_to_page(self) -> None: ...

    def _onTargetCreated(self, **kwargs) -> None: ...

    def _onTargetDestroyed(self, **kwargs) -> None: ...

    def quit(self, timeout: float = 5, force: bool = False) -> None: ...

    def _on_disconnect(self) -> None: ...
