package com.jzby.jzbysounderclient.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by gordan on 2018/3/2.
 */

public class MyViewPager extends ViewPager
{
    private boolean mSlideFlag=false;

    public MyViewPager(Context context)
    {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs)
    {
        super(context,attrs);
    }

    public void setViewPagerSlideFlag(boolean flag)
    {
        mSlideFlag=flag;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mSlideFlag && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mSlideFlag && super.onTouchEvent(ev);
    }
}
