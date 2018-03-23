package com.jzby.jzbysounderclient.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jzby.jzbysounderclient.AlbumListActivity;
import com.jzby.jzbysounderclient.MainActivity;
import com.jzby.jzbysounderclient.R;
import com.jzby.jzbysounderclient.RadioActivity;
import com.jzby.jzbysounderclient.adapter.BannerPagerAdapter;
import com.jzby.jzbysounderclient.adapter.CategoryAdapter;
import com.jzby.jzbysounderclient.adapter.HotMusicAdapter;
import com.jzby.jzbysounderclient.adapter.RecommendArtistAdapter;
import com.jzby.jzbysounderclient.bean.ArtistBean;
import com.jzby.jzbysounderclient.bean.MusicBean;
import com.jzby.jzbysounderclient.view.MyRecycleViewScrollListener;
import com.jzby.jzbysounderclient.view.PtrClassicHeader;
import com.jzby.jzbysounderclient.view.SpacesItemDecoration;

import java.util.ArrayList;
import java.util.List;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

import static com.jzby.jzbysounderclient.util.Constant.FLAG_SWITCH_BANNER;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_UPDATE_INDICATE;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_UPDATE_RECYCLE_VIEW;

/**
 * A simple {@link Fragment} subclass.
 */
public class FoundFragment extends LazyFragment implements ViewPager.OnPageChangeListener,
        View.OnClickListener,CategoryAdapter.OnItemClickInterface
{
    final static String TAG = FoundFragment.class.getSimpleName();
    MainActivity activity;

    RecyclerView mCategoryRecyclerView;

    CategoryAdapter mCategoryAdapter;

    ViewPager mBannerViewPager;

    PtrFrameLayout mPtrFrameLayout;

    LinearLayout mIndicateView;

    GridView mRecommendArtist;

    RecommendArtistAdapter mArtistAdapter;

    RecyclerView mHotSongRecyclerView;

    BannerPagerAdapter mBannerPagerAdapter;

    List<View> mBannerViews;

    List<ArtistBean> mArtistBeans;

    List<MusicBean> mMusicList;

    HotMusicAdapter mHotMusicAdapter;

    StaggeredGridLayoutManager layoutManager;

    int mPosition = 0;

    public FoundFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {


                case FLAG_UPDATE_RECYCLE_VIEW:

                    mHotMusicAdapter.notifyDataChanged(mMusicList);

                    mRecycleViewScrollListener.setLoadingMore(false);
                    break;

                case FLAG_UPDATE_INDICATE:

                    Log.i(TAG, "====FLAG_UPDATE_INDICATE=======");
                    updateBannerIndicate(mPosition);
                    break;

                case FLAG_SWITCH_BANNER:
                    mPosition++;
                    if (mPosition > 3) {
                        mPosition = 0;
                    }
                    mBannerViewPager.setCurrentItem(mPosition);

                    mHandler.sendEmptyMessageDelayed(FLAG_SWITCH_BANNER, 5000);
                    break;
            }

            return false;
        }
    });

    @Override
    protected void onCreateViewLazy(Bundle savedInstanceState) {
        super.onCreateViewLazy(savedInstanceState);
        setContentView(R.layout.fragment_found);
        mPtrFrameLayout=(PtrFrameLayout) findViewById(R.id.pfl_found);
        mBannerViewPager = (ViewPager) findViewById(R.id.vp_banner);
        mIndicateView = (LinearLayout) findViewById(R.id.ll_indicate);
        mRecommendArtist=(GridView)findViewById(R.id.gv_artist);
        mHotSongRecyclerView =(RecyclerView)findViewById(R.id.rv_hot_song);
        mCategoryRecyclerView=(RecyclerView)findViewById(R.id.rv_category);

        findViewById(R.id.iv_category_more).setOnClickListener(this);

        layoutManager=new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        layoutManager.getChildCount();
        mHotSongRecyclerView.setLayoutManager(layoutManager);

        PtrClassicHeader header = new PtrClassicHeader(activity);
        mPtrFrameLayout.addPtrUIHandler(header);
        mPtrFrameLayout.setHeaderView(header);
        mPtrFrameLayout.setPtrHandler(new PtrHandler() {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mPtrFrameLayout.refreshComplete();
            }

            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }
        });

        initCategory();

        initBannerView();

        initRecommendArtist();

        initHotMusic();
    }

    private void initCategory()
    {
        if(activity.getXiMaLaYaCategory()!=null && activity.getXiMaLaYaCategory().size()>0)
        {
            mCategoryRecyclerView.setLayoutManager(new GridLayoutManager(activity,4));
            mCategoryAdapter=new CategoryAdapter(activity.getXiMaLaYaCategory(),R.layout.item_category);
            mCategoryAdapter.setOnItemClickListener(this);
            mCategoryRecyclerView.setAdapter(mCategoryAdapter);
        }
        else
        {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    initCategory();
                }
            },3000);
        }

    }

    private void initRecommendArtist()
    {
        mArtistBeans=new ArrayList<ArtistBean>();

        for (int i=0;i<3;i++)
        {
            ArtistBean bean=new ArtistBean();
            bean.setArtImage(R.drawable.album_default);
            bean.setArtName("庄心妍_"+i);
            mArtistBeans.add(bean);
        }

        mArtistAdapter=new RecommendArtistAdapter(activity,mArtistBeans,R.layout.item_artist);
        mRecommendArtist.setAdapter(mArtistAdapter);
    }

    private MyRecycleViewScrollListener mRecycleViewScrollListener=new MyRecycleViewScrollListener() {
        @Override
        public void onLoadMore()
        {
            loadMore();
        }
    };

    private void initHotMusic()
    {
        mMusicList=new ArrayList<MusicBean>();

        for (int i=0;i<10;i++)
        {
            MusicBean bean=new MusicBean();
            bean.setMusicArtist("吴若曦");
            bean.setMusicName("越难越爱");
            if(i%3==0)
            {
                bean.setMusicImage(R.drawable.album_2);
            }
            else if(i%3==1)
            {
                bean.setMusicImage(R.drawable.artist_default);
            }
            else
            {
                bean.setMusicImage(R.drawable.album_default);
            }
            mMusicList.add(bean);
        }

        View header=LayoutInflater.from(activity).inflate(R.layout.item_push_header_layout,null);
        mHotMusicAdapter=new HotMusicAdapter(mMusicList,R.layout.item_hot_music);
        mHotMusicAdapter.setFooterView(header);
        mHotSongRecyclerView.setAdapter(mHotMusicAdapter);
        mHotSongRecyclerView.addOnScrollListener(mRecycleViewScrollListener);
        mHotSongRecyclerView.addItemDecoration(new SpacesItemDecoration(15));
    }

    private void loadMore()
    {
        for (int i=0;i<10;i++)
        {
            MusicBean bean=new MusicBean();
            bean.setMusicArtist("刘德华");
            bean.setMusicName("恭喜发财_"+i);
            if(i%3==0)
            {
                bean.setMusicImage(R.drawable.album_2);
            }
            else if(i%3==1)
            {
                bean.setMusicImage(R.drawable.artist_default);
            }
            else
            {
                bean.setMusicImage(R.drawable.album_default);
            }
            mMusicList.add(bean);
        }
        mHandler.sendEmptyMessage(FLAG_UPDATE_RECYCLE_VIEW);
    }

    private void updateBannerIndicate(int position) {
        // ImageView imageView=(ImageView) mIndicateView.getChildAt(position);
        ImageView imageView = (ImageView) mIndicateView.findViewById(R.id.ll_iv_indicate_1);
        ImageView imageView_1 = (ImageView) mIndicateView.findViewById(R.id.ll_iv_indicate_2);
        ImageView imageView_2 = (ImageView) mIndicateView.findViewById(R.id.ll_iv_indicate_3);
        ImageView imageView_3 = (ImageView) mIndicateView.findViewById(R.id.ll_iv_indicate_4);

        switch (position) {
            case 0:
                imageView.setImageResource(R.drawable.icon_point_selected);
                imageView_1.setImageResource(R.drawable.icon_point_unselected);
                imageView_2.setImageResource(R.drawable.icon_point_unselected);
                imageView_3.setImageResource(R.drawable.icon_point_unselected);
                break;
            case 1:
                imageView.setImageResource(R.drawable.icon_point_unselected);
                imageView_1.setImageResource(R.drawable.icon_point_selected);
                imageView_2.setImageResource(R.drawable.icon_point_unselected);
                imageView_3.setImageResource(R.drawable.icon_point_unselected);
                break;
            case 2:
                imageView.setImageResource(R.drawable.icon_point_unselected);
                imageView_1.setImageResource(R.drawable.icon_point_unselected);
                imageView_2.setImageResource(R.drawable.icon_point_selected);
                imageView_3.setImageResource(R.drawable.icon_point_unselected);
                break;
            case 3:
                imageView.setImageResource(R.drawable.icon_point_unselected);
                imageView_1.setImageResource(R.drawable.icon_point_unselected);
                imageView_2.setImageResource(R.drawable.icon_point_unselected);
                imageView_3.setImageResource(R.drawable.icon_point_selected);
                break;
        }
    }

    private void initBannerView() {
        if (activity == null) {
            return;
        }

        mBannerViews = new ArrayList<View>();

        View banner_view = LayoutInflater.from(activity).inflate(R.layout.item_banner, null);
        ImageView iv_banner = (ImageView) banner_view.findViewById(R.id.iv_banner_content);
        iv_banner.setImageResource(R.drawable.banner_1);
        mBannerViews.add(banner_view);

        banner_view = LayoutInflater.from(activity).inflate(R.layout.item_banner, null);
        iv_banner = (ImageView) banner_view.findViewById(R.id.iv_banner_content);
        iv_banner.setImageResource(R.drawable.banner_2);
        mBannerViews.add(banner_view);

        banner_view = LayoutInflater.from(activity).inflate(R.layout.item_banner, null);
        iv_banner = (ImageView) banner_view.findViewById(R.id.iv_banner_content);
        iv_banner.setImageResource(R.drawable.banner_3);
        mBannerViews.add(banner_view);

        banner_view = LayoutInflater.from(activity).inflate(R.layout.item_banner, null);
        iv_banner = (ImageView) banner_view.findViewById(R.id.iv_banner_content);
        iv_banner.setImageResource(R.drawable.banner_4);
        mBannerViews.add(banner_view);

        mBannerPagerAdapter = new BannerPagerAdapter(mBannerViews);
        mBannerViewPager.setAdapter(mBannerPagerAdapter);
        mBannerViewPager.addOnPageChangeListener(this);
        mBannerViewPager.setCurrentItem(mPosition);

        mHandler.sendEmptyMessageDelayed(FLAG_SWITCH_BANNER, 5000);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.iv_category_more:
                Intent intent=new Intent(activity,RadioActivity.class);
                startActivity(intent);

                break;

            default:

                break;
        }
    }

    @Override
    protected void onDestroyViewLazy() {
        super.onDestroyViewLazy();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {
        Log.i(TAG, "====onPageSelected====" + position);
        mPosition = position;
        mHandler.sendEmptyMessage(FLAG_UPDATE_INDICATE);

    }

    @Override
    public void onItemClickListener(View view, int position)
    {
        Log.i(TAG, "====onItemClickListener====" + position);
        if(activity.getCategory(position)==null)
        {
            Log.i(TAG, "====has no category====");
            return;
        }
        Intent tent=new Intent(activity, AlbumListActivity.class);
        tent.putExtra("categoryId",""+activity.getCategory(position).getId());
        tent.putExtra("categoryTitle",activity.getCategory(position).getCategoryName());
        startActivity(tent);
    }
}
