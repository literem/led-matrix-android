package com.literem.matrix.music.dialog;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.common.base.BasePopupWindow;
import com.literem.matrix.music.R;
import com.literem.matrix.music.adapter.AdapterPopupChooseMusic;
import com.literem.matrix.music.entity.Music;
import com.literem.matrix.music.utils.SQLiteMusicUtils;

import java.util.List;

/**
 * author : literem
 * time   : 2023/02/10
 * desc   :
 * version: 1.0
 */
public class PopupChooseMusic extends BasePopupWindow {

    private AdapterPopupChooseMusic adapter;
    private OnMusicChooseListener listener;

    public PopupChooseMusic(Activity activity,OnMusicChooseListener listener) {
        super(activity);
        this.listener = listener;
    }

    @Override
    protected int setLayoutId() {
        return R.layout.pop_music_choose;
    }

    @Override
    protected void initView(View view) {
        adapter = new AdapterPopupChooseMusic(context,onClickListener);
        RecyclerView recycleView = view.findViewById(R.id.recycle_view);
        recycleView.setLayoutManager(new LinearLayoutManager(context));
        recycleView.setAdapter(adapter);
        addClickListener(view);
        List<Music> list = SQLiteMusicUtils.getInstance(context).queryAllMusic();
        adapter.setList(list);
    }


    @Override
    protected void initPopupWindow(PopupWindow popupWindow) {
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(getScreenHeight() / 5 * 4);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setAnimationStyle(R.style.popup_anim_bottom_top);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
    }

    @Override
    protected void onViewClick(View view) {
        if(view.getId() == R.id.ll_item_root){
            int position = adapter.getItemPosition((ViewParent) view);
            listener.onMusicChoose(adapter.getMusic(position));
        }
    }

    public void showDialog(){
        backgroundAlpha(0.6f);//设置遮罩层
        popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    public interface OnMusicChooseListener{
        void onMusicChoose(Music music);
    }
}
