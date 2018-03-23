package com.jzby.jzbysounderclient.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by gordan on 2018/3/1.
 */

public class BannerPagerAdapter extends PagerAdapter
{
    List<View> mItemViews;

    public BannerPagerAdapter(List<View> views)
    {
        mItemViews=views;
    }

    @Override
    public int getCount() {
        return mItemViews.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mItemViews.get(position));
        return mItemViews.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //super.destroyItem(container, position, object);

        container.removeView(mItemViews.get(position));
    }
}
