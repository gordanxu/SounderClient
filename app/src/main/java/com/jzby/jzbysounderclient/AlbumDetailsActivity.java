package com.jzby.jzbysounderclient;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jzby.jzbysounderclient.adapter.AlbumAdapter;
import com.jzby.jzbysounderclient.bean.ElementBean;
import com.jzby.jzbysounderclient.connect.ConnectClientUtil;
import com.jzby.jzbysounderclient.util.Constant;
import com.jzby.jzbysounderclient.util.ImageUtil;
import com.jzby.jzbysounderclient.view.DividerItemDecoration;
import com.jzby.jzbysounderclient.view.LinearLayoutColorDivider;
import com.jzby.jzbysounderclient.view.MyRecycleViewScrollListener;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Announcer;
import com.ximalaya.ting.android.opensdk.model.track.Track;
import com.ximalaya.ting.android.opensdk.model.track.TrackList;

import org.xutils.common.Callback;
import org.xutils.x;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jzby.jzbysounderclient.util.Constant.FLAG_REFRESH_ALBUM_COVER;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_REFRESH_DATA;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_SEND_MESSAGE;

public class AlbumDetailsActivity extends BaseActivity implements AlbumAdapter.OnItemClickInterface,
        ConnectClientUtil.ResponseCallback,View.OnClickListener
{
    final static String TAG = AlbumDetailsActivity.class.getSimpleName();

    PlayerBroadcastReceiver mPlayerBroadcastReceiver;

    ClientService mClientService;

    RecyclerView mAlbumRecyclerView;

    AlbumAdapter mAlbumAdapter;

    List<Track> mTracks;

    String albumId="239463";

    int currentPage = 1,currentPosition=-1;

    ImageView mCoverImageView;

    TextView mAlbumNameTextView, mNameTextView;

    LinearLayout llAlbumBg;

    Bitmap mAlbumBgBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        //透明状态栏
        //window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP) {
            //window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            //window.setNavigationBarColor(Color.TRANSPARENT);
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
        setContentView(R.layout.activity_album_details);

        mPlayerBroadcastReceiver = new PlayerBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Constant.FLAG_BROADCAST_PLAYER);
        registerReceiver(mPlayerBroadcastReceiver,filter);

        Intent intent=new Intent(this,ClientService.class);
        bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);

        if(getIntent()!=null)
        {
            if(getIntent().hasExtra("albumId"))
            {
                albumId=""+getIntent().getLongExtra("albumId",0);
            }
        }

        findViewById(R.id.iv_back).setOnClickListener(this);
        mCoverImageView = (ImageView) findViewById(R.id.iv_album_cover);
        mAlbumNameTextView = (TextView) findViewById(R.id.tv_album_name);
        mNameTextView = (TextView) findViewById(R.id.tv_album_sub_name);
        llAlbumBg=(LinearLayout)findViewById(R.id.ll_album_bg);

        mAlbumRecyclerView = (RecyclerView) findViewById(R.id.rv_album);
        mAlbumRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DividerItemDecoration dividerItemDecoration=new DividerItemDecoration(this,DividerItemDecoration.VERTICAL_LIST);

        LinearLayoutColorDivider divider = new LinearLayoutColorDivider(this.getResources(),R.color.line,
                R.dimen.line,LinearLayoutManager.VERTICAL);

        mAlbumRecyclerView.addItemDecoration(divider);
        initData();
    }

    private int getStatusBarHeight() {
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return getResources().getDimensionPixelSize(resourceId);
    }

    ServiceConnection serviceConnection=new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            Log.i(TAG,"========onServiceConnected()=========");
            mClientService=((ClientService.ClientBinder)iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            Log.i(TAG,"========onServiceDisconnected()=========");
            mClientService=null;
        }
    };


    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            switch (message.what) {

                case FLAG_SEND_MESSAGE:
                    Log.i(TAG,"====FLAG_SEND_MESSAGE====");
                    currentPosition=message.arg1;
                    Track track= mTracks.get(currentPosition);
                    long albumId=-1;
                    if(track.getAlbum()!=null)
                    {
                        albumId=track.getAlbum().getAlbumId();
                    }
                    Log.i(TAG, track.getDataId()+"==========="+albumId);
                    ElementBean elementBean=new ElementBean();
                    elementBean.setAction("media");
                    elementBean.setType("ximalaya");
                    elementBean.setSlots("vod");

                    List<ElementBean> subBeans=new ArrayList<ElementBean>();

                    ElementBean e1=new ElementBean();
                    e1.setAction("playId");
                    e1.setAttrType("long");
                    e1.setAttrValue(track.getDataId()+"");
                    subBeans.add(e1);

                    if(albumId>0)
                    {
                        ElementBean e2=new ElementBean();
                        e2.setAction("albumId");
                        e2.setAttrType("long");
                        e2.setAttrValue(albumId+"");

                        subBeans.add(e2);
                    }

                    elementBean.setElements(subBeans);

                    Gson gson=new Gson();
                    String msg=gson.toJson(elementBean);

                    Log.i(TAG,"====send===="+msg);
                    mClientService.sendMessage(msg,AlbumDetailsActivity.this);

                    break;


                case FLAG_REFRESH_ALBUM_COVER:

                    if(mAlbumBgBitmap!=null)
                    {
                        llAlbumBg.setBackground(ImageUtil.BoxBlurFilter(mAlbumBgBitmap));
                    }



                    break;

                case FLAG_REFRESH_DATA:

                    if (mAlbumAdapter == null) {
                        View loading = LayoutInflater.from(AlbumDetailsActivity.this).inflate(R.layout.item_push_header_layout, null);

                        Announcer announcer = mTracks.get(0).getAnnouncer();

                        x.image().bind(mCoverImageView, mTracks.get(0).getCoverUrlLarge(), new Callback.CommonCallback<Drawable>() {
                            @Override
                            public void onSuccess(Drawable drawable) {

                                Log.i(TAG,"=====onSuccess()=======");
                                mAlbumBgBitmap= ImageUtil.drawableToBitmap(drawable);
                                mHandler.sendEmptyMessage(FLAG_REFRESH_ALBUM_COVER);
                            }

                            @Override
                            public void onError(Throwable throwable, boolean b) {
                                Log.i(TAG,"=====onError()======="+throwable.getMessage());
                            }

                            @Override
                            public void onCancelled(CancelledException e) {
                                Log.i(TAG,"=====onCancelled()======="+e.getMessage());
                            }

                            @Override
                            public void onFinished() {
                                Log.i(TAG,"=====onFinished()=======");
                            }
                        });

                        mAlbumNameTextView.setText(announcer.getNickname());
                        mNameTextView.setText("播放 " + announcer.getNickname());

                        mAlbumAdapter = new AlbumAdapter(mTracks, R.layout.item_album);
                        mAlbumAdapter.setOnItemClickListener(AlbumDetailsActivity.this);
                        mAlbumAdapter.setFooterResId(R.layout.item_push_header_layout);
                        mAlbumRecyclerView.setAdapter(mAlbumAdapter);

                        mAlbumRecyclerView.addOnScrollListener(myRecycleViewScrollListener);

                    } else {
                        mAlbumAdapter.notifyDataChanged(mTracks);
                        myRecycleViewScrollListener.setLoadingMore(false);
                        mAlbumAdapter.hiddenFooterView();
                    }
                    break;
                case Constant.FLAG_FETCH_DATA_ERROR:

                    showTextToast("obtain message error!!");
                    if(mAlbumAdapter!=null)
                    {
                        mAlbumAdapter.hiddenFooterView();
                        myRecycleViewScrollListener.setLoadingMore(false);
                    }
                    break;
                case Constant.FLAG_FETCH_DATA_MOST:
                    showTextToast("has no more data !!");
                    if(mAlbumAdapter!=null)
                    {
                        mAlbumAdapter.hiddenFooterView();
                        myRecycleViewScrollListener.setLoadingMore(false);
                    }
                    break;
            }
            return false;
        }
    });


    MyRecycleViewScrollListener myRecycleViewScrollListener = new MyRecycleViewScrollListener() {
        @Override
        public void onLoadMore() {
            currentPage++;
            initData();
        }
    };

    private void initData() {
        if (mTracks == null) {
            mTracks = new ArrayList<Track>();
        }

        Map<String, String> param = new HashMap<String, String>();
        param.put(DTransferConstants.PAGE, "" + currentPage);
        param.put(DTransferConstants.PAGE_SIZE, "10");
        param.put(DTransferConstants.ALBUM_ID, albumId);
        Log.i(TAG,"=======albumId====="+albumId);
        CommonRequest.getTracks(param, new IDataCallBack<TrackList>() {
            @Override
            public void onSuccess(TrackList trackList) {
                if (trackList != null && trackList.getTracks() != null && trackList.getTracks().size() != 0) {

                    for (int i = 0; i < trackList.getTracks().size(); i++) {
                        Track bean = trackList.getTracks().get(i);
                        mTracks.add(bean);
                        Log.i(TAG, i + "=========" + bean.getTrackTitle() + "====" + bean.getTrackTags() + "====" + bean.getRadioRate64AacUrl());
                    }

                    mHandler.sendEmptyMessage(FLAG_REFRESH_DATA);
                } else {
                    myRecycleViewScrollListener.setLoadingMore(false);
                    mHandler.sendEmptyMessage(Constant.FLAG_FETCH_DATA_MOST);
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.i(TAG, "=====onError()=======" + i + "=====" + s);
                myRecycleViewScrollListener.setLoadingMore(false);
                mHandler.sendEmptyMessage(Constant.FLAG_FETCH_DATA_ERROR);
            }
        });
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.iv_back:

                this.finish();
                break;
            default:break;
        }
    }

    @Override
    protected void onDestroy()
    {
        unbindService(serviceConnection);
        mHandler.removeCallbacksAndMessages(null);
        llAlbumBg.setBackground(null);
        if(mAlbumBgBitmap!=null)
        {
            mAlbumBgBitmap.recycle();
        }
        super.onDestroy();
    }


    @Override
    public void onItemClickListener(View view, int position)
    {
        Log.i(TAG,"====onItemClickListener======"+position);
        if(mClientService!=null)
        {
            ImageView imageView=(ImageView) view.findViewById(R.id.iv_status_album);
            if(imageView!=null && imageView.getTag()!=null)
            {
                String tag=imageView.getTag()+"";
                if("1".equals(tag))
                {
                    Intent intent=new Intent(this,PlayActivity.class);
                    startActivity(intent);
                    return;
                }
            }

            mAlbumAdapter.notifyDataChanged(position);

            Message message=new Message();
            message.what=FLAG_SEND_MESSAGE;
            message.arg1=position;
            mHandler.sendMessage(message);
        }
        else
        {
            showTextToast("server disconnected!");
        }
    }

    @Override
    public void onReceive(String data)
    {
        Log.i(TAG,"=====onReceive()====="+data);
        Gson gson=new Gson();
        ElementBean result=gson.fromJson(data,ElementBean.class);
        if(result!=null && result.getAction()!=null && result.getSlots()!=null)
        {
            String action=result.getAction();
            String value=result.getSlots();
            Log.i(TAG,action+"====="+value);
            if("result".equalsIgnoreCase(action) && "0".equalsIgnoreCase(value))
            {

            }
        }
        else
        {
            Log.i(TAG,"====is null====");
        }
    }

    @Override
    public void timeout()
    {
        Log.i(TAG,"=====timeout()=====");
    }

    class PlayerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "=======" + action);
        }
    }
}
