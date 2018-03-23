package com.jzby.jzbysounderclient.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by gordan on 2018/3/2.
 */

public class ClientPagerAdapter extends FragmentPagerAdapter
{
    List<Fragment> mItemFragment;

    public ClientPagerAdapter(FragmentManager fm,List<Fragment> fragments) {
        super(fm);
        mItemFragment=fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mItemFragment.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mItemFragment.size();
    }

}
