package com.limemobile.app.lbs.location;

public interface LocationListener<T> {

    public void onReceiveLocation(T location);

}
