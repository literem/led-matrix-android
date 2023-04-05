package com.literem.matrix.music.callback;

/**
 * 音乐播放回调接口
 */
public interface OnMusicEventListener {
    void onPublish(int percent);
    void onPlayStart(int position, int max);
    void onCompletion(int position);
}
