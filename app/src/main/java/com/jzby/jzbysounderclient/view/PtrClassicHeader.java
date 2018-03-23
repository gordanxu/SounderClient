package com.jzby.jzbysounderclient.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jzby.jzbysounderclient.R;

import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrUIHandler;
import in.srain.cube.views.ptr.indicator.PtrIndicator;

/**
 * Created by gordan on 2018/3/5.
 */

public class PtrClassicHeader extends FrameLayout implements PtrUIHandler {

    ImageView ivLoading = null;

    public PtrClassicHeader(Context context) {
        super(context);
        initView();
    }

    public PtrClassicHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PtrClassicHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {

        View header = LayoutInflater.from(getContext()).inflate(R.layout.item_push_header_layout, this);
        ivLoading = (ImageView) header.findViewById(R.id.iv_loading);
    }

    @Override
    public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status, PtrIndicator ptrIndicator) {
        final int mOffsetToRefresh = frame.getOffsetToRefresh();
        final int currentPos = ptrIndicator.getCurrentPosY();  //获取到下拉的高度
        final int lastPos = ptrIndicator.getLastPosY();      //最大下拉的高度
        //根据下拉的位置进行控件的显示
        if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh) {
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE) {
                crossRotateLineFromBottomUnderTouch(frame); //调用方法
            }
        } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh) {
            if (isUnderTouch && status == PtrFrameLayout.PTR_STATUS_PREPARE) {
                crossRotateLineFromTopUnderTouch(frame);  //调用方法
            }
        }
    }

    @Override
    public void onUIRefreshBegin(PtrFrameLayout frame) {

    }

    @Override
    public void onUIRefreshComplete(PtrFrameLayout frame) {


    }

    @Override
    public void onUIRefreshPrepare(PtrFrameLayout frame) {

        initAnim();
    }

    @Override
    public void onUIReset(PtrFrameLayout frame) {

    }

    public void initAnim() {
        ObjectAnimator anim = ObjectAnimator.ofFloat(ivLoading, "rotation", 0f, 180f);
        anim.setDuration(400);
        anim.start();
    }


    //下拉到可以刷新时显示
    private void crossRotateLineFromTopUnderTouch(PtrFrameLayout frame) {
        if (!frame.isPullToRefresh()) {
            //mTitleTextView.setText("释放刷新");
        }
    }

    //动态改变文字
    private void crossRotateLineFromBottomUnderTouch(PtrFrameLayout frame) {
        if (frame.isPullToRefresh()) {
            // mTitleTextView.setText("释放刷新");
        } else {
            // mTitleTextView.setText("下拉加载");
        }
    }

}
