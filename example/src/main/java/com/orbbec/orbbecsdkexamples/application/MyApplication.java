package com.orbbec.orbbecsdkexamples.application;

import android.app.Application;

import com.orbbec.orbbecsdkexamples.utils.MyCrashHandler;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new MyCrashHandler());
    }
}
