package com.jzby.jzbysounderclient.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jzby.jzbysounderclient.R;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;

import org.xutils.x;

import java.util.List;

/**
 * Created by gordan on 2018/3/7.
 */

public class RadioAdapter extends RecyclerView.Adapter<RadioAdapter.RadioViewHolder> {

    List<Radio> mRadioList;

    int resId;

    @Override
    public int getItemCount() {
        return mRadioList.size();
    }

    public RadioAdapter(List<Radio> radios, int resId) {
        super();
        this.mRadioList = radios;
        this.resId = resId;
    }

    public void notifyDataChanged(List<Radio> radios)
    {
        this.mRadioList=radios;
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final RadioViewHolder holder, int position) {
        Radio bean = mRadioList.get(position);

        x.image().bind(holder.ivCover, bean.getCoverUrlSmall());
        holder.tvRadioName.setText(bean.getRadioName());
        holder.tvRadioDesc.setText(bean.getProgramName());


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
    public RadioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View root = LayoutInflater.from(parent.getContext()).inflate(resId,parent,false);

        root.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view)
            {

            }
        });
        return new RadioViewHolder(root);
    }

    private OnItemClickInterface mListener;

    public void setOnItemClickListener(OnItemClickInterface listener) {
        this.mListener = listener;
    }

    public interface OnItemClickInterface {
        void onItemClickListener(View view, int position);
    }

    public class RadioViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvRadioName;
        TextView tvRadioDesc;

        public RadioViewHolder(View itemView) {
            super(itemView);

            ivCover = (ImageView) itemView.findViewById(R.id.iv_radio_cover);
            tvRadioName = (TextView) itemView.findViewById(R.id.tv_radio_name);
            tvRadioDesc = (TextView) itemView.findViewById(R.id.tv_radio_tag);
        }
    }
}
