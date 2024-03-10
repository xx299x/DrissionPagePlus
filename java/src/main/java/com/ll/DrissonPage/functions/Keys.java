package com.ll.DrissonPage.functions;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ll.DrissonPage.error.extend.AlertExistsError;
import com.ll.DrissonPage.page.ChromiumBase;
import lombok.AllArgsConstructor;

import java.util.*;

/**
 * @author 陆
 * @address <a href="https://t.me/blanksig"/>click
 */
public class Keys {

    public static final Map<String, String> K;
    public static final JSONObject KEY_DEFINITIONS = JSON.parseObject("{\"0\": {\"keyCode\": 48, \"key\": \"0\", \"code\": \"Digit0\"}, \"1\": {\"keyCode\": 49, \"key\": \"1\", \"code\": \"Digit1\"}, \"2\": {\"keyCode\": 50, \"key\": \"2\", \"code\": \"Digit2\"}, \"3\": {\"keyCode\": 51, \"key\": \"3\", \"code\": \"Digit3\"}, \"4\": {\"keyCode\": 52, \"key\": \"4\", \"code\": \"Digit4\"}, \"5\": {\"keyCode\": 53, \"key\": \"5\", \"code\": \"Digit5\"}, \"6\": {\"keyCode\": 54, \"key\": \"6\", \"code\": \"Digit6\"}, \"7\": {\"keyCode\": 55, \"key\": \"7\", \"code\": \"Digit7\"}, \"8\": {\"keyCode\": 56, \"key\": \"8\", \"code\": \"Digit8\"}, \"9\": {\"keyCode\": 57, \"key\": \"9\", \"code\": \"Digit9\"}, \"Power\": {\"key\": \"Power\", \"code\": \"Power\"}, \"Eject\": {\"key\": \"Eject\", \"code\": \"Eject\"}, \"\\ue001\": {\"keyCode\": 3, \"code\": \"Abort\", \"key\": \"Cancel\"}, \"\\ue002\": {\"keyCode\": 6, \"code\": \"Help\", \"key\": \"Help\"}, \"\\ue003\": {\"keyCode\": 8, \"code\": \"Backspace\", \"key\": \"Backspace\"}, \"\\ue004\": {\"keyCode\": 9, \"code\": \"Tab\", \"key\": \"Tab\"}, \"\\ue005\": {\"keyCode\": 12, \"shiftKeyCode\": 101, \"key\": \"Clear\", \"code\": \"Numpad5\", \"shiftKey\": \"5\", \"location\": 3}, \"\\ue006\": {\"keyCode\": 13, \"code\": \"NumpadEnter\", \"key\": \"Enter\", \"text\": \"\\r\", \"location\": 3}, \"\\ue007\": {\"keyCode\": 13, \"code\": \"Enter\", \"key\": \"Enter\", \"text\": \"\\r\"}, \"\\r\": {\"keyCode\": 13, \"code\": \"Enter\", \"key\": \"Enter\", \"text\": \"\\r\"}, \"\\n\": {\"keyCode\": 13, \"code\": \"Enter\", \"key\": \"Enter\", \"text\": \"\\r\"}, \"\\ue008\": {\"keyCode\": 16, \"code\": \"ShiftLeft\", \"key\": \"Shift\", \"location\": 1}, \"\\ue009\": {\"keyCode\": 17, \"code\": \"ControlLeft\", \"key\": \"Control\", \"location\": 1}, \"\\ue00a\": {\"keyCode\": 18, \"code\": \"AltLeft\", \"key\": \"Alt\", \"location\": 1}, \"\\ue00b\": {\"keyCode\": 19, \"code\": \"Pause\", \"key\": \"Pause\"}, \"CapsLock\": {\"keyCode\": 20, \"code\": \"CapsLock\", \"key\": \"CapsLock\"}, \"\\ue00c\": {\"keyCode\": 27, \"code\": \"Escape\", \"key\": \"Escape\"}, \"Convert\": {\"keyCode\": 28, \"code\": \"Convert\", \"key\": \"Convert\"}, \"NonConvert\": {\"keyCode\": 29, \"code\": \"NonConvert\", \"key\": \"NonConvert\"}, \"\\ue00d\": {\"keyCode\": 32, \"code\": \"Space\", \"key\": \" \"}, \"\\ue00e\": {\"keyCode\": 33, \"code\": \"PageUp\", \"key\": \"PageUp\"}, \"\\ue00f\": {\"keyCode\": 34, \"code\": \"PageDown\", \"key\": \"PageDown\"}, \"\\ue010\": {\"keyCode\": 35, \"code\": \"End\", \"key\": \"End\"}, \"\\ue011\": {\"keyCode\": 36, \"code\": \"Home\", \"key\": \"Home\"}, \"\\ue012\": {\"keyCode\": 37, \"code\": \"ArrowLeft\", \"key\": \"ArrowLeft\"}, \"\\ue013\": {\"keyCode\": 38, \"code\": \"ArrowUp\", \"key\": \"ArrowUp\"}, \"\\ue014\": {\"keyCode\": 39, \"code\": \"ArrowRight\", \"key\": \"ArrowRight\"}, \"\\ue015\": {\"keyCode\": 40, \"code\": \"ArrowDown\", \"key\": \"ArrowDown\"}, \"Select\": {\"keyCode\": 41, \"code\": \"Select\", \"key\": \"Select\"}, \"Open\": {\"keyCode\": 43, \"code\": \"Open\", \"key\": \"Execute\"}, \"PrintScreen\": {\"keyCode\": 44, \"code\": \"PrintScreen\", \"key\": \"PrintScreen\"}, \"\\ue016\": {\"keyCode\": 45, \"code\": \"Insert\", \"key\": \"Insert\"}, \"\\ue017\": {\"keyCode\": 46, \"code\": \"Delete\", \"key\": \"Delete\"}, \"\\ue028\": {\"keyCode\": 46, \"shiftKeyCode\": 110, \"code\": \"NumpadDecimal\", \"key\": \"\\u0000\", \"shiftKey\": \".\", \"location\": 3}, \"\\ue01a\": {\"keyCode\": 48, \"code\": \"Digit0\", \"shiftKey\": \")\", \"key\": \"0\"}, \"\\ue01b\": {\"keyCode\": 49, \"code\": \"Digit1\", \"shiftKey\": \"!\", \"key\": \"1\"}, \"\\ue01c\": {\"keyCode\": 50, \"code\": \"Digit2\", \"shiftKey\": \"@\", \"key\": \"2\"}, \"\\ue01d\": {\"keyCode\": 51, \"code\": \"Digit3\", \"shiftKey\": \"#\", \"key\": \"3\"}, \"\\ue01e\": {\"keyCode\": 52, \"code\": \"Digit4\", \"shiftKey\": \"$\", \"key\": \"4\"}, \"\\ue01f\": {\"keyCode\": 53, \"code\": \"Digit5\", \"shiftKey\": \"%\", \"key\": \"5\"}, \"\\ue020\": {\"keyCode\": 54, \"code\": \"Digit6\", \"shiftKey\": \"^\", \"key\": \"6\"}, \"\\ue021\": {\"keyCode\": 55, \"code\": \"Digit7\", \"shiftKey\": \"&\", \"key\": \"7\"}, \"\\ue022\": {\"keyCode\": 56, \"code\": \"Digit8\", \"shiftKey\": \"*\", \"key\": \"8\"}, \"\\ue023\": {\"keyCode\": 57, \"code\": \"Digit9\", \"shiftKey\": \"\\\\(\", \"key\": \"9\"}, \"KeyA\": {\"keyCode\": 65, \"code\": \"KeyA\", \"shiftKey\": \"A\", \"key\": \"a\"}, \"KeyB\": {\"keyCode\": 66, \"code\": \"KeyB\", \"shiftKey\": \"B\", \"key\": \"b\"}, \"KeyC\": {\"keyCode\": 67, \"code\": \"KeyC\", \"shiftKey\": \"C\", \"key\": \"c\"}, \"KeyD\": {\"keyCode\": 68, \"code\": \"KeyD\", \"shiftKey\": \"D\", \"key\": \"d\"}, \"KeyE\": {\"keyCode\": 69, \"code\": \"KeyE\", \"shiftKey\": \"E\", \"key\": \"e\"}, \"KeyF\": {\"keyCode\": 70, \"code\": \"KeyF\", \"shiftKey\": \"F\", \"key\": \"f\"}, \"KeyG\": {\"keyCode\": 71, \"code\": \"KeyG\", \"shiftKey\": \"G\", \"key\": \"g\"}, \"KeyH\": {\"keyCode\": 72, \"code\": \"KeyH\", \"shiftKey\": \"H\", \"key\": \"h\"}, \"KeyI\": {\"keyCode\": 73, \"code\": \"KeyI\", \"shiftKey\": \"I\", \"key\": \"i\"}, \"KeyJ\": {\"keyCode\": 74, \"code\": \"KeyJ\", \"shiftKey\": \"J\", \"key\": \"j\"}, \"KeyK\": {\"keyCode\": 75, \"code\": \"KeyK\", \"shiftKey\": \"K\", \"key\": \"k\"}, \"KeyL\": {\"keyCode\": 76, \"code\": \"KeyL\", \"shiftKey\": \"L\", \"key\": \"l\"}, \"KeyM\": {\"keyCode\": 77, \"code\": \"KeyM\", \"shiftKey\": \"M\", \"key\": \"m\"}, \"KeyN\": {\"keyCode\": 78, \"code\": \"KeyN\", \"shiftKey\": \"N\", \"key\": \"n\"}, \"KeyO\": {\"keyCode\": 79, \"code\": \"KeyO\", \"shiftKey\": \"O\", \"key\": \"o\"}, \"KeyP\": {\"keyCode\": 80, \"code\": \"KeyP\", \"shiftKey\": \"P\", \"key\": \"p\"}, \"KeyQ\": {\"keyCode\": 81, \"code\": \"KeyQ\", \"shiftKey\": \"Q\", \"key\": \"q\"}, \"KeyR\": {\"keyCode\": 82, \"code\": \"KeyR\", \"shiftKey\": \"R\", \"key\": \"r\"}, \"KeyS\": {\"keyCode\": 83, \"code\": \"KeyS\", \"shiftKey\": \"S\", \"key\": \"s\"}, \"KeyT\": {\"keyCode\": 84, \"code\": \"KeyT\", \"shiftKey\": \"T\", \"key\": \"t\"}, \"KeyU\": {\"keyCode\": 85, \"code\": \"KeyU\", \"shiftKey\": \"U\", \"key\": \"u\"}, \"KeyV\": {\"keyCode\": 86, \"code\": \"KeyV\", \"shiftKey\": \"V\", \"key\": \"v\"}, \"KeyW\": {\"keyCode\": 87, \"code\": \"KeyW\", \"shiftKey\": \"W\", \"key\": \"w\"}, \"KeyX\": {\"keyCode\": 88, \"code\": \"KeyX\", \"shiftKey\": \"X\", \"key\": \"x\"}, \"KeyY\": {\"keyCode\": 89, \"code\": \"KeyY\", \"shiftKey\": \"Y\", \"key\": \"y\"}, \"KeyZ\": {\"keyCode\": 90, \"code\": \"KeyZ\", \"shiftKey\": \"Z\", \"key\": \"z\"}, \"MetaLeft\": {\"keyCode\": 91, \"code\": \"MetaLeft\", \"key\": \"Meta\"}, \"MetaRight\": {\"keyCode\": 92, \"code\": \"MetaRight\", \"key\": \"Meta\"}, \"ContextMenu\": {\"keyCode\": 93, \"code\": \"ContextMenu\", \"key\": \"ContextMenu\"}, \"\\ue024\": {\"keyCode\": 106, \"code\": \"NumpadMultiply\", \"key\": \"*\", \"location\": 3}, \"\\ue025\": {\"keyCode\": 107, \"code\": \"NumpadAdd\", \"key\": \"+\", \"location\": 3}, \"\\ue027\": {\"keyCode\": 109, \"code\": \"NumpadSubtract\", \"key\": \"-\", \"location\": 3}, \"\\ue029\": {\"keyCode\": 111, \"code\": \"NumpadDivide\", \"key\": \"/\", \"location\": 3}, \"\\ue031\": {\"keyCode\": 112, \"code\": \"F1\", \"key\": \"F1\"}, \"\\ue032\": {\"keyCode\": 113, \"code\": \"F2\", \"key\": \"F2\"}, \"\\ue033\": {\"keyCode\": 114, \"code\": \"F3\", \"key\": \"F3\"}, \"\\ue034\": {\"keyCode\": 115, \"code\": \"F4\", \"key\": \"F4\"}, \"\\ue035\": {\"keyCode\": 116, \"code\": \"F5\", \"key\": \"F5\"}, \"\\ue036\": {\"keyCode\": 117, \"code\": \"F6\", \"key\": \"F6\"}, \"\\ue037\": {\"keyCode\": 118, \"code\": \"F7\", \"key\": \"F7\"}, \"\\ue038\": {\"keyCode\": 119, \"code\": \"F8\", \"key\": \"F8\"}, \"\\ue039\": {\"keyCode\": 120, \"code\": \"F9\", \"key\": \"F9\"}, \"\\ue03a\": {\"keyCode\": 121, \"code\": \"F10\", \"key\": \"F10\"}, \"\\ue03b\": {\"keyCode\": 122, \"code\": \"F11\", \"key\": \"F11\"}, \"\\ue03c\": {\"keyCode\": 123, \"code\": \"F12\", \"key\": \"F12\"}, \"F13\": {\"keyCode\": 124, \"code\": \"F13\", \"key\": \"F13\"}, \"F14\": {\"keyCode\": 125, \"code\": \"F14\", \"key\": \"F14\"}, \"F15\": {\"keyCode\": 126, \"code\": \"F15\", \"key\": \"F15\"}, \"F16\": {\"keyCode\": 127, \"code\": \"F16\", \"key\": \"F16\"}, \"F17\": {\"keyCode\": 128, \"code\": \"F17\", \"key\": \"F17\"}, \"F18\": {\"keyCode\": 129, \"code\": \"F18\", \"key\": \"F18\"}, \"F19\": {\"keyCode\": 130, \"code\": \"F19\", \"key\": \"F19\"}, \"F20\": {\"keyCode\": 131, \"code\": \"F20\", \"key\": \"F20\"}, \"F21\": {\"keyCode\": 132, \"code\": \"F21\", \"key\": \"F21\"}, \"F22\": {\"keyCode\": 133, \"code\": \"F22\", \"key\": \"F22\"}, \"F23\": {\"keyCode\": 134, \"code\": \"F23\", \"key\": \"F23\"}, \"F24\": {\"keyCode\": 135, \"code\": \"F24\", \"key\": \"F24\"}, \"NumLock\": {\"keyCode\": 144, \"code\": \"NumLock\", \"key\": \"NumLock\"}, \"ScrollLock\": {\"keyCode\": 145, \"code\": \"ScrollLock\", \"key\": \"ScrollLock\"}, \"AudioVolumeMute\": {\"keyCode\": 173, \"code\": \"AudioVolumeMute\", \"key\": \"AudioVolumeMute\"}, \"AudioVolumeDown\": {\"keyCode\": 174, \"code\": \"AudioVolumeDown\", \"key\": \"AudioVolumeDown\"}, \"AudioVolumeUp\": {\"keyCode\": 175, \"code\": \"AudioVolumeUp\", \"key\": \"AudioVolumeUp\"}, \"MediaTrackNext\": {\"keyCode\": 176, \"code\": \"MediaTrackNext\", \"key\": \"MediaTrackNext\"}, \"MediaTrackPrevious\": {\"keyCode\": 177, \"code\": \"MediaTrackPrevious\", \"key\": \"MediaTrackPrevious\"}, \"MediaStop\": {\"keyCode\": 178, \"code\": \"MediaStop\", \"key\": \"MediaStop\"}, \"MediaPlayPause\": {\"keyCode\": 179, \"code\": \"MediaPlayPause\", \"key\": \"MediaPlayPause\"}, \"\\ue018\": {\"keyCode\": 186, \"code\": \"Semicolon\", \"shiftKey\": \":\", \"key\": \";\"}, \"Equal\": {\"keyCode\": 187, \"code\": \"Equal\", \"shiftKey\": \"+\", \"key\": \"=\"}, \"\\ue019\": {\"keyCode\": 187, \"code\": \"NumpadEqual\", \"key\": \"=\", \"location\": 3}, \"Comma\": {\"keyCode\": 188, \"code\": \"Comma\", \"shiftKey\": \"<\", \"key\": \",\"}, \"Minus\": {\"keyCode\": 189, \"code\": \"Minus\", \"shiftKey\": \"_\", \"key\": \"-\"}, \"Period\": {\"keyCode\": 190, \"code\": \"Period\", \"shiftKey\": \">\", \"key\": \".\"}, \"Slash\": {\"keyCode\": 191, \"code\": \"Slash\", \"shiftKey\": \"?\", \"key\": \"/\"}, \"Backquote\": {\"keyCode\": 192, \"code\": \"Backquote\", \"shiftKey\": \"~\", \"key\": \"`\"}, \"BracketLeft\": {\"keyCode\": 219, \"code\": \"BracketLeft\", \"shiftKey\": \"{\", \"key\": \"[\"}, \"Backslash\": {\"keyCode\": 220, \"code\": \"Backslash\", \"shiftKey\": \"|\", \"key\": \"\\\\\"}, \"BracketRight\": {\"keyCode\": 221, \"code\": \"BracketRight\", \"shiftKey\": \"}\", \"key\": \"]\"}, \"Quote\": {\"keyCode\": 222, \"code\": \"Quote\", \"shiftKey\": \"\\\"\", \"key\": \"'\"}, \"AltGraph\": {\"keyCode\": 225, \"code\": \"AltGraph\", \"key\": \"AltGraph\"}, \"Props\": {\"keyCode\": 247, \"code\": \"Props\", \"key\": \"CrSel\"}, \"Cancel\": {\"keyCode\": 3, \"key\": \"Cancel\", \"code\": \"Abort\"}, \"Clear\": {\"keyCode\": 12, \"key\": \"Clear\", \"code\": \"Numpad5\", \"location\": 3}, \"Shift\": {\"keyCode\": 16, \"key\": \"Shift\", \"code\": \"ShiftLeft\"}, \"Control\": {\"keyCode\": 17, \"key\": \"Control\", \"code\": \"ControlLeft\"}, \"Alt\": {\"keyCode\": 18, \"key\": \"Alt\", \"code\": \"AltLeft\"}, \"Accept\": {\"keyCode\": 30, \"key\": \"Accept\"}, \"ModeChange\": {\"keyCode\": 31, \"key\": \"ModeChange\"}, \" \": {\"keyCode\": 32, \"key\": \" \", \"code\": \"Space\"}, \"Print\": {\"keyCode\": 42, \"key\": \"Print\"}, \"Execute\": {\"keyCode\": 43, \"key\": \"Execute\", \"code\": \"Open\"}, \"\\u0000\": {\"keyCode\": 46, \"key\": \"\\u0000\", \"code\": \"NumpadDecimal\", \"location\": 3}, \"a\": {\"keyCode\": 65, \"key\": \"a\", \"code\": \"KeyA\"}, \"b\": {\"keyCode\": 66, \"key\": \"b\", \"code\": \"KeyB\"}, \"c\": {\"keyCode\": 67, \"key\": \"c\", \"code\": \"KeyC\"}, \"d\": {\"keyCode\": 68, \"key\": \"d\", \"code\": \"KeyD\"}, \"e\": {\"keyCode\": 69, \"key\": \"e\", \"code\": \"KeyE\"}, \"f\": {\"keyCode\": 70, \"key\": \"f\", \"code\": \"KeyF\"}, \"g\": {\"keyCode\": 71, \"key\": \"g\", \"code\": \"KeyG\"}, \"h\": {\"keyCode\": 72, \"key\": \"h\", \"code\": \"KeyH\"}, \"i\": {\"keyCode\": 73, \"key\": \"i\", \"code\": \"KeyI\"}, \"j\": {\"keyCode\": 74, \"key\": \"j\", \"code\": \"KeyJ\"}, \"k\": {\"keyCode\": 75, \"key\": \"k\", \"code\": \"KeyK\"}, \"l\": {\"keyCode\": 76, \"key\": \"l\", \"code\": \"KeyL\"}, \"m\": {\"keyCode\": 77, \"key\": \"m\", \"code\": \"KeyM\"}, \"n\": {\"keyCode\": 78, \"key\": \"n\", \"code\": \"KeyN\"}, \"o\": {\"keyCode\": 79, \"key\": \"o\", \"code\": \"KeyO\"}, \"p\": {\"keyCode\": 80, \"key\": \"p\", \"code\": \"KeyP\"}, \"q\": {\"keyCode\": 81, \"key\": \"q\", \"code\": \"KeyQ\"}, \"r\": {\"keyCode\": 82, \"key\": \"r\", \"code\": \"KeyR\"}, \"s\": {\"keyCode\": 83, \"key\": \"s\", \"code\": \"KeyS\"}, \"t\": {\"keyCode\": 84, \"key\": \"t\", \"code\": \"KeyT\"}, \"u\": {\"keyCode\": 85, \"key\": \"u\", \"code\": \"KeyU\"}, \"v\": {\"keyCode\": 86, \"key\": \"v\", \"code\": \"KeyV\"}, \"w\": {\"keyCode\": 87, \"key\": \"w\", \"code\": \"KeyW\"}, \"x\": {\"keyCode\": 88, \"key\": \"x\", \"code\": \"KeyX\"}, \"y\": {\"keyCode\": 89, \"key\": \"y\", \"code\": \"KeyY\"}, \"z\": {\"keyCode\": 90, \"key\": \"z\", \"code\": \"KeyZ\"}, \"\\ue03d\": {\"keyCode\": 91, \"key\": \"Meta\", \"code\": \"MetaLeft\"}, \"*\": {\"keyCode\": 106, \"key\": \"*\", \"code\": \"NumpadMultiply\", \"location\": 3}, \"+\": {\"keyCode\": 107, \"key\": \"+\", \"code\": \"NumpadAdd\", \"location\": 3}, \"-\": {\"keyCode\": 109, \"key\": \"-\", \"code\": \"NumpadSubtract\", \"location\": 3}, \"/\": {\"keyCode\": 111, \"key\": \"/\", \"code\": \"NumpadDivide\", \"location\": 3}, \";\": {\"keyCode\": 186, \"key\": \";\", \"code\": \"Semicolon\"}, \"=\": {\"keyCode\": 187, \"key\": \"=\", \"code\": \"Equal\"}, \",\": {\"keyCode\": 188, \"key\": \",\", \"code\": \"Comma\"}, \".\": {\"keyCode\": 190, \"key\": \".\", \"code\": \"Period\"}, \"`\": {\"keyCode\": 192, \"key\": \"`\", \"code\": \"Backquote\"}, \"[\": {\"keyCode\": 219, \"key\": \"[\", \"code\": \"BracketLeft\"}, \"\\\\\": {\"keyCode\": 220, \"key\": \"\\\\\", \"code\": \"Backslash\"}, \"]\": {\"keyCode\": 221, \"key\": \"]\", \"code\": \"BracketRight\"}, \"'\": {\"keyCode\": 222, \"key\": \"'\", \"code\": \"Quote\"}, \"Attn\": {\"keyCode\": 246, \"key\": \"Attn\"}, \"CrSel\": {\"keyCode\": 247, \"key\": \"CrSel\", \"code\": \"Props\"}, \"ExSel\": {\"keyCode\": 248, \"key\": \"ExSel\"}, \"EraseEof\": {\"keyCode\": 249, \"key\": \"EraseEof\"}, \"Play\": {\"keyCode\": 250, \"key\": \"Play\"}, \"ZoomOut\": {\"keyCode\": 251, \"key\": \"ZoomOut\"}, \")\": {\"keyCode\": 48, \"key\": \")\", \"code\": \"Digit0\"}, \"!\": {\"keyCode\": 49, \"key\": \"!\", \"code\": \"Digit1\"}, \"@\": {\"keyCode\": 50, \"key\": \"@\", \"code\": \"Digit2\"}, \"#\": {\"keyCode\": 51, \"key\": \"#\", \"code\": \"Digit3\"}, \"$\": {\"keyCode\": 52, \"key\": \"$\", \"code\": \"Digit4\"}, \"%\": {\"keyCode\": 53, \"key\": \"%\", \"code\": \"Digit5\"}, \"^\": {\"keyCode\": 54, \"key\": \"^\", \"code\": \"Digit6\"}, \"&\": {\"keyCode\": 55, \"key\": \"&\", \"code\": \"Digit7\"}, \"(\": {\"keyCode\": 57, \"key\": \"(\", \"code\": \"Digit9\"}, \"A\": {\"keyCode\": 65, \"key\": \"A\", \"code\": \"KeyA\"}, \"B\": {\"keyCode\": 66, \"key\": \"B\", \"code\": \"KeyB\"}, \"C\": {\"keyCode\": 67, \"key\": \"C\", \"code\": \"KeyC\"}, \"D\": {\"keyCode\": 68, \"key\": \"D\", \"code\": \"KeyD\"}, \"E\": {\"keyCode\": 69, \"key\": \"E\", \"code\": \"KeyE\"}, \"F\": {\"keyCode\": 70, \"key\": \"F\", \"code\": \"KeyF\"}, \"G\": {\"keyCode\": 71, \"key\": \"G\", \"code\": \"KeyG\"}, \"H\": {\"keyCode\": 72, \"key\": \"H\", \"code\": \"KeyH\"}, \"I\": {\"keyCode\": 73, \"key\": \"I\", \"code\": \"KeyI\"}, \"J\": {\"keyCode\": 74, \"key\": \"J\", \"code\": \"KeyJ\"}, \"K\": {\"keyCode\": 75, \"key\": \"K\", \"code\": \"KeyK\"}, \"L\": {\"keyCode\": 76, \"key\": \"L\", \"code\": \"KeyL\"}, \"M\": {\"keyCode\": 77, \"key\": \"M\", \"code\": \"KeyM\"}, \"N\": {\"keyCode\": 78, \"key\": \"N\", \"code\": \"KeyN\"}, \"O\": {\"keyCode\": 79, \"key\": \"O\", \"code\": \"KeyO\"}, \"P\": {\"keyCode\": 80, \"key\": \"P\", \"code\": \"KeyP\"}, \"Q\": {\"keyCode\": 81, \"key\": \"Q\", \"code\": \"KeyQ\"}, \"R\": {\"keyCode\": 82, \"key\": \"R\", \"code\": \"KeyR\"}, \"S\": {\"keyCode\": 83, \"key\": \"S\", \"code\": \"KeyS\"}, \"T\": {\"keyCode\": 84, \"key\": \"T\", \"code\": \"KeyT\"}, \"U\": {\"keyCode\": 85, \"key\": \"U\", \"code\": \"KeyU\"}, \"V\": {\"keyCode\": 86, \"key\": \"V\", \"code\": \"KeyV\"}, \"W\": {\"keyCode\": 87, \"key\": \"W\", \"code\": \"KeyW\"}, \"X\": {\"keyCode\": 88, \"key\": \"X\", \"code\": \"KeyX\"}, \"Y\": {\"keyCode\": 89, \"key\": \"Y\", \"code\": \"KeyY\"}, \"Z\": {\"keyCode\": 90, \"key\": \"Z\", \"code\": \"KeyZ\"}, \":\": {\"keyCode\": 186, \"key\": \":\", \"code\": \"Semicolon\"}, \"<\": {\"keyCode\": 188, \"key\": \"<\", \"code\": \"Comma\"}, \"_\": {\"keyCode\": 189, \"key\": \"_\", \"code\": \"Minus\"}, \">\": {\"keyCode\": 190, \"key\": \">\", \"code\": \"Period\"}, \"?\": {\"keyCode\": 191, \"key\": \"?\", \"code\": \"Slash\"}, \"~\": {\"keyCode\": 192, \"key\": \"~\", \"code\": \"Backquote\"}, \"{\": {\"keyCode\": 219, \"key\": \"{\", \"code\": \"BracketLeft\"}, \"|\": {\"keyCode\": 220, \"key\": \"|\", \"code\": \"Backslash\"}, \"}\": {\"keyCode\": 221, \"key\": \"}\", \"code\": \"BracketRight\"}, \"\\\"\": {\"keyCode\": 222, \"key\": \"\\\"\", \"code\": \"Quote\"}}\n");
    public static final Map<String, Integer> modifierBit = Map.of("\ue00a", 1, "\ue009", 2, "\ue03d", 4, "\ue008", 8);

