package com.literam.matrix.music.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.literam.matrix.music.R;
import com.literam.matrix.music.entity.Music;

import java.io.File;
import java.text.DecimalFormat;

public class DialogMusicInfo extends Dialog {

    private Context context;
    private Music music;
    private String strName,strSigner,strAlbum,strPath,strSize,strTime;

    public DialogMusicInfo(@NonNull Context context, Music music) {
        super(context);
        this.context = context;
        this.music = music;

        strName   = "歌名：%1$s";
        strSigner = "歌手：%1$s";
        strAlbum = "专辑：%1$s";
        strPath = "路径：%1$s";
        strSize   = "大小：%1$s";
        strTime = "添加时间：%1$s";

        if(music == null) return;
        initView();
    }

    private void initView() {
        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_music_info,null);

        //关闭按钮
        ImageView ivClose = view.findViewById(R.id.iv_close);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        TextView tvName,tvSigner,tvAlbum,tvLength,tvPath,tvSize,tvTime,tvLyric;
        tvName = view.findViewById(R.id.tv_name);
        tvSigner = view.findViewById(R.id.tv_signer);
        tvAlbum = view.findViewById(R.id.tv_album);
        tvLength = view.findViewById(R.id.tv_length);
        tvPath = view.findViewById(R.id.tv_path);
        tvSize = view.findViewById(R.id.tv_size);
        tvTime = view.findViewById(R.id.tv_time);
        tvLyric = view.findViewById(R.id.tv_lyric);

        tvName.setText(getText(strName,music.getTitle()));
        tvSigner.setText(getText(strSigner,music.getSinger()));
        tvAlbum.setText(getText(strAlbum,music.getAlbum()));
        tvLength.setText(getLength());
        tvSize.setText(getText(strSize,getFileSize(music.getPath())));
        tvPath.setText(getText(strPath,music.getPath()));
        tvTime.setText(getText(strTime,music.getAddTime()));
        tvLyric.setText(getLrc());

        super.setContentView(view);
        setCanceledOnTouchOutside(false);

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int width = dm.widthPixels;
        width = width - width / 10;

        Window window = getWindow();
        if(window != null){
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.gravity = Gravity.CENTER;
            lp.width = width;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//设置Dialog背景透明
            window.setDimAmount(0.5f);//设置Dialog窗口后面的透明度
        }
    }

   /* private String getLyricName(int lyricId){
        SQLiteMusicUtils sql = SQLiteMusicUtils.getInstance(context);
        Lyric lyric = sql.queryLyricById(lyricId);
        if(lyric == null){
            return "暂无歌词";
        }
        return lyric.getName();
    }*/

   private String getText(String str, String value){
       if(value == null){
           return String.format(str,"暂无");
       }
       return String.format(str,value);
   }

   private String getLength(){
       int length = music.getDuration();
       if(length <= 0){
           return "时长：未知";
       }

       int sec = length / 1000;
       int min = sec / 60;
       sec = sec %60;
       return "时长：" + min+"分"+sec+"秒";
   }

   private String getLrc(){
       String lrc = music.getLyric();
       if(lrc != null && lrc.length() > 10){
           return "歌词："+music.getTitle() + ".lrc";
       }else{
           return "歌词：无";
       }

   }

    private String getFileSize(String path){
        File file = new File(path);
        String size;
        if(file.exists() && file.isFile()){
            long fileS = file.length();
            DecimalFormat df = new DecimalFormat("#.00");
            if (fileS < 1024) {
                size = df.format((double) fileS) + "BT";
            } else if (fileS < 1048576) {
                size = df.format((double) fileS / 1024) + "KB";
            } else if (fileS < 1073741824) {
                size = df.format((double) fileS / 1048576) + "MB";
            } else {
                size = df.format((double) fileS / 1073741824) +"GB";
            }
            return size;
        }
        return "未知大小";
    }
}
