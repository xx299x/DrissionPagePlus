package com.ll.DrissonPage.units.screencast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.base.MyRunnable;
import com.ll.DrissonPage.page.ChromiumPage;
import com.ll.DrissonPage.page.ChromiumBase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Screencast {
    private final ChromiumBase page;
    protected String mode;
    private Path path;
    private Path tmpPath;
    private boolean running;
    private boolean enable;

    public Screencast(ChromiumBase chromiumBase) {
        this.page = chromiumBase;
        this.path = null;
        this.tmpPath = null;
        this.running = false;
        this.enable = false;
        this.mode = "video";
    }

    /**
     * @return 返回用于设置录屏幕式的对象
     */
    public ScreencastMode setMode() {
        return new ScreencastMode(this);
    }

    public void start(String savePath) {
        this.setSavePath(savePath);
        if (this.path == null) throw new NullPointerException("savePath必须设置");
        if (this.mode.equals("frugal_video") || this.mode.equals("video")) {
            String tmpPath1 = ((ChromiumPage) this.page.browser().getPage()).getChromiumOptions().getTmpPath();
            this.tmpPath = Paths.get(tmpPath1 == null ? System.getProperty("java.io.tmpdir") + File.separatorChar + "DrissionPage" : tmpPath1 + File.separatorChar + "screencast_tmp_" + System.currentTimeMillis() + "_" + ((int) (Math.random() * 100)));
            this.tmpPath.toFile().mkdirs();
        }
        if (this.mode.startsWith("frugal")) {
            this.page.driver().setCallback("Page.screencastFrame", new MyRunnable() {
                @Override
                public void run() {
                    onScreencastFrame(getMessage());
                }
            });
            this.page.runCdp("Page.startScreencast", Map.of("everyNthFrame", 1, "quality", 100));
        } else if (!this.mode.startsWith("js")) {
            this.running = true;
            this.enable = true;
            new Thread(this::run).start();
        } else {
            String js =
                    "async function () {\n" +
                    "    stream = await navigator.mediaDevices.getDisplayMedia({video: true, audio: true});\n" +
                    "    mime = MediaRecorder.isTypeSupported(\"video/webm; codecs=vp9\") ? \"video/webm; codecs=vp9\" : \"video/webm\";\n" +
                    "    mediaRecorder = new MediaRecorder(stream, {mimeType: mime});\n" +
                    "    DrissionPage_Screencast_chunks = [];\n" +
                    "    mediaRecorder.addEventListener('dataavailable', function(e) {\n" +
                    "        DrissionPage_Screencast_blob_ok = false;\n" +
                    "        DrissionPage_Screencast_chunks.push(e.data);\n" +
                    "        DrissionPage_Screencast_blob_ok = true;\n" +
                    "    });\n" +
                    "    mediaRecorder.start();\n" +
                    "    mediaRecorder.addEventListener('stop', function(){\n" +
                    "        while(DrissionPage_Screencast_blob_ok==false){}\n" +
                    "        DrissionPage_Screencast_blob = new Blob(DrissionPage_Screencast_chunks, {type: DrissionPage_Screencast_chunks[0].type});\n" +
                    "    });\n" +
                    "}";
            System.out.println("请手动选择要录制的目标。");
            this.page.runJs("var DrissionPage_Screencast_blob;var DrissionPage_Screencast_blob_ok=false;");
            this.page.runJs(js);
        }
    }

    /**
     * 设置保存路径
     *
     * @param savePath 保存路径
     */

    public void setSavePath(String savePath) {
        if (savePath != null && !savePath.isEmpty()) {
            Path path = Paths.get(savePath);
            File pathFile = path.toFile();
            if (pathFile.exists() && pathFile.isFile()) throw new IllegalArgumentException("saveOath必须指定文件夹。");
            pathFile.mkdirs();
            this.path = path;
        }
    }


    /**
     * 非节俭模式运行方法
     */
    private void run() {
        this.running = true;
        Path path = this.tmpPath != null ? this.tmpPath : this.path;
        while (this.enable) {
            this.page.getScreenshot(path.toFile().getAbsolutePath(), "", null, null, false, null, null);
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.running = false;
    }

    /**
     * 节俭模式运行方法
     */
    private void onScreencastFrame(Object params) {
        Path path = this.tmpPath != null ? this.tmpPath : this.path;
        JSONObject jsonObject = JSON.parseObject(params.toString());
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile().getAbsolutePath() + File.separatorChar + jsonObject.getJSONObject("metadata").getString("timestamp") + ".jpg"))) {
            writer.write(Arrays.toString(Base64.getDecoder().decode(jsonObject.getBytes("data"))));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.page.runCdp("Page.screencastFrameAck", Map.of("sessionId", jsonObject.get("sessionId")));
    }
}
