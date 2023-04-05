package com.literem.matrix.common.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class BaseApplication extends Application{
    private static Application app;
    private static Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public static Context getContext() {
        return app.getApplicationContext();
    }

    public static void setHandler(Handler handler){
        BaseApplication.handler = handler;
    }

    public static Handler getHandler(){
        return BaseApplication.handler;
    }

    public static void sendEmptyMessage(int what,int arg,int delayMS){
        if (handler != null){
            Message msg = Message.obtain();
            msg.what = what;
            msg.arg1 = arg;
            handler.sendMessageDelayed(msg,delayMS);
        }
    }

    public static void sendMessage(Message msg,int delayMS){
        if (handler != null) handler.sendMessageDelayed(msg,delayMS);
    }
}
