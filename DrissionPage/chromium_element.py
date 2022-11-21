# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   chromium_element.py
"""
from json import loads
from os import sep
from os.path import basename
from pathlib import Path
from re import search
from typing import Union, Tuple, List, Any
from time import perf_counter, sleep

from pychrome import Tab
from requests import Session
from requests.cookies import RequestsCookieJar

from .config import DriverOptions, _cookies_to_tuple
from .keys import _keys_to_typing, _keyDescriptionForString, _keyDefinitions
from .session_element import make_session_ele, SessionElement
from .base import DrissionElement, BaseElement, BasePage
from .common import make_absolute_link, get_loc, get_ele_txt, format_html, is_js_func, _location_in_viewport


class ChromiumElement(DrissionElement):
    """ChromePage页面对象中的元素对象"""

    def __init__(self, page, node_id: str = None, obj_id: str = None):
        """初始化，node_id和obj_id必须至少传入一个                       \n
        :param page: 元素所在ChromePage页面对象
        :param node_id: cdp中的node id
        :param obj_id: js中的object id
        """
        super().__init__(page)
        self._select = None
        self._scroll = None
        self._tag = None
        if not node_id and not obj_id:
            raise TypeError('node_id或obj_id必须传入一个。')

        if node_id:
            self._node_id = node_id
            self._obj_id = self._get_obj_id(node_id)
        else:
            self._node_id = self._get_node_id(obj_id)
            self._obj_id = obj_id

    def __repr__(self) -> str:
        attrs = [f"{attr}='{self.attrs[attr]}'" for attr in self.attrs]
        return f'<ChromiumElement {self.tag} {" ".join(attrs)}>'

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str],
                 timeout: float = None) -> Union['ChromiumElement', 'ChromiumFrame', str, None]:
        """在内部查找元素                                             \n
        例：ele2 = ele1('@id=ele_id')                               \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 超时时间
        :return: ChromiumElement对象或属性、文本
        """
        return self.ele(loc_or_str, timeout)

    @property
    def tag(self) -> str:
        """返回元素tag"""
        if self._tag is None:
            self._tag = self.page.driver.DOM.describeNode(nodeId=self._node_id)['node']['localName'].lower()
        return self._tag

    @property
    def html(self) -> str:
        """返回元素outerHTML文本"""
        tag = self.tag
        if tag in ('iframe', 'frame'):
            out_html = self.page.driver.DOM.getOuterHTML(nodeId=self._node_id)['outerHTML']
            in_html = self.inner_html
            sign = search(rf'<{tag}.*?>', out_html).group(0)
            return f'{sign}{in_html}</{tag}>'
        return self.page.driver.DOM.getOuterHTML(nodeId=self._node_id)['outerHTML']

    @property
    def inner_html(self) -> str:
        """返回元素innerHTML文本"""
        if self.tag in ('iframe', 'frame'):
            return self.run_script('return this.contentDocument.documentElement;').html
            # return self.run_script(self, 'this.contentDocument.body;').html
        return self.run_script('return this.innerHTML;')

    @property
    def attrs(self) -> dict:
        """返回元素所有attribute属性"""
        attrs = self.page.driver.DOM.getAttributes(nodeId=self._node_id)['attributes']
        attrs_len = len(attrs)
        return {attrs[i]: attrs[i + 1] for i in range(0, attrs_len, 2)}

    @property
    def text(self) -> str:
        """返回元素内所有文本，文本已格式化"""
        return get_ele_txt(make_session_ele(self.html))

    @property
    def raw_text(self) -> str:
        """返回未格式化处理的元素内文本"""
        return self.prop('innerText')

    # -----------------d模式独有属性-------------------
    @property
    def obj_id(self) -> str:
        """返回js中的object id"""
        return self._obj_id

    @property
    def node_id(self) -> str:
        """返回cdp中的node id"""
        return self._node_id

    @property
    def size(self) -> dict:
        """返回元素宽和高"""
        try:
            model = self.page.driver.DOM.getBoxModel(nodeId=self._node_id)['model']
            return {'height': model['height'], 'width': model['width']}
        except Exception:
            return {'height': 0, 'width': 0}

    @property
    def client_location(self) -> Union[dict, None]:
        """返回元素左上角在视口中的坐标"""
        m = self._get_client_rect('border')
        return {'x': m[0], 'y': m[1]} if m else None

    @property
    def client_midpoint(self) -> Union[dict, None]:
        """返回元素中间点在视口中的坐标"""
        m = self._get_client_rect('border')
        return {'x': m[2] - m[0], 'y': m[5] - m[1]} if m else None

    @property
    def location(self) -> Union[dict, None]:
        """返回元素左上角的绝对坐标"""
        cl = self.client_location
        return self._get_absolute_rect(cl['x'], cl['y']) if cl else None

    @property
    def midpoint(self) -> dict:
        """返回元素中间点的绝对坐标"""
        cl = self.client_midpoint
        return self._get_absolute_rect(cl['x'], cl['y']) if cl else None

    @property
    def _client_click_point(self) -> Union[dict, None]:
        """返回元素左上角可接受点击的点视口坐标"""
        m = self._get_client_rect('padding')
        return {'x': m[0], 'y': m[1]} if m else None

    @property
    def _click_point(self) -> Union[dict, None]:
        """返回元素左上角可接受点击的点的绝对坐标"""
        cl = self._client_click_point
        return self._get_absolute_rect(cl['x'], cl['y']) if cl else None

    @property
    def shadow_root(self) -> Union[None, 'ChromiumShadowRootElement']:
        """返回当前元素的shadow_root元素对象"""
        shadow = self.run_script('return this.shadowRoot;')
        return shadow

    @property
    def sr(self):
        """返回当前元素的shadow_root元素对象"""
        return self.shadow_root

    @property
    def pseudo_before(self) -> str:
        """返回当前元素的::before伪元素内容"""
        return self.style('content', 'before')

    @property
    def pseudo_after(self) -> str:
        """返回当前元素的::after伪元素内容"""
        return self.style('content', 'after')

    @property
    def scroll(self) -> 'ChromeScroll':
        """用于滚动滚动条的对象"""
        if self._scroll is None:
            self._scroll = ChromeScroll(self)
        return self._scroll

    def parent(self, level_or_loc: Union[tuple, str, int] = 1) -> Union['ChromiumElement', None]:
        """返回上面某一级父元素，可指定层数或用查询语法定位              \n
        :param level_or_loc: 第几级父元素，或定位符
        :return: 上级元素对象
        """
        return super().parent(level_or_loc)

    def prev(self,
             index: int = 1,
             filter_loc: Union[tuple, str] = '',
             timeout: float = 0) -> Union['ChromiumElement', str, None]:
        """返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param index: 前面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素
        """
        return super().prev(index, filter_loc, timeout)

    def next(self,
             index: int = 1,
             filter_loc: Union[tuple, str] = '',
             timeout: float = 0) -> Union['ChromiumElement', str, None]:
        """返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param index: 后面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素
        """
        return super().next(index, filter_loc, timeout)

    def before(self,
               index: int = 1,
               filter_loc: Union[tuple, str] = '',
               timeout: float = None) -> Union['ChromiumElement', str, None]:
        """返回当前元素前面的一个元素，可指定筛选条件和第几个。查找范围不限兄弟元素，而是整个DOM文档        \n
        :param index: 前面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 本元素前面的某个元素或节点
        """
        return super().before(index, filter_loc, timeout)

    def after(self,
              index: int = 1,
              filter_loc: Union[tuple, str] = '',
              timeout: float = None) -> Union['ChromiumElement', str, None]:
        """返回当前元素后面的一个元素，可指定筛选条件和第几个。查找范围不限兄弟元素，而是整个DOM文档        \n
        :param index: 后面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 本元素后面的某个元素或节点
        """
        return super().after(index, filter_loc, timeout)

    def prevs(self,
              filter_loc: Union[tuple, str] = '',
              timeout: float = 0) -> List[Union['ChromiumElement', str]]:
        """返回前面全部兄弟元素或节点组成的列表，可用查询语法筛选        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素或节点文本组成的列表
        """
        return super().prevs(filter_loc, timeout)

    def nexts(self,
              filter_loc: Union[tuple, str] = '',
              timeout: float = 0) -> List[Union['ChromiumElement', str]]:
        """返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素或节点文本组成的列表
        """
        return super().nexts(filter_loc, timeout)

    def befores(self,
                filter_loc: Union[tuple, str] = '',
                timeout: float = None) -> List[Union['ChromiumElement', str]]:
        """返回当前元素后面符合条件的全部兄弟元素或节点组成的列表，可用查询语法筛选。查找范围不限兄弟元素，而是整个DOM文档        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 本元素前面的元素或节点组成的列表
        """
        return super().befores(filter_loc, timeout)

    def wait_ele(self,
                 loc_or_ele: Union[str, tuple, 'ChromiumElement'],
                 timeout: float = None) -> 'ChromiumElementWaiter':
        """返回用于等待子元素到达某个状态的等待器对象                    \n
        :param loc_or_ele: 可以是元素、查询字符串、loc元组
        :param timeout: 等待超时时间
        :return: 用于等待的ElementWaiter对象
        """
        return ChromiumElementWaiter(self, loc_or_ele, timeout)

    @property
    def select(self) -> 'ChromeSelect':
        """返回专门处理下拉列表的Select类，非下拉列表元素返回False"""
        if self._select is None:
            if self.tag != 'select':
                self._select = False
            else:
                self._select = ChromeSelect(self)

        return self._select

    @property
    def is_selected(self) -> bool:
        """返回元素是否被选择"""
        return self.run_script('return this.selected;')

    @property
    def is_displayed(self) -> bool:
        """返回元素是否显示"""
        return not (self.style('visibility') == 'hidden'
                    or self.run_script('return this.offsetParent === null;')
                    or self.style('display') == 'none')

    @property
    def is_enabled(self) -> bool:
        """返回元素是否可用"""
        return not self.run_script('return this.disabled;')

    @property
    def is_alive(self) -> bool:
        """返回元素是否仍在DOM中"""
        try:
            d = self.attrs
            return True
        except Exception:
            return False

    @property
    def is_in_viewport(self) -> bool:
        """返回元素是否出现在视口中，以元素可以接受点击的点为判断"""
        loc = self.location
        return _location_in_viewport(self.page, loc['x'], loc['y'])

    def attr(self, attr: str) -> Union[str, None]:
        """返回attribute属性值                           \n
        :param attr: 属性名
        :return: 属性值文本，没有该属性返回None
        """
        attrs = self.attrs
        if attr == 'href':  # 获取href属性时返回绝对url
            link = attrs.get('href', None)
            if not link or link.lower().startswith(('javascript:', 'mailto:')):
                return link
            else:
                return make_absolute_link(link, self.page)

        elif attr == 'src':
            return make_absolute_link(attrs.get('src', None), self.page)

        elif attr == 'text':
            return self.text

        elif attr == 'innerText':
            return self.raw_text

        elif attr in ('html', 'outerHTML'):
            return self.html

        elif attr == 'innerHTML':
            return self.inner_html

        else:
            return attrs.get(attr, None)

    def set_attr(self, attr: str, value: str) -> None:
        """设置元素attribute属性          \n
        :param attr: 属性名
        :param value: 属性值
        :return: None
        """
        self.run_script(f'this.setAttribute(arguments[0], arguments[1]);', False, attr, str(value))

    def remove_attr(self, attr: str) -> None:
        """删除元素attribute属性          \n
        :param attr: 属性名
        :return: None
        """
        self.run_script(f'this.removeAttribute("{attr}");')

    def prop(self, prop: str) -> Union[str, int, None]:
        """获取property属性值            \n
        :param prop: 属性名
        :return: 属性值文本
        """
        p = self.page.driver.Runtime.getProperties(objectId=self._obj_id)['result']
        for i in p:
            if i['name'] == prop:
                if 'value' not in i or 'value' not in i['value']:
                    return None

                return format_html(i['value']['value'])

    def set_prop(self, prop: str, value: str) -> None:
        """设置元素property属性          \n
        :param prop: 属性名
        :param value: 属性值
        :return: None
        """
        value = value.replace('"', r'\"')
        self.run_script(f'this.{prop}="{value}";')

    def set_innerHTML(self, html: str) -> None:
        """设置元素innerHTML        \n
        :param html: html文本
        :return: None
        """
        self.set_prop('innerHTML', html)

    def run_script(self, script: str, as_expr: bool = False, *args: Any) -> Any:
        """运行javascript代码                                                 \n
        :param script: js文本
        :param as_expr: 是否作为表达式运行，为True时args无效
        :param args: 参数，按顺序在js文本中对应argument[0]、argument[2]...
        :return: 运行的结果
        """
        return _run_script(self, script, as_expr, self.page.timeouts.script, args)

    def run_async_script(self, script: str, as_expr: bool = False, *args: Any) -> None:
        """以异步方式执行js代码                                                 \n
        :param script: js文本
        :param as_expr: 是否作为表达式运行，为True时args无效
        :param args: 参数，按顺序在js文本中对应argument[0]、argument[2]...
        :return: None
        """
        from threading import Thread
        Thread(target=_run_script, args=(self, script, as_expr, self.page.timeouts.script, args)).start()

    def ele(self,
            loc_or_str: Union[Tuple[str, str], str],
            timeout: float = None) -> Union['ChromiumElement', 'ChromiumFrame', str, None]:
        """返回当前元素下级符合条件的第一个元素、属性或节点文本                 \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与元素所在页面等待时间一致
        :return: ChromiumElement对象或属性、文本
        """
        return self._ele(loc_or_str, timeout)

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None) -> List[Union['ChromiumElement', 'ChromiumFrame', str]]:
        """返回当前元素下级所有符合条件的子元素、属性或节点文本                 \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与元素所在页面等待时间一致
        :return: ChromiumElement对象或属性、文本组成的列表
        """
        return self._ele(loc_or_str, timeout=timeout, single=False)

    def s_ele(self, loc_or_str: Union[Tuple[str, str], str] = None) -> Union[SessionElement, str, None]:
        """查找第一个符合条件的元素以SessionElement形式返回，处理复杂页面时效率很高        \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        if self.tag in ('iframe', 'frame'):
            return make_session_ele(self.inner_html, loc_or_str)
        return make_session_ele(self, loc_or_str)

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str] = None) -> List[Union[SessionElement, str]]:
        """查找所有符合条件的元素以SessionElement列表形式返回                         \n
        :param loc_or_str: 定位符
        :return: SessionElement或属性、文本组成的列表
        """
        if self.tag in ('iframe', 'frame'):
            return make_session_ele(self.inner_html, loc_or_str, single=False)
        return make_session_ele(self, loc_or_str, single=False)

    def _ele(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None, single: bool = True, relative=False) \
            -> Union['ChromiumElement', 'ChromiumFrame', str, None,
                     List[Union['ChromiumElement', 'ChromiumFrame', str]]]:
        """返回当前元素下级符合条件的子元素、属性或节点文本，默认返回第一个                                      \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间
        :param single: True则返回第一个，False则返回全部
        :return: ChromiumElement对象或文本、属性或其组成的列表
        """
        return make_chromium_ele(self, loc_or_str, single, timeout, relative=relative)

    def style(self, style: str, pseudo_ele: str = '') -> str:
        """返回元素样式属性值，可获取伪元素属性值                \n
        :param style: 样式属性名称
        :param pseudo_ele: 伪元素名称（如有）
        :return: 样式属性的值
        """
        if pseudo_ele:
            pseudo_ele = f', "{pseudo_ele}"' if pseudo_ele.startswith(':') else f', "::{pseudo_ele}"'
        js = f'return window.getComputedStyle(this{pseudo_ele}).getPropertyValue("{style}");'
        return self.run_script(js)

    def save(self, path: [str, bool] = None, rename: str = None) -> Union[bytes, str, bool]:
        """保存图片或其它有src属性的元素的资源                                \n
        :param path: 文件保存路径，为None时保存到当前文件夹，为False时不保存
        :param rename: 文件名称，为None时从资源url获取
        :return: 资源内容文本
        """
        src = self.attr('src')
        if not src:
            return False
        if self.tag == 'img':  # 等待图片加载完成
            js = ('return this.complete && typeof this.naturalWidth != "undefined" '
                  '&& this.naturalWidth > 0 && typeof this.naturalHeight != "undefined" '
                  '&& this.naturalHeight > 0')
            end_time = perf_counter() + self.page.timeout
            while not self.run_script(js) and perf_counter() < end_time:
                sleep(.1)

        path = path or '.'

        node = self.page.driver.DOM.describeNode(nodeId=self._node_id)['node']
        frame = node.get('frameId', None)
        frame = frame or self.page.tab_id
        result = self.page.driver.Page.getResourceContent(frameId=frame, url=src)
        if result['base64Encoded']:
            from base64 import b64decode
            data = b64decode(result['content'])
            write_type = 'wb'
        else:
            data = result['content']
            write_type = 'w'

        if path:
            rename = rename or basename(src)
            Path(path).mkdir(parents=True, exist_ok=True)
            with open(f'{path}{sep}{rename}', write_type) as f:
                f.write(data)

        return data

    def get_screenshot(self, path: [str, Path] = None,
                       as_bytes: [bool, str] = None) -> Union[str, bytes]:
        """对当前元素截图                                                                            \n
        :param path: 完整路径，后缀可选'jpg','jpeg','png','webp'
        :param as_bytes: 是否已字节形式返回图片，可选'jpg','jpeg','png','webp'，生效时path参数无效
        :return: 图片完整路径或字节文本
        """
        if self.tag == 'img':  # 等待图片加载完成
            js = ('return this.complete && typeof this.naturalWidth != "undefined" '
                  '&& this.naturalWidth > 0 && typeof this.naturalHeight != "undefined" '
                  '&& this.naturalHeight > 0')
            end_time = perf_counter() + self.page.timeout
            while not self.run_script(js) and perf_counter() < end_time:
                sleep(.1)

        left, top = self.location.values()
        height, width = self.size.values()
        left_top = (left, top)
        right_bottom = (left + width, top + height)
        return self.page.get_screenshot(path, as_bytes=as_bytes, full_page=False,
                                        left_top=left_top, right_bottom=right_bottom)

    def input(self, vals: Union[str, tuple, list],
              clear: bool = True) -> None:
        """输入文本或组合键，也可用于输入文件路径到input元素（文件间用\n间隔）                          \n
        :param vals: 文本值或按键组合
        :param clear: 输入前是否清空文本框
        :return: None
        """
        if self.tag == 'input' and self.attr('type') == 'file':
            return self._set_file_input(vals)

        try:
            self.page.driver.DOM.focus(nodeId=self._node_id)
        except Exception:
            self.click(by_js=True)

        if clear:
            self.clear(by_js=True)

        # ------------处理字符-------------
        if not isinstance(vals, (tuple, list)):
            vals = (str(vals),)
        modifier, vals = _keys_to_typing(vals)

        if modifier != 0:  # 包含修饰符
            for key in vals:
                _send_key(self, modifier, key)
            return

        if vals.endswith('\n'):
            self.page.run_cdp('Input.insertText', text=vals[:-1])
            _send_key(self, modifier, '\n')
        else:
            self.page.run_cdp('Input.insertText', text=vals)

    def _set_file_input(self, files: Union[str, list, tuple]) -> None:
        """设置上传控件值
        :param files: 文件路径列表或字符串，字符串时多个文件用回车分隔
        :return: None
        """
        if isinstance(files, str):
            files = files.split('\n')
        self.page.driver.DOM.setFileInputFiles(files=files, nodeId=self._node_id)

    def clear(self, by_js: bool = False) -> None:
        """清空元素文本                                    \n
        :param by_js: 是否用js方式清空
        :return: None
        """
        if by_js:
            self.run_script("this.value='';")

        else:
            self.input(('\ue009', 'a', '\ue017'), clear=False)

    def click(self, by_js: bool = None, retry: bool = False, timeout: float = .2) -> bool:
        """点击元素                                                                      \n
        如果遇到遮挡，会重新尝试点击直到超时，若都失败就改用js点击                                \n
        :param by_js: 是否用js点击，为True时直接用js点击，为False时重试失败也不会改用js
        :param retry: 遇到其它元素遮挡时，是否重试
        :param timeout: 尝试点击的超时时间，不指定则使用父页面的超时时间，retry为True时才生效
        :return: 是否点击成功
        """

        def do_it(cx, cy, lx, ly) -> Union[None, bool]:
            """无遮挡返回True，有遮挡返回False，无元素返回None"""
            try:
                r = self.page.driver.DOM.getNodeForLocation(x=lx, y=ly)
            except Exception:
                return None

            if retry and r.get('nodeId') != self._node_id:
                return False

            self._click(cx, cy)
            return True

        if not by_js:
            self.page.scroll_to_see(self)
            if self.is_in_viewport:
                client_point = self._client_click_point
                if client_point:
                    loc_point = self._click_point
                    client_x = client_point['x']
                    client_y = client_point['y']
                    loc_x = loc_point['x']
                    loc_y = loc_point['y']

                    click = do_it(client_x, client_y, loc_x, loc_y)
                    if click:
                        return True

                    timeout = timeout if timeout is not None else self.page.timeout
                    end_time = perf_counter() + timeout
                    while click is False and perf_counter() < end_time:
                        click = do_it(client_x, client_y, loc_x, loc_y)

                    if click is not None:
                        return True

        if by_js is not False:
            self.run_script('this.click();')
            return True

        return False

    def click_at(self,
                 offset_x: Union[int, str] = None,
                 offset_y: Union[int, str] = None,
                 button: str = 'left') -> None:
        """带偏移量点击本元素，相对于左上角坐标。不传入x或y值时点击元素左上角可接受点击的点    \n
        :param offset_x: 相对元素左上角坐标的x轴偏移量
        :param offset_y: 相对元素左上角坐标的y轴偏移量
        :param button: 左键还是右键
        :return: None
        """
        x, y = _offset_scroll(self, offset_x, offset_y)
        self._click(x, y, button)

    def r_click(self) -> None:
        """右键单击"""
        self.page.scroll_to_see(self)
        xy = self._client_click_point
        self._click(xy['x'], xy['y'], 'right')

    def r_click_at(self, offset_x: Union[int, str] = None, offset_y: Union[int, str] = None) -> None:
        """带偏移量右键单击本元素，相对于左上角坐标。不传入x或y值时点击元素中点    \n
        :param offset_x: 相对元素左上角坐标的x轴偏移量
        :param offset_y: 相对元素左上角坐标的y轴偏移量
        :return: None
        """
        self.click_at(offset_x, offset_y, 'right')

    def _click(self, client_x: int, client_y: int, button: str = 'left') -> None:
        """实施点击                        \n
        :param client_x: 视口中的x坐标
        :param client_y: 视口中的y坐标
        :param button: 'left'或'right'
        :return: None
        """
        self.page.driver.Input.dispatchMouseEvent(type='mousePressed', x=client_x, y=client_y, button=button,
                                                  clickCount=1)
        sleep(.1)
        self.page.driver.Input.dispatchMouseEvent(type='mouseReleased', x=client_x, y=client_y, button=button)

    def hover(self, offset_x: int = None, offset_y: int = None) -> None:
        """鼠标悬停，可接受偏移量，偏移量相对于元素左上角坐标。不传入x或y值时悬停在元素中点    \n
        :param offset_x: 相对元素左上角坐标的x轴偏移量
        :param offset_y: 相对元素左上角坐标的y轴偏移量
        :return: None
        """
        x, y = _offset_scroll(self, offset_x, offset_y)
        self.page.driver.Input.dispatchMouseEvent(type='mouseMoved', x=x, y=y)

    def drag(self, offset_x: int = 0, offset_y: int = 0, speed: int = 40, shake: bool = True) -> None:
        """拖拽当前元素到相对位置                   \n
        :param offset_x: x变化值
        :param offset_y: y变化值
        :param speed: 拖动的速度，传入0即瞬间到达
        :param shake: 是否随机抖动
        :return: None
        """
        curr_xy = self.midpoint
        offset_x += curr_xy['x']
        offset_y += curr_xy['y']
        self.drag_to((offset_x, offset_y), speed, shake)

    def drag_to(self,
                ele_or_loc: Union[tuple, 'ChromiumElement'],
                speed: int = 40,
                shake: bool = True) -> None:
        """拖拽当前元素，目标为另一个元素或坐标元组                     \n
        :param ele_or_loc: 另一个元素或坐标元组，坐标为元素中点的坐标
        :param speed: 拖动的速度，传入0即瞬间到达
        :param shake: 是否随机抖动
        :return: None
        """
        # x, y：目标点坐标
        if isinstance(ele_or_loc, ChromiumElement):
            midpoint = ele_or_loc.midpoint
            target_x = midpoint['x']
            target_y = midpoint['y']
        elif isinstance(ele_or_loc, (list, tuple)):
            target_x, target_y = ele_or_loc
        else:
            raise TypeError('需要ChromiumElement对象或坐标。')

        curr_xy = self.midpoint
        current_x = curr_xy['x']
        current_y = curr_xy['y']
        width = target_x - current_x
        height = target_y - current_y
        num = 0 if not speed else int(((abs(width) ** 2 + abs(height) ** 2) ** .5) // speed)

        # 将要经过的点存入列表
        points = [(int(current_x + i * (width / num)), int(current_y + i * (height / num))) for i in range(1, num)]
        points.append((target_x, target_y))

        from .action_chains import ActionChains
        from random import randint
        actions = ActionChains(self.page)
        actions.hold(self)

        # 逐个访问要经过的点
        for x, y in points:
            if shake:
                x += randint(-3, 4)
                y += randint(-3, 4)
            actions.move(x - current_x, y - current_y)
            current_x, current_y = x, y
        actions.release()

    def _get_obj_id(self, node_id) -> str:
        """根据传入node id获取js中的object id          \n
        :param node_id: cdp中的node id
        :return: js中的object id
        """
        return self.page.driver.DOM.resolveNode(nodeId=node_id)['object']['objectId']

    def _get_node_id(self, obj_id) -> str:
        """根据传入object id获取cdp中的node id          \n
        :param obj_id: js中的object id
        :return: cdp中的node id
        """
        return self.page.driver.DOM.requestNode(objectId=obj_id)['nodeId']

    def _get_ele_path(self, mode) -> str:
        """返获取css路径或xpath路径"""
        if mode == 'xpath':
            txt1 = 'var tag = el.nodeName.toLowerCase();'
            # txt2 = '''return '//' + tag + '[@id="' + el.id + '"]'  + path;'''
            txt3 = ''' && sib.nodeName.toLowerCase()==tag'''
            txt4 = '''
            if(nth>1){path = '/' + tag + '[' + nth + ']' + path;}
                    else{path = '/' + tag + path;}'''
            txt5 = '''return path;'''

        elif mode == 'css':
            txt1 = ''
            # txt2 = '''return '#' + el.id + path;'''
            txt3 = ''
            txt4 = '''path = '>' + ":nth-child(" + nth + ")" + path;'''
            txt5 = '''return path.substr(1);'''

        else:
            raise ValueError(f"mode参数只能是'xpath'或'css'，现在是：'{mode}'。")

        js = '''function(){
        function e(el) {
            if (!(el instanceof Element)) return;
            var path = '';
            while (el.nodeType === Node.ELEMENT_NODE) {
                ''' + txt1 + '''
                    var sib = el, nth = 0;
                    while (sib) {
                        if(sib.nodeType === Node.ELEMENT_NODE''' + txt3 + '''){nth += 1;}
                        sib = sib.previousSibling;
                    }
                    ''' + txt4 + '''
                el = el.parentNode;
            }
            ''' + txt5 + '''
        }
        return e(this);}
        '''
        t = self.run_script(js)
        return f':root{t}' if mode == 'css' else t

    def _get_client_rect(self, quad: str) -> Union[dict, None]:
        """按照类型返回坐标
        :param quad: 方框类型，margin border padding
        :return: 四个角坐标，大小为0时返回None
        """
        try:
            return self.page.run_cdp('DOM.getBoxModel', nodeId=self.node_id)['model'][quad]
        except:
            return None

    def _get_absolute_rect(self, x, y) -> dict:
        """根据绝对坐标获取窗口坐标"""
        js = 'return document.documentElement.scrollLeft+" "+document.documentElement.scrollTop;'
        xy = self.run_script(js)
        sx, sy = xy.split(' ')
        return {'x': x + int(sx), 'y': y + int(sy)}


class ChromiumShadowRootElement(BaseElement):
    """ChromiumShadowRootElement是用于处理ShadowRoot的类，使用方法和ChromiumElement基本一致"""

    def __init__(self, parent_ele: ChromiumElement, obj_id: str):
        super().__init__(parent_ele.page)
        self.parent_ele = parent_ele
        self._node_id = self._get_node_id(obj_id)
        self._obj_id = obj_id

    def __repr__(self) -> str:
        return f'<ShadowRootElement in {self.parent_ele} >'

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str],
                 timeout: float = None) -> Union[ChromiumElement, None]:
        """在内部查找元素                                            \n
        例：ele2 = ele1('@id=ele_id')                               \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 超时时间
        :return: DriverElement对象或属性、文本
        """
        return self.ele(loc_or_str, timeout)

    @property
    def is_enabled(self) -> bool:
        """返回元素是否可用"""
        return not self.run_script('return this.disabled;')

    @property
    def is_alive(self) -> bool:
        """返回元素是否仍在DOM中"""
        try:
            self.page.driver.DOM.describeNode(nodeId=self._node_id)
            return True
        except Exception:
            return False

    @property
    def node_id(self) -> str:
        """返回元素cdp中的node id"""
        return self._node_id

    @property
    def obj_id(self) -> str:
        """返回元素js中的obect id"""
        return self._obj_id

    @property
    def tag(self) -> str:
        """返回元素标签名"""
        return 'shadow-root'

    @property
    def html(self) -> str:
        """返回outerHTML文本"""
        return f'<shadow_root>{self.inner_html}</shadow_root>'

    @property
    def inner_html(self) -> str:
        """返回内部的html文本"""
        return self.run_script('return this.innerHTML;')

    def run_script(self, script: str, as_expr: bool = False, *args: Any) -> Any:
        """运行javascript代码                                                 \n
        :param script: js文本
        :param as_expr: 是否作为表达式运行，为True时args无效
        :param args: 参数，按顺序在js文本中对应argument[0]、argument[2]...
        :return: 运行的结果
        """
        return _run_script(self, script, as_expr, self.page.timeouts.script, args)

    def run_async_script(self, script: str, as_expr: bool = False, *args: Any) -> None:
        """以异步方式执行js代码                                                 \n
        :param script: js文本
        :param as_expr: 是否作为表达式运行，为True时args无效
        :param args: 参数，按顺序在js文本中对应argument[0]、argument[2]...
        :return: None
        """
        from threading import Thread
        Thread(target=_run_script, args=(self, script, as_expr, self.page.timeouts.script, args)).start()

    def parent(self, level_or_loc: Union[str, int] = 1) -> ChromiumElement:
        """返回上面某一级父元素，可指定层数或用查询语法定位              \n
        :param level_or_loc: 第几级父元素，或定位符
        :return: ChromiumElement对象
        """
        if isinstance(level_or_loc, int):
            loc = f'xpath:./ancestor-or-self::*[{level_or_loc}]'

        elif isinstance(level_or_loc, (tuple, str)):
            loc = get_loc(level_or_loc, True)

            if loc[0] == 'css selector':
                raise ValueError('此css selector语法不受支持，请换成xpath。')

            loc = f'xpath:./ancestor-or-self::{loc[1].lstrip(". / ")}'

        else:
            raise TypeError('level_or_loc参数只能是tuple、int或str。')

        return self.parent_ele._ele(loc, timeout=0, relative=True)

    def next(self,
             index: int = 1,
             filter_loc: Union[tuple, str] = '') -> Union[ChromiumElement, str, None]:
        """返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param index: 第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :return: ChromiumElement对象
        """
        nodes = self.nexts(filter_loc=filter_loc)
        return nodes[index - 1] if nodes else None

    def before(self,
               index: int = 1,
               filter_loc: Union[tuple, str] = '') -> Union[ChromiumElement, str, None]:
        """返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param index: 前面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :return: 本元素前面的某个元素或节点
        """
        nodes = self.befores(filter_loc=filter_loc)
        return nodes[index - 1] if nodes else None

    def after(self, index: int = 1,
              filter_loc: Union[tuple, str] = '') -> Union[ChromiumElement, str, None]:
        """返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param index: 后面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :return: 本元素后面的某个元素或节点
        """
        nodes = self.afters(filter_loc=filter_loc)
        return nodes[index - 1] if nodes else None

    def nexts(self, filter_loc: Union[tuple, str] = '') -> List[Union[ChromiumElement, str]]:
        """返回后面所有兄弟元素或节点组成的列表        \n
        :param filter_loc: 用于筛选元素的查询语法
        :return: ChromiumElement对象组成的列表
        """
        loc = get_loc(filter_loc, True)
        if loc[0] == 'css selector':
            raise ValueError('此css selector语法不受支持，请换成xpath。')

        loc = loc[1].lstrip('./')
        xpath = f'xpath:./{loc}'
        return self.parent_ele._ele(xpath, timeout=0.1, single=False, relative=True)

    def befores(self, filter_loc: Union[tuple, str] = '') -> List[Union[ChromiumElement, str]]:
        """返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选        \n
        :param filter_loc: 用于筛选元素的查询语法
        :return: 本元素前面的元素或节点组成的列表
        """
        loc = get_loc(filter_loc, True)
        if loc[0] == 'css selector':
            raise ValueError('此css selector语法不受支持，请换成xpath。')

        loc = loc[1].lstrip('./')
        xpath = f'xpath:./preceding::{loc}'
        return self.parent_ele._ele(xpath, timeout=0.1, single=False, relative=True)

    def afters(self, filter_loc: Union[tuple, str] = '') -> List[Union[ChromiumElement, str]]:
        """返回前面全部兄弟元素或节点组成的列表，可用查询语法筛选        \n
        :param filter_loc: 用于筛选元素的查询语法
        :return: 本元素后面的元素或节点组成的列表
        """
        eles1 = self.nexts(filter_loc)
        loc = get_loc(filter_loc, True)[1].lstrip('./')
        xpath = f'xpath:./following::{loc}'
        return eles1 + self.parent_ele._ele(xpath, timeout=0.1, single=False, relative=True)

    def ele(self,
            loc_or_str: Union[Tuple[str, str], str],
            timeout: float = None) -> Union[ChromiumElement, None]:
        """返回当前元素下级符合条件的第一个元素                                   \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与元素所在页面等待时间一致
        :return: ChromiumElement对象
        """
        return self._ele(loc_or_str, timeout)

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None) -> List[ChromiumElement]:
        """返回当前元素下级所有符合条件的子元素                                              \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与元素所在页面等待时间一致
        :return: ChromiumElement对象组成的列表
        """
        return self._ele(loc_or_str, timeout=timeout, single=False)

    def s_ele(self, loc_or_ele=None) -> Union[SessionElement, str, None]:
        """查找第一个符合条件的元素以SessionElement形式返回，处理复杂页面时效率很高                 \n
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        return make_session_ele(self, loc_or_ele)

    def s_eles(self, loc_or_ele) -> List[SessionElement]:
        """查找所有符合条件的元素以SessionElement列表形式返回，处理复杂页面时效率很高                 \n
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象
        """
        return make_session_ele(self, loc_or_ele, single=False)

    def _ele(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None,
             single: bool = True, relative=False) -> Union['ChromiumElement', None, List[ChromiumElement]]:
        """返回当前元素下级符合条件的子元素、属性或节点文本，默认返回第一个               \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间
        :param single: True则返回第一个，False则返回全部
        :return: ChromiumElement对象或其组成的列表
        """
        loc = get_loc(loc_or_str)
        if loc[0] == 'css selector' and str(loc[1]).startswith(':root'):
            loc = loc[0], loc[1][5:]

        timeout = timeout if timeout is not None else self.page.timeout
        t1 = perf_counter()
        eles = make_session_ele(self.html).eles(loc)
        while not eles and perf_counter() - t1 <= timeout:
            eles = make_session_ele(self.html).eles(loc)

        if not eles:
            return None if single else eles

        css_paths = [i.css_path[47:] for i in eles]
        if single:
            node_id = self.page.driver.DOM.querySelector(nodeId=self._node_id, selector=css_paths[0])['nodeId']
            return ChromiumElement(self.page, node_id) if node_id else None

        else:
            results = []
            for i in css_paths:
                node_id = self.page.driver.DOM.querySelector(nodeId=self._node_id, selector=i)['nodeId']
                if node_id:
                    results.append(ChromiumElement(self.page, node_id))
            return results

    def _get_node_id(self, obj_id) -> str:
        """返回元素node id"""
        return self.page.driver.DOM.requestNode(objectId=obj_id)['nodeId']


class ChromiumBase(BasePage):
    """标签页、frame、页面基类"""

    def __init__(self,
                 address: str,
                 tab_id: str = None,
                 timeout: float = None):
        """初始化                                                      \n
        :param address: 浏览器地址:端口
        :param tab_id: 要控制的标签页id，不指定默认为激活的
        :param timeout: 超时时间
        """
        super().__init__(timeout)
        self._is_loading = None
        self._root_id = None
        self._connect_browser(address, tab_id)

    def _connect_browser(self,
                         addr_tab_opts: Union[str, Tab, DriverOptions] = None,
                         tab_id: str = None) -> None:
        """连接浏览器，在第一次时运行                                    \n
        :param addr_tab_opts: 浏览器地址、Tab对象或DriverOptions对象
        :param tab_id: 要控制的标签页id，不指定默认为激活的
        :return: None
        """
        self._root_id = None
        self.timeouts = Timeout(self)
        self._control_session = Session()
        self._control_session.keep_alive = False
        self._first_run = True
        self._is_reading = False  # 用于避免不同线程重复读取document

        self.address = addr_tab_opts
        if not tab_id:
            json = loads(self._control_session.get(f'http://{self.address}/json').text)
            tab_id = [i['id'] for i in json if i['type'] == 'page'][0]
        self._set_options()
        self._init_page(tab_id)
        self._get_document()

    def _init_page(self, tab_id: str = None) -> None:
        """新建页面、页面刷新、切换标签页后要进行的cdp参数初始化
        :param tab_id: 要跳转到的标签页id
        :return: None
        """
        self._is_loading = True
        if tab_id:
            self._tab_obj = Tab(id=tab_id, type='page',
                                webSocketDebuggerUrl=f'ws://{self.address}/devtools/page/{tab_id}')

        self._tab_obj.start()
        self._tab_obj.DOM.enable()
        self._tab_obj.Page.enable()

        self._tab_obj.Page.frameNavigated = self._onFrameNavigated
        self._tab_obj.Page.loadEventFired = self._onLoadEventFired
        # self._tab_obj.DOM.documentUpdated = self._onDocumentUpdated

    def _get_document(self) -> None:
        """刷新cdp使用的document数据"""
        # print('get doc')
        if not self._is_reading:
            self._is_reading = True
            self._wait_loading()
            root_id = self._tab_obj.DOM.getDocument()['root']['nodeId']
            self._root_id = self._tab_obj.DOM.resolveNode(nodeId=root_id)['object']['objectId']
            self._is_loading = False
            self._is_reading = False

    def _wait_loading(self, timeout: float = None) -> bool:
        """等待页面加载完成
        :param timeout: 超时时间
        :return: 是否成功，超时返回False
        """
        timeout = timeout if timeout is not None else self.timeouts.page_load

        end_time = perf_counter() + timeout
        while perf_counter() < end_time:
            state = self.ready_state
            if state == 'complete':
                return True
            elif self.page_load_strategy == 'eager' and state in ('interactive', 'complete'):
                self.stop_loading()
                return True
            elif self.page_load_strategy == 'none':
                self.stop_loading()
                return True
            sleep(.1)

        self.stop_loading()
        return False

    def _onLoadEventFired(self, **kwargs):
        """在页面刷新、变化后重新读取页面内容"""
        # print('load complete')
        if self._first_run is False and self._is_loading:
            self._get_document()

    def _onFrameNavigated(self, **kwargs):
        """页面跳转时触发"""
        if not kwargs['frame'].get('parentId', None):
            # print('nav')
            self._is_loading = True

    # def _onDocumentUpdated(self, **kwargs):
    #     # print('doc')
    #     pass

    def _set_options(self) -> None:
        pass

    def __call__(self, loc_or_str: Union[Tuple[str, str], str, 'ChromiumElement'],
                 timeout: float = None) -> Union['ChromiumElement', 'ChromiumFrame', None]:
        """在内部查找元素                                              \n
        例：ele = page('@id=ele_id')                                 \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 超时时间
        :return: ChromiumElement对象
        """
        return self.ele(loc_or_str, timeout)

    @property
    def driver(self) -> Tab:
        """返回用于控制浏览器的Tab对象"""
        return self._tab_obj

    @property
    def _driver(self):
        return self._tab_obj

    @property
    def _wait_driver(self) -> Tab:
        """返回用于控制浏览器的Tab对象，会先等待页面加载完毕"""
        while self._is_loading:
            sleep(.1)
        self._wait_loading()
        return self._tab_obj

    @property
    def is_loading(self) -> bool:
        """返回页面是否正在加载状态"""
        return self._is_loading

    @property
    def url(self) -> str:
        """返回当前页面url"""
        json = loads(self._control_session.get(f'http://{self.address}/json').text)
        return [i['url'] for i in json if i['id'] == self._tab_obj.id][0]  # change_mode要调用，不能用_driver

    @property
    def html(self) -> str:
        """返回当前页面html文本"""
        node_id = self._wait_driver.DOM.getDocument()['root']['nodeId']
        return self._wait_driver.DOM.getOuterHTML(nodeId=node_id)['outerHTML']

    @property
    def json(self) -> dict:
        """当返回内容是json格式时，返回对应的字典"""
        return loads(self('t:pre').text)

    @property
    def tab_id(self) -> str:
        """返回当前标签页id"""
        return self._wait_driver.id

    @property
    def ready_state(self) -> str:
        """返回当前页面加载状态，'loading' 'interactive' 'complete'"""
        return self._driver.Runtime.evaluate(expression='document.readyState;')['result']['value']

    @property
    def size(self) -> dict:
        """返回页面总长宽，{'height': int, 'width': int}"""
        w = self.run_script('document.body.scrollWidth;', as_expr=True)
        h = self.run_script('document.body.scrollHeight;', as_expr=True)
        return {'height': h, 'width': w}

    @property
    def active_ele(self) -> ChromiumElement:
        """返回当前焦点所在元素"""
        return self.run_script('return document.activeElement;')

    @property
    def page_load_strategy(self) -> str:
        """返回页面加载策略"""
        return self._page_load_strategy

    @property
    def scroll(self) -> 'ChromeScroll':
        """返回用于滚动滚动条的对象"""
        if not hasattr(self, '_scroll'):
            self._scroll = ChromeScroll(self)
        return self._scroll

    def set_page_load_strategy(self, value: str) -> None:
        """设置页面加载策略                                    \n
        :param value: 可选'normal', 'eager', 'none'
        :return: None
        """
        if value not in ('normal', 'eager', 'none'):
            raise ValueError("只能选择'normal', 'eager', 'none'。")
        self._page_load_strategy = value

    def set_timeouts(self, implicit: float = None, page_load: float = None, script: float = None) -> None:
        """设置超时时间，单位为秒                   \n
        :param implicit: 查找元素超时时间
        :param page_load: 页面加载超时时间
        :param script: 脚本运行超时时间
        :return: None
        """
        if implicit is not None:
            self.timeout = implicit

        if page_load is not None:
            self.timeouts.page_load = page_load

        if script is not None:
            self.timeouts.script = script

    def run_script(self, script: str, as_expr: bool = False, *args: Any) -> Any:
        """运行javascript代码                                                 \n
        :param script: js文本
        :param as_expr: 是否作为表达式运行，为True时args无效
        :param args: 参数，按顺序在js文本中对应argument[0]、argument[2]...
        :return: 运行的结果
        """
        return _run_script(self, script, as_expr, self.timeouts.script, args)

    def run_async_script(self, script: str, as_expr: bool = False, *args: Any) -> None:
        """以异步方式执行js代码                                                 \n
        :param script: js文本
        :param as_expr: 是否作为表达式运行，为True时args无效
        :param args: 参数，按顺序在js文本中对应argument[0]、argument[2]...
        :return: None
        """
        from threading import Thread
        Thread(target=_run_script, args=(self, script, as_expr, self.timeouts.script, args)).start()

    def get(self,
            url: str,
            show_errmsg: bool = False,
            retry: int = None,
            interval: float = None,
            timeout: float = None) -> Union[None, bool]:
        """访问url                                            \n
        :param url: 目标url
        :param show_errmsg: 是否显示和抛出异常
        :param retry: 重试次数
        :param interval: 重试间隔（秒）
        :param timeout: 连接超时时间
        :return: 目标url是否可用，返回None表示不确定
        """
        retry, interval = self._before_connect(url, retry, interval)
        self._url_available = self._d_connect(self._url,
                                              times=retry,
                                              interval=interval,
                                              show_errmsg=show_errmsg,
                                              timeout=timeout)
        return self._url_available

    def get_cookies(self, as_dict: bool = False) -> Union[list, dict]:
        """获取cookies信息                                              \n
        :param as_dict: 为True时返回由{name: value}键值对组成的dict
        :return: cookies信息
        """
        cookies = self._wait_driver.Network.getCookies()['cookies']
        if as_dict:
            return {cookie['name']: cookie['value'] for cookie in cookies}
        else:
            return cookies

    def set_cookies(self, cookies: Union[RequestsCookieJar, list, tuple, str, dict]) -> None:
        """设置cookies值                            \n
        :param cookies: cookies信息
        :return: None
        """
        cookies = _cookies_to_tuple(cookies)
        result_cookies = []
        for cookie in cookies:
            if not cookie.get('domain', None):
                continue
            c = {'value': '' if cookie['value'] is None else cookie['value'],
                 'name': cookie['name'],
                 'domain': cookie['domain']}
            result_cookies.append(c)
        self._wait_driver.Network.setCookies(cookies=result_cookies)

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, ChromiumElement, 'ChromiumFrame'],
            timeout: float = None) -> Union[ChromiumElement, 'ChromiumFrame', None]:
        """获取第一个符合条件的元素对象                       \n
        :param loc_or_ele: 定位符或元素对象
        :param timeout: 查找超时时间
        :return: ChromiumElement对象
        """
        return self._ele(loc_or_ele, timeout=timeout)

    def eles(self,
             loc_or_ele: Union[Tuple[str, str], str],
             timeout: float = None) -> List[Union[ChromiumElement, 'ChromiumFrame']]:
        """获取所有符合条件的元素对象                         \n
        :param loc_or_ele: 定位符或元素对象
        :param timeout: 查找超时时间
        :return: ChromiumElement对象组成的列表
        """
        return self._ele(loc_or_ele, timeout=timeout, single=False)

    def s_ele(self, loc_or_ele: Union[Tuple[str, str], str, ChromiumElement] = None) \
            -> Union[SessionElement, str, None]:
        """查找第一个符合条件的元素以SessionElement形式返回，处理复杂页面时效率很高       \n
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        if isinstance(loc_or_ele, ChromiumElement):
            return make_session_ele(loc_or_ele)
        else:
            return make_session_ele(self, loc_or_ele)

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str] = None) -> List[Union[SessionElement, str]]:
        """查找所有符合条件的元素以SessionElement列表形式返回                       \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象组成的列表
        """
        return make_session_ele(self, loc_or_str, single=False)

    def _ele(self,
             loc_or_ele: Union[Tuple[str, str], str, ChromiumElement, 'ChromiumFrame'],
             timeout: float = None, single: bool = True, relative: bool = False) \
            -> Union[ChromiumElement, 'ChromiumFrame', None, List[Union[ChromiumElement, 'ChromiumFrame']]]:
        """执行元素查找
        :param loc_or_ele: 定位符或元素对象
        :param timeout: 查找超时时间
        :param single: 是否只返回第一个
        :return: ChromiumElement对象或元素对象组成的列表
        """
        if isinstance(loc_or_ele, (str, tuple)):
            loc = get_loc(loc_or_ele)[1]
        elif isinstance(loc_or_ele, ChromiumElement):
            return loc_or_ele
        else:
            raise ValueError('loc_or_str参数只能是tuple、str、ChromiumElement类型。')

        timeout = timeout if timeout is not None else self.timeout
        search_result = self._wait_driver.DOM.performSearch(query=loc, includeUserAgentShadowDOM=True)
        count = search_result['resultCount']

        end_time = perf_counter() + timeout
        while count == 0 and perf_counter() < end_time:
            search_result = self._wait_driver.DOM.performSearch(query=loc, includeUserAgentShadowDOM=True)
            count = search_result['resultCount']

        if count == 0:
            return None if single else []

        count = 1 if single else count
        nodeIds = self._wait_driver.DOM.getSearchResults(searchId=search_result['searchId'], fromIndex=0,
                                                         toIndex=count)
        eles = []
        for i in nodeIds['nodeIds']:
            ele = ChromiumElement(self, node_id=i)
            if ele.tag in ('iframe', 'frame'):
                ele = ChromiumFrame(self, ele)
            eles.append(ele)

        return eles[0] if single else eles

    def wait_ele(self,
                 loc_or_ele: Union[str, tuple, ChromiumElement],
                 timeout: float = None) -> 'ChromiumElementWaiter':
        """返回用于等待元素到达某个状态的等待器对象                             \n
        :param loc_or_ele: 可以是元素、查询字符串、loc元组
        :param timeout: 等待超时时间
        :return: 用于等待的ElementWaiter对象
        """
        return ChromiumElementWaiter(self, loc_or_ele, timeout)

    def scroll_to_see(self, loc_or_ele: Union[str, tuple, ChromiumElement]) -> None:
        """滚动页面直到元素可见                                                        \n
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串（详见ele函数注释）
        :return: None
        """
        node_id = self.ele(loc_or_ele).node_id
        try:
            self._wait_driver.DOM.scrollIntoViewIfNeeded(nodeId=node_id)
        except Exception:
            self.ele(loc_or_ele).run_script("this.scrollIntoView();")

    def refresh(self, ignore_cache: bool = False) -> None:
        """刷新当前页面                      \n
        :param ignore_cache: 是否忽略缓存
        :return: None
        """
        self._driver.Page.reload(ignoreCache=ignore_cache)

    def forward(self, steps: int = 1) -> None:
        """在浏览历史中前进若干步    \n
        :param steps: 前进步数
        :return: None
        """
        self.run_script(f'window.history.go({steps});', as_expr=True)

    def back(self, steps: int = 1) -> None:
        """在浏览历史中后退若干步    \n
        :param steps: 后退步数
        :return: None
        """
        self.run_script(f'window.history.go({-steps});', as_expr=True)

    def stop_loading(self) -> None:
        """页面停止加载"""
        self._driver.Page.stopLoading()
        self._get_document()

    def run_cdp(self, cmd: str, **cmd_args) -> dict:
        """执行Chrome DevTools Protocol语句     \n
        :param cmd: 协议项目
        :param cmd_args: 参数
        :return: 执行的结果
        """
        return self._driver.call_method(cmd, **cmd_args)

    def set_user_agent(self, ua: str) -> None:
        """为当前tab设置user agent，只在当前tab有效          \n
        :param ua: user agent字符串
        :return: None
        """
        self._wait_driver.Network.setUserAgentOverride(userAgent=ua)

    def get_session_storage(self, item: str = None) -> Union[str, dict, None]:
        """获取sessionStorage信息，不设置item则获取全部       \n
        :param item: 要获取的项，不设置则返回全部
        :return: sessionStorage一个或所有项内容
        """
        js = f'sessionStorage.getItem("{item}");' if item else 'sessionStorage;'
        return self.run_script(js, as_expr=True)

    def get_local_storage(self, item: str = None) -> Union[str, dict, None]:
        """获取localStorage信息，不设置item则获取全部       \n
        :param item: 要获取的项目，不设置则返回全部
        :return: localStorage一个或所有项内容
        """
        js = f'localStorage.getItem("{item}");' if item else 'localStorage;'
        return self.run_script(js, as_expr=True)

    def set_session_storage(self, item: str, value: Union[str, bool]) -> None:
        """设置或删除某项sessionStorage信息                         \n
        :param item: 要设置的项
        :param value: 项的值，设置为False时，删除该项
        :return: None
        """
        js = f'sessionStorage.removeItem("{item}");' if item is False else f'sessionStorage.setItem("{item}","{value}");'
        return self.run_script(js, as_expr=True)

    def set_local_storage(self, item: str, value: Union[str, bool]) -> None:
        """设置或删除某项localStorage信息                           \n
        :param item: 要设置的项
        :param value: 项的值，设置为False时，删除该项
        :return: None
        """
        js = f'localStorage.removeItem("{item}");' if item is False else f'localStorage.setItem("{item}","{value}");'
        return self.run_script(js, as_expr=True)

    def clear_cache(self,
                    session_storage: bool = True,
                    local_storage: bool = True,
                    cache: bool = True,
                    cookies: bool = True) -> None:
        """清除缓存，可选要清除的项                            \n
        :param session_storage: 是否清除sessionStorage
        :param local_storage: 是否清除localStorage
        :param cache: 是否清除cache
        :param cookies: 是否清除cookies
        :return: None
        """
        if session_storage:
            self.run_script('sessionStorage.clear();', as_expr=True)
        if local_storage:
            self.run_script('localStorage.clear();', as_expr=True)
        if cache:
            self._wait_driver.Network.clearBrowserCache()
        if cookies:
            self._wait_driver.Network.clearBrowserCookies()

    def _d_connect(self,
                   to_url: str,
                   times: int = 0,
                   interval: float = 1,
                   show_errmsg: bool = False,
                   timeout: float = None) -> Union[bool, None]:
        """尝试连接，重试若干次                            \n
        :param to_url: 要访问的url
        :param times: 重试次数
        :param interval: 重试间隔（秒）
        :param show_errmsg: 是否抛出异常
        :param timeout: 连接超时时间
        :return: 是否成功，返回None表示不确定
        """
        err = None
        timeout = timeout if timeout is not None else self.timeouts.page_load

        for _ in range(times + 1):
            result = self._driver.Page.navigate(url=to_url)
            is_timeout = not self._wait_loading(timeout)

            if is_timeout:
                err = TimeoutError('页面连接超时。')
            if 'errorText' in result:
                err = ConnectionError(result['errorText'])

            if not err:
                break

            if _ < times:
                sleep(interval)
                if show_errmsg:
                    print(f'重试 {to_url}')

        if err and show_errmsg:
            raise err if err is not None else ConnectionError('连接异常。')

        return False if err else True


