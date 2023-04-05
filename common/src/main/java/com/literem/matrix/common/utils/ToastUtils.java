package com.literem.matrix.common.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {

    public static void Show(Context context, String text) {
        Toast.makeText(context,text, Toast.LENGTH_SHORT).show();
    }

    public static void ShowLong(Context context, String text) {
        Toast.makeText(context,text, Toast.LENGTH_LONG).show();
    }

}
