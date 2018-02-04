package com.gaoxx.yasuo.base;

import android.app.Application;

import com.uzmap.pkg.openapi.APICloud;

/**
 * 创建时间: 2018/2/2
 * gxx
 * 注释描述:xxxxx
 */

public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        APICloud.initialize(this);//初始化APICloud，SDK中所有的API均需要初始化后方可调用执行
    }
}
