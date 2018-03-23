package com.jzby.jzbysounderclient.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jzby.jzbysounderclient.R;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.List;

/**
 * Created by gordan on 2018/3/8.
 */

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumAdapterViewHolder> {

    public static final int TYPE_FOOTER = 1;
    public static final int TYPE_NORMAL = 2;

    private View mFooterView;

    List<Track> mList;

    int mFooterResId;

    int resId;

    int mSelectedIndex=-1;

    public AlbumAdapter(List<Track> tracks, int resId) {
        super();
        this.mList = tracks;
        this.resId = resId;
    }

    public View getFooterView()
    {
        return this.mFooterView;
    }

    public void setFooterResId(int resId)
    {
        this.mFooterResId=resId;
        notifyItemInserted(mList.size());
    }

    public void setFooterView(View view)
    {
        this.mFooterView=view;
    }

    public void hiddenFooterView()
    {
        if(this.mFooterResId>0)
        {
            notifyItemRemoved(mList.size());
        }
    }

    public void notifyDataChanged(List<Track> data)
    {
        this.mList=data;
        this.notifyDataSetChanged();
    }

    public void notifyDataChanged(int position)
    {
        this.mSelectedIndex=position;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mList.size()+(this.mFooterResId>0?1:0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position)
    {
        return position>=mList.size()?TYPE_FOOTER:TYPE_NORMAL;
    }

    @Override
    public void onBindViewHolder(final AlbumAdapterViewHolder holder, int position)
    {
        if(getItemViewType(position)==TYPE_FOOTER)
        {
            return;
        }

        Track track = mList.get(position);
        holder.textView.setText(track.getTrackTitle());

        if(position==mSelectedIndex)
        {
            holder.imageView.setImageResource(R.drawable.notify_btn_dark_pause_normal);
            holder.imageView.setTag("1");
        }
        else
        {
            holder.imageView.setImageResource(R.drawable.notify_btn_dark_play_normal);
            holder.imageView.setTag("0");
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener!=null)
                {
                    mListener.onItemClickListener(holder.itemView,holder.getLayoutPosition());
                }
            }
        });
    }

    @Override
    public AlbumAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if(viewType==TYPE_FOOTER)
        {
            mFooterView=LayoutInflater.from(parent.getContext()).inflate(this.mFooterResId,parent, false);
            return new AlbumAdapterViewHolder(mFooterView);
        }
        View root = LayoutInflater.from(parent.getContext()).inflate(resId,parent,false);
        return new AlbumAdapterViewHolder(root);
    }

    private OnItemClickInterface mListener;

    public void setOnItemClickListener(OnItemClickInterface listener) {
        this.mListener = listener;
    }

    public interface OnItemClickInterface {
        void onItemClickListener(View view, int position);
    }

    public class AlbumAdapterViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public AlbumAdapterViewHolder(View itemView) {
            super(itemView);

            if(itemView == mFooterView)
            {
                return;
            }

            textView = (TextView) itemView.findViewById(R.id.tv_album_name);
            imageView = (ImageView) itemView.findViewById(R.id.iv_status_album);
        }
    }

}
