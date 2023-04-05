package com.literem.matrix.music.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.common.base.BaseDialog;
import com.literem.matrix.common.utils.ToastUtils;
import com.literem.matrix.music.R;
import com.literem.matrix.music.adapter.AdapterLocalLyric;
import com.literem.matrix.music.callback.OnMusicDetailsDialogListener;
import com.literem.matrix.music.callback.OnValueChangeListener;
import com.literem.matrix.music.entity.Lrc;
import com.literem.matrix.music.fragment.MusicPlayFragment;
import com.literem.matrix.music.utils.LocalMusicUtils;
import com.literem.matrix.music.utils.SQLiteMusicUtils;


public class DialogAddLyric extends BaseDialog {

    private AdapterLocalLyric adapter;
    private String strCount;
    private OnMusicDetailsDialogListener listener;
    private int musicId;

    public DialogAddLyric(@NonNull Context context, int musicId, OnMusicDetailsDialogListener listener) {
        super(context);
        this.musicId = musicId;
        this.listener = listener;
    }

    @Override
    protected int setLayoutId() {
        return R.layout.dialog_add_lyric;
    }

    @Override
    protected void initView(View view) {
        addOnClickListener(view,R.id.btn_sure);
        addOnClickListener(view,R.id.btn_close);
        RecyclerView recycle = view.findViewById(R.id.recycle_local_lyric);
        recycle.setLayoutManager(new LinearLayoutManager(context));
        adapter = new AdapterLocalLyric(context);
        adapter.addList(LocalMusicUtils.queryLocalLyric(LocalMusicUtils.getLrcDir()));
        adapter.setButtonStateText("已选择","选择");
        adapter.setOnButtonClickListener(new OnValueChangeListener() {
            @Override
            public void onValueChange(int value) {
                adapter.setCurrentItemAdded(value);
            }
        });
        recycle.setAdapter(adapter);
        strCount = "选择本地歌词(%1$d)";
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(String.format(strCount,adapter.getItemCount()));
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void onViewClick(View view) {
        if(view.getId() == R.id.btn_close){
            dismiss();
        }else if(view.getId() == R.id.btn_sure){
            updateLyric();
        }
    }

    //更新歌词到数据库，并且回调成功
    private void updateLyric(){
        //获取当前选中项
        Lrc lrc = adapter.getCurrentItemAdded();
        if(lrc == null) {
            ToastUtils.Show(context,"请选择歌词");
            return;
        }

        //根据当前选中项获取歌词
        String strLrc = LocalMusicUtils.getStringLyricByPath(lrc.getPath());
        if(strLrc == null){
            ToastUtils.Show(context,"歌词获取失败，请检查歌词文件是否存在！");
            return;
        }
        boolean isSuccess = SQLiteMusicUtils.getInstance(context).updateMusicLyric(musicId,strLrc);
        if(isSuccess){
            listener.onLyricChange(strLrc, MusicPlayFragment.AddLyric);
            dismiss();
        }else{
            ToastUtils.Show(context,"歌词获取失败，请检查歌词文件是否存在！");
        }
    }

}
