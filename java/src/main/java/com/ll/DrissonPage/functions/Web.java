package com.ll.DrissonPage.functions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.ll.DrissonPage.base.DrissionElement;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.page.ChromiumBase;
import com.ll.DrissonPage.units.Coordinate;
import okhttp3.Cookie;
import org.apache.commons.text.StringEscapeUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Web {
    /**
     * 前面无须换行的元素
     */
    private static final List<String> NOWRAP_LIST = Arrays.asList("br", "sub", "sup", "em", "strong", "a", "font", "b", "span", "s", "i", "del", "ins", "img", "td", "th", "abbr", "bdi", "bdo", "cite", "code", "data", "dfn", "kbd", "mark", "q", "rp", "rt", "ruby", "samp", "small", "time", "u", "var", "wbr", "button", "slot", "content");
    /**
     * 后面添加换行的元素
     */
    private static final List<String> WRAP_AFTER_LIST = Arrays.asList("p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "ol", "li", "blockquote", "header", "footer", "address", "article", "aside", "main", "nav", "section", "figcaption", "summary");
    /**
     * 不获取文本的元素
     */
    private static final List<String> NO_TEXT_LIST = Arrays.asList("script", "style", "video", "audio", "iframe", "embed", "noscript", "canvas", "template");
    /**
     * 用/t分隔的元素
     */
    private static final List<String> TAB_LIST = Arrays.asList("td", "th");

    /**
     * 获取元素内所有文本
     *
     * @param e 元素对象
     * @return 元素内所有文本
     */
    public static String getEleTxt(DrissionElement<?, ?> e) {
        String tag = e.tag();
        if (NO_TEXT_LIST.contains(tag)) return e.rawText();

        List<String> reStr = getNodeTxt(e, false);
        if (!reStr.isEmpty() && reStr.get(reStr.size() - 1).equals("\n")) reStr.remove(reStr.size() - 1);
        return formatHtml(reStr.stream().map(i -> i != null && !i.equals("true") ? i : "\n").collect(Collectors.joining("")));
    }

    private static List<String> getNodeTxt(DrissionElement<?, ?> ele, boolean pre) {
        String tag = ele.tag();
        if ("br".equals(tag)) return new ArrayList<>(List.of("true"));
        if (!pre && "pre".equals(tag)) pre = true;

        List<String> strList = new ArrayList<>();
        //标签内的文本不返回
        if (NO_TEXT_LIST.contains(tag) && !pre) return strList;

        List<?> eles = ele.eles("xpath:./text() | *");
        String prevEle = "";
        for (Object e : eles) {
            //元素节点
            DrissionElement<?, ?> el = (DrissionElement<?, ?>) e;
            //元素间换行的情况
            if (!NOWRAP_LIST.contains(el.tag()) && !strList.isEmpty() && !"\n".equals(strList.get(strList.size() - 1)))
                strList.add("\n");
            //表格的行
            if (TAB_LIST.contains(el.tag()) && TAB_LIST.contains(prevEle)) strList.add("\t");
            strList.addAll(getNodeTxt(el, pre));
            prevEle = el.tag();
        }
        if (WRAP_AFTER_LIST.contains(tag) && !strList.isEmpty() && !"\n".equals(strList.get(strList.size() - 1)))
            strList.add("\n");
        return strList;
    }

    /**
     * 处理HTML编码字符
     *
     * @param text HTML文本
     * @return 格式化后的HTML文本
     */
    public static String formatHtml(String text) {
        return text != null ? StringEscapeUtils.unescapeHtml4(text).replace("\u00a0", " ") : text;
    }

    /**
     * 判断给定的坐标是否在视口中
     *
     * @param page       ChromePage对象
     * @param coordinate 页面绝对坐标
     */
    public static Boolean locationInViewport(ChromiumBase page, Coordinate coordinate) {
        return locationInViewport(page, coordinate.getX(), coordinate.getY());
    }

    /**
     * 判断给定的坐标是否在视口中
     *
     * @param page ChromePage对象
     * @param locX 页面绝对坐标x
     * @param locY 页面绝对坐标y
     */
    public static Boolean locationInViewport(ChromiumBase page, Integer locX, Integer locY) {
        String js = "function(){var x = " + locX + "; var y = " + locY + ";\n" + "    const scrollLeft = document.documentElement.scrollLeft;\n" + "    const scrollTop = document.documentElement.scrollTop;\n" + "    const vWidth = document.documentElement.clientWidth;\n" + "    const vHeight = document.documentElement.clientHeight;\n" + "    if (x< scrollLeft || y < scrollTop || x > vWidth + scrollLeft || y > vHeight + scrollTop){return false;}\n" + "    return true;}";
        return Boolean.parseBoolean(page.runJs(js).toString());
    }

    /**
     * 接收元素及偏移坐标，把坐标滚动到页面中间，返回该点在视口中的坐标
     * 有偏移量时以元素左上角坐标为基准，没有时以click_point为基准
     *
     * @param ele      元素对象
     * @param offset_x 偏移量x
     * @param offset_y 偏移量y
     * @return 视口中的坐标
     */

    public static Coordinate offsetScroll(ChromiumElement ele, Integer offset_x, Integer offset_y) {
        Coordinate location = ele.rect().location();
        int loc_x = location.getX();
        int loc_y = location.getY();
        Coordinate clickPoint = ele.rect().clickPoint();
        int cp_x = clickPoint.getX();  // Assuming click_point is the same as location for x
        int cp_y = clickPoint.getY();  // Assuming click_point is the same as location for y

        int lx = (offset_x != null && offset_x != 0) ? loc_x + offset_x : cp_x;
        int ly = (offset_y != null && offset_y != 0) ? loc_y + offset_y : cp_y;

        if (!locationInViewport(ele.getOwner(), lx, ly)) {
            int clientWidth = Integer.parseInt(ele.getOwner().runJs("return document.body.clientWidth;").toString());
            int clientHeight = Integer.parseInt(ele.getOwner().runJs("return document.body.clientHeight;").toString());
            ele.scroll().toLocation(lx - clientWidth / 2, ly - clientHeight / 2);

        }
        location = ele.rect().viewportLocation();
        int cl_x = location.getX();
        int cl_y = location.getY();
        clickPoint = ele.rect().viewportClickPoint();
        int ccp_x = clickPoint.getX();  // Assuming viewport_click_point is the same as viewport_location for x
        int ccp_y = clickPoint.getY();  // Assuming viewport_click_point is the same as viewport_location for y
        int cx = (offset_x != null && offset_x != 0) ? cl_x + offset_x : ccp_x;
        int cy = (offset_y != null && offset_y != 0) ? cl_y + offset_y : ccp_y;

        return new Coordinate(cx, cy);
    }


    /**
     * 获取绝对url
     *
     * @param link 超链接
     * @return 绝对链接
     */
    public static String makeAbsoluteLink(String link) {
        return makeAbsoluteLink(link, null);
    }

    /**
     * 获取绝对url
     *
     * @param link    超链接
     * @param baseURI 页面或iframe的url
     * @return 绝对链接
     */
    public static String makeAbsoluteLink(String link, String baseURI) {
        if (Objects.isNull(link) || link.trim().isEmpty()) {
            return link;
        }

        link = link.trim();
        URI parsed = URI.create(link);

        // 是相对路径，与页面url拼接并返回
        if (parsed.getScheme() == null && parsed.getHost() == null) {
            if (baseURI == null || baseURI.isEmpty()) return link;
            try {
                return new URL(new URL(baseURI), link).toString();
            } catch (Exception e) {
                // 处理异常，例如URL格式不正确
                e.printStackTrace();
                return link;
            }
        }
        // 是绝对路径但缺少协议，从页面url获取协议并修复
        if (!parsed.isAbsolute() && baseURI != null && !baseURI.isEmpty()) {
            try {
                return new URI(baseURI).resolve(parsed).toString();
            } catch (URISyntaxException e) {
                // 处理异常，例如URL格式不正确
                e.printStackTrace();
                return link;
            }
        }

        // 绝对路径且不缺协议，直接返回
        return link;
    }

    /**
     * 检查文本是否js函数
     */
    public static boolean isJsFunc(String func) {
        func = func.trim();
        return (func.startsWith("function") || func.startsWith("async ")) && func.endsWith("}") || func.contains("=>");
    }


    public static Map<String, Object> cookieToMap(Object cookie) {
        if (cookie instanceof Cookie) {
            // 如果是 Cookie 对象
            return cookieObjectToMap((Cookie) cookie);
        } else if (cookie instanceof String) {
            // 如果是字符串
            return cookieStringToMap((String) cookie);
        } else if (cookie instanceof Map) {
            // 如果已经是 Map
            return JSON.parseObject(JSON.toJSONString(cookie));
        } else {
            throw new IllegalArgumentException("Invalid cookie type: " + cookie.getClass().getName());
        }
    }

    private static Map<String, Object> cookieObjectToMap(Cookie cookie) {
        Map<String, Object> cookieMap = new HashMap<>();
        // 根据 Cookie 对象的属性进行填充 cookieMap
        cookieMap.put("name", cookie.name());
        cookieMap.put("value", cookie.value());
        // 其他属性类似，根据需要添加
        return cookieMap;
    }

    private static Map<String, Object> cookieStringToMap(String cookieString) {
        Map<String, Object> cookieMap = new HashMap<>();
        String[] cookieParts = cookieString.split(";");

        for (String part : cookieParts) {
            String[] keyValue = part.trim().split("=");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                cookieMap.put(key, value);
            }
        }

        return cookieMap;
    }

    public static List<Map<String, Object>> cookiesToList(Object cookies) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (cookies instanceof List) {
            List<?> cookieList = (List<?>) cookies;
            for (Object cookie : cookieList) {
                if (cookie instanceof String) {
                    Map<String, Object> cookieMap = cookieToMap(cookie);
                    if (cookieMap != null) result.add(cookieMap);
                } else if (cookie instanceof Map) {
                    result.add((JSON.parseObject(JSON.toJSONString(cookie))));
                }
            }
        }
        return result;
    }

    public static void setBrowserCookies(ChromiumBase page, Object cookies) {
        List<Map<String, Object>> mapList = cookiesToList(cookies);
        for (Map<String, Object> cookieMap : mapList) {
            if (cookieMap.containsKey("expiry")) {
                cookieMap.put("expires", Integer.parseInt(cookieMap.get("expiry").toString()));
                cookieMap.remove("expiry");
            }

            if (cookieMap.containsKey("expires")) {
                Object expires = cookieMap.get("expires");
                if (expires != null) {
                    if (expires.toString().matches("\\d+?")) {
                        cookieMap.put("expires", Integer.parseInt(expires.toString()));
                    } else if (expires.toString().matches("\\d+(\\.\\d+)?"))
                        cookieMap.put("expires", Double.parseDouble(expires.toString()));
                    else {
                        try {
                            cookieMap.put("expires", convertExpiresToTimestamp(expires.toString()));
                        } catch (ParseException ignored) {

                        }
                    }
                }
            }

            if (cookieMap.get("value") == null) cookieMap.put("value", "");
            else if (!(cookieMap.get("value") instanceof String))
                cookieMap.put("value", String.valueOf(cookieMap.get("value")));
            if (cookieMap.get("name") != null && ((String) cookieMap.get("name")).startsWith("__Secure-"))
                cookieMap.put("secure", true);

            if (cookieMap.get("name") != null && ((String) cookieMap.get("name")).startsWith("__Host-")) {
                cookieMap.put("path", "/");
                cookieMap.put("secure", true);
                cookieMap.put("url", page.url());
                page.runCdpLoaded("Network.setCookie", JSONObject.parseObject(JSONObject.toJSONString(cookieMap)));
                continue;  // 不用设置域名，可退出
            }
            if (cookieMap.containsKey("domain")) try {
                page.runCdpLoaded("Network.setCookie", JSONObject.parseObject(JSONObject.toJSONString(cookieMap)));
                isCookieInDriver(page, cookieMap);
            } catch (Exception ignored) {
                // Handle the exception as needed
            }
            URL url;
            try {
                url = new URL(page.url());
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            List<String> dList = new ArrayList<>(List.of(url.getHost().split("\\.")));
            List<String> tmp = new ArrayList<>();
            if (dList.size() > 1) {
                for (int i = 1; i < dList.size(); i++) {
                    tmp.add(".");
                    tmp.add(dList.get(i));
                }
            }
            for (int i = 0; i < tmp.size(); i++) {
                String d = String.join("", tmp.subList(i, tmp.size()));
                cookieMap.put("domain", d);
                page.runCdpLoaded("Network.setCookie", cookieMap);
                if (isCookieInDriver(page, cookieMap)) break;
            }
        }
    }


    public static long convertExpiresToTimestamp(String expiresStr) throws ParseException {
        // 定义日期时间格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        Date expiresDate = dateFormat.parse(expiresStr);

        // 将日期时间对象转换为 UNIX 时间戳（毫秒）
        return expiresDate.getTime() / 1000;
    }

    /**
     * 查询cookie是否在浏览器内
     *
     * @param page   BasePage对象
     * @param cookie map
     * @return 是否存在
     */
    public static boolean isCookieInDriver(ChromiumBase page, Map<String, Object> cookie) {
        Object cookies;
        List<Map<String, Object>> allCookies;
        if (cookie.containsKey("domain")) {
            cookies = page.cookies(true);
            allCookies = JSON.parseObject(cookies.toString(), new TypeReference<>() {
            });
            for (Map<String, Object> c : allCookies) {
                if (cookie.get("name").equals(c.get("name")) && cookie.get("value").equals(c.get("value")) && cookie.get("domain").equals(c.get("domain"))) {
                    return true;
                }
            }
        } else {
            cookies = page.cookies(true);
            allCookies = JSON.parseObject(cookies.toString(), new TypeReference<>() {
            });
            for (Map<String, Object> c : allCookies) {
                if (cookie.get("name").equals(c.get("name")) && cookie.get("value").equals(c.get("value"))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取知道blob资源
     *
     * @param page    资源所在页面对象
     * @param url     资源url
     * @param asBytes 是否以字节形式返回
     * @return 资源内容
     */
    public static Object getBlob(ChromiumBase page, String url, boolean asBytes) {
        if (!url.startsWith("blob")) {
            throw new IllegalArgumentException("该链接非blob类型。");
        }

        String js = "function fetchData(url) {\n" + "  return new Promise((resolve, reject) => {\n" + "    var xhr = new XMLHttpRequest();\n" + "    xhr.responseType = 'blob';\n" + "    xhr.onload = function() {\n" + "      var reader  = new FileReader();\n" + "      reader.onloadend = function(){resolve(reader.result);}\n" + "      reader.readAsDataURL(xhr.response);\n" + "    };\n" + "    xhr.open('GET', url, true);\n" + "    xhr.send();\n" + "  });\n" + "}\n";

        Object result;
        try {
            result = page.runJs(js, List.of(url));
        } catch (Exception e) {
            throw new RuntimeException("无法获取该资源。", e);
        }

        if (asBytes) {
            return Base64.getDecoder().decode(result.toString().split(",", 2)[1]);
        } else {
            return result;
        }
    }
}
