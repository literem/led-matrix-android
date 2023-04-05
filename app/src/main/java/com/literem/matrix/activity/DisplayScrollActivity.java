package com.literem.matrix.activity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.literem.matrix.R;
import com.literem.matrix.common.base.BaseBTActivity;
import com.literem.matrix.common.bluetooth.BTUtils;
import com.literem.matrix.common.entity.ResponseResultBean;
import com.literem.matrix.common.font.CommandUtils;
import com.literem.matrix.common.utils.ToastUtils;
import com.literem.matrix.common.widget.DrawableTextView;
import com.literem.matrix.dialog.DialogNumberInput;

import static com.literem.matrix.common.data.DataUtils.ModuleSize;

public class DisplayScrollActivity extends BaseBTActivity {

    private EditText etInput;
    private Button btnSend;
    private TextView tvCount;
    private CheckBox cbRunOnStart;
    private DrawableTextView tvSpeed,tvStatus;
    private String strCount,strMS;
    private int currentSpeed = 200;
    private boolean scrollState = false;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_display_scroll;
    }

    @Override
    protected void onInitView() {
        tvSpeed = findAndAddOnClickListener(R.id.dtv_scroll_speed);
        tvStatus = findAndAddOnClickListener(R.id.dtv_scroll_state);
        btnSend = findAndAddOnClickListener(R.id.btn_send);
        findAndAddOnClickListener(R.id.tv_stop);
        findAndAddOnClickListener(R.id.tv_start);
        findAndAddOnClickListener(R.id.tv_pause);
        tvCount = findViewById(R.id.tv_count);
        tvCount.setText("(共0字)");
        cbRunOnStart = findViewById(R.id.cb_run_on_start);
        etInput = findViewById(R.id.et_input);
        etInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                tvCount.setText(String.format(strCount,editable.length()));
            }
        });
    }

    @Override
    protected void onInit() {
        setTitle("文本滚动显示");
        strCount = "(共%1$d字)";
        strMS = "%1$dms";
        clearQueue();
        appendToQueue(CommandUtils.getInstance().getDisplayInfo(CommandUtils.DISPLAY_SCROLL,CommandUtils.STATE_SCROLL));
        appendToQueue(CommandUtils.getInstance().getDisplayInfo(CommandUtils.DISPLAY_SCROLL,CommandUtils.SPEED));
        sendQueue(300);
    }

    @Override
    protected void onViewClick(View view) {
        switch (view.getId()){
            case R.id.dtv_scroll_speed: showSetSpeedDialog();break;
            case R.id.dtv_scroll_state: refreshState();break;
            case R.id.btn_send: sendScrollData();break;
            case R.id.tv_stop: setStopScroll();break;
            case R.id.tv_pause: setPauseScroll();break;
            case R.id.tv_start: setStartScroll();break;
        }
    }

    private void setScrollStatus(boolean status){
        if(status){//设置状态为开始滚动，禁用输入和发送按钮
            scrollState = true;
            etInput.setEnabled(false);
            btnSend.setEnabled(false);
        }else{
            scrollState = false;
            etInput.setEnabled(true);
            btnSend.setEnabled(true);
        }
    }

    private void showSetSpeedDialog(){
        DialogNumberInput.Builder(this)
                .setTitle("设置滚动速度","速度(30-5000)")
                .setEdgeValue(30,5000)
                .setStep(20)
                .setInitialValue(currentSpeed)
                .setListener(null, new DialogNumberInput.OnSetTimeListener() {
                    @Override
                    public void onSetValue(DialogNumberInput dialog, int value) {
                        currentSpeed = value;
                        tvSpeed.setText(String.format(strMS, currentSpeed));
                        dialog.dismiss();
                        handler.sendOrAddQueue(CommandUtils.getInstance().setSpeed(true,value));
                    }
                }).showDialog();
    }

    private void refreshState(){
        showLoadingDelayDismiss("正在获取数据");
        BTUtils.getInstance().send(CommandUtils.getInstance().getDisplayInfo(CommandUtils.DISPLAY_SCROLL,CommandUtils.STATE_SCROLL));
    }

    private void setStartScroll(){
        if (scrollState){
            ToastUtils.Show(this,"当前处于滚动状态，不能开始滚动");
            return;
        }
        showLoadingDelayDismiss("正在开始滚动");
        appendToQueue(CommandUtils.getInstance().getDisplayInfo(CommandUtils.DISPLAY_SCROLL,CommandUtils.STATE_SCROLL));
        appendToQueue(CommandUtils.getInstance().setSpeed(true,1));
        sendQueue(0);
    }

    private void setPauseScroll(){
        if (!scrollState){
            ToastUtils.Show(this,"当前未处于滚动状态，不能暂停滚动");
            return;
        }
        appendToQueue(CommandUtils.getInstance().getDisplayInfo(CommandUtils.DISPLAY_SCROLL,CommandUtils.STATE_SCROLL));
        showLoadingDelayDismiss("正在暂停滚动");
        CommandUtils.getInstance().setSpeed(true,0);
    }

    private void setStopScroll(){
        if (!scrollState){
            ToastUtils.Show(this,"当前未处于滚动状态，不能停止滚动");
            return;
        }
        showLoadingDelayDismiss("正在停止滚动");
        CommandUtils.getInstance().setMatrixData(CommandUtils.DISPLAY_NONE,0);
        sendDataDelay(CommandUtils.getInstance().getDisplayInfo(CommandUtils.DISPLAY_SCROLL,CommandUtils.STATE_SCROLL),500);
    }

    private void sendScrollData(){
        if(scrollState){
            setPauseScroll();
            return;
        }
        String input = etInput.getText().toString();
        if(input.isEmpty()){
            ToastUtils.Show(this,"请输入内容");
            return;
        }
        if(input.length() < ModuleSize){
            ToastUtils.Show(this,"请输入大于点阵模块长度的字");
            return;
        }
        showLoadingDelayDismiss("正在发送数据");
        BTUtils.getInstance().send(CommandUtils.getInstance().setDisplayScroll(input,cbRunOnStart.isChecked()));
        sendDataDelay(CommandUtils.getInstance().getDisplayInfo(CommandUtils.DISPLAY_SCROLL,CommandUtils.STATE_SCROLL),800);
    }


    @Override
    protected void handlerReceive(@NonNull byte[] receive) {
        ResponseResultBean bean = handler.responseParse.parseResponseOneData(receive);
        switch (bean.getFlag()) {
            case CommandUtils.STATE_SCROLL:
                setScrollStatus((byte)bean.getData() == 1);
                tvStatus.setText(scrollState ? "正在滚动" : "未在滚动");
                break;
            case CommandUtils.SPEED:
                if (bean.getData() == 0) return;
                currentSpeed = bean.getData();
                tvSpeed.setText(String.format(strMS, currentSpeed));
                break;
        }
    }
}
