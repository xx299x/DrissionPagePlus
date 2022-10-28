# -*- coding:utf-8 -*-
# 问题：跨iframe查找元素可能出现同名元素如何解决
# 须用DOM.documentUpdated检测元素有效性
from typing import Union, Tuple, List

from .base import DrissionElement
from .common import make_absolute_link, get_loc


class ChromeElement(DrissionElement):
    def __init__(self, page, node_id: str = None, obj_id: str = None):
        super().__init__(page)
        if not node_id and not obj_id:
            raise TypeError('node_id或obj_id必须传入一个。')

        if node_id:
            self._node_id = node_id
            self._obj_id = self._get_obj_id(node_id)
        else:
            self._node_id = self._get_node_id(obj_id)
            self._obj_id = obj_id

    @property
    def html(self) -> str:
        """返回元素outerHTML文本"""
        return self.page.driver.DOM.getOuterHTML(nodeId=self._node_id)['outerHTML']

    @property
    def inner_html(self) -> str:
        """返回元素innerHTML文本"""
        return self.page.driver.Runtime.callFunctionOn('function(){this.innerHTML;}')

    @property
    def attrs(self) -> dict:
        attrs = self.page.driver.DOM.getAttributes(nodeId=self._node_id)['attributes']
        attrs_len = len(attrs)
        return {attrs[i]: attrs[i + 1] for i in range(0, attrs_len, 2)}

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

    def click(self, by_js: bool = True):
        if by_js:
            js = 'function(){this.click();}'
            self.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=self._obj_id)

    def _get_obj_id(self, node_id):
        return self.page.driver.DOM.resolveNode(nodeId=node_id)['object']['objectId']

    def _get_node_id(self, obj_id):
        return self.page.driver.DOM.requestNode(objectId=obj_id)['nodeId']

    @property
    def tag(self) -> str:
        return self.page.driver.DOM.describeNode(nodeId=self._node_id)['node']['localName']

    @property
    def is_valid(self):
        return True

    @property
    def text(self):
        return

    @property
    def raw_text(self):
        return

    def _get_ele_path(self, mode):
        return ''


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
        type_txt = '9' if single else '7'
        node_txt = 'this.contentDocument' if ele.tag in ('iframe', 'frame') else 'this'
        js = _make_js(loc[1], type_txt, node_txt)
        print(js)
        r = ele.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=ele._obj_id,)
        # print(r)
        if r['result']['type'] == 'string':
            return r['result']['value']
        if r['result']['subtype'] == 'null':
            return None if single else []
        if r['result']['className'] == 'TypeError':
            if 'The result is not a node set' in r['result']['description']:
                js = _make_js(loc[1], '1', node_txt)
                r = ele.page.driver.Runtime.callFunctionOn(functionDeclaration=js, objectId=ele._obj_id)
                return r['result']['value']

            else:
                raise RuntimeError(r['result']['description'])

        elif 'objectId' in r['result']:
            if not single:
                r = ele.page.driver.Runtime.getProperties(objectId=r['result']['objectId'])['result']
                result = []
                for i in r:
                    if not i['enumerable']:
                        break
                    result.append(ChromeElement(ele.page, obj_id=i['value']['objectId']))
                r = result

            return r

    # try:
    #     # 使用xpath查找
    #     if loc[0] == 'xpath':
    #         js = _make_js()
    #         r = ele.page.driver.Runtime.callFunctionOn(functionDeclaration=js,
    #                                                     objectId=self._obj_id)['result'].get('objectId', None)
    #         return r if not r else _ele(self.page, obj_id=r)
    #
    #         return wait.until(ElementsByXpath(page, loc[1], single, timeout))
    #
    #     # 使用css selector查找
    #     else:
    #         if single:
    #             return DriverElement(wait.until(ec.presence_of_element_located(loc)), page)
    #         else:
    #             eles = wait.until(ec.presence_of_all_elements_located(loc))
    #             return [DriverElement(ele, page) for ele in eles]
    #
    # except TimeoutException:
    #     return [] if not single else None
    #
    # except InvalidElementStateException:
    #     raise ValueError(f'无效的查找语句：{loc}')


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

