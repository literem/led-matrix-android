package com.literam.matrix.common.font;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.*;

/**
 * author : literem
 * time   : 2022/12/05
 * desc   : 解码工具，获取汉字的GBK编码
 * version: 1.0
 */
public class EncodeUtils {
    private final CharsetEncoder charsetEncoder;

    public EncodeUtils(String charset){
        charsetEncoder = ThreadLocalCoders.encoderFor(Charset.forName(charset))
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
    }

    public byte[] encode(String str){
        char[] chars = str.toCharArray();
        int length = str.length();
        byte[] byteData = new byte[length*2];
        int len;
        short b1,b2;
        CharBuffer cb = CharBuffer.wrap(chars);
        try {
            for (int i=0,j=0; i < length; i++,j=i*2) {
                if(chars[i] == ' '){
                    byteData[j] = 0;
                    byteData[j+1] = 0;
                    continue;
                }
                cb.limit(i+1);
                cb.position(i);
                ByteBuffer byteBuffer = charsetEncoder.encode(cb);
                if(byteBuffer.limit() == 2){
                    b1 = (short) (byteBuffer.get() & 0xff);
                    b2 = (short) (byteBuffer.get() & 0xff);
                    len = getGBK16Range(b1, b2);
                }else if(byteBuffer.limit() == 1){
                    b2 = (short) (byteBuffer.get() & 0xff);
                    len = getGBK16Range((short) 0,b2);
                }else{
                    len = 0;
                }
                byteData[j] = (byte) ((len>>8) & 0x00ff);
                byteData[j+1] = (byte) (len & 0x00ff);
            }
            return byteData;
        } catch (CharacterCodingException x) {
            return null;
        }
    }


    byte[] getLengthByteArray(String[] str){
        byte[] b = new byte[str.length];
        for (int i = 0; i < str.length; i++) {
            b[i] = (byte) str[i].length();
        }
        return b;
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

    private int getGB2312Range(short areaCode,short posCode){
        int range = ((areaCode & 0xff) << 8 | (posCode & 0xff));
        if (areaCode == 0){
            return 187 + (posCode - 0x20);
        }else{
            return (areaCode - 0xA1) * 94 + (posCode - 0xA1);//通过GB2312定位该字符在数组中的位置
        }
    }
}
