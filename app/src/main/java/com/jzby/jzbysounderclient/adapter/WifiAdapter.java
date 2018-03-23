package com.jzby.jzbysounderclient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.jzby.jzbysounderclient.R;

import java.util.List;

/**
 * Created by gordan on 2018/3/22.
 */

public class WifiAdapter extends BaseAdapter
{
    List<String> mWifiList;

    Context mContext;

    int mResId;


    public WifiAdapter(Context context,List<String> data,int resId)
    {
        mContext=context;
        mWifiList=data;
        mResId=resId;
    }

    @Override
    public int getCount() {
        return mWifiList.size();
    }

    @Override
    public Object getItem(int i) {
        return mWifiList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        String name=mWifiList.get(i);
        View root= LayoutInflater.from(viewGroup.getContext()).inflate(mResId,viewGroup,false);
        TextView textView=(TextView) root.findViewById(R.id.tv_wifi_name);
        textView.setText(name);
        return root;
    }
}
