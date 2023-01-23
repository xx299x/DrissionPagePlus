# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from re import search
from time import sleep
from urllib.parse import urlparse

from DownloadKit import DownloadKit
from requests import Session, Response
from requests.structures import CaseInsensitiveDict
from tldextract import extract

from .base import BasePage
from .configs.session_options import SessionOptions
from .functions.web import cookie_to_dict, set_session_cookies
from .session_element import SessionElement, make_session_ele


class SessionPage(BasePage):
    """SessionPage封装了页面操作的常用功能，使用requests来获取、解析网页"""

    def __init__(self, session_or_options=None, timeout=None):
        """
        :param session_or_options: Session对象或SessionOptions对象
        :param timeout: 连接超时时间，为None时从ini文件读取
        """
        self._response = None
        self._download_set = None
        self._create_session(session_or_options)
        timeout = timeout if timeout is not None else self.timeout
        super().__init__(timeout)

    def _create_session(self, Session_or_Options):
        """创建内建Session对象
        :param Session_or_Options: Session对象或SessionOptions对象
        :return: None
        """
        if Session_or_Options is None or isinstance(Session_or_Options, SessionOptions):
            self._session_options = Session_or_Options or SessionOptions()
            self._set_session(self._session_options)

        elif isinstance(Session_or_Options, Session):
            self._session = Session_or_Options
            self._session_options = SessionOptions(read_file=False)

        self._set_options()

    def _set_session(self, opt):
        """根据传入字典对session进行设置
        :param opt: session配置字典
        :return: None
        """
        self._session = Session()

        if opt.headers:
            self._session.headers = CaseInsensitiveDict(opt.headers)
        if opt.cookies:
            self.set_cookies(opt.cookies)
        if opt.adapters:
            for url, adapter in opt.adapters:
                self._session.mount(url, adapter)

        attrs = ['auth', 'proxies', 'hooks', 'params', 'verify',
                 'cert', 'stream', 'trust_env', 'max_redirects']
        for i in attrs:
            attr = opt.__getattribute__(i)
            if attr:
                self._session.__setattr__(i, attr)

    def _set_options(self):
        """设置WebPage中与d模式共用的配置，便于WebPage覆盖掉"""
        self._timeout = self._session_options.timeout
        self._download_path = self._session_options.download_path

    def set_cookies(self, cookies):
        """为Session对象设置cookies
        :param cookies: cookies信息
        :return: None
        """
        set_session_cookies(self.session, cookies)

    def set_headers(self, headers):
        """设置通用的headers，设置的headers值回逐个覆盖原有的，不会清理原来的
        :param headers: dict形式的headers
        :return: None
        """
        headers = CaseInsensitiveDict(headers)
        for i in headers:
            self.session.headers[i] = headers[i]

    def __call__(self, loc_or_str, timeout=None):
        """在内部查找元素
        例：ele2 = ele1('@id=ele_id')
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 不起实际作用，用于和DriverElement对应，便于无差别调用
        :return: SessionElement对象或属性文本
        """
        return self.ele(loc_or_str)

    # -----------------共有属性和方法-------------------
    @property
    def url(self):
        """返回当前访问url"""
        return self._url

    @property
    def html(self):
        """返回页面的html文本"""
        return self.response.text if self.response else ''

    @property
    def json(self):
        """当返回内容是json格式时，返回对应的字典，非json格式时返回None"""
        try:
            return self.response.json()
        except Exception:
            return None

    @property
    def download_path(self):
        """返回下载路径"""
        return self._download_path

    @property
    def download_set(self):
        """返回用于设置下载参数的对象"""
        if self._download_set is None:
            self._download_set = DownloadSetter(self)
        return self._download_set

    @property
    def download(self):
        """返回下载器对象"""
        return self.download_set.DownloadKit

    @property
    def session(self):
        """返回session对象"""
        return self._session

    @property
    def response(self):
        """返回访问url得到的response对象"""
        return self._response

    def get(self, url, show_errmsg=False, retry=None, interval=None, timeout=None, **kwargs):
        """用get方式跳转到url
        :param url: 目标url
        :param show_errmsg: 是否显示和抛出异常
        :param retry: 重试次数
        :param interval: 重试间隔（秒）
        :param timeout: 连接超时时间（秒）
        :param kwargs: 连接参数
        :return: url是否可用
        """
        return self._s_connect(url, 'get', None, show_errmsg, retry, interval, **kwargs)

    def ele(self, loc_or_ele, timeout=None):
        """返回页面中符合条件的第一个元素、属性或节点文本
        :param loc_or_ele: 元素的定位信息，可以是元素对象，loc元组，或查询字符串
        :param timeout: 不起实际作用，用于和DriverElement对应，便于无差别调用
        :return: SessionElement对象或属性、文本
        """
        return self._ele(loc_or_ele)

    def eles(self, loc_or_str, timeout=None):
        """返回页面中所有符合条件的元素、属性或节点文本
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 不起实际作用，用于和DriverElement对应，便于无差别调用
        :return: SessionElement对象或属性、文本组成的列表
        """
        return self._ele(loc_or_str, single=False)

    def s_ele(self, loc_or_ele=None):
        """返回页面中符合条件的第一个元素、属性或节点文本
        :param loc_or_ele: 元素的定位信息，可以是元素对象，loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        return make_session_ele(self.html) if loc_or_ele is None else self._ele(loc_or_ele)

    def s_eles(self, loc_or_str):
        """返回页面中符合条件的所有元素、属性或节点文本
        :param loc_or_str: 元素的定位信息，可以是元素对象，loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        return self._ele(loc_or_str, single=False)

    def _ele(self, loc_or_ele, timeout=None, single=True):
        """返回页面中符合条件的元素、属性或节点文本，默认返回第一个
        :param loc_or_ele: 元素的定位信息，可以是元素对象，loc元组，或查询字符串
        :param timeout: 不起实际作用，用于和父类对应
        :param single: True则返回第一个，False则返回全部
        :return: SessionElement对象
        """
        return loc_or_ele if isinstance(loc_or_ele, SessionElement) else make_session_ele(self, loc_or_ele, single)

    def get_cookies(self, as_dict=False, all_domains=False):
        """返回cookies
        :param as_dict: 是否以字典方式返回
        :param all_domains: 是否返回所有域的cookies
        :return: cookies信息
        """
        if all_domains:
            cookies = self.session.cookies
        else:
            if self.url:
                url = extract(self.url)
                domain = f'{url.domain}.{url.suffix}'
                cookies = tuple(x for x in self.session.cookies if domain in x.domain or x.domain == '')
            else:
                cookies = tuple(x for x in self.session.cookies)

        if as_dict:
            return {x.name: x.value for x in cookies}
        else:
            return [cookie_to_dict(cookie) for cookie in cookies]

    def post(self, url, data=None, show_errmsg=False, retry=None, interval=None, **kwargs):
        """用post方式跳转到url
        :param url: 目标url
        :param data: 提交的数据
        :param show_errmsg: 是否显示和抛出异常
        :param retry: 重试次数
        :param interval: 重试间隔（秒）
        :param kwargs: 连接参数
        :return: url是否可用
        """
        return self._s_connect(url, 'post', data, show_errmsg, retry, interval, **kwargs)

    def _s_connect(self, url, mode, data=None, show_errmsg=False, retry=None, interval=None, **kwargs):
        """执行get或post连接
        :param url: 目标url
        :param mode: 'get' 或 'post'
        :param data: 提交的数据
        :param show_errmsg: 是否显示和抛出异常
        :param retry: 重试次数
        :param interval: 重试间隔（秒）
        :param kwargs: 连接参数
        :return: url是否可用
        """
        retry, interval = self._before_connect(url, retry, interval)
        self._response, info = self._make_response(self._url, mode, data, retry, interval, show_errmsg, **kwargs)

        if self._response is None:
            self._url_available = False

        else:
            if self._response.ok:
                self._url_available = True

            else:
                if show_errmsg:
                    raise ConnectionError(f'状态码：{self._response.status_code}.')
                self._url_available = False

        return self._url_available

    def _make_response(self, url, mode='get', data=None, retry=None, interval=None, show_errmsg=False, **kwargs):
        """生成Response对象
        :param url: 目标url
        :param mode: 'get' 或 'post'
        :param data: post方式要提交的数据
        :param show_errmsg: 是否显示和抛出异常
        :param kwargs: 其它参数
        :return: tuple，第一位为Response或None，第二位为出错信息或'Success'
        """
        kwargs = CaseInsensitiveDict(kwargs)
        if 'headers' not in kwargs:
            kwargs['headers'] = {}
        else:
            kwargs['headers'] = CaseInsensitiveDict(kwargs['headers'])

        # 设置referer和host值
        parsed_url = urlparse(url)
        hostname = parsed_url.hostname
        scheme = parsed_url.scheme
        if not check_headers(kwargs, self.session.headers, 'Referer'):
            kwargs['headers']['Referer'] = self.url if self.url else f'{scheme}://{hostname}'
        if 'Host' not in kwargs['headers']:
            kwargs['headers']['Host'] = hostname

        if not check_headers(kwargs, self.session.headers, 'timeout'):
            kwargs['timeout'] = self.timeout

        if 'allow_redirects' not in kwargs:
            kwargs['allow_redirects'] = False

        r = err = None
        retry = retry if retry is not None else self.retry_times
        interval = interval if interval is not None else self.retry_interval
        for i in range(retry + 1):
            try:
                if mode == 'get':
                    r = self.session.get(url, **kwargs)
                elif mode == 'post':
                    r = self.session.post(url, data=data, **kwargs)

                if r:
                    return set_charset(r), 'Success'

            except Exception as e:
                err = e

            # if r and r.status_code in (403, 404):
            #     break

            if i < retry:
                sleep(interval)
                if show_errmsg:
                    print(f'重试 {url}')

        if r is None:
            if show_errmsg:
                if err:
                    raise err
                else:
                    raise ConnectionError('连接失败')
            return None, '连接失败' if err is None else err

        if not r.ok:
            if show_errmsg:
                raise ConnectionError(f'状态码：{r.status_code}')
            return r, f'状态码：{r.status_code}'


