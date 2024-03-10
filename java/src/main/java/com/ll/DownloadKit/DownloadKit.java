package com.ll.DownloadKit;

import com.ll.DataRecorder.Recorder;
import com.ll.DownloadKit.mission.BaseTask;
import com.ll.DownloadKit.mission.Mission;
import com.ll.DownloadKit.mission.Task;
import com.ll.DrissonPage.base.BasePage;
import com.ll.DrissonPage.config.SessionOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import okhttp3.*;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 下载器对象
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
public class DownloadKit {
    /**
     * 保存路径
     */
    protected String goalPath = ".";
    /**
     * 最大线程
     */
    protected int roads = 10;
    private Setter setter;
    protected String printMode;
    protected String logMode;
    protected Recorder logger;
    protected Integer retry;
    protected Double interval;
    protected BasePage<?> page;
    private BlockingQueue<BaseTask> waitingList;
    protected OkHttpClient session;
    private int runningCount;
    private int missionsNum;
    private Map<Integer, Mission> missions;
    protected ThreadPoolExecutor threads;
    protected Map<Integer, Map<String, Object>> threadMap;
    protected Double timeout;
    private boolean stopPrinting;
    private final Lock lock = new ReentrantLock();//线程锁
    protected boolean split;
    protected Long blockSize;
    protected String encoding;
    /**
     * 有同名文件名时的处理方式，可选 'skip', 'overwrite', 'rename', 'add'
     */
    protected FileMode fileMode = FileMode.rename;

    /**
     * 使用的Session对象，或配置对象、页面对象等
     */

    public DownloadKit(Path goalPath, Integer roads, FileMode fileMode, BasePage<?> driver) {
        this(goalPath.toAbsolutePath().toString(), roads, fileMode, driver);
    }

    public DownloadKit(String goalPath, Integer roads, FileMode fileMode, BasePage<?> driver) {
        init(goalPath, roads, fileMode);
        this.set().driver(driver);
    }

    public DownloadKit(Path goalPath, Integer roads, FileMode fileMode, OkHttpClient driver) {
        this(goalPath.toAbsolutePath().toString(), roads, fileMode, driver);
    }

    public DownloadKit(String goalPath, Integer roads, FileMode fileMode, OkHttpClient driver) {
        init(goalPath, roads, fileMode);
        this.set().driver(driver);

    }

    public DownloadKit(Path goalPath, Integer roads, FileMode fileMode, SessionOptions driver) {
        this(goalPath.toAbsolutePath().toString(), roads, fileMode, driver);
    }

    public DownloadKit(String goalPath, Integer roads, FileMode fileMode, SessionOptions driver) {
        init(goalPath, roads, fileMode);
        this.set().driver(driver);
    }

    private void init(String goalPath, Integer roads, FileMode fileMode) {
        if (roads != null && roads > 0) this.roads = roads;
        this.missions = new HashMap<>();
        this.waitingList = new LinkedBlockingQueue<>();
        this.threadMap = new ConcurrentHashMap<>(this.roads);
        this.threads = new ThreadPoolExecutor(5, this.roads, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        this.missionsNum = 0;
        this.runningCount = 0;  //正在运行的任务数
        this.stopPrinting = false; //用于控制显示线程停止
        this.goalPath = goalPath != null && !goalPath.trim().isEmpty() ? Utils.pathSetter(goalPath.trim()) : ".";
        if (fileMode != null) this.fileMode = fileMode;
        this.split = true;
        this.blockSize = Utils.blockSizeSetter("50M");
    }

    /***
     *
     * @return 用于设置打印和记录模式的对象
     */
    public Setter set() {
        if (setter == null) setter = new Setter(this);
        return setter;
    }

    /**
     * @return 可同时运行的线程数
     */
    public Integer roads() {
        return this.roads;
    }

    /**
     * @return 返回连接失败时重试次数
     */
    public int retry() {
        if (this.retry != null) return this.retry;
        else if (this.page != null) return this.page.getRetryTimes();
        return 3;
    }

    /**
     * @return 返回连接失败时重试间隔
     */
    public double interval() {
        if (this.interval != null) return this.interval;
        else if (this.page != null) return this.page.getRetryInterval();
        return 5.0;
    }

    /**
     * @return 返回连接超时时间
     */
    public double timeout() {
        if (this.timeout != null) return this.timeout;
        else if (this.page != null) return this.page.timeout();
        return 20.0;
    }

    /**
     * @return 返回等待队列
     */
    public BlockingQueue<BaseTask> waitingList() {
        return this.waitingList;
    }

    /**
     * @return 返回用于保存默认连接设置的Session对象
     */
    public OkHttpClient session() {
        return this.session;
    }

    /**
     * @return 返回是否有线程还在运行
     */
    public boolean isRunning() {
        return this.runningCount > 0;
    }

    /**
     * @return map方式返回所有任务对象
     */
    public Map<Integer, Mission> missions() {
        return this.missions;
    }

    /**
     * @return 返回指定的编码格式
     */
    public String encoding() {
        return this.encoding;
    }


    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl 文件网址
     * @return 任务对象
     */
    public Mission add(String fileUrl) {
        return add(fileUrl, new HashMap<>());
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl 文件网址
     * @param params  连接参数
     * @return 任务对象
     */
    public Mission add(String fileUrl, Map<String, Object> params) {
        return add(fileUrl, "", params);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @return 任务对象
     */
    public Mission add(String fileUrl, String goalPath) {
        return add(fileUrl, goalPath, new HashMap<>());
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param params   连接参数
     * @return 任务对象
     */
    public Mission add(String fileUrl, String goalPath, Map<String, Object> params) {
        return add(fileUrl, goalPath == null || goalPath.isEmpty() ? null : goalPath, null, params);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @return 任务对象
     */
    public Mission add(String fileUrl, String goalPath, String rename) {
        return add(fileUrl, goalPath, rename, new HashMap<>());
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param params   连接参数
     * @return 任务对象
     */
    public Mission add(String fileUrl, String goalPath, String rename, Map<String, Object> params) {
        return add(fileUrl, goalPath, rename, null, params);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @return 任务对象
     */
    public Mission add(String fileUrl, String goalPath, String rename, String suffix) {
        return add(fileUrl, goalPath, rename, suffix, new HashMap<>());
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param params   连接参数
     * @return 任务对象
     */
    public Mission add(String fileUrl, String goalPath, String rename, String suffix, Map<String, Object> params) {
        return add(fileUrl, goalPath, rename, suffix, null, params);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @return 任务对象
     */
    public Mission add(String fileUrl, String goalPath, String rename, String suffix, FileMode fileMode) {
        return add(fileUrl, goalPath, rename, suffix, fileMode, null);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param fileMode 遇到同名文件时的处理方式，可选 'skip', 'overwrite', 'rename', 'add'，默认跟随实例属性
     * @param params   连接参数
     * @return 任务对象
     */
    public Mission add(String fileUrl, String goalPath, String rename, String suffix, FileMode fileMode, Map<String, Object> params) {
        return add(fileUrl, Paths.get(goalPath), rename, suffix, fileMode, null, params);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param fileMode 遇到同名文件时的处理方式，可选 'skip', 'overwrite', 'rename', 'add'，默认跟随实例属性
     * @param split    是否允许多线程分块下载，为null则使用对象属性
     * @param params   连接参数
     * @return 任务对象
     */
    public Mission add(String fileUrl, String goalPath, String rename, String suffix, FileMode fileMode, Boolean split, Map<String, Object> params) {
        return add(fileUrl, Paths.get(goalPath), rename, suffix, fileMode, split, params);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @return 任务对象
     */
    public Mission add(String fileUrl, Path goalPath) {
        return add(fileUrl, goalPath, new HashMap<>());
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param params   连接参数
     * @return 任务对象
     */
    public Mission add(String fileUrl, Path goalPath, Map<String, Object> params) {
        return add(fileUrl, goalPath, null, params);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @return 任务对象
     */
    public Mission add(String fileUrl, Path goalPath, String rename) {
        return add(fileUrl, goalPath, rename, new HashMap<>());
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param params   连接参数
     * @return 任务对象
     */
    public Mission add(String fileUrl, Path goalPath, String rename, Map<String, Object> params) {
        return add(fileUrl, goalPath, rename, null, params);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @return 任务对象
     */
    public Mission add(String fileUrl, Path goalPath, String rename, String suffix) {
        return add(fileUrl, goalPath, rename, suffix, new HashMap<>());
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param params   连接参数
     * @return 任务对象
     */
    public Mission add(String fileUrl, Path goalPath, String rename, String suffix, Map<String, Object> params) {
        return add(fileUrl, goalPath, rename, suffix, null, params);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @return 任务对象
     */
    public Mission add(String fileUrl, Path goalPath, String rename, String suffix, FileMode fileMode) {
        return add(fileUrl, goalPath, rename, suffix, fileMode, null);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param fileMode 遇到同名文件时的处理方式，可选 'skip', 'overwrite', 'rename', 'add'，默认跟随实例属性
     * @param params   连接参数
     * @return 任务对象
     */
    public Mission add(String fileUrl, Path goalPath, String rename, String suffix, FileMode fileMode, Map<String, Object> params) {
        return add(fileUrl, goalPath, rename, suffix, fileMode, null, params);
    }

    /**
     * 添加一个下载任务并将其返回
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param fileMode 遇到同名文件时的处理方式，可选 'skip', 'overwrite', 'rename', 'add'，默认跟随实例属性
     * @param split    是否允许多线程分块下载，为null则使用对象属性
     * @param params   连接参数
     * @return 任务对象
     */
    public Mission add(String fileUrl, Path goalPath, String rename, String suffix, FileMode fileMode, Boolean split, Map<String, Object> params) {
        this.missionsNum++;
        this.runningCount++;
        fileMode = fileMode == null ? this.fileMode : fileMode;
        Mission mission = goalPath == null ? new Mission(String.valueOf(this.missionsNum), this, fileUrl, this.goalPath, rename, suffix, fileMode, split == null ? this.split : split, this.encoding, params) : new Mission(String.valueOf(this.missionsNum), this, fileUrl, goalPath, rename, suffix, fileMode, split == null ? this.split : split, this.encoding, params);
        this.missions.put(this.missionsNum, mission);
        this.runOrWait(mission);
        return mission;
    }


    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl 文件网址
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl) {
        return download(fileUrl, new HashMap<>());
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl 文件网址
     * @param params  连接参数
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, Map<String, Object> params) {
        return download(fileUrl, "", params);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, String goalPath) {
        return download(fileUrl, goalPath, new HashMap<>());
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param params   连接参数
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, String goalPath, Map<String, Object> params) {
        return download(fileUrl, goalPath == null || goalPath.isEmpty() ? null : goalPath, null, params);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, String goalPath, String rename) {
        return download(fileUrl, goalPath, rename, new HashMap<>());
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param params   连接参数
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, String goalPath, String rename, Map<String, Object> params) {
        return download(fileUrl, goalPath, rename, null, params);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, String goalPath, String rename, String suffix) {
        return download(fileUrl, goalPath, rename, suffix, new HashMap<>());
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param params   连接参数
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, String goalPath, String rename, String suffix, Map<String, Object> params) {
        return download(fileUrl, goalPath, rename, suffix, null, params);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, String goalPath, String rename, String suffix, FileMode fileMode) {
        return download(fileUrl, goalPath, rename, suffix, fileMode, null);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param fileMode 遇到同名文件时的处理方式，可选 'skip', 'overwrite', 'rename', 'add'，默认跟随实例属性
     * @param params   连接参数
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, String goalPath, String rename, String suffix, FileMode fileMode, Map<String, Object> params) {
        return download(fileUrl, Paths.get(goalPath), rename, suffix, fileMode, true, params);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param fileMode 遇到同名文件时的处理方式，可选 'skip', 'overwrite', 'rename', 'add'，默认跟随实例属性
     * @param showMsg  是否打印进度
     * @param params   连接参数
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, String goalPath, String rename, String suffix, FileMode fileMode, boolean showMsg, Map<String, Object> params) {
        return download(fileUrl, Paths.get(goalPath), rename, suffix, fileMode, split, params);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, Path goalPath) {
        return download(fileUrl, goalPath, new HashMap<>());
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param params   连接参数
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, Path goalPath, Map<String, Object> params) {
        return download(fileUrl, goalPath, null, params);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, Path goalPath, String rename) {
        return download(fileUrl, goalPath, rename, new HashMap<>());
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param params   连接参数
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, Path goalPath, String rename, Map<String, Object> params) {
        return download(fileUrl, goalPath, rename, null, params);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, Path goalPath, String rename, String suffix) {
        return download(fileUrl, goalPath, rename, suffix, new HashMap<>());
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param params   连接参数
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, Path goalPath, String rename, String suffix, Map<String, Object> params) {
        return download(fileUrl, goalPath, rename, suffix, null, params);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, Path goalPath, String rename, String suffix, FileMode fileMode) {
        return download(fileUrl, goalPath, rename, suffix, fileMode, null);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param fileMode 遇到同名文件时的处理方式，可选 'skip', 'overwrite', 'rename', 'add'，默认跟随实例属性
     * @param params   连接参数
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, Path goalPath, String rename, String suffix, FileMode fileMode, Map<String, Object> params) {
        return download(fileUrl, goalPath, rename, suffix, fileMode, true, params);
    }

    /**
     * 以阻塞的方式下载一个文件并返回结果
     *
     * @param fileUrl  文件网址
     * @param goalPath 保存路径
     * @param rename   重命名的文件名
     * @param suffix   重命名的文件后缀名
     * @param fileMode 遇到同名文件时的处理方式，可选 'skip', 'overwrite', 'rename', 'add'，默认跟随实例属性
     * @param showMsg  是否打印进度
     * @param params   连接参数
     * @return 任务结果和信息组成的数组
     */
    public String[] download(String fileUrl, Path goalPath, String rename, String suffix, FileMode fileMode, boolean showMsg, Map<String, Object> params) {
        String tmp = null;
        if (showMsg) {
            tmp = this.printMode;
            this.printMode = null;
        }
        String[] wait = this.add(fileUrl, goalPath, rename, suffix, fileMode, false, params).wait(showMsg);
        if (showMsg) this.printMode = tmp;
        return wait;
    }


    /**
     * 接收任务，有空线程则运行，没有则进入等待队列
     *
     * @param mission 任务对象
     */
    private void runOrWait(BaseTask mission) {
        Integer usableThread = this.getUsableThread();
        if (usableThread != null) {
            Runnable runnable = () -> run(usableThread, mission);
            Map<String, Object> map = this.threadMap.computeIfAbsent(usableThread, key -> new HashMap<>());
            map.put("thread", runnable);
            map.put("mission", null);
            threads.execute(runnable);
        } else {
            try {
                this.waitingList.put(mission);
            } catch (InterruptedException ignored) {
                System.out.println("插入队列失败");
            }
        }
    }

    /**
     * 线程函数
     *
     * @param id      线程id
     * @param mission 任务对象，Mission或Task
     */

    private void run(int id, BaseTask mission) {

        while (true) {
            if (mission == null) if (this.waitingList.isEmpty()) break;
            else try {
                    mission = waitingList.poll(500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    break;
                }
            threadMap.computeIfAbsent(id, key -> new HashMap<>()).put("mission", mission);
            this.download(mission, id);
            mission = null;

        }
        threadMap.put(id, null);
    }

    /**
     * 根据id值获取一个任务
     *
     * @param id 任务id
     * @return 任务对象
     */
    public Mission getMission(int id) {
        return this.missions.get(id);
    }

    /**
     * 根据id值获取一个任务
     *
     * @param id 任务id
     * @return 任务对象
     */
    public List<Mission> getFailedMissions(int id) {
        List<Mission> list = new ArrayList<>();
        this.missions.forEach((a, b) -> {
            if (b.getResult().equals("false")) list.add(b);
        });
        return list;
    }

    /**
     * 等待所有或指定任务完成
     *
     * @return 任务结果和信息组成的数组
     */
    public String[] waits() {
        return wait(null);
    }

    /**
     * 等待所有或指定任务完成
     *
     * @param mission 任务id，为null时等待所有任务结束
     * @return 任务结果和信息组成的数组
     */
    public String[] wait(Integer mission) {
        return wait(mission, false);
    }

    /**
     * 等待所有或指定任务完成
     *
     * @param mission 任务id，为null时等待所有任务结束
     * @param show    是否显示进度
     * @return 任务结果和信息组成的数组
     */
    public String[] wait(Integer mission, boolean show) {
        return wait(mission, show, null);
    }

    /**
     * 等待所有或指定任务完成
     *
     * @param mission 任务id，为null时等待所有任务结束
     * @param show    是否显示进度
     * @param timeout 超时时间，null或0为无限
     * @return 任务结果和信息组成的数组
     */

    public String[] wait(Integer mission, boolean show, Double timeout) {
        timeout = timeout == null ? 0 : timeout;
        if (mission != null) return this.getMission(mission).wait(show, timeout);
        else {
            if (show) {
                this.show(false);
            } else {
                long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
                while (this.isRunning() && (System.currentTimeMillis() < endTime || timeout == 0)) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return null;
        }
    }

    /**
     * 取消所有等待中或执行中的任务
     */
    public void cancel() {
        this.missions.values().forEach(Mission::cancel);
    }

    /**
     * 实时显示所有线程进度
     */
    public void show() {
        show(true);
    }

    /**
     * 实时显示所有线程进度
     *
     * @param asy 是否以异步方式显示
     */
    public void show(boolean asy) {
        show(asy, false);
    }

    /**
     * 实时显示所有线程进度
     *
     * @param asy  是否以异步方式显示
     * @param keep 任务列表为空时是否保持显示
     */
    public void show(boolean asy, boolean keep) {
        if (asy) {
            new Thread(() -> show(2.0, keep)).start();
        } else {
            this.show(0.1, keep);
        }
    }

    private void show(double wait, boolean keep) {
        this.stopPrinting = false;
        if (keep) new Thread(this::stopShow).start();
        long endTime = (long) (System.currentTimeMillis() + wait);
        while (!this.stopPrinting && (keep || isRunning() || System.currentTimeMillis() < endTime)) {
            System.out.println("\33[K");
            System.out.println("等待任务数:" + waitingList.size());
            this.threadMap.forEach((k, v) -> {
                BaseTask m = null;
                if (v != null) if (v.get("mission") instanceof BaseTask) m = (BaseTask) v.get("mission");
                String path;
                if (m != null) {
                    String[] items;
                    if (m instanceof Task) {
                        items = new String[]{String.valueOf(((Task) m).getMission().rate()), ((Task) m).mid()};
                    } else {
                        items = new String[]{String.valueOf(((Mission) m).rate()), m.id()};
                    }
                    path = "M" + items[1] + " " + items[0] + "% " + m;
                } else {
                    path = "空闲";
                }
                System.out.println("\033[K");
                System.out.println("线程" + k + ":" + path);


            });
            System.out.println("\33[" + this.roads + 1 + "A\r");
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println();
    }

    /**
     * 生成response对象
     *
     * @param url      目标url
     * @param session  用于连接的Session对象
     * @param header   内置的headers参数
     * @param method   请求方式
     * @param encoding 编码格式
     * @param params   连接参数
     * @return 第一位为Response或None，第二位为出错信息或'Success'
     */
    public ResponseConnect connect(String url, OkHttpClient session, CaseInsensitiveMap<String, Object> header, String method, String encoding, Map<String, Object> params) {
        if (params.containsKey("headers")) {
            Object o = params.get("headers");
            if (o instanceof Map) header.putAll((Map<? extends String, ?>) o);
            params.put("headers", header);
        } else {
            params.put("headers", header);
        }
        Response response = null;
        Exception e = null;
        for (int i = 0; i < this.retry + 1; i++) {
            try {
                if ("get".equalsIgnoreCase(method)) {
                    Request.Builder builder = new Request.Builder();
                    builder.get();
                    Object headerObj = params.get("header");
                    if (headerObj instanceof Map) for (Map.Entry<?, ?> entry : ((Map<?, ?>) headerObj).entrySet())
                        builder.addHeader(entry.getKey().toString(), entry.getValue().toString());
                    builder.setUrl$okhttp(HttpUrl.get(url));
                    try (Response responses = session.newCall(builder.build()).execute()) {
                        response = responses;
                    }
                } else if ("post".equalsIgnoreCase(method)) {
                    Request.Builder builder = new Request.Builder();
                    builder.get();
                    Object headerObj = params.get("header");
                    if (headerObj instanceof Map) for (Map.Entry<?, ?> entry : ((Map<?, ?>) headerObj).entrySet())
                        builder.addHeader(entry.getKey().toString(), entry.getValue().toString());
                    builder.setUrl$okhttp(HttpUrl.get(url));
                    Object o = params.get("json");
                    if (o == null) o = params.get("body");
                    if (o != null)
                        builder.setBody$okhttp(RequestBody.create(o.toString(), MediaType.get("application/json")));
                    try (Response responses = session.newCall(builder.build()).execute()) {
                        response = responses;
                    }
                }
                if (response != null) {
                    return new ResponseConnect(Utils.setCharset(response, encoding), "Success");
                }
            } catch (Exception es) {
                e = es;
            }
            if (response != null && (response.code() == 403 || response.code() == 404)) {
                break;
            }
            if (i < this.retry) {
                try {
                    Thread.sleep((long) (this.interval * 1000));
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }

        if (response == null) return new ResponseConnect(null, e == null ? "连接失败" : e.toString());
        if (response.code() != 200) return new ResponseConnect(response, "状态码：" + response.code());
        return new ResponseConnect(response, "返回成功");

    }


    /**
     * @return 获取是否可用
     */
    private Integer getUsableThread() {
        for (Map.Entry<Integer, Map<String, Object>> entry : this.threadMap.entrySet())
            if (entry.getValue() != null) return entry.getKey();
        return null;
    }


    /**
     * 设置停止打印的变量
     */
    private void stopShow() {
        this.stopPrinting = false;
    }

    /**
     * 当任务完成时执行的操作
     *
     * @param mission 完结的任务
     */
    public void _whenMissionDone(Mission mission) {
        this.runningCount--;
        if (Objects.equals(this.printMode, "all") || (Objects.equals(this.printMode, "failed") && Objects.equals(mission.getResult(), "false")))
            System.out.println("[" + Mission.RESULT_TEXTS.get(mission.getResult()) + "]" + mission.data().getUrl() + " " + mission.getInfo());
        if (Objects.equals(this.logMode, "all") || (Objects.equals(this.logMode, "failed") && Objects.equals(mission.getResult(), "false"))) {
            Object[] data = {"下载结果", mission.data().getUrl(), mission.data().getRename(), mission.data().getParams()};
            this.logger.addData(data);
        }

    }


    /**
     * 执行下载的方法，根据任务下载文件
     *
     * @param missionOrTask 下载任务对象
     * @param threadId      线程号
     */
    private void download(BaseTask missionOrTask, int threadId) {
        if (missionOrTask.isDone()) {
            return;
        }

        if (missionOrTask.getState().equals("cancel")) {
            missionOrTask.setState("done");
            return;
        }

        String fileUrl = missionOrTask.data().getUrl();

        if (missionOrTask instanceof Task) {
            Task task = (Task) missionOrTask;
            Map<String, Object> params = new HashMap<>(missionOrTask.data().getParams());
            Object o = params.get("headers");
            if (o instanceof Map) {
                ((Map) o).put("Range", "bytes=" + task.getRange().get(0) + "-" + task.getRange().get(1));
            }
            ResponseConnect r = connect(fileUrl, task.getMission().getSession(), task.getMission().getHeaders(), task.getMission().getMethod(), task.getMission().getEncoding(), params);

            if (r.response != null) {
                doDownload(r.response, task, false);
            } else {
                task._setDone("false", r.info);
            }
            return;
        }
        // ===================开始处理mission====================

        Mission mission = (Mission) missionOrTask;
        mission.setInfo("下载中");
        mission.setState("running");
        Map<String, Object> kwargs = missionOrTask.data().getParams();
        if (Objects.equals(printMode, "all")) {
            System.out.println("开始下载：" + mission.data().getUrl());
        }
        if (Objects.equals(logMode, "all")) {
            logger.addData("开始下载", mission.data().getUrl());
        }

        String rename = mission.data().getRename();
        String suffix = mission.data().getSuffix();
        String goalPath = mission.data().getGoalPath();
        FileMode fileExists = mission.data().getFileExists();
        boolean split = mission.data().isSplit();

        Path goalPathObj = Paths.get(goalPath);
        //按windows规则去除路径中的非法字符
        goalPath = goalPathObj.getRoot() + goalPathObj.subpath(1, goalPathObj.getNameCount()).toString().replaceAll("[*:|<>?\"]", "").trim();

        goalPathObj = Paths.get(goalPath).toAbsolutePath();
        goalPathObj.toFile().mkdirs();
        goalPath = goalPathObj.toString();

        if (fileExists.equals(FileMode.SKIP) && rename != null && !rename.isEmpty()) {
            Path tmp = goalPathObj.resolve(rename);
            if (Files.exists(tmp) && Files.isRegularFile(tmp)) {
                mission.setFileName(rename);
                mission._setPath(tmp.toString());
                mission._setDone("skipped", mission.path());
                return;
            }
        }

        ResponseConnect r = connect(fileUrl, mission.getSession(), mission.getHeaders(), mission.getMethod(), mission.getEncoding(), kwargs);

        if (mission.isDone()) {
            return;
        }

        if (r.response == null) {
            mission._breakMission("false", r.info);
            return;
        }
        //-------------------获取文件信息-------------------
        Map<String, Object> fileInfo = Utils.getFileInfo(r.response, goalPath, rename, suffix, fileExists, mission.getEncoding(), this.lock);
        long fileSize = Long.parseLong(fileInfo.get("size").toString());
        Path fullPath = Paths.get((String) fileInfo.get("path"));
        mission._setPath(fullPath);
        mission.setFileName(fullPath.getFileName().toString());
        mission.setSize(fileSize);

        if ((boolean) fileInfo.get("skip")) {
            mission._setDone("skipped", mission.path());
            return;
        }

        fullPath = Paths.get((String) fileInfo.get("path"));
        if (fileExists.equals("add") && Files.exists(fullPath)) {
            try {
                mission.data().setOffset(Files.size(fullPath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        boolean first = false;
        if (split && fileSize > this.blockSize && Objects.equals(r.response.headers().get("Accept-Ranges"), "bytes")) {
            first = true;
            List<List<Object>> chunks = new ArrayList<>();
            for (long s = 0; s < fileSize; s += blockSize) {
                double e = Math.min(s + blockSize, fileSize) - 1;
                List<Object> objects = new ArrayList<>();
                objects.add(s);
                objects.add(e);
                chunks.add(objects);
            }
            chunks.get(chunks.size() - 1).set(1, -1);
            Task task1 = new Task(mission, chunks.get(0), "1/" + chunks.size(), new BigDecimal(chunks.get(0).get(1).toString()).subtract(new BigDecimal(chunks.get(0).get(0).toString())).longValue());
            mission.setTasksCount(chunks.size());
            mission.setTasks(Collections.singletonList(task1));

            for (int ind = 2; ind <= chunks.size(); ind++) {
                List<Object> chunk = chunks.get(ind - 1);
                long s = fileSize - Long.parseLong(chunk.get(0).toString());
                Task task = new Task(mission, chunk, ind + "/" + chunks.size(), s);
                mission.getTasks().add(task);
                runOrWait(task);
            }
        } else {
            Task task1 = new Task(mission, null, "1/1", fileSize);
            mission.getTasks().add(task1);
        }

        threadMap.get(threadId).put("mission", mission.getTasks().get(0));
        doDownload(r.response, mission.getTasks().get(0), first);
    }


    @AllArgsConstructor
    public static class ResponseConnect {
        private Response response;
        private String info;
    }


    /**
     * 执行下载任务
     *
     * @param response 响应对象
     * @param task     任务对象
     * @param first    是否第一个分块
     */
    public static void doDownload(Response response, Task task, boolean first) {
        if (task.isDone() || task.getMission().isDone()) {
            return;
        }

        task.setStates(null, "下载中", "running");
        int blockSize = 131072;  // 128k
        String result = null;
        String info = null;

        try (ResponseBody responseBody = response.body()) {
            if (first) {  // 第一个分块
                if (Long.parseLong(task.getRange().get(1).toString()) <= blockSize || Long.parseLong(task.getRange().get(1).toString()) % blockSize != 0) {
                    byte[] content = new byte[0];
                    if (responseBody != null) {
                        content = responseBody.bytes();
                    }
                    task.addData(content, task.getMission().data().getOffset());
                    if ("cancel".equals(task.getState()) || "done".equals(task.getState())) {
                        result = "canceled";
                        task.clearCache();
                    }
                } else {
                    long blocks = Long.parseLong(task.getRange().get(1).toString()) / blockSize;
                    for (int b = 0; b < blocks; b++) {
                        byte[] content = new byte[0];
                        if (responseBody != null) {
                            content = responseBody.bytes();
                        }
                        task.addData(content, (long) b * blockSize + task.getMission().data().getOffset());
                        if ("cancel".equals(task.getState()) || "done".equals(task.getState())) {
                            result = "canceled";
                            task.clearCache();
                            break;
                        }
                    }

                    if ("cancel".equals(task.getState()) || "done".equals(task.getState())) {
                        result = "canceled";
                        task.clearCache();
                    } else {
                        byte[] content = new byte[0];
                        if (responseBody != null) {
                            content = responseBody.bytes();
                        }
                        task.addData(content, blocks * blockSize + task.getMission().data().getOffset());
                    }
                }
            } else {
                if (task.getRange() == null) {  // 不分块
                    byte[] chunk = new byte[0];
                    if (responseBody != null) {
                        chunk = responseBody.bytes();
                    }
                    for (byte b : chunk) {
                        if ("cancel".equals(task.getState()) || "done".equals(task.getState())) {
                            result = "canceled";
                            task.clearCache();
                            break;
                        }
                        task.addData(new byte[]{b}, null);
                    }
                } else if (task.getRange().get(1) == null) {  // 结尾的数据块
                    long begin = Long.parseLong(task.getRange().get(0).toString());
                    if (responseBody != null) {
                        for (byte b : responseBody.bytes()) {
                            if ("cancel".equals(task.getState()) || "done".equals(task.getState())) {
                                result = "canceled";
                                task.clearCache();
                                break;
                            }
                            task.addData(new byte[]{b}, begin + task.getMission().data().getOffset());
                            begin++;
                        }
                    }
                } else {  // 有始末数字的数据块
                    long begin = (long) task.getRange().get(0);
                    long end = (long) task.getRange().get(1);
                    int num = (int) ((end - begin) / blockSize);
                    int ind = 1;
                    if (responseBody != null) {
                        for (byte b : responseBody.bytes()) {
                            if ("cancel".equals(task.getState()) || "done".equals(task.getState())) {
                                result = "canceled";
                                task.clearCache();
                                break;
                            }
                            task.addData(new byte[]{b}, begin + task.getMission().data().getOffset());
                            if (ind <= num) {
                                begin += blockSize;
                            }
                            ind++;
                        }
                    }
                }
            }
        } catch (IOException e) {
            result = "failed";
            info = "下载失败。" + response.code() + " " + e.getMessage();
        } finally {
            response.close();
        }

        if (result == null) {
            result = "success";
            info = String.valueOf(task.path());
        }

        task._setDone(result, info);
    }
}
