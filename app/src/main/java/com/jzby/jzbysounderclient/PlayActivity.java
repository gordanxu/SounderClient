package com.jzby.jzbysounderclient;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jzby.jzbysounderclient.adapter.PlayPagerAdapter;
import com.jzby.jzbysounderclient.bean.ElementBean;
import com.jzby.jzbysounderclient.connect.ConnectClientUtil;
import com.jzby.jzbysounderclient.fragment.LyricFragment;
import com.jzby.jzbysounderclient.fragment.PlayFragment;
import com.jzby.jzbysounderclient.fragment.ProgramFragment;
import com.jzby.jzbysounderclient.util.Constant;
import com.jzby.jzbysounderclient.util.ImageUtil;
import com.ximalaya.ting.android.opensdk.model.album.Announcer;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

import static com.jzby.jzbysounderclient.util.Constant.FLAG_REFRESH_ALBUM_COVER;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_REFRESH_TRACK_TIME;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_UPDATE_INDICATE;


public class PlayActivity extends FragmentActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, SeekBar.OnSeekBarChangeListener, ConnectClientUtil.ResponseCallback {
    final static String TAG = PlayActivity.class.getSimpleName();

    ClientService mClientService;

    PlayerBroadcastReceiver mPlayerBroadcastReceiver;

    ViewPager mPlayerViewPager;

    PlayPagerAdapter mPlayPagerAdapter;

    List<Fragment> mPlayerFragments;

    int mPosition = 0, /**
     * 当前碎片的位置
     **/
    mCurrentCategory;
    /**
     * 当前播放声音的类型 点播 直播
     **/
    LinearLayout llPlayer;
    LinearLayout mIndicateView, mSeekLinearLayout;

    Bitmap mPlayerBitmap;

    ImageView ivFavorite, ivPlayerPrev, ivPlayerPlay, ivPlayerNext;

    TextView tvPlayerTitle, tvPlayerArtist, tvNowTime, tvEndTime;

    SeekBar sbPlayer;

    List<Track> mTrackList;

    Track mCurrentTrack;

    Radio mCurrentRadio;

    int playerStatus = 1, /**
     * 播放器的状态 播放 暂停 默认是播放
     **/
    trackPosition, /**
     * 点播时 当前声音在专辑中的位置
     **/
    playTime = 1;//点播节目时播放的进度

    String mAction = "";//用户的操作 暂停 播放 拖动 上一节目  下一节目

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        Intent intent = new Intent(this, ClientService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mPlayerBroadcastReceiver = new PlayerBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Constant.FLAG_BROADCAST_PLAYER);
        registerReceiver(mPlayerBroadcastReceiver, filter);

        llPlayer = (LinearLayout) findViewById(R.id.ll_player);
        mPlayerViewPager = (ViewPager) findViewById(R.id.vp_player);
        mIndicateView = (LinearLayout) findViewById(R.id.ll_indicate);
        mSeekLinearLayout = (LinearLayout) findViewById(R.id.ll_seek_view);
        sbPlayer = (SeekBar) findViewById(R.id.sb_player);
        ivFavorite = (ImageView) findViewById(R.id.iv_favorite);
        ivPlayerPlay = (ImageView) findViewById(R.id.iv_player_play);
        ivPlayerPrev = (ImageView) findViewById(R.id.iv_player_prev);
        ivPlayerNext = (ImageView) findViewById(R.id.iv_player_next);

        tvPlayerTitle = (TextView) findViewById(R.id.tv_player_title);
        tvPlayerArtist = (TextView) findViewById(R.id.tv_player_artist);
        tvNowTime = (TextView) findViewById(R.id.tv_player_now_time);
        tvEndTime = (TextView) findViewById(R.id.tv_player_end_time);

        findViewById(R.id.iv_back).setOnClickListener(this);
        ivFavorite.setOnClickListener(this);
        ivPlayerPlay.setOnClickListener(this);
        ivPlayerPrev.setOnClickListener(this);
        ivPlayerNext.setOnClickListener(this);
        sbPlayer.setOnSeekBarChangeListener(this);