class ChromiumFrame(ChromiumBase):
    """实现浏览器frame的类"""

    def __init__(self, page,
                 ele: ChromiumElement):
        """初始化                                                      \n
        :param page: 浏览器地址:端口、Tab对象或DriverOptions对象
        :param ele: 页面上的frame元素
        """
        self.page = page
        self._inner_ele = ele
        frame_id = page.run_cdp('DOM.describeNode', nodeId=ele.node_id)['node'].get('frameId', None)
        super().__init__(page.address, frame_id, page.timeout)

    def __repr__(self) -> str:
        attrs = [f"{attr}='{self.attrs[attr]}'" for attr in self.attrs]
        return f'<ChromiumFrame {self.tag} {" ".join(attrs)}>'

    @property
    def tag(self) -> str:
        """返回元素tag"""
        return self._inner_ele.tag

    @property
    def html(self) -> str:
        """返回元素outerHTML文本"""
        tag = self.tag
        out_html = self.page.driver.DOM.getOuterHTML(nodeId=self._inner_ele.node_id)['outerHTML']
        in_html = super().html
        sign = search(rf'<{tag}.*?>', out_html).group(0)
        return f'{sign}{in_html}</{tag}>'

    @property
    def inner_html(self) -> str:
        """返回元素innerHTML文本"""
        return super().html

    @property
    def attrs(self) -> dict:
        return self._inner_ele.attrs

    @property
    def frame_size(self) -> dict:
        """返回frame元素大小"""
        return self._inner_ele.size

    def _set_options(self) -> None:
        self.set_timeouts(page_load=self.page.timeouts.page_load,
                          script=self.page.timeouts.script,
                          implicit=self.page.timeouts.implicit if self.timeout is None else self.timeout)
        self._page_load_strategy = self.page.page_load_strategy

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
             index: int = 1,
             filter_loc: Union[tuple, str] = '',
             timeout: float = 0) -> Union['ChromiumElement', str, None]:
        """返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param index: 前面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素
        """
        return self._inner_ele.prev(index, filter_loc, timeout)

    def next(self,
             index: int = 1,
             filter_loc: Union[tuple, str] = '',
             timeout: float = 0) -> Union['ChromiumElement', str, None]:
        """返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param index: 后面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素
        """
        return self._inner_ele.next(index, filter_loc, timeout)

    def before(self,
               index: int = 1,
               filter_loc: Union[tuple, str] = '',
               timeout: float = None) -> Union['ChromiumElement', str, None]:
        """返回当前元素前面的一个元素，可指定筛选条件和第几个。查找范围不限兄弟元素，而是整个DOM文档        \n
        :param index: 前面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 本元素前面的某个元素或节点
        """
        return self._inner_ele.before(index, filter_loc, timeout)

    def after(self,
              index: int = 1,
              filter_loc: Union[tuple, str] = '',
              timeout: float = None) -> Union['ChromiumElement', str, None]:
        """返回当前元素后面的一个元素，可指定筛选条件和第几个。查找范围不限兄弟元素，而是整个DOM文档        \n
        :param index: 后面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 本元素后面的某个元素或节点
        """
        return self._inner_ele.after(index, filter_loc, timeout)

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


