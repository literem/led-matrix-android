package com.literem.matrix.music.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.common.base.BaseRecycleViewAdapter;
import com.literem.matrix.music.R;
import com.literem.matrix.music.entity.Music;

import java.util.List;

/**
 * author : literem
 * time   : 2023/02/10
 * desc   :
 * version: 1.0
 */
public class AdapterPopupChooseMusic extends BaseRecycleViewAdapter<AdapterPopupChooseMusic.Holder, Music> {

    public AdapterPopupChooseMusic(Context context,View.OnClickListener onClickListener) {
        super(context,R.layout.item_choose_music,onClickListener);
    }

    public Music getMusic(int position){
        return list.get(position);
    }

    public void setList(List<Music> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @Override
    protected Holder createItemView(View view) {
        addOnClickListener(view);
        return new Holder(view);
    }

    @Override
    protected void bindViewHolder(@NonNull Holder holder, Music bean) {
        holder.tvName.setText(bean.getTitle());
        holder.tvSigner.setText(bean.getSinger());
    }

    static class Holder extends RecyclerView.ViewHolder{
        TextView tvName, tvSigner;
        Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.item_choose_music_name);
            tvSigner = itemView.findViewById(R.id.item_choose_music_signer);
        }
    }
}
