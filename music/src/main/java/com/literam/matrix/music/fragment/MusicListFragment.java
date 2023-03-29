package com.literam.matrix.music.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.literam.matrix.common.base.BaseApplication;
import com.literam.matrix.common.dialog.DialogPrompt;
import com.literam.matrix.common.utils.ToastUtils;
import com.literam.matrix.music.R;
import com.literam.matrix.music.activity.MusicActivity;
import com.literam.matrix.music.activity.ScanMusicActivity;
import com.literam.matrix.music.adapter.AdapterMusicList;
import com.literam.matrix.music.callback.OnMusicServiceListener;
import com.literam.matrix.music.callback.UpdateProgressListener;
import com.literam.matrix.music.dialog.DialogMusicInfo;
import com.literam.matrix.music.dialog.PopMusicPlayList;
import com.literam.matrix.music.entity.Music;
import com.literam.matrix.music.entity.PlayList;
import com.literam.matrix.music.service.MusicService;
import com.literam.matrix.music.utils.MusicListData;
import com.literam.matrix.music.utils.SQLiteMusicUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Random;

public class MusicListFragment extends Fragment implements UpdateProgressListener {
    private Context context;
    private AdapterMusicList mMusicListAdapter;

    //主activity传过来的
    private PopMusicPlayList mPlayList;
    private MusicService mMusicService;
    private int imgStart,imgPause;
    private int currentMusicId = -1;
    private String strCount = " (共%1$d首)";
    private TextView tvCount;
    private ImageView ivPlay,ivConvert;
    private TextView tvName,tvSinger;
    private ProgressBar progressBar;

    //跳转监听，在此点击跳转，在activity实现跳转逻辑
    private MusicActivity.OnJumpListener jumpListener;


