package com.ll.DrissonPage.units.waiter;

import com.ll.DrissonPage.page.ChromiumBase;
import com.ll.DrissonPage.units.downloader.DownloadMission;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class TabWaiter extends BaseWaiter {
    public TabWaiter(ChromiumBase chromiumBase) {
        super(chromiumBase);
    }

    /**
     * 等待所有浏览器下载任务结束
     *
     * @return 是否等待成功
     */
    public boolean downloadsDone() {
        return downloadsDone(null);
    }

    /**
     * 等待所有浏览器下载任务结束
     *
     * @param timeout 超时时间，为null时无限等待
     * @return 是否等待成功
     */
    public boolean downloadsDone(Float timeout) {
        return downloadsDone(timeout, true);
    }

    /**
     * 等待所有浏览器下载任务结束
     *
     * @param timeout         超时时间，为null时无限等待
     * @param cancelIfTimeout 超时时是否取消剩余任务
     * @return 是否等待成功
     */
    public boolean downloadsDone(Float timeout, boolean cancelIfTimeout) {
        if (timeout == null) {
            while (!this.driver.browser().getDlMgr().getTabMissions(this.driver.tabId()).isEmpty()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        } else {
            long endTime = (long) (System.currentTimeMillis() + this.driver.timeout() * 1000);
            while (System.currentTimeMillis() < endTime) {
                if (this.driver.browser().getDlMgr().getTabMissions(this.driver.tabId()).isEmpty()) {
                    return true;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!this.driver.browser().getDlMgr().getTabMissions(this.driver.tabId()).isEmpty()) {
                if (cancelIfTimeout) {
                    for (DownloadMission tabMission : this.driver.browser().getDlMgr().getTabMissions(this.driver.tabId())) {
                        tabMission.cancel();
                    }
                }
                return false;
            }
            return true;
        }
    }

    /**
     * 等待弹出框关闭
     */
    public void alertClose() {
        while (!super.driver.states().hasAlert()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        while (super.driver.states().hasAlert()) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
