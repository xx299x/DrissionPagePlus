# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from time import sleep, perf_counter

from .chromium_driver import BrowserDriver, ChromiumDriver
from .._commons.tools import stop_process_on_port, raise_error
from .._units.download_manager import DownloadManager

__ERROR__ = 'error'


class Browser(object):
    BROWSERS = {}

    def __new__(cls, address, browser_id, page):
        """
        :param address: 浏览器地址
        :param browser_id: 浏览器id
        :param page: ChromiumPage对象
        """
        if browser_id in cls.BROWSERS:
            return cls.BROWSERS[browser_id]
        return object.__new__(cls)

    def __init__(self, address, browser_id, page):
        """
        :param address: 浏览器地址
        :param browser_id: 浏览器id
        :param page: ChromiumPage对象
        """
        if hasattr(self, '_created'):
            return
        self._created = True
        Browser.BROWSERS[browser_id] = self

        self.page = page
        self.address = address
        self._driver = BrowserDriver(browser_id, 'browser', address, self)
        self.id = browser_id
        self._frames = {}
        self._drivers = {}
        # self._drivers = {t: ChromiumDriver(t, 'page', address) for t in self.tabs}
        self._connected = False

        self._process_id = None
        r = self.run_cdp('SystemInfo.getProcessInfo')
        for i in r.get('processInfo', []):
            if i['type'] == 'browser':
                self._process_id = i['id']
                break

        self.run_cdp('Target.setDiscoverTargets', discover=True)
        self._driver.set_callback('Target.targetDestroyed', self._onTargetDestroyed)
        self._driver.set_callback('Target.targetCreated', self._onTargetCreated)

    def _get_driver(self, tab_id):
        """获取对应tab id的ChromiumDriver
        :param tab_id: 标签页id
        :return: ChromiumDriver对象
        """
        return self._drivers.pop(tab_id, ChromiumDriver(tab_id, 'page', self.address))

    def _onTargetCreated(self, **kwargs):
        """标签页创建时执行"""
        if kwargs['targetInfo']['type'] == 'page' and not kwargs['targetInfo']['url'].startswith('devtools://'):
            self._drivers[kwargs['targetInfo']['targetId']] = ChromiumDriver(kwargs['targetInfo']['targetId'], 'page',
                                                                             self.address)

    def _onTargetDestroyed(self, **kwargs):
        """标签页关闭时执行"""
        tab_id = kwargs['targetId']
        if hasattr(self, '_dl_mgr'):
            self._dl_mgr.clear_tab_info(tab_id)
        for key in [k for k, i in self._frames.items() if i == tab_id]:
            self._frames.pop(key, None)
        self._drivers.pop(tab_id, None)

    def connect_to_page(self):
        """执行与page相关的逻辑"""
        if not self._connected:
            self._dl_mgr = DownloadManager(self)
            self._connected = True

    def run_cdp(self, cmd, **cmd_args):
        """执行Chrome DevTools Protocol语句
        :param cmd: 协议项目
        :param cmd_args: 参数
        :return: 执行的结果
        """
        r = self._driver.run(cmd, **cmd_args)
        return r if __ERROR__ not in r else raise_error(r)

    @property
    def driver(self):
        return self._driver

    @property
    def tabs_count(self):
        """返回标签页数量"""
        j = self.run_cdp('Target.getTargets')['targetInfos']  # 不要改用get，避免卡死
        return len([i for i in j if i['type'] == 'page' and not i['url'].startswith('devtools://')])

    @property
    def tabs(self):
        """返回所有标签页id组成的列表"""
        j = self._driver.get(f'http://{self.address}/json').json()  # 不要改用cdp，因为顺序不对
        return [i['id'] for i in j if i['type'] == 'page' and not i['url'].startswith('devtools://')]

    @property
    def process_id(self):
        """返回浏览器进程id"""
        return self._process_id

    def find_tabs(self, title=None, url=None, tab_type=None, single=True):
        """查找符合条件的tab，返回它们的id组成的列表
        :param title: 要匹配title的文本
        :param url: 要匹配url的文本
        :param tab_type: tab类型，可用列表输入多个
        :param single: 是否返回首个结果的id，为False返回所有信息
        :return: tab id或tab dict
        """
        tabs = self._driver.get(f'http://{self.address}/json').json()  # 不要改用cdp

        if isinstance(tab_type, str):
            tab_type = {tab_type}
        elif isinstance(tab_type, (list, tuple, set)):
            tab_type = set(tab_type)
        elif tab_type is not None:
            raise TypeError('tab_type只能是set、list、tuple、str、None。')

        r = [i for i in tabs if ((title is None or title in i['title']) and (url is None or url in i['url'])
                                 and (tab_type is None or i['type'] in tab_type))]
        return r[0]['id'] if r and single else r

    def close_tab(self, tab_id):
        """关闭标签页
        :param tab_id: 标签页id
        :return: None
        """
        self.run_cdp('Target.closeTarget', targetId=tab_id)

    def activate_tab(self, tab_id):
        """使标签页变为活动状态
        :param tab_id: 标签页id
        :return: None
        """
        self.run_cdp('Target.activateTarget', targetId=tab_id)

    def get_window_bounds(self, tab_id=None):
        """返回浏览器窗口位置和大小信息
        :param tab_id: 标签页id
        :return: 窗口大小字典
        """
        return self.run_cdp('Browser.getWindowForTarget', targetId=tab_id or self.id)['bounds']

    def quit(self, timeout=5, force=False):
        """关闭浏览器
        :param timeout: 等待浏览器关闭超时时间
        :param force: 是否立刻强制终止进程
        :return: None
        """
        self.run_cdp('Browser.close')
        self.driver.stop()

        if force:
            ip, port = self.address.split(':')
            if ip not in ('127.0.0.1', 'localhost'):
                return
            stop_process_on_port(port)
            return

        if self.process_id:
            from os import popen
            from platform import system
            txt = f'tasklist | findstr {self.process_id}' if system().lower() == 'windows' \
                else f'ps -ef | grep  {self.process_id}'
            end_time = perf_counter() + timeout
            while perf_counter() < end_time:
                p = popen(txt)
                sleep(.1)
                try:
                    if f'  {self.process_id} ' not in p.read():
                        return
                except TypeError:
                    pass

    def _on_quit(self):
        Browser.BROWSERS.pop(self.id, None)
