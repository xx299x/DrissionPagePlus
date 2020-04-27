# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   driver_page.py
"""
from html import unescape
from time import sleep
from typing import Union
from urllib import parse

from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.remote.webelement import WebElement
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import Select
from selenium.webdriver.support.wait import WebDriverWait


class DriverPage(object):
    """DriverPage封装了页面操作的常用功能，使用selenium来获取、解析、操作网页"""

    def __init__(self, driver: WebDriver, locs=None):
        """初始化函数，接收一个WebDriver对象，用来操作网页"""
        self._driver = driver
        self._locs = locs
        self._url = None
        self._url_available = None

    @property
    def driver(self) -> WebDriver:
        return self._driver

    @property
    def url(self) -> Union[str, None]:
        """当前网页url"""
        if not self._driver or not self._driver.current_url.startswith('http'):
            return None
        else:
            return self._driver.current_url

    @property
    def url_available(self) -> bool:
        """url有效性"""
        return self._url_available

    def get(self, url: str, params: dict = None, go_anyway: bool = False) -> Union[None, bool]:
        """跳转到url"""
        to_url = f'{url}?{parse.urlencode(params)}' if params else url
        if not url or (not go_anyway and self.url == to_url):
            return
        self._url = to_url
        self.driver.get(to_url)
        self._url_available = True if self.check_driver_url() else False
        return self._url_available

    @property
    def cookies(self) -> list:
        """返回当前网站cookies"""
        return self.driver.get_cookies()

    def get_title(self) -> str:
        """获取网页title"""
        return self._driver.title

    def _get_ele(self, loc_or_ele: Union[WebElement, tuple]) -> WebElement:
        """接收loc或元素实例，返回元素实例"""
        # ========================================
        # ** 必须与SessionPage类中同名函数保持一致 **
        # ========================================
        if isinstance(loc_or_ele, tuple):
            return self.find(loc_or_ele)
        return loc_or_ele

    def find(self, loc: tuple, mode: str = None, timeout: float = 10, show_errmsg: bool = True) \
            -> Union[WebElement, list]:
        """查找一个元素
        :param loc: 页面元素地址
        :param mode: 以某种方式查找元素，可选'single' , 'all', 'visible'
        :param timeout: 是否显示错误信息
        :param show_errmsg: 是否显示错误信息
        :return: 页面元素对象或列表
        """
        mode = mode if mode else 'single'
        if mode not in ['single', 'all', 'visible']:
            raise ValueError("mode须在'single', 'all', 'visible'中选择")
        msg = ele = None
        try:
            wait = WebDriverWait(self.driver, timeout=timeout)
            if mode == 'single':
                msg = '未找到元素'
                ele = wait.until(EC.presence_of_element_located(loc))
            elif mode == 'all':
                msg = '未找到元素s'
                ele = wait.until(EC.presence_of_all_elements_located(loc))
            elif mode == 'visible':
                msg = '元素不可见或不存在'
                ele = wait.until(EC.visibility_of_element_located(loc))
            return ele
        except:
            if show_errmsg:
                print(msg, loc)

    def find_all(self, loc: tuple, timeout: float = 10, show_errmsg=True) -> list:
        """查找符合条件的所有元素"""
        return self.find(loc, mode='all', timeout=timeout, show_errmsg=show_errmsg)

    def search(self, value: str, mode: str = None, timeout: float = 10) -> Union[WebElement, list, None]:
        """根据内容搜索元素
        :param value: 搜索内容
        :param mode: 可选'single','all'
        :param timeout: 超时时间
        :return: 页面元素对象
        """
        mode = mode if mode else 'single'
        if mode not in ['single', 'all']:
            raise ValueError("mode须在'single', 'all'中选择")
        ele = []
        try:
            loc = 'xpath', f'//*[contains(text(),"{value}")]'
            wait = WebDriverWait(self.driver, timeout=timeout)
            if mode == 'single':
                ele = wait.until(EC.presence_of_element_located(loc))
            elif mode == 'all':
                ele = wait.until(EC.presence_of_all_elements_located(loc))
            return ele
        except:
            if mode == 'single':
                return None
            elif mode == 'all':
                return []

    def search_all(self, value: str, timeout: float = 10) -> list:
        """根据内容搜索元素"""
        return self.search(value, mode='all', timeout=timeout)

    def get_attr(self, loc_or_ele: Union[WebElement, tuple], attr: str) -> str:
        """获取元素属性"""
        ele = self._get_ele(loc_or_ele)
        try:
            return ele.get_attribute(attr)
        except:
            return ''

    def get_html(self, loc_or_ele: Union[WebElement, tuple] = None) -> str:
        """获取元素innerHTML，如未指定元素则获取页面源代码"""
        if not loc_or_ele:
            return self.driver.find_element_by_xpath("//*").get_attribute("outerHTML")
        return unescape(self.get_attr(loc_or_ele, 'innerHTML')).replace('\xa0', ' ')

    def get_text(self, loc_or_ele: Union[WebElement, tuple]) -> str:
        """获取innerText"""
        return unescape(self.get_attr(loc_or_ele, 'innerText')).replace('\xa0', ' ')

    # ----------------以下为独有函数-----------------------

    def find_visible(self, loc: tuple, timeout: float = 10, show_errmsg: bool = True) -> WebElement:
        """查找一个可见元素"""
        return self.find(loc, mode='visible', timeout=timeout, show_errmsg=show_errmsg)

    def check_driver_url(self) -> bool:
        """由子类自行实现各页面的判定规则"""
        return True

    def input(self, loc_or_ele: Union[WebElement, tuple], value: str, clear: bool = True) -> bool:
        """向文本框填入文本"""
        ele = self._get_ele(loc_or_ele)
        try:
            if clear:
                self.run_script(ele, "arguments[0].value=''")
            ele.send_keys(value)
            return True
        except:
            raise

    def click(self, loc_or_ele: Union[WebElement, tuple]) -> bool:
        """点击一个元素"""
        ele = self._get_ele(loc_or_ele)
        if not ele:
            raise
        for _ in range(10):
            try:
                ele.click()
                return True
            except Exception as e:
                print(e)
                sleep(0.2)
        # 点击失败代表被遮挡，用js方式点击
        print(f'用js点击{loc_or_ele}')
        try:
            self.run_script(ele, 'arguments[0].click()')
            return True
        except:
            raise

    def set_attr(self, loc_or_ele: Union[WebElement, tuple], attribute: str, value: str) -> bool:
        """设置元素属性"""
        ele = self._get_ele(loc_or_ele)
        try:
            self.driver.execute_script(f"arguments[0].{attribute} = '{value}';", ele)
            return True
        except:
            raise

    def run_script(self, loc_or_ele: Union[WebElement, tuple], script: str) -> bool:
        """执行js脚本"""
        ele = self._get_ele(loc_or_ele)
        try:
            return self.driver.execute_script(script, ele)
        except:
            raise

    def get_tabs_sum(self) -> int:
        """获取标签页数量"""
        return len(self.driver.window_handles)

    def get_tab_num(self) -> int:
        """获取当前tab号码"""
        handle = self.driver.current_window_handle
        handle_list = self.driver.window_handles
        return handle_list.index(handle)

    def to_tab(self, index: int = 0) -> None:
        """跳转到第几个标签页，从0开始算"""
        tabs = self.driver.window_handles  # 获得所有标签页权柄
        self.driver.switch_to.window(tabs[index])

    def close_current_tab(self) -> None:
        """关闭当前标签页"""
        self.driver.close()

    def close_other_tabs(self, tab_index: int = None) -> None:
        """关闭其它标签页，没有传入序号代表保留当前页"""
        tabs = self.driver.window_handles  # 获得所有标签页权柄
        page_handle = tabs[tab_index] if tab_index >= 0 else self.driver.current_window_handle
        for i in tabs:  # 遍历所有标签页，关闭非保留的
            if i != page_handle:
                self.driver.switch_to.window(i)
                self.close_current_tab()
        self.driver.switch_to.window(page_handle)  # 把权柄定位回保留的页面

    def to_iframe(self, loc_or_ele: Union[str, tuple, WebElement] = 'main') -> bool:
        """跳转到iframe，若传入字符串main则跳转到最高级"""
        if loc_or_ele == 'main':
            self.driver.switch_to.default_content()
            return True
        else:
            ele = self._get_ele(loc_or_ele)
            try:
                self.driver.switch_to.frame(ele)
                return True
            except:
                raise

    def get_screen(self, loc_or_ele: Union[WebElement, tuple], path: str, file_name: str = None) -> str:
        """获取元素截图"""
        ele = self._get_ele(loc_or_ele)
        name = file_name if file_name else ele.tag_name
        # 等待元素加载完成
        js = 'return arguments[0].complete && typeof arguments[0].naturalWidth ' \
             '!= "undefined" && arguments[0].naturalWidth > 0'
        while not self.run_script(ele, js):
            pass
        img_path = f'{path}\\{name}.png'
        ele.screenshot(img_path)
        return img_path

    def scroll_to_see(self, loc_or_ele: Union[WebElement, tuple]) -> None:
        """滚动直到元素可见"""
        ele = self._get_ele(loc_or_ele)
        self.run_script(ele, "arguments[0].scrollIntoView();")

    def choose_select_list(self, loc_or_ele: Union[WebElement, tuple], text: str) -> bool:
        """选择下拉列表"""
        ele = Select(self._get_ele(loc_or_ele))
        try:
            ele.select_by_visible_text(text)
            return True
        except:
            return False

    def refresh(self) -> None:
        """刷新页面"""
        self.driver.refresh()

    def back(self) -> None:
        """后退"""
        self.driver.back()

    def set_window_size(self, x: int = None, y: int = None) -> None:
        """设置窗口大小，默认最大化"""
        if not x and not y:
            self.driver.maximize_window()
        else:
            new_x = x if x else self.driver.get_window_size()['width']
            new_y = y if y else self.driver.get_window_size()['height']
            self.driver.set_window_size(new_x, new_y)

    def close_driver(self) -> None:
        """关闭driver及浏览器"""
        self._driver.quit()
        self._driver = None
