package com.jzby.jzbysounderclient.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Qi on 2018/3/7.
 */

public class ConnectClientUtil implements Handler.Callback {
    private static final String TAG = "ConnectClientUtil";
    //private static final String PROXY_SERVER_IP = "123.207.172.51";
    private static final String PROXY_SERVER_IP = "10.7.2.52";
    private static final int PROXY_SERVER_PORT = 12121;
    static final int MSG_CREATE_THREAD = 1;
    static final int MSG_HEART_BEAT = 2;
    static final int MSG_CONNECT_STATE = 3;
    private static ConnectClientUtil mInstance;
    private Context mContext;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private ConnectedCallback mConnectedCallback;
    private boolean mConnectedStatus = false;
    private ReceiveCallback mReceiveCallback;
    static Map<String, ResponseTimeoutRunnable> mResponseTimeouts;
    private SocketThread mSocketThread;
    private List<SocketThread> mSocketThreadList = new ArrayList<SocketThread>();
    private boolean mIsRegisterConnectivity = false;

    public interface ResponseCallback {
        void onReceive(String data);

        void timeout();
    }

    public interface ConnectedCallback {
        void connected();

        void disconnected();
    }

    public interface ReceiveCallback {
        void onReceive(String data);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context
                    .CONNECTIVITY_SERVICE);

            //获取ConnectivityManager对象对应的NetworkInfo对象
            //获取WIFI连接的信息
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动数据连接的信息
            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                Log.i("lqitest", "WIFI已连接,移动数据已连接");
                connectSocket(true);
            } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                Log.i("lqitest", "WIFI已连接,移动数据已断开");
                connectSocket(true);
            } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                Log.i("lqitest", "WIFI已断开,移动数据已连接");
                connectSocket(true);
            } else {
                Log.i("lqitest", "WIFI已断开,移动数据已断开");
                connectSocket(false);
            }
        }
    };

    private ConnectClientUtil(Context context) {
        mContext = context;

        Util.getInstance().setContext(mContext);

        mResponseTimeouts = new HashMap<String, ResponseTimeoutRunnable>();

        mHandlerThread = new HandlerThread("tcp-connect");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
    }

    public static ConnectClientUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ConnectClientUtil(context);
        }
        return mInstance;
    }

    public void registerConnectedCallback(ConnectedCallback callback) {
        mConnectedCallback = callback;
    }

    public void unregisterConnectedCallback() {
        mConnectedCallback = null;
    }

    public void registerReceiveCallback(ReceiveCallback callback) {
        mReceiveCallback = callback;
    }

    public void unregisterReceiveCallback() {
        mReceiveCallback = null;
    }

    public void connect() {
        if (!mIsRegisterConnectivity) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mContext.registerReceiver(mBroadcastReceiver, filter);

            mIsRegisterConnectivity = true;
        }
    }

    public void destroy() {
        unregisterConnectedCallback();
        unregisterReceiveCallback();
        if(mIsRegisterConnectivity) {
            mContext.unregisterReceiver(mBroadcastReceiver);
        }

        mSocketThread.stopThread();
        mHandlerThread.quit();
        mResponseTimeouts.clear();
        mInstance = null;
    }

    private void send(String data) {
        if (mSocketThread != null && mSocketThread.isWritable()) {
            Log.i("lqitest", "[send] " + data);
            mSocketThread.send(data);
        }
    }

    public void send(final String data, final ResponseCallback responseCallback) {
        if (mSocketThread != null && mSocketThread.isWritable()) {
            Log.i("lqitest", "[send] " + data);
            new Thread() {
                @Override
                public void run() {
                    super.run();

                    ResponseTimeoutRunnable r = new ResponseTimeoutRunnable(mHandler,
                            mResponseTimeouts, responseCallback);
                    r.put();
                    mSocketThread.send(Util.getInstance().getJsonStr("send-by-client", r
                            .getSessionId(), data));
                }
            }.start();
        } else if (responseCallback != null) {
            responseCallback.timeout();
        }
    }

    private void setConnectedStatus(boolean status) {
        if (status && mConnectedCallback != null && !mConnectedStatus) {
            mConnectedCallback.connected();
        } else if (!status && mConnectedCallback != null && mConnectedStatus) {
            mConnectedCallback.disconnected();
        }
        mConnectedStatus = status;
    }

    private void connectSocket(boolean isNow) {
        if (mSocketThread != null) {
            mSocketThread.stopThread();
        }
        if (isNow) {
            if (mHandler.hasMessages(MSG_CREATE_THREAD)) {
                mHandler.removeMessages(MSG_CREATE_THREAD);
            }
            mHandler.sendEmptyMessage(MSG_CREATE_THREAD);
        }
    }

    @Override
    public boolean handleMessage(Message message) {

        switch (message.what) {

            case MSG_CREATE_THREAD:
                Log.i("lqitest", "msg = MSG_CREATE_THREAD, mSocketThreadList.size() = " +
                        mSocketThreadList.size());
                if (mSocketThreadList.size() == 0) {
                    mSocketThread = new SocketThread(PROXY_SERVER_IP, PROXY_SERVER_PORT, new
                            SocketThreadCallback(mHandler, mSocketThreadList, mReceiveCallback));
                    mSocketThread.start();
                } else {
                    mHandler.sendEmptyMessageDelayed(MSG_CREATE_THREAD, 1000);
                }
                break;

            case MSG_HEART_BEAT:
                send("heart-beat");
                mHandler.sendEmptyMessageDelayed(MSG_HEART_BEAT, 30000);
                break;

            case MSG_CONNECT_STATE:
                Log.i("lqitest", "msg = MSG_CONNECT_STATE, message.arg1 = " + message.arg1);
                if (message.arg1 == 1) {
                    setConnectedStatus(true);
                } else {
                    setConnectedStatus(false);
                }
                break;

            default:
                break;
        }
        return true;
    }

}
