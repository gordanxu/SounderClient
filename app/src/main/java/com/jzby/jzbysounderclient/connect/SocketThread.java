package com.jzby.jzbysounderclient.connect;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by Qi on 2018/3/8.
 */

class SocketThread extends Thread {
    private Socket mSocket;
    private BufferedReader mBufferedReader;
    private BufferedWriter mBufferedWriter;
    private SocketThreadCallback mSocketThreadCallback;
    private String mIp;
    private int mPort;

    interface SocketThreadCallback {
        void onReceive(String data);

        void onStart(SocketThread socketThread);

        void onConnected(SocketThread socketThread);

        void onFinished(SocketThread socketThread);
    }

    SocketThread(String ip, int port, SocketThreadCallback callback) {
        mIp = ip;
        mPort = port;
        mSocketThreadCallback = callback;
        if (mSocketThreadCallback != null) {
            mSocketThreadCallback.onStart(this);
        }
    }

    void stopThread() {
        stopSocket();
    }

            void send(String data) {
                if (mBufferedWriter != null) {
            try {
                mBufferedWriter.write(data + "\n");
                mBufferedWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    boolean isWritable() {
        return mBufferedWriter != null;
    }

    @Override
    public void run() {
        super.run();

        try {
            mSocket = new Socket(mIp, mPort);
            mSocket.setKeepAlive(true);

            mBufferedReader = new BufferedReader(new InputStreamReader(mSocket
                    .getInputStream()));
            mBufferedWriter = new BufferedWriter(new OutputStreamWriter(mSocket
                    .getOutputStream()));

            if (mSocketThreadCallback != null) {
                mSocketThreadCallback.onConnected(this);
            }

            String recvData;
            while ((recvData = mBufferedReader.readLine()) != null) {
                if (mSocketThreadCallback != null) {
                    mSocketThreadCallback.onReceive(recvData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mSocketThreadCallback != null) {
                mSocketThreadCallback.onFinished(this);
            }
            stopSocket();
            mSocketThreadCallback = null;
        }
    }

    private void stopSocket() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = null;
        }
        if (mBufferedReader != null) {
            try {
                mBufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (mBufferedWriter != null) {
            try {
                mBufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