    public MusicListFragment(Context context, MusicActivity.OnJumpListener jumpListener, PopMusicPlayList mPlayList, MusicService mMusicService){
        this.context = context;
        this.jumpListener = jumpListener;
        this.mPlayList = mPlayList;
        this.mMusicService = mMusicService;
        imgPause = R.drawable.img_player_start_1;//切换图标
        imgStart = R.drawable.img_player_pause_1;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);
        mMusicListAdapter = new AdapterMusicList(context,onItemListener,new MusicListData());
        RecyclerView recyclerView = view.findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(mMusicListAdapter);
        ivPlay = view.findViewById(R.id.iv_play);
        ivPlay.setOnClickListener(onClickListener);
        ivPlay.setImageResource(imgPause);
        view.findViewById(R.id.iv_next).setOnClickListener(onClickListener);
        view.findViewById(R.id.iv_list).setOnClickListener(onClickListener);
        view.findViewById(R.id.tv_add).setOnClickListener(onClickListener);
        view.findViewById(R.id.iv_close).setOnClickListener(onClickListener);
        view.findViewById(R.id.ll_music).setOnClickListener(onClickListener);
        view.findViewById(R.id.iv_more).setOnClickListener(onClickListener);
        progressBar = view.findViewById(R.id.progress_bar);
        tvName = view.findViewById(R.id.tv_name);
        tvSinger = view.findViewById(R.id.tv_signer);
        ivConvert = view.findViewById(R.id.iv_convert);
        ivConvert.setOnClickListener(onClickListener);
        tvCount = view.findViewById(R.id.tv_count);
        tvCount.setText(String.format(strCount, mMusicListAdapter.getItemCount()));
        setPlayImage(false);
        if(mPlayList.getListSize() == 0){//如果列表无歌曲，显示空信息
            setNullInfo();
            return view;
        }
        try {
            mMusicService.prepareMusic(0);//准备列表的第一首歌
            setPlayingInfo(mMusicService.getCurrentMusic());
        } catch (Exception e) {
            setNullInfo();
            mMusicService.setPlayPosition(-1);
            ToastUtils.Show(context,"音乐文件准备失败！");
        }
        return view;
    }

    private void setNullInfo(){
        setPlayImage(false);
        tvName.setText("暂无歌曲");
        tvSinger.setText("请添加歌曲到播放列表");
        ivConvert.setImageResource(R.drawable.img_music_null);
        progressBar.setProgress(0);
    }

    //刷新播放状态
    public void refreshPlayInfo(){
        setPlayImage(mMusicService.getPlayingState());
        progressBar.setProgress(mMusicService.getCurrentTime());
        if(mMusicService.getCurrentMusicId() != currentMusicId){
            currentMusicId = mMusicService.getCurrentMusicId();
            setPlayingInfo(mMusicService.getCurrentMusic());
        }
    }

    //设置播放信息
    private void setPlayingInfo(Music music){
        if(music == null) return;
        currentMusicId = music.getId();

        //选中音乐列表的某项，根据音乐id去音乐列表查询索引
        mMusicListAdapter.queryPosAndCheck(currentMusicId);//position是播放列表的索引
        tvName.setText(music.getTitle());//设置歌手和歌名
        tvSinger.setText(music.getSinger());
        progressBar.setMax(music.getDuration());

        //设置封面图片
        final String url = music.getImage();
        if(url == null || url.length() == 0){
            ivConvert.setImageResource(R.drawable.img_music_null);
            return;
        }
        try {
            File file = new File(url);
            if(file.isFile() && file.exists()){
                FileInputStream fis = new FileInputStream(file);
                ivConvert.setImageBitmap(BitmapFactory.decodeStream(fis));
            }else{
                ivConvert.setImageResource(R.drawable.img_music_null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean imgStatus = true;
    private void setPlayImage(boolean status){
        if(status == imgStatus) return;
        imgStatus = status;
        ivPlay.setImageResource(imgStatus ? imgStart : imgPause);
    }

    //region 回调

    //歌曲列表回调
    private AdapterMusicList.OnMusicListItemListener onItemListener = new AdapterMusicList.OnMusicListItemListener() {

        @Override
        public void onItemAdd(int position, Music music) {//添加到播放列表
            if (addPlayList(music)) {
                ToastUtils.Show(context,"已添加到播放列表");
            }else{
                ToastUtils.Show(context,"添加失败");
            }
        }

        @Override
        public void onItemClick(int position,Music music) {//播放指定索引音乐
            int playListPos = mPlayList.queryPosByMusicId(music.getId());// 通过id查询play list的位置
            if(playListPos == -1){//查不到就添加到播放列表
                if (addPlayList(music)) {
                    ToastUtils.Show(context,"已添加到播放列表");
                    mMusicService.playMusic(mPlayList.getListSize()-1);
                }else{
                    ToastUtils.Show(context,"歌曲添加失败");
                }
            }else{
                mMusicService.playMusic(playListPos);
            }
        }

        @Override
        public void onItemMore(Music music) {//歌曲更多信息
            new DialogMusicInfo(context,music).show();
        }

        @Override
        public void onItemDelete(int position, int musicId) {
            showDeleteDialog(position,musicId);
        }

        private boolean addPlayList(Music music){
            PlayList pl = new PlayList();
            pl.setMusicId(music.getId());
            pl.setTitle(music.getTitle());
            pl.setSinger(music.getSinger());
            int id = SQLiteMusicUtils.getInstance(BaseApplication.getContext()).insertPlayList(pl);
            if (id == -1) return false;
            pl.setId(id);
            mPlayList.addOnePlayList(pl);
            return true;
        }
    };


    private void showDeleteDialog(final int position, final int musicId){
        new AlertDialog.Builder(context)
                .setTitle("您确定要删除吗？")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(SQLiteMusicUtils.getInstance(context).deleteMusic(musicId)){
                            if(mMusicService.getCurrentMusicId() == musicId){//如果要移除的音乐是当前正在播放的，则停掉
                                mMusicService.stopAndClean();
                            }
                            mPlayList.removeByMusicId(musicId);//移除播放列表对应的
                            mMusicListAdapter.removeItem(position);//移除音乐列表对应的
                            ToastUtils.Show(context,"删除成功！");
                        }else{
                            ToastUtils.Show(context,"删除失败！");
                        }
                    }
                })
                .setNegativeButton("取消", null).show();
    }

    //MusicService服务回调
    public OnMusicServiceListener musicServiceListener = new OnMusicServiceListener() {
        @Override
        public void onPlayPrepare(int position, Music music) {//音乐文件加载就绪
            setPlayingInfo(music);
            mPlayList.cleanPlayStatus();//清除上一个播放状态
            mPlayList.setPlayStatus(position);//设置播放列表的播放状态
        }

        @Override
        public void onPlayCompletion(int position) {//播放完成
            int PLAY_MODE = mPlayList.getPlayMode();
            if(PLAY_MODE == PopMusicPlayList.SingleLoop){//单曲循环
                mMusicService.playMusic(position);
            }else if(PLAY_MODE == PopMusicPlayList.ListOrder){
                if(!mMusicService.next()) {//如果不存在下一首，停止播放，
                    mMusicService.stopAndClean();
                }
            }else if(PLAY_MODE == PopMusicPlayList.ListRandom){//随机播放
                Random random = new Random();
                int i = position;
                while(i == position){
                    i = random.nextInt(mPlayList.getListSize() - 1);
                }
                mMusicService.playMusic(i);
            }
        }

        @Override
        public void onPlayPause() {//暂停播放
            setPlayImage(false);
        }

        @Override
        public void onPlayContinue() {//继续播放
            setPlayImage(true);
        }

        @Override
        public void onPlayStop() {//停止播放
            setNullInfo();
            mPlayList.cleanPlayStatus();//清除上一个播放状态
            mMusicListAdapter.unCheckItem();//取消音乐列表选中状态
        }
    };

    //点击事件，播放暂停，下一曲，跳转页面
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.tv_add) {//扫描本地音乐
                startActivityForResult(new Intent(context, ScanMusicActivity.class), 1000);
            } else if (id == R.id.iv_play) {//播放暂停
                mMusicService.PlayOrPause();
            } else if (id == R.id.iv_next) {//下一首
                if (!mMusicService.next()) mMusicService.playMusic(0);//不存在下一首，从0开始播放
            } else if (id == R.id.iv_list) {//播放列表
                mPlayList.showMusicList();
            } else if (id == R.id.ll_music || id == R.id.iv_convert) {//点歌曲图标和歌曲跳转
                jumpListener.onJump();
            } else if (id == R.id.iv_close) {//退出
                jumpListener.onExit();
            }else if(id == R.id.iv_more){
                showClearMenu(view);
            }
        }
    };


    private void showClearMenu(View view) {
        Context wrapper = new ContextThemeWrapper(context, R.style.MenuStyle);
        PopupMenu popupMenu = new PopupMenu(wrapper, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_clear, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_clear){
                    if (mMusicListAdapter.getItemCount() == 0){
                        ToastUtils.Show(context,"音乐列表已经为空");
                    }else{
                        showClearDialog();
                    }
                }
                return true;
            }
        });
    }

    private void showClearDialog(){
        DialogPrompt.builder(context)
                .setContent("确定要清空音乐列表吗？")
                .setOnSureListener("清空", new DialogPrompt.OnDialogPromptListener() {
                    @Override
                    public void onButtonClick(DialogPrompt dialog) {
                        SQLiteMusicUtils.getInstance(context).clearMusicList();
                        mMusicService.stopAndClean();//停止播放
                        mPlayList.clearAll();//清空播放列表
                        mMusicListAdapter.clearAll();//清空音乐列表
                        tvCount.setText(String.format(strCount,0));
                        dialog.dismiss();
                        ToastUtils.Show(context,"已清空音乐列表");
                    }
                }).showDialog();
    }


    @Override
    public void onUpdate(int progress) {
        progressBar.setProgress(progress);
    }
    //endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 1000 && resultCode == 2000){//刷新音乐列表
            mMusicListAdapter.initMusicList();
            tvCount.setText(String.format(strCount, mMusicListAdapter.getItemCount()));
        }
    }
}

