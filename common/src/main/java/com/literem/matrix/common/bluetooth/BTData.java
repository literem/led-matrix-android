package com.literem.matrix.common.bluetooth;

public class BTData {

    static final int Failed = 0;
    public static final int Success = 1;

    //蓝牙状态码
    static final int NoConnect = 20;
    static final int Disconnect = 21;
    static final int NoEnable = 22;
    public static final int StartConnect = 23;
    public static final int CloseConnect = 24;


    static final int Receive = 30;
    static final int SendState = 31;
    static final int BTState = 32;
    public static final int TOGGLE = 34;

    //蓝牙操作的代码
    public static final int OpenBluetoothCode = 2001;
    public static final int BT_NONE = 2002;
    public static final int BT_CLOSE = 2003;
    static final int BT_OK = 2004;
}
