package com.ll.DrissonPage.config;

import com.alibaba.fastjson.JSON;
import com.ll.DrissonPage.error.extend.loadFileError;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.ini4j.Profile;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ini配置文件加载
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 * @original DrissionPage
 */
@Getter
public class OptionsManager {
    /**
     * 配置文件路径
     */
    private final String iniPath;
    /**
     * 配置文件参数
     */
    private final Wini ini;

    /**
     * 使用默认初始化参数
     */
    public OptionsManager() {
        this(null);
    }


    /**
     * 初始化参数
     *
     * @param iniPath 配置文件路径
     */
    public OptionsManager(String iniPath) {
        this("configs.ini", iniPath);
    }

    /**
     * 初始化参数
     *
     * @param fileName 初始化值
     * @param iniPath  配置文件路径
     */
    public OptionsManager(String fileName, String iniPath) {
        this.iniPath = iniPath;
        //加载配置文件中的数据
        this.ini = loadIni(fileName, iniPath);
    }

    /**
     * 加载配置文件，使用的是map的putAll
     *
     * @param fileName 源位置
     * @param path     重新加载的文件
     * @return 返回map集合
     */
    private Wini loadIni(String fileName, String path) {
        //加载内部资源configs.ini
        Wini wini;
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(fileName)) {
            wini = new Wini(resourceAsStream);
        } catch (IOException e) {
            throw new loadFileError(e);
        }
        //加载外部资源configs.ini
        if (StringUtils.isNotEmpty(path)) {
            Wini externalWini = null;
            try {
                externalWini = new Wini(new File(path));
            } catch (IOException ignored) {
            }
            if (externalWini != null) {
                wini.putAll(externalWini);
            }
        }
        return wini;
    }

    /**
     * 获取配置项
     *
     * @param section 配置项名称
     * @return 配置项
     */
    public Profile.Section getOption(String section) {
        return ini.get(section);
    }

    /**
     * 获取配置的值
     *
     * @param section 段名
     * @param key     项名
     * @return 项值
     */
    public String getValue(String section, String key) {
        Profile.Section option = getOption(section);
        return option != null ? option.get(key) : null;
    }

    /**
     * 设置配置项的值
     *
     * @param section 配置项
     * @param item    配置
     * @param value   值
     */
    public void setItem(String section, String item, Object value) {
        ini.add(section, item, value);
    }

    // 删除配置项
    public String removeItem(String sectionName, String optionName) {
        Profile.Section section = ini.get(sectionName);
        return section != null ? section.remove(optionName) : null;
    }

    // 保存配置文件
    public void save(String path) throws IOException {
        Path filePath;
        if ("default".equals(path)) {
            // 如果保存路径为'default'，则使用默认的configs.ini
            filePath = Paths.get(getClass().getResource("/configs.ini").getFile()).toAbsolutePath();
        } else if (path == null) {
            // 如果保存路径为null，则使用当前配置文件路径
            filePath = Paths.get(iniPath).toAbsolutePath();
        } else {
            // 使用指定的保存路径
            filePath = Paths.get(path).toAbsolutePath();
        }

        Files.write(filePath, ini.toString().getBytes());
        System.out.println("配置已保存到文件：" + filePath);
        if (filePath.equals(Paths.get(getClass().getResource("/configs.ini").getFile()).toAbsolutePath())) {
            System.out.println("以后程序可自动从文件加载配置.");
        }
    }

    /**
     * 保存配置到默认文件
     */
    public void saveToDefault() throws IOException {
        save("default");
    }

    public void show() {
        System.out.println(JSON.toJSONString(ini));
    }
}