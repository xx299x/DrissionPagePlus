# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from configparser import RawConfigParser, NoSectionError, NoOptionError
from http.cookiejar import Cookie
from pathlib import Path

from requests.cookies import RequestsCookieJar
from selenium.webdriver.chrome.options import Options


class OptionsManager(object):
    """管理配置文件内容的类"""

    def __init__(self, path=None):
        """初始化，读取配置文件，如没有设置临时文件夹，则设置并新建  \n
        :param path: ini文件的路径，默认读取模块文件夹下的
        """
        self.ini_path = str(Path(__file__).parent / 'configs.ini') if path == 'default' or path is None else path
        if not Path(self.ini_path).exists():
            raise FileNotFoundError('ini文件不存在。')
        self._conf = RawConfigParser()
        self._conf.read(self.ini_path, encoding='utf-8')

    def __getattr__(self, item):
        """以dict形似返回获取大项信息
        :param item: 项名
        :return: None
        """
        return self.get_option(item)

    def get_value(self, section, item):
        """获取配置的值         \n
        :param section: 段名
        :param item: 项名
        :return: 项值
        """
        try:
            return eval(self._conf.get(section, item))
        except (SyntaxError, NameError):
            return self._conf.get(section, item)
        except NoSectionError and NoOptionError:
            return None

    def get_option(self, section):
        """把section内容以字典方式返回   \n
        :param section: 段名
        :return: 段内容生成的字典
        """
        items = self._conf.items(section)
        option = dict()

        for j in items:
            try:
                option[j[0]] = eval(self._conf.get(section, j[0]))
            except Exception:
                option[j[0]] = self._conf.get(section, j[0])

        return option

    def set_item(self, section, item, value):
        """设置配置值            \n
        :param section: 段名
        :param item: 项名
        :param value: 项值
        :return: None
        """
        self._conf.set(section, item, str(value))
        self.__setattr__(f'_{section}', None)
        return self

    def remove_item(self, section, item):
        """删除配置值            \n
        :param section: 段名
        :param item: 项名
        :return: None
        """
        self._conf.remove_option(section, item)
        return self

    def save(self, path=None):
        """保存配置文件                                               \n
        :param path: ini文件的路径，传入 'default' 保存到默认ini文件
        :return: 保存路径
        """
        default_path = (Path(__file__).parent / 'configs.ini').absolute()
        if path == 'default':
            path = default_path
        elif path is None:
            path = Path(self.ini_path).absolute()
        else:
            path = Path(path).absolute()

        path = path / 'config.ini' if path.is_dir() else path

        path = str(path)
        self._conf.write(open(path, 'w', encoding='utf-8'))

        print(f'配置已保存到文件：{path}')
        if path == str(default_path):
            print('以后程序可自动从文件加载配置。')

        return path

    def save_to_default(self):
        """保存当前配置到默认ini文件"""
        return self.save('default')


