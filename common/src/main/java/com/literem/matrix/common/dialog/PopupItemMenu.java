package com.literem.matrix.common.dialog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.literem.matrix.common.R;
import com.literem.matrix.common.base.BasePopupWindow;
import com.literem.matrix.common.utils.DisplayUtil;

/**
 * author : literem
 * time   : 2022/12/08
 * desc   :
 * version: 1.0
 */
public class PopupItemMenu extends BasePopupWindow {

    private LinearLayout llRoot;
    private OnPopupItemListener listener;

    public PopupItemMenu(Activity activity, OnPopupItemListener listener){
        super(activity);
        this.listener = listener;
    }

    @Override
    protected int setLayoutId() {
        return R.layout.pop_menu;
    }

    @Override
    protected void initView(View view) {
        llRoot = view.findViewById(R.id.ll_root);
    }

    @Override
    protected void initPopupWindow(PopupWindow popupWindow) {
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true); //设置可获取焦点
    }

    @Override
    protected void onViewClick(View view) {
        if(listener == null) return;
        final int itemId = (int) view.getTag();
        listener.itemClick(itemId,popupWindow);
    }

    private void addView(String name,int pos){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                DisplayUtil.dip2px(context, 40));

        View view = LayoutInflater.from(context).inflate(R.layout.item_menu,null);
        view.setTag(pos);
        TextView tvContent = view.findViewById(R.id.tv_content);
        tvContent.setText(name);
        addClickListener(view);
        llRoot.addView(view,lp);
    }

    public void showDown(View view, String[] items){
        for (int i = 0; i < items.length; i++) {
            addView(items[i],i);
        }
        popupWindow.showAsDropDown(view);
    }

    public interface OnPopupItemListener {
        void itemClick(int itemId,PopupWindow popupWindow);
    }
}
