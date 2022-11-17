# -*- coding:utf-8 -*-
from typing import Union, Tuple, List

from DownloadKit import DownloadKit
from pychrome import Tab
from requests import Session, Response
from tldextract import extract

from .chromium_element import ChromiumElement
from .session_element import SessionElement
from .base import BasePage
from .config import DriverOptions, SessionOptions, _cookies_to_tuple
from .chromium_page import ChromiumPage
from .session_page import SessionPage


class WebPage(SessionPage, ChromiumPage, BasePage):
    """整合浏览器和request的页面类"""

    def __init__(self,
                 mode: str = 'd',
                 timeout: float = 10,
                 tab_id: str = None,
                 driver_or_options: Union[Tab, DriverOptions, bool] = None,
                 session_or_options: Union[Session, SessionOptions, bool] = None) -> None:
        """初始化函数                                                                        \n
        :param mode: 'd' 或 's'，即driver模式和session模式
        :param timeout: 超时时间，d模式时为寻找元素时间，s模式时为连接时间，默认10秒
        :param driver_or_options: Tab对象或DriverOptions对象，只使用s模式时应传入False
        :param session_or_options: Session对象或SessionOptions对象，只使用d模式时应传入False
        """
        self._mode = mode.lower()
        if self._mode not in ('s', 'd'):
            raise ValueError('mode参数只能是s或d。')

        super(ChromiumPage, self).__init__(timeout)  # 调用Base的__init__()
        self._session = None
        self._tab_obj = None
        self._is_loading = False
        self._set_session_options(session_or_options)
        self._set_driver_options(driver_or_options)
        self._setting_tab_id = tab_id
        self._has_driver, self._has_session = (None, True) if self._mode == 's' else (True, None)
        self._response = None

        if self._mode == 'd':
            self._driver

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str, ChromiumElement, SessionElement],
                 timeout: float = None) -> Union[ChromiumElement, SessionElement, None]:
        """在内部查找元素                                            \n
        例：ele = page('@id=ele_id')                               \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 超时时间
        :return: 子元素对象
        """
        if self._mode == 's':
            return super().__call__(loc_or_str)
        elif self._mode == 'd':
            return super(SessionPage, self).__call__(loc_or_str, timeout)

    # -----------------共有属性和方法-------------------
    @property
    def url(self) -> Union[str, None]:
        """返回当前url"""
        if self._mode == 'd':
            return super(SessionPage, self).url if self._has_driver else None
        elif self._mode == 's':
            return self._session_url

    @property
    def html(self) -> str:
        """返回页面html文本"""
        if self._mode == 's':
            return super().html
        elif self._mode == 'd':
            return super(SessionPage, self).html if self._has_driver else ''

    @property
    def json(self) -> dict:
        """当返回内容是json格式时，返回对应的字典"""
        if self._mode == 's':
            return super().json
        elif self._mode == 'd':
            return super(SessionPage, self).json

    @property
    def response(self) -> Response:
        """返回 s 模式获取到的 Response 对象，切换到 s 模式"""
        self.change_mode('s')
        return self._response

    @property
    def mode(self) -> str:
        """返回当前模式，'s'或'd' """
        return self._mode

    @property
    def cookies(self):
        if self._mode == 's':
            return super().get_cookies()
        elif self._mode == 'd':
            return super(SessionPage, self).get_cookies()

    @property
    def session(self) -> Session:
        """返回Session对象，如未初始化则按配置信息创建"""
        if self._session is None:
            self._set_session(self._session_options)

            # if self._proxy:
            #     self._session.proxies = self._proxy

        return self._session

    @property
    def _driver(self) -> Tab:
        """返回纯粹的Tab对象，调用时切换到d模式，并连接浏览器"""
        self.change_mode('d')
        if self._tab_obj is None:
            self._connect_browser(self._driver_options, self._setting_tab_id)
        return self._tab_obj

    @_driver.setter
    def _driver(self, tab):
        self._tab_obj = tab

    @property
    def _session_url(self) -> str:
        """返回 session 保存的url"""
        return self._response.url if self._response else None

    def get(self,
            url: str,
            show_errmsg: bool = False,
            retry: int = None,
            interval: float = None,
            timeout: float = None,
            **kwargs) -> Union[bool, None]:
        """跳转到一个url                                         \n
        :param url: 目标url
        :param show_errmsg: 是否显示和抛出异常
        :param retry: 重试次数
        :param interval: 重试间隔（秒）
        :param timeout: 连接超时时间（秒）
        :param kwargs: 连接参数，s模式专用
        :return: url是否可用，d模式返回None时表示不确定
        """
        if self._mode == 'd':
            return super(SessionPage, self).get(url, show_errmsg, retry, interval, timeout)
        elif self._mode == 's':
            return super().get(url, show_errmsg, retry, interval, timeout, **kwargs)

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, ChromiumElement, SessionElement],
            timeout: float = None) -> Union[ChromiumElement, SessionElement, str, None]:
        """返回第一个符合条件的元素、属性或节点文本                               \n
        :param loc_or_ele: 元素的定位信息，可以是元素对象，loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与页面等待时间一致
        :return: 元素对象或属性、文本节点文本
        """
        if self._mode == 's':
            return super().ele(loc_or_ele)
        elif self._mode == 'd':
            return super(SessionPage, self).ele(loc_or_ele, timeout=timeout)

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None) -> List[Union[ChromiumElement, SessionElement, str]]:
        """返回页面中所有符合条件的元素、属性或节点文本                                \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与页面等待时间一致
        :return: 元素对象或属性、文本组成的列表
        """
        if self._mode == 's':
            return super().eles(loc_or_str)
        elif self._mode == 'd':
            return super(SessionPage, self).eles(loc_or_str, timeout=timeout)

    def s_ele(self, loc_or_ele: Union[Tuple[str, str], str, ChromiumElement, SessionElement] = None) \
            -> Union[SessionElement, str, None]:
        """查找第一个符合条件的元素以SessionElement形式返回，d模式处理复杂页面时效率很高                 \n
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        if self._mode == 's':
            return super().s_ele(loc_or_ele)
        elif self._mode == 'd':
            return super(SessionPage, self).s_ele(loc_or_ele)

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str] = None) -> List[Union[SessionElement, str]]:
        """查找所有符合条件的元素以SessionElement形式返回，d模式处理复杂页面时效率很高                 \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象或属性、文本组成的列表
        """
        if self._mode == 's':
            return super().s_eles(loc_or_str)
        elif self._mode == 'd':
            return super(SessionPage, self).s_eles(loc_or_str)

    def change_mode(self, mode: str = None, go: bool = True) -> None:
        """切换模式，接收's'或'd'，除此以外的字符串会切换为 d 模式     \n
        切换时会把当前模式的cookies复制到目标模式                   \n
        切换后，如果go是True，调用相应的get函数使访问的页面同步        \n
        注意：s转d时，若浏览器当前网址域名和s模式不一样，必须会跳转      \n
        :param mode: 模式字符串
        :param go: 是否跳转到原模式的url
        """
        if mode is not None and mode.lower() == self._mode:
            return

        self._mode = 's' if self._mode == 'd' else 'd'

        # s模式转d模式
        if self._mode == 'd':
            if not self._has_driver:
                d = self.driver
            self._url = None if not self._has_driver else super(SessionPage, self).url
            self._has_driver = True

            if self._session_url:
                self.cookies_to_driver()

                if go:
                    self.get(self._session_url)

        # d模式转s模式
        elif self._mode == 's':
            self._has_session = True
            self._url = self._session_url

            if self._has_driver:
                self.cookies_to_session()

                if go:
                    url = super(SessionPage, self).url
                    if url.startswith('http'):
                        self.get(url)

    def cookies_to_session(self, copy_user_agent: bool = False) -> None:
        """把driver对象的cookies复制到session对象    \n
        :param copy_user_agent: 是否复制ua信息
        :return: None
        """
        if copy_user_agent:
            selenium_user_agent = self.run_script("navigator.userAgent;")
            self.session.headers.update({"User-Agent": selenium_user_agent})

        self.set_cookies(self._get_driver_cookies(as_dict=True), set_session=True)

    def cookies_to_driver(self) -> None:
        """把session对象的cookies复制到driver对象"""
        ex_url = extract(self._session_url)
        domain = f'{ex_url.domain}.{ex_url.suffix}'
        cookies = []
        for cookie in super().get_cookies():
            if cookie.get('domain', '') == '':
                cookie['domain'] = domain

            if domain in cookie['domain']:
                cookies.append(cookie)
        self.set_cookies(cookies, set_driver=True)

    def get_cookies(self, as_dict: bool = False, all_domains: bool = False) -> Union[dict, list]:
        """返回cookies                               \n
        :param as_dict: 是否以字典方式返回
        :param all_domains: 是否返回所有域的cookies
        :return: cookies信息
        """
        if self._mode == 's':
            return super().get_cookies(as_dict, all_domains)
        elif self._mode == 'd':
            return self._get_driver_cookies(as_dict)

    def _get_driver_cookies(self, as_dict: bool = False):
        cookies = super(SessionPage, self)._wait_driver.Network.getCookies()['cookies']
        if as_dict:
            return {cookie['name']: cookie['value'] for cookie in cookies}
        else:
            return cookies

    def set_cookies(self, cookies, set_session: bool = False, set_driver: bool = False):
        # 添加cookie到driver
        if set_driver:
            cookies = _cookies_to_tuple(cookies)
            result_cookies = []
            for cookie in cookies:
                if not cookie.get('domain', None):
                    continue
                c = {'value': '' if cookie['value'] is None else cookie['value'],
                     'name': cookie['name'],
                     'domain': cookie['domain']}
                result_cookies.append(c)
            super(SessionPage, self)._wait_driver.Network.setCookies(cookies=result_cookies)

        # 添加cookie到session
        if set_session:
            super().set_cookies(cookies)

    def check_page(self, by_requests: bool = False) -> Union[bool, None]:
        """d模式时检查网页是否符合预期                \n
        默认由response状态检查，可重载实现针对性检查   \n
        :param by_requests: 是否用内置response检查
        :return: bool或None，None代表不知道结果
        """
        if self._session_url and self._session_url == self.url:
            return self._response.ok

        # 使用requests访问url并判断可用性
        if by_requests:
            self.cookies_to_session()
            r = self._make_response(self.url, retry=0)[0]
            return r.ok if r else False

    def close_driver(self) -> None:
        """关闭driver及浏览器"""
        if self._has_driver:
            self.change_mode('s')
            try:
                self.driver.Browser.close()
            except Exception:
                pass
            self._has_driver = None

    def close_session(self) -> None:
        """关闭session"""
        if self._has_session:
            self.change_mode('d')
            self._session = None
            self._response = None
            self._has_session = None

    # ----------------重写SessionPage的函数-----------------------
    def post(self,
             url: str,
             data: Union[dict, str] = None,
             show_errmsg: bool = False,
             retry: int = None,
             interval: float = None,
             **kwargs) -> bool:
        """用post方式跳转到url，会切换到s模式                        \n
        :param url: 目标url
        :param data: post方式时提交的数据
        :param show_errmsg: 是否显示和抛出异常
        :param retry: 重试次数
        :param interval: 重试间隔（秒）
        :param kwargs: 连接参数
        :return: url是否可用
        """
        self.change_mode('s', go=False)
        return super().post(url, data, show_errmsg, retry, interval, **kwargs)

    @property
    def download(self) -> DownloadKit:
        if self.mode == 'd':
            self.cookies_to_session()
        return super().download

    def _ele(self,
             loc_or_ele: Union[Tuple[str, str], str, ChromiumElement, SessionElement],
             timeout: float = None, single: bool = True) \
            -> Union[ChromiumElement, SessionElement, str, None, List[Union[SessionElement, str]], List[
                Union[ChromiumElement, str]]]:
        """返回页面中符合条件的元素、属性或节点文本，默认返回第一个                                               \n
        :param loc_or_ele: 元素的定位信息，可以是元素对象，loc元组，或查询字符串
        :param timeout: 查找元素超时时间，d模式专用
        :param single: True则返回第一个，False则返回全部
        :return: 元素对象或属性、文本节点文本
        """
        if self._mode == 's':
            return super()._ele(loc_or_ele, single=single)
        elif self._mode == 'd':
            return super(SessionPage, self)._ele(loc_or_ele, timeout=timeout, single=single)

    def _set_driver_options(self, Tab_or_Options):
        """处理driver设置"""
        if Tab_or_Options is None:
            self._driver_options = DriverOptions()

        elif Tab_or_Options is False:
            self._driver_options = DriverOptions(read_file=False)

        elif isinstance(Tab_or_Options, Tab):
            self._connect_browser(Tab_or_Options)
            self._has_driver = True

        elif isinstance(Tab_or_Options, DriverOptions):
            self._driver_options = Tab_or_Options

        else:
            raise TypeError('driver_or_options参数只能接收WebDriver, Options, DriverOptions或False。')

    def _set_session_options(self, Session_or_Options):
        """处理session设置"""
        if Session_or_Options is None:
            self._session_options = SessionOptions().as_dict()

        elif Session_or_Options is False:
            self._session_options = SessionOptions(read_file=False).as_dict()

        elif isinstance(Session_or_Options, Session):
            self._session = Session_or_Options
            self._has_session = True

        elif isinstance(Session_or_Options, SessionOptions):
            self._session_options = Session_or_Options.as_dict()

        elif isinstance(Session_or_Options, dict):
            self._session_options = Session_or_Options

        else:
            raise TypeError('session_or_options参数只能接收Session, dict, SessionOptions或False。')
