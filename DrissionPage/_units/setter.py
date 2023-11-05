# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from pathlib import Path

from requests.structures import CaseInsensitiveDict

from .._commons.tools import show_or_hide_browser
from .._commons.web import set_browser_cookies, set_session_cookies


class ChromiumBaseSetter(object):
    def __init__(self, page):
        self._page = page

    @property
    def load_strategy(self):
        """返回用于设置页面加载策略的对象"""
        return PageLoadStrategy(self._page)

    @property
    def scroll(self):
        """返回用于设置页面滚动设置的对象"""
        return PageScrollSetter(self._page.scroll)

    def retry_times(self, times):
        """设置连接失败重连次数"""
        self._page.retry_times = times

    def retry_interval(self, interval):
        """设置连接失败重连间隔"""
        self._page.retry_interval = interval

    def timeouts(self, implicit=None, page_load=None, script=None):
        """设置超时时间，单位为秒
        :param implicit: 查找元素超时时间
        :param page_load: 页面加载超时时间
        :param script: 脚本运行超时时间
        :return: None
        """
        if implicit is not None:
            self._page.timeouts.implicit = implicit
            self._page._timeout = implicit

        if page_load is not None:
            self._page.timeouts.page_load = page_load

        if script is not None:
            self._page.timeouts.script = script

    def user_agent(self, ua, platform=None):
        """为当前tab设置user agent，只在当前tab有效
        :param ua: user agent字符串
        :param platform: platform字符串
        :return: None
        """
        keys = {'userAgent': ua}
        if platform:
            keys['platform'] = platform
        self._page.run_cdp('Emulation.setUserAgentOverride', **keys)

    def session_storage(self, item, value):
        """设置或删除某项sessionStorage信息
        :param item: 要设置的项
        :param value: 项的值，设置为False时，删除该项
        :return: None
        """
        js = f'sessionStorage.removeItem("{item}");' if item is False else f'sessionStorage.setItem("{item}","{value}");'
        return self._page.run_js_loaded(js, as_expr=True)

    def local_storage(self, item, value):
        """设置或删除某项localStorage信息
        :param item: 要设置的项
        :param value: 项的值，设置为False时，删除该项
        :return: None
        """
        js = f'localStorage.removeItem("{item}");' if item is False else f'localStorage.setItem("{item}","{value}");'
        return self._page.run_js_loaded(js, as_expr=True)

    def cookie(self, cookie):
        """设置单个cookie
        :param cookie: cookie信息
        :return: None
        """
        if isinstance(cookie, str):
            self.cookies(cookie)
        else:
            self.cookies([cookie])

    def cookies(self, cookies):
        """设置多个cookie，注意不要传入单个
        :param cookies: cookies信息
        :return: None
        """
        set_browser_cookies(self._page, cookies)

    def upload_files(self, files):
        """等待上传的文件路径
        :param files: 文件路径列表或字符串，字符串时多个文件用回车分隔
        :return: None
        """
        if not self._page._upload_list:
            self._page.driver.set_listener('Page.fileChooserOpened', self._page._onFileChooserOpened)
            self._page.run_cdp('Page.setInterceptFileChooserDialog', enabled=True)

        if isinstance(files, str):
            files = files.split('\n')
        self._page._upload_list = [str(Path(i).absolute()) for i in files]

    def headers(self, headers: dict) -> None:
        """设置固定发送的headers
        :param headers: dict格式的headers数据
        :return: None
        """
        self._page.run_cdp('Network.enable')
        self._page.run_cdp('Network.setExtraHTTPHeaders', headers=headers)


