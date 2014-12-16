package com.limemobile.app.sdk.http.volley;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.json.JSONObject;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Build;
import android.text.TextUtils;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;
import com.limemobile.app.sdk.http.HttpUtils;
import com.limemobile.app.sdk.http.volley.internal.VolleyJSONRequest;
import com.loopj.android.http.PersistentCookieStore;

public class VolleyClient {
    protected final Context mContext;
    protected final RequestQueue mRequestQueue;
    protected final String mUserAgent;
    protected final PersistentCookieStore mCookieStore;

    /*
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     * 
     * Volley doesn't actually make HTTP requests itself, and thus doesn't
     * manage Cookies directly. It instead uses an instance of HttpStack to do
     * this. There are two main implementations:
     * 
     * HurlStack: Uses HttpUrlConnection under the hood HttpClientStack: uses
     * Apache HttpClient under the hood Cookie management is the responsibility
     * of those HttpStacks. And they each handle Cookies differently.
     * 
     * If you need to support < 2.3, then you should use the HttpClientStack:
     * 
     * Configure an HttpClient instance, and pass that to Volley for it to use
     * under the hood:
     * 
     * // If you need to directly manipulate cookies later on, hold onto this
     * client // object as it gives you access to the Cookie Store
     * DefaultHttpClient httpclient = new DefaultHttpClient();
     * 
     * CookieStore cookieStore = new BasicCookieStore();
     * httpclient.setCookieStore( cookieStore );
     * 
     * HttpStack httpStack = new HttpClientStack( httpclient ); RequestQueue
     * requestQueue = Volley.newRequestQueue( context, httpStack ); The
     * advantage with this vs manually inserting cookies into the headers is
     * that you get actual cookie management. Cookies in your store will
     * properly respond to HTTP controls that expire or update them.
     */
    public VolleyClient(Context context) {
        super();
        mContext = context.getApplicationContext();
        mUserAgent = HttpUtils.createUserAgentString(mContext);

        mCookieStore = new PersistentCookieStore(mContext);

        File cacheDir = new File(context.getCacheDir(), "volley");

        HttpStack stack;
        if (Build.VERSION.SDK_INT >= 9) {
            stack = new HurlStack();
        } else {
            // Prior to Gingerbread, HttpUrlConnection was unreliable.
            // See:
            // http://android-developers.blogspot.com/2011/09/androids-http-clients.html
            stack = new HttpClientStack(
                    AndroidHttpClient.newInstance(mUserAgent));
        }

        Network network = new BasicNetwork(stack);

        mRequestQueue = new RequestQueue(new DiskBasedCache(cacheDir), network);
        mRequestQueue.start();
    }

    public List<Cookie> getCookies(String domain) {
        List<Cookie> cookies = new ArrayList<Cookie>();

        List<Cookie> allCookies = mCookieStore.getCookies();
        for (Cookie cookie : allCookies) {
            if (TextUtils.isEmpty(domain) || cookie.getDomain().equals(domain)) {
                cookies.add(cookie);
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
        List<Cookie> cookies = mCookieStore.getCookies();
        for (Cookie cookie : cookies) {
            if ((TextUtils.isEmpty(domain) || domain.equals(cookie.getDomain()))) {
                mCookieStore.deleteCookie(cookie);
            }
        }
    }

    public void cancelAllRequests(String tag) {
        mRequestQueue.cancelAll(tag);
    }

   public Request<JSONObject> get(Context context, VolleyClientRequest request) {
        return get(context, request, null);
    }

    public Request<JSONObject> get(Context context,
            VolleyClientRequest request, Map<String, String> headers) {
        return mRequestQueue.add(new VolleyJSONRequest(Request.Method.GET,
                headers, request, mCookieStore));
    }

    public Request<JSONObject> post(Context context, VolleyClientRequest request) {
        return post(context, request, null);
    }

    public Request<JSONObject> post(Context context,
            VolleyClientRequest request, Map<String, String> headers) {
        return mRequestQueue.add(new VolleyJSONRequest(Request.Method.POST,
                headers, request, mCookieStore));
    }

    public Request<JSONObject> put(Context context, VolleyClientRequest request) {
        return put(context, request, null);
    }

    public Request<JSONObject> put(Context context,
            VolleyClientRequest request, Map<String, String> headers) {
        return mRequestQueue.add(new VolleyJSONRequest(Request.Method.PUT,
                headers, request, mCookieStore));
    }

    public Request<JSONObject> delete(Context context,
            VolleyClientRequest request) {
        return delete(context, request, null);
    }

    public Request<JSONObject> delete(Context context,
            VolleyClientRequest request, Map<String, String> headers) {
        return mRequestQueue.add(new VolleyJSONRequest(Request.Method.DELETE,
                headers, request, mCookieStore));
    }

    public final RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
