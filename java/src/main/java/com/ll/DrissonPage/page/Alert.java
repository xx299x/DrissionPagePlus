package com.ll.DrissonPage.page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    private Boolean activated = false;
    private String text = null;
    private String type = null;
    private String defaultPrompt = null;
    private String responseAccept = null;
    private String responseText = null;
    private Boolean handleNext = null;
    private String nextText = null;
    private Boolean auto = null;
}