class TabSetter(ChromiumBaseSetter):
    def __init__(self, page):
        super().__init__(page)

    @property
    def window(self):
        """返回用于设置浏览器窗口的对象"""
        return WindowSetter(self._page)

    def download_path(self, path):
        """设置下载路径
        :param path: 下载路径
        :return: None
        """
        self._page._download_path = str(Path(path).absolute())
        self._page.browser._dl_mgr.set_path(self._page.tab_id, path)
        if self._page._DownloadKit:
            self._page._DownloadKit.set.goal_path(path)

    def download_file_name(self, name=None, suffix=None):
        """设置下一个被下载文件的名称
        :param name: 文件名，可不含后缀，会自动使用远程文件后缀
        :param suffix: 后缀名，显式设置后缀名，不使用远程文件后缀
        :return: None
        """
        self._page.browser._dl_mgr.set_rename(self._page.tab_id, name, suffix)

    def when_download_file_exists(self, mode):
        """设置当存在同名文件时的处理方式
        :param mode: 可在 'rename', 'overwrite', 'skip', 'r', 'o', 's'中选择
        :return: None
        """
        types = {'rename': 'rename', 'overwrite': 'overwrite', 'skip': 'skip', 'r': 'rename', 'o': 'overwrite',
                 's': 'skip'}
        mode = types.get(mode, mode)
        if mode not in types:
            raise ValueError(f'''mode参数只能是 '{"', '".join(types.keys())}' 之一，现在是：{mode}''')

        self._page.browser._dl_mgr.set_file_exists(self._page.tab_id, mode)

    def activate(self):
        """使标签页处于最前面"""
        self._page.browser.activate_tab(self._page.tab_id)


class ChromiumPageSetter(TabSetter):
    def main_tab(self, tab_id=None):
        """设置主tab
        :param tab_id: 标签页id，不传入则设置当前tab
        :return: None
        """
        self._page._main_tab = tab_id or self._page.tab_id

    def tab_to_front(self, tab_or_id=None):
        """激活标签页使其处于最前面
        :param tab_or_id: 标签页对象或id，为None表示当前标签页
        :return: None
        """
        if not tab_or_id:
            tab_or_id = self._page.tab_id
        elif not isinstance(tab_or_id, str):  # 传入Tab对象
            tab_or_id = tab_or_id.tab_id
        self._page.browser.activate_tab(tab_or_id)

    @property
    def window(self):
        """返回用于设置浏览器窗口的对象"""
        return PageWindowSetter(self._page)


class SessionPageSetter(object):
    def __init__(self, page):
        """
        :param page: SessionPage对象
        """
        self._page = page

    def retry_times(self, times):
        """设置连接失败时重连次数"""
        self._page.retry_times = times

    def retry_interval(self, interval):
        """设置连接失败时重连间隔"""
        self._page.retry_interval = interval

    def download_path(self, path):
        """设置下载路径
        :param path: 下载路径
        :return: None
        """
        self._page._download_path = str(Path(path).absolute())
        if self._page._DownloadKit:
            self._page._DownloadKit.set.goal_path(path)

    def timeout(self, second):
        """设置连接超时时间
        :param second: 秒数
        :return: None
        """
        self._page.timeout = second

    def cookie(self, cookie):
        """为Session对象设置单个cookie
        :param cookie: cookie信息
        :return: None
        """
        if isinstance(cookie, str):
            self.cookies(cookie)
        else:
            self.cookies([cookie])

    def cookies(self, cookies):
        """为Session对象设置多个cookie，注意不要传入单个
        :param cookies: cookies信息
        :return: None
        """
        set_session_cookies(self._page.session, cookies)

    def headers(self, headers):
        """设置通用的headers
        :param headers: dict形式的headers
        :return: None
        """
        self._page.session.headers = CaseInsensitiveDict(headers)

    def header(self, attr, value):
        """设置headers中一个项
        :param attr: 设置名称
        :param value: 设置值
        :return: None
        """
        self._page.session.headers[attr.lower()] = value

    def user_agent(self, ua):
        """设置user agent
        :param ua: user agent
        :return: None
        """
        self._page.session.headers['user-agent'] = ua

    def proxies(self, http=None, https=None):
        """设置proxies参数
        :param http: http代理地址
        :param https: https代理地址
        :return: None
        """
        self._page.session.proxies = {'http': http, 'https': https}

    def auth(self, auth):
        """设置认证元组或对象
        :param auth: 认证元组或对象
        :return: None
        """
        self._page.session.auth = auth

    def hooks(self, hooks):
        """设置回调方法
        :param hooks: 回调方法
        :return: None
        """
        self._page.session.hooks = hooks

    def params(self, params):
        """设置查询参数字典
        :param params: 查询参数字典
        :return: None
        """
        self._page.session.params = params

    def verify(self, on_off):
        """设置是否验证SSL证书
        :param on_off: 是否验证 SSL 证书
        :return: None
        """
        self._page.session.verify = on_off

    def cert(self, cert):
        """SSL客户端证书文件的路径(.pem格式)，或(‘cert’, ‘key’)元组
        :param cert: 证书路径或元组
        :return: None
        """
        self._page.session.cert = cert

    def stream(self, on_off):
        """设置是否使用流式响应内容
        :param on_off: 是否使用流式响应内容
        :return: None
        """
        self._page.session.stream = on_off

    def trust_env(self, on_off):
        """设置是否信任环境
        :param on_off: 是否信任环境
        :return: None
        """
        self._page.session.trust_env = on_off

    def max_redirects(self, times):
        """设置最大重定向次数
        :param times: 最大重定向次数
        :return: None
        """
        self._page.session.max_redirects = times

    def add_adapter(self, url, adapter):
        """添加适配器
        :param url: 适配器对应url
        :param adapter: 适配器对象
        :return: None
        """
        self._page.session.mount(url, adapter)


