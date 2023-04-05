package com.literem.matrix.music.activity;

import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.common.base.BaseActivity;
import com.literem.matrix.common.utils.ShowLoadingUtils;
import com.literem.matrix.common.utils.ToastUtils;
import com.literem.matrix.music.R;
import com.literem.matrix.music.adapter.AdapterScanMusic;
import com.literem.matrix.music.entity.LocalMusicBean;
import com.literem.matrix.music.utils.LocalMusicUtils;
import com.literem.matrix.music.utils.SQLiteMusicUtils;

import java.util.List;

public class ScanMusicActivity extends BaseActivity {

    private AdapterScanMusic adapter;
    private TextView tvCheckCount;
    private TextView tvMusicCount;
    private ShowLoadingUtils showLoading;
    private String strMusicCount = "(共%1$d首)",strCheckCount = "已选择%1$d首";

    @Override
    protected int setLayoutId() {
        return R.layout.activity_scan_music;
    }

    @Override
    protected void onInit() {
        showLoading = new ShowLoadingUtils(this);
        adapter = new AdapterScanMusic(this);
        adapter.setOnChangeListener(new AdapterScanMusic.OnCheckBoxChange() {
            @Override
            public void onChange(int count) {
                tvCheckCount.setText(String.format(strCheckCount,count));
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        queryMusic();
    }

    @Override
    protected void onInitView() {
        setToolbarTitle("添加本地歌曲");
        tvCheckCount = findViewById(R.id.tv_check_count);
        tvMusicCount = findViewById(R.id.tv_music_count);
        addOnClickListener(R.id.cb_check_all);
        addOnClickListener(R.id.fl_scan_local);
        addOnClickListener(R.id.fl_add);
        tvCheckCount.setText(String.format(strCheckCount,0));
    }

    @Override
    protected void onViewClick(View view) {
        if (view.getId() == R.id.fl_scan_local){//扫描本机
            queryMusic();
        }else if(view.getId() == R.id.fl_add){//添加歌曲
            addMusic();
        }else if(view.getId() == R.id.cb_check_all){//全选或反选
            CheckBox cb = (CheckBox) view;
            if(!cb.isChecked()){
                adapter.setCheck(false);
                tvCheckCount.setText(String.format(strCheckCount,0));
            }else {
                adapter.setCheck(true);
                tvCheckCount.setText(String.format(strCheckCount,adapter.getItemCount()));
            }
        }
    }


    private void queryMusic(){
        showLoading.show("扫描本地歌曲");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.setList(LocalMusicUtils.queryLocalAllMusic());
                tvMusicCount.setText(String.format(strMusicCount,adapter.getItemCount()));
                showLoading.dismiss();
            }
        },1000);
    }

    private void addMusic(){
        int count = adapter.getCheckCount();
        if(count <= 0){
            ToastUtils.Show(this,"请选择要添加的歌曲");
            return;
        }

        showLoading.show("添加歌曲中");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SQLiteMusicUtils sql = SQLiteMusicUtils.getInstance(ScanMusicActivity.this);
                List<LocalMusicBean> checkList = adapter.getCheckList();
                if(checkList.size() == 1){
                    sql.insertMusic(checkList.get(0));
                }else{
                    sql.insertMusicList(checkList);
                }
                showLoading.dismiss();
                setResult(2000);
                ScanMusicActivity.super.onBackPressed();
            }
        },1000);
    }
}
