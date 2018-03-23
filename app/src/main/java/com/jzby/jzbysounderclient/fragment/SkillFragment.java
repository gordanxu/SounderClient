package com.jzby.jzbysounderclient.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.jzby.jzbysounderclient.MainActivity;
import com.jzby.jzbysounderclient.R;
import com.jzby.jzbysounderclient.view.PtrClassicHeader;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

/**
 * A simple {@link Fragment} subclass.
 */
public class SkillFragment extends LazyFragment {

    MainActivity activity;

    PtrFrameLayout mPtrFrameLayout=null;

    public SkillFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity=(MainActivity)getActivity();
    }

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_skill);

        mPtrFrameLayout=(PtrFrameLayout)findViewById(R.id.pfl_skill);

        PtrClassicHeader header=new PtrClassicHeader(activity);

        mPtrFrameLayout.setHeaderView(header);
        mPtrFrameLayout.addPtrUIHandler(header);
        mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mPtrFrameLayout.refreshComplete();

            }
        });
    }


    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
    }
}
