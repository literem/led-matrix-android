package com.literam.matrix.common.dialog;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.literam.matrix.common.R;
import com.literam.matrix.common.base.BaseDialog;

/**
 * author : literem
 * time   : 2023/01/30
 * desc   :
 * version: 1.0
 */
public class DialogTextInput extends BaseDialog {

    private TextView tvSure;
    private TextView tvCancel;
    private TextView tvTitle;
    private EditText editText;

    private DialogTextInput.OnDialogInputListener btnSureListener;
    private DialogTextInput.OnDialogInputListener btnCancelListener;

    private DialogTextInput(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.dialog_input_text;
    }

    @Override
    protected void initView(View view) {
        tvTitle = view.findViewById(R.id.tv_dialog_title);
        editText = view.findViewById(R.id.tv_dialog_input);
        tvSure = view.findViewById(R.id.tv_dialog_sure);
        tvCancel = view.findViewById(R.id.tv_dialog_cancel);
        addOnClickListener(tvSure);
        addOnClickListener(tvCancel);
    }

    public void showDialog() {
        editText.setFocusableInTouchMode(true);//自动显示键盘
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                InputMethodManager manager = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
                if (manager != null) manager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 300);
        super.showDialog();
    }

    @Override
    protected int setDialogWidth(int screenWidth) {
        return (int) (screenWidth*0.8);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        //如果开启了输入法，则隐藏
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            if(imm.isActive()) {
                imm.hideSoftInputFromWindow(editText.getWindowToken(),0);
            }
        }
    }

    @Override
    public void onViewClick(View view) {
        if(view.getId() == R.id.tv_dialog_sure){
            if(btnSureListener != null)
                btnSureListener.onInputFinish(DialogTextInput.this, getInputText());
            else
                dismiss();
        }else if(view.getId() == R.id.tv_dialog_cancel){
            if(btnCancelListener != null)
                btnSureListener.onInputFinish(DialogTextInput.this, getInputText());
            else
                dismiss();
        }
    }


    public static DialogTextInput builder(Context context){
        return new DialogTextInput(context);
    }

    public DialogTextInput setTitle(String title) {
        tvTitle.setText(title);
        return this;
    }

    public DialogTextInput setInputText(String text){
        editText.setText(text);
        editText.setSelection(text.length());//光标在右边
        return this;
    }

    public DialogTextInput setInputType(int type){
        editText.setInputType(type);
        return this;
    }

    public DialogTextInput setHint(String hint){
        editText.setHint(hint);
        return this;
    }

    //获取当前输入框对象
    public EditText getEditText() {
        return editText;
    }

    public String getInputText(){
        return editText.getText().toString();
    }


    //确定键监听器
    public DialogTextInput setOnSureListener(String text, DialogTextInput.OnDialogInputListener listener) {
        if(text != null)
            tvSure.setText(text);
        this.btnSureListener = listener;
        return this;
    }

    //取消键监听器
    public DialogTextInput setOnCancelListener(String text, DialogTextInput.OnDialogInputListener listener) {
        if(text != null)
            tvCancel.setText(text);
        this.btnCancelListener = listener;
        return this;
    }

    public interface OnDialogInputListener {
        void onInputFinish(DialogTextInput dialog,String text);
    }

}