# class ElementsByXpath(object):
#     """用js通过xpath获取元素、节点或属性，与WebDriverWait配合使用"""
#
#     def __init__(self, page, xpath: str = None, single: bool = False, timeout: float = 10):
#         """
#         :param page: DrissionPage对象
#         :param xpath: xpath文本
#         :param single: True则返回第一个，False则返回全部
#         :param timeout: 超时时间
#         """
#         self.page = page
#         self.xpath = xpath
#         self.single = single
#         self.timeout = timeout
#
#     def __call__(self, ele_or_driver: Union[RemoteWebDriver, WebElement]) \
#             -> Union[str, DriverElement, None, List[str or DriverElement]]:
#
#         def get_nodes(node=None, xpath_txt=None, type_txt='7'):
#             """用js通过xpath获取元素、节点或属性
#             :param node: 'document' 或 元素对象
#             :param xpath_txt: xpath语句
#             :param type_txt: resultType,参考 https://developer.mozilla.org/zh-CN/docs/Web/API/Document/evaluate
#             :return: 元素对象或属性、文本字符串
#             """
#             node_txt = 'document' if not node or node == 'document' else 'arguments[0]'
#             for_txt = ''
#
#             # 获取第一个元素、节点或属性
#             if type_txt == '9':
#                 return_txt = '''
#                     if(e.singleNodeValue.constructor.name=="Text"){return e.singleNodeValue.data;}
#                     else if(e.singleNodeValue.constructor.name=="Attr"){return e.singleNodeValue.nodeValue;}
#                     else if(e.singleNodeValue.constructor.name=="Comment"){return e.singleNodeValue.nodeValue;}
#                     else{return e.singleNodeValue;}
#                     '''
#
#             # 按顺序获取所有元素、节点或属性
#             elif type_txt == '7':
#                 for_txt = """
#                     var a=new Array();
#                     for(var i = 0; i <e.snapshotLength ; i++){
#                         if(e.snapshotItem(i).constructor.name=="Text"){a.push(e.snapshotItem(i).data);}
#                         else if(e.snapshotItem(i).constructor.name=="Attr"){a.push(e.snapshotItem(i).nodeValue);}
#                         else if(e.snapshotItem(i).constructor.name=="Comment"){a.push(e.snapshotItem(i).nodeValue);}
#                         else{a.push(e.snapshotItem(i));}
#                     }
#                     """
#                 return_txt = 'return a;'
#
#             elif type_txt == '2':
#                 return_txt = 'return e.stringValue;'
#             elif type_txt == '1':
#                 return_txt = 'return e.numberValue;'
#             else:
#                 return_txt = 'return e.singleNodeValue;'
#
#             js = """
#                 var e=document.evaluate(arguments[1], """ + node_txt + """, null, """ + type_txt + """,null);
#                 """ + for_txt + """
#                 """ + return_txt + """
#                 """
#             return driver.execute_script(js, node, xpath_txt)
#
#         if isinstance(ele_or_driver, RemoteWebDriver):
#             driver, the_node = ele_or_driver, 'document'
#         else:
#             driver, the_node = ele_or_driver.parent, ele_or_driver
#
#         # 把lxml元素对象包装成DriverElement对象并按需要返回第一个或全部
#         if self.single:
#             try:
#                 e = get_nodes(the_node, xpath_txt=self.xpath, type_txt='9')
#
#                 if isinstance(e, WebElement):
#                     return DriverElement(e, self.page)
#                 elif isinstance(e, str):
#                     return format_html(e)
#                 else:
#                     return e
#
#             # 找不到目标时
#             except JavascriptException as err:
#                 if 'The result is not a node set' in err.msg:
#                     try:
#                         return get_nodes(the_node, xpath_txt=self.xpath, type_txt='1')
#                     except JavascriptException:
#                         return None
#                 else:
#                     return None
#
#         else:  # 返回全部
#             return ([DriverElement(x, self.page) if isinstance(x, WebElement)
#                      else format_html(x)
#                      for x in get_nodes(the_node, xpath_txt=self.xpath)
#                      if x != '\n'])
