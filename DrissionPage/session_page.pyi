from typing import Any, Union

from requests import Session

from .config import SessionOptions


class SessionPage:
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
            ) -> bool: ...

    @property
    def url(self) -> str: ...

    @property
    def html(self) -> str: ...

    def _set_session(self, Session_or_Options: Union[Session, SessionOptions]) -> None: ...
