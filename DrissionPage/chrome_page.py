# -*- coding:utf-8 -*-
from pathlib import Path
from platform import system
from re import search
from time import perf_counter, sleep
from typing import Union, Tuple, List, Any

from pychrome import Tab
from requests import get as requests_get
from json import loads

from requests.cookies import RequestsCookieJar

from .session_element import SessionElement, make_session_ele
from .config import DriverOptions, _cookies_to_tuple
from .base import BasePage
from .common import get_loc
from .drission import connect_chrome
from .chrome_element import ChromeElement, ChromeScroll, _run_script, ChromeElementWaiter


class ChromePage(BasePage):

    def __init__(self, Tab_or_Options: Union[Tab, DriverOptions] = None,
                 tab_handle: str = None,
                 timeout: float = 10):
        super().__init__(timeout)
        self._connect_debugger(Tab_or_Options, tab_handle)

    def _connect_debugger(self, Tab_or_Options: Union[Tab, DriverOptions] = None, tab_handle: str = None):
        self.timeouts = Timeout(self)
        self._page_load_strategy = 'normal'
        if isinstance(Tab_or_Options, Tab):
            self._driver = Tab_or_Options
            self.address = search(r'ws://(.*?)/dev', Tab_or_Options._websocket_url).group(1)
            self.options = None
            self.process = None

        elif isinstance(Tab_or_Options, DriverOptions):
            self.options = Tab_or_Options or DriverOptions()  # 从ini文件读取
            self.set_timeouts(page_load=self.options.timeouts['pageLoad'],
                              script=self.options.timeouts['script'])
            self._page_load_strategy = self.options.page_load_strategy
            self.process = connect_chrome(self.options)[1]
            self.address = self.options.debugger_address
            tab_handle = self.tab_handles[0] if not tab_handle else tab_handle
            self._driver = Tab(id=tab_handle, type='page',
                               webSocketDebuggerUrl=f'ws://{self.options.debugger_address}/devtools/page/{tab_handle}')

        else:
            raise TypeError('只能接收Tab或DriverOptions类型参数。')

        self._driver.start()
        self._driver.DOM.enable()
        self._driver.Page.enable()
        root = self._driver.DOM.getDocument()
        self.root = ChromeElement(self, node_id=root['root']['nodeId'])

        self._alert = Alert()
        self.driver.Page.javascriptDialogOpening = self._on_alert_open
        self.driver.Page.javascriptDialogClosed = self._on_alert_close

    def __call__(self, loc_or_str: Union[Tuple[str, str], str, 'ChromeElement'],
                 timeout: float = None) -> Union['ChromeElement', str, None]:
        """在内部查找元素                                           \n
        例：ele = page('@id=ele_id')                              \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 超时时间
        :return: DriverElement对象或属性、文本
        """
        return self.ele(loc_or_str, timeout)

    @property
    def driver(self) -> Tab:
        return self._driver

    @property
    def url(self) -> str:
        """返回当前页面url"""
        tab_id = self.driver.id  # 用于WebPage时激活浏览器
        json = loads(requests_get(f'http://{self.address}/json').text)
        return [i['url'] for i in json if i['id'] == tab_id][0]

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
    def tabs_count(self) -> int:
        """返回标签页数量"""
        return len(self.tab_handles)

    @property
    def tab_handles(self) -> list:
        """返回所有标签页id"""
        json = loads(requests_get(f'http://{self.address}/json').text)
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
        return self.run_script('document.readyState;', as_expr=True)

    @property
    def scroll(self) -> ChromeScroll:
        """用于滚动滚动条的对象"""
        if not hasattr(self, '_scroll'):
            self._scroll = ChromeScroll(self)
        return self._scroll

    @property
    def size(self) -> dict:
        """返回页面总长宽"""
        w = self.run_script('document.body.scrollWidth;', as_expr=True)
        h = self.run_script('document.body.scrollHeight;', as_expr=True)
        return {'height': h, 'width': w}

    @property
    def active_ele(self) -> ChromeElement:
        return self.run_script('return document.activeElement;')

    @property
    def page_load_strategy(self) -> str:
        """返回页面加载策略"""
        return self._page_load_strategy

    def set_page_load_strategy(self, value: str) -> None:
        """设置页面加载策略，可选'normal', 'eager', 'none'"""
        if value not in ('normal', 'eager', 'none'):
            raise ValueError("只能选择'normal', 'eager', 'none'。")
        self._page_load_strategy = value

    def set_timeouts(self, implicit: float = None, page_load: float = None, script: float = None) -> None:
        """设置超时时间，单位为秒，selenium4以上版本有效       \n
        :param implicit: 查找元素超时时间
        :param page_load: 页面加载超时时间
        :param script: 脚本运行超时时间
        :return: None
        """
        if implicit is not None:
            self.timeout = implicit

        if page_load is not None:
            self.timeouts.page_load = page_load

        if script is not None:
            self.timeouts.script = script

    def run_script(self, script: str, as_expr: bool = False, *args: Any) -> Any:
        """运行javascript代码                                                 \n
        :param script: js文本
        :param as_expr: 是否作为表达式运行，为True时args无效
        :param args: 参数，按顺序在js文本中对应argument[0]、argument[2]...
        :return: 运行的结果
        """
        return _run_script(self, script, as_expr, self.timeouts.script, args)

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
        self.driver.DOM.getDocument()
        return self._url_available

    def get_cookies(self, as_dict: bool = False) -> Union[list, dict]:
        cookies = self.driver.Network.getCookies()['cookies']
        if as_dict:
            return {cookie['name']: cookie['value'] for cookie in cookies}
        else:
            return cookies

    def set_cookies(self, cookies: Union[RequestsCookieJar, list, tuple, str, dict]):
        cookies = _cookies_to_tuple(cookies)
        result_cookies = []
        for cookie in cookies:
            if not cookie.get('domain', None):
                continue
            c = {'value': '' if cookie['value'] is None else cookie['value'],
                 'name': cookie['name'],
                 'domain': cookie['domain']}
            result_cookies.append(c)
        self.driver.Network.setCookies(cookies=result_cookies)

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, ChromeElement],
            timeout: float = None) -> Union[ChromeElement, str, None]:
        return self._ele(loc_or_ele, timeout=timeout)

    def eles(self,
             loc_or_ele: Union[Tuple[str, str], str, ChromeElement],
             timeout: float = None) -> List[Union[ChromeElement, str]]:
        return self._ele(loc_or_ele, timeout=timeout, single=False)

    def s_ele(self, loc_or_ele: Union[Tuple[str, str], str, ChromeElement] = None) -> Union[SessionElement, str, None]:
        """查找第一个符合条件的元素以SessionElement形式返回，处理复杂页面时效率很高       \n
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        if isinstance(loc_or_ele, ChromeElement):
            return make_session_ele(loc_or_ele)
        else:
            return make_session_ele(self, loc_or_ele)

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str] = None) -> List[Union[SessionElement, str]]:
        """查找所有符合条件的元素以SessionElement列表形式返回                       \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象组成的列表
        """
        return make_session_ele(self, loc_or_str, single=False)

    def _ele(self,
             loc_or_ele: Union[Tuple[str, str], str, ChromeElement],
             timeout: float = None,
             single: bool = True) -> Union[ChromeElement, str, None, List[Union[ChromeElement, str]]]:
        if isinstance(loc_or_ele, (str, tuple)):
            loc = get_loc(loc_or_ele)[1]
        elif isinstance(loc_or_ele, ChromeElement):
            return loc_or_ele
        else:
            raise ValueError('loc_or_str参数只能是tuple、str、ChromeElement类型。')

        timeout = timeout if timeout is not None else self.timeout
        search_result = self.driver.DOM.performSearch(query=loc, includeUserAgentShadowDOM=True)
        count = search_result['resultCount']

        end_time = perf_counter() + timeout
        while count == 0 and perf_counter() < end_time:
            search_result = self.driver.DOM.performSearch(query=loc, includeUserAgentShadowDOM=True)
            count = search_result['resultCount']

        if count == 0:
            return None

        else:
            count = 1 if single else count
            nodeIds = self.driver.DOM.getSearchResults(searchId=search_result['searchId'], fromIndex=0, toIndex=count)
            if count == 1:
                return ChromeElement(self, node_id=nodeIds['nodeIds'][0])
            else:
                return [ChromeElement(self, node_id=i) for i in nodeIds['nodeIds']]

    def wait_ele(self,
                 loc_or_ele: Union[str, tuple, ChromeElement],
                 timeout: float = None) -> ChromeElementWaiter:
        """等待元素从dom删除、显示、隐藏                             \n
        :param loc_or_ele: 可以是元素、查询字符串、loc元组
        :param timeout: 等待超时时间
        :return: 用于等待的ElementWaiter对象
        """
        return ChromeElementWaiter(self, loc_or_ele, timeout)

    def get_screenshot(self, path: [str, Path] = None,
                       as_bytes: [bool, str] = None,
                       full_page: bool = False,
                       left_top: Tuple[int, int] = None,
                       right_bottom: Tuple[int, int] = None) -> Union[str, bytes]:
        """对页面进行截图，可对整个网页、可见网页、指定范围截图。对可视范围外截图需要新版浏览器支持             \n
        :param path: 完整路径，后缀可选'jpg','jpeg','png','webp'
        :param as_bytes: 是否已字节形式返回图片，可选'jpg','jpeg','png','webp'，生效时path参数无效
        :param full_page: 是否整页截图，为True截取整个网页，为False截取可视窗口
        :param left_top: 截取范围左上角坐标
        :param right_bottom: 截取范围右下角角坐标
        :return: 图片完整路径或字节文本
        """
        if as_bytes:
            if as_bytes is True:
                pic_type = 'png'
            else:
                if as_bytes not in ('jpg', 'jpeg', 'png', 'webp'):
                    raise ValueError("只能接收'jpg', 'jpeg', 'png', 'webp'四种格式。")
                pic_type = 'jpeg' if as_bytes == 'jpg' else as_bytes

        else:
            if not path:
                raise ValueError('保存为文件时必须传入路径。')
            path = Path(path)
            pic_type = path.suffix.lower()
            if pic_type not in ('.jpg', '.jpeg', '.png', '.webp'):
                raise TypeError(f'不支持的文件格式：{pic_type}。')
            pic_type = 'jpeg' if pic_type == '.jpg' else pic_type[1:]

        hw = self.size
        if full_page:
            vp = {'x': 0, 'y': 0, 'width': hw['width'], 'height': hw['height'], 'scale': 1}
            png = self.driver.Page.captureScreenshot(format=pic_type, captureBeyondViewport=True, clip=vp)['data']
        else:
            if left_top and right_bottom:
                x, y = left_top
                w = right_bottom[0] - x
                h = right_bottom[1] - y
                vp = {'x': x, 'y': y, 'width': w, 'height': h, 'scale': 1}
                png = self.driver.Page.captureScreenshot(format=pic_type, captureBeyondViewport=True, clip=vp)['data']
            else:
                png = self.driver.Page.captureScreenshot(format=pic_type)['data']

        from base64 import b64decode
        png = b64decode(png)

        if as_bytes:
            return png

        path.parent.mkdir(parents=True, exist_ok=True)
        with open(path, 'wb') as f:
            f.write(png)
        return str(path.absolute())

    def scroll_to_see(self, loc_or_ele: Union[str, tuple, ChromeElement]) -> None:
        """滚动页面直到元素可见                                                        \n
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串（详见ele函数注释）
        :return: None
        """
        node_id = self.ele(loc_or_ele).node_id
        self.driver.DOM.scrollIntoViewIfNeeded(nodeId=node_id)

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
        self.run_script(f'window.history.go({steps});', as_expr=True)

    def back(self, steps: int = 1) -> None:
        """在浏览历史中后退若干步    \n
        :param steps: 次数
        :return: None
        """
        self.run_script(f'window.history.go({-steps});', as_expr=True)

    def stop_loading(self) -> None:
        """页面停止加载"""
        self.driver.Page.stopLoading()

    def run_cdp(self, cmd: str, **cmd_args):
        """执行Chrome DevTools Protocol语句     \n
        :param cmd: 协议项目
        :param cmd_args: 参数
        :return: 执行的结果
        """
        return self.driver.call_method(cmd, **cmd_args)

    def set_user_agent(self, ua: str) -> None:
        """为当前tab设置user agent，只在当前tab有效          \n
        :param ua: user agent字符串
        :return: None
        """
        self.driver.Network.setUserAgentOverride(userAgent=ua)

    def get_session_storage(self, item: str = None) -> Union[str, dict, None]:
        """获取sessionStorage信息，不设置item则获取全部       \n
        :param item: 要获取的项，不设置则返回全部
        :return: sessionStorage一个或所有项内容
        """
        js = f'sessionStorage.getItem("{item}");' if item else 'sessionStorage;'
        return self.run_script(js, as_expr=True)

    def get_local_storage(self, item: str = None) -> Union[str, dict, None]:
        """获取localStorage信息，不设置item则获取全部       \n
        :param item: 要获取的项目，不设置则返回全部
        :return: localStorage一个或所有项内容
        """
        js = f'localStorage.getItem("{item}");' if item else 'localStorage;'
        return self.run_script(js, as_expr=True)

    def set_session_storage(self, item: str, value: Union[str, bool]) -> None:
        """设置或删除某项sessionStorage信息                         \n
        :param item: 要设置的项
        :param value: 项的值，设置为False时，删除该项
        :return: None
        """
        js = f'sessionStorage.removeItem("{item}");' if item is False else f'sessionStorage.setItem("{item}","{value}");'
        return self.run_script(js, as_expr=True)

    def set_local_storage(self, item: str, value: Union[str, bool]) -> None:
        """设置或删除某项localStorage信息                           \n
        :param item: 要设置的项
        :param value: 项的值，设置为False时，删除该项
        :return: None
        """
        js = f'localStorage.removeItem("{item}");' if item is False else f'localStorage.setItem("{item}","{value}");'
        return self.run_script(js, as_expr=True)

    def create_tab(self, url: str = None) -> None:
        """新建并定位到一个标签页,该标签页在最后面       \n
        :param url: 新标签页跳转到的网址
        :return: None
        """
        url = f'?{url}' if url else ''
        requests_get(f'http://{self.address}/json/new{url}')

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
            requests_get(f'http://{self.address}/json/activate/{tab}')

    def to_front(self) -> None:
        """激活当前标签页使其处于最前面"""
        requests_get(f'http://{self.address}/json/activate/{self.current_tab_handle}')

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
            requests_get(f'http://{self.address}/json/close/{tab}')

        if is_alive:
            self.to_tab(0)

    def close_other_tabs(self, num_or_handles: Union[int, str, list, tuple] = None) -> None:
        """关闭传入的标签页以外标签页，默认保留当前页。可传入多个                                              \n
        注意：当程序使用的是接管的浏览器，获取到的 handle 顺序和视觉效果不一致，不能按序号关闭。                   \n
        :param num_or_handles: 要保留的标签页序号或handle，可传入handle和序号组成的列表或元组，为None时保存当前页
        :return: None
        """
        self.close_tabs(num_or_handles, True)

    def set_window_size(self, width: int = None, height: int = None) -> None:
        """设置浏览器窗口大小，默认最大化，任一参数为0最小化  \n
        :param width: 浏览器窗口高
        :param height: 浏览器窗口宽
        :return: None
        """
        self.driver.Emulation.setDeviceMetricsOverride(width=500, height=500,
                                                       deviceScaleFactor=0, mobile=False,
                                                       )
        # if width is None and height is None:
        #     self.driver.maximize_window()
        #
        # elif width == 0 or height == 0:
        #     self.driver.minimize_window()
        #
        # else:
        #     if width < 0 or height < 0:
        #         raise ValueError('x 和 y参数必须大于0。')
        #
        #     new_x = width or self.driver.get_window_size()['width']
        #     new_y = height or self.driver.get_window_size()['height']
        #     self.driver.set_window_size(new_x, new_y)

    def clear_cache(self,
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
            self.run_script('sessionStorage.clear();', as_expr=True)
        if local_storage:
            self.run_script('localStorage.clear();', as_expr=True)
        if cache:
            self.driver.Network.clearBrowserCache()
        if cookies:
            self.driver.Network.clearBrowserCookies()

    def handle_alert(self, accept: bool = True, send: str = None, timeout: float = None) -> Union[str, None]:
        """处理提示框                                                            \n
        :param accept: True表示确认，False表示取消，其它值不会按按钮但依然返回文本值
        :param send: 处理prompt提示框时可输入文本
        :param timeout: 等待提示框出现的超时时间
        :return: 提示框内容文本，未等到提示框则返回None
        """
        timeout = timeout or self.timeout
        end_time = perf_counter() + timeout
        while not self._alert.activated and perf_counter() < end_time:
            sleep(.1)
        if not self._alert.activated:
            return None

        res_text = self._alert.text
        if self._alert.type == 'prompt':
            self.driver.Page.handleJavaScriptDialog(accept=accept, promptText=send)
        else:
            self.driver.Page.handleJavaScriptDialog(accept=accept)
        return res_text

    def hide_browser(self) -> None:
        """隐藏浏览器窗口，只在Windows系统可用"""
        _show_or_hide_browser(self, hide=True)

    def show_browser(self) -> None:
        """显示浏览器窗口，只在Windows系统可用"""
        _show_or_hide_browser(self, hide=False)

    def check_page(self) -> Union[bool, None]:
        """检查页面是否符合预期            \n
        由子类自行实现各页面的判定规则
        """
        return None

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
        :param timeout: 连接超时时间
        :return: 是否成功，返回None表示不确定
        """
        err = None
        is_ok = False
        timeout = timeout if timeout is not None else self.timeouts.page_load

        for _ in range(times + 1):
            result = self.driver.Page.navigate(url=to_url)

            is_timeout = True
            end_time = perf_counter() + timeout
            while perf_counter() < end_time:
                if ((self.page_load_strategy == 'normal' and self.ready_state == 'complete')
                        or (self.page_load_strategy == 'eager' and self.ready_state in ('interactive', 'complete'))
                        or (self.page_load_strategy == 'none' and self.ready_state
                            in ('loading', 'interactive', 'complete'))):
                    self.stop_loading()
                    is_timeout = False
                    break

            if is_timeout:
                raise TimeoutError('页面连接超时。')
            if 'errorText' in result:
                raise ConnectionError(result['errorText'])

            is_ok = self.check_page()

            if is_ok is not False:
                break

            if _ < times:
                sleep(interval)
                if show_errmsg:
                    print(f'重试 {to_url}')

        if is_ok is False and show_errmsg:
            raise err if err is not None else ConnectionError('连接异常。')

        return is_ok

    def _on_alert_close(self, **kwargs):
        """alert关闭时触发的方法"""
        self._alert.activated = False
        self._alert.text = None
        self._alert.type = None
        self._alert.defaultPrompt = None
        self._alert.response_accept = kwargs.get('result')
        self._alert.response_text = kwargs['userInput']

    def _on_alert_open(self, **kwargs):
        """alert出现时触发的方法"""
        self._alert.activated = True
        self._alert.text = kwargs['message']
        self._alert.type = kwargs['message']
        self._alert.defaultPrompt = kwargs.get('defaultPrompt', None)
        self._alert.response_accept = None
        self._alert.response_text = None


