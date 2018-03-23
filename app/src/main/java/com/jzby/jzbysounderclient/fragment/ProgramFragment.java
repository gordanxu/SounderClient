package com.jzby.jzbysounderclient.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.jzby.jzbysounderclient.PlayActivity;
import com.jzby.jzbysounderclient.R;
import com.jzby.jzbysounderclient.adapter.ProgramAdapter;
import com.jzby.jzbysounderclient.bean.ElementBean;
import com.ximalaya.ting.android.opensdk.model.track.Track;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProgramFragment extends Fragment implements AdapterView.OnItemClickListener
{
    final static String TAG=ProgramFragment.class.getSimpleName();

    PlayActivity activity;

    ListView lvProgram;

    ProgramAdapter mProgramAdapter;

    List<Track> mProgramBeans;

    private int mCurrentPosition=3;

    public ProgramFragment() { }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity=(PlayActivity)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Log.i(TAG,"====onCreateView()====");
        View root=inflater.inflate(R.layout.fragment_program, container, false);
        lvProgram=(ListView)root.findViewById(R.id.lv_program);
        return root;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);
        Log.i(TAG,"====setUserVisibleHint()===="+isVisibleToUser);
        if(activity!=null && isVisibleToUser)
        {
            initProgramInfo();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.i(TAG,"====onResume()====");
    }

    private void initProgramInfo()
    {
        mProgramBeans=activity.getCurrentTrackList();
        mCurrentPosition=activity.getCurrentTrackPosition();

        Log.i(TAG,"=======mCurrentPosition========="+mCurrentPosition);
        if(mProgramBeans!=null && mProgramBeans.size()>0 && mCurrentPosition>=0)
        {
            mProgramAdapter=new ProgramAdapter(activity,mProgramBeans,R.layout.item_program,mCurrentPosition);
            lvProgram.setAdapter(mProgramAdapter);
            lvProgram.setOnItemClickListener(this);
        }
        else
        {

        }
    }

    public void refreshTrackList()
    {
        if(mProgramAdapter!=null)
        {
            mProgramAdapter.notifyDataChanged(mCurrentPosition);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        mCurrentPosition=i;
        Track track= mProgramBeans.get(i);
        long albumId=-1;
        if(track.getAlbum()!=null)
        {
            albumId=track.getAlbum().getAlbumId();
        }
        Log.i(TAG, track.getDataId()+"==========="+albumId);
        ElementBean elementBean=new ElementBean();
        elementBean.setAction("media");
        elementBean.setType("ximalaya");
        elementBean.setSlots("vod");

        List<ElementBean> subBeans=new ArrayList<ElementBean>();

        ElementBean e1=new ElementBean();
        e1.setAction("playId");
        e1.setAttrType("long");
        e1.setAttrValue(track.getDataId()+"");
        subBeans.add(e1);

        if(albumId>0)
        {
            ElementBean e2=new ElementBean();
            e2.setAction("albumId");
            e2.setAttrType("long");
            e2.setAttrValue(albumId+"");

            subBeans.add(e2);
        }

        elementBean.setElements(subBeans);
        Gson gson=new Gson();
        String msg=gson.toJson(elementBean);
        Log.i(TAG,"====send===="+msg);
        activity.sendPlayTrackCommand(msg);

    }
}
