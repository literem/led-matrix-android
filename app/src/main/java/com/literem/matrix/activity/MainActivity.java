package com.literem.matrix.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.literem.matrix.R;
import com.literem.matrix.common.base.BaseApplication;
import com.literem.matrix.common.bluetooth.BTMonitorReceiver;
import com.literem.matrix.common.bluetooth.BTData;
import com.literem.matrix.common.bluetooth.BTHandler;
import com.literem.matrix.common.bluetooth.BTUtils;
import com.literem.matrix.common.data.DataUtils;
import com.literem.matrix.common.dialog.DialogPrompt;
import com.literem.matrix.common.dialog.DialogWarn;
import com.literem.matrix.common.font.CommandUtils;
import com.literem.matrix.common.utils.ShowLoadingUtils;
import com.literem.matrix.fragment.MainHomeFragment;
import com.literem.matrix.fragment.MainMoreFragment;
import com.literem.matrix.common.utils.StatusBarUtil;
import com.literem.matrix.common.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {

    private static final int TAB_Home = 1;
    private static final int TAB_Mode = 2;
    private TextView tvTitle;
    private TextView tvHome,tvMore,tvCurrent;
    private ImageView ivHome,ivMore,ivCurrent;
    private FragmentManager fragmentManager;
    private MainMoreFragment moreFragment;
    private MainHomeFragment homeFragment;
    private MainActivityHandler handler;
    private int currentIndex = 0; //当前选中的页面
    private ShowLoadingUtils showLoading;
    private BTMonitorReceiver btListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();//设置顶部控件填充到状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
        StatusBarUtil.setLightMode(this);
        StatusBarUtil.setColor(this,Color.WHITE,0);
        handler = new MainActivityHandler(getMainLooper(),this);
        showLoading = new ShowLoadingUtils(this);
        this.btListener = new BTMonitorReceiver();//蓝牙打开或关闭的监听
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);//监视蓝牙连接的状态
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);//监视蓝牙断开连接的状态
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);//监视蓝牙关闭和打开的状态
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_OFF");
        intentFilter.addAction("android.bluetooth.BluetoothAdapter.STATE_ON");
        registerReceiver(this.btListener, intentFilter); // 注册广播
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //6.0才用动态权限，如果不是6.0，直接检查蓝牙功能
                if (Build.VERSION.SDK_INT >= 23) {
                    initPermission();
                }else{
                    checkBluetooth();//检查蓝牙是否开启
                }
            }
        },1000);

        initView();
        fragmentManager = getSupportFragmentManager();
        setTabSelection(TAB_Home);
    }

    private void initView() {
        tvHome = findViewById(R.id.tv_main_home);
        tvMore = findViewById(R.id.tv_main_more);
        ivHome = findViewById(R.id.iv_main_home);
        ivMore = findViewById(R.id.iv_main_more);
        tvTitle = findViewById(R.id.toolbar_title);
        ivCurrent = ivHome;
        tvCurrent = tvHome;
        findViewById(R.id.llHome).setOnClickListener(onClickListener);
        findViewById(R.id.llMode).setOnClickListener(onClickListener);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.llHome:// 首页
                    if(currentIndex == TAB_Home) return;
                    setTabSelection(TAB_Home);
                    break;
                case R.id.llMode:// 功能
                    if(currentIndex == TAB_Mode) return;
                    setTabSelection(TAB_Mode);
                    break;
            }
        }
    };

    /* -------------------- Fragment 控制 --------------------- */
    /**
     * 根据传入的index参数来设置选中的tab页。 每个tab页对应的下标。
     */
    private void setTabSelection(int index) {
        ivCurrent.setSelected(false);tvCurrent.setSelected(false);//清除选中状态
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();// 开启事务
        hideFragments(fragmentTransaction);//隐藏所有Fragment
        switch (index) {
            case TAB_Home:
                tvTitle.setText("首页");
                currentIndex = TAB_Home;
                selectedSelection(TAB_Home);
                if (homeFragment == null) {// 如果Fragment为空，则创建一个并添加到界面上
                    homeFragment = new MainHomeFragment(this);
                    //callBackHome = (OnActivityCallBack) homeFragment;
                    fragmentTransaction.add(R.id.main_fragment, homeFragment,"home");
                } else {
                    fragmentTransaction.show(homeFragment);// 如果Fragment不为空，则直接将它显示出来
                }
                fragmentTransaction.commitNowAllowingStateLoss();
                homeFragment.updateConnectStatus();
                break;
            case TAB_Mode:
                tvTitle.setText("功能");
                currentIndex = TAB_Mode;
                selectedSelection(TAB_Mode);
                if (moreFragment == null) {
                    moreFragment = new MainMoreFragment(this);
                    fragmentTransaction.add(R.id.main_fragment, moreFragment);
                } else {
                    fragmentTransaction.show(moreFragment);
                }
                fragmentTransaction.commitNowAllowingStateLoss();
                break;
        }
    }
    /**
     * 设置选中项样式
     */
    private void selectedSelection(int index) {
        switch (index) {
            case TAB_Home:
                ivCurrent = ivHome;
                tvCurrent = tvHome;
                ivCurrent.setSelected(true);
                tvCurrent.setSelected(true);
                break;
            case TAB_Mode:
                ivCurrent = ivMore;
                tvCurrent = tvMore;
                ivCurrent.setSelected(true);
                tvCurrent.setSelected(true);
                break;
        }
    }

    /**
     * 将所有的Fragment都置为隐藏状态。
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (homeFragment != null) {
            transaction.hide(homeFragment);
        }
        if (moreFragment != null) {
            transaction.hide(moreFragment);
        }
    }

    public MainActivityHandler getMainHandler(){
        return handler;
    }

    private long exitTime = 0;
    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            ToastUtils.Show(this,"再按一次退出");
            exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(BTData.OpenBluetoothCode == requestCode){//要打开蓝牙的弹窗，返回码
            if(resultCode == 0){//拒绝打开
                DialogWarn.builder(this).showDialog("未开启蓝牙功能将无法使用");
            }
        }
    }

    /* -------------------- 权限检查 --------------------- */
    private void checkBluetooth(){
        int status = BTUtils.getInstance().checkBluetooth();
        if(status == BTData.BT_NONE){
            DialogWarn.builder(this).showDialog("此设备没有蓝牙或蓝牙功能无效");
        }else if(status == BTData.BT_CLOSE){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                DialogWarn.builder(this).showDialog("蓝牙打开失败，请手动打开蓝牙");
            }else{
                Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enabler,BTData.OpenBluetoothCode);
            }
        }
    }

    private final int mRequestCode = 100;//权限请求码
    //权限判断和申请
    private void initPermission() {
        List<String> mPermissionList = new ArrayList<>();//未授予的权限存储到mPermissionList中
        String[] permissions;//把需要的权限都放在数组里面
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            permissions = new String[]{//把需要的权限都放在数组里面
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
            };
        }else{
            permissions = new String[]{//把需要的权限都放在数组里面
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }
        //逐个判断权限是否已经通过
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permission);//添加还未授予的权限
            }
        }

        //如果有不通过的权限，则申请权限
        if (mPermissionList.size() > 0) {
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }else{
            // 否则说明权限都已经通过
            checkBluetooth();
        }
    }

    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length == 0) return;
        if (mRequestCode != requestCode)return;

        boolean hasPermissionDismiss = false;//有权限没有通过
        for (int grantResult : grantResults) {
            if (grantResult == -1) {
                hasPermissionDismiss = true;
            }
        }
        //全部权限通过，进行蓝牙功能检测
        if (!hasPermissionDismiss) {
            checkBluetooth();
            return;
        }

        //跳转到系统设置权限页面
        DialogPrompt
                .builder(this)
                .setTitle("提示")
                .setContent("已禁用权限，请手动授予")
                .setOnSureListener("设置", new DialogPrompt.OnDialogPromptListener() {
                    @Override
                    public void onButtonClick(DialogPrompt dialog) {
                        dialog.dismiss();

                        //跳转到权限页面
                        Uri packageURI = Uri.parse("package:" + getPackageName());
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        startActivity(intent);
                        finish();
                    }
                })
                .setOnCancelListener("退出", new DialogPrompt.OnDialogPromptListener() {
                    @Override
                    public void onButtonClick(DialogPrompt dialog) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .showDialog();
    }

    public class MainActivityHandler extends BTHandler<MainActivity> {

        MainActivityHandler(Looper looper, MainActivity activity){
            super(looper,activity);
        }

        @Override
        protected void onConnectState(int stateFlag, MainActivity activity) {
            super.onConnectState(stateFlag,activity);
            if (stateFlag == BTData.StartConnect){//开始连接蓝牙
                showLoadingAndDelayDismiss("蓝牙连接中...",10000);
                return;
            }else if(stateFlag == BTData.CloseConnect) {//正在关闭蓝牙
                showLoadingAndDelayDismiss("断开连接中...",10000);
                return;
            }
            showLoading.dismiss();
            MainHomeFragment homeFragment = (MainHomeFragment) activity.fragmentManager.findFragmentByTag("home");
            if(homeFragment == null) return;
            if(stateFlag == BTData.Success){
                homeFragment.setConnectState(true);
                byte[] b = CommandUtils.getInstance().getDeviceInfo(CommandUtils.ALL_INFO);
                sendOrAddQueue(b);//连接成功后加载设备信息
            }else{
                homeFragment.setConnectState(false);
            }
        }

        @Override
        protected boolean onReceive(@NonNull byte[] receive, MainActivity activity) {
            if(!super.onReceive(receive,activity)) return false;//返回值无意义
            if(homeFragment == null) return false;
            if(responseParse.getResponseDataType(receive) == CommandUtils.ALL_INFO){
                homeFragment.setDeviceInfo(responseParse.parseResponseData(receive));
            }else{
                homeFragment.setDeviceInfo(responseParse.parseResponseOneData(receive));
            }
            return true;
        }

        @Override
        protected void onHandle(int what, Object obj, MainActivity activity) {
            if (what == DataUtils.DISMISS_LOADING){
                showLoading.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(BTUtils.getInstance().getConnect()){
            BTUtils.getInstance().close();
        }
        if(btListener != null){
            unregisterReceiver(btListener);
            btListener = null;
        }
    }

    public void showLoadingAndDelayDismiss(String text,int delayMS){
        showLoading.show(text);
        handler.sendEmptyMessageDelayed(DataUtils.DISMISS_LOADING,delayMS);
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

    @Override
    protected void onStart() {
        super.onStart();
        BaseApplication.setHandler(handler);
    }
}