# -*- coding:utf-8 -*-
from time import sleep

from .common import _location_in_viewport
from .keys import _modifierBit, _keyDescriptionForString


class ActionChains:
    """用于实现动作链的类"""

    def __init__(self, page):
        """初始化                          \n
        :param page: ChromiumPage对象
        """
        self.page = page
        self._dr = page.driver
        self.modifier = 0  # 修饰符，Alt=1, Ctrl=2, Meta/Command=4, Shift=8
        self.curr_x = 0  # 视口坐标
        self.curr_y = 0

    def move_to(self, ele_or_loc, offset_x=0, offset_y=0):
        """鼠标移动到元素中点，或页面上的某个绝对坐标。可设置偏移量          \n
        当带偏移量时，偏移量相对于元素左上角坐标
        :param ele_or_loc: 元素对象或绝对坐标，坐标为tuple(int, int)形式
        :param offset_x: 偏移量x
        :param offset_y: 偏移量y
        :return: self
        """
        if isinstance(ele_or_loc, (tuple, list)):
            lx = ele_or_loc[0] + offset_x
            ly = ele_or_loc[1] + offset_y
        elif 'ChromiumElement' in str(type(ele_or_loc)):
            x, y = ele_or_loc.location if offset_x or offset_y else ele_or_loc.midpoint
            lx = x + offset_x
            ly = y + offset_y
        else:
            raise TypeError('ele_or_loc参数只能接受坐标(x, y)或ChromiumElement对象。')

        if not _location_in_viewport(self.page, lx, ly):
            self.page.scroll.to_location(lx, ly)

        cx, cy = location_to_client(self.page, lx, ly)
        self._dr.Input.dispatchMouseEvent(type='mouseMoved', x=cx, y=cy, modifiers=self.modifier)
        self.curr_x = cx
        self.curr_y = cy
        return self

    def move(self, offset_x=0, offset_y=0):
        """鼠标相对当前位置移动若干位置           \n
        :param offset_x: 偏移量x
        :param offset_y: 偏移量y
        :return: self
        """
        self.curr_x += offset_x
        self.curr_y += offset_y
        self._dr.Input.dispatchMouseEvent(type='mouseMoved', x=self.curr_x, y=self.curr_y, modifiers=self.modifier)
        return self

    def hold(self, on_ele=None):
        """点击并按住当前坐标或指定元素            \n
        :param on_ele: ChromiumElement对象
        :return: self
        """
        if on_ele:
            self.move_to(on_ele)
        self._dr.Input.dispatchMouseEvent(type='mousePressed', button='left',
                                          x=self.curr_x, y=self.curr_y, modifiers=self.modifier)
        return self

    def click(self, on_ele=None):
        """点击鼠标左键，可先移动到元素上      \n
        :param on_ele: ChromiumElement元素
        :return: self
        """
        if on_ele:
            self.move_to(on_ele)
        self._dr.Input.dispatchMouseEvent(type='mousePressed', button='left',
                                          x=self.curr_x, y=self.curr_y, modifiers=self.modifier)
        self._dr.Input.dispatchMouseEvent(type='mouseReleased', button='left',
                                          x=self.curr_x, y=self.curr_y, modifiers=self.modifier)
        return self

    def r_click(self, on_ele=None):
        """点击鼠标右键，可先移动到元素上      \n
        :param on_ele: ChromiumElement元素
        :return: self
        """
        if on_ele:
            self.move_to(on_ele)
        self._dr.Input.dispatchMouseEvent(type='mousePressed', button='right',
                                          x=self.curr_x, y=self.curr_y, modifiers=self.modifier)
        self._dr.Input.dispatchMouseEvent(type='mouseReleased', button='right',
                                          x=self.curr_x, y=self.curr_y, modifiers=self.modifier)
        return self

    def release(self, on_ele=None):
        """释放鼠标左键，可先移动到元素再释放            \n
        :param on_ele: ChromiumElement对象
        :return: self
        """
        if on_ele:
            self.move_to(on_ele)
        self._dr.Input.dispatchMouseEvent(type='mouseReleased', button='left',
                                          x=self.curr_x, y=self.curr_y, modifiers=self.modifier)
        return self

    def scroll(self, delta_x=0, delta_y=0, on_ele=None):
        """滚动鼠标滚轮，可先移动到元素上                \n
        :param delta_x: 滚轮变化值x
        :param delta_y: 滚轮变化值y
        :param on_ele: ChromiumElement元素
        :return: self
        """
        if on_ele:
            self.move_to(on_ele)
        self._dr.Input.dispatchMouseEvent(type='mouseWheel', x=self.curr_x, y=self.curr_y,
                                          deltaX=delta_x, deltaY=delta_y, modifiers=self.modifier)
        return self

    def up(self, pixel):
        """鼠标向上移动若干像素"""
        return self.move(0, -pixel)

    def down(self, pixel):
        """鼠标向下移动若干像素"""
        return self.move(0, pixel)

    def left(self, pixel):
        """鼠标向左移动若干像素"""
        return self.move(-pixel, 0)

    def right(self, pixel):
        """鼠标向右移动若干像素"""
        return self.move(pixel, 0)

    def key_down(self, key):
        """按下键盘上的按键                    \n
        :param key: 按键，特殊字符见Keys
        :return: self
        """
        if key in ('\ue009', '\ue008', '\ue00a', '\ue03d'):  # 如果上修饰符，添加到变量
            self.modifier |= _modifierBit.get(key, 0)
            return self

        data = self._get_key_data(key, 'keyDown')
        self.page.run_cdp('Input.dispatchKeyEvent', **data)
        return self

    def key_up(self, key):
        """提起键盘上的按键                    \n
        :param key: 按键，特殊字符见Keys
        :return: self
        """
        if key in ('\ue009', '\ue008', '\ue00a', '\ue03d'):  # 如果上修饰符，添加到变量
            self.modifier ^= _modifierBit.get(key, 0)
            return self

        data = self._get_key_data(key, 'keyUp')
        self.page.run_cdp('Input.dispatchKeyEvent', **data)
        return self

    def wait(self, second):
        """等待若干秒"""
        sleep(second)
        return self

    def _get_key_data(self, key, action):
        """获取用于发送的按键信息                   \n
        :param key: 按键
        :param action: 'keyDown' 或 'keyUp'
        :return: 按键信息
        """
        description = _keyDescriptionForString(self.modifier, key)
        text = description['text']
        if action != 'keyUp':
            action = 'keyDown' if text else 'rawKeyDown'
        return {'type': action,
                'modifiers': self.modifier,
                'windowsVirtualKeyCode': description['keyCode'],
                'code': description['code'],
                'key': description['key'],
                'text': text,
                'autoRepeat': False,
                'unmodifiedText': text,
                'location': description['location'],
                'isKeypad': description['location'] == 3}


def location_to_client(page, lx, ly):
    """绝对坐标转换为视口坐标"""
    scrool_x = page.run_script('return document.documentElement.scrollLeft;')
    scrool_y = page.run_script('return document.documentElement.scrollTop;')
    return lx + scrool_x, ly + scrool_y
