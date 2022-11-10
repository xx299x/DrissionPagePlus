# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   chrome_element.py
"""
from os import sep
from os.path import basename
from pathlib import Path
from re import search
from typing import Union, Tuple, List, Any
from time import perf_counter, sleep

from .keys import _keys_to_typing, _keyDescriptionForString, _keyDefinitions
from .session_element import make_session_ele, SessionElement
from .base import DrissionElement, BaseElement
from .common import make_absolute_link, get_loc, get_ele_txt, format_html, is_js_func


class ChromeElement(DrissionElement):
    def __init__(self, page, node_id: str = None, obj_id: str = None):
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
        # attrs = [f"{attr}='{self.attrs[attr]}'" for attr in self.attrs]
        # return f'<ChromeElement {self.tag} {" ".join(attrs)}>'
        return f'<ChromeElement {self.tag} >'

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str],
                 timeout: float = None) -> Union['ChromeElement', str, None]:
        """在内部查找元素                                             \n
        例：ele2 = ele1('@id=ele_id')                               \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 超时时间
        :return: ChromeElement对象或属性、文本
        """
        return self.ele(loc_or_str, timeout)

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
    def tag(self) -> str:
        """返回元素tag"""
        if self._tag is None:
            self._tag = self.page.driver.DOM.describeNode(nodeId=self._node_id)['node']['localName'].lower()
        return self._tag

    @property
    def inner_html(self) -> str:
        """返回元素innerHTML文本"""
        if self.tag in ('iframe', 'frame'):
            return self.run_script('return this.contentDocument.documentElement;').html
            # return run_script(self, 'this.contentDocument.body;').html
        return self.run_script('return this.innerHTML;')

    @property
    def attrs(self) -> dict:
        attrs = self.page.driver.DOM.getAttributes(nodeId=self._node_id)['attributes']
        attrs_len = len(attrs)
        return {attrs[i]: attrs[i + 1] for i in range(0, attrs_len, 2)}

    @property
    def text(self) -> str:
        """返回元素内所有文本"""
        return get_ele_txt(make_session_ele(self.html))

    @property
    def raw_text(self):
        """返回未格式化处理的元素内文本"""
        return self.prop('innerText')

    # -----------------driver独有属性-------------------
    @property
    def obj_id(self) -> str:
        return self._obj_id

    @property
    def node_id(self) -> str:
        return self._node_id

    @property
    def size(self) -> dict:
        """返回元素宽和高"""
        model = self.page.driver.DOM.getBoxModel(nodeId=self._node_id)['model']
        return {"height": model['height'], "width": model['width']}

    @property
    def client_location(self) -> dict:
        """返回元素左上角坐标"""
        js = 'return this.getBoundingClientRect().left.toString()+" "+this.getBoundingClientRect().top.toString();'
        xy = self.run_script(js)
        x, y = xy.split(' ')
        return {'x': int(x.split('.')[0]), 'y': int(y.split('.')[0])}

    @property
    def location(self) -> dict:
        """返回元素左上角坐标"""
        js = '''function(){
            function getElementPagePosition(element){
              var actualLeft = element.offsetLeft;
              var current = element.offsetParent;
              while (current !== null){
                actualLeft += current.offsetLeft;
                current = current.offsetParent;
              }
              var actualTop = element.offsetTop;
              var current = element.offsetParent;
              while (current !== null){
                actualTop += (current.offsetTop+current.clientTop);
                current = current.offsetParent;
              }
              return actualLeft.toString() +' '+actualTop.toString();
            }
            return getElementPagePosition(this);}'''
        xy = self.run_script(js)
        x, y = xy.split(' ')
        return {'x': int(x.split('.')[0]), 'y': int(y.split('.')[0])}

    @property
    def shadow_root(self):
        """返回当前元素的shadow_root元素对象"""
        shadow = self.run_script('return this.shadowRoot;')
        return shadow
        # if shadow:
        #     from .shadow_root_element import ShadowRootElement
        #     return ShadowRootElement(shadow, self)

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

    def parent(self, level_or_loc: Union[tuple, str, int] = 1) -> Union['ChromeElement', None]:
        """返回上面某一级父元素，可指定层数或用查询语法定位              \n
        :param level_or_loc: 第几级父元素，或定位符
        :return: 上级元素对象
        """
        return super().parent(level_or_loc)

    def prev(self,
             index: int = 1,
             filter_loc: Union[tuple, str] = '',
             timeout: float = 0) -> Union['ChromeElement', str, None]:
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
             timeout: float = 0) -> Union['ChromeElement', str, None]:
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
               timeout: float = None) -> Union['ChromeElement', str, None]:
        """返回当前元素前面的一个元素，可指定筛选条件和第几个。查找范围不限兄弟元，而是整个DOM文档        \n
        :param index: 前面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 本元素前面的某个元素或节点
        """
        return super().before(index, filter_loc, timeout)

    def after(self,
              index: int = 1,
              filter_loc: Union[tuple, str] = '',
              timeout: float = None) -> Union['ChromeElement', str, None]:
        """返回当前元素后面的一个元素，可指定筛选条件和第几个。查找范围不限兄弟元，而是整个DOM文档        \n
        :param index: 后面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 本元素后面的某个元素或节点
        """
        return super().after(index, filter_loc, timeout)

    def prevs(self,
              filter_loc: Union[tuple, str] = '',
              timeout: float = 0) -> List[Union['ChromeElement', str]]:
        """返回前面全部兄弟元素或节点组成的列表，可用查询语法筛选        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素或节点文本组成的列表
        """
        return super().prevs(filter_loc, timeout)

    def nexts(self,
              filter_loc: Union[tuple, str] = '',
              timeout: float = 0) -> List[Union['ChromeElement', str]]:
        """返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 兄弟元素或节点文本组成的列表
        """
        return super().nexts(filter_loc, timeout)

    def befores(self,
                filter_loc: Union[tuple, str] = '',
                timeout: float = None) -> List[Union['ChromeElement', str]]:
        """返回当前元素后面符合条件的全部兄弟元素或节点组成的列表，可用查询语法筛选。查找范围不限兄弟元，而是整个DOM文档        \n
        :param filter_loc: 用于筛选元素的查询语法
        :param timeout: 查找元素的超时时间
        :return: 本元素前面的元素或节点组成的列表
        """
        return super().befores(filter_loc, timeout)

    def wait_ele(self,
                 loc_or_ele: Union[str, tuple, 'ChromeElement'],
                 timeout: float = None) -> 'ChromeElementWaiter':
        """等待子元素从dom删除、显示、隐藏                             \n
        :param loc_or_ele: 可以是元素、查询字符串、loc元组
        :param timeout: 等待超时时间
        :return: 等待是否成功
        """
        return ChromeElementWaiter(self, loc_or_ele, timeout)

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
            self.tag
            return True
        except Exception:
            return False

    @property
    def is_in_view(self) -> bool:
        """返回元素是否出现在视口中，已元素中点为判断"""
        js = """function(){
            const rect = this.getBoundingClientRect();
            x = rect.left+(rect.right-rect.left)/2;
            y = rect.top+(rect.bottom-rect.top)/2;
            const vWidth = window.innerWidth || document.documentElement.clientWidth;
            const vHeight = window.innerHeight || document.documentElement.clientHeight;
            if (x< 0 || y < 0 || x > vWidth || y > vHeight){return false;}
            return true;}"""
        return self.run_script(js)

    def attr(self, attr: str) -> Union[str, None]:
        """返回attribute属性值                           \n
        :param attr: 属性名
        :return: 属性值文本，没有该属性返回None
        """
        # 获取href属性时返回绝对url
        attrs = self.attrs
        if attr == 'href':
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
            timeout: float = None) -> Union['ChromeElement', str, None]:
        """返回当前元素下级符合条件的第一个元素、属性或节点文本                 \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与元素所在页面等待时间一致
        :return: ChromeElement对象或属性、文本
        """
        return self._ele(loc_or_str, timeout)

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None) -> List[Union['ChromeElement', str]]:
        """返回当前元素下级所有符合条件的子元素、属性或节点文本                 \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与元素所在页面等待时间一致
        :return: ChromeElement对象或属性、文本组成的列表
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
             timeout: float = None,
             single: bool = True) -> Union['ChromeElement', str, None, List[Union['ChromeElement', str]]]:
        """返回当前元素下级符合条件的子元素、属性或节点文本，默认返回第一个                                      \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间
        :param single: True则返回第一个，False则返回全部
        :return: ChromeElement对象
        """
        return make_chrome_ele(self, loc_or_str, single, timeout)

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
        :return: 是否设置成功
        """
        value = value.replace("'", "\\'")
        self.run_script(f'this.{prop}="{value}";')

    def set_attr(self, attr: str, value: str) -> None:
        """设置元素attribute属性          \n
        :param attr: 属性名
        :param value: 属性值
        :return: 是否设置成功
        """
        self.run_script(f'this.setAttribute(arguments[0], arguments[1]);', False, attr, str(value))

    def remove_attr(self, attr: str) -> None:
        """删除元素attribute属性          \n
        :param attr: 属性名
        :return: 是否删除成功
        """
        self.run_script(f'this.removeAttribute("{attr}");')

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
        path = path or '.'

        node = self.page.driver.DOM.describeNode(nodeId=self._node_id)['node']
        frame = node.get('frameId', None)
        frame = frame or self.page.current_tab_handle
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

        if modifier != 0:  # 包含组合键
            for key in vals:
                _send_key(self, modifier, key)
            return

        if vals.endswith('\n'):
            self.page.run_cdp('Input.insertText', text=vals[:-1])
            _send_key(self, modifier, '\n')
        else:
            self.page.run_cdp('Input.insertText', text=vals)

    def _set_file_input(self, files: Union[str, list, tuple]) -> None:
        """设置上传控件值"""
        if isinstance(files, str):
            files = files.split('\n')
        self.page.driver.DOM.setFileInputFiles(files=files, nodeId=self._node_id)

    def clear(self, by_js: bool = True) -> None:
        """清空元素文本                                    \n
        :param by_js: 是否用js方式清空
        :return: None
        """
        if by_js:
            self.run_script("this.value='';")

        else:
            self.input(('\ue009', 'a', '\ue017'), clear=False)

    def click(self, by_js: bool = None, timeout: float = None) -> bool:
        """点击元素                                                                      \n
        尝试点击直到超时，若都失败就改用js点击                                                \n
        :param by_js: 是否用js点击，为True时直接用js点击，为False时重试失败也不会改用js
        :param timeout: 尝试点击的超时时间，不指定则使用父页面的超时时间
        :return: 是否点击成功
        """

        def do_it(cx, cy, lx, ly) -> bool:
            r = self.page.driver.DOM.getNodeForLocation(x=lx + 1, y=ly + 1)
            if r.get('nodeId') != self._node_id:
                return False

            self._click(cx, cy)
            return True

        if not by_js:
            self.page.scroll_to_see(self)
            if self.is_in_view:
                xy = self.client_location
                location = self.location
                size = self.size
                client_x = xy['x'] + size['width'] // 2
                client_y = xy['y'] + size['height'] // 2
                loc_x = location['x'] + size['width'] // 2
                loc_y = location['y'] + size['height'] // 2

                timeout = timeout if timeout is not None else self.page.timeout
                end_time = perf_counter() + timeout
                click = do_it(client_x, client_y, loc_x, loc_y)
                while not click and perf_counter() < end_time:
                    click = do_it(client_x, client_y, location['x'], location['y'])

                if click:
                    return True

        if by_js is not False:
            js = 'this.click();'
            self.run_script(js)
            return True

        return False

    def click_at(self,
                 x: Union[int, str] = None,
                 y: Union[int, str] = None,
                 button: str = 'left') -> None:
        """带偏移量点击本元素，相对于左上角坐标。不传入x或y值时点击元素中点    \n
        :param x: 相对元素左上角坐标的x轴偏移量
        :param y: 相对元素左上角坐标的y轴偏移量
        :param button: 左键还是右键
        :return: None
        """
        x, y = _offset_scroll(self, x, y)
        self._click(x, y, button)

    def r_click(self) -> None:
        """右键单击"""
        self.page.scroll_to_see(self)
        xy = self.client_location
        size = self.size
        cx = xy['x'] + size['width'] // 2
        cy = xy['y'] + size['height'] // 2
        self._click(cx, cy, 'right')

    def r_click_at(self, x: Union[int, str], y: Union[int, str]) -> None:
        """带偏移量右键单击本元素，相对于左上角坐标。不传入x或y值时点击元素中点    \n
        :param x: 相对元素左上角坐标的x轴偏移量
        :param y: 相对元素左上角坐标的y轴偏移量
        :return: None
        """
        self.click_at(x, y, 'right')

    def _click(self, x: int, y: int, button: str = 'left') -> None:
        """实施点击"""
        self.page.driver.Input.dispatchMouseEvent(type='mousePressed', x=x, y=y, button=button, clickCount=1)
        sleep(.1)
        self.page.driver.Input.dispatchMouseEvent(type='mouseReleased', x=x, y=y, button=button)

    def hover(self, offset_x: int = None, offset_y: int = None) -> None:
        """鼠标悬停，可接受偏移量，偏移量相对于元素左上角坐标。不传入x或y值时悬停在元素中点    \n
        :param offset_x: 相对元素左上角坐标的x轴偏移量
        :param offset_y: 相对元素左上角坐标的y轴偏移量
        :return: None
        """
        x, y = _offset_scroll(self, offset_x, offset_y)
        self.page.driver.Input.dispatchMouseEvent(type='mouseMoved', x=x, y=y)

    def _get_obj_id(self, node_id) -> str:
        return self.page.driver.DOM.resolveNode(nodeId=node_id)['object']['objectId']

    def _get_node_id(self, obj_id) -> str:
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


class ChromeShadowRootElement(BaseElement):
    """ChromeShadowRootElement是用于处理ShadowRoot的类，使用方法和ChromeElement基本一致"""

    def __init__(self, parent_ele: ChromeElement, obj_id: str):
        super().__init__(parent_ele.page)
        self.parent_ele = parent_ele
        self._node_id = self._get_node_id(obj_id)
        self._obj_id = obj_id

    def __repr__(self) -> str:
        return f'<ShadowRootElement in {self.parent_ele} >'

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str],
                 timeout: float = None) -> Union[ChromeElement, str, None]:
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
    def node_id(self):
        return self._node_id

    @property
    def obj_id(self):
        return self._obj_id

    def _get_node_id(self, obj_id) -> str:
        return self.page.driver.DOM.requestNode(objectId=obj_id)['nodeId']

    @property
    def tag(self) -> str:
        """元素标签名"""
        return 'shadow-root'

    @property
    def html(self) -> str:
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

    def parent(self, level_or_loc: Union[str, int] = 1) -> ChromeElement:
        """返回上面某一级父元素，可指定层数或用查询语法定位              \n
        :param level_or_loc: 第几级父元素，或定位符
        :return: ChromeElement对象
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

        return self.parent_ele.ele(loc, timeout=0)

    def next(self,
             index: int = 1,
             filter_loc: Union[tuple, str] = '') -> Union[ChromeElement, str, None]:
        """返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param index: 第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :return: ChromeElement对象
        """
        nodes = self.nexts(filter_loc=filter_loc)
        return nodes[index - 1] if nodes else None

    def before(self,
               index: int = 1,
               filter_loc: Union[tuple, str] = '') -> Union[ChromeElement, str, None]:
        """返回前面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param index: 前面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :return: 本元素前面的某个元素或节点
        """
        nodes = self.befores(filter_loc=filter_loc)
        return nodes[index - 1] if nodes else None

    def after(self, index: int = 1,
              filter_loc: Union[tuple, str] = '') -> Union[ChromeElement, str, None]:
        """返回后面的一个兄弟元素，可用查询语法筛选，可指定返回筛选结果的第几个        \n
        :param index: 后面第几个查询结果元素
        :param filter_loc: 用于筛选元素的查询语法
        :return: 本元素后面的某个元素或节点
        """
        nodes = self.afters(filter_loc=filter_loc)
        return nodes[index - 1] if nodes else None

    def nexts(self, filter_loc: Union[tuple, str] = '') -> List[Union[ChromeElement, str]]:
        """返回后面所有兄弟元素或节点组成的列表        \n
        :param filter_loc: 用于筛选元素的查询语法
        :return: ChromeElement对象组成的列表
        """
        loc = get_loc(filter_loc, True)
        if loc[0] == 'css selector':
            raise ValueError('此css selector语法不受支持，请换成xpath。')

        loc = loc[1].lstrip('./')
        xpath = f'xpath:./{loc}'
        return self.parent_ele.eles(xpath, timeout=0.1)

    def befores(self, filter_loc: Union[tuple, str] = '') -> List[Union[ChromeElement, str]]:
        """返回后面全部兄弟元素或节点组成的列表，可用查询语法筛选        \n
        :param filter_loc: 用于筛选元素的查询语法
        :return: 本元素前面的元素或节点组成的列表
        """
        loc = get_loc(filter_loc, True)
        if loc[0] == 'css selector':
            raise ValueError('此css selector语法不受支持，请换成xpath。')

        loc = loc[1].lstrip('./')
        xpath = f'xpath:./preceding::{loc}'
        return self.parent_ele.eles(xpath, timeout=0.1)

    def afters(self, filter_loc: Union[tuple, str] = '') -> List[Union[ChromeElement, str]]:
        """返回前面全部兄弟元素或节点组成的列表，可用查询语法筛选        \n
        :param filter_loc: 用于筛选元素的查询语法
        :return: 本元素后面的元素或节点组成的列表
        """
        eles1 = self.nexts(filter_loc)
        loc = get_loc(filter_loc, True)[1].lstrip('./')
        xpath = f'xpath:./following::{loc}'
        return eles1 + self.parent_ele.eles(xpath, timeout=0.1)

    def ele(self,
            loc_or_str: Union[Tuple[str, str], str],
            timeout: float = None) -> Union[ChromeElement, str, None]:
        """返回当前元素下级符合条件的第一个元素，默认返回                                   \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与元素所在页面等待时间一致
        :return: ChromeElement对象或属性、文本
        """
        return self._ele(loc_or_str, timeout)

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None) -> List[Union[ChromeElement, str]]:
        """返回当前元素下级所有符合条件的子元素                                              \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与元素所在页面等待时间一致
        :return: ChromeElement对象或属性、文本组成的列表
        """
        return self._ele(loc_or_str, timeout=timeout, single=False)

    def s_ele(self, loc_or_ele=None) -> Union[SessionElement, str, None]:
        """查找第一个符合条件的元素以SessionElement形式返回，处理复杂页面时效率很高                 \n
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        return make_session_ele(self, loc_or_ele)

    def s_eles(self, loc_or_ele) -> List[Union[SessionElement, str]]:
        """查找所有符合条件的元素以SessionElement列表形式返回，处理复杂页面时效率很高                 \n
        :param loc_or_ele: 元素的定位信息，可以是loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        return make_session_ele(self, loc_or_ele, single=False)

    def _ele(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None,
             single: bool = True) -> Union['ChromeElement', str, None, List[Union['ChromeElement', str]]]:
        """返回当前元素下级符合条件的子元素、属性或节点文本，默认返回第一个                                      \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间
        :param single: True则返回第一个，False则返回全部
        :return: ChromeElement对象
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
            return ChromeElement(self.page, node_id) if node_id else None

        else:
            results = []
            for i in css_paths:
                node_id = self.page.driver.DOM.querySelector(nodeId=self._node_id, selector=i)['nodeId']
                if node_id:
                    results.append(ChromeElement(self.page, node_id))
            return results


def make_chrome_ele(ele: ChromeElement,
                    loc: Union[str, Tuple[str, str]],
                    single: bool = True,
                    timeout: float = None) -> Union[ChromeElement, str, None, List[Union[ChromeElement, str]]]:
    """在chrome元素中查找                                   \n
    :param ele: ChromeElement对象
    :param loc: 元素定位元组
    :param single: True则返回第一个，False则返回全部
    :param timeout: 查找元素超时时间
    :return: 返回ChromeElement元素或它们组成的列表
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
        return _find_by_xpath(ele, loc[1], single, timeout)

    else:
        return _find_by_css(ele, loc[1], single, timeout)


def _find_by_xpath(ele: ChromeElement, xpath: str, single: bool, timeout: float):
    type_txt = '9' if single else '7'
    node_txt = 'this.contentDocument' if ele.tag in ('iframe', 'frame') else 'this'
    js = _make_js(xpath, type_txt, node_txt)
    # print(js)
    r = ele.page.run_cdp('Runtime.callFunctionOn',
                         functionDeclaration=js, objectId=ele.obj_id, returnByValue=False, awaitPromise=True,
                         userGesture=True)
    if r['result']['type'] == 'string':
        return r['result']['value']

    if 'exceptionDetails' in r:
        if 'The result is not a node set' in r['result']['description']:
            js = _make_js(xpath, '1', node_txt)
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
            return ChromeElement(ele.page, obj_id=r['result']['objectId'])

    else:
        if r['result']['description'] == 'NodeList(0)':
            return []
        else:
            r = ele.page.driver.Runtime.getProperties(objectId=r['result']['objectId'], ownProperties=True)['result']
            return [ChromeElement(ele.page, obj_id=i['value']['objectId'])
                    if i['value']['type'] == 'object' else i['value']['value']
                    for i in r[:-1]]


def _find_by_css(ele: ChromeElement, selector: str, single: bool, timeout: float):
    selector = selector.replace('"', r'\"')
    find_all = '' if single else 'All'
    node_txt = 'this.contentDocument' if ele.tag in ('iframe', 'frame', 'shadow-root') else 'this'
    js = f'function(){{return {node_txt}.querySelector{find_all}("{selector}");}}'
    r = ele.page.run_cdp('Runtime.callFunctionOn',
                         functionDeclaration=js, objectId=ele.obj_id, returnByValue=False, awaitPromise=True,
                         userGesture=True)
    if 'exceptionDetails' in r:
        raise SyntaxError(f'查询语句错误：\n{r}')

    print(js)
    print(r)
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
            return ChromeElement(ele.page, obj_id=r['result']['objectId'])

    else:
        if r['result']['description'] == 'NodeList(0)':
            return []
        else:
            r = ele.page.driver.Runtime.getProperties(objectId=r['result']['objectId'], ownProperties=True)['result']
            return [ChromeElement(ele.page, obj_id=i['value']['objectId']) for i in r]


