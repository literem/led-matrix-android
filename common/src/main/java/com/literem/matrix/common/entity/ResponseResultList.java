package com.literem.matrix.common.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * author : literem
 * time   : 2023/01/30
 * desc   :
 * version: 1.0
 */
public class ResponseResultList {

    private List<ResponseResultBean> list;

    public ResponseResultList(int size){
        list = new ArrayList<>(size);
    }

    public ResponseResultList(){
        list = new ArrayList<>();
    }

    public void add(ResponseResultBean bean){
        this.list.add(bean);
    }

    public void add(int flag,int data){
        ResponseResultBean bean = new ResponseResultBean();
        bean.setFlag(flag);
        bean.setData(data);
        this.list.add(bean);
    }

    public int getValueByFlag(int flag){
        for(ResponseResultBean bean : list){
            if (bean.getFlag() == flag){
                return bean.getData();
            }
        }
        return 0;
    }

}
