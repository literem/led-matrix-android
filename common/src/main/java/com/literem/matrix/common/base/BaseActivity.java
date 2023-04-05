package com.literem.matrix.common.base;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.literem.matrix.common.R;
import com.literem.matrix.common.utils.StatusBarUtil;

@SuppressLint("Registered")
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setLayoutId());
        setStatusBarColor(Color.WHITE);
        onInitView();
        onInit();
    }

    protected abstract int setLayoutId();
    protected abstract void onInit();
    protected abstract void onInitView();
    protected abstract void onViewClick(View view);

    protected void addOnClickListener(View view){
        view.setOnClickListener(clickListener);
    }

    protected void addOnClickListener(int viewID){
        if (clickListener != null){
            findViewById(viewID).setOnClickListener(clickListener);
        }
    }

    protected <T extends View> T findAndAddOnClickListener(int viewID){
        if (clickListener != null){
            findViewById(viewID).setOnClickListener(clickListener);
        }
        return findViewById(viewID);
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

    public void setStatusBarColor(int color) {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        StatusBarUtil.setColor(this, color, 0);
        mToolbar.setBackgroundColor(color);
        StatusBarUtil.setLightMode(this);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar == null) return;
        actionBar.setDisplayShowTitleEnabled(false);//隐藏默认标题
        if(isShowBackKey()) {//显示返回键
            actionBar.setDisplayHomeAsUpEnabled(true);//添加默认的返回图标
            actionBar.setHomeButtonEnabled(true); //设置返回键可用
        }
    }
    public void setToolbarTitle(String title) {
        TextView tvTitle = findViewById(R.id.toolbar_title);
        tvTitle.setText(title);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            if (onExit()) super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isShowBackKey(){
        return true;
    }

    @Override
    public void onBackPressed() {
        if (onExit()) super.onBackPressed();
    }

    protected View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onViewClick(v);
        }
    };


    /**
     * 退出方法，当点击返回键或者点击toolbar的返回图标时调用该方法
     * @return true：关闭页面，false，无任何操作
     */
    protected boolean onExit(){
        return true;
    }
}
