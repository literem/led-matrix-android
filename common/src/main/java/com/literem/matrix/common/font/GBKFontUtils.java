package com.literem.matrix.common.font;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class GBKFontUtils {

    private Context context;
    private byte[] gbk16Array;

    public GBKFontUtils(Context context){
        this.context = context;
    }

    /**
     * 生成点阵字模
     * @param oneChar 字模
     * @param b 生成的对象
     * @param isModeNormal 是否正常取模
     */
    public void createMatrixFont(char oneChar,boolean[][] b,boolean isModeNormal){
        if(gbk16Array == null) gbk16Array = ReadGBK();
        int offset = getGBKAddress(oneChar);
        int condition = isModeNormal ? 1 : 0;//如果是反向取模，为0，否则为1
        int lCount;
        for (int line = 0; line < 16; line++) {
            lCount = 0;
            for (int k = 0; k < 2; k++) {
                for (int j = 0; j < 8; j++) {
                    b[line][lCount] = ((gbk16Array[offset] >> (7 - j)) & 0x1) == condition;
                    lCount++;
                }
                offset++;
            }
        }
    }


    /**
     * 读取GBK16字库
     * @return 返回字节数组
     */
    private byte[] ReadGBK(){
        byte[] data = null;
        AssetManager manager = context.getResources().getAssets();
        try {

            InputStream inputStream = manager.open("GBK16.bin");
            int length = inputStream.available();//拿到长度
            data = new byte[length];
            inputStream.read(data);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private int getGBKAddress(char oneChar){
        try {
            //获取GBK编码，转成十六进制，获取该编码在GBK16字模的int范围，转成十六进制
            String str = String.valueOf(oneChar);
            byte[] gbk_byte = str.getBytes("GBK");//获取该字符的GBK编码

            //计算该GBK编码在GBK16字模中的实际位置
            if (gbk_byte.length == 1){
                return getGBK16Range((short) 0,(short)(gbk_byte[0]&0xff)) * 32;
            }else{
                return getGBK16Range((short)(gbk_byte[0]&0xff),(short)(gbk_byte[1]&0xff)) * 32;
            }
        } catch (UnsupportedEncodingException e){
            return 0;
        }
    }

    /**
     * 获取GBK16编码的范围
     * GBK16编码的int类型的范围
     * @return 在GBK16中的位置
     */
    private int getGBK16Range(short areaCode,short posCode){
        int offset;
        int location;
        int dValue;
        int pageNumber;//页数
        int baseAddress;//基地址
        int range = ((areaCode & 0xff) << 8) + (posCode & 0xff);
        if(range <= 127 && range >= 32){
            return range + 155; //ASCII码
        }

        //129 160
        if (areaCode >= 0x81 && areaCode <= 0xA0){//GBK扩充3区
            //GBK范围：(0x8140)33088 - (0xA0FE)41214，字符范围：7808 - 13919，*191, *1
            dValue = range - 0x8140;
            baseAddress = 7808;
            pageNumber = 191;
        }else if (areaCode >= 0xA1 && areaCode <= 0xA9){//161 169
            if (posCode <= 0xA0) {
                //GBK范围：(0xA840)43072 - (0xA9A0)43424，字符范围：846 - 1039，*97, *1.
                dValue = range - 0xA840;
                baseAddress = 846;
                pageNumber = 97;
            }else {
                //GBK范围：(0xA1A1)41377 - (0xA9FE)43518，字符范围：0 - 845
                dValue = range - 0xA1A1;
                baseAddress = 0;
                pageNumber = 94;
            }
        }else if (areaCode >= 0xAA && areaCode <= 0xFD){//170 253
            if (posCode <= 0xA0){//GBK扩充4区
                //GBK范围：(0xAA40)43584 - (0xFDA0)64928，字符范围：13920 - 22067，*97, *1.
                dValue = range - 0xAA40;
                baseAddress = 13920;
                pageNumber = 97;
            }else {//GBK扩充5区:
                //GBK范围：(0xB0A1)45217 - (0xF7FE)63486，字符范围：1040 - 7807，*94, *1.
                dValue = range - 0xB0A1;
                baseAddress = 1040;
                pageNumber = 94;
            }
        } else if (areaCode == 0xFE){//254
            //GBK范围：(0xFE40)65088 - (0xFE4F)65103，字符范围：22068 - 22083，-, *1.
            dValue = range - 0xFE40;
            baseAddress = 22068;
            pageNumber = 1;
        }else {
            return 0;
        }
        location = dValue % 256;//某一页的位置
        offset = dValue / 256;//该字符在该范围内
        //该范围的基地址+当前页数*页数数量+实际位置
        return baseAddress + offset*pageNumber + location;
    }


}
