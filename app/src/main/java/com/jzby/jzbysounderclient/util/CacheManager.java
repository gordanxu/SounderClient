package com.jzby.jzbysounderclient.util;


/**
 * Created by gordan on 2018/3/13.
 */

public class CacheManager
{
    public static CacheManager mInstance=null;


    private CacheManager()
    {}

    public static CacheManager getInstance()
    {
        if(mInstance==null)
        {
            mInstance=new CacheManager();
        }
        return mInstance;
    }

}
