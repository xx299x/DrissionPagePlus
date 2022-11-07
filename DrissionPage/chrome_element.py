# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   chrome_element.py
"""
from pathlib import Path
from re import search
from typing import Union, Tuple, List, Any
from time import perf_counter, sleep

from .session_element import make_session_ele, SessionElement
from .base import DrissionElement
from .common import make_absolute_link, get_loc, get_ele_txt, format_html, is_js_func


class ChromeElement(DrissionElement):
    def __init__(self, page, node_id: str = None, obj_id: str = None):
        super().__init__(page)
        self._select = None
        self._scroll = None
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
        return f'<ChromeElement {self.tag} {" ".join(attrs)}>'

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
        return self.page.driver.DOM.describeNode(nodeId=self._node_id)['node']['localName']

    @property
    def inner_html(self) -> str:
        """返回元素innerHTML文本"""
        if self.tag in ('iframe', 'frame'):
            return _run_script(self, 'this.contentDocument.documentElement;').html
            # return _run_script(self, 'this.contentDocument.body;').html
        return self.run_script('this.innerHTML;')

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
        js = 'this.getBoundingClientRect().left.toString()+" "+this.getBoundingClientRect().top.toString();'
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

    # @property
    # def shadow_root(self):
    #     """返回当前元素的shadow_root元素对象"""
    #     shadow = self.run_script('return arguments[0].shadowRoot')
    #     if shadow:
    #         from .shadow_root_element import ShadowRootElement
    #         return ShadowRootElement(shadow, self)
    #
    # @property
    # def sr(self):
    #     """返回当前元素的shadow_root元素对象"""
    #     return self.shadow_root

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

    # def wait_ele(self,
    #              loc_or_ele: Union[str, tuple, 'ChromeElement'],
    #              timeout: float = None) -> 'ElementWaiter':
    #     """等待子元素从dom删除、显示、隐藏                             \n
    #     :param loc_or_ele: 可以是元素、查询字符串、loc元组
    #     :param timeout: 等待超时时间
    #     :return: 等待是否成功
    #     """
    #     return ElementWaiter(self, loc_or_ele, timeout)

    @property
    def select(self):
        """返回专门处理下拉列表的Select类，非下拉列表元素返回False"""
        if self._select is None:
            if self.tag != 'select':
                self._select = False
            else:
                self._select = ChromeSelect(self)

        return self._select

    @property
    def is_selected(self):
        return self.run_script('this.selected;')

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
            link = attrs['href']
            if not link or link.lower().startswith(('javascript:', 'mailto:')):
                return link
            else:
                return make_absolute_link(link, self.page)

        elif attr == 'src':
            return make_absolute_link(attrs['src'], self.page)

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
        return _run_script(self, script, as_expr, args)

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
        js = f'function(){{return window.getComputedStyle(this{pseudo_ele}).getPropertyValue("{style}");}}'
        return self.run_script(js)

    def get_screenshot(self, path: [str, Path] = None,
                       as_bytes: [bool, str] = None) -> Union[str, bytes]:
        """对当前元素截图                                                                            \n
        :param path: 完整路径，后缀可选'jpg','jpeg','png','webp'
        :param as_bytes: 是否已字节形式返回图片，可选'jpg','jpeg','png','webp'，生效时path参数无效
        :return: 图片完整路径或字节文本
        """
        if self.tag == 'img':  # 等待图片加载完成
            js = ('arguments[0].complete && typeof arguments[0].naturalWidth != "undefined" '
                  '&& arguments[0].naturalWidth > 0 && typeof arguments[0].naturalHeight != "undefined" '
                  '&& arguments[0].naturalHeight > 0')
            t1 = perf_counter()
            while not self.run_script(js) and perf_counter() - t1 < self.page.timeout:
                pass

        left, top = self.location.values()
        height, width = self.size.values()
        left_top = (left, top)
        right_bottom = (left + width, top + height)
        return self.page.get_screenshot(path, as_bytes=as_bytes, full_page=False,
                                        left_top=left_top, right_bottom=right_bottom)

    def clean(self, by_js: bool = True):
        if by_js:
            js = "this.value='';"
            self.run_script(js)

        else:
            self.page.driver.DOM.focus(nodeId=self._node_id)
            self.page.driver.Input.dispatchKeyEvent(type='char',
                                                    modifiers=2, code='KeyA')

    # def input(self,
    #           vals: Union[str, tuple],
    #           clear: bool = True,
    #           insure: bool = True,
    #           timeout: float = None) -> bool:
    #     """输入文本或组合键，也可用于输入文件路径到input元素（文件间用\n间隔）                          \n
    #     :param vals: 文本值或按键组合
    #     :param clear: 输入前是否清空文本框
    #     :param insure: 确保输入正确，解决文本框有时输入失效的问题，不能用于输入组合键
    #     :param timeout: 尝试输入的超时时间，不指定则使用父页面的超时时间，只在insure为True时生效
    #     :return: bool
    #     """
    #     if not insure or self.tag != 'input' or self.prop('type') != 'text':  # 普通输入
    #         if not isinstance(vals, (str, tuple)):
    #             vals = str(vals)
    #         if clear:
    #             self.inner_ele.clear()
    #
    #         self.inner_ele.send_keys(*vals)
    #         return True
    #
    #     else:  # 确保输入正确
    #         if not isinstance(vals, str):
    #             vals = str(vals)
    #         enter = '\n' if vals.endswith('\n') else None
    #         full_txt = vals if clear else f'{self.attr("value")}{vals}'
    #         full_txt = full_txt.rstrip('\n')
    #
    #         self.click(by_js=True)
    #         timeout = timeout if timeout is not None else self.page.timeout
    #         t1 = perf_counter()
    #         while self.is_valid() and self.attr('value') != full_txt and perf_counter() - t1 <= timeout:
    #             try:
    #                 if clear:
    #                     self.inner_ele.send_keys(u'\ue009', 'a', u'\ue017')  # 有些ui下clear()不生效，用CTRL+a代替
    #                 self.inner_ele.send_keys(vals)
    #
    #             except Exception:
    #                 pass
    #
    #         if not self.is_valid():
    #             return False
    #         else:
    #             if self.attr('value') != full_txt:
    #                 return False
    #             else:
    #                 if enter:
    #                     self.inner_ele.send_keys(enter)
    #                 return True

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
            self.page.driver.Input.dispatchMouseEvent(type='mousePressed', x=cx, y=cy, button='left', clickCount=1)
            sleep(.1)
            self.page.driver.Input.dispatchMouseEvent(type='mouseReleased', x=cx, y=cy, button='left')
            return True

        if not by_js:
            self.page.scroll_to_see(self)
            if self.is_in_view:
                timeout = timeout if timeout is not None else self.page.timeout
                xy = self.client_location
                location = self.location
                size = self.size
                client_x = xy['x'] + size['width'] // 2
                client_y = xy['y'] + size['height'] // 2
                loc_x = location['x'] + size['width'] // 2
                loc_y = location['y'] + size['height'] // 2

                t1 = perf_counter()
                click = do_it(client_x, client_y, loc_x, loc_y)
                while not click and perf_counter() - t1 <= timeout:
                    click = do_it(client_x, client_y, location['x'], location['y'])

                if click:
                    return True

        if by_js is not False:
            js = 'function(){this.click();}'
            self.run_script(js)
            return True

        return False

    def click_at(self):
        pass

    def _get_obj_id(self, node_id) -> str:
        return self.page.driver.DOM.resolveNode(nodeId=node_id)['object']['objectId']

    def _get_node_id(self, obj_id) -> str:
        return self.page.driver.DOM.requestNode(objectId=obj_id)['nodeId']

    @property
    def is_valid(self):
        return True

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

    t1 = perf_counter()
    while (r['result']['subtype'] == 'null'
           or r['result']['description'] == 'NodeList(0)') and perf_counter() - t1 < timeout:
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
    js = f'this.querySelector{find_all}("{selector}");'
    r = ele.page.run_cdp('Runtime.callFunctionOn',
                         functionDeclaration=js, objectId=ele.obj_id, returnByValue=False, awaitPromise=True,
                         userGesture=True)
    if 'exceptionDetails' in r:
        raise SyntaxError(f'查询语句错误：\n{r}')

    t1 = perf_counter()
    while (r['result']['subtype'] == 'null'
           or r['result']['description'] == 'NodeList(0)') and perf_counter() - t1 < timeout:
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


