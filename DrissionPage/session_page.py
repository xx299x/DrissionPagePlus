# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   session_page.py
"""
import re
from html import unescape
from typing import Union
from urllib import parse

from requests_html import Element, HTMLSession, HTMLResponse

from .config import global_session_options


def _translate_loc(loc):
    """把By类型转为xpath或css selector"""
    loc_by = loc_str = None
    if loc[0] == 'xpath':
        loc_by = 'xpath'
        loc_str = loc[1]
    elif loc[0] == 'css selector':
        loc_by = 'css selector'
        loc_str = loc[1]
    elif loc[0] == 'id':
        loc_by = 'css selector'
        loc_str = f'#{loc[1]}'
    elif loc[0] == 'class name':
        loc_by = 'xpath'
        loc_str = f'//*[@class="{loc[1]}"]'
    elif loc[0] == 'link text':
        loc_by = 'xpath'
        loc_str = f'//a[text()="{loc[1]}"]'
    elif loc[0] == 'name':
        loc_by = 'css selector'
        loc_str = f'[name={loc[1]}]'
    elif loc[0] == 'tag name':
        loc_by = 'css selector'
        loc_str = loc[1]
    elif loc[0] == 'partial link text':
        loc_by = 'xpath'
        loc_str = f'//a[contains(text(),"{loc[1]}")]'
    return loc_by, loc_str


class SessionPage(object):
    """SessionPage封装了页面操作的常用功能，使用requests_html来获取、解析网页。
    """

    def __init__(self, session: HTMLSession, locs=None):
        """初始化函数"""
        self._session = session
        self._locs = locs
        self._url = None
        self._url_available = None
        self._response = None

    @property
    def session(self) -> HTMLSession:
        return self._session

    @property
    def response(self) -> HTMLResponse:
        return self._response

    @property
    def url(self) -> str:
        """当前访问url"""
        return self._url

    @property
    def url_available(self) -> bool:
        """url有效性"""
        return self._url_available

    @property
    def cookies(self) -> dict:
        """当前session的cookies"""
        return self.session.cookies.get_dict()

    def get_title(self) -> str:
        """获取网页title"""
        return self.get_text(('css selector', 'title'))

    def find(self, loc: tuple, mode: str = None, show_errmsg: bool = True) -> Union[Element, list]:
        """查找一个元素
        :param loc: 页面元素地址
        :param mode: 以某种方式查找元素，可选'single','all'
        :param show_errmsg: 是否显示错误信息
        :return: 页面元素对象或列表
        """
        mode = mode if mode else 'single'
        if mode not in ['single', 'all']:
            raise ValueError("mode须在'single', 'all'中选择")
        loc_by, loc_str = _translate_loc(loc)
        msg = first = None
        try:
            if mode == 'single':
                msg = '未找到元素'
                first = True
            elif mode == 'all':
                msg = '未找到元素s'
                first = False
            if loc_by == 'xpath':
                return self.response.html.xpath(loc_str, first=first, _encoding='utf-8')
            else:
                return self.response.html.find(loc_str, first=first, _encoding='utf-8')
        except:
            if show_errmsg:
                print(msg, loc)
                raise

    def find_all(self, loc: tuple, show_errmsg: bool = True) -> list:
        """查找符合条件的所有元素"""
        return self.find(loc, mode='all', show_errmsg=True)

    def search(self, value: str, mode: str = None) -> Union[Element, list, None]:
        """根据内容搜索元素
        :param value: 搜索内容
        :param mode: 可选'single','all'
        :return: 页面元素对象
        """
        mode = mode if mode else 'single'
        if mode not in ['single', 'all']:
            raise ValueError("mode须在'single', 'all'中选择")
        try:
            if mode == 'single':
                ele = self.response.html.xpath(f'.//*[contains(text(),"{value}")]', first=True)
                return ele
            elif mode == 'all':
                eles = self.response.html.xpath(f'.//*[contains(text(),"{value}")]')
                return eles
        except:
            return

    def search_all(self, value: str) -> list:
        """根据内容搜索元素"""
        return self.search(value, mode='all')

    def _get_ele(self, loc_or_ele: Union[Element, tuple]) -> Element:
        """获取loc或元素实例，返回元素实例"""
        # ======================================
        # ** 必须与DriverPage类中同名函数保持一致 **
        # ======================================
        if isinstance(loc_or_ele, tuple):
            return self.find(loc_or_ele)
        return loc_or_ele

    def get_attr(self, loc_or_ele: Union[Element, tuple], attr: str) -> str:
        """获取元素属性"""
        ele = self._get_ele(loc_or_ele)
        try:
            if attr == 'href':
                # 如直接获取attr只能获取相对地址
                for link in ele.absolute_links:
                    return link
            elif attr == 'class':
                class_str = ''
                for key, i in enumerate(ele.attrs['class']):
                    class_str += ' ' if key > 0 else ''
                    class_str += i
                return class_str
            else:
                return ele.attrs[attr]
        except:
            return ''

    def get_html(self, loc_or_ele: Union[Element, tuple] = None) -> str:
        """获取元素innerHTML，如未指定元素则获取所有源代码"""
        if not loc_or_ele:
            return self.response.html.html
        ele = self._get_ele(loc_or_ele)
        re_str = r'<.*?>(.*)</.*?>'
        html = unescape(ele.html).replace('\xa0', ' ')
        r = re.match(re_str, html, flags=re.DOTALL)
        return r.group(1)

    def get_text(self, loc_or_ele: Union[Element, tuple]) -> str:
        """获取innerText"""
        ele = self._get_ele(loc_or_ele)
        return unescape(ele.text).replace('\xa0', ' ')

    def get(self, url: str, params: dict = None, go_anyway: bool = False, **kwargs) -> Union[bool, None]:
        """用get方式跳转到url，调用_make_response()函数生成response对象"""
        to_url = f'{url}?{parse.urlencode(params)}' if params else url
        if not url or (not go_anyway and self.url == to_url):
            return
        self._response = self._make_response(to_url, **kwargs)[0]
        self._url_available = self._response
        return self._url_available

    # ------------以下为独占函数--------------

    def post(self, url: str, params: dict = None, data: dict = None, go_anyway: bool = False, **kwargs) \
            -> Union[bool, None]:
        """用post方式跳转到url，调用_make_response()函数生成response对象"""
        to_url = f'{url}?{parse.urlencode(params)}' if params else url
        if not url or (not go_anyway and self._url == to_url):
            return
        self._response = self._make_response(to_url, mode='post', data=data, **kwargs)[0]
        self._url_available = self._response
        return self._url_available

    def _make_response(self, url: str, mode: str = 'get', data: dict = None, **kwargs) -> tuple:
        """生成response对象。接收mode参数，以决定用什么方式。
        :param url: 要访问的网址
        :param mode: 'get','post'中选择
        :param data: 提交的数据
        :param kwargs: 其它参数
        :return: Response对象
        """
        if mode not in ['get', 'post']:
            raise ValueError("mode须在'get', 'post'中选择")
        self._url = url
        if not kwargs:
            kwargs = global_session_options
        else:
            for i in global_session_options:
                if i not in kwargs:
                    kwargs[i] = global_session_options[i]
        try:
            r = None
            if mode == 'get':
                r = self.session.get(url, **kwargs)
            elif mode == 'post':
                r = self.session.post(url, data=data, **kwargs)
        except:
            return_value = False
            info = 'URL Invalid'
        else:
            if r.status_code == 200:
                return_value = r
                info = 'Success'
            else:
                return_value = False
                info = f'{r.status_code}'
        return return_value, info
