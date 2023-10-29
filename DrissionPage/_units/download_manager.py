# -*- coding:utf-8 -*-
"""
@Author  :   g1879
@Contact :   g1879@qq.com
"""
from os.path import sep
from pathlib import Path
from shutil import move
from time import sleep, perf_counter

from .._commons.tools import get_usable_path


class BrowserDownloadManager(object):

    def __init__(self, browser):
        """
        :param browser: Browser对象
        """
        self._browser = browser
        self._page = browser.page
        self._when_download_file_exists = 'rename'

        t = TabDownloadSettings(self._page.tab_id)
        t.path = self._page.download_path
        self._missions = {}  # {guid: DownloadMission}
        self._tab_missions = {}  # {tab_id: DownloadMission}
        self._flags = {}  # {tab_id: [bool, DownloadMission]}

        self._browser.driver.set_listener('Browser.downloadProgress', self._onDownloadProgress)
        self._browser.driver.set_listener('Browser.downloadWillBegin', self._onDownloadWillBegin)
        self._browser.run_cdp('Browser.setDownloadBehavior', downloadPath=self._page.download_path,
                              behavior='allowAndName', eventsEnabled=True)

    @property
    def missions(self):
        """返回所有未完成的下载任务"""
        return self._missions

    def set_path(self, tab_id, path):
        """设置某个tab的下载路径
        :param tab_id: tab id
        :param path: 下载路径
        :return: None
        """
        TabDownloadSettings(tab_id).path = str(Path(path).absolute())
        if tab_id == self._page.tab_id:
            self._browser.run_cdp('Browser.setDownloadBehavior', downloadPath=str(Path(path).absolute()),
                                  behavior='allowAndName', eventsEnabled=True)

    def set_rename(self, tab_id, rename):
        """设置某个tab的重命名文件名
        :param tab_id: tab id
        :param rename: 文件名
        :return: None
        """
        TabDownloadSettings(tab_id).rename = rename

    def set_file_exists(self, tab_id, mode):
        """设置某个tab下载文件重名时执行的策略
        :param tab_id: tab id
        :param mode: 下载路径
        :return: None
        """
        TabDownloadSettings(tab_id).when_file_exists = mode

    def set_flag(self, tab_id, flag):
        """设置某个tab的重命名文件名
        :param tab_id: tab id
        :param flag: 等待标志
        :return: None
        """
        self._flags[tab_id] = flag

    def get_flag(self, tab_id):
        """获取tab下载等待标记
        :param tab_id: tab id
        :return: 任务对象或False
        """
        return self._flags.get(tab_id, None)

    def get_tab_missions(self, tab_id):
        """获取某个tab正在下载的任务
        :param tab_id:
        :return: 下载任务组成的列表
        """
        return self._tab_missions.get(tab_id, [])

    def set_done(self, mission, state, final_path=None):
        """设置任务结束
        :param mission: 任务对象
        :param state: 任务状态
        :param final_path: 最终路径
        :return: None
        """
        if mission.state not in ('canceled', 'skipped'):
            mission.state = state
        mission.final_path = final_path
        if mission.tab_id in self._tab_missions and mission.id in self._tab_missions[mission.tab_id]:
            self._tab_missions[mission.tab_id].remove(mission.id)
        self._missions.pop(mission.id)
        mission._is_done = True

    def cancel(self, mission):
        """取消任务
        :param mission: 任务对象
        :return: None
        """
        mission.state = 'canceled'
        self._browser.run_cdp('Browser.cancelDownload', guid=mission.id)
        if mission.final_path:
            Path(mission.final_path).unlink(True)

    def skip(self, mission):
        """跳过任务
        :param mission: 任务对象
        :return: None
        """
        mission.state = 'skipped'
        self._browser.run_cdp('Browser.cancelDownload', guid=mission.id)

    def clear_tab_info(self, tab_id):
        """当tab关闭时清除有关信息
        :param tab_id: 标签页id
        :return: None
        """
        self._tab_missions.pop(tab_id)
        self._flags.pop(tab_id)
        TabDownloadSettings.TABS.pop(tab_id)

    def _onDownloadWillBegin(self, **kwargs):
        """用于获取弹出新标签页触发的下载任务"""
        guid = kwargs['guid']
        tab_id = self._browser._frames.get(kwargs['frameId'], self._page.tab_id)

        settings = TabDownloadSettings(tab_id if tab_id in TabDownloadSettings.TABS else self._page.tab_id)
        if settings.rename:
            tmp = kwargs['suggestedFilename'].rsplit('.', 1)
            ext_name = tmp[-1] if len(tmp) > 1 else ''
            tmp = settings.rename.rsplit('.', 1)
            ext_rename = tmp[-1] if len(tmp) > 1 else ''
            name = settings.rename if ext_rename == ext_name else f'{settings.rename}.{ext_name}'
            settings.rename = None

        else:
            name = kwargs['suggestedFilename']

        skip = False
        goal_path = Path(settings.path) / name
        if goal_path.exists():
            if settings.when_file_exists == 'skip':
                skip = True
            elif settings.when_file_exists == 'overwrite':
                goal_path.unlink()

        m = DownloadMission(self, tab_id, guid, settings.path, name, kwargs['url'], self._page.download_path)
        self._missions[guid] = m

        if self.get_flag(tab_id) is False:  # 取消该任务
            self.cancel(m)
        elif skip:
            self.skip(m)
        else:
            self._tab_missions.setdefault(tab_id, []).append(guid)

        if self.get_flag(tab_id) is not None:
            self._flags[tab_id] = m

    def _onDownloadProgress(self, **kwargs):
        """下载状态变化时执行"""
        if kwargs['guid'] in self._missions:
            mission = self._missions[kwargs['guid']]
            if kwargs['state'] == 'inProgress':
                mission.received_bytes = kwargs['receivedBytes']
                mission.total_bytes = kwargs['totalBytes']

            elif kwargs['state'] == 'completed':
                if mission.state == 'skipped':
                    Path(f'{mission.save_path}{sep}{mission.id}').unlink(True)
                    self.set_done(mission, 'skipped')
                    return
                mission.received_bytes = kwargs['receivedBytes']
                mission.total_bytes = kwargs['totalBytes']
                form_path = f'{mission.save_path}{sep}{mission.id}'
                to_path = str(get_usable_path(f'{mission.path}{sep}{mission.name}'))
                move(form_path, to_path)
                self.set_done(mission, 'completed', final_path=to_path)

            else:  # 'canceled'
                self.set_done(mission, 'canceled')


