package com.literam.matrix.dialog;

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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;


import com.literam.matrix.R;
import com.literam.matrix.common.utils.ToastUtils;

public class DialogSetAnimation extends Dialog {

    public enum Anim{
        IN,
        OUT
    }
    private String[] inAnimContent = {
            "从底部往下翻页",
            "从顶部往上翻页",
            "从左往右显示",
            "从中间扩张显示",
            "从两边收缩显示",
            "从底部往上显示",
            "从顶部往下显示",
            "无进场动画"
    };

    private String[] outAnimContent = {
            "从左往右消失",
            "从中间扩张消失",
            "从两边收缩消失",
            "从底部往上消失",
            "从顶部往下消失",
            "从右往左消失",
            "无退场动画"
    };

    private Context context;
    private int lastClickTag = -1;
    private TextView tvTitle;
    private String[] content;
    private Anim animType;
    private OnAnimationDialogListener listener;
    private RadioButton lastRadioButton;


    public DialogSetAnimation(@NonNull Context context,OnAnimationDialogListener listener) {
        super(context);
        this.context = context;
        this.listener = listener;
    }

    private void initView(){
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_set_animation,null);

        tvTitle = view.findViewById(R.id.tv_dialog_animation_title);
        view.findViewById(R.id.btn_dialog_animation_sure).setOnClickListener(onClickListener);
        view.findViewById(R.id.btn_dialog_animation_cancel).setOnClickListener(onClickListener);

        //加载列表
        LinearLayout ll_root = view.findViewById(R.id.ll_dialog_animation);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LayoutInflater inflater = LayoutInflater.from(context);

        for (int i = 0; i < content.length; i++) {
            @SuppressLint("InflateParams")
            View itemView = inflater.inflate(R.layout.item_dialog_animation, null);
            itemView.setLayoutParams(lp);
            TextView tv = itemView.findViewById(R.id.tv_dialog_animation);
            RadioButton button = itemView.findViewById(R.id.rb_dialog_animation);
            tv.setText(content[i]);
            button.setOnClickListener(onClickListener);
            button.setTag(i);
            ll_root.addView(itemView);
        }

        super.setContentView(view);
        setCanceledOnTouchOutside(false);
    }

    public void showDialog(String title, Anim anim) {
        this.content = anim==Anim.IN?inAnimContent:outAnimContent;
        initView();
        animType = anim;
        tvTitle.setText(title);
        show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        width = width - width / 10;

        Window window = getWindow();
        if(window != null){
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = width;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//设置Dialog背景透明
            window.setDimAmount(0.5f);//设置Dialog窗口后面的透明度
        }
    }

    private void setRadioClick(RadioButton currentClickRadio){
        int tag = (int) currentClickRadio.getTag();
        if(lastClickTag == tag) return;
        if(lastRadioButton != null) {
            lastRadioButton.setChecked(false);//取消上一个选中
        }
        currentClickRadio.setChecked(true);//选中当前的
        lastRadioButton = currentClickRadio;
        lastClickTag = tag;
    }

    private void onSureClick(boolean isInAnim,int animId){
        if(lastClickTag == -1){
            ToastUtils.Show(context,"请选择动画");
            return;
        }
        animId += isInAnim ? 0xE0 : 0xE2;
        if(listener != null) listener.onDialogClick(content[lastClickTag],isInAnim, animId);
        dismiss();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.btn_dialog_animation_sure: onSureClick(animType==Anim.IN,lastClickTag);break;
                case R.id.btn_dialog_animation_cancel: dismiss();break;
                case R.id.rb_dialog_animation: setRadioClick((RadioButton) view);break;
            }
        }
    };

    public interface OnAnimationDialogListener{
        void onDialogClick(String animName, boolean isInAnim, int animId);
    }
}
