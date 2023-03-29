package com.literam.matrix.common.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * author : literem
 * time   : 2022/11/12
 * desc   :
 * version: 1.0
 */
public class ClipboardUtils {

    private static final String DATA = "data";

    /**
     * 复制到剪切板
     * @param context context
     * @param label 标签
     * @param string 内容
     */
    public static void copy(Context context, String label, String string) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setPrimaryClip(ClipData.newPlainText(label != null ? label : DATA, string));
    }

    /**
     * 从剪切板取出
     * @param context context
     * @return 剪切板里的内容
     */
    public static String paste(Context context) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = clipboardManager.getPrimaryClip();
        if(clipData == null) return "";
        return clipData.getItemAt(0).toString();
    }

}
