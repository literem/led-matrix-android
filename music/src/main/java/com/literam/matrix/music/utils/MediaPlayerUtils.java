package com.literam.matrix.music.utils;

import android.media.MediaPlayer;
import android.os.SystemClock;
import android.widget.ImageView;

import com.literam.matrix.music.R;
import com.literam.matrix.music.callback.OnMusicEventListener;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaPlayerUtils {

    private boolean isPrepare;

    private MediaPlayer mediaPlayer;
    private OnMusicEventListener mListener;
    private ExecutorService mProgressUpdatedListener = Executors.newSingleThreadExecutor();

    private int iv_res_start,iv_res_pause;

    public MediaPlayerUtils(){
        iv_res_start = R.drawable.img_player_start_black;
        iv_res_pause = R.drawable.img_player_pause_black;
    }

    /**
     * 更新进度的线程
     */
    private Runnable mPublishProgressRunnable = new Runnable() {
        @Override
        public void run() {
            while (isPrepare) {
                if (mediaPlayer != null && mediaPlayer.isPlaying() && mListener != null) {
                    mListener.onPublish(mediaPlayer.getCurrentPosition());
                }
                SystemClock.sleep(300);
            }
        }
    };

    public boolean getPrepare(){
        return isPrepare;
    }

    public int getCurrentTime(){
        if (mediaPlayer!=null){
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    //根据当前播放状态，暂停或开始音乐
    public void setPlayState(ImageView iv){
        if(mediaPlayer.isPlaying()){
            setPauseState(iv);
        }else{
            iv.setImageResource(iv_res_pause);
            mediaPlayer.start();
        }
    }

    //设置暂停状态
    public void setPauseState(ImageView iv){
        iv.setImageResource(iv_res_start);
        mediaPlayer.pause();
    }

    public void cleanMusic(ImageView iv){
        isPrepare = false;
        mediaPlayer.stop();
        iv.setImageResource(iv_res_start);
    }

    public boolean prepareMusic(String path){
        File file = new File(path);
        if (!file.exists()) {
            //ToastUtils.Show(this,"音乐文件不存在！");
            return false;
        }

        if(mediaPlayer == null){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    if(mListener!=null && isPrepare) mListener.onCompletion(0);
                    isPrepare = false;
                }
            });
        }
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            isPrepare = true;
            if(mListener != null) mListener.onPlayStart(0,mediaPlayer.getDuration());
            mProgressUpdatedListener.execute(mPublishProgressRunnable);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void setOnMusicEventListener(OnMusicEventListener l) {
        mListener = l;
    }

    public void Close(){
        if(!mProgressUpdatedListener.isShutdown()) mProgressUpdatedListener.shutdownNow();
        mProgressUpdatedListener = null;


        if(mediaPlayer != null) {
            if(mediaPlayer.isPlaying()) mediaPlayer.stop();
            mediaPlayer.release();
        }
        mediaPlayer = null;
    }
}
