package com.literem.matrix.music.entity;

public class Music {

    private int id; //序号
    private int songId;//音乐的id
    private String title; // 音乐标题
    private String singer; // 歌手
    private String album;//专辑
    private int duration;//时长
    private String path; // 音乐路径
    private String image;//封面图片
    private String lyric;//歌词
    private int sortId;//排序的id
    private String addTime;//添加时间
    private boolean isPlay = false;//是否正在播放
    private boolean isExpend = false;//是否展开列表

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
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

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getLyric() {
        return lyric;
    }

    public void setLyric(String lyric) {
        this.lyric = lyric;
    }

    public int getSortId() {
        return sortId;
    }

    public void setSortId(int sortId) {
        this.sortId = sortId;
    }

    public String getAddTime() {
        return addTime;
    }

    public void setAddTime(String addTime) {
        this.addTime = addTime;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }

    public boolean isExpend() {
        return isExpend;
    }

    public void setExpend(boolean expend) {
        isExpend = expend;
    }

    @Override
    public String toString() {
        return "Music{" +
                "id=" + id +
                ", songId=" + songId +
                ", title='" + title + '\'' +
                ", singer='" + singer + '\'' +
                ", album='" + album + '\'' +
                ", duration=" + duration +
                ", path='" + path + '\'' +
                ", image='" + image + '\'' +
                ", lyric='" + lyric + '\'' +
                ", sortId=" + sortId +
                ", addTime='" + addTime + '\'' +
                ", isPlay=" + isPlay +
                '}';
    }
}
