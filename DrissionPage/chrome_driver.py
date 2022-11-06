# -*- coding:utf-8 -*-
class ChromeDriver(object):
    def __init__(self,
                 address: str = 'localhost:9222',
                 path: str = 'chrome'):
        self.address = address[7:] if address.startswith('http://') else address
