package com.literem.matrix.activity;

import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import androidx.annotation.NonNull;

import com.literem.matrix.R;
import com.literem.matrix.common.base.BaseBTActivity;
import com.literem.matrix.common.bluetooth.BTUtils;
import com.literem.matrix.common.entity.ResponseResultBean;
import com.literem.matrix.common.font.CommandUtils;
import com.literem.matrix.common.data.DataUtils;
import com.literem.matrix.common.utils.ToastUtils;
import com.literem.matrix.common.widget.DrawableTextView;
import com.literem.matrix.common.widget.RowScrollView;
import com.literem.matrix.dialog.DialogNumberInput;
import com.literem.matrix.dialog.DialogSetAnimation;
import com.literem.matrix.music.activity.TextEditActivity;

public class DisplayStaticActivity extends BaseBTActivity {

    private DrawableTextView tvInAnim,tvOutAnim,tvSpeed,tvDisplayState;
    private RowScrollView textScroll;//文本滚动
    private DialogSetAnimation dialogSetAnimation;
    private RadioButton rbSelfAnim,rbPageAnim;
    private CheckBox cbAutoDisplay;
    private String lines;
    private boolean isPageAnim = true;
    private int currentSpeed = 5000;
    private byte currentDisplayState = 0;
    private String strMS;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_display_static;
    }

    @Override
    protected void onInitView() {
        findAndAddOnClickListener(R.id.dtv_edit_text);
        findAndAddOnClickListener(R.id.btn_last_line);
        findAndAddOnClickListener(R.id.btn_next_line);
        findAndAddOnClickListener(R.id.btn_send_data);
        tvSpeed = findAndAddOnClickListener(R.id.dtv_toggle_speed);
        rbSelfAnim = findAndAddOnClickListener(R.id.rb_self_anim);
        rbPageAnim = findAndAddOnClickListener(R.id.rb_page_anim);
        tvInAnim = findAndAddOnClickListener(R.id.dtv_in_anim);
        tvOutAnim = findAndAddOnClickListener(R.id.dtv_out_anim);
        tvDisplayState = findAndAddOnClickListener(R.id.dtv_display_state);
        textScroll = findViewById(R.id.text_vertical_scroll);
        cbAutoDisplay = findViewById(R.id.cb_auto_display);
    }

    @Override
    protected void onInit() {
        setTitle("静态显示");
        strMS = "%1$dms";
        dialogSetAnimation = new DialogSetAnimation(this,onAnimationDialogListener);
        if(!BTUtils.getInstance().getConnect()) {
            ToastUtils.Show(this,"数据获取失败，蓝牙未连接");
            return;
        }
        clearQueue();
        appendToQueue(CommandUtils.getInstance().getDisplayInfo(CommandUtils.DISPLAY_STATIC,CommandUtils.ANIM_IN));
        appendToQueue(CommandUtils.getInstance().getDisplayInfo(CommandUtils.DISPLAY_STATIC,CommandUtils.ANIM_OUT));
        appendToQueue(CommandUtils.getInstance().getDisplayInfo(CommandUtils.DISPLAY_STATIC,CommandUtils.SPEED));
        appendToQueue(CommandUtils.getInstance().getDeviceInfo(CommandUtils.DISPLAY_STATE));
        sendQueue(300);
    }

    @Override
    protected void onViewClick(View view) {
        switch (view.getId()){
            case R.id.btn_last_line: lastPage();break;//上一行
            case R.id.btn_next_line: nextPage();break;//下一行
            case R.id.dtv_edit_text: showTextActivity();break;//编辑文本
            case R.id.dtv_in_anim:   showSetAnimDialog(true);break;
            case R.id.dtv_out_anim:  showSetAnimDialog(false);break;
            case R.id.rb_self_anim:  setRadioButtonAnim(false);break;
            case R.id.rb_page_anim:  setRadioButtonAnim(true);break;
            case R.id.dtv_toggle_speed:showSetSpeedDialog();break;
            case R.id.btn_send_data: sendToggleData();break;
            case R.id.dtv_display_state: refreshDisplayState();break;
        }
    }

    private DialogSetAnimation.OnAnimationDialogListener onAnimationDialogListener = new DialogSetAnimation.OnAnimationDialogListener() {
        @Override
        public void onDialogClick(String animName, boolean isInAnim, int animId) {
            if(isInAnim){
                DataUtils.inAnim = animId;
                tvInAnim.setText(animName);
            }else{
                DataUtils.outAnim = animId;
                tvOutAnim.setText(animName);
            }
            byte[] bytesAnim = CommandUtils.getInstance().setDisplayStaticAnim(isInAnim, animId);
            BTUtils.getInstance().send(bytesAnim);
        }
    };

    private void setRadioButtonAnim(boolean isPageAnim){
        this.isPageAnim = isPageAnim;
        if(isPageAnim){//翻页动画
            rbSelfAnim.setChecked(false);
        }else{//自定义动画
            rbPageAnim.setChecked(false);
        }
        if (currentDisplayState == CommandUtils.DISPLAY_TOGGLE){
            byte anim = isPageAnim ? CommandUtils.MOVE_PAGE_DOWN : CommandUtils.MOVE_ANIM;
            BTUtils.getInstance().send(CommandUtils.getInstance().setDisplayToggleAnim(anim));
        }
    }

    private void sendToggleData(){
        if (lines == null || lines.isEmpty()){
            ToastUtils.Show(this,"请输入文本信息！");
            return;
        }
        showLoadingDelayDismiss("发送数据中");
        byte anim = isPageAnim ? CommandUtils.MOVE_PAGE_DOWN : CommandUtils.MOVE_ANIM;
        clearQueue();
        appendToQueue(CommandUtils.getInstance().setDisplayToggle(anim,lines,cbAutoDisplay.isChecked()));
        appendToQueue(CommandUtils.getInstance().getDeviceInfo(CommandUtils.DISPLAY_STATE));
        sendQueue(100);
    }

    private void refreshDisplayState(){
        showLoadingDelayDismiss("正在获取数据");
        BTUtils.getInstance().send(CommandUtils.getInstance().getDeviceInfo(CommandUtils.DISPLAY_STATE));
    }

    private void showSetSpeedDialog(){
        DialogNumberInput.Builder(this)
                .setTitle("设置切换速度","范围(500-30000)")
                .setEdgeValue(500,30000)
                .setStep(100)
                .setInitialValue(currentSpeed)
                .setListener(null, new DialogNumberInput.OnSetTimeListener() {
                    @Override
                    public void onSetValue(DialogNumberInput dialog, int value) {
                        currentSpeed = value;
                        tvSpeed.setText(String.format(strMS, currentSpeed));
                        dialog.dismiss();
                        handler.sendOrAddQueue(CommandUtils.getInstance().setSpeed(false,value));
                    }
                }).showDialog();
    }

    //下一行
    private void nextPage(){
        if (!textScroll.scrollNextLine()) {
            ToastUtils.Show(DisplayStaticActivity.this,"已到达最后一行");
            return;
        }
        String input = textScroll.getCurrentString(true);
        if(input.length() > DataUtils.ModuleSize){
            input = input.substring(0,DataUtils.ModuleSize);
        }
        byte[] moveData = CommandUtils.getInstance().setDisplayStaticMove(isPageAnim?CommandUtils.MOVE_PAGE_DOWN:CommandUtils.MOVE_ANIM, input);
        handler.sendOrAddQueue(moveData);
    }

    //上一行
    private void lastPage(){
        if (!textScroll.scrollLastLine()) {
            ToastUtils.Show(DisplayStaticActivity.this,"已在第一行");
            return;
        }
        String input = textScroll.getCurrentString(false);
        if(input.length() > DataUtils.ModuleSize){
            input = input.substring(0,DataUtils.ModuleSize);
        }
        byte[] moveData = CommandUtils.getInstance().setDisplayStaticMove(isPageAnim?CommandUtils.MOVE_PAGE_UP:CommandUtils.MOVE_ANIM, input);
        handler.sendOrAddQueue(moveData);
    }

    private void showSetAnimDialog(boolean isInAnim){
        if(isInAnim)dialogSetAnimation.showDialog("设置进场动画", DialogSetAnimation.Anim.IN);
        else dialogSetAnimation.showDialog("设置退场动画", DialogSetAnimation.Anim.OUT);
    }

    private void showTextActivity(){
        Intent intent = new Intent(DisplayStaticActivity.this, TextEditActivity.class);
        intent.putExtra("text",lines);
        intent.putExtra("title","编辑文本");
        startActivityForResult(intent,0x01);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == 0xff){
            if(data != null){
                String input = data.getStringExtra("text");
                if(input != null){
                    lines = input;
                    textScroll.setData(lines);
                    ToastUtils.Show(DisplayStaticActivity.this,"添加成功");
                }else{
                    ToastUtils.Show(DisplayStaticActivity.this,"添加失败");
                }
            }
        }
    }

    @Override
    protected void handlerReceive(@NonNull byte[] receive) {
        ResponseResultBean bean = handler.responseParse.parseResponseOneData(receive);
        switch (bean.getFlag()){
            case CommandUtils.ANIM_IN: tvInAnim.setText(CommandUtils.getInstance().getAnim(true,bean.getData()));break;
            case CommandUtils.ANIM_OUT:tvOutAnim.setText(CommandUtils.getInstance().getAnim(false,bean.getData()));break;
            case CommandUtils.SPEED:
                if (bean.getData() == 0) break;
                currentSpeed = bean.getData();
                tvSpeed.setText(String.format(strMS, currentSpeed));
                break;
            case CommandUtils.DISPLAY_STATE:
                currentDisplayState = (byte) bean.getData();
                tvDisplayState.setText(CommandUtils.getInstance().getDisplayMode(currentDisplayState));
                break;
        }
    }
}
