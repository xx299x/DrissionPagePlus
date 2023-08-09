# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from DrissionPage.configs.chromium_options import ChromiumOptions


def connect_browser(option: ChromiumOptions) -> tuple: ...


def get_launch_args(opt: ChromiumOptions) -> list: ...


def set_prefs(opt: ChromiumOptions) -> None: ...
