package com.jzby.jzbysounderclient;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;

import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jzby.jzbysounderclient.adapter.ClientPagerAdapter;
import com.jzby.jzbysounderclient.fragment.FavoriteFragment;
import com.jzby.jzbysounderclient.fragment.FoundFragment;
import com.jzby.jzbysounderclient.fragment.MeFragment;
import com.jzby.jzbysounderclient.fragment.SkillFragment;
import com.jzby.jzbysounderclient.util.Constant;
import com.jzby.jzbysounderclient.view.CircleImageView;
import com.jzby.jzbysounderclient.view.MyViewPager;

import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.category.Category;
import com.ximalaya.ting.android.opensdk.model.category.CategoryList;

import org.xutils.image.ImageOptions;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends FragmentActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    final static String TAG = MainActivity.class.getSimpleName();

    PlayerBroadcastReceiver mPlayerBroadcastReceiver;

    ClientService mClientService;

    MyViewPager mViewPager;

    ClientPagerAdapter mClientPagerAdapter;

    List<Fragment> mPagerFragment;

    CircleImageView mCircleImageView;

    ObjectAnimator mRotateAnimation;

    LinearLayout mTitleLinearLayout;

    ImageView mImageViewSearch,ivPlayerCover,iv_found, iv_skill, iv_favorite, iv_me;

    TextView mTitleTextView,tv_found, tv_skill, tv_favorite, tv_me;

    int currentStatus;

    private String currentUrl="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        //透明状态栏
        //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP) {
            //避免状态栏和内容重叠
            getWindow().getDecorView().setFitsSystemWindows(true);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            //透明导航栏
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        else
        {
            ViewGroup mContentView = (ViewGroup) findViewById(Window.ID_ANDROID_CONTENT);
            int statusBarHeight = getStatusBarHeight();
            Log.i(TAG, "=====statusBarHeight=====" + statusBarHeight);
            View mChildView = mContentView.getChildAt(0);
            if (mChildView != null) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mChildView.getLayoutParams();
                //如果已经为 ChildView 设置过了 marginTop, 再次调用时直接跳过
                if (lp != null && lp.topMargin < statusBarHeight && lp.height != statusBarHeight) {
                    //不预留系统空间
                    ViewCompat.setFitsSystemWindows(mChildView, false);
                    lp.topMargin += statusBarHeight;
                    mChildView.setLayoutParams(lp);
                }
            }

            View statusBarView = mContentView.getChildAt(0);
            if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == statusBarHeight) {
                //避免重复调用时多次添加 View
                statusBarView.setBackgroundColor(getResources().getColor(R.color.menu_selected));
                return;
            }
            statusBarView = new View(this);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
            statusBarView.setBackgroundColor(getResources().getColor(R.color.menu_selected));
            //向 ContentView 中添加假 View
            mContentView.addView(statusBarView, 0, lp);
        }
        setContentView(R.layout.activity_main);

        Intent intent=new Intent(this,ClientService.class);
        bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);

        mPlayerBroadcastReceiver = new PlayerBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Constant.FLAG_BROADCAST_PLAYER);
        registerReceiver(mPlayerBroadcastReceiver, filter);

        initData();

        mImageViewSearch=(ImageView)findViewById(R.id.iv_search);
        ivPlayerCover=(ImageView)findViewById(R.id.iv_player_cover);
        mTitleLinearLayout=(LinearLayout)findViewById(R.id.ll_title);
        mTitleTextView=(TextView)findViewById(R.id.tv_title);
        mViewPager = (MyViewPager) findViewById(R.id.vp_client);
        mCircleImageView = (CircleImageView) findViewById(R.id.cv_player);
        iv_found = (ImageView) findViewById(R.id.iv_menu_found);
        iv_skill = (ImageView) findViewById(R.id.iv_menu_skill);
        iv_favorite = (ImageView) findViewById(R.id.iv_menu_favorite);
        iv_me = (ImageView) findViewById(R.id.iv_menu_me);

        tv_found = (TextView) findViewById(R.id.tv_menu_found);
        tv_skill = (TextView) findViewById(R.id.tv_menu_skill);
        tv_favorite = (TextView) findViewById(R.id.tv_menu_favorite);
        tv_me = (TextView) findViewById(R.id.tv_menu_me);

        findViewById(R.id.fl_player).setOnClickListener(this);
        findViewById(R.id.ll_menu_found).setOnClickListener(this);
        findViewById(R.id.ll_menu_skill).setOnClickListener(this);
        findViewById(R.id.ll_menu_favorite).setOnClickListener(this);
        findViewById(R.id.ll_menu_me).setOnClickListener(this);

        mRotateAnimation= ObjectAnimator.ofFloat(mCircleImageView, "rotation", 0f,360.0f);
        mRotateAnimation.setDuration(3000);
        mRotateAnimation.setRepeatCount(-1);
        mRotateAnimation.setRepeatMode(ValueAnimator.RESTART);
        mRotateAnimation.setInterpolator(new LinearInterpolator());
        mRotateAnimation.start();
        currentStatus=1;

        initMainMenu(0);

        initViewPager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "=====onResume()=======");
        mHandler.sendEmptyMessage(Constant.FLAG_REFRESH_DATA);
    }

    Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message)
        {
            if(mClientService==null)
            {
                Log.i(TAG, "=====mClientService is null=======");
                return false;
            }

            if(mClientService.getCurrentCategory()==Constant.CATEGORY_TRACK)
            {
                Log.i(TAG,"====track=====");
                if(mClientService.getCurrentTrack()!=null)
                {
                    if(!currentUrl.equalsIgnoreCase(mClientService.getCurrentTrack().getCoverUrlSmall()))
                    {
                        Log.i(TAG,"====set=====");
                        x.image().bind(mCircleImageView,mClientService.getCurrentTrack().getCoverUrlSmall(),new ImageOptions.Builder()
                                .setPlaceholderScaleType(ImageView.ScaleType.CENTER_CROP).build());
                        currentUrl=mClientService.getCurrentTrack().getCoverUrlSmall();
                    }
                    else
                    {
                        Log.i(TAG,"====the same=====");
                    }
                }
                else
                {
                    Log.i(TAG,"====track is null=====");
                    mHandler.sendEmptyMessageDelayed(Constant.FLAG_REFRESH_DATA,1000);
                }
            }
            else if(mClientService.getCurrentCategory()==Constant.CATEGORY_RADIO)
            {
                Log.i(TAG,"====radio=====");
                if(mClientService.getCurrentRadio()!=null)
                {
                    if(!currentUrl.equalsIgnoreCase(mClientService.getCurrentRadio().getCoverUrlLarge()))
                    {
                        x.image().bind(mCircleImageView,mClientService.getCurrentRadio().getCoverUrlLarge(),new ImageOptions.Builder()
                                .setPlaceholderScaleType(ImageView.ScaleType.CENTER_CROP).build());
                        currentUrl=mClientService.getCurrentRadio().getCoverUrlLarge();
                    }
                }
            }
            else
            {
                //return false;
            }

            int status=mClientService.getPlayerStatus();
            Log.i(TAG,currentStatus+"====status====="+status);
            if(status!=currentStatus)
            {
                if(currentStatus==1)
                {
                    mRotateAnimation.pause();
                    ivPlayerCover.setVisibility(View.VISIBLE);
                }

                if(status == 1)
                {
                    mRotateAnimation.resume();
                    ivPlayerCover.setVisibility(View.INVISIBLE);
                }
                else
                {

                }
                currentStatus=status;
            }
            return false;
        }
    });

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "=====onServiceConnected()=======");
            mClientService = ((ClientService.ClientBinder) iBinder).getService();
            mHandler.sendEmptyMessage(Constant.FLAG_REFRESH_DATA);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "=====onServiceDisconnected()=======");
            mClientService = null;
        }
    };


    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return getResources().getDimensionPixelSize(resourceId);
    }

    private void initViewPager() {

        mPagerFragment = new ArrayList<Fragment>();

        FoundFragment foundFragment = new FoundFragment();
        mPagerFragment.add(foundFragment);

        SkillFragment skillFragment = new SkillFragment();
        mPagerFragment.add(skillFragment);

        FavoriteFragment favoriteFragment = new FavoriteFragment();
        mPagerFragment.add(favoriteFragment);

        MeFragment meFragment = new MeFragment();
        mPagerFragment.add(meFragment);

        mClientPagerAdapter = new ClientPagerAdapter(getSupportFragmentManager(), mPagerFragment);

        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mClientPagerAdapter);
        mViewPager.addOnPageChangeListener(this);
    }

    private void initMainMenu(int position) {

        switch (position) {
            case 0:

                mTitleTextView.setText("发现");
                if(mTitleLinearLayout.getVisibility()!=View.VISIBLE)
                {
                    mTitleLinearLayout.setVisibility(View.VISIBLE);
                }

                if(mImageViewSearch.getVisibility()!=View.VISIBLE)
                {
                    mImageViewSearch.setVisibility(View.VISIBLE);
                }
                iv_found.setImageResource(R.drawable.menu_found_selected);
                tv_found.setTextColor(getResources().getColor(R.color.menu_selected));

                iv_skill.setImageResource(R.drawable.menu_skill_unselected);
                tv_skill.setTextColor(getResources().getColor(R.color.menu_unselected));

                iv_favorite.setImageResource(R.drawable.menu_fav_unselected);
                tv_favorite.setTextColor(getResources().getColor(R.color.menu_unselected));

                iv_me.setImageResource(R.drawable.menu_me_unselected);
                tv_me.setTextColor(getResources().getColor(R.color.menu_unselected));
                break;
            case 1:
                mTitleTextView.setText("技能");
                if(mImageViewSearch.getVisibility()==View.VISIBLE)
                {
                    mImageViewSearch.setVisibility(View.GONE);
                }
                if(mTitleLinearLayout.getVisibility()!=View.VISIBLE)
                {
                    mTitleLinearLayout.setVisibility(View.VISIBLE);
                }
                iv_found.setImageResource(R.drawable.menu_found_unselected);
                tv_found.setTextColor(getResources().getColor(R.color.menu_unselected));

                iv_skill.setImageResource(R.drawable.menu_skill_selected);
                tv_skill.setTextColor(getResources().getColor(R.color.menu_selected));

                iv_favorite.setImageResource(R.drawable.menu_fav_unselected);
                tv_favorite.setTextColor(getResources().getColor(R.color.menu_unselected));

                iv_me.setImageResource(R.drawable.menu_me_unselected);
                tv_me.setTextColor(getResources().getColor(R.color.menu_unselected));

                break;
            case 2:
                mTitleTextView.setText("喜欢");
                if(mImageViewSearch.getVisibility()==View.VISIBLE)
                {
                    mImageViewSearch.setVisibility(View.GONE);
                }
                if(mTitleLinearLayout.getVisibility()!=View.VISIBLE)
                {
                    mTitleLinearLayout.setVisibility(View.VISIBLE);
                }
                iv_found.setImageResource(R.drawable.menu_found_unselected);
                tv_found.setTextColor(getResources().getColor(R.color.menu_unselected));

                iv_skill.setImageResource(R.drawable.menu_skill_unselected);
                tv_skill.setTextColor(getResources().getColor(R.color.menu_unselected));

                iv_favorite.setImageResource(R.drawable.menu_fav_selected);
                tv_favorite.setTextColor(getResources().getColor(R.color.menu_selected));

                iv_me.setImageResource(R.drawable.menu_me_unselected);
                tv_me.setTextColor(getResources().getColor(R.color.menu_unselected));

                break;
            case 3:
                mTitleLinearLayout.setVisibility(View.GONE);
                iv_found.setImageResource(R.drawable.menu_found_unselected);
                tv_found.setTextColor(getResources().getColor(R.color.menu_unselected));

                iv_skill.setImageResource(R.drawable.menu_skill_unselected);
                tv_skill.setTextColor(getResources().getColor(R.color.menu_unselected));

                iv_favorite.setImageResource(R.drawable.menu_fav_unselected);
                tv_favorite.setTextColor(getResources().getColor(R.color.menu_unselected));

                iv_me.setImageResource(R.drawable.menu_me_selected);
                tv_me.setTextColor(getResources().getColor(R.color.menu_selected));
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fl_player:
                Intent intent = new Intent(this, PlayActivity.class);
                startActivity(intent);
                break;

            case R.id.ll_menu_found:

                initMainMenu(0);
                mViewPager.setCurrentItem(0);

                break;

            case R.id.ll_menu_me:
                initMainMenu(3);
                mViewPager.setCurrentItem(3);
                break;

            case R.id.ll_menu_favorite:
                initMainMenu(2);
                mViewPager.setCurrentItem(2);
                break;

            case R.id.ll_menu_skill:
                initMainMenu(1);
                mViewPager.setCurrentItem(1);
                break;
        }
    }

    List<Category> mCategory;

    public List<Category> getXiMaLaYaCategory()
    {
        return mCategory;
    }

    public Category getCategory(int position)
    {
        if(mCategory!=null && mCategory.size()>0 && (position<mCategory.size()))
        {
            return mCategory.get(position);
        }
        return null;
    }

    private void initData()
    {
        CommonRequest.getCategories(null, new IDataCallBack<CategoryList>() {

            @Override
            public void onSuccess(@Nullable CategoryList categoryList)
            {
                if(categoryList!=null &&categoryList.getCategories()!=null && categoryList.getCategories().size()>0)
                {
                    mCategory=categoryList.getCategories();
                    for (int i=0;i<categoryList.getCategories().size();i++)
                    {
                        Category category=categoryList.getCategories().get(i);
                        Log.i(TAG,category.getId()+"======="+category.getCategoryName());
                    }
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.i(TAG,i+"====onError()===="+s);
            }
        });
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.i(TAG, "======onPageSelected()========" + position);
    }

    class PlayerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "=======" + action);
            if(Constant.FLAG_BROADCAST_PLAYER.equalsIgnoreCase(action))
            {
                mHandler.sendEmptyMessage(Constant.FLAG_REFRESH_DATA);
            }
        }
    }
}
