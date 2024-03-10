package com.ll.DrissonPage.page;

import lombok.Getter;
import lombok.Setter;

import java.io.*;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
public class Timeout {
    private final ChromiumBase page;
    @Setter
    private Double base = 10.0;
    @Setter

    private Double pageLoad = 30.0;
    @Setter

    private Double script = 30.0;

    public Timeout(ChromiumBase page, Double base, Double pageLoad, Double script) {
        this.page = page;
        if (base != null && base >= 0) this.base = base;
        if (pageLoad != null && pageLoad >= 0) this.pageLoad = pageLoad;
        if (script != null && script >= 0) this.script = script;
    }

    public Timeout(ChromiumBase page, Integer base, Integer pageLoad, Integer script) {
        this.page = page;
        if (base != null && base >= 0) this.base = Double.valueOf(base);
        if (pageLoad != null && pageLoad >= 0) this.pageLoad = Double.valueOf(pageLoad);
        if (script != null && script >= 0) this.script = Double.valueOf(script);
    }

    public Timeout(ChromiumBase page) {
        this(page, -1.0, -1.0, -1.0);
    }

    @Override
    public String toString() {
        return "{base=" + base + ", pageLoad=" + pageLoad + ", script=" + script + '}';
    }

    // 深拷贝方法
    // 深拷贝方法
    public Timeout copy() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(this);
            out.flush();  // 在写入对象之前调用 flush

            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray()); ObjectInputStream in = new ObjectInputStream(bis)) {
                return (Timeout) in.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("深拷贝失败，错误原因：" + e.getMessage());
            return this;  // 如果发生异常，返回原始对象
        }
    }
}
