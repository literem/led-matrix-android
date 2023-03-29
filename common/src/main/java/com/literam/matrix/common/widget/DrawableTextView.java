package com.literam.matrix.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.literam.matrix.common.R;
import com.literam.matrix.common.utils.DisplayUtil;

/**
 * author :
 * time   : 2021
 * desc   : 带有显示图片功能的TextView
 * version: 1.0
 */
public class DrawableTextView extends AppCompatTextView {

    private final int LEFT = 1, TOP = 2, RIGHT = 3, BOTTOM = 4;
    private Drawable drawableImage;
    private int drawableWidth;
    private int drawableHeight;
    private int drawableLocation;

    public DrawableTextView(Context context) {
        this(context, null);
    }
    public DrawableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.DrawableTextView, 0, 0);
        drawableImage = typedArray.getDrawable(R.styleable.DrawableTextView_textImage);
        drawableWidth = (int) typedArray.getDimension(R.styleable.DrawableTextView_textImageWidth, 20);
        drawableHeight = (int) typedArray.getDimension(R.styleable.DrawableTextView_textImageHeight, 20);
        drawableLocation = typedArray.getInt(R.styleable.DrawableTextView_textImageLocation, LEFT);
        typedArray.recycle();
        drawDrawable();
    }

    /**
     * 设置TextView的图片，并使用默认大小（默认宽高20dp）
     * @param drawable 图片
     */
    public void setTextViewDrawable(Drawable drawable) {
        this.drawableImage = drawable;
        drawDrawable();
    }

    /**
     * 设置Text View图片大小，并使用给定宽高（单位dp）
     * @param drawable d
     * @param width w
     * @param height h
     */
    public void setTextViewDrawable(Drawable drawable,Context context, int width, int height) {

        //DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        //int mWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,width,displayMetrics);
        //int mHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,height,displayMetrics);

        int mWidth = DisplayUtil.dip2px(context,width);
        int mHeight = DisplayUtil.dip2px(context,height);

        setting(drawable,mWidth,mHeight);
    }

    public void showDrawable(){
        drawDrawable();
    }

    public void hideDrawable(){
        setCompoundDrawables(null, null, null, null);
    }

    private void setting(Drawable drawable, int Width, int Height) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        drawable = new BitmapDrawable(getResources(), getScaleBitmap(bitmap,
                Width, Height));
        drawable.setBounds(0, 0, Width, Height);
        this.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable,null,null,null);
    }

    /**
     * 设置Drawable的宽高和位置
     */
    private void drawDrawable() {
        if (drawableImage != null) {
            Bitmap bitmap = ((BitmapDrawable) drawableImage).getBitmap();
            Drawable drawable;
            if (drawableWidth != 0 && drawableHeight != 0) {
                drawable = new BitmapDrawable(getResources(), getScaleBitmap(bitmap,
                        drawableWidth, drawableHeight));
            } else {
                drawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap,
                        bitmap.getWidth(), bitmap.getHeight(), true));
            }
            drawable.setBounds(0, 0, drawableWidth, drawableHeight);
            switch (drawableLocation) {
                case LEFT:
                    setCompoundDrawables(drawable, null, null, null);
                    break;
                case TOP:
                    setCompoundDrawables(null, drawable, null, null);
                    break;
                case RIGHT:
                    setCompoundDrawables(null, null, drawable, null);
                    break;
                case BOTTOM:
                    setCompoundDrawables(null, null, null, drawable);
                    break;
            }
        }
    }

    public static Bitmap getScaleBitmap(Bitmap bm, int mWidth, int mHeight) {
        // 获得图片宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) mWidth) / width;
        float scaleHeight = ((float) mHeight) / height;
        // 设置缩放Matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 返回缩放后新图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

}
