package com.literem.matrix.music.dialog;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.literem.matrix.common.base.BasePopupWindow;
import com.literem.matrix.music.R;
import com.literem.matrix.music.callback.OnMusicDetailsDialogListener;
import com.literem.matrix.music.fragment.MusicPlayFragment;

/**
 *
 * 类名：PopMusicDetails
 *
 * 说明：点击更多按钮时，弹出的PopupWindow，用于更多的操作
 *
 */
public class PopMusicDetails extends BasePopupWindow {

    private TextView tvName;
    private SeekBar seekBar;

    private OnMusicDetailsDialogListener listener;

    public PopMusicDetails(Activity activity){
        super(activity);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.pop_music_details;
    }

    @Override
    protected void initView(View view) {
        tvName = view.findViewById(R.id.tv_music_details_name);
        addClickListener(R.id.btn_music_details_close);//关闭
        addClickListener(R.id.fl_update_music_info);//修改歌曲信息
        addClickListener(R.id.fl_add_lyric);//添加本地歌词
        addClickListener(R.id.fl_update_lyric);//歌词改错
        addClickListener(R.id.fl_music_info);//歌曲信息
        addClickListener(R.id.fl_remove_lyric);//移除歌词
        addClickListener(R.id.fl_make_lyric);//制作歌词
        initVolume(view);
    }

    @Override
    protected void initPopupWindow(PopupWindow popupWindow) {
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setAnimationStyle(R.style.popup_anim_bottom_top);//设置滑入滑出的动画效果
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true); //设置可获取焦点
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
    }

    @Override
    protected void onViewClick(View view) {
        int id = view.getId();
        if(id == R.id.btn_music_details_close){//关闭窗口
            dismiss();
            return;
        }
        if(listener == null) return;
        if(id == R.id.fl_music_info){//查看歌曲信息
            listener.onMusicChange(MusicPlayFragment.MusicInfo);
        }else if(id == R.id.fl_update_music_info){//更新歌曲
            listener.onMusicChange(MusicPlayFragment.MusicUpdateInfoAction);
        }else if(id == R.id.fl_add_lyric){//添加本地歌词
            listener.onLyricChange(null,MusicPlayFragment.AddLyricAction);
        }else if(id == R.id.fl_update_lyric){//改歌词
            listener.onLyricChange(null,MusicPlayFragment.UpdateLyric);
        }else if(id == R.id.fl_make_lyric){//制作歌词
            listener.onLyricChange(null,MusicPlayFragment.MakeLyric);
        }else if(id == R.id.fl_remove_lyric){//移除歌词
            listener.onLyricChange(null,MusicPlayFragment.RemoveLyric);
        }
    }

    public void setOnValueChangeListener(OnMusicDetailsDialogListener listener){
        this.listener = listener;
    }

    //设置popup_window在底部显示
    public void showMusicDetails(){
        backgroundAlpha(0.6f);//设置遮罩层
        popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    //设置seekBar最大的音量值
    public void setMaxVolume(int max){
        if(seekBar != null) seekBar.setMax(max);
    }

    //设置seekBar当前的音量值
    public void setVolume(int volume){
        seekBar.setProgress(volume);
    }

    //初始化seekBar，设置进度改变监听
    private void initVolume(View view){
        seekBar = view.findViewById(R.id.seekBar_volume);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b && listener != null){
                    listener.onValueChange(i, MusicPlayFragment.CHANGE_VOLUME);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
    }

    public void setMusicInfoTitle(String title){
        tvName.setText(title);
    }
}
