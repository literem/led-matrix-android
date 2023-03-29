package com.literam.matrix.music.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import androidx.annotation.Nullable;

import com.literam.matrix.common.utils.DisplayUtil;
import com.literam.matrix.music.R;
import com.literam.matrix.music.entity.LrcRow;
import com.literam.matrix.music.utils.DefaultLrcBuilder;
import com.literam.matrix.music.utils.ILrcBuilder;

import java.io.File;
import java.util.List;

/**
 * Created by ky on 2018/5/8.
 * 歌词控件
 */

public class LyricView extends View {

    //private static final String TAG = "LyricView";

    private static final String DEFAULT_TEXT = "*暂无歌词*";
    private static final String LOADING_LRC_TEXT = "正在加载歌词…";

    private Context context;

    //时间的颜色
    private static final int COLOR_FOR_TIME_LINE = 0xffffffff;

    // 自动滚动每一行歌词的持续时长
    private static final int DURATION_SCROLL_LRC = 1000;

    // 歌词最大宽度，单位px
    private int LRC_MAX_WIDTH = 800;

    // 延迟消失indicator的时间，ms
    private static final int DELAY_HIDE_DURATION = 2000;

    // 是否正在拖动歌词
    private boolean isDraggingLrc = false;

    // ACTION_DOWN是否落在play按钮上
    private boolean isClickPlay;

    // 是否是一次点击事件，用于确认点击事件，切换歌词显示/关闭
    private boolean isClickEvent = false;

    // 是否需要画指示线，进度和播放按钮
    private boolean needDrawIndicator = false;

    // 是否正在显示指示线，进度和播放按钮
    private boolean isShowingIndicator = false;

    // 是否正在加载歌词
    private boolean isLoadingLrc = false;

    //保存Lrc歌词的集合
    private List<LrcRow> lrcRowList;

    // 实现歌词垂直方向滚动的辅助类
    private Scroller scroller;

    private int curLine;

    // 水平滚动歌词的x坐标
    private float horizonScrollTextX = 0;

    //监听
    private OnLyricViewListener onLyricViewListener;
    private OnViewClickListener onViewClickListener;

    // 用户横向滚动的动画
    private ValueAnimator animator;

    // 用于实现横向滚动
    private CountDownTimer timer;

    // 用于计算高亮歌词的进度
    private CountDownTimer percentageTimer;

    // 高亮歌词的播放进度
    private float finishPercentage;


    private float downY;
    private float lastY;
    private int touchSlop;
    private int playBitmapHeight;
    private Paint highlightPaint;//高亮歌词
    private Paint normalTextPaint;//正常歌词
    private Paint timelinePaint;//线 画笔
    private Paint progressPaint;//时间进度 画笔
    private Bitmap playBitmap;//按钮
    private RectF rect;
    private String lyric;

    /****** 颜色设置 ******/
    private int highlightColor = Color.parseColor("#FFFFFF");//高亮颜色
    private int normalTextColor = Color.parseColor("#BBBBBB");//正常颜色
    private int progressColor =  Color.parseColor("#FFFFFF"); //时间的 颜色

    //高亮行的上下行的颜色
    private int besideHighlightColor = normalTextColor;

    /****** 字体大小 ******/
    private float highlightTextSize;//高亮行的字体大小
    private float besideHighlightTextSize;//高亮行的上下行字体大小
    private float normalTextSize;//正常的字体大小
    private float progressTextSize;//显示时间的字体大小
    //private int padding;// 垂直方向上的padding
    private float eachLineHeight;// 每行歌词的高度

    // 用于控制indicator的显示逻辑
    Runnable hideIndicatorRunnable = new Runnable() {
        @Override
        public void run() {
            isShowingIndicator = false;
            if (hasLrc()) {
                needDrawIndicator = false;
                smoothScrollTo(getYHeight(curLine));
            }
        }
    };

    public LyricView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public LyricView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public LyricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
        highlightTextSize = DisplayUtil.dip2px(context,23.0f);
        besideHighlightTextSize = DisplayUtil.dip2px(context,19.0f);
        normalTextSize = DisplayUtil.dip2px(context,19.0f);
        progressTextSize = DisplayUtil.dip2px(context,12.0f);
        int padding = DisplayUtil.dip2px(context,18);
        eachLineHeight = normalTextSize + padding;

        //LRC_MAX_WIDTH = 800;

        scroller = new Scroller(getContext());
        rect = new RectF();

        highlightPaint = new Paint();
        highlightPaint.setColor(highlightColor);
        highlightPaint.setTextSize(highlightTextSize);
        highlightPaint.setAntiAlias(true);

