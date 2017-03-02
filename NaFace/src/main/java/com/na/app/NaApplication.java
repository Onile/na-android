package com.na.app;

import android.app.Application;

import com.na.api.NaApi;

/**
 * @actor:taotao
 * @DATE: 17/3/1
 */

public class NaApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NaApi.init(this);
    }
}
