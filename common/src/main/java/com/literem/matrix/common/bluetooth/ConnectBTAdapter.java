package com.literem.matrix.common.bluetooth;

import android.bluetooth.BluetoothDevice;
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

import com.literem.matrix.common.R;
import com.literem.matrix.common.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ConnectBTAdapter extends RecyclerView.Adapter<ConnectBTAdapter.ConnectBluetoothHolder> {

    private Context context;
    private RecyclerView recyclerView;
    private List<ConnectBTBean> list;
    private View.OnClickListener listener;

    ConnectBTAdapter(Context context,View.OnClickListener onClickListener){
        this.context = context;
        this.listener = onClickListener;
        list = new ArrayList<>();
    }

    void setList(Set<BluetoothDevice> devices,String connectAddress){
        if(devices == null) return;

        //如果为空则说明没有设备连接
        if(connectAddress == null){
            for(BluetoothDevice bondDevice : devices) {
                ConnectBTBean bean = new ConnectBTBean();
                bean.setDeviceName(bondDevice.getName());
                bean.setDeviceMac(bondDevice.getAddress());
                bean.setConnect(false);
                list.add(bean);
            }
            notifyDataSetChanged();
            return;
        }

        for(BluetoothDevice bondDevice : devices) {
            String address = bondDevice.getAddress();
            ConnectBTBean bean = new ConnectBTBean();
            bean.setDeviceName(bondDevice.getName());
            bean.setDeviceMac(address);
            if(connectAddress.equals(address)){
                bean.setConnect(true);
            }else{
                bean.setConnect(false);
            }
            list.add(bean);
        }
        notifyDataSetChanged();
    }

    void setItemStatus(String address,boolean status){
        if(address == null){//如果为空，则全部重置为false
            for (ConnectBTBean bean : list){
                bean.setConnect(false);
            }
        }else{
            for (ConnectBTBean bean : list){
                if(address.equals(bean.getDeviceMac())){
                    bean.setConnect(status);
                }
            }
        }
        notifyDataSetChanged();
    }

    void cleanList(){
        this.list.clear();
        notifyDataSetChanged();
    }

    public ConnectBTBean getBean(View v){
        int position = recyclerView.getChildAdapterPosition(v);
        return list.get(position);
    }

    @NonNull
    @Override
    public ConnectBluetoothHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_connect_bluetooth,parent,false);
        view.findViewById(R.id.ll_root).setOnClickListener(listener);
        return new ConnectBluetoothHolder(view);
    }




    @Override
    public void onBindViewHolder(@NonNull ConnectBluetoothHolder holder, int position) {
        ConnectBTBean bean = list.get(position);
        holder.tvName.setText(bean.getDeviceName());
        holder.tvMac.setText(bean.getDeviceMac());
        if(bean.isConnect()){
            holder.tvName.setTextColor(Color.WHITE);
            holder.tvMac.setTextColor(Color.WHITE);
            holder.ivBluetooth.setImageResource(R.drawable.img_bluetooth_white);
            holder.ivArrow.setImageResource(R.drawable.img_arrow_right_white);
            holder.llRoot.setBackgroundResource(R.drawable.bg_circle_15_blue);
        }else{
            holder.tvName.setTextColor(ColorUtils.COLOR_505050);
            holder.tvMac.setTextColor(ColorUtils.COLOR_808080);
            holder.ivBluetooth.setImageResource(R.drawable.img_bluetooth_grey);
            holder.ivArrow.setImageResource(R.drawable.img_arrow_right_grey);
            holder.llRoot.setBackgroundResource(R.drawable.bg_circle_15_grey);
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

    class ConnectBluetoothHolder extends RecyclerView.ViewHolder{
        private TextView tvName,tvMac;
        private ImageView ivBluetooth,ivArrow;
        private LinearLayout llRoot;
        ConnectBluetoothHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_device_name);
            tvMac = itemView.findViewById(R.id.tv_device_mac);
            ivBluetooth = itemView.findViewById(R.id.iv_bluetooth_icon);
            ivArrow = itemView.findViewById(R.id.iv_arrow_right);
            llRoot = itemView.findViewById(R.id.ll_root);
        }
    }
}
