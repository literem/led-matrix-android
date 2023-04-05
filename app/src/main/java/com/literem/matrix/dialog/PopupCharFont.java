package com.literem.matrix.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.literem.matrix.R;
import com.literem.matrix.callback.OnFontCreateCallback;
import com.literem.matrix.common.base.BasePopupWindow;
import com.literem.matrix.common.font.GBKFontUtils;
import com.literem.matrix.common.font.HZKFontUtils;
import com.literem.matrix.common.font.HexUtils;
import com.literem.matrix.common.utils.ToastUtils;

import java.util.List;

public class PopupCharFont extends BasePopupWindow {
    private List<boolean[][]> list;
    private EditText etInput;
    private TextView tvObjectCurrent,tvObjectAll,tvFontGBK,tvFontHZK,tvModeNormal,tvModeReverse;
    private Drawable drawableCheck,drawableUncheck;
    private OnFontCreateCallback onCallbackListener;
    private HZKFontUtils hzkFontUtils;
    private GBKFontUtils gbkFontUtils;
    private String strModeName;
    private boolean isObjectCurrent = true;//是否当前模块
    private boolean isFontGBK = true;//GBK16:true , HZK16:false
    private boolean isModeNormal = true;//正常取模:true，反向取模:false
    private int fontRotate = 0;//旋转角度
    private int checkColor,uncheckColor;
    private int currentModule = 0;
    private int maxCharSize = 0;

    public PopupCharFont(Activity activity, List<boolean[][]> list, OnFontCreateCallback listener){
        super(activity);
        this.list = list;
        this.onCallbackListener = listener;
        drawableCheck = context.getDrawable(R.drawable.bg_circle_5_blue);
        drawableUncheck = context.getDrawable(R.drawable.bg_circle_5_grey);
        checkColor = Color.parseColor("#FFFFFF");
        uncheckColor = Color.parseColor("#505050");
        strModeName = "当前模块%1$d";
        setCheck(tvObjectCurrent);
        setCheck(tvFontGBK);
        setCheck(tvModeNormal);
    }

    public void setMaxCharSize(int size){
        this.maxCharSize = size;
    }

    @Override
    protected int setLayoutId() {
        return R.layout.popup_char_font;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initView(View view) {
        tvObjectCurrent = findAndAddClickListener(R.id.tv_object_current);
        tvObjectAll = findAndAddClickListener(R.id.tv_object_all);
        tvFontGBK = findAndAddClickListener(R.id.tv_font_gbk);
        tvFontHZK = findAndAddClickListener(R.id.tv_font_hzk);
        tvModeNormal = findAndAddClickListener(R.id.tv_mode_normal);
        tvModeReverse = findAndAddClickListener(R.id.tv_mode_reverse);
        addClickListener(R.id.btn_sure);
        etInput = view.findViewById(R.id.et_input);
        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.radio_button_rotate_0:
                        fontRotate = 0;
                        break;
                    case R.id.radio_button_rotate_90:
                        fontRotate = 90;
                        break;
                    case R.id.radio_button_rotate_180:
                        fontRotate = 180;
                        break;
                    case R.id.radio_button_rotate_270:
                        fontRotate = 270;
                        break;
                }
            }
        });

        view.findViewById(R.id.fl_close).setOnTouchListener(new View.OnTouchListener() {
            private float downY,upY;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        downY = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        upY = motionEvent.getY();
                        if(downY > upY){
                            dismiss();
                        }
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void initPopupWindow(PopupWindow popupWindow) {
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setAnimationStyle(R.style.popup_anim_top_bottom);//设置滑入滑出的动画效果
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(false);
    }

    @Override
    protected void onViewClick(View view) {
        switch (view.getId()){
            case R.id.btn_sure: if(onCallbackListener != null)createFont();break;
            case R.id.tv_object_current: isObjectCurrent = true;setCheckState(tvObjectCurrent,tvObjectAll);break;
            case R.id.tv_object_all: isObjectCurrent = false;setCheckState(tvObjectAll,tvObjectCurrent);break;
            case R.id.tv_font_gbk: isFontGBK = true;setCheckState(tvFontGBK,tvFontHZK);break;
            case R.id.tv_font_hzk: isFontGBK = false;setCheckState(tvFontHZK,tvFontGBK);break;
            case R.id.tv_mode_normal: isModeNormal = true;setCheckState(tvModeNormal,tvModeReverse);break;
            case R.id.tv_mode_reverse: isModeNormal = false;setCheckState(tvModeReverse,tvModeNormal);break;
        }
    }

    private void createFont(){
        String inputText = etInput.getText().toString();
        if(inputText.length() == 0){
            ToastUtils.Show(context,"请输入字模数据！");
            return;
        }
        if (list.size() == 0){
            ToastUtils.Show(context,"请选择模块");
            return;
        }
        if(isObjectCurrent){
            CreateOneFont(inputText);
        }else{
            CreateAllFont(inputText);
        }
    }

    private void CreateAllFont(String input){
        int size = input.length() > maxCharSize ? maxCharSize : input.length();
        if (size == 0) return;
        char[] c = input.toCharArray();
        if(isFontGBK){//选择GBK16字库
            if(gbkFontUtils == null) gbkFontUtils = new GBKFontUtils(context);
        }else{//选择HZK16字库
            if(hzkFontUtils == null) hzkFontUtils = new HZKFontUtils(context);
        }
        for (int i = 0; i < size; i++) {
            if(isFontGBK){
                gbkFontUtils.createMatrixFont(c[i],list.get(i),isModeNormal);
            }else{
                hzkFontUtils.createMatrixFont(c[i],list.get(i),isModeNormal);
            }
            //旋转角度
            if(fontRotate == 0) continue;
            HexUtils.rotate(list.get(i),fontRotate);
        }
        etInput.setText("");
        onCallbackListener.onCreateFont(size,0,popupWindow);
    }

    private void CreateOneFont(String input){
        char c = input.charAt(0);
        if(isFontGBK){//选择GBK16字库
            if(gbkFontUtils == null) gbkFontUtils = new GBKFontUtils(context);
            gbkFontUtils.createMatrixFont(c,list.get(currentModule),isModeNormal);
        }else{//选择HZK16字库
            if(hzkFontUtils == null) hzkFontUtils = new HZKFontUtils(context);
            hzkFontUtils.createMatrixFont(c,list.get(currentModule),isModeNormal);
        }
        if(fontRotate != 0) HexUtils.rotate(list.get(currentModule),fontRotate);
        etInput.setText("");
        onCallbackListener.onCreateFont(1,currentModule,popupWindow);
    }

    //设置popup_window在底部显示
    public void showPopupWindow(View v,int module){
        this.currentModule = module==-1 ? 0 : module;
        popupWindow.showAsDropDown(v);
        tvObjectCurrent.setText(String.format(strModeName, currentModule+1));
    }

    private void setCheckState(TextView checkView,TextView uncheckView){
        checkView.setBackground(drawableCheck);
        checkView.setTextColor(checkColor);
        uncheckView.setBackground(drawableUncheck);
        uncheckView.setTextColor(uncheckColor);
    }

    private void setCheck(TextView tv){
        tv.setBackground(drawableCheck);
        tv.setTextColor(checkColor);
    }
}

