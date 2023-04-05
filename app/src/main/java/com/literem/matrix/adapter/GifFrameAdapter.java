package com.literem.matrix.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.literem.matrix.R;
import com.literem.matrix.entity.GifFrameBean;

import java.util.Vector;

public class GifFrameAdapter extends RecyclerView.Adapter<GifFrameAdapter.GifFrameViewHolder>{

    private RecyclerView recyclerView;
    private Context context;
    private Vector<GifFrameBean> list;
    private String strFrame;

    private Drawable selectDrawable,unSelectDrawable;
    private int selectColor,unSelectColor;

    public GifFrameAdapter(Context context) {
        this.context = context;
        strFrame = "第%1$d帧";
        unSelectDrawable = context.getDrawable(R.drawable.bg_circle_5_grey);
        selectDrawable = context.getDrawable(R.drawable.bg_circle_5_blue);
        selectColor = Color.parseColor("#FFFFFF");
        unSelectColor = Color.parseColor("#606060");
    }

    public void setList(Vector<GifFrameBean> list){
        this.list = list;
        notifyDataSetChanged();
    }

    public void isSelectAll(boolean isAll){
        if(list == null) return;
        for (GifFrameBean bean : list){
            bean.setCheck(isAll);
        }
        notifyDataSetChanged();
    }

    public void invertSelect(){
        if(list == null) return;
        for (GifFrameBean bean : list){
            bean.setCheck(!bean.isCheck());
        }
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public GifFrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gif_preview,null);
        view.setOnClickListener(onClickListener);
        return new GifFrameViewHolder(view);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int position = recyclerView.getChildAdapterPosition(v);
            final GifFrameBean bean = list.get(position);
            bean.setCheck(!bean.isCheck());
            notifyItemChanged(position);
            //ToastUtils.Show(context,"点击了："+position);
        }
    };

    @Override
    public void onBindViewHolder(@NonNull GifFrameViewHolder holder, int position) {
        final GifFrameBean bean = list.get(position);
        holder.image.setImageBitmap(bean.getImage());
        holder.textView.setText(String.format(strFrame,bean.getIndex()));
        if(bean.isCheck()){
            holder.textView.setBackground(selectDrawable);
            holder.textView.setTextColor(selectColor);
        }else{
            holder.textView.setBackground(unSelectDrawable);
            holder.textView.setTextColor(unSelectColor);
        }
    }

    @Override
    public int getItemCount() {
        return list==null ? 0 : list.size();
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

    class GifFrameViewHolder extends RecyclerView.ViewHolder{
        private ImageView image;
        private TextView textView;

        GifFrameViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_image);
            textView = itemView.findViewById(R.id.item_text_view);

        }
    }

}
