# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from typing import Union

from .._configs.chromium_options import ChromiumOptions


def connect_browser(option: ChromiumOptions) -> None: ...


def get_launch_args(opt: ChromiumOptions) -> list: ...


def set_prefs(opt: ChromiumOptions) -> None: ...


def test_connect(ip: str, port: Union[int, str], timeout: float = 30) -> None: ...
