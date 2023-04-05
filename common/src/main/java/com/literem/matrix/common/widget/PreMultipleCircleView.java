package com.literem.matrix.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * author : literem
 * time   : 2021
 * desc   : 用于16*16个点预览的view
 * version: 1.0
 */
public class PreMultipleCircleView extends View{

    private boolean[][] data;
    private int colorCheck, colorUncheck;
    private boolean isSetDate = false;

    private Paint paint;

    private float radius;
    private int padding = 5;//距离上下左右的位置
    private int spacing = 2;//间距
    private float space;//每一个点的实际间隔
    private float startLocal;//开始位置

    public PreMultipleCircleView(Context context) {
        this(context,null);
    }

    public PreMultipleCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PreMultipleCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        paint = new Paint();
        colorCheck = Color.parseColor("#EE0000");
        colorUncheck = Color.parseColor("#CCCCCC");
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = getWidth();

        width = width - spacing*33 - padding*2;
        radius = width / 32.0f;
        //paint.setStrokeWidth(radius*2);
        space = spacing*2 + radius * 2;
        startLocal = spacing + padding + radius;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //width = width + padding*2;
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY);
        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }


    public void setData(boolean[][] d){
        this.data = d;
        isSetDate = true;
        invalidate();
    }

    public boolean[][] getData(){
        return this.data;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isSetDate) return;
        if(data == null) return;

        float x,y = startLocal;
        for (int i = 0; i < 16; i++,y+=space) {
            x = startLocal;
            for (int j = 0; j < 16; j++,x+=space) {
                paint.setColor(data[i][j] ? colorCheck : colorUncheck);
                canvas.drawCircle(x,y,radius,paint);
            }
        }
    }
}