    static {
        K = new HashMap<>();
        K.put("NULL", "\ue000");
        K.put("CANCEL", "\ue001");
        K.put("HELP", "\ue002");
        K.put("BACKSPACE", "\ue003");
        K.put("BACK_SPACE", K.get("BACKSPACE"));
        K.put("TAB", "\ue004");
        K.put("CLEAR", "\ue005");
        K.put("RETURN", "\ue006");
        K.put("ENTER", "\ue007");
        K.put("SHIFT", "\ue008");
        K.put("LEFT_SHIFT", K.get("SHIFT"));
        K.put("CONTROL", "\ue009");
        K.put("CTRL", "\ue009");
        K.put("LEFT_CONTROL", K.get("CONTROL"));
        K.put("ALT", "\ue00a");
        K.put("LEFT_ALT", K.get("ALT"));
        K.put("PAUSE", "\ue00b");
        K.put("ESCAPE", "\ue00c");
        K.put("SPACE", "\ue00d");
        K.put("PAGE_UP", "\ue00e");
        K.put("PAGE_DOWN", "\ue00f");
        K.put("END", "\ue010");
        K.put("HOME", "\ue011");
        K.put("LEFT", "\ue012");
        K.put("ARROW_LEFT", K.get("LEFT"));
        K.put("UP", "\ue013");
        K.put("ARROW_UP", K.get("UP"));
        K.put("RIGHT", "\ue014");
        K.put("ARROW_RIGHT", K.get("RIGHT"));
        K.put("DOWN", "\ue015");
        K.put("ARROW_DOWN", K.get("DOWN"));
        K.put("INSERT", "\ue016");
        K.put("DELETE", "\ue017");
        K.put("DEL", "\ue017");
        K.put("SEMICOLON", "\ue018");
        K.put("EQUALS", "\ue019");
        K.put("NUMPAD0", "\ue01a");
        K.put("NUMPAD1", "\ue01b");
        K.put("NUMPAD2", "\ue01c");
        K.put("NUMPAD3", "\ue01d");
        K.put("NUMPAD4", "\ue01e");
        K.put("NUMPAD5", "\ue01f");
        K.put("NUMPAD6", "\ue020");
        K.put("NUMPAD7", "\ue021");
        K.put("NUMPAD8", "\ue022");
        K.put("NUMPAD9", "\ue023");
        K.put("MULTIPLY", "\ue024");
        K.put("ADD", "\ue025");
        K.put("SUBTRACT", "\ue027");
        K.put("DECIMAL", "\ue028");
        K.put("DIVIDE", "\ue029");
        K.put("F1", "\ue031");
        K.put("F2", "\ue032");
        K.put("F3", "\ue033");
        K.put("F4", "\ue034");
        K.put("F5", "\ue035");
        K.put("F6", "\ue036");
        K.put("F7", "\ue037");
        K.put("F8", "\ue038");
        K.put("F9", "\ue039");
        K.put("F10", "\ue03a");
        K.put("F11", "\ue03b");
        K.put("F12", "\ue03c");
        K.put("META", "\ue03d");
        K.put("COMMAND", "\ue03d");

    }