class TabDownloadSettings(object):
    TABS = {}

    def __new__(cls, tab_id):
        """
        :param tab_id: tab id
        """
        if tab_id in cls.TABS:
            return cls.TABS[tab_id]
        return object.__new__(cls)

    def __init__(self, tab_id):
        """
        :param tab_id: tab id
        """
        if hasattr(self, '_created'):
            return
        self._created = True
        self.tab_id = tab_id
        self.rename = None
        self.path = ''
        self.when_file_exists = 'rename'

        TabDownloadSettings.TABS[tab_id] = self


class DownloadMission(object):
    def __init__(self, mgr, tab_id, _id, path, name, url, save_path):
        """
        :param mgr: BrowserDownloadManager对象
        :param tab_id: 标签页id
        :param _id: 任务id
        :param path: 保存路径
        :param name: 文件名
        :param url: url
        :param save_path: 下载路径
        """
        self._mgr = mgr
        self.url = url
        self.tab_id = tab_id
        self.id = _id
        self.path = path
        self.name = name
        self.state = 'running'
        self.total_bytes = None
        self.received_bytes = 0
        self.final_path = None
        self.save_path = save_path
        self._is_done = False

    def __repr__(self):
        return f'<DownloadMission {id(self)} {self.rate}>'

    @property
    def rate(self):
        """以百分比形式返回下载进度"""
        return round((self.received_bytes / self.total_bytes) * 100, 2) if self.total_bytes else None

    @property
    def is_done(self):
        """返回任务是否在运行中"""
        return self._is_done

    def cancel(self):
        """取消该任务，如任务已完成，删除已下载的文件"""
        self._mgr.cancel(self)

    def wait(self, show=True, timeout=None, cancel_if_timeout=True):
        """等待任务结束
        :param show: 是否显示下载信息
        :param timeout: 超时时间，为None则无限等待
        :param cancel_if_timeout: 超时时是否取消任务
        :return: 等待成功返回完整路径，否则返回False
        """
        if show:
            print(f'url：{self.url}')
            end_time = perf_counter()
            while self.name is None and perf_counter() < end_time:
                sleep(0.01)
            print(f'文件名：{self.name}')
            print(f'目标路径：{self.path}')

        if timeout is None:
            while not self.is_done:
                if show:
                    print(f'\r{self.rate}% ', end='')
                sleep(.2)

        else:
            end_time = perf_counter() + timeout
            while perf_counter() < end_time:
                if show:
                    print(f'\r{self.rate}% ', end='')
                sleep(.2)

            if not self.is_done and cancel_if_timeout:
                self.cancel()

        if show:
            if self.state == 'completed':
                print(f'下载完成 {self.final_path}')
            elif self.state == 'canceled':
                print(f'下载取消')
            elif self.state == 'skipped':
                print(f'已跳过')
            print()

        return self.final_path if self.final_path else False
