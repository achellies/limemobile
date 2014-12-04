package com.limemobile.app.lbs.location.baidu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.baidu.location.BDGeofence;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.BDLocationStatusCodes;
import com.baidu.location.GeofenceClient;
import com.baidu.location.GeofenceClient.OnAddBDGeofencesResultListener;
import com.baidu.location.GeofenceClient.OnGeofenceTriggerListener;
import com.baidu.location.GeofenceClient.OnRemoveBDGeofencesResultListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.limemobile.app.lbs.location.GeoFenceListener;
import com.limemobile.app.lbs.location.LocationListener;
import com.limemobile.app.lbs.location.LocationMode;

public class BaiduLBSManager implements BDLocationListener,
        OnGeofenceTriggerListener {
    static final int MSG_LOCATION = 1;
    static final int MSG_GEOFENCE_ENTER = 2;
    static final int MSG_GEOFENCE_EXIT = 3;

    private static BaiduLBSManager sInstance;

    private final Context mAppCtx;
    private final Handler mHandler;

    private final LocationClient mLocationClient;
    private final GeofenceClient mGeoFenceClient;

    private final List<LocationListener<BDLocation>> mLocationListeners;
    private final Map<String, GeoFenceListener> mGeoFenceListeners;

    private BDLocation mLastKnowLocation;

    private BaiduLBSManager(Context ctx) {
        super();
        mAppCtx = ctx.getApplicationContext();
        mHandler = new MyHandler(ctx.getMainLooper());

        mLocationClient = new LocationClient(mAppCtx);
        mGeoFenceClient = new GeofenceClient(mAppCtx);

        mLocationListeners = Collections
                .synchronizedList(new ArrayList<LocationListener<BDLocation>>());
        mGeoFenceListeners = Collections
                .synchronizedMap(new HashMap<String, GeoFenceListener>());

        mLocationClient.registerLocationListener(this);
        mGeoFenceClient.registerGeofenceTriggerListener(this);
    }

    public static BaiduLBSManager sharedInstance(Context ctx) {
        if (sInstance == null) {
            synchronized (BaiduLBSManager.class) {
                if (sInstance == null) {
                    sInstance = new BaiduLBSManager(ctx);
                }
            }
        }
        return sInstance;
    }

    public BDLocation getLastKnownLocation() {
        return mLastKnowLocation;
    }

    /**
     * 
     * @param mode
     *            设置定位模式
     * @param scanSpan
     *            设置发起定位请求的间隔时间
     */
    public void config(LocationMode mode, int scanSpan) {
        LocationClientOption option = new LocationClientOption();

        com.baidu.location.LocationClientOption.LocationMode tempMode = com.baidu.location.LocationClientOption.LocationMode.Hight_Accuracy;
        if (LocationMode.LowPower == mode) {
            tempMode = com.baidu.location.LocationClientOption.LocationMode.Battery_Saving;
        }
        // 设置定位模式
        option.setLocationMode(tempMode);

        // gcj02 国测局加密经纬度坐标
        // bd0911 百度加密经纬度坐标
        // bd09 百度加密墨卡托坐标
        String tempcoor = "gcj02";
        // 返回的定位结果是百度经纬度
        option.setCoorType(tempcoor);

        if (scanSpan < 1000) {
            scanSpan = 1000;
        }
        option.setScanSpan(scanSpan);

        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    public void startLocation() {
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
    }

    public void requestLocation() {
        mLocationClient.requestLocation();
    }

    public void stopLocation() {
        mLocationClient.stop();
    }

    @Override
    public void onReceiveLocation(BDLocation bdLocation) {
        if (bdLocation == null) {
            return;
        }
        mLastKnowLocation = bdLocation;
        // StringBuffer sb = new StringBuffer(256);
        // sb.append("time : ");
        // location.setTime(bdLocation.getTime());
        // sb.append(bdLocation.getLocType());
        // sb.append("\nlatitude : ");
        // sb.append(location.getLatitude());
        // sb.append("\nlontitude : ");
        // sb.append(location.getLongitude());
        // sb.append("\nradius : ");
        // sb.append(location.getRadius());
        // if (location.getLocType() == BDLocation.TypeGpsLocation){
        // sb.append("\nspeed : ");
        // sb.append(location.getSpeed());
        // sb.append("\nsatellite : ");
        // sb.append(location.getSatelliteNumber());
        // sb.append("\ndirection : ");
        // sb.append("\naddr : ");
        // sb.append(location.getAddrStr());
        // sb.append(location.getDirection());
        // } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
        // sb.append("\naddr : ");
        // sb.append(location.getAddrStr());
        // //运营商信息
        // sb.append("\noperationers : ");
        // sb.append(location.getOperators());
        // }
        mHandler.obtainMessage(MSG_LOCATION, bdLocation).sendToTarget();
    }

    public void addLocationListener(LocationListener<BDLocation> listener) {
        mLocationListeners.add(listener);
    }

    public void removeLocationListener(LocationListener<BDLocation> listener) {
        mLocationListeners.remove(listener);
    }

    public void startGeoFence() {
        if (!mGeoFenceClient.isStarted()) {
            mGeoFenceClient.start();
        }
    }

    public void stopGeoFence() {
        mGeoFenceClient.stop();
    }

    /**
     * 
     * @param geofenceId
     * @param longtitude
     * @param latotide
     * @param expirationDruation
     *            围栏持续时间（ms）
     */
    public void addGeoFence(final String geofenceId, double longtitude,
            double latotide, long expirationDruation,
            final GeoFenceListener listener) {
        BDGeofence fence = new BDGeofence.Builder()
                .setGeofenceId(geofenceId)
                .setCircularRegion(longtitude, latotide,
                        BDGeofence.RADIUS_TYPE_SMALL)
                .setExpirationDruation(expirationDruation)
                .setCoordType(BDGeofence.COORD_TYPE_GCJ).build();
        mGeoFenceClient.setInterval(199009999);
        mGeoFenceClient.addBDGeofence(fence,
                new OnAddBDGeofencesResultListener() {

                    @Override
                    public void onAddBDGeofencesResult(int statusCode,
                            String geofenceId) {
                        if (statusCode == BDLocationStatusCodes.SUCCESS) {
                            mGeoFenceListeners.put(geofenceId, listener);
                            // 在添加地理围栏成功后，开启地理围栏服务，对本次创建成功且已进入的地理围栏，可以实时的提醒
                            if (mGeoFenceClient.isStarted()) {
                                mGeoFenceClient.startGeofenceScann();
                            }
                        }
                    }

                });
    }

    public void removeGeoFence(String geofenceId) {
        List<String> fences = new ArrayList<String>();
        fences.add(geofenceId);
        mGeoFenceClient.removeBDGeofences(fences,
                new OnRemoveBDGeofencesResultListener() {

                    @Override
                    public void onRemoveBDGeofencesByRequestIdsResult(
                            int statusCode, String[] geofenceRequestIds) {
                        if (statusCode == BDLocationStatusCodes.SUCCESS) {
                            for (String key : geofenceRequestIds) {
                                if (mGeoFenceListeners.containsKey(key)) {
                                    mGeoFenceListeners.remove(key);
                                }
                            }
                        }
                    }

                });
    }

    @Override
    public void onGeofenceExit(String geofenceId) {
        mHandler.obtainMessage(MSG_GEOFENCE_EXIT, geofenceId).sendToTarget();
    }

    @Override
    public void onGeofenceTrigger(String geofenceId) {
        mHandler.obtainMessage(MSG_GEOFENCE_ENTER, geofenceId).sendToTarget();
    }

    private class MyHandler extends Handler {

        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String geofenceId = null;
            switch (msg.what) {
            case MSG_LOCATION:
                BDLocation location = (BDLocation) msg.obj;
                if (location != null) {
                    for (LocationListener<BDLocation> listener : mLocationListeners) {
                        listener.onReceiveLocation(location);
                    }
                }
                break;
            case MSG_GEOFENCE_ENTER:
                geofenceId = (String) msg.obj;
                if (!TextUtils.isEmpty(geofenceId)
                        && mGeoFenceListeners.containsKey(geofenceId)) {
                    mGeoFenceListeners.get(geofenceId).onGeofenceEnter(
                            geofenceId);
                }
                break;
            case MSG_GEOFENCE_EXIT:
                geofenceId = (String) msg.obj;
                if (!TextUtils.isEmpty(geofenceId)
                        && mGeoFenceListeners.containsKey(geofenceId)) {
                    mGeoFenceListeners.get(geofenceId).onGeofenceExit(
                            geofenceId);
                }
                break;
            }
            super.handleMessage(msg);
        }

    }
}
