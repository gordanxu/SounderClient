package com.jzby.jzbysounderclient.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jzby.jzbysounderclient.R;
import com.jzby.jzbysounderclient.view.CircleImageView;
import com.ximalaya.ting.android.opensdk.model.category.Category;

import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.util.List;

/**
 * Created by gordan on 2018/3/21.
 */

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>
{
    List<Category> mCategories;

    int mResId;

    public CategoryAdapter(List<Category> data,int resId)
    {
        mCategories=data;
        mResId=resId;
    }

    @Override
    public void onBindViewHolder(final CategoryViewHolder holder,int position)
    {
        Category category=mCategories.get(position);
        x.image().bind(holder.circleImageView,category.getCoverUrlMiddle(),new ImageOptions.Builder()
                .setPlaceholderScaleType(ImageView.ScaleType.CENTER_CROP).build());
        holder.textView.setText(category.getCategoryName());
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
    public int getItemCount() {
        return mCategories.size()>=8?8:mCategories.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public CategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {

        View root= LayoutInflater.from(parent.getContext()).inflate(mResId,parent,false);
        return new CategoryViewHolder(root);
    }

    private OnItemClickInterface mListener;

    public void setOnItemClickListener(OnItemClickInterface listener) {
        this.mListener = listener;
    }

    public interface OnItemClickInterface {
        void onItemClickListener(View view, int position);
    }


    public class CategoryViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView circleImageView;
        TextView textView;

        public CategoryViewHolder(View itemView) {
            super(itemView);

            circleImageView=(CircleImageView)itemView.findViewById(R.id.civ_category_cover);

            textView=(TextView)itemView.findViewById(R.id.tv_category_name);

        }
    }

}
