package com.ll.DataRecorder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Tools {


    /**
     * 检查文件或文件夹是否有重名，并返回可以使用的路径
     * @param path 文件或文件夹路径
     * @return 可用的路径，Path对象
     */
    public static Path getUsablePath(String path) {
        return getUsablePath(path, true, true);
    }

    /**
     * 检查文件或文件夹是否有重名，并返回可以使用的路径
     *
     * @param path          文件或文件夹路径
     * @param isFile        目标是文件还是文件夹
     * @param createParents 是否创建目标路径
     * @return 可用的路径，Path对象
     */
    public static Path getUsablePath(String path, boolean isFile, boolean createParents) {
        Path filePath = Paths.get(path).toAbsolutePath();
        Path parent = filePath.getParent();
        if (createParents) parent.toFile().mkdirs();
        String name = makeValidName(filePath.getFileName().toString());
        int num;
        String srcName;
        boolean firstTime = true;

        while (Files.exists(filePath) && (Files.isRegularFile(filePath) == isFile)) {
            Matcher matcher = Pattern.compile("(.*)_(\\d+)$").matcher(name);
            if (!matcher.find() || (matcher.find() && firstTime)) {
                srcName = name;
                num = 1;
            } else {
                srcName = matcher.group(1);
                num = Integer.parseInt(matcher.group(2)) + 1;
            }
            name = srcName + "_" + num;
            filePath = parent.resolve(name);
            firstTime = false;
        }

        return filePath;
    }

    /**
     * 获取有效的文件名
     *
     * @param fullName 文件名
     * @return 可用的文件名
     */
    public static String makeValidName(String fullName) {
        fullName = fullName.trim();

        String name;
        String ext;
        int extLong;

        Matcher matcher = Pattern.compile("(.*)(\\.[^.]+$)").matcher(fullName);
        if (matcher.find()) {
            name = matcher.group(1);
            ext = matcher.group(2);
            extLong = ext.length();
        } else {
            name = fullName;
            ext = "";
            extLong = 0;
        }

        while (getLong(name) > 255 - extLong) {
            name = name.substring(0, name.length() - 1);
        }

        fullName = name + ext;

        return fullName.replaceAll("[<>/\\\\|:*?\\n]", "");
    }

    /**
     * 返回字符串中字符个数（一个汉字是2个字符）
     *
     * @param txt 字符串
     * @return 字符个数
     */
    public static int getLong(String txt) {
        int txtLen = txt.length();
        return (txt.getBytes().length - txtLen) / 2 + txtLen;
    }


}