class Timeout(object):
    """用于保存d模式timeout信息的类"""

    def __init__(self, page):
        self.page = page
        self.page_load = 30
        self.script = 30

    @property
    def implicit(self):
        return self.page.timeout


def make_chromium_ele(ele: ChromiumElement,
                      loc: Union[str, Tuple[str, str]],
                      single: bool = True,
                      timeout: float = None,
                      relative=True) -> Union[ChromiumElement, str, None, List[Union[ChromiumElement, str]]]:
    """在chromium元素中查找                                   \n
    :param ele: ChromiumElement对象
    :param loc: 元素定位元组
    :param single: True则返回第一个，False则返回全部
    :param timeout: 查找元素超时时间
    :param relative: WebPage用于标记是否相对定位使用
    :return: 返回ChromiumElement元素或它们组成的列表
    """
    # ---------------处理定位符---------------
    if isinstance(loc, (str, tuple)):
        loc = get_loc(loc)
    else:
        raise ValueError("定位符必须为str或长度为2的tuple对象。")

    loc_str = loc[1]
    if loc[0] == 'xpath' and loc[1].lstrip().startswith('/'):
        loc_str = f'.{loc_str}'
    elif loc[0] == 'css selector' and loc[1].lstrip().startswith('>'):
        loc_str = f'{ele.css_path}{loc[1]}'
    loc = loc[0], loc_str

    timeout = timeout if timeout is not None else ele.page.timeout

    # ---------------执行查找-----------------
    if loc[0] == 'xpath':
        return _find_by_xpath(ele, loc[1], single, timeout, relative=relative)

    else:
        return _find_by_css(ele, loc[1], single, timeout)