    /**
     * @param value 输入的值
     * @return 把要输入的内容连成字符串，去掉其中 ctrl 等键。
     * 返回的modifier表示是否有按下组合键
     */
    public static Key keysToTyping(List<String> value) {
        StringBuilder typing = new StringBuilder();
        int modifier = 0;

        for (Object val : value) {
            if (val.equals("\ue009") || val.equals("\ue008") || val.equals("\ue00a") || val.equals("\ue03d")) {
                modifier |= modifierBit.getOrDefault(val.toString(), 0);
            } else {
                if (val instanceof Integer || val instanceof Float) {
                    String valStr = String.valueOf(val);
                    for (int i = 0; i < valStr.length(); i++) {
                        typing.append(valStr.charAt(i));
                    }
                } else {
                    String string = val.toString();
                    for (int i = 0; i < string.length(); i++) {
                        typing.append(string.charAt(i));
                    }
                }
            }
        }
        return new Key(modifier, typing.toString());
    }

    public static Map<String, Object> keyDescriptionForString(int modifiers, String keyString) {
        int shift = modifiers & 8;
        Map<String, Object> description = new HashMap<>();
        description.put("key", "");
        description.put("keyCode", 0);
        description.put("code", "");
        description.put("text", "");
        description.put("location", 0);
        // Get the definition for the given keyString
        Map<String, Object> definition = KEY_DEFINITIONS.getJSONObject(keyString);

        if (definition == null) {
            throw new IllegalArgumentException("未知按键：" + keyString);
        }

        // Populate description map with the required key descriptions
        if (definition.containsKey("key")) {
            description.put("key", definition.get("key"));
        }
        if (shift != 0 && definition.containsKey("shiftKey")) {
            description.put("key", definition.get("shiftKey"));
        }

        if (definition.containsKey("keyCode")) {
            description.put("keyCode", definition.get("keyCode"));
        }
        if (shift != 0 && definition.containsKey("shiftKeyCode")) {
            description.put("keyCode", definition.get("shiftKeyCode"));
        }

        if (definition.containsKey("code")) {
            description.put("code", definition.get("code"));
        }

        if (definition.containsKey("location")) {
            description.put("location", definition.get("location"));
        }

        if (description.get("key").toString().length() == 1) {
            description.put("text", description.get("key"));
        }

        if (definition.containsKey("text")) {
            description.put("text", definition.get("text"));
        }
        if (shift != 0 && definition.containsKey("shiftText")) {
            description.put("text", definition.get("shiftText"));
        }

        if ((modifiers & ~8) != 0) {
            description.put("text", "");
        }

        return description;
    }