class SessionOptions(object):
    """requests的Session对象配置类"""

    def __init__(self, read_file=True, ini_path=None):
        """
        :param read_file: 是否从文件读取配置
        :param ini_path: ini文件路径
        """
        self.ini_path = None
        self._download_path = None
        self._headers = None
        self._cookies = None
        self._auth = None
        self._proxies = None
        self._hooks = None
        self._params = None
        self._verify = None
        self._cert = None
        self._adapters = None
        self._stream = None
        self._trust_env = None
        self._max_redirects = None
        self._timeout = 10

        self._del_set = set()  # 记录要从ini文件删除的参数

        if read_file:
            self.ini_path = ini_path or str(Path(__file__).parent / 'configs.ini')
            om = OptionsManager(self.ini_path)
            options_dict = om.session_options

            if options_dict.get('headers', None) is not None:
                self.set_headers(options_dict['headers'])

            if options_dict.get('cookies', None) is not None:
                self.set_cookies(options_dict['cookies'])

            if options_dict.get('auth', None) is not None:
                self._auth = options_dict['auth']

            if options_dict.get('params', None) is not None:
                self._params = options_dict['params']

            if options_dict.get('verify', None) is not None:
                self._verify = options_dict['verify']

            if options_dict.get('cert', None) is not None:
                self._cert = options_dict['cert']

            if options_dict.get('stream', None) is not None:
                self._stream = options_dict['stream']

            if options_dict.get('trust_env', None) is not None:
                self._trust_env = options_dict['trust_env']

            if options_dict.get('max_redirects', None) is not None:
                self._max_redirects = options_dict['max_redirects']

            self.set_proxies(om.proxies.get('http', None), om.proxies.get('https', None))
            self._timeout = om.timeouts.get('implicit', 10)
            self._download_path = om.paths.get('download_path', None)

    # ===========须独立处理的项开始============
    @property
    def download_path(self):
        """返回默认下载路径属性信息"""
        return self._download_path

    def set_paths(self, download_path=None):
        """设置默认下载路径                          \n
        :param download_path: 下载路径
        :return: 返回当前对象
        """
        if download_path is not None:
            self._download_path = str(download_path)
        return self

    @property
    def timeout(self):
        """返回timeout属性信息"""
        return self._timeout

    def set_timeout(self, second):
        """设置超时信息
        :param second: 秒数
        :return: 返回当前对象
        """
        self._timeout = second
        return self

    @property
    def proxies(self):
        """返回proxies设置信息"""
        if self._proxies is None:
            self._proxies = {}
        return self._proxies

    def set_proxies(self, http, https=None):
        """设置proxies参数                                                                 \n
        :param http: http代理地址
        :param https: https代理地址
        :return: 返回当前对象
        """
        proxies = None if http == https is None else {'http': http, 'https': https or http}
        self._sets('proxies', proxies)
        return self

    # ===========须独立处理的项结束============

    @property
    def headers(self):
        """返回headers设置信息"""
        if self._headers is None:
            self._headers = {}
        return self._headers

    def set_headers(self, headers):
        """设置headers参数                                                               \n
        :param headers: 参数值，传入None可在ini文件标记删除
        :return: 返回当前对象
        """
        if headers is None:
            self._headers = None
            self._del_set.add('headers')
        else:
            self._headers = {key.lower(): headers[key] for key in headers}
        return self

    def set_a_header(self, attr, value):
        """设置headers中一个项          \n
        :param attr: 设置名称
        :param value: 设置值
        :return: 返回当前对象
        """
        if self._headers is None:
            self._headers = {}

        self._headers[attr.lower()] = value
        return self

    def remove_a_header(self, attr):
        """从headers中删除一个设置     \n
        :param attr: 要删除的设置
        :return: 返回当前对象
        """
        if self._headers is None:
            return self

        attr = attr.lower()
        if attr in self._headers:
            self._headers.pop(attr)

        return self

    @property
    def cookies(self):
        """以list形式返回cookies"""
        if self._cookies is None:
            self._cookies = []
        return self._cookies

    def set_cookies(self, cookies):
        """设置cookies信息                                                                        \n
        :param cookies: cookies，可为CookieJar, list, tuple, str, dict，传入None可在ini文件标记删除
        :return: 返回当前对象
        """
        cookies = cookies if cookies is None else list(cookies_to_tuple(cookies))
        self._sets('cookies', cookies)
        return self

    @property
    def auth(self):
        """返回auth设置信息"""
        return self._auth

    def set_auth(self, auth):
        """设置认证元组或对象                                                                \n
        :param auth: 认证元组或对象
        :return: 返回当前对象
        """
        self._sets('auth', auth)
        return self

    @property
    def hooks(self):
        """返回回调方法"""
        if self._hooks is None:
            self._hooks = {}
        return self._hooks

    def set_hooks(self, hooks):
        """设置回调方法                       \n
        :param hooks:
        :return: 返回当前对象
        """
        self._hooks = hooks
        return self

    @property
    def params(self):
        """返回params设置信息"""
        if self._params is None:
            self._params = {}
        return self._params

    def set_params(self, params):
        """设置查询参数字典                                                                  \n
        :param params: 查询参数字典
        :return: 返回当前对象
        """
        self._sets('params', params)
        return self

    @property
    def verify(self):
        """返回是否验证SSL证书设置"""
        return self._verify

    def set_verify(self, on_off):
        """设置是否验证SSL证书                                                               \n
        :param on_off: 是否验证 SSL 证书
        :return: 返回当前对象
        """
        self._sets('verify', on_off)
        return self

    @property
    def cert(self):
        """返回cert设置信息"""
        return self._cert

    def set_cert(self, cert):
        """SSL客户端证书文件的路径(.pem格式)，或(‘cert’, ‘key’)元组                            \n
        :param cert: 证书路径或元组
        :return: 返回当前对象
        """
        self._sets('cert', cert)
        return self

    @property
    def adapters(self):
        """返回适配器设置信息"""
        if self._adapters is None:
            self._adapters = []
        return self._adapters

    def add_adapter(self, url, adapter):
        """添加适配器                        \n
        :param url: 适配器对应url
        :param adapter: 适配器对象
        :return: 返回当前对象
        """
        self._adapters.append((url, adapter))
        return self

    @property
    def stream(self):
        """返回stream设置信息"""
        return self._stream

    def set_stream(self, on_off):
        """设置是否使用流式响应内容            \n
        :param on_off: 是否使用流式响应内容
        :return: 返回当前对象
        """
        self._sets('stream', on_off)
        return self

    @property
    def trust_env(self):
        """返回trust_env设置信息"""
        return self._trust_env

    def set_trust_env(self, on_off):
        """设置是否信任环境            \n
        :param on_off: 是否信任环境
        :return: 返回当前对象
        """
        self._sets('trust_env', on_off)
        return self

    @property
    def max_redirects(self):
        """返回最大重定向次数"""
        return self._max_redirects

    def set_max_redirects(self, times):
        """设置最大重定向次数            \n
        :param times: 最大重定向次数
        :return: 返回当前对象
        """
        self._sets('max_redirects', times)
        return self

    def _sets(self, arg, val):
        """给属性赋值或标记删除
        :param arg: 属性名称
        :param val: 参数值
        :return: None
        """
        if val is None:
            self.__setattr__(f'_{arg}', None)
            self._del_set.add(arg)
        else:
            self.__setattr__(f'_{arg}', val)
            if arg in self._del_set:
                self._del_set.remove(arg)

    def save(self, path=None):
        """保存设置到文件                                              \n
        :param path: ini文件的路径，传入 'default' 保存到默认ini文件
        :return: 保存文件的绝对路径
        """
        if path == 'default':
            path = (Path(__file__).parent / 'configs.ini').absolute()

        elif path is None:
            if self.ini_path:
                path = Path(self.ini_path).absolute()
            else:
                path = (Path(__file__).parent / 'configs.ini').absolute()

        else:
            path = Path(path).absolute()

        path = path / 'config.ini' if path.is_dir() else path

        if path.exists():
            om = OptionsManager(str(path))
        else:
            om = OptionsManager(self.ini_path or str(Path(__file__).parent / 'configs.ini'))

        options = session_options_to_dict(self)

        for i in options:
            if i not in ('download_path', 'timeout', 'proxies'):
                om.set_item('session_options', i, options[i])

        om.set_item('paths', 'download_path', self.download_path)
        om.set_item('timeouts', 'implicit', self.timeout)
        om.set_item('proxies', 'http', self.proxies.get('http', None))
        om.set_item('proxies', 'https', self.proxies.get('https', None))

        for i in self._del_set:
            if i == 'download_path':
                om.set_item('paths', 'download_path', '')
            elif i == 'proxies':
                om.set_item('proxies', 'http', '')
                om.set_item('proxies', 'https', '')
            else:
                om.remove_item('session_options', i)

        path = str(path)
        om.save(path)

        return path

    def save_to_default(self):
        """保存当前配置到默认ini文件"""
        return self.save('default')

    def as_dict(self):
        """以字典形式返回本对象"""
        return session_options_to_dict(self)


