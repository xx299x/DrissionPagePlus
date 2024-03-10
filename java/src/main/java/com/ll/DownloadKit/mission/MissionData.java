package com.ll.DownloadKit.mission;

import com.ll.DownloadKit.FileMode;
import lombok.Getter;
import lombok.Setter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.Map;

/**
 * 保存任务数据的对象
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class MissionData {
    /**
     * 下载文件url
     */
    @Getter

    protected String url;
    /**
     * 保存文件夹
     */
    @Getter
    protected String goalPath;
    /**
     * 文件重命名
     */
    @Getter
    protected String rename;
    /**
     * 文件重命名后缀名
     */
    @Getter
    protected String suffix;
    /**
     * 存在重名文件时处理方式
     */
    @Getter
    protected FileMode fileExists;
    /**
     * 是否允许分块下载
     */
    @Getter
    protected boolean split;
    /**
     * requests其它参数
     */
    @Getter
    protected Map<String, Object> params;
    /**
     * 文件存储偏移量
     */
    @Setter
    @Getter
    protected Long offset;

    public MissionData(String url, String goalPath, String rename, String suffix, FileMode fileExists, boolean split, Map<String, Object> params, Long offset) {
        if (url != null) {
            //版本兼容
            try {
                this.url = URLEncoder.encode(url, "utf-8").replaceAll("\\+", "%20").replaceAll("%21", "!").replaceAll("%27", "'").replaceAll("%28", "(").replaceAll("%29", ")").replaceAll("%7E", "~");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        this.goalPath = goalPath;
        this.rename = rename;
        this.suffix = suffix;
        this.fileExists = fileExists;
        this.split = split;
        this.params = params;
        this.offset = offset;
    }

    public MissionData(String url, Path goalPath, String rename, String suffix, FileMode fileExists, boolean split, Map<String, Object> params, Long offset) {
        this(url, goalPath.toAbsolutePath().toString(),rename,suffix,fileExists,split,params,offset);
    }
}
