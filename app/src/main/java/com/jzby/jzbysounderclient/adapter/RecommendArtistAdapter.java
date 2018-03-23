package com.jzby.jzbysounderclient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jzby.jzbysounderclient.R;
import com.jzby.jzbysounderclient.bean.ArtistBean;

import java.util.List;

/**
 * Created by gordan on 2018/3/6.
 */

public class RecommendArtistAdapter extends BaseAdapter
{

    Context mContext;
    List<ArtistBean> mArtistBeans;
    int mResId;


    @Override
    public Object getItem(int i) {
        return mArtistBeans.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ArtistBean bean= mArtistBeans.get(i);

        if(view==null)
        {
            view=LayoutInflater.from(mContext).inflate(mResId,null);
        }

        ImageView imageView=(ImageView)view.findViewById(R.id.iv_artist);
        TextView textView=(TextView)view.findViewById(R.id.tv_artist);
        imageView.setImageResource(bean.getArtImage());
        textView.setText(bean.getArtName());
        return view;
    }

    @Override
    public int getCount() {
        return mArtistBeans.size();
    }

    public RecommendArtistAdapter(Context context, List<ArtistBean> artistBeen,int resId) {
        super();
        this.mContext=context;
        this.mArtistBeans=artistBeen;
        this.mResId=resId;
    }
}
