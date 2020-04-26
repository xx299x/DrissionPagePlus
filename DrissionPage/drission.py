# -*- encoding: utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   drission.py
"""
from urllib.parse import urlparse

import tldextract
from requests_html import HTMLSession
from selenium import webdriver
from selenium.common.exceptions import WebDriverException
from selenium.webdriver.chrome.options import Options

from DrissionPage.config import global_driver_options, global_session_options


def _get_chrome_options(options: dict) -> Options:
    """ 从传入的字典获取浏览器设置，返回ChromeOptions对象"""
    chrome_options = webdriver.ChromeOptions()
    if 'debuggerAddress' in options:
        # 控制已打开的浏览器
        chrome_options.add_experimental_option('debuggerAddress', options['debuggerAddress'])
    else:
        if 'binary_location' in options and options['binary_location']:
            # 手动指定使用的浏览器位置
            chrome_options.binary_location = options['binary_location']
        if 'arguments' in options:
            # 启动参数
            if isinstance(options['arguments'], list):
                for arg in options['arguments']:
                    chrome_options.add_argument(arg)
            else:
                raise Exception(f'需要list，而非{type(options["arguments"])}')
        if 'extension_files' in options and options['extension_files']:
            # 加载插件
            if isinstance(options['extension_files'], list):
                for arg in options['extension_files']:
                    chrome_options.add_extension(arg)
            else:
                raise Exception(f'需要list，而非{type(options["extension_files"])}')
        if 'experimental_options' in options:
            # 实验性质的设置参数
            if isinstance(options['experimental_options'], dict):
                for i in options['experimental_options']:
                    chrome_options.add_experimental_option(i, options['experimental_options'][i])
            else:
                raise Exception(f'需要dict，而非{type(options["experimental_options"])}')

    return chrome_options


class Drission(object):
    """ Drission类整合了WebDriver对象和HTLSession对象，
    可按要求创建、关闭及同步cookies
    """

    def __init__(self, driver_options: dict = None, session_options: dict = None):
        self._driver = None
        self._session = None
        self._driver_options = driver_options if driver_options else global_driver_options
        self._session_options = session_options if session_options else global_session_options

    @property
    def session(self):
        """ 获取HTMLSession对象"""
        if self._session is None:
            self._session = HTMLSession()
        return self._session

    @property
    def driver(self):
        """ 获取WebDriver对象，按传入配置信息初始化"""
        if self._driver is None:
            if 'chromedriver_path' in self._driver_options:
                driver_path = self._driver_options['chromedriver_path']
            else:
                driver_path = 'chromedriver'
            self._driver = webdriver.Chrome(driver_path, options=_get_chrome_options(self._driver_options))
        return self._driver

    @property
    def session_options(self):
        return self._session_options

    def cookies_to_session(self, copy_user_agent: bool = False) -> None:
        """ 把driver的cookies复制到session"""
        if copy_user_agent:
            self.copy_user_agent_from_driver()
        for cookie in self.driver.get_cookies():
            self.session.cookies.set(cookie['name'], cookie['value'], domain=cookie['domain'])

    def cookies_to_driver(self, url: str):
        """ 把session的cookies复制到driver"""
        domain = urlparse(url).netloc
        if not domain:
            raise Exception('Without specifying a domain')

        # 翻译cookies
        for i in [x for x in self.session.cookies if domain in x.domain]:
            cookie_data = {'name': i.name, 'value': str(i.value), 'path': i.path, 'domain': i.domain}
            if i.expires:
                cookie_data['expiry'] = i.expires
            self.ensure_add_cookie(cookie_data)

    def ensure_add_cookie(self, cookie, override_domain=None) -> None:
        """ 添加cookie到driver"""
        if override_domain:
            cookie['domain'] = override_domain

        cookie_domain = cookie['domain'] if cookie['domain'][0] != '.' else cookie['domain'][1:]
        try:
            browser_domain = tldextract.extract(self.driver.current_url).fqdn
        except AttributeError:
            browser_domain = ''
        if cookie_domain not in browser_domain:
            self.driver.get(f'http://{cookie_domain.lstrip("http://")}')

        self.driver.add_cookie(cookie)

        # 如果添加失败，尝试更宽的域名
        if not self.is_cookie_in_driver(cookie):
            cookie['domain'] = tldextract.extract(cookie['domain']).registered_domain
            self.driver.add_cookie(cookie)
            if not self.is_cookie_in_driver(cookie):
                raise WebDriverException(f"Couldn't add the following cookie to the webdriver\n{cookie}\n")

    def is_cookie_in_driver(self, cookie) -> bool:
        """ 检查cookie是否已经在driver里
        只检查name、value、domain，检查domain时比较宽"""
        for driver_cookie in self.driver.get_cookies():
            if (cookie['name'] == driver_cookie['name'] and
                    cookie['value'] == driver_cookie['value'] and
                    (cookie['domain'] == driver_cookie['domain'] or
                     f'.{cookie["domain"]}' == driver_cookie['domain'])):
                return True
        return False

    def copy_user_agent_from_driver(self) -> None:
        """ 把driver的user-agent复制到session"""
        selenium_user_agent = self.driver.execute_script("return navigator.userAgent;")
        self.session.headers.update({"user-agent": selenium_user_agent})

    def close_driver(self) -> None:
        """ 关闭driver和浏览器"""
        self._driver.quit()
        self._driver = None

    def close_session(self) -> None:
        """ 关闭session"""
        self._session.close()
        self._session = None

    def close(self) -> None:
        """ 关闭session、driver和浏览器"""
        if self._driver:
            self.close_driver()
        if self._session:
            self.close_session()

    def __del__(self):
        self.close()
