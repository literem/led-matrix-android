package com.literem.matrix.common.entity;

/**
 * author : literem
 * time   : 2022/12/19
 * desc   : 先进先出的队列
 * version: 1.0
 */
public class SendDataQueue {

    private final int capacity;
    private int size = 0;
    private final byte[][] list;
    private int head = 0;
    private int tail = 0;

    public SendDataQueue(int capacity) {
        this.capacity = capacity;
        list = new byte[capacity][];
    }

    public void clearQueue(){
        head = 0;
        tail = 0;
        size = 0;
    }

    public boolean addQueue(byte[] t) {
        if (size >= capacity) {
            return false;
        }
        list[tail++] = t;
        tail = tail % capacity;
        size++;
        return true;
    }

    public byte[] popQueue() {
        if (size < 0) {
            return null;
        }
        byte[] remove = list[head++];
        head = head % capacity;
        size--;
        return remove;
    }

    public int size() {
        return size;
    }
}