def _find_by_xpath(ele: ChromiumElement,
                   xpath: str,
                   single: bool,
                   timeout: float,
                   relative=True) -> Union[ChromiumElement, List[ChromiumElement], None]:
    """执行用xpath在元素中查找元素
    :param ele: 在此元素中查找
    :param xpath: 查找语句
    :param single: 是否只返回第一个结果
    :param timeout: 超时时间
    :return: ChromiumElement或其组成的列表
    """
    type_txt = '9' if single else '7'
    node_txt = 'this.contentDocument' if ele.tag in ('iframe', 'frame') and not relative else 'this'
    js = _make_js_for_find_ele_by_xpath(xpath, type_txt, node_txt)
    r = ele.page.run_cdp('Runtime.callFunctionOn',
                         functionDeclaration=js, objectId=ele.obj_id, returnByValue=False, awaitPromise=True,
                         userGesture=True)
    if r['result']['type'] == 'string':
        return r['result']['value']

    if 'exceptionDetails' in r:
        if 'The result is not a node set' in r['result']['description']:
            js = _make_js_for_find_ele_by_xpath(xpath, '1', node_txt)
            r = ele.page.run_cdp('Runtime.callFunctionOn',
                                 functionDeclaration=js, objectId=ele.obj_id, returnByValue=False, awaitPromise=True,
                                 userGesture=True)
            return r['result']['value']
        else:
            raise SyntaxError(f'查询语句错误：\n{r}')

    end_time = perf_counter() + timeout
    while (r['result']['subtype'] == 'null'
           or r['result']['description'] == 'NodeList(0)') and perf_counter() < end_time:
        r = ele.page.run_cdp('Runtime.callFunctionOn',
                             functionDeclaration=js, objectId=ele.obj_id, returnByValue=False, awaitPromise=True,
                             userGesture=True)

    if single:
        if r['result']['subtype'] == 'null':
            return None
        else:
            # return ChromiumElement(ele.page, obj_id=r['result']['objectId'])
            return _make_chromium_ele(ele.page, obj_id=r['result']['objectId'])

    else:
        if r['result']['description'] == 'NodeList(0)':
            return []
        else:
            r = ele.page.driver.Runtime.getProperties(objectId=r['result']['objectId'], ownProperties=True)['result']
            return [_make_chromium_ele(ele.page, obj_id=i['value']['objectId'])
                    if i['value']['type'] == 'object' else i['value']['value']
                    for i in r[:-1]]