class DriverOptions(Options):
    """chrome浏览器配置类，继承自selenium.webdriver.chrome.options的Options类，
    增加了删除配置和保存到文件方法。
    """

    def __init__(self, read_file=True, ini_path=None):
        """初始化，默认从文件读取设置                      \n
        :param read_file: 是否从默认ini文件中读取配置信息
        :param ini_path: ini文件路径，为None则读取默认ini文件
        """
        super().__init__()
        self._user_data_path = None

        if read_file:
            self.ini_path = ini_path or str(Path(__file__).parent / 'configs.ini')
            om = OptionsManager(self.ini_path)
            options_dict = om.chrome_options

            self._driver_path = om.paths.get('chromedriver_path', None)
            self._download_path = om.paths.get('download_path', None)
            self._binary_location = options_dict.get('binary_location', '')
            self._arguments = options_dict.get('arguments', [])
            self._extensions = options_dict.get('extensions', [])
            self._experimental_options = options_dict.get('experimental_options', {})
            self._debugger_address = options_dict.get('debugger_address', None)
            self.page_load_strategy = options_dict.get('page_load_strategy', 'normal')

            for arg in self._arguments:
                if arg.startswith('--user-data-dir='):
                    self.set_paths(user_data_path=arg[16:])
                    break

            self.timeouts = options_dict.get('timeouts', {'implicit': 10, 'pageLoad': 30, 'script': 30})
            return

        self._driver_path = None
        self._download_path = None
        self.ini_path = None
        self.timeouts = {'implicit': 10, 'pageLoad': 30, 'script': 30}
        self._debugger_address = '127.0.0.1:9222'

    @property
    def driver_path(self):
        """chromedriver文件路径"""
        return self._driver_path

    @property
    def download_path(self):
        """默认下载路径文件路径"""
        return self._download_path

    @property
    def chrome_path(self):
        """浏览器启动文件路径"""
        return self.browser_path

    @property
    def browser_path(self):
        """浏览器启动文件路径"""
        return self.binary_location or 'chrome'

    @property
    def user_data_path(self):
        """返回用户文件夹路径"""
        return self._user_data_path

    # -------------重写父类方法，实现链式操作-------------
    def add_argument(self, argument):
        """添加一个配置项               \n
        :param argument: 配置项内容
        :return: 当前对象
        """
        super().add_argument(argument)
        return self

    def set_capability(self, name, value):
        """设置一个capability          \n
        :param name: capability名称
        :param value: capability值
        :return: 当前对象
        """
        super().set_capability(name, value)
        return self

    def add_extension(self, extension):
        """添加插件                           \n
        :param extension: crx文件路径
        :return: 当前对象
        """
        super().add_extension(extension)
        return self

    def add_encoded_extension(self, extension):
        """将带有扩展数据的 Base64 编码字符串添加到将用于将其提取到 ChromeDriver 的列表中  \n
        :param extension: 带有扩展数据的 Base64 编码字符串
        :return: 当前对象
        """
        super().add_encoded_extension(extension)
        return self

    def add_experimental_option(self, name, value):
        """添加一个实验选项到浏览器  \n
        :param name: 选项名称
        :param value: 选项值
        :return: 当前对象
        """
        super().add_experimental_option(name, value)
        return self

    # -------------重写父类方法结束-------------

    def save(self, path=None):
        """保存设置到文件                                                                        \n
        :param path: ini文件的路径， None 保存到当前读取的配置文件，传入 'default' 保存到默认ini文件
        :return: 保存文件的绝对路径
        """
        if path == 'default':
            path = (Path(__file__).parent / 'configs.ini').absolute()

        elif path is None:
            if self.ini_path:
                path = Path(self.ini_path).absolute()
            else:
                path = (Path(__file__).parent / 'configs.ini').absolute()

        else:
            path = Path(path).absolute()

        path = path / 'config.ini' if path.is_dir() else path

        if path.exists():
            om = OptionsManager(str(path))
        else:
            om = OptionsManager(self.ini_path or str(Path(__file__).parent / 'configs.ini'))

        options = self.as_dict()

        for i in options:
            if i == 'driver_path':
                om.set_item('paths', 'chromedriver_path', options[i])
            elif i == 'download_path':
                om.set_item('paths', 'download_path', options[i])
            else:
                om.set_item('chrome_options', i, options[i])

        path = str(path)
        om.save(path)

        return path

    def save_to_default(self):
        """保存当前配置到默认ini文件"""
        return self.save('default')

    def remove_argument(self, value):
        """移除一个argument项                                    \n
        :param value: 设置项名，有值的设置项传入设置名称即可
        :return: 当前对象
        """
        del_list = []

        for argument in self._arguments:
            if argument.startswith(value):
                del_list.append(argument)

        for del_arg in del_list:
            self._arguments.remove(del_arg)

        return self

    def remove_experimental_option(self, key):
        """移除一个实验设置，传入key值删除  \n
        :param key: 实验设置的名称
        :return: 当前对象
        """
        if key in self._experimental_options:
            self._experimental_options.pop(key)

        return self

    def remove_all_extensions(self):
        """移除所有插件             \n
        :return: 当前对象
        """
        # 因插件是以整个文件储存，难以移除其中一个，故如须设置则全部移除再重设
        self._extensions = []
        return self

    def set_argument(self, arg, value):
        """设置浏览器配置的argument属性                          \n
        :param arg: 属性名
        :param value: 属性值，有值的属性传入值，没有的传入bool
        :return: 当前对象
        """
        self.remove_argument(arg)

        if value:
            arg_str = arg if isinstance(value, bool) else f'{arg}={value}'
            self.add_argument(arg_str)

        return self

    def set_timeouts(self, implicit=None, pageLoad=None, script=None):
        """设置超时时间，设置单位为秒，selenium4以上版本有效       \n
        :param implicit: 查找元素超时时间
        :param pageLoad: 页面加载超时时间
        :param script: 脚本运行超时时间
        :return: 当前对象
        """
        if implicit is not None:
            self.timeouts['implicit'] = implicit
        if pageLoad is not None:
            self.timeouts['pageLoad'] = pageLoad
        if script is not None:
            self.timeouts['script'] = script

        return self

    def set_headless(self, on_off=True):
        """设置是否隐藏浏览器界面   \n
        :param on_off: 开或关
        :return: 当前对象
        """
        on_off = True if on_off else False
        return self.set_argument('--headless', on_off)

    def set_no_imgs(self, on_off=True):
        """设置是否加载图片           \n
        :param on_off: 开或关
        :return: 当前对象
        """
        on_off = True if on_off else False
        return self.set_argument('--blink-settings=imagesEnabled=false', on_off)

    def set_no_js(self, on_off=True):
        """设置是否禁用js       \n
        :param on_off: 开或关
        :return: 当前对象
        """
        on_off = True if on_off else False
        return self.set_argument('--disable-javascript', on_off)

    def set_mute(self, on_off=True):
        """设置是否静音            \n
        :param on_off: 开或关
        :return: 当前对象
        """
        on_off = True if on_off else False
        return self.set_argument('--mute-audio', on_off)

    def set_user_agent(self, user_agent):
        """设置user agent                  \n
        :param user_agent: user agent文本
        :return: 当前对象
        """
        return self.set_argument('--user-agent', user_agent)

    def set_proxy(self, proxy):
        """设置代理                    \n
        :param proxy: 代理url和端口
        :return: 当前对象
        """
        return self.set_argument('--proxy-server', proxy)

    def set_page_load_strategy(self, value):
        """设置page_load_strategy，可接收 'normal', 'eager', 'none'                    \n
        selenium4以上版本才支持此功能
        normal：默认情况下使用, 等待所有资源下载完成
        eager：DOM访问已准备就绪, 但其他资源 (如图像) 可能仍在加载中
        none：完全不阻塞WebDriver
        :param value: 可接收 'normal', 'eager', 'none'
        :return: 当前对象
        """
        if value not in ('normal', 'eager', 'none'):
            raise ValueError("只能选择'normal', 'eager', 'none'。")
        self.page_load_strategy = value.lower()
        return self

    def set_paths(self, driver_path=None, chrome_path=None, browser_path=None, local_port=None,
                  debugger_address=None, download_path=None, user_data_path=None, cache_path=None):
        """快捷的路径设置函数                                             \n
        :param driver_path: chromedriver.exe路径
        :param chrome_path: chrome.exe路径
        :param browser_path: 浏览器可执行文件路径
        :param local_port: 本地端口号
        :param debugger_address: 调试浏览器地址，例：127.0.0.1:9222
        :param download_path: 下载文件路径
        :param user_data_path: 用户数据路径
        :param cache_path: 缓存路径
        :return: 当前对象
        """
        if driver_path is not None:
            self._driver_path = str(driver_path)

        if chrome_path is not None:
            self.binary_location = str(chrome_path)

        if browser_path is not None:
            self.binary_location = str(browser_path)

        if local_port is not None:
            self.debugger_address = '' if local_port == '' else f'127.0.0.1:{local_port}'

        if debugger_address is not None:
            self.debugger_address = debugger_address

        if download_path is not None:
            self._download_path = str(download_path)

        if user_data_path is not None:
            self.set_argument('--user-data-dir', str(user_data_path))
            self._user_data_path = user_data_path

        if cache_path is not None:
            self.set_argument('--disk-cache-dir', str(cache_path))

        return self

    def as_dict(self):
        """已dict方式返回所有配置信息"""
        return chrome_options_to_dict(self)


