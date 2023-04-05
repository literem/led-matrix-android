package com.literem.matrix.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.literem.matrix.R;

public class LaunchActivity extends AppCompatActivity {

    private ImageView ivRotate;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//设置全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_launch);
        initView();
        launchActivity();
    }

    private void initView(){
        ivRotate = findViewById(R.id.iv_lunch_loading);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.img_360_run_animation);
        LinearInterpolator lin = new LinearInterpolator();//设置动画匀速运动
        animation.setInterpolator(lin);
        ivRotate.startAnimation(animation);
    }

    private void launchActivity(){
        handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ivRotate.clearAnimation();
                startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                finish();
            }
        },1000);
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
    protected void onDestroy() {
        if(handler != null)
            handler.removeCallbacksAndMessages(null);
        ivRotate.clearAnimation();
        super.onDestroy();
    }
}
