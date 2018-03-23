package com.jzby.jzbysounderclient;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jzby.jzbysounderclient.adapter.AlbumListAdapter;
import com.jzby.jzbysounderclient.view.MyRecycleViewScrollListener;
import com.jzby.jzbysounderclient.view.SpacesItemDecoration;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.album.Album;
import com.ximalaya.ting.android.opensdk.model.album.AlbumList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jzby.jzbysounderclient.util.Constant.FLAG_ALBUM_LIST_CLICK;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_FETCH_DATA_ERROR;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_FETCH_DATA_MOST;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_REFRESH_DATA;

public class AlbumListActivity extends BaseActivity implements AlbumListAdapter.OnItemClickInterface,View.OnClickListener
{
    final static String TAG=AlbumListActivity.class.getSimpleName();

    String categoryId,categoryTitle;

    TextView mTitleTextView;

    RecyclerView mAlbumListRecyclerView;

    AlbumListAdapter mAlbumListAdapter;

    List<Album> mAlbumList;

    private int currentPage=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_list);
        if(getIntent()!=null)
        {
            categoryId=getIntent().getStringExtra("categoryId").trim();
            categoryTitle=getIntent().getStringExtra("categoryTitle").trim();
            initData();
        }


        findViewById(R.id.iv_back).setOnClickListener(this);
        mTitleTextView=(TextView)findViewById(R.id.tv_album_title);
        mAlbumListRecyclerView=(RecyclerView)findViewById(R.id.rv_album_list);
        mAlbumListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAlbumListRecyclerView.addOnScrollListener(myRecycleViewScrollListener);

        mTitleTextView.setText(categoryTitle);
    }

    private MyRecycleViewScrollListener myRecycleViewScrollListener=new MyRecycleViewScrollListener() {

        @Override
        public void onLoadMore()
        {
            currentPage++;
            initData();
        }
    };


    private void initData()
    {
        if(mAlbumList==null)
        {
            mAlbumList=new ArrayList<Album>();
        }

        Map<String ,String> map = new HashMap<String, String>();
        map.put(DTransferConstants.CATEGORY_ID ,categoryId);
        map.put(DTransferConstants.CALC_DIMENSION ,"1");
        map.put(DTransferConstants.PAGE,""+currentPage);
        map.put(DTransferConstants.PAGE_SIZE,"10");
        CommonRequest.getAlbumList(map, new IDataCallBack<AlbumList>() {

            @Override
            public void onSuccess(@Nullable AlbumList albumList)
            {
                if(albumList!=null && albumList.getAlbums()!=null && albumList.getAlbums().size()>0)
                {
                    for (int i=0;i<albumList.getAlbums().size();i++)
                    {
                        Album album=albumList.getAlbums().get(i);
                        Log.i(TAG,album.getId()+"==========="+album.getAlbumTitle());
                        mAlbumList.add(album);
                    }
                    mHandler.sendEmptyMessage(FLAG_REFRESH_DATA);
                }
                else
                {
                    mHandler.sendEmptyMessage(FLAG_FETCH_DATA_MOST);
                }
            }

            @Override
            public void onError(int i, String s)
            {
                Log.i(TAG,i+"====onError()===="+s);
                mHandler.sendEmptyMessage(FLAG_FETCH_DATA_ERROR);
            }
        });
    }

    Handler mHandler=new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message message) {

            switch (message.what)
            {
                case FLAG_ALBUM_LIST_CLICK:

                    int position=message.arg1;
                    Album album=mAlbumList.get(position);

                    Log.i(TAG,"====FLAG_ALBUM_LIST_CLICK====="+album.getId());

                    Intent intent=new Intent(AlbumListActivity.this,AlbumDetailsActivity.class);
                    intent.putExtra("albumId",album.getId());
                    startActivity(intent);

                    break;

                case FLAG_REFRESH_DATA:

                    if(mAlbumListAdapter==null)
                    {
                        mAlbumListAdapter=new AlbumListAdapter(mAlbumList,R.layout.item_list_album);
                        mAlbumListAdapter.setOnItemClickListener(AlbumListActivity.this);
                        mAlbumListAdapter.setFooterResId(R.layout.item_push_header_layout);
                        mAlbumListRecyclerView.setAdapter(mAlbumListAdapter);
                        mAlbumListRecyclerView.addItemDecoration(new SpacesItemDecoration(15));
                    }
                    else
                    {
                        mAlbumListAdapter.notifyDataChanged(mAlbumList);
                        myRecycleViewScrollListener.setLoadingMore(false);
                        mAlbumListAdapter.hiddenFooterView();
                    }
                    break;

                case FLAG_FETCH_DATA_MOST:
                    showTextToast("has no more data!!");
                    myRecycleViewScrollListener.setLoadingMore(false);
                    mAlbumListAdapter.hiddenFooterView();
                    break;
                case FLAG_FETCH_DATA_ERROR:
                    showTextToast("gain data error!!");
                    break;
            }

            return false;
        }
    });

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.iv_back:

                this.finish();
                break;
        }
    }

    @Override
    public void onItemClickListener(View view, int position)
    {
        Log.i(TAG,"======onItemClickListener()======="+position);
        Message message=new Message();
        message.what=FLAG_ALBUM_LIST_CLICK;
        message.arg1=position;
        mHandler.sendMessage(message);
    }
}
