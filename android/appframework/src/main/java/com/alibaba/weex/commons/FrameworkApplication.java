package com.alibaba.weex.commons;

import android.app.Application;

import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;

/**
 * Created by Admin on 2017/10/13.
 */

public class FrameworkApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        PlatformConfig.setWeixin("wx1e6833878049e18f", "b5052e89425d401ec7a8951f06eba63d");
        PlatformConfig.setQQZone("1106378602", "1DFZNhZI5RJghFMv");
        Config.DEBUG = true;
        UMShareAPI.get(this);
    }
}
