package com.jzby.jzbysounderclient.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jzby.jzbysounderclient.R;
import com.ximalaya.ting.android.opensdk.model.album.Announcer;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

/**
 * Created by gordan on 2018/3/2.
 */

public class ProgramAdapter extends BaseAdapter
{
    Context mContext;

    List<Track> mProgramBeans;

    int mResId;

    int mCurrentPosition;

    public ProgramAdapter(Context context,List<Track> programs,int resId,int selectedPosition)
    {
        super();
        mContext=context;
        mProgramBeans=programs;
        mResId=resId;
        mCurrentPosition=selectedPosition;
    }


    public void notifyDataChanged(int position)
    {
        mCurrentPosition=position;
        this.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return mProgramBeans.size();
    }

    @Override
    public Object getItem(int i) {
        return mProgramBeans.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup)
    {
        Track bean=mProgramBeans.get(i);

        Announcer mAnnouncer=bean.getAnnouncer();

        ProgramViewHolder holder=null;

        if(view==null)
        {
            view=LayoutInflater.from(mContext).inflate(mResId,null);
            TextView tvName=(TextView)view.findViewById(R.id.tv_program_name);
            TextView tvAuthor=(TextView)view.findViewById(R.id.tv_program_author);
            ImageView iv=(ImageView)view.findViewById(R.id.iv_program_album);

            holder=new ProgramViewHolder();
            holder.tvName=tvName;
            holder.tvAuthor=tvAuthor;
            holder.iv=iv;

            view.setTag(holder);
        }
        else
        {
            holder=(ProgramViewHolder)view.getTag();
        }

        holder.tvName.setText(bean.getTrackTitle());
        holder.tvAuthor.setText(mAnnouncer.getNickname());

        if(i==mCurrentPosition)
        {
            holder.iv.setVisibility(View.VISIBLE);
            holder.tvName.setTextColor(mContext.getResources().getColor(R.color.menu_selected));
            holder.tvAuthor.setTextColor(mContext.getResources().getColor(R.color.menu_selected));
        }
        else
        {
            holder.iv.setVisibility(View.GONE);
            holder.tvName.setTextColor(mContext.getResources().getColor(R.color.default_bg));
            holder.tvAuthor.setTextColor(mContext.getResources().getColor(R.color.default_bg));
        }
        return view;
    }

    class ProgramViewHolder
    {
        TextView tvName;

        TextView tvAuthor;

        ImageView iv;
    }

}
