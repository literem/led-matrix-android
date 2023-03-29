package com.literam.matrix.common.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class BaseRecycleViewAdapter<T extends RecyclerView.ViewHolder,E> extends RecyclerView.Adapter<T> {

    private Context context;
    private RecyclerView recyclerView;
    private LayoutInflater inflater;
    private View.OnClickListener onClickListener;
    protected List<E> list;
    private int itemId;

    public BaseRecycleViewAdapter(Context context,int itemId,View.OnClickListener onClickListener){
        this.context = context;
        this.itemId = itemId;
        this.onClickListener = onClickListener;
        inflater = LayoutInflater.from(context);
    }

    public BaseRecycleViewAdapter(Context context,int itemId){
        this.context = context;
        this.itemId = itemId;
        inflater = LayoutInflater.from(context);
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onViewClick(v);
            }
        };
    }

    protected abstract T createItemView(View view);
    protected abstract void bindViewHolder(@NonNull T holder,E bean);
    protected void onViewClick(View view){}

    public int getItemPosition(ViewParent viewParent){
        return recyclerView.getChildAdapterPosition((View) viewParent);
    }

    protected void addOnClickListener(View view){
        if(onClickListener != null) view.setOnClickListener(onClickListener);
    }

    @NonNull
    @Override
    public T onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(itemId,parent,false);
        return createItemView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull T holder, int position) {
        bindViewHolder(holder,list.get(position));
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

}
