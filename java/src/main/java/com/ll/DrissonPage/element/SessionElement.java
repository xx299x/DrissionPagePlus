package com.ll.DrissonPage.element;

import com.alibaba.fastjson.JSON;
import com.ll.DrissonPage.base.*;
import com.ll.DrissonPage.functions.Locator;
import com.ll.DrissonPage.functions.Web;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class SessionElement extends DrissionElement<BasePage<?>, SessionElement> {
    private final Element innerEle;

    public SessionElement(Element ele) {
        this(ele, null);
    }

    public SessionElement(Element ele, BasePage<?> page) {
        super(page);
        this.innerEle = ele;
        this.setType("SessionElement");
    }

    public static List<SessionElement> makeSessionEle(String html, String loc, Integer index) {
        return _makeSessionEle(html, loc, index);
    }

    public static List<SessionElement> makeSessionEle(BaseParser<?> ele, String loc, Integer index) {
        return _makeSessionEle(ele, loc, index);

    }

    public static List<SessionElement> makeSessionEle(String html, By by, Integer index) {
        return _makeSessionEle(html, by, index);

    }

    public static List<SessionElement> makeSessionEle(BaseParser<?> ele, By by, Integer index) {
        return _makeSessionEle(ele, by, index);

    }

    /**
     * 从接收到的对象或html文本中查找元素，返回SessionElement对象
     * 如要直接从html生成SessionElement而不在下级查找，loc输入None即可
     *
     * @param htmlOrEle html文本、BaseParser对象
     * @param loc       by或字符串，为None时不在下级查找，返回根元素
     * @param index     获取第几个元素，从1开始，可传入负数获取倒数第几个，None获取所有
     * @return 返回SessionElement元素或列表
     */
    private static List<SessionElement> _makeSessionEle(Object htmlOrEle, Object loc, Integer index) {
        //----------------------------------处理定位符------------------------------------------
        By locs;
        if (loc == null) {
            if (htmlOrEle instanceof SessionElement) {
                ArrayList<SessionElement> sessionElements = new ArrayList<>();
                sessionElements.add((SessionElement) htmlOrEle);
                return sessionElements;
            }
            locs = By.xpath(".");
        } else if (loc instanceof By || loc instanceof String) {
            locs = Locator.getLoc(loc);
        } else {
            throw new ClassCastException("定位符必须为str或By");
        }
        //---------------根据传入对象类型获取页面对象和lxml元素对象---------------
        //直接传入html文本
        BasePage<?> page;
        if (htmlOrEle instanceof String) {
            page = null;
            htmlOrEle = Jsoup.parse((String) htmlOrEle);
        } else if (htmlOrEle instanceof SessionElement) {
            page = ((SessionElement) htmlOrEle).getOwner();
            String str = locs.getValue();
            if (locs.getName().equals(BySelect.XPATH) && str.stripLeading().startsWith("/")) {
                str = "." + str;
                htmlOrEle = ((SessionElement) htmlOrEle).innerEle();
                //若css以>开头，表示找元素的直接子元素，要用page以绝对路径才能找到
            } else if (locs.getName().equals(BySelect.CSS_SELECTOR) && str.stripLeading().startsWith(">")) {
                str = ((SessionElement) htmlOrEle).cssPath() + str;
                if (((SessionElement) htmlOrEle).getOwner() != null) {
                    htmlOrEle = Jsoup.parse(((SessionElement) htmlOrEle).getOwner().html());
                } else { //接收html文本，无page的情况
                    htmlOrEle = Jsoup.parse(((SessionElement) htmlOrEle).ele("xpath:/ancestor::*").html());
                }
            } else {
                htmlOrEle = ((SessionElement) htmlOrEle).innerEle();
            }
            locs.setValue(str);
        } else if (htmlOrEle instanceof ChromiumElement) {
            String str = locs.getValue();
            if (locs.getName().equals(BySelect.XPATH) && str.stripLeading().startsWith("/")) {
                str = "." + str;
            } else if (locs.getName().equals(BySelect.CSS_SELECTOR) && str.stripLeading().startsWith(">")) {
                str = ((ChromiumElement) htmlOrEle).cssPath() + str;
            }
            locs.setValue(str);
            //获取整个页面html再定位到当前元素，以实现查找上级元素
            page = ((ChromiumElement) htmlOrEle).getOwner();
            String xpath = ((ChromiumElement) htmlOrEle).xpath();
            //ChromiumElement，兼容传入的元素在iframe内的情况
            String html;
            if (((ChromiumElement) htmlOrEle).getDocId() != null) {
                html = JSON.parseObject(((ChromiumElement) htmlOrEle).getOwner().runCdp("DOM.getOuterHTML", Map.of("objectId", ((ChromiumElement) htmlOrEle).getDocId())).toString()).getString("outerHTML");
            } else {
                html = ((ChromiumElement) htmlOrEle).getOwner().html();
            }
            htmlOrEle = Jsoup.parse(html);
            htmlOrEle = ((Document) htmlOrEle).selectXpath(xpath).get(0);
        } else if (htmlOrEle instanceof BasePage) { //各种页面对象
            page = (BasePage<?>) htmlOrEle;
            String html = ((BasePage<?>) htmlOrEle).html();
            if (html.startsWith("<?xml ")) {
                html = html.replaceAll("<\\?xml.*?>", "");
            }
            htmlOrEle = Jsoup.parse(html);
        } else if (htmlOrEle instanceof BaseElement) {
            page = ((BaseElement<?, ?>) htmlOrEle).getOwner();
            htmlOrEle = Jsoup.parse(((BaseElement<?, ?>) htmlOrEle).html());
        } else {
            throw new ClassCastException("html_or_ele参数只能是元素、页面对象或html文本。");
        }
        // ---------------执行查找-----------------
        Elements elements;
        //用lxml内置方法获取lxml的元素对象列表
        if (locs.getName().equals(BySelect.XPATH)) {
            elements = ((Element) htmlOrEle).selectXpath(locs.getValue());
        } else {
            elements = ((Element) htmlOrEle).select(locs.getValue());
        }
        //把lxml元素对象包装成SessionElement对象并按需要返回一个或全部
        ArrayList<SessionElement> sessionElements = new ArrayList<>();
        if (index != null) {
            int count = elements.size();
            if (count == 0 || Math.abs(index) > count) return null;
            if (index < 0) index = count + index + 1;
            Element ele = elements.get(index - 1);
            sessionElements.add(new SessionElement(ele, page));
        } else {
            for (Element element : elements) {
                sessionElements.add(new SessionElement(element, page));
            }
        }
        return sessionElements;
    }

    public Element innerEle() {
        return innerEle;
    }

    public String toString() {
        return "<SessionElement " + this.tag() + " " + JSON.toJSONString(this.attrs()) + '>';
    }

    /**
     * @return 返回元素类型
     */
    @Override
    public String tag() {
        return this.innerEle.tagName();
    }

    /**
     * @return 返回outerHTML文本
     */
    @Override
    public String html() {
        return this.innerEle.outerHtml();
    }

    /**
     * @return 返回元素innerHTML文本
     */
    public String innerHtml() {
        return this.innerEle.html();
    }

    /**
     * @return 返回元素所有属性及值
     */
    @Override
    public Map<String, String> attrs() {
        Attributes attributes = this.innerEle.attributes();
        Map<String, String> map = new HashMap<>();
        for (Attribute attribute : attributes) {
            String key = attribute.getKey();
            map.put(key, this.attr(key));
        }
        return map;
    }

    @Override
    public String text() {
        return Web.getEleTxt(this);
    }

    @Override
    public String rawText() {
        return this.innerEle.wholeText();
    }

    @Override
    public SessionElement sEle(By by, Integer index) {
        List<SessionElement> sessionElements = this._ele(by, null, index, false, null, "s_ele()");
        return !sessionElements.isEmpty() ? sessionElements.get(0) : null;
    }

    @Override
    public SessionElement sEle(String loc, Integer index) {
        List<SessionElement> sessionElements = this._ele(loc, null, index, false, null, "s_ele()");
        return !sessionElements.isEmpty() ? sessionElements.get(0) : null;
    }

    @Override
    public List<SessionElement> sEles(By by) {
        return this._ele(by, null, null, false, null, "s_eles()");
    }

    @Override
    public List<SessionElement> sEles(String loc) {
        return this._ele(loc, null, null, false, null, "s_eles()");
    }

    /**
     * @param by       查询元素
     * @param timeout  查找超时时间（秒） 无效参数
     * @param index    获取第几个，从0开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数  无效参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置 无效参数
     * @return SessionElement对象
     */
    @Override
    protected List<SessionElement> findElements(By by, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        return makeSessionEle(this, by, index);
    }

    /**
     * @param loc      查询元素
     * @param timeout  查找超时时间（秒）    无效参数
     * @param index    获取第几个，从0开始，可传入负数获取倒数第几个 如果是null则是返回全部
     * @param relative WebPage用的表示是否相对定位的参数 无效参数
     * @param raiseErr 找不到元素是是否抛出异常，为null时根据全局设置    无效参数
     * @return SessionElement对象
     */
    @Override
    protected List<SessionElement> findElements(String loc, Double timeout, Integer index, Boolean relative, Boolean raiseErr) {
        return makeSessionEle(this, loc, index);
    }

    @Override
    public String attr(String attr) {
        if (Objects.equals(attr, "href")) {
            String href = this.innerEle.attr("href");
            if (href.toLowerCase().startsWith("javascript:") || href.toLowerCase().startsWith("mailto:")) {
                return href;
            } else {
                return Web.makeAbsoluteLink(href, this.getOwner() != null ? this.getOwner().url() : null);
            }
        } else if (Objects.equals(attr, "src")) {
            return Web.makeAbsoluteLink(this.innerEle.attr("src"), this.getOwner() != null ? this.getOwner().url() : null);
        } else if (Objects.equals(attr, "text")) {
            return this.text();
        } else if (Objects.equals(attr, "innerText")) {
            return this.rawText();
        } else if (Objects.equals(attr, "html") || Objects.equals(attr, "outerHTML")) {
            return this.html();
        } else if (Objects.equals(attr, "innerHTML")) {
            return this.innerHtml();
        } else {
            return this.innerEle.attr(attr);
        }
    }

    @Override
    protected String getElePath(ElePathMode mode) {
        StringBuilder pathStr = new StringBuilder();
        SessionElement ele = this;
        int brothers;
        while (ele != null) {
            if ("css".equalsIgnoreCase(mode.getMode())) {
                brothers = ele.eles("xpath:./preceding-sibling::*").size();
                pathStr.insert(0, ">" + ele.tag() + ":nth-child(" + (brothers + 1) + ")");
            } else {
                brothers = ele.eles("xpath:./preceding-sibling::" + ele.tag()).size();
                pathStr = new StringBuilder(brothers > 0 ? "/" + ele.tag() + "[" + (brothers + 1) + "]" + pathStr : "/" + ele.tag() + pathStr);
            }
            ele = ele.parent();
        }
        return "css".equalsIgnoreCase(mode.getMode()) ? pathStr.substring(1) : pathStr.toString();
    }

}
