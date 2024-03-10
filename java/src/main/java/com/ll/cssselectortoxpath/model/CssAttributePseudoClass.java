package com.ll.cssselectortoxpath.model;

public class CssAttributePseudoClass extends CssAttribute {
    private final CssPsuedoClassType pseudoClassType;
    private final String element;
    private final String parenthesisExpression;

    public CssAttributePseudoClass(CssPsuedoClassType pseudoClassTypeIn, String elementIn, String parenthesisExpressionIn) {
        super(null, null, (CssAttributeValueType) null);
        pseudoClassType = pseudoClassTypeIn;
        element = elementIn;
        parenthesisExpression = parenthesisExpressionIn;
    }

    public String getXPath() {
        return pseudoClassType.getXpath(element, parenthesisExpression);

    }

    public CssPsuedoClassType getCssPsuedoClassType() {
        return pseudoClassType;
    }

    @Override
    public String toString() {
        return "Pseudo Class = " + pseudoClassType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof CssAttributePseudoClass)) {
            return false;
        }
        CssAttributePseudoClass obj = (CssAttributePseudoClass) o;
        if (this.parenthesisExpression == null) {
            if (obj.parenthesisExpression != null) {
                return false;
            }
        } else if (!this.parenthesisExpression.equals(obj.parenthesisExpression)) {
            return false;
        }
        return this.pseudoClassType.equals(obj.pseudoClassType);
    }

}
