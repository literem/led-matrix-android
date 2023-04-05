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
 * time   : 2022/11/11
 * desc   : 只有一个按钮的提示框
 * version: 1.0
 */
public class DialogWarn extends BaseDialog {

    private TextView tvSure;
    private TextView tvContent;
    private TextView tvTitle;
    private OnDialogWarnListener listener;

    private DialogWarn(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.dialog_warn;
    }

    @Override
    protected void initView(View view) {
        tvTitle = view.findViewById(R.id.tv_dialog_title);
        tvContent = view.findViewById(R.id.tv_dialog_content);
        tvSure = view.findViewById(R.id.tv_dialog_sure);
        addOnClickListener(tvSure);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void onViewClick(View view) {
        if (view.getId() == R.id.tv_dialog_sure){
            if(listener != null)
                listener.onButtonClick(DialogWarn.this);
            else
                dismiss();
        }
    }

    @Override
    protected int setDialogWidth(int screenWidth) {
        return (int) (screenWidth * 0.8);
    }

    public static DialogWarn builder(Context context){
        return new DialogWarn(context);
    }

    public DialogWarn setTitle(String title){
        tvTitle.setText(title);
        return this;
    }

    public DialogWarn isShowTitle(boolean isShow){
        if(isShow){
            tvTitle.setVisibility(View.VISIBLE);
            tvContent.setPadding(0,0,0,0);
        }else{
            tvTitle.setVisibility(View.INVISIBLE);
            tvContent.setPadding(0,0,0,20);
        }
        return this;
    }

    public DialogWarn setContent(String content){
        tvContent.setText(content);
        return this;
    }

    public DialogWarn setSureButtonColor(int color){
        tvSure.setTextColor(color);
        return this;
    }

    public DialogWarn setOnSureListener(@Nullable String text, @Nullable OnDialogWarnListener listener){
        if(text != null)
            tvSure.setText(text);
        this.listener = listener;
        return this;
    }

    public void showDialog(boolean showTitle){
        isShowTitle(showTitle);
        show();
    }

    public void showDialog(String text){
        isShowTitle(true);
        setContent(text);
        show();
    }

    public interface OnDialogWarnListener {
        void onButtonClick(DialogWarn dialog);
    }
}
