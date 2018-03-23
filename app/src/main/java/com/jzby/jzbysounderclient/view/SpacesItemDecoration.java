package com.jzby.jzbysounderclient.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by gordan on 2018/3/6.
 */

public class SpacesItemDecoration extends RecyclerView.ItemDecoration
{
    int space;

    public SpacesItemDecoration(int space) {
        super();
        this.space=space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left=space;
        outRect.right=space;
        outRect.bottom=space;
        if(parent.getChildAdapterPosition(view)==0){
            outRect.top=space;
        }
    }
}
