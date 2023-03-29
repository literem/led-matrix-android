package com.literam.matrix.common.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.literam.matrix.common.utils.ToastUtils;

/**
 * author : literem
 * time   : 2022/11/19
 * desc   : 蓝牙监听广播
 * version: 1.0
 */
public class BTMonitorReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;
        switch (action) {
            case BluetoothDevice.ACTION_ACL_CONNECTED://蓝牙已连接
                BTUtils.getInstance().sendBTState(true, BTData.Success,1000);
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED://蓝牙断开
                BTUtils.getInstance().sendBTState(false, BTData.Disconnect,0);
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                if (blueState == BluetoothAdapter.STATE_OFF) {//蓝牙已关闭
                    BTUtils.getInstance().sendBTState(false,BTData.NoEnable,0);
                } else if (blueState == BluetoothAdapter.STATE_ON) {//蓝牙已打开
                    BTUtils.getInstance().sendBTState(true,0,0);//蓝牙打开，不需要发送handle事件
                }
                break;
        }
    }
}