def _run_script(page_or_ele, script: str, as_expr: bool = False, args: tuple = None) -> Any:
    """运行javascript代码                                                 \n
    :param page_or_ele: 页面对象或元素对象
    :param script: js文本
    :param as_expr: 是否作为表达式运行，为True时args无效
    :param args: 参数，按顺序在js文本中对应argument[0]、argument[2]...
    :return:
    """
    if isinstance(page_or_ele, ChromeElement):
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
                           userGesture=True)
    else:
        args = args or ()
        if not is_js_func(script):
            script = script if script.strip().startswith('return') else f'return {script}'
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
    return _parse_js_result(page, res.get('result'))


def _parse_js_result(page, result: dict):
    """解析js返回的结果"""
    if 'unserializableValue' in result:
        return result['unserializableValue']

    the_type = result['type']

    if the_type == 'object':
        sub_type = result['subtype']
        if sub_type == 'null':
            return None

        elif sub_type == 'node':
            return ChromeElement(page, obj_id=result['objectId'])

        elif sub_type == 'array':
            r = page.driver.Runtime.getProperties(objectId=result['result']['objectId'], ownProperties=True)['result']
            return [_parse_js_result(page, result=i['value']) for i in r]

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
        ele = self._ele.run_script('this.options[this.selectedIndex];')
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
            if opt.is_selected():
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
            t1 = perf_counter()
            ok = do_select()
            while not ok and perf_counter() - t1 < timeout:
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

