package com.cowave.commons.framework.util;

import java.net.*;
import java.util.Enumeration;

/**
 *
 * @see com.alibaba.nacos.api.utils.NetUtils
 *
 * @author shanhuiming
 *
 */
public class NetUtils {

    public static String hostIp(){
        InetAddress inetAddress = findFirstNonLoopbackAddress();
        if (inetAddress == null) {
            return "";
        }
        return inetAddress.getHostAddress();
    }

    public static String hostName(){
        InetAddress inetAddress = findFirstNonLoopbackAddress();
        if (inetAddress == null) {
            return "";
        }
        return inetAddress.getHostName();
    }

    private static InetAddress findFirstNonLoopbackAddress() {
        InetAddress result = null;

        try {
            int lowest = Integer.MAX_VALUE;
            for (Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
                 nics.hasMoreElements(); ) {
                NetworkInterface ifc = nics.nextElement();
                if (ifc.isUp()) {
                    if (ifc.getIndex() < lowest || result == null) {
                        lowest = ifc.getIndex();
                    } else {
                        continue;
                    }

                    for (Enumeration<InetAddress> addrs = ifc.getInetAddresses(); addrs.hasMoreElements(); ) {
                        InetAddress address = addrs.nextElement();
                        boolean isLegalIpVersion = Boolean.parseBoolean(System.getProperty("java.net.preferIPv6Addresses"))
                                ? address instanceof Inet6Address : address instanceof Inet4Address;
                        if (isLegalIpVersion && !address.isLoopbackAddress()) {
                            result = address;
                        }
                    }

                }
            }
        } catch (Exception ignore) {}
        if (result != null) {
            return result;
        }
        try {
            return InetAddress.getLocalHost();
        } catch (UnknownHostException ignore) {
        }
        return null;
    }
}
