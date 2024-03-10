package com.ll.DownloadKit.mission;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 任务类基类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class BaseTask {
    protected final static String DONE = "done";
    public final static Map<String, String> RESULT_TEXTS = Map.of("success", "成功", "skipped", "跳过", "canceled", "取消", "false", "失败", "null", "未知");
    /**
     * 任务id
     */
    private final String id;
    @Getter
    @Setter

    protected String state = "waiting";  // 'waiting'、'running'、'done'
    @Getter
    protected String result = "null";     //'success'、'skipped'、'canceled'、'false'、'null'
    @Getter
    @Setter
    protected String info = "等待下载"; // 信息

    /**
     * @param id 任务id
     */
    public BaseTask(String id) {
        this.id = id;
    }

    /**
     * @return 返回任务或子任务id
     */
    public String id() {
        return this.id;
    }

    /**
     * @return 返回任务数据
     */

    public MissionData data() {
        return null;
    }

    /**
     * @return 返回任务是否结束
     */
    public boolean isDone() {
        return "done".equalsIgnoreCase(this.state) || "cancel".equalsIgnoreCase(this.state);
    }

    /**
     * 设置任务结果值
     */
    public void setStates() {
        setStates(result, info, "done");
    }

    /**
     * 设置任务结果值
     *
     * @param result 结果：'success'、'skipped'、'canceled'、false、null
     */
    public void setStates(String result) {
        setStates(result, null);
    }

    /**
     * 设置任务结果值
     *
     * @param result 结果：'success'、'skipped'、'canceled'、false、null
     * @param info   任务信息
     */
    public void setStates(String result, String info) {
        setStates(result, info, "done");
    }

    /**
     * 设置任务结果值
     *
     * @param result 结果：'success'、'skipped'、'canceled'、false、null
     * @param info   任务信息
     * @param state  任务状态：'waiting'、'running'、'done'
     */
    public void setStates(String result, String info, String state) {
        if (result == null) result = "null";
        result = result.toLowerCase().trim();
        switch (result) {
            case "success":
                this.result = "success";
                break;
            case "skipped":
                this.result = "skipped";
                break;
            case "canceled":
                this.result = "canceled";
                break;
            case "false":
                this.result = "False";
                break;
            case "null":
                this.result = "null";
                break;
            default:
                this.result = "null";
                break;
        }
        this.info = info;
        if (state == null) state = "done";
        state = state.toLowerCase().trim();
        switch (state) {
            case "running":
                this.state = "running";
                break;
            case "waiting":
                this.state = "waiting";
                break;
            default:
                this.state = "done";
                break;
        }
    }
}
