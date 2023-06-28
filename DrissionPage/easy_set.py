# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from os import popen
from pathlib import Path
from re import search

from .commons.constants import Settings
from .configs.chromium_options import ChromiumOptions
from .configs.options_manage import OptionsManager


def raise_when_ele_not_found(on_off=True):
    """设置全局变量，找不到元素时是否抛出异常
    :param on_off: True 或 False
    :return: None
    """
    Settings.raise_ele_not_found = on_off


def configs_to_here(save_name=None):
    """把默认ini文件复制到当前目录
    :param save_name: 指定文件名，为None则命名为'dp_configs.ini'
    :return: None
    """
    om = OptionsManager('default')
    save_name = f'{save_name}.ini' if save_name is not None else 'dp_configs.ini'
    om.save(save_name)


def show_settings(ini_path=None):
    """打印ini文件内容
    :param ini_path: ini文件路径
    :return: None
    """
    OptionsManager(ini_path).show()


def set_paths(browser_path=None,
              local_port=None,
              debugger_address=None,
              download_path=None,
              user_data_path=None,
              cache_path=None,
              ini_path=None):
    """快捷的路径设置函数
    :param browser_path: 浏览器可执行文件路径
    :param local_port: 本地端口号
    :param debugger_address: 调试浏览器地址，例：127.0.0.1:9222
    :param download_path: 下载文件路径
    :param user_data_path: 用户数据路径
    :param cache_path: 缓存路径
    :param ini_path: 要修改的ini文件路径
    :return: None
    """
    om = OptionsManager(ini_path)

    def format_path(path: str) -> str:
        return str(path) if path else ''

    if browser_path is not None:
        om.set_item('chrome_options', 'binary_location', format_path(browser_path))

    if local_port is not None:
        om.set_item('chrome_options', 'debugger_address', f'127.0.0.1:{local_port}')

    if debugger_address is not None:
        address = debugger_address.replace('localhost', '127.0.0.1').lstrip('http://').lstrip('https://')
        om.set_item('chrome_options', 'debugger_address', address)

    if download_path is not None:
        om.set_item('paths', 'download_path', format_path(download_path))

    om.save()

    if user_data_path is not None:
        set_argument('--user-data-dir', format_path(user_data_path), ini_path)

    if cache_path is not None:
        set_argument('--disk-cache-dir', format_path(cache_path), ini_path)


def use_auto_port(on_off=True, ini_path=None):
    """设置启动浏览器时使用自动分配的端口和临时文件夹
    :param on_off: 是否开启自动端口
    :param ini_path: 要修改的ini文件路径
    :return: None
    """
    if not isinstance(on_off, bool):
        raise TypeError('on_off参数只能输入bool值。')
    om = OptionsManager(ini_path)
    om.set_item('chrome_options', 'auto_port', on_off)
    om.save()


def use_system_user_path(on_off=True, ini_path=None):
    """设置是否使用系统安装的浏览器默认用户文件夹
    :param on_off: 开或关
    :param ini_path: 要修改的ini文件路径
    :return: 当前对象
    """
    if not isinstance(on_off, bool):
        raise TypeError('on_off参数只能输入bool值。')
    om = OptionsManager(ini_path)
    om.set_item('chrome_options', 'system_user_path', on_off)
    om.save()


def set_argument(arg, value=None, ini_path=None):
    """设置浏览器配置argument属性
    :param arg: 属性名
    :param value: 属性值，有值的属性传入值，没有的传入None
    :param ini_path: 要修改的ini文件路径
    :return: None
    """
    co = ChromiumOptions(ini_path=ini_path)
    co.set_argument(arg, value)
    co.save()


def set_headless(on_off=True, ini_path=None):
    """设置是否隐藏浏览器界面
    :param on_off: 开或关
    :param ini_path: 要修改的ini文件路径
    :return: None
    """
    on_off = 'new' if on_off else False
    set_argument('--headless', on_off, ini_path)


def set_no_imgs(on_off=True, ini_path=None):
    """设置是否禁止加载图片
    :param on_off: 开或关
    :param ini_path: 要修改的ini文件路径
    :return: None
    """
    on_off = None if on_off else False
    set_argument('--blink-settings=imagesEnabled=false', on_off, ini_path)


def set_no_js(on_off=True, ini_path=None):
    """设置是否禁用js
    :param on_off: 开或关
    :param ini_path: 要修改的ini文件路径
    :return: None
    """
    on_off = None if on_off else False
    set_argument('--disable-javascript', on_off, ini_path)


def set_mute(on_off=True, ini_path=None):
    """设置是否静音
    :param on_off: 开或关
    :param ini_path: 要修改的ini文件路径
    :return: None
    """
    on_off = None if on_off else False
    set_argument('--mute-audio', on_off, ini_path)


def set_user_agent(user_agent, ini_path=None):
    """设置user agent
    :param user_agent: user agent文本
    :param ini_path: 要修改的ini文件路径
    :return: None
    """
    set_argument('--user-agent', user_agent, ini_path)


def set_proxy(proxy, ini_path=None):
    """设置代理
    :param proxy: 代理网址和端口
    :param ini_path: 要修改的ini文件路径
    :return: None
    """
    set_argument('--proxy-server', proxy, ini_path)


def get_chrome_path(ini_path=None,
                    show_msg=True,
                    from_ini=True,
                    from_regedit=True,
                    from_system_path=True):
    """从ini文件或系统变量中获取chrome.exe的路径
    :param ini_path: ini文件路径
    :param show_msg: 是否打印信息
    :param from_ini: 是否从ini文件获取
    :param from_regedit: 是否从注册表获取
    :param from_system_path: 是否从系统路径获取
    :return: chrome.exe路径
    """
    # -----------从ini文件中获取--------------
    if ini_path and from_ini:
        try:
            path = OptionsManager(ini_path).chrome_options['binary_location']
        except KeyError:
            path = None
    else:
        path = None

    if path and Path(path).is_file():
        if show_msg:
            print('ini文件中', end='')
        return str(path)

    from platform import system
    if system().lower() != 'windows':
        return None

    # -----------从注册表中获取--------------
    if from_regedit:
        import winreg
        try:
            key = winreg.OpenKey(winreg.HKEY_LOCAL_MACHINE,
                                 r'SOFTWARE\Microsoft\Windows\CurrentVersion\App Paths\chrome.exe',
                                 reserved=0, access=winreg.KEY_READ)
            # key = winreg.OpenKey(winreg.HKEY_CURRENT_USER, r'Software\Google\Chrome\BLBeacon\version',
            #                      reserved=0, access=winreg.KEY_READ)
            k = winreg.EnumValue(key, 0)
            winreg.CloseKey(key)

            if show_msg:
                print('注册表中', end='')

            return k[1]

        except FileNotFoundError:
            pass

    # -----------从系统变量中获取--------------
    if from_system_path:
        try:
            paths = popen('set path').read().lower()
        except:
            return None
        r = search(r'[^;]*chrome[^;]*', paths)

        if r:
            path = Path(r.group(0)) if 'chrome.exe' in r.group(0) else Path(r.group(0)) / 'chrome.exe'

            if path.exists():
                if show_msg:
                    print('系统变量中', end='')
                return str(path)

        paths = paths.split(';')

        for path in paths:
            path = Path(path) / 'chrome.exe'

            try:
                if path.exists():
                    if show_msg:
                        print('系统变量中', end='')
                    return str(path)
            except OSError:
                pass
