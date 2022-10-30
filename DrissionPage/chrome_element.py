# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   chrome_element.py
"""
from typing import Union, Tuple, List
from time import perf_counter

from .session_element import make_session_ele
from .base import DrissionElement
from .common import make_absolute_link, get_loc, get_ele_txt, format_html


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

    @property
    def obj_id(self) -> str:
        return self._obj_id

    @property
    def node_id(self) -> str:
        return self._node_id

    @property
    def html(self) -> str:
        """返回元素outerHTML文本"""
        return self.page.driver.DOM.getOuterHTML(nodeId=self._node_id)['outerHTML']

    @property
    def tag(self) -> str:
        return self.page.driver.DOM.describeNode(nodeId=self._node_id)['node']['localName']

    @property
    def inner_html(self) -> str:
        """返回元素innerHTML文本"""
        return self.page.driver.Runtime.callFunctionOn(functionDeclaration='function(){return this.innerHTML;}',
                                                       objectId=self._obj_id)['result']['value']

    @property
    def attrs(self) -> dict:
        attrs = self.page.driver.DOM.getAttributes(nodeId=self._node_id)['attributes']
        attrs_len = len(attrs)
        return {attrs[i]: attrs[i + 1] for i in range(0, attrs_len, 2)}

    @property
    def size(self) -> dict:
        """返回元素宽和高"""
        model = self.page.driver.DOM.getBoxModel(nodeId=self._node_id)['model']
        return {"height": model['height'], "width": model['width']}

    @property
    def client_location(self) -> dict:
        """返回元素左上角坐标"""
        js = '''function(){
        return this.getBoundingClientRect().left.toString()+" "+this.getBoundingClientRect().top.toString();}'''
        xy = self.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=self._obj_id)['result']['value']
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
        xy = self.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=self._obj_id)['result']['value']
        x, y = xy.split(' ')
        return {'x': int(x.split('.')[0]), 'y': int(y.split('.')[0])}

    @property
    def scroll(self) -> 'ChromeScroll':
        """用于滚动滚动条的对象"""
        if self._scroll is None:
            self._scroll = ChromeScroll(self)
        return self._scroll

    def ele(self,
            loc_or_str: Union[Tuple[str, str], str],
            timeout: float = None) -> Union['ChromeElement', str, None]:
        """返回当前元素下级符合条件的第一个元素、属性或节点文本                 \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与元素所在页面等待时间一致
        :return: DriverElement对象或属性、文本
        """
        return self._ele(loc_or_str, timeout)

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None) -> List[Union['ChromeElement', str]]:
        """返回当前元素下级所有符合条件的子元素、属性或节点文本                 \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间，默认与元素所在页面等待时间一致
        :return: DriverElement对象或属性、文本组成的列表
        """
        return self._ele(loc_or_str, timeout=timeout, single=False)

    def _ele(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None,
             single: bool = True) -> Union['ChromeElement', str, None, List[Union['ChromeElement', str]]]:
        """返回当前元素下级符合条件的子元素、属性或节点文本，默认返回第一个                                      \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 查找元素超时时间
        :param single: True则返回第一个，False则返回全部
        :return: DriverElement对象
        """
        return make_chrome_ele(self, loc_or_str, single, timeout)

    def attr(self, attr: str) -> Union[str, None]:
        """返回attribute属性值                           \n
        :param attr: 属性名
        :return: 属性值文本，没有该属性返回None
        """
        # 获取href属性时返回绝对url
        attrs = self.attrs
        if attr == 'href':
            link = attrs['href']
            # 若为链接为None、js或邮件，直接返回
            if not link or link.lower().startswith(('javascript:', 'mailto:')):
                return link

            else:  # 其它情况直接返回绝对url
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
            return attrs[attr]

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
        r = self.page.driver.Runtime.callFunctionOn(functionDeclaration=f"function(){{this.{prop}='{value}';}}",
                                                    objectId=self._obj_id)
        if 'exceptionDetails' in r:
            raise SyntaxError(r['result']['description'])

    def click(self, by_js: bool = False) -> None:
        """点击元素                                                                      \n
        尝试点击直到超时，若都失败就改用js点击                                                \n
        :param by_js: 是否用js点击，为True时直接用js点击，为False时重试失败也不会改用js
        :return: 是否点击成功
        """
        if by_js:
            js = 'function(){this.click();}'
            self.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=self._obj_id)
            return

        self.page.driver.DOM.scrollIntoViewIfNeeded(nodeId=self._node_id)
        xy = self.client_location
        size = self.size
        x = xy['x'] + size['width'] // 2
        y = xy['y'] + size['height'] // 2
        self.page.driver.Input.dispatchMouseEvent(type='mousePressed', x=x, y=y, button='left', clickCount=1)
        self.page.driver.Input.dispatchMouseEvent(type='mouseReleased', x=x, y=y, button='left')

        # js = """function(){const event=new MouseEvent('click',{view:window, bubbles:true, cancelable:true});
        # this.dispatchEvent(event);}"""
        # self.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=self._obj_id)

    def _get_obj_id(self, node_id) -> str:
        return self.page.driver.DOM.resolveNode(nodeId=node_id)['object']['objectId']

    def _get_node_id(self, obj_id) -> str:
        return self.page.driver.DOM.requestNode(objectId=obj_id)['nodeId']

    @property
    def is_valid(self):
        return True

    @property
    def text(self) -> str:
        """返回元素内所有文本"""
        return get_ele_txt(make_session_ele(self.html))

    @property
    def raw_text(self):
        return

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
        t = self.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=self._obj_id)['result']['value']
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
    :return: 返回DriverElement元素或它们组成的列表
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
    r = ele.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=ele.obj_id)
    if r['result']['type'] == 'string':
        return r['result']['value']

    if 'exceptionDetails' in r:
        if 'The result is not a node set' in r['result']['description']:
            js = _make_js(xpath, '1', node_txt)
            r = ele.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=ele.obj_id)
            return r['result']['value']
        else:
            raise SyntaxError(f'查询语句错误：\n{r}')

    t1 = perf_counter()
    while (r['result']['subtype'] == 'null'
           or r['result']['description'] == 'NodeList(0)') and perf_counter() - t1 < timeout:
        r = ele.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=ele.obj_id)

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
    js = f'function(){{return this.querySelector{find_all}("{selector}");}}'
    r = ele.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=ele.obj_id)
    if 'exceptionDetails' in r:
        raise SyntaxError(f'查询语句错误：\n{r}')

    t1 = perf_counter()
    while (r['result']['subtype'] == 'null'
           or r['result']['description'] == 'NodeList(0)') and perf_counter() - t1 < timeout:
        r = ele.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=ele.obj_id)

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
            js = f'function(){{{js}}}'
            self.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=self.obj_id)
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