def _make_js(xpath: str, type_txt: str, node_txt: str):
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
    :return:
    """
    if isinstance(page_or_ele, (ChromeElement, ChromeShadowRootElement)):
        page = page_or_ele.page
        obj_id = page_or_ele.obj_id
    else:
        page = page_or_ele
        obj_id = page_or_ele.root.obj_id

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
        # print(script)
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

    # print(res)
    return _parse_js_result(page, page_or_ele, res.get('result'))


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
                return ChromeShadowRootElement(ele, obj_id=result['objectId'])
            else:
                return ChromeElement(page, obj_id=result['objectId'])

        elif sub_type == 'array':
            r = page.driver.Runtime.getProperties(objectId=result['result']['objectId'], ownProperties=True)['result']
            return [_parse_js_result(page, ele, result=i['value']) for i in r]

        else:
            return result['value']

    elif the_type == 'undefined':
        return None

    # elif the_type in ('string', 'number', 'boolean'):
    #     return result['value']

    else:
        return result['value']


def _convert_argument(arg: Any) -> dict:
    """把参数转换成js能够接收的形式"""
    if isinstance(arg, ChromeElement):
        return {'objectId': arg.obj_id}

    elif isinstance(arg, (int, float, str, bool)):
        return {'value': arg}

    from math import inf
    if arg == inf:
        return {'unserializableValue': 'Infinity'}
    if arg == -inf:
        return {'unserializableValue': '-Infinity'}

    # objectHandle = arg if isinstance(arg, JSHandle) else None
    # if objectHandle:
    #     if objectHandle._context != self:
    #         raise ElementHandleError('JSHandles can be evaluated only in the context they were created!')
    #     if objectHandle._disposed:
    #         raise ElementHandleError('JSHandle is disposed!')
    #     if objectHandle._remoteObject.get('unserializableValue'):
    #         return {'unserializableValue': objectHandle._remoteObject.get('unserializableValue')}  # noqa: E501
    #     if not objectHandle._remoteObject.get('objectId'):
    #         return {'value': objectHandle._remoteObject.get('value')}
    #     return {'objectId': objectHandle._remoteObject.get('objectId')}
    # return {'value': arg}


def _offset_scroll(ele: ChromeElement, x: int, y: int):
    location = ele.location
    size = ele.size
    lx = location['x'] + int(x) if x is not None else location['x'] + size['width'] // 2
    ly = location['y'] + int(y) if y is not None else location['y'] + size['height'] // 2

    ele.page.scroll.to_location(lx - 5, ly - 5)
    cl = ele.client_location
    x = cl['x'] + int(x) if x is not None else cl['x'] + size['width'] // 2
    y = cl['y'] + int(y) if y is not None else cl['y'] + size['height'] // 2
    return x, y


def _send_enter(ele: ChromeElement):
    # todo:windows系统回车是否不一样
    data = {'type': 'keyDown', 'modifiers': 0, 'windowsVirtualKeyCode': 13, 'code': 'Enter', 'key': 'Enter',
            'text': '\r', 'autoRepeat': False, 'unmodifiedText': '\r', 'location': 0, 'isKeypad': False}

    ele.page.run_cdp('Input.dispatchKeyEvent', **data)
    data['type'] = 'keyUp'
    ele.page.run_cdp('Input.dispatchKeyEvent', **data)


def _send_key(ele: ChromeElement, modifier: int, key: str) -> None:
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


class ChromeScroll(object):
    """用于滚动的对象"""

    def __init__(self, page_or_ele):
        """
        :param page_or_ele: ChromePage或ChromeElement
        """
        if isinstance(page_or_ele, ChromeElement):
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

    def __init__(self, ele: ChromeElement):
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
    def options(self) -> List[ChromeElement]:
        """返回所有选项元素组成的列表"""
        return self._ele.eles('tag:option')

    @property
    def selected_option(self) -> Union[ChromeElement, None]:
        """返回第一个被选中的option元素        \n
        :return: ChromeElement对象或None
        """
        ele = self._ele.run_script('return this.options[this.selectedIndex];')
        return ele

    @property
    def selected_options(self) -> List[ChromeElement]:
        """返回所有被选中的option元素列表        \n
        :return: ChromeElement对象组成的列表
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


class ChromeElementWaiter(object):
    """等待元素在dom中某种状态，如删除、显示、隐藏"""

    def __init__(self,
                 page_or_ele,
                 loc_or_ele: Union[str, tuple, ChromeElement],
                 timeout: float = None):
        """等待元素在dom中某种状态，如删除、显示、隐藏                         \n
        :param page_or_ele: 页面或父元素
        :param loc_or_ele: 要等待的元素，可以是已有元素、定位符
        :param timeout: 超时时间，默认读取页面超时时间
        """
        if not isinstance(loc_or_ele, (str, tuple, ChromeElement)):
            raise TypeError('loc_or_ele只能接收定位符或元素对象。')

        self.driver = page_or_ele
        self.loc_or_ele = loc_or_ele
        if timeout is not None:
            self.timeout = timeout
        else:
            self.timeout = page_or_ele.page.timeout if isinstance(page_or_ele, ChromeElement) else page_or_ele.timeout

    def delete(self) -> bool:
        """等待元素从dom删除"""
        if isinstance(self.loc_or_ele, ChromeElement):
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
