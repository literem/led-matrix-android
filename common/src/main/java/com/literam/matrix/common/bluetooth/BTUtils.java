package com.literam.matrix.common.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Message;

import com.literam.matrix.common.base.BaseApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BTUtils {
    private static final UUID BluetoothUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final Object lock = new Object();
    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice bluetoothDevice;
    private BluetoothAdapter mBluetoothAdapter;
    private SendDataRunnable sendDataRunnable;
    private ExecutorService executorService;

    private boolean isOpenBT = false;//是否打开蓝牙
    private boolean isConnect = false;//是否连接蓝牙设备

    private static class BluetoothHelperInner{
        private static BTUtils singleton = new BTUtils();
    }

    public static BTUtils getInstance(){
        return BluetoothHelperInner.singleton;
    }

    private BTUtils() {
        sendDataRunnable = new SendDataRunnable();
        executorService = Executors.newSingleThreadExecutor();
        BluetoothManager manager = (BluetoothManager) BaseApplication.getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();
    }

    public String getDeviceName(){
        return bluetoothSocket.getRemoteDevice().getName();
    }

    /**
     * 获取蓝牙地址
     * @return string address
     */
    public String getDeviceAddress(){
        if(!getConnect() || bluetoothSocket==null || !bluetoothSocket.isConnected()) return null;
        return bluetoothSocket.getRemoteDevice().getAddress();
    }

    /**
     * 获取连接状态
     * @return true：已连接
     */
    public boolean getConnect(){
        if(isOpenBT && bluetoothSocket != null)
            return bluetoothSocket.isConnected();
        return false;
    }

    /**
     * 初始化蓝牙
     */
    public int checkBluetooth(){
        if(mBluetoothAdapter == null){//为空表示手机上蓝牙功能无效
            sendEmptyMessage(BTData.BTState,BTData.NoEnable);
            isOpenBT = false;
            return BTData.BT_NONE;
        }
        if(!mBluetoothAdapter.isEnabled()){//未打开蓝牙
            sendEmptyMessage(BTData.BTState,BTData.NoEnable);
            isOpenBT = false;
            return BTData.BT_CLOSE;
        }
        isOpenBT = true;
        return BTData.BT_OK;
    }

    /**
     * 获取已绑定的蓝牙设备
     */
    Set<BluetoothDevice> getBoundDevice(){
        return mBluetoothAdapter.getBondedDevices(); //获取已配对蓝牙设备
    }

    /**
     * 连接蓝牙
     * @param address mac address
     */
    void connectBluetooth(String address){
        if (checkBluetooth() == BTData.BT_OK) {
            connect(mBluetoothAdapter.getRemoteDevice(address));
        }else{
            sendEmptyMessage(BTData.BTState,BTData.Failed);
        }
    }


    //发送消息空消息
    private void sendEmptyMessage(int what,int arg1){
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = arg1;
        BaseApplication.sendMessage(msg,0);
    }

    //发送蓝牙状态
    void sendBTState(boolean state,int flag,int time){
        this.isOpenBT = state;
        if(flag == 0) return;
        Message msg = Message.obtain();
        msg.what = BTData.BTState;
        msg.arg1 = flag;
        if(time == 0)
            BaseApplication.sendMessage(msg,0);
        else
            BaseApplication.sendMessage(msg,time);
    }


    /**
     * 发送蓝牙数据
     * @param str 字符串数据
     */
    public void send(String str){
        if(!isConnect) {
            sendEmptyMessage(BTData.BTState,BTData.NoConnect);
            return;
        }
        if(str == null || str.isEmpty()){
            sendEmptyMessage(BTData.SendState,BTData.Failed);
            return;
        }
        sendDataRunnable.setString(str);
        executorService.execute(sendDataRunnable);
    }

    /**
     * 发送蓝牙数据
     * @param bytes 字节数组
     */
    public void send(byte[] bytes){
        if(!isConnect) {
            sendEmptyMessage(BTData.BTState,BTData.NoConnect);
            return;
        }
        if(bytes == null ||bytes.length == 0){
            sendEmptyMessage(BTData.SendState,BTData.Failed);
            return;
        }

        sendDataRunnable.setByte(bytes);
        executorService.execute(sendDataRunnable);
    }

    class SendDataRunnable implements Runnable{
        private byte[] send_byte;
        private Random random;

        SendDataRunnable(){
            random = new Random();
        }

        void setByte(byte[] b){
            this.send_byte = b;
        }

        void setString(String str){
            this.send_byte = str.getBytes();
        }

        @Override
        public void run() {
            try {
                int r = random.nextInt(50000) + 10000;
                send_byte[2] = (byte) ((r >>8) & 0x00ff);
                send_byte[3] = (byte) (r & 0x00ff);
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                outputStream.write(send_byte);
                outputStream.flush();
                Message msg = Message.obtain();
                msg.what = BTData.SendState;
                msg.arg1 = BTData.Success;
                msg.arg2 = r;
                msg.obj = send_byte;
                BaseApplication.sendMessage(msg,0);
            } catch (IOException e) {
                //e.printStackTrace();
                sendEmptyMessage(BTData.SendState,BTData.Failed);
            }
        }
    }

    /**
     * 连接蓝牙
     * @param device 要连接的蓝牙设备对象
     */
    private void connect(BluetoothDevice device){
        isConnect = false;
        try {
            bluetoothDevice = device;
            bluetoothSocket = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(BluetoothUUID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (bluetoothSocket != null && isConnect) {
                    cancelConnect();
                    isConnect = false;
                }
                try {
                    synchronized (lock) {
                        bluetoothSocket.connect();
                        isConnect = true;
                    }
                }catch (Exception e) {
                    connectByInvoke();
                }
                //设置连接失败提醒（若连接成功则通过系统广播的形式发出）
                if(!isConnect) sendEmptyMessage(BTData.BTState, BTData.Failed);
            }
        };
        executorService.execute(runnable);
    }

    /**
     * 关闭蓝牙，注意，关闭成功后通过系统广播发出
     */
    public void close(){
        isConnect = false;
        try {
            if(bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            sendEmptyMessage(BTData.BTState,BTData.Failed);
        }
    }

    /**
     * 监听数据
     */
    public void listener(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream inputStream = bluetoothSocket.getInputStream();
                    byte[] buffer = new byte[1024];
                    int bytes;
                    while(isConnect){
                        bytes = inputStream.read(buffer);
                        if(bytes > 0){
                            byte[] data = new byte[bytes];
                            System.arraycopy(buffer,0,data,0,bytes);
                            Message msg = Message.obtain();
                            msg.obj = data;
                            msg.what = BTData.Receive;
                            BaseApplication.sendMessage(msg,0);
                        }
                    }

                } catch (IOException e) {
                    //e.printStackTrace();
                    isConnect = false;
                    //sendEmptyMessage(BTData.BTState,BTData.Disconnect);
                }
            }
        }).start();
    }

    private void connectByInvoke() {
        //取消连接
        cancelConnect();

        //通过反射调用连接
        try {
            Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", int.class);
            bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
            if(bluetoothSocket != null){
                bluetoothSocket.connect();
                isConnect = true;
            }
        } catch (Exception ex) {
            //ex.printStackTrace();
            sendEmptyMessage(BTData.BTState,BTData.Failed);
        }
    }

    private void cancelConnect() {
        try {
            synchronized (lock) {
                if (isConnect) {
                    bluetoothSocket.close();
                    isConnect = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
