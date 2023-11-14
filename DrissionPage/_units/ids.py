# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""


class ShadowRootIds(object):
    def __init__(self, ele):
        self._ele = ele

    @property
    def node_id(self):
        """返回元素cdp中的node id"""
        return self._ele._node_id

    @property
    def obj_id(self):
        """返回元素js中的object id"""
        return self._ele._obj_id

    @property
    def backend_id(self):
        """返回backend id"""
        return self._ele._backend_id


class ElementIds(ShadowRootIds):
    @property
    def doc_id(self):
        """返回所在document的object id"""
        return self._ele._doc_id


class FrameIds(object):
    def __init__(self, frame):
        self._frame = frame

    @property
    def tab_id(self):
        """返回当前标签页id"""
        return self._frame._tab_id

    @property
    def backend_id(self):
        """返回cdp中的node id"""
        return self._frame._backend_id

    @property
    def obj_id(self):
        """返回frame元素的object id"""
        return self._frame.frame_ele.ids.obj_id

    @property
    def node_id(self):
        """返回cdp中的node id"""
        return self._frame.frame_ele.ids.node_id
