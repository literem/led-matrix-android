package com.literam.matrix.music.callback;

public interface OnPlayListListener {
    void onItemClick(int position);
    void onDeleteClick(int position);
    void onListClean();
}
