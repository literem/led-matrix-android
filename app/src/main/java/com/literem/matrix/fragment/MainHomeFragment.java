package com.literem.matrix.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import com.literem.matrix.R;
import com.literem.matrix.activity.MainActivity;
import com.literem.matrix.common.bluetooth.BTDialog;
import com.literem.matrix.common.bluetooth.BTUtils;
import com.literem.matrix.common.data.DataUtils;
import com.literem.matrix.common.entity.ResponseResultBean;
import com.literem.matrix.common.entity.ResponseResultList;
import com.literem.matrix.common.font.CommandUtils;
import com.literem.matrix.common.utils.ColorUtils;
import com.literem.matrix.common.utils.ConfigUtils;
import com.literem.matrix.common.widget.DrawableTextView;
import com.literem.matrix.dialog.DialogNumberInput;

public class MainHomeFragment extends Fragment {

    private Context context;
    private ConfigUtils configData;

    //蓝牙设备名称和mac控件
    private TextView tvDeviceName, tvDeviceMac;
    private ImageView ivBtIcon,ivArrow;
    private LinearLayout llDevice;
    private DrawableTextView tvModuleSize,tvAutoDisplay;
    private TextView tvDisplayMode,tvSaveMode,tvChargeState,tvPowerSupply;
    private FrameLayout[] flViews;
    private TextView[] tvViews;
    private Button btnRefresh;
    private Button btnConnect; //连接按钮
    private String deviceName,deviceAddress;//设备名称和设备mac
    private boolean isConnect = false; //是否已经连接
    private MainActivity activity;
    private BTDialog btDialog;

