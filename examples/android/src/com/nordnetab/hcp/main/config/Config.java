package com.nordnetab.hcp.main.config;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

/**
 * Created by M on 16/9/9.
 * <p/>
 * config配置
 */
public class Config {

    private static final String CONFIG_FILE = "hcp.json";

    private String configUrl;
    private String webUrl;
    private int nativeInterfaceVersion;

    public static Config getDefaultConfig() {
        Config config = new Config();
        config.configUrl = "";
        config.webUrl = "";
        config.nativeInterfaceVersion = 1;
        return config;
    }

    /**
     * 获取服务器hcp.json url
     *
     * @return url
     */
    public String getConfigUrl() {
        return configUrl;
    }

//    /**
//     * 设置服务器hcp.json url
//     *
//     * @param configUrl url
//     */
//    public void setConfigUrl(String configUrl) {
//        this.configUrl = configUrl;
//    }

    /**
     * 设置服务器文件夹 url
     *
     * @param webUrl url
     */
    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
        configUrl = webUrl + CONFIG_FILE;
    }

//    /**
//     * Setter for the flag if updates auto download is allowed.
//     *
//     * @param isAllowed set to <code>true</code> to allow automatic update downloads.
//     */
//    public void allowUpdatesAutoDownload(boolean isAllowed) {
//        allowUpdatesAutoDownload = isAllowed;
//    }
//
//    /**
//     * Getter for the flag if updates auto download is allowed.
//     * By default it is on, but you can disable it from JavaScript.
//     *
//     * @return <code>true</code> if automatic downloads are enabled, <code>false</code> - otherwise.
//     */
//    public boolean isAutoDownloadIsAllowed() {
//        return allowUpdatesAutoDownload;
//    }
//
//    /**
//     * Setter for the flag if updates auto installation is allowed.
//     *
//     * @param isAllowed set to <code>true</code> to allow automatic installation for the loaded updates.
//     */
//    public void allowUpdatesAutoInstall(boolean isAllowed) {
//        allowUpdatesAutoInstall = isAllowed;
//    }
//
//    /**
//     * Getter for the flag if updates auto installation is allowed.
//     * By default it is on, but you can disable it from JavaScript.
//     *
//     * @return <code>true</code> if automatic installation is enabled, <code>false</code> - otherwise.
//     */
//    public boolean isAutoInstallIsAllowed() {
//        return allowUpdatesAutoInstall;
//    }

    /**
     * 获取app版本
     *
     * @return app版本
     * */
    public int getNativeInterfaceVersion() {
        return nativeInterfaceVersion;
    }

    /**
     * 设置app版本
     *
     * @param version app版本
     * */
    void setNativeInterfaceVersion(int version) {
        nativeInterfaceVersion = version > 0 ? version : 1;
    }
}
