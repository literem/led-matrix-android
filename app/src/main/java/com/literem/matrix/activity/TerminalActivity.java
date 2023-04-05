package com.literem.matrix.activity;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.R;
import com.literem.matrix.adapter.TerminalAdapter;
import com.literem.matrix.common.base.BaseActivity;
import com.literem.matrix.common.base.BaseApplication;
import com.literem.matrix.common.bluetooth.BTData;
import com.literem.matrix.common.bluetooth.BTDialog;
import com.literem.matrix.common.bluetooth.BTHandler;
import com.literem.matrix.common.bluetooth.BTUtils;
import com.literem.matrix.common.data.DataUtils;
import com.literem.matrix.common.dialog.DialogPrompt;
import com.literem.matrix.common.utils.DisplayUtil;
import com.literem.matrix.common.utils.HiddenAnimUtils;
import com.literem.matrix.common.utils.ShowLoadingUtils;
import com.literem.matrix.common.utils.SoftKeyboardListener;
import com.literem.matrix.common.utils.ToastUtils;


public class TerminalActivity extends BaseActivity {

    private EditText etInput;
    private RecyclerView recyclerView;
    private TerminalAdapter adapter;
    private TextView tvName,tvMac;
    private Button btnConnect;
    private FrameLayout flBluetoothShow;
    private FrameLayout llBluetooth;
    private HiddenAnimUtils hiddenAnimUtils;
    private BTDialog btDialog;
    private TerminalHandler handler;
    private int llBluetoothHeight;
    private ShowLoadingUtils showLoading;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_terminal;
    }

    @Override
    protected void onInit() {
        llBluetoothHeight = DisplayUtil.dip2px(this,50);
        handler = new TerminalHandler(getMainLooper(),this);
        BaseApplication.setHandler(handler);
    }

    @Override
    protected void onInitView() {
        setToolbarTitle("蓝牙数据收发");
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(toolbar_MenuItemClickListener);

        adapter = new TerminalAdapter(this);
        recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        //adapter.initData();

        flBluetoothShow = findAndAddOnClickListener(R.id.fl_bluetooth_show);
        // Android是消息驱动的模式，View.post的Runnable任务会被加入任务队列，
        // 并且等待第一次TraversalRunnable执行结束后才执行，此时已经执行过一次measure、layout过程了，
        // 所以在后面执行post的Runnable时，已经有measure的结果，因此此时可以获取到View的宽高
        llBluetooth = findViewById(R.id.ll_bluetooth);
        llBluetooth.measure(0,0);
        llBluetooth.post(new Runnable() {
            @Override
            public void run() {
                int width = llBluetooth.getMeasuredWidth();
                //获取需要平铺展开的控件高度
                hiddenAnimUtils = HiddenAnimUtils.newInstance(llBluetooth,flBluetoothShow,width);
                //llBluetooth.setVisibility(View.GONE);

                boolean status = BTUtils.getInstance().getConnect();
                //如果已经连接蓝牙就隐藏图标
                if(status){
                    hiddenAnimUtils.toggle();
                }
                setConnectStatus(status);
            }
        });
        tvName = findViewById(R.id.tv_terminal_device_name);
        tvMac = findViewById(R.id.tv_terminal_device_mac);
        btnConnect = findAndAddOnClickListener(R.id.btn_terminal_device_connect);

        etInput = findViewById(R.id.et_terminal_input);
        addOnClickListener(R.id.tv_send);

        //软键盘显示/隐藏监听
        SoftKeyboardListener softKeyBoardListener = new SoftKeyboardListener(this);
        softKeyBoardListener.setListener(new SoftKeyboardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                recyclerView.scrollToPosition(adapter.getItemCount()-1);
            }
            @Override
            public void keyBoardHide(int height) {}
        });
    }

    @Override
    protected void onViewClick(View view) {
        switch (view.getId()){
            case R.id.tv_send: send();break;//发送
            case R.id.fl_bluetooth_show: hiddenAnimUtils.toggle();break;//蓝牙图标
            case R.id.btn_terminal_device_connect:connectButtonClick();break;//连接按钮
        }
    }

    @SuppressLint("SetTextI18n")
    private void setConnectStatus(boolean status){
        //设置可变宽（执行动画后就会变为固定宽）
        llBluetooth.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, llBluetoothHeight));
        if(status){
            flBluetoothShow.setBackgroundResource(R.drawable.bg_circle_50_red);
            btnConnect.setBackgroundResource(R.drawable.bg_circle_10_red);
            btnConnect.setText("已连接");
            tvName.setText(BTUtils.getInstance().getDeviceName());
            tvMac.setText(BTUtils.getInstance().getDeviceAddress());
        }else{
            flBluetoothShow.setBackgroundResource(R.drawable.bg_circle_50_grey);
            btnConnect.setBackgroundResource(R.drawable.bg_circle_10_grey);
            btnConnect.setText("未连接");
            tvName.setText("请连接蓝牙设备");
            tvMac.setText("00-00-00");
        }
    }

    private void send(){
        String input = etInput.getText().toString().trim();
        if(input.length() == 0){
            ToastUtils.Show(this,"输入的内容不能为空！");
            return;
        }
        BTUtils.getInstance().send(input);
    }

    private void cleanData(){
        adapter.clean();
        ToastUtils.Show(this,"已清空全部数据");
    }

    private void connectButtonClick(){
        if(btDialog == null){
            btDialog = new BTDialog(TerminalActivity.this,handler);
        }
        if(BTUtils.getInstance().getConnect()){
            btDialog.closeConnect();
        }else{
            btDialog.show();
        }
    }

    private Toolbar.OnMenuItemClickListener toolbar_MenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if(item.getItemId() == R.id.menu_terminal_clean){
                DialogPrompt.builder(TerminalActivity.this)
                        .setContent("确定要清空数据吗？")
                        .setOnSureListener(null, new DialogPrompt.OnDialogPromptListener() {
                            @Override
                            public void onButtonClick(DialogPrompt dialog) {
                                cleanData();
                            }
                        })
                        .showDialog();
            }
            return true;
        }
    };

    @Override
    public boolean onCreatePanelMenu(int featureId, @NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_terminal, menu);//toolbar添加menu菜单
        return true;
    }

    public void showLoadingAndDelayDismiss(String text,int delayMS){
        if (showLoading == null) showLoading = new ShowLoadingUtils(this);
        showLoading.show(text);
        handler.sendEmptyMessageDelayed(DataUtils.DISMISS_LOADING,delayMS);
    }

    private class TerminalHandler extends BTHandler<TerminalActivity>{

        TerminalHandler(Looper looper, TerminalActivity activity) {
            super(looper, activity);
        }

        @Override
        protected boolean onReceive(@NonNull byte[] receive, TerminalActivity activity) {
            if(!super.onReceive(receive, activity)) return false;//无意义返回
            adapter.addOne(TerminalAdapter.RECEIVE,new String(receive));
            activity.recyclerView.scrollToPosition(adapter.getItemCount()-1);
            return true;
        }

        @Override
        protected void onSendSuccess(Object msg,int result ,TerminalActivity activity) {
            adapter.addOne(TerminalAdapter.SEND,new String((byte[]) msg));
            activity.etInput.setText("");
            activity.recyclerView.scrollToPosition(adapter.getItemCount()-1);
        }

        @Override
        protected void onConnectState(int stateFlag, TerminalActivity activity) {
            super.onConnectState(stateFlag,activity);
            if (stateFlag == BTData.StartConnect){//开始连接蓝牙
                showLoadingAndDelayDismiss("蓝牙连接中...",10000);
                return;
            }else if(stateFlag == BTData.CloseConnect) {//正在关闭蓝牙
                showLoadingAndDelayDismiss("断开连接中...",10000);
                return;
            }

            if (showLoading != null) showLoading.dismiss();
            boolean state = stateFlag == BTData.Success;
            activity.setConnectStatus(state);
            activity.btDialog.finishConnect(state);
        }

        @Override
        protected void onHandle(int what, Object obj, TerminalActivity activity) {}
    }

}
