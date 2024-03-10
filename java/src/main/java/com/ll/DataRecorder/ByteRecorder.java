package com.ll.DataRecorder;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class ByteRecorder extends OriginalRecorder {
    private static final long[] END = {0, 2};
    protected List<ByteData> data;

    /**
     * 用于记录字节数据的工具
     */
    public ByteRecorder() {
        this("");
    }

    /**
     * 用于记录字节数据的工具
     *
     * @param path 保存的文件路径
     */
    public ByteRecorder(Path path) {
        this(path, null);
    }

    /**
     * 用于记录字节数据的工具
     *
     * @param cacheSize 每接收多少条记录写入文件，0为不自动写入
     */
    public ByteRecorder(Integer cacheSize) {
        this("", null);
    }

    /**
     * 用于记录字节数据的工具
     *
     * @param path      保存的文件路径
     * @param cacheSize 每接收多少条记录写入文件，0为不自动写入
     */
    public ByteRecorder(Path path, Integer cacheSize) {
        super(path == null ? null : path.toAbsolutePath().toString(), cacheSize);
    }

    /**
     * 用于记录字节数据的工具
     *
     * @param path 保存的文件路径
     */
    public ByteRecorder(String path) {
        super(path, null);
    }

    /**
     * 用于记录字节数据的工具
     *
     * @param path      保存的文件路径
     * @param cacheSize 每接收多少条记录写入文件，0为不自动写入
     */
    public ByteRecorder(String path, Integer cacheSize) {
        super("".equals(path) ? null : path, cacheSize);
    }

    /**
     * @param data 类型只能为byte[]
     */
    @Override
    public void addData(Object data) {
        if (data instanceof byte[]) addData((byte[]) data, null);
        else throw new IllegalArgumentException("data类型只能为byte[]为了兼容");
    }


    /**
     * 添加一段二进制数据
     *
     * @param data bytes类型数据
     * @param seek 在文件中的位置，None表示最后
     */
    public void addData(byte[] data, Long seek) {
        while (this.pauseAdd) {  //等待其它线程写入结束
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (seek != null && seek < 0) throw new IllegalArgumentException("seek参数只能接受null或大于等于0的整数。");
        this.data.add(new ByteData(data, seek));
        this.dataCount++;
        if (0 < this.cacheSize() && this.cacheSize() <= this.dataCount) this.record();

    }

    /**
     * @return 返回当前保存在缓存的数据
     */
    public List<ByteData> data() {
        return this.data;
    }

    /**
     * 记录数据到文件
     */
    protected void _record() {
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (SeekableByteChannel fileChannel = Files.newByteChannel(filePath, StandardOpenOption.WRITE, StandardOpenOption.READ)) {
            long[] previous = null;
            for (ByteData entry : data) {

                long[] loc = (entry.seek == null ? ByteRecorder.END : new long[]{entry.seek, 0});
                if (!(previous != null && previous[0] == loc[0] && previous[1] == loc[1] && ByteRecorder.END[0] == loc[0] && ByteRecorder.END[1] == loc[1])) {
                    fileChannel.position(loc[0] + loc[1]);
                    previous = loc;
                }
                fileChannel.write(ByteBuffer.wrap(entry.data));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ByteData {
        private byte[] data;
        private Long seek;
    }
}
