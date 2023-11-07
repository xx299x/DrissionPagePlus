# -*- coding:utf-8 -*-
from .._commons.web import location_in_viewport
from ..errors import CDPError, NoRectError


class ChromiumElementStates(object):
    def __init__(self, ele):
        """
        :param ele: ChromiumElement
        """
        self._ele = ele

    @property
    def is_selected(self):
        """返回元素是否被选择"""
        return self._ele.run_js('return this.selected;')

    @property
    def is_checked(self):
        """返回元素是否被选择"""
        return self._ele.run_js('return this.checked;')

    @property
    def is_displayed(self):
        """返回元素是否显示"""
        return not (self._ele.style('visibility') == 'hidden'
                    or self._ele.run_js('return this.offsetParent === null;')
                    or self._ele.style('display') == 'none')

    @property
    def is_enabled(self):
        """返回元素是否可用"""
        return not self._ele.run_js('return this.disabled;')

    @property
    def is_alive(self):
        """返回元素是否仍在DOM中"""
        try:
            d = self._ele.attrs
            return True
        except Exception:
            return False

    @property
    def is_in_viewport(self):
        """返回元素是否出现在视口中，以元素click_point为判断"""
        x, y = self._ele.locations.click_point
        return location_in_viewport(self._ele.page, x, y) if x else False

    @property
    def is_whole_in_viewport(self):
        """返回元素是否整个都在视口内"""
        x1, y1 = self._ele.location
        w, h = self._ele.size
        x2, y2 = x1 + w, y1 + h
        return location_in_viewport(self._ele.page, x1, y1) and location_in_viewport(self._ele.page, x2, y2)

    @property
    def is_covered(self):
        """返回元素是否被覆盖，与是否在视口中无关"""
        lx, ly = self._ele.locations.click_point
        try:
            r = self._ele.page.run_cdp('DOM.getNodeForLocation', x=lx, y=ly)
        except CDPError:
            return False

        if r.get('backendNodeId') != self._ele.ids.backend_id:
            return True

        return False

    @property
    def has_rect(self):
        """返回元素是否拥有位置和大小，没有返回False，有返回大小元组"""
        try:
            return self._ele.size
        except NoRectError:
            return False


class ShadowRootStates(object):
    def __init__(self, ele):
        """
        :param ele: ChromiumElement
        """
        self._ele = ele

    @property
    def is_enabled(self):
        """返回元素是否可用"""
        return not self._ele.run_js('return this.disabled;')

    @property
    def is_alive(self):
        """返回元素是否仍在DOM中"""
        try:
            self._ele.page.run_cdp('DOM.describeNode', backendNodeId=self._ele.ids.backend_id)
            return True
        except Exception:
            return False
