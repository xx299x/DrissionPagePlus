# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from pathlib import Path
from typing import Union, Tuple, List, Any

from DataRecorder import Recorder
from requests import Session
from requests.cookies import RequestsCookieJar

from .commons.constants import NoneElement
from .base import BasePage
from .chromium_driver import ChromiumDriver
from .chromium_element import ChromiumElement, ChromiumScroll
from .chromium_frame import ChromiumFrame
from .session_element import SessionElement


class ChromiumBase(BasePage):

    def __init__(self,
                 address: Union[str, int],
                 tab_id: str = None,
                 timeout: float = None):
        self._control_session: Session = ...
        self.address: str = ...
        self._tab_obj: ChromiumDriver = ...
        self._is_reading: bool = ...
        self._timeouts: Timeout = ...
        self._first_run: bool = ...
        self._is_loading: bool = ...
        self._page_load_strategy: str = ...
        self._scroll: ChromiumScroll = ...
        self._url: str = ...
        self._root_id: str = ...
        self._debug: bool = ...
        self._debug_recorder: Recorder = ...
        self._upload_list: list = ...
        self._wait: ChromiumBaseWaiter = ...
        self._set: ChromiumBaseSetter = ...
        self._screencast: Screencast = ...

    def _connect_browser(self, tab_id: str = None) -> None: ...

    def _chromium_init(self): ...

    def _driver_init(self, tab_id: str) -> None: ...

    def _get_document(self) -> None: ...

    def _wait_loaded(self, timeout: float = None) -> bool: ...

    def _onFrameStartedLoading(self, **kwargs): ...

    def _onFrameStoppedLoading(self, **kwargs): ...

    def _onLoadEventFired(self, **kwargs): ...

    def _onDocumentUpdated(self, **kwargs): ...

    def _onFrameNavigated(self, **kwargs): ...

    def _onFileChooserOpened(self, **kwargs): ...

    def _set_start_options(self, address, none) -> None: ...

    def _set_runtime_settings(self) -> None: ...

    def __call__(self, loc_or_str: Union[Tuple[str, str], str, ChromiumElement],
                 timeout: float = None) -> ChromiumElement: ...

    @property
    def title(self) -> str: ...

    @property
    def driver(self) -> ChromiumDriver: ...

    @property
    def is_loading(self) -> bool: ...

    @property
    def is_alive(self) -> bool: ...

    @property
    def url(self) -> str: ...

    @property
    def _browser_url(self) -> str: ...

    @property
    def html(self) -> str: ...

    @property
    def json(self) -> Union[dict, None]: ...

    @property
    def tab_id(self) -> str: ...

    @property
    def ready_state(self) -> Union[str, None]: ...

    @property
    def size(self) -> Tuple[int, int]: ...

    @property
    def active_ele(self) -> ChromiumElement: ...

    @property
    def page_load_strategy(self) -> str: ...

    @property
    def scroll(self) -> ChromiumPageScroll: ...

    @property
    def timeouts(self) -> Timeout: ...

    @property
    def upload_list(self) -> list: ...

    @property
    def wait(self) -> ChromiumBaseWaiter: ...

    @property
    def set(self) -> ChromiumBaseSetter: ...

    @property
    def screencast(self) -> Screencast: ...

    def run_js(self, script: str, *args: Any, as_expr: bool = False) -> Any: ...

    def run_js_loaded(self, script: str, *args: Any, as_expr: bool = False) -> Any: ...

    def run_async_js(self, script: str, *args: Any, as_expr: bool = False) -> None: ...

    def get(self,
            url: str,
            show_errmsg: bool = False,
            retry: int = None,
            interval: float = None,
            timeout: float = None) -> Union[None, bool]: ...

    def get_cookies(self, as_dict: bool = False, all_domains: bool = False, all_info: bool = False) -> Union[
        list, dict]: ...

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, ChromiumElement, ChromiumFrame],
            timeout: float = None) -> ChromiumElement: ...

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = None) -> List[ChromiumElement]: ...

    def s_ele(self, loc_or_ele: Union[Tuple[str, str], str] = None) \
            -> Union[SessionElement, str, NoneElement]: ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str]) -> List[Union[SessionElement, str]]: ...

    def _find_elements(self,
                       loc_or_ele: Union[Tuple[str, str], str, ChromiumElement, ChromiumFrame],
                       timeout: float = None, single: bool = True, relative: bool = False, raise_err: bool = None) \
            -> Union[ChromiumElement, ChromiumFrame, NoneElement, List[Union[ChromiumElement, ChromiumFrame]]]: ...

    def refresh(self, ignore_cache: bool = False) -> None: ...

    def forward(self, steps: int = 1) -> None: ...

    def back(self, steps: int = 1) -> None: ...

    def _forward_or_back(self, steps: int) -> None: ...

    def stop_loading(self) -> None: ...

    def remove_ele(self, loc_or_ele: Union[ChromiumElement, ChromiumFrame, str, Tuple[str, str]]) -> None: ...

    def get_frame(self, loc_ind_ele: Union[str, int, tuple, ChromiumFrame], timeout: float = None) -> ChromiumFrame: ...

    def get_frames(self, loc: Union[str, tuple] = None, timeout: float = None) -> List[ChromiumFrame]: ...

    def run_cdp(self, cmd: str, **cmd_args) -> dict: ...

    def run_cdp_loaded(self, cmd: str, **cmd_args) -> dict: ...

    def get_session_storage(self, item: str = None) -> Union[str, dict, None]: ...

    def get_local_storage(self, item: str = None) -> Union[str, dict, None]: ...

    def get_screenshot(self, path: [str, Path] = None,
                       as_bytes: [bool, str] = None, as_base64: [bool, str] = None,
                       full_page: bool = False,
                       left_top: Tuple[int, int] = None,
                       right_bottom: Tuple[int, int] = None) -> Union[str, bytes]: ...

    def _get_screenshot(self, path: [str, Path] = None,
                        as_bytes: [bool, str] = None, as_base64: [bool, str] = None,
                        full_page: bool = False,
                        left_top: Tuple[int, int] = None,
                        right_bottom: Tuple[int, int] = None,
                        ele: ChromiumElement = None) -> Union[str, bytes]: ...

    def clear_cache(self,
                    session_storage: bool = True,
                    local_storage: bool = True,
                    cache: bool = True,
                    cookies: bool = True) -> None: ...

    def _d_connect(self,
                   to_url: str,
                   times: int = 0,
                   interval: float = 1,
                   show_errmsg: bool = False,
                   timeout: float = None) -> Union[bool, None]: ...


