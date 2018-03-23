package com.jzby.jzbysounderclient.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.jzby.jzbysounderclient.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeFragment extends LazyFragment {


    public MeFragment() {
        // Required empty public constructor
    }


    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_me);


    }

    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
    }
}
