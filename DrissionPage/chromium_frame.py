# -*- coding:utf-8 -*-
from re import search
from typing import Union, Tuple, List
from urllib.parse import urlparse

from .chromium_element import ChromiumElement
from .chromium_base import ChromiumBase


class ChromiumFrame(object):
    def __init__(self, page: ChromiumBase, ele: ChromiumElement):
        self.page = page
        self._inner_ele = ele
        self._is_diff_domain = False
        self.frame_id = page.run_cdp('DOM.describeNode', nodeId=ele.node_id)['node'].get('frameId', None)

        # 有src属性，且域名和主框架不一样，为异域frame
        src = ele.attr('src')
        if src and urlparse(src).netloc != urlparse(page.url).netloc:
            self._is_diff_domain = True
            self.inner_page = ChromiumBase(page.address, self.frame_id, page.timeout)
            self.inner_page.set_page_load_strategy(self.page.page_load_strategy)
            self.inner_page.timeouts = self.page.timeouts

    def __repr__(self) -> str:
        attrs = self._inner_ele.attrs
        attrs = [f"{attr}='{attrs[attr]}'" for attr in attrs]
        return f'<ChromiumFrame {self._inner_ele.tag} {" ".join(attrs)}>'

    @property
    def tag(self) -> str:
        """返回元素tag"""
        return self._inner_ele.tag

    @property
    def url(self) -> str:
        """"""
        if self._is_diff_domain:
            return self.inner_page.url
        else:
            r = self.page.run_cdp('DOM.describeNode', nodeId=self._inner_ele.node_id)
            return r['node']['contentDocument']['documentURL']

    @property
    def html(self) -> str:
        """返回元素outerHTML文本"""
        if self._is_diff_domain:
            tag = self.tag
            out_html = self.page.run_cdp('DOM.getOuterHTML', nodeId=self._inner_ele.node_id)['outerHTML']
            in_html = self.inner_page.html
            sign = search(rf'<{tag}.*?>', out_html).group(0)
            return f'{sign}{in_html}</{tag}>'

        else:
            return self._inner_ele.html

    @property
    def title(self) -> str:
        d = self.inner_page if self._is_diff_domain else self._inner_ele
        ele = d.ele('xpath://title')
        return ele.text if ele else None

    @property
    def cookies(self):
        return self.inner_page.cookies if self._is_diff_domain else self.page.cookies

    @property
    def inner_html(self) -> str:
        """返回元素innerHTML文本"""
        return self.inner_page.html if self._is_diff_domain else self._inner_ele.inner_html

    @property
    def attrs(self) -> dict:
        return self._inner_ele.attrs

    @property
    def frame_size(self) -> dict:
        if self._is_diff_domain:
            return self.inner_page.size
        else:
            h = self._inner_ele.run_script('return this.contentDocument.body.scrollHeight;')
            w = self._inner_ele.run_script('return this.contentDocument.body.scrollWidth;')
            return {'height': h, 'width': w}

    @property
    def size(self) -> dict:
        """返回frame元素大小"""
        return self._inner_ele.size

    @property
    def obj_id(self) -> str:
        """返回js中的object id"""
        return self._inner_ele.obj_id

    @property
    def node_id(self) -> str:
        """返回cdp中的node id"""
        return self._inner_ele.node_id

    @property
    def location(self) -> dict:
        """返回frame元素左上角的绝对坐标"""
        return self._inner_ele.location

    @property
    def is_displayed(self) -> bool:
        """返回frame元素是否显示"""
        return self._inner_ele.is_displayed

    def get(self, url):
        self.page._get(url, False, None, None, None, self.frame_id)

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, ChromiumElement, 'ChromiumFrame'],
            timeout: float = None):
        d = self.inner_page if self._is_diff_domain else self._inner_ele
        return d.ele(loc_or_ele, timeout)

    def eles(self,
             loc_or_ele: Union[Tuple[str, str], str],
             timeout: float = None):
        d = self.inner_page if self._is_diff_domain else self._inner_ele
        return d.eles(loc_or_ele, timeout)

    # def s_ele(self, loc_or_ele: Union[Tuple[str, str], str, ChromiumElement] = None) \
    #         -> Union[SessionElement, str, None]:
    #     """查找第一个符合条件的元素以SessionElement形式返回，处理复杂页面时效率很高       \n
    #     :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串
    #     :return: SessionElement对象或属性、文本
    #     """
    #     if isinstance(loc_or_ele, ChromiumElement):
    #         return make_session_ele(loc_or_ele)
    #     else:
    #         return make_session_ele(self, loc_or_ele)
    #
    # def s_eles(self, loc_or_str: Union[Tuple[str, str], str] = None) -> List[Union[SessionElement, str]]:
    #     """查找所有符合条件的元素以SessionElement列表形式返回                       \n
    #     :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
    #     :return: SessionElement对象组成的列表
    #     """
    #     return make_session_ele(self, loc_or_str, single=False)

    def attr(self, attr: str) -> Union[str, None]:
        """返回frame元素attribute属性值                           \n
        :param attr: 属性名
        :return: 属性值文本，没有该属性返回None
        """
        return self._inner_ele.attr(attr)

    def set_attr(self, attr: str, value: str) -> None:
        """设置frame元素attribute属性          \n
        :param attr: 属性名
        :param value: 属性值
        :return: None
        """
        self._inner_ele.set_attr(attr, value)

    def remove_attr(self, attr: str) -> None:
        """删除frame元素attribute属性          \n
        :param attr: 属性名
        :return: None
        """
        self._inner_ele.remove_attr(attr)

    def parent(self, level_or_loc: Union[tuple, str, int] = 1) -> Union['ChromiumElement', None]:
        """返回上面某一级父元素，可指定层数或用查询语法定位              \n
        :param level_or_loc: 第几级父元素，或定位符
        :return: 上级元素对象
        """
        return self._inner_ele.parent(level_or_loc)

    def prev(self,
             filter_loc: Union[tuple, str] = '',
             index: int = 1,
             timeout: float = 0) -> Union['ChromiumElement', str, None]:
        """返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param index: 前面第几个查询结果元素
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素
        """
        return self._inner_ele.prev(filter_loc, index, timeout)

    def next(self,
             filter_loc: Union[tuple, str] = '',
             index: int = 1,
             timeout: float = 0) -> Union['ChromiumElement', str, None]:
        """返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param index: 后面第几个查询结果元素
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素
        """
        return self._inner_ele.next(filter_loc, index, timeout)

    def before(self,
               filter_loc: Union[tuple, str] = '',
               index: int = 1,
               timeout: float = None) -> Union['ChromiumElement', str, None]:
        """返回当前元素前面的一个元素，可指定筛选条件和第几个。查找范围不限兄弟元素，而是整个DOM文档        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param index: 前面第几个查询结果元素
        :param timeout: 查找元素的超时时间
        :return: 本元素前面的某个元素或节点
        """
        return self._inner_ele.before(filter_loc, index, timeout)

    def after(self,
              filter_loc: Union[tuple, str] = '',
              index: int = 1,
              timeout: float = None) -> Union['ChromiumElement', str, None]:
        """返回当前元素后面的一个元素，可指定筛选条件和第几个。查找范围不限兄弟元素，而是整个DOM文档        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param index: 后面第几个查询结果元素
        :param timeout: 查找元素的超时时间
        :return: 本元素后面的某个元素或节点
        """
        return self._inner_ele.after(filter_loc, index, timeout)

    def prevs(self,
              filter_loc: Union[tuple, str] = '',
              timeout: float = 0) -> List[Union['ChromiumElement', str]]:
        """返回前面全部兄弟元素或节点组成的列表，可用查询语法筛选        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素或节点文本组成的列表
        """
        return self._inner_ele.prevs(filter_loc, timeout)

    def nexts(self,
              filter_loc: Union[tuple, str] = '',
              timeout: float = 0) -> List[Union['ChromiumElement', str]]:
        """返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素或节点文本组成的列表
        """
        return self._inner_ele.nexts(filter_loc, timeout)

    def befores(self,
                filter_loc: Union[tuple, str] = '',
                timeout: float = None) -> List[Union['ChromiumElement', str]]:
        """返回当前元素后面符合条件的全部兄弟元素或节点组成的列表，可用查询语法筛选。查找范围不限兄弟元素，而是整个DOM文档        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 本元素前面的元素或节点组成的列表
        """
        return self._inner_ele.befores(filter_loc, timeout)
