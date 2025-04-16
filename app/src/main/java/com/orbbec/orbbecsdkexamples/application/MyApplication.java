package com.orbbec.orbbecsdkexamples.application;

import android.app.Application;

import com.orbbec.orbbecsdkexamples.utils.MyCrashHandler;

public class MyApplication extends Application {
    private static MyApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new MyCrashHandler());
        mInstance = this;
    }

    public static MyApplication getInstance() {
        return mInstance;
    }
}
