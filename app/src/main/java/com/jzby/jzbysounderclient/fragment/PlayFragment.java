package com.jzby.jzbysounderclient.fragment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.jzby.jzbysounderclient.PlayActivity;
import com.jzby.jzbysounderclient.R;
import com.jzby.jzbysounderclient.util.ImageUtil;
import com.jzby.jzbysounderclient.view.CircleImageView;

import org.xutils.common.Callback;
import org.xutils.image.ImageOptions;
import org.xutils.x;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayFragment extends Fragment implements Callback.CacheCallback<Drawable>
{
    final static String TAG=PlayFragment.class.getSimpleName();

    PlayActivity activity;

    CircleImageView mCircleImageView;

    ObjectAnimator mRotateAnimation;

    int mCurrentStatus;

    String mCurrentUrl="";

    public PlayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG,"======onAttach()=======");
        activity=(PlayActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.i(TAG,"======onCreateView()=======");
        View root=inflater.inflate(R.layout.fragment_play, container, false);
        mCircleImageView=(CircleImageView)root.findViewById(R.id.cv_big_player);
        mRotateAnimation= ObjectAnimator.ofFloat(mCircleImageView, "rotation", 0f, 360.0f);
        mRotateAnimation.setDuration(3000);
        mRotateAnimation.setRepeatCount(-1);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.start();

        //mCurrentStatus=1;

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG,"======onResume()=======");
       /* if(mCircleImageView!=null)
        {
            setCircleImageViewStatus(1,activity.getCoverImageUrl());
        }*/
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i(TAG,"======setUserVisibleHint()======="+isVisibleToUser);
        if(activity!=null && isVisibleToUser && mCircleImageView!=null)
        {
            String url=activity.getCoverImageUrl();
            mCurrentStatus=activity.getPlayerStatus();
            setCircleImageViewStatus(mCurrentStatus,url);
        }
        else
        {
            Log.i(TAG,"======activity is null=======");
        }
    }

    public void setCircleImageViewStatus(int status, String url)
    {
        Log.i(TAG,status+"=============="+url);
        if(!TextUtils.isEmpty(url) && !mCurrentUrl.equalsIgnoreCase(url))
        {
            x.image().bind(mCircleImageView, url, new ImageOptions.Builder()
                    .setPlaceholderScaleType(ImageView.ScaleType.CENTER_CROP).build(),this);
            mCurrentUrl=url;
        }

        if(mRotateAnimation==null)
        {
            Log.i(TAG,"=======mRotateAnimation null=======");
            return;
        }

        if(mCurrentStatus!=status)
        {
            if(mCurrentStatus==1)
            {
                mRotateAnimation.pause();
            }

            if(status==1)
            {
                mRotateAnimation.resume();
            }
            else
            {
                //mRotateAnimation.pause();
            }
            mCurrentStatus=status;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG,"======onDestroyView()=======");
        mCircleImageView=null;
        mCurrentUrl="";
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG,"======onDetach()=======");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"======onDestroy()=======");
    }

    @Override
    public boolean onCache(Drawable drawable) {
        Log.i(TAG,"======onCache()=======");
        activity.setPlayerBg(drawable);
        return false;
    }

    @Override
    public void onCancelled(CancelledException e) {
        Log.i(TAG,"======onCancelled()=======");
    }

    @Override
    public void onError(Throwable throwable, boolean b) {
        Log.i(TAG,b+"======onError()======="+throwable.getMessage());
    }

    @Override
    public void onFinished() {
        Log.i(TAG,"======onFinished()=======");
    }

    @Override
    public void onSuccess(Drawable drawable) {
        Log.i(TAG,"======onSuccess()=======");
        activity.setPlayerBg(drawable);
    }
}
