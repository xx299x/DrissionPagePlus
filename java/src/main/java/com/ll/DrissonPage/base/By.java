package com.ll.DrissonPage.base;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */

@Getter
@Setter
public class By {
    private BySelect name;
    private String value;

    private By(BySelect name, String value) {

        this.name = name;
        this.value = value;
    }

    public static By NULL() {
        return null;
    }

    public static By id(String value) {
        return new By(BySelect.ID, Objects.requireNonNullElse(value, "Cannot find elements when id is null."));
    }

    public static By className(String value) {
        return new By(BySelect.CLASS_NAME, Objects.requireNonNullElse(value, "Cannot find elements when the class name expression is null."));
    }

    public static By tag(String value) {
        return new By(BySelect.TAG_NAME, Objects.requireNonNullElse(value, "Cannot find elements when the tag name is null."));
    }

    public static By name(String value) {
        return new By(BySelect.NAME, Objects.requireNonNullElse(value, "Cannot find elements when name text is null."));
    }

    public static By css(String value) {
        return new By(BySelect.CSS_SELECTOR, Objects.requireNonNullElse(value, "Cannot find elements when the css selector is null."));
    }

    public static By xpath(String value) {
        return new By(BySelect.XPATH, Objects.requireNonNullElse(value, "Cannot find elements when the XPath is null."));
    }

    public static By linkText(String value) {
        return new By(BySelect.LINK_TEXT, Objects.requireNonNullElse(value, "Cannot find elements when the link text is null."));
    }

    public static By partialLinkText(String value) {
        return new By(BySelect.PARTIAL_LINK_TEXT, Objects.requireNonNullElse(value, "Cannot find elements when the partial link text is null."));
    }

    public static By text(String value) {
        return new By(BySelect.TEXT, Objects.requireNonNullElse(value, "Cannot find elements when the text is null."));
    }

    public static By partialText(String value) {
        return new By(BySelect.PARTIAL_TEXT, Objects.requireNonNullElse(value, "Cannot find elements when the partial text is null."));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof By)) return false;
        By by = (By) o;
        return Objects.equals(getName().getName(), by.getName().getName()) && Objects.equals(getValue(), by.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getValue());
    }

    @Override
    public String toString() {
        return "By{" +
               "name=" + name.getName() +
               ", value='" + value + '\'' +
               '}';
    }


}
