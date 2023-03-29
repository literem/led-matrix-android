package com.literam.matrix.music.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.literam.matrix.common.base.BaseApplication;
import com.literam.matrix.common.bluetooth.BTData;
import com.literam.matrix.common.bluetooth.BTHandler;
import com.literam.matrix.common.data.DataUtils;
import com.literam.matrix.common.dialog.DialogPrompt;
import com.literam.matrix.common.font.CommandUtils;
import com.literam.matrix.common.utils.ShowLoadingUtils;
import com.literam.matrix.common.utils.StatusBarUtil;
import com.literam.matrix.common.utils.ToastUtils;
import com.literam.matrix.music.R;
import com.literam.matrix.music.callback.OnPlayListListener;
import com.literam.matrix.music.callback.UpdateProgressListener;
import com.literam.matrix.music.dialog.PopMusicPlayList;
import com.literam.matrix.music.fragment.MusicListFragment;
import com.literam.matrix.music.fragment.MusicPlayFragment;
import com.literam.matrix.music.service.MusicService;
import com.literam.matrix.music.utils.PlayListData;
import com.literam.matrix.music.utils.SQLiteMusicUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * author : literem
 * time   : 2022/11/09
 * desc   :
 * version: 1.0
 */
public class MusicActivity extends AppCompatActivity {

