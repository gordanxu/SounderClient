package com.jzby.jzbysounderclient.adapter;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jzby.jzbysounderclient.R;
import com.jzby.jzbysounderclient.bean.MusicBean;

import java.util.List;

/**
 * Created by gordan on 2018/3/6.
 */

public class HotMusicAdapter extends RecyclerView.Adapter<HotMusicAdapter.HotMusicViewHolder> {

    public static final int TYPE_FOOTER = 1;  //说明是带有Footer的
    public static final int TYPE_NORMAL = 2;  //说明是不带有header和footer的

    private View mFooterView;

    List<MusicBean> mMusicList;

    int resId;

    public HotMusicAdapter(List<MusicBean> musicBeans, int resId) {
        this.mMusicList = musicBeans;
        this.resId = resId;
    }

    public View getFooterView() {
        return mFooterView;
    }

    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyItemInserted(getItemCount() - 1);
    }

    public void notifyDataChanged(List<MusicBean> data) {
        mMusicList = data;
        this.notifyDataSetChanged();
    }


    @Override
    public int getItemViewType(int position) {
        if (mFooterView == null) {
            return TYPE_NORMAL;
        }

        if (position == getItemCount() - 1) {
            return TYPE_FOOTER;
        }

        return TYPE_NORMAL;
    }

    @Override
    public HotMusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (mFooterView != null && viewType == TYPE_FOOTER) {
            return new HotMusicViewHolder(mFooterView);
        }

        View root = LayoutInflater.from(parent.getContext()).inflate(resId, null);

        return new HotMusicViewHolder(root);
    }

    @Override
    public void onBindViewHolder(final HotMusicViewHolder holder, int position) {

        if (getItemViewType(position) == TYPE_NORMAL) {
            MusicBean bean = mMusicList.get(position);

            holder.ivMusicImage.setImageResource(bean.getMusicImage());
            holder.tvMusicName.setText(bean.getMusicName());
            holder.tvMusicArtist.setText(bean.getMusicArtist());

            holder.itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onItemClickListener(holder.itemView, holder.getLayoutPosition());
                    }
                }
            });
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onViewAttachedToWindow(HotMusicViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (isStaggeredGridLayout(holder)) {
            handleLayoutIfStaggeredGridLayout(holder, holder.getLayoutPosition());
        }
    }


    private boolean isStaggeredGridLayout(RecyclerView.ViewHolder holder) {
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        if (layoutParams != null && layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
            return true;
        }
        return false;
    }

    protected void handleLayoutIfStaggeredGridLayout(RecyclerView.ViewHolder holder, int position) {
        if (position >= mMusicList.size()) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            p.setFullSpan(true);
        }
    }

    @Override
    public int getItemCount() {

        if (mFooterView != null) {
            return mMusicList.size() + 1;
        } else {
            return mMusicList.size();
        }
    }

    private OnItemClickInterface mListener;

    public void setOnItemClickListener(OnItemClickInterface listener) {
        this.mListener = listener;
    }

    public interface OnItemClickInterface {
        void onItemClickListener(View view, int position);
    }

    public class HotMusicViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMusicImage;

        TextView tvMusicName;

        TextView tvMusicArtist;

        public HotMusicViewHolder(View itemView) {
            super(itemView);

            if (itemView == mFooterView) {
                return;
            }

            ivMusicImage = (ImageView) itemView.findViewById(R.id.iv_music);

            tvMusicName = (TextView) itemView.findViewById(R.id.tv_music_name);

            tvMusicArtist = (TextView) itemView.findViewById(R.id.tv_music_artist);

        }
    }
}
