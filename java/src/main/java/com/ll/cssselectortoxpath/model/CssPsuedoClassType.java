package com.ll.cssselectortoxpath.model;


import com.ll.cssselectortoxpath.utilities.CssSelectorToXPathConverterException;
import com.ll.cssselectortoxpath.utilities.CssSelectorToXPathConverterInvalidFirstLastOnlyOfType;
import com.ll.cssselectortoxpath.utilities.CssSelectorToXpathConverterInvalidNthOfType;

public enum CssPsuedoClassType {

    EMPTY(":empty", (e, p) -> "[not(*) and .=\"\"]"),
    NTH_OF_TYPE(":nth-of-type", new CssPseudoClassNthToXpath(false)),
    NTH_LAST_OF_TYPE(":nth-last-of-type", new CssPseudoClassNthToXpath(true)),
    FIRST_OF_TYPE(":first-of-type", (e, p) -> NTH_OF_TYPE.getXpath(e, "1")),
    LAST_OF_TYPE(":last-of-type", (e, p) -> NTH_LAST_OF_TYPE.getXpath(e, "1")),
    ONLY_OF_TYPE(":only-of-type", (e, p) -> FIRST_OF_TYPE.getXpath(e, p) + LAST_OF_TYPE.getXpath(e, p)),
    NTH_CHILD(":nth-child", new CssPseudoClassNthChildToXpath(false)),
    NTH_LAST_CHILD(":nth-last-child", new CssPseudoClassNthChildToXpath(true)),
    FIRST_CHILD(":first-child", (e, p) -> NTH_CHILD.getXpath(e, "1")),
    LAST_CHILD(":last-child", (e, p) -> NTH_LAST_CHILD.getXpath(e, "1")),
    ONLY_CHILD(":only-child", (e, p) -> FIRST_CHILD.getXpath("", null) + LAST_CHILD.getXpath("", null));


    private final String typeString;
    private final CssPseudoClassToXpath toXpath;

    private CssPsuedoClassType(String typeStringIn, CssPseudoClassToXpath toXpathIn) {
        this.typeString = typeStringIn;
        this.toXpath = toXpathIn;
    }

    public static CssPsuedoClassType pseudoClassTypeString(String unknownString, String element, String parenthesisExpression) throws CssSelectorToXPathConverterException {
        if (unknownString == null) {
            return null;
        }
        switch (unknownString) {
            case ":empty":
                return EMPTY;
            case ":first-of-type":
                return getOfType(FIRST_OF_TYPE, element);
            case ":last-of-type":
                return getOfType(LAST_OF_TYPE, element);
            case ":only-of-type":
                return getOfType(ONLY_OF_TYPE, element);
            case ":nth-of-type":
                return getOfType(NTH_OF_TYPE, element, parenthesisExpression);
            case ":nth-last-of-type":
                return getOfType(NTH_LAST_OF_TYPE, element, parenthesisExpression);
            case ":nth-child":
                return getOfType(NTH_CHILD, element, parenthesisExpression);
            case ":nth-last-child":
                return getOfType(NTH_LAST_CHILD, element, parenthesisExpression);
            case ":first-child":
                return FIRST_CHILD;
            case ":last-child":
                return LAST_CHILD;
            case ":only-child":
                return ONLY_CHILD;
            default:
                throw new IllegalArgumentException(unknownString);
        }
    }

    private static CssPsuedoClassType getOfType(CssPsuedoClassType ofType, String element) throws CssSelectorToXPathConverterInvalidFirstLastOnlyOfType {
        if (element == null || element.equals("*")) {
            throw new CssSelectorToXPathConverterInvalidFirstLastOnlyOfType();
        } else {
            return ofType;
        }
    }

    private static CssPsuedoClassType getOfType(CssPsuedoClassType ofType, String element, String parenthesisExpression) throws CssSelectorToXPathConverterException {
        if (element == null || element.equals("*")) {
            throw new CssSelectorToXPathConverterInvalidFirstLastOnlyOfType();
        } else {
            String positiveN = "^[+]?([0]*[1-9][0-9]*)?n([+-][0-9]+)?$";
            String negativeN = "^[-][0-9]*n[+]([0]*[1-9][0-9]*)$";
            String noN = "^[+]?([1-9][0-9]*)$";

            String nthOfTypeRe = "odd|even|" + positiveN + "|" + negativeN + "|" + noN;
            if (parenthesisExpression.matches(nthOfTypeRe)) {
                return ofType;
            } else {
                throw new CssSelectorToXpathConverterInvalidNthOfType(parenthesisExpression);
            }
        }
    }

    public String getPsuedoString() {
        return typeString;
    }

    public String getXpath(String element, String parenthesisExpression) {
        return toXpath.getXpath(element, parenthesisExpression);
    }
}

//Algorithm for :nth-of-type():
//	Given: div:nth-of-type(xn+y)   
//	
//	Case 1: x=0 
//		//div[y]
//	
//	Case 2: x>0 and y=0 
//		//div[((count(preceding-sibling::div)+1) mod x)=0]
//	
//	Case 3:	x>=1 and y==x 
//		Equivalent to div:nth-of-type(xn)
//
//	Case 4:	x>=1 and y<x 
//		//div[(count(preceding-sibling::div)=(y-1)) or (((count(preceding-sibling::div)-(y-1)) mod x)=0)]
//	
//	Case 5:	x>=1 and y>x 
//		//div[(count(preceding-sibling::div)=(y-1)) or (((count(preceding-sibling::div)>y) and (((count(preceding-sibling::div)-(y-1)) mod x)=0)))]
//
//	Case 6:	x>0  and y<0
//		if (abs(y)<=x) let Y=abs(y)
//		else Y=abs(y) mod x
//		let YY=x-Y, note this is greater or equal to zero
//		same as  :nth-of-type(xn+(YY))
//
//	Case 7: x<0 and y>0
//		let X=abs(x)
//		if (y<=X) then //div[y]
//		else Y= (y mod X)
//		let YY=X-Y, note this is greater or equal to zero
//		nth-of-type(Xn+(YY)) = div[q]
//		then solution is div[(q) and (count(preceding-sibling::div)<y) ]

