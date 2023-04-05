package com.literem.matrix.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.R;
import com.literem.matrix.adapter.PopupImageFontAdapter;
import com.literem.matrix.callback.OnFontCreateCallback;
import com.literem.matrix.common.base.BasePopupWindow;
import com.literem.matrix.common.font.BitmapUtils;
import com.literem.matrix.common.font.HexUtils;
import com.literem.matrix.common.utils.ToastUtils;
import com.literem.matrix.entity.ImageBean;

import java.io.IOException;

public class PopupImageFont extends BasePopupWindow {
    private int IMG_SIZE = 16;
    private PopupImageFontAdapter adapter;
    private TextView tvObjectCurrent, tvModeNormal, tvModeReverse;
    private Drawable drawableCheck, drawableUncheck;
    private OnFontCreateCallback mListener;
    private String strModeName;
    private boolean[][] data = null;
    private boolean isModeNormal = true;
    private int currentModule = 0;
    private int checkColor, uncheckColor;
    private int rotate = 0;

    public PopupImageFont(Activity activity, OnFontCreateCallback listener, int imgSize){
        this(activity,listener);
        IMG_SIZE = imgSize;
    }

    public PopupImageFont(Activity activity, OnFontCreateCallback listener) {
        super(activity);
        this.mListener = listener;
        drawableCheck = context.getDrawable(R.drawable.bg_circle_5_blue);
        drawableUncheck = context.getDrawable(R.drawable.bg_circle_5_grey);
        checkColor = Color.parseColor("#FFFFFF");
        uncheckColor = Color.parseColor("#505050");
        strModeName = "当前模块%1$d";
        setCheck(tvModeNormal);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.popup_image_font;
    }

    @Override
    protected void initView(View view) {
        tvObjectCurrent = view.findViewById(R.id.tv_object_current);
        tvModeNormal = findAndAddClickListener(R.id.tv_mode_normal);
        tvModeReverse = findAndAddClickListener(R.id.tv_mode_reverse);
        addClickListener(R.id.btn_sure);
        RecyclerView recyclerView = view.findViewById(R.id.recycle_view);
        adapter = new PopupImageFontAdapter(context);
        ImageBean bean = new ImageBean();
        bean.setFirst(true);
        bean.setUri(Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.img_add_music));
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);
        adapter.addImage(bean);
        listenerRotate((RadioGroup) view.findViewById(R.id.radio_group));
        listenerTouchExit((FrameLayout) view.findViewById(R.id.fl_close));
    }

    @Override
    protected void initPopupWindow(PopupWindow popupWindow) {
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setAnimationStyle(R.style.popup_anim_top_bottom);//设置滑入滑出的动画效果
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
    }

    @Override
    protected void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sure:
                if (mListener != null) createOneImgFont();
                break;
            case R.id.tv_mode_normal:
                isModeNormal = true;
                setCheckState(tvModeNormal, tvModeReverse);
                break;
            case R.id.tv_mode_reverse:
                isModeNormal = false;
                setCheckState(tvModeReverse, tvModeNormal);
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void listenerTouchExit(FrameLayout flClose){
        flClose.setOnTouchListener(new View.OnTouchListener() {
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
                            popupWindow.dismiss();
                        }
                        break;
                }
                return true;
            }
        });
    }

    private void listenerRotate(RadioGroup radioGroup){
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.radio_button_rotate_0:
                        rotate = 0;
                        break;
                    case R.id.radio_button_rotate_90:
                        rotate = 90;
                        break;
                    case R.id.radio_button_rotate_180:
                        rotate = 180;
                        break;
                    case R.id.radio_button_rotate_270:
                        rotate = 270;
                        break;
                }
            }
        });
    }

    public void addImage(Uri uri){
        ImageBean bean = new ImageBean();
        bean.setUri(uri);
        bean.setCheck(false);
        bean.setId(adapter.getItemCount());
        adapter.addImage(bean);
    }

    /**
     * 创建一张图片的字模
     */
    private void createOneImgFont(){
        //判断是否选择图片
        Uri uri = adapter.getCurrentImageUri();
        if(uri == null){
            ToastUtils.Show(context,"没有选择图片");
            return;
        }

        if(data == null){
            ToastUtils.Show(context,"没有选择要应用的模块");
            return;
        }

        //根据Uri生成bitmap位图对象
        Bitmap bitmap;
        try{
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),uri);
        }catch(IOException e) {
            e.printStackTrace();
            return;
        }

        //获取的位图缩放成16x16大小，转换成黑白带灰度的图片
        Bitmap newBitMap = BitmapUtils.ScaleAndGray(bitmap,IMG_SIZE);

        //获取位图宽高
        int width = newBitMap.getWidth();
        int height = newBitMap.getHeight();
        int current_color;

        //获得每一个位点的颜色
        for (int i=0;i<width;i++) {
            for (int j = 0; j < height; j++) {
                current_color = newBitMap.getPixel(i, j);
                data[j][i] = current_color != 0 && current_color != 0xffffffff;
            }
        }
        //根据条件判断是否反转和旋转角度
        rotateAndReverse();

        //回调接口
        if(mListener != null) mListener.onCreateFont(1,currentModule,popupWindow);
    }

    //反转模块的颜色和旋转角度
    private void rotateAndReverse(){
        //旋转角度
        if(rotate != 0){
            HexUtils.rotate(data, rotate);
        }
        int length = data.length;
        if(!isModeNormal){//反转
            for (int i = 0; i < length; i++) {
                for (int j = 0; j < length; j++) {
                    data[i][j] = !data[i][j];
                }
            }
        }
    }

    //设置popup_window在底部显示
    public void showPopupWindow(View v,boolean[][] data,int mode){
        if(mode == -1) mode = 0;
        this.data = data;
        this.currentModule = mode;
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
