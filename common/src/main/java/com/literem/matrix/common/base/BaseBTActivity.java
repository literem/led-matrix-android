package com.literem.matrix.common.base;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.literem.matrix.common.R;
import com.literem.matrix.common.bluetooth.BTHandler;
import com.literem.matrix.common.bluetooth.BTUtils;
import com.literem.matrix.common.utils.ShowLoadingUtils;
import com.literem.matrix.common.utils.StatusBarUtil;
import com.literem.matrix.common.utils.ToastUtils;

@SuppressLint("Registered")
public abstract class BaseBTActivity extends AppCompatActivity {

    private final int DELAY_SEND_QUEUE = 213;
    protected final int DELAY_LOADING_DISMISS = 216;
    protected final int DELAY_LOADING_DISMISS_TEXT = 217;
    protected final int DELAY_SEND_DATA = 218;
    public BaseBTHandler handler;
    private ShowLoadingUtils loadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayoutId());
        initStatusBar();
        handler = new BaseBTHandler(getMainLooper(),this);
        BaseApplication.setHandler(handler);
        onInitView();
        onInit();
    }

    protected abstract int setLayoutId();
    protected abstract void onInitView();
    protected abstract void onInit();
    protected abstract void onViewClick(View view);

    public AppCompatActivity getContext(){
        return this;
    }

    public void initStatusBar() {
        int color = getResources().getColor(R.color.white);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        StatusBarUtil.setColor(this, color, 0);
        mToolbar.setBackgroundColor(color);
        StatusBarUtil.setLightMode(this);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);//隐藏默认标题
            if(isShowBackKey()) {//显示返回键
                actionBar.setDisplayHomeAsUpEnabled(true);//添加默认的返回图标
                actionBar.setHomeButtonEnabled(true); //设置返回键可用
            }
        }
    }

    public void setTitle(String title){
        TextView tvTitle = findViewById(R.id.toolbar_title);
        if(title != null && tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    public boolean isShowBackKey(){
        return true;
    }

    //锁定字体大小
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics());
        return res;
    }

    /**
     * 添加点击事件
     */
    protected void addOnClickListener(View view){
        if (clickListener == null) return;
        view.setOnClickListener(clickListener);
    }

    protected void addOnClickListener(int viewID){
        if (clickListener == null) return;
        findViewById(viewID).setOnClickListener(clickListener);
    }

    /**
     * 显示LoadingDialog，设置超时关闭时间
     */
    protected void showLoadingDelayDismiss(String text){
        if (loadingDialog == null) this.loadingDialog = new ShowLoadingUtils(this);
        loadingDialog.show(text);
        handler.sendEmptyMessageDelayed(DELAY_LOADING_DISMISS, 1500);
    }

    protected <T extends View> T findAndAddOnClickListener(int viewID){
        if (clickListener != null){
            findViewById(viewID).setOnClickListener(clickListener);
        }
        return findViewById(viewID);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            onToolbarBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 退出方法，当点击返回键或者点击toolbar的返回图标时调用该方法
     */
    protected void onToolbarBackPressed(){
        super.onBackPressed();
    }

    protected View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onViewClick(v);
        }
    };

    public class BaseBTHandler extends BTHandler<BaseBTActivity> {
        BaseBTHandler(Looper looper, BaseBTActivity activity){
            super(looper,activity);
        }

        @Override
        protected void onSendFailed(Object obj, BaseBTActivity activity) {
            super.onSendFailed(obj,activity);
            activity.handlerSendFailed();
        }

        @Override
        protected void onConnectState(int stateFlag, BaseBTActivity activity) {
            super.onConnectState(stateFlag,activity);
        }

        @Override
        protected boolean onReceive(@NonNull byte[] receive, BaseBTActivity activity) {
            if(super.onReceive(receive,activity)){
                handlerReceive(receive);//处理数据正确了，才调用该方法
            }
            return false;//无意义的返回值
        }

        @Override
        protected void onSendSuccess(Object msg, int result, BaseBTActivity activity) {
            super.onSendSuccess(msg,result,activity);
            activity.handleSendSuccess(msg,result);
        }

        @Override
        protected void onHandle(int what, Object obj, BaseBTActivity activity) {
            activity.handlerData(what,obj);
        }
    }

    protected void handleSendSuccess(Object msg, int result){}

    protected void handlerData(int what, Object obj){
        switch (what){
            case DELAY_SEND_QUEUE:
                handler.sendNextData();
                break;

            case DELAY_LOADING_DISMISS:
                if (loadingDialog != null) loadingDialog.dismiss();
                break;

            case DELAY_LOADING_DISMISS_TEXT:
                if (loadingDialog != null) loadingDialog.dismiss();
                ToastUtils.Show(this,obj.toString());
                break;

            case DELAY_SEND_DATA:
                if (obj instanceof byte[]){
                    byte[] b = (byte[]) obj;
                    BTUtils.getInstance().send(b);
                }
                break;
        }
    }

    /**
     * 处理接收的数据，返回的数据若不正确，不会执行此方法
     */
    protected void handlerReceive(@NonNull byte[] receive){}

    protected void handlerSendFailed(){}

    protected void appendToQueue(byte[] bytes){
        handler.queue.addQueue(bytes);
    }

    protected void clearQueue(){
        handler.queue.clearQueue();
    }

    protected void sendQueue(int delayMs){
        if (delayMs == 0){
            handler.sendNextData();
        }else{
            handler.sendEmptyMessageDelayed(DELAY_SEND_QUEUE,delayMs);
        }
    }

    protected void sendDataDelay(byte[] bytes,int delayMS){
        Message msg = Message.obtain();
        msg.obj = bytes;
        msg.what = DELAY_SEND_DATA;
        handler.sendMessageDelayed(msg,delayMS);
    }

    @Override
    public void onBackPressed() {
        if (onExit()) super.onBackPressed();
    }

    /**
     * 退出方法，当点击返回键或者点击toolbar的返回图标时调用该方法
     * @return true：关闭页面，false，无任何操作
     */
    protected boolean onExit(){
        return true;
    }
}
