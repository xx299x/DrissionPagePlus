# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from pathlib import Path
from typing import Union


def configs_to_here(file_name: Union[Path, str] = None) -> None: ...


def show_settings(ini_path: Union[str, Path] = None) -> None: ...


def set_paths(browser_path: Union[str, Path] = None,
              local_port: Union[int, str] = None,
              debugger_address: str = None,
              download_path: Union[str, Path] = None,
              user_data_path: Union[str, Path] = None,
              cache_path: Union[str, Path] = None,
              ini_path: Union[str, Path] = None) -> None: ...


def use_auto_port(on_off: bool = True, ini_path: Union[str, Path] = None) -> None: ...


def use_system_user_path(on_off: bool = True, ini_path: Union[str, Path] = None) -> None: ...


def set_argument(arg: str, value: Union[bool, str] = None, ini_path: Union[str, Path] = None) -> None: ...


def set_headless(on_off: bool = True, ini_path: Union[str, Path] = None) -> None: ...


def set_no_imgs(on_off: bool = True, ini_path: Union[str, Path] = None) -> None: ...


def set_no_js(on_off: bool = True, ini_path: Union[str, Path] = None) -> None: ...


def set_mute(on_off: bool = True, ini_path: Union[str, Path] = None) -> None: ...


def set_user_agent(user_agent: str, ini_path: Union[str, Path] = None) -> None: ...


def set_proxy(proxy: str, ini_path: Union[str, Path] = None) -> None: ...


def get_chrome_path(ini_path: str = None,
                    show_msg: bool = True,
                    from_ini: bool = True,
                    from_regedit: bool = True,
                    from_system_path: bool = True, ) -> Union[str, None]: ...
