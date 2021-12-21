# class SessionOptions()

Session 对象配置类。

参数说明：

- read_file: bool - 创建时是否从 ini 文件读取配置信息
- ini_path:  str - ini 文件路径，为None则读取默认 ini 文件

## headers

headers 配置信息。

返回： dict

## cookies

cookies 配置信息。

返回： list

## auth

auth 配置信息。

返回： tuple

## proxies

proxies 配置信息。

返回： dict

## hooks

hooks 配置信息。

返回： dict

## params

params 配置信息。

返回： dict

## verify

verify 配置信息。

返回： bool

## cert

cert 配置信息。

返回： [str, tuple]

## adapters

adapters 配置信息。

返回： adapters

## stream

stream 配置信息。

返回： bool

## trust_env

srust_env 配置信息。

返回： bool

## max_redirects

max_redirect 配置信息。

返回： int

## set_a_header()

设置 headers 中一个项。

参数说明：

- attr: str - 配置项名称
- value: str - 配置的值

返回： 当前对象

## remove_a_header()

从 headers 中删除一个设置。

参数说明：

- attr: str - 要删除的配置名称

返回：当前对象

## set_proxies()

设置proxies参数

{'http': 'http://xx.xx.xx.xx:xxxx', 'https': 'http://xx.xx.xx.xx:xxxx'}

参数说明：

- proxies: dict - 代理信息字典

返回：当前对象

## save()

保存设置到文件。

参数说明：

- path: str - ini文件的路径，传入 'default' 保存到默认ini文件

返回：当前对象

## as_dict()

以字典形式返回当前对象。

返回： dict