package com.ll.DataRecorder;

import javax.naming.NoPermissionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 记录器的基类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public abstract class OriginalRecorder {
    protected int cache;
    protected String path;
    protected String type;
    protected List<Object> data;
    protected Lock lock = new ReentrantLock();//线程锁
    protected boolean pauseAdd = false;
    protected boolean pauseWrite = false;
    public boolean showMsg = true;
    protected OriginalSetter<?> setter;
    protected int dataCount = 0;

    /**
     *
     */
    public OriginalRecorder() {
        this("");
    }

    /**
     * @param cacheSize 每接收多少条记录写入文件，0为不自动写入
     */
    public OriginalRecorder(Integer cacheSize) {
        this("", cacheSize);
    }

    /**
     * @param path 保存的文件路径
     */
    public OriginalRecorder(Path path) {
        this(path.toString(), null);
    }

    /**
     * @param path      保存的文件路径
     * @param cacheSize 每接收多少条记录写入文件，0为不自动写入
     */
    public OriginalRecorder(Path path, Integer cacheSize) {
        this(path.toString(), cacheSize);
    }

    /**
     * @param path 保存的文件路径
     */
    public OriginalRecorder(String path) {
        this(path, null);
    }

    /**
     * @param path      保存的文件路径
     * @param cacheSize 每接收多少条记录写入文件，0为不自动写入
     */
    public OriginalRecorder(String path, Integer cacheSize) {
        this.set().path(path);
        this.cache = cacheSize != null ? cacheSize : 1000;
    }

    /**
     * @return 返回用于设置属性的对象
     */
    public OriginalSetter<?> set() {
        if (this.setter == null) this.setter = new OriginalSetter<>(this);
        return this.setter;
    }

    /**
     * @return 返回缓存大小
     */
    public int cacheSize() {
        return this.cache;
    }

    /**
     * @return 返回文件路径
     */
    public String path() {
        return this.path;
    }

    /**
     * @return 返回文件类型
     */
    public String type() {
        return this.type;
    }

    /**
     * @return 返回当前保存在缓存的数据
     */
    public Object data() {
        return this.data;
    }

    /**
     * 记录数据，可保存到新文件
     *
     * @return 文件路径
     */
    public String record() {
        return record("");
    }

    /**
     * 记录数据，可保存到新文件
     *
     * @param newPath 文件另存为的路径，会保存新文件
     * @return 文件路径
     */

    public String record(Path newPath) {
        return record(newPath.toString());
    }


    /**
     * 记录数据，可保存到新文件
     *
     * @param newPath 文件另存为的路径，会保存新文件
     * @return 文件路径
     * @throws IOException 读写文件时可能发生IOException
     */
    public String record(String newPath) {
        if ("".equals(newPath)) newPath = null;
        // 具体功能由_record()实现，本方法实现自动重试及另存文件功能
        String originalPath = path;
        String returnPath = path;
        if (newPath != null && !newPath.isEmpty()) {
            newPath = Tools.getUsablePath(newPath).toString();
            returnPath = path = newPath;

            Path originalFilePath = Paths.get(originalPath);
            if (Files.exists(originalFilePath)) {
                Path newPathObject = Paths.get(newPath);
                try {
                    Files.copy(originalFilePath, newPathObject, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (!data.isEmpty()) {
            return returnPath;
        }

        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("保存路径为空。");
        }

        lock.lock();
        try {
            pauseAdd = true;  // 写入文件前暂缓接收数据
            if (showMsg) {
                System.out.println(path + " 开始写入文件，切勿关闭进程。");
            }

            try {
                Files.createDirectories(Paths.get(path).getParent());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            while (true) {
                try {
                    while (pauseWrite) {  // 等待其它线程写入结束
                        Thread.sleep(100);
                    }
                    pauseWrite = true;
                    this._record();
                    break;

                } catch (NoPermissionException e) {

                } catch (Exception e) {
                    try {
                        Files.write(Paths.get("failed_data.txt"), (data.toString() + "\n").getBytes());
                        System.out.println("保存失败的数据已保存到failed_data.txt。");
                    } catch (IOException ioException) {
                        throw e;
                    }
                    throw e;
                } finally {
                    pauseWrite = false;
                }

                Thread.sleep(300);
            }

            if (newPath != null && !newPath.isEmpty()) {
                path = originalPath;
            }

            if (showMsg) {
                System.out.println(path + " 写入文件结束。");
            }
            clear();
            dataCount = 0;
            pauseAdd = false;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

        return returnPath;
    }

    /**
     * 清空缓存中的数据
     */
    public void clear() {
        if (this.data != null) this.data.clear();
    }

    public abstract void addData(Object data);

    protected abstract void _record() throws NoPermissionException;
}
