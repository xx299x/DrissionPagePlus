# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from pathlib import Path
from typing import Union
from requests import get as requests_get

from .base import BasePage, DrissionElement
from .config import DriverOptions


def get_ele_txt(e: DrissionElement) -> str: ...


def get_loc(loc: Union[tuple, str], translate_css: bool = False) -> tuple: ...


def str_to_loc(loc: str) -> tuple: ...


def translate_loc(loc: tuple) -> tuple: ...


def format_html(text: str) -> str: ...


def clean_folder(folder_path: str, ignore: list = None) -> None: ...


def unzip(zip_path: str, to_path: str) -> Union[list, None]: ...


def get_exe_path_from_port(port: Union[str, int]) -> Union[str, None]: ...


def get_pid_from_port(port: Union[str, int]) -> Union[str, None]: ...


def get_usable_path(path: Union[str, Path]) -> Path: ...


def make_valid_name(full_name: str) -> str: ...


def get_long(txt) -> int: ...


def make_absolute_link(link, page: BasePage = None) -> str: ...


def is_js_func(func: str) -> bool: ...


def connect_chrome(option: DriverOptions) -> tuple: ...


def location_in_viewport(page, loc_x: int, loc_y: int) -> bool: ...
