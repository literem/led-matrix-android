package com.literam.matrix.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.literam.matrix.R;
import com.literam.matrix.common.utils.ClipboardUtils;
import com.literam.matrix.common.utils.TimeUtils;
import com.literam.matrix.common.utils.ToastUtils;
import com.literam.matrix.dialog.PopupMessageMore;
import com.literam.matrix.entity.TerminalBean;

import java.util.ArrayList;
import java.util.List;

public class TerminalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int RECEIVE = 0;
    public static final int SEND = 1;

    private RecyclerView recyclerView;
    private List<TerminalBean> list;
    private Context context;

    private PopupMessageMore popupMessageMore;


    public TerminalAdapter(Context context){
        this.context = context;
        list = new ArrayList<>();
        popupMessageMore = new PopupMessageMore((Activity)context,onPopupMessageMoreListener);
    }

    public void initData(){
        for (int i = 0; i < 10; i++) {
            TerminalBean bean = new TerminalBean();
            bean.setTime("2022-11-10 13:59:0"+i);
            bean.setContent("收到很多数据现在是第"+i);
            bean.setType(i%2);
            list.add(bean);
        }

        for (int i = 10; i < 20; i++) {
            TerminalBean bean = new TerminalBean();
            bean.setTime("2022-11-10 13:59:"+i);
            bean.setContent("收到很多数据现在是第"+i);
            bean.setType(i%2);
            list.add(bean);
        }

        notifyDataSetChanged();
    }

    public void addAll(List<TerminalBean> list){
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public void addOne(int type,String content){
        TerminalBean bean = new TerminalBean();
        bean.setTime(TimeUtils.getNowDateTime());
        bean.setType(type);
        bean.setContent(content);
        list.add(bean);
        notifyItemInserted(list.size()-1);
    }

    public void clean(){
        this.list.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == RECEIVE){
            view = LayoutInflater.from(context).inflate(R.layout.item_terminal_left,parent,false);
            return new TerminalLeftViewHolder(view);
        }else{
            view = LayoutInflater.from(context).inflate(R.layout.item_terminal_right,parent,false);
            return new TerminalRightViewHolder(view);
        }
    }

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            int position = recyclerView.getChildAdapterPosition((View) view.getParent().getParent().getParent());
            popupMessageMore.showPopupAtViewTop(view,position);
            return false;
        }
    };

    private PopupMessageMore.OnPopupMessageMoreListener onPopupMessageMoreListener = new PopupMessageMore.OnPopupMessageMoreListener() {
        @Override
        public void onMoreClick(int type, int position) {
            switch (type){
                case PopupMessageMore.TYPE_COPY:
                    copyText(position);
                    break;
                case PopupMessageMore.TYPE_DEL:
                    list.remove(position);
                    notifyItemRemoved(position);
                    break;
                case PopupMessageMore.TYPE_RE:
                    break;
            }
        }
    };

    private void copyText(int pos){
        String text = list.get(pos).getContent();
        if (text == null || text.length() == 0){
            ToastUtils.Show(context,"没有要复制的文本！");
            return;
        }
        ClipboardUtils.copy(context,null,text);
        ToastUtils.Show(context,"已复制该文本");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TerminalBean bean = list.get(position);
        if(bean.getType() == SEND){
            setRightData((TerminalRightViewHolder) holder,bean);
        }else{
            setLeftData((TerminalLeftViewHolder) holder,bean);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getType();
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

    private void setLeftData(TerminalLeftViewHolder holder,TerminalBean bean){
        holder.tvContent.setText(bean.getContent());
        holder.tvTime.setText(bean.getTime());
    }

    private void setRightData(TerminalRightViewHolder holder,TerminalBean bean){
        holder.tvContent.setText(bean.getContent());
        holder.tvTime.setText(bean.getTime());
    }

    class TerminalLeftViewHolder extends RecyclerView.ViewHolder{
        private TextView tvTime;
        private TextView tvContent;

        TerminalLeftViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_item_terminal_time_left);
            tvContent = itemView.findViewById(R.id.tv_item_terminal_content_left);
            tvContent.setOnLongClickListener(onLongClickListener);
        }
    }

    class TerminalRightViewHolder extends RecyclerView.ViewHolder{
        private TextView tvTime;
        private TextView tvContent;

        TerminalRightViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_item_terminal_time_right);
            tvContent = itemView.findViewById(R.id.tv_item_terminal_content_right);
            tvContent.setOnLongClickListener(onLongClickListener);
        }
    }
}
