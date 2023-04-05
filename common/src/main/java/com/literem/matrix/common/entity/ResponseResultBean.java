package com.literem.matrix.common.entity;

/**
 * author : literem
 * time   : 2023/01/30
 * desc   :
 * version: 1.0
 */
public class ResponseResultBean {
    private int flag;
    private int data;

    public ResponseResultBean(){
        flag = 0;
        data = 0;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }
}