def _find_by_css(ele: ChromiumElement,
                 selector: str,
                 single: bool,
                 timeout: float) -> Union[ChromiumElement, List[ChromiumElement], None]:
    """执行用css selector在元素中查找元素
    :param ele: 在此元素中查找
    :param selector: 查找语句
    :param single: 是否只返回第一个结果
    :param timeout: 超时时间
    :return: ChromiumElement或其组成的列表
    """
    selector = selector.replace('"', r'\"')
    find_all = '' if single else 'All'
    node_txt = 'this.contentDocument' if ele.tag in ('iframe', 'frame', 'shadow-root') else 'this'
    js = f'function(){{return {node_txt}.querySelector{find_all}("{selector}");}}'
    r = ele.page.run_cdp('Runtime.callFunctionOn',
                         functionDeclaration=js, objectId=ele.obj_id, returnByValue=False, awaitPromise=True,
                         userGesture=True)
    if 'exceptionDetails' in r:
        raise SyntaxError(f'查询语句错误：\n{r}')

    end_time = perf_counter() + timeout
    while (r['result']['subtype'] == 'null'
           or r['result']['description'] == 'NodeList(0)') and perf_counter() < end_time:
        r = ele.page.run_cdp('Runtime.callFunctionOn',
                             functionDeclaration=js, objectId=ele.obj_id, returnByValue=False, awaitPromise=True,
                             userGesture=True)

    if single:
        if r['result']['subtype'] == 'null':
            return None
        else:
            return _make_chromium_ele(ele.page, obj_id=r['result']['objectId'])

    else:
        if r['result']['description'] == 'NodeList(0)':
            return []
        else:
            r = ele.page.driver.Runtime.getProperties(objectId=r['result']['objectId'], ownProperties=True)['result']
            return [_make_chromium_ele(ele.page, obj_id=i['value']['objectId']) for i in r]


