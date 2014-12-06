package com.limemobile.app.sdk.http.volley;

import java.util.HashMap;
import java.util.Map;

import com.limemobile.app.sdk.http.BasicRequest;
import com.limemobile.app.sdk.http.JSONResponseListener;

public abstract class VolleyClientRequest extends BasicRequest {
    protected Map<String, String> mRequestParams;

    public VolleyClientRequest(String domain, String host, String path,
            JSONResponseListener listener) {
        super(domain, host, path, listener);

        mRequestParams = new HashMap<String, String>();
    }

    public void setRequestParams(Map<String, String> requestParams) {
        mRequestParams = requestParams;
    }

    public Map<String, String> getRequestParams() {
        return mRequestParams;
    } 
    
}
