# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from json import loads, JSONDecodeError
from os.path import sep
from pathlib import Path
from re import findall
from threading import Thread
from time import perf_counter, sleep

from requests import get

from .._base.base import BasePage
from .._base.chromium_driver import ChromiumDriver
from .._commons.constants import ERROR, NoneElement
from .._commons.locator import get_loc
from .._commons.tools import get_usable_path
from .._commons.web import location_in_viewport
from .._elements.chromium_element import ChromiumScroll, ChromiumElement, run_js, make_chromium_ele
from .._elements.session_element import make_session_ele
from .._units.action_chains import ActionChains
from .._units.network_listener import NetworkListener
from .._units.screencast import Screencast
from .._units.setter import ChromiumBaseSetter
from .._units.waiter import ChromiumBaseWaiter
from ..errors import (ContextLossError, ElementLossError, CDPError, TabClosedError, NoRectError, BrowserConnectError,
                      AlertExistsError, GetDocumentError)


class ChromiumBase(BasePage):
    """标签页、frame、页面基类"""

    def __init__(self, address, tab_id=None, timeout=None):
        """
        :param address: 浏览器 ip:port
        :param tab_id: 要控制的标签页id，不指定默认为激活的
        :param timeout: 超时时间
        """
        super().__init__()
        self._is_loading = None
        self._root_id = None  # object id
        self._debug = False
        self._set = None
        self._screencast = None
        self._actions = None
        self._listener = None
        self._has_alert = False
        self._ready_state = None

        self._download_path = str(Path('.').absolute())

        if isinstance(address, int) or (isinstance(address, str) and address.isdigit()):
            address = f'127.0.0.1:{address}'

        self._d_set_start_options(address, None)
        self._d_set_runtime_settings()
        self._connect_browser(tab_id)
        if timeout is not None:
            self.timeout = timeout

    def _d_set_start_options(self, address, none):
        """设置浏览器启动属性
        :param address: 'ip:port'
        :param none: 用于后代继承
        :return: None
        """
        self.address = address.replace('localhost', '127.0.0.1').lstrip('http://').lstrip('https://')

    def _d_set_runtime_settings(self):
        self._timeouts = Timeout(self)
        self._page_load_strategy = 'normal'

    def _connect_browser(self, tab_id=None):
        """连接浏览器，在第一次时运行
        :param tab_id: 要控制的标签页id，不指定默认为激活的
        :return: None
        """
        self._is_reading = False
        self._upload_list = None
        self._wait = None
        self._scroll = None

        if not tab_id:
            json = get(f'http://{self.address}/json', headers={'Connection': 'close'}).json()

            tab_id = [i['id'] for i in json if i['type'] == 'page']
            if not tab_id:
                raise BrowserConnectError('浏览器连接失败，可能是浏览器版本原因。')
            tab_id = tab_id[0]

        self._driver_init(tab_id)
        if self.ready_state == 'complete' and self._ready_state is None:
            self._get_document()
            self._ready_state = 'complete'

        r = self.run_cdp('Page.getFrameTree')
        for i in findall(r"'id': '(.*?)'", str(r)):
            self.browser._frames[i] = self.tab_id

    def _driver_init(self, tab_id):
        """新建页面、页面刷新、切换标签页后要进行的cdp参数初始化
        :param tab_id: 要跳转到的标签页id
        :return: None
        """
        self._is_loading = True
        self._frame_id = tab_id
        self._driver = ChromiumDriver(tab_id=tab_id, tab_type='page', address=self.address)
        self._alert = Alert()
        self._driver.set_listener('Page.javascriptDialogOpening', self._on_alert_open)
        self._driver.set_listener('Page.javascriptDialogClosed', self._on_alert_close)

        self._driver.call_method('DOM.enable')
        self._driver.call_method('Page.enable')
        self._driver.call_method('Emulation.setFocusEmulationEnabled', enabled=True)

        self._driver.set_listener('Page.frameStartedLoading', self._onFrameStartedLoading)
        self._driver.set_listener('Page.frameNavigated', self._onFrameNavigated)
        self._driver.set_listener('Page.domContentEventFired', self._onDomContentEventFired)
        self._driver.set_listener('Page.loadEventFired', self._onLoadEventFired)
        self._driver.set_listener('Page.frameStoppedLoading', self._onFrameStoppedLoading)
        self._driver.set_listener('Page.frameAttached', self._onFrameAttached)
        self._driver.set_listener('Page.frameDetached', self._onFrameDetached)

    def _get_document(self):
        if self._is_reading:
            return
        self._is_reading = True
        end_time = perf_counter() + 10
        while perf_counter() < end_time:
            try:
                b_id = self.run_cdp('DOM.getDocument')['root']['backendNodeId']
                self._root_id = self.run_cdp('DOM.resolveNode', backendNodeId=b_id)['object']['objectId']
                break
            except:
                continue
        else:
            raise GetDocumentError

        r = self.run_cdp('Page.getFrameTree')
        for i in findall(r"'id': '(.*?)'", str(r)):
            self.browser._frames[i] = self.tab_id

        self._is_loading = False
        self._is_reading = False

    def _wait_loaded(self, timeout=None):
        """等待页面加载完成，超时触发停止加载
        :param timeout: 超时时间
        :return: 是否成功，超时返回False
        """
        if self.page_load_strategy == 'none':
            return True

        timeout = timeout if timeout is not None else self.timeouts.page_load
        end_time = perf_counter() + timeout
        while perf_counter() < end_time:
            if self._ready_state == 'complete':
                return True
            elif self.page_load_strategy == 'eager' and self._ready_state in ('interactive', 'complete'):
                self.stop_loading()
                return True

            sleep(.1)

        self.stop_loading()
        return False

    def _onFrameDetached(self, **kwargs):
        try:
            self.browser._frames.pop(kwargs['frameId'])
        except KeyError:
            pass

    def _onFrameAttached(self, **kwargs):
        self.browser._frames[kwargs['frameId']] = self.tab_id

    def _onFrameStartedLoading(self, **kwargs):
        """页面开始加载时执行"""
        self.browser._frames[kwargs['frameId']] = self.tab_id
        if kwargs['frameId'] == self._frame_id:
            self._ready_state = 'loading'
            self._is_loading = True
            if self.page_load_strategy == 'eager':
                t = Thread(target=self._wait_to_stop)
                t.daemon = True
                t.start()
            if self._debug:
                print(f'frameStartedLoading {kwargs}')

    def _onFrameNavigated(self, **kwargs):
        """页面跳转时执行"""
        if kwargs['frame']['id'] == self._frame_id:
            self._ready_state = 'loading'
            self._is_loading = True
            if self._debug:
                print(f'FrameNavigated {kwargs}')

    def _onDomContentEventFired(self, **kwargs):
        """在页面刷新、变化后重新读取页面内容"""
        self._ready_state = 'interactive'
        if self.page_load_strategy == 'eager':
            self.run_cdp('Page.stopLoading')
        if self._debug:
            print(f'DomContentEventFired {kwargs}')

    def _onLoadEventFired(self, **kwargs):
        """在页面刷新、变化后重新读取页面内容"""
        self._ready_state = 'complete'
        if self._debug:
            print(f'LoadEventFired {kwargs}')
        # self._get_document()

    def _onFrameStoppedLoading(self, **kwargs):
        """页面加载完成后执行"""
        self.browser._frames[kwargs['frameId']] = self.tab_id
        if kwargs['frameId'] == self._frame_id:
            self._ready_state = 'complete'
            if self._debug:
                print(f'FrameStoppedLoading {kwargs}')
            self._get_document()

    def _onFileChooserOpened(self, **kwargs):
        """文件选择框打开时执行"""
        if self._upload_list:
            if 'backendNodeId' not in kwargs:
                raise TypeError('该输入框无法接管，请改用对<input>元素输入路径的方法设置。')
            files = self._upload_list if kwargs['mode'] == 'selectMultiple' else self._upload_list[:1]
            self.run_cdp('DOM.setFileInputFiles', files=files, backendNodeId=kwargs['backendNodeId'])

            self.driver.set_listener('Page.fileChooserOpened', None)
            self.run_cdp('Page.setInterceptFileChooserDialog', enabled=False)
            self._upload_list = None

    def __call__(self, loc_or_str, timeout=None):
        """在内部查找元素
        例：ele = page('@id=ele_id')
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 超时时间
        :return: ChromiumElement对象
        """
        return self.ele(loc_or_str, timeout)

    def _wait_to_stop(self):
        """eager策略超时时使页面停止加载"""
        end_time = perf_counter() + self.timeouts.page_load
        while perf_counter() < end_time:
            sleep(.1)
        if self._ready_state in ('interactive', 'complete') and self._is_loading:
            self.stop_loading()

    @property
    def main(self):
        return self._page

    @property
    def browser(self):
        return self._browser

    @property
    def driver(self):
        """返回用于控制浏览器的ChromiumDriver对象"""
        if self._driver is None:
            raise RuntimeError('浏览器已关闭或链接已断开。')
        return self._driver

    @property
    def is_loading(self):
        """返回页面是否正在加载状态"""
        return self._is_loading

    @property
    def is_alive(self):
        """返回页面对象是否仍然可用"""
        try:
            self.run_cdp('Page.getLayoutMetrics')
            return True
        except TabClosedError:
            return False

    @property
    def title(self):
        """返回当前页面title"""
        return self.run_cdp_loaded('Target.getTargetInfo', targetId=self._target_id)['targetInfo']['title']

    @property
    def url(self):
        """返回当前页面url"""
        return self.run_cdp_loaded('Target.getTargetInfo', targetId=self._target_id)['targetInfo']['url']

    @property
    def _browser_url(self):
        """用于被WebPage覆盖"""
        return self.url

    @property
    def html(self):
        """返回当前页面html文本"""
        self.wait.load_complete()
        return self.run_cdp('DOM.getOuterHTML', objectId=self._root_id)['outerHTML']

    @property
    def json(self):
        """当返回内容是json格式时，返回对应的字典，非json格式时返回None"""
        try:
            return loads(self('t:pre', timeout=.5).text)
        except JSONDecodeError:
            return None

    @property
    def tab_id(self):
        """返回当前标签页id"""
        return self._target_id

    @property
    def _target_id(self):
        """返回当前标签页id"""
        return self.driver.id if not self.driver._stopped.is_set() else ''

    @property
    def ready_state(self):
        """返回当前页面加载状态，'loading' 'interactive' 'complete'，'timeout' 表示可能有弹出框"""
        try:
            return self.run_cdp('Runtime.evaluate', expression='document.readyState;', _timeout=3)['result']['value']
        except ContextLossError:
            return None
        except TimeoutError:
            return 'timeout'

    @property
    def size(self):
        """返回页面总宽高，格式：(宽, 高)"""
        r = self.run_cdp_loaded('Page.getLayoutMetrics')['contentSize']
        return r['width'], r['height']

    @property
    def active_ele(self):
        """返回当前焦点所在元素"""
        return self.run_js_loaded('return document.activeElement;')

    @property
    def page_load_strategy(self):
        """返回页面加载策略，有3种：'none'、'normal'、'eager'"""
        return self._page_load_strategy

    @property
    def user_agent(self):
        """返回user agent"""
        return self.run_cdp('Runtime.evaluate', expression='navigator.userAgent;')['result']['value']

    @property
    def scroll(self):
        """返回用于滚动滚动条的对象"""
        self.wait.load_complete()
        if self._scroll is None:
            self._scroll = ChromiumPageScroll(self)
        return self._scroll

    @property
    def timeouts(self):
        """返回timeouts设置"""
        return self._timeouts

    @property
    def upload_list(self):
        """返回等待上传文件列表"""
        return self._upload_list

    @property
    def wait(self):
        """返回用于等待的对象"""
        if self._wait is None:
            self._wait = ChromiumBaseWaiter(self)
        return self._wait

    @property
    def set(self):
        """返回用于等待的对象"""
        if self._set is None:
            self._set = ChromiumBaseSetter(self)
        return self._set

    @property
    def screencast(self):
        """返回用于录屏的对象"""
        if self._screencast is None:
            self._screencast = Screencast(self)
        return self._screencast

    @property
    def actions(self):
        """返回用于执行动作链的对象"""
        if self._actions is None:
            self._actions = ActionChains(self)
        self.wait.load_complete()
        return self._actions

    @property
    def listen(self):
        """返回用于聆听数据包的对象"""
        if self._listener is None:
            self._listener = NetworkListener(self)
        return self._listener

    @property
    def has_alert(self):
        """返回是否存在提示框"""
        return self._has_alert

    def run_cdp(self, cmd, **cmd_args):
        """执行Chrome DevTools Protocol语句
        :param cmd: 协议项目
        :param cmd_args: 参数
        :return: 执行的结果
        """
        r = self.driver.call_method(cmd, **cmd_args)
        if ERROR not in r:
            return r

        error = r[ERROR]
        if error in ('Cannot find context with specified id', 'Inspected target navigated or closed'):
            raise ContextLossError
        elif error in ('Could not find node with given id', 'Could not find object with given id',
                       'No node with given id found', 'Node with given id does not belong to the document',
                       'No node found for given backend id'):
            raise ElementLossError
        elif error == 'tab closed':
            raise TabClosedError
        elif error == 'timeout':
            raise TimeoutError
        elif error == 'alert exists.':
            raise AlertExistsError
        elif error in ('Node does not have a layout object', 'Could not compute box model.'):
            raise NoRectError
        elif r['type'] == 'call_method_error':
            raise CDPError(f'\n错误：{r["error"]}\nmethod：{r["method"]}\nargs：{r["args"]}')
        else:
            raise RuntimeError(r)

    def run_cdp_loaded(self, cmd, **cmd_args):
        """执行Chrome DevTools Protocol语句，执行前等待页面加载完毕
        :param cmd: 协议项目
        :param cmd_args: 参数
        :return: 执行的结果
        """
        self.wait.load_complete()
        return self.run_cdp(cmd, **cmd_args)

    def run_js(self, script, *args, as_expr=False):
        """运行javascript代码
        :param script: js文本
        :param args: 参数，按顺序在js文本中对应arguments[0]、arguments[1]...
        :param as_expr: 是否作为表达式运行，为True时args无效
        :return: 运行的结果
        """
        return run_js(self, script, as_expr, self.timeouts.script, args)

    def run_js_loaded(self, script, *args, as_expr=False):
        """运行javascript代码，执行前等待页面加载完毕
        :param script: js文本
        :param args: 参数，按顺序在js文本中对应arguments[0]、arguments[1]...
        :param as_expr: 是否作为表达式运行，为True时args无效
        :return: 运行的结果
        """
        self.wait.load_complete()
        return run_js(self, script, as_expr, self.timeouts.script, args)

    def run_async_js(self, script, *args, as_expr=False):
        """以异步方式执行js代码
        :param script: js文本
        :param args: 参数，按顺序在js文本中对应arguments[0]、arguments[1]...
        :param as_expr: 是否作为表达式运行，为True时args无效
        :return: None
        """
        from threading import Thread
        Thread(target=run_js, args=(self, script, as_expr, self.timeouts.script, args)).start()

    def get(self, url, show_errmsg=False, retry=None, interval=None, timeout=None):
        """访问url
        :param url: 目标url
        :param show_errmsg: 是否显示和抛出异常
        :param retry: 重试次数
        :param interval: 重试间隔（秒）
        :param timeout: 连接超时时间
        :return: 目标url是否可用
        """
        retry, interval = self._before_connect(url, retry, interval)
        self._url_available = self._d_connect(self._url,
                                              times=retry,
                                              interval=interval,
                                              show_errmsg=show_errmsg,
                                              timeout=timeout)
        return self._url_available

    def get_cookies(self, as_dict=False, all_domains=False, all_info=False):
        """获取cookies信息
        :param as_dict: 为True时返回由{name: value}键值对组成的dict，为True时返回list且all_info无效
        :param all_domains: 是否返回所有域的cookies
        :param all_info: 是否返回所有信息，为False时只返回name、value、domain
        :return: cookies信息
        """
        txt = 'Storage' if all_domains else 'Network'
        cookies = self.run_cdp_loaded(f'{txt}.getCookies')['cookies']

        if as_dict:
            return {cookie['name']: cookie['value'] for cookie in cookies}
        elif all_info:
            return cookies
        else:
            return [{'name': cookie['name'], 'value': cookie['value'], 'domain': cookie['domain']}
                    for cookie in cookies]

    def ele(self, loc_or_ele, timeout=None):
        """获取第一个符合条件的元素对象
        :param loc_or_ele: 定位符或元素对象
        :param timeout: 查找超时时间
        :return: ChromiumElement对象
        """
        return self._ele(loc_or_ele, timeout=timeout)

    def eles(self, loc_or_str, timeout=None):
        """获取所有符合条件的元素对象
        :param loc_or_str: 定位符或元素对象
        :param timeout: 查找超时时间
        :return: ChromiumElement对象组成的列表
        """
        return self._ele(loc_or_str, timeout=timeout, single=False)

    def s_ele(self, loc_or_ele=None):
        """查找第一个符合条件的元素以SessionElement形式返回，处理复杂页面时效率很高
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        return make_session_ele(self, loc_or_ele)

    def s_eles(self, loc_or_str):
        """查找所有符合条件的元素以SessionElement列表形式返回
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象组成的列表
        """
        return make_session_ele(self, loc_or_str, single=False)

    def _find_elements(self, loc_or_ele, timeout=None, single=True, relative=False, raise_err=None):
        """执行元素查找
        :param loc_or_ele: 定位符或元素对象
        :param timeout: 查找超时时间
        :param single: 是否只返回第一个
        :param relative: WebPage用的表示是否相对定位的参数
        :param raise_err: 找不到元素是是否抛出异常，为None时根据全局设置
        :return: ChromiumElement对象或元素对象组成的列表
        """
        if isinstance(loc_or_ele, (str, tuple)):
            loc = get_loc(loc_or_ele)[1]
        elif isinstance(loc_or_ele, ChromiumElement) or str(type(loc_or_ele)).endswith(".ChromiumFrame'>"):
            return loc_or_ele
        else:
            raise ValueError('loc_or_str参数只能是tuple、str、ChromiumElement类型。')

        ok = False
        nodeIds = None

        timeout = timeout if timeout is not None else self.timeout
        end_time = perf_counter() + timeout

        try:
            search_result = self.run_cdp_loaded('DOM.performSearch', query=loc, includeUserAgentShadowDOM=True)
            count = search_result['resultCount']
        except ContextLossError:
            search_result = None
            count = 0

        while True:
            if count > 0:
                count = 1 if single else count
                try:
                    nodeIds = self.run_cdp_loaded('DOM.getSearchResults', searchId=search_result['searchId'],
                                                  fromIndex=0, toIndex=count)
                    if nodeIds['nodeIds'][0] != 0:
                        ok = True

                except Exception:
                    pass

            if ok:
                try:
                    if single:
                        r = make_chromium_ele(self, node_id=nodeIds['nodeIds'][0])
                        break

                    else:
                        r = [make_chromium_ele(self, node_id=i) for i in nodeIds['nodeIds']]
                        break

                except ElementLossError:
                    ok = False

            try:
                search_result = self.run_cdp_loaded('DOM.performSearch', query=loc, includeUserAgentShadowDOM=True)
                count = search_result['resultCount']
            except ContextLossError:
                pass

            if perf_counter() >= end_time:
                return NoneElement() if single else []

            sleep(.1)

        try:
            self.run_cdp('DOM.discardSearchResults', searchId=search_result['searchId'])
        except:
            pass
        return r

    def refresh(self, ignore_cache=False):
        """刷新当前页面
        :param ignore_cache: 是否忽略缓存
        :return: None
        """
        self._is_loading = True
        self.run_cdp('Page.reload', ignoreCache=ignore_cache)
        self.wait.load_start()

    def forward(self, steps=1):
        """在浏览历史中前进若干步
        :param steps: 前进步数
        :return: None
        """
        self._forward_or_back(steps)

    def back(self, steps=1):
        """在浏览历史中后退若干步
        :param steps: 后退步数
        :return: None
        """
        self._forward_or_back(-steps)

    def _forward_or_back(self, steps):
        """执行浏览器前进或后退，会跳过url相同的历史记录
        :param steps: 步数
        :return: None
        """
        if steps == 0:
            return

        history = self.run_cdp('Page.getNavigationHistory')
        index = history['currentIndex']
        history = history['entries']
        direction = 1 if steps > 0 else -1
        curr_url = history[index]['url']
        nid = None
        for num in range(abs(steps)):
            for i in history[index::direction]:
                index += direction
                if i['url'] != curr_url:
                    nid = i['id']
                    curr_url = i['url']
                    break

        if nid:
            self._is_loading = True
            self.run_cdp('Page.navigateToHistoryEntry', entryId=nid)

    def stop_loading(self):
        """页面停止加载"""
        if self._debug:
            print('停止页面加载')
        try:
            self.run_cdp('Page.stopLoading')
        except TabClosedError:
            pass
        end_time = perf_counter() + self.timeouts.page_load
        while self._ready_state != 'complete' and perf_counter() < end_time:
            sleep(.1)

    def remove_ele(self, loc_or_ele):
        """从页面上删除一个元素
        :param loc_or_ele: 元素对象或定位符
        :return: None
        """
        if not loc_or_ele:
            return
        ele = self._ele(loc_or_ele, raise_err=False)
        if ele:
            self.run_cdp('DOM.removeNode', nodeId=ele.ids.node_id)

    def get_frame(self, loc_ind_ele, timeout=None):
        """获取页面中一个frame对象，可传入定位符、iframe序号、ChromiumFrame对象，序号从1开始
        :param loc_ind_ele: 定位符、iframe序号、ChromiumFrame对象
        :param timeout: 查找元素超时时间
        :return: ChromiumFrame对象
        """
        if isinstance(loc_ind_ele, str):
            if not loc_ind_ele.startswith(('.', '#', '@', 't:', 't=', 'tag:', 'tag=', 'tx:', 'tx=', 'tx^', 'tx$',
                                           'text:', 'text=', 'text^', 'text$', 'xpath:', 'xpath=', 'x:', 'x=', 'css:',
                                           'css=', 'c:', 'c=')):
                loc_ind_ele = f'xpath://*[(name()="iframe" or name()="frame") and ' \
                              f'(@name="{loc_ind_ele}" or @id="{loc_ind_ele}")]'
            ele = self._ele(loc_ind_ele, timeout=timeout)
            if ele and not str(type(ele)).endswith(".ChromiumFrame'>"):
                raise TypeError('该定位符不是指向frame元素。')
            return ele

        elif isinstance(loc_ind_ele, tuple):
            ele = self._ele(loc_ind_ele, timeout=timeout)
            if ele and not str(type(ele)).endswith(".ChromiumFrame'>"):
                raise TypeError('该定位符不是指向frame元素。')
            return ele

        elif isinstance(loc_ind_ele, int):
            if loc_ind_ele < 1:
                raise ValueError('序号必须大于0。')
            xpath = f'xpath:(//*[name()="frame" or name()="iframe"])[{loc_ind_ele}]'
            return self._ele(xpath, timeout=timeout)

        elif str(type(loc_ind_ele)).endswith(".ChromiumFrame'>"):
            return loc_ind_ele

        else:
            raise TypeError('必须传入定位符、iframe序号、id、name、ChromiumFrame对象其中之一。')

    def get_frames(self, loc=None, timeout=None):
        """获取所有符合条件的frame对象
        :param loc: 定位符，为None时返回所有
        :param timeout: 查找超时时间
        :return: ChromiumFrame对象组成的列表
        """
        loc = loc or 'xpath://*[name()="iframe" or name()="frame"]'
        frames = self._ele(loc, timeout=timeout, single=False, raise_err=False)
        return [i for i in frames if str(type(i)).endswith(".ChromiumFrame'>")]

    def get_session_storage(self, item=None):
        """获取sessionStorage信息，不设置item则获取全部
        :param item: 要获取的项，不设置则返回全部
        :return: sessionStorage一个或所有项内容
        """
        if item:
            js = f'sessionStorage.getItem("{item}");'
            return self.run_js_loaded(js, as_expr=True)
        else:
            js = '''
            var dp_ls_len = sessionStorage.length;
            var dp_ls_arr = new Array();
            for(var i = 0; i < dp_ls_len; i++) {
                var getKey = sessionStorage.key(i);
                var getVal = sessionStorage.getItem(getKey);
                dp_ls_arr[i] = {'key': getKey, 'val': getVal}
            }
            return dp_ls_arr;
            '''
            return {i['key']: i['val'] for i in self.run_js_loaded(js)}

    def get_local_storage(self, item=None):
        """获取localStorage信息，不设置item则获取全部
        :param item: 要获取的项目，不设置则返回全部
        :return: localStorage一个或所有项内容
        """
        if item:
            js = f'localStorage.getItem("{item}");'
            return self.run_js_loaded(js, as_expr=True)
        else:
            js = '''
            var dp_ls_len = localStorage.length;
            var dp_ls_arr = new Array();
            for(var i = 0; i < dp_ls_len; i++) {
                var getKey = localStorage.key(i);
                var getVal = localStorage.getItem(getKey);
                dp_ls_arr[i] = {'key': getKey, 'val': getVal}
            }
            return dp_ls_arr;
            '''
            return {i['key']: i['val'] for i in self.run_js_loaded(js)}

    def get_screenshot(self, path=None, name=None, as_bytes=None, as_base64=None,
                       full_page=False, left_top=None, right_bottom=None):
        """对页面进行截图，可对整个网页、可见网页、指定范围截图。对可视范围外截图需要90以上版本浏览器支持
        :param path: 保存路径
        :param name: 完整文件名，后缀可选 'jpg','jpeg','png','webp'
        :param as_bytes: 是否以字节形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数和as_base64参数无效
        :param as_base64: 是否以base64字符串形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数无效
        :param full_page: 是否整页截图，为True截取整个网页，为False截取可视窗口
        :param left_top: 截取范围左上角坐标
        :param right_bottom: 截取范围右下角角坐标
        :return: 图片完整路径或字节文本
        """
        return self._get_screenshot(path=path, name=name, as_bytes=as_bytes, as_base64=as_base64,
                                    full_page=full_page, left_top=left_top, right_bottom=right_bottom)

    def clear_cache(self, session_storage=True, local_storage=True, cache=True, cookies=True):
        """清除缓存，可选要清除的项
        :param session_storage: 是否清除sessionStorage
        :param local_storage: 是否清除localStorage
        :param cache: 是否清除cache
        :param cookies: 是否清除cookies
        :return: None
        """
        if session_storage:
            self.run_js('sessionStorage.clear();', as_expr=True)
        if local_storage:
            self.run_js('localStorage.clear();', as_expr=True)
        if cache:
            self.run_cdp_loaded('Network.clearBrowserCache')
        if cookies:
            self.run_cdp_loaded('Network.clearBrowserCookies')

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

    def _on_alert_close(self, **kwargs):
        """alert关闭时触发的方法"""
        self._alert.activated = False
        self._alert.text = None
        self._alert.type = None
        self._alert.defaultPrompt = None
        self._alert.response_accept = kwargs.get('result')
        self._alert.response_text = kwargs['userInput']
        self._has_alert = False

    def _on_alert_open(self, **kwargs):
        """alert出现时触发的方法"""
        self._alert.activated = True
        self._alert.text = kwargs['message']
        self._alert.type = kwargs['message']
        self._alert.defaultPrompt = kwargs.get('defaultPrompt', None)
        self._alert.response_accept = None
        self._alert.response_text = None
        self._has_alert = True

    def _d_connect(self, to_url, times=0, interval=1, show_errmsg=False, timeout=None):
        """尝试连接，重试若干次
        :param to_url: 要访问的url
        :param times: 重试次数
        :param interval: 重试间隔（秒）
        :param show_errmsg: 是否抛出异常
        :param timeout: 连接超时时间
        :return: 是否成功，返回None表示不确定
        """
        err = None
        timeout = timeout if timeout is not None else self.timeouts.page_load
        for t in range(times + 1):
            err = None
            end_time = perf_counter() + timeout
            result = self.run_cdp('Page.navigate', url=to_url, _timeout=timeout)
            if result.get('error') == 'timeout':
                err = TimeoutError('页面连接超时。')

            elif 'errorText' in result:
                err = ConnectionError(result['errorText'])

            if err:
                sleep(interval)
                if self._debug or show_errmsg:
                    print(f'重试{t + 1} {to_url}')
                self.stop_loading()
                continue

            if self.page_load_strategy == 'none':
                return True

            yu = end_time - perf_counter()
            ok = self._wait_loaded(1 if yu <= 0 else yu)
            if not ok:
                err = TimeoutError('页面连接超时。')
                sleep(interval)
                if self._debug or show_errmsg:
                    print(f'重试{t + 1} {to_url}')
                self.stop_loading()
                continue

            if not err:
                break

        if err:
            if show_errmsg:
                raise err if err is not None else ConnectionError('连接异常。')
            return False

        return True

    def _get_screenshot(self, path=None, name=None, as_bytes=None, as_base64=None,
                        full_page=False, left_top=None, right_bottom=None, ele=None):
        """对页面进行截图，可对整个网页、可见网页、指定范围截图。对可视范围外截图需要90以上版本浏览器支持
        :param path: 保存路径
        :param name: 完整文件名，后缀可选 'jpg','jpeg','png','webp'
        :param as_bytes: 是否以字节形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数和as_base64参数无效
        :param as_base64: 是否以base64字符串形式返回图片，可选 'jpg','jpeg','png','webp'，生效时path参数无效
        :param full_page: 是否整页截图，为True截取整个网页，为False截取可视窗口
        :param left_top: 截取范围左上角坐标
        :param right_bottom: 截取范围右下角角坐标
        :param ele: 为异域iframe内元素截图设置
        :return: 图片完整路径或字节文本
        """
        if as_bytes:
            if as_bytes is True:
                pic_type = 'png'
            else:
                if as_bytes not in ('jpg', 'jpeg', 'png', 'webp'):
                    raise TypeError("只能接收 'jpg', 'jpeg', 'png', 'webp' 四种格式。")
                pic_type = 'jpeg' if as_bytes == 'jpg' else as_bytes

        elif as_base64:
            if as_base64 is True:
                pic_type = 'png'
            else:
                if as_base64 not in ('jpg', 'jpeg', 'png', 'webp'):
                    raise TypeError("只能接收 'jpg', 'jpeg', 'png', 'webp' 四种格式。")
                pic_type = 'jpeg' if as_base64 == 'jpg' else as_base64

        else:
            path = str(path).rstrip('\\/') if path else '.'
            if not path.endswith(('.jpg', '.jpeg', '.png', '.webp')):
                if not name:
                    name = f'{self.title}.jpg'
                elif not name.endswith(('.jpg', '.jpeg', '.png', '.webp')):
                    name = f'{name}.jpg'
                path = f'{path}{sep}{name}'

            path = get_usable_path(path)
            pic_type = path.suffix.lower()
            pic_type = 'jpeg' if pic_type == '.jpg' else pic_type[1:]

        width, height = self.size
        if full_page:
            vp = {'x': 0, 'y': 0, 'width': width, 'height': height, 'scale': 1}
            png = self.run_cdp_loaded('Page.captureScreenshot', format=pic_type,
                                      captureBeyondViewport=True, clip=vp)['data']
        else:
            if left_top and right_bottom:
                x, y = left_top
                w = right_bottom[0] - x
                h = right_bottom[1] - y

                v = not (location_in_viewport(self, x, y) and
                         location_in_viewport(self, right_bottom[0], right_bottom[1]))
                if v and (self.run_js('return document.body.scrollHeight > window.innerHeight;') and
                          not self.run_js('return document.body.scrollWidth > window.innerWidth;')):
                    x += 10

                vp = {'x': x, 'y': y, 'width': w, 'height': h, 'scale': 1}
                png = self.run_cdp_loaded('Page.captureScreenshot', format=pic_type,
                                          captureBeyondViewport=v, clip=vp)['data']

            else:
                png = self.run_cdp_loaded('Page.captureScreenshot', format=pic_type)['data']

        if as_base64:
            return png

        from base64 import b64decode
        png = b64decode(png)

        if as_bytes:
            return png

        path.parent.mkdir(parents=True, exist_ok=True)
        with open(path, 'wb') as f:
            f.write(png)
        return str(path.absolute())


class ChromiumPageScroll(ChromiumScroll):
    def __init__(self, page):
        """
        :param page: 页面对象
        """
        super().__init__(page)
        self.t1 = 'window'
        self.t2 = 'document.documentElement'

    def to_see(self, loc_or_ele, center=None):
        """滚动页面直到元素可见
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串
        :param center: 是否尽量滚动到页面正中，为None时如果被遮挡，则滚动到页面正中
        :return: None
        """
        ele = self._driver._ele(loc_or_ele)
        self._to_see(ele, center)

    def _to_see(self, ele, center):
        """执行滚动页面直到元素可见
        :param ele: 元素对象
        :param center: 是否尽量滚动到页面正中，为None时如果被遮挡，则滚动到页面正中
        :return: None
        """
        txt = 'true' if center else 'false'
        ele.run_js(f'this.scrollIntoViewIfNeeded({txt});')
        if center or (center is not False and ele.states.is_covered):
            ele.run_js('''function getWindowScrollTop() {var scroll_top = 0;
                    if (document.documentElement && document.documentElement.scrollTop) {
                      scroll_top = document.documentElement.scrollTop;
                    } else if (document.body) {scroll_top = document.body.scrollTop;}
                    return scroll_top;}
            const { top, height } = this.getBoundingClientRect();
                    const elCenter = top + height / 2;
                    const center = window.innerHeight / 2;
                    window.scrollTo({top: getWindowScrollTop() - (center - elCenter),
                    behavior: 'instant'});''')
        self._wait_scrolled()


class Timeout(object):
    """用于保存d模式timeout信息的类"""

    def __init__(self, page, implicit=None, page_load=None, script=None):
        """
        :param page: ChromiumBase页面
        :param implicit: 默认超时时间
        :param page_load: 页面加载超时时间
        :param script: js超时时间
        """
        self._page = page
        self.implicit = 10 if implicit is None else implicit
        self.page_load = 30 if page_load is None else page_load
        self.script = 30 if script is None else script

    def __repr__(self):
        return str({'implicit': self.implicit, 'page_load': self.page_load, 'script': self.script})


class Alert(object):
    """用于保存alert信息的类"""

    def __init__(self):
        self.activated = False
        self.text = None
        self.type = None
        self.defaultPrompt = None
        self.response_accept = None
        self.response_text = None
