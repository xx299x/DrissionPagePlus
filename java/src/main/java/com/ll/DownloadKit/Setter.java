package com.ll.DownloadKit;

import com.ll.DrissonPage.base.BasePage;
import com.ll.DrissonPage.config.SessionOptions;
import com.ll.DrissonPage.page.SessionPage;
import com.ll.DrissonPage.page.WebPage;
import com.ll.DrissonPage.units.HttpClient;
import lombok.AllArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.Header;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class Setter {
    protected final DownloadKit downloadKit;


    /**
     * -
     * 设置Session对象
     *
     * @param driver Session对象或DrissionPage的页面对象
     */

    public void driver(Object driver) {
        if (driver == null) {
            this.downloadKit.session = new OkHttpClient();
            return;
        }
        if (driver instanceof OkHttpClient) {
            this.downloadKit.session = (OkHttpClient) driver;
            return;
        }
        if (driver instanceof SessionOptions) {
            HttpClient httpClient = ((SessionOptions) driver).makeSession();
            Collection<? extends Header> headers = httpClient.getHeaders();
            this.downloadKit.session = this.downloadKit.session().newBuilder().addInterceptor(new Interceptor() {
                @NotNull
                @Override
                public Response intercept(@NotNull Interceptor.Chain chain) throws IOException {
                    Request request = chain.request();
                    Request.Builder builder1 = request.newBuilder();
                    if (!headers.isEmpty()) headers.forEach((a) -> builder1.addHeader(a.getName(), a.getValue()));
                    return chain.proceed(request);
                }
            }).build();
        } else if (driver instanceof BasePage) {
            if (driver instanceof SessionPage) this.downloadKit.session = ((SessionPage) driver).session();
            else if (driver instanceof WebPage) this.downloadKit.session = ((WebPage) driver).session();
            else this.downloadKit.session = new OkHttpClient();
            this.downloadKit.page = ((BasePage<?>) driver);
        } else {
            throw new IllegalArgumentException("类型只能为OkHttpClient SessionOptions BasePage");
        }
    }

    /**
     * 设置可同时运行的线程数
     *
     * @param num 线程数量
     */
    public void roads(int num) {
        if (this.downloadKit.isRunning()) {
            System.out.println("有任务未完成时不能改变roads。");
            return;
        }
        if (num != this.downloadKit.roads()) {
            this.downloadKit.roads = num;
            this.downloadKit.getThreads().setMaximumPoolSize(num);
            this.downloadKit.threadMap = new HashMap<>(num);
        }
    }

    /**
     * 设置连接失败时重试次数
     *
     * @param times 重试次数
     */
    public void retry(int times) {
        if (times < 0) throw new IllegalArgumentException("times参数过于小");
        this.downloadKit.retry = times;
    }

    /**
     * 设置连接失败时重试间隔
     *
     * @param seconds 连接失败时重试间隔（秒）
     */

    public void interval(double seconds) {
        if (seconds < 0) throw new IllegalArgumentException("seconds参数过于小");
        this.downloadKit.interval = seconds;
    }

    /**
     * 设置连接超时时间
     *
     * @param timeout 超时时间（秒）
     */
    public void timeout(double timeout) {
        if (timeout < 0) throw new IllegalArgumentException("timeout参数过于小");
        this.downloadKit.timeout = timeout;
    }

    /**
     * 设置文件保存路径
     *
     * @param path 文件路径，可以是str或Path
     */
    public void goalPath(Path path) {
        this.downloadKit.goalPath = path.toAbsolutePath().toString();
    }

    /**
     * 设置文件保存路径
     *
     * @param path 文件路径，可以是str或Path
     */
    public void goalPath(String path) {
        this.downloadKit.goalPath = path;
    }

    /**
     * 设置大文件是否分块下载
     *
     * @param onOff 代表开关
     */
    public void split(boolean onOff) {
        this.downloadKit.split = onOff;
    }

    /**
     * 设置分块大小
     *
     * @param size 单位为字节，可用'K'、'M'、'G'为单位，如'50M'
     */
    public void blockSize(String size) {
        this.downloadKit.blockSize = Utils.blockSizeSetter(size);
    }

    /**
     * 设置分块大小
     *
     * @param size 单位为字节，可用'K'、'M'、'G'为单位，如'50M'
     */
    public void blockSize(int size) {
        this.downloadKit.blockSize = Utils.blockSizeSetter(size);
    }

    /**
     * 设置代理地址及端口，例：'127.0.0.1:1080'  创建方式 new Proxy(Proxy.Type.HTTP,new InetSocketAddress("127.0.0.1",80))
     */
    public void proxy(Proxy proxy) {
        OkHttpClient.Builder builder = this.downloadKit.session.newBuilder();
        builder.setProxy$okhttp(proxy);
        this.downloadKit.session = builder.build();
    }

    /**
     * 设置编码
     *
     * @param encoding 编码名称
     */
    public void encoding(Charset encoding) {
        this.downloadKit.encoding = encoding.name();
    }

    /**
     * 设置编码
     *
     * @param encoding 编码名称  使用Charset.forName去校验
     */
    public void encoding(String encoding) {
        this.downloadKit.encoding = Charset.forName(encoding).name();
    }

    /**
     * 设置编码 为空
     */
    public void encoding() {
        this.downloadKit.encoding = null;
    }
}
