package com.literam.matrix.common.dialog;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;

import com.literam.matrix.common.R;
import com.literam.matrix.common.base.BasePopupWindow;
import com.literam.matrix.common.utils.DisplayUtil;
import com.literam.matrix.common.utils.ToastUtils;

/**
 * author : literem
 * time   : 2022/12/11
 * desc   :
 * version: 1.0
 */
public class DialogList extends BasePopupWindow {

    private ScrollView scrollView;
    private LinearLayout llRoot;
    private TextView tvTitle;

    private int defaultItemHeight;
    private int defaultItemColor;

    private String[] items;

    private LinearLayout.LayoutParams splitLine;//分隔线

    private DialogList(Activity context) {
        super(context);
        defaultItemHeight = DisplayUtil.dip2px(context,50);
        defaultItemColor = Color.parseColor("#1296db");
        splitLine = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(context, 1));
    }

    public static DialogList Builder(Activity context){
        return new DialogList(context);
    }

    //设置标题
    public DialogList setTitle(String title){
        tvTitle.setText(title);
        return this;
    }

    //设置每一项的名称
    public DialogList setItems(String[] items){
        if(items != null && items.length !=0){
            llRoot.removeAllViews();
            this.items = items;
            for (int i = 0,size=items.length-1; i <= size; i++) {
                addView(i,items[i]);
            }
        }
        return this;
    }

    //设置点击每项的事件监听
    public DialogList setItemListener(OnDialogListListener listener){
        this.listener = listener;
        return this;
    }

    //显示弹窗
    public void show(){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int height = dm.heightPixels / 3;
        int width = dm.widthPixels;
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        popupWindow.setWidth((int) (width*0.9));
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        backgroundAlpha(0.6f);
        popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.dialog_list_view;
    }

    @Override
    protected void initView(View view) {
        addClickListener(R.id.tv_cancel);
        llRoot = findViewById(R.id.ll_root);
        scrollView = findViewById(R.id.scroll_view);
        tvTitle = findViewById(R.id.tv_dialog_title);
    }

    @Override
    protected void initPopupWindow(PopupWindow popupWindow) {
        popupWindow.setOutsideTouchable(false);
        popupWindow.setFocusable(true); //设置可获取焦点
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setAnimationStyle(R.style.popup_anim_bottom_top);//设置滑入滑出的动画效果
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }});
    }

    @Override
    protected void onViewClick(View view) {
        if(view.getId() == R.id.tv_cancel){
            popupWindow.dismiss();
            return;
        }
        try{
            int itemId = (int) view.getTag();
            if (listener != null) listener.dialogListItemClick(DialogList.this,items[itemId],itemId);
        }catch (Exception e){
            ToastUtils.Show(context,"请点击正确的项");
        }
    }

    //关闭弹窗
    public void dismiss(){
        popupWindow.dismiss();
    }


    private void addView(int id,String itemName){
        TextView tvItem = new TextView(context);
        tvItem.setText(itemName);
        tvItem.setTextColor(defaultItemColor);
        tvItem.setGravity(Gravity.CENTER);
        tvItem.setTextSize(15);
        tvItem.setHeight(defaultItemHeight);
        tvItem.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        tvItem.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        tvItem.setTag(id);
        tvItem.setOnClickListener(onClickListener);
        llRoot.addView(tvItem);

        View view = new View(context);
        view.setBackgroundColor(Color.parseColor("#DDDDDD"));
        view.setLayoutParams(splitLine);
        llRoot.addView(view);
    }

    private OnDialogListListener listener;
    public interface OnDialogListListener {
        void dialogListItemClick(DialogList dialog,String itemName,int itemId);
    }
}
