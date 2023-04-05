package com.literem.matrix.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.literem.matrix.R;
import com.literem.matrix.common.base.BaseDialog;
import com.literem.matrix.common.utils.ToastUtils;


public class DialogNumberInput extends BaseDialog {

    private EditText etNumber;
    private TextView tvTitle,tvSubTitle;
    private Button btnSure;

    //是否显示光标
    private boolean isShowCursor = false;
    private int value = 0;
    private int step = 1;
    private int maxValue = 10;
    private int minValue = 0;

    private OnSetTimeListener listener;

    private DialogNumberInput(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.dialog_set_time;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initView(View view) {
        etNumber = view.findViewById(R.id.et_input);
        etNumber.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    etNumber.setFocusable(true);
                    etNumber.setFocusableInTouchMode(true);
                    etNumber.requestFocus();
                    etNumber.setCursorVisible(true);
                    isShowCursor = true;
                }
                return false;
            }
        });
        tvTitle = view.findViewById(R.id.tv_dialog_animation_title);
        tvSubTitle = view.findViewById(R.id.tv_sub_title);
        btnSure = view.findViewById(R.id.btn_dialog_set_time_sure);
        addOnClickListener(btnSure);
        addOnClickListener(view.findViewById(R.id.iv_dialog_set_time_add));
        addOnClickListener(view.findViewById(R.id.iv_dialog_set_time_sub));
        addOnClickListener(view.findViewById(R.id.btn_dialog_set_time_cancel));
    }


    public void showDialog() {
        isShowCursor = false;
        etNumber.setCursorVisible(false);
        show();
    }

    @Override
    public void onViewClick(View view) {
        switch (view.getId()){
            case R.id.btn_dialog_set_time_sure:
                if (checkNumber()) return;
                if(listener != null) listener.onSetValue(DialogNumberInput.this,value);
                break;
            case R.id.btn_dialog_set_time_cancel:
                dismiss();
                break;
            case R.id.iv_dialog_set_time_add:
                addNumber();
                break;
            case R.id.iv_dialog_set_time_sub:
                subNumber();
                break;
        }
    }

    //设置监听
    public DialogNumberInput setListener(String text, OnSetTimeListener listener){
        if(text != null && text.length() > 0){
            btnSure.setText(text);
        }
        this.listener = listener;
        return this;
    }

    public static DialogNumberInput Builder(@NonNull Context context){
        return new DialogNumberInput(context);
    }

    //设置最大值和最小值
    public DialogNumberInput setEdgeValue(int min, int max){
        this.maxValue = max;
        this.minValue = min;
        return this;
    }

    //设置初始值
    public DialogNumberInput setInitialValue(int value){
        this.value = value;
        etNumber.setText(String.valueOf(value));
        return this;
    }

    //设置标题和子标题
    public DialogNumberInput setTitle(String title, String sbuTitle){
        tvTitle.setText(title);
        tvSubTitle.setText(sbuTitle);
        return this;
    }

    public DialogNumberInput setStep(int step){
        this.step = step;
        return this;
    }

    private void addNumber(){
        if(isShowCursor) etNumber.setFocusable(false);
        getEditTextValue();
        value += step;
        if(value > maxValue){
            value -= step;
        }else{
            etNumber.setText(String.valueOf(value));
        }
    }

    private void subNumber(){
        if(isShowCursor) etNumber.setFocusable(false);
        getEditTextValue();
        value -= step;
        if(value < minValue){
            value += step;
        }else{
            etNumber.setText(String.valueOf(value));
        }
    }

    private void getEditTextValue(){
        String strNum = etNumber.getText().toString();
        try {
            value =  Integer.valueOf(strNum);
        }catch (NumberFormatException e){
            value = 0;
        }
    }

    private boolean checkNumber(){
        value = Integer.parseInt(etNumber.getText().toString());
        if(value > maxValue) {
            ToastUtils.Show(context,"数字超出范围");
            return true;
        }
        if(value < minValue){
            ToastUtils.Show(context,"数字小于范围");
            return true;
        }
        return false;
    }

    public interface OnSetTimeListener{
        void onSetValue(DialogNumberInput dialog, int value);
    }
}
