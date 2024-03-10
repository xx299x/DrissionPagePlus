package com.ll.cssselectortoxpath.model;

public class CssPseudoClassNthChildToXpath extends CssPseudoClassNthToXpath {


    public CssPseudoClassNthChildToXpath(boolean lastIn) {
        super(lastIn);
    }

    @Override
    public String getNthToXpath(String element, String parenthesisExpression) {
        return super.getNthToXpath("*", parenthesisExpression);
    }


}
