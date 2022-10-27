# -*- coding:utf-8 -*-
# 问题：跨iframe查找元素可能出现同名元素如何解决
# 须用DOM.documentUpdated检测元素有效性


class ChromeElement(object):
    def __init__(self, page, node_id: str = None, obj_id: str = None):
        self.page = page
        if not node_id and not obj_id:
            raise TypeError('node_id或obj_id必须传入一个')

        if node_id:
            self._node_id = node_id
            self._obj_id = self._get_obj_id(node_id)
        else:
            self._node_id = self._get_node_id(obj_id)
            self._obj_id = obj_id

    @property
    def html(self):
        return self.page.driver.DOM.getOuterHTML(nodeId=self._node_id)['outerHTML']

    def ele(self, xpath: str):
        # todo: 引号记得转码
        js = f'''function(){{
        frame=this.contentDocument;
        return document.evaluate("{xpath}", frame, null, 9, null).singleNodeValue;
        }}'''
        r = self.page.driver.Runtime.callFunctionOn(functionDeclaration=js,
                                                    objectId=self._obj_id)['result'].get('objectId', None)
        return r if not r else _ele(self.page, obj_id=r)

    def click(self, by_js: bool = True):
        if by_js:
            js = 'function(){this.click();}'
            self.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=self._obj_id)

    def _get_obj_id(self, node_id):
        return self.page.driver.DOM.resolveNode(nodeId=node_id)['object']['objectId']

    def _get_node_id(self, obj_id):
        return self.page.driver.DOM.requestNode(objectId=obj_id)['nodeId']


def _ele(page, node_id=None, obj_id=None) -> ChromeElement:
    return ChromeElement(page=page, node_id=node_id, obj_id=obj_id)
