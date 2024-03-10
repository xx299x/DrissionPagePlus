package com.ll.cssselectortoxpath;

import com.ll.cssselectortoxpath.utilities.CssElementCombinatorPairsToXpath;
import com.ll.cssselectortoxpath.utilities.CssSelectorToXPathConverterException;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class CssToXpath {
    public static String convertCssSelectorToXpath(String cssSelector) {
        try {
            return new CssElementCombinatorPairsToXpath().convertCssSelectorStringToXpathString(cssSelector);
        } catch (CssSelectorToXPathConverterException e) {
            e.printStackTrace();
            return cssSelector;
        }
    }
}
