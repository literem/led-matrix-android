package com.literem.matrix.music.dialog;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.common.base.BasePopupWindow;
import com.literem.matrix.common.utils.ToastUtils;
import com.literem.matrix.music.R;
import com.literem.matrix.music.adapter.AdapterPopupPlayList;
import com.literem.matrix.music.callback.OnPlayListListener;
import com.literem.matrix.music.entity.PlayList;
import com.literem.matrix.music.utils.PlayListData;
import com.literem.matrix.music.utils.SQLiteMusicUtils;


//展示歌曲播放列表的弹窗
public class PopMusicPlayList extends BasePopupWindow {
    public static final int ListOrder = 1;//顺序播放
    public static final int ListRandom = 2;//随机播放
    public static final int SingleLoop = 3;//单曲循环
    private OnPlayListListener listListener;
    public AdapterPopupPlayList adapter;
    private PlayListData playListData;
    private TextView tvSize;
    private TextView tvPlayMode;
    private ImageView ivPlayMode;
    private String strSize = "全部歌曲(%1$d)";
    public boolean isTouchMode;
    private int lastPosition = -1;
    private int PLAY_MODE = ListOrder;

    /**
     * 初始化操作
     */
    public PopMusicPlayList(Activity activity, OnPlayListListener listListener, PlayListData playListData){
        super(activity);
        this.listListener = listListener;
        this.playListData = playListData;
        adapter = new AdapterPopupPlayList(context,playListData,listListener);
        RecyclerView recyclerView = view.findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
        RefreshPlayListCount();
    }

    @Override
    protected int setLayoutId() {
        return R.layout.pop_music_play_list;
    }

    @Override
    protected void initView(View view) {
        tvSize = view.findViewById(R.id.dialog_Music_list_size);
        ivPlayMode = view.findViewById(R.id.iv_play_mode);
        tvPlayMode = findAndAddClickListener(R.id.tv_play_mode);
        addClickListener(R.id.dialog_music_list_clean);
    }

    @Override
    protected void initPopupWindow(PopupWindow popupWindow) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(dm.heightPixels / 2);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setAnimationStyle(R.style.popup_anim_bottom_top);//设置滑入滑出的动画效果
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true); //设置可获取焦点
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }});
    }

    @Override
    protected void onViewClick(View view) {
        if(view.getId() == R.id.dialog_music_list_clean){//清空全部
            listListener.onListClean();
        }else if(view.getId() == R.id.tv_play_mode){//播放模式
            setPlayMode(PLAY_MODE+1);
        }
    }

    //刷新播放列表的数量
    public void RefreshPlayListCount(){
        tvSize.setText(String.format(strSize,adapter.getItemCount()));
    }

    //清空播放列表
    public void clearAll(){
        this.playListData.clean();
        adapter.notifyDataSetChanged();
        tvSize.setText(String.format(strSize,0));
    }

    //根据musicId在播放列表中移除数据
    public void removeByMusicId(int musicId){
        //移除与musicId匹配的项，只有移除了才刷新列表，更新数据库
        if (playListData.removeByMusicId(musicId)) {
            SQLiteMusicUtils.getInstance(context).removePlayListByMusicId(musicId);//移除数据库中与musicId匹配的
            adapter.notifyDataSetChanged();
        }

    }

    //通过id找到索引
    public int queryPosByMusicId(int musicId){
        return playListData.queryPosByMusicId(musicId);
    }

    /** ----------------------------- set相关 -----------------------------*/
    //region

    //添加一条列表信息
    public void addOnePlayList(PlayList playList){
        adapter.addOne(playList);
        RefreshPlayListCount();
    }

    //设置滑动模式
    /*public void setTouchMoveMode(boolean isTouch){
        isTouchMode = isTouch;
        if(isTouch) {
            popupWindow.setOutsideTouchable(false);//禁止点击外面关闭
            tvClean.setText("完成");
            tvClean.setTextViewDrawable(context.getDrawable(R.drawable.img_finish),context,7,7);
        } else {
            popupWindow.setOutsideTouchable(true);//允许点击外面关闭
            tvClean.setText("清空");
            tvClean.setTextViewDrawable(context.getDrawable(R.drawable.img_delete),context,7,7);
            adapter.setItemRightImg(adapter.IMG_DEL);
        }
    }*/

    //更新删除，从给定开始的索引更新到末尾
    public void notifyItemRangeRemoved(int position){
        adapter.notifyItemRangeRemoved(position,getListSize());
    }
    //endregion



    //region 显示和关闭
    //显示
    public void showMusicList(){
        //isShowing = true;
        backgroundAlpha(0.6f);//设置遮罩层
        popupWindow.showAtLocation(activity.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }

    //endregion



    //设置播放模式
    public void setPlayMode(int mode){
        switch (mode){
            case ListOrder:
                ivPlayMode.setImageResource(R.drawable.img_mode_list_loop_black);
                tvPlayMode.setText("顺序播放");
                ToastUtils.Show(context,"顺序播放");
                break;
            case ListRandom:
                ivPlayMode.setImageResource(R.drawable.img_mode_random_black);
                tvPlayMode.setText("随机播放");
                ToastUtils.Show(context,"随机播放");
                break;
            case SingleLoop:
                ivPlayMode.setImageResource(R.drawable.img_mode_single_loop_black);
                tvPlayMode.setText("单曲循环");
                ToastUtils.Show(context,"单曲循环");
                break;
            default:
                setPlayMode(ListOrder);//默认设置为列表循环
                return;
        }
        this.PLAY_MODE = mode;
    }

    //获取播放模式
    public int getPlayMode(){
        return this.PLAY_MODE;
    }

    public int getListSize(){
        return adapter.getItemCount();
    }

    //清除列表的播放状态
    public void cleanPlayStatus(){
        if(lastPosition == -1) return;
        if (adapter.getItemCount() != 0) adapter.setPlayListStatus(lastPosition,false);
        lastPosition = -1;
    }

    //设置播放列表的某一项状态
    public void setPlayStatus(int position){
        adapter.setPlayListStatus(position,true);
        lastPosition = position;
    }


    //长按拖拽的实现类
    /*private ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            if(adapter.isMove){
                return makeMovementFlags(ItemTouchHelper.UP|ItemTouchHelper.DOWN,0);
            }
            return 0;
        }
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            //得到当拖拽的viewHolder的Position
            //拿到当前拖拽到的item的viewHolder
            adapter.swap(viewHolder.getAdapterPosition(),target.getAdapterPosition());
            return true;
        }
        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            //侧滑删除可以使用；
        }
        //长按选中Item的时候开始调用
        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                viewHolder.itemView.setBackgroundColor(Color_Touch);
                //获取系统震动服务//震动70毫秒
                Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
                if(vib != null)
                    vib.vibrate(70);

                setTouchMoveMode(true);
            }
            super.onSelectedChanged(viewHolder, actionState);
        }
        //手指松开的时候还原高亮
        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setBackgroundColor(0);
            //notifyDataSetChanged();  //完成拖动后刷新适配器，这样拖动后删除就不会错乱
        }
    });*/
}
