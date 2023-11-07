# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from time import perf_counter, sleep

from .._commons.constants import Settings
from .._commons.web import offset_scroll
from ..errors import CanNotClickError, CDPError


class Clicker(object):
    def __init__(self, ele):
        """
        :param ele: ChromiumElement
        """
        self._ele = ele

    def __call__(self, by_js=False, timeout=1):
        """点击元素
        如果遇到遮挡，可选择是否用js点击
        :param by_js: 是否用js点击，为None时先用模拟点击，遇到遮挡改用js，为True时直接用js点击，为False时只用模拟点击
        :param timeout: 模拟点击的超时时间，等待元素可见、不被遮挡、进入视口
        :return: 是否点击成功
        """
        return self.left(by_js, timeout)

    def left(self, by_js=False, timeout=1):
        """点击元素，可选择是否用js点击
        :param by_js: 是否用js点击，为None时先用模拟点击，遇到遮挡改用js，为True时直接用js点击，为False时只用模拟点击
        :param timeout: 模拟点击的超时时间，等待元素可见、不被遮挡、进入视口
        :return: 是否点击成功
        """
        if not by_js:  # 模拟点击
            can_click = False
            timeout = self._ele.page.timeout if timeout is None else timeout
            if timeout == 0 and self._ele.states.has_rect:
                self._ele.scroll.to_see()
                if self._ele.states.is_in_viewport and self._ele.states.is_enabled and self._ele.states.is_displayed:
                    can_click = True

            else:
                end_time = perf_counter() + timeout
                while not self._ele.states.has_rect and perf_counter() < end_time:
                    sleep(.001)
                if self._ele.states.has_rect:
                    self._ele.scroll.to_see()
                    while perf_counter() < end_time:
                        if (self._ele.states.is_in_viewport and self._ele.states.is_enabled
                                and self._ele.states.is_displayed):
                            can_click = True
                            break
                        sleep(.001)

            if not self._ele.states.has_rect or not self._ele.states.is_in_viewport:
                by_js = True

            elif can_click and (by_js is False or not self._ele.states.is_covered):
                vx, vy = self._ele.locations.click_point
                try:
                    r = self._ele.page.run_cdp('DOM.getNodeForLocation', x=vx, y=vy, includeUserAgentShadowDOM=True,
                                               ignorePointerEventsNone=True)
                    if r['backendNodeId'] != self._ele.ids.backend_id:
                        vx, vy = self._ele.locations.viewport_click_point
                    else:
                        vx, vy = self._ele.locations.viewport_click_point

                except CDPError:
                    vx, vy = self._ele.locations.viewport_midpoint

                self._click(vx, vy)
                return True

        if by_js is not False:
            self._ele.run_js('this.click();')
            return True
        if Settings.raise_when_click_failed:
            raise CanNotClickError
        return False

    def right(self):
        """右键单击"""
        self._ele.page.scroll.to_see(self._ele)
        x, y = self._ele.locations.viewport_click_point
        self._click(x, y, 'right')

    def middle(self):
        """中键单击"""
        self._ele.page.scroll.to_see(self._ele)
        x, y = self._ele.locations.viewport_click_point
        self._click(x, y, 'middle')

    def at(self, offset_x=None, offset_y=None, button='left', count=1):
        """带偏移量点击本元素，相对于左上角坐标。不传入x或y值时点击元素中间点
        :param offset_x: 相对元素左上角坐标的x轴偏移量
        :param offset_y: 相对元素左上角坐标的y轴偏移量
        :param button: 点击哪个键，可选 left, middle, right, back, forward
        :param count: 点击次数
        :return: None
        """
        self._ele.page.scroll.to_see(self._ele)
        if offset_x is None and offset_y is None:
            w, h = self._ele.size
            offset_x = w // 2
            offset_y = h // 2
        x, y = offset_scroll(self._ele, offset_x, offset_y)
        self._click(x, y, button, count)

    def twice(self):
        """双击元素"""
        self.at(count=2)

    def _click(self, client_x, client_y, button='left', count=1):
        """实施点击
        :param client_x: 视口中的x坐标
        :param client_y: 视口中的y坐标
        :param button: 'left' 'right' 'middle'  'back' 'forward'
        :param count: 点击次数
        :return: None
        """
        self._ele.page.run_cdp('Input.dispatchMouseEvent', type='mousePressed',
                               x=client_x, y=client_y, button=button, clickCount=count)
        # sleep(.05)
        self._ele.page.run_cdp('Input.dispatchMouseEvent', type='mouseReleased',
                               x=client_x, y=client_y, button=button)
