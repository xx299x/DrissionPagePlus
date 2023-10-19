# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from pathlib import Path
from time import perf_counter, sleep

from requests import get

from .browser import Browser
from .chromium_base import ChromiumBase, Timeout
from .chromium_tab import ChromiumTab
from .commons.browser import connect_browser
from .configs.chromium_options import ChromiumOptions
from .setter import ChromiumPageSetter
from .waiter import ChromiumPageWaiter


class ChromiumPage(ChromiumBase):
    """用于管理浏览器的类"""

    def __init__(self, addr_or_opts=None, tab_id=None, timeout=None, addr_driver_opts=None):
        """
        :param addr_or_opts: 浏览器地址:端口或ChromiumOptions对象
        :param tab_id: 要控制的标签页id，不指定默认为激活的
        :param timeout: 超时时间
        """
        if addr_driver_opts:
            addr_or_opts = addr_driver_opts
        self._page = self
        address = self._handle_options(addr_or_opts)
        self._run_browser()
        super().__init__(address, tab_id)
        self.set.timeouts(implicit=timeout)
        self._page_init()

    def _handle_options(self, addr_or_opts):
        """设置浏览器启动属性
        :param addr_or_opts: 'ip:port'、ChromiumOptions
        :return: 返回浏览器地址
        """
        if not addr_or_opts:
            self._driver_options = ChromiumOptions(addr_or_opts)

        elif isinstance(addr_or_opts, ChromiumOptions):
            self._driver_options = addr_or_opts

        # 接收浏览器地址和端口
        elif isinstance(addr_or_opts, str):
            self._driver_options = ChromiumOptions()
            self._driver_options.debugger_address = addr_or_opts

        else:
            raise TypeError('只能接收ip:port格式或ChromiumOptions类型参数。')

        return self._driver_options.debugger_address

    def _run_browser(self):
        """连接浏览器"""
        connect_browser(self._driver_options)
        ws = get(f'http://{self._driver_options.debugger_address}/json/version',
                 headers={'Connection': 'close'}).json()['webSocketDebuggerUrl']
        self._browser = Browser(self._driver_options.debugger_address, ws.split('/')[-1], self)

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
        self._alert = Alert()
        self._driver.set_listener('Page.javascriptDialogOpening', self._on_alert_open)
        self._driver.set_listener('Page.javascriptDialogClosed', self._on_alert_close)

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
            self._rect = ChromiumTabRect(self)
        return self._rect

    @property
    def wait(self):
        """返回用于等待的对象"""
        if self._wait is None:
            self._wait = ChromiumPageWaiter(self)
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
            begin_tabs = set(self.tabs)
            len_tabs = len(begin_tabs)
            tid = self.run_cdp('Target.createTarget', url='')['targetId']

            tabs = self.tabs
            while len(tabs) == len_tabs:
                tabs = self.tabs
                sleep(.005)

            new_tab = set(tabs) - begin_tabs
            self._to_tab(new_tab.pop(), read_doc=False)
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
        :return: 新标签页对象
        """
        return ChromiumTab(self, self._new_tab(url, switch_to))

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
        while len(self.tabs) != end_len:
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

    def handle_alert(self, accept=True, send=None, timeout=None):
        """处理提示框，可以自动等待提示框出现
        :param accept: True表示确认，False表示取消，其它值不会按按钮但依然返回文本值
        :param send: 处理prompt提示框时可输入文本
        :param timeout: 等待提示框出现的超时时间，为None则使用self.timeout属性的值
        :return: 提示框内容文本，未等到提示框则返回False
        """
        timeout = self.timeout if timeout is None else timeout
        timeout = .1 if timeout <= 0 else timeout
        end_time = perf_counter() + timeout
        while not self._alert.activated and perf_counter() < end_time:
            sleep(.1)
        if not self._alert.activated:
            return False

        res_text = self._alert.text
        if self._alert.type == 'prompt':
            self.driver.call_method('Page.handleJavaScriptDialog', accept=accept, promptText=send)
        else:
            self.driver.call_method('Page.handleJavaScriptDialog', accept=accept)
        return res_text

    def quit(self):
        """关闭浏览器"""
        self.browser.quit()

    def _on_alert_close(self, **kwargs):
        """alert关闭时触发的方法"""
        self._alert.activated = False
        self._alert.text = None
        self._alert.type = None
        self._alert.defaultPrompt = None
        self._alert.response_accept = kwargs.get('result')
        self._alert.response_text = kwargs['userInput']
        self._driver.has_alert = False

    def _on_alert_open(self, **kwargs):
        """alert出现时触发的方法"""
        self._alert.activated = True
        self._alert.text = kwargs['message']
        self._alert.type = kwargs['message']
        self._alert.defaultPrompt = kwargs.get('defaultPrompt', None)
        self._alert.response_accept = None
        self._alert.response_text = None
        self._driver.has_alert = True


class ChromiumTabRect(object):
    def __init__(self, page):
        self._page = page

    @property
    def window_state(self):
        """返回窗口状态：normal、fullscreen、maximized、 minimized"""
        return self._get_browser_rect()['windowState']

    @property
    def browser_location(self):
        """返回浏览器在屏幕上的坐标，左上角为(0, 0)"""
        r = self._get_browser_rect()
        if r['windowState'] in ('maximized', 'fullscreen'):
            return 0, 0
        return r['left'] + 7, r['top']

    @property
    def page_location(self):
        """返回页面左上角在屏幕中坐标，左上角为(0, 0)"""
        w, h = self.viewport_location
        r = self._get_page_rect()['layoutViewport']
        return w - r['pageX'], h - r['pageY']

    @property
    def viewport_location(self):
        """返回视口在屏幕中坐标，左上角为(0, 0)"""
        w_bl, h_bl = self.browser_location
        w_bs, h_bs = self.browser_size
        w_vs, h_vs = self.viewport_size_with_scrollbar
        return w_bl + w_bs - w_vs, h_bl + h_bs - h_vs

    @property
    def browser_size(self):
        """返回浏览器大小"""
        r = self._get_browser_rect()
        if r['windowState'] == 'fullscreen':
            return r['width'], r['height']
        elif r['windowState'] == 'maximized':
            return r['width'] - 16, r['height'] - 16
        else:
            return r['width'] - 16, r['height'] - 7

    @property
    def page_size(self):
        """返回页面总宽高，格式：(宽, 高)"""
        r = self._get_page_rect()['contentSize']
        return r['width'], r['height']

    @property
    def viewport_size(self):
        """返回视口宽高，不包括滚动条，格式：(宽, 高)"""
        r = self._get_page_rect()['visualViewport']
        return r['clientWidth'], r['clientHeight']

    @property
    def viewport_size_with_scrollbar(self):
        """返回视口宽高，包括滚动条，格式：(宽, 高)"""
        r = self._page.run_js('return window.innerWidth.toString() + " " + window.innerHeight.toString();')
        w, h = r.split(' ')
        return int(w), int(h)

    def _get_page_rect(self):
        """获取页面范围信息"""
        return self._page.run_cdp_loaded('Page.getLayoutMetrics')

    def _get_browser_rect(self):
        """获取浏览器范围信息"""
        return self._page.browser.get_window_bounds()


class Alert(object):
    """用于保存alert信息的类"""

    def __init__(self):
        self.activated = False
        self.text = None
        self.type = None
        self.defaultPrompt = None
        self.response_accept = None
        self.response_text = None


def get_rename(original, rename):
    if '.' in rename:
        return rename
    else:
        suffix = original[original.rfind('.'):] if '.' in original else ''
        return f'{rename}{suffix}'
