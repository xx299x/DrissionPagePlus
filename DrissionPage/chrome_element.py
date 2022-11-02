# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
@File    :   chrome_element.py
"""
from pathlib import Path
from typing import Union, Tuple, List, Any
from time import perf_counter, sleep

from .session_element import make_session_ele
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
        return self.run_script('this.innerHTML;')['result']['value']

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
        js = 'this.getBoundingClientRect().left.toString()+" "+this.getBoundingClientRect().top.toString();'
        xy = self.run_script(js)['result']['value']
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
        xy = self.run_script(js)['result']['value']
        x, y = xy.split(' ')
        return {'x': int(x.split('.')[0]), 'y': int(y.split('.')[0])}

    @property
    def scroll(self) -> 'ChromeScroll':
        """用于滚动滚动条的对象"""
        if self._scroll is None:
            self._scroll = ChromeScroll(self)
        return self._scroll

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
        return self.run_script(js)['result']['value']

    def run_script(self, script: str, as_expr: bool = False, *args: Any) -> Any:
        """运行javascript代码                                                 \n
        :param script: js文本
        :param as_expr: 是否作为表达式运行，为True时args无效
        :param args: 参数，按顺序在js文本中对应argument[0]、argument[2]...
        :return: 运行的结果
        """
        return run_script(self, script, as_expr, *args)

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
        r = self.run_script(f'this.{prop}="{value}";')
        if 'exceptionDetails' in r:
            raise SyntaxError(r['result']['description'])

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
        t = self.run_script(js)['result']['value']
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
    r = ele.run_script(js)
    if r['result']['type'] == 'string':
        return r['result']['value']

    if 'exceptionDetails' in r:
        if 'The result is not a node set' in r['result']['description']:
            js = _make_js(xpath, '1', node_txt)
            r = ele.run_script(js)
            return r['result']['value']
        else:
            raise SyntaxError(f'查询语句错误：\n{r}')

    t1 = perf_counter()
    while (r['result']['subtype'] == 'null'
           or r['result']['description'] == 'NodeList(0)') and perf_counter() - t1 < timeout:
        r = ele.run_script(js)

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
    r = ele.run_script(js)
    if 'exceptionDetails' in r:
        raise SyntaxError(f'查询语句错误：\n{r}')

    t1 = perf_counter()
    while (r['result']['subtype'] == 'null'
           or r['result']['description'] == 'NodeList(0)') and perf_counter() - t1 < timeout:
        r = ele.run_script(js)

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


def run_script(page_or_ele, script: str, as_expr: bool = False, *args: Any) -> Any:
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
                           # contextId=self._contextId,
                           returnByValue=False,
                           awaitPromise=True,
                           userGesture=True)
    else:
        if not is_js_func(script):
            script = script if script.strip().startswith('return') else f'return {script}'
            script = f'function(){{{script}}}'
        res = page.run_cdp('Runtime.callFunctionOn',
                           functionDeclaration=script,
                           objectId=obj_id,
                           # 'executionContextId': self._contextId,
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
