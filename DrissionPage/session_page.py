# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   session_page.py
"""
from os import path as os_PATH
from pathlib import Path
from random import randint
from re import search as re_SEARCH, sub
from time import time, sleep
from typing import Union, List, Tuple
from urllib.parse import urlparse, quote, unquote

from requests import Session, Response
from tldextract import extract

from .base import BasePage
from .common import str_to_loc, translate_loc, get_available_file_name, format_html
from .config import _cookie_to_dict
from .session_element import SessionElement, execute_session_find


class SessionPage(BasePage):
    """SessionPage封装了页面操作的常用功能，使用requests来获取、解析网页"""

    def __init__(self, session: Session, timeout: float = 10):
        """初始化函数"""
        super().__init__(timeout)
        self._session = session
        self._response = None

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str, SessionElement],
                 mode: str = 'single',
                 timeout: float = None) -> Union[SessionElement, List[SessionElement]]:
        """在内部查找元素                                            \n
        例：ele2 = ele1('@id=ele_id')                               \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param mode: 'single' 或 'all'，对应查找一个或全部
        :param timeout: 不起实际作用，用于和父类对应
        :return: SessionElement对象
        """
        return super().__call__(loc_or_str, mode, timeout)

    # -----------------共有属性和方法-------------------
    @property
    def url(self) -> str:
        """返回当前访问url"""
        return self._url

    @property
    def title(self) -> str:
        """返回网页title"""
        return self.ele('tag:title').text

    @property
    def html(self) -> str:
        """返回页面html文本"""
        return format_html(self.response.text) if self.response else ''

    @property
    def json(self) -> dict:
        """当返回内容是json格式时，返回对应的字典"""
        return self.response.json()

    def get(self,
            url: str,
            go_anyway: bool = False,
            show_errmsg: bool = False,
            retry: int = None,
            interval: float = None,
            **kwargs) -> Union[bool, None]:
        """用get方式跳转到url                                 \n
        :param url: 目标url
        :param go_anyway: 若目标url与当前url一致，是否强制跳转
        :param show_errmsg: 是否显示和抛出异常
        :param retry: 重试次数
        :param interval: 重试间隔（秒）
        :param kwargs: 连接参数
        :return: url是否可用
        """
        to_url = quote(url, safe='/:&?=%;#@+!')
        retry = int(retry) if retry is not None else int(self.retry_times)
        interval = int(interval) if interval is not None else int(self.retry_interval)

        if not url or (not go_anyway and self.url == to_url):
            return

        self._url = to_url
        self._response = self._try_to_connect(to_url, times=retry, interval=interval, show_errmsg=show_errmsg, **kwargs)

        if self._response is None:
            self._url_available = False

        else:
            if self._response.ok:
                self._url_available = True

            else:
                if show_errmsg:
                    raise ConnectionError(f'{to_url}\nStatus code: {self._response.status_code}.')

                self._url_available = False

        return self._url_available

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, SessionElement],
            mode: str = None, timeout=None) -> Union[SessionElement, List[SessionElement], str, None]:
        """返回页面中符合条件的元素、属性或节点文本，默认返回第一个                                           \n
        :param loc_or_ele: 元素的定位信息，可以是元素对象，loc元组，或查询字符串
        :param mode: 'single' 或 'all‘，对应查找一个或全部
        :param timeout: 不起实际作用，用于和父类对应
        :return: SessionElement对象
        """
        if isinstance(loc_or_ele, (str, tuple)):
            if isinstance(loc_or_ele, str):
                loc_or_ele = str_to_loc(loc_or_ele)
            else:
                if len(loc_or_ele) != 2:
                    raise ValueError("Len of loc_or_ele must be 2 when it's a tuple.")
                loc_or_ele = translate_loc(loc_or_ele)

        elif isinstance(loc_or_ele, SessionElement):
            return loc_or_ele

        else:
            raise ValueError('Argument loc_or_str can only be tuple, str, SessionElement, Element.')

        return execute_session_find(self, loc_or_ele, mode)

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str], timeout=None) -> List[SessionElement]:
        """返回页面中所有符合条件的元素、属性或节点文本                                                     \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 不起实际作用，用于和父类对应
        :return: SessionElement对象组成的列表
        """
        if not isinstance(loc_or_str, (tuple, str)):
            raise TypeError('Type of loc_or_str can only be tuple or str.')

        return super().eles(loc_or_str, timeout)

    def get_cookies(self, as_dict: bool = False, all_domains: bool = False) -> Union[dict, list]:
        """返回cookies                               \n
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
            return [_cookie_to_dict(cookie) for cookie in cookies]

    def _try_to_connect(self,
                        to_url: str,
                        times: int = 0,
                        interval: float = 1,
                        mode: str = 'get',
                        data: dict = None,
                        show_errmsg: bool = False,
                        **kwargs) -> Response:
        """尝试连接，重试若干次                            \n
        :param to_url: 要访问的url
        :param times: 重试次数
        :param interval: 重试间隔（秒）
        :param mode: 连接方式，'get' 或 'post'
        :param data: post方式提交的数据
        :param show_errmsg: 是否抛出异常
        :param kwargs: 连接参数
        :return: HTMLResponse对象
        """
        err = None
        r = None

        for _ in range(times + 1):
            try:
                r = self._make_response(to_url, mode=mode, show_errmsg=True, **kwargs)[0]
            except Exception as e:
                err = e
                r = None

            if r and (r.content != b'' or r.status_code in (403, 404)):
                break

            if _ < times:
                sleep(interval)
                print(f'重试 {to_url}')

        if not r and show_errmsg:
            raise err if err is not None else ConnectionError('Connect error.')

        return r

    # ----------------session独有属性和方法-----------------------
    @property
    def session(self) -> Session:
        """返回session对象"""
        return self._session

    @property
    def response(self) -> Response:
        """返回访问url得到的response对象"""
        return self._response

    def post(self,
             url: str,
             data: dict = None,
             go_anyway: bool = True,
             show_errmsg: bool = False,
             retry: int = None,
             interval: float = None,
             **kwargs) -> Union[bool, None]:
        """用post方式跳转到url                                 \n
        :param url: 目标url
        :param data: 提交的数据
        :param go_anyway: 若目标url与当前url一致，是否强制跳转
        :param show_errmsg: 是否显示和抛出异常
        :param retry: 重试次数
        :param interval: 重试间隔（秒）
        :param kwargs: 连接参数
        :return: url是否可用
        """
        to_url = quote(url, safe='/:&?=%;#@+!')
        retry = int(retry) if retry is not None else int(self.retry_times)
        interval = int(interval) if interval is not None else int(self.retry_interval)

        if not url or (not go_anyway and self._url == to_url):
            return

        self._url = to_url
        self._response = self._try_to_connect(to_url, retry, interval, 'post', data, show_errmsg, **kwargs)

        if self._response is None:
            self._url_available = False

        else:
            if self._response.ok:
                self._url_available = True

            else:
                if show_errmsg:
                    raise ConnectionError(f'Status code: {self._response.status_code}.')
                self._url_available = False

        return self._url_available

    def download(self,
                 file_url: str,
                 goal_path: str,
                 rename: str = None,
                 file_exists: str = 'rename',
                 post_data: dict = None,
                 show_msg: bool = False,
                 show_errmsg: bool = False,
                 retry: int = None,
                 interval: float = None,
                 **kwargs) -> tuple:
        """下载一个文件                                                                   \n
        :param file_url: 文件url
        :param goal_path: 存放路径
        :param rename: 重命名文件，可不写扩展名
        :param file_exists: 若存在同名文件，可选择 'rename', 'overwrite', 'skip' 方式处理
        :param post_data: post方式的数据
        :param show_msg: 是否显示下载信息
        :param show_errmsg: 是否抛出和显示异常
        :param retry: 重试次数
        :param interval: 重试间隔时间
        :param kwargs: 连接参数
        :return: 下载是否成功（bool）和状态信息（成功时信息为文件路径）的元组
        """
        if file_exists == 'skip' and Path(f'{goal_path}\\{rename}').exists():
            if show_msg:
                print(f'{file_url}\n{goal_path}\\{rename}\nSkipped.\n')

            return False, 'Skipped because a file with the same name already exists.'

        def do(url: str,
               goal: str,
               new_name: str = None,
               exists: str = 'rename',
               data: dict = None,
               msg: bool = False,
               errmsg: bool = False,
               **args) -> tuple:
            args['stream'] = True

            if 'timeout' not in args:
                args['timeout'] = 20

            mode = 'post' if data else 'get'
            # 生成的response不写入self._response，是临时的
            r, info = self._make_response(url, mode=mode, data=data, show_errmsg=errmsg, **args)

            if r is None:
                if msg:
                    print(info)

                return False, info

            if not r.ok:
                if errmsg:
                    raise ConnectionError(f'Status code: {r.status_code}.')

                return False, f'Status code: {r.status_code}.'

            # -------------------获取文件名-------------------
            file_name = ''
            content_disposition = r.headers.get('content-disposition')

            # 使用header里的文件名
            if content_disposition:
                file_name = content_disposition.encode('ISO-8859-1').decode('utf-8')
                file_name = re_SEARCH(r'filename *= *"?([^";]+)', file_name)

                if file_name:
                    file_name = file_name.group(1)

                    if file_name[0] == file_name[-1] == "'":
                        file_name = file_name[1:-1]

            # 在url里获取文件名
            if not file_name and os_PATH.basename(url):
                file_name = os_PATH.basename(url).split("?")[0]

            # 找不到则用时间和随机数生成文件名
            if not file_name:
                file_name = f'untitled_{time()}_{randint(0, 100)}'

            # 去除非法字符
            file_name = sub(r'[\\/*:|<>?"]', '', file_name).strip()
            file_name = unquote(file_name)

            # -------------------重命名，不改变扩展名-------------------
            if new_name:
                new_name = sub(r'[\\/*:|<>?"]', '', new_name).strip()
                ext_name = file_name.split('.')[-1]

                if '.' in new_name or ext_name == file_name:
                    full_name = new_name
                else:
                    full_name = f'{new_name}.{ext_name}'

            else:
                full_name = file_name

            # -------------------生成路径-------------------
            goal_Path = Path(goal)
            goal = ''
            skip = False

            for key, p in enumerate(goal_Path.parts):  # 去除路径中的非法字符
                goal += goal_Path.drive if key == 0 and goal_Path.drive else sub(r'[*:|<>?"]', '', p).strip()
                goal += '\\' if p != '\\' and key < len(goal_Path.parts) - 1 else ''

            goal_Path = Path(goal).absolute()
            goal_Path.mkdir(parents=True, exist_ok=True)
            full_path = Path(f'{goal}\\{full_name}')

            if full_path.exists():
                if file_exists == 'rename':
                    full_name = get_available_file_name(goal, full_name)
                    full_path = Path(f'{goal}\\{full_name}')

                elif exists == 'skip':
                    skip = True

                elif exists == 'overwrite':
                    pass

                else:
                    raise ValueError("Argument file_exists can only be 'skip', 'overwrite', 'rename'.")

            # -------------------打印要下载的文件-------------------
            if msg:
                print(file_url)
                print(full_name if file_name == full_name else f'{file_name} -> {full_name}')
                print(f'Downloading to: {goal}')

                if skip:
                    print('Skipped.\n')

            # -------------------开始下载-------------------
            if skip:
                return False, 'Skipped because a file with the same name already exists.'

            # 获取远程文件大小
            content_length = r.headers.get('content-length')
            file_size = int(content_length) if content_length else None

            # 已下载文件大小和下载状态
            downloaded_size, download_status = 0, False

            try:
                with open(str(full_path), 'wb') as tmpFile:
                    for chunk in r.iter_content(chunk_size=1024):
                        if chunk:
                            tmpFile.write(chunk)

                            # 如表头有返回文件大小，显示进度
                            if msg and file_size:
                                downloaded_size += 1024
                                rate = downloaded_size / file_size if downloaded_size < file_size else 1
                                print('\r {:.0%} '.format(rate), end="")

            except Exception as e:
                if errmsg:
                    raise ConnectionError(e)

                download_status, info = False, f'Download failed.\n{e}'

            else:
                if full_path.stat().st_size == 0:
                    if errmsg:
                        raise ValueError('File size is 0.')

                    download_status, info = False, 'File size is 0.'

                else:
                    download_status, info = True, str(full_path)

            finally:
                # 删除下载出错文件
                if not download_status and full_path.exists():
                    full_path.unlink()

                r.close()

            # -------------------显示并返回值-------------------
            if msg:
                print(info, '\n')

            info = f'{goal}\\{full_name}' if download_status else info
            return download_status, info

        retry_times = retry or self.retry_times
        retry_interval = interval or self.retry_interval
        result = do(file_url, goal_path, rename, file_exists, post_data, show_msg, show_errmsg, **kwargs)

        if not result[0] and not str(result[1]).startswith('Skipped'):
            for i in range(retry_times):
                sleep(retry_interval)

                print(f'重试 {file_url}')
                result = do(file_url, goal_path, rename, file_exists, post_data, show_msg, show_errmsg, **kwargs)
                if result[0]:
                    break

        return result

    def _make_response(self,
                       url: str,
                       mode: str = 'get',
                       data: dict = None,
                       show_errmsg: bool = False,
                       **kwargs) -> tuple:
        """生成response对象                     \n
        :param url: 目标url
        :param mode: 'get', 'post' 中选择
        :param data: post方式要提交的数据
        :param show_errmsg: 是否显示和抛出异常
        :param kwargs: 其它参数
        :return: tuple，第一位为Response或None，第二位为出错信息或'Success'
        """
        if not url:
            if show_errmsg:
                raise ValueError('url is empty.')
            return None, 'url is empty.'

        if mode not in ('get', 'post'):
            raise ValueError("Argument mode can only be 'get' or 'post'.")

        url = quote(url, safe='/:&?=%;#@+!')

        # 设置referer和host值
        kwargs_set = set(x.lower() for x in kwargs)

        if 'headers' in kwargs_set:
            header_set = set(x.lower() for x in kwargs['headers'])

            if self.url and 'referer' not in header_set:
                kwargs['headers']['Referer'] = self.url

            if 'host' not in header_set:
                kwargs['headers']['Host'] = urlparse(url).hostname

        else:
            kwargs['headers'] = self.session.headers
            kwargs['headers']['Host'] = urlparse(url).hostname

            if self.url:
                kwargs['headers']['Referer'] = self.url

        if 'timeout' not in kwargs_set:
            kwargs['timeout'] = self.timeout

        try:
            r = None

            if mode == 'get':
                r = self.session.get(url, **kwargs)
            elif mode == 'post':
                r = self.session.post(url, data=data, **kwargs)

        except Exception as e:
            if show_errmsg:
                raise e

            return None, e

        else:
            # ----------------获取并设置编码开始-----------------
            # 在headers中获取编码
            content_type = r.headers.get('content-type', '').lower()
            charset = re_SEARCH(r'charset[=: ]*(.*)?[;]', content_type)

            if charset:
                r.encoding = charset.group(1)

            # 在headers中获取不到编码，且如果是网页
            elif content_type.replace(' ', '').startswith('text/html'):
                re_result = re_SEARCH(b'<meta.*?charset=[ \\\'"]*([^"\\\' />]+).*?>', r.content)

                if re_result:
                    charset = re_result.group(1).decode()
                else:
                    charset = r.apparent_encoding

                r.encoding = charset
            # ----------------获取并设置编码结束-----------------

            return r, 'Success'
