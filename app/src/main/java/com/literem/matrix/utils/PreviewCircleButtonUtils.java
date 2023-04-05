package com.literem.matrix.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.Button;

import com.literem.matrix.R;

public class PreviewCircleButtonUtils {
    private Drawable drawableCheck,drawableUncheck;
    private int checkColor,uncheckColor;
    private String strCheckText,strUncheckText;

    public PreviewCircleButtonUtils(Context context){
        drawableCheck = context.getDrawable(R.drawable.bg_circle_5_blue);
        drawableUncheck = context.getDrawable(R.drawable.bg_circle_5_grey);
        checkColor = Color.WHITE;
        uncheckColor = Color.parseColor("#505050");
        strUncheckText = "模块%1$d";
        strCheckText = "编辑中";
    }

    public void setTextFormat(String format){
        this.strUncheckText = format;
    }

    public void setButtonCheck(Button btn){
        btn.setText(strCheckText);
        btn.setBackground(drawableCheck);
        btn.setTextColor(checkColor);
    }

    public void setButtonUncheck(Button btn,int tag){
        btn.setText(String.format(strUncheckText,tag+1));
        btn.setTextColor(uncheckColor);
        btn.setBackground(drawableUncheck);
    }

    void setButtonDisable(Button btn){
        btn.setText("动态帧");
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundResource(R.drawable.bg_circle_5_red);
    }

    public void setButtonText(Button btn,int tag){
        btn.setText(String.format(strUncheckText,tag+1));
    }

    public String getModuleText(int tag){
        return String.format(strUncheckText,tag+1);
    }

}
