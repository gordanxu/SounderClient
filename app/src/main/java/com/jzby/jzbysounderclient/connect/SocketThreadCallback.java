package com.jzby.jzbysounderclient.connect;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by Qi on 2018/3/8.
 */

public class SocketThreadCallback implements SocketThread.SocketThreadCallback {

    private Handler mHandler;
    private SocketThread mSocketThread;
    private ConnectClientUtil.ReceiveCallback mReceiveCallback;
    private List<SocketThread> mSocketThreadList;
    private boolean mIsReConnect = true;

    SocketThreadCallback(Handler handler, List<SocketThread> socketThreadList, ConnectClientUtil
            .ReceiveCallback receiveCallback) {
        mHandler = handler;
        mSocketThreadList = socketThreadList;
        mReceiveCallback = receiveCallback;
    }

    private void postConnectState(boolean isConnect) {
        Message message = mHandler.obtainMessage(ConnectClientUtil.MSG_CONNECT_STATE);
        message.arg1 = isConnect ? 1 : 0;
        mHandler.sendMessage(message);
    }

    @Override
    public void onReceive(String data) {
        if ("kick".equals(data)) {
            mIsReConnect = false;
            mSocketThread.stopThread();
        } else if ("unknown".equals(data)) {
            postConnectState(false);
        } else {
            try {
                JSONObject jsonObject = new JSONObject(data);
                String operate = jsonObject.optString("operate");
                String uuid = jsonObject.optString("uuid");
                if ("server-alive".equals(operate)) {
                    postConnectState(true);
                } else if ("server-out".equals(operate)) {
                    postConnectState(false);
                } else if ("send-by-server".equals(operate)) {
                    Log.i("lqitest", "[onReceive] " + jsonObject.optJSONObject("payload")
                            .toString());
                    JSONObject payloadObject = jsonObject.optJSONObject("payload");
                    String sessionId = jsonObject.optString("sessionId");
                    Map<String, ResponseTimeoutRunnable> hashmap = ConnectClientUtil
                            .mResponseTimeouts;
                    if (!TextUtils.isEmpty(sessionId)) {
                        if (hashmap.containsKey(sessionId)) {
                            ResponseTimeoutRunnable r = hashmap.get(sessionId);
                            if (r != null) {
                                if (payloadObject != null) {
                                    r.callback(payloadObject.toString());
                                }
                                r.remove();
                            }
                        }
                    } else if (mReceiveCallback != null && payloadObject != null) {
                        mReceiveCallback.onReceive(payloadObject.toString());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStart(SocketThread socketThread) {
        mSocketThreadList.add(socketThread);
    }

    @Override
    public void onConnected(SocketThread socketThread) {
        mSocketThread = socketThread;
        mSocketThread.send(Util.getInstance().getJsonStr("client-join", null, null));

        mHandler.sendEmptyMessageDelayed(ConnectClientUtil.MSG_HEART_BEAT, 30000);
    }

    @Override
    public void onFinished(SocketThread socketThread) {
        mSocketThreadList.remove(socketThread);
        postConnectState(false);
        mHandler.removeMessages(ConnectClientUtil.MSG_HEART_BEAT);

        Log.i("lqitest", "onFinished mIsReConnect is " + mIsReConnect);
        if (mIsReConnect) {
            mHandler.sendEmptyMessageDelayed(ConnectClientUtil.MSG_CREATE_THREAD, 5000);
        }
    }
}
