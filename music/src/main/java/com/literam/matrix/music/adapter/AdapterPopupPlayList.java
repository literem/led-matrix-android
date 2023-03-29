package com.literam.matrix.music.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.literam.matrix.music.R;
import com.literam.matrix.music.callback.OnPlayListListener;
import com.literam.matrix.music.entity.PlayList;
import com.literam.matrix.music.utils.PlayListData;

public class AdapterPopupPlayList extends RecyclerView.Adapter<AdapterPopupPlayList.MusicListHolder> {

    private LayoutInflater inflater;
    private RecyclerView recyclerView;

    private int Color_Check;
    private int Color_Title;
    private int Color_Signed;

    private PlayListData playListData;
    private OnPlayListListener listener;

    public AdapterPopupPlayList(Context context,PlayListData playListData,OnPlayListListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        this.playListData = playListData;
        Color_Check = Color.parseColor("#d81e06");
        Color_Title = Color.parseColor("#404040");
        Color_Signed = Color.parseColor("#666666");
    }

    //设置该项为红色高亮或正常颜色，true：红色高亮，false：正常颜色
    public void setPlayListStatus(int position,boolean state){
        playListData.setPlayStatusByPos(position,state);
        notifyItemChanged(position);
    }

    public void addOne(PlayList playList){
        int pos = playListData.getSize();
        playList.setNumber(pos+1);
        playListData.addOne(playList);
        notifyItemInserted(pos);
    }

    /* 长按触发移动相关
    //移动某个item后交换List的位置
    public boolean isMove = false;
    private final int IMG_MOVE = 1;
    public final int IMG_DEL = 2;
    private int ImgMode = IMG_DEL;
    public void swap(int fromPosition,int toPosition){
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                playListData.swap(i,i+1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                playListData.swap(i,i-1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    //设置右边的图标 移动标志 和 删除图标
    public void setItemRightImg(int mode){
        this.ImgMode = mode;
        notifyDataSetChanged();
    }

    //长按触发排序
    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            if(!isMove){
                isMove = true;
                setItemRightImg(IMG_MOVE);
            }
            return true;
        }
    };*/

    @NonNull
    @Override
    public MusicListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_popup_play_list,parent,false);
        view.setOnClickListener(onClickListener);
        //view.setOnLongClickListener(onLongClickListener);
        view.findViewById(R.id.item_music_list_delete).setOnClickListener(onClickListener);
        return new MusicListHolder(view);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(listener == null) return;
            if(view.getId() == R.id.item_music_list_delete){//点击删除
                int position = recyclerView.getChildAdapterPosition((View) view.getParent().getParent());
                listener.onDeleteClick(position);
            }else{//点击item
                listener.onItemClick(recyclerView.getChildAdapterPosition(view));
            }
        }
    };


    @Override
    public void onBindViewHolder(@NonNull MusicListHolder holder, int position) {
        /*if(ImgMode == IMG_DEL){
            holder.iv_delete.setImageResource(R.drawable.img_delete);
        }else if(ImgMode == IMG_MOVE){
            holder.iv_delete.setImageResource(R.drawable.img_move_flag);
        }*/
        PlayList bean = playListData.get(position);
        if(bean.isPlay()) {
            holder.tv_number.setVisibility(View.GONE);
            holder.iv_is_play.setVisibility(View.VISIBLE);
            holder.tv_name.setTextColor(Color_Check);
            holder.tv_signer.setTextColor(Color_Check);
        }else{
            holder.tv_number.setVisibility(View.VISIBLE);
            holder.tv_number.setText(String.valueOf(bean.getNumber()));
            holder.iv_is_play.setVisibility(View.GONE);
            holder.tv_name.setTextColor(Color_Title);
            holder.tv_signer.setTextColor(Color_Signed);
        }
        holder.tv_name.setText(bean.getTitle());
        holder.tv_signer.setText(bean.getSinger());
        holder.iv_delete.setImageResource(R.drawable.img_delete);
    }

    @Override
    public int getItemCount() {
        return playListData.getSize();
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

    class MusicListHolder extends RecyclerView.ViewHolder{
        TextView tv_name,tv_signer,tv_number;
        ImageView iv_is_play,iv_delete;
        MusicListHolder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.item_music_list_name);
            tv_signer = itemView.findViewById(R.id.item_music_list_signer);
            tv_number = itemView.findViewById(R.id.item_music_list_number);
            iv_is_play = itemView.findViewById(R.id.item_music_list_play);
            iv_delete = itemView.findViewById(R.id.item_music_list_delete);
        }
    }
}
