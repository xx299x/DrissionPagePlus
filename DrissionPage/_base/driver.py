# -*- coding: utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from json import dumps, loads, JSONDecodeError
from queue import Queue, Empty
from threading import Thread, Event
from time import perf_counter

from requests import get
from websocket import (WebSocketTimeoutException, WebSocketConnectionClosedException, create_connection,
                       WebSocketException)


class Driver(object):
    def __init__(self, tab_id, tab_type, address):
        """
        :param tab_id: 标签页id
        :param tab_type: 标签页类型
        :param address: 浏览器连接地址
        """
        self.id = tab_id
        self.address = address
        self.type = tab_type
        self._debug = False
        self.alert_flag = False  # 标记alert出现，跳过一条请求后复原

        self._websocket_url = f'ws://{address}/devtools/{tab_type}/{tab_id}'
        self._cur_id = 0
        self._ws = None

        self._recv_th = Thread(target=self._recv_loop)
        self._handle_event_th = Thread(target=self._handle_event_loop)
        self._recv_th.daemon = True
        self._handle_event_th.daemon = True

        self._stopped = Event()

        self.event_handlers = {}
        self.immediate_event_handlers = {}
        self.method_results = {}
        self.event_queue = Queue()

        self.start()

    def _send(self, message, timeout=None):
        """发送信息到浏览器，并返回浏览器返回的信息
        :param message: 发送给浏览器的数据
        :param timeout: 超时时间，为None表示无限
        :return: 浏览器返回的数据
        """
        self._cur_id += 1
        ws_id = self._cur_id
        message['id'] = ws_id
        message_json = dumps(message)

        if self._debug:
            if self._debug is True or (isinstance(self._debug, str) and
                                       message.get('method', '').startswith(self._debug)):
                print(f'发> {message_json}')
            elif isinstance(self._debug, (list, tuple, set)):
                for m in self._debug:
                    if message.get('method', '').startswith(m):
                        print(f'发> {message_json}')
                        break

        end_time = perf_counter() + timeout if timeout is not None else None
        self.method_results[ws_id] = Queue()
        try:
            self._ws.send(message_json)
            if timeout == 0:
                self.method_results.pop(ws_id, None)
                return {'id': ws_id, 'result': {}}

        except (OSError, WebSocketConnectionClosedException):
            self.method_results.pop(ws_id, None)
            return {'error': {'message': 'page closed'}}

        while not self._stopped.is_set():
            try:
                result = self.method_results[ws_id].get(timeout=.2)
                self.method_results.pop(ws_id, None)
                return result

            except Empty:
                if self.alert_flag and message['method'].startswith(('Input.', 'Runtime.')):
                    return {'error': {'message': 'alert exists.'}}

                if timeout is not None and perf_counter() > end_time:
                    self.method_results.pop(ws_id, None)
                    return {'error': {'message': 'alert exists.'}} \
                        if self.alert_flag else {'error': {'message': 'timeout'}}

                continue

    def _recv_loop(self):
        """接收浏览器信息的守护线程方法"""
        while not self._stopped.is_set():
            try:
                # self._ws.settimeout(1)
                msg_json = self._ws.recv()
                msg = loads(msg_json)
            except WebSocketTimeoutException:
                continue
            except (WebSocketException, OSError, WebSocketConnectionClosedException, JSONDecodeError):
                self.stop()
                return

            if self._debug:
                if self._debug is True or 'id' in msg or (isinstance(self._debug, str)
                                                          and msg.get('method', '').startswith(self._debug)):
                    print(f'<收 {msg_json}')
                elif isinstance(self._debug, (list, tuple, set)):
                    for m in self._debug:
                        if msg.get('method', '').startswith(m):
                            print(f'<收 {msg_json}')
                            break

            if 'method' in msg:
                if msg['method'].startswith('Page.javascriptDialog'):
                    self.alert_flag = msg['method'].endswith('Opening')
                function = self.immediate_event_handlers.get(msg['method'])
                if function:
                    Thread(target=function, kwargs=msg['params']).start()
                    # function(**msg['params'])
                else:
                    self.event_queue.put(msg)

            elif msg.get('id') in self.method_results:
                self.method_results[msg['id']].put(msg)

            elif self._debug:
                print(f'未知信息：{msg}')

    def _handle_event_loop(self):
        """当接收到浏览器信息，执行已绑定的方法"""
        while not self._stopped.is_set():
            try:
                event = self.event_queue.get(timeout=1)
            except Empty:
                continue

            function = self.event_handlers.get(event['method'])
            if function:
                function(**event['params'])

            self.event_queue.task_done()

    def run(self, _method, **kwargs):
        """执行cdp方法
        :param _method: cdp方法名
        :param args: cdp参数
        :param kwargs: cdp参数
        :return: 执行结果
        """
        if self._stopped.is_set():
            return {'error': 'page closed', 'type': 'page_closed'}

        timeout = kwargs.pop('_timeout', 15)
        result = self._send({'method': _method, 'params': kwargs}, timeout=timeout)
        if 'result' not in result and 'error' in result:
            return {'error': result['error']['message'], 'type': result.get('type', 'call_method_error'),
                    'method': _method, 'args': kwargs}

        return result['result']

    def start(self):
        """启动连接"""
        self._stopped.clear()
        self._ws = create_connection(self._websocket_url, enable_multithread=True, suppress_origin=True)
        self._recv_th.start()
        self._handle_event_th.start()
        return True

    def stop(self):
        """中断连接"""
        if self._stopped.is_set():
            return False

        self._stopped.set()
        if self._ws:
            self._ws.close()
            self._ws = None

        while not self.event_queue.empty():
            event = self.event_queue.get_nowait()
            function = self.event_handlers.get(event['method'])
            if function:
                try:
                    function(**event['params'])
                except:
                    pass

        self.event_handlers.clear()
        self.method_results.clear()
        self.event_queue.queue.clear()
        return True

    def set_callback(self, event, callback, immediate=False):
        """绑定cdp event和回调方法
        :param event: cdp event
        :param callback: 绑定到cdp event的回调方法
        :param immediate: 是否要立即处理的动作
        :return: None
        """
        handler = self.immediate_event_handlers if immediate else self.event_handlers
        if callback:
            handler[event] = callback
        else:
            handler.pop(event, None)

    def __str__(self):
        return f'<Driver {self.id}>'

    __repr__ = __str__


class BrowserDriver(Driver):
    BROWSERS = {}

    def __new__(cls, tab_id, tab_type, address, browser):
        if tab_id in cls.BROWSERS:
            return cls.BROWSERS[tab_id]
        return object.__new__(cls)

    def __init__(self, tab_id, tab_type, address, browser):
        if hasattr(self, '_created'):
            return
        self._created = True
        BrowserDriver.BROWSERS[tab_id] = self
        super().__init__(tab_id, tab_type, address)
        self.browser = browser

    def __repr__(self):
        return f'<BrowserDriver {self.id}>'

    def get(self, url):
        r = get(url, headers={'Connection': 'close'})
        r.close()
        return r

    def stop(self):
        super().stop()
        self.browser._on_quit()