class ChromiumBaseWaiter(object):
    def __init__(self, page: ChromiumBase):
        self._driver: ChromiumBase = ...

    def ele_delete(self, loc_or_ele: Union[str, tuple, ChromiumElement], timeout: float = None) -> bool: ...

    def ele_display(self, loc_or_ele: Union[str, tuple, ChromiumElement], timeout: float = None) -> bool: ...

    def ele_hidden(self, loc_or_ele: Union[str, tuple, ChromiumElement], timeout: float = None) -> bool: ...

    def _loading(self, timeout: float = None, start: bool = True, gap: float = .01) -> bool: ...

    def load_start(self, timeout: float = None) -> bool: ...

    def load_complete(self, timeout: float = None) -> bool: ...

    def upload_paths_inputted(self) -> None: ...


class ChromiumPageScroll(ChromiumScroll):
    def __init__(self, page: ChromiumBase): ...

    def to_see(self, loc_or_ele: Union[str, tuple, ChromiumElement], center: bool = False) -> None: ...

    def _to_see(self, ele: ChromiumElement, center: bool) -> None: ...


class ChromiumBaseSetter(object):
    def __init__(self, page):
        self._page: ChromiumBase = ...

    @property
    def load_strategy(self) -> PageLoadStrategy: ...

    @property
    def scroll(self) -> PageScrollSetter: ...

    def retry_times(self, times: int) -> None: ...

    def retry_interval(self, interval: float) -> None: ...

    def timeouts(self, implicit: float = None, page_load: float = None, script: float = None) -> None: ...

    def user_agent(self, ua: str, platform: str = None) -> None: ...

    def session_storage(self, item: str, value: Union[str, bool]) -> None: ...

    def local_storage(self, item: str, value: Union[str, bool]) -> None: ...

    def cookies(self, cookies: Union[RequestsCookieJar, list, tuple, str, dict]) -> None: ...

    def headers(self, headers: dict) -> None: ...

    def upload_files(self, files: Union[str, list, tuple]) -> None: ...


class Timeout(object):

    def __init__(self, page: ChromiumBase, implicit=None, page_load=None, script=None):
        self._page: ChromiumBase = ...
        self.implicit: float = ...
        self.page_load: float = ...
        self.script: float = ...


class PageLoadStrategy(object):
    def __init__(self, page: ChromiumBase):
        self._page: ChromiumBase = ...

    def __call__(self, value: str) -> None: ...

    def normal(self) -> None: ...

    def eager(self) -> None: ...

    def none(self) -> None: ...


class PageScrollSetter(object):
    def __init__(self, scroll: ChromiumPageScroll):
        self._scroll: ChromiumPageScroll = ...

    def wait_complete(self, on_off: bool = True): ...

    def smooth(self, on_off: bool = True): ...


class Screencast(object):
    def __init__(self, page: ChromiumBase):
        self._page: ChromiumBase = ...
        self._path: Path = ...
        self._running: bool = ...
        self._enable: bool = ...
        self._mode: str = ...

    @property
    def set_mode(self) -> ScreencastMode: ...

    def start(self, save_path: Union[str, Path] = None) -> None: ...

    def stop(self, video_name: str = None) -> str: ...

    def set_save_path(self, save_path: Union[str, Path] = None) -> None: ...

    def _run(self) -> None: ...

    def _onScreencastFrame(self, **kwargs) -> None: ...


class ScreencastMode(object):
    def __init__(self, screencast: Screencast):
        self._screencast: Screencast = ...

    def video_mode(self) -> None: ...

    def frugal_video_mode(self) -> None: ...

    def js_video_mode(self) -> None: ...

    def frugal_imgs_mode(self) -> None: ...

    def imgs_mode(self) -> None: ...