def _make_chromium_ele(page, node_id: str = None, obj_id: str = None):
    """根据node id或object id生成相应元素对象"""
    ele = ChromiumElement(page, obj_id=obj_id, node_id=node_id)
    if ele.tag in ('iframe', 'frame') and ele.attr('src'):
        ele = ChromiumFrame(page, ele)
    return ele


def _make_js_for_find_ele_by_xpath(xpath: str, type_txt: str, node_txt: str) -> str:
    """生成用xpath在元素中查找元素的js文本
    :param xpath: xpath文本
    :param type_txt: 查找类型
    :param node_txt: 节点类型
    :return: js文本
    """
    for_txt = ''

    # 获取第一个元素、节点或属性
    if type_txt == '9':
        return_txt = '''
if(e.singleNodeValue==null){return null;}
else if(e.singleNodeValue.constructor.name=="Text"){return e.singleNodeValue.data;}
else if(e.singleNodeValue.constructor.name=="Attr"){return e.singleNodeValue.nodeValue;}
else if(e.singleNodeValue.constructor.name=="Comment"){return e.singleNodeValue.nodeValue;}
else{return e.singleNodeValue;}'''

    # 按顺序获取所有元素、节点或属性
    elif type_txt == '7':
        for_txt = """
var a=new Array();
for(var i = 0; i <e.snapshotLength ; i++){
if(e.snapshotItem(i).constructor.name=="Text"){a.push(e.snapshotItem(i).data);}
else if(e.snapshotItem(i).constructor.name=="Attr"){a.push(e.snapshotItem(i).nodeValue);}
else if(e.snapshotItem(i).constructor.name=="Comment"){a.push(e.snapshotItem(i).nodeValue);}
else{a.push(e.snapshotItem(i));}}"""
        return_txt = 'return a;'

    elif type_txt == '2':
        return_txt = 'return e.stringValue;'
    elif type_txt == '1':
        return_txt = 'return e.numberValue;'
    else:
        return_txt = 'return e.singleNodeValue;'

    js = f'function(){{var e=document.evaluate(\'{xpath}\',{node_txt},null,{type_txt},null);\n{for_txt}\n{return_txt}}}'

    return js


