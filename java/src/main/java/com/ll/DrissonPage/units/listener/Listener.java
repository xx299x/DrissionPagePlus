package com.ll.DrissonPage.units.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.base.Driver;
import com.ll.DrissonPage.base.MyRunnable;
import com.ll.DrissonPage.error.extend.WaitTimeoutError;
import com.ll.DrissonPage.functions.Settings;
import com.ll.DrissonPage.page.ChromiumBase;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 监听器基类
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Listener {
    protected ChromiumBase page;
    private String address;
    private String targetId;
    private Collection<String> targets;
    private ListenerMethod method;
    private Collection<String> resType;
    private Queue<DataPacket> caught;
    private boolean isRegex;
    private Driver driver;
    private Map<String, DataPacket> requestIds;
    private Map<String, Map<String, Object>> extraInfoIds;
    private boolean listening;
    private int runningRequests;
    private int runningTargets;

    public Listener(ChromiumBase page) {
        this.page = page;
        this.address = page.getAddress();
        this.targetId = page.tabId();
        this.driver = null;
        this.runningRequests = 0;
        this.runningTargets = 0;
        this.caught = new ArrayDeque<>();
        this.requestIds = null;
        this.extraInfoIds = null;
        this.listening = false;
        this.targets = null;
        this.isRegex = false;
        this.method = ListenerMethod.all;
        this.resType = null;
    }

    /**
     * @return 返回监听目标
     */

    public Collection<String> targets() {
        return this.targets;
    }

    /**
     * 指定要等待的数据包
     */
    public void setTargets() {
        setTargets(new ArrayList<>());
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     */
    public void setTargets(String targets) {
        setTargets(Collections.singletonList(targets));
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     */
    public void setTargets(String[] targets) {
        setTargets(Arrays.asList(targets));
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     */
    public void setTargets(Collection<String> targets) {
        setTargets(targets, ListenerMethod.ALL);
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     */
    public void setTargets(String targets, ListenerMethod method) {
        setTargets(targets, method, false);
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     */
    public void setTargets(String[] targets, ListenerMethod method) {
        setTargets(targets, method, false);
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     */
    public void setTargets(Collection<String> targets, ListenerMethod method) {
        setTargets(targets, method, false);
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     * @param isRegex 设置的target是否正则表达式
     */
    public void setTargets(String targets, ListenerMethod method, boolean isRegex) {
        setTargets(targets, method, isRegex, new ArrayList<>());
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     * @param isRegex 设置的target是否正则表达式
     */
    public void setTargets(String[] targets, ListenerMethod method, boolean isRegex) {
        setTargets(targets, method, isRegex, new ArrayList<>());
    }


    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     * @param isRegex 设置的target是否正则表达式
     */
    public void setTargets(Collection<String> targets, ListenerMethod method, boolean isRegex) {
        setTargets(targets, method, isRegex, new ArrayList<>());
    }


    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     * @param isRegex 设置的target是否正则表达式
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void setTargets(String targets, ListenerMethod method, boolean isRegex, String resType) {
        setTargets(Collections.singletonList(targets), method, isRegex, Collections.singletonList(resType));
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     * @param isRegex 设置的target是否正则表达式
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void setTargets(String targets, ListenerMethod method, boolean isRegex, String[] resType) {
        setTargets(Collections.singletonList(targets), method, isRegex, resType);
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     * @param isRegex 设置的target是否正则表达式
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void setTargets(String targets, ListenerMethod method, boolean isRegex, Collection<String> resType) {
        setTargets(Collections.singletonList(targets), method, isRegex, resType);
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     * @param isRegex 设置的target是否正则表达式
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void setTargets(String[] targets, ListenerMethod method, boolean isRegex, Collection<String> resType) {
        setTargets(Arrays.asList(targets), method, isRegex, resType);
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     * @param isRegex 设置的target是否正则表达式
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void setTargets(Collection<String> targets, ListenerMethod method, boolean isRegex, String resType) {
        setTargets(targets, method, isRegex, Collections.singletonList(resType));
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     * @param isRegex 设置的target是否正则表达式
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void setTargets(String[] targets, ListenerMethod method, boolean isRegex, String[] resType) {
        setTargets(Arrays.asList(targets), method, isRegex, Arrays.asList(resType));
    }

    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     * @param isRegex 设置的target是否正则表达式
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void setTargets(Collection<String> targets, ListenerMethod method, boolean isRegex, String[] resType) {
        setTargets(targets, method, isRegex, Arrays.asList(resType));
    }


    /**
     * 指定要等待的数据包
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个
     * @param isRegex 设置的target是否正则表达式
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void setTargets(Collection<String> targets, ListenerMethod method, boolean isRegex, Collection<String> resType) {
        if (targets != null) this.targets = targets;
        this.isRegex = isRegex;
        if (method != null) this.method = method;
        if (resType != null) this.resType = resType;
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     */
    public void start() {
        start(null, null, null, null, null);
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     */
    public void start(String targets) {
        start(Collections.singletonList(targets));
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     */
    public void start(String[] targets) {
        start(Arrays.asList(targets));
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     */
    public void start(Collection<String> targets) {
        start(targets, null);
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     */
    public void start(String targets, ListenerMethod method) {
        start(Collections.singletonList(targets), method);
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     */
    public void start(String[] targets, ListenerMethod method) {
        start(Arrays.asList(targets), method);
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     */
    public void start(Collection<String> targets, ListenerMethod method) {
        start(targets, method, null);
    }


    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     * @param isRegex 设置的target是否正则表达式，为null时保持原来设置
     */
    public void start(String targets, ListenerMethod method, Boolean isRegex) {
        start(Collections.singletonList(targets), method, isRegex);
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     * @param isRegex 设置的target是否正则表达式，为null时保持原来设置
     */
    public void start(String[] targets, ListenerMethod method, Boolean isRegex) {
        start(Arrays.asList(targets), method, isRegex);
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     * @param isRegex 设置的target是否正则表达式，为null时保持原来设置
     */
    public void start(Collection<String> targets, ListenerMethod method, Boolean isRegex) {
        start(targets, method, isRegex, "null");
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     * @param isRegex 设置的target是否正则表达式，为null时保持原来设置
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，为null时保持原来设置，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void start(String targets, ListenerMethod method, Boolean isRegex, Collection<String> resType) {
        start(Collections.singletonList(targets), method, isRegex, resType, null);
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     * @param isRegex 设置的target是否正则表达式，为null时保持原来设置
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，为null时保持原来设置，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void start(String targets, ListenerMethod method, Boolean isRegex, String resType) {
        start(Collections.singletonList(targets), method, isRegex, resType);
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     * @param isRegex 设置的target是否正则表达式，为null时保持原来设置
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，为null时保持原来设置，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void start(String[] targets, ListenerMethod method, Boolean isRegex, String[] resType) {
        start(Arrays.asList(targets), method, isRegex, resType);
    }


    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     * @param isRegex 设置的target是否正则表达式，为null时保持原来设置
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，为null时保持原来设置，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void start(Collection<String> targets, ListenerMethod method, Boolean isRegex, String resType) {
        start(targets, method, isRegex, resType.equals("null") ? null : Collections.singletonList(resType), null);
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     * @param isRegex 设置的target是否正则表达式，为null时保持原来设置
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，为null时保持原来设置，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void start(String[] targets, ListenerMethod method, Boolean isRegex, Collection<String> resType) {
        start(Arrays.asList(targets), method, isRegex, resType, null);
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     * @param isRegex 设置的target是否正则表达式，为null时保持原来设置
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，为null时保持原来设置，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void start(Collection<String> targets, ListenerMethod method, Boolean isRegex, String[] resType) {
        start(targets, method, isRegex, Arrays.asList(resType), null);
    }

    /**
     * 拦截目标请求，每次拦截前清空结果
     *
     * @param targets 要匹配的数据包url特征，可用list等传入多个，为空集合时获取所有
     * @param method  设置监听的请求类型，可指定多个，默认('GET', 'POST')，为True时监听全部，为null时保持原来设置
     * @param isRegex 设置的target是否正则表达式，为null时保持原来设置
     * @param resType 设置监听的资源类型，可指定多个，为空集合时监听全部，为null时保持原来设置，可指定的值有：
     *                Document, Stylesheet, Image, Media, Font, Script, TextTrack, XHR, Fetch, Prefetch, EventSource, WebSocket,
     *                Manifest, SignedExchange, Ping, CSPViolationReport, Preflight, Other
     */
    public void start(Collection<String> targets, ListenerMethod method, Boolean isRegex, Collection<String> resType, Object ignored) {
        if (targets != null) {
            if (isRegex == null) isRegex = false;
        }
        if (targets != null || isRegex != null || method != null || resType != null) {
            this.setTargets(targets, method, Boolean.TRUE.equals(isRegex), resType);
        }
        this.clear();
        if (this.listening) return;
        this.driver = new Driver(this.targetId, "page", this.address);
        this.driver.run("Network.enable");
        this.setCallback();
        this.listening = true;
    }

    /**
     * 等待符合要求的数据包到达指定数量
     */
    public List<DataPacket> waits() {
        return waits(1);
    }

    /**
     * 等待符合要求的数据包到达指定数量
     *
     * @param count 需要捕捉的数据包数量
     */
    public List<DataPacket> waits(int count) {
        return waits(count, null);
    }

    /**
     * 等待符合要求的数据包到达指定数量
     *
     * @param count   需要捕捉的数据包数量
     * @param timeout 超时时间，为null无限等待
     */
    public List<DataPacket> waits(int count, Double timeout) {
        return waits(count, timeout, true);
    }

    /**
     * 等待符合要求的数据包到达指定数量
     *
     * @param count    需要捕捉的数据包数量
     * @param timeout  超时时间，为null无限等待
     * @param fitCount 是否必须满足总数要求，发生超时，为True返回False，为False返回已捕捉到的数据包
     * @return count为1时返回数据包对象，大于1时返回列表，超时且fitCount为True时返回False
     */
    public List<DataPacket> waits(int count, Double timeout, boolean fitCount) {
        return waits(count, timeout, fitCount, null);
    }

    /**
     * 等待符合要求的数据包到达指定数量
     *
     * @param count    需要捕捉的数据包数量
     * @param timeout  超时时间，为null无限等待
     * @param fitCount 是否必须满足总数要求，发生超时，为True返回False，为False返回已捕捉到的数据包
     * @param raiseErr 超时时是否抛出错误，为null时根据Settings设置
     * @return count为1时返回数据包对象，大于1时返回列表，超时且fitCount为True时返回False
     */
    public List<DataPacket> waits(int count, Double timeout, boolean fitCount, Boolean raiseErr) {
        boolean fail;
        if (!this.listening) throw new RuntimeException("监听未启动或已暂停.");
        if (timeout == null) {
            while (this.caught.size() < count) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            fail = false;
        } else {
            long end = (long) (System.currentTimeMillis() + timeout * 1000);
            while (true) {
                if (System.currentTimeMillis() > end) {
                    fail = true;
                    break;
                }
                if (this.caught.size() >= count) {
                    fail = false;
                    break;
                }
            }
        }
        if (fail) {
            if (fitCount || (!this.caught.isEmpty())) {
                if (Boolean.TRUE.equals(raiseErr) || Settings.raiseWhenWaitFailed)
                    throw new WaitTimeoutError("等待数据包失败（等待" + timeout + "秒).");
                else return null;
            } else {
                ArrayList<DataPacket> objects = new ArrayList<>(this.caught);
                this.caught.clear();
                return objects;
            }
        }
        if (count == 1) {
            return Collections.singletonList(this.caught.poll());
        } else {
            return IntStream.range(0, count).mapToObj(i -> this.caught.poll()).collect(Collectors.toCollection(ArrayList::new));
        }

    }

    /**
     * 用于单步操作，可实现每收到若干个数据包执行一步操作（如翻页）
     */

    public Iterable<List<Object>> steps() {
        return steps(null);
    }

    /**
     * 用于单步操作，可实现每收到若干个数据包执行一步操作（如翻页）
     *
     * @param count 需捕获的数据包总数，为None表示无限
     */

    public Iterable<List<Object>> steps(Integer count) {
        return steps(count, null);
    }

    /**
     * 用于单步操作，可实现每收到若干个数据包执行一步操作（如翻页）
     *
     * @param count   需捕获的数据包总数，为None表示无限
     * @param timeout 每个数据包等待时间，为None表示无限
     */

    public Iterable<List<Object>> steps(Integer count, Double timeout) {
        return steps(count, timeout, 1);
    }

    /**
     * 用于单步操作，可实现每收到若干个数据包执行一步操作（如翻页）
     *
     * @param count   需捕获的数据包总数，为None表示无限
     * @param timeout 每个数据包等待时间，为None表示无限
     * @param gap     每接收到多少个数据包返回一次数据
     */

    public Iterable<List<Object>> steps(Integer count, Double timeout, int gap) {
        int caughtCount = 0;
        Long end = timeout != null ? (long) (System.currentTimeMillis() + timeout * 1000) : null;

        List<List<Object>> result = new ArrayList<>();
        while (true) {
            if (timeout != null && System.currentTimeMillis() > end) {
                return result;
            }

            if (caught.size() >= gap) {
                List<Object> data = new ArrayList<>();
                for (int i = 0; i < gap; i++) {
                    data.add(caught.poll());
                }
                result.add(data);
                if (timeout != null) end = (long) (System.currentTimeMillis() + timeout * 1000);

                if (count != null) {
                    caughtCount += gap;
                    if (caughtCount >= count) {
                        return result;
                    }
                }
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 停止监听，清空已监听到的列表
     */
    public void stop() {
        if (this.listening) {
            this.pause();
            this.clear();
        }
        if (this.driver != null) {
            this.driver.stop();
            this.driver = null;
        }

    }

    /**
     * 暂停监听
     */
    public void pause() {
        pause(true);
    }

    /**
     * 暂停监听
     *
     * @param clear 是否清空已获取队列
     */
    public void pause(boolean clear) {
        if (this.listening) {
            this.driver.setCallback("Network.requestWillBeSent", null);
            this.driver.setCallback("Network.responseReceived", null);
            this.driver.setCallback("Network.loadingFinished", null);
            this.driver.setCallback("Network.loadingFailed", null);
            this.listening = false;
        }
        if (clear) this.clear();
    }

    /**
     * 继续暂停的监听
     */
    public void resume() {
        if (this.listening) return;
        this.setCallback();
        this.listening = true;
    }

    /**
     * 清空结果
     */
    public void clear() {
        if (this.requestIds != null) this.requestIds.clear();
        else this.requestIds = new HashMap<>();
        if (this.extraInfoIds != null) this.extraInfoIds.clear();
        else this.extraInfoIds = new HashMap<>();
        this.caught = new ArrayDeque<>();
        this.runningRequests = 0;
        this.runningTargets = 0;
    }

    /**
     * 等待所有请求结束
     *
     * @return 返回是否等待成功
     */
    public boolean waitSilent() {
        return waitSilent(null);
    }

    /**
     * 等待所有请求结束
     *
     * @param timeout 超时，为None时无限等待
     * @return 返回是否等待成功
     */
    public boolean waitSilent(Double timeout) {
        return waitSilent(timeout, false);
    }

    /**
     * 等待所有请求结束
     *
     * @param timeout     超时，为None时无限等待
     * @param targetsOnly 是否只等待targets指定的请求结束
     * @return 返回是否等待成功
     */
    public boolean waitSilent(Double timeout, boolean targetsOnly) {
        if (!this.listening) throw new RuntimeException("监听未启动，用listen.start()启动。");
        if (timeout == null) {
            while ((!targetsOnly && this.runningRequests > 0) || (targetsOnly && this.runningTargets > 0)) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        }
        long endTime = (long) (System.currentTimeMillis() + timeout * 1000);
        while (System.currentTimeMillis() < endTime) {
            if ((!targetsOnly && this.runningRequests <= 0) || (targetsOnly && this.runningTargets <= 0)) return true;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

    /**
     * 切换监听的页面对象
     *
     * @param targetId 新页面对象_target_id
     * @param address  新页面对象address
     * @param page     新页面对象
     */
    protected void toTarget(String targetId, String address, ChromiumBase page) {
        this.targetId = targetId;
        this.address = address;
        this.page = page;
        boolean debug = false;
        if (this.driver != null) {
            debug = this.driver.isDebug();
            this.driver.stop();
        }
        if (this.listening) {
            this.driver = new Driver(this.targetId, "page", this.address);
            this.driver.setDebug(debug);
            this.driver.run("Network.enable");
            this.setCallback();
        }
    }

    /**
     * 设置监听请求的回调函数
     */
    private void setCallback() {
        this.driver.setCallback("Network.requestWillBeSent", new MyRunnable() {
            @Override
            public void run() {
                requestWillBeSent(getMessage());
            }
        });

        this.driver.setCallback("Network.requestWillBeSentExtraInfo", new MyRunnable() {
            @Override
            public void run() {
                requestWillBeSentExtraInfo(getMessage());
            }
        });
        this.driver.setCallback("Network.responseReceived", new MyRunnable() {
            @Override
            public void run() {
                responseReceived(getMessage());
            }
        });
        this.driver.setCallback("Network.responseReceivedExtraInfo", new MyRunnable() {
            @Override
            public void run() {
                responseReceivedExtraInfo(getMessage());
            }
        });
        this.driver.setCallback("Network.loadingFinished", new MyRunnable() {
            @Override
            public void run() {
                loadingFinished(getMessage());
            }
        });
        this.driver.setCallback("Network.loadingFailed", new MyRunnable() {
            @Override
            public void run() {
                loadingFailed(getMessage());
            }
        });
    }

    /**
     * 接收到请求时的回调函数
     */
    protected void requestWillBeSent(Object params) {
        JSONObject jsonObject = JSON.parseObject(params.toString());
        this.runningRequests++;
        DataPacket p = null;
        if (targets != null && targets.isEmpty()) {
            if ((method.equals(ListenerMethod.all) || method.mode.equals(jsonObject.getString("request.method").toUpperCase())) && ((resType != null && resType.isEmpty()) || resType != null && resType.contains(jsonObject.getOrDefault("type", "").toString().toUpperCase()))) {
                runningTargets++;
                String rid = jsonObject.get("requestId").toString();
                p = requestIds.computeIfAbsent(rid, k -> new DataPacket(page.tabId(), ""));
                p.rawRequest = jsonObject;
                if (Boolean.TRUE.equals(jsonObject.get("request.hasPostData")) && jsonObject.get("request.postData") != null) {
                    p.rawPostData = (JSON.parseObject(driver.run("Network.getRequestPostData", Map.of("requestId", rid)).toString()).get("postData")).toString();
                }
            }
        } else {
            String rid = jsonObject.get("requestId").toString();
            for (String target : this.targets) {
                if ((isRegex && Pattern.compile(target).matcher(jsonObject.get("request.url").toString()).find()) || (!isRegex && target.equals(jsonObject.get("request.url"))) && (method.equals(ListenerMethod.all) || method.equals(jsonObject.get("request.method"))) && (resType != null && resType.isEmpty() || resType != null && resType.contains(jsonObject.get("type").toString().toUpperCase()))) {
                    runningTargets++;
                    p = requestIds.computeIfAbsent(rid, k -> new DataPacket(page.tabId(), target));
                    p.rawRequest = jsonObject;
                    break;
                }
            }
        }
        this.extraInfoIds.computeIfAbsent(jsonObject.getString("requestId"), k -> new HashMap<>()).put("obj", p != null ? p : false);
    }

    /**
     * 接收到请求额外信息时的回调函数
     */
    private void requestWillBeSentExtraInfo(Object params) {
        this.runningRequests++;
        this.extraInfoIds.computeIfAbsent(JSON.parseObject(params.toString()).getString("requestId"), k -> new HashMap<>()).put("request", params);

    }

    protected void responseReceived(Object params) {
        JSONObject jsonObject = JSON.parseObject(params.toString());
        DataPacket request = this.requestIds.get(jsonObject.getString("requestId"));
        if (request != null) {
            request.rawResponse = jsonObject.getJSONObject("response");
            request.resourceType = jsonObject.getString("type");
        }
    }

    /**
     * 接收到返回额外信息时的回调函数
     */
    private void responseReceivedExtraInfo(Object params) {
        this.runningRequests--;
        JSONObject jsonObject = JSON.parseObject(params.toString());

        Map<String, Object> map = this.extraInfoIds.get(jsonObject.getString("requestId"));
        if (map != null && !map.isEmpty()) {
            Object o = map.get("obj");
            if (Objects.equals(o, false)) {
                this.extraInfoIds.remove(jsonObject.getString("requestId"));
            } else if (o instanceof DataPacket) {
                ((DataPacket) o).requestExtraInfo = JSON.parseObject(map.get("request").toString());
                ((DataPacket) o).responseExtraInfo = jsonObject;
                this.extraInfoIds.remove(jsonObject.getString("requestId"));
            } else {
                map.put("response", jsonObject);
            }
        }
    }

    /**
     * 请求完成时处理方法
     */
    private void loadingFinished(Object params) {
        JSONObject jsonObject = JSON.parseObject(params.toString());
        this.runningRequests--;
        String rid = jsonObject.getString("requestId");
        DataPacket packet = this.requestIds.get(rid);
        if (packet != null) {
            JSONObject r = JSON.parseObject(this.driver.run("Network.getResponseBody", Map.of("requestId", rid)).toString());
            if (r.containsKey("body")) {
                packet.rawBody = r.getString("body");
                packet.base64Body = r.getBoolean("base64Encoded");
            } else {
                packet.rawBody = "";
                packet.base64Body = false;
            }
            if (packet.rawRequest.getJSONObject("request").get("hasPostData") != null && packet.rawRequest.getJSONObject("request").get("postData") == null) {
                packet.rawPostData = JSON.parseObject(this.driver.run("Network.getRequestPostData", Map.of("requestId", rid, "_timeout", 1)).toString()).get("postData").toString();
            }
        }
        Map<String, Object> map = this.extraInfoIds.get(jsonObject.getString("requestId"));
        if (map != null && !map.isEmpty()) {
            Object o = map.get("obj");
            if (Objects.equals(o, false) || o instanceof DataPacket && this.extraInfoIds.get("request") == null) {
                this.extraInfoIds.remove(jsonObject.getString("requestId"));
            } else if (o instanceof DataPacket && this.extraInfoIds.get("response") != null) {
                ((DataPacket) o).requestExtraInfo = JSON.parseObject(map.get("request").toString());
                ((DataPacket) o).responseExtraInfo = JSON.parseObject(map.get("response").toString());
                ;
                this.extraInfoIds.remove(jsonObject.getString("requestId"));
            }
        }
        this.requestIds.remove(rid);
        if (packet != null) {
            this.caught.add(packet);
            this.runningTargets--;
        }
    }

    /**
     * 请求失败时的回调方法
     */
    private void loadingFailed(Object params) {
        JSONObject jsonObject = JSON.parseObject(params.toString());
        this.runningRequests--;
        String string = jsonObject.getString("requestId");
    }

    public static enum ListenerMethod {
        GET("GET"), get(GET.mode), g(GET.mode), G(GET.mode), POST("POST"), post(POST.mode), p(POST.mode), P(POST.mode), ALL("ALL"), all(ALL.mode), a(ALL.mode), A(ALL.mode);
        private final String mode;

        ListenerMethod(String mode) {
            this.mode = mode;
        }
    }
}
