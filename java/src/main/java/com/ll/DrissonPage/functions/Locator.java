package com.ll.DrissonPage.functions;

import com.ll.DrissonPage.base.By;
import com.ll.DrissonPage.base.BySelect;
import com.ll.cssselectortoxpath.CssToXpath;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串选择器
 *
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Locator {
    public static boolean isLoc(String text) {
        return startsWithAny(text, ".", "#", "@", "t:", "t=", "tag:", "tag=", "tx:", "tx=", "tx^", "tx$", "text:", "text=", "text^", "text$", "xpath:", "xpath=", "x:", "x=", "css:", "css=", "c:", "c=");
    }

    public static By getLoc(Object str) {
        return getLoc(str, false);
    }

    public static By getLoc(Object str, boolean cssMode) {
        return getLoc(str, false, cssMode);
    }

    /**
     * 接收本库定位语法或By，转换为标准定位元组，
     *
     * @param str          本库定位语法或By定位元组
     * @param translateCss 是否翻译css selector为xpath
     * @param cssMode      是否尽量用css selector方式
     * @return DrissionPage中By定位元素
     */
    public static By getLoc(Object str, boolean translateCss, boolean cssMode) {
        By byLoc;
        if (str == null || str.toString().trim().isEmpty()) str = "";
        if (str instanceof By) {
            byLoc = cssMode ? translateCssLoc((By) str) : translateLoc((By) str);
        } else if (str instanceof String) {
            byLoc = cssMode ? strToCssLoc((String) str) : strToXPathLoc((String) str);
        } else {
            throw new IllegalArgumentException("loc参数只能是By选择器或str。");
        }
        if (byLoc.getName().equals(BySelect.CSS_SELECTOR) && translateCss) {
            try {
                // 使用你的方法将 CSS Selector 转换为 XPath
                String s = CssToXpath.convertCssSelectorToXpath(byLoc.getValue());
                if (!s.equals(byLoc.getValue())) byLoc = By.xpath(s);
            } catch (Exception e) {
                // 处理异常
            }
        }
        return byLoc;
    }


    /**
     * 处理元素查找语句
     *
     * @param loc 查找语法字符串
     * @return By查询对象
     */
    public static By strToXPathLoc(String loc) {

        By by = By.xpath("");
        String locStr;

        if (loc.startsWith(".")) {
            loc = startsWithAny(loc, ".=", ".:", ".^", ".$") ? loc.replaceFirst(".", "@class") : loc.replaceFirst(".", "@class=");
        } else if (loc.startsWith("#")) {
            loc = startsWithAny(loc, "#=", "#:", "#^", "#$") ? loc.replaceFirst("#", "@id") : loc.replaceFirst("#", "@id=");
        } else if (loc.startsWith("t:") || loc.startsWith("t=")) {
            loc = "tag:" + loc.substring(2);
        } else if (loc.startsWith("tx:") || loc.startsWith("tx=") || loc.startsWith("tx^") || loc.startsWith("tx$")) {
            loc = "text" + loc.substring(2);
        }

        // 多属性查找
        if ((loc.startsWith("@@") || loc.startsWith("@|") || loc.startsWith("@!")) && !("@@".equals(loc) || "@|".equals(loc) || "@!".equals(loc))) {
            locStr = makeMultiXPathStr("*", loc).getValue();
        }
        // 单属性查找
        else if (loc.startsWith("@") && !"@".equals(loc)) {
            locStr = makeSingleXPathStr("*", loc).getValue();
        }
        // 根据tag name查找
        else if ((loc.startsWith("tag:") || loc.startsWith("tag=")) && !("tag:".equals(loc) || "tag=".equals(loc))) {
            int atInd = loc.indexOf('@');
            if (atInd == -1) {
                locStr = "//*[name()='" + loc.substring(4) + "']";
            } else {
                String substring = loc.substring(atInd);
                locStr = substring.startsWith("@@") || substring.startsWith("@|") || substring.startsWith("@!") ? makeMultiXPathStr(loc.substring(4, atInd), substring).getValue() : makeSingleXPathStr(loc.substring(4, atInd), substring).getValue();
            }
        }
        // 根据文本查找
        else if (loc.startsWith("text=")) {
            locStr = "//*[text()=" + makeSearchStr(loc.substring(5)) + "]";
        } else if (loc.startsWith("text:") && !"text:".equals(loc)) {
            locStr = "//*/text()[contains(., " + makeSearchStr(loc.substring(5)) + ")]/..";
        } else if (loc.startsWith("text^") && !"text^".equals(loc)) {
            locStr = "//*/text()[starts-with(., " + makeSearchStr(loc.substring(5)) + ")]/..";
        } else if (loc.startsWith("text$") && !"text$".equals(loc)) {
            locStr = "//*/text()[substring(., string-length(.) - string-length(" + makeSearchStr(loc.substring(5)) + ") +1) = " + makeSearchStr(loc.substring(5)) + "]/..";
        }

        // 用xpath查找
        else if ((loc.startsWith("xpath:") || loc.startsWith("xpath=")) && !("xpath:".equals(loc) || "xpath=".equals(loc))) {
            locStr = loc.substring(6);
        } else if ((loc.startsWith("x:") || loc.startsWith("x=")) && !("x:".equals(loc) || "x=".equals(loc))) {
            locStr = loc.substring(2);
        }

        // 用css selector查找
        else if ((loc.startsWith("css:") || loc.startsWith("css=")) && !("css:".equals(loc) || "css=".equals(loc))) {
            by.setName(BySelect.CSS_SELECTOR);
            locStr = loc.substring(4);
        } else if ((loc.startsWith("c:") || loc.startsWith("c=")) && !("c:".equals(loc) || "c=".equals(loc))) {
            by.setName(BySelect.CSS_SELECTOR);
            locStr = loc.substring(2);
        }

        // 根据文本模糊查找
        else if (!loc.isEmpty()) {
            locStr = "//*/text()[contains(., " + makeSearchStr(loc) + ")]/..";
        } else {
            locStr = "//*";
        }
        by.setValue(locStr);
        return by;
    }

    /**
     * 处理元素查找语句
     *
     * @param loc 查找语法字符串
     * @return By查询对象
     */
    public static By strToCssLoc(String loc) {

        By by = By.css("");
        if (loc.startsWith(".")) {
            if (startsWithAny(loc, ".=", ".:", ".^", ".$")) {
                loc = loc.replaceFirst(".", "@class");
            } else {
                loc = loc.replaceFirst(".", "@class=");
            }
        } else if (loc.startsWith("#")) {
            if (startsWithAny(loc, "#=", "#:", "#^", "#$")) {
                loc = loc.replaceFirst("#", "@id");
            } else {
                loc = loc.replaceFirst("#", "@id=");
            }
        } else if (loc.startsWith("t:") || loc.startsWith("t=")) {
            loc = "tag:" + loc.substring(2);
        } else if (loc.startsWith("tx:") || loc.startsWith("tx=") || loc.startsWith("tx^") || loc.startsWith("tx$")) {
            loc = "text" + loc.substring(2);
        }

        // 多属性查找
        if ((loc.startsWith("@@") || loc.startsWith("@|") || loc.startsWith("@!")) && !("@@".equals(loc) || "@|".equals(loc) || "@!".equals(loc))) {
            by.setValue(makeMultiCssStr("*", loc).getValue());
        }
        // 单属性查找
        else if (loc.startsWith("@") && !"@".equals(loc)) {
            By by1 = makeSingleCssStr("*", loc);
            by.setName(by1.getName());
            by.setValue(by1.getValue());
        }
        // 根据tag name查找
        else if ((loc.startsWith("tag:") || loc.startsWith("tag=")) && !("tag:".equals(loc) || "tag=".equals(loc))) {
            int atInd = loc.indexOf('@');
            if (atInd == -1) {
                by.setValue(loc.substring(4));
            } else if (loc.substring(atInd).startsWith("@@") || loc.substring(atInd).startsWith("@|") || loc.substring(atInd).startsWith("@!")) {

                By by1 = makeMultiCssStr(loc.substring(4, atInd), loc.substring(atInd));
                by.setName(by1.getName());
                by.setValue(by1.getValue());
            } else {

                By by1 = makeSingleCssStr(loc.substring(4, atInd), loc.substring(atInd));
                by.setName(by1.getName());
                by.setValue(by1.getValue());
            }
        }
        // 根据文本查找
        else if (startsWithAny(loc, "text=", "text:", "text^", "text$", "xpath=", "xpath:", "x=", "x:")) {
            by = strToXPathLoc(loc);
        }
        // 用css selector查找
        else if ((loc.startsWith("css:") || loc.startsWith("css=")) && !("css:".equals(loc) || "css=".equals(loc))) {
            by.setValue(loc.substring(4));
        } else if ((loc.startsWith("c:") || loc.startsWith("c=")) && !("c:".equals(loc) || "c=".equals(loc))) {
            by.setValue(loc.substring(2));
        }
        // 根据文本模糊查找
        else if (!loc.isEmpty()) {
            by = strToXPathLoc(loc);
        } else {
            by.setValue("*");
        }

        return by;
    }

    /**
     * 生成单属性xpath语句
     *
     * @param tag  标签名
     * @param text 待处理的字符串
     * @return By 对象
     */
    private static By makeSingleXPathStr(String tag, String text) {
        String argStr = "";
        String txtStr = "";
        List<String> argList = new ArrayList<>();

        if (!tag.equals("*")) {
            argList.add("name()=\"" + tag + "\"");
        }

        if (text.equals("@")) {
            argStr = "not(@*)";
        } else {
            Pattern pattern = Pattern.compile("(.*?)([:=$^])(.*)");
            Matcher matcher = pattern.matcher(text);
            String[] r = new String[0];
            if (matcher.find()) {
                int i = matcher.groupCount();
                r = new String[i];
                for (int i1 = 0; i1 < i; i1++) r[i1] = matcher.group(i1 + 1);
            }
            int lenR = r.length;
            int lenR0 = r[0].length();

            if (lenR == 3 && lenR0 > 1) {
                String symbol = r[1];
                switch (symbol) {
                    case "=":   // 精确查找
                        String arg = r[0].equals("@text()") || r[0].equals("@tx()") ? "." : r[0];
                        argStr = arg + "=" + makeSearchStr(r[2]);
                        break;
                    case "^":   // 匹配开头
                        if (r[0].equals("@text()") || r[0].equals("@tx()")) {
                            txtStr = "/text()[starts-with(., " + makeSearchStr(r[2]) + ")]/..";
                            argStr = "";
                        } else {
                            argStr = "starts-with(" + r[0] + "," + makeSearchStr(r[2]) + ")";
                        }
                        break;
                    case "$":   // 匹配结尾
                        if (r[0].equals("@text()") || r[0].equals("@tx()")) {
                            txtStr = "/text()[substring(., string-length(.) - string-length(" + makeSearchStr(r[2]) + ") +1) = " + makeSearchStr(r[2]) + "]/..";
                            argStr = "";
                        } else {
                            argStr = "substring(" + r[0] + ", string-length(" + r[0] + ") - string-length(" + makeSearchStr(r[2]) + ") +1) = " + makeSearchStr(r[2]);
                        }
                        break;
                    case ":":   // 模糊查找
                        if (r[0].equals("@text()") || r[0].equals("@tx()")) {
                            txtStr = "/text()[contains(., " + makeSearchStr(r[2]) + ")]/..";
                            argStr = "";
                        } else {
                            argStr = "contains(" + r[0] + "," + makeSearchStr(r[2]) + ")";
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("符号不正确：" + symbol);
                }
            } else if (lenR != 3 && lenR0 > 1) {
                argStr = r[0].equals("@text()") || r[0].equals("@tx()") ? "normalize-space(text())" : r[0];
            }
        }

        if (!argStr.isEmpty()) {
            argList.add(argStr);
        }

        argStr = String.join(" and ", argList);
        String xpath = "//*[" + argStr + "]" + txtStr;
        return By.xpath(xpath);
    }

    /**
     * 生成多属性查找的xpath语句
     *
     * @param tag  标签名
     * @param text 待处理的字符串
     * @return By 对象
     */
    private static By makeMultiXPathStr(String tag, String text) {
        List<String> argList = new ArrayList<>();
        String[] args = text.split("@!|@@|@\\|", -1);
        args = Arrays.copyOfRange(args, 1, args.length);
        boolean and;
        if (ArrayUtils.contains(args, "@@") && ArrayUtils.contains(args, "@|")) {
            throw new IllegalArgumentException("@@和@|不能同时出现在一个定位语句中。");
        } else and = ArrayUtils.contains(args, "@@");

        for (int k = 0; k < args.length - 1; k += 2) {
            String[] r = args[k + 1].split("([:=$^])", 2);
            String argStr;
            int lenR = r.length;

            if (r[0].isEmpty()) {  // 不查询任何属性
                argStr = "not(@*)";
            } else {
                boolean ignore = args[k].equals("@!");  // 是否去除某个属性
                if (lenR != 3) {  // 只有属性名没有属性内容，查询是否存在该属性
                    argStr = r[0].equals("text()") || r[0].equals("tx()") ? "normalize-space(text())" : "@" + r[0];
                } else {  // 属性名和内容都有
                    String arg = r[0].equals("text()") || r[0].equals("tx()") ? "." : "@" + r[0];
                    String symbol = r[1];
                    switch (symbol) {
                        case "=":
                            argStr = arg + "=" + makeSearchStr(r[2]);
                            break;
                        case ":":
                            argStr = "contains(" + arg + "," + makeSearchStr(r[2]) + ")";
                            break;
                        case "^":
                            argStr = "starts-with(" + arg + "," + makeSearchStr(r[2]) + ")";
                            break;
                        case "$":
                            argStr = "substring(" + arg + ", string-length(" + arg + ") - string-length(" + makeSearchStr(r[2]) + ") +1) = " + makeSearchStr(r[2]);
                            break;
                        default:
                            throw new IllegalArgumentException("符号不正确：" + symbol);
                    }
                }
                if (ignore) argStr = "not(" + argStr + ")";
            }

            argList.add(argStr);
        }

        String argStr = and ? String.join(" and ", argList) : String.join(" or ", argList);
        if (!tag.equals("*")) {
            String condition = !argStr.isEmpty() ? " and (" + argStr + ")" : "";
            argStr = "name()=\"" + tag + "\"" + condition;
        }

        String xpath = "//*[" + argStr + "]";
        return By.xpath(xpath);
    }

    /**
     * 将"转义，不知何故不能直接用 \ 来转义
     *
     * @param searchStr 查询字符串
     * @return 把"转义后的字符串
     */
    private static String makeSearchStr(String searchStr) {

        String[] parts = searchStr.split("\"");
        int partsNum = parts.length;
        StringBuilder result = new StringBuilder("concat(");

        for (int key = 0; key < partsNum; key++) {
            result.append("\"").append(parts[key]).append("\"");
            if (key < partsNum - 1) result.append(",'\"',");
        }

        result.append(",\"\")");
        return result.toString();
    }

    /**
     * 生成多属性查找的css selector语句
     *
     * @param tag  标签名
     * @param text 待处理的字符串
     * @return By 对象
     */
    private static By makeMultiCssStr(String tag, String text) {

        List<String> argList = new ArrayList<>();
        String[] args = text.split("@!|@@|@\\|", -1);
        args = Arrays.copyOfRange(args, 1, args.length);
        boolean and;
        // @|
        if (ArrayUtils.contains(args, "@@") && ArrayUtils.contains(args, "@|")) {
            throw new IllegalArgumentException("@@和@|不能同时出现在一个定位语句中。");
        } else and = ArrayUtils.contains(args, "@@");

        for (int k = 0; k < args.length - 1; k += 2) {
            String[] r = args[k + 1].split("([:=$^])", 2);
            if (r[0].isEmpty() || r[0].startsWith("text()") || r[0].startsWith("tx()")) {
                return makeMultiXPathStr(tag, text);
            }

            String argStr = "";
            int lenR = r.length;
            boolean ignore = args[k].equals("@!");  // 是否去除某个属性
            if (lenR != 3) {  // 只有属性名没有属性内容，查询是否存在该属性
                argStr = "[" + r[0] + "]";
            } else {  // 属性名和内容都有
                Map<String, String> d = Map.of("=", "", "^", "^", "$", "$", ":", "*");
                argStr = "[" + r[0] + d.get(r[1]) + "=" + cssTrans(r[2]) + "]";
            }

            if (ignore) {
                argStr = ":not(" + argStr + ")";
            }

            argList.add(argStr);
        }

        String argStr = and ? String.join("", argList) : String.join(",", argList);
        return By.css(tag + argStr);
    }

    /**
     * 生成单属性css selector语句
     *
     * @param tag  标签名
     * @param text 待处理的字符串
     * @return By 对象
     */
    private static By makeSingleCssStr(String tag, String text) {

        if (text.equals("@") || text.startsWith("@text()") || text.startsWith("@tx()")) {
            return makeSingleXPathStr(tag, text);
        }

        Pattern pattern = Pattern.compile("(.*?)([:=$^])(.*)");
        Matcher matcher = pattern.matcher(text);
        String[] r = new String[0];
        if (matcher.find()) {
            int i = matcher.groupCount();
            r = new String[i];
            for (int i1 = 0; i1 < i; i1++) r[i1] = matcher.group(i1 + 1);
        }
        String argStr;
        if (r.length == 3) {
            Map<String, String> d = Map.of("=", "", "^", "^", "$", "$", ":", "*");
            argStr = "[" + r[0].substring(1) + d.get(r[1]) + "=" + cssTrans(r[2]) + "]";
        } else {
            argStr = "[" + cssTrans(r[0].substring(1)) + "]";
        }

        String cssSelector = tag + argStr;
        return By.css(cssSelector);
    }

    /**
     * 把By类型转换为css selector或xpath类型的 先转xpath如果xpath无法转换则是css
     *
     * @param by 查询元素
     * @return css selector或xpath查询元素
     */
    public static By translateLoc(By by) {
        if (by == null) throw new NullPointerException("by is not null");
        switch (by.getName()) {
            case XPATH:
            case CSS_SELECTOR:
                break;
            case ID:
                by.setName(BySelect.XPATH);
                by.setValue("//*[@id=\"" + by.getValue() + "\"]");
                break;
            case CLASS_NAME:
                by.setName(BySelect.XPATH);
                by.setValue("//*[@class=\"" + by.getValue() + "\"]");
                break;
            case LINK_TEXT:
                by.setName(BySelect.XPATH);
                by.setValue("//a[text()=\"" + by.getValue() + "\"]");
                break;
            case NAME:
                by.setName(BySelect.XPATH);
                by.setValue("//*[@name=\"" + by.getValue() + "\"]");
                break;
            case TAG_NAME:
                by.setName(BySelect.XPATH);
                by.setValue("//*[name()=\"" + by.getValue() + "\"]");
                break;
            case PARTIAL_LINK_TEXT:
                by.setName(BySelect.XPATH);
                by.setValue("//a[contains(text(),\"" + by.getValue() + "\")]");
            case TEXT:
                by.setName(BySelect.XPATH);
                by.setValue("//*[text()=\"" + by.getValue() + "\")]");
            case PARTIAL_TEXT:
                by.setName(BySelect.XPATH);
                by.setValue("//*[contains(text(),\"" + by.getValue() + "\")]");
            default:
                throw new IllegalArgumentException(by.getName().name() + " Type does not exist,value is " + by.getValue());
        }
        return by;
    }

    /**
     * 把By类型转换为css selector或xpath类型的 先转css如果css无法转换则是xpath
     *
     * @param by 查询元素
     * @return css selector或xpath查询元素
     */
    public static By translateCssLoc(By by) {
        if (by == null) throw new NullPointerException("by is not null");
        switch (by.getName()) {
            case XPATH:
            case CSS_SELECTOR:
                break;
            case ID:
                by.setName(BySelect.CSS_SELECTOR);
                by.setValue("#" + cssTrans(by.getValue()));
                break;
            case CLASS_NAME:
                by.setName(BySelect.CSS_SELECTOR);
                by.setValue("." + cssTrans(by.getValue()));
                break;
            case LINK_TEXT:
                by.setName(BySelect.XPATH);
                by.setValue("//a[text()=\"" + by.getValue() + "\"]");
                break;
            case NAME:
                by.setName(BySelect.CSS_SELECTOR);
                by.setValue("*[@name=" + cssTrans(by.getValue()) + "]");
                break;
            case TAG_NAME:
                by.setName(BySelect.CSS_SELECTOR);
                break;
            case PARTIAL_LINK_TEXT:
                by.setName(BySelect.XPATH);
                by.setValue("//a[contains(text(),\"" + by.getValue() + "\")]");
            case TEXT:
                by.setName(BySelect.XPATH);
                by.setValue("//*[text()=\"" + by.getValue() + "\")]");
            case PARTIAL_TEXT:
                by.setName(BySelect.XPATH);
                by.setValue("//*[contains(text(),\"" + by.getValue() + "\")]");
            default:
                throw new IllegalArgumentException(by.getName().name() + " Type does not exist,value is " + by.getValue());
        }
        return by;
    }

    private static boolean startsWithAny(String str, String... prefixes) {
        for (String prefix : prefixes) {
            if (str.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将文本转换为CSS选择器中的转义形式
     *
     * @param txt 输入文本
     * @return 转义后的文本
     */
    public static String cssTrans(String txt) {
        char[] c = {'!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '`', ',', '{', '|', '}', '~', ' '};
        StringBuilder result = new StringBuilder();
        for (char i : txt.toCharArray()) {
            if (indexOf(c, i) != -1) result.append('\\');
            result.append(i);
        }
        return result.toString();
    }

    private static int indexOf(char[] array, char target) {
        for (int i = 0; i < array.length; i++) if (array[i] == target) return i;
        return -1;
    }
}
