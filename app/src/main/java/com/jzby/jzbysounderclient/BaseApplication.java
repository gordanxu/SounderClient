package com.jzby.jzbysounderclient;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.util.BaseUtil;
import com.ximalaya.ting.android.sdkdownloader.XmDownloadManager;
import com.ximalaya.ting.android.sdkdownloader.http.RequestParams;
import com.ximalaya.ting.android.sdkdownloader.http.app.RequestTracker;
import com.ximalaya.ting.android.sdkdownloader.http.request.UriRequest;

import org.xutils.x;


/**
 * Created by gordan on 2018/3/7.
 */

public class BaseApplication extends Application
{
    final static String TAG=BaseApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Intent intent=new Intent(this,ClientService.class);
        startService(intent);

        String mp3 = getExternalFilesDir("mp3").getAbsolutePath();
        Log.i(TAG,"===path===="+mp3);

        x.Ext.init(this);

        CommonRequest mXimalaya = CommonRequest.getInstanse();
        String mAppSecret = "8646d66d6abe2efd14f2891f9fd1c8af";
        mXimalaya.setAppkey("9f9ef8f10bebeaa83e71e62f935bede8");
        mXimalaya.setPackid("com.app.test.android");
        mXimalaya.init(this,mAppSecret);

        if(BaseUtil.isMainProcess(this)) {
            XmDownloadManager.Builder(this)
                    .maxDownloadThread(1)			// 最大的下载个数 默认为1 最大为3
                    .maxSpaceSize(Long.MAX_VALUE)	// 设置下载文件占用磁盘空间最大值，单位字节。不设置没有限制
                    .connectionTimeOut(15000)		// 下载时连接超时的时间 ,单位毫秒 默认 30000
                    .readTimeOut(15000)				// 下载时读取的超时时间 ,单位毫秒 默认 30000
                    .fifo(false)					// 等待队列的是否优先执行先加入的任务. false表示后添加的先执行(不会改变当前正在下载的音频的状态) 默认为true
                    .maxRetryCount(3)				// 出错时重试的次数 默认2次
                    .progressCallBackMaxTimeSpan(1000)//  进度条progress 更新的频率 默认是800
                    .requestTracker(mRequestTracker)	// 日志 可以打印下载信息
                    .savePath(mp3)	// 保存的地址 会检查这个地址是否有效
                    .create();
        }
    }


    private RequestTracker mRequestTracker=new RequestTracker() {
        @Override
        public void onWaiting(RequestParams requestParams) {
            Log.i(TAG,"======onWaiting()======"+requestParams);
        }

        @Override
        public void onStart(RequestParams requestParams) {
            Log.i(TAG,"======onStart()======"+requestParams);
        }

        @Override
        public void onRequestCreated(UriRequest uriRequest) {
            Log.i(TAG,"======onRequestCreated()======"+uriRequest);
        }

        @Override
        public void onSuccess(UriRequest uriRequest, Object o) {
            Log.i(TAG,"======onSuccess()======"+uriRequest+"======="+o);
        }

        @Override
        public void onRemoved(UriRequest uriRequest) {
            Log.i(TAG,"======onRemoved()======"+uriRequest);
        }

        @Override
        public void onCancelled(UriRequest uriRequest) {
            Log.i(TAG,"======onCancelled()======"+uriRequest);
        }

        @Override
        public void onError(UriRequest uriRequest, Throwable throwable, boolean b) {
            Log.i(TAG,"======onError()======"+uriRequest+"==="+throwable.getMessage()+"========"+b);
        }

        @Override
        public void onFinished(UriRequest uriRequest) {
            Log.i(TAG,"======onFinished()======"+uriRequest);
        }
    };
}