# class ElementWaiter(object):
#     """等待元素在dom中某种状态，如删除、显示、隐藏"""
#
#     def __init__(self,
#                  page_or_ele,
#                  loc_or_ele: Union[str, tuple, ChromeElement],
#                  timeout: float = None):
#         """等待元素在dom中某种状态，如删除、显示、隐藏                         \n
#         :param page_or_ele: 页面或父元素
#         :param loc_or_ele: 要等待的元素，可以是已有元素、定位符
#         :param timeout: 超时时间，默认读取页面超时时间
#         """
#         if isinstance(page_or_ele, ChromeElement):
#             page = page_or_ele.page
#             self.driver = page_or_ele.inner_ele
#         else:
#             page = page_or_ele
#             self.driver = page_or_ele.driver
#
#         if isinstance(loc_or_ele, ChromeElement):
#             self.target = loc_or_ele.inner_ele
#
#         elif isinstance(loc_or_ele, str):
#             self.target = str_to_loc(loc_or_ele)
#
#         elif isinstance(loc_or_ele, tuple):
#             self.target = loc_or_ele
#
#         else:
#             raise TypeError('loc_or_ele参数只能是str、tuple、DriverElement 或 WebElement类型。')
#
#         self.timeout = timeout if timeout is not None else page.timeout
#
#     def delete(self) -> bool:
#         """等待元素从dom删除"""
#         return self._wait_ele('del')
#
#     def display(self) -> bool:
#         """等待元素从dom显示"""
#         return self._wait_ele('display')
#
#     def hidden(self) -> bool:
#         """等待元素从dom隐藏"""
#         return self._wait_ele('hidden')
#
#     def _wait_ele(self, mode: str) -> bool:
#         """执行等待
#         :param mode: 等待模式
#         :return: 是否等待成功
#         """
#         if isinstance(self.target, WebElement):
#             end_time = time() + self.timeout
#             while time() < end_time:
#                 if mode == 'del':
#                     try:
#                         self.target.is_enabled()
#                     except Exception:
#                         return True
#
#                 elif mode == 'display' and self.target.is_displayed():
#                     return True
#
#                 elif mode == 'hidden' and not self.target.is_displayed():
#                     return True
#
#             return False
#
#         else:
#             try:
#                 if mode == 'del':
#                     WebDriverWait(self.driver, self.timeout).until_not(ec.presence_of_element_located(self.target))
#
#                 elif mode == 'display':
#                     WebDriverWait(self.driver, self.timeout).until(ec.visibility_of_element_located(self.target))
#
#                 elif mode == 'hidden':
#                     WebDriverWait(self.driver, self.timeout).until_not(ec.visibility_of_element_located(self.target))
#
#                 return True
#
#             except Exception:
#                 return False
