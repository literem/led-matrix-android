package com.literam.matrix.common.utils;

import android.widget.TextView;

/**
 * author : literem
 * time   : 2022/12/02
 * desc   :
 * version: 1.0
 */
public class HexPrintUtils {
    private static final char[] hexIndex = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    private static final char[] ch = new char[5];

    public static String byte2Str(byte b){
        ch[0]='0';
        ch[1]='x';
        ch[2] = hexIndex[(b & 0xF0) >> 4];
        ch[3] = hexIndex[b & 0x0F];
        return new String(ch);
    }

    public static void bytePrintln(byte b){
        ch[0]='0';
        ch[1]='x';
        ch[2] = hexIndex[(b & 0xF0) >> 4];
        ch[3] = hexIndex[b & 0x0F];
        System.out.println(ch);
    }

    public static void bytePrint(byte b){
        ch[0]='0';
        ch[1]='x';
        ch[2] = hexIndex[(b & 0xF0) >> 4];
        ch[3] = hexIndex[b & 0x0F];
        ch[4]=' ';
        System.out.print(ch);
    }

    public static void bytePrint(byte[] bs){
        StringBuilder sb = new StringBuilder();
        for (byte b : bs){
            sb.append('0').append('x');
            sb.append(hexIndex[(b & 0xF0) >> 4]);
            sb.append(hexIndex[b & 0x0F]);
            sb.append(',');
        }
        sb.deleteCharAt(sb.length()-1);
        System.out.println(sb.toString());
    }

    public static void bytePrint(byte[] bs, TextView tvText){
        StringBuilder sb = new StringBuilder();
        for (byte b : bs){
            sb.append('0').append('x');
            sb.append(hexIndex[(b & 0xF0) >> 4]);
            sb.append(hexIndex[b & 0x0F]);
            sb.append(',');
        }
        tvText.append(sb.toString());
    }

    public static void intPrintHext(int i){
        StringBuilder sb = new StringBuilder();
        sb.append('0').append('x');
        sb.append(hexIndex[(i&0xF000)>>12]);
        sb.append(hexIndex[(i&0x0F00)>>8]);
        sb.append(hexIndex[(i&0x00F0)>>4]);
        sb.append(hexIndex[(i&0x000F)]);
        System.out.println(sb.toString());
    }
}
