package com.limemobile.app.sdk.http;

public abstract class BasicRequest {
    public static final int DEFAULT_SOCKET_TIMEOUT = 15 * 1000;
    public static final int DEFAULT_MAX_RETRIES = 3;

    protected final String mDomain;
    protected final String mHost;
    protected final String mPath;
    protected JSONResponseListener mListener;
    protected boolean mShouldCache = true;
    protected int mTimeout = DEFAULT_SOCKET_TIMEOUT;
    protected int mRetryCount = DEFAULT_MAX_RETRIES;

    public BasicRequest(String domain, String host, String path,
            JSONResponseListener listener) {
        super();
        mDomain = domain;
        mHost = host;
        mPath = path;
        mListener = listener;
    }

    public final JSONResponseListener getJSONResponseListener() {
        return mListener;
    }

    public void setResponseHandler(JSONResponseListener listener) {
        mListener = listener;
    }

    public String getDomain() {
        return mDomain;
    }

    public String getHost() {
        return mHost;
    }

    public String getPath() {
        return mPath;
    }

    public void setShouldCache(boolean shouldCache) {
        mShouldCache = shouldCache;
    }

    public final boolean shouldCache() {
        return mShouldCache;
    }

    public final int getTimeoutMs() {
        return mTimeout;
    }

    public final int getRetryCount() {
        return mRetryCount;
    }

    public String getUrl() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(mHost);
        stringBuilder.append(mPath);

        return stringBuilder.toString();
    }

    public void parseResponse(BasicJSONResponse response)throws JSONException {
    }
}
