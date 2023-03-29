package com.literam.matrix.common.dialog;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.literam.matrix.common.R;
import com.literam.matrix.common.base.BaseDialog;
import com.literam.matrix.common.utils.DisplayUtil;

/**
 * author : literem
 * time   : 2022/12/18
 * desc   :
 * version: 1.0
 */
public class DialogSelectAnim extends BaseDialog {


    /*
    *
    *   #define anim_scroll_bottom  0xE0 //进场动画独有，从底部往下翻页
        #define anim_scroll_top		0xE1 //进场动画独有，从顶部往上翻页
        #define anim_left_right		0xE2 //进入、退出动画共有，从左往右显示
        #define anim_middle_open	0xE3 //进入、退出动画共有，从中间展开显示
        #define anim_both_close     0xE4 //进入、退出动画共有，从两边收缩显示
        #define anim_bottom_top		0xE5 //进入、退出动画共有，从底部往上显示
        #define anim_top_bottom		0xE6 //进入、退出动画共有，从顶部往下显示
        #define anim_right_left		0xE7 //退场动画独有
    * */

    private OnSelectAnimCallback callback;
    private LinearLayout llRootIn,llRootOut;
    private int checkColor,unCheckColor;
    private int checkBgColor,unCheckBgColor;
    private TextView lastInView = null;
    private TextView lastOutView = null;
    private int inAnim=-1,outAnim=-1;

    public DialogSelectAnim(@NonNull Context context,OnSelectAnimCallback callback) {
        super(context);
        this.callback = callback;
    }

    private TextView createView(String str,int tag){
        TextView tv = new TextView(context);
        tv.setPadding(10,0,10,0);
        tv.setGravity(Gravity.CENTER);
        tv.setText(str);
        tv.setTextColor(unCheckColor);
        tv.setBackgroundColor(unCheckBgColor);
        tv.setTextSize(13);
        tv.setTag(tag);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        tv.setMaxLines(1);
        tv.setOnClickListener(onClickListener);
        return tv;
    }

    private void checkInAnim(TextView tv,int tag){
        this.inAnim = tag;
        if(lastInView != null){
            lastInView.setBackgroundColor(unCheckBgColor);
            lastInView.setTextColor(unCheckColor);
        }
        tv.setBackgroundColor(checkBgColor);
        tv.setTextColor(checkColor);
        lastInView = tv;
    }

    private void checkOutAnim(TextView tv,int tag){
        this.outAnim = tag;
        if(lastOutView != null){
            lastOutView.setBackgroundColor(unCheckBgColor);
            lastOutView.setTextColor(unCheckColor);
        }
        tv.setBackgroundColor(checkBgColor);
        tv.setTextColor(checkColor);
        lastOutView = tv;
    }

    @Override
    protected int setLayoutId() {
        return R.layout.dialog_select_anim;
    }

    @Override
    protected void initView(View view) {
        addOnClickListener(view,R.id.btn_sure);
        llRootIn = view.findViewById(R.id.ll_root_in);
        llRootOut = view.findViewById(R.id.ll_root_out);
        setCanceledOnTouchOutside(false);

        String[] inAnimContent = {
                "从左往右显示",
                "从中间扩张显示",
                "从两边收缩显示",
                "从底部往上显示",
                "从顶部往下显示"
        };

        String[] outAnimContent = {
                "从左往右消失",
                "从中间扩张消失",
                "从两边收缩消失",
                "从底部往上消失",
                "从顶部往下消失",
                "从右往左消失"
        };

        checkColor = Color.WHITE;
        unCheckColor = Color.parseColor("#505050");
        checkBgColor = Color.parseColor("#1296db");
        unCheckBgColor = Color.parseColor("#EFEFEF");

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtil.dip2px(context,30));
        int tag = 2;
        for (String str : inAnimContent){
            llRootIn.addView(createView(str,tag++),layoutParams);
        }
        tag = 102;
        for (String str : outAnimContent){
            llRootOut.addView(createView(str,tag++),layoutParams);
        }
    }

    @Override
    public void onViewClick(View v) {
        if(v.getId() == R.id.btn_sure){
            dismiss();
            return;
        }
        int tag = (int) v.getTag();
        if(tag < 100){
            if (tag == inAnim) return;
            checkInAnim((TextView) v,tag + 0xE0);
            callback.selectAnim(true,inAnim);
        }else{
            if (tag == outAnim) return;
            checkOutAnim((TextView) v,tag + 124);
            callback.selectAnim(false,outAnim);
        }
    }

    public void showDialog(int inAnim,int outAnim){
        this.inAnim = inAnim;
        this.outAnim = outAnim;
        if(inAnim != -1){
            inAnim -= 0xE0;
            for (int i = 0; i < llRootIn.getChildCount(); i++) {
                View childAt = llRootIn.getChildAt(i);
                if(childAt.getTag().equals(inAnim)){
                    checkInAnim((TextView) childAt,this.inAnim);
                    break;
                }
            }
        }
        if(outAnim != -1){
            outAnim -= 124;
            for (int i = 0; i < llRootOut.getChildCount(); i++) {
                View childAt = llRootOut.getChildAt(i);
                if(childAt.getTag().equals(outAnim)){
                    checkOutAnim((TextView) childAt,this.outAnim);
                    break;
                }
            }
        }
        show();
    }


    public interface OnSelectAnimCallback{
        void selectAnim(boolean isInAnim,int animValue);
    }
}
