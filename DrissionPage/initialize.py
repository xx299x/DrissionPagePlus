#!/usr/bin/env python
# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   initialize.py
检测用户chrome版本，自动下载匹配的chromedriver
"""

import os
import re
# from DrissionPage import MixPage
#
# page = MixPage()
# import subprocess
from pathlib import Path


def get_chrome_path() -> str:
    paths = os.popen('set path').read().lower()
    r = re.search(r'[^;]*chrome[^;]*', paths)

    if r:
        path = Path(r.group(0)) if 'chrome.exe' in r.group(0) else Path(r.group(0)) / 'chrome.exe'
        if path.exists():
            return str(path)

    paths = paths.split(';')
    for path in paths:
        path = Path(path) / 'chrome.exe'
        if path.exists():
            return str(path)


def get_chrome_version(path: str) -> str:
    path = path.replace('\\', '\\\\')
    version = os.popen(f'wmic datafile where "name=\'{path}\'" get version').read().lower().split('\n')[2]

    return version


p = get_chrome_path()
print(get_chrome_version(p))