def chrome_options_to_dict(options):
    """把chrome配置对象转换为字典                             \n
    :param options: chrome配置对象，字典或DriverOptions对象
    :return: 配置字典
    """
    if options in (False, None):
        return DriverOptions(read_file=False).as_dict()

    if isinstance(options, dict):
        return options

    re_dict = dict()
    attrs = ['debugger_address', 'binary_location', 'arguments', 'extensions', 'experimental_options', 'driver_path',
             'page_load_strategy', 'download_path']

    options_dir = options.__dir__()
    for attr in attrs:
        try:
            re_dict[attr] = options.__getattribute__(attr) if attr in options_dir else None
        except Exception:
            pass

    if 'timeouts' in options_dir and 'timeouts' in options._caps:
        timeouts = options.__getattribute__('timeouts')
        re_dict['timeouts'] = timeouts

    return re_dict


def session_options_to_dict(options):
    """把session配置对象转换为字典                 \n
    :param options: session配置对象或字典
    :return: 配置字典
    """
    if options in (False, None):
        return SessionOptions(read_file=False).as_dict()

    if isinstance(options, dict):
        return options

    re_dict = dict()
    attrs = ['headers', 'cookies', 'proxies', 'params', 'verify', 'stream', 'trust_env',
             'max_redirects', 'timeout', 'download_path']

    for attr in attrs:
        val = options.__getattribute__(f'_{attr}')
        if val is not None:
            re_dict[attr] = val

    return re_dict


