package com.jzby.jzbysounderclient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jzby.jzbysounderclient.adapter.RadioAdapter;
import com.jzby.jzbysounderclient.bean.ElementBean;
import com.jzby.jzbysounderclient.connect.ConnectClientUtil;
import com.jzby.jzbysounderclient.util.Constant;
import com.jzby.jzbysounderclient.view.LinearLayoutColorDivider;
import com.jzby.jzbysounderclient.view.MyRecycleViewScrollListener;
import com.jzby.jzbysounderclient.view.SpacesItemDecoration;
import com.ximalaya.ting.android.opensdk.constants.DTransferConstants;
import com.ximalaya.ting.android.opensdk.datatrasfer.CommonRequest;
import com.ximalaya.ting.android.opensdk.datatrasfer.IDataCallBack;
import com.ximalaya.ting.android.opensdk.model.live.radio.Radio;
import com.ximalaya.ting.android.opensdk.model.live.radio.RadioList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jzby.jzbysounderclient.util.Constant.FLAG_FETCH_DATA_ERROR;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_FETCH_DATA_MOST;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_REFRESH_DATA;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_SEND_MESSAGE;
import static com.jzby.jzbysounderclient.util.Constant.FLAG_SWITCH_ACTIVITY;

public class RadioActivity extends Activity implements ConnectClientUtil.ResponseCallback,
        RadioAdapter.OnItemClickInterface,View.OnClickListener
{
    final static String TAG=RadioActivity.class.getSimpleName();

    PlayerBroadcastReceiver mPlayerBroadcastReceiver;

    ClientService mClientService;

    RecyclerView mRadioRecyclerView;

    List<Radio> mList;

    RadioAdapter mRadioAdapter;

    int currentPage=1,currentPosition=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radio);

        Intent intent=new Intent(this,ClientService.class);
        bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);

        mPlayerBroadcastReceiver = new PlayerBroadcastReceiver();
        IntentFilter filter = new IntentFilter(Constant.FLAG_BROADCAST_PLAYER);
        registerReceiver(mPlayerBroadcastReceiver, filter);

        initRadioData();
        findViewById(R.id.iv_back).setOnClickListener(this);
        mRadioRecyclerView=(RecyclerView)findViewById(R.id.rv_radio);
        mRadioRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        LinearLayoutColorDivider divider = new LinearLayoutColorDivider(this.getResources(),R.color.line,
                R.dimen.line,LinearLayoutManager.VERTICAL);
        mRadioRecyclerView.addItemDecoration(divider);
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

    Handler mHandler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {

            switch (message.what)
            {

                case FLAG_SWITCH_ACTIVITY:
                    Intent intent=new Intent(RadioActivity.this,PlayActivity.class);
                    startActivity(intent);
                    break;

                case FLAG_SEND_MESSAGE:
                    currentPosition=message.arg1;
                    Radio radio=mList.get(currentPosition);
                    Log.i(TAG,"====FLAG_SEND_MESSAGE===="+radio.getDataId());

                    ElementBean elementBean=new ElementBean();
                    elementBean.setAction("media");
                    elementBean.setType("ximalaya");
                    elementBean.setSlots("live");

                    List<ElementBean> subBeans=new ArrayList<ElementBean>();

                    ElementBean e1=new ElementBean();
                    e1.setAction("name");
                    e1.setAttrType("string");
                    e1.setAttrValue(radio.getRadioName());
                    subBeans.add(e1);

                    ElementBean e2=new ElementBean();
                    e2.setAction("coverUrl");
                    e2.setAttrType("string");
                    e2.setAttrValue(radio.getCoverUrlLarge());
                    subBeans.add(e2);

                    ElementBean e3=new ElementBean();
                    e3.setAction("playUrl");
                    e3.setAttrType("string");
                    e3.setAttrValue(radio.getRate64TsUrl());
                    subBeans.add(e3);

                    elementBean.setElements(subBeans);

                    Gson gson=new Gson();
                    String msg=gson.toJson(elementBean);
                    Log.i(TAG,"=====msg====="+msg);
                    mClientService.sendMessage(msg,RadioActivity.this);



                    break;

                case FLAG_FETCH_DATA_MOST:showText("=====no more data====");break;

                case FLAG_REFRESH_DATA:

                    if(mRadioAdapter==null)
                    {
                        mRadioAdapter=new RadioAdapter(mList,R.layout.item_radio);
                        mRadioAdapter.setOnItemClickListener(RadioActivity.this);
                        mRadioRecyclerView.setAdapter(mRadioAdapter);
                        mRadioRecyclerView.addOnScrollListener(myRecycleViewScrollListener);
                        mRadioRecyclerView.addItemDecoration(new SpacesItemDecoration(15));
                    }
                    else
                    {
                        mRadioAdapter.notifyDataChanged(mList);
                        myRecycleViewScrollListener.setLoadingMore(false);
                    }
                    break;

                case FLAG_FETCH_DATA_ERROR:

                    showText("=====gain data error====");
                    break;
            }
            return false;
        }
    });



    private MyRecycleViewScrollListener myRecycleViewScrollListener=new MyRecycleViewScrollListener() {

        @Override
        public void onLoadMore()
        {
            currentPage++;
            loadMoreData();
        }
    };


    private void loadMoreData()
    {
        initRadioData();
    }

    private void initRadioData()
    {
        if(mList==null)
        {
            mList=new ArrayList<Radio>();
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put(DTransferConstants.RADIOTYPE, "2");
        map.put(DTransferConstants.PROVINCECODE,"110000");
        map.put(DTransferConstants.PAGE,""+currentPage);
        map.put(DTransferConstants.PAGE_SIZE, "10");
        CommonRequest.getRadios(map, new IDataCallBack<RadioList>() {

            @Override
            public void onSuccess(RadioList object) {

                Log.i(TAG,"=====onSuccess()======");
                if (object != null && object.getRadios() != null)
                {
                    for (int i=0;i<object.getRadios().size();i++)
                    {
                        Radio mRadio=object.getRadios().get(i);
                        mList.add(mRadio);
                        Log.i(TAG,i+"========="+mRadio.getProgramName()+"====="+mRadio.getRadioName()+
                                "==="+mRadio.getRate64AacUrl()+"==="+mRadio.getCoverUrlLarge()+"=="+mRadio.getCoverUrlSmall());
                    }
                    mHandler.sendEmptyMessage(FLAG_REFRESH_DATA);
                }
                else
                {
                    mHandler.sendEmptyMessage(FLAG_FETCH_DATA_MOST);
                }
            }

            @Override
            public void onError(int code, String message) {

                Log.i(TAG,"=====onError()======="+code+"====="+message);

                mHandler.sendEmptyMessage(FLAG_FETCH_DATA_ERROR);
            }
        });

    }

    private void showText(String text)
    {
        Toast.makeText(this,text,Toast.LENGTH_LONG).show();
    }


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
    protected void onDestroy()
    {
        unbindService(serviceConnection);
        unregisterReceiver(mPlayerBroadcastReceiver);
        mHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onItemClickListener(View view, int position)
    {
        Log.i(TAG,"====onItemClickListener===="+position);
        if(mClientService!=null)
        {
            Message message=new Message();
            message.what=FLAG_SEND_MESSAGE;
            message.arg1=position;
            mHandler.sendMessage(message);
        }
    }

    @Override
    public void onReceive(String data)
    {
        Log.i(TAG,"====onReceive()======"+data);
        Gson gson=new Gson();
        ElementBean result=gson.fromJson(data,ElementBean.class);
        if(result!=null && result.getAction()!=null && result.getSlots()!=null)
        {
            String action=result.getAction();
            String value=result.getSlots();
            if("result".equalsIgnoreCase(action) && "0".equalsIgnoreCase(value))
            {

            }
        }

    }

    @Override
    public void timeout()
    {
        Log.i(TAG,"====timeout()======");
    }

    class PlayerBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "=======" + action);
            if(Constant.FLAG_BROADCAST_PLAYER.equalsIgnoreCase(action))
            {
                mHandler.sendEmptyMessage(FLAG_SWITCH_ACTIVITY);
            }
        }
    }
}
