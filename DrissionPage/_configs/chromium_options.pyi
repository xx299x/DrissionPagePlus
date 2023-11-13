# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from pathlib import Path
from typing import Union, Tuple, Any


class ChromiumOptions(object):
    def __init__(self, read_file: [bool, None] = True, ini_path: Union[str, Path] = None):
        self.ini_path: str = ...
        self._driver_path: str = ...
        self._user_data_path: str = ...
        self._download_path: str = ...
        self._arguments: list = ...
        self._binary_location: str = ...
        self._user: str = ...
        self._page_load_strategy: str = ...
        self._timeouts: dict = ...
        self._proxy: str = ...
        self._debugger_address: str = ...
        self._extensions: list = ...
        self._prefs: dict = ...
        self._prefs_to_del: list = ...
        self._auto_port: bool = ...
        self._system_user_path: bool = ...
        self._existing_only: bool = ...
        self._headless: bool = ...

    @property
    def download_path(self) -> str: ...

    @property
    def browser_path(self) -> str: ...

    @property
    def user_data_path(self) -> str: ...

    @property
    def user(self) -> str: ...

    @property
    def page_load_strategy(self) -> str: ...

    @property
    def timeouts(self) -> dict: ...

    @property
    def proxy(self) -> str: ...

    @property
    def debugger_address(self) -> str: ...

    @property
    def arguments(self) -> list: ...

    @property
    def extensions(self) -> list: ...

    @property
    def preferences(self) -> dict: ...

    @property
    def system_user_path(self) -> bool: ...

    @property
    def is_existing_only(self) -> bool: ...

    def set_argument(self, arg: str, value: Union[str, None, bool] = None) -> ChromiumOptions: ...

    def remove_argument(self, value: str) -> ChromiumOptions: ...

    def add_extension(self, path: Union[str, Path]) -> ChromiumOptions: ...

    def remove_extensions(self) -> ChromiumOptions: ...

    def set_pref(self, arg: str, value: Any) -> ChromiumOptions: ...

    def remove_pref(self, arg: str) -> ChromiumOptions: ...

    def remove_pref_from_file(self, arg: str) -> ChromiumOptions: ...

    def set_timeouts(self, implicit: float = None, pageLoad: float = None,
                     script: float = None) -> ChromiumOptions: ...

    def set_user(self, user: str = 'Default') -> ChromiumOptions: ...

    def set_headless(self, on_off: bool = True) -> ChromiumOptions: ...

    def set_no_imgs(self, on_off: bool = True) -> ChromiumOptions: ...

    def set_no_js(self, on_off: bool = True) -> ChromiumOptions: ...

    def set_mute(self, on_off: bool = True) -> ChromiumOptions: ...

    def set_user_agent(self, user_agent: str) -> ChromiumOptions: ...

    def set_proxy(self, proxy: str) -> ChromiumOptions: ...

    def set_page_load_strategy(self, value: str) -> ChromiumOptions: ...

    def set_browser_path(self, path: Union[str, Path]) -> ChromiumOptions: ...

    def set_local_port(self, port: Union[str, int]) -> ChromiumOptions: ...

    def set_debugger_address(self, address: str) -> ChromiumOptions: ...

    def set_download_path(self, path: Union[str, Path]) -> ChromiumOptions: ...

    def set_user_data_path(self, path: Union[str, Path]) -> ChromiumOptions: ...

    def set_cache_path(self, path: Union[str, Path]) -> ChromiumOptions: ...

    def set_paths(self, browser_path: Union[str, Path] = None, local_port: Union[int, str] = None,
                  debugger_address: str = None, download_path: Union[str, Path] = None,
                  user_data_path: Union[str, Path] = None, cache_path: Union[str, Path] = None) -> ChromiumOptions: ...

    def use_system_user_path(self, on_off: bool = True) -> ChromiumOptions: ...

    def auto_port(self, on_off: bool = True) -> ChromiumOptions: ...

    def existing_only(self, on_off: bool = True) -> ChromiumOptions: ...

    def save(self, path: Union[str, Path] = None) -> str: ...

    def save_to_default(self) -> str: ...


class PortFinder(object):
    used_port: dict = ...

    @staticmethod
    def get_port() -> Tuple[int, str]: ...
