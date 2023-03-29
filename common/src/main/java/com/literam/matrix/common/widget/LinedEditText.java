package com.literam.matrix.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

/**
 * author : literem
 * time   : 2023/01/31
 * desc   :
 * version: 1.0
 */
public class LinedEditText extends AppCompatEditText {

    private Paint linePaint;
    private float margin;
    private int paperColor;

    public LinedEditText(@NonNull Context context) {
        this(context,null,0);
    }

    public LinedEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public LinedEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.linePaint = new Paint();
        paperColor = Color.YELLOW;
        margin = 10;
    }

    protected void onDraw(Canvas paramCanvas) {
        paramCanvas.drawColor(this.paperColor);
        int i = getLineCount();
        int j = getHeight();
        int k = getLineHeight();
        int m = 1 + j / k;
        if (i < m)
            i = m;
        int n = getCompoundPaddingTop();
        paramCanvas.drawLine(0.0F, n, getRight(), n, this.linePaint);
        for (int i2 = 0;; i2++) {
            if (i2 >= i) {
                setPadding(10 + (int) this.margin, 0, 0, 0);
                super.onDraw(paramCanvas);
                paramCanvas.restore();
                return;
            }
            n += k;
            paramCanvas.drawLine(0.0F, n, getRight(), n, this.linePaint);
            paramCanvas.save();
        }
    }
}
