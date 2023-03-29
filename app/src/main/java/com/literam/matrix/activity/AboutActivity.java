package com.literam.matrix.activity;

import android.view.View;

import com.literam.matrix.R;
import com.literam.matrix.common.base.BaseActivity;

public class AboutActivity extends BaseActivity {

    @Override
    protected int setLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void onInit() {}

    @Override
    protected void onInitView() {
        setToolbarTitle("关于软件");
    }

    @Override
    protected void onViewClick(View view) {}
}
