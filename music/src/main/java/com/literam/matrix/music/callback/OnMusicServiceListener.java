package com.literam.matrix.music.callback;

import com.literam.matrix.music.entity.Music;

public interface OnMusicServiceListener {

    void onPlayPrepare(int position, Music music);//音乐准备就绪
    void onPlayCompletion(int position);//播放完成
    void onPlayPause();//播放暂停
    void onPlayContinue();//播放继续
    void onPlayStop();//播放停止


}
