package com.literem.matrix.music.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.common.widget.DrawableTextView;
import com.literem.matrix.music.R;
import com.literem.matrix.music.entity.Music;
import com.literem.matrix.music.utils.MusicListData;

public class AdapterMusicList extends RecyclerView.Adapter<AdapterMusicList.MyViewHolder> {

    private RecyclerView recyclerView;
    private Context context;
    private MusicListData musicListData;
    private int CheckColor,UncheckColor;
    private int lastItemCheck = -1;
    private int expendPos = -1;

    public AdapterMusicList(Context context, OnMusicListItemListener listener, MusicListData musicListData){
        this.context = context;
        this.listener = listener;
        this.musicListData = musicListData;
        CheckColor = Color.parseColor("#EEEEEE");
        UncheckColor = Color.WHITE;
        musicListData.initMusicList();
    }

    //更新音乐列表
    public void initMusicList(){
        musicListData.initMusicList();
        notifyDataSetChanged();
    }


    //通过音乐id查询音乐在列表中的索引
    public void queryPosAndCheck(int musicId){
        int pos = musicListData.queryPosByMusicId(musicId);
        if(pos != -1) checkItem(pos);
    }

    //移除一项
    public void removeItem(int position){
        if (position == expendPos) expendPos = -1;
        if (position == lastItemCheck) lastItemCheck = -1;
        musicListData.remove(position);
        notifyItemRemoved(position);
    }

    //清空音乐列表
    public void clearAll(){
        lastItemCheck = -1;
        expendPos = -1;
        musicListData.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdapterMusicList.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music_list,parent,false);
        view.findViewById(R.id.iv_add).setOnClickListener(onClickListener);
        view.findViewById(R.id.iv_more).setOnClickListener(onClickListener);
        view.findViewById(R.id.ll_item_root).setOnClickListener(onClickListener);
        view.findViewById(R.id.fl_add).setOnClickListener(onClickListener);
        view.findViewById(R.id.fl_delete).setOnClickListener(onClickListener);
        view.findViewById(R.id.fl_info).setOnClickListener(onClickListener);
        view.setOnClickListener(onClickListener);
        return new AdapterMusicList.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterMusicList.MyViewHolder holder, int position) {
        Music music = musicListData.get(position);
        holder.tvName.setText(music.getTitle());
        holder.tvSinger.setText(music.getSinger());
        if (music.isPlay()) {
            holder.llItem.setBackgroundColor(CheckColor);
            holder.ivAdd.setImageResource(R.drawable.img_playing);
        }else{
            holder.llItem.setBackgroundColor(UncheckColor);
            holder.ivAdd.setImageResource(R.drawable.img_add_music);
        }

        if(music.isExpend()){
            holder.llMore.setVisibility(View.VISIBLE);
        }else{
            holder.llMore.setVisibility(View.GONE);
        }
    }

    //选中项
    private void checkItem(int position){
        unCheckItem();
        musicListData.setPlay(position,true);
        lastItemCheck = position;
        notifyItemChanged(position);
    }

    //取消选中上一项
    public void unCheckItem(){
        if(lastItemCheck == -1 || musicListData.getSize() == 0) return;
        musicListData.setPlay(lastItemCheck,false);
        notifyItemChanged(lastItemCheck);
    }

    //设置展开
    private void setExpend(int position){
        if(expendPos != -1) {//如果上一项还展开，则先隐藏
            musicListData.setExpend(expendPos,false);
            notifyItemChanged(expendPos);
        }
        musicListData.setExpend(position,true);
        notifyItemChanged(position);
        expendPos = position;
    }

    //清除
    private void cleanExpend(int position){
        musicListData.setExpend(position,false);
        notifyItemChanged(position);
        expendPos = -1;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int pos;
            int id = view.getId();
            if (id == R.id.fl_add || id == R.id.iv_add) {
                pos = recyclerView.getChildAdapterPosition((View) view.getParent().getParent());
                if (listener != null) listener.onItemAdd(pos, musicListData.get(pos));
            } else if (id == R.id.ll_item_root) {
                pos = recyclerView.getChildAdapterPosition((View) view.getParent());
                if (listener != null) listener.onItemClick(pos, musicListData.get(pos));
            } else if (id == R.id.iv_more) {
                pos = recyclerView.getChildAdapterPosition((View) view.getParent().getParent());
                if (expendPos == pos) {//相等，说明要关闭
                    cleanExpend(pos);
                } else {
                    setExpend(pos);
                }
            } else if (id == R.id.fl_delete) {
                pos = recyclerView.getChildAdapterPosition((View) view.getParent().getParent());
                if (listener != null) listener.onItemDelete(pos, musicListData.getMusicId(pos));
            } else if (id == R.id.fl_info) {
                pos = recyclerView.getChildAdapterPosition((View) view.getParent().getParent());
                if (listener != null) listener.onItemMore(musicListData.get(pos));
            }

            /*if(view.getId() == R.id.iv_add){

            }else if(view.getId() == R.id.iv_more){
                pos = recyclerView.getChildAdapterPosition((View) view.getParent());
                if(listener != null) listener.onItemMore(musicListData.get(pos));
            }else if(view.getId() == R.id.ll_item_root){

            }*/
        }
    };

    @Override
    public int getItemCount() {
        return musicListData.getSize();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView){
        super.onDetachedFromRecyclerView(recyclerView);
        this.recyclerView = null;
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView tvName;
        DrawableTextView tvSinger;
        ImageView ivAdd,ivMore;
        //CheckBox cb;
        LinearLayout llItem,llMore;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.item_name);
            tvSinger = itemView.findViewById(R.id.item_signer);
            ivAdd = itemView.findViewById(R.id.iv_add);
            ivMore = itemView.findViewById(R.id.iv_more);
            //cb = itemView.findViewById(R.id.item_check_box);
            llItem = itemView.findViewById(R.id.ll_item_root);
            llMore = itemView.findViewById(R.id.ll_item_more);
        }
    }

    public OnMusicListItemListener listener;
    public interface OnMusicListItemListener{
        void onItemAdd(int position, Music music);
        void onItemClick(int position, Music music);
        void onItemMore(Music music);
        void onItemDelete(int position, int musicId);
    }
}
