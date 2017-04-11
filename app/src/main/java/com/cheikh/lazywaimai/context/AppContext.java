package com.cheikh.lazywaimai.context;

import android.content.Context;
import android.support.multidex.MultiDex;

import com.baidu.mapapi.SDKInitializer;
import com.cheikh.lazywaimai.controller.MainController;
import com.cheikh.lazywaimai.manager.LbsManager;
import com.cheikh.lazywaimai.module.ApplicationModule;
import com.cheikh.lazywaimai.module.library.ContextProvider;
import com.cheikh.lazywaimai.module.library.InjectorModule;
import com.cheikh.lazywaimai.module.qualifiers.ShareDirectory;
import com.cheikh.lazywaimai.network.GsonHelper;
import com.cheikh.lazywaimai.util.Injector;
import com.cheikh.lazywaimai.util.PreferenceUtil;
import com.cheikh.lazywaimai.util.ToastUtil;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.File;

import javax.inject.Inject;

import cn.bmob.v3.Bmob;
import cn.sharesdk.framework.ShareSDK;
import dagger.ObjectGraph;

public class AppContext extends android.support.multidex.MultiDexApplication implements Injector {

    private static AppContext mInstance;

    private ObjectGraph mObjectGraph; //依赖注解
    private RefWatcher mRefWatcher;  //检测内存泄露

    @Inject
    MainController mMainController;

    @Inject
    @ShareDirectory
    File mShareLocation;

    public static AppContext getContext() {
        return mInstance;
    }

    public RefWatcher getRefWatcher() {

        return mRefWatcher;
    }

    public MainController getMainController() {
        return mMainController;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        // 吐司初始化
        ToastUtil.init(this);

        // 本地存储工具类初始化
        PreferenceUtil.init(this, GsonHelper.builderGson());

        // 日志打印器初始化
        Logger.init(getPackageName()).setLogLevel(AppConfig.DEBUG ? LogLevel.FULL : LogLevel.NONE);

        // bugly初始化
        CrashReport.initCrashReport(this, "1a859a4b54", false);

        // LeakCanary
        mRefWatcher = LeakCanary.install(this);//直白的展现Android中的内存泄露

        // 依赖注解初始化
        mObjectGraph = ObjectGraph.create(
                new ApplicationModule(),
                new ContextProvider(this),
                new InjectorModule(this)
        );
        mObjectGraph.inject(this);
        // 百度地图在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        // 初始化百度定位服务
        LbsManager.getInstance().init(this);
        //数据库初始化
//        LitePal.initialize(this);
        //初始化ShareSDK
        ShareSDK.initSDK(this);
        //Bmob初始化
        Bmob.initialize(this,"037271e76e2d96a4950d5bd2457f034f");

    }

    @Override
    public void inject(Object object) {
        mObjectGraph.inject(object);
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}