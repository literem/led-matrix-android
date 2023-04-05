package com.literem.matrix.music.activity;

import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.literem.matrix.common.base.BaseActivity;
import com.literem.matrix.common.data.DataUtils;
import com.literem.matrix.common.dialog.DialogPrompt;
import com.literem.matrix.common.dialog.DialogTextInput;
import com.literem.matrix.common.utils.FileUtils;
import com.literem.matrix.common.utils.ShowLoadingUtils;
import com.literem.matrix.common.utils.ToastUtils;
import com.literem.matrix.common.widget.RowScrollView;
import com.literem.matrix.music.R;
import com.literem.matrix.music.callback.OnMusicEventListener;
import com.literem.matrix.music.utils.LocalMusicUtils;
import com.literem.matrix.music.utils.MediaPlayerUtils;
import com.literem.matrix.music.utils.SQLiteMusicUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 制作歌词流程：
 * 选择歌曲 --> 添加歌词 --> 制作歌词
 *
 * 会一直跳转到LyricMakeActivity
 *
 * ChooseMusicActivity  -->  AddLyricActivity  -->  LyricMakeActivity
 *
 *
 *
 * 传入的值，通过getIntent方法获取
 * lyric_text   编辑后的歌词文本
 * id           歌曲id
 * music_name   歌名
 * signer       歌手
 * path         歌曲路径
 */
public class LyricMakeActivity extends BaseActivity {

    private RowScrollView rowScroll;
    private TextView tvName,tvSigner;
    private TextView tvCurrentTime,tvTotalTime;
    private ProgressBar progressBar;
    private ImageView ivPlay;
    private MediaPlayerUtils playerUtils;
    private String musicPath;
    private int currentProgress;
    private String lyricText;
    private List<String> lrcList;
    private String[] lrcTime = new String[]{"[00:0%1$d.%2$s]%3$s\n","[00:%1$d.%2$s]%3$s\n","[0%1$d:0%2$d.%3$s]%4$s\n","[0%1$d:%2$d.%3$s]%4$s\n","[%1$d:0%2$d.%3$s]%4$s\n","[%1$d:%2$d.%3$s]%4$s\n"};
    private String[] playTime = new String[]{"00:0%1$d","00:%1$d","0%1$d:0%2$d","0%1$d:%2$d","%1$d:0%2$d","%1$d:%2$d"};
    private int id;
    private String strFullName;
    private Vibrator vib;

    /**
     * 音乐播放监听
     */
    private OnMusicEventListener mMusicEventListener = new OnMusicEventListener() {
        @Override
        public void onPublish(int progress) {//当前进度
            currentProgress = progress;
            runOnUiThread(runnable);
        }

        @Override
        public void onPlayStart(int position, int max) {//开始播放音乐的回调
            setMaxTime(max);
        }

        @Override
        public void onCompletion(int position) {//播放完成
            playerUtils.setPauseState(ivPlay);
            showSaveDialog();
        }
    };

    /**
     * 更新当前时间
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            progressBar.setProgress(currentProgress);
            setCurrentTime(currentProgress/1000);
        }
    };

    @Override
    protected int setLayoutId() {
        return R.layout.activity_lyric_make;
    }

    @Override
    protected void onInit() {
        strFullName = "%1$s - %2$s";
        lrcList = new ArrayList<>();
        playerUtils = new MediaPlayerUtils();
        playerUtils.setOnMusicEventListener(mMusicEventListener);
        setMusic(getIntent());
        setLyric(getIntent());
        setResult(0);
        vib = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
    }

    @Override
    protected void onInitView() {
        setToolbarTitle("制作LRC歌词");
        rowScroll = findViewById(R.id.text_vertical_scroll);
        rowScroll.disableScrollNext(true);
        tvName = findViewById(R.id.tv_lyric_make_name);
        tvSigner = findViewById(R.id.tv_lyric_make_signer);
        tvCurrentTime = findViewById(R.id.tv_lyric_current_time);
        tvTotalTime = findViewById(R.id.tv_lyric_current_total_time);
        progressBar = findViewById(R.id.progress_lyric_make);
        ivPlay = findAndAddOnClickListener(R.id.dtv_lyric_make_play);
        addOnClickListener(R.id.dtv_lyric_make_choose);
        addOnClickListener(R.id.dtv_lyric_make_edit);
        addOnClickListener(R.id.dtv_lyric_make_restart);
        addOnClickListener(R.id.btn_lyric_make_next);
    }

    @Override
    protected void onViewClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.dtv_lyric_make_choose) {
            if (playerUtils.getPrepare()) {
                showReChooseDialog();
            }else{
                finish();
            }
        } else if (viewId == R.id.dtv_lyric_make_edit) {
            playerUtils.setPlayState(ivPlay);//设置播放状态
            Intent intent = new Intent(LyricMakeActivity.this, TextEditActivity.class);
            intent.putExtra("text", lyricText);
            intent.putExtra("title", "编辑歌词");
            startActivityForResult(intent, DataUtils.EditText);
        } else if (viewId == R.id.dtv_lyric_make_restart) {
            if (playerUtils.getPrepare()) {
                showRestartDialog();
            }
        } else if (viewId == R.id.dtv_lyric_make_play) {
            if (musicPath == null || musicPath.length() == 0) {
                ToastUtils.Show(LyricMakeActivity.this, "请选择歌曲");
                return;
            }
            if (lyricText == null || TextUtils.isEmpty(lyricText)) {
                ToastUtils.Show(LyricMakeActivity.this, "请添加歌词");
                return;
            }
            if (!playerUtils.getPrepare()) {//如果没有准备音乐，则先准备音乐
                if (!playerUtils.prepareMusic(musicPath)) return;//如果准备音乐失败，退出
            }
            playerUtils.setPlayState(ivPlay);//设置播放状态
        } else if (viewId == R.id.btn_lyric_make_next) {
            if (rowScroll.scrollNextLine()) {
                lrcList.add(nextLine());
                if (vib != null) vib.vibrate(50);
            } else {
                ToastUtils.Show(LyricMakeActivity.this, "已到达最后一行");
            }
        }
    }

    /**
     * 根据传入的值设置音乐信息
     */
    private void setMusic(Intent intent){
        if(intent == null) return;
        id = intent.getIntExtra("id",-1);
        if(id == -1){
            ToastUtils.Show(this,"选择的歌曲错误，请重新选择");
            return;
        }
        String musicName,signer;
        musicPath = intent.getStringExtra("path");
        musicName = intent.getStringExtra("music_name");
        signer = intent.getStringExtra("signer");
        if(musicName != null && signer != null){
            tvName.setText(musicName);
            tvSigner.setText(signer);
        }
    }

    /**
     * 根据传入的值设置歌词
     */
    private void setLyric(Intent intent){
        if(intent == null) return;
        String text = intent.getStringExtra("lyric_text");
        if(text == null || TextUtils.isEmpty(text)){
            ToastUtils.Show(LyricMakeActivity.this,"添加失败");
        }else{
            lyricText = text;
            rowScroll.setData(lyricText);
            ToastUtils.Show(LyricMakeActivity.this,"添加成功");
        }
    }

    //下一行
    private String nextLine(){
        String temp;
        String input = rowScroll.getCurrentString(true);
        int time = playerUtils.getCurrentTime();
        int second = time / 1000;
        int milliSecond = time % 1000 / 10;
        String strMilliSecond;
        if(milliSecond < 10){
            strMilliSecond = "0" + milliSecond;
        }else{
            strMilliSecond = String.valueOf(milliSecond);
        }
        if(second < 60){
            if(second < 10){
                temp = String.format(lrcTime[0],second,strMilliSecond,input);
            }else{
                temp = String.format(lrcTime[1],second,strMilliSecond,input);
            }
            return temp;
        }
        int min = second / 60;//超过一分钟
        second = second - min * 60;
        if(min < 10){
            if(second < 10){
                temp = String.format(lrcTime[2],min,second,strMilliSecond,input);
            }else{
                temp = String.format(lrcTime[3],min,second,strMilliSecond,input);
            }
        }else{
            if(second < 10){
                temp = String.format(lrcTime[4],min,second,strMilliSecond,input);
            }else{
                temp = String.format(lrcTime[5],min,second,strMilliSecond,input);
            }
        }
        return temp;
    }

    //保存歌词
    private boolean saveLrc(String fileName){
        if(lrcList == null || lrcList.size() == 0){
            ToastUtils.Show(this,"没有要保存的歌词");
            return false;
        }
        SQLiteMusicUtils sql = SQLiteMusicUtils.getInstance(this);
        StringBuilder sb = new StringBuilder();
        sb.append("[00:00.00]").append(tvName.getText().toString()).append("\n");
        for (String s : lrcList){
            sb.append(s);
        }
        String lyric = sb.toString();
        boolean isWriteFileSuccess = FileUtils.writeTxtToFile(lyric, LocalMusicUtils.getLrcDir(), fileName);
        boolean isUpdateSuccess = sql.updateMusicLyric(this.id,lyric);//更新数据库中的歌词id
        if(isWriteFileSuccess && isUpdateSuccess){
            ToastUtils.Show(this,"保存成功");
        }else if(!isWriteFileSuccess){
            ToastUtils.Show(this,"本地歌词保存失败，歌词文件已保存至软件");
        }else{
            ToastUtils.Show(this,"保存失败");
            return false;
        }
        return true;
    }


