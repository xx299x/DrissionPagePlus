package com.ll.DrissonPage.units.downloader;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
public class DownloadMission {
    private final String tabId;
    private final DownloadManager mgr;
    private final String url;
    private final String path;
    private final String name;
    protected  Integer totalBytes;
    protected  int receiveBytes;
    private final String savePath;
    private final Boolean isDone;
    protected String id;
    protected String state;
    protected String finalPath;

    public DownloadMission(DownloadManager mgr, String tabId, String id, String path, String name, String url, String savePath) {
        this.mgr = mgr;
        this.url = url;
        this.tabId = tabId;
        this.id = id;
        this.path = path;
        this.name = name;
        this.state = "running";
        this.totalBytes = null;
        this.receiveBytes = 0;
        this.finalPath = null;
        this.savePath = savePath;
        this.isDone = null;
    }

    /**
     * 以百分比形式返回下载进度
     */
    public Float rate() {
        return this.totalBytes != null ? new BigDecimal(this.receiveBytes).divide(new BigDecimal(this.totalBytes * 100), 2, RoundingMode.FLOOR).floatValue() : null;
    }

    /**
     * @return 返回任务是否在运行中
     */
    public boolean isDone() {
        return this.isDone;
    }

    /**
     * 取消该任务，如任务已完成，删除已下载的文件
     */
    public void cancel() {
        this.mgr.cancel(this);
    }

    /**
     * 等待任务结束
     *
     * @return 等待成功返回完整路径，否则返回null
     */

    public String waits() {
        return wait(true);
    }

    /**
     * 等待任务结束
     *
     * @param show 是否显示下载信息
     * @return 等待成功返回完整路径，否则返回null
     */

    public String wait(boolean show) {
        return wait(show, null);
    }

    /**
     * 等待任务结束
     *
     * @param show    是否显示下载信息
     * @param timeout 超时时间，为null则无限等待
     * @return 等待成功返回完整路径，否则返回null
     */

    public String wait(boolean show, Double timeout) {
        return wait(show, timeout, true);
    }

    /**
     * 等待任务结束
     *
     * @param show            是否显示下载信息
     * @param timeout         超时时间，为null则无限等待
     * @param cancelIfTimeout 超时时是否取消任务
     * @return 等待成功返回完整路径，否则返回null
     */

    public String wait(boolean show, Double timeout, boolean cancelIfTimeout) {
        if (show) {
            System.out.println("url:" + url);
            long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
            while (this.name == null && System.currentTimeMillis() < endTime) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("文件名：" + this.name);
            System.out.println("目标路径：" + this.path);
        }
        if (timeout == null) {
            while (!this.isDone) {
                if (show) {
                    System.out.println(this.rate() + "%");
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
            while (System.currentTimeMillis() < endTime) {
                if (show) {
                    System.out.println(this.rate() + "%");
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!this.isDone && cancelIfTimeout) {
                this.cancel();
            }
        }
        if (show) {
            switch (this.state) {
                case "completed":
                    System.out.println("下载完成" + this.finalPath);
                    break;
                case "canceled":
                    System.out.println("下载取消");
                    break;
                case "skipped":
                    System.out.println("已跳过");
                    break;

            }
        }
        return this.finalPath;
    }
}
