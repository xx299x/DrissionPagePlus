# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from .._commons.settings import Settings
from ..errors import ElementNotFoundError


class NoneElement(object):
    def __init__(self, method=None, args=None):
        self.method = method
        self.args = args

    def __call__(self, *args, **kwargs):
        if Settings.NoneElement_value is None:
            raise ElementNotFoundError(None, self.method, self.args)
        else:
            return self

    def __getattr__(self, item):
        if Settings.NoneElement_value is None:
            raise ElementNotFoundError(None, self.method, self.args)
        elif item in ('ele', 's_ele', 'parent', 'child', 'next', 'prev', 'before',
                      'after', 'get_frame', 'shadow_root', 'sr'):
            return self
        else:
            if item in ('size', 'link', 'css_path', 'xpath', 'comments', 'texts', 'tag', 'html', 'inner_html',
                        'attrs', 'text', 'raw_text'):
                return Settings.NoneElement_value
            else:
                raise ElementNotFoundError(None, self.method, self.args)

    def __eq__(self, other):
        if other is None:
            return True

    def __bool__(self):
        return False

    def __repr__(self):
        return 'None'
