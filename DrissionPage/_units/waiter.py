# -*- coding:utf-8 -*-
from time import sleep, perf_counter

from .._commons.constants import Settings
from ..errors import WaitTimeoutError


class ChromiumBaseWaiter(object):
    def __init__(self, page_or_ele):
        """
        :param page_or_ele: 页面对象或元素对象
        """
        self._driver = page_or_ele

    def __call__(self, second):
        """等待若干秒
        :param second: 秒数
        :return: None
        """
        sleep(second)

    def ele_delete(self, loc_or_ele, timeout=None, raise_err=None):
        """等待元素从DOM中删除
        :param loc_or_ele: 要等待的元素，可以是已有元素、定位符
        :param timeout: 超时时间，默认读取页面超时时间
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        ele = self._driver._ele(loc_or_ele, raise_err=False, timeout=0)
        return ele.wait.delete(timeout, raise_err=raise_err) if ele else True

    def ele_display(self, loc_or_ele, timeout=None, raise_err=None):
        """等待元素变成显示状态
        :param loc_or_ele: 要等待的元素，可以是已有元素、定位符
        :param timeout: 超时时间，默认读取页面超时时间
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        ele = self._driver._ele(loc_or_ele, raise_err=False, timeout=0)
        return ele.wait.display(timeout, raise_err=raise_err)

    def ele_hidden(self, loc_or_ele, timeout=None, raise_err=None):
        """等待元素变成隐藏状态
        :param loc_or_ele: 要等待的元素，可以是已有元素、定位符
        :param timeout: 超时时间，默认读取页面超时时间
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        ele = self._driver._ele(loc_or_ele, raise_err=False, timeout=0)
        return ele.wait.hidden(timeout, raise_err=raise_err)

    def ele_load(self, loc, timeout=None, raise_err=None):
        """等待元素加载到DOM
        :param loc: 要等待的元素，输入定位符
        :param timeout: 超时时间，默认读取页面超时时间
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 成功返回元素对象，失败返回False
        """
        ele = self._driver._ele(loc, raise_err=False, timeout=timeout)
        if ele:
            return True
        if raise_err is True or Settings.raise_when_wait_failed is True:
            raise WaitTimeoutError('等待元素加载失败。')
        else:
            return False

    def load_start(self, timeout=None, raise_err=None):
        """等待页面开始加载
        :param timeout: 超时时间，为None时使用页面timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        return self._loading(timeout=timeout, gap=.002, raise_err=raise_err)

    def load_complete(self, timeout=None, raise_err=None):
        """等待页面加载完成
        :param timeout: 超时时间，为None时使用页面timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        return self._loading(timeout=timeout, start=False, raise_err=raise_err)

    def upload_paths_inputted(self):
        """等待自动填写上传文件路径"""
        while self._driver._upload_list:
            sleep(.01)

    def download_begin(self, timeout=None, cancel_it=False):
        """等待浏览器下载开始，可将其拦截
        :param timeout: 超时时间，None使用页面对象超时时间
        :param cancel_it: 是否取消该任务
        :return: 成功返回任务对象，失败返回False
        """
        self._driver.browser._dl_mgr.set_flag(self._driver.tab_id, False if cancel_it else True)
        if timeout is None:
            timeout = self._driver.timeout

        r = False
        end_time = perf_counter() + timeout
        while perf_counter() < end_time:
            v = self._driver.browser._dl_mgr.get_flag(self._driver.tab_id)
            if not isinstance(v, bool):
                r = v
                break

        self._driver.browser._dl_mgr.set_flag(self._driver.tab_id, None)
        return r

    def url_change(self, text, exclude=False, timeout=None, raise_err=None):
        """等待url变成包含或不包含指定文本
        :param text: 用于识别的文本
        :param exclude: 是否排除，为True时当url不包含text指定文本时返回True
        :param timeout: 超时时间
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        return self._change('url', text, exclude, timeout, raise_err)

    def title_change(self, text, exclude=False, timeout=None, raise_err=None):
        """等待title变成包含或不包含指定文本
        :param text: 用于识别的文本
        :param exclude: 是否排除，为True时当title不包含text指定文本时返回True
        :param timeout: 超时时间
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        return self._change('title', text, exclude, timeout, raise_err)

    def data_packets(self, count=1, timeout=None, fix_count: bool = True):
        """等待符合要求的数据包到达指定数量
        :param count: 需要捕捉的数据包数量
        :param timeout: 超时时间，为None无限等待
        :param fix_count: 是否必须满足总数要求，发生超时，为True返回False，为False返回已捕捉到的数据包
        :return: count为1时返回数据包对象，大于1时返回列表，超时且fix_count为True时返回False"""
        return self._driver.listener.wait(count, timeout, fix_count)

    def _change(self, arg, text, exclude=False, timeout=None, raise_err=None):
        """等待指定属性变成包含或不包含指定文本
        :param arg: 要被匹配的属性
        :param text: 用于识别的文本
        :param exclude: 是否排除，为True时当属性不包含text指定文本时返回True
        :param timeout: 超时时间
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        if timeout is None:
            timeout = self._driver.timeout

        end_time = perf_counter() + timeout
        while perf_counter() < end_time:
            if arg == 'url':
                val = self._driver.url
            elif arg == 'title':
                val = self._driver.title
            else:
                raise ValueError
            if (not exclude and text in val) or (exclude and text not in val):
                return True
            sleep(.05)

        if raise_err is True or Settings.raise_when_wait_failed is True:
            raise WaitTimeoutError(f'等待{arg}改变失败。')
        else:
            return False

    def _loading(self, timeout=None, start=True, gap=.01, raise_err=None):
        """等待页面开始加载或加载完成
        :param timeout: 超时时间，为None时使用页面timeout属性
        :param start: 等待开始还是结束
        :param gap: 间隔秒数
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        if timeout != 0:
            if timeout is None or timeout is True:
                timeout = self._driver.timeout
            end_time = perf_counter() + timeout
            while perf_counter() < end_time:
                if self._driver.is_loading == start:
                    return True
                sleep(gap)

            if raise_err is True or Settings.raise_when_wait_failed is True:
                raise WaitTimeoutError('等待页面加载失败。')
            else:
                return False


class ChromiumTabWaiter(ChromiumBaseWaiter):

    def downloads_done(self, timeout=None, cancel_if_timeout=True):
        """等待所有浏览器下载任务结束
        :param timeout: 超时时间，为None时无限等待
        :param cancel_if_timeout: 超时时是否取消剩余任务
        :return: 是否等待成功
        """
        if not timeout:
            while self._driver.browser._dl_mgr.get_tab_missions(self._driver.tab_id):
                sleep(.5)
            return True

        else:
            end_time = perf_counter() + timeout
            while end_time > perf_counter():
                if not self._driver.browser._dl_mgr.get_tab_missions(self._driver.tab_id):
                    return True
                sleep(.5)

            if self._driver.browser._dl_mgr.get_tab_missions(self._driver.tab_id):
                if cancel_if_timeout:
                    for m in self._driver.browser._dl_mgr.get_tab_missions(self._driver.tab_id):
                        m.cancel()
                return False
            else:
                return True


class ChromiumPageWaiter(ChromiumTabWaiter):
    def __init__(self, page):
        super().__init__(page)
        # self._listener = None

    def new_tab(self, timeout=None, raise_err=None):
        """等待新标签页出现
        :param timeout: 等待超时时间，为None则使用页面对象timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等到新标签页出现
        """
        timeout = timeout if timeout is not None else self._driver.timeout
        end_time = perf_counter() + timeout
        while perf_counter() < end_time:
            if self._driver.tab_id != self._driver.latest_tab:
                return True
            sleep(.01)

        if raise_err is True or Settings.raise_when_wait_failed is True:
            raise WaitTimeoutError('等待新标签页失败。')
        else:
            return False

    def all_downloads_done(self, timeout=None, cancel_if_timeout=True):
        """等待所有浏览器下载任务结束
        :param timeout: 超时时间，为None时无限等待
        :param cancel_if_timeout: 超时时是否取消剩余任务
        :return: 是否等待成功
        """
        if not timeout:
            while self._driver.browser._dl_mgr._missions:
                sleep(.5)
            return True

        else:
            end_time = perf_counter() + timeout
            while end_time > perf_counter():
                if not self._driver.browser._dl_mgr._missions:
                    return True
                sleep(.5)

            if self._driver.browser._dl_mgr._missions:
                if cancel_if_timeout:
                    for m in list(self._driver.browser._dl_mgr._missions.values()):
                        m.cancel()
                return False
            else:
                return True


class ChromiumElementWaiter(object):
    """等待元素在dom中某种状态，如删除、显示、隐藏"""

    def __init__(self, page, ele):
        """等待元素在dom中某种状态，如删除、显示、隐藏
        :param page: 元素所在页面
        :param ele: 要等待的元素
        """
        self._page = page
        self._ele = ele

    def __call__(self, second):
        """等待若干秒
        :param second: 秒数
        :return: None
        """
        sleep(second)

    def delete(self, timeout=None, raise_err=None):
        """等待元素从dom删除
        :param timeout: 超时时间，为None使用元素所在页面timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        return self._wait_state('is_alive', False, timeout, raise_err)

    def display(self, timeout=None, raise_err=None):
        """等待元素从dom显示
        :param timeout: 超时时间，为None使用元素所在页面timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        return self._wait_state('is_displayed', True, timeout, raise_err)

    def hidden(self, timeout=None, raise_err=None):
        """等待元素从dom隐藏
        :param timeout: 超时时间，为None使用元素所在页面timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        return self._wait_state('is_displayed', False, timeout, raise_err)

    def covered(self, timeout=None, raise_err=None):
        """等待当前元素被遮盖
        :param timeout:超时时间，为None使用元素所在页面timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        return self._wait_state('is_covered', True, timeout, raise_err)

    def not_covered(self, timeout=None, raise_err=None):
        """等待当前元素被遮盖
        :param timeout:超时时间，为None使用元素所在页面timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        return self._wait_state('is_covered', False, timeout, raise_err)

    def enabled(self, timeout=None, raise_err=None):
        """等待当前元素变成可用
        :param timeout:超时时间，为None使用元素所在页面timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        return self._wait_state('is_enabled', True, timeout, raise_err)

    def disabled(self, timeout=None, raise_err=None):
        """等待当前元素变成可用
        :param timeout:超时时间，为None使用元素所在页面timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        return self._wait_state('is_enabled', False, timeout, raise_err)

    def disabled_or_delete(self, timeout=None, raise_err=None):
        """等待当前元素变成不可用或从DOM移除
        :param timeout:超时时间，为None使用元素所在页面timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        if timeout is None:
            timeout = self._page.timeout
        end_time = perf_counter() + timeout
        while perf_counter() < end_time:
            if not self._ele.states.is_enabled or not self._ele.states.is_alive:
                return True
            sleep(.05)

        if raise_err is True or Settings.raise_when_wait_failed is True:
            raise WaitTimeoutError('等待元素隐藏或删除失败。')
        else:
            return False

    def _wait_state(self, attr, mode=False, timeout=None, raise_err=None):
        """等待元素某个bool状态到达指定状态
        :param attr: 状态名称
        :param mode: True或False
        :param timeout: 超时时间，为None使用元素所在页面timeout属性
        :param raise_err: 等待失败时是否报错，为None时根据Settings设置
        :return: 是否等待成功
        """
        if timeout is None:
            timeout = self._page.timeout
        end_time = perf_counter() + timeout
        while perf_counter() < end_time:
            if self._ele.states.__getattribute__(attr) == mode:
                return True
            sleep(.05)

        if raise_err is True or Settings.raise_when_wait_failed is True:
            raise WaitTimeoutError('等待元素状态改变失败。')
        else:
            return False


class FrameWaiter(ChromiumBaseWaiter, ChromiumElementWaiter):
    def __init__(self, frame):
        """
        :param frame: ChromiumFrame对象
        """
        super().__init__(frame)
        super(ChromiumBaseWaiter, self).__init__(frame, frame.frame_ele)
