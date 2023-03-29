package com.literam.matrix.common.utils;

import android.content.Context;

import com.literam.matrix.common.dialog.LoadingDialog;

public class ShowLoadingUtils {
    private LoadingDialog loadingDialog = null;
    private LoadingDialog.Builder builder;

    public ShowLoadingUtils(Context context) {
        builder = new LoadingDialog.Builder(context);
    }

    public void show(String text) {
        builder.setCancelable(false);
        builder.setMessage(text);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    public void dismiss() {
        if(loadingDialog!=null && loadingDialog.isShowing()) loadingDialog.dismiss();
    }

    public void setShowText(String text){
        builder.setMessage(text);
    }
}