def _run_script(page_or_ele, script: str, as_expr: bool = False, timeout: float = None, args: tuple = None) -> Any:
    """运行javascript代码                                                 \n
    :param page_or_ele: 页面对象或元素对象
    :param script: js文本
    :param as_expr: 是否作为表达式运行，为True时args无效
    :param args: 参数，按顺序在js文本中对应argument[0]、argument[2]...
    :return: js执行结果
    """
    if isinstance(page_or_ele, (ChromiumElement, ChromiumShadowRootElement)):
        page = page_or_ele.page
        obj_id = page_or_ele.obj_id
    else:
        page = page_or_ele
        obj_id = page_or_ele._root_id

    if as_expr:
        res = page.run_cdp('Runtime.evaluate',
                           expression=script,
                           returnByValue=False,
                           awaitPromise=True,
                           userGesture=True,
                           timeout=timeout * 1000)
    else:
        args = args or ()
        if not is_js_func(script):
            script = f'function(){{{script}}}'
        res = page.run_cdp('Runtime.callFunctionOn',
                           functionDeclaration=script,
                           objectId=obj_id,
                           arguments=[_convert_argument(arg) for arg in args],
                           returnByValue=False,
                           awaitPromise=True,
                           userGesture=True)

    exceptionDetails = res.get('exceptionDetails')
    if exceptionDetails:
        raise RuntimeError(f'Evaluation failed: {exceptionDetails}')

    try:
        return _parse_js_result(page, page_or_ele, res.get('result'))
    except Exception:
        return res


def _parse_js_result(page, ele, result: dict):
    """解析js返回的结果"""
    if 'unserializableValue' in result:
        return result['unserializableValue']

    the_type = result['type']

    if the_type == 'object':
        sub_type = result['subtype']
        if sub_type == 'null':
            return None

        elif sub_type == 'node':
            if result['className'] == 'ShadowRoot':
                return ChromiumShadowRootElement(ele, obj_id=result['objectId'])
            else:
                return _make_chromium_ele(page, obj_id=result['objectId'])

        elif sub_type == 'array':
            r = page.driver.Runtime.getProperties(objectId=result['result']['objectId'], ownProperties=True)['result']
            return [_parse_js_result(page, ele, result=i['value']) for i in r]

        else:
            return result['value']

    elif the_type == 'undefined':
        return None

    else:
        return result['value']


def _convert_argument(arg: Any) -> dict:
    """把参数转换成js能够接收的形式"""
    if isinstance(arg, ChromiumElement):
        return {'objectId': arg.obj_id}

    elif isinstance(arg, (int, float, str, bool)):
        return {'value': arg}

    from math import inf
    if arg == inf:
        return {'unserializableValue': 'Infinity'}
    if arg == -inf:
        return {'unserializableValue': '-Infinity'}


def _send_enter(ele: ChromiumElement) -> None:
    """发送回车"""
    data = {'type': 'keyDown', 'modifiers': 0, 'windowsVirtualKeyCode': 13, 'code': 'Enter', 'key': 'Enter',
            'text': '\r', 'autoRepeat': False, 'unmodifiedText': '\r', 'location': 0, 'isKeypad': False}

    ele.page.run_cdp('Input.dispatchKeyEvent', **data)
    data['type'] = 'keyUp'
    ele.page.run_cdp('Input.dispatchKeyEvent', **data)


def _send_key(ele: ChromiumElement, modifier: int, key: str) -> None:
    """发送一个字，在键盘中的字符触发按键，其它直接发送文本"""
    if key not in _keyDefinitions:
        ele.page.run_cdp('Input.insertText', text=key)

    else:
        description = _keyDescriptionForString(modifier, key)
        text = description['text']
        data = {'type': 'keyDown' if text else 'rawKeyDown',
                'modifiers': modifier,
                'windowsVirtualKeyCode': description['keyCode'],
                'code': description['code'],
                'key': description['key'],
                'text': text,
                'autoRepeat': False,
                'unmodifiedText': text,
                'location': description['location'],
                'isKeypad': description['location'] == 3}

        ele.page.run_cdp('Input.dispatchKeyEvent', **data)
        data['type'] = 'keyUp'
        ele.page.run_cdp('Input.dispatchKeyEvent', **data)


def _offset_scroll(ele: ChromiumElement, offset_x: int, offset_y: int) -> tuple:
    """接收元素及偏移坐标，滚动到偏移坐标，返回该点在视口中的坐标
    :param ele: 元素对象
    :param offset_x: 偏移量x
    :param offset_y: 偏移量y
    :return: 视口中的坐标
    """
    location = ele.location
    midpoint = ele._click_point
    lx = location['x'] + offset_x if offset_x else midpoint['x']
    ly = location['y'] + offset_y if offset_y else midpoint['y']

    if not _location_in_viewport(ele.page, lx, ly):
        ele.page.scroll.to_location(lx, ly)
    cl = ele.client_location
    cm = ele._client_click_point
    cx = cl['x'] + offset_x if offset_x else cm['x']
    cy = cl['y'] + offset_y if offset_y else cm['y']
    return cx, cy


class ChromeScroll(object):
    """用于滚动的对象"""

    def __init__(self, page_or_ele):
        """
        :param page_or_ele: ChromePage或ChromiumElement
        """
        if isinstance(page_or_ele, ChromiumElement):
            self.t1 = self.t2 = 'this'
            self.obj_id = page_or_ele.obj_id
            self.page = page_or_ele.page
        else:
            self.t1 = 'window'
            self.t2 = 'document.documentElement'
            self.obj_id = None
            self.page = page_or_ele

    def _run_script(self, js: str):
        js = js.format(self.t1, self.t2, self.t2)
        if self.obj_id:
            self.page.run_script(js)
        else:
            self.page.driver.Runtime.evaluate(expression=js)

    def to_top(self) -> None:
        """滚动到顶端，水平位置不变"""
        self._run_script('{}.scrollTo({}.scrollLeft,0);')

    def to_bottom(self) -> None:
        """滚动到底端，水平位置不变"""
        self._run_script('{}.scrollTo({}.scrollLeft,{}.scrollHeight);')

    def to_half(self) -> None:
        """滚动到垂直中间位置，水平位置不变"""
        self._run_script('{}.scrollTo({}.scrollLeft,{}.scrollHeight/2);')

    def to_rightmost(self) -> None:
        """滚动到最右边，垂直位置不变"""
        self._run_script('{}.scrollTo({}.scrollWidth,{}.scrollTop);')

    def to_leftmost(self) -> None:
        """滚动到最左边，垂直位置不变"""
        self._run_script('{}.scrollTo(0,{}.scrollTop);')

    def to_location(self, x: int, y: int) -> None:
        """滚动到指定位置                 \n
        :param x: 水平距离
        :param y: 垂直距离
        :return: None
        """
        self._run_script(f'{{}}.scrollTo({x},{y});')

    def up(self, pixel: int = 300) -> None:
        """向上滚动若干像素，水平位置不变    \n
        :param pixel: 滚动的像素
        :return: None
        """
        pixel = -pixel
        self._run_script(f'{{}}.scrollBy(0,{pixel});')

    def down(self, pixel: int = 300) -> None:
        """向下滚动若干像素，水平位置不变    \n
        :param pixel: 滚动的像素
        :return: None
        """
        self._run_script(f'{{}}.scrollBy(0,{pixel});')

    def left(self, pixel: int = 300) -> None:
        """向左滚动若干像素，垂直位置不变    \n
        :param pixel: 滚动的像素
        :return: None
        """
        pixel = -pixel
        self._run_script(f'{{}}.scrollBy({pixel},0);')

    def right(self, pixel: int = 300) -> None:
        """向右滚动若干像素，垂直位置不变    \n
        :param pixel: 滚动的像素
        :return: None
        """
        self._run_script(f'{{}}.scrollBy({pixel},0);')


