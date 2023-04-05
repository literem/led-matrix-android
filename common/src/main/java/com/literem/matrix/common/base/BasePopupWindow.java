package com.literem.matrix.common.base;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;

public abstract class BasePopupWindow {
    protected PopupWindow popupWindow;
    protected Context context;
    protected View.OnClickListener onClickListener;
    protected Activity activity;
    protected View view;

    public BasePopupWindow(Activity activity,View.OnClickListener onClickListener){
        this.activity = activity;
        this.context = activity;
        this.onClickListener = onClickListener;
        int layoutId = setLayoutId();
        view = LayoutInflater.from(context).inflate(layoutId,null);
        initView(view);
        popupWindow = new PopupWindow(view);
        initPopupWindow(popupWindow);
    }

    public BasePopupWindow(Activity activity){
        this.activity = activity;
        this.context = activity;
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClick(v);
            }
        };
        int layoutId = setLayoutId();
        view = LayoutInflater.from(context).inflate(layoutId,null);
        initView(view);
        initPopupWindow(popupWindow = new PopupWindow(view));
    }

    protected abstract int setLayoutId();
    protected abstract void initView(View view);
    protected abstract void initPopupWindow(PopupWindow popupWindow);
    protected abstract void onViewClick(View view);

    protected void addClickListener(View view){
        if(view != null && onClickListener != null) view.setOnClickListener(onClickListener);
    }

    protected void addClickListener(int viewID){
        if (onClickListener != null) view.findViewById(viewID).setOnClickListener(onClickListener);
    }
    protected <T extends View> T findAndAddClickListener(int viewID){
        if (onClickListener != null) view.findViewById(viewID).setOnClickListener(onClickListener);
        return view.findViewById(viewID);
    }

    protected <T extends View> T findViewById(int viewID){
        return view.findViewById(viewID);
    }

    protected int getScreenWidth(){
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    protected int getScreenHeight(){
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    //是否显示
    public boolean isShowPopupWindow(){
        if(popupWindow == null) return false;
        return popupWindow.isShowing();
    }

    //关闭PopupWindow
    public void dismiss(){
        if (popupWindow != null){
            popupWindow.dismiss();
        }
    }

    /**
     * 背景遮罩层
     * @param value 0.0-1.0
     */
    public void backgroundAlpha(float value) {
        activity.getWindow();
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = value;
        activity.getWindow().setAttributes(lp);
    }
}
