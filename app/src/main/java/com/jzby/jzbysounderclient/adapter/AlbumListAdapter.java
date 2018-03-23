package com.jzby.jzbysounderclient.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jzby.jzbysounderclient.R;
import com.ximalaya.ting.android.opensdk.model.album.Album;

import org.xutils.x;

import java.util.List;

/**
 * Created by gordan on 2018/3/12.
 */

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.AlbumListViewHolder>
{
    public static final int TYPE_FOOTER = 1;
    public static final int TYPE_NORMAL = 2;

    List<Album> mAlbums;

    int mFootResId;

    int mResId;

    private View mFooterView;

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mAlbums.size()+(this.mFootResId>0?1:0);
    }

    public AlbumListAdapter(List<Album> albumList,int resId) {
        super();
        this.mAlbums=albumList;
        this.mResId=resId;
    }

    public void setFooterResId(int viewId)
    {
        this.mFootResId=viewId;
    }

    public void hiddenFooterView()
    {
        this.notifyItemRemoved(this.mAlbums.size());
    }

    public void notifyDataChanged(List<Album> data)
    {
        this.mAlbums=data;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {

        if(position>=mAlbums.size())
        {
            return TYPE_FOOTER;
        }
        else
        {
            return TYPE_NORMAL;
        }
    }

    @Override
    public void onBindViewHolder(final AlbumListViewHolder holder, int position)
    {
        if(getItemViewType(position) == TYPE_FOOTER)
        {
            return;
        }

        Album album=mAlbums.get(position);
        holder.tvRadioName.setText(album.getAlbumTitle());
        holder.tvRadioDesc.setText(album.getAlbumIntro());
        x.image().bind(holder.ivCover,album.getCoverUrlLarge());

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
    public AlbumListViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        if(viewType==TYPE_FOOTER)
        {
            mFooterView=LayoutInflater.from(parent.getContext()).inflate(mFootResId,parent,false);
            return new AlbumListViewHolder(mFooterView);
        }
        else
        {
            View root= LayoutInflater.from(parent.getContext()).inflate(mResId,parent,false);
            return new AlbumListViewHolder(root);
        }
    }

    private OnItemClickInterface mListener;

    public void setOnItemClickListener(OnItemClickInterface listener) {
        this.mListener = listener;
    }

    public interface OnItemClickInterface {
        void onItemClickListener(View view, int position);
    }

    public class AlbumListViewHolder extends RecyclerView.ViewHolder
    {
        ImageView ivCover;
        TextView tvRadioName;
        TextView tvRadioDesc;

        public AlbumListViewHolder(View itemView) {
            super(itemView);

            if(itemView==mFooterView)
            {
                return;
            }
            ivCover = (ImageView) itemView.findViewById(R.id.iv_radio_cover);
            tvRadioName = (TextView) itemView.findViewById(R.id.tv_radio_name);
            tvRadioDesc = (TextView) itemView.findViewById(R.id.tv_radio_tag);
        }
    }
}
