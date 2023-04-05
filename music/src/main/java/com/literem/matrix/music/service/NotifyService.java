package com.literem.matrix.music.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.literem.matrix.music.R;
import com.literem.matrix.music.activity.MusicActivity;
import com.literem.matrix.music.entity.PlayList;

import static com.literem.matrix.music.service.MusicService.NOTIFICATION_ACTION;
import static com.literem.matrix.music.service.MusicService.NOTIFICATION_BTN_STATE;

/**
 * author : literem
 * time   : 2023/02/06
 * desc   : 一个更新状态栏音乐播放信息的类，暂不使用
 * version: 1.0
 */
public class NotifyService {

    private static final String CHANNEL_ID = "1";
    private static final String CHANNEL_NAME = "MyChannel";
    private Context context;
    private RemoteViews remoteViews;
    private Notification notification;

    public NotifyService(Context context){
        this.context = context;
    }

    /**
     * 初始化Notification（系统默认UI）
     * 这里是谷歌官方使用步骤：
     * A：要开始，您需要使用notificationCompat.builder对象设置通知的内容和通道。
     * B: 在Android 8.0及更高版本上传递通知之前，必须通过将NotificationChannel实例传递给CreateNotificationChannel（），在系统中注册应用程序的通知通道。
     * C: 每个通知都应该响应tap，通常是为了在应用程序中打开与通知对应的活动。为此，必须指定用PendingIntent对象定义的内容意图，并将其传递给setContentIntent（）。
     * D: 若要显示通知，请调用notificationManagerCompat.notify（），为通知传递唯一的ID以及notificationCompat.builder.build（）的结果。
     */
    private void initNotification() {
        if(remoteViews != null) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
        Intent intent = new Intent(context, MusicActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification);
        remoteViews.setTextViewText(R.id.tv_music_name, "");
        remoteViews.setTextViewText(R.id.tv_music_signer,"");
        remoteViews.setOnClickPendingIntent(R.id.iv_last_music, getPendingIntent(context, MusicService.NOTIFICATION_PRE_MUSIC));
        remoteViews.setOnClickPendingIntent(R.id.iv_play_pause, getPendingIntent(context, MusicService.NOTIFICATION_PLAY));
        remoteViews.setOnClickPendingIntent(R.id.iv_next_music, getPendingIntent(context, MusicService.NOTIFICATION_NEXT_MUSIC));
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.img_music)
                .setContent(remoteViews)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE);
        notification = builder.build();
        //两种方法都可以，这里使用兼容的NotificationManager
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(MusicService.NOTIFICATION_ID, notification);
//        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
//        manager.notify(NOTIFICATION_ID,builder.build());
    }

    private PendingIntent getPendingIntent(Context context, int state) {
        Intent intent = new Intent();
        intent.setAction(NOTIFICATION_ACTION);
        Bundle bundle = new Bundle();
        bundle.putInt(NOTIFICATION_BTN_STATE, state);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = null;
        switch (state) {
            case MusicService.NOTIFICATION_PRE_MUSIC:
                pendingIntent = PendingIntent.getBroadcast(context,1,intent, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case MusicService.NOTIFICATION_PLAY:
                pendingIntent = PendingIntent.getBroadcast(context,2,intent, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
            case MusicService.NOTIFICATION_NEXT_MUSIC:
                pendingIntent = PendingIntent.getBroadcast(context,3,intent, PendingIntent.FLAG_UPDATE_CURRENT);
                break;
        }
        return pendingIntent;
    }

    //更新Notification
    private void updateNotifyInfo(PlayList playList,boolean playingStatus) {
        remoteViews.setImageViewResource(R.id.iv_play_pause, playingStatus ? R.drawable.img_play_pause : R.drawable.img_play_start);
        remoteViews.setTextViewText(R.id.tv_music_name, playList.getTitle());
        remoteViews.setTextViewText(R.id.tv_music_signer, playList.getSinger());
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(MusicService.NOTIFICATION_ID, notification);
    }
    private void updateNotifyStatus(boolean playingStatus) {
        remoteViews.setImageViewResource(R.id.iv_play_pause, playingStatus ? R.drawable.img_play_pause : R.drawable.img_play_start);
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(MusicService.NOTIFICATION_ID, notification);
    }

    public void clearNotifyInfo(){
        remoteViews.setImageViewResource(R.id.iv_play_pause,R.drawable.img_play_start);
        remoteViews.setTextViewText(R.id.tv_music_name, "选择要播放的歌曲");
        remoteViews.setTextViewText(R.id.tv_music_signer, "");
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(MusicService.NOTIFICATION_ID, notification);
    }

    private void destroyNotify(){
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.cancel(MusicService.NOTIFICATION_ID);
    }

    //endregion相关
}
