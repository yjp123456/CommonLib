package com.commonlib;

import android.app.Application;

public class BaseApplication extends Application {

    private static BaseApplication instance;

    public static BaseApplication get() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
