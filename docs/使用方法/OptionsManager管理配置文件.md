### class OptionsManager()

管理配置文件内容的类。

参数说明：

- path: str - ini文件路径，不传入则默认读取当前文件夹下的 configs.ini 文件

### paths

返回 paths 设置信息。

返回： dict

### chrome_options

返回 chrome 设置信息。

返回： dict

### session_options

返回 session 设置信息。

返回： dict

### get_value()

获取配置的值。

参数说明：

- section: str - 段落名称
- item: str - 配置项名称

返回： Any

### get_option()

以字典的格式返回整个段落的配置信息。

参数说明：

- section: str - 段落名称

返回： dict

### set_item()

设置配置值，返回自己，用于链式操作。

参数说明：

- section: str - 段落名称
- item: str - 配置项名称
- value: Any - 值内容

返回： OptionsManager - 返回自己

### save()

保存设置到文件，返回自己，用于链式操作。

参数说明：

- path: str - ini 文件的路径，传入 'default' 保存到默认ini文件

返回： OptionsManager - 返回自己