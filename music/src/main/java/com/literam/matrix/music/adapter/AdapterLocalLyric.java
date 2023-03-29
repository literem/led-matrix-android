package com.literam.matrix.music.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.literam.matrix.music.R;
import com.literam.matrix.music.callback.OnValueChangeListener;
import com.literam.matrix.music.entity.Lrc;

import java.util.ArrayList;
import java.util.List;

public class AdapterLocalLyric extends RecyclerView.Adapter<AdapterLocalLyric.LyricViewHolder>{

    private Context context;
    private List<Lrc> list;
    private RecyclerView recyclerView;

    private int colorCheck, colorNotCheck;
    private Drawable drawableCheck, drawableNotCheck;
    private String strCheck, strNotCheck;

    private int lastAdded = -1;//上一个添加的项
    private OnValueChangeListener listener;

    public AdapterLocalLyric(Context context){
        this.context = context;
        this.list = new ArrayList<>();

        colorCheck = Color.parseColor("#AAAAAA");
        colorNotCheck = Color.parseColor("#505050");
        drawableCheck = context.getDrawable(R.drawable.bg_circle_10_grey);
        drawableNotCheck = context.getDrawable(R.drawable.bg_circle_10_blue);
        strCheck = "已选择";
        strNotCheck = "选择";
    }

    public void setButtonStateText(String check, String uncheck){
        this.strCheck = check;
        this.strNotCheck = uncheck;
    }

    /**
     * 设置item右边的按钮的点击回调
     * @param listener 点击事件
     */
    public void setOnButtonClickListener(OnValueChangeListener listener){
        this.listener = listener;
    }

    /**
     * 把歌词添加到list中
     * @param list list歌词
     */
    public void addList(List<Lrc> list){
        if(list == null) return;
        this.list.addAll(list);
    }

    /**
     * 通过position获取Lyric
     * @param position 索引
     * @return 返回：Lyric对象
     */
    public Lrc getItem(int position){
        return list.get(position);
    }

    /**
     * 设置某项为选中状态
     * @param position 索引
     */
    public void setItemAdded(int position, boolean isAdded){
        list.get(position).setCheck(isAdded);
        notifyItemChanged(position);
    }

    /**
     * 设置当前项为添加状态，并取消上一项
     * @param position 索引
     */
    public void setCurrentItemAdded(int position){
        if(lastAdded != -1){//如果有选中上一项，先把上一项取消
            list.get(lastAdded).setCheck(false);
            notifyItemChanged(lastAdded);
        }
        lastAdded = position;
        list.get(position).setCheck(true);
        notifyItemChanged(position);
    }

    public Lrc getCurrentItemAdded(){
        if(lastAdded == -1) return null;
        return list.get(lastAdded);
    }

    @NonNull
    @Override
    public LyricViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lyric,parent,false);
        view.findViewById(R.id.btn_add).setOnClickListener(onClickListener);
        return new LyricViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LyricViewHolder holder, int position) {
        Lrc bean = list.get(position);
        if(bean.isCheck()){
            holder.tvName.setTextColor(colorCheck);
            holder.btnAdd.setBackground(drawableCheck);
            holder.btnAdd.setText(strCheck);
        }else{
            holder.tvName.setTextColor(colorNotCheck);
            holder.btnAdd.setBackground(drawableNotCheck);
            holder.btnAdd.setText(strNotCheck);
        }
        holder.tvName.setText(bean.getName());
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int position = recyclerView.getChildAdapterPosition((View) view.getParent());


            if(listener != null)
                listener.onValueChange(position);
        }
    };

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

    class LyricViewHolder extends RecyclerView.ViewHolder{
        TextView tvName;
        Button btnAdd;
        LyricViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            btnAdd = itemView.findViewById(R.id.btn_add);
        }
    }


}
