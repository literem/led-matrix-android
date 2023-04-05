package com.literem.matrix.common.font;

/**
 * author : literem
 * time   : 2022/12/01
 * desc   :
 * version: 1.0
 */
public class Command {

    private byte[] command;
    private byte[] byteData = null;

    private Command(){
        command = new byte[14];
        command[0] = (byte) 0xff;
        command[1] = (byte) 0xff;
        command[10] = 0;
        command[11] = 0;
    }

    private Command(byte[] src){
        this.command = src;
    }

    public static Command Builder(){
        return new Command();
    }

    public static Command Builder(byte[] command){
        return new Command(command);
    }

    private void setCommandLength(int length){
        command[4] = (byte) ((length>>8) & 0x00ff);
        command[5] = (byte) (length & 0x00ff);
    }

    Command setCommand(byte c1, byte c2, byte c3){
        command[6] = c1;
        command[7] = c2;
        command[8] = c3;
        return this;
    }

    Command setCommandValue(int value){
        command[9] = (byte) ((value>>8) & 0x00ff);
        command[10] = (byte) (value & 0x00ff);
        return this;
    }

    Command setDataType(byte type){
        command[11] = type;
        return this;
    }

    public Command setData(byte[] dat){
        byteData = dat;
        return this;
    }

    byte[] create(){
        if(byteData == null || byteData.length == 0){
            setCommandLength(command.length);
            return command;
        }
        byte[] result = new byte[command.length + byteData.length];
        setCommandLength(result.length);
        System.arraycopy(command,0,result,0,12);
        System.arraycopy(byteData,0,result,12,byteData.length);
        result[result.length-2] = 0x00;
        result[result.length-1] = 0x00;
        return result;
    }
}