        mPlayerBitmap=BitmapFactory.decodeResource(getResources(),R.drawable.fruit);
        llPlayer.setBackground(ImageUtil.BoxBlurFilter(mPlayerBitmap));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "=====onResume()==========");
        if(mClientService!=null)
        {
            mHandler.sendEmptyMessage(Constant.FLAG_REFRESH_DATA);
        }
    }

    Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message message) {

            switch (message.what) {
                case FLAG_UPDATE_INDICATE:

                    updateIndicateView(mCurrentCategory,mPosition);

                    break;

                case FLAG_REFRESH_ALBUM_COVER:
                    llPlayer.setBackground(ImageUtil.BoxBlurFilter(mPlayerBitmap));
                    break;

                case FLAG_REFRESH_TRACK_TIME:
                    Log.i(TAG, "======FLAG_REFRESH_TRACK_TIME====="+playTime);
                    playTime += 1;
                    sbPlayer.setProgress(playTime);
                    mHandler.sendEmptyMessageDelayed(FLAG_REFRESH_TRACK_TIME, 1000);
                    break;

                case Constant.FLAG_FETCH_DATA_ERROR:
                    break;

                case Constant.FLAG_REFRESH_DATA:
                    Log.i(TAG, "======FLAG_REFRESH_DATA=====" + mAction);
                    mHandler.removeMessages(FLAG_REFRESH_TRACK_TIME);
                    mCurrentCategory=mClientService.getCurrentCategory();
                    if (Constant.CATEGORY_RADIO == mCurrentCategory) {

                        if ("play".equalsIgnoreCase(mAction) || "pause".equalsIgnoreCase(mAction)) {
                            updateCoverView();
                        }
                        else
                        {
                            initPlayerViewPager(Constant.CATEGORY_RADIO);
                            updateCoverView();
                            mCurrentRadio=mClientService.getCurrentRadio();

                            tvPlayerTitle.setText(mCurrentRadio.getRadioName());
                            tvPlayerArtist.setText("");

                            mSeekLinearLayout.setVisibility(View.INVISIBLE);
                            ivPlayerPrev.setVisibility(View.INVISIBLE);
                            ivPlayerNext.setVisibility(View.INVISIBLE);
                        }

                    } else {
                        if ("pause".equalsIgnoreCase(mAction) || "play".equalsIgnoreCase(mAction)) {

                            updateCoverView();

                        } else if ("seek".equalsIgnoreCase(mAction)) {
                            playTime=mClientService.getPlayerProgress();
                            Log.i(TAG,"=========="+playTime);
                            int minute = playTime / 60;
                            int second = playTime % 60;
                            String startTime = (minute >= 10 ? minute + "" : "0" + minute) + ":" + (second >= 10 ? "" + second : "0" + second);

                            tvNowTime.setText(startTime);
                            mHandler.sendEmptyMessageDelayed(FLAG_REFRESH_TRACK_TIME, 1000);
                        } else if ("prev".equalsIgnoreCase(mAction)) {
                            if (mTrackList == null || mTrackList.size() == 0 || trackPosition <= 0) {
                                Log.i(TAG,"=====first one=====");
                                break;
                            }
                            trackPosition--;
                            mCurrentTrack = mTrackList.get(trackPosition);
                            tvPlayerTitle.setText(mCurrentTrack.getTrackTitle());

                            playTime = 1;
                            updateSeekBarView();
                            mHandler.sendEmptyMessageDelayed(FLAG_REFRESH_TRACK_TIME, 1000);
                        } else if ("next".equalsIgnoreCase(mAction)) {
                            if (mTrackList == null || mTrackList.size() == 0 || trackPosition >= mTrackList.size()-1) {
                                Log.i(TAG,"=====last one=====");
                                break;
                            }
                            trackPosition++;
                            mCurrentTrack = mTrackList.get(trackPosition);
                            tvPlayerTitle.setText(mCurrentTrack.getTrackTitle());

                            playTime = 1;
                            updateSeekBarView();
                            mHandler.sendEmptyMessageDelayed(FLAG_REFRESH_TRACK_TIME, 1000);
                        }
                        else {
                            //从声音列表处切换新的声音
                            if("track".equalsIgnoreCase(mAction))
                            {
                                Fragment fragment=mPlayerFragments.get(mPosition);
                                if(fragment instanceof ProgramFragment)
                                {
                                    ((ProgramFragment)fragment).refreshTrackList();
                                    playTime = 1;
                                }
                            }
                            else
                            {
                                if(mClientService.getCurrentTrackList()==null || mClientService.getCurrentTrackList().size()==0)
                                {
                                    break;
                                }
                                initPlayerViewPager(Constant.CATEGORY_TRACK);
                                playTime=mClientService.getPlayerProgress();
                            }
                            updateCoverView();

                            mTrackList=mClientService.getCurrentTrackList();
                            trackPosition=mClientService.getCurrentTrackPosition();
                            Log.i(TAG,"======trackPosition==="+trackPosition);
                            mCurrentTrack=mClientService.getCurrentTrackList().get(mClientService.getCurrentTrackPosition());

                            Announcer mAnnouncer = mCurrentTrack.getAnnouncer();
                            tvPlayerTitle.setText(mCurrentTrack.getTrackTitle());
                            tvPlayerArtist.setText(mAnnouncer.getNickname());

                            Log.i(TAG, "====playTime=="+playTime+"===Duration=====" + mCurrentTrack.getDuration());

                            updateSeekBarView();
                            mHandler.sendEmptyMessageDelayed(FLAG_REFRESH_TRACK_TIME, 1000);
                        }
                        mAction = "";
                    }
                    break;
            }
            return false;
        }
    });

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i(TAG, "=====onServiceConnected()=======");
            mClientService = ((ClientService.ClientBinder) iBinder).getService();

            if(mClientService.getCurrentRadio()!=null || mClientService.getCurrentTrack()!=null)
            {
                if(mClientService.getPlayerStatus()!=1)
                {
                    sendActionCommand("play",null);
                    mAction="";
                    Log.i(TAG, "=====first play=======");
                    return;
                }
            }
            mHandler.sendEmptyMessage(Constant.FLAG_REFRESH_DATA);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG, "=====onServiceDisconnected()=======");
            mClientService = null;
        }
    };

    public void sendPlayTrackCommand(String msg)
    {
        mAction="track";
        mClientService.sendMessage(msg,this);
    }

    public List<Track> getCurrentTrackList()
    {
        if(mClientService!=null)
        {
            return mClientService.getCurrentTrackList();
        }
        else
        {
            return null;
        }
    }

    public int getCurrentTrackPosition()
    {
        if(mClientService!=null)
        {
           return mClientService.getCurrentTrackPosition();
        }
        else
        {
            Log.i(TAG,"======position is null======");
            return -1;
        }
    }

    public String getCoverImageUrl()
    {
        if(mClientService==null)
        {
            return null;
        }

        if(mClientService.getCurrentCategory()==Constant.CATEGORY_TRACK)
        {
            if(mClientService.getCurrentTrack()!=null)
            {
                return mClientService.getCurrentTrack().getCoverUrlSmall();
            }
        }
        else if(mClientService.getCurrentCategory()==Constant.CATEGORY_RADIO)
        {
            if(mClientService.getCurrentRadio()!=null)
            {
                return mClientService.getCurrentRadio().getCoverUrlLarge();
            }
        }
        else
        {

        }
        return null;
    }

    public int getPlayerStatus()
    {
        if(mClientService!=null)
        {
            return mClientService.getPlayerStatus();
        }
        return -1;
    }

    public void setPlayerBg(Drawable drawable)
    {
        this.mPlayerBitmap=ImageUtil.drawableToBitmap(drawable);
        mHandler.sendEmptyMessage(FLAG_REFRESH_ALBUM_COVER);
    }

    private void updateCoverView()
    {
        if(mClientService.getPlayerStatus()==1)
        {
            Fragment fragment=mPlayerFragments.get(mPosition);
            if(fragment instanceof PlayFragment)
            {
                ((PlayFragment)fragment).setCircleImageViewStatus(1,getCoverImageUrl());
            }
            ivPlayerPlay.setTag("1");
            ivPlayerPlay.setImageResource(R.drawable.notify_btn_light_pause_normal);
        }
        else
        {
            Fragment fragment=mPlayerFragments.get(mPosition);
            if(fragment instanceof PlayFragment)
            {
                ((PlayFragment)fragment).setCircleImageViewStatus(-1,getCoverImageUrl());
            }
            ivPlayerPlay.setTag("0");
            ivPlayerPlay.setImageResource(R.drawable.notify_btn_light_play_normal);
        }
    }

    private void updateSeekBarView() {
        int minute = mCurrentTrack.getDuration() / 60;
        int second = mCurrentTrack.getDuration() % 60;
        String endTime = (minute >= 10 ? minute + "" : "0" + minute) + ":" + (second >= 10 ? "" + second : "0" + second);

        minute = playTime / 60;
        second = playTime % 60;
        String startTime = (minute >= 10 ? minute + "" : "0" + minute) + ":" + (second >= 10 ? "" + second : "0" + second);

        tvNowTime.setText(startTime);
        tvEndTime.setText(endTime);

        sbPlayer.setMax(mCurrentTrack.getDuration());
        sbPlayer.setProgress(playTime);
    }

    private void updateIndicateView(int category,int position) {
        ImageView iv = (ImageView) mIndicateView.findViewById(R.id.ll_iv_indicate_1);
        ImageView iv_1 = (ImageView) mIndicateView.findViewById(R.id.ll_iv_indicate_2);
        ImageView iv_2 = (ImageView) mIndicateView.findViewById(R.id.ll_iv_indicate_3);

        if(category==Constant.CATEGORY_RADIO)
        {
            iv.setVisibility(View.INVISIBLE);
            iv_1.setImageResource(R.drawable.icon_point_selected);
            iv_2.setVisibility(View.INVISIBLE);
        }
        else
        {
            switch (position) {
                case 0:
                    iv.setImageResource(R.drawable.icon_point_selected);
                    iv_1.setImageResource(R.drawable.icon_point_unselected);
                    iv_2.setImageResource(R.drawable.icon_point_unselected);

                    break;
                case 1:
                    iv.setImageResource(R.drawable.icon_point_unselected);
                    iv_1.setImageResource(R.drawable.icon_point_selected);
                    iv_2.setImageResource(R.drawable.icon_point_unselected);
                    break;
                case 2:
                    iv.setImageResource(R.drawable.icon_point_unselected);
                    iv_1.setImageResource(R.drawable.icon_point_unselected);
                    iv_2.setImageResource(R.drawable.icon_point_selected);
                    break;
            }
        }


    }

    private void initPlayerViewPager(int category) {
        mPlayerFragments = new ArrayList<Fragment>();

        if(category==Constant.CATEGORY_TRACK)
        {
            ProgramFragment programFragment = new ProgramFragment();
            mPlayerFragments.add(programFragment);
            mPosition=1;
        }
        else
        {
            mPosition=0;
        }
        PlayFragment playFragment = new PlayFragment();
        mPlayerFragments.add(playFragment);

        if(category==Constant.CATEGORY_TRACK)
        {
            LyricFragment lyricFragment = new LyricFragment();
            mPlayerFragments.add(lyricFragment);
        }

        mPlayPagerAdapter = new PlayPagerAdapter(getSupportFragmentManager(), mPlayerFragments);
        mPlayerViewPager.addOnPageChangeListener(this);
        mPlayerViewPager.setOffscreenPageLimit(2);
        mPlayerViewPager.setAdapter(mPlayPagerAdapter);
        mPlayerViewPager.setCurrentItem(mPosition);
        updateIndicateView(category,mPosition);
    }

    private void sendActionCommand(String action,String value)
    {
        mAction = action;
        ElementBean bean = new ElementBean();
        bean.setAction("media");
        bean.setType("control");
        bean.setSlots(action);
        if(value!=null)
        {
            bean.setAttrValue(value);
        }
        Gson gson = new Gson();
        String msg = gson.toJson(bean);
        Log.i(TAG, "====msg====" + msg);
        mClientService.sendMessage(msg, this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                this.finish();
                break;

            case R.id.iv_player_prev:

                sendActionCommand("prev",null);

                break;
            case R.id.iv_player_play:

                if (ivPlayerPlay.getTag() != null) {
                    String status = ivPlayerPlay.getTag() + "";
                    if ("0".equals(status)) {
                        sendActionCommand("play",null);
                        break;
                    }
                }
                sendActionCommand("pause",null);
                break;
            case R.id.iv_player_next:
                sendActionCommand("next",null);
                break;

            case R.id.iv_favorite:
                ivFavorite.setImageResource(R.drawable.icon_fav_selected);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(mServiceConnection);
        unregisterReceiver(mPlayerBroadcastReceiver);
        mHandler.removeCallbacksAndMessages(null);
        llPlayer.setBackground(null);
        if(mPlayerBitmap!=null)
        {
            mPlayerBitmap.recycle();
        }
        super.onDestroy();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageSelected(int position) {
        mPosition = position;
        mHandler.sendEmptyMessage(FLAG_UPDATE_INDICATE);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        Log.i(TAG, "=====onProgressChanged()======");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "=====onStartTrackingTouch()======");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.i(TAG, "=====onStopTrackingTouch()======");
        playTime = seekBar.getProgress();
        sendActionCommand("seek",playTime*1000+"");
    }

    @Override
    public void onReceive(String data) {
        Log.i(TAG, "====onReceive()=====" + data);
        Gson gson = new Gson();
        ElementBean result = gson.fromJson(data, ElementBean.class);
        if (result != null && result.getAction() != null && result.getSlots() != null) {
            String action = result.getAction();
            String value = result.getSlots();
            Log.i(TAG, action + "=====" + value);
            if ("result".equalsIgnoreCase(action)) {
                if ("0".equalsIgnoreCase(value)) {
                    //mHandler.sendEmptyMessage(Constant.FLAG_REFRESH_DATA);
                } else {
                    mAction = "";
                }
            } else {

            }

        } else {
            Log.i(TAG, "=====is null===");
        }
    }

    @Override
    public void timeout() {
        Log.i(TAG, "====timeout()=====");
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