class DownloadSetter(object):
    """用于设置下载参数的类"""

    def __init__(self, page):
        self._page = page
        self._DownloadKit = None

    @property
    def DownloadKit(self):
        if self._DownloadKit is None:
            self._DownloadKit = DownloadKit(session=self._page, goal_path=self._page.download_path)
        return self._DownloadKit

    @property
    def if_file_exists(self):
        """返回用于设置存在同名文件时处理方法的对象"""
        return FileExists(self)

    def split(self, on_off):
        """设置是否允许拆分大文件用多线程下载
        :param on_off: 是否启用多线程下载大文件
        :return: None
        """
        self.DownloadKit.split = on_off

    def save_path(self, path):
        """设置下载保存路径
        :param path: 下载保存路径
        :return: None
        """
        path = path if path is None else str(path)
        self._page._download_path = path
        self.DownloadKit.goal_path = path


class FileExists(object):
    """用于设置存在同名文件时处理方法"""

    def __init__(self, setter):
        """
        :param setter: DownloadSetter对象
        """
        self._setter = setter

    def __call__(self, mode):
        if mode not in ('skip', 'rename', 'overwrite'):
            raise ValueError("mode参数只能是'skip', 'rename', 'overwrite'")
        self._setter.DownloadKit.file_exists = mode

    def skip(self):
        """设为跳过"""
        self._setter.DownloadKit.file_exists = 'skip'

    def rename(self):
        """设为重命名，文件名后加序号"""
        self._setter.DownloadKit._file_exists = 'rename'

    def overwrite(self):
        """设为覆盖"""
        self._setter.DownloadKit._file_exists = 'overwrite'


def check_headers(kwargs, headers, arg) -> bool:
    """检查kwargs或headers中是否有arg所示属性"""
    return arg in kwargs['headers'] or arg in headers


def set_charset(response) -> Response:
    """设置Response对象的编码"""
    # 在headers中获取编码
    content_type = response.headers.get('content-type', '').lower()
    charset = search(r'charset[=: ]*(.*)?;', content_type)

    if charset:
        response.encoding = charset.group(1)

    # 在headers中获取不到编码，且如果是网页
    elif content_type.replace(' ', '').startswith('text/html'):
        re_result = search(b'<meta.*?charset=[ \\\'"]*([^"\\\' />]+).*?>', response.content)

        if re_result:
            charset = re_result.group(1).decode()
        else:
            charset = response.apparent_encoding

        response.encoding = charset

    return response
