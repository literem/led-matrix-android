package com.literem.matrix.common.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.literem.matrix.common.R;

import java.util.ArrayList;
import java.util.List;

/**
 * author :
 * time   : 2021
 * desc   : 可以滚动每行文本的ScrollView
 * version: 1.0
 */
public class RowScrollView extends View {

    private static final int SCROLL_TIME = 500;
    private static final String DEFAULT_TEXT = "没有文本显示";

    private final int DISPLAY_MODE_NORMAL = 1;//正常滚动
    private final int DISPLAY_MODE_SEEK = 2;//是否拖动
    private final int SCROLL_NEXT=3;//滚动下一行的状态
    private final int SCROLL_LAST=4;//滚动上一行的状态

    private int mDisplayMode;//当前显示模式
    private int mScrollMode;//当前滚动模式，向上滚动或向下滚动
    private boolean disableScrollNext = false;//是否禁用滚动下一行

    private List<String> mLines = new ArrayList<>();

    private int mLrcHeight; // lrc界面的高度
    private int mRows;      // 多少行
    private int mCurrentLine = 0; // 当前行
    private int mMaxLine;
    private int mOffsetY;   // y上的偏移
    private int mMaxScroll; // 最大滑动距离=一行歌词高度+歌词间距
    private int mCurrentXOffset;

    private float mDividerHeight; // 行间距

    private Rect mTextBounds;

    private Paint mNormalPaint; // 常规的字体
    private Paint mCurrentPaint; // 当前歌词的大小
    private Paint mLinePaint;//画线

    private Bitmap mBackground;

    private Scroller mScroller;


