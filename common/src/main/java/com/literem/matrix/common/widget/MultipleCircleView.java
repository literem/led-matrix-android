package com.literem.matrix.common.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * author : literem
 * time   : 2021
 * desc   : 具有16*16个圆点并且可以点击的View
 * version: 1.0
 */
public class MultipleCircleView extends View {

    private boolean[][] data;
    private int colorCheck, colorUncheck;
    private boolean isNotEdit = false;
    private boolean isPen = true;

    private Paint paint;

    private float radius;
    private int padding = 10;//距离上下左右的位置
    private int spacing = 5;//间距
    private float space;//每一个点的实际间隔
    private float startLocal;//开始位置

    public MultipleCircleView(Context context) {
        this(context,null);
    }

    public MultipleCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MultipleCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setPen(boolean isPen){
        this.isPen = isPen;
    }

    private void init(){
        paint = new Paint();
        colorCheck = Color.parseColor("#EE0000");
        colorUncheck = Color.parseColor("#CCCCCC");
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);

        for (int i = 0; i < 10; i++) {
            undoPos[i] = new UndoPos();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        int width = getWidth() - spacing*17 - padding*2;//17是包括左右+中间的间距
        radius = width / 32.0f;//16个点，由于是半径需要再除2
        //paint.setStrokeWidth(radius*2);
        space = spacing + radius*2;
        startLocal = spacing + padding + radius;
        //System.out.println("space:"+space+",startLocal:"+startLocal);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //LogUtils.print("width",width);
        /*width = width - padding;
        width = width / 16;
        width = width * 16 + padding;*/
        //LogUtils.print("after width",width);
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);

        setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
    }

    public void setData(boolean[][] d){
        this.data = d;
        invalidate();
    }

    public void setEditState(boolean isEdit){
        this.isNotEdit = !isEdit;
    }

    public boolean isReadState(){
        return this.isNotEdit;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //if (isNotEdit) return;
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

    private float touchX,touchY;
    int drawX,drawY;
    private boolean isMove = false;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(data == null || isNotEdit) return true;
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX();
                touchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                drawX = Math.round((event.getX()-padding) / space)-1;
                drawY = Math.round((event.getY()-padding-spacing) / space)-1;
                if((drawX & 0x000F) != drawX) drawX = checkOutOfLength(drawX);
                if((drawY & 0x000F) != drawY) drawY = checkOutOfLength(drawY);

                if(data[drawY][drawX] != isPen){
                    data[drawY][drawX] = isPen;
                    if(listener != null) listener.onPositionChange(drawX,drawY);
                    isMove = true;
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isMove) {
                    isMove = false;
                    return true;
                }
                if(touchY == event.getY() && touchX == event.getX()){
                    drawX = Math.round((event.getX()-padding) / space)-1;
                    drawY = Math.round((event.getY()-padding-spacing) / space)-1;
                    if((drawX & 0x000F) != drawX){//如果数组越界，重新计算值
                        drawX = checkOutOfLength(drawX);
                    }
                    if((drawY & 0x000F) != drawY){//如果数组越界，重新计算值
                        drawY = checkOutOfLength(drawY);
                    }
                    if(data[drawY][drawX] != isPen){
                        drawPos();
                    }
                }
                break;
        }
        return true;
    }

    /*@Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return false;
    }*/

    private void drawPos(){
        data[drawY][drawX] = isPen;
        if(listener != null) listener.onPositionChange(drawX,drawY);
        invalidate();
        currentUndoIndex++;
        undoTop = currentUndoIndex;
        undoPos[currentUndoIndex].state = !isPen;
        undoPos[currentUndoIndex].x = drawX;
        undoPos[currentUndoIndex].y = drawY;
        if(currentUndoIndex == 9) currentUndoIndex = -1;
    }

    public void doUndo(){
        if(undoTop == 0){
            undoTop = 9;
        }

        drawY = undoPos[undoTop].y;
        drawX = undoPos[undoTop].x;
        data[drawY][drawX] = undoPos[undoTop].state;
        if(listener != null) listener.onPositionChange(drawX,drawY);
        invalidate();
        undoTop--;
        currentUndoIndex = undoTop;
    }

    private int checkOutOfLength(int value){
        if(value > 15) return 15;
        else return 0;
    }

    private OnXYPositionListener listener;
    public void setOnDataChangeListener(OnXYPositionListener listener){
        this.listener = listener;
    }

    public interface OnXYPositionListener{
        void onPositionChange(int x,int y);
    }

    private UndoPos[] undoPos = new UndoPos[10];
    private int currentUndoIndex = -1;
    private int undoTop = 0;

    private class UndoPos{
        public int x=0;
        public int y=0;
        public boolean state;
    }

}
