package com.limemobile.app.sdk.http.loopj;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;

import android.content.Context;
import android.text.TextUtils;

import com.limemobile.app.sdk.http.HttpUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.SyncHttpClient;

public class AndroidAsyncHttpClient {
    public static final int DEFAULT_MAX_CONNECTIONS = 10;
    public static final int DEFAULT_CONNECT_TIMEOUT = 15 * 1000;
    public static final int DEFAULT_RESPONSE_TIMEOUT = 15 * 1000;
    public static final int DEFAULT_MAX_RETRIES = 3;
    public static final int DEFAULT_RETRY_SLEEP_TIME_MILLIS = 2000;

    protected final Context mContext;
    protected AsyncHttpClient mClient;
    protected boolean isSync;
    protected final String mUserAgent;

    public AndroidAsyncHttpClient(Context context) {
        this(context, false);
    }

    public AndroidAsyncHttpClient(Context context, boolean isSync) {
        super();

        mContext = context.getApplicationContext();
        mUserAgent = HttpUtils.createUserAgentString(mContext);
        this.isSync = isSync;
        initClient();
    }

    public void setAsync(boolean async) {
        if (isSync != async || mClient == null) {
            isSync = !async;
            initClient();
        }
    }

    public List<Cookie> getCookies(String domain) {
        List<Cookie> cookies = new ArrayList<Cookie>();
        PersistentCookieStore cookieStore = getCookieStore();
        if (cookieStore != null) {
            List<Cookie> allCookies = cookieStore.getCookies();
            for (Cookie cookie : allCookies) {
                if (TextUtils.isEmpty(domain)
                        || cookie.getDomain().equals(domain)) {
                    cookies.add(cookie);
                }
            }
        }
        return cookies;
    }

    public Cookie getCookie(String domain, String name) {
        if (TextUtils.isEmpty(name)) {
            throw new IllegalArgumentException();
        }
        List<Cookie> cookies = getCookies(domain);
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())
                        && (TextUtils.isEmpty(domain) || domain.equals(cookie
                                .getDomain()))) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public void clearCookies(String domain) {
        PersistentCookieStore cookieStore = getCookieStore();
        if (cookieStore == null) {
            return;
        }
        List<Cookie> cookies = cookieStore.getCookies();
        for (Cookie cookie : cookies) {
            if ((TextUtils.isEmpty(domain) || domain.equals(cookie.getDomain()))) {
                cookieStore.deleteCookie(cookie);
            }
        }
    }

    public void setConnectTimeout(int value) {
        mClient.setConnectTimeout(value);
    }

    public void setResponseTimeout(int value) {
        mClient.setResponseTimeout(value);
    }

    public void setMaxConnections(int maxConnections) {
        mClient.setMaxConnections(maxConnections);
    }

    public void setMaxRetriesAndTimeout(int retries, int timeout) {
        mClient.setMaxRetriesAndTimeout(retries, timeout);
    }

    public void cancelRequests(final Context context,
            final boolean mayInterruptIfRunning) {
        mClient.cancelRequests(context, mayInterruptIfRunning);
    }

    public void cancelAllRequests(boolean mayInterruptIfRunning) {
        mClient.cancelAllRequests(mayInterruptIfRunning);
    }

    public void setProxy(String hostname, int port) {
        mClient.setProxy(hostname, port);
    }

    public void setProxy(String hostname, int port, String username,
            String password) {
        mClient.setProxy(hostname, port, username, password);
    }

    public RequestHandle get(Context context, AndroidAsyncClientRequest request) {
        return mClient.get(context, request.getUrl(),
                request.getRequestParams(), request.getResponseHanlder());
    }

    public RequestHandle post(Context context, AndroidAsyncClientRequest request) {
        return mClient.post(context, request.getUrl(),
                request.getRequestParams(), request.getResponseHanlder());
    }

    public RequestHandle put(Context context, AndroidAsyncClientRequest request) {
        return mClient.post(context, request.getUrl(),
                request.getRequestParams(), request.getResponseHanlder());
    }

    public RequestHandle delete(Context context,
            AndroidAsyncClientRequest request) {
        return mClient.delete(context, request.getUrl(), null,
                request.getRequestParams(), request.getResponseHanlder());
    }

    private PersistentCookieStore getCookieStore() {
        return (PersistentCookieStore) mClient.getHttpContext().getAttribute(
                ClientContext.COOKIE_STORE);
    }

    private void initClient() {
        if (isSync) {
            mClient = new SyncHttpClient(true, 80, 443);
        } else {
            mClient = new AsyncHttpClient(true, 80, 443);
        }

        mClient.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT);
        mClient.setResponseTimeout(DEFAULT_RESPONSE_TIMEOUT);
        mClient.setMaxConnections(DEFAULT_MAX_CONNECTIONS);
        mClient.setMaxRetriesAndTimeout(DEFAULT_MAX_RETRIES,
                DEFAULT_RETRY_SLEEP_TIME_MILLIS);
        mClient.setThreadPool(Executors.newCachedThreadPool());
        mClient.setUserAgent(mUserAgent);

        PersistentCookieStore cookieStore = new PersistentCookieStore(mContext);
        mClient.setCookieStore(cookieStore);
    }
}