    private void waitSaveLrcLoading(final DialogTextInput dialog){
        final ShowLoadingUtils showLoading = new ShowLoadingUtils(this);
        showLoading.show("正在保存中...");
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (saveLrc(dialog.getInputText())) {
                    dialog.dismiss();
                    setResult(1);
                    LyricMakeActivity.super.onBackPressed();
                }
                showLoading.dismiss();
            }
        },1000);
    }

    private void showReChooseDialog(){
        DialogPrompt.builder(this)
                .setContent("当前歌曲正在制作中，是否重新选择歌词和歌曲？")
                .setOnSureListener("重新选择", new DialogPrompt.OnDialogPromptListener() {
                    @Override
                    public void onButtonClick(DialogPrompt dialog) {
                        finish();
                        dialog.dismiss();
                    }
                }).showDialog();
    }

    //显示保存歌词对话框
    private void showSaveDialog(){
        String text = String.format(strFullName,tvSigner.getText(),tvName.getText())+".lrc";
        DialogTextInput.builder(this)
                .setTitle("保存歌词文件")
                .setInputText(text)
                .setOnSureListener(null, new DialogTextInput.OnDialogInputListener() {
                    @Override
                    public void onInputFinish(DialogTextInput dialog, String text) {
                        waitSaveLrcLoading(dialog);
                    }
                })
                .showDialog();
    }

    //设置当前播放的时间
    private void setCurrentTime(int second){
        if(second < 60){//不超过一分钟
            if(second < 10){
                tvCurrentTime.setText(String.format(playTime[0],second));//秒钟是一位数
            }else{
                tvCurrentTime.setText(String.format(playTime[1],second));//秒钟是两位数
            }
            return;
        }
        int min = second / 60; //超过一分钟
        second = second - min * 60;
        if(min < 10){//分钟是一位数
            if(second < 10){
                tvCurrentTime.setText(String.format(playTime[2],min,second));//秒钟是一位数
            }else{
                tvCurrentTime.setText(String.format(playTime[3],min,second));//秒钟是两位数
            }
        }else{//分钟是两位数
            if(second < 10){
                tvCurrentTime.setText(String.format(playTime[4],min,second));//秒钟是一位数
            }else{
                tvCurrentTime.setText(String.format(playTime[5],min,second));//秒钟是两位数
            }
        }
    }

    /**
     * 设置进度条最大的事件
     * @param milliSecond 毫秒
     */
    private void setMaxTime(int milliSecond){
        progressBar.setMax(milliSecond);
        milliSecond/=1000;
        if(milliSecond < 60){//不超过一分钟
            if(milliSecond < 10){
                tvTotalTime.setText(String.format(playTime[0],milliSecond));//秒钟是一位数
            }else{
                tvTotalTime.setText(String.format(playTime[1],milliSecond));//秒钟是两位数
            }
            return;
        }
        int min = milliSecond / 60;//超过一分钟
        milliSecond = milliSecond - min * 60;
        if(min < 10){//分钟是一位数
            if(milliSecond < 10){
                tvTotalTime.setText(String.format(playTime[2],min,milliSecond));//秒钟是一位数
            }else{
                tvTotalTime.setText(String.format(playTime[3],min,milliSecond));//秒钟是两位数
            }
        }else{//分钟是两位数
            if(milliSecond < 10){
                tvTotalTime.setText(String.format(playTime[4],min,milliSecond));//秒钟是一位数
            }else{
                tvTotalTime.setText(String.format(playTime[5],min,milliSecond));//秒钟是两位数
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == DataUtils.EditText){//编辑歌词
            setLyric(data);
        }else if(resultCode == DataUtils.ChangeMusic){//重新选择歌曲
            setMusic(data);
        }
    }

    private void showRestartDialog(){
        String content = "确定要重新开始制作？";
        AlertDialog.Builder builder  = new AlertDialog.Builder(this);
        builder.setTitle("提示") ;
        builder.setMessage(content) ;
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                playerUtils.cleanMusic(ivPlay);
                rowScroll.scrollToFirstLine();
                lrcList.clear();
                progressBar.setProgress(0);
                tvCurrentTime.setText("00:00");

            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    private void showExitDialog(){
        String content = "歌词未制作完成，确定要退出吗？";
        AlertDialog.Builder builder  = new AlertDialog.Builder(this);
        builder.setTitle("提示") ;
        builder.setMessage(content) ;
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

    @Override
    protected boolean onExit() {
        if(playerUtils.getPrepare()){
            showExitDialog();
            return false;
        }
        return true;
    }


    @Override
    protected void onDestroy() {
        playerUtils.Close();
        super.onDestroy();
    }
}
