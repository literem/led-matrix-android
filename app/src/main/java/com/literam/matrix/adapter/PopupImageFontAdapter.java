package com.literam.matrix.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.literam.matrix.R;
import com.literam.matrix.entity.ImageBean;

import java.util.ArrayList;
import java.util.List;

public class PopupImageFontAdapter extends RecyclerView.Adapter<PopupImageFontAdapter.ImageViewHolder>{

    private RecyclerView recyclerView;
    private List<ImageBean> list;
    private LayoutInflater inflater;
    private Drawable checkDrawable,uncheckDrawable,addDrawable;
    private int checkColor,uncheckColor;
    private Activity activity;
    private int currentPos=-1;

    public PopupImageFontAdapter(Context context){
        inflater = LayoutInflater.from(context);
        list = new ArrayList<>();
        activity = (Activity) context;
        checkDrawable = context.getDrawable(R.drawable.bg_circle_5_blue);
        uncheckDrawable = context.getDrawable(R.drawable.bg_circle_5_white);
        addDrawable = context.getDrawable(R.drawable.bg_circle_5_red);
        uncheckColor = Color.parseColor("#606060");
        checkColor = Color.WHITE;

    }

    public void addImage(ImageBean bean){
        list.add(bean);
        notifyItemInserted(list.size());
    }

    public Uri getCurrentImageUri(){
        if(currentPos == -1) return null;
        return list.get(currentPos).getUri();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_image_font,parent,false);
        view.setOnClickListener(onClickListener);
        return new PopupImageFontAdapter.ImageViewHolder(view);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = recyclerView.getChildAdapterPosition(view);
            if(position == 0){
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                activity.startActivityForResult(intent, 2);
                return;
            }
            if(position == currentPos) return;
            if(currentPos != -1){
                list.get(currentPos).setCheck(false);
                notifyItemChanged(currentPos);
            }
            list.get(position).setCheck(true);
            currentPos = position;
            notifyItemChanged(position);
        }
    };

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageBean bean = list.get(position);
        holder.img.setImageURI(bean.getUri());
        if(bean.isFirst()){
            holder.tvTitle.setBackground(addDrawable);
            holder.tvTitle.setText("添加");
            return;
        }
        if(bean.isCheck()){
            holder.tvTitle.setBackground(checkDrawable);
            holder.tvTitle.setTextColor(checkColor);
        }else{
            holder.tvTitle.setBackground(uncheckDrawable);
            holder.tvTitle.setTextColor(uncheckColor);
        }
        holder.tvTitle.setText(String.valueOf(bean.getId()));
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

    class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView img;
        TextView tvTitle;
        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.iv_img);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }
}