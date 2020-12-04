package com.fgtit.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * IP地址工具类
 */
public class IpUtils {
    /**
     * 获取IP地 址
     *
     * @return
     */
    public static String getIpAddress() {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()
                            && inetAddress instanceof Inet4Address) {
                        // if (!inetAddress.isLoopbackAddress() && inetAddress
                        // instanceof Inet6Address) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取WIFI的ip地址
     *
     * @param context
     * @return
     */
    public static String getWifiIp(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
//        DhcpInfo dhcp = wifi.getDhcpInfo();
//        String mask = getIpStringByint(dhcp.netmask);
//        String gateWay = getIpStringByint(dhcp.gateway);
//        L.d("ytmfdw", "掩码：" + mask);
//        L.d("ytmfdw", "网关：" + gateWay);
        // 获得IP地址的方法一：
        int ipAddress = info.getIpAddress();
        String ipString = "";
        ipString = getIpStringByint(ipAddress);
        return ipString;
    }

    /**
     * 获取网关
     *
     * @param context
     * @return
     */
    public static String getGateWay(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        DhcpInfo dhcp = wifi.getDhcpInfo();
        return getIpStringByint(dhcp.gateway);
    }

    /**
     * 获取掩码
     *
     * @param context
     * @return
     */
    public static String getNetMask(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        DhcpInfo dhcp = wifi.getDhcpInfo();
        return getIpStringByint(dhcp.netmask);
    }

    /**
     * 获取MAC地址
     *
     * @param context
     * @return
     */
    public String getLocalMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }


    /**
     * 获取当前WIFI状态
     *
     * @param context
     * @return
     */
    public static String getCurrentNetType(Context context) {
        String type = "";
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            type = "null";
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            type = "wifi";
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = info.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS
                    || subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                type = "2g";
            } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_A || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                type = "3g";
            } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE是3g到4g的过渡，是3.9G的全球标准
                type = "4g";
            }
        }
        return type;
    }

    /**
     * 把ip整形数据转换点分十进制
     *
     * @param ipAddress
     * @return
     */
    public static String getIpStringByint(int ipAddress) {
        String ipString = "";
        if (ipAddress != 0) {
            ipString = ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                    + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
        }
//        ipString = Formatter.formatIpAddress(ipAddress);
        return ipString;
    }
}
