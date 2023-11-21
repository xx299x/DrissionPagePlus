# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from os import popen
from pathlib import Path
from typing import Union
from types import FunctionType

from .._pages.chromium_page import ChromiumPage


def get_usable_path(path: Union[str, Path], is_file: bool = True, parents: bool = True) -> Path: ...


def make_valid_name(full_name: str) -> str: ...


def get_long(txt) -> int: ...


def port_is_using(ip: str, port: Union[str, int]) -> bool: ...


def clean_folder(folder_path: Union[str, Path], ignore: Union[tuple, list] = None) -> None: ...


def show_or_hide_browser(page: ChromiumPage, hide: bool = True) -> None: ...


def get_browser_progress_id(progress: Union[popen, None], address: str) -> Union[str, None]: ...


def get_chrome_hwnds_from_pid(pid: Union[str, int], title: str) -> list: ...


def wait_until(page, condition: Union[FunctionType, str, tuple], timeout: float, poll: float, raise_err: bool): ...


def stop_process_on_port(port: Union[int, str]) -> None: ...


def configs_to_here(file_name: Union[Path, str] = None) -> None: ...
