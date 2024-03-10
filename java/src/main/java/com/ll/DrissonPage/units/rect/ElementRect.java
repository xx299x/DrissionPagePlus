package com.ll.DrissonPage.units.rect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.element.ChromiumElement;
import com.ll.DrissonPage.units.Coordinate;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@AllArgsConstructor
public class ElementRect {
    private final ChromiumElement ele;

    /**
     * @return 返回元素四个角坐标，顺序：左上、右上、右下、左下
     */
    public List<Coordinate> corners() {
        String string = this.getViewportRect(RectParam.BORDER).toString();

        List<Integer> vr = JSONObject.parseArray(string, Integer.class);
        JSONObject viewport = JSON.parseObject(this.ele.getOwner().runCdpLoaded("Page.getLayoutMetrics").toString()).getJSONObject("visualViewport");
        Integer pageX = viewport.getInteger("pageX");
        Integer pageY = viewport.getInteger("pageY");
        return List.of(new Coordinate(vr.get(0) + pageX, vr.get(1) + pageY), new Coordinate(vr.get(2) + pageX, vr.get(3) + pageY), new Coordinate(vr.get(4) + pageX, vr.get(5) + pageY), new Coordinate(vr.get(6) + pageX, vr.get(7) + pageY));
    }

    /**
     * @return 返回元素四个角视口坐标，顺序：左上、右上、右下、左下
     */
    public List<Coordinate> viewportCorners() {
        List<Integer> vr = JSONObject.parseArray(this.getViewportRect(RectParam.BORDER).toString(), Integer.class);
        return List.of(new Coordinate(vr.get(0), vr.get(1)), new Coordinate(vr.get(2), vr.get(3)), new Coordinate(vr.get(4), vr.get(5)), new Coordinate(vr.get(6), vr.get(7)));
    }

    /**
     * @return 返回元素大小，格式(长, 高)
     */
    public Coordinate size() {
        JSONArray jsonArray = JSON.parseObject(this.ele.getOwner().runCdp("DOM.getBoxModel", Map.of("backendNodeId", this.ele.getBackendId(), "nodeId", this.ele.getNodeId(), "objectId", this.ele.getObjId())).toString()).getJSONObject("model").getJSONArray("border");
        return new Coordinate(jsonArray.getInteger(2) - jsonArray.getInteger(0), jsonArray.getInteger(5) - jsonArray.getInteger(1));
    }

    /**
     * @return 返回元素左上角的绝对坐标
     */
    public Coordinate location() {
        Coordinate coordinate = this.viewportLocation();
        return this.getPageCoord(coordinate.getX(), coordinate.getY());
    }

    /**
     * @return 返回元素中间点的绝对坐标
     */
    public Coordinate midpoint() {
        Coordinate coordinate = this.viewportMidpoint();
        return this.getPageCoord(coordinate.getX(), coordinate.getY());
    }

    /**
     * @return 返回元素接受点击的点的绝对坐标
     */
    public Coordinate clickPoint() {
        Coordinate coordinate = this.viewportClickPoint();
        return this.getPageCoord(coordinate.getX(), coordinate.getY());
    }

    /**
     * @return 返回元素左上角在视口中的坐标
     */
    public Coordinate viewportLocation() {
        JSONArray objects = JSON.parseArray(this.getViewportRect(RectParam.BORDER).toString());
        return new Coordinate(objects.getInteger(0), objects.getInteger(1));
    }

    /**
     * @return 返回元素中间点在视口中的坐标
     */
    public Coordinate viewportMidpoint() {
        JSONArray objects = JSON.parseArray(this.getViewportRect(RectParam.BORDER).toString());
        return new Coordinate(objects.getInteger(0) + (objects.getInteger(2) - objects.getInteger(0)) / 2, objects.getInteger(3) + (objects.getInteger(5) - objects.getInteger(3)) / 2);
    }

    /**
     * @return 返回元素接受点击的点视口坐标
     */

    public Coordinate viewportClickPoint() {
        return new Coordinate(this.viewportMidpoint().getX(), JSON.parseArray(this.getViewportRect(RectParam.PADDING).toString()).getInteger(1) + 3);
    }

    /**
     * @return 返回元素左上角在屏幕上坐标，左上角为(0, 0)
     */
    public Coordinate screenLocation() {
        Coordinate v = this.ele.getOwner().rect().viewportLocation();
        Coordinate x = this.viewportLocation();
        Integer pr = JSONObject.parseObject(this.ele.getOwner().runJs("return window.devicePixelRatio;").toString(), Integer.class);
        return new Coordinate((v.getX() + x.getX()) * pr, (v.getY() + x.getY()) * pr);
    }

    /**
     * @return 返回元素中点在屏幕上坐标，左上角为(0, 0)
     */
    public Coordinate screenMidpoint() {
        Coordinate v = this.ele.getOwner().rect().viewportLocation();
        Coordinate x = this.viewportMidpoint();
        Integer pr = JSONObject.parseObject(this.ele.getOwner().runJs("return window.devicePixelRatio;").toString(), Integer.class);
        return new Coordinate((v.getX() + x.getX()) * pr, (v.getY() + x.getY()) * pr);
    }

    /**
     * @return 返回元素中点在屏幕上坐标，左上角为(0, 0)
     */
    public Coordinate screenClickPoint() {
        Coordinate v = this.ele.getOwner().rect().viewportLocation();
        Coordinate x = this.viewportClickPoint();
        Integer pr = JSONObject.parseObject(this.ele.getOwner().runJs("return window.devicePixelRatio;").toString(), Integer.class);
        return new Coordinate((v.getX() + x.getX()) * pr, (v.getY() + x.getY()) * pr);
    }

    /**
     * 按照类型返回在可视窗口中的范围
     *
     * @param rect 方框类型，margin border padding
     * @return 四个角坐标
     */
    private Object getViewportRect(RectParam rect) {
        JSONObject jsonObject = JSON.parseObject(this.ele.getOwner().runCdp("DOM.getBoxModel", Map.of("backendNodeId", this.ele.getBackendId(), "nodeId", this.ele.getNodeId(), "objectId", this.ele.getObjId())).toString());
        return jsonObject.getJSONObject("model").get(rect.value);
    }

    /**
     * @param x x坐标
     * @param y y坐标
     * @return 根据视口坐标获取绝对坐标
     */
    private Coordinate getPageCoord(Integer x, Integer y) {
        JSONObject jsonObject = JSON.parseObject(this.ele.getOwner().runCdpLoaded("Page.getLayoutMetrics").toString()).getJSONObject("visualViewport");
        Integer pageX = jsonObject.getInteger("pageX");
        Integer pageY = jsonObject.getInteger("pageY");
        return new Coordinate(x + pageX, y + pageY);
    }


    public enum RectParam {
        MARGIN("margin"), BORDER("border"), PADDING("padding");
        private final String value;

        RectParam(String value) {
            this.value = value;
        }
    }
}
