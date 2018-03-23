package com.jzby.jzbysounderclient.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by gordan on 2018/3/2.
 */

public class PlayPagerAdapter extends FragmentPagerAdapter {

    List<Fragment> mItemFragment;

    @Override
    public int getCount() {
        return mItemFragment.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mItemFragment.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public PlayPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        mItemFragment = fragments;
    }


}
