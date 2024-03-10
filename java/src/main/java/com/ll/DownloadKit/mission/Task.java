package com.ll.DownloadKit.mission;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * 子任务类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Task extends BaseTask {

    @Getter
    private Mission mission;
    @Getter
    List<Object> range;
    protected int downloadedSize;
    private Long size;

    /**
     * @param id 任务id
     */
    public Task(Mission mission, List<Object> range, String id, long size) {
        super(id);
        this.mission = mission;
        this.range = range;
        this.size = size;
        this.downloadedSize = 0;
    }

    /**
     * @return 返回父任务id
     */
    public String mid() {
        return this.mission.id();
    }

    /**
     * @return 返回任务数据对象
     */
    public MissionData data() {
        return this.mission.data();
    }

    /**
     * @return 返回文件保存路径
     */

    public String path() {
        return this.mission.path();
    }


    /**
     * @return 返回文件名
     */
    public String fileName() {
        return this.mission.fileName;
    }

    /**
     * @return 返回下载进度百分比
     */

    public Float rate() {
        return this.size == null ? null : new BigDecimal(this.downloadedSize * 100).divide(new BigDecimal(this.size), 2, RoundingMode.FLOOR).floatValue();
    }


    public void addData(byte[] data) {
        addData(data, null);
    }

    public void addData(byte[] data, Long seek) {
        this.downloadedSize += data.length;
        this.mission.recorder().addData(data, seek);

    }

    /**
     * 清除以接收但未写入硬盘的缓存
     */
    public void clearCache() {
        this.mission.recorder().clear();
    }

    /**
     * 设置一个子任务为done状态
     *
     * @param result 结果：'success'、'skipped'、'canceled'、'false'、'null'
     * @param info   任务信息
     */
    public void _setDone(String result, String info) {
        this.setStates(result, info, Task.DONE);
        this.mission.aTaskDone(!Objects.equals(result, "false"), info);
    }


}
