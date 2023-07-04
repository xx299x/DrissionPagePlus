# -*- coding:utf-8 -*-
from base64 import b64decode
from json import JSONDecodeError, loads
from queue import Queue
from re import search
from threading import Thread
from time import perf_counter, sleep

from requests.structures import CaseInsensitiveDict

from .errors import CDPError


class NetworkListener(object):
    """监听器基类"""

    def __init__(self, page):
        """
        :param page: ChromiumBase对象
        """
        self._page = page
        self._driver = self._page.driver

        self._tmp = None  # 临存捕捉到的数据
        self._request_ids = None  # 暂存须要拦截的请求id

        self._total_count = None  # 当次监听的数量上限
        self._caught_count = None  # 当次已监听到的数量
        self._begin_time = None  # 当次监听开始时间
        self._timeout = None  # 当次监听超时时间

        self.listening = False
        self._targets = None  # 默认监听所有
        self.tab_id = None  # 当前tab的id
        self._results = []

        self._is_regex = False
        self._method = None

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

            if isinstance(targets, str):
                self._targets = {targets}
            else:
                self._targets = set(targets)

        self._is_regex = is_regex

        if method is not None:
            if isinstance(method, str):
                self._method = {method.upper()}
            elif isinstance(method, (list, tuple, set)):
                self._method = set(i.upper() for i in method)
            else:
                raise TypeError('method参数只能是str、list、tuple、set类型。')

    def listen(self, targets=None, count=None, timeout=None):
        """拦截目标请求，直到超时或达到拦截个数，每次拦截前清空结果
        可监听多个目标，请求url包含这些字符串就会被记录
        :param targets: 要监听的目标字符串或其组成的列表，True监听所有，None则保留之前的目标不变
        :param count: 要记录的个数，到达个数停止监听
        :param timeout: 监听最长时间，到时间即使未达到记录个数也停止，None为无限长
        :return: None
        """
        if targets:
            self.set_targets(targets)

        self.listening = True
        self._results = []
        self._request_ids = {}
        self._tmp = Queue(maxsize=0)

        self._caught_count = 0
        self._begin_time = perf_counter()
        self._timeout = timeout

        self._set_callback_func()

        self._total_count = len(self._targets) if not count else count

        Thread(target=self._wait_to_stop).start()

    def stop(self):
        """停止监听"""
        self._stop()
        self.listening = False

    def wait(self):
        """等待监听结束"""
        while self.listening:
            sleep(.2)
        return self._results

    def get_results(self, target=None):
        """获取结果列表
        :param target: 要获取的目标，为None时获取全部
        :return: 结果数据组成的列表
        """
        return self._results if target is None else [i for i in self._results if i.target == target]

    def _wait_to_stop(self):
        """当收到停止信号、到达须获取结果数、到时间就停止"""
        while self._is_continue():
            sleep(.2)
        self.stop()

    def _is_continue(self):
        """是否继续当前监听"""
        return self.listening \
            and (self._total_count is None or self._caught_count < self._total_count) \
            and (self._timeout is None or perf_counter() - self._begin_time < self._timeout)

    def steps(self, gap=1):
        """用于单步操作，可实现没收到若干个数据包执行一步操作（如翻页）
        :param gap: 每接收到多少个数据包触发
        :return: 用于在接收到监听目标时触发动作的可迭代对象
        """
        if not isinstance(gap, int) or gap < 1:
            raise ValueError('gap参数必须为大于0的整数。')
        while self.listening or not self._tmp.empty():
            while self._tmp.qsize() >= gap:
                yield self._tmp.get(False) if gap == 1 else [self._tmp.get(False) for _ in range(gap)]

            sleep(.1)

    def _set_callback_func(self):
        """设置监听请求的回调函数"""
        self._driver.set_listener('Network.requestWillBeSent', self._requestWillBeSent)
        self._driver.set_listener('Network.responseReceived', self._response_received)
        self._driver.set_listener('Network.loadingFinished', self._loading_finished)
        self._driver.set_listener('Network.loadingFailed', self._loading_failed)
        self._driver.call_method('Network.enable')

    def _stop(self) -> None:
        """停止监听前要做的工作"""
        self._driver.set_listener('Network.requestWillBeSent', None)
        self._driver.set_listener('Network.responseReceived', None)
        self._driver.set_listener('Network.loadingFinished', None)
        self._driver.set_listener('Network.loadingFailed', None)
        # self._driver.call_method('Network.disable')

    def _requestWillBeSent(self, **kwargs):
        """接收到请求时的回调函数"""
        for target in self._targets:
            if ((self._is_regex and search(target, kwargs['request']['url'])) or
                (not self._is_regex and target in kwargs['request']['url'])) and (
                    not self._method or kwargs['request']['method'] in self._method):
                self._request_ids[kwargs['requestId']] = DataPacket(self._page.tab_id, target, kwargs)

                if kwargs['request'].get('hasPostData', None) and not kwargs['request'].get('postData', None):
                    self._request_ids[kwargs['requestId']]._raw_post_data = \
                        self._page.run_cdp('Network.getRequestPostData', requestId=kwargs['requestId'])['postData']

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
                r = self._page.run_cdp('Network.getResponseBody', requestId=request_id)
                body = r['body']
                is_base64 = r['base64Encoded']
            except CDPError:
                body = ''
                is_base64 = False

            dp._raw_body = body
            dp._base64_body = is_base64

            self._tmp.put(dp)
            self._results.append(dp)
            self._caught_count += 1

    def _loading_failed(self, **kwargs):
        """请求失败时的回调方法"""
        request_id = kwargs['requestId']
        if request_id in self._request_ids:
            dp = self._request_ids[request_id]
            dp.errorText = kwargs['errorText']
            dp._resource_type = kwargs['type']

            self._tmp.put(dp)
            self._results.append(dp)
            self._caught_count += 1


class DataPacket(object):
    """返回的数据包管理类"""

    def __init__(self, tab, target, raw_request):
        """
        :param tab: 产生这个数据包的tab的id
        :param target: 监听目标
        :param raw_request: 原始request数据，从cdp获得
        """
        self.tab = tab
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
