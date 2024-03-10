package com.ll.cssselectortoxpath.model;

import lombok.Getter;

@Getter
public class CssAttribute {
    private final String name;
    private final String value;
    private final CssAttributeValueType type;

    public CssAttribute(String nameIn, String valueIn, String typeStringIn) {
        this(nameIn, valueIn, CssAttributeValueType.valueTypeString(typeStringIn));

    }

    public CssAttribute(String nameIn, String valueIn, CssAttributeValueType typeIn) {
        this.name = nameIn;
        this.value = valueIn;
        this.type = typeIn;
    }

    @Override
    public String toString() {
        return "Name=" + this.name + "; Value=" + this.value + "; Type=" + this.type;
    }

    @Override
    public boolean equals(Object cssAttribute) {
        return cssAttribute instanceof CssElementAttributes && this.toString().equals(cssAttribute.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
