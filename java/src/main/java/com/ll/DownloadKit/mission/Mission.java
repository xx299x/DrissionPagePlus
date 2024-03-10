package com.ll.DownloadKit.mission;

import com.alibaba.fastjson.JSON;
import com.ll.DataRecorder.ByteRecorder;
import com.ll.DownloadKit.DownloadKit;
import com.ll.DownloadKit.FileMode;
import com.ll.DownloadKit.Utils;
import kotlin.Pair;
import lombok.Getter;
import lombok.Setter;
import okhttp3.*;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 任务类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Mission extends BaseTask {

    @Setter
    protected String fileName;
    private final MissionData data;
    private String path;
    private ByteRecorder recorder;
    @Setter
    private Long size;
    private int doneTasksCount = 0;
    @Setter
    private int tasksCount = 1;
    @Setter
    @Getter
    private List<Task> tasks;
    /**
     * 所属DownloadKit对象
     */
    private final DownloadKit downloadKit;
    @Getter
    private OkHttpClient session;
    @Getter
    private CaseInsensitiveMap<String, Object> headers;
    @Getter
    private String method;
    @Getter

    private String encoding;

    /**
     * @param id          任务id
     * @param downloadKit 所属DownloadKit对象
     * @param fileUrl     文件网址
     * @param goalPath    保存文件夹路径
     * @param rename      重命名
     * @param suffix      重命名后缀名
     * @param fileExists  存在同名文件处理方式
     * @param split       是否分块下载
     * @param encoding    编码格式
     * @param params      连接参数
     */
    public Mission(String id, DownloadKit downloadKit, String fileUrl, String goalPath, String rename, String suffix, FileMode fileExists, boolean split, String encoding, Map<String, Object> params) {
        super(id);
        this.downloadKit = downloadKit;
        this.size = null;
        this.tasks = new ArrayList<>();
        this.encoding = encoding;
        this.setSession();
        params = this.handleParams(fileUrl, params);
        this.data = new MissionData(fileUrl, goalPath, rename, suffix, fileExists, split, params, 0L);
        this.method = this.data.params.get("data") != null || this.data.params.get("json") != null ? "post" : "get";
    }

    /**
     * @param id          任务id
     * @param downloadKit 所属DownloadKit对象
     * @param fileUrl     文件网址
     * @param goalPath    保存文件夹路径
     * @param rename      重命名
     * @param suffix      重命名后缀名
     * @param fileExists  存在同名文件处理方式
     * @param split       是否分块下载
     * @param encoding    编码格式
     * @param params      连接参数
     */
    public Mission(String id, DownloadKit downloadKit, String fileUrl, Path goalPath, String rename, String suffix, FileMode fileExists, boolean split, String encoding, Map<String, Object> params) {
        super(id);
        this.downloadKit = downloadKit;
        this.size = null;
        this.tasks = new ArrayList<>();
        this.encoding = encoding;
        this.setSession();
        params = this.handleParams(fileUrl, params);
        this.data = new MissionData(fileUrl, goalPath, rename, suffix, fileExists, split, params, 0L);
        this.method = this.data.params.get("data") != null || this.data.params.get("json") != null ? "post" : "get";
    }

    @Override
    public String toString() {
        return "<Mission " + this.id() + " " + this.info + " " + this.fileName + ">";
    }

    /**
     * @return 返回任务数据
     */
    @Override
    public MissionData data() {
        return this.data;
    }

    /**
     * @return 返回文件保存路径
     */
    public String path() {
        return this.path;
    }

    /**
     * @return 返回记录器对象
     */
    public ByteRecorder recorder() {
        if (this.recorder == null) {
            this.recorder = new ByteRecorder("", 100);
            this.recorder.showMsg = false;
        }
        return this.recorder;
    }

    /**
     * @return 返回下载进度百分比
     */
    public Float rate() {
        if (this.size == null) return null;
        int c = 0;
        for (Task task : this.tasks) c += task.downloadedSize;
        return new BigDecimal(c * 100).divide(new BigDecimal(this.size), 2, RoundingMode.FLOOR).floatValue();
    }

    /**
     * 取消该任务，停止未下载完的task
     */
    public void cancel() {
        this._breakMission("canceled", "已取消");
    }

    /**
     * @return 删除下载的文件
     */
    public boolean delFile() {
        if (this.path != null && Paths.get(this.path).toFile().exists()) {
            try {
                return Paths.get(this.path).toFile().delete();
            } catch (Exception ignored) {

            }
        }
        return false;
    }

    /**
     * 等待当前任务完成
     *
     * @return 任务结果和信息组成的数组
     */
    public String[] waits() {
        return wait(true);
    }

    /**
     * 等待当前任务完成
     *
     * @param show 是否显示下载进度
     * @return 任务结果和信息组成的数组
     */
    public String[] wait(boolean show) {
        return wait(show, 0);
    }

    /**
     * 等待当前任务完成
     *
     * @param timeout 超时时间
     * @return 任务结果和信息组成的数组
     */
    public String[] wait(double timeout) {
        return wait(true, 0);
    }

    /**
     * 等待当前任务完成
     *
     * @param show    是否显示下载进度
     * @param timeout 超时时间
     * @return 任务结果和信息组成的数组
     */
    public String[] wait(boolean show, double timeout) {
        if (show) {
            System.out.println("url:" + this.data().url);
            long t2 = System.currentTimeMillis();
            while (this.fileName == null && System.currentTimeMillis() - t2 < 4000) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            System.out.println("file:" + this.fileName);
            System.out.println("filePath:" + this.fileName);
            if (this.size == null) System.out.println("Unknown size");
        }
        long t1 = System.currentTimeMillis();
        while (!this.isDone() && (System.currentTimeMillis() - t1 < timeout * 1000 || timeout == 0)) {
            if (show && this.size != null) {
                try {
                    long rate = Files.size(Paths.get(this.path()));
                    System.out.println("\r" + new BigDecimal(rate * 100).divide(new BigDecimal(this.size), 2, RoundingMode.FLOOR) + "%");
                } catch (IOException ignored) {

                }
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (show) {
            String result = this.result.trim().toLowerCase();
            switch (result) {
                case "false":
                    System.out.println("下载失败 " + this.info);
                    break;
                case "success":
                    System.out.println("100%");
                    System.out.println("下载成功 " + this.info);
                    break;
                case "skipped":
                    System.out.println("已跳过 " + this.info);
                    break;
            }
        }
        return new String[]{super.result, super.info};
    }

    /**
     * 复制Session对象，并设置cookies
     */
    private void setSession() {
        OkHttpClient.Builder builder = this.downloadKit.session().newBuilder();
        CaseInsensitiveMap<String, Object> headers = new CaseInsensitiveMap<>();
        /*
         * 使用拦截器去获取请求头参数
         */
        builder.addInterceptor(new Interceptor() {
            @NotNull
            @Override
            public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
                Request request = chain.request();
                Headers headers1 = request.headers();
                for (Pair<? extends String, ? extends String> pair : headers1)
                    headers.put(String.valueOf(pair), headers1.get(String.valueOf(pair)));
                return chain.proceed(request.newBuilder().headers(Headers.of()).build());
            }
        });
        if (this.downloadKit.getPage() != null) {
            Utils.setSessionCookies(builder, this.downloadKit.getPage().cookies());
            try {
                Field header;
                header = this.downloadKit.getPage().getClass().getField("headers");
                header.setAccessible(true);
                Object o = header.get(this.downloadKit.getPage());
                if (o instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) o;
                    // 检查泛型参数是否为<String, Object>
                    if (map.keySet().stream().allMatch(key -> key instanceof String) && map.values().stream().allMatch(Objects::nonNull))
                        map.forEach((a, b) -> headers.put((String) a, b));
                }
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
            headers.put("User-Agent", this.downloadKit.getPage().userAgent());
        }
        this.session = builder.build();
        this.headers = headers;

    }


    /**
     * 处理接收到的参数
     *
     * @param url    要访问的url
     * @param params 传入的参数map
     * @return 处理后的参数map
     */
    private Map<String, Object> handleParams(String url, Map<String, Object> params) {
        if (!params.containsKey("timeout")) params.put("timeout", this.downloadKit.timeout());
        Map<String, Object> headers = params.containsKey("headers") ? new CaseInsensitiveMap<>(JSON.parseObject(params.get("headers").toString())) : new CaseInsensitiveMap<>();
        URI uri = URI.create(url);
        String hostName = uri.getHost();
        String scheme = uri.getScheme();
        if (!(headers.containsKey("Referer") || this.headers.containsKey("Referer")))
            headers.put("Referer", this.downloadKit.getPage() != null && this.downloadKit.getPage().url() != null ? this.downloadKit.getPage().url() : scheme + "://" + hostName);
        if (!(headers.containsKey("Host") || this.headers.containsKey("Host"))) headers.put("Host", hostName);
        params.put("headers", headers);
        return params;

    }

    /**
     * 设置文件保存路径
     */
    public void _setPath(Object path) {
        Path path1;
        if (path instanceof Path) path1 = (Path) path;
        else if (path instanceof String) {
            path1 = Paths.get((String) path);
        } else throw new IllegalArgumentException("path只能是String或者Path");
        this.fileName = path1.toAbsolutePath().getFileName().toString();
        this.path = path1.toAbsolutePath().toString();
        this.recorder.set().path(path1);
    }

    /**
     * 设置一个任务为done状态
     *
     * @param result 结果：'success'、'skipped'、'canceled'、False、None
     * @param info   任务信息
     */
    public void _setDone(String result, String info) {
        switch (result) {
            case "skipped":
                this.setStates(result, info, Mission.DONE);
                break;
            case "canceled":
            case "false":
                this.recorder.clear();
                this.setStates(result, info, Mission.DONE);
                break;
            case "success":
                this.recorder.record();
                try {
                    if (this.size != null && Files.size(Paths.get(this.path)) < this.size) {
                        this.delFile();
                        this.setStates("false", "下载失败", Mission.DONE);
                    } else {
                        this.setStates("success", info, Mission.DONE);

                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }
        this.downloadKit._whenMissionDone(this);
    }

    /**
     * 当一个task完成时调用
     *
     * @param isSuccess 该task是否成功
     * @param info      该task传入的信息
     */
    protected void aTaskDone(boolean isSuccess, String info) {
        if (this.isDone()) return;
        if (!isSuccess) this._breakMission("false", info);
        if (++this.doneTasksCount == this.tasksCount) this._setDone("success", info);
    }

    /**
     * 中止该任务，停止未下载完的task
     *
     * @param result 结果：'success'、'skipped'、'canceled'、false、None
     * @param info   任务信息
     */
    public void _breakMission(String result, String info) {
        if (this.isDone()) return;
        this.tasks.stream().filter(task -> !task.isDone()).forEach(task -> task.setStates(result, info, "cancel"));
        this.tasks.stream().filter(task -> !task.isDone()).forEach(task -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        this._setDone(result, info);
        this.delFile();
    }
}
