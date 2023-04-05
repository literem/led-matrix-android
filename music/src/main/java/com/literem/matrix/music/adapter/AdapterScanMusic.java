package com.literem.matrix.music.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.music.R;
import com.literem.matrix.music.entity.LocalMusicBean;

import java.util.ArrayList;
import java.util.List;

public class AdapterScanMusic extends RecyclerView.Adapter<AdapterScanMusic.MyViewHolder> {

    private RecyclerView recyclerView;
    private Context context;
    private List<LocalMusicBean> list;
    private int checkCount = 0;

    private int colorCheck,colorUncheck;


    public AdapterScanMusic(Context context){
        this.context = context;
        this.list = new ArrayList<>();

        colorCheck = Color.WHITE;
        colorUncheck = Color.parseColor("#EFEFEF");
    }


    public void setList(List<LocalMusicBean> list){
        this.list.clear();
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    //设置选中或不选中全部
    public void setCheck(boolean isCheck){
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setCheck(isCheck);
        }
        notifyDataSetChanged();
    }

    public int getCheckCount(){
        return this.checkCount;
    }

    public List<LocalMusicBean> getCheckList(){
        List<LocalMusicBean> listMusic = new ArrayList<>();
        for (int i = 0; i < this.list.size(); i++) {
            LocalMusicBean musicBean = this.list.get(i);
            if (musicBean.isCheck()){
                listMusic.add(musicBean);
            }
        }
        return listMusic;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_local_music,parent,false);
        view.findViewById(R.id.ll_root).setOnClickListener(onClickListener);
        return new MyViewHolder(view);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = recyclerView.getChildAdapterPosition(view);
            boolean status = !list.get(position).isCheck();
            list.get(position).setCheck(status);
            notifyItemChanged(position);
            checkCount = status ? checkCount+1 : checkCount-1;
            if(listener != null) listener.onChange(checkCount);
        }
    };

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final LocalMusicBean music = list.get(position);
        holder.tvName.setText(music.getTitle());
        holder.tvSinger.setText(music.getSigner());


        /*//如果设置了全选
        if(isCheckAll) {
            holder.checkBox.setChecked(true);
            holder.llRoot.setBackgroundColor(colorCheck);
            if((position+1) == getItemCount()) isCheckAll = false;
            return;
        }

        //如果设置全不选
        if(isUncheckAll){
            holder.checkBox.setChecked(false);
            holder.llRoot.setBackgroundColor(colorUncheck);
            if((position+1) == getItemCount()) isUncheckAll = false;
            return;
        }*/

        if (music.isCheck()) {
            holder.llRoot.setBackgroundColor(colorCheck);
            holder.checkBox.setChecked(true);
        }else{
            holder.llRoot.setBackgroundColor(colorUncheck);
            holder.checkBox.setChecked(false);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
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
        TextView tvName,tvSinger;
        CheckBox checkBox;
        FrameLayout llRoot;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.item_name);
            tvSinger = itemView.findViewById(R.id.item_signer);
            checkBox = itemView.findViewById(R.id.item_check_box);
            llRoot = itemView.findViewById(R.id.ll_root);
        }
    }

    public void setOnChangeListener(OnCheckBoxChange listener){
        this.listener = listener;
    }

    private OnCheckBoxChange listener;
    public interface OnCheckBoxChange{
        void onChange(int count);
    }

}
