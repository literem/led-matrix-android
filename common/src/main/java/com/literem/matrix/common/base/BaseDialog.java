package com.literem.matrix.common.base;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public abstract class BaseDialog extends Dialog{
    protected Context context;

    public BaseDialog(@NonNull Context context){
        super(context);
        this.context = context;
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(setLayoutId(),null);
        initView(view);
        super.setContentView(view);
    }

    protected abstract int setLayoutId();
    protected abstract void initView(View view);

    public abstract void onViewClick(View view);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        Window window = getWindow();
        if(window != null){
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = setDialogWidth(dm.widthPixels);
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//设置Dialog背景透明
            window.setDimAmount(0.5f);//设置Dialog窗口后面的透明度
        }
    }

    protected int setDialogWidth(int screenWidth){
        return (int) (screenWidth * 0.9);
    }

    public void showDialog(){
        super.show();
    }

    protected void addOnClickListener(View view){
        view.setOnClickListener(onClickListener);
    }

    protected void addOnClickListener(View view,int viewID){
        view.findViewById(viewID).setOnClickListener(onClickListener);
    }

    protected View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onViewClick(v);
        }
    };
}
