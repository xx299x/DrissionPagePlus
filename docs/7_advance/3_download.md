浏览器没有为下载功能提供方便的程序接口，难以实现有效的下载管理，如检测下载状态、重命名、失败管理等。使用 requests 下载文件能较好实现以上功能，但代码较为繁琐。  
因此 DrissionPage 内置了高效可靠的下载工具，提供任务管理、多线程、大文件分块、自动重连、文件名冲突处理等功能。

无论是控制浏览器，还是收发数据包的场景，都可以使用它。甚至可以拦截浏览器的下载动作，转用内置下载器进行下载。使用方式简洁高效。

?> 该工具名为 DownloadKit，现已独立打包成一个库，详细用法见：[DownloadKit](https://gitee.com/g1879/DownloadKit)。这里只介绍和页面对象有关的用法。

# ✔️ 功能简介

`SessionPage`、`ChromiumPage`、`WebPage`都可以使用`download()`方法进行下载，还可以使用`download_set`属性对下载参数进行配置。内置下载器功能如下：

- 支持多线程同时下载多个文件
- 大文件自动分块使用多线程下载
- 可拦截浏览器下载动作，自动调用下载器下载
- 自动创建目标路径
- 自动去除路径中的非法字符
- 下载时支持文件重命名
- 自动处理文件名冲突
- 自动去除文件名非法字符
- 支持 post 方式
- 支持自定义连接参数
- 任务失败自动重试

---

# ✔️ 简单示例

下载一个文件（单线程）：

```python
page.download('https://xxxxxx/file.pdf')
```

添加下载任务，以多线程方式下载：

```python
page.download.add('https://xxxxxx/file.pdf')
```

拦截浏览器下载动作，改用 DwonloadKit 下载：

```python
page.download_set.by_DownloadKit()
page('#download_btn').click()
```

下载设置：

```python
# 设置默认保存路径
page.download_set.save_path('...')
# 设置存在同名文件时处理方式
page.download_set.if_file_exists.skip()
# 设置使用浏览器或DownloadKit下载
page.download_set.by_DownloadKit()
page.download_set.by_browser()
# 设置使用DownloadKit下载时是否允许多线程同时下载一个文件
page.download_set.split(True)
```

---

# ✔️ 单线程下载

直接调用页面对象的`download()`方法，可下载一个文件，该方法是阻塞式的，会等待下载完成再往下操作。下载时默认打印下载进度。

### 🔸 `download()`

| 参数名称          | 类型              | 默认值    | 说明                                               |
|:-------------:|:---------------:|:------:| ------------------------------------------------ |
| `file_url`    | `str`           | 必填     | 文件网址                                             |
| `goal_path`   | `str`<br>`Path` | `None` | 保存文件夹路径，为`None`则保存在当前目录                          |
| `rename`      | `str`           | `None` | 重命名文件名，可不包含后缀                                    |
| `file_exists` | `str`           | `None` | 遇到同名文件时的处理方式，可选`'skip'`、`'overwrite'`、`'rename'` |
| `data`        | `str`<br>`dict` | `None` | post 方式使用的数据，如不为`None`，使用 post 方式发送请求            |
| `show_msg`    | `bool`          | `True` | 是否显示下载进度                                         |
| `**kwargs`    | -               | 必填     | requests的`get()`方法参数                             |

| 返回类型    | 返回格式         | 说明                  |
|:-------:|:------------:| ------------------- |
| `tuple` | (任务结果, 任务信息) | 返回任务结果和信息组成的`tuple` |

任务结果可能出现以下值：

- `'success'`：表示下载成功

- `'skip'`：表示存在同名文件，跳过任务

- `'cancel'`：表示任务下载途中被取消

- `False`：表示下载失败

任务信息成功时返回文件绝对路径，其它情况返回相应说明。

**示例：**

```python
from DrissionPage import WebPage

page = WebPage('s')
# 文件 url
url = 'https://www.baidu.com/img/flexible/logo/pc/result.png'  
# 存放路径
save_path = r'C:\download'  

# 重命名为img.png，存在重名时自动在文件名末尾加上序号，显示下载进度
res = page.download(url, save_path, 'img', 'rename', show_msg=True)
# 打印结果
print(res)
```

显示：

```console
url：https://www.baidu.com/img/flexible/logo/pc/result.png
文件名：img.png
目标路径：C:\download
100% 下载完成 C:\download\img.png

('success', 'C:\\download\\img.png')
```

---

# ✔️ 多线程并发下载

您可以添加数量不限的下载任务，程序会自动调配线程去完成这些任务。

默认设置下，页面对象最多使用 10 个下载线程，如进程已满，新任务会进入等待队列排队，待有空线程时自动开始。

添加多线程任务的方法是调用页面对象`donwload`属性的`add()`方法。

### 🔸 `download.add()`

| 参数名称          | 类型              | 默认值    | 说明                                                                |
|:-------------:|:---------------:|:------:| ----------------------------------------------------------------- |
| `file_url`    | `str`           | 必填     | 文件网址                                                              |
| `goal_path`   | `str`<br>`Path` | `None` | 保存文件夹路径，为`None`则保存在当前目录                                           |
| `rename`      | `str`           | `None` | 重命名文件名，可不包含后缀                                                     |
| `file_exists` | `str`           | `None` | 遇到同名文件时的处理方式，可选`'skip'`、`'overwrite'`、`'rename'`                  |
| `data`        | `str`<br>`dict` | `None` | post 方式使用的数据，如不为`None`，使用 post 方式发送请求                             |
| `split`       | `bool`          | `None` | 是否允许多线程分块下载，为`None`则使用下载器内置设置，默认为`True`<br>默认大于 50MB 的文件使用多线程分块下载 |
| `**kwargs`    | -               | 必填     | requests的`get()`方法参数                                              |

| 返回类型      | 说明                                                                           |
|:---------:| ---------------------------------------------------------------------------- |
| `Mission` | 返回任务对象，任务对象具体属性及方法详见 [DownloadKit](https://gitee.com/g1879/DownloadKit) 相关说明 |

**示例：**

```python
from DrissionPage import WebPage

page = WebPage('s')
# 文件 url
url = 'https://www.baidu.com/img/flexible/logo/pc/result.png'  
# 存放路径
save_path = r'C:\download'  

# 返回一个任务对象
mission = page.download.add(url, save_path)

# 通过任务对象查看状态
print(mission.rate, mission.info)
```

**输出：**

```console
90% '下载中'
```

---

# ✔️ 接管浏览器下载任务

## 📍 切换下载方式

很多时候，网站会用某些非显式的方式触发浏览器下载，因此不能很好地获取文件 url，下载依赖浏览器功能。，页面对象可以设置用`DownloadKit`拦截浏览器的下载任务，使下载更快，下载过程更可控。

方法是使用`download_set.by_DownloadKit()`方法，指定使用`DownloadKit`接管浏览器的下载动作。

```python
page.download_set.by_DownloadKit()
```

事实上，页面对象创建时就默认开启了此功能，如果想用浏览器本身的下载功能，可以这样写：

```python
page.download_set.by_browser()
```

!>**注意：**<br>接管浏览器下载任务后会使用 get 方式下载，如果是需要 post 的请求，可能不能正确下载。

---

## 📍 等待下载开始

在浏览器中点击下载按钮后，有时下载不会立即触发，这时如果过快进行其它操作，可能导致一些意想不到的问题。因此设计了`wait_download_begin()`
方法，用于等待下载动作的开始，以便正确接管。可控制浏览器的页面对象`ChromiumPage`和`WebPage`拥有此方法。

!>**注意：**<br>如果网站须要很长时间准备下载的文件，请设置一个足够长的超时时间。

### 🔸 `wait_download_begin()`

此方法会阻塞程序，等待下载动作触发。如到达超时时间仍未触发，则返回`False`。

| 参数名称      | 类型               | 默认值    | 说明                                    |
|:---------:|:----------------:|:------:| ------------------------------------- |
| `timeout` | `int`<br>`float` | `None` | 等待下载开始的超时时间，为`None`则使用页面对象`timeout`属性 |

| 返回类型   | 说明       |
|:------:| -------- |
| `bool` | 是否等到下载开始 |

**示例：**

```python
page('#download_btn').click()
page.wait_download_begin()
```

---

# ✔️ 查看任务信息

用单线程方式下载，会阻塞程序直到下载完成，因此无须查看任务信息。

用多线程或接管浏览器下载任务的方式下载，可用以下方式查看任务信息。

## 📍 获取单个任务对象

使用`download.add()`
添加任务时，会返回一个任务对象，后续程序可以使用该对象查询该任务下载进度、结果，也可以取消任务。这里不对该对象作详细说明，详情请移步：[DownloadKit](https://gitee.com/g1879/DownloadKit)。

**示例：**

```python
mission = page.download.add('http://xxxx.pdf')
print(mission.id)  # 获取任务id
print(mission.rate)  # 打印下载进度（百分比）
print(mission.state)  # 打印任务状态，'waiting'、'running'、'done'
print(mission.info)  # 打印任务信息
print(mission.result)  # 打印任务结果，'success'表示成功，False表示失败，'skip'表示跳过，'cancel'表示取消
```

除添加任务时获取对象，也可以使用`download.get_mission()`获取。在上一个示例中可以看到，任务对象有`id`属性，把任务的`id`传入此方法，会返回该任务对象。

**示例：**

```python
mission_id = mission.id
mission = page.download.get_mission(mission_id)
```

---

## 📍 获取全部任务对象

使用页面对象的`download.missions`属性，可以获取所有下载任务。该属性返回一个`dict`，保存了所有下载任务。以任务对象的`id`为 key。

```python
page.download_set.save_path(r'D:\download')
page.download('http://xxxxx/xxx1.pdf')
page.download('http://xxxxx/xxx1.pdf')
print(page.download.missions)
```

**输出：**

```
{
    1: <Mission 1 D:\download\xxx1.pdf xxx1.pdf>
    2: <Mission 2 D:\download\xxx1_1.pdf xxx1_1.pdf>
    ...
}
```

---

## 📍 获取下载失败的任务

使用`download.get_failed_missions()`方法，可以获取下载失败的任务列表。

```python
page.download_set.save_path(r'D:\download')
page.download('http://xxxxx/xxx1.pdf')
page.download('http://xxxxx/xxx1.pdf')
print(page.download.get_failed_missions()
```

**输出：**

```
[
    <Mission 1 状态码：404 None>,
    <Mission 2 状态码：404 None>
    ...
]
```

?>**Tips：**<br>获取失败任务对象后，可从其`data`属性读取任务内容，以便记录日志或择机重试。

---

# ✔️ 下载设置

主要的下载设置使用`download_set`内置方法进行，更多运行参数使用`download`属性的子属性进行。

## 📍 设置文件保存路径

### 🔸 `download_set.save_path()`

此方法用于设置文件下载默认保存路径。

| 参数名称   | 类型              | 默认值 | 说明                 |
|:------:|:---------------:|:---:| ------------------ |
| `path` | `str`<br>`Path` | 必填  | 文件保存路径，绝对路径和相对路径均可 |

**返回：**`None`

**示例：**

```python
page.download_set.save_path(r'D:\tmp')
```

?>**Tips：**<br>- 保存路径可指定不存在的文件夹，程序会自动创建。<br>- 设置默认保存路径后，每个任务仍可在创建时指定自己的保存路径，以覆盖默认设置。

---

## 📍 设置下载方式

### 🔸 `download_set.by_DownloadKit()`

此方法用于设置当前页面对象使用`DownloadKit`下载文件。

### 🔸 `download_set.by_browser()`

此方法用于设置当前页面对象使用浏览器下载工具下载文件。

---

## 📍 设置重名文件处理方法

下载过程中可能遇到保存路径已存在同名文件，`DownloadKit`提供 3 种处理方法。

!>**注意：**<br>这个设置只有在使用`DownloadKit`作为下载工具时有效。

### 🔸 `download_set.if_file_exists.rename()`

此方式会对新文件进行重命名，在文件名后加上序号。

假如，保存路径已存在`abc.txt`文件，新的`abc.txt`文件会自动重命名为`abc_1.txt`，再下载一个`abc.txt`时，会命名为`abc_2.txt`，如此类推。

**示例：** 3 次下载同一个文件

```python
page.download_set.if_file_exists.rename()
page.download('http://xxxxx/xxx.pdf')
page.download('http://xxxxx/xxx.pdf')
page.download('http://xxxxx/xxx.pdf')
```

在文件夹会生成如下 3 个文件：

```
xxx.pdf
xxx_1.pdf
xxx_2.pdf
```

---

### 🔸 `download_set.if_file_exists.skip()`

遇到同名文件时，跳过。同时任务对象的`result`属性设置为`'skip'`。

**示例：** 3 次下载同一个文件

```python
page.download_set.if_file_exists.skip()
page.download('http://xxxxx/xxx.pdf')
page.download('http://xxxxx/xxx.pdf')
page.download('http://xxxxx/xxx.pdf')
```

在文件夹只会生成如下 1 个文件：

```
xxx.pdf
```

---

### 🔸 `download_set.if_file_exists.overwrite()`

遇到同名文件时，覆盖。

!>**注意：**<br>这个方式在多线程下载时要慎用，万一多个任务下载多个同名文件，会导致互相覆盖的现象。

?>**Tips：**<br>- 除了整体设置，还可以在创建任务时单独设置该任务的处理方式。<br>- 文件名如遇到`'?'`、`'\'`等非法字符，会自动替换为空格。

---

## 📍 设置大文件是否分块

`DownloadKit`具备多线程下载大文件功能，在文件超过指定大小时（默认 50MB），可对文件进行多线程分块下载，每个线程负责 50MB 的下载，以提高下载速度。这个功能默认是关闭的，您可以设置是否开启。

!>**注意：**  
这个设置只有在使用`DownloadKit`作为下载工具时有效。

### 🔸 `download_set.split()`

```python
# 使用分块下载
page.download_set.split(on_off=True)
# 禁用分块下载
page.download_set.split(on_off=False)
```

?>**Tips：**  
除了整体设置，还可以在创建任务时单独设置该任务是否使用分块下载。

---

## 📍运行参数设置

运行参数主要包括可使用线程上限、连接失败重试次数、重试间隔。

### 🔸 `download.roads`

此参数设置整个`DownloadKit`对象允许使用的线程数上限，默认为 10。

默认设置下，页面对象最多使用 10 个下载线程，如进程已满，新任务会进入等待队列排队，待有空线程时自动开始。

这个属性只能在没有任务在运行的时候设置。

```python
page.download.roads = 20  # 允许最多使用20个线程进行下载
```

---

### 🔸 `download.retry`

此属性用于设置连接失败时重试次数，默认为 3。

```python
page.download.roads = 5  # 设置连接失败时重试5次
```

---

### 🔸 `download.interval`

此属性用于设置连接失败时重试间隔，默认为 5 秒。

```python
page.download.interval = 10  # 设置连接失败时等待10秒再重试
```

?> **Tips：**<br> 重试次数和间隔在初始化时继承页面对象的`retry_times`和`retry_interval`属性，可用上面例子的方法对下载的重试次数和间隔进行设置，设置后不会影响页面对象的设置。
