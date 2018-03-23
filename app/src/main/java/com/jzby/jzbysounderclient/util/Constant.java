package com.jzby.jzbysounderclient.util;

/**
 * Created by gordan on 2018/3/9.
 */

public class Constant {
    public final static String FLAG_BROADCAST_PLAYER = "BROADCAST_PLAYER";

    public final static String KEY_ALBUM_ID = "ALBUM_ID";
    public final static String KEY_TRACK_ID = "TRACK_ID";

    public final static String SOUNDER_WIFI_RSS_ID="jzby";
    public final static String SOUNDER_WIFI_PWD="123456";

    public final static int CATEGORY_RADIO = 1;
    public final static int CATEGORY_TRACK = 2;

    /**Handler消息**/
    public final static int FLAG_REFRESH_DATA = 10000;
    public final static int FLAG_FETCH_DATA_MOST=10001;
    public final static int FLAG_FETCH_DATA_ERROR=10002;
    public final static int FLAG_REFRESH_ALBUM_COVER=10003;
    public final static int FLAG_SEND_MESSAGE=10004;
    public final static int FLAG_ALBUM_LIST_CLICK=10005;
    public final static int FLAG_UPDATE_INDICATE = 10006;
    public final static int FLAG_REFRESH_TRACK_TIME = 10007;
    public final static int FLAG_SWITCH_ACTIVITY=10008;
    public final static int FLAG_UPDATE_RECYCLE_VIEW=10009;
    public final static int FLAG_SWITCH_BANNER = 10010;

}
