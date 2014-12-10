package com.limemobile.app.plugin;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Looper;

import com.limemobile.app.plugin.internal.PluginClientInfo;

public abstract class PluginClientService extends Service implements
        IPluginService {
    protected Service mContext;

    protected Service mProxyService;

    protected PluginClientInfo mPluginPackage;

    @Override
    public void setDelegate(Service pluginHostService,
            PluginClientInfo pluginPackage) {
        mContext = pluginHostService;
        mProxyService = pluginHostService;
        mPluginPackage = pluginPackage;
    }

    @Override
    public void onCreate() {
        if (mProxyService == null) {
            mContext = this;
            super.onCreate();
        }
    }

    @Override
    @Deprecated
    public void onStart(Intent intent, int startId) {
        if (mProxyService == null) {
            super.onStart(intent, startId);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mProxyService == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        return 0;
    }

    @Override
    public void onDestroy() {
        if (mProxyService == null) {
            super.onDestroy();
        }
    }

    @Override
    public Looper getMainLooper() {
        if (mProxyService == null) {
            return super.getMainLooper();
        } else {
            return mProxyService.getMainLooper();
        }
    }

    @Override
    public Resources getResources() {
        if (mProxyService == null) {
            return super.getResources();
        } else {
            return mProxyService.getResources();
        }
    }

    @Override
    public ClassLoader getClassLoader() {
        if (mProxyService == null) {
            return super.getClassLoader();
        } else {
            return mProxyService.getClassLoader();
        }
    }

    @Override
    public Resources.Theme getTheme() {
        if (mProxyService == null) {
            return super.getTheme();
        } else {
            return mProxyService.getTheme();
        }
    }

    @Override
    public String getPackageName() {
        if (mProxyService == null) {
            return super.getPackageName();
        } else {
            return mPluginPackage.mPackageName;
        }
    }

    @Override
    public AssetManager getAssets() {
        if (mProxyService == null) {
            return super.getAssets();
        }
        return mProxyService.getAssets();
    }

    @Override
    public ContentResolver getContentResolver() {
        if (mProxyService == null) {
            return super.getContentResolver();
        }
        return mProxyService.getContentResolver();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        if (mProxyService == null) {
            return super.getApplicationInfo();
        }
        return mProxyService.getApplicationInfo();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        if (mProxyService == null) {
            return super.getSharedPreferences(name, mode);
        } else {
            return mProxyService.getSharedPreferences(name, mode);
        }
    }

    @Override
    public Context getApplicationContext() {
        if (mProxyService == null) {
            return super.getApplicationContext();
        } else {
            return mProxyService.getApplicationContext();
        }
    }

    @Override
    public Object getSystemService(String name) {
        if (mProxyService == null) {
            return super.getSystemService(name);
        } else {
            return mProxyService.getSystemService(name);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mProxyService == null) {
            super.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onLowMemory() {
        if (mProxyService == null) {
            super.onLowMemory();
        }
    }

    @Override
    public void onTrimMemory(int level) {
        if (mProxyService == null) {
            super.onTrimMemory(level);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (mProxyService == null) {
            return super.onUnbind(intent);
        }
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        if (mProxyService == null) {
            super.onRebind(intent);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (mProxyService == null) {
            super.onTaskRemoved(rootIntent);
        }
    }

}
