package com.ll.DrissonPage.config;

import com.ll.DrissonPage.functions.Tools;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */

public class PortFinder {
    private static final Map<Integer, String> usedPort = new HashMap<>();
    private static final Lock lock = new ReentrantLock();

    private final Path tmpDir;

    public PortFinder() {
        this(null);
    }

    /**
     * @param path 临时文件保存路径，为None时使用系统临时文件夹
     */
    public PortFinder(String path) {
        Path tmp = (path != null) ? Paths.get(path) : Paths.get(System.getProperty("java.io.tmpdir")).resolve("DrissionPage");
        this.tmpDir = tmp.resolve("UserTempFolder");
        try {
            Files.createDirectories(this.tmpDir);
            if (usedPort.isEmpty()) {
                Tools.cleanFolder(this.tmpDir.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error initializing PortFinder", e);
        }
    }

    private static void cleanDirectory(Path directory) throws IOException {
        Tools.deleteDirectory(directory);
    }

    /**
     * 查找一个可用端口
     *
     * @return 可以使用的端口和用户文件夹路径组成的元组
     */
    public synchronized PortInfo getPort() {
        try {
            lock.lock();
            for (int i = 9600; i < 19600; i++) {
                if (usedPort.containsKey(i)) {
                    continue;
                } else if (Tools.portIsUsing("127.0.0.1", i)) {
                    usedPort.put(i, null);
                    continue;
                }
                String path = Files.createTempDirectory(this.tmpDir, "tmp").toString();
                usedPort.put(i, path);
                return new PortInfo(i, path);
            }

            for (int i = 9600; i < 19600; i++) {
                if (Tools.portIsUsing("127.0.0.1", i)) {
                    continue;
                }
                cleanDirectory(Paths.get(usedPort.get(i)));
                return new PortInfo(i, Files.createTempDirectory(this.tmpDir, "tmp").toString());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }

        throw new RuntimeException("No available port found.");
    }

    @Getter
    public static class PortInfo {
        private final int port;
        private final String path;

        public PortInfo(int port, String path) {
            this.port = port;
            this.path = path;
        }

    }
}