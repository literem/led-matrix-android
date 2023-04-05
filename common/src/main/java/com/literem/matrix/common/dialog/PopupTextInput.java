package com.literem.matrix.common.dialog;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.literem.matrix.common.R;
import com.literem.matrix.common.base.BasePopupWindow;

/**
 * author : literem
 * time   : 2023/01/31
 * desc   :
 * version: 1.0
 */
public class PopupTextInput extends BasePopupWindow {

    private EditText etInput;

    public PopupTextInput(Activity activity) {
        super(activity);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.popup_text_input;
    }

    @Override
    protected void initView(View view) {
        addClickListener(view.findViewById(R.id.tv_close));
        addClickListener(view.findViewById(R.id.tv_finish));
        etInput = view.findViewById(R.id.et_input);
    }

    @Override
    protected void initPopupWindow(PopupWindow popupWindow) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int height = dm.heightPixels;
        height = height - height / 10;
        popupWindow.setHeight(height);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setAnimationStyle(R.style.popup_anim_bottom_top);//设置滑入滑出的动画效果
    }

    @Override
    protected void onViewClick(View view) {
        if (view.getId() == R.id.tv_close) {
            dismiss();
        }else if(view.getId() == R.id.tv_finish) {

        }
    }

    public void show(View view){
        popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0);
        /*Window window = ((Activity)context).getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.5f;
        window.setAttributes(lp);*/
    }

    @Override
    public void dismiss() {
        super.dismiss();
        /*Window window = ((Activity)context).getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 1.0f;
        window.setAttributes(lp);*/
    }
}
