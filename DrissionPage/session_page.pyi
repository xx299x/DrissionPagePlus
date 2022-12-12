from typing import Any, Union, Tuple, List

from DownloadKit import DownloadKit
from requests import Session, Response
from requests.cookies import RequestsCookieJar

from .session_element import SessionElement
from .config import SessionOptions


class SessionPage:
    def __init__(self,
                 session_or_options: Union[Session, SessionOptions] = ...,
                 timeout: float = ...):
        self._session: Session = ...
        self._url: str = ...
        self._response: Response = ...
        self._download_kit: DownloadKit = ...
        self._url_available: bool = ...
        self.timeout: float = ...
        self.retry_times: int = ...
        self.retry_interval: float = ...

    def _create_session(self,
                        Session_or_Options: Union[Session, SessionOptions]) -> None:
        ...

    def _set_session(self, data: dict) -> None:
        ...

    def set_cookies(self, cookies: Union[RequestsCookieJar, list, tuple, str, dict]) -> None:
        ...

    def __call__(self,
                 loc_or_str: Union[Tuple[str, str], str, SessionElement],
                 timeout=...) -> Union[SessionElement, str, None]:
        """在内部查找元素                                                  \n
        例：ele2 = ele1('@id=ele_id')                                     \n
        :param loc_or_str: 元素的定位信息，可以是loc元组，或查询字符串
        :param timeout: 不起实际作用，用于和DriverElement对应，便于无差别调用
        :return: SessionElement对象或属性文本
        """
        return self.ele(loc_or_str)

    # -----------------共有属性和方法-------------------

    @property
    def url(self) -> str:
        ...

    @property
    def html(self) -> str:
        ...

    @property
    def json(self) -> dict:
        ...

    def get(self,
            url: str,
            show_errmsg: bool | None = ...,
            retry: int | None = ...,
            interval: float | None = ...,
            timeout: float | None = ...,
            params: dict | None = ...,
            data: Union[dict, str, None] = ...,
            headers: dict | None = ...,
            cookies: Any | None = ...,
            files: Any | None = ...,
            auth: Any | None = ...,
            allow_redirects: bool = ...,
            proxies: Any | None = ...,
            hooks: Any | None = ...,
            stream: Any | None = ...,
            verify: Any | None = ...,
            cert: Any | None = ...,
            json: Union[dict, str, None] = ...,
            ) -> bool:
        ...

    def ele(self,
            loc_or_ele: Union[Tuple[str, str], str, SessionElement],
            timeout: float = ...) -> Union[SessionElement, str, None]:
        ...

    def eles(self,
             loc_or_str: Union[Tuple[str, str], str],
             timeout: float = ...) -> List[Union[SessionElement, str]]:
        ...

    def s_ele(self,
              loc_or_ele: Union[Tuple[str, str], str, SessionElement] = ...) -> Union[SessionElement, str, None]:
        ...

    def s_eles(self, loc_or_str: Union[Tuple[str, str], str] = ...) -> List[Union[SessionElement, str]]:
        """返回页面中符合条件的所有元素、属性或节点文本                              \n
        :param loc_or_str: 元素的定位信息，可以是元素对象，loc元组，或查询字符串
        :return: SessionElement对象或属性、文本
        """
        return self._ele(loc_or_str, single=False)

    def _ele(self,
             loc_or_ele: Union[Tuple[str, str], str, SessionElement],
             timeout: float = ...,
             single: bool = ...) -> Union[SessionElement, str, None, List[Union[SessionElement, str]]]:
        ...

    def get_cookies(self,
                    as_dict: bool = ...,
                    all_domains: bool = ...) -> Union[dict, list]:
        ...

    # ----------------session独有属性和方法-----------------------
    @property
    def session(self) -> Session:
        ...

    @property
    def response(self) -> Response:
        ...

    @property
    def download(self) -> DownloadKit:
        ...

    def post(self,
             url: str,
             data: Union[dict, str] = ...,
             show_errmsg: bool = ...,
             retry: int = ...,
             interval: float = ...,
             **kwargs) -> bool:
        ...

    def _s_connect(self,
                   url: str,
                   mode: str,
                   data: Union[dict, str, None] = ...,
                   show_errmsg: bool = ...,
                   retry: int = ...,
                   interval: float = ...,
                   **kwargs) -> bool:
        ...

    def _make_response(self,
                       url: str,
                       mode: str = ...,
                       data: Union[dict, str] = ...,
                       retry: int = ...,
                       interval: float = ...,
                       show_errmsg: bool = ...,
                       **kwargs) -> tuple:
        ...
