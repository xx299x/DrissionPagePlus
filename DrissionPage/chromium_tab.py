# -*- coding:utf-8 -*-
from pathlib import Path
from time import perf_counter, sleep
from typing import Union, Tuple, List, Any

from pychrome import Tab
from requests import Session
from json import loads

from requests.cookies import RequestsCookieJar

from .session_element import SessionElement, make_session_ele
from .config import DriverOptions, _cookies_to_tuple
from .base import BasePage
from .common import get_loc
from .chromium_element import ChromiumElement, ChromeScroll, _run_script, ChromeElementWaiter


class ChromiumBase(BasePage):
    """用于管理浏览器的类"""

    def __init__(self, address: str,
                 tab_id: str = None,
                 timeout: float = None):
        """初始化                                                      \n
        :param address: 浏览器地址:端口、Tab对象或DriverOptions对象
        :param tab_id: 要控制的标签页id，不指定默认为激活的
        :param timeout: 超时时间
        """
        super().__init__(timeout)
        self._is_loading = None
        self._root_id = None
        self._connect_browser(address, tab_id)

    def _connect_browser(self, addr_tab_opts: Union[str, Tab, DriverOptions] = None,
                         tab_id: str = None) -> None:
        """连接浏览器，在第一次时运行                                    \n
        :param addr_tab_opts: 浏览器地址、Tab对象或DriverOptions对象
        :param tab_id: 要控制的标签页id，不指定默认为激活的
        :return: None
        """
        self._is_loading = False
        self._root_id = None
        self.timeouts = Timeout(self)
        self._control_session = Session()
        self._control_session.keep_alive = False
        self._first_run = True

        self.address = addr_tab_opts
        if not tab_id:
            json = loads(self._control_session.get(f'http://{self.address}/json').text)
            tab_id = [i['id'] for i in json if i['type'] == 'page'][0]
        self._set_options()
        self._init_page(tab_id)
        self._get_document()
        self._first_run = False

    def _init_page(self, tab_id: str = None) -> None:
        """新建页面、页面刷新、切换标签页后要进行的cdp参数初始化
        :param tab_id: 要跳转到的标签页id
        :return: None
        """
        self._is_loading = True
        if tab_id:
            self._driver = Tab(id=tab_id, type='page',
                               webSocketDebuggerUrl=f'ws://{self.address}/devtools/page/{tab_id}')

        self._driver.start()
        self._driver.DOM.enable()
        self._driver.Page.enable()

        self._driver.Page.frameNavigated = self._onFrameNavigated
        self._driver.Page.loadEventFired = self._onLoadEventFired
        self._driver.DOM.documentUpdated = self._onDocumentUpdated

    def _get_document(self) -> None:
        """刷新cdp使用的document数据"""
        # print('get doc')
        self._wait_loading()
        root_id = self._driver.DOM.getDocument()['root']['nodeId']
        self._root_id = self._driver.DOM.resolveNode(nodeId=root_id)['object']['objectId']
        self._is_loading = False

    def _wait_loading(self, timeout: float = None) -> bool:
        """等待页面加载完成
        :param timeout: 超时时间
        :return: 是否成功，超时返回False
        """
        timeout = timeout if timeout is not None else self.timeouts.page_load

        end_time = perf_counter() + timeout
        while perf_counter() < end_time:
            state = self.ready_state
            if state == 'complete':
                return True
            elif self.page_load_strategy == 'eager' and state in ('interactive', 'complete'):
                self.stop_loading()
                return True
            elif self.page_load_strategy == 'none':
                self.stop_loading()
                return True

        return False

    def _onLoadEventFired(self, **kwargs):
        """在页面刷新、变化后重新读取页面内容"""
        print('load complete')
        if self._first_run is False and self._is_loading:
            self._get_document()

    def _onFrameNavigated(self, **kwargs):
        """页面跳转时触发"""
        # todo: 考虑frame的情况，修改别的判断方式
        if not kwargs['frame'].get('parentId', None):
            print('nav')
            self._is_loading = True

    def _onDocumentUpdated(self, **kwargs):
        print('doc')
        pass

    def _set_options(self) -> None:
        pass

    def __call__(self, loc_or_str: Union[Tuple[str, str], str, 'ChromiumElement'],
                 timeout: float = None) -> Union['ChromiumElement', None]:
        """在内部查找元素                                              \n
        例：ele = page('@id=ele_id')                                 \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 超时时间
        :return: ChromeElement对象
        """
        return self.ele(loc_or_str, timeout)

    @property
    def driver(self) -> Tab:
        """返回用于控制浏览器的Tab对象"""
        return self._driver

    @property
    def _wait_driver(self) -> Tab:
        """返回用于控制浏览器的Tab对象，会先等待页面加载完毕"""
        while self._is_loading:
            print('loading')
            sleep(.1)
        return self._driver

    @property
    def url(self) -> str:
        """返回当前页面url"""
        tab_id = self._wait_driver.id  # 用于WebPage时激活浏览器
        json = loads(self._control_session.get(f'http://{self.address}/json').text)
        return [i['url'] for i in json if i['id'] == tab_id][0]

    @property
    def html(self) -> str:
        """返回当前页面html文本"""
        node_id = self._wait_driver.DOM.getDocument()['root']['nodeId']
        return self._wait_driver.DOM.getOuterHTML(nodeId=node_id)['outerHTML']

    @property
    def json(self) -> dict:
        """当返回内容是json格式时，返回对应的字典"""
        return loads(self('t:pre').text)

    @property
    def tab_id(self) -> str:
        """返回当前标签页id"""
        return self._wait_driver.id

    @property
    def ready_state(self) -> str:
        """返回当前页面加载状态，'loading' 'interactive' 'complete'"""
        return self._driver.Runtime.evaluate(expression='document.readyState;')['result']['value']

    @property
    def size(self) -> dict:
        """返回页面总长宽，{'height': int, 'width': int}"""
        w = self.run_script('document.body.scrollWidth;', as_expr=True)
        h = self.run_script('document.body.scrollHeight;', as_expr=True)
        return {'height': h, 'width': w}

    @property
    def active_ele(self) -> ChromiumElement:
        """返回当前焦点所在元素"""
        return self.run_script('return document.activeElement;')

    @property
    def page_load_strategy(self) -> str:
        """返回页面加载策略"""
        return self._page_load_strategy

    @property
    def scroll(self) -> ChromeScroll:
        """返回用于滚动滚动条的对象"""
        if not hasattr(self, '_scroll'):
            self._scroll = ChromeScroll(self)
        return self._scroll

    def set_page_load_strategy(self, value: str) -> None:
        """设置页面加载策略                                    \n
        :param value: 可选'normal', 'eager', 'none'
        :return: None
        """
        if value not in ('normal', 'eager', 'none'):
            raise ValueError("只能选择'normal', 'eager', 'none'。")
        self._page_load_strategy = value

    def set_timeouts(self, implicit: float = None, page_load: float = None, script: float = None) -> None:
        """设置超时时间，单位为秒                   \n
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

    def run_async_script(self, script: str, as_expr: bool = False, *args: Any) -> None:
        """以异步方式执行js代码                                                 \n
        :param script: js文本
        :param as_expr: 是否作为表达式运行，为True时args无效
        :param args: 参数，按顺序在js文本中对应argument[0]、argument[2]...
        :return: None
        """
        from threading import Thread
        Thread(target=_run_script, args=(self, script, as_expr, self.timeouts.script, args)).start()

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

    def get_cookies(self, as_dict: bool = False) -> Union[list, dict]:
        """获取cookies信息                                              \n
        :param as_dict: 为True时返回由{name: value}键值对组成的dict
        :return: cookies信息
        """
        cookies = self._wait_driver.Network.getCookies()['cookies']
        if as_dict:
            return {cookie['name']: cookie['value'] for cookie in cookies}
        else:
            return cookies

    def set_cookies(self, cookies: Union[RequestsCookieJar, list, tuple, str, dict]) -> None:
        """设置cookies值                            \n
        :param cookies: cookies信息
        :return: None
        """
        cookies = _cookies_to_tuple(cookies)
        result_cookies = []
        for cookie in cookies:
            if not cookie.get('domain', None):
                continue
            c = {'value': '' if cookie['value'] is None else cookie['value'],
                 'name': cookie['name'],
                 'domain': cookie['domain']}
            result_cookies.append(c)
        self._wait_driver.Network.setCookies(cookies=result_cookies)

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, ChromiumElement],
            timeout: float = None) -> Union[ChromiumElement, None]:
        """获取第一个符合条件的元素对象                       \n
        :param loc_or_ele: 定位符或元素对象
        :param timeout: 查找超时时间
        :return: ChromeElement对象
        """
        return self._ele(loc_or_ele, timeout=timeout)

    def eles(self,
             loc_or_ele: Union[Tuple[str, str], str, ChromiumElement],
             timeout: float = None) -> List[ChromiumElement]:
        """获取所有符合条件的元素对象                         \n
        :param loc_or_ele: 定位符或元素对象
        :param timeout: 查找超时时间
        :return: ChromeElement对象组成的列表
        """
        return self._ele(loc_or_ele, timeout=timeout, single=False)

    def s_ele(self, loc_or_ele: Union[Tuple[str, str], str, ChromiumElement] = None) \
            -> Union[SessionElement, str, None]:
        """查找第一个符合条件的元素以SessionElement形式返回，处理复杂页面时效率很高       \n
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        if isinstance(loc_or_ele, ChromiumElement):
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
             loc_or_ele: Union[Tuple[str, str], str, ChromiumElement],
             timeout: float = None,
             single: bool = True) -> Union[ChromiumElement, None, List[ChromiumElement]]:
        """执行元素查找
        :param loc_or_ele: 定位符或元素对象
        :param timeout: 查找超时时间
        :param single: 是否只返回第一个
        :return: ChromeElement对象或元素对象组成的列表
        """
        if isinstance(loc_or_ele, (str, tuple)):
            loc = get_loc(loc_or_ele)[1]
        elif isinstance(loc_or_ele, ChromiumElement):
            return loc_or_ele
        else:
            raise ValueError('loc_or_str参数只能是tuple、str、ChromeElement类型。')

        timeout = timeout if timeout is not None else self.timeout
        search_result = self._wait_driver.DOM.performSearch(query=loc, includeUserAgentShadowDOM=True)
        count = search_result['resultCount']

        end_time = perf_counter() + timeout
        while count == 0 and perf_counter() < end_time:
            search_result = self._wait_driver.DOM.performSearch(query=loc, includeUserAgentShadowDOM=True)
            count = search_result['resultCount']

        if count == 0:
            return None

        else:
            count = 1 if single else count
            nodeIds = self._wait_driver.DOM.getSearchResults(searchId=search_result['searchId'], fromIndex=0,
                                                             toIndex=count)
            if count == 1:
                return ChromiumElement(self, node_id=nodeIds['nodeIds'][0])
            else:
                return [ChromiumElement(self, node_id=i) for i in nodeIds['nodeIds']]

    def wait_ele(self,
                 loc_or_ele: Union[str, tuple, ChromiumElement],
                 timeout: float = None) -> ChromeElementWaiter:
        """返回用于等待元素到达某个状态的等待器对象                             \n
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
        """对页面进行截图，可对整个网页、可见网页、指定范围截图。对可视范围外截图需要90以上版本浏览器支持             \n
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
            png = self._wait_driver.Page.captureScreenshot(format=pic_type, captureBeyondViewport=True, clip=vp)['data']
        else:
            if left_top and right_bottom:
                x, y = left_top
                w = right_bottom[0] - x
                h = right_bottom[1] - y
                vp = {'x': x, 'y': y, 'width': w, 'height': h, 'scale': 1}
                png = self._wait_driver.Page.captureScreenshot(format=pic_type, captureBeyondViewport=True, clip=vp)[
                    'data']
            else:
                png = self._wait_driver.Page.captureScreenshot(format=pic_type)['data']

        from base64 import b64decode
        png = b64decode(png)

        if as_bytes:
            return png

        path.parent.mkdir(parents=True, exist_ok=True)
        with open(path, 'wb') as f:
            f.write(png)
        return str(path.absolute())

    def scroll_to_see(self, loc_or_ele: Union[str, tuple, ChromiumElement]) -> None:
        """滚动页面直到元素可见                                                        \n
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串（详见ele函数注释）
        :return: None
        """
        node_id = self.ele(loc_or_ele).node_id
        try:
            self._wait_driver.DOM.scrollIntoViewIfNeeded(nodeId=node_id)
        except Exception:
            self.ele(loc_or_ele).run_script("this.scrollIntoView();")

    def refresh(self, ignore_cache: bool = False) -> None:
        """刷新当前页面                      \n
        :param ignore_cache: 是否忽略缓存
        :return: None
        """
        self._driver.Page.reload(ignoreCache=ignore_cache)

    def forward(self, steps: int = 1) -> None:
        """在浏览历史中前进若干步    \n
        :param steps: 前进步数
        :return: None
        """
        self.run_script(f'window.history.go({steps});', as_expr=True)

    def back(self, steps: int = 1) -> None:
        """在浏览历史中后退若干步    \n
        :param steps: 后退步数
        :return: None
        """
        self.run_script(f'window.history.go({-steps});', as_expr=True)

    def stop_loading(self) -> None:
        """页面停止加载"""
        self._driver.Page.stopLoading()
        self._get_document()

    def run_cdp(self, cmd: str, **cmd_args) -> dict:
        """执行Chrome DevTools Protocol语句     \n
        :param cmd: 协议项目
        :param cmd_args: 参数
        :return: 执行的结果
        """
        return self._driver.call_method(cmd, **cmd_args)

    def set_user_agent(self, ua: str) -> None:
        """为当前tab设置user agent，只在当前tab有效          \n
        :param ua: user agent字符串
        :return: None
        """
        self._wait_driver.Network.setUserAgentOverride(userAgent=ua)

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
            self._wait_driver.Network.clearBrowserCache()
        if cookies:
            self._wait_driver.Network.clearBrowserCookies()

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
        timeout = timeout if timeout is not None else self.timeouts.page_load

        for _ in range(times + 1):
            result = self._driver.Page.navigate(url=to_url)
            is_timeout = not self._wait_loading(timeout)

            if is_timeout:
                err = TimeoutError('页面连接超时。')
            if 'errorText' in result:
                err = ConnectionError(result['errorText'])

            if not err:
                break

            if _ < times:
                sleep(interval)
                if show_errmsg:
                    print(f'重试 {to_url}')

        if err and show_errmsg:
            raise err if err is not None else ConnectionError('连接异常。')

        return False if err else True


class ChromiumTab(ChromiumBase):
    """用于管理浏览器的类"""

    def __init__(self, page,
                 tab_id: str = None):
        """初始化                                                      \n
        :param page: 浏览器地址:端口、Tab对象或DriverOptions对象
        :param tab_id: 要控制的标签页id，不指定默认为激活的
        """
        self.page = page
        super().__init__(page.address, tab_id, page.timeout)

    def _set_options(self) -> None:
        self.set_timeouts(page_load=self.page.timeouts['pageLoad'],
                          script=self.page.timeouts['script'] / 1000,
                          implicit=self.page.timeouts['implicit'] / 1000 if self.timeout is None else self.timeout)
        self._page_load_strategy = self.page.page_load_strategy


class Timeout(object):
    """用于保存d模式timeout信息的类"""

    def __init__(self, page):
        self.page = page
        self.page_load = 30
        self.script = 30

    @property
    def implicit(self):
        return self.page.timeout
