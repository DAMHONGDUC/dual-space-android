package com.dd.the.universe.proxy;

import java.util.Locale;

import com.dd.the.universe.TheUniverseCore;


public class ProxyManifest {
    public static final int FREE_COUNT = 50;

    public static boolean isProxy(String msg) {
        return getBindProvider().equals(msg) || msg.contains("proxy_content_provider_");
    }

    public static String getBindProvider() {
        return TheUniverseCore.getHostPkg() + ".theuniverse.SystemCallProvider";
    }

    public static String getProxyAuthorities(int index) {
        return String.format(Locale.CHINA, "%s.proxy_content_provider_%d", TheUniverseCore.getHostPkg(), index);
    }

    public static String getProxyPendingActivity(int index) {
        return String.format(Locale.CHINA, "com.dd.the.universe.proxy.ProxyPendingActivity$P%d", index);
    }

    public static String getProxyActivity(int index) {
        return String.format(Locale.CHINA, "com.dd.the.universe.proxy.ProxyActivity$P%d", index);
    }

    public static String TransparentProxyActivity(int index) {
        return String.format(Locale.CHINA, "com.dd.the.universe.proxy.TransparentProxyActivity$P%d", index);
    }

    public static String getProxyService(int index) {
        return String.format(Locale.CHINA, "com.dd.the.universe.proxy.ProxyService$P%d", index);
    }

    public static String getProxyJobService(int index) {
        return String.format(Locale.CHINA, "com.dd.the.universe.proxy.ProxyJobService$P%d", index);
    }

    public static String getProxyFileProvider() {
        return TheUniverseCore.getHostPkg() + ".theuniverse.FileProvider";
    }

    public static String getMediaProviderAuthority() {
        return TheUniverseCore.getHostPkg() + ".theuniverse.MediaProvider";
    }

    public static String getProxyReceiver() {
        return TheUniverseCore.getHostPkg() + ".stub_receiver";
    }

    public static String getProcessName(int bPid) {
        return TheUniverseCore.getHostPkg() + ":p" + bPid;
    }
}
