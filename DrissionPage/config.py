# -*- coding:utf-8 -*-
"""
配置文件
"""

from pathlib import Path

global_tmp_path = f'{str(Path(__file__).parent)}\\tmp'
Path(global_tmp_path).mkdir(parents=True, exist_ok=True)

global_driver_options = {
    # ---------------已打开的浏览器---------------
    # 'debuggerAddress': '127.0.0.1:9222',
    # ---------------chromedriver路径---------------
    'chromedriver_path': r'D:\python\Google Chrome\Chrome\chromedriver.exe',
    # ---------------手动指定使用的浏览器位置---------------
    # 'binary_location': r'C:\Program Files (x86)\Google\Chrome\Application\chrome.exe',
    # ---------------启动参数---------------
    'arguments': [
        '--headless',  # 隐藏浏览器窗口
        '--mute-audio',  # 静音
        '--no-sandbox',
        '--blink-settings=imagesEnabled=false',  # 不加载图片
        # r'--user-data-dir="E:\tmp\chrome_tmp"',  # 指定用户文件夹路径
        # '-–disk-cache-dir=""',  # 指定缓存路径
        'zh_CN.UTF-8',  # 编码格式
        # "--proxy-server=http://127.0.0.1:8888",  # 设置代理
        # '--hide-scrollbars',  # 隐藏滚动条
        # '--start-maximized',  # 浏览器窗口最大化
        # "--disable-javascript",  # 禁用JavaScript
        # 模拟移动设备
        # 'user-agent="MQQBrowser/26 Mozilla/5.0 (Linux; U; Android 2.3.7; zh-cn; MB200 Build/GRJ22; CyanogenMod-7) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1"',
        '--disable-gpu'  # 谷歌文档提到需要加上这个属性来规避bug
    ],
    # ---------------扩展文件---------------
    'extension_files': [],
    # 'extensions': [],
    # ---------------实验性质的设置参数---------------
    'experimental_options': {
        'prefs': {
            # 设置下载路径
            'download.default_directory': global_tmp_path,
            # 下载不弹出窗口
            'profile.default_content_settings.popups': 0,
            # 无弹窗
            'profile.default_content_setting_values': {'notifications': 2},
            # 禁用PDF插件
            'plugins.plugins_list': [{"enabled": False, "name": "Chrome PDF Viewer"}],
            # 设置为开发者模式，防反爬虫
            'excludeSwitches': ["ignore-certificate-errors", "enable-automation"]
        }

    }
}

global_session_options = {
    'headers': {
        "User-Agent": 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/603.3.8 (KHTML, like Gecko)'
                      ' Version/10.1.2 Safari/603.3.8',
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language": "zh-cn", "Connection": "keep-alive",
        "Accept-Charset": "GB2312,utf-8;q=0.7,*;q=0.7"}
}
