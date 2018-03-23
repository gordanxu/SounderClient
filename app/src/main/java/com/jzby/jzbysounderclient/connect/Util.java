package com.jzby.jzbysounderclient.connect;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;

/**
 * Created by Qi on 2018/3/7.
 */

public class Util {

    private static Util mInstance;
    private Context mContext;

    private Util() {

    }

    public static Util getInstance() {
        if (mInstance == null) {
            mInstance = new Util();
        }
        return mInstance;
    }

    void setContext(Context context) {
        mContext = context;
    }

    public String getJsonStr(String operate, String sessionId, String payload) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("operate", operate);
            jsonObject.put("uuid", getUniqueId());
            jsonObject.put("localIP", getHostIP());
            jsonObject.put("deviceType", "client");
            if (!TextUtils.isEmpty(sessionId)) {
                jsonObject.put("sessionId", sessionId);
            }
            if (!TextUtils.isEmpty(payload)) {
                jsonObject.put("payload", new JSONObject(payload));
            }
            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getUniqueId() {
        String androidID = Settings.Secure.getString(mContext.getContentResolver(), Settings
                .Secure.ANDROID_ID);
        String id = androidID + Build.SERIAL;
        try {
            return toMD5(id);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return id;
        }
    }

    private String toMD5(String text) throws NoSuchAlgorithmException {
        //获取摘要器 MessageDigest
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        //通过摘要器对字符串的二进制字节数组进行hash计算
        byte[] digest = messageDigest.digest(text.getBytes());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            //循环每个字符 将计算结果转化为正整数;
            int digestInt = digest[i] & 0xff;
            //将10进制转化为较短的16进制
            String hexString = Integer.toHexString(digestInt);
            //转化结果如果是个位数会省略0,因此判断并补0
            if (hexString.length() < 2) {
                sb.append(0);
            }
            //将循环结果添加到缓冲区
            sb.append(hexString);
        }
        //返回整个结果
        return sb.toString();
    }

    public static String getHostIP() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIp;

    }

}
