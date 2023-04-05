package com.literem.matrix.activity;

import android.view.View;
import android.widget.RadioButton;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.literem.matrix.R;
import com.literem.matrix.common.base.BaseBTActivity;
import com.literem.matrix.common.entity.ResponseResultBean;
import com.literem.matrix.common.font.CommandUtils;

public class DisplayClockActivity extends BaseBTActivity {

    private RadioButton radioShowHMS, radioShowHM;
    private Switch switchClockTime;
    private int clockStyle = 0;


    @Override
    protected int setLayoutId() {
        return R.layout.activity_display_clock;
    }

    @Override
    protected void onInitView() {
        switchClockTime = findViewById(R.id.switch_clock_time);
        radioShowHMS = findAndAddOnClickListener(R.id.radio_show_hms);
        radioShowHM = findAndAddOnClickListener(R.id.radio_show_hm);
        addOnClickListener(R.id.iv_sync);
    }

    @Override
    protected void onInit() {
        setTitle("点阵时钟");
        clearQueue();
        appendToQueue(CommandUtils.getInstance().getDeviceInfo(CommandUtils.DISPLAY_STATE));
        appendToQueue(CommandUtils.getInstance().getDisplayInfo(CommandUtils.DISPLAY_CLOCK,CommandUtils.MODE));
        sendQueue(200);
    }

    private void setClockStyle(int style){
        clockStyle = style;
        if (!switchClockTime.isChecked()) {
            switchClockTime.setChecked(true);
        }
        showLoadingDelayDismiss("正在设置时钟");
        clearQueue();
        appendToQueue(CommandUtils.getInstance().setDisplayClock(style));
        appendToQueue(CommandUtils.getInstance().getDeviceInfo(CommandUtils.DISPLAY_STATE));
        sendQueue(300);
    }

    private void setRadioMode(int mode){
        if (!switchClockTime.isChecked())return;
        if(mode == 1){
            radioShowHM.setChecked(true);
            radioShowHMS.setChecked(false);
        }else if(mode == 2){
            radioShowHM.setChecked(false);
            radioShowHMS.setChecked(true);
        }else{
            radioShowHM.setChecked(false);
            radioShowHMS.setChecked(false);
        }
    }

    @Override
    protected void onViewClick(View view) {
        switch (view.getId()){
            case R.id.radio_show_hms:
                if (clockStyle == 2) return;
                radioShowHM.setChecked(false);
                setClockStyle(2);
                break;
            case R.id.radio_show_hm:
                if (clockStyle == 1) return;
                radioShowHMS.setChecked(false);
                setClockStyle(1);
                break;
            case R.id.iv_sync:
                syncDatetime();
                break;
        }
    }

    private void syncDatetime(){
        showLoadingDelayDismiss("正在同步时间");
        handler.sendOrAddQueue(CommandUtils.getInstance().setDisplayClockDatetime());
    }

    @Override
    protected void handlerReceive(@NonNull byte[] receive) {
        ResponseResultBean bean = handler.responseParse.parseResponseOneData(receive);
        switch (bean.getFlag()) {
            case CommandUtils.DISPLAY_STATE:
                int clock = CommandUtils.DISPLAY_CLOCK & 0xff;//一定要&0xff，否则byte转成int会是负数
                switchClockTime.setChecked(bean.getData() == clock);
                break;
            case CommandUtils.MODE:
                setRadioMode(bean.getData());
                break;
        }
    }
}
