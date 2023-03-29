package com.literam.matrix.music.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.literam.matrix.common.bluetooth.BTData;
import com.literam.matrix.common.bluetooth.BTDialog;
import com.literam.matrix.common.bluetooth.BTUtils;
import com.literam.matrix.common.data.DataUtils;
import com.literam.matrix.common.dialog.DialogPrompt;
import com.literam.matrix.common.dialog.DialogSelectAnim;
import com.literam.matrix.common.dialog.PopupItemMenu;
import com.literam.matrix.common.font.CommandUtils;
import com.literam.matrix.common.utils.ToastUtils;
import com.literam.matrix.common.widget.DrawableTextView;
import com.literam.matrix.music.R;
import com.literam.matrix.music.activity.AddLyricActivity;
import com.literam.matrix.music.activity.MusicActivity;
import com.literam.matrix.music.activity.TextEditActivity;
import com.literam.matrix.music.callback.OnMusicDetailsDialogListener;
import com.literam.matrix.music.callback.OnMusicServiceListener;
import com.literam.matrix.music.callback.UpdateProgressListener;
import com.literam.matrix.music.dialog.DialogAddLyric;
import com.literam.matrix.music.dialog.DialogMusicInfo;
import com.literam.matrix.music.dialog.DialogMusicUpdateInfo;
import com.literam.matrix.music.dialog.PopMusicDetails;
import com.literam.matrix.music.dialog.PopMusicPlayList;
import com.literam.matrix.music.dialog.PopMusicPlayMode;
import com.literam.matrix.music.entity.Music;
import com.literam.matrix.music.service.MusicService;
import com.literam.matrix.music.utils.SQLiteMusicUtils;
import com.literam.matrix.music.widget.LyricView;

import java.util.Random;

public class MusicPlayFragment extends Fragment implements UpdateProgressListener {

    public static final int CHANGE_VOLUME = 6;
    public static final int PLAY_MODE = 7;

    //歌词相关
    public static final int AddLyricAction = 10;
    public static final int AddLyric = 11;//增加歌词
    public static final int RemoveLyric = 12;//删除歌词
    public static final int UpdateLyric = 13;//修改歌词
    public static final int MakeLyric = 14;//制作歌词
    public static final int MusicInfo = 20;
    public static final int MusicUpdateInfo = 21;
    public static final int MusicUpdateInfoAction = 22;
    private String[] strTimes = new String[]{"00:0%1$d","00:%1$d","0%1$d:0%2$d","0%1$d:%2$d","%1$d:0%2$d","%1$d:%2$d"};

    //控件
    private LyricView lyricView;
    private ImageView ivPlay;
    private ImageView ivPlayMode;
    private TextView tvTitle, tvSinger;
    private TextView tvCurrentTime,tvTotalTime;
    private SeekBar seekBar;
    private DrawableTextView dtvLyricSync,dtvConnectBT, dtvAnim;

    //dialog
    private PopMusicPlayList playList;//音乐列表的弹出框
    private MusicService musicService;
    private PopMusicDetails musicDetails;
    private MusicActivity.OnJumpListener jumpListener;

    private String[] listAnim = new String[]{"翻页动画","自定义动画"};

    private boolean isSync = false;//是否同步歌词到点阵屏上
    private boolean isTogglePage = true;//是否同步歌词到点阵屏上

    private boolean isTouchSeekBar;//是否正在拖动进度条
    private int imgStart,imgPause;//播放、暂停图标
    private int dragProgress = 0;//拖动进度条的临时值
    private int singleLoop, listOrder, listRandom;
    private int currentMusicId = -1;//当前播放的音乐id
    private int PlayMode;

    private AudioManager am;//音量对象
    private MusicActivity.UpdateHandler handler;
    private Context context;
    private BTDialog btDialog;

    public MusicPlayFragment(Context context, PopMusicPlayList playList, MusicService musicService, MusicActivity.UpdateHandler handler, MusicActivity.OnJumpListener jumpListener){
        this.context = context;
        this.playList = playList;
        this.musicService = musicService;
        this.handler = handler;
        this.jumpListener = jumpListener;
        imgPause = R.drawable.img_player_start;
        imgStart = R.drawable.img_player_pause;
        listOrder = R.drawable.img_mode_order_white;
        singleLoop = R.drawable.img_mode_single_loop_white;
        listRandom = R.drawable.img_mode_random_white;
        am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_play,container,false);
        lyricView = view.findViewById(R.id.lyric_view);
        lyricView.setOnLyricViewListener(onLyricViewListener);
        dtvLyricSync = view.findViewById(R.id.dtv_lyric_sync);
        dtvConnectBT = view.findViewById(R.id.dtv_connect_bt);
        dtvAnim = view.findViewById(R.id.dtv_toggle_anim);
        dtvLyricSync.setOnClickListener(onClickListener);
        dtvConnectBT.setOnClickListener(onClickListener);
        dtvAnim.setOnClickListener(onClickListener);
        ivPlay = view.findViewById(R.id.iv_lyric_play);
        ivPlayMode = view.findViewById(R.id.iv_lyric_play_mode);
        tvTitle = view.findViewById(R.id.tv_lyric_title);
        tvSinger = view.findViewById(R.id.tv_lyric_signer);
        tvCurrentTime = view.findViewById(R.id.tv_lyric_current_time);
        tvTotalTime = view.findViewById(R.id.tv_lyric_current_total_time);
        seekBar = view.findViewById(R.id.sb_play_progress);
        seekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
        view.findViewById(R.id.iv_play_list).setOnClickListener(onClickListener);
        view.findViewById(R.id.iv_lyric_next).setOnClickListener(onClickListener);
        view.findViewById(R.id.iv_lyric_last).setOnClickListener(onClickListener);
        ivPlay.setOnClickListener(onClickListener);
        ivPlayMode.setOnClickListener(onClickListener);
        view.findViewById(R.id.iv_lyric_more).setOnClickListener(onClickListener);
        view.findViewById(R.id.iv_lyric_back).setOnClickListener(onClickListener);
        ivPlayMode.setImageResource(listOrder);
        this.PlayMode = playList.getPlayMode();//获取播放模式
        //setPlayImage(false);//初次进入，设置播放图标为暂停
        dtvConnectBT.setText(BTUtils.getInstance().getConnect()?"已连接":"未连接");
        refreshPlayInfo();
        return view;
    }


    //每次显示当前fragment时都调用，判断是否更新的歌曲信息
    public void refreshPlayInfo(){

        //检查播放模式是否改变了
        if(this.PlayMode != playList.getPlayMode()){
            setPlayMode(playList.getPlayMode());
        }

        //刷新播放图标
        setPlayImage(musicService.getPlayingState());

        //检查歌曲是否改变了，如果是，则要更新
        if(musicService.getCurrentMusicId() != currentMusicId){
            currentMusicId = musicService.getCurrentMusicId();
            setPlayingInfo(musicService.getCurrentMusic());
        }
    }

    private void refreshLyric(){
        int musicId = musicService.getCurrentMusicId();
        String lyric = SQLiteMusicUtils.getInstance(context).queryMusicLyric(musicId);
        lyricView.setLrc(lyric);
    }


    //region 设置
    /**
     * 更新音量大小
     * @param volume 音量
     */
    public void setChangeVolume(int volume){
        if(musicDetails != null)  musicDetails.setVolume(volume);
    }

    private void setPlayImage(boolean status){
        ivPlay.setImageResource(status ? imgStart : imgPause);
    }

    /**
     * 设置当前时间
     * @param second 单位：秒
     */
    private void setCurrentTime(int second){
        //不超过一分钟
        if(second < 60){
            if(second < 10)
                tvCurrentTime.setText(String.format(strTimes[0],second));//秒钟是一位数
            else
                tvCurrentTime.setText(String.format(strTimes[1],second));//秒钟是两位数
            return;
        }
        //超过一分钟
        int min = second / 60;
        second = second - min * 60;
        if(min < 10){//分钟是一位数
            if(second < 10)
                tvCurrentTime.setText(String.format(strTimes[2],min,second));//秒钟是一位数
            else
                tvCurrentTime.setText(String.format(strTimes[3],min,second));//秒钟是两位数
        }else{//分钟是两位数
            if(second < 10)
                tvCurrentTime.setText(String.format(strTimes[4],min,second));//秒钟是一位数
            else
                tvCurrentTime.setText(String.format(strTimes[5],min,second));//秒钟是两位数
        }
    }

    /**
     * 设置进度条最大的时间，并在textView上显示
     * @param milliSecond 单位：毫秒
     */
    private void setMaxTime(int milliSecond){
        seekBar.setMax(milliSecond);
        milliSecond/=1000;
        //不超过一分钟
        if(milliSecond < 60){
            if(milliSecond < 10){
                tvTotalTime.setText(String.format(strTimes[0],milliSecond));//秒钟是一位数
            }else{
                tvTotalTime.setText(String.format(strTimes[1],milliSecond));//秒钟是两位数
            }
            return;
        }

        //超过一分钟
        int min = milliSecond / 60;
        milliSecond = milliSecond - min * 60;


        if(min < 10){//分钟是一位数
            if(milliSecond < 10){
                tvTotalTime.setText(String.format(strTimes[2],min,milliSecond));//秒钟是一位数
            }else{
                tvTotalTime.setText(String.format(strTimes[3],min,milliSecond));//秒钟是两位数
            }
        }else{//分钟是两位数
            if(milliSecond < 10){
                tvTotalTime.setText(String.format(strTimes[4],min,milliSecond));//秒钟是一位数
            }else{
                tvTotalTime.setText(String.format(strTimes[5],min,milliSecond));//秒钟是两位数
            }
        }
    }

    /**
     * 设置播放模式
     * @param mode 播放模式
     */
    private void setPlayMode(int mode){
        if(mode == PopMusicPlayList.SingleLoop){
            ivPlayMode.setImageResource(singleLoop);
        }else if(mode == PopMusicPlayList.ListOrder){
            ivPlayMode.setImageResource(listOrder);
        }else if(mode == PopMusicPlayList.ListRandom){
            ivPlayMode.setImageResource(listRandom);
        }
        playList.setPlayMode(mode);
        PlayMode = mode;
    }


    /**
     * 播放音乐时设置当前播放的信息，标题，歌手，播放按钮状态、播放列表项的红色高亮
     */
    private void setPlayingInfo(Music music){
        if(music == null) return;
        setMaxTime(music.getDuration());//设置时间
        lyricView.setLrc(music.getLyric());//设置歌词
        tvTitle.setText(music.getTitle());//设置歌手和歌名
        tvSinger.setText(music.getSinger());
    }
    //endregion


    //region dialog

    //显示是否移除歌词的对话框
    private void showRemoveLyricDialog() {
        DialogPrompt.builder(context)
                .setContent("确定要移除歌词吗？")
                .setOnSureListener(null, new DialogPrompt.OnDialogPromptListener() {
                    @Override
                    public void onButtonClick(DialogPrompt dialog) {
                        dialog.dismiss();
                        if(!lyricView.hasLrc() || musicService.getCurrentMusic() == null){
                            ToastUtils.Show(context,"暂无歌词可移除！");
                            return;
                        }
                        if (SQLiteMusicUtils.getInstance(context).removeLyricByMusicId(musicService.getCurrentMusicId())) {
                            lyricView.reset();
                            ToastUtils.Show(context,"歌词移除成功");
                        }else{
                            ToastUtils.Show(context,"歌词移除失败");
                        }
                    }
                })
                .showDialog();
    }


    //endregion


    //region 监听事件

    /**
     * -------------- MusicService的回调 ---------------
     */
    public OnMusicServiceListener musicServiceListener = new OnMusicServiceListener() {
        @Override
        public void onPlayPrepare(int position, Music music) {
            setPlayingInfo(music);
            playList.cleanPlayStatus();//清除上一个播放状态
            playList.setPlayStatus(position);//设置当前播放状态
        }

        @Override
        public void onPlayCompletion(int position) {
            int PLAY_MODE = playList.getPlayMode();
            if(PLAY_MODE == PopMusicPlayList.SingleLoop){//单曲循环
                musicService.playMusic(position);
            }else if(PLAY_MODE == PopMusicPlayList.ListOrder){//顺序播放
                if(!musicService.next()) {//如果不存在下一首，停止播放，
                    onPlayPause();
                }
            }else if(PLAY_MODE == PopMusicPlayList.ListRandom){//随机播放
                Random random = new Random();
                int size = playList.getListSize();
                if(size <= 2){//如果列表只有两首
                    musicService.playMusic(0);
                }else{
                    musicService.playMusic(random.nextInt(size));
                }
            }
        }

        @Override
        public void onPlayPause() {
            setPlayImage(false);
        }

        @Override
        public void onPlayContinue() {
            setPlayImage(true);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onPlayStop() {
            playList.cleanPlayStatus();//清除上一个播放状态
            setPlayImage(false);
            lyricView.reset();
            seekBar.setProgress(0);
            tvCurrentTime.setText("00:00");
            tvTitle.setText("选择要播放的音乐");//设置歌手和歌名
            tvSinger.setText("");
        }
    };

    /**
     * -------------- 歌词滚动 ---------------
     */
    private LyricView.OnLyricViewListener onLyricViewListener = new LyricView.OnLyricViewListener() {
        @Override
        public void onPlayButtonClick(int progress) {
            musicService.seekTo(progress*1000);
        }

        @Override
        public void currentLyric(String lyric) {
            if(!isSync || lyric.length() > 20) return;
            if(lyric.length() > DataUtils.ModuleSize ){ //同步歌词
                Message msg = Message.obtain();
                msg.what = BTData.TOGGLE;
                msg.obj = lyric.length() - DataUtils.ModuleSize;
                handler.sendMessageDelayed(msg,lyricView.getLineTotalTime()/2);
            }
            byte[] b = CommandUtils.getInstance().setDisplayStaticMove(isTogglePage?CommandUtils.MOVE_PAGE_DOWN:CommandUtils.MOVE_ANIM,lyric);
            handler.sendOrAddQueue(b);
        }
    };

    /**
     * -------------- 拖动进度条监听 ---------------
     */
    private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(isTouchSeekBar){
                progress /= 1000;
                if(progress != dragProgress){//滑动会有多次相同值，所以在此判断，保证每个不同的值只设置一遍
                    setCurrentTime(progress);
                    dragProgress = progress;
                }
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isTouchSeekBar = true;
        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isTouchSeekBar = false;
            musicService.seekTo(seekBar.getProgress());
        }
    };

    /**
     * -------------- 更多菜单 弹出的dialog点击的监听 ---------------
     */
    private OnMusicDetailsDialogListener onMusicDetailsDialogListener = new OnMusicDetailsDialogListener() {
        @Override
        public void onValueChange(int dat, int status) {
            if(status == CHANGE_VOLUME){//音量改变
                am.setStreamVolume(AudioManager.STREAM_MUSIC, dat, 0);
            }else if(status == PLAY_MODE){//设置播放模式
                setPlayMode(dat);
            }
        }

        //歌词操作相关的回调
        @Override
        public void onLyricChange(String dat, int status) {
            switch (status){

                case AddLyricAction://添加歌词操作（点击后的操作）
                    if(musicService.getCurrentMusic() == null){
                        ToastUtils.Show(context,"当前没有播放的音乐");
                        return;
                    }
                    musicDetails.dismiss();
                    DialogAddLyric dialogAddLyric = new DialogAddLyric(context,musicService.getCurrentMusicId(),this);
                    dialogAddLyric.show();
                    break;

                case AddLyric://添加歌词（注意，该操作是成功的回调，不是点击后的操作）
                    if(dat != null){
                        lyricView.setLrc(dat);
                        ToastUtils.Show(context,"成功添加歌词");
                    }
                    break;

                case RemoveLyric://移除歌词
                    if(musicService.getCurrentMusic() == null){
                        ToastUtils.Show(context,"当前没有播放的音乐");
                        return;
                    }
                    musicDetails.dismiss();
                    showRemoveLyricDialog();
                    break;

                case UpdateLyric://修改当前歌词 跳转页面
                    if(!lyricView.hasLrc() || musicService.getCurrentMusic() == null){
                        ToastUtils.Show(context,"暂无歌词可修改");
                        return;
                    }
                    musicDetails.dismiss();
                    musicService.pause();
                    Intent intent1 = new Intent(context, TextEditActivity.class);
                    intent1.putExtra("text",lyricView.getLyricText());
                    intent1.putExtra("title","编辑歌词");
                    intent1.putExtra("id",musicService.getCurrentMusicId());
                    startActivityForResult(intent1,DataUtils.REQ_EDIT_LRC);
                    break;

                case MakeLyric://制作歌词 跳转页面
                    Music music = musicService.getCurrentMusic();
                    if(music == null){
                        ToastUtils.Show(context,"请先选择歌曲");
                        return;
                    }
                    musicDetails.dismiss();
                    musicService.pause();
                    Intent intent2 = new Intent(context, AddLyricActivity.class);
                    intent2.putExtra("id",music.getId());
                    intent2.putExtra("music_name",music.getTitle());
                    intent2.putExtra("signer",music.getSinger());
                    intent2.putExtra("path",music.getPath());
                    startActivityForResult(intent2,DataUtils.REQ_MAKE_LRC);
                    break;
            }
        }

        //音乐操作相关的回调
        @Override
        public void onMusicChange(int status) {
            if(musicService.getCurrentMusic() == null){
                ToastUtils.Show(context,"当前没有播放的音乐");
                return;
            }
            switch (status){
                case MusicInfo://查看歌曲信息
                    musicDetails.dismiss();
                    new DialogMusicInfo(context,musicService.getCurrentMusic()).show();
                    break;
                case MusicUpdateInfoAction://更改音乐信息操作（在此弹出修改框）
                    musicDetails.dismiss();
                    new DialogMusicUpdateInfo(context,musicService.getCurrentMusic(),this).showDialog();
                    break;
                case MusicUpdateInfo://更改音乐信息操作（在此回调修改后的操作）
                    Music music = musicService.getCurrentMusic();
                    tvTitle.setText(music.getTitle());//设置歌手和歌名
                    tvSinger.setText(music.getSinger());
                    break;
            }
        }
    };

    private PopupItemMenu.OnPopupItemListener popupItemListener = new PopupItemMenu.OnPopupItemListener() {
        @Override
        public void itemClick(int itemId, PopupWindow popupWindow) {
            if(itemId == 0){
                ToastUtils.Show(context,"翻页动画");
                dtvAnim.setText("翻页动画");
                isTogglePage = true;
            }else if(itemId == 1){
                if(!BTUtils.getInstance().getConnect()){
                    ToastUtils.Show(context,"请先连接设备");
                    return;
                }else{
                    new DialogSelectAnim(context,onSelectAnimCallback).showDialog(DataUtils.inAnim, DataUtils.outAnim);
                }
            }
            popupWindow.dismiss();
        }
    };

    //设置同步或不同步歌词
    private void setSyncLyricState(){
        if (isSync){ // 设置成不同步歌词
            isSync = false;
            ToastUtils.Show(context,"不同步歌词");
            dtvLyricSync.setText("不同步歌词");
        }else{ // 设置成同步歌词
            if (!BTUtils.getInstance().getConnect()) {
                ToastUtils.Show(context,"设备未连接，不能同步歌词");
                isSync = false;
            }else{
                ToastUtils.Show(context,"同步歌词");
                dtvLyricSync.setText("同步歌词");
                isSync = true;
            }
        }
    }

    private DialogSelectAnim.OnSelectAnimCallback onSelectAnimCallback = new DialogSelectAnim.OnSelectAnimCallback() {
        @Override
        public void selectAnim(boolean isInAnim, int animValue) {
            byte[] byteAnim;
            if (isInAnim){
                byteAnim =  CommandUtils.getInstance().setDisplayStaticAnim(true, animValue);
                DataUtils.inAnim = animValue;
                ToastUtils.Show(context,"设置进入动画");
            }else{
                byteAnim =  CommandUtils.getInstance().setDisplayStaticAnim(false, animValue);
                DataUtils.outAnim = animValue;
                ToastUtils.Show(context,"设置退出动画");
            }
            handler.sendOrAddQueue(byteAnim);//把发送任务提交到队列中运行
            dtvAnim.setText("自定义动画");
            isTogglePage = false;
        }
    };

    /**
     * -------------- 点击事件 ---------------
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.iv_play_list) {//音乐列表
                playList.showMusicList();
            } else if (id == R.id.iv_lyric_play) {//播放、暂停
                musicService.PlayOrPause();
                ivPlay.setImageResource(musicService.getPlayingState() ? imgStart : imgPause);
            } else if (id == R.id.iv_lyric_next) { //下一曲
                musicService.next();
            } else if (id == R.id.iv_lyric_last) { //上一曲
                musicService.pre();
            } else if (id == R.id.iv_lyric_more) {//更多
                if (musicDetails == null) {
                    musicDetails = new PopMusicDetails(getActivity());
                    musicDetails.setOnValueChangeListener(onMusicDetailsDialogListener);
                    musicDetails.setMaxVolume(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));//获取系统最大音量，设置到seekBar
                    musicDetails.setVolume(am.getStreamVolume(AudioManager.STREAM_MUSIC));//设置当前音量
                }
                musicDetails.setMusicInfoTitle(musicService.getTitleSinger());
                musicDetails.showMusicDetails();
            } else if (id == R.id.iv_lyric_back) {//返回
                jumpListener.onJump();
            } else if (id == R.id.iv_lyric_play_mode) {//播放模式：单曲循环、列表循环、顺序播放
                PopMusicPlayMode playMode = new PopMusicPlayMode(getActivity());
                playMode.setOnItemSelectedListener(onMusicDetailsDialogListener);
                playMode.showAsDropDown(view);
            } else if (id == R.id.dtv_lyric_sync) {//同步歌词到点阵屏
                setSyncLyricState();
            } else if (id == R.id.dtv_connect_bt){//连接蓝牙
                if(!BTUtils.getInstance().getConnect()){
                    if(btDialog==null) btDialog = new BTDialog(getActivity(),handler);
                    btDialog.show();
                }
            } else if (id == R.id.dtv_toggle_anim){//选择翻页动画
                PopupItemMenu popupItemMenu = new PopupItemMenu(getActivity(),popupItemListener);
                popupItemMenu.showDown(view,listAnim);
            }
        }
    };

    /**
     * -------------- 线程更新进度 ---------------
     */
    @Override
    public void onUpdate(int progress) {
        if(!isTouchSeekBar) {//如果不是处于拖动进度条，才更新进度条
            seekBar.setProgress(progress);//设置进度条
            setCurrentTime(progress / 1000);//设置时间
        }
        lyricView.setProgress(progress / 1000,false);//更新歌词
    }

    //endregion

    public void setBTState(boolean state){
        if(state){
            dtvConnectBT.setText("已连接");
        }else{
            dtvConnectBT.setText("未连接");
            dtvLyricSync.setText("不同步歌词");
            isSync = false;
        }
        if(btDialog != null) btDialog.finishConnect(state);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        //编辑歌词
        if (requestCode == DataUtils.REQ_EDIT_LRC && resultCode == DataUtils.EditText){
            if (data == null){
                ToastUtils.Show(context,"歌词更新失败！");
                return;
            }
            int id = data.getIntExtra("id",-1);
            String text = data.getStringExtra("text");
            boolean isSuccess = SQLiteMusicUtils.getInstance(context).updateMusicLyric(id, text);
            if (isSuccess){
                ToastUtils.Show(context,"歌词更新成功！");
                refreshLyric();
            }else{
                ToastUtils.Show(context,"歌词更新失败！");
            }
        }
        //制作歌词
        else if (requestCode == DataUtils.REQ_MAKE_LRC && resultCode == DataUtils.RES_MAKE_LRC){
            refreshLyric();
        }
    }
}
