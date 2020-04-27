#!/usr/bin/env python
# -*- coding:utf-8 -*-
import re
from html import unescape
from time import sleep
from typing import Union

from requests_html import Element
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.wait import WebDriverWait

from .config import global_tmp_path
from .session_page import _translate_loc


class MixElement(object):
    def __init__(self, ele: Union[WebElement, Element]):
        self._ele = ele

    @property
    def ele(self):
        return self._ele

    @property
    def text(self):
        if isinstance(self._ele, Element):
            return unescape(self._ele.text).replace('\xa0', ' ')
        else:
            return unescape(self.attr('innerText')).replace('\xa0', ' ')

    @property
    def html(self):
        if isinstance(self._ele, Element):
            html = unescape(self._ele.html).replace('\xa0', ' ')
            r = re.match(r'<.*?>(.*)</.*?>', html, flags=re.DOTALL)
            return r.group(1)
        else:
            return unescape(self.attr('innerHTML')).replace('\xa0', ' ')

    @property
    def tag_name(self):
        if isinstance(self._ele, Element):
            html = unescape(self._ele.html).replace('\xa0', ' ')
            r = re.match(r'^<(.*?)\s+', html, flags=re.DOTALL)
            return r.group(1)
        else:
            return self._ele.tag_name

    def attr(self, attr):
        if isinstance(self._ele, Element):
            try:
                if attr == 'href':
                    # 如直接获取attr只能获取相对地址
                    for link in self._ele.absolute_links:
                        return link
                elif attr == 'class':
                    class_str = ''
                    for key, i in enumerate(self._ele.attrs['class']):
                        class_str += ' ' if key > 0 else ''
                        class_str += i
                    return class_str
                else:
                    return self._ele.attrs[attr]
            except:
                return ''
        else:
            return self._ele.get_attribute(attr)

    def find(self, loc: tuple, mode: str = None, show_errmsg: bool = True):
        """根据loc获取元素"""
        if isinstance(self._ele, Element):
            mode = mode if mode else 'single'
            if mode not in ['single', 'all']:
                raise ValueError("mode须在'single', 'all'中选择")
            loc_by, loc_str = _translate_loc(loc)
            msg = first = None
            try:
                if mode == 'single':
                    msg = '未找到元素'
                    first = True
                elif mode == 'all':
                    msg = '未找到元素s'
                    first = False
                if loc_by == 'xpath':
                    ele = self.ele.xpath(loc_str, first=first, _encoding='utf-8')
                else:
                    ele = self.ele.find(loc_str, first=first, _encoding='utf-8')
                return MixElement(ele)
            except:
                if show_errmsg:
                    print(msg, loc)
                    raise
        else:  # d模式
            mode = mode if mode else 'single'
            if mode not in ['single', 'all', 'visible']:
                raise ValueError("mode须在'single', 'all', 'visible'中选择")
            msg = ele = None
            try:
                wait = WebDriverWait(self.ele.parent, timeout=10)
                if mode == 'single':
                    msg = '未找到元素'
                    ele = wait.until(EC.presence_of_element_located(loc))
                elif mode == 'all':
                    msg = '未找到元素s'
                    ele = wait.until(EC.presence_of_all_elements_located(loc))
                elif mode == 'visible':
                    msg = '元素不可见或不存在'
                    ele = wait.until(EC.visibility_of_element_located(loc))
                return MixElement(ele)
            except:
                if show_errmsg:
                    print(msg, loc)

    def find_all(self, loc: tuple, show_errmsg: bool = True):
        return self.find(loc, mode='all', show_errmsg=show_errmsg)

    def search(self, value: str, mode: str = None):
        """根据内容获取元素"""
        mode = mode if mode else 'single'
        if mode not in ['single', 'all']:
            raise ValueError("mode须在'single', 'all'中选择")
        if isinstance(self._ele, Element):
            try:
                if mode == 'single':
                    ele = self.ele.xpath(f'.//*[contains(text(),"{value}")]', first=True)
                    return MixElement(ele)
                elif mode == 'all':
                    eles = self.ele.xpath(f'.//*[contains(text(),"{value}")]')
                    return [MixElement(ele) for ele in eles]
            except:
                return None
        else:  # d模式
            try:
                loc = 'xpath', f'.//*[contains(text(),"{value}")]'
                wait = WebDriverWait(self.ele.parent, timeout=10)
                if mode == 'single':
                    ele = wait.until(EC.presence_of_element_located(loc))
                    return MixElement(ele)
                elif mode == 'all':
                    eles = wait.until(EC.presence_of_all_elements_located(loc))
                    return [MixElement(ele) for ele in eles]
            except:
                return None

    def search_all(self, value: str):
        return self.search(value, mode='all')

    # -----------------以下为d模式独占-------------------
    def click(self):
        """点击"""
        for _ in range(10):
            try:
                self.ele.click()
                return True
            except Exception as e:
                print(e)
                sleep(0.2)
        # 若点击失败，用js方式点击
        print('用js点击')
        try:
            self.run_script('arguments[0].click()')
            return True
        except:
            raise

    def input(self, value, clear: bool = True):
        try:
            if clear:
                self.run_script("arguments[0].value=''")
            self.ele.send_keys(value)
            return True
        except:
            raise

    def run_script(self, script: str):
        self.ele.parent.execute_script(script, self.ele)

    def submit(self):
        self.ele.submit()

    def clear(self):
        self.ele.clear()

    def is_selected(self):
        return self.ele.is_selected()

    def is_enabled(self):
        return self.ele.is_enabled()

    def is_displayed(self):
        return self.ele.is_displayed()

    @property
    def size(self):
        return self.ele.size

    @property
    def location(self):
        return self.ele.location

    def screenshot(self, path: str = None, filename: str = None):
        path = path if path else global_tmp_path
        name = filename if filename else self.tag_name
        # 等待元素加载完成
        if self.tag_name == 'img':
            js = 'return arguments[0].complete && typeof arguments[0].naturalWidth ' \
                 '!= "undefined" && arguments[0].naturalWidth > 0'
            while not self.run_script(js):
                pass
        img_path = f'{path}\\{name}.png'
        self.ele.screenshot(img_path)
        return img_path

    def select(self, value: str):
        pass

    def set_attr(self, attr, value):
        """设置元素属性"""
        try:
            self.run_script(f"arguments[0].{attr} = '{value}';")
            return True
        except:
            raise
