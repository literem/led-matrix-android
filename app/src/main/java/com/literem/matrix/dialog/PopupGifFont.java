package com.literem.matrix.dialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.R;
import com.literem.matrix.activity.DisplayFrameActivity;
import com.literem.matrix.adapter.GifFrameAdapter;
import com.literem.matrix.common.base.BasePopupWindow;
import com.literem.matrix.common.font.BitmapUtils;
import com.literem.matrix.common.font.HexUtils;
import com.literem.matrix.common.utils.ToastUtils;
import com.literem.matrix.common.widget.DrawableTextView;
import com.literem.matrix.entity.GifFrameBean;
import com.literem.matrix.utils.GifDecoder;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class PopupGifFont extends BasePopupWindow {

    private GifFrameAdapter adapter;
    private GifDecoder gifDecoder;
    private DisplayFrameActivity activity;
    private TextView tvModeNormal,tvModeReverse;
    private TextView tvCount;
    private DrawableTextView tvSelectImage;
    private ImageView ivPreview;
    private Drawable drawableCheck,drawableUncheck;
    private String strCount;
    private int checkColor,uncheckColor;
    private boolean isDecoder;
    private boolean isModeNormal = true;
    private int rotate = 0;

    public PopupGifFont(Activity activity) {
        super(activity);
        if (activity instanceof DisplayFrameActivity){
            this.activity = (DisplayFrameActivity) activity;
        }
        drawableCheck = context.getDrawable(R.drawable.bg_circle_5_blue);
        drawableUncheck = context.getDrawable(R.drawable.bg_circle_5_grey);
        checkColor = Color.parseColor("#FFFFFF");
        uncheckColor = Color.parseColor("#505050");
        strCount = "共(%1$d)帧";
        setCheckState(tvModeNormal,tvModeReverse);
    }

    @Override
    protected int setLayoutId() {
        return R.layout.popup_gif_font;
    }

    @Override
    protected void initView(View view) {
        tvSelectImage = view.findViewById(R.id.dtv_select_image);
        ivPreview = view.findViewById(R.id.iv_preview);
        tvCount = view.findViewById(R.id.tv_count);
        addClickListener(R.id.dtv_select_image);
        addClickListener(R.id.tv_select_all);
        addClickListener(R.id.tv_select_all_not);
        addClickListener(R.id.tv_select_invert);
        addClickListener(R.id.btn_sure);
        tvModeNormal = findAndAddClickListener(R.id.tv_mode_normal);
        tvModeReverse = findAndAddClickListener(R.id.tv_mode_reverse);
        adapter = new GifFrameAdapter(context);
        RecyclerView recyclerView = view.findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context,RecyclerView.HORIZONTAL,false));
        recyclerView.setAdapter(adapter);
        listenerRotate((RadioGroup) view.findViewById(R.id.radio_group));
        listenerTouchExit((FrameLayout) view.findViewById(R.id.fl_close));
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
            case R.id.btn_sure: createFrame();break;
            case R.id.tv_select_all:     adapter.isSelectAll(true);break;
            case R.id.tv_select_all_not: adapter.isSelectAll(false);break;
            case R.id.tv_select_invert:  adapter.invertSelect();break;
            case R.id.tv_mode_normal:
                isModeNormal = true;
                setCheckState(tvModeNormal,tvModeReverse);
                break;
            case R.id.tv_mode_reverse:
                isModeNormal = false;
                setCheckState(tvModeReverse,tvModeNormal);
                break;
            case R.id.dtv_select_image:
                ((Activity)context).startActivityForResult(new Intent(Intent.ACTION_PICK, null)
                        .setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*"),2);
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

    private void setCheckState(TextView checkView, TextView uncheckView){
        checkView.setBackground(drawableCheck);
        checkView.setTextColor(checkColor);
        uncheckView.setBackground(drawableUncheck);
        uncheckView.setTextColor(uncheckColor);
    }

    //设置popup_window在底部显示
    public void show(View v){
        popupWindow.showAsDropDown(v);
    }

    @SuppressLint("SetTextI18n")
    private void setSelectImage(Uri uri){
        ivPreview.setVisibility(View.VISIBLE);
        ivPreview.setImageURI(uri);
        tvSelectImage.setText("更换GIF");
    }

    //解析GIF图片
    public void GIFDesc(Uri uri){
        if(uri == null){
            ToastUtils.Show(context,"请打开一个文件！");
            isDecoder = false;
            return;
        }
        setSelectImage(uri);
        gifDecoder = new GifDecoder();
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            int result = gifDecoder.read(in);
            if(result != GifDecoder.STATUS_OK){
                ToastUtils.Show(context,"GIF文件打开失败！");
                isDecoder = false;
                return;
            }
            isDecoder = true;
            adapter.setList(gifDecoder.getFrameList());
            tvCount.setText(String.format(strCount,adapter.getItemCount()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void createFrame(){
        if(!isDecoder){
            ToastUtils.Show(context,"请先解析GIF图片！");
            return;
        }
        if(gifDecoder == null) return;
        int max = 100;
        for (int i = 0; i<gifDecoder.getFrameCount() && max!=0; i++) {
            final GifFrameBean frameBean = gifDecoder.getFrameBean(i);
            if(frameBean.isCheck()){
                max--;
                createOneImgFont(frameBean.image);
            }
        }
        if(max != 100){
            ToastUtils.Show(context,"图片转换成功！");
            popupWindow.dismiss();
        }else{
            ToastUtils.Show(context,"没有要转换的图片！");
        }
    }

    /**
     * 创建一张图片的字模
     */
    private void createOneImgFont(Bitmap bitmap){
        if(bitmap == null) return;

        //获取的位图缩放成16x16大小，转换成黑白带灰度的图片
        Bitmap newBitMap = BitmapUtils.ScaleAndGray(bitmap,16);

        //获取位图宽高
        int width = newBitMap.getWidth();
        int height = newBitMap.getHeight();
        int current_color;

        //获得每一个位点的颜色
        boolean[][] b = new boolean[16][16];
        for (int i=0;i<width;i++) {
            for (int j = 0; j < height; j++) {
                current_color = newBitMap.getPixel(i, j);
                b[j][i] = current_color != 0 && current_color != 0xffffffff;
            }
        }
        //根据条件判断是否反转和旋转角度
        //旋转角度
        if(rotate != 0){
            HexUtils.rotate(b, rotate);
        }
        if(!isModeNormal){//反转
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    b[i][j] = !b[i][j];
                }
            }
        }
        activity.addFrame(b);
    }
}