def cookie_to_dict(cookie):
    """把Cookie对象转为dict格式                \n
    :param cookie: Cookie对象
    :return: cookie字典
    """
    if isinstance(cookie, Cookie):
        cookie_dict = cookie.__dict__.copy()
        cookie_dict.pop('rfc2109')
        cookie_dict.pop('_rest')
        return cookie_dict

    elif isinstance(cookie, dict):
        cookie_dict = cookie

    elif isinstance(cookie, str):
        cookie = cookie.split(',' if ',' in cookie else ';')
        cookie_dict = {}

        for key, attr in enumerate(cookie):
            attr_val = attr.lstrip().split('=')

            if key == 0:
                cookie_dict['name'] = attr_val[0]
                cookie_dict['value'] = attr_val[1] if len(attr_val) == 2 else ''
            else:
                cookie_dict[attr_val[0]] = attr_val[1] if len(attr_val) == 2 else ''

        return cookie_dict

    else:
        raise TypeError('cookie参数必须为Cookie、str或dict类型。')

    return cookie_dict


def cookies_to_tuple(cookies):
    """把cookies转为tuple格式                                                \n
    :param cookies: cookies信息，可为CookieJar, list, tuple, str, dict
    :return: 返回tuple形式的cookies
    """
    if isinstance(cookies, (list, tuple, RequestsCookieJar)):
        cookies = tuple(cookie_to_dict(cookie) for cookie in cookies)

    elif isinstance(cookies, str):
        cookies = tuple(cookie_to_dict(cookie.lstrip()) for cookie in cookies.split(";"))

    elif isinstance(cookies, dict):
        cookies = tuple({'name': cookie, 'value': cookies[cookie]} for cookie in cookies)

    else:
        raise TypeError('cookies参数必须为RequestsCookieJar、list、tuple、str或dict类型。')

    return cookies
