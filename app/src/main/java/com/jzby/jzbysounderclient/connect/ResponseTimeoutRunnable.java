package com.jzby.jzbysounderclient.connect;

import android.os.Handler;

import java.util.Map;
import java.util.UUID;

/**
 * Created by Qi on 2018/3/9.
 */
class ResponseTimeoutRunnable implements Runnable {
    private static final int RESPONSE_TIMEOUT = 3000;
    private String mSessionId;
    private Handler mHandler;
    private ConnectClientUtil.ResponseCallback mResponseCallback;
    private Map<String, ResponseTimeoutRunnable> mHashMap;

    ResponseTimeoutRunnable(Handler handler, Map<String, ResponseTimeoutRunnable>
            hashmap, ConnectClientUtil.ResponseCallback responseCallback) {
        mHandler = handler;
        mResponseCallback = responseCallback;
        mHashMap = hashmap;
    }

    void put() {
        mSessionId = UUID.randomUUID().toString();
        mHashMap.put(mSessionId, this);
        mHandler.postDelayed(this, RESPONSE_TIMEOUT);
    }

    void remove() {
        mHandler.removeCallbacks(this);
        mHashMap.remove(mSessionId);
    }

    void callback(String data) {
        if (mResponseCallback != null) {
            mResponseCallback.onReceive(data);
        }
    }

    String getSessionId() {
        return mSessionId;
    }

    @Override
    public void run() {
        if (mResponseCallback != null) {
            mResponseCallback.timeout();
        }
        mHashMap.remove(mSessionId);
    }
}