    public RowScrollView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RowScrollView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context, new LinearInterpolator());
        init(attrs);
    }

    // 初始化操作
    private void init(AttributeSet attrs) {
        // 解析自定义属性
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.RowScrollView);
        float textSize = ta.getDimension(R.styleable.RowScrollView_android_textSize, 50.0f);
        mRows = ta.getInteger(R.styleable.RowScrollView_row, 10);
        mDividerHeight = ta.getDimension(R.styleable.RowScrollView_RowDivider, 50.0f);

        int normalTextColor = ta.getColor(R.styleable.RowScrollView_normalColor, 0xffffffff);
        int currentTextColor = ta.getColor(R.styleable.RowScrollView_currentColor, 0xff00ffde);
        ta.recycle();

        if (mRows != 0) {
            // 计算lrc面板的高度
            mLrcHeight = (int) (textSize + mDividerHeight) * mRows + 5;
        }

        mNormalPaint = new Paint();
        mCurrentPaint = new Paint();
        mLinePaint = new Paint();

        // 初始化paint
        mNormalPaint.setTextSize(textSize);
        mNormalPaint.setColor(normalTextColor);
        mNormalPaint.setAntiAlias(true);
        mCurrentPaint.setTextSize(textSize);
        mCurrentPaint.setColor(currentTextColor);
        mCurrentPaint.setAntiAlias(true);
        mLinePaint.setColor(Color.parseColor("#FFFFFF"));

        mTextBounds = new Rect();
        mCurrentPaint.getTextBounds(DEFAULT_TEXT, 0, DEFAULT_TEXT.length(), mTextBounds);
        computeMaxScroll();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // 如果没有设置固定行数， 则默认测量高度，并根据高度计算行数
        if (mRows == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        // 设置了固定行数，重新设置view的高度
        int measuredHeightSpec = MeasureSpec.makeMeasureSpec(mLrcHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, measuredHeightSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mBackground != null) {
            mBackground = Bitmap.createScaledBitmap(mBackground, getMeasuredWidth(), mLrcHeight, true);
        }
    }

    private int width;
    private int height;
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        //LogUtils.print("height",height);
        mLrcHeight = getMeasuredHeight();
        computeRows();
    }


    /**
     * 根据高度计算行数
     */
    private void computeRows() {
        float lineHeight = mTextBounds.height() + mDividerHeight;
        mRows = (int) (getMeasuredHeight() / lineHeight);
    }

    /**
     * 计算滚动距离
     */
    private void computeMaxScroll() {
        mMaxScroll = (int) (mTextBounds.height() + mDividerHeight);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        if (mBackground != null) {
            canvas.drawBitmap(mBackground, new Matrix(), null);
        }

        //float centerY = (getMeasuredHeight() + mTextBounds.height() - mDividerHeight) / 2;
        //计算中心位置
        float centerY = (height + mTextBounds.height()) / 2;
        float position;

        //如果没有歌词，显示默认文本
        if (mLines.isEmpty()) {
            canvas.drawText(DEFAULT_TEXT,
                    (width - mCurrentPaint.measureText(DEFAULT_TEXT)) / 2,
                    centerY, mCurrentPaint);
            return;
        }

        //每一行的高度
        float offsetY = mTextBounds.height() + mDividerHeight;

        if(mDisplayMode == DISPLAY_MODE_SEEK){
            canvas.drawLine(0, centerY, width, centerY, mLinePaint);
        }

        //计算第一行和最后一行
        int firstLine = mCurrentLine - mRows / 2;
        firstLine = firstLine <= 0 ? 0 : firstLine;
        int lastLine = mCurrentLine + mRows / 2 + 2;
        lastLine = lastLine >= mLines.size() - 1 ? mLines.size() - 1 : lastLine;

        //画出当前行的高亮显示
        drawCurrentLine(canvas, width, centerY - mOffsetY);

        // 画当前行上面的
        for (int i = mCurrentLine - 1,j = 1; i >= firstLine; i--,j++) {
            position = centerY - j * offsetY - mOffsetY;
            if(position < offsetY) continue;//如果小于一行的高度，不显示

            String lrc = mLines.get(i);
            float x = (width - mNormalPaint.measureText(lrc)) / 2;//测量一句歌词的长度，得到起始x轴的位置
            canvas.drawText(lrc, x, position, mNormalPaint);
        }

        // 画当前行下面的
        for (int i = mCurrentLine + 1,j = 1; i <= lastLine; i++,j++) {
            position = centerY + j * offsetY - mOffsetY;
            if(position > height) continue;//如果下面行超过总高度，不显示

            String lrc = mLines.get(i);
            float x = (width - mNormalPaint.measureText(lrc)) / 2;
            canvas.drawText(lrc, x, position, mNormalPaint);
        }

    }

    private void drawCurrentLine(Canvas canvas, int width, float y) {
        String currentLrc = mLines.get(mCurrentLine);
        float contentWidth = mCurrentPaint.measureText(currentLrc);
        if (contentWidth > width) {
            canvas.drawText(currentLrc, mCurrentXOffset, y, mCurrentPaint);
            if (contentWidth - Math.abs(mCurrentXOffset) < width) {
                mCurrentXOffset = 0;
            }
        } else {
            float currentX = (width - contentWidth) / 2;
            canvas.drawText(currentLrc, currentX, y, mCurrentPaint);// 画当前行
        }
    }

    //平滑滚动的重要方法
    @Override
    public void computeScroll() {
        //Scroller类会从1到95，每隔3个数，回调该方法
        if(mScroller.computeScrollOffset()) {
            mOffsetY = mScroller.getCurrY();//平滑滚动，参数mOffsetY是关键
            if(mScroller.isFinished()) {//滚动结束后，设置高亮
                if(mScrollMode==SCROLL_NEXT)
                    mCurrentLine++;
                else
                    mCurrentLine--;
                mOffsetY = 0;
            }
            //每次回调该方法，会得到1到95的范围变化，赋值mOffsetY，再重新绘制页面，达到平滑滚动的效果
            postInvalidate();
        }
    }

    public int setData(String str){
        mCurrentLine=0;
        this.mLines.clear();
        String[] temp = str.split("\n");

        for (String s : temp){
            if(TextUtils.isEmpty(s)) continue;
            mLines.add(s);
        }
        mMaxLine = mLines.size()-1;

        return temp.length;
    }

    //禁用滑动下一行
    public void disableScrollNext(boolean disableScrollNext){
        this.disableScrollNext = disableScrollNext;
    }

    public void scrollToFirstLine(){
        mCurrentLine = 0;
        postInvalidate();
    }

    /**
     * 滚动下一行
     */
    public boolean scrollNextLine() {
        if(mCurrentLine == mMaxLine)
            return false;
        mScrollMode = SCROLL_NEXT;
        mScroller.abortAnimation();
        mScroller.startScroll(0, 0, 0, mMaxScroll, SCROLL_TIME);
        postInvalidate();
        return true;
    }

    /**
     * 滚动上一行
     */
    public boolean scrollLastLine(){
        if(mCurrentLine == 0)
            return false;
        mScrollMode = SCROLL_LAST;
        mScroller.abortAnimation();
        mScroller.startScroll(0, 0, 0, -mMaxScroll, SCROLL_TIME);
        postInvalidate();
        return true;
    }

    //获取当前高亮行的字符串
    public String getCurrentString(boolean isPageDown){
        if(isPageDown){
            return mLines.get(mCurrentLine+1);
        }else{
            return mLines.get(mCurrentLine-1);
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLines == null || mLines.size() == 0) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://手指按下
                mLastMotionY = event.getY();
                mDisplayMode = DISPLAY_MODE_SEEK;
                break;
            case MotionEvent.ACTION_MOVE://手指移动
                doSeek(event);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP://手指抬起
                mDisplayMode = DISPLAY_MODE_NORMAL;
                invalidate();
                break;
        }
        return true;
    }

    private float mLastMotionY;
    //处理单指在屏幕移动时，歌词上下滚动
    private void doSeek(MotionEvent event) {
        float y = event.getY();//手指当前位置的y坐标

        //第一次按下的y坐标和目前移动手指位置的y坐标之差
        float offsetY = y - mLastMotionY;
        if (Math.abs(offsetY) < 50) {//如果移动距离小于50，不做任何处理
            return;
        }

        if (offsetY < 0) {//手指向上移动，歌词向下滚动
            if(mCurrentLine == mMaxLine)
                return;
            if(disableScrollNext) return;
            mCurrentLine++;
        } else if (offsetY > 0) {//手指向下移动，歌词向上滚动
            if(mCurrentLine==0)
                return;
            mCurrentLine--;
        }
        mLastMotionY = y;
        invalidate();
    }

}
