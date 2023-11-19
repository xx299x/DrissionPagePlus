# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from json import load, dump, JSONDecodeError
from pathlib import Path
from subprocess import Popen, DEVNULL
from tempfile import gettempdir
from time import perf_counter, sleep
from platform import system

from requests import get as requests_get

from ..errors import BrowserConnectError
from .tools import port_is_using


def connect_browser(option):
    """连接或启动浏览器
    :param option: ChromiumOptions对象
    :return: 返回是否接管的浏览器
    """
    debugger_address = option.debugger_address.replace('localhost', '127.0.0.1').lstrip('http://').lstrip('https://')
    chrome_path = option.browser_path

    ip, port = debugger_address.split(':')
    if ip != '127.0.0.1' or port_is_using(ip, port) or option.is_existing_only:
        test_connect(ip, port)
        option._headless = False
        for i in option.arguments:
            if i.startswith('--headless') and not i.endswith('=false'):
                option._headless = True
                break
        return True

    # ----------创建浏览器进程----------
    args = get_launch_args(option)
    set_prefs(option)
    set_flags(option)
    try:
        _run_browser(port, chrome_path, args)

    # 传入的路径找不到，主动在ini文件、注册表、系统变量中找
    except FileNotFoundError:
        from ..easy_set import get_chrome_path
        chrome_path = get_chrome_path(show_msg=False)

        if not chrome_path:
            raise FileNotFoundError('无法找到chrome路径，请手动配置。')

        _run_browser(port, chrome_path, args)

    test_connect(ip, port)
    return False


def get_launch_args(opt):
    """从ChromiumOptions获取命令行启动参数
    :param opt: ChromiumOptions
    :return: 启动参数列表
    """
    # ----------处理arguments-----------
    result = set()
    has_user_path = False
    remote_allow = False
    headless = None
    for i in opt.arguments:
        if i.startswith(('--load-extension=', '--remote-debugging-port=')):
            continue
        elif i.startswith('--user-data-dir') and not opt.system_user_path:
            result.add(f'--user-data-dir={Path(i[16:]).absolute()}')
            has_user_path = True
            continue
        elif i.startswith('--remote-allow-origins='):
            remote_allow = True
        elif i.startswith('--headless'):
            if i == '--headless=false':
                headless = False
                continue
            elif i == '--headless':
                i = '--headless=new'
                headless = True
            else:
                headless = True

        result.add(i)

    if not has_user_path and not opt.system_user_path:
        port = opt.debugger_address.split(':')[-1] if opt.debugger_address else '0'
        path = Path(gettempdir()) / 'DrissionPage' / f'userData_{port}'
        path.mkdir(parents=True, exist_ok=True)
        opt.set_user_data_path(path)
        result.add(f'--user-data-dir={path}')

    if not remote_allow:
        result.add('--remote-allow-origins=*')

    if headless is None and system().lower() == 'linux':
        from os import popen
        r = popen('systemctl list-units | grep graphical.target')
        if 'graphical.target' not in r.read():
            headless = True
            result.add('--headless=new')

    result = list(result)
    opt._headless = headless

    # ----------处理插件extensions-------------
    ext = [str(Path(e).absolute()) for e in opt.extensions]
    if ext:
        ext = ','.join(set(ext))
        ext = f'--load-extension={ext}'
        result.append(ext)

    return result


def set_prefs(opt):
    """处理启动配置中的prefs项，目前只能对已存在文件夹配置
    :param opt: ChromiumOptions
    :return: None
    """
    if not opt.user_data_path or (not opt.preferences and not opt._prefs_to_del):
        return
    prefs = opt.preferences
    del_list = opt._prefs_to_del

    user = 'Default'
    for arg in opt.arguments:
        if arg.startswith('--profile-directory'):
            user = arg.split('=')[-1].strip()
            break

    prefs_file = Path(opt.user_data_path) / user / 'Preferences'

    if not prefs_file.exists():
        prefs_file.parent.mkdir(parents=True, exist_ok=True)
        with open(prefs_file, 'w') as f:
            f.write('{}')

    with open(prefs_file, "r", encoding='utf-8') as f:
        try:
            prefs_dict = load(f)
        except JSONDecodeError:
            prefs_dict = {}

        for pref in prefs:
            value = prefs[pref]
            pref = pref.split('.')
            _make_leave_in_dict(prefs_dict, pref, 0, len(pref))
            _set_value_to_dict(prefs_dict, pref, value)

        for pref in del_list:
            _remove_arg_from_dict(prefs_dict, pref)

    with open(prefs_file, 'w', encoding='utf-8') as f:
        dump(prefs_dict, f)


