package com.literam.matrix.common.data;

import java.util.List;

public class DataUtils {

    public static final int DISMISS_LOADING = 10;

    /********** Frame *************/
    public static List<boolean[][]> listFrame = null;
    public static final int EditFrame = 2000;
    public static final int MaxFrame = 100;//最大帧数


    public static final int EditText = 255;
    public static final int ChangeMusic = 254;

    public static final int REQ_EDIT_LRC = 101;
    public static final int REQ_MAKE_LRC = 201;
    public static final int RES_MAKE_LRC = 202;



    /********** 点阵屏相关 *************/
    public static int ModuleSize = 8;
    public static int inAnim = -1;
    public static int outAnim = -1;
    public static boolean runOnStart = false;
}
