# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from pathlib import Path
from typing import Any, Union, Tuple, Optional

from requests import Session
from requests.adapters import HTTPAdapter
from requests.auth import HTTPBasicAuth
from requests.cookies import RequestsCookieJar
from requests.structures import CaseInsensitiveDict


class SessionOptions(object):
    def __init__(self, read_file: [bool, None] = True, ini_path: Union[str, Path] = None):
        self.ini_path: str = ...
        self._download_path: str = ...
        self._headers: dict = ...
        self._cookies: list = ...
        self._auth: tuple = ...
        self._proxies: dict = ...
        self._hooks: dict = ...
        self._params: dict = ...
        self._verify: bool = ...
        self._cert: Union[str, tuple] = ...
        self._adapters: list = ...
        self._stream: bool = ...
        self._trust_env: bool = ...
        self._max_redirects: int = ...
        self._timeout: float = ...
        self._del_set: set = ...

    @property
    def download_path(self) -> str: ...

    def set_download_path(self, path: Union[str, Path]) -> SessionOptions: ...

    @property
    def timeout(self) -> float: ...

    def set_timeout(self, second: float) -> SessionOptions: ...

    @property
    def headers(self) -> dict: ...

    def set_headers(self, headers: Union[dict, None]) -> SessionOptions: ...

    def set_a_header(self, attr: str, value: str) -> SessionOptions: ...

    def remove_a_header(self, attr: str) -> SessionOptions: ...

    @property
    def cookies(self) -> list: ...

    def set_cookies(self, cookies: Union[RequestsCookieJar, list, tuple, str, dict, None]) -> SessionOptions: ...

    @property
    def auth(self) -> Union[Tuple[str, str], HTTPBasicAuth]: ...

    def set_auth(self, auth: Union[Tuple[str, str], HTTPBasicAuth, None]) -> SessionOptions: ...

    @property
    def proxies(self) -> dict: ...

    def set_proxies(self, http: Union[str, None], https: Union[str, None] = None) -> SessionOptions: ...

    @property
    def hooks(self) -> dict: ...

    def set_hooks(self, hooks: Union[dict, None]) -> SessionOptions: ...

    @property
    def params(self) -> dict: ...

    def set_params(self, params: Union[dict, None]) -> SessionOptions: ...

    @property
    def verify(self) -> bool: ...

    def set_verify(self, on_off: Union[bool, None]) -> SessionOptions: ...

    @property
    def cert(self) -> Union[str, tuple]: ...

    def set_cert(self, cert: Union[str, Tuple[str, str], None]) -> SessionOptions: ...

    @property
    def adapters(self): list: ...

    def add_adapter(self, url: str, adapter: HTTPAdapter) -> SessionOptions: ...

    @property
    def stream(self) -> bool: ...

    def set_stream(self, on_off: Union[bool, None]) -> SessionOptions: ...

    @property
    def trust_env(self) -> bool: ...

    def set_trust_env(self, on_off: Union[bool, None]) -> SessionOptions: ...

    @property
    def max_redirects(self) -> int: ...

    def set_max_redirects(self, times: Union[int, None]) -> SessionOptions: ...

    def _sets(self, arg: str, val: Any) -> None: ...

    def save(self, path: str = None) -> str: ...

    def save_to_default(self) -> str: ...

    def as_dict(self) -> dict: ...

    def make_session(self) -> Tuple[Session, Optional[CaseInsensitiveDict]]: ...

    def from_session(self, session: Session, headers: CaseInsensitiveDict = None) -> SessionOptions: ...


def session_options_to_dict(options: Union[dict, SessionOptions, None]) -> Union[dict, None]: ...
