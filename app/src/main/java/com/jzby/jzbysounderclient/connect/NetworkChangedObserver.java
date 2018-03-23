package com.jzby.jzbysounderclient.connect;

import android.content.Context;

/**
 * Created by Qi on 2018/3/7.
 */

public class NetworkChangedObserver {

    private NetworkChangedCallback mCallback;

    interface NetworkChangedCallback {
        void changed(boolean isConnected);
    }

    public NetworkChangedObserver(Context context, NetworkChangedCallback callback) {
        mCallback = callback;
    }
}
