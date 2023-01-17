# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from http.cookiejar import Cookie
from pathlib import Path
from typing import Union

from requests.cookies import RequestsCookieJar

from .base import BasePage, DrissionElement
from .chromium_element import ChromiumElement
from .config import DriverOptions
from .configs.chromium_options import ChromiumOptions


def get_ele_txt(e: DrissionElement) -> str: ...


def get_loc(loc: Union[tuple, str], translate_css: bool = False) -> tuple: ...


def str_to_loc(loc: str) -> tuple: ...


def translate_loc(loc: tuple) -> tuple: ...


def format_html(text: str) -> str: ...


def cookie_to_dict(cookie: Union[Cookie, str, dict]) -> dict: ...


def cookies_to_tuple(cookies: Union[RequestsCookieJar, list, tuple, str, dict]) -> tuple: ...


def clean_folder(folder_path: str, ignore: list = None) -> None: ...


def unzip(zip_path: str, to_path: str) -> Union[list, None]: ...


def get_exe_from_port(port: Union[str, int]) -> Union[str, None]: ...


def get_pid_from_port(port: Union[str, int]) -> Union[str, None]: ...


def get_usable_path(path: Union[str, Path]) -> Path: ...


def make_valid_name(full_name: str) -> str: ...


def get_long(txt) -> int: ...


def make_absolute_link(link, page: BasePage = None) -> str: ...


def is_js_func(func: str) -> bool: ...


def port_is_using(ip: str, port: Union[str, int]) -> bool: ...


def connect_browser(option: Union[ChromiumOptions, DriverOptions]) -> tuple: ...


def get_launch_args(opt: Union[ChromiumOptions, DriverOptions]) -> list: ...


def set_prefs(opt: Union[ChromiumOptions, DriverOptions]) -> None: ...


def location_in_viewport(page, loc_x: int, loc_y: int) -> bool: ...


def offset_scroll(ele: ChromiumElement, offset_x: int, offset_y: int) -> tuple: ...