    private final int UPDATE_PROGRESS = 398;
    private final int UPDATE_BACKGROUND = 399;
    private int currentProgress;
    private MusicPlayFragment playFragment;
    private MusicListFragment listFragment;
    private FrameLayout fragmentContainer;
    private UpdateProgressListener progressListener;
    private int containerID;
    private boolean isShowMain = true;
    private boolean isLoop = true;
    private UpdateHandler handler;
    private PopMusicPlayList playList;
    private PlayListData playListData;
    private MusicService musicService;
    private AudioManager am;//音量
    private VolumeReceiver volumeReceiver;//音量改变的广播
    private ExecutorService progressUpdateThread;
    private ShowLoadingUtils showLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        fragmentContainer = findViewById(R.id.music_fragment);
        StatusBarUtil.setStatusBarColor(this, Color.WHITE);//设置状态栏颜色等
        StatusBarUtil.setLightMode(this);
        fragmentContainer.setBackgroundColor(Color.WHITE);
        playListData = new PlayListData(this);
        playList = new PopMusicPlayList(this, playListListener, playListData);
        handler = new UpdateHandler(getMainLooper(),this);//初始化handler
        BaseApplication.setHandler(handler);
        containerID = R.id.music_fragment;//设置容器ID
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);//初始化音量广播接收器
        volumeReceiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter() ;
        filter.addAction("android.media.VOLUME_CHANGED_ACTION") ;
        registerReceiver(volumeReceiver, filter) ;
        progressUpdateThread = Executors.newSingleThreadExecutor(); //初始化进度刷新线程
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

    // region 播放服务相关
    @Override
    protected void onStart() {
        super.onStart();
        startService(new Intent(this, MusicService.class));
        bindService(new Intent(this, MusicService.class), musicServiceConnect, Context.BIND_AUTO_CREATE);
    }

    /**
     * -------------- 播放服务连接 ---------------
     */
    private ServiceConnection musicServiceConnect = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder mBinder = (MusicService.MusicBinder) service;
            musicService = mBinder.getService();
            musicService.setPlayListData(playListData);
            progressUpdateThread.execute(updateRunnable);
            //如果position不是-1，说明有音乐播放过
           /* if(musicService.getPlayPosition() != -1){
                setPlayingInfo(musicService.getPlayPosition(),musicService.getMusicByCurrentPosition());
                //setMaxTime(mMusicService.getDurationTime());
            }*/
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();// 开启事务
            if (playFragment != null && !playFragment.isHidden()) {//隐藏playFragment
                fragmentTransaction.hide(playFragment);
            }
            if (listFragment == null) {// 如果Fragment为空，则创建一个并添加到界面上
                listFragment = new MusicListFragment(MusicActivity.this,jumpListener,playList,musicService);
                fragmentTransaction.add(containerID, listFragment,"home");
            } else {
                handler.sendEmptyMessageDelayed(UPDATE_BACKGROUND,500);
                fragmentTransaction.show(listFragment);// 如果Fragment不为空，则直接将它显示出来
            }
            progressListener = listFragment;
            fragmentTransaction.commitNowAllowingStateLoss();
            musicService.setMusicServiceListener(listFragment.musicServiceListener);//设置MusicService监听
            isShowMain = true;
        }
    };
    //endregion


    //显示列表fragment
    private void showListFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();// 开启事务
        fragmentTransaction.setCustomAnimations(R.anim.slide_left_in,R.anim.slide_right_out);
        if (playFragment != null && !playFragment.isHidden()) {//隐藏playFragment
            fragmentTransaction.hide(playFragment);
        }
        if (listFragment == null) {// 如果Fragment为空，则创建一个并添加到界面上
            listFragment = new MusicListFragment(this,jumpListener,playList,musicService);
            fragmentTransaction.add(containerID, listFragment,"home");
        } else {
            handler.sendEmptyMessageDelayed(UPDATE_BACKGROUND,500);
            fragmentTransaction.show(listFragment);// 如果Fragment不为空，则直接将它显示出来
        }
        progressListener = listFragment;
        fragmentTransaction.commitNowAllowingStateLoss();
        musicService.setMusicServiceListener(listFragment.musicServiceListener);//设置MusicService监听
        setKeepScreenOn(false);//设置可以息屏
        isShowMain = true;
    }

    //显示播放fragment
    private void showPlayFragment(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();// 开启事务
        fragmentTransaction.setCustomAnimations(R.anim.slide_right_in,R.anim.slide_left_out);
        StatusBarUtil.setDarkMode(this);//设置状态栏颜色等
        StatusBarUtil.setImgMatchStatusBar(this);
        fragmentContainer.setBackgroundResource(R.drawable.img_play_bg);
        if (listFragment != null && !listFragment.isHidden()) {//隐藏listFragment
            fragmentTransaction.hide(listFragment);
        }
        if (playFragment == null) {
            playFragment = new MusicPlayFragment(this,playList,musicService,handler,jumpListener);
            fragmentTransaction.add(containerID, playFragment);
        } else {
            fragmentTransaction.show(playFragment);
            playFragment.refreshPlayInfo();
        }
        progressListener = playFragment;
        fragmentTransaction.commitNowAllowingStateLoss();
        musicService.setMusicServiceListener(playFragment.musicServiceListener);//设置MusicService监听
        setKeepScreenOn(true);//设置不能息屏
        isShowMain = false;
    }

    //设置是否不息屏
    private void setKeepScreenOn(boolean status){
        if(status)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
        if(playList.isShowPopupWindow()){
            playList.dismiss();
            return;
        }
        if(isShowMain){
            showExitDialog();
        }else{
            showListFragment();
            listFragment.refreshPlayInfo();
        }
    }

    private void exit(){
        unregisterReceiver(volumeReceiver);//注销音量广播
        unbindService(musicServiceConnect);//解绑服务
        stopService(new Intent(MusicActivity.this,MusicService.class));
        isLoop = false;
        //关闭进度更新线程
        if(!progressUpdateThread.isShutdown()) progressUpdateThread.shutdownNow();
        progressUpdateThread = null;
        finish();
    }

    //region dialog
    //退出确认框
    private void showExitDialog(){
        DialogPrompt.builder(this)
                .setTitle("提示")
                .setContent("您确定要退出音乐播放器吗？")
                .setOnSureListener("退出", new DialogPrompt.OnDialogPromptListener() {
                    @Override
                    public void onButtonClick(DialogPrompt dialog) {
                        exit();
                        dialog.dismiss();
                    }
                })
                .showDialog();
    }

    //清空播放列表确认框
    private void showCleanDialog(){
        DialogPrompt.builder(this)
                .setTitle("提示")
                .setContent("确定要清空播放列表吗？")
                .setOnSureListener("清空", new DialogPrompt.OnDialogPromptListener() {
                    @Override
                    public void onButtonClick(DialogPrompt dialog) {
                        SQLiteMusicUtils.getInstance(MusicActivity.this).cleanPlayList();
                        ToastUtils.Show(MusicActivity.this,"已清空播放列表");
                        musicService.stopAndClean();
                        playList.clearAll();
                        dialog.dismiss();
                    }
                })
                .showDialog();
    }
    //endregion

    //region 监听
    //跳转监听
    private OnJumpListener jumpListener = new OnJumpListener() {
        @Override
        public void onJump() {
            if(isShowMain){
                showPlayFragment();//是主页就跳到播放页面
            }else{
                showListFragment();//不是主页就跳到主页
                listFragment.refreshPlayInfo();
            }
        }

        @Override
        public void onExit() {
            showExitDialog();
        }
    };

    //播放列表监听
    private OnPlayListListener playListListener = new OnPlayListListener() {

        @Override
        public void onItemClick(int position) {//点击播放列表
            if(playList.isTouchMode) return;
            musicService.playMusic(position);
        }

        @Override
        public void onDeleteClick(int position) { //删除播放列表
            if(playList.isTouchMode) return;
            //删除数据
            boolean isSuccess = SQLiteMusicUtils.getInstance(MusicActivity.this).deleteOnePlayListById(playListData.getPlayListIdByPos(position));
            if(!isSuccess){
                ToastUtils.Show(MusicActivity.this,"删除失败");
                return;
            }
            ToastUtils.Show(MusicActivity.this,"已移除 "+ playListData.getNameByPos(position));
            if(musicService.getPlayPosition() == position){ //如果要删除的是当前播放的
                musicService.stopAndClean();//清除状态栏的播放信息
            }
            if(musicService.getPlayPosition() > position){//如果播放的位置在删除的位置下面，要更新
                musicService.setPlayPosition(position-1);
            }
            playListData.removeAndSort(position);//移除元素并且重新排序
            playList.notifyItemRangeRemoved(position);//刷新列表
            playList.RefreshPlayListCount();//刷新播放列表数量
        }

        @Override
        public void onListClean() { //清除全部播放列表
            /*if(playList.isTouchMode) {//如果处于移动item，点击这个按钮会退出移动模式
                playList.setTouchMoveMode(false);
            }else{
                showCleanDialog();
            }*/
            showCleanDialog();
        }
    };

    //系统音量发生改变的监听
    private class VolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() != null && intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
                if(playFragment != null) playFragment.setChangeVolume(am.getStreamVolume(AudioManager.STREAM_MUSIC));
            }

        }
    }
    //endregion

    public interface OnJumpListener{
        void onJump();
        void onExit();
    }

    //更新进度的runnable，具体实现
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            while(isLoop){
                currentProgress = musicService.getCurrentTime();
                if(currentProgress != 0){
                    handler.sendEmptyMessage(UPDATE_PROGRESS);
                }
                SystemClock.sleep(500);
            }
        }
    };

    public void showLoadingAndDelayDismiss(String text,int delayMS){
        if (showLoading == null) showLoading = new ShowLoadingUtils(this);
        showLoading.show(text);
        handler.sendEmptyMessageDelayed(DataUtils.DISMISS_LOADING,delayMS);
    }


    public class UpdateHandler extends BTHandler<MusicActivity> {
        UpdateHandler(Looper looper, MusicActivity activity){
            super(looper,activity);
        }

        @Override
        protected void onConnectState(int stateFlag, MusicActivity activity) {
            super.onConnectState(stateFlag, activity);
            if (stateFlag == BTData.StartConnect){//开始连接蓝牙
                showLoadingAndDelayDismiss("蓝牙连接中...",10000);
                return;
            }else if(stateFlag == BTData.CloseConnect) {//正在关闭蓝牙
                showLoadingAndDelayDismiss("断开连接中...",10000);
                return;
            }
            if (showLoading != null) showLoading.dismiss();
            if(playFragment != null) playFragment.setBTState(stateFlag == BTData.Success);
        }

        @Override
        protected void onHandle(int what,Object obj, MusicActivity activity) {
            if(what == BTData.TOGGLE){
                sendOrAddQueue(CommandUtils.getInstance().setDisplayStaticHorizontalMove((Integer) obj));
            }else if(what == activity.UPDATE_PROGRESS){
                activity.progressListener.onUpdate(currentProgress);
            }else if (what == DataUtils.DISMISS_LOADING){
                if (showLoading != null) showLoading.dismiss();
            }else if(what == UPDATE_BACKGROUND){
                StatusBarUtil.setStatusBarColor(activity, Color.WHITE);//设置状态栏颜色等
                StatusBarUtil.setLightMode(activity);
                activity.fragmentContainer.setBackgroundColor(Color.WHITE);
            }
        }
    }
}
