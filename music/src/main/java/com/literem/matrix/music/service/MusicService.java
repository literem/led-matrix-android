package com.literem.matrix.music.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.literem.matrix.common.utils.ToastUtils;
import com.literem.matrix.music.R;
import com.literem.matrix.music.callback.OnMusicServiceListener;
import com.literem.matrix.music.entity.Music;
import com.literem.matrix.music.utils.PlayListData;
import com.literem.matrix.music.utils.SQLiteMusicUtils;

public class MusicService extends Service {

    //按钮点击状态标识符
    public static String NOTIFICATION_ACTION = "notification_action";
    public static String NOTIFICATION_BTN_STATE = "notification_btn_state";
    public static final int NOTIFICATION_PLAY = 0, NOTIFICATION_NEXT_MUSIC = 1, NOTIFICATION_PRE_MUSIC = 2;
    public static final int PLAY_STATE = 0, NEXT_MUSIC_STATE = 1, PRE_MUSIC_STATE = 2;
    public static final int NOTIFICATION_ID = 2;

    private String ACTION = "action";//动态广播的Action
    public String BTN_STATE = "btn_state";//按钮点击状态标识符

    private int mPosition = -1;
    private MediaPlayer mMediaPlayer;
    private OnMusicServiceListener listener;
    private PlayListData playListData;
    private Music currentMusic = null;


    //onCreate()只被执行一次，因此用来做初始化
    @Override
    public void onCreate() {
        super.onCreate();

        //在MusicService刚刚启动的时候就注册了一个广播，为的是让它来接收到在其他页面点击了上一曲、下一曲、暂停/播放等按钮时，来做相应的处理
        //因此，其他页面中点击上一曲、下一曲、暂停、播放按钮时都需要向MusicService发送广播通知，让它来更新音乐
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION);
        intentFilter.addAction(NOTIFICATION_ACTION);//注册状态栏点击发出的广播
        registerReceiver(receiver, intentFilter);
    }

    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //initNotification();
        initMediaPlayer();
        return initNotify();
    }

    private int initNotify(){
        //点击服务进入Activity
        //Intent activityIntent = new Intent(this, MusicActivity.class);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
        String ticker = "前台MusicService启动";
        String title = "LED点阵控制";
        String text = "音乐播放器正在运行中";

        //前台通知显示
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "alive_channel";//自定义字符串
        String name = "MusicService前台服务";//频道名称
        String description = "前台服务，可以一直运行";//通道描述,界面不显示
        NotificationCompat.Builder notification;
        //Android8.0之后和之前的通知有很大的差异
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            manager.createNotificationChannel(channel);
            notification = new NotificationCompat.Builder(this, id)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)//图标
                    .setTicker(ticker)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setWhen(System.currentTimeMillis());
                    //.setContentIntent(pendingIntent);
        } else {
            notification = new NotificationCompat.Builder(this)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setTicker(ticker)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setWhen(System.currentTimeMillis());
                    //.setContentIntent(pendingIntent);
        }
        startForeground(1, notification.build());
        return START_STICKY;
    }

    public void setPlayListData(PlayListData playListData){
        this.playListData = playListData;
    }


    /**
     * 广播接收器：接收playMusic()、nextMusic()、preMusic()的动态广播
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction() == null) return;
            if (intent.getAction().equals(ACTION)) {
                switch (intent.getIntExtra(BTN_STATE, -1)) {
                    case PLAY_STATE:
                        PlayOrPause();
                        break;
                    case PRE_MUSIC_STATE:
                        pre();
                        break;
                    case NEXT_MUSIC_STATE:
                        next();
                        break;
                    default:
                        Toast.makeText(context, "系统出错，请稍后重试！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            if (intent.getAction().equals(NOTIFICATION_ACTION)) {
                switch (intent.getIntExtra(NOTIFICATION_BTN_STATE, -1)) {
                    case NOTIFICATION_PRE_MUSIC:
                        pre();
                        break;
                    case NOTIFICATION_PLAY:
                        PlayOrPause();
                        break;
                    case NOTIFICATION_NEXT_MUSIC:
                        next();
                        break;
                    default:
                        Toast.makeText(context, "系统出错，请稍后重试！", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    };

    //音乐播放完成时
    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            if(listener == null){
                mPosition += 1;
                int pos = playListData.getSize();
                mPosition = (pos + mPosition) % pos;
                playMusic(mPosition);
                Toast.makeText(getApplicationContext(), "自动为您切换下一首：" + playListData.getNameByPos(mPosition), Toast.LENGTH_SHORT).show();
            }else{
                listener.onPlayCompletion(mPosition);
            }
        }
    };

    /**
     * 初始化播放器
     */
    public void initMediaPlayer() {
        if(mMediaPlayer != null) return;
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
    }

    public void prepareMusic(int position) throws Exception {
        if(mMediaPlayer == null) initMediaPlayer();
        if(playListData.getSize() == 0){//如果播放列表没有音乐，则返回
            ToastUtils.Show(getApplicationContext(),"没有要播放的音乐！");
            throw new Exception();
        }

        if(position < 0) position = 0;
        position = position % playListData.getSize();
        mPosition = position;

        //根据选中的position获取对应的musicId查询music对象
        currentMusic = getMusicByCurrentPosition();
        if(currentMusic == null){
            ToastUtils.Show(getApplicationContext(),"播放失败，音乐文件不存在");
            throw new Exception();
        }
        mMediaPlayer.reset();
        mMediaPlayer.setDataSource(currentMusic.getPath());
        mMediaPlayer.prepare();
        currentMusic.setDuration(mMediaPlayer.getDuration());
        if(listener != null) listener.onPlayPrepare(position,currentMusic);
        //updateNotifyInfo();
    }

    public void playMusic(int position){
        try {
            prepareMusic(position);//准备音乐
            mMediaPlayer.start();//播放音乐
            if(listener != null) listener.onPlayContinue();//回调状态
            //updateNotifyStatus();//更新状态栏
        } catch (Exception e) {
            mPosition = -1;
            currentMusic = null;
        }
    }

    public void setMusicServiceListener(OnMusicServiceListener listener){
        this.listener = listener;
    }

    /**
     * 根据当前状态（是否正在播放）来开始播放或停止播放音乐
     */
    public void PlayOrPause() {
        if(mPosition == -1) {
            playMusic(0);
            return;
        }
        if (mMediaPlayer.isPlaying()) {//播放暂停
            mMediaPlayer.pause();
            if(listener != null) listener.onPlayPause();
        } else {//播放继续
            mMediaPlayer.start();
            if(listener != null) listener.onPlayContinue();
        }
        //updateNotifyStatus();
    }

    public void pause(){
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            if(listener != null) listener.onPlayPause();
            //updateNotifyStatus();
        }
    }

    /**
     * 清除播放信息
     */
    public void stopAndClean(){
        if(mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
        mPosition = -1;
        //clearNotifyInfo();
        currentMusic = null;
        if (listener != null) listener.onPlayStop();
    }


    /**
     * 下一首
     * @return 如果列表还有下一首，返回true，否则返回false
     */
    public boolean next() {
        if(mPosition >= playListData.getSize() - 1) {//如果当前位置超过或等于列表数量-1，不播放
            return false;
        }
        playMusic(mPosition+1);
        return true;
    }

    /**
     * 上一曲
     */
    public void pre() {
        if(mPosition <= 0) {
            playMusic(playListData.getSize() - 1);
        }
        playMusic(mPosition - 1);
    }

    /**
     * 设置音乐播放的进度
     * @param progress 毫秒
     */
    public void seekTo(int progress) {
        mMediaPlayer.seekTo(progress);
    }

    public int getPlayPosition(){
        return this.mPosition;
    }

    public void setPlayPosition(int position){
        this.mPosition = position;
    }


    //region get相关
    /**
     * 获取当前音乐的名字
     */
    /*public String getName() {
        return playListData.getNameByPos(mPosition);
    }*/

    public String getTitleSinger(){
        return playListData.getTitleSinger(mPosition);
    }

    /**
     * 获取当前索引正在播放的音乐的id
     * @return music id
     */
    public int getCurrentMusicId(){
        if(mPosition == -1)return 0;
        return playListData.getMusicIdByPos(mPosition);
    }


    /**
     * 获取当前播放状态
     * @return true:正在播放    false:停止播放
     */
    public boolean getPlayingState(){
        if(mMediaPlayer == null)
            return false;
        return mMediaPlayer.isPlaying();
    }


    /**
     * 获取当前播放的音乐对象
     * @return music
     */
    public Music getMusicByCurrentPosition(){
        if(mPosition != -1 && mPosition < playListData.getSize()){
            int musicId =  playListData.getMusicIdByPos(mPosition);
            SQLiteMusicUtils sql = SQLiteMusicUtils.getInstance(getApplicationContext());
            return sql.queryOneMusicById(musicId);
            /*File file = new File(music.getPath());
            if(file.isFile() && file.exists()){
                return music;
            }*/
        }else{
            return null;
        }
    }

    public Music getCurrentMusic(){
        return this.currentMusic;
    }

    /**
     * 获取当前音乐的播放时间
     */
    public int getDurationTime() {
        return mMediaPlayer.getDuration();
    }

    /**
     * 获取当前播放位置，返回当前播放时间：毫秒
     */
    public int getCurrentTime() {
        if(mMediaPlayer != null)
            return mMediaPlayer.getCurrentPosition();
        return 0;
    }
    //endregion


    @Override
    public void onDestroy() {
        this.listener = null;
        //destroyNotify();
        if(mMediaPlayer != null) mMediaPlayer.release();
        mMediaPlayer = null;
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
