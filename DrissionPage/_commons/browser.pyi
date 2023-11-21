# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from typing import Union

from .._configs.chromium_options import ChromiumOptions


def connect_browser(option: ChromiumOptions) -> bool: ...


def get_launch_args(opt: ChromiumOptions) -> list: ...


def set_prefs(opt: ChromiumOptions) -> None: ...


def set_flags(opt: ChromiumOptions) -> None: ...


def test_connect(ip: str, port: Union[int, str], timeout: float = 30) -> None: ...


def get_chrome_path(ini_path: str = None,
                    show_msg: bool = True,
                    from_ini: bool = True,
                    from_regedit: bool = True,
                    from_system_path: bool = True, ) -> Union[str, None]: ...
