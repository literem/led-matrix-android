package com.literam.matrix.music.entity;

public class PlayList {

    private int id;//数据库记录的唯一id
    private int number;//列表展示的序号
    private int musicId;//引用的音乐id
    private String title;//歌名
    private String singer;//歌手
    private int sort;//排序，序号
    private boolean isPlay = false;//是否正在播放

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getMusicId() {
        return musicId;
    }

    public void setMusicId(int musicId) {
        this.musicId = musicId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }

    @Override
    public String toString() {
        return "PlayList{" +
                "id=" + id +
                ", musicId=" + musicId +
                ", title='" + title + '\'' +
                ", singer='" + singer + '\'' +
                ", sort=" + sort +
                ", isPlay=" + isPlay +
                '}'+"\n";
    }
}