class Alert(object):
    """用于保存alert信息"""

    def __init__(self):
        self.activated = False
        self.text = None
        self.type = None
        self.defaultPrompt = None
        self.response_accept = None
        self.response_text = None


class Timeout(object):
    """用于保存d模式timeout信息"""

    def __init__(self, page: ChromePage):
        self.page = page
        self.page_load = 30
        self.script = 30

    @property
    def implicit(self):
        return self.page.timeout


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


def _show_or_hide_browser(page: ChromePage, hide: bool = True) -> None:
    if system().lower() != 'windows':
        raise OSError('该方法只能在Windows系统使用。')

    try:
        from win32gui import ShowWindow
        from win32con import SW_HIDE, SW_SHOW
    except ImportError:
        raise ImportError('请先安装：pip install pypiwin32')

    pid = _get_browser_progress_id(page.process, page.address)
    if not pid:
        return None
    hds = _get_chrome_hwnds_from_pid(pid, page.title)
    sw = SW_HIDE if hide else SW_SHOW
    for hd in hds:
        ShowWindow(hd, sw)


def _get_browser_progress_id(progress, address: str) -> Union[str, None]:
    """获取浏览器进程id"""
    if progress:
        return progress.pid

    address = address.split(':')
    if len(address) != 2:
        return None

    ip, port = address
    if ip not in ('127.0.0.1', 'localhost') or not port.isdigit():
        return None

    from os import popen
    txt = ''
    progresses = popen(f'netstat -nao | findstr :{port}').read().split('\n')
    for progress in progresses:
        if 'LISTENING' in progress:
            txt = progress
            break
    if not txt:
        return None

    return txt.split(' ')[-1]


def _get_chrome_hwnds_from_pid(pid, title) -> list:
    """通过PID查询句柄ID"""
    try:
        from win32gui import IsWindow, GetWindowText, EnumWindows
        from win32process import GetWindowThreadProcessId
    except ImportError:
        raise ImportError('请先安装win32gui，pip install pypiwin32')

    def callback(hwnd, hds):
        if IsWindow(hwnd) and title in GetWindowText(hwnd):
            _, found_pid = GetWindowThreadProcessId(hwnd)
            if str(found_pid) == str(pid):
                hds.append(hwnd)
            return True

    hwnds = []
    EnumWindows(callback, hwnds)
    return hwnds
