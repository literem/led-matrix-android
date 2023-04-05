package com.literem.matrix.common.utils;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

/**
 * Created by debbytang.
 * Description:显示隐藏布局的属性动画(铺展)
 * Date:2017/3/30.
 */
public class HiddenAnimUtils {

    private View hideView;
    private View down;//需要展开隐藏的布局，开关控件

    private int width,lastWidth;

    private RotateAnimation animation;//旋转动画

    /**
     * 构造器(可根据自己需要修改传参)
     * @param hideView 需要隐藏或显示的布局view
     * @param down 按钮开关的view
     */
    public static HiddenAnimUtils newInstance(View hideView,View down,int width){
        return new HiddenAnimUtils(hideView,down,width);
    }

    private HiddenAnimUtils(View hideView,View down,int width){
        this.hideView = hideView;
        this.down = down;
        this.width = width;
    }

    /**
     * 开关
     */
    public void toggle(){
        startAnimation();
        if (View.VISIBLE == hideView.getVisibility()) {
            closeAnimate(hideView);//布局隐藏
        } else {
            openAnim(hideView);//布局铺开
        }
    }

    /**
     * 开关旋转动画
     */
    private void startAnimation() {
        if (View.VISIBLE == hideView.getVisibility()) {
            animation = new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        } else {
            animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        }
        animation.setDuration(300);//设置动画持续时间
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatMode(Animation.REVERSE);//设置反方向执行
        animation.setFillAfter(true);//动画执行完后是否停留在执行完的状态
        down.startAnimation(animation);
    }

    private void openAnim(View v) {
        v.setVisibility(View.VISIBLE);
        ValueAnimator animator = createDropAnimator(v, 0, width);
        animator.start();
    }

    private void closeAnimate(final View view) {
        lastWidth = view.getWidth();
        if(lastWidth != width && lastWidth > 0) width = lastWidth;
        ValueAnimator animator = createDropAnimator(view, this.width, 0);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
        animator.start();
    }

    private ValueAnimator createDropAnimator(final View v, int start, int end) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(700);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int value;
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                value = (int) arg0.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = v.getLayoutParams();
                layoutParams.width = value;

                v.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }
}

