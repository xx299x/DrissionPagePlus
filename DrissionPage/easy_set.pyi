# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from typing import Union


def show_settings(ini_path: str = None) -> None: ...


def set_paths(driver_path: str = None,
              chrome_path: str = None,
              browser_path: str = None,
              local_port: Union[int, str] = None,
              debugger_address: str = None,
              tmp_path: str = None,
              download_path: str = None,
              user_data_path: str = None,
              cache_path: str = None,
              ini_path: str = None,
              check_version: bool = False) -> None: ...


def set_argument(arg: str, value: Union[bool, str], ini_path: str = None) -> None: ...


def set_headless(on_off: bool = True, ini_path: str = None) -> None: ...


def set_no_imgs(on_off: bool = True, ini_path: str = None) -> None: ...


def set_no_js(on_off: bool = True, ini_path: str = None) -> None: ...


def set_mute(on_off: bool = True, ini_path: str = None) -> None: ...


def set_user_agent(user_agent: str, ini_path: str = None) -> None: ...


def set_proxy(proxy: str, ini_path: str = None) -> None: ...


def check_driver_version(driver_path: str = None, chrome_path: str = None) -> bool: ...


# -------------------------自动识别chrome版本号并下载对应driver------------------------
def get_match_driver(ini_path: Union[str, None] = 'default',
                     save_path: str = None,
                     chrome_path: str = None,
                     show_msg: bool = True,
                     check_version: bool = True) -> Union[str, None]: ...


def get_chrome_path(ini_path: str = None,
                    show_msg: bool = True,
                    from_ini: bool = True,
                    from_regedit: bool = True,
                    from_system_path: bool = True, ) -> Union[str, None]: ...