def set_flags(opt):
    """处理启动配置中的prefs项，目前只能对已存在文件夹配置
    :param opt: ChromiumOptions
    :return: None
    """
    if not opt.user_data_path or (not opt.clear_file_flags and not opt.flags):
        return

    state_file = Path(opt.user_data_path) / 'Local State'

    if not state_file.exists():
        state_file.parent.mkdir(parents=True, exist_ok=True)
        with open(state_file, 'w') as f:
            f.write('{}')

    with open(state_file, "r", encoding='utf-8') as f:
        try:
            states_dict = load(f)
        except JSONDecodeError:
            states_dict = {}
        flags_list = [] if opt.clear_file_flags else states_dict.setdefault(
            'browser', {}).setdefault('enabled_labs_experiments', [])
        flags_dict = {}
        for i in flags_list:
            f = str(i).split('@', 1)
            flags_dict[f[0]] = None if len(f) == 1 else f[1]

        for k, i in opt.flags.items():
            flags_dict[k] = i

        states_dict['browser']['enabled_labs_experiments'] = [f'{k}@{i}' if i else k for k, i in flags_dict.items()]

    with open(state_file, 'w', encoding='utf-8') as f:
        dump(states_dict, f)


def test_connect(ip, port, timeout=30):
    """测试浏览器是否可用
    :param ip: 浏览器ip
    :param port: 浏览器端口
    :param timeout: 超时时间
    :return: None
    """
    end_time = perf_counter() + timeout
    while perf_counter() < end_time:
        try:
            tabs = requests_get(f'http://{ip}:{port}/json', timeout=10, headers={'Connection': 'close'},
                                proxies={'http': None, 'https': None}).json()
            for tab in tabs:
                if tab['type'] == 'page':
                    return
        except Exception:
            sleep(.2)

    raise BrowserConnectError(f'\n{ip}:{port}浏览器无法链接。\n请确认：\n1、该端口为浏览器\n'
                              f'2、已添加--remote-allow-origins=*和--remote-debugging-port={port}启动项\n'
                              f'3、用户文件夹没有和已打开的浏览器冲突\n'
                              f'4、如为无界面系统，请添加--headless=new参数\n'
                              f'5、如果是Linux系统，可能还要添加--no-sandbox启动参数\n'
                              f'可使用ChromiumOptions设置端口和用户文件夹路径。')


def _run_browser(port, path: str, args) -> Popen:
    """创建chrome进程
    :param port: 端口号
    :param path: 浏览器路径
    :param args: 启动参数
    :return: 进程对象
    """
    p = Path(path)
    p = str(p / 'chrome') if p.is_dir() else str(path)
    arguments = [p, f'--remote-debugging-port={port}']
    arguments.extend(args)
    try:
        return Popen(arguments, shell=False, stdout=DEVNULL, stderr=DEVNULL)
    except FileNotFoundError:
        raise FileNotFoundError('未找到浏览器，请手动指定浏览器可执行文件路径。')


def _make_leave_in_dict(target_dict: dict, src: list, num: int, end: int) -> None:
    """把prefs中a.b.c形式的属性转为a['b']['c']形式
    :param target_dict: 要处理的字典
    :param src: 属性层级列表[a, b, c]
    :param num: 当前处理第几个
    :param end: src长度
    :return: None
    """
    if num == end:
        return
    if src[num] not in target_dict:
        target_dict[src[num]] = {}
    num += 1
    _make_leave_in_dict(target_dict[src[num - 1]], src, num, end)


def _set_value_to_dict(target_dict: dict, src: list, value) -> None:
    """把a.b.c形式的属性的值赋值到a['b']['c']形式的字典中
    :param target_dict: 要处理的字典
    :param src: 属性层级列表[a, b, c]
    :param value: 属性值
    :return: None
    """
    src = "']['".join(src)
    src = f"target_dict['{src}']=value"
    exec(src)


def _remove_arg_from_dict(target_dict: dict, arg: str) -> None:
    """把a.b.c形式的属性从字典中删除
    :param target_dict: 要处理的字典
    :param arg: 层级属性，形式'a.b.c'
    :return: None
    """
    args = arg.split('.')
    args = [f"['{i}']" for i in args]
    src = ''.join(args)
    src = f"target_dict{src}"
    try:
        exec(src)
        src = ''.join(args[:-1])
        src = f"target_dict{src}.pop({args[-1][1:-1]})"
        exec(src)
    except:
        pass
