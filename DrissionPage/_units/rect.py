# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""


class TabRect(object):
    def __init__(self, page):
        self._page = page

    @property
    def window_state(self):
        """返回窗口状态：normal、fullscreen、maximized、 minimized"""
        return self._get_window_rect()['windowState']

    @property
    def window_location(self):
        """返回窗口在屏幕上的坐标，左上角为(0, 0)"""
        r = self._get_window_rect()
        if r['windowState'] in ('maximized', 'fullscreen'):
            return 0, 0
        return r['left'] + 7, r['top']

    @property
    def window_size(self):
        """返回窗口大小"""
        r = self._get_window_rect()
        if r['windowState'] == 'fullscreen':
            return r['width'], r['height']
        elif r['windowState'] == 'maximized':
            return r['width'] - 16, r['height'] - 16
        else:
            return r['width'] - 16, r['height'] - 7

    @property
    def page_location(self):
        """返回页面左上角在屏幕中坐标，左上角为(0, 0)"""
        w, h = self.viewport_location
        r = self._get_page_rect()['layoutViewport']
        return w - r['pageX'], h - r['pageY']

    @property
    def viewport_location(self):
        """返回视口在屏幕中坐标，左上角为(0, 0)"""
        w_bl, h_bl = self.window_location
        w_bs, h_bs = self.window_size
        w_vs, h_vs = self.viewport_size_with_scrollbar
        return w_bl + w_bs - w_vs, h_bl + h_bs - h_vs

    @property
    def page_size(self):
        """返回页面总宽高，格式：(宽, 高)"""
        r = self._get_page_rect()['contentSize']
        return r['width'], r['height']

    @property
    def viewport_size(self):
        """返回视口宽高，不包括滚动条，格式：(宽, 高)"""
        r = self._get_page_rect()['visualViewport']
        return r['clientWidth'], r['clientHeight']

    @property
    def viewport_size_with_scrollbar(self):
        """返回视口宽高，包括滚动条，格式：(宽, 高)"""
        r = self._page.run_js('return window.innerWidth.toString() + " " + window.innerHeight.toString();')
        w, h = r.split(' ')
        return int(w), int(h)

    def _get_page_rect(self):
        """获取页面范围信息"""
        return self._page.run_cdp_loaded('Page.getLayoutMetrics')

    def _get_window_rect(self):
        """获取窗口范围信息"""
        return self._page.browser.get_window_bounds(self._page.tab_id)


class FrameRect(object):
    """异域iframe使用"""

    def __init__(self, frame):
        self._frame = frame

    @property
    def viewport_location(self):
        """返回视口在屏幕中坐标，左上角为(0, 0)"""
        return self._frame.frame_ele.locations.screen_location

    @property
    def page_size(self):
        """返回页面总宽高，格式：(宽, 高)"""
        return self._frame.page_size

    @property
    def viewport_size(self):
        """返回视口宽高，不包括滚动条，格式：(宽, 高)"""
        return self._frame.frame_ele.size
