# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""


class Locations(object):
    def __init__(self, ele):
        """
        :param ele: ChromiumElement
        """
        self._ele = ele

    @property
    def location(self):
        """返回元素左上角的绝对坐标"""
        cl = self.viewport_location
        return self._get_page_coord(cl[0], cl[1])

    @property
    def midpoint(self):
        """返回元素中间点的绝对坐标"""
        cl = self.viewport_midpoint
        return self._get_page_coord(cl[0], cl[1])

    @property
    def click_point(self):
        """返回元素接受点击的点的绝对坐标"""
        cl = self.viewport_click_point
        return self._get_page_coord(cl[0], cl[1])

    @property
    def viewport_location(self):
        """返回元素左上角在视口中的坐标"""
        m = self._get_viewport_rect('border')
        return m[0], m[1]

    @property
    def viewport_midpoint(self):
        """返回元素中间点在视口中的坐标"""
        m = self._get_viewport_rect('border')
        return m[0] + (m[2] - m[0]) // 2, m[3] + (m[5] - m[3]) // 2

    @property
    def viewport_click_point(self):
        """返回元素接受点击的点视口坐标"""
        m = self._get_viewport_rect('padding')
        return self.viewport_midpoint[0], m[1] + 3

    @property
    def screen_location(self):
        """返回元素左上角在屏幕上坐标，左上角为(0, 0)"""
        vx, vy = self._ele.page.rect.viewport_location
        ex, ey = self.viewport_location
        pr = self._ele.page.run_js('return window.devicePixelRatio;')
        return (vx + ex) * pr, (ey + vy) * pr

    @property
    def screen_midpoint(self):
        """返回元素中点在屏幕上坐标，左上角为(0, 0)"""
        vx, vy = self._ele.page.rect.viewport_location
        ex, ey = self.viewport_midpoint
        pr = self._ele.page.run_js('return window.devicePixelRatio;')
        return (vx + ex) * pr, (ey + vy) * pr

    @property
    def screen_click_point(self):
        """返回元素中点在屏幕上坐标，左上角为(0, 0)"""
        vx, vy = self._ele.page.rect.viewport_location
        ex, ey = self.viewport_click_point
        pr = self._ele.page.run_js('return window.devicePixelRatio;')
        return (vx + ex) * pr, (ey + vy) * pr

    @property
    def rect(self):
        """返回元素四个角坐标，顺序：坐上、右上、右下、左下，没有大小的元素抛出NoRectError"""
        vr = self._get_viewport_rect('border')
        r = self._ele.page.run_cdp_loaded('Page.getLayoutMetrics')['visualViewport']
        sx = r['pageX']
        sy = r['pageY']
        return [(vr[0] + sx, vr[1] + sy), (vr[2] + sx, vr[3] + sy),
                (vr[4] + sx, vr[5] + sy), (vr[6] + sx, vr[7] + sy)]

    @property
    def viewport_rect(self):
        """返回元素四个角视口坐标，顺序：坐上、右上、右下、左下，没有大小的元素抛出NoRectError"""
        r = self._get_viewport_rect('border')
        return [(r[0], r[1]), (r[2], r[3]), (r[4], r[5]), (r[6], r[7])]

    def _get_viewport_rect(self, quad):
        """按照类型返回在可视窗口中的范围
        :param quad: 方框类型，margin border padding
        :return: 四个角坐标
        """
        return self._ele.page.run_cdp('DOM.getBoxModel', backendNodeId=self._ele.ids.backend_id)['model'][quad]

    def _get_page_coord(self, x, y):
        """根据视口坐标获取绝对坐标"""
        r = self._ele.page.run_cdp_loaded('Page.getLayoutMetrics')['visualViewport']
        sx = r['pageX']
        sy = r['pageY']
        return x + sx, y + sy
