package com.literem.matrix.music.callback;

/**
 * 播放音乐页面，更多 按钮弹出的PopupWindow，里面每个item弹出的dialog的监听
 */
public interface OnMusicDetailsDialogListener {

    void onValueChange(int dat, int status);
    void onLyricChange(String dat, int status);
    void onMusicChange(int status);

}
