# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from time import sleep

from .chromium_driver import BrowserDriver
from .._units.browser_download_manager import BrowserDownloadManager


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
        self._driver = BrowserDriver(browser_id, 'browser', address)
        self.id = browser_id
        self._frames = {}
        self._connected = False

        self._process_id = None
        r = self.run_cdp('SystemInfo.getProcessInfo')
        for i in r.get('processInfo', []):
            if i['type'] == 'browser':
                self._process_id = i['id']
                break

        self.run_cdp('Target.setDiscoverTargets')
        self._driver.set_listener('Target.targetDestroyed', self._onTargetDestroyed)

    def _onTargetDestroyed(self, **kwargs):
        """标签页关闭时执行"""
        tab_id = kwargs['targetId']
        self._dl_mgr.clear_tab_info(tab_id)
        for k, i in self._frames.items():
            if i == tab_id:
                self._frames.pop(k)

    def connect_to_page(self):
        """执行与page相关的逻辑"""
        if not self._connected:
            self._dl_mgr = BrowserDownloadManager(self)
            self._connected = True

    def run_cdp(self, cmd, **cmd_args):
        """执行Chrome DevTools Protocol语句
        :param cmd: 协议项目
        :param cmd_args: 参数
        :return: 执行的结果
        """
        return self._driver.call_method(cmd, **cmd_args)

    @property
    def driver(self):
        return self._driver

    @property
    def tabs_count(self):
        """返回标签页数量"""
        return len(self.tabs)

    @property
    def tabs(self):
        """返回所有标签页id组成的列表"""
        j = self._driver.get(f'http://{self.address}/json').json()  # 不要改用cdp
        return [i['id'] for i in j if i['type'] == 'page']

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
        self._driver.get(f'http://{self.address}/json/close/{tab_id}')

    def activate_tab(self, tab_id):
        """使标签页变为活动状态
        :param tab_id: 标签页id
        :return: None
        """
        self._driver.get(f'http://{self.address}/json/activate/{tab_id}')

    def get_window_bounds(self):
        """返回浏览器窗口位置和大小信息"""
        return self.run_cdp('Browser.getWindowForTarget', targetId=self.id)['bounds']

    def quit(self):
        """关闭浏览器"""
        self.run_cdp('Browser.close')
        self.driver.stop()

        if self.process_id:
            from os import popen
            from platform import system
            txt = f'tasklist | findstr {self.process_id}' if system().lower() == 'windows' \
                else f'ps -ef | grep  {self.process_id}'
            while True:
                p = popen(txt)
                if f'  {self.process_id} ' not in p.read():
                    break
                sleep(.2)
