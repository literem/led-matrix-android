package com.literam.matrix.common.utils;

import com.literam.matrix.common.base.BaseApplication;
import com.literam.matrix.common.entity.ResponseResultBean;
import com.literam.matrix.common.entity.ResponseResultList;
import com.literam.matrix.common.font.CommandUtils;

public class ResponseParse {

    /**
     * 检查响应状态是否为RESPONSE_OK
     * @param receive 接收的数据
     * @return 若响应状态为RESPONSE_OK，返回false，否则返回true
     */
    private boolean checkResponseStatus(byte[] receive){
        switch (receive[5]) {
            case CommandUtils.RESPONSE_OK:return false;
            case CommandUtils.WRITE_ERROR:ToastUtils.Show(BaseApplication.getContext(),"数据写入错误");return true;
            case CommandUtils.CMD_ERROR:ToastUtils.Show(BaseApplication.getContext(),"发送的命令有误");return true;
            case CommandUtils.VALUE_ERROR:ToastUtils.Show(BaseApplication.getContext(),"发送的值有误");return true;
            default:return true;
        }
    }

    /**
     * 检查响应数据是否正确，检查起始、结束位、响应长度
     * 起始位：第0、1位
     * 结束位：第len-1、len-2位
     * number：第2、3位
     * 长度：第4位
     * 状态码：第5位
     * 数据起始位(dataBit):6
     * 一条数据：三个字节，第dataBit位:flag;第dataBit+1,dataBit+2
     */
    public boolean checkResponseData(byte[] receive){
        //检查起始和结束位
        if((receive[0] & receive[1]) != CommandUtils.START_BIT || (receive[receive.length - 1] & receive[receive.length - 2]) != CommandUtils.START_BIT) return true;

        //判断响应长度和实际长度是否匹配
        return receive.length != receive[4];
    }

    //public byte getResponseStatus(byte[] receive){ return receive[5]; }

    public byte getResponseDataType(byte[] receive){
        return receive[6];
    }


    public boolean checkResponseNumber(byte[] receive,int originalNumber){
        int highBit = receive[2] & 0xff;
        int lowBit = receive[3] & 0xff;
        return ((highBit << 8) + lowBit) == originalNumber;
    }

    private int get2ByteToInt(byte highBit, byte lowBit){
        int h = highBit & 0xff;
        int l = lowBit & 0xff;
        return (h << 8) + l;
    }

    public ResponseResultList parseResponseData(byte[] receive){
        int actualDataLen = receive.length - 9;
        if(actualDataLen <= 0 || actualDataLen % 3 != 0) return null;//一条数据3个字节，若长度不对，则返回
        actualDataLen /= 3;
        ResponseResultList list = new ResponseResultList(actualDataLen);
        for (int i=7,j=0; j<actualDataLen; i+=3,j++) {
            list.add(receive[i],get2ByteToInt(receive[i+1],receive[i+2]));
        }
        return list;
    }

    public ResponseResultBean parseResponseOneData(byte[] receive){
        int actualDataLen = receive.length - 9;
        ResponseResultBean bean = new ResponseResultBean();
        if(actualDataLen != 3 || checkResponseStatus(receive)) {//一条数据3个字节，若长度不对，则设置0
            return bean;
        }else{
            bean.setFlag(receive[7]);
            bean.setData(get2ByteToInt(receive[8],receive[9]));
        }
        return bean;
    }
}