    public static void sendKey(ChromiumBase page, int modifier, String key) {
        // Check if the key is not in keyDefinitions
        if (!KEY_DEFINITIONS.containsKey(key)) {
            page.runCdp("Input.insertText", Map.of("text", key, "_ignore", AlertExistsError.class));
        } else {
            Map<String, Object> description = keyDescriptionForString(modifier, key);
            String text = (String) description.get("text");

            Map<String, Object> data = new HashMap<>();
            data.put("type", text != null ? "keyDown" : "rawKeyDown");
            data.put("modifiers", modifier);
            data.put("code", description.get("code"));
            data.put("windowsVirtualKeyCode", description.get("keyCode"));
            data.put("key", description.get("key"));
            data.put("text", text);
            data.put("autoRepeat", false);
            data.put("unmodifiedText", text);
            data.put("location", description.get("location"));
            data.put("isKeypad", description.get("location").equals(3));
            data.put("_ignore", new AlertExistsError());

            page.runCdp("Input.dispatchKeyEvent", data);
            data.put("type", "keyUp");
            page.runCdp("Input.dispatchKeyEvent", data);
        }
    }

    public static void inputTextOrKeys(ChromiumBase page, Object textOrKey) {
        List<String> textOrKeys;
        if (textOrKey instanceof Integer || textOrKey instanceof Double || textOrKey instanceof Float || textOrKey instanceof Boolean) {
            textOrKey = String.valueOf(textOrKey);
        }
        if (textOrKey instanceof String) {
            textOrKeys = new ArrayList<>();
            textOrKeys.add((String) textOrKey);
        } else if (textOrKey instanceof List) {
            textOrKeys = (List<String>) textOrKey;
        } else if (textOrKey instanceof String[]) {
            textOrKeys = new ArrayList<>(Arrays.asList((String[]) textOrKey));
        } else if (textOrKey instanceof Integer[]) {
            Integer[] textOrKey1 = (Integer[]) textOrKey;
            textOrKeys = new ArrayList<>();
            for (Integer i : textOrKey1) textOrKeys.add(i.toString());
        } else if (textOrKey instanceof Float[]) {
            Float[] textOrKey1 = (Float[]) textOrKey;
            textOrKeys = new ArrayList<>();
            for (Float i : textOrKey1) textOrKeys.add(i.toString());
        } else if (textOrKey instanceof Double[]) {
            Double[] textOrKey1 = (Double[]) textOrKey;
            textOrKeys = new ArrayList<>();
            for (Double i : textOrKey1) textOrKeys.add(i.toString());
        } else {
            throw new IllegalArgumentException("参数无法转换：" + textOrKey);
        }

        Key key = keysToTyping(textOrKeys);
        if (key.modifier != 0) {
            for (Object c : textOrKeys) sendKey(page, key.modifier, c.toString());
            return;
        }
        if (key.typing.endsWith("\n") || key.typing.endsWith("\ue007")) {
            page.runCdp("Input.insertText", Map.of("text", key.typing.substring(0, key.typing.length() - 1), "_ignore", new AlertExistsError()));
            sendKey(page, key.modifier, "\n");
        } else {
            try {
                page.runCdp("Input.insertText", Map.of("text", key.typing, "_ignore", new AlertExistsError()));
            } catch (Exception ignored) {

            }
        }
    }

    @AllArgsConstructor
    public static class Key {
        private int modifier;
        private String typing;
    }
}
