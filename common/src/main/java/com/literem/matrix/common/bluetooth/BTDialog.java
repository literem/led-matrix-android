package com.literem.matrix.common.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.common.R;
import com.literem.matrix.common.base.BaseApplication;
import com.literem.matrix.common.base.BasePopupWindow;
import com.literem.matrix.common.dialog.DialogPrompt;

import java.util.Set;

/**
 * author : literem
 * time   : 2022/11/13
 * desc   : 连接蓝牙的弹窗
 *          需要在handle里重写onConnect方法，调用ConnectBluetoothPopupWindow.connectFinish()才能完成连接
 * version: 1.0
 */
public class BTDialog extends BasePopupWindow {

    private BTHandler handler;
    private TextView tvPairCount;
    private ConnectBTAdapter adapter;
    private String strPairCount;
    private boolean isRefresh = false;//是否正在刷新

    public BTDialog(Activity context, BTHandler handler){
        super(context);
        this.handler = handler;
        strPairCount = "已配对设备（%1$d）";
    }

    @Override
    protected int setLayoutId() {
        return R.layout.pop_connect_bluetooth;
    }

    @Override
    protected void initView(View view) {
        adapter = new ConnectBTAdapter(context,onClickListener);
        RecyclerView recyclerView = view.findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        tvPairCount = view.findViewById(R.id.tv_pair_count);
        addClickListener(R.id.iv_refresh);
        addClickListener(R.id.iv_close);
        addClickListener(R.id.tv_pair);
        view.findViewById(R.id.fl_touch).setOnTouchListener(new View.OnTouchListener() {
            float y=0;
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    y = event.getY();
                }else if (event.getAction() == MotionEvent.ACTION_UP){
                    if ((y - event.getY()) > 2) dismiss();
                }
                return true;
            }
        });
    }

    @Override
    protected void initPopupWindow(PopupWindow popupWindow) {
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true); //设置可获取焦点
        popupWindow.setAnimationStyle(R.style.popup_anim_top_bottom);//设置滑入滑出的动画效果
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void onViewClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_close) {
            dismiss();
        }else if(id == R.id.tv_pair){
            dismiss();
            context.startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
        }else if(id == R.id.iv_refresh){
            if(!isRefresh) loadBluetoothDevice();
        }else if(id == R.id.ll_root){
            ConnectBTBean bean = adapter.getBean(view);
            if(bean.isConnect()) {
                showCloseConnectDialog();
            }else{
                startConnect(bean.getDeviceMac());
            }
        }
    }


    /**
     * 加载蓝牙设备
     */
    private void loadBluetoothDevice(){
        isRefresh = true;
        adapter.cleanList();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //过500ms后加载列表
                Set<BluetoothDevice> boundDevice = BTUtils.getInstance().getBoundDevice();
                adapter.setList(boundDevice, BTUtils.getInstance().getDeviceAddress());
                tvPairCount.setText(String.format(strPairCount,boundDevice.size()));
                isRefresh = false;
            }
        },300);
    }

    /**
     * 开始连接蓝牙
     * @param address 蓝牙mac地址
     */
    public void startConnect(String address){
        BaseApplication.sendEmptyMessage(BTData.BTState,BTData.StartConnect,0);
        BTUtils.getInstance().connectBluetooth(address);
    }

    public void closeConnect(){
        BaseApplication.sendEmptyMessage(BTData.BTState,BTData.CloseConnect,0);
        BTUtils.getInstance().close();
    }

    /**
     * 结束蓝牙连接，在蓝牙开始连接后返回结果时调用
     * @param state 状态：true成功，false失败
     */
    public void finishConnect(boolean state){
        if(state){
            adapter.setItemStatus(BTUtils.getInstance().getDeviceAddress(),true);
        }else{
            adapter.setItemStatus(null,false);
        }
    }

    /**
     * 关闭蓝牙连接
     */
    private void showCloseConnectDialog(){
        DialogPrompt
                .builder(context)
                .setTitle("提示")
                .setContent("是否退出蓝牙连接？")
                .setOnSureListener("退出", new DialogPrompt.OnDialogPromptListener() {
                    @Override
                    public void onButtonClick(DialogPrompt dialog) {
                        dismiss();//如果蓝牙列表框打开，则关闭
                        dialog.dismiss();//关闭当前对话框
                        closeConnect();
                    }
                })
                .showDialog();
    }

    //显示
    public void show(){
        if (BTUtils.getInstance().checkBluetooth() != BTData.BT_OK) return;
        if (!isRefresh) loadBluetoothDevice();
        popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.TOP, 0, 0);
    }

}
