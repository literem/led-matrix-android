package com.literem.matrix.music.utils;

import com.literem.matrix.common.base.BaseApplication;
import com.literem.matrix.music.entity.Music;

import java.util.ArrayList;
import java.util.List;

public class MusicListData {

    // 存放歌曲列表
    private List<Music> musicList;

    public MusicListData(){
        musicList = new ArrayList<>();
    }

    //初始化音乐列表
    public void initMusicList(){
        musicList.clear();
        musicList.addAll(SQLiteMusicUtils.getInstance(BaseApplication.getContext()).queryAllMusic());
    }

    public void clear(){
        musicList.clear();
    }

    public Music get(int position){
        if (checkPosition(position)) return null;
        return musicList.get(position);
    }

    public void remove(int position){
        if (checkPosition(position)) return;
        musicList.remove(position);
    }

    public int getSize(){
        return musicList.size();
    }

    public int getMusicId(int position){
        if (checkPosition(position)) return 0;
        return musicList.get(position).getId();
    }

    public void setPlay(int position,boolean state){
        if (checkPosition(position)) return;
        musicList.get(position).setPlay(state);
    }

    public void setExpend(int position,boolean state){
        if (checkPosition(position)) return;
        musicList.get(position).setExpend(state);
    }

    public int queryPosByMusicId(int musicId){
        for (int i = 0; i < musicList.size(); i++) {
            if(musicList.get(i).getId() == musicId){
                return i;
            }
        }
        return -1;
    }

    //检查position是否越界
    private boolean checkPosition(int position){
        return position >= musicList.size();
    }
}
