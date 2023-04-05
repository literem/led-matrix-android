package com.literem.matrix.music.utils;

import com.literem.matrix.music.entity.LrcRow;

import java.util.List;

/**
 * 解析歌词，得到LrcRow的集合
 */
public interface ILrcBuilder {
    /**
     * 解析歌词，得到LrcRow的集合
     * @return LrcRow的集合
     */
    List<LrcRow> getLrcRows(String lrc);
    List<LrcRow> getLrcRowsByPath(String path);
    String getLrcString(String path);
}
