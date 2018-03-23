package com.jzby.jzbysounderclient;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.jzby.jzbysounderclient.bean.ElementBean;
import com.jzby.jzbysounderclient.connect.ConnectClientUtil;
import com.jzby.jzbysounderclient.util.Constant;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.track.LastPlayTrackList;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by gordan on 2018/3/9.
 */

public class ClientService extends Service implements ConnectClientUtil.ConnectedCallback,
        ConnectClientUtil.ReceiveCallback {
    final static String TAG = ClientService.class.getSimpleName();

    private Radio mCurrentRadio;

    private List<Track> mCurrentTrackList;

    private Track mCurrentTrack;

    private int mCurrentCategory,mCurrentTrackPosition, mPlayerStatus, mPlayerProgress;

    private int serverStatus = -1;

    private ConnectClientUtil mConnectClientUtil = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "=====onCreate()=======");
        mConnectClientUtil = ConnectClientUtil.getInstance(getApplicationContext());
        mConnectClientUtil.registerConnectedCallback(this);
        mConnectClientUtil.registerReceiveCallback(this);
    }

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            mConnectClientUtil.connect();

            return false;
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "=====onBind()=======");
        return new ClientBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "=====onStartCommand()=======");
        if (serverStatus < 0) {
            mConnectClientUtil.connect();
        }
        return Service.START_STICKY;
    }

    public int getPlayerProgress() {
        return mPlayerProgress;
    }

    public void setPlayerProgress(int mPlayerProgress) {
        this.mPlayerProgress = mPlayerProgress;
    }

    public int getPlayerStatus() {
        return mPlayerStatus;
    }

    public void setPlayerStatus(int mPlayerStatus) {
        this.mPlayerStatus = mPlayerStatus;
    }

    public int getCurrentCategory() {
        return mCurrentCategory;
    }

    public void setCurrentCategory(int mCurrentCategory) {
        this.mCurrentCategory = mCurrentCategory;
    }

    public int getCurrentTrackPosition() {
        return mCurrentTrackPosition;
    }

    public void setCurrentTrackPosition(int mCurrentTrackPosition) {
        this.mCurrentTrackPosition = mCurrentTrackPosition;
    }

    public Track getCurrentTrack() {
        return mCurrentTrack;
    }

    public void setCurrentTrack(Track mCurrentTrack) {
        this.mCurrentTrack = mCurrentTrack;
    }

    public Radio getCurrentRadio() {
        return mCurrentRadio;
    }

    public void setCurrentRadio(Radio mCurrentRadio) {
        this.mCurrentRadio = mCurrentRadio;
    }

    public List<Track> getCurrentTrackList() {
        return mCurrentTrackList;
    }

    public void setCurrentTrackList(List<Track> mCurrentTrackList) {
        this.mCurrentTrackList = mCurrentTrackList;
    }

    public void sendMessage(String msg, ConnectClientUtil.ResponseCallback callback) {
        Log.i(TAG, "=====sendMessage()=======" + msg);
        if (serverStatus > 0) {
            mConnectClientUtil.send(msg, callback);
        } else {
            mHandler.sendEmptyMessage(10000);
        }
    }

    @Override
    public void connected() {
        Log.i(TAG, "======connected()=======");
        serverStatus = 1;
        getPlayerInfo();
    }

    @Override
    public void disconnected() {
        Log.i(TAG, "======disconnected()=======");
        serverStatus = -1;
        mHandler.sendEmptyMessageDelayed(10000, 3000);
    }

    @Override
    public void onReceive(String data) {
        Log.i(TAG, "======onReceive()=======" + data);
        if (!TextUtils.isEmpty(data)) {

            if (getResponseResult(data) == 1) {
                Intent intent = new Intent();
                intent.setAction(Constant.FLAG_BROADCAST_PLAYER);
                sendBroadcast(intent);
            }
        }
    }

    private int tryCount=0;

    public void getPlayerInfo() {
        ElementBean bean = new ElementBean();
        bean.setType("media");
        bean.setAction("get");

        Gson gson = new Gson();
        String msg = gson.toJson(bean);
        sendMessage(msg, new ConnectClientUtil.ResponseCallback() {

            @Override
            public void onReceive(String data) {
                Log.i(TAG, "===getPlayerInfo===onReceive()=======" + data);
                if (getResponseResult(data) == 1) {
                    Intent intent = new Intent();
                    intent.setAction(Constant.FLAG_BROADCAST_PLAYER);
                    sendBroadcast(intent);
                }
            }

            @Override
            public void timeout() {
                Log.i(TAG, "===getPlayerInfo===timeout()======="+tryCount);
                if(tryCount>3)
                {
                    return;
                }

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getPlayerInfo();
                    }
                },1500);
            }
        });
        tryCount++;
    }

    private void getTrackInfo(String albumId, final String trackId) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.ALBUM_ID, albumId);
        map.put(DTransferConstants.TRACK_ID, trackId);
        CommonRequest.getLastPlayTracks(map, new IDataCallBack<LastPlayTrackList>() {

            @Override
            public void onSuccess(@Nullable LastPlayTrackList lastPlayTrackList) {

                if (lastPlayTrackList != null && lastPlayTrackList.getTracks() != null && lastPlayTrackList.getTracks().size() > 0) {
                    mCurrentTrackList = lastPlayTrackList.getTracks();
                    Log.i(TAG, "====mTrackList size========" + mCurrentTrackList.size());
                    for (int i=0;i<mCurrentTrackList.size();i++)
                    {
                        Track bean=mCurrentTrackList.get(i);
                        if(trackId.equalsIgnoreCase(bean.getDataId()+""))
                        {
                            mCurrentTrack=bean;
                            mCurrentTrackPosition=i;
                            Log.i(TAG, "====Track Position========"+i);
                            break;
                        }
                    }
                } else {
                    Log.i(TAG, "====getTracks is null========");
                }
            }

            @Override
            public void onError(int i, String s) {
                Log.i(TAG, i + "========" + s);
            }
        });
    }

    private int getResponseResult(String data) {
        Gson gson = new Gson();
        ElementBean resultBean = gson.fromJson(data, ElementBean.class);
        if (resultBean != null && resultBean.getAction() != null && resultBean.getSlots() != null) {
            String action = resultBean.getAction();
            String value = resultBean.getSlots();
            Log.i(TAG, action + "=====" + value);
            if ("media".equalsIgnoreCase(action)) {

                if ("vod".equalsIgnoreCase(value)) {
                    mCurrentCategory = Constant.CATEGORY_TRACK;
                } else if ("live".equalsIgnoreCase(value)) {
                    mCurrentCategory = Constant.CATEGORY_RADIO;
                    mCurrentRadio = new Radio();
                }
                else if("pause".equalsIgnoreCase(value))
                {
                    mPlayerStatus = 0;
                }
                else if("resume".equalsIgnoreCase(value))
                {
                    mPlayerStatus = 1;
                }
                else if("seek".equalsIgnoreCase(value))
                {
                    mPlayerProgress= Integer.parseInt(resultBean.getAttrValue());
                    mPlayerProgress = mPlayerProgress / 1000;
                }
                else if("prev".equalsIgnoreCase(value))
                {
                    mPlayerProgress=1;
                }
                else if("next".equalsIgnoreCase(value))
                {
                    mPlayerProgress=1;
                }
                else
                {

                }

                List<ElementBean> subBeans = resultBean.getElements();
                if(subBeans==null || subBeans.size()==0)
                {
                    return 1;
                }
                mPlayerProgress=1;
                mPlayerStatus = 1;
                String albumId = "", trackId = "";
                for (int i = 0; i < subBeans.size(); i++) {
                    ElementBean e = subBeans.get(i);
                    if ("state".equalsIgnoreCase(e.getAction())) {
                        if ("playing".equalsIgnoreCase(e.getAttrValue())) {
                            mPlayerStatus = 1;
                        } else if ("paused".equalsIgnoreCase(e.getAttrValue())) {
                            mPlayerStatus = 0;
                        }
                    } else if (e.getAction() != null && "playTime".equalsIgnoreCase(e.getAction())) {
                        mPlayerProgress = Integer.parseInt(e.getAttrValue());
                        mPlayerProgress = mPlayerProgress / 1000;
                    } else if (e.getAction() != null && "albumId".equalsIgnoreCase(e.getAction())) {
                        albumId = e.getAttrValue();
                    } else if (e.getAction() != null && "playId".equalsIgnoreCase(e.getAction())) {
                        trackId = e.getAttrValue();
                    } else if (e.getAction() != null && "name".equalsIgnoreCase(e.getAction())) {
                        mCurrentRadio.setRadioName(e.getAttrValue());
                    } else if (e.getAction() != null && "coverUrl".equalsIgnoreCase(e.getAction())) {
                        mCurrentRadio.setCoverUrlLarge(e.getAttrValue());
                    } else {
                    }
                }

                if (Constant.CATEGORY_TRACK == mCurrentCategory) {
                    Log.i(TAG,"albumId========"+albumId+"===trackId====="+trackId);
                    if (!TextUtils.isEmpty(albumId) && !TextUtils.isEmpty(albumId)) {
                        getTrackInfo(albumId, trackId);
                    }
                }

                return 1;
            } else {

            }

        } else {
            Log.i(TAG, "=====result is null===");
        }
        return -1;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "=====onStartCommand()=======");
    }

    public class ClientBinder extends Binder {
        public ClientService getService() {
            return ClientService.this;
        }
    }

}