class ChromeSelect(object):
    """ChromeSelect 类专门用于处理 d 模式下 select 标签"""

    def __init__(self, ele: ChromiumElement):
        """初始化                      \n
        :param ele: select 元素对象
        """
        if ele.tag != 'select':
            raise TypeError("select方法只能在<select>元素使用。")

        self._ele = ele

    def __call__(self, text_or_index: Union[str, int, list, tuple], timeout=None) -> bool:
        """选定下拉列表中子元素                                                             \n
        :param text_or_index: 根据文本、值选或序号择选项，若允许多选，传入list或tuple可多选
        :param timeout: 超时时间，不输入默认实用页面超时时间
        :return: None
        """
        para_type = 'index' if isinstance(text_or_index, int) else 'text'
        timeout = timeout if timeout is not None else self._ele.page.timeout
        return self._select(text_or_index, para_type, timeout=timeout)

    @property
    def is_multi(self) -> bool:
        """返回是否多选表单"""
        multi = self._ele.attr('multiple')
        return multi and multi.lower() != "false"

    @property
    def options(self) -> List[ChromiumElement]:
        """返回所有选项元素组成的列表"""
        return self._ele.eles('tag:option')

    @property
    def selected_option(self) -> Union[ChromiumElement, None]:
        """返回第一个被选中的option元素        \n
        :return: ChromiumElement对象或None
        """
        ele = self._ele.run_script('return this.options[this.selectedIndex];')
        return ele

    @property
    def selected_options(self) -> List[ChromiumElement]:
        """返回所有被选中的option元素列表        \n
        :return: ChromiumElement对象组成的列表
        """
        return [x for x in self.options if x.is_selected]

    def clear(self) -> None:
        """清除所有已选项"""
        if not self.is_multi:
            raise NotImplementedError("只能在多选菜单执行此操作。")
        for opt in self.options:
            if opt.is_selected:
                opt.click(by_js=True)

    def by_text(self, text: Union[str, list, tuple], timeout=None) -> bool:
        """此方法用于根据text值选择项。当元素是多选列表时，可以接收list或tuple  \n
        :param text: text属性值，传入list或tuple可选择多项
        :param timeout: 超时时间，不输入默认实用页面超时时间
        :return: None
        """
        timeout = timeout if timeout is not None else self._ele.page.timeout
        return self._select(text, 'text', False, timeout)

    def by_value(self, value: Union[str, list, tuple], timeout=None) -> bool:
        """此方法用于根据value值选择项。当元素是多选列表时，可以接收list或tuple  \n
        :param value: value属性值，传入list或tuple可选择多项
        :param timeout: 超时时间，不输入默认实用页面超时时间
        :return: None
        """
        timeout = timeout if timeout is not None else self._ele.page.timeout
        return self._select(value, 'value', False, timeout)

    def by_index(self, index: Union[int, list, tuple], timeout=None) -> bool:
        """此方法用于根据index值选择项。当元素是多选列表时，可以接收list或tuple  \n
        :param index: index属性值，传入list或tuple可选择多项
        :param timeout: 超时时间，不输入默认实用页面超时时间
        :return: None
        """
        timeout = timeout if timeout is not None else self._ele.page.timeout
        return self._select(index, 'index', False, timeout)

    def cancel_by_text(self, text: Union[str, list, tuple], timeout=None) -> bool:
        """此方法用于根据text值取消选择项。当元素是多选列表时，可以接收list或tuple  \n
        :param text: text属性值，传入list或tuple可取消多项
        :param timeout: 超时时间，不输入默认实用页面超时时间
        :return: None
        """
        timeout = timeout if timeout is not None else self._ele.page.timeout
        return self._select(text, 'text', True, timeout)

    def cancel_by_value(self, value: Union[str, list, tuple], timeout=None) -> bool:
        """此方法用于根据value值取消选择项。当元素是多选列表时，可以接收list或tuple  \n
        :param value: value属性值，传入list或tuple可取消多项
        :param timeout: 超时时间，不输入默认实用页面超时时间
        :return: None
        """
        timeout = timeout if timeout is not None else self._ele.page.timeout
        return self._select(value, 'value', True, timeout)

    def cancel_by_index(self, index: Union[int, list, tuple], timeout=None) -> bool:
        """此方法用于根据index值取消选择项。当元素是多选列表时，可以接收list或tuple  \n
        :param index: value属性值，传入list或tuple可取消多项
        :param timeout: 超时时间，不输入默认实用页面超时时间
        :return: None
        """
        timeout = timeout if timeout is not None else self._ele.page.timeout
        return self._select(index, 'index', True, timeout)

    def invert(self) -> None:
        """反选"""
        if not self.is_multi:
            raise NotImplementedError("只能对多项选框执行反选。")

        for i in self.options:
            i.click(by_js=True)

    def _select(self,
                text_value_index: Union[str, int, list, tuple] = None,
                para_type: str = 'text',
                deselect: bool = False,
                timeout=None) -> bool:
        """选定或取消选定下拉列表中子元素                                                             \n
        :param text_value_index: 根据文本、值选或序号择选项，若允许多选，传入list或tuple可多选
        :param para_type: 参数类型，可选 'text'、'value'、'index'
        :param deselect: 是否取消选择
        :return: 是否选择成功
        """
        if not self.is_multi and isinstance(text_value_index, (list, tuple)):
            raise TypeError('单选下拉列表不能传入list和tuple')

        def do_select():
            if para_type == 'text':
                ele = self._ele(f'tx={text_value_index}', timeout=0)
            elif para_type == 'value':
                ele = self._ele(f'@value={text_value_index}', timeout=0)
            elif para_type == 'index':
                ele = self._ele(f'x:.//option[{int(text_value_index)}]', timeout=0)
            else:
                raise ValueError('para_type参数只能传入"text"、"value"或"index"。')

            if not ele:
                return False

            js = 'false' if deselect else 'true'
            ele.run_script(f'this.selected={js};')

            return True

        if isinstance(text_value_index, (str, int)):
            ok = do_select()
            end_time = perf_counter() + timeout
            while not ok and perf_counter() < end_time:
                sleep(.2)
                ok = do_select()
            return ok

        elif isinstance(text_value_index, (list, tuple)):
            return self._select_multi(text_value_index, para_type, deselect)

        else:
            raise TypeError('只能传入str、int、list和tuple类型。')

    def _select_multi(self,
                      text_value_index: Union[list, tuple] = None,
                      para_type: str = 'text',
                      deselect: bool = False) -> bool:
        """选定或取消选定下拉列表中多个子元素                                                             \n
        :param text_value_index: 根据文本、值选或序号择选多项
        :param para_type: 参数类型，可选 'text'、'value'、'index'
        :param deselect: 是否取消选择
        :return: 是否选择成功
        """
        if para_type not in ('text', 'value', 'index'):
            raise ValueError('para_type参数只能传入“text”、“value”或“index”')

        if not isinstance(text_value_index, (list, tuple)):
            raise TypeError('只能传入list或tuple类型。')

        success = True
        for i in text_value_index:
            if not isinstance(i, (int, str)):
                raise TypeError('列表只能由str或int组成')

            p = 'index' if isinstance(i, int) else para_type
            if not self._select(i, p, deselect):
                success = False

        return success


class ChromiumElementWaiter(object):
    """等待元素在dom中某种状态，如删除、显示、隐藏"""

    def __init__(self,
                 page_or_ele,
                 loc_or_ele: Union[str, tuple, ChromiumElement],
                 timeout: float = None):
        """等待元素在dom中某种状态，如删除、显示、隐藏                         \n
        :param page_or_ele: 页面或父元素
        :param loc_or_ele: 要等待的元素，可以是已有元素、定位符
        :param timeout: 超时时间，默认读取页面超时时间
        """
        if not isinstance(loc_or_ele, (str, tuple, ChromiumElement)):
            raise TypeError('loc_or_ele只能接收定位符或元素对象。')

        self.driver = page_or_ele
        self.loc_or_ele = loc_or_ele
        if timeout is not None:
            self.timeout = timeout
        else:
            self.timeout = page_or_ele.page.timeout if isinstance(page_or_ele, ChromiumElement) else page_or_ele.timeout

    def delete(self) -> bool:
        """等待元素从dom删除"""
        if isinstance(self.loc_or_ele, ChromiumElement):
            end_time = perf_counter() + self.timeout
            while perf_counter() < end_time:
                if not self.loc_or_ele.is_alive:
                    return True

        ele = self.driver(self.loc_or_ele, timeout=.5)
        if ele is None:
            return True

        end_time = perf_counter() + self.timeout
        while perf_counter() < end_time:
            if not self.loc_or_ele.is_alive:
                return True

        return False

    def display(self) -> bool:
        """等待元素从dom显示"""
        return self._wait_ele('display')

    def hidden(self) -> bool:
        """等待元素从dom隐藏"""
        return self._wait_ele('hidden')

    def _wait_ele(self, mode: str) -> Union[None, bool]:
        """执行等待
        :param mode: 等待模式
        :return: 是否等待成功
        """
        target = self.driver(self.loc_or_ele)
        if target is None:
            return None

        end_time = perf_counter() + self.timeout
        while perf_counter() < end_time:
            if mode == 'display' and target.is_displayed:
                return True

            elif mode == 'hidden' and not target.is_displayed:
                return True

        return False
