package com.ll.cssselectortoxpath.model;


import com.ll.cssselectortoxpath.utilities.CssElementAttributeParser;
import com.ll.cssselectortoxpath.utilities.CssSelectorToXPathConverterException;
import lombok.Getter;

@Getter
public class CssElementCombinatorPair {
    private final CssCombinatorType combinatorType;
    private final CssElementAttributes cssElementAttributes;

    public CssElementCombinatorPair(CssCombinatorType combinatorTypeIn, String cssElementAttributesStringIn) throws CssSelectorToXPathConverterException {
        this.combinatorType = combinatorTypeIn;
        this.cssElementAttributes = new CssElementAttributeParser().createElementAttribute(cssElementAttributesStringIn);
    }

    @Override
    public String toString() {
        return "(Combinator=" + this.getCombinatorType() + ", " + this.cssElementAttributes + ")";
    }

    @Override
    public boolean equals(Object cssElementCombinatorPair) {
        return cssElementCombinatorPair instanceof CssElementCombinatorPair && this.toString().equals(cssElementCombinatorPair.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
