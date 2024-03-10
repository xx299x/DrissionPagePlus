package com.ll.cssselectortoxpath.model;


import lombok.Getter;

import java.util.ArrayList;
import java.util.List;


@Getter
public class CssElementAttributes {
    private final String element;
    private final List<CssAttribute> cssAttributeList;

    public CssElementAttributes(String elementIn, List<CssAttribute> cssAttributeListIn) {
        this.element = elementIn;
        this.cssAttributeList = new ArrayList<>(cssAttributeListIn);
    }

    @Override
    public String toString() {
        return "Element=" + this.element + ", CssAttributeList=" + this.cssAttributeList;
    }

    @Override
    public boolean equals(Object cssElementAttributes) {
        return cssElementAttributes instanceof CssElementAttributes && this.toString().equals(cssElementAttributes.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}