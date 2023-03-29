package com.literam.matrix.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.literam.matrix.R;
import com.literam.matrix.common.widget.PreMultipleCircleView;

//预览点阵的生成工具类
public class PreViewMultipleCircleUtils {

    private PreMultipleCircleView preMultipleCircleView;
    private Button btn;
    private int tag;
    private PreviewCircleButtonUtils buttonStateUtils;
    private View view;

    @SuppressLint("InflateParams")
    public PreViewMultipleCircleUtils(Context context, int tag, PreviewCircleButtonUtils buttonStateUtils){
        this.buttonStateUtils = buttonStateUtils;
        this.tag = tag;

        view = LayoutInflater.from(context).inflate(R.layout.item_preview_multiple_circle, null);
        preMultipleCircleView = view.findViewById(R.id.pre_view_multiple_circle);
        btn = view.findViewById(R.id.btn_module);
        btn.setText(buttonStateUtils.getModuleText(tag));
    }

    public void setOnClickListener(View.OnClickListener onClickListener){
        preMultipleCircleView.setTag(tag);
        preMultipleCircleView.setOnClickListener(onClickListener);
    }

    public void setData(boolean[][] d){
        preMultipleCircleView.setData(d);
    }

    public boolean[][] getData(){
        return preMultipleCircleView.getData();
    }

    public void setButtonCheck(){
        buttonStateUtils.setButtonCheck(btn);
    }

    public void setButtonUncheck(){
        buttonStateUtils.setButtonUncheck(btn,tag);
    }

    public void setButtonDisable(){
        buttonStateUtils.setButtonDisable(btn);
    }

    public void invalidate(){
        preMultipleCircleView.invalidate();
    }

    public void setTag(int tag){
        this.tag = tag;
        preMultipleCircleView.setTag(tag);
        buttonStateUtils.setButtonText(btn,tag);
    }

    public int getTag(){
        return tag;
    }

    public PreMultipleCircleView getPreView(){
        return preMultipleCircleView;
    }

    public View getView(){
        return this.view;
    }
}
