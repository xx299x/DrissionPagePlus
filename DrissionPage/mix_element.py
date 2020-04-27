# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   mix_page.py
"""
import re
from html import unescape
from time import sleep
from typing import Union

from requests_html import Element
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.select import Select
from selenium.webdriver.support.wait import WebDriverWait

from .config import global_tmp_path
from .session_page import _translate_loc


class MixElement(object):
    def __init__(self, ele: Union[WebElement, Element]):
        self._ele = ele

    @property
    def ele(self) -> Union[WebElement, Element]:
        """返回元素对象"""
        return self._ele

    @property
    def text(self) -> str:
        """元素内文本"""
        if isinstance(self._ele, Element):
            return unescape(self._ele.text).replace('\xa0', ' ')
        else:
            return unescape(self.attr('innerText')).replace('\xa0', ' ')

    @property
    def html(self) -> str:
        """元素innerHTML"""
        if isinstance(self._ele, Element):
            html = unescape(self._ele.html).replace('\xa0', ' ')
            r = re.match(r'<.*?>(.*)</.*?>', html, flags=re.DOTALL)
            return r.group(1)
        else:
            return unescape(self.attr('innerHTML')).replace('\xa0', ' ')

    @property
    def tag_name(self) -> str:
        """获取标签名"""
        if isinstance(self._ele, Element):
            html = unescape(self._ele.html).replace('\xa0', ' ')
            r = re.match(r'^<(.*?)\s+', html, flags=re.DOTALL)
            return r.group(1)
        else:
            return self._ele.tag_name

    def attr(self, attr) -> str:
        """获取属性值"""
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

    def find(self, loc: tuple, mode: str = None, show_errmsg: bool = True) -> Union[WebElement, Element, list, None]:
        """根据loc获取元素"""
        if isinstance(self._ele, Element):
            mode = mode if mode else 'single'
            if mode not in ['single', 'all']:
                raise ValueError("mode须在'single', 'all'中选择")
            loc_by, loc_str = _translate_loc(loc)
            msg = ele = None
            try:
                if mode == 'single':
                    msg = '未找到元素'
                    if loc_by == 'xpath':
                        ele = MixElement(self.ele.xpath(loc_str, first=True, _encoding='utf-8'))
                    else:
                        ele = MixElement(self.ele.find(loc_str, first=True, _encoding='utf-8'))
                elif mode == 'all':
                    msg = '未找到元素s'
                    if loc_by == 'xpath':
                        ele = self.ele.xpath(loc_str, first=False, _encoding='utf-8')
                    else:
                        ele = self.ele.find(loc_str, first=False, _encoding='utf-8')
                return ele
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
                    ele = MixElement(wait.until(EC.presence_of_all_elements_located(loc)))
                elif mode == 'visible':
                    msg = '元素不可见或不存在'
                    ele = wait.until(EC.visibility_of_element_located(loc))
                return ele
            except:
                if show_errmsg:
                    print(msg, loc)
                    raise

    def find_all(self, loc: tuple, show_errmsg: bool = True) -> list:
        """根据loc获取子元素列表"""
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

    def search_all(self, value: str) -> list:
        """根据内容获取元素列表"""
        return self.search(value, mode='all')

    # -----------------以下为d模式独占-------------------
    def click(self) -> bool:
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

    def input(self, value, clear: bool = True) -> bool:
        """输入文本"""
        try:
            if clear:
                self.run_script("arguments[0].value=''")
            self.ele.send_keys(value)
            return True
        except:
            raise

    def run_script(self, script: str):
        """运行js"""
        self.ele.parent.execute_script(script, self.ele)

    def submit(self):
        """提交表单"""
        self.ele.submit()

    def clear(self):
        """清空元素"""
        self.ele.clear()

    def is_selected(self) -> bool:
        """是否选中"""
        return self.ele.is_selected()

    def is_enabled(self) -> bool:
        """是否可用"""
        return self.ele.is_enabled()

    def is_displayed(self) -> bool:
        """是否可见"""
        return self.ele.is_displayed()

    @property
    def size(self):
        """元素大小"""
        return self.ele.size

    @property
    def location(self):
        """元素坐标"""
        return self.ele.location

    def screenshot(self, path: str = None, filename: str = None) -> str:
        """元素截图"""
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

    def select(self, text: str):
        """选择下拉列表"""
        ele = Select(self.ele)
        try:
            ele.select_by_visible_text(text)
            return True
        except:
            return False

    def set_attr(self, attr, value) -> bool:
        """设置元素属性"""
        try:
            self.run_script(f"arguments[0].{attr} = '{value}';")
            return True
        except:
            raise
