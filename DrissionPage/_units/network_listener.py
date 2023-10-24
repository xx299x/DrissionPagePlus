# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from base64 import b64decode
from json import JSONDecodeError, loads
from queue import Queue
from re import search
from time import perf_counter, sleep

from requests.structures import CaseInsensitiveDict

from .._base.chromium_driver import ChromiumDriver
from ..errors import CDPError


class NetworkListener(object):
    """监听器基类"""

    def __init__(self, page):
        """
        :param page: ChromiumBase对象
        """
        self._page = page
        self._driver = None

        self._caught = None  # 临存捕捉到的数据
        self._request_ids = None  # 暂存须要拦截的请求id

        self.listening = False
        self._targets = None  # 默认监听所有
        self.tab_id = None  # 当前tab的id

        self._is_regex = False
        self._method = None

    @property
    def targets(self):
        """返回监听目标"""
        return self._targets

    def set_targets(self, targets=True, is_regex=False, method=None):
        """指定要等待的数据包
        :param targets: 要匹配的数据包url特征，可用list等传入多个，为True时获取所有
        :param is_regex: 设置的target是否正则表达式
        :param method: 设置监听的请求类型，可用list等指定多个，为None时监听全部
        :return: None
        """
        if targets is not None:
            if not isinstance(targets, (str, list, tuple, set)) and targets is not True:
                raise TypeError('targets只能是str、list、tuple、set、True。')
            if targets is True:
                targets = ''

            self._targets = {targets} if isinstance(targets, str) else set(targets)

        self._is_regex = is_regex

        if method is not None:
            if isinstance(method, str):
                self._method = {method.upper()}
            elif isinstance(method, (list, tuple, set)):
                self._method = set(i.upper() for i in method)
            else:
                raise TypeError('method参数只能是str、list、tuple、set类型。')

    def start(self, targets=None, is_regex=False, method=None):
        """拦截目标请求，每次拦截前清空结果
        :param targets: 要匹配的数据包url特征，可用list等传入多个，为True时获取所有
        :param is_regex: 设置的target是否正则表达式
        :param method: 设置监听的请求类型，可用list等指定多个，为None时监听全部
        :return: None
        """
        if targets:
            self.set_targets(targets, is_regex, method)
        if self.listening:
            return

        self._driver = ChromiumDriver(self._page.tab_id, 'page', self._page.address)
        self._driver.call_method('Network.enable')

        self.listening = True
        self._request_ids = {}
        self._caught = Queue(maxsize=0)

        self._set_callback()

    def wait(self, count=1, timeout=None, fix_count=True):
        """等待符合要求的数据包到达指定数量
        :param count: 需要捕捉的数据包数量
        :param timeout: 超时时间，为None无限等待
        :param fix_count: 是否必须满足总数要求，发生超时，为True返回False，为False返回已捕捉到的数据包
        :return: count为1时返回数据包对象，大于1时返回列表，超时且fix_count为True时返回False
        """
        if not self.listening:
            raise RuntimeError('监听未启动或已暂停。')
        if not timeout:
            while self._caught.qsize() < count:
                sleep(.05)
            fail = False

        else:
            end = perf_counter() + count
            while True:
                if perf_counter() > end:
                    fail = True
                    break
                if self._caught.qsize() >= count:
                    fail = False
                    break

        if fail:
            if fix_count or not self._caught.qsize():
                return False
            else:
                return [self._caught.get_nowait() for _ in range(self._caught.qsize())]

        if count == 1:
            return self._caught.get_nowait()

        return [self._caught.get_nowait() for _ in range(count)]

    def steps(self, count=None, timeout=None, gap=1):
        """用于单步操作，可实现每收到若干个数据包执行一步操作（如翻页）
        :param count: 需捕获的数据包总数，为None表示无限
        :param timeout: 每个数据包等待时间，为None表示无限
        :param gap: 每接收到多少个数据包返回一次数据
        :return: 用于在接收到监听目标时触发动作的可迭代对象
        """
        caught = 0
        end = perf_counter() + timeout if timeout else None
        while True:
            if timeout and perf_counter() > end:
                return
            if self._caught.qsize() >= gap:
                yield self._caught.get_nowait() if gap == 1 else [self._caught.get_nowait() for _ in range(gap)]
                if timeout:
                    end = perf_counter() + timeout
                if count:
                    caught += gap
                    if caught >= count:
                        return
            sleep(.05)

    def stop(self):
        """停止监听，清空已监听到的列表"""
        if self.listening:
            self.pause()
            self.clear()
        self._driver.stop()
        self._driver = None

    def pause(self, clear=True):
        """暂停监听
        :param clear: 是否清空已获取队列
        :return: None
        """
        if self.listening:
            self._driver.set_listener('Network.requestWillBeSent', None)
            self._driver.set_listener('Network.responseReceived', None)
            self._driver.set_listener('Network.loadingFinished', None)
            self._driver.set_listener('Network.loadingFailed', None)
            self.listening = False
        if clear:
            self.clear()

    def go_on(self):
        """继续暂停的监听"""
        if self.listening:
            return
        self._set_callback()
        self.listening = True

    def clear(self):
        """清空结果"""
        self._request_ids = {}
        self._caught.queue.clear()

    def _set_callback(self):
        """设置监听请求的回调函数"""
        self._driver.set_listener('Network.requestWillBeSent', self._requestWillBeSent)
        self._driver.set_listener('Network.responseReceived', self._response_received)
        self._driver.set_listener('Network.loadingFinished', self._loading_finished)
        self._driver.set_listener('Network.loadingFailed', self._loading_failed)

    def _requestWillBeSent(self, **kwargs):
        """接收到请求时的回调函数"""
        if not self._targets:
            self._request_ids[kwargs['requestId']] = DataPacket(self._page.tab_id, None, kwargs)
            if kwargs['request'].get('hasPostData', None) and not kwargs['request'].get('postData', None):
                self._request_ids[kwargs['requestId']]._raw_post_data = \
                    self._driver.call_method('Network.getRequestPostData', requestId=kwargs['requestId'])['postData']

            return

        for target in self._targets:
            if ((self._is_regex and search(target, kwargs['request']['url'])) or
                (not self._is_regex and target in kwargs['request']['url'])) and (
                    not self._method or kwargs['request']['method'] in self._method):
                self._request_ids[kwargs['requestId']] = DataPacket(self._page.tab_id, target, kwargs)

                if kwargs['request'].get('hasPostData', None) and not kwargs['request'].get('postData', None):
                    self._request_ids[kwargs['requestId']]._raw_post_data = \
                        self._driver.call_method('Network.getRequestPostData', requestId=kwargs['requestId'])['postData']

                break

    def _response_received(self, **kwargs):
        """接收到返回信息时处理方法"""
        request_id = kwargs['requestId']
        if request_id in self._request_ids:
            self._request_ids[request_id]._raw_response = kwargs['response']
            self._request_ids[request_id]._resource_type = kwargs['type']

    def _loading_finished(self, **kwargs):
        """请求完成时处理方法"""
        request_id = kwargs['requestId']
        dp = self._request_ids.get(request_id)
        if dp:
            try:
                r = self._driver.call_method('Network.getResponseBody', requestId=request_id)
                body = r['body']
                is_base64 = r['base64Encoded']
            except CDPError:
                body = ''
                is_base64 = False

            dp._raw_body = body
            dp._base64_body = is_base64

            self._caught.put(dp)
            try:
                self._request_ids.pop(request_id)
            except:
                pass

    def _loading_failed(self, **kwargs):
        """请求失败时的回调方法"""
        request_id = kwargs['requestId']
        if request_id in self._request_ids:
            dp = self._request_ids[request_id]
            dp.errorText = kwargs['errorText']
            dp._resource_type = kwargs['type']

            self._caught.put(dp)
            try:
                self._request_ids.pop(request_id)
            except:
                pass


