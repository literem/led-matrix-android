package com.literam.matrix.common.utils;


import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class SoftKeyboardListener {

    //activity的根视图
    private View rootView;

    //纪录根视图的显示高度
    private int rootViewVisibleHeight;

    //监听对象
    private OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener;

    private Context context;

    public SoftKeyboardListener(Activity activity) {
        context = activity;

        //获取activity的根视图
        rootView = activity.getWindow().getDecorView();

        //监听视图树中全局布局发生改变或者视图树中的某个视图的可视状态发生改变
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //获取当前根视图在屏幕上显示的大小
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int visibleHeight = r.height();
                if (rootViewVisibleHeight == 0) {
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                //根视图显示高度没有变化，可以看作软键盘显示／隐藏状态没有改变
                if (rootViewVisibleHeight == visibleHeight) {
                    return;
                }

                //根视图显示高度变小超过200，可以看作软键盘显示了
                if (rootViewVisibleHeight - visibleHeight > 200) {
                    if (onSoftKeyBoardChangeListener != null) {
                        onSoftKeyBoardChangeListener.keyBoardShow(rootViewVisibleHeight - visibleHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

                //根视图显示高度变大超过200，可以看作软键盘隐藏了
                if (visibleHeight - rootViewVisibleHeight > 200) {
                    if (onSoftKeyBoardChangeListener != null) {
                        onSoftKeyBoardChangeListener.keyBoardHide(visibleHeight - rootViewVisibleHeight);
                    }
                    rootViewVisibleHeight = visibleHeight;
                    return;
                }

            }
        });
    }

    private void setOnSoftKeyBoardChangeListener(OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        this.onSoftKeyBoardChangeListener = onSoftKeyBoardChangeListener;
    }

    public void setListener(OnSoftKeyBoardChangeListener onSoftKeyBoardChangeListener) {
        setOnSoftKeyBoardChangeListener(onSoftKeyBoardChangeListener);
    }

    public void showKeyboard(EditText et){
        //自动显示键盘
        et.setFocusableInTouchMode(true);
        et.requestFocus();
        InputMethodManager manager = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
        if (manager != null) manager.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
    }

    public void hideKeyboard(EditText et){
        InputMethodManager manager = ((InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE));
        if (manager != null) manager.hideSoftInputFromWindow(et.getWindowToken(),0);
    }

    public interface OnSoftKeyBoardChangeListener {
        void keyBoardShow(int height);
        void keyBoardHide(int height);
    }
}

