package com.literem.matrix.common.font;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.InputStream;

public class HZKFontUtils {

    private Context context;

    public HZKFontUtils(Context context){
        this.context = context;
    }

    public boolean[][] drawString(char c) {
        if(c < 0x80){
            return null;
        }
        byte[] data;
        int[] code;
        int byteCount = 0,lCount;
        boolean[][] b = new boolean[16][16];
        code = getGBKCode(c);
        data = read(code[0], code[1]);
        for (int line = 0; line < 16; line++) {
            lCount = 0;
            for (int k = 0; k < 2; k++) {
                for (int j = 0; j < 8; j++) {
                    b[line][lCount] = ((data[byteCount] >> (7 - j)) & 0x1) == 1;
                    lCount++;
                }
                byteCount++;
            }
        }

        return b;
    }


    public void swap(boolean[][] swapArray){
        boolean[][] temp = new boolean[16][16];
        System.arraycopy(swapArray,0,temp,0,16);
        for (int row = 0; row < 16; row++) {
            for (int column = 0,newColumn=15; column < 16; column++,newColumn--) {
                swapArray[newColumn][row] = temp[row][column];
            }
        }
    }

    public void createMatrixFont(char c,boolean[][] b,boolean isModeNormal){
        if(c < 0x80){
            return;
        }
        int condition = isModeNormal ? 1 : 0;//如果是反向取模，为0，否则为1
        byte[] data;
        int[] code;
        int byteCount = 0,lCount;
        code = getGBKCode(c);
        data = read(code[0], code[1]);
        for (int line = 0; line < 16; line++) {//从第一行开始扫描
            lCount = 0;
            for (int k = 0; k < 2; k++) {//左边8个和右边8个
                for (int j = 0; j < 8; j++) {
                    b[line][lCount] = ((data[byteCount] >> (7 - j)) & 0x1) == condition;
                    lCount++;
                }
                byteCount++;
            }
        }
    }

    /**
     * 单个字生成 16x16的点阵数组
     * @param c 单个字符
     * @param b 16x16点阵数组
     */
    public void createBoolean(char c,boolean[][] b){
        if(c < 0x80){
            return;
        }
        byte[] data;
        int[] code;
        int byteCount = 0,lCount;
        code = getGBKCode(c);
        data = read(code[0], code[1]);
        for (int line = 0; line < 16; line++) {
            lCount = 0;
            for (int k = 0; k < 2; k++) {
                for (int j = 0; j < 8; j++) {
                    b[line][lCount] = ((data[byteCount] >> (7 - j)) & 0x1) == 1;
                    lCount++;
                }
                byteCount++;
            }
        }
    }

    private byte[] read(int areaCode, int posCode) {
        byte[] data = null;
        try {
            int area = areaCode - 0xa0;
            int pos = posCode - 0xa0;
            AssetManager manager = context.getResources().getAssets();
            InputStream in = manager.open("hzk16");
            long offset = 32 * ((area - 1) * 94 + pos - 1);//通过GB2312定位该字符在数组中的位置
            in.skip(offset);//跳到该位置
            data = new byte[32];
            in.read(data, 0, 32);//从定位的位置读取32字节的数据
            in.close();
        } catch (Exception ex) {
            System.err.println("SORRY,THE FILE CAN'T BE READ");
        }
        return data;
    }

    /**
     * 获取char的GBK编码
     * @param c 字符
     * @return gbk编码
     */
    private int[] getGBKCode(char c) {
        int[] byteCode = new int[2];
        try {
            byte[] data = String.valueOf(c).getBytes("GB2312");
            byteCode[0] = data[0] < 0 ? 256 + data[0] : data[0];
            byteCode[1] = data[1] < 0 ? 256 + data[1] : data[1];
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return byteCode;
    }

}
