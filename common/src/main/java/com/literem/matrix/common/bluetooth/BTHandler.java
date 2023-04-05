package com.literem.matrix.common.bluetooth;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.literem.matrix.common.entity.SendDataQueue;
import com.literem.matrix.common.utils.ResponseParse;
import com.literem.matrix.common.utils.ToastUtils;

import java.lang.ref.WeakReference;

public abstract class BTHandler<T extends Activity> extends Handler {

    private WeakReference<T> weakReference;
    private boolean isCanSend; //是否能发送数据，用来约束sendData方法的
    private int sendNumber = 0;
    private long lastTime=0;
    public SendDataQueue queue;//一个先进先出的队列
    public ResponseParse responseParse;

    public BTHandler(Looper looper, T activity){
        super(looper);
        queue = new SendDataQueue(10);
        isCanSend = true;
        weakReference = new WeakReference<>(activity);
        responseParse = new ResponseParse();
    }

    public void sendOrAddQueue(byte[] bytes){
        if(isCanSend) {//若能发直接发送数据
            BTUtils.getInstance().send(bytes);
        } else {//不能发，查看是否超时
            if((System.currentTimeMillis()-lastTime) > 5000){
                BTUtils.getInstance().send(bytes);//超时了直接发送数据
            }else{
                queue.addQueue(bytes);//若没有超时，添加到队列中
            }
        }
    }

    public void sendNextData(){
        if (queue.size() != 0){
            BTUtils.getInstance().send(queue.popQueue());
        }
    }

    /***************** 处理消息的入口方法 ******************/
    @Override
    public final void handleMessage(@NonNull Message msg) {
        T activity = weakReference.get();
        switch (msg.what){
            case BTData.SendState:
                if(msg.arg1 == BTData.Success)
                    onSendSuccess(msg.obj,msg.arg2,activity);//发送成功
                else
                    onSendFailed(msg.obj,activity);//发送失败
                break;
            case BTData.Receive: if(msg.obj!=null) onReceive((byte[])msg.obj,activity);break;//收到数据
            case BTData.BTState: onConnectState(msg.arg1,activity);break;//蓝牙状态
            default:onHandle(msg.what,msg.obj,activity);break;//处理其他的
        }
    }

    /***************** 处理回调，可选择重写 ******************/

    //数据处理，必须重写
    protected abstract void onHandle(int what, Object obj, T activity);

    //蓝牙连接状态改变
    protected void onConnectState(int stateFlag,T activity){
        switch (stateFlag){
            case BTData.Success:
                BTUtils.getInstance().listener();//启动监听
                ToastUtils.Show(activity,"蓝牙连接成功");
                break;
            case BTData.Failed: ToastUtils.Show(activity,"蓝牙连接失败");break;//连接失败
            case BTData.NoConnect: ToastUtils.Show(activity,"蓝牙未连接");break;//未连接
            case BTData.Disconnect: ToastUtils.Show(activity,"蓝牙已断开连接");break;//断开连接
            case BTData.NoEnable: ToastUtils.Show(activity,"蓝牙已关闭");break;//未打开蓝牙
        }
    }

    //接收到数据调用的函数，可以选择重写
    protected boolean onReceive(@NonNull byte[] receive, T activity){
        isCanSend = true;
        if (responseParse.checkResponseData(receive)){
            ToastUtils.Show(activity,"收到的数据格式有误！");
            return false;
        }
        if (responseParse.checkResponseNumber(receive, sendNumber)){//检查响应数据和发送数据是否正确
            sendNextData();
            return true;
        }else{
            return false;
        }
    }

    //发送成功调用的函数，可以选择重写
    protected void onSendSuccess(Object msg,int result, T activity){
        this.sendNumber = result;
        isCanSend = false;//数据发送成功，没收到回应之前，不能放开
        lastTime = System.currentTimeMillis();
    }

    //发送失败调用的函数，可以选择重写
    protected void onSendFailed(Object obj, T activity){
        ToastUtils.Show(activity,"发送失败");
    }

}
