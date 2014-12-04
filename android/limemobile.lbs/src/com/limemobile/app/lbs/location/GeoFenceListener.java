package com.limemobile.app.lbs.location;

public interface GeoFenceListener {
    public void onGeofenceEnter(String geofenceId);

    public void onGeofenceExit(String geofenceId);
}
