package com.literem.matrix.common.font;

import android.widget.TextView;

public class HexUtils {

    public static void rotate(boolean[][] a,int angle){
        int i,j;
        boolean t;
        int n=15;
        while(angle != 0) {
            angle-=90;
            for(i=0; i<8; i++ ) {
                for(j=i; j<n-i; j++ ) {
                    t = a[i][j];

                    a[i][j] = a[n-j][i];
                    a[n-j][i] = a[n-i][n-j];
                    a[n-i][n-j] = a[j][n-i];

                    a[j][n-i] = t;
                }
            }
        }
    }

    public static void showHexToTextView(boolean[][] buf, TextView tv){
        int pos;
        int num = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j <= 8; j+=8) {//每组数据有8bit，一列数据有16bit，所以循环2遍
                sb.append("0x");
                for (int k = 4; k >= 0; k-=4) {
                    pos = k+j;
                    if(buf[pos][i])   num+=1;
                    if(buf[pos+1][i]) num+=2;
                    if(buf[pos+2][i]) num+=4;
                    if(buf[pos+3][i]) num+=8;
                    sb.append(hexIndex[num]);
                    num = 0;
                }
                sb.append(",");
            }
        }

        String hex = sb.toString();
        tv.setText(hex.substring(0,hex.length()-1));

        /*
        for (int i = 0; i < 32; i++) {
            sb.append("0x");
            for (int j = 0; j < 2; j++) {
                pos = i*j;
                //sb.append(toHex(buf[pos],buf[pos+1],buf[pos+2],buf[pos+3]));
            }
            sb.append(",");
        }*/
    }

    public static String get8BitHexByBoolean(boolean[][] buf, boolean isOrder){
        int pos;
        int num = 0;
        StringBuilder sb = new StringBuilder();
        int j=0;//左边8个和右边8个，j=0时选择左8，j=8时选择右8
        for (int i = 0; i < 16; i++) {
            sb.append("0x");
            for (int k = 0; k <= 4; k+=4) {
                pos = k+j;
                if(buf[i][pos]   == isOrder) num+=8;
                if(buf[i][pos+1] == isOrder) num+=4;
                if(buf[i][pos+2] == isOrder) num+=2;
                if(buf[i][pos+3] == isOrder) num+=1;
                sb.append(hexIndex[num]);
                num = 0;
            }
            sb.append(",");
        }

        sb.append("\n\n");

        j=8;
        for (int i = 0; i < 16; i++) {
            sb.append("0x");
            for (int k = 0; k <= 4; k+=4) {
                pos = k+j;
                if(buf[i][pos]   == isOrder) num+=8;
                if(buf[i][pos+1] == isOrder) num+=4;
                if(buf[i][pos+2] == isOrder) num+=2;
                if(buf[i][pos+3] == isOrder) num+=1;
                sb.append(hexIndex[num]);
                num = 0;
            }
            sb.append(",");
        }

        String hex = sb.toString();
        return hex.substring(0,hex.length()-1);
    }

    public static String getHexByBoolean(boolean[][] buf, boolean isOrder){
        int pos;
        int num = 0;
        StringBuilder sb = new StringBuilder();
        //左边8个和右边8个，j=0时选择左8，j=8时选择右8
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j <=8 ; j+=8) {
                sb.append("0x");
                for (int k = 0; k <= 4; k+=4) {
                    pos = k+j;
                    if(buf[i][pos]   == isOrder) num+=8;
                    if(buf[i][pos+1] == isOrder) num+=4;
                    if(buf[i][pos+2] == isOrder) num+=2;
                    if(buf[i][pos+3] == isOrder) num+=1;
                    sb.append(hexIndex[num]);
                    num = 0;
                }
                sb.append(",");
            }
        }

        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
        //return hex.substring(0,hex.length()-1);
    }

    public static byte[] getByteByBoolean(boolean[][] buf,boolean isOrder){
        int b;
        int j,k;
        byte[] bytes = new byte[32];
        for (int i = 0; i < 16; i++) {

            for (k=15,b=0,j=1; k>=7; k--,j*=2) {
                if(buf[i][k] == isOrder)
                {
                    b+=j;
                }
            }
            bytes[i*2] = (byte) b;

            for (k=7,b=0,j=1; k>=0; k--,j*=2) {
                if(buf[i][k] == isOrder)
                {
                    b+=j;
                }
            }
            bytes[i*2+1] = (byte) b;
        }

        return bytes;
    }

    //256个像素点，每4个一组，生成十六进制
    private static String boolean2String(boolean[][] buf){
        int pos;
        int num = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {//一共16列数据
            for (int j = 0; j <= 8; j+=8) {//一列数据有16bit，循环2遍，每次取出8bit
                for (int k = 4; k >= 0; k-=4) {//从8bit数据中，每4bit一组，生成16进制
                    pos = k + j;
                    if(buf[pos][i]) num+=1;
                    if(buf[pos+1][i]) num+=2;
                    if(buf[pos+2][i]) num+=4;
                    if(buf[pos+3][i]) num+=8;
                    sb.append(hexIndex[num]);
                    num = 0;
                }
            }

            /*for (int j = 0; j < 4; j++) {//把每列数据按照4个分组
                pos = j*4;
                if(buf[pos][i]) num+=1;
                if(buf[pos+1][i]) num+=2;
                if(buf[pos+2][i]) num+=4;
                if(buf[pos+3][i]) num+=8;
                sb.append(toHex(num));
                num = 0;
            }*/
        }
        return sb.toString();
    }

    //256个像素点转换成字节数组
    public static byte[] hexStr2Bytes(boolean[][] b) {
        String str = boolean2String(b);
        int length = str.length() /2;
        byte[] ret = new byte[length];
        for (int i = 0; i < length; i++) {
            ret[i] = (byte)Integer.parseInt(str.substring(i*2,i*2+2),16);
        }
        return ret;
    }

    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    private static final char[] hexIndex = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

    /*private static String toHex(int num){
        if(num <= 9) return String.valueOf(num);
        String h = "0";
        switch (num){
            case 10:h="a";break;
            case 11:h="b";break;
            case 12:h="c";break;
            case 13:h="d";break;
            case 14:h="e";break;
            case 15:h="f";break;
        }
        return h;
    }*/

}
