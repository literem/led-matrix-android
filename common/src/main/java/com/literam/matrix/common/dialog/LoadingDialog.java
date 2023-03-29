package com.literam.matrix.common.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.literam.matrix.common.R;


/**
 * author : literem
 * time   : 2022/11/09
 * desc   :
 * version: 1.0
 */
public class LoadingDialog extends Dialog {

    private LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public static class Builder{

        private Context context;
        private String message;
        private boolean isShowMessage=true;
        private boolean isCancelable=false;
        private boolean isCancelOutside=false;


        public Builder(Context context) {
            this.context = context;
        }

        /**
         * 设置提示信息
         * @param message msg
         * @return this
         */
        public Builder setMessage(String message){
            this.message=message;
            return this;
        }

        /**
         * 设置是否显示提示信息
         * @param isShowMessage isShowMsg
         * @return this
         */
        public Builder setShowMessage(boolean isShowMessage){
            this.isShowMessage = isShowMessage;
            return this;
        }

        /**
         * 设置是否可以按返回键取消
         * @param isCancelable isbackCancel
         * @return this
         */

        public Builder setCancelable(boolean isCancelable){
            this.isCancelable = isCancelable;
            return this;
        }

        /**
         * 设置是否可以取消
         * @param isCancelOutside isCancel
         * @return this
         */
        public Builder setCancelOutside(boolean isCancelOutside){
            this.isCancelOutside=isCancelOutside;
            return this;
        }

        public LoadingDialog create() {
            LayoutInflater inflater = LayoutInflater.from(context);
            @SuppressLint("InflateParams")
            View view = inflater.inflate(R.layout.dialog_loading,null);
            LoadingDialog loadingDialog = new LoadingDialog(context,R.style.MyDialogStyle);
            TextView msgText = view.findViewById(R.id.tv_loading_text);
            if(isShowMessage){
                msgText.setText(message);
            }else{
                msgText.setVisibility(View.GONE);
            }
            loadingDialog.setContentView(view);
            loadingDialog.setCancelable(isCancelable);
            loadingDialog.setCanceledOnTouchOutside(isCancelOutside);
            return loadingDialog;
        }
    }
}
