# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from pathlib import Path
from time import sleep

from requests import get

from .._base.browser import Browser
from .._commons.browser import connect_browser
from .._configs.chromium_options import ChromiumOptions
from .._pages.chromium_base import ChromiumBase, Timeout
from .._pages.chromium_tab import ChromiumTab
from .._units.setter import ChromiumPageSetter
from .._units.tab_rect import TabRect
from .._units.waiter import PageWaiter
from ..errors import BrowserConnectError


class ChromiumPage(ChromiumBase):
    """用于管理浏览器的类"""

    def __init__(self, addr_or_opts=None, tab_id=None, timeout=None, addr_driver_opts=None):
        """
        :param addr_or_opts: 浏览器地址:端口、ChromiumOptions对象或端口数字（int）
        :param tab_id: 要控制的标签页id，不指定默认为激活的
        :param timeout: 超时时间
        """
        if not addr_or_opts and addr_driver_opts:
            addr_or_opts = addr_driver_opts
        self._page = self
        address = self._handle_options(addr_or_opts)
        self._run_browser()
        super().__init__(address, tab_id)
        self.set.timeouts(implicit=timeout)
        self._page_init()

    def _handle_options(self, addr_or_opts):
        """设置浏览器启动属性
        :param addr_or_opts: 'ip:port'、ChromiumOptions、ChromiumDriver
        :return: 返回浏览器地址
        """
        if not addr_or_opts:
            self._driver_options = ChromiumOptions(addr_or_opts)

        elif isinstance(addr_or_opts, ChromiumOptions):
            self._driver_options = addr_or_opts

        elif isinstance(addr_or_opts, str):
            self._driver_options = ChromiumOptions()
            self._driver_options.set_debugger_address(addr_or_opts)

        elif isinstance(addr_or_opts, int):
            self._driver_options = ChromiumOptions()
            self._driver_options.set_local_port(addr_or_opts)

        else:
            raise TypeError('只能接收ip:port格式或ChromiumOptions类型参数。')

        return self._driver_options.debugger_address

    def _run_browser(self):
        """连接浏览器"""
        connect_browser(self._driver_options)
        ws = get(f'http://{self._driver_options.debugger_address}/json/version',
                 headers={'Connection': 'close'})
        if not ws:
            raise BrowserConnectError('\n浏览器连接失败，请检查是否启用全局代理。如有，须开放127.0.0.1地址。')
        ws = ws.json()['webSocketDebuggerUrl'].split('/')[-1]
        self._browser = Browser(self._driver_options.debugger_address, ws, self)

    def _d_set_runtime_settings(self):
        """设置运行时用到的属性"""
        self._timeouts = Timeout(self,
                                 page_load=self._driver_options.timeouts['pageLoad'],
                                 script=self._driver_options.timeouts['script'],
                                 implicit=self._driver_options.timeouts['implicit'])
        if self._driver_options.timeouts['implicit'] is not None:
            self._timeout = self._driver_options.timeouts['implicit']
        self._page_load_strategy = self._driver_options.page_load_strategy
        self._download_path = str(Path(self._driver_options.download_path).absolute())

    def _page_init(self):
        """浏览器相关设置"""
        self._rect = None
        self._main_tab = self.tab_id
        self._browser.connect_to_page()

    @property
    def browser(self):
        """返回用于控制浏览器cdp的driver"""
        return self._browser

    @property
    def tabs_count(self):
        """返回标签页数量"""
        return self.browser.tabs_count

    @property
    def tabs(self):
        """返回所有标签页id组成的列表"""
        return self.browser.tabs

    @property
    def main_tab(self):
        return self._main_tab

    @property
    def latest_tab(self):
        """返回最新的标签页id，最新标签页指最后创建或最后被激活的"""
        return self.tabs[0]

    @property
    def process_id(self):
        """返回浏览器进程id"""
        return self.browser.process_id

    @property
    def set(self):
        """返回用于等待的对象"""
        if self._set is None:
            self._set = ChromiumPageSetter(self)
        return self._set

    @property
    def rect(self):
        if self._rect is None:
            self._rect = TabRect(self)
        return self._rect

    @property
    def wait(self):
        """返回用于等待的对象"""
        if self._wait is None:
            self._wait = PageWaiter(self)
        return self._wait

    def get_tab(self, tab_id=None):
        """获取一个标签页对象
        :param tab_id: 要获取的标签页id，为None时获取当前tab
        :return: 标签页对象
        """
        return tab_id if isinstance(tab_id, ChromiumTab) else ChromiumTab(self, tab_id or self.tab_id)

    def find_tabs(self, title=None, url=None, tab_type=None, single=True):
        """查找符合条件的tab，返回它们的id组成的列表
        :param title: 要匹配title的文本
        :param url: 要匹配url的文本
        :param tab_type: tab类型，可用列表输入多个
        :param single: 是否返回首个结果的id，为False返回所有信息
        :return: tab id或tab dict
        """
        return self._browser.find_tabs(title, url, tab_type, single)

    def _new_tab(self, url=None, switch_to=False):
        """新建一个标签页,该标签页在最后面
        :param url: 新标签页跳转到的网址
        :param switch_to: 新建标签页后是否把焦点移过去
        :return: 新标签页的id
        """
        if switch_to:
            tid = self.run_cdp('Target.createTarget', url='')['targetId']
            self._to_tab(tid, read_doc=False)
            if url:
                self.get(url)

        elif url:
            tid = self.run_cdp('Target.createTarget', url=url)['targetId']

        else:
            tid = self.run_cdp('Target.createTarget', url='')['targetId']

        return tid

    def new_tab(self, url=None, switch_to=False):
        """新建一个标签页,该标签页在最后面
        :param url: 新标签页跳转到的网址
        :param switch_to: 新建标签页后是否把焦点移过去
        :return: switch_to为False时返回新标签页对象，否则返回当前对象，
        """
        tid = self._new_tab(url, switch_to)
        return self if switch_to else ChromiumTab(self, tid)

    def to_main_tab(self):
        """跳转到主标签页"""
        self.to_tab(self._main_tab)

    def to_tab(self, tab_or_id=None, activate=True):
        """跳转到标签页
        :param tab_or_id: 标签页对象或id，默认跳转到main_tab
        :param activate: 切换后是否变为活动状态
        :return: None
        """
        self._to_tab(tab_or_id, activate)

    def _to_tab(self, tab_or_id=None, activate=True, read_doc=True):
        """跳转到标签页
        :param tab_or_id: 标签页对象或id，默认跳转到main_tab
        :param activate: 切换后是否变为活动状态
        :param read_doc: 切换后是否读取文档
        :return: None
        """
        tabs = self.tabs
        if not tab_or_id:
            tab_id = self._main_tab
        elif isinstance(tab_or_id, ChromiumTab):
            tab_id = tab_or_id.tab_id
        else:
            tab_id = tab_or_id

        if tab_id not in tabs:
            tab_id = self.latest_tab

        if activate:
            self.browser.activate_tab(tab_id)

        if tab_id == self.tab_id:
            return

        self.driver.stop()
        self._driver_init(tab_id)
        if read_doc and self.ready_state in ('complete', None):
            self._get_document()

    def close_tabs(self, tabs_or_ids=None, others=False):
        """关闭传入的标签页，默认关闭当前页。可传入多个
        :param tabs_or_ids: 要关闭的标签页对象或id，可传入列表或元组，为None时关闭当前页
        :param others: 是否关闭指定标签页之外的
        :return: None
        """
        all_tabs = set(self.tabs)
        if isinstance(tabs_or_ids, str):
            tabs = {tabs_or_ids}
        elif isinstance(tabs_or_ids, ChromiumTab):
            tabs = {tabs_or_ids.tab_id}
        elif tabs_or_ids is None:
            tabs = {self.tab_id}
        elif isinstance(tabs_or_ids, (list, tuple)):
            tabs = set(i.tab_id if isinstance(i, ChromiumTab) else i for i in tabs_or_ids)
        else:
            raise TypeError('tabs_or_ids参数只能传入标签页对象或id。')

        if others:
            tabs = all_tabs - tabs

        end_len = len(all_tabs) - len(tabs)
        if end_len <= 0:
            self.quit()
            return

        if self.tab_id in tabs:
            self.driver.stop()

        for tab in tabs:
            self.browser.close_tab(tab)
            sleep(.2)
        while self.tabs_count != end_len:
            sleep(.1)

        if self._main_tab in tabs:
            self._main_tab = self.tabs[0]

        self.to_tab()

    def close_other_tabs(self, tabs_or_ids=None):
        """关闭传入的标签页以外标签页，默认保留当前页。可传入多个
        :param tabs_or_ids: 要保留的标签页对象或id，可传入列表或元组，为None时保存当前页
        :return: None
        """
        self.close_tabs(tabs_or_ids, True)

    def quit(self):
        """关闭浏览器"""
        self.browser.quit()


def get_rename(original, rename):
    if '.' in rename:
        return rename
    else:
        suffix = original[original.rfind('.'):] if '.' in original else ''
        return f'{rename}{suffix}'
