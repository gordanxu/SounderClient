package com.jzby.jzbysounderclient.view;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * Created by gordan on 2018/3/6.
 */

public abstract class MyRecycleViewScrollListener extends RecyclerView.OnScrollListener
{
    private int mLayoutManagerType=0;
    private int[] lastPositions;
    private int lastVisibleItemPosition;
    private int currentScrollState = 0;

    private boolean mIsLoadingMore = false;
    public boolean isLoadingMore() {
        return mIsLoadingMore;
    }
    public void setLoadingMore(boolean loadingMore) {
        mIsLoadingMore = loadingMore;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            mLayoutManagerType = 0;
        } else if (layoutManager instanceof GridLayoutManager) {
            mLayoutManagerType = 1;
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            mLayoutManagerType = 2;
        } else {
            throw new RuntimeException("Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
        }

        switch (mLayoutManagerType) {
            case 0:
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case 1:
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case 2:
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                if (lastPositions == null) {
                    lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                }
                staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                lastVisibleItemPosition = findMax(lastPositions);
                break;
            default:
                break;
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        currentScrollState = newState;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        //防止 ITEM错位
        if(layoutManager instanceof StaggeredGridLayoutManager)
        {
            ((StaggeredGridLayoutManager) layoutManager).invalidateSpanAssignments();
        }

        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        if (visibleItemCount > 0 && currentScrollState == RecyclerView.SCROLL_STATE_IDLE
                && lastVisibleItemPosition >= totalItemCount - 1) {
            if (!isLoadingMore()){
                mIsLoadingMore =true;
                onLoadMore();
            }
        }
    }

    private int findMax(int[] lastPositions) {
        int max = lastPositions[0];
        for (int value : lastPositions) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public abstract void onLoadMore();
}