class DataPacket(object):
    """返回的数据包管理类"""

    def __init__(self, tab_id, target, raw_request):
        """
        :param tab_id: 产生这个数据包的tab的id
        :param target: 监听目标
        :param raw_request: 原始request数据，从cdp获得
        """
        self.tab_id = tab_id
        self.target = target

        self._raw_request = raw_request
        self._raw_post_data = None

        self._raw_response = None
        self._raw_body = None
        self._base64_body = False

        self._request = None
        self._response = None
        self.errorText = None
        self._resource_type = None

    def __repr__(self):
        t = f'"{self.target}"' if self.target is not None else None
        return f'<DataPacket target={t} url="{self.url}">'

    @property
    def url(self):
        return self.request.url

    @property
    def method(self):
        return self.request.method

    @property
    def frameId(self):
        return self._raw_request.get('frameId')

    @property
    def resourceType(self):
        return self._resource_type

    @property
    def request(self):
        if self._request is None:
            self._request = Request(self._raw_request['request'], self._raw_post_data)
        return self._request

    @property
    def response(self):
        if self._response is None:
            self._response = Response(self._raw_response, self._raw_body, self._base64_body)
        return self._response


class Request(object):
    def __init__(self, raw_request, post_data):
        self._request = raw_request
        self._raw_post_data = post_data
        self._postData = None
        self._headers = None

    def __getattr__(self, item):
        return self._request.get(item, None)

    @property
    def headers(self):
        """以大小写不敏感字典返回headers数据"""
        if self._headers is None:
            self._headers = CaseInsensitiveDict(self._request['headers'])
        return self._headers

    @property
    def postData(self):
        """返回postData数据"""
        if self._postData is None:
            if self._raw_post_data:
                postData = self._raw_post_data
            elif self._request.get('postData', None):
                postData = self._request['postData']
            else:
                postData = False
            try:
                self._postData = loads(postData)
            except (JSONDecodeError, TypeError):
                self._postData = postData
        return self._postData


class Response(object):
    def __init__(self, raw_response, raw_body, base64_body):
        self._response = raw_response
        self._raw_body = raw_body
        self._is_base64_body = base64_body
        self._body = None
        self._headers = None

    def __getattr__(self, item):
        return self._response.get(item, None)

    @property
    def headers(self):
        """以大小写不敏感字典返回headers数据"""
        if self._headers is None:
            self._headers = CaseInsensitiveDict(self._response['headers'])
        return self._headers

    @property
    def raw_body(self):
        """返回未被处理的body文本"""
        return self._raw_body

    @property
    def body(self):
        """返回body内容，如果是json格式，自动进行转换，如果时图片格式，进行base64转换，其它格式直接返回文本"""
        if self._body is None:
            if self._is_base64_body:
                self._body = b64decode(self._raw_body)

            else:
                try:
                    self._body = loads(self._raw_body)
                except (JSONDecodeError, TypeError):
                    self._body = self._raw_body

        return self._body
