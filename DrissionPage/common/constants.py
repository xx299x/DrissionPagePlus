# -*- coding:utf-8 -*-
from .errors import NotElementFoundError

HANDLE_ALERT_METHOD = 'Page.handleJavaScriptDialog'
FRAME_ELEMENT = ('iframe', 'frame')
ERROR = 'error'


class NoneElement(object):
    _instance = None

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = super(NoneElement, cls).__new__(cls, *args, **kwargs)
        return cls._instance

    def __call__(self, *args, **kwargs):
        raise NotElementFoundError

    def __getattr__(self, item):
        raise NotElementFoundError

    def __eq__(self, other):
        if other is None:
            return True

    def __bool__(self):
        return False

    def __repr__(self):
        return 'None'