class WebPageSetter(ChromiumPageSetter):
    def __init__(self, page):
        super().__init__(page)
        self._session_setter = SessionPageSetter(self._page)
        self._chromium_setter = ChromiumPageSetter(self._page)

    def cookies(self, cookies):
        """添加cookies信息到浏览器或session对象
        :param cookies: 可以接收`CookieJar`、`list`、`tuple`、`str`、`dict`格式的`cookies`
        :return: None
        """
        if self._page.mode == 'd' and self._page._has_driver:
            self._chromium_setter.cookies(cookies)
        elif self._page.mode == 's' and self._page._has_session:
            self._session_setter.cookies(cookies)

    def headers(self, headers) -> None:
        """设置固定发送的headers
        :param headers: dict格式的headers数据
        :return: None
        """
        if self._page.mode == 's':
            self._session_setter.headers(headers)
        else:
            self._chromium_setter.headers(headers)

    def user_agent(self, ua, platform=None):
        """设置user agent，d模式下只有当前tab有效"""
        if self._page.mode == 's':
            self._session_setter.user_agent(ua)
        else:
            self._chromium_setter.user_agent(ua, platform)


class WebPageTabSetter(TabSetter):
    def __init__(self, page):
        super().__init__(page)
        self._session_setter = SessionPageSetter(self._page)
        self._chromium_setter = ChromiumBaseSetter(self._page)

    def cookies(self, cookies):
        """添加多个cookies信息到浏览器或session对象，注意不要传入单个
        :param cookies: 可以接收`CookieJar`、`list`、`tuple`、`str`、`dict`格式的`cookies`
        :return: None
        """
        if self._page.mode == 'd' and self._page._has_driver:
            self._chromium_setter.cookies(cookies)
        elif self._page.mode == 's' and self._page._has_session:
            self._session_setter.cookies(cookies)

    def headers(self, headers) -> None:
        """设置固定发送的headers
        :param headers: dict格式的headers数据
        :return: None
        """
        if self._page._has_session:
            self._session_setter.headers(headers)
        if self._page._has_driver:
            self._chromium_setter.headers(headers)

    def user_agent(self, ua, platform=None):
        """设置user agent，d模式下只有当前tab有效"""
        if self._page._has_session:
            self._session_setter.user_agent(ua)
        if self._page._has_driver:
            self._chromium_setter.user_agent(ua, platform)


class ChromiumElementSetter(object):
    def __init__(self, ele):
        """
        :param ele: ChromiumElement
        """
        self._ele = ele

    def attr(self, attr, value):
        """设置元素attribute属性
        :param attr: 属性名
        :param value: 属性值
        :return: None
        """
        self._ele.page.run_cdp('DOM.setAttributeValue', nodeId=self._ele.ids.node_id, name=attr, value=str(value))

    def prop(self, prop, value):
        """设置元素property属性
        :param prop: 属性名
        :param value: 属性值
        :return: None
        """
        value = value.replace('"', r'\"')
        self._ele.run_js(f'this.{prop}="{value}";')

    def innerHTML(self, html):
        """设置元素innerHTML
        :param html: html文本
        :return: None
        """
        self.prop('innerHTML', html)


class ChromiumFrameSetter(ChromiumBaseSetter):
    def attr(self, attr, value):
        """设置frame元素attribute属性
        :param attr: 属性名
        :param value: 属性值
        :return: None
        """
        self._page._check_ok()
        self._page.frame_ele.set.attr(attr, value)


