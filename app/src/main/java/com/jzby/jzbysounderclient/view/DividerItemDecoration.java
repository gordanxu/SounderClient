package com.jzby.jzbysounderclient.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.jzby.jzbysounderclient.R;

/**
 * Created by gordan on 2018/3/8.
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    final static String TAG = DividerItemDecoration.class.getSimpleName();

    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private Drawable mDivider;

    private int mOrientation;

    public DividerItemDecoration(Context context, int orientation) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        //mDivider = a.getDrawable(0);
        mDivider = new ColorDrawable(context.getResources().getColor(R.color.menu_selected));
        a.recycle();
        setOrientation(orientation);
    }

    public void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }


    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

        Log.i(TAG, "=====onDraw(2)=====");
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    public void drawVertical(Canvas c, RecyclerView parent) {
         int left = parent.getPaddingLeft();
         int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();

        Log.i(TAG, "=====drawVertical()=====" + childCount);
        for (int i = 0; i < childCount-1; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
             int top = child.getBottom() + params.bottomMargin;
             //int bottom = top + mDivider.getIntrinsicHeight();
            int bottom = top + 10;
            //Log.i(TAG,"===xpz=========="+mDivider.getIntrinsicHeight());
            Log.i(TAG,top+"====="+right+"====xpz===="+bottom+ "======="+left);
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);

            Log.i(TAG,i+"=====drawVertical()=====");
        }
    }

    public void drawHorizontal(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        if (mOrientation == VERTICAL_LIST) {
            Log.i(TAG,"====vertical=====");
            Log.i(TAG,"===xpz=========="+mDivider.getIntrinsicHeight());
            //outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
            outRect.set(0, 0, 0, 10);
        } else {
            Log.i(TAG,"====horizontal=====");
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
    }
}
