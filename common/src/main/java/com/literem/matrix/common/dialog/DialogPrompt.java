package com.literem.matrix.common.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.literem.matrix.common.R;
import com.literem.matrix.common.base.BaseDialog;

/**
 * author : literem
 * time   : 2022/11/10
 * desc   : 取消确认的对话框
 * version: 1.0
 */
public class DialogPrompt extends BaseDialog {

    private TextView tvTitle,tvContent;
    private TextView tvSure,tvCancel;
    private OnDialogPromptListener btnSureListener;
    private OnDialogPromptListener btnCancelListener;
    private boolean isShowTitle = true;

    private DialogPrompt(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.dialog_prompt;
    }

    @Override
    protected void initView(View view) {
        tvTitle = view.findViewById(R.id.tv_dialog_title);
        tvContent = view.findViewById(R.id.tv_dialog_content);
        tvSure = view.findViewById(R.id.tv_dialog_sure);
        tvCancel = view.findViewById(R.id.tv_dialog_cancel);
        addOnClickListener(tvSure);
        addOnClickListener(tvCancel);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void onViewClick(View view) {
        if(view.getId() == R.id.tv_dialog_sure){
            if(btnSureListener != null)
                btnSureListener.onButtonClick(DialogPrompt.this);
            else
                dismiss();
        }else if(view.getId() == R.id.tv_dialog_cancel){
            if(btnCancelListener != null)
                btnCancelListener.onButtonClick(DialogPrompt.this);
            else
                dismiss();
        }
    }

    @Override
    protected int setDialogWidth(int screenWidth) {
        return (int) (screenWidth * 0.8);
    }

    public static DialogPrompt builder(Context context){
        return new DialogPrompt(context);
    }

    private void isShowTitle(boolean isShow){
        if(isShow){
            tvTitle.setVisibility(View.VISIBLE);
            tvContent.setPadding(0,0,0,0);
        }else{
            tvTitle.setVisibility(View.INVISIBLE);
            tvContent.setPadding(0,0,0,20);
        }
    }

    public DialogPrompt setTitle(String title){
        tvTitle.setText(title);
        this.isShowTitle = true;
        return this;
    }

    public DialogPrompt setShowTitle(boolean isShow){
        this.isShowTitle = isShow;
        return this;
    }

    public DialogPrompt setContent(String content){
        tvContent.setText(content);
        return this;
    }

    public DialogPrompt setSureButtonColor(int color){
        tvSure.setTextColor(color);
        return this;
    }

    public DialogPrompt setOnSureListener(@Nullable String text, OnDialogPromptListener listener){
        if(text != null)
            tvSure.setText(text);
        btnSureListener = listener;
        return this;
    }

    public  DialogPrompt setCancelButtonColor(int color){
        tvCancel.setTextColor(color);
        return this;
    }

    public DialogPrompt setOnCancelListener(@Nullable String text,@Nullable OnDialogPromptListener listener){
        if(text != null)
            tvCancel.setText(text);
        btnCancelListener = listener;
        return this;
    }

    public void showDialog(){
        isShowTitle(isShowTitle);
        show();
    }

    public DialogPrompt getDialog(){
        return this;
    }


    public interface OnDialogPromptListener{
        void onButtonClick(DialogPrompt dialog);
    }
}
