package com.ll.cssselectortoxpath.model;

import lombok.Getter;

public enum CssCombinatorType {
    SPACE(' ', "//"),
    PLUS('+', "/following-sibling::*[1]/self::"),
    GREATER_THAN('>', "/"),
    TILDA('~', "/following-sibling::");

    private final char typeChar;
    @Getter
    private final String xpath;

    private CssCombinatorType(char typeCharIn, String xpathIn) {
        this.typeChar = typeCharIn;
        this.xpath = xpathIn;
    }

    public static CssCombinatorType combinatorTypeChar(String unknownString) {
        if (unknownString == null) {
            return null;
        }

        switch (unknownString) {
            case " ":
                return SPACE;
            case "+":
                return PLUS;
            case ">":
                return GREATER_THAN;
            case "~":
                return TILDA;
            default:
                throw new IllegalArgumentException(unknownString);
        }
    }

    public char getCombinatorChar() {
        return typeChar;
    }

}
