package com.literem.matrix.music.utils;

import android.content.Context;

import com.literem.matrix.music.entity.PlayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PlayListData {


    /**
     * 注意在这里是最底层，需要做保护
     */

    private List<PlayList> playList;

    public PlayListData(Context context){
        playList = new ArrayList<>();
        SQLiteMusicUtils sql = SQLiteMusicUtils.getInstance(context);
        playList.addAll(sql.queryPlayList());
    }

    private boolean checkError(int position){
        return position < 0 || position >= playList.size();
    }

    public int getSize(){
        return playList.size();
    }

    public int getMusicIdByPos(int position){
        return playList.get(position).getMusicId();
    }

    //根据索引获取id
    public int getPlayListIdByPos(int position){
        return playList.get(position).getId();
    }

    //根据索引获取名称
    public String getNameByPos(int position){
        return playList.get(position).getTitle();
    }

    public String getTitleSinger(int position){
        if(checkError(position)) return "";
        String name = playList.get(position).getTitle();
        String singer = playList.get(position).getSinger();
        return singer + " - " + name;
    }

    public PlayList get(int position){
        return playList.get(position);
    }


    public void setPlayStatusByPos(int position,boolean state){
        playList.get(position).setPlay(state);
    }

    public void addOne(PlayList pl){
        this.playList.add(pl);
    }

    public void clean(){
        this.playList.clear();
    }

    public void swap(int i,int j){
        Collections.swap(playList, i, j);
    }

    public void removeAndSort(int pos){
        playList.remove(pos);
        for (int i = pos,size = playList.size(); i < size; i++) {
            playList.get(i).setNumber(i+1);
        }
    }

    public boolean removeByMusicId(int musicId){
        boolean isRemove = false;//是否真的移除了播放列表的某项或多项
        Iterator<PlayList> iterator = playList.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getMusicId() == musicId) {
                iterator.remove();
                isRemove = true;
            }
        }
        return isRemove;
    }

    //根据musicId查询索引
    public int queryPosByMusicId(int musicId){
        for (int i = 0; i < playList.size(); i++) {
            if(playList.get(i).getMusicId() == musicId){
                return i;
            }
        }
        return -1;
    }

    //播放列表里是否有匹配的音乐id
    public int isPlayListExistMusicId(int musicId){
        for (int i = 0, size = playList.size(); i < size; i++) {
            if(playList.get(i).getMusicId() == musicId){
                return i;
            }
        }
        return -1;
    }

}