    public MainHomeFragment(Context context){
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
        if(activity == null) {
            return;
        }
        btDialog = new BTDialog(activity,activity.getMainHandler());
        flViews = new FrameLayout[6];
        tvViews = new TextView[6];
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_home,container,false);
        initView(view);
        loadData();
        return view;
    }

    public void updateConnectStatus(){
        boolean currentStatus = BTUtils.getInstance().getConnect();
        if(currentStatus != isConnect){
            setConnectViewState(currentStatus);
        }
    }

    private void initView(View view) {
        tvModuleSize = view.findViewById(R.id.dtv_module_size);
        tvAutoDisplay = view.findViewById(R.id.dtv_auto_display);
        tvDisplayMode = view.findViewById(R.id.tv_display_mode);
        tvSaveMode = view.findViewById(R.id.tv_save_display);
        tvChargeState = view.findViewById(R.id.tv_charge_state);
        tvPowerSupply = view.findViewById(R.id.tv_power_supply);
        tvViews[0] = tvModuleSize;
        tvViews[1] = tvAutoDisplay;
        tvViews[2] = tvDisplayMode;
        tvViews[3] = tvSaveMode;
        tvViews[4] = tvChargeState;
        tvViews[5] = tvPowerSupply;
        flViews[0] = view.findViewById(R.id.fl_module_size);
        flViews[1] = view.findViewById(R.id.fl_auto_display);
        flViews[2] = view.findViewById(R.id.fl_display_mode);
        flViews[3] = view.findViewById(R.id.fl_save_mode);
        flViews[4] = view.findViewById(R.id.fl_charge);
        flViews[5] = view.findViewById(R.id.fl_power_supply);
        btnRefresh = view.findViewById(R.id.btn_refresh);
        tvModuleSize.setOnClickListener(onClickListener);
        tvAutoDisplay.setOnClickListener(onClickListener);
        tvDisplayMode.setOnClickListener(onClickListener);
        btnRefresh.setOnClickListener(onClickListener);
        btnConnect = view.findViewById(R.id.btn_fragment_home_connect);
        tvDeviceName = view.findViewById(R.id.tv_main_home_device_name);
        tvDeviceMac = view.findViewById(R.id.tv_main_home_device_mac);
        ivBtIcon = view.findViewById(R.id.iv_main_home_bt_icon);
        ivArrow = view.findViewById(R.id.iv_main_home_arrow);
        llDevice = view.findViewById(R.id.ll_fragment_main_home_device);
        llDevice.setOnClickListener(onClickListener);
        btnConnect.setOnClickListener(onClickListener);
        setConnectViewState(false);
    }

    //加载配置数据
    private void loadData(){
        configData = new ConfigUtils(context);
        deviceName = configData.loadStringData(ConfigUtils.ConfigType.device_name);
        deviceAddress = configData.loadStringData(ConfigUtils.ConfigType.device_address);
        if(deviceName.length() > 0){
            tvDeviceName.setText(deviceName);
            tvDeviceMac.setText(deviceAddress);
        }
    }

    //连接成功做出的操作
    public void setConnectState(boolean state){
        if(state){
            deviceName = BTUtils.getInstance().getDeviceName();
            deviceAddress = BTUtils.getInstance().getDeviceAddress();
            configData.saveStringData(ConfigUtils.ConfigType.device_name,deviceName);
            configData.saveStringData(ConfigUtils.ConfigType.device_address,deviceAddress);
        }
        setConnectViewState(state);
        if(btDialog != null) btDialog.finishConnect(state);
    }

    public void setDeviceInfo(ResponseResultList list){
        if (list == null) return;
        DataUtils.ModuleSize = list.getValueByFlag(CommandUtils.MODULE_SIZE);
        tvModuleSize.setText(String.valueOf(DataUtils.ModuleSize));
        tvChargeState.setText(list.getValueByFlag(CommandUtils.CHARGE_STATE)==1?"正在充电":"未在充电");
        tvDisplayMode.setText(CommandUtils.getInstance().getDisplayMode((byte) list.getValueByFlag(CommandUtils.DISPLAY_STATE)));
        tvPowerSupply.setText(list.getValueByFlag(CommandUtils.POWER_STATE)==1?"外接5V":"电池");
        tvSaveMode.setText(CommandUtils.getInstance().getDisplayMode((byte) list.getValueByFlag(CommandUtils.SAVED_MODE)));
        DataUtils.runOnStart = list.getValueByFlag(CommandUtils.RUN_ON_START)==1;
        tvAutoDisplay.setText(DataUtils.runOnStart?"打开":"关闭");
    }

    public void setDeviceInfo(ResponseResultBean bean){
        if (bean == null) return;
        switch (bean.getFlag()){
            case CommandUtils.POWER_STATE:
                tvPowerSupply.setText(bean.getData()==1?"外接5V":"电池");
                break;
            case CommandUtils.CHARGE_STATE:
                tvChargeState.setText(bean.getData()==1?"正在充电":"未在充电");
                break;
            case CommandUtils.DISPLAY_STATE:
                tvDisplayMode.setText(CommandUtils.getInstance().getDisplayMode((byte) bean.getData()));
                break;
            case CommandUtils.RUN_ON_START:
                DataUtils.runOnStart = bean.getData() == 1;
                tvAutoDisplay.setText(DataUtils.runOnStart?"打开":"关闭");
                break;
        }
    }

    //设置连接状态，按钮文本和颜色
    private void setConnectViewState(boolean connect){
        if(connect){
            tvDeviceName.setTextColor(Color.WHITE);
            tvDeviceMac.setTextColor(Color.WHITE);
            tvDeviceName.setText(deviceName);
            tvDeviceMac.setText(deviceAddress);
            ivArrow.setVisibility(View.INVISIBLE);
            ivBtIcon.setImageResource(R.drawable.img_bluetooth_white);
            llDevice.setBackgroundResource(R.drawable.bg_circle_10_blue);
            btnConnect.setBackgroundResource(R.drawable.bg_circle_10_red);
            btnConnect.setText("已连接");
            btnRefresh.setEnabled(true);
            btnRefresh.setBackgroundResource(R.drawable.bg_circle_10_blue);
        }else{
            tvDeviceName.setTextColor(ColorUtils.COLOR_505050);
            tvDeviceMac.setTextColor(ColorUtils.COLOR_808080);
            ivArrow.setVisibility(View.VISIBLE);
            ivBtIcon.setImageResource(R.drawable.img_bluetooth_grey);
            llDevice.setBackgroundResource(R.drawable.bg_circle_10_light_grey);
            btnConnect.setBackgroundResource(R.drawable.bg_circle_10_grey);
            btnConnect.setText("未连接");
            btnRefresh.setEnabled(false);
            btnRefresh.setBackgroundResource(R.drawable.bg_circle_10_grey);
        }
        isConnect = connect;
        if (isConnect){
            for (int i = 0; i < flViews.length; i++) {
                flViews[i].setBackgroundResource(R.drawable.bg_circle_10_white);
                tvViews[i].setTextColor(ColorUtils.COLOR_505050);
                tvViews[i].setEnabled(true);
            }
        }else{
            for (int i = 0; i < flViews.length; i++) {
                flViews[i].setBackgroundResource(R.drawable.bg_circle_10_light_grey);
                tvViews[i].setTextColor(ColorUtils.COLOR_808080);
                tvViews[i].setEnabled(false);
            }
        }
    }

    private void setRunOnStartState(boolean state){
        if (DataUtils.runOnStart == state) return;
        DataUtils.runOnStart = state;
        activity.showLoadingAndDelayDismiss("正在设置中",1000);
        CommandUtils.getInstance().setMatrixData(CommandUtils.SET_RUN_ON_START,state?1:0);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.ll_fragment_main_home_device://蓝牙选项
                    if(BTUtils.getInstance().getConnect())
                        btDialog.closeConnect();
                    else
                        btDialog.show();
                    break;
                case R.id.btn_fragment_home_connect://蓝牙连接按钮
                    if(BTUtils.getInstance().getConnect()){//如果状态是已经连接，再点击就是断开连接
                        btDialog.closeConnect();
                        return;
                    }
                    if(TextUtils.isEmpty(deviceAddress))
                        btDialog.show();
                    else
                        btDialog.startConnect(deviceAddress);
                    break;
                case R.id.dtv_module_size: showModuleSizeDialog();break;
                case R.id.btn_refresh: refreshMatrixData();break;
                case R.id.dtv_auto_display: showSetAutoStateDialog(view);break;
                case R.id.tv_display_mode:showCloseModeDialog(view);break;
            }
        }
    };


    private void refreshMatrixData(){
        if (!isConnect)return;
        activity.showLoadingAndDelayDismiss("正在获取数据...",1000);
        BTUtils.getInstance().send(CommandUtils.getInstance().getDeviceInfo(CommandUtils.ALL_INFO));
    }

    private void showModuleSizeDialog(){
        DialogNumberInput.Builder(context)
                .setTitle("设置点阵模块数量","数量(个)")
                .setEdgeValue(2,20)
                .setInitialValue(DataUtils.ModuleSize)
                .setListener(null, new DialogNumberInput.OnSetTimeListener() {
                    @Override
                    public void onSetValue(DialogNumberInput dialog, int value) {
                        tvModuleSize.setText(String.valueOf(value));
                        DataUtils.ModuleSize = value;
                        dialog.dismiss();
                        CommandUtils.getInstance().setMatrixData(CommandUtils.MODULE_SIZE,value);
                    }
                }).showDialog();
    }

    private void showSetAutoStateDialog(View view){
        Context wrapper = new ContextThemeWrapper(context, com.literem.matrix.music.R.style.MenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_open_and_close, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_open){
                    setRunOnStartState(true);
                    tvAutoDisplay.setText("打开");
                }else if(item.getItemId() == R.id.menu_close){
                    setRunOnStartState(false);
                    tvAutoDisplay.setText("关闭");
                }
                return true;
            }
        });
    }

    private void showCloseModeDialog(View view){
        if (tvDisplayMode.getText().equals("无")) return;
        Context wrapper = new ContextThemeWrapper(context, com.literem.matrix.music.R.style.MenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_close, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_close_mode){
                    CommandUtils.getInstance().setDisplayNone();
                    tvDisplayMode.setText("无");
                }
                return true;
            }
        });
    }
}