class PageLoadStrategy(object):
    """用于设置页面加载策略的类"""

    def __init__(self, page):
        """
        :param page: ChromiumBase对象
        """
        self._page = page

    def __call__(self, value):
        """设置加载策略
        :param value: 可选 'normal', 'eager', 'none'
        :return: None
        """
        if value.lower() not in ('normal', 'eager', 'none'):
            raise ValueError("只能选择 'normal', 'eager', 'none'。")
        self._page._page_load_strategy = value

    def normal(self):
        """设置页面加载策略为normal"""
        self._page._page_load_strategy = 'normal'

    def eager(self):
        """设置页面加载策略为eager"""
        self._page._page_load_strategy = 'eager'

    def none(self):
        """设置页面加载策略为none"""
        self._page._page_load_strategy = 'none'


class PageScrollSetter(object):
    def __init__(self, scroll):
        self._scroll = scroll

    def wait_complete(self, on_off=True):
        """设置滚动命令后是否等待完成
        :param on_off: 开或关
        :return: None
        """
        if not isinstance(on_off, bool):
            raise TypeError('on_off必须为bool。')
        self._scroll._wait_complete = on_off

    def smooth(self, on_off=True):
        """设置页面滚动是否平滑滚动
        :param on_off: 开或关
        :return: None
        """
        if not isinstance(on_off, bool):
            raise TypeError('on_off必须为bool。')
        b = 'smooth' if on_off else 'auto'
        self._scroll._driver.run_js(f'document.documentElement.style.setProperty("scroll-behavior","{b}");')
        self._scroll._wait_complete = on_off


class WindowSetter(object):
    """用于设置窗口大小的类"""

    def __init__(self, page):
        """
        :param page: 页面对象
        """
        self._page = page
        self._window_id = self._get_info()['windowId']

    def maximized(self):
        """窗口最大化"""
        s = self._get_info()['bounds']['windowState']
        if s in ('fullscreen', 'minimized'):
            self._perform({'windowState': 'normal'})
        self._perform({'windowState': 'maximized'})

    def minimized(self):
        """窗口最小化"""
        s = self._get_info()['bounds']['windowState']
        if s == 'fullscreen':
            self._perform({'windowState': 'normal'})
        self._perform({'windowState': 'minimized'})

    def fullscreen(self):
        """设置窗口为全屏"""
        s = self._get_info()['bounds']['windowState']
        if s == 'minimized':
            self._perform({'windowState': 'normal'})
        self._perform({'windowState': 'fullscreen'})

    def normal(self):
        """设置窗口为常规模式"""
        s = self._get_info()['bounds']['windowState']
        if s == 'fullscreen':
            self._perform({'windowState': 'normal'})
        self._perform({'windowState': 'normal'})

    def size(self, width=None, height=None):
        """设置窗口大小
        :param width: 窗口宽度
        :param height: 窗口高度
        :return: None
        """
        if width or height:
            s = self._get_info()['bounds']['windowState']
            if s != 'normal':
                self._perform({'windowState': 'normal'})
            info = self._get_info()['bounds']
            width = width - 16 if width else info['width']
            height = height + 7 if height else info['height']
            self._perform({'width': width, 'height': height})

    def location(self, x=None, y=None):
        """设置窗口在屏幕中的位置，相对左上角坐标
        :param x: 距离顶部距离
        :param y: 距离左边距离
        :return: None
        """
        if x is not None or y is not None:
            self.normal()
            info = self._get_info()['bounds']
            x = x if x is not None else info['left']
            y = y if y is not None else info['top']
            self._perform({'left': x - 8, 'top': y})

    def _get_info(self):
        """获取窗口位置及大小信息"""
        return self._page.run_cdp('Browser.getWindowForTarget')

    def _perform(self, bounds):
        """执行改变窗口大小操作
        :param bounds: 控制数据
        :return: None
        """
        self._page.run_cdp('Browser.setWindowBounds', windowId=self._window_id, bounds=bounds)


class PageWindowSetter(WindowSetter):
    def hide(self):
        """隐藏浏览器窗口，只在Windows系统可用"""
        show_or_hide_browser(self._page, hide=True)

    def show(self):
        """显示浏览器窗口，只在Windows系统可用"""
        show_or_hide_browser(self._page, hide=False)
