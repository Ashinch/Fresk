package com.asterism.fresk.ui.widget;

import android.content.Context;
import androidx.viewpager.widget.ViewPager;

import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 滚动ViewPager控件类
 *
 * @author Ashinch
 * @email Glaxyinfinite@outlook.com
 * @date on 2019-07-01 16:08
 */
public class ScrollViewPager extends ViewPager {

    private boolean mIsCanScroll = true;


    public ScrollViewPager(Context context) {
        super(context);
    }

    public ScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置控件是否可以滚动
     *
     * @param isCanScroll 是否可滚动
     */
    public void setCanScroll(boolean isCanScroll) {
        this.mIsCanScroll = isCanScroll;
    }

    /**
     * 重写 拦截触摸事件
     *
     * @param ev 动作事件
     *
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mIsCanScroll && super.onInterceptTouchEvent(ev);
    }

    /**
     * 重写 触摸事件
     *
     * @param ev 动作事件
     *
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mIsCanScroll && super.onTouchEvent(ev);
    }
}