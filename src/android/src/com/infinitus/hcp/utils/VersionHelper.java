package com.infinitus.hcp.utils;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by M on 16/9/9.
 * <p/>
 * build version工具类
 */
public class VersionHelper {

    private VersionHelper() {
    }

    /**
     * 获取build version.
     *
     * @param context application context
     * @return build version
     */
    public static int applicationVersionCode(Context context) {
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionCode;
    }
}