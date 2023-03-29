package com.literam.matrix.entity;

import android.graphics.Bitmap;

public class GifFrameBean {
    private int index;
    public int delay;//图像延迟时间
    public Bitmap image;//静态图Bitmap
    private boolean isCheck = false;

    public GifFrameBean(Bitmap im, int del,int index) {
        image = im;
        delay = del;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}
