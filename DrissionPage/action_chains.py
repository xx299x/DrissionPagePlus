# -*- coding:utf-8 -*-
# from chrome_element import ChromeElement


class ActionChains:
    """
    ActionChains are a way to automate low level interactions such as
    mouse movements, mouse button actions, key press, and context menu interactions.
    This is useful for doing more complex actions like hover over and drag and drop.

    Generate user actions.
       When you call methods for actions on the ActionChains object,
       the actions are stored in a queue in the ActionChains object.
       When you call perform(), the events are fired in the order they
       are queued up.

    ActionChains can be used in a chain pattern::

        menu = driver.find_element(By.CSS_SELECTOR, ".nav")
        hidden_submenu = driver.find_element(By.CSS_SELECTOR, ".nav #submenu1")

        ActionChains(driver).move_to_element(menu).click(hidden_submenu).perform()

    Or actions can be queued up one by one, then performed.::

        menu = driver.find_element(By.CSS_SELECTOR, ".nav")
        hidden_submenu = driver.find_element(By.CSS_SELECTOR, ".nav #submenu1")

        actions = ActionChains(driver)
        actions.move_to_element(menu)
        actions.click(hidden_submenu)
        actions.perform()

    Either way, the actions are performed in the order they are called, one after
    another.
    """

    def __init__(self, page):
        """
        Creates a new ActionChains.

        :Args:
         - driver: The WebDriver instance which performs user actions.
         - duration: override the default 250 msecs of DEFAULT_MOVE_DURATION in PointerInput
        """
        self._dr = page.driver
        self.curr_x = 0
        self.curr_y = 0

    def move_to_element(self, to_element):
        cl = to_element.client_location
        size = to_element.size
        x = cl['x'] + size['width'] // 2
        y = cl['y'] + size['height'] // 2
        self._dr.Input.dispatchMouseEvent(type='mouseMoved', x=x, y=y)
        self.curr_x = x
        self.curr_y = y
        return self

    def move_to_element_with_offset(self, to_element, offset_x=0, offset_y=0):
        cl = to_element.client_location
        size = to_element.size
        x = int(offset_x) + cl['x'] + size['width'] // 2
        y = int(offset_y) + cl['y'] + size['height'] // 2
        self._dr.Input.dispatchMouseEvent(type='mouseMoved', x=x, y=y)
        self.curr_x = x
        self.curr_y = y
        return self

    def click_and_hold(self, on_element=None):
        if on_element:
            self.move_to_element(on_element)
        self._dr.Input.dispatchMouseEvent(type='mousePressed', button='left', clickCount=1,
                                          x=self.curr_x, y=self.curr_y)
        # self.key_down()

        return self

    def release(self, on_element=None):
        if on_element:
            self.move_to_element(on_element)
        self._dr.Input.dispatchMouseEvent(type='mouseReleased', button='left',
                                          x=self.curr_x, y=self.curr_y)
        # self.key_down()
        return self

    def key_down(self):
        data = {'type': 'rawKeyDown', 'modifiers': 0, 'windowsVirtualKeyCode': 19, 'code': 'Pause', 'key': 'Pause',
                'text': '', 'autoRepeat': False, 'unmodifiedText': '', 'location': 0, 'isKeypad': False}
        self._dr.call_method('Input.dispatchKeyEvent', **data)