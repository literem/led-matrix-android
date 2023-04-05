package com.literem.matrix.common.font;

import com.literem.matrix.common.bluetooth.BTUtils;
import com.literem.matrix.common.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * author : literem
 * time   : 2022/12/01
 * desc   :
 * version: 1.0
 */
public class CommandUtils {

    //返回的操作码
    public static final byte CMD_ERROR	 = 0;
    public static final byte VALUE_ERROR = 1;
    public static final byte WRITE_ERROR = 2;
    public static final byte RESPONSE_OK = 100;

    public static final byte START_BIT = (byte) 0xff;

    private final byte X = 0;
    private final byte DATA_TYPE_GBK = (byte) 0x0F;
    private final byte DATA_TYPE_FONT = (byte) 0xF0;
    public static final byte STATE_SCROLL = 0x01;

    /*********** 第一级命令 ***********/
    private static final byte SET = (byte) 0xA1;	//设置命令
    private static final byte GET = (byte) 0xA2;	//获取数据命令

    /*********** 第二级命令 ***********/
    //显示命令
    public static final byte DISPLAY_NONE     = (byte) 0xB0;
    public static final byte DISPLAY_SCROLL   = (byte) 0xB1;
    public static final byte DISPLAY_STATIC   = (byte) 0xB2;
    public static final byte DISPLAY_TOGGLE   = (byte) 0xB3;
    public static final byte DISPLAY_CLOCK    = (byte) 0xB4;
    private static final byte DISPLAY_CUSTOM  = (byte) 0xB5;
    private final byte MATRIX_DATA            = (byte) 0xB6;
    private static final byte DEVICE_INFO     = (byte) 0xB7;
    //private static final byte DISPLAY_SPEED   = (byte) 0xB8;
    private static final byte DISPLAY_ANIM    = (byte) 0xB9;


    /*********** 第三级命令 ***********/
    public static final byte MODE               = (byte) 0xC0;	//设置模式
    public static final byte SPEED              = (byte) 0xC1;	//设置速度
    public static final byte ANIM_IN            = (byte) 0xC2;	//设置进入动画
    public static final byte ANIM_OUT           = (byte) 0xC3;	//设置退出动画
    public static final byte SET_RUN_ON_START   = (byte) 0xC4;
    private static final byte DATETIME          = (byte) 0xC5;
    public static final byte MODULE_SIZE        = (byte) 0xC6;	//设置模块大小
    public static final byte ALL_INFO           = (byte) 0xC7;
    public static final byte DISPLAY_STATE      = (byte) 0xC8;
    public static final byte CHARGE_STATE       = (byte) 0xC9;
    public static final byte POWER_STATE        = (byte) 0xCA;
    public static final byte SAVED_MODE         = (byte) 0xCB;
    public static final byte RUN_ON_START       = (byte) 0xCC;
    private static final byte MOVE_HOR          = (byte) 0x10;  //静态显示下的移动操作
    public static final byte MOVE_ANIM          = (byte) 0x20;
    public static final byte MOVE_PAGE_UP       = (byte) 0x30;
    public static final byte MOVE_PAGE_DOWN     = (byte) 0x40;
    private static final byte FONT_DATA         = (byte) 0x50;	//自定显示相关//字模数据
    private static final byte FONT_FRAME_START  = (byte) 0x51;	//帧头
    private static final byte FONT_FRAME_PART   = (byte) 0x52;	//帧内容
    private static final byte FONT_FRAME_END    = (byte) 0x53;	//帧尾
    private static final byte FONT_FRAME_SPEED  = (byte) 0x54;	//帧动画播放速度


    public String getDisplayMode(byte mode){
        switch (mode){
            case DISPLAY_SCROLL:return "滚动显示";
            case DISPLAY_STATIC:return "静态显示";
            case DISPLAY_TOGGLE:return "自动切换";
            case DISPLAY_CLOCK:return "时钟显示";
            case DISPLAY_CUSTOM:return "自定义显示";
            default:return "无";
        }
    }

    public String getAnim(boolean isInAnim,int animID){
        switch (animID){
            case 0xE0:return "从底部往下翻页";
            case 0xE1:return "从顶部往上翻页";
            case 0xE2:return isInAnim?"从左往右显示":"从左往右消失";
            case 0xE3:return isInAnim?"从中间展开显示":"从中间扩张消失";
            case 0xE4:return isInAnim?"从两边收缩显示":"从两边收缩消失";
            case 0xE5:return isInAnim?"从底部往上显示":"从底部往上消失";
            case 0xE6:return isInAnim?"从顶部往下显示":"从顶部往下消失";
            case 0xE7:return "从右往左消失";
        }
        return "无";
    }

    private static CommandUtils instance;
    private EncodeUtils encodeUtils;

    private CommandUtils(){
        encodeUtils = new EncodeUtils("GBK");
    }

    public static CommandUtils getInstance(){
        if(instance == null)
            instance = new CommandUtils();
        return instance;
    }

    private byte[] getGBK(String str){
        return encodeUtils.encode(str);
    }

    private byte[] getToggleGBK(String[] split){
        int totalLength;
        List<byte[]> list = new ArrayList<>(split.length+1);
        byte[] lengthByteArray = encodeUtils.getLengthByteArray(split);
        totalLength = lengthByteArray.length;
        list.add(lengthByteArray);
        for (String s : split){
            byte[] encode = encodeUtils.encode(s);
            totalLength += encode.length;
            list.add(encode);
        }
        byte[] result = new byte[totalLength];
        int destPos = 0;
        for (byte[] bytes : list){
            System.arraycopy(bytes,0,result,destPos,bytes.length);
            destPos += bytes.length;
        }
        return result;
    }


    public byte[] getDisplayInfo(byte cmd2,byte cmd3){
        return Command.Builder()
                .setCommand(GET,cmd2,cmd3)
                .setCommandValue(0)
                .create();
    }

    public byte[] getDeviceInfo(byte type) {
        return Command.Builder()
                .setCommand(GET,DEVICE_INFO,type)
                .setCommandValue(X)
                .create();
    }


    /************************** display_static ******************************/
    public byte[] setDisplayStaticMove(byte type, String str){//设置动画移动
        return Command.Builder()
                .setCommand(SET,DISPLAY_STATIC,type)
                .setCommandValue(X)
                .setData(getGBK(str))
                .setDataType(DATA_TYPE_GBK)
                .create();
    }
    public byte[] setDisplayStaticHorizontalMove(int len){ //水平移动
        return Command.Builder()
                .setCommand(SET,DISPLAY_STATIC,MOVE_HOR)
                .setCommandValue(len)
                .create();
    }
    public byte[] setDisplayStaticAnim(boolean isInAnim, int value){ //设置进入退出动画
        return Command.Builder()
                .setCommand(SET,DISPLAY_STATIC,isInAnim?ANIM_IN:ANIM_OUT)
                .setCommandValue(value)
                .create();
    }


    /************************** display_scroll ******************************/
    public  byte[] setDisplayScroll(String str,boolean isRunOnStart){
        //if (isRunOnStart) System.out.println("run on start");
        return Command.Builder()
                .setCommand(SET,DISPLAY_SCROLL,isRunOnStart ? SET_RUN_ON_START : MODE)
                .setCommandValue(X)
                .setData(getGBK(str))
                .setDataType(DATA_TYPE_GBK)
                .create();
    }

    /************************** display_toggle ******************************/
    public byte[] setDisplayToggle(byte anim,String str,boolean isRunOnStart){
        String[] split = str.split("\n");
        int rowLen = split.length;
        return Command.Builder()
                .setCommand(SET,DISPLAY_TOGGLE,isRunOnStart ? SET_RUN_ON_START : MODE)
                .setCommandValue(((rowLen&0xff) << 8) + (anim&0xff))
                .setData(getToggleGBK(split))
                .setDataType(DATA_TYPE_GBK)
                .create();
    }

    public byte[] setDisplayToggleAnim(byte anim){
        return Command.Builder()
                .setCommand(SET,DISPLAY_TOGGLE,DISPLAY_ANIM)
                .setCommandValue(anim)
                .create();
    }


    /************************** display_custom ******************************/
    public byte[] setDisplayCustom(int pos, byte[] dat){
        return Command.Builder()
                .setCommand(SET,DISPLAY_CUSTOM,FONT_DATA)
                .setCommandValue(pos)
                .setData(dat)
                .setDataType(DATA_TYPE_FONT)
                .create();
    }
    public byte[] setDisplayFrameSpeed(int speed){
        return Command.Builder()
                .setCommand(SET,DISPLAY_CUSTOM, FONT_FRAME_SPEED)
                .setCommandValue(speed)
                .create();
    }
    public byte[] setDisplayFrameStart(){
        return Command.Builder()
                .setCommand(SET,DISPLAY_CUSTOM, FONT_FRAME_START)
                .setCommandValue(X)
                .create();
    }
    public byte[] setDisplayFramePart(byte[] dat){
        return Command.Builder()
                .setCommand(SET,DISPLAY_CUSTOM, FONT_FRAME_PART)
                .setCommandValue(X)
                .setDataType(DATA_TYPE_FONT)
                .setData(dat)
                .create();
    }
    public byte[] setDisplayFrameEnd(int moduleIndex){
        return Command.Builder()
                .setCommand(SET,DISPLAY_CUSTOM, FONT_FRAME_END)
                .setCommandValue(moduleIndex)
                .create();
    }

    /************************** display_clock ******************************/
    public byte[] setDisplayClock(int mode){
        return Command.Builder()
                .setCommand(SET,DISPLAY_CLOCK, MODE)
                .setCommandValue(mode)
                .create();
    }
    public byte[] setDisplayClockDatetime(){
        return Command.Builder()
                .setCommand(SET,DISPLAY_CLOCK, DATETIME)
                .setCommandValue(X)
                .setData(TimeUtils.getCurrentDatetime())
                .setDataType(X)
                .create();
    }

    public byte[] setSpeed(boolean isScroll,int speed){
        return Command.Builder()
                .setCommand(SET,isScroll?DISPLAY_SCROLL:DISPLAY_TOGGLE,SPEED)
                .setCommandValue(speed)
                .create();
    }

    public void setMatrixData(byte cmd3,int value){
        byte[] result = Command.Builder()
                .setCommand(SET,MATRIX_DATA,cmd3)
                .setCommandValue(value)
                .create();
        BTUtils.getInstance().send(result);
    }

    public void setDisplayNone(){
        byte[] result = Command.Builder()
                .setCommand(SET,MATRIX_DATA,DISPLAY_NONE)
                .setCommandValue(X)
                .create();
        BTUtils.getInstance().send(result);
    }

}
