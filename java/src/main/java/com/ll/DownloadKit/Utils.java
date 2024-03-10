package com.ll.DownloadKit;

import com.ll.DataRecorder.Tools;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Utils {
    private static final Map<String, Long> blockSizeMap;
    private static final Map<String, String> FILE_EXISTS_MODE;

    static {
        blockSizeMap = new HashMap<>();
        blockSizeMap.put("b", 1L);
        blockSizeMap.put("k", 1024L);
        blockSizeMap.put("m", 1048576L);
        blockSizeMap.put("g", 21_474_836_480L);

        FILE_EXISTS_MODE = new HashMap<>();
        FILE_EXISTS_MODE.put("rename", "rename");
        FILE_EXISTS_MODE.put("overwrite", "overwrite");
        FILE_EXISTS_MODE.put("skip", "skip");
        FILE_EXISTS_MODE.put("add", "add");
        FILE_EXISTS_MODE.put("r", "rename");
        FILE_EXISTS_MODE.put("o", "overwrite");
        FILE_EXISTS_MODE.put("s", "skip");
        FILE_EXISTS_MODE.put("a", "add");
    }

    public static long blockSizeSetter(Object val) {
        if (val instanceof Integer || val instanceof Long) {
            if (Long.parseLong(val.toString()) > 0) {
                return (int) val;
            } else {
                throw new IllegalArgumentException(val + "数字需要大于0");
            }
        }
        if (val instanceof String && val.toString().length() >= 2) {
            String string = val.toString();
            long num = Long.parseLong(string.substring(0, string.length() - 2));
            Long unit = blockSizeMap.get(String.valueOf(string.charAt(string.length() - 1)).toLowerCase());
            if (unit != null && num > 0) {
                return unit * num;
            } else {
                throw new IllegalArgumentException("单位只支持B、K、M、G，数字必须为大于0的整数。");

            }
        } else {
            throw new IllegalArgumentException("只能传入int或str，数字必须为大于0的整数。");
        }
    }

    public static String pathSetter(Object path) {
        if (path instanceof String) return (String) path;
        if (path instanceof Path) return ((Path) path).toAbsolutePath().toString();
        throw new IllegalArgumentException("只能传入Path或str");

    }



    /**
     * 设置Response对象的编码
     *
     * @param response Response对象
     * @param encoding 指定的编码格式
     * @return 设置编码后的Response对象
     */
    public static Response setCharset(Response response, String encoding) {
        if (encoding != null && !encoding.isEmpty()) {
            response = setEncoding(response, encoding);
            return response;
        }

        // 在headers中获取编码
        String contentType = response.headers("content-type").toString().toLowerCase();
        if (!contentType.endsWith(";")) {
            contentType += ";";
        }

        String charset = findCharset(contentType);
        if (charset != null) {
            response = setEncoding(response, charset);
            return response;
        }

        // 在headers中获取不到编码，且如果是网页
        if (contentType.replace(" ", "").startsWith("text/html")) {
            Matcher matcher = null;
            try {
                if (response.body() != null) {
                    matcher = Pattern.compile("<meta.*?charset=[ \\\\'\"]*([^\"\\\\' />]+).*?>").matcher(response.body().string());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (matcher != null && matcher.find()) {
                charset = matcher.group(1);
            }

            response = setEncoding(response, charset);
        }

        return response;
    }

    private static Response setEncoding(Response response, String charset) {
        if (charset != null && !charset.isEmpty()) {
            Response.Builder build = response.newBuilder();
            ResponseBody body = response.body();
            if (body != null) if (body.contentType() != null)
                Objects.requireNonNull(body.contentType()).charset(Charset.forName(charset));
            return build.build();
        }
        return response;
    }

    private static String findCharset(String contentType) {
        Matcher matcher = Pattern.compile("charset[=: ]*(.*)?;?").matcher(contentType);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 获取文件信息，大小单位为byte
     * 包括：size、path、skip
     *
     * @param response   Response对象
     * @param goalPath   目标文件夹
     * @param rename     重命名
     * @param suffix     重命名后缀名
     * @param fileExists 存在重名文件时的处理方式
     * @param encoding   编码格式
     * @param lock       线程锁
     * @return 文件名、文件大小、保存路径、是否跳过
     */
    public static Map<String, Object> getFileInfo(Response response, String goalPath, String rename, String suffix, FileMode fileExists, String encoding, Lock lock) {
        // ------------获取文件大小------------
        long fileSize = Optional.ofNullable(response.headers().get("Content-Length")).map(Long::parseLong).orElse(-1L);

        // ------------获取网络文件名------------
        String fileName = getFileName(response, encoding);

        // ------------获取保存路径------------
        Path goalPathObj = Paths.get(goalPath);
        goalPath = goalPathObj.getRoot() + goalPathObj.subpath(1, goalPathObj.getNameCount()).toString().replaceAll("[*:|<>?\"]", "").trim();

        Path goalPathAbsolute = Paths.get(goalPath).toAbsolutePath();
        goalPathAbsolute.toFile().mkdirs();
        goalPath = goalPathAbsolute.toString();

        // ------------获取保存文件名------------
        // -------------------重命名-------------------
        String fullFileName;
        if (rename != null) {
            if (suffix != null) {
                fullFileName = suffix.isEmpty() ? rename : rename + "." + suffix;
            } else {
                String[] tmp = fileName.split("\\.", 2);
                String extName = tmp.length > 1 ? "." + tmp[1] : "";
                tmp = rename.split("\\.", 2);
                String extRename = tmp.length > 1 ? "." + tmp[1] : "";
                fullFileName = extRename.equals(extName) ? rename : rename + extName;
            }
        } else if (suffix != null) {
            String[] tmp = fileName.split("\\.", 2);
            fullFileName = suffix.isEmpty() ? tmp[0] : tmp[0] + "." + suffix;
        } else {
            fullFileName = fileName;
        }

        fullFileName = Tools.makeValidName(fullFileName);

        // -------------------生成路径-------------------
        boolean skip = false;
        boolean create = true;
        Path fullPath = Paths.get(goalPath, fullFileName);

        lock.lock();
        try {
            if (Files.exists(fullPath)) {
                if (FileMode.RENAME.equals(fileExists)) {
                    fullPath = Tools.getUsablePath(fullPath.toString());
                } else if (FileMode.SKIP.equals(fileExists)) {
                    skip = true;
                    create = false;
                } else if (FileMode.OVERWRITE.equals(fileExists)) {
                    try {
                        Files.delete(fullPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (FileMode.ADD.equals(fileExists)) {
                    create = false;
                }
            }

            if (create) {
                try {
                    Files.createFile(fullPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            lock.unlock();
        }

        Map<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("size", fileSize);
        fileInfo.put("path", fullPath);
        fileInfo.put("skip", skip);

        return fileInfo;
    }


    /**
     * 从headers或url中获取文件名，如果获取不到，生成一个随机文件名
     *
     * @param response 返回的response
     * @param encoding 在headers获取时指定编码格式
     * @return 下载文件的文件名
     */
    private static String getFileName(Response response, String encoding) {
        String fileName = "";
        String charset = "utf-8";

        String contentDisposition = Objects.requireNonNull(response.header("content-disposition", "")).replace(" ", "");

        if (!contentDisposition.isEmpty()) {
            String txt = matchFilename(contentDisposition);
            if (txt != null) {
                if (txt.contains("''")) {
                    String[] parts = txt.split("''", 2);
                    charset = parts[0];
                    fileName = parts[1];
                } else {
                    fileName = txt;
                }
            } else {
                txt = matchFilename(contentDisposition, "filename");
                if (txt != null) {
                    fileName = txt;
                    if (response.body() != null) {
                        charset = encoding != null ? encoding : Objects.requireNonNull(Objects.requireNonNull(response.body().contentType()).charset()).toString();
                    }
                }
            }

            fileName = fileName.replace("'", "");
        }

        if (fileName.isEmpty()) {
            Paths.get(response.request().url().encodedPath());
            fileName = Paths.get(response.request().url().encodedPath()).getFileName().toString().split("\\?")[0];
        }

        if (fileName.isEmpty()) {
            fileName = "untitled_" + System.currentTimeMillis() + "_" + ThreadLocalRandom.current().nextInt(100);
        }

        charset = charset.isEmpty() ? "utf-8" : charset;
        try {
            return URLDecoder.decode(fileName, charset);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String matchFilename(String contentDisposition) {
        Matcher matcher = Pattern.compile("filename\\*?=\"?([^\";]+)\"?").matcher(contentDisposition);
        if (matcher.find()) {
            String[] parts = matcher.group(1).split("''", 2);
            return parts.length == 2 ? parts[0] + parts[1] : parts[0];
        }
        return null;
    }

    private static String matchFilename(String contentDisposition, String pattern) {
        Matcher matcher = Pattern.compile(pattern + "=\"?([^\";]+)\"?").matcher(contentDisposition);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 设置OkHttpClient.Builder对象的cookies
     *
     * @param builder OkHttpClient.Builder对象
     * @param cookies cookies信息
     */
    public static void setSessionCookies(OkHttpClient.Builder builder, List<Cookie> cookies) {
        for (Cookie cookie : cookies) {
            builder.setCookieJar$okhttp(new CookieJar() {
                @Override
                public void saveFromResponse(@NotNull HttpUrl httpUrl, @NotNull List<Cookie> list) {
                    list.add(cookie);
                }

                @NotNull
                @Override
                public List<Cookie> loadForRequest(@NotNull HttpUrl httpUrl) {
                    return new ArrayList<>();
                }
            });
        }
    }
}