        normalTextPaint = new Paint();
        normalTextPaint.setColor(normalTextColor);
        normalTextPaint.setTextSize(normalTextSize);
        normalTextPaint.setAntiAlias(true);

        timelinePaint = new Paint();
        timelinePaint.setColor(COLOR_FOR_TIME_LINE);
        timelinePaint.setTextSize(5);

        progressPaint = new Paint();
        progressPaint.setTextSize(progressTextSize);
        progressPaint.setAntiAlias(true);
        progressPaint.setColor(progressColor);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.img_arrow_play);
        playBitmap = scaleBitmap(bitmapDrawable.getBitmap());

        playBitmapHeight = playBitmap.getHeight()/2;
        touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDensity = 30;
        options.inTargetDensity = 30;
    }

    private Bitmap scaleBitmap(Bitmap origin) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = 50.0f / width;
        float scaleHeight = 50.0f / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        origin = null;
        /*if (!origin.isRecycled()) {
            origin.recycle();
        }*/
        return newBM;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        LRC_MAX_WIDTH = getWidth();
        LRC_MAX_WIDTH = (int) (LRC_MAX_WIDTH * 0.95);
        //System.out.println("width:"+getWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isLoadingLrc) {
            drawHintText(canvas, LOADING_LRC_TEXT);
            return;
        }

        if (!hasLrc()) {
            drawHintText(canvas, DEFAULT_TEXT);
            return;
        }

        if (needDrawIndicator) {
            drawIndicator(canvas);
        }

        float y = getHeight() / 2 + 40;
        for (int i = 0; i < lrcRowList.size(); i++) {
            if (i == curLine) {
                drawHighlightText(canvas, getLrc(i), y);
            } else {
                drawNormalText(canvas, i, y);
            }
            y = y + eachLineHeight;// 计算得到y坐标
        }
    }

    /**
     * 当正在加载或者暂无歌词时，绘制提示词
     */
    private void drawHintText(Canvas canvas, String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }
        float textWidth = normalTextPaint.measureText(text);
        float textX = (getWidth() - textWidth) / 2;
        canvas.drawText(text, textX, getHeight() / 2, normalTextPaint);
    }

    //绘制分隔线
    private void drawIndicator(Canvas canvas) {
        if (!hasLrc()) {
            return;
        }
        isShowingIndicator = true;
        // 因为会调用scroll滚动，所以需要加上getScrollY()
        float y = getHeight() / 2 + getScrollY() - 5;
        float x = getWidth();

        canvas.drawLine(150, y, x - 150, y, timelinePaint);
        canvas.drawBitmap(playBitmap, x-100, y-playBitmapHeight, null);


        String curProgress = lrcRowList.get(curLine).getStartTimeStr().substring(0, 5);
        Paint.FontMetricsInt fontMetricsInt = progressPaint.getFontMetricsInt();

        // 文字所占高度
        int fontHeight = fontMetricsInt.bottom - fontMetricsInt.top;

        // 拿到文字开始的位置
        float baselineY = y + fontHeight / 2 - fontMetricsInt.bottom;
        canvas.drawText(curProgress, 30, baselineY, progressPaint);
    }

    //绘制高亮行
    private void drawHighlightText(Canvas canvas, String text, float y) {
        if (text.isEmpty()) return;
        canvas.save();
        float textWidth = highlightPaint.measureText(text);
        // 默认为居中显示
        float x = (getWidth() - textWidth) / 2;
        if (textWidth > LRC_MAX_WIDTH) {
            // 歌词宽度大于控件宽度，动态设置歌词的起始x坐标，实现滚动显示
            x = horizonScrollTextX;
            RectF rect = new RectF(getLrcStartX(), y - highlightTextSize,
                    getLrcStartX() + LRC_MAX_WIDTH, y + highlightTextSize);
            canvas.clipRect(rect);
        }

        // 设置shader，用于渐变色显示
        /*float[] positions = new float[] {finishPercentage, finishPercentage + 0.1f};
        highlightPaint.setShader(new LinearGradient(x, y, x+textWidth, y,
                colors, positions, Shader.TileMode.CLAMP));*/

        canvas.drawText(text, x, y, highlightPaint);
        canvas.restore();
    }

    //绘制正常行
    private void drawNormalText(Canvas canvas, int lineNo, float y) {
        String text = getLrc(lineNo);
        if (TextUtils.isEmpty(text)) return;

        // 因为高亮歌词上下一行的字号和透明度，与其他位置的普通歌词不同
        if (lineNo == curLine - 1 || lineNo == curLine + 1) {
            normalTextPaint.setColor(besideHighlightColor);
            normalTextPaint.setTextSize(besideHighlightTextSize);
        } else {
            normalTextPaint.setColor(normalTextColor);
            normalTextPaint.setTextSize(normalTextSize);
        }

        canvas.save();
        float textWidth = normalTextPaint.measureText(text);
        float x = (getWidth() - textWidth) / 2;
        if (textWidth > LRC_MAX_WIDTH) {
            // 如果歌词宽度大于控件宽度，则居左显示
            x = getLrcStartX();
            /*RectF rect = new RectF(getLrcStartX(), y - normalTextSize,
                    getLrcStartX() + LRC_MAX_WIDTH, y + normalTextSize);*/
            rect.left = x;
            rect.top = y - normalTextSize;
            rect.right = x + LRC_MAX_WIDTH;
            rect.bottom = y + normalTextSize;
            canvas.clipRect(rect);
        }
        canvas.drawText(text, x, y, normalTextPaint);
        canvas.restore();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (!hasLrc()) {
                    return false;
                }
                if (isDraggingLrc) {//如果正在拖动歌词
                    isClickEvent = false;
                    float deltaY = event.getY() - lastY;
                    if ((getScrollY() - deltaY) < -eachLineHeight) {
                        // 处理上滑边界，如果已经滑动至顶端，则限制其继续上滑
                        deltaY = deltaY > 0 ? 0 : deltaY;
                    } else if ((getScrollY() - deltaY) > lrcRowList.size() * eachLineHeight) {
                        // 处理下滑边界
                        deltaY = deltaY < 0 ? 0 : deltaY;
                    }

                    scrollBy(getScrollX(), -(int)deltaY);
                    curLine = calculateLineNo();
                    lastY = event.getY();
                    return true;
                } else { //没有拖动歌词
                    if (Math.abs(event.getY() - downY) > touchSlop) {
                        isDraggingLrc = true;
//                        stopHorizontalScroll();
                        //stopHorizontalScrollWithTimer();
                        scroller.forceFinished(true);
                        lastY = event.getY();
                    }
                }
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                actionUp(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                // 如果出现侧向轻微滑动，并抬起手指的情况，此时并不会走ACTION_UP的回调
                // 而是会走ACTION_CANCEL.
                isDraggingLrc = false;
                break;
            default:
                break;
        }
        return true;
    }

    private void actionDown(MotionEvent event) {
        isClickEvent = true;
        if (!hasLrc()) {
            return;
        }

        removeCallbacks(hideIndicatorRunnable);
        downY = event.getY();
        needDrawIndicator = true;
        isClickPlay = isClickPlayBtn(event);
    }

    private void actionUp(MotionEvent event) {
        if (isClickEvent && !isClickPlay) {
            needDrawIndicator = false;
            isDraggingLrc = false;
            isShowingIndicator = false;
            if (onViewClickListener != null) {
                onViewClickListener.onClick();
            }
        }

        if (!hasLrc()) {
            return;
        }

        isDraggingLrc = false;
        // 设置3s后隐藏indicator
        postHideIndicator();

        // 只有当正在显示播放按钮，且点击事件落在其上时，才响应
        if (isClickPlay && isClickPlayBtn(event) && isShowingIndicator) {
            // 如果点击播放，则立即刷新界面
            needDrawIndicator = false;
            isShowingIndicator = false;
            invalidate();
            if (onLyricViewListener != null) {
                // 避免外部调用setProgress方法，将curLine重置，此处再主动计算一次curLine
                curLine = calculateLineNo();
                int progress = lrcRowList.get(curLine).getStartTime() / 1000;
                onLyricViewListener.onPlayButtonClick(progress);
                isClickPlay = false;
            }
        }
    }

    private void seekProgress(int progress, boolean seekBarByUser) {
        int lineNum = getLineNum(progress);
        if (lineNum != curLine) {
            curLine = lineNum;

            if(onLyricViewListener != null) onLyricViewListener.currentLyric(lrcRowList.get(curLine).getContent());
            //System.out.println(lrcRowList.get(curLine).getTotalTime());

            //如果显示分割线，并且不是由用户拖动 seekBar导致，则隐藏分割线
            if (needDrawIndicator && !seekBarByUser) {
                postHideIndicator();
            } else {
                if (seekBarByUser) {
                    hideIndicator();
                    forceScrollTo(getScrollX(), getYHeight(curLine));
                } else {
                    smoothScrollTo(getYHeight(curLine));
                }
                //checkNeedHorizScroll();
                //calculateProgress(lrcRowList.get(curLine).getTotalTime());
            }
        }
    }

    private void postHideIndicator() {
        //Log.d(TAG, "postHideIndicator()");
        removeCallbacks(hideIndicatorRunnable);
        postDelayed(hideIndicatorRunnable, DELAY_HIDE_DURATION);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            int oldY = getScrollY();
            int curY = scroller.getCurrY();
            if (oldY != curY && !isDraggingLrc) {
                scrollTo(getScrollX(), curY);
            }
            invalidate();
        }
    }

    private void smoothScrollTo(int targetY) {
        if (!scroller.isFinished()) {
            scroller.forceFinished(true);
        }
        int oldScrollY = getScrollY();
        int deltaY = targetY - oldScrollY;
        scroller.startScroll(getScrollX(), oldScrollY, 0, deltaY, DURATION_SCROLL_LRC);
        invalidate();
    }

    private void forceScrollTo(int targetX, int targetY) {
        if (!scroller.isFinished()) {
            scroller.forceFinished(true);
        }

        scrollTo(targetX, targetY);
    }



    //region ****************** 卡拉ok歌词高亮方法，暂时用不上 *************************/
    /**
     * 计算curLine已经播放过的进度
     * @param duration 当前行歌词的时长
     */
    private void calculateProgress(final long duration) {
        if (percentageTimer != null) {
            percentageTimer.cancel();
            percentageTimer = null;
        }
        percentageTimer = new CountDownTimer(duration, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                finishPercentage = 1 - ((float)millisUntilFinished) / duration;
                // 避免最后一个字因为计算问题，显示不完整
                if (finishPercentage > 0.9) {
                    finishPercentage = 1;
                }
                invalidate();
            }

            @Override
            public void onFinish() {

            }
        };
        percentageTimer.start();
    }

    private void checkNeedHorizScroll() {
        String text = getLrc(curLine);
        float textWidth = highlightPaint.measureText(text);
        if (textWidth > LRC_MAX_WIDTH) {
            startHorizontalScrollWithTimer(LRC_MAX_WIDTH + getLrcStartX() - textWidth,
                    lrcRowList.get(curLine).getTotalTime());
//            startHorizontalScroll(LRC_MAX_WIDTH + getLrcStartX() - textWidth,
//                    lrcRowList.get(curLine).getTotalTime());
        }
    }

    /**
     * 以动画的方式，不停改变歌词的起始x坐标，重绘，达到水平滚动的目的
     * Notice： 动画的onAnimationUpdate()方法偶尔会出现只调用两三次的情况，导致失效！原因不明
     */
    private void startHorizontalScroll(float endX, long duration) {
        if (animator == null) {
            animator = ValueAnimator.ofFloat(getLrcStartX(), endX);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    horizonScrollTextX = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
        } else {
            horizonScrollTextX = getLrcStartX();
            animator.cancel();
            animator.setFloatValues(getLrcStartX(), endX);
        }

        animator.setDuration(duration);
        animator.start();
    }

    private void stopHorizontalScroll() {
        if (animator != null) {
            animator.cancel();
        }
        horizonScrollTextX = 0;
    }

    /**
     * 以定时器的方式实现歌词的横向滚动，因为动画的方式偶尔失效
     */
    private void startHorizontalScrollWithTimer(final float endX, final long duration) {
        stopHorizontalScrollWithTimer();
        timer = new CountDownTimer(duration, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                float finishPercentage = 1 - ((float)millisUntilFinished) / duration;
                // 避免最后一个字因为计算问题，显示不完整
                if (finishPercentage > 0.9) {
                    finishPercentage = 1;
                }

                horizonScrollTextX = endX * finishPercentage;
                invalidate();
            }

            @Override
            public void onFinish() {

            }
        };
        timer.start();
    }

    private void stopHorizontalScrollWithTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    //endregion




    /**
     * 隐藏水平线
     */
    private void hideIndicator() {
        needDrawIndicator = false;
        isShowingIndicator = false;
        invalidate();
    }

    /**
     * 判断点击事件区域是否落在播放按键上
     */
    private boolean isClickPlayBtn(MotionEvent event) {
        if (event == null) {
            return false;
        }

        // 用于增大点击区域
        int spaceHolder = 20;
        // x y分别是draw play按钮时的坐标
        float x = getWidth() - 77;
        float y = getHeight() / 2 - playBitmap.getHeight()/2;
        return event.getX() > x && event.getY() > y - spaceHolder &&
                event.getY() < (y + playBitmap.getHeight() + spaceHolder);
    }

    /**
     * 根据传入的进度，计算对应的行号
     * @param progress: 传入进度，单位为秒
     */
    private int getLineNum(int progress) {
        if (!hasLrc()) {
            return 0;
        }

        for (int i = lrcRowList.size() - 1; i >= 0; i--) {
            if (lrcRowList.get(i).getStartTime() / 1000 <= progress) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 根据在垂直方向上的滚动距离，计算当前的高亮行号
     */
    private int calculateLineNo() {
        if (!hasLrc()) {
            return 0;
        }

        int curLineNum = (int) (getScrollY() / eachLineHeight);
        curLineNum = Math.max(curLineNum, 0);
        curLineNum = Math.min(curLineNum, lrcRowList.size() - 1);
        return curLineNum;
    }

    private int getYHeight(int lineNum) {
        return (int)(lineNum * eachLineHeight);
    }

    /**
     * 计算当歌词宽度大于LRC_MAX_WIDTH时，歌词的x起始坐标
     */
    private int getLrcStartX() {
        return (getWidth() - LRC_MAX_WIDTH) / 2;
    }

    /**
     * 根据索引获取某行的歌词
     */
    private String getLrc(int pos) {
        if (!hasLrc() || pos < 0 || pos >= lrcRowList.size()) {
            return "";
        }
        return lrcRowList.get(pos).getContent();
    }





    // ----------------- 对外提供的方法 ----------------------
    public boolean hasLrc() {
        return lrcRowList != null && !lrcRowList.isEmpty();
    }

    public String getLyricText(){
        if(!hasLrc()) return "";
        /*StringBuilder sb = new StringBuilder();
        for(LrcRow lrcRow : lrcRowList){
            sb.append(lrcRow.getContent()).append("\n");
        }
        return sb.toString();*/
        return lyric;
    }

    public void setLrc(String lrc) {
        reset();
        if(lrc == null || lrc.isEmpty()){
            return;
        }
        lyric = lrc;
        ILrcBuilder lrcBuilder = new DefaultLrcBuilder();
        this.lrcRowList = lrcBuilder.getLrcRows(lrc);
        invalidate();
    }

    /**
     * 设置lrc路径
     * @param path 路径
     */
    public void setLrcPath(String path) {
        reset();
        if (TextUtils.isEmpty(path)) return;
        File file = new File(path);
        if (!file.exists()) {
            postInvalidate();
            //Log.i("test","文件不存在");
            return;
        }
        ILrcBuilder lrcBuilder = new DefaultLrcBuilder();
        this.lrcRowList = lrcBuilder.getLrcRowsByPath(path);
        invalidate();
    }

    /**
     * 设置当前进度
     * @param progress: 当前进度，单位为秒.
     * @param seekBarByUser: 是否由用户拖动 seekBar导致
     */
    public void setProgress(int progress, boolean seekBarByUser) {
        if (!hasLrc()) {
            return;
        }
        if(isDraggingLrc) return;
        seekProgress(progress, seekBarByUser);
    }

    public void reset() {
        forceScrollTo(getScrollX(), 0);
        lrcRowList = null;
        isLoadingLrc = false;
        curLine = 0;
        needDrawIndicator = false;
        isShowingIndicator = false;
        isDraggingLrc = false;
        removeCallbacks(hideIndicatorRunnable);
        invalidate();
    }


    public int getLineTotalTime(){
        int totalTime;
        if(curLine >= 1){
            int lastTime = lrcRowList.get(curLine-1).getTotalTime();
            int nowTime = lrcRowList.get(curLine).getTotalTime();
            totalTime = nowTime - lastTime;
        }else{
            totalTime = lrcRowList.get(curLine).getTotalTime();
        }
        return totalTime;
    }

    public void setOnLyricViewListener(OnLyricViewListener onLyricViewListener) {
        this.onLyricViewListener = onLyricViewListener;
    }

    public void setOnViewClickListener(OnViewClickListener onViewClickListener) {
        this.onViewClickListener = onViewClickListener;
    }

    /**
     * 用于监听播放按钮是否被点击
     */
    public interface OnLyricViewListener {
        void onPlayButtonClick(int progress);// progress为点击播放时，选中的歌词对应的时间，单位为秒
        void currentLyric(String lyric);//当前行的歌词
    }

    /**
     * 用于监听该view是否被点击
     */
    public interface OnViewClickListener {
        void onClick();
    }
}
