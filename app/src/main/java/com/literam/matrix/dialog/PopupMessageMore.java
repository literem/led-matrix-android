package com.literam.matrix.dialog;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.literam.matrix.R;
import com.literam.matrix.common.base.BasePopupWindow;
import com.literam.matrix.common.utils.DisplayUtil;
import com.literam.matrix.common.utils.ToastUtils;

public class PopupMessageMore extends BasePopupWindow {

    public static final int TYPE_COPY = 1;
    public static final int TYPE_DEL = 2;
    public static final int TYPE_RE = 3;
    private int popupShowX,popupShowY;
    private int position = -1;
    private OnPopupMessageMoreListener listener;

    public PopupMessageMore(Activity activity, OnPopupMessageMoreListener listener){
        super(activity);
        this.listener = listener;
    }

    public void showPopupAtViewTop(View v,int position){
        //获取需要在其上方显示的控件的位置信息
        int[] location = new int[2];
        int viewWidth = v.getWidth() / 2;
        v.getLocationOnScreen(location);
        //在控件上方显示
        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, (location[0] + viewWidth) - popupShowX, location[1] - popupShowY);
        this.position = position;
    }

    @Override
    protected int setLayoutId() {
        return R.layout.pop_message_more;
    }

    @Override
    protected void initView(View view) {
        addClickListener(R.id.tv_pop_message_more_copy);
        addClickListener(R.id.tv_pop_message_more_del);
        addClickListener(R.id.tv_pop_message_more_re);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupShowX = view.getMeasuredWidth() / 2;
        popupShowY = view.getMeasuredHeight() - 45;
    }

    @Override
    protected void initPopupWindow(PopupWindow popupWindow) {
        int px = DisplayUtil.dip2px(context, 45);
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(px);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true); //设置可获取焦点
    }

    @Override
    protected void onViewClick(View view) {
        if(position < 0){
            ToastUtils.Show(context,"选择出错");
            popupWindow.dismiss();
            return;
        }
        if(listener == null) return;
        switch (view.getId()){
            case R.id.tv_pop_message_more_copy:
                listener.onMoreClick(TYPE_COPY,position);
                break;
            case R.id.tv_pop_message_more_del:
                listener.onMoreClick(TYPE_DEL,position);
                break;
            case R.id.tv_pop_message_more_re:
                listener.onMoreClick(TYPE_RE,position);
                break;
        }
        position = -1;
        popupWindow.dismiss();
    }

    public interface OnPopupMessageMoreListener {
        void onMoreClick(int type,int position);
    }
}
