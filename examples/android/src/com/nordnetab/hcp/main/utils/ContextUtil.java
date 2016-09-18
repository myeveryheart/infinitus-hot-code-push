package com.nordnetab.hcp.main.utils;

/**
 * Created by M on 16/9/9.
 */
import android.app.Application;

public class ContextUtil extends Application {
    private static ContextUtil instance;

    public static ContextUtil getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        instance = this;
    }
}