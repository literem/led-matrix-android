package com.literem.matrix.music.dialog;

import android.app.Activity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.literem.matrix.common.base.BasePopupWindow;
import com.literem.matrix.music.R;
import com.literem.matrix.music.callback.OnMusicDetailsDialogListener;
import com.literem.matrix.music.fragment.MusicPlayFragment;

public class PopMusicPlayMode extends BasePopupWindow {

    private int popupWidth;
    private int popupHeight;
    private OnMusicDetailsDialogListener mListener;//菜单选择监听.

    public PopMusicPlayMode(Activity activity) {
        super(activity);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.pop_play_mode_menu;
    }

    @Override
    protected void initView(View view) {
        view.setFocusableInTouchMode(true);
        addClickListener(R.id.tv_play_mode_loop);
        addClickListener(R.id.tv_play_mode_order);
        addClickListener(R.id.tv_play_mode_random);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWidth = view.getMeasuredWidth();
        popupHeight = view.getMeasuredHeight();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void initPopupWindow(PopupWindow popupWindow) {
        popupWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onViewClick(View view) {
        dismiss();
        if(mListener == null) return;
        if (view.getId() == R.id.tv_play_mode_loop) {//单曲循环
            mListener.onValueChange(PopMusicPlayList.SingleLoop, MusicPlayFragment.PLAY_MODE);
        } else if (view.getId() == R.id.tv_play_mode_order) {//顺序播放
            mListener.onValueChange(PopMusicPlayList.ListOrder, MusicPlayFragment.PLAY_MODE);
        } else if (view.getId() == R.id.tv_play_mode_random) {//随机播放
            mListener.onValueChange(PopMusicPlayList.ListRandom, MusicPlayFragment.PLAY_MODE);
        }
    }


    /**
     * 作为指定View的下拉控制显示.
     */
    public void showAsDropDown(View v) {
        //获取需要在其上方显示的控件的位置信息
        int[] location = new int[2];
        v.getLocationOnScreen(location);

        //在控件上方显示
        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, (location[0] + v.getWidth() / 2) - popupWidth / 2, location[1] - popupHeight);
    }


    /**
     * 设置菜单选择监听.
     */
    public void setOnItemSelectedListener(OnMusicDetailsDialogListener listener) {
        mListener = listener;
    }


}
