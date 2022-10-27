# -*- coding:utf-8 -*-
from time import perf_counter, sleep
from typing import Union, Tuple

from pychrome import Tab
from requests import get as requests_get
from json import loads

from .base import BasePage
from .common import get_loc
from .drission import connect_chrome
from .chrome_element import ChromeElement


class ChromePage(BasePage):

    def __init__(self, address: str,
                 path: str = 'chrome',
                 tab_handle: str = None,
                 timeout: float = 10):
        super().__init__(timeout)
        self.debugger_address = address[7:] if address.startswith('http://') else address
        connect_chrome(path, self.debugger_address)
        tab_handle = self.tab_handles[0] if not tab_handle else tab_handle
        self._connect_debugger(tab_handle)

    def _connect_debugger(self, tab_handle: str):
        self.driver = Tab(id=tab_handle, type='page',
                          webSocketDebuggerUrl=f'ws://{self.debugger_address}/devtools/page/{tab_handle}')
        self.driver.start()
        self.driver.DOM.enable()
        self.driver.DOM.getDocument()

    @property
    def url(self) -> str:
        """返回当前页面url"""
        # todo: 是否有更好的方法？
        json = loads(requests_get(f'http://{self.debugger_address}/json').text)
        return [i['url'] for i in json if i['id'] == self.driver.id][0]

    @property
    def html(self) -> str:
        """返回当前页面html文本"""
        node_id = self.driver.DOM.getDocument()['root']['nodeId']
        return self.driver.DOM.getOuterHTML(nodeId=node_id)['outerHTML']

    @property
    def json(self) -> dict:
        """当返回内容是json格式时，返回对应的字典"""
        return loads(self('t:pre').text)

    @property
    def tab_handles(self) -> list:
        """返回所有标签页id"""
        json = loads(requests_get(f'http://{self.debugger_address}/json').text)
        return [i['id'] for i in json if i['type'] == 'page']

    @property
    def current_tab_handle(self) -> str:
        """返回当前标签页handle"""
        return self.driver.id

    @property
    def current_tab_index(self) -> int:
        """返回当前标签页序号"""
        return self.tab_handles.index(self.current_tab_handle)

    @property
    def ready_state(self) -> str:
        """返回当前页面加载状态，"""
        return self.driver.Runtime.evaluate(expression='document.readyState;')['result']['value']

    def get(self,
            url: str,
            show_errmsg: bool = False,
            retry: int = None,
            interval: float = None,
            timeout: float = None) -> Union[None, bool]:
        """访问url                                            \n
        :param url: 目标url
        :param show_errmsg: 是否显示和抛出异常
        :param retry: 重试次数
        :param interval: 重试间隔（秒）
        :param timeout: 连接超时时间
        :return: 目标url是否可用，返回None表示不确定
        """
        retry, interval = self._before_connect(url, retry, interval)
        self._url_available = self._d_connect(self._url,
                                              times=retry,
                                              interval=interval,
                                              show_errmsg=show_errmsg,
                                              timeout=timeout)
        return self._url_available

    def get_cookies(self, as_dict: bool = False):
        return self.driver.Network.getCookies()

    def ele(self, loc_or_ele: Union[Tuple[str, str], str, ChromeElement], timeout: float = None):
        return self._ele(loc_or_ele, timeout=timeout)

    def eles(self, loc_or_ele: Union[Tuple[str, str], str, ChromeElement], timeout: float = None):
        return self._ele(loc_or_ele, timeout=timeout, single=False)

    def _ele(self,
             loc_or_ele: Union[Tuple[str, str], str, ChromeElement],
             timeout: float = None,
             single: bool = True):
        if isinstance(loc_or_ele, (str, tuple)):
            loc = get_loc(loc_or_ele)[1]
        elif isinstance(loc_or_ele, ChromeElement):
            return loc_or_ele
        else:
            raise ValueError('loc_or_str参数只能是tuple、str、ChromeElement类型。')

        timeout = timeout if timeout is not None else self.timeout
        search = self.driver.DOM.performSearch(query=loc)
        count = search['resultCount']

        t1 = perf_counter()
        while count == 0 and perf_counter() - t1 < timeout:
            search = self.driver.DOM.performSearch(query=loc)
            count = search['resultCount']

        if count == 0:
            return None

        else:
            count = 1 if single else count
            nodeIds = self.driver.DOM.getSearchResults(searchId=search['searchId'], fromIndex=0, toIndex=count)
            if count == 1:
                return ChromeElement(self, node_id=nodeIds['nodeIds'][0])
            else:
                return [ChromeElement(self, node_id=i) for i in nodeIds['nodeIds']]

    def refresh(self, ignore_cache: bool = False) -> None:
        """刷新当前页面                      \n
        :param ignore_cache: 是否忽略缓存
        :return: None
        """
        self.driver.Page.reload(ignoreCache=ignore_cache)

    def forward(self, steps: int = 1) -> None:
        """在浏览历史中前进若干步    \n
        :param steps: 次数
        :return: None
        """
        self.driver.Runtime.evaluate(expression=f'window.history.go({steps});')

    def back(self, steps: int = 1) -> None:
        """在浏览历史中后退若干步    \n
        :param steps: 次数
        :return: None
        """
        self.driver.Runtime.evaluate(expression=f'window.history.go({-steps});')

    def stop_loading(self) -> None:
        self.driver.Page.stopLoading()

    def run_cdp(self, cmd: str, **cmd_args):
        """执行Chrome DevTools Protocol语句     \n
        :param cmd: 协议项目
        :param cmd_args: 参数
        :return: 执行的结果
        """
        return self.driver.call_method(cmd, **cmd_args)

    def create_tab(self, url: str = None) -> None:
        """新建并定位到一个标签页,该标签页在最后面       \n
        :param url: 新标签页跳转到的网址
        :return: None
        """
        url = f'?{url}' if url else ''
        requests_get(f'http://{self.debugger_address}/json/new{url}')

    def to_tab(self, num_or_handle: Union[int, str] = 0, activate: bool = True) -> None:
        """跳转到标签页                                                         \n
        注意：当程序使用的是接管的浏览器，获取到的 handle 顺序和视觉效果不一致         \n
        :param num_or_handle: 标签页序号或handle字符串，序号第一个为0，最后为-1
        :param activate: 切换后是否变为活动状态
        :return: None
        """
        try:
            tab = int(num_or_handle)
        except (ValueError, TypeError):
            tab = num_or_handle

        if not self.tab_handles:
            return

        tab = self.tab_handles[tab] if isinstance(tab, int) else tab
        self.driver.stop()
        self._connect_debugger(tab)

        if activate:
            requests_get(f'http://{self.debugger_address}/json/activate/{tab}')

    def close_tabs(self, num_or_handles: Union[int, str, list, tuple, set] = None, others: bool = False) -> None:
        """关闭传入的标签页，默认关闭当前页。可传入多个                                                        \n
        注意：当程序使用的是接管的浏览器，获取到的 handle 顺序和视觉效果不一致，不能按序号关闭。                    \n
        :param num_or_handles:要关闭的标签页序号或handle，可传入handle和序号组成的列表或元组，为None时关闭当前页
        :param others: 是否关闭指定标签页之外的
        :return: None
        """
        if others:
            all_tabs = self.tab_handles
            reserve_tabs = {self.current_tab_handle} if num_or_handles is None else _get_tabs(all_tabs, num_or_handles)
            tabs = set(all_tabs) - reserve_tabs
        else:
            tabs = (self.current_tab_handle,) if num_or_handles is None else _get_tabs(self.tab_handles, num_or_handles)

        tabs_len = len(tabs)
        all_len = len(self.tab_handles)
        if tabs_len > all_len:
            raise ValueError('要关闭的页面数量不能大于总数量。')

        is_alive = True
        if tabs_len == all_len:
            self.driver.stop()
            is_alive = False

        for tab in tabs:
            requests_get(f'http://{self.debugger_address}/json/close/{tab}')

        if is_alive:
            self.to_tab(0)

    def close_other_tabs(self, num_or_handles: Union[int, str, list, tuple] = None) -> None:
        """关闭传入的标签页以外标签页，默认保留当前页。可传入多个                                              \n
        注意：当程序使用的是接管的浏览器，获取到的 handle 顺序和视觉效果不一致，不能按序号关闭。                   \n
        :param num_or_handles: 要保留的标签页序号或handle，可传入handle和序号组成的列表或元组，为None时保存当前页
        :return: None
        """
        self.close_tabs(num_or_handles, True)

    def clean_cache(self,
                    session_storage: bool = True,
                    local_storage: bool = True,
                    cache: bool = True,
                    cookies: bool = True) -> None:
        """清除缓存，可选要清除的项                            \n
        :param session_storage: 是否清除sessionStorage
        :param local_storage: 是否清除localStorage
        :param cache: 是否清除cache
        :param cookies: 是否清除cookies
        :return: None
        """
        if session_storage:
            self.driver.Runtime.evaluate(expression='sessionStorage.clear();')
        if local_storage:
            self.driver.Runtime.evaluate(expression='localStorage.clear();')
        if cache:
            self.driver.Network.clearBrowserCache()
        if cookies:
            self.driver.Network.clearBrowserCookies()

    def _d_connect(self,
                   to_url: str,
                   times: int = 0,
                   interval: float = 1,
                   show_errmsg: bool = False,
                   timeout: float = None) -> Union[bool, None]:
        """尝试连接，重试若干次                            \n
        :param to_url: 要访问的url
        :param times: 重试次数
        :param interval: 重试间隔（秒）
        :param show_errmsg: 是否抛出异常
        :return: 是否成功，返回None表示不确定
        """
        err = None
        is_ok = False
        timeout = timeout if timeout is not None else self.timeout

        for _ in range(times + 1):
            try:
                result = self.driver.Page.navigate(url=to_url)
                t1 = perf_counter()
                while self.ready_state != 'complete' and perf_counter() - t1 < timeout:
                    sleep(.5)
                if self.ready_state != 'complete':
                    raise TimeoutError
                if 'errorText' in result:
                    raise ConnectionError(result['errorText'])
                go_ok = True
            except Exception as e:
                err = e
                go_ok = False

            is_ok = self.check_page() if go_ok else False

            if is_ok is not False:
                break

            if _ < times:
                sleep(interval)
                if show_errmsg:
                    print(f'重试 {to_url}')

        if is_ok is False and show_errmsg:
            raise err if err is not None else ConnectionError('连接异常。')

        return is_ok

    def check_page(self):
        pass


def _get_tabs(handles: list, num_or_handles: Union[int, str, list, tuple, set]) -> set:
    """返回指定标签页handle组成的set                           \n
    :param handles: handles列表
    :param num_or_handles: 指定的标签页，可以是多个
    :return: 指定标签页组成的set
    """
    if isinstance(num_or_handles, (int, str)):
        num_or_handles = (num_or_handles,)
    elif not isinstance(num_or_handles, (list, tuple, set)):
        raise TypeError('num_or_handle参数只能是int、str、list、set 或 tuple类型。')

    return set(i if isinstance(i, str) else handles[i] for i in num_or_handles)
