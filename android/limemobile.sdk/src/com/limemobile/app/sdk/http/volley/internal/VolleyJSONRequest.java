package com.limemobile.app.sdk.http.volley.internal;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.limemobile.app.sdk.http.BasicJSONResponse;
import com.limemobile.app.sdk.http.volley.VolleyClientRequest;
import com.loopj.android.http.PersistentCookieStore;

public class VolleyJSONRequest extends Request<JSONObject> implements
        RetryPolicy {
    private static final String UTF8_BOM = "\uFEFF";
    private static final String SET_COOKIE_KEY = "Set-Cookie";
    private static final String COOKIE_KEY = "Cookie";

    protected final Map<String, String> mHeaders;
    protected final VolleyClientRequest mBasicRequest;
    protected BasicJSONResponse mBasicJSONResponse;
    protected final PersistentCookieStore mCookieStore;
    protected final List<Cookie> mCookies = new ArrayList<Cookie>();

    public VolleyJSONRequest(int method, Map<String, String> headers,
            VolleyClientRequest request, PersistentCookieStore cookieStore) {
        super(method, request.getUrl(), new ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        this.mBasicRequest = request;
        this.mHeaders = headers;
        this.mCookieStore = cookieStore;

        List<Cookie> allCookies = mCookieStore.getCookies();
        for (Cookie cookie : allCookies) {
            if (TextUtils.isEmpty(mBasicRequest.getDomain())
                    || (cookie.getDomain() != null && cookie.getDomain()
                            .equals(mBasicRequest.getDomain()))) {
                mCookies.add(cookie);
            }
        }

        this.setShouldCache(this.mBasicRequest.shouldCache());
        this.setRetryPolicy(this);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mBasicRequest.getRequestParams();
    }

    @Override
    public String getUrl() {
        String url = mBasicRequest.getUrl();
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(url);
        Map<String, String> requestParams = mBasicRequest.getRequestParams();
        if ((Request.Method.GET == getMethod() || Request.Method.DELETE == getMethod())
                && requestParams != null && !requestParams.isEmpty()) {
            if (url.contains("?")) {
                if (!url.endsWith("&")) {
                    urlBuilder.append("&");
                }
            } else {
                urlBuilder.append("?");
            }
            Set<Map.Entry<String, String>> set = requestParams.entrySet();
            Iterator<Map.Entry<String, String>> iterator = set.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                urlBuilder.append(entry.getKey());
                urlBuilder.append("=");
                urlBuilder.append(entry.getValue());
                if (iterator.hasNext()) {
                    urlBuilder.append("&");
                }
            }
        }
        return urlBuilder.toString();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = mHeaders != null ? mHeaders
                : new HashMap<String, String>();

        StringBuilder builder = new StringBuilder();
        if (headers.containsKey(COOKIE_KEY)) {
            builder.append("; ");
            builder.append(headers.get(COOKIE_KEY));
        }
        for (Cookie cookie : mCookies) {
            builder.append(cookie.getName());
            builder.append("=");
            builder.append(cookie.getValue());
            builder.append(";");
        }
        headers.put(COOKIE_KEY, builder.toString());

        return headers;
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        if (mBasicRequest != null
                && mBasicRequest.getJSONResponseListener() != null
                && mBasicJSONResponse != null) {
            mBasicRequest.getJSONResponseListener().onResponse(
                    mBasicJSONResponse);
        }
    }

    @Override
    public void deliverError(VolleyError error) {
        if (mBasicRequest != null
                && mBasicRequest.getJSONResponseListener() != null
                && mBasicJSONResponse != null) {
            mBasicRequest.getJSONResponseListener().onResponse(
                    mBasicJSONResponse);
        }
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        int statusCode = response.statusCode;
        Map<String, String> headers = response.headers;
        mBasicJSONResponse = new BasicJSONResponse(statusCode, headers);

        if (headers != null) {
            if (headers.containsKey(SET_COOKIE_KEY)) {
                String cookieString = headers.get(SET_COOKIE_KEY);
                if (!TextUtils.isEmpty(cookieString)) {
                    Cookie cookie = parseRawCookie(cookieString);
                    if (cookie != null) {
                        mCookieStore.addCookie(cookie);
                    }
                }
            }
        }

        JSONObject jsonObject = null;
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            Object result = null;
            if (!TextUtils.isEmpty(jsonString)) {
                jsonString = jsonString.trim();
                if (jsonString.startsWith(UTF8_BOM)) {
                    jsonString = jsonString.substring(1);
                }
                if (jsonString.startsWith("{") || jsonString.startsWith("[")) {
                    try {
                        result = new JSONTokener(jsonString).nextValue();
                    } catch (JSONException e) {
                        mBasicJSONResponse
                                .setErrorCode(BasicJSONResponse.FAILED);
                        mBasicJSONResponse.setErrorMessage(e.toString());
                    }
                }
            }
            if (result != null) {
                if (result instanceof JSONObject) {
                    mBasicJSONResponse.parseResponse((JSONObject) result);
                    return Response.success(jsonObject,
                            HttpHeaderParser.parseCacheHeaders(response));
                } else if (result instanceof JSONArray) {
                    mBasicJSONResponse.setErrorCode(BasicJSONResponse.FAILED);
                    mBasicJSONResponse.setErrorMessage(((JSONArray) result)
                            .toString());
                    return Response.error(new ParseError(response));
                } else if (result instanceof String) {
                    mBasicJSONResponse.setErrorCode(BasicJSONResponse.FAILED);
                    mBasicJSONResponse.setErrorMessage(((String) result));
                    return Response.error(new ParseError(response));
                } else {
                    mBasicJSONResponse.setErrorCode(BasicJSONResponse.FAILED);
                    mBasicJSONResponse.setErrorMessage(result.toString());
                    return Response.error(new ParseError(response));
                }
            } else {
                mBasicJSONResponse.setErrorCode(BasicJSONResponse.FAILED);
                mBasicJSONResponse.setErrorMessage(jsonString);
                return Response.error(new ParseError(response));
            }

        } catch (UnsupportedEncodingException e) {
            mBasicJSONResponse.setErrorCode(BasicJSONResponse.FAILED);
            mBasicJSONResponse.setErrorMessage(e.toString());
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public int getCurrentTimeout() {
        return mBasicRequest.getTimeoutMs();
    }

    @Override
    public int getCurrentRetryCount() {
        return mBasicRequest.getRetryCount();
    }

    @Override
    public void retry(VolleyError error) throws VolleyError {
    }

    protected BasicJSONResponse parseJSONResponse(int statusCode,
            Header[] headers) {
        BasicJSONResponse response = new BasicJSONResponse(statusCode, headers);
        return response;
    }

    private BasicClientCookie parseRawCookie(String rawCookie) {
        String[] rawCookieParams = rawCookie.split(";");

        String[] rawCookieNameAndValue = rawCookieParams[0].split("=");
        if (rawCookieNameAndValue.length != 2) {
            // throw new Exception("Invalid cookie: missing name and value.");
            return null;
        }

        String cookieName = rawCookieNameAndValue[0].trim();
        String cookieValue = rawCookieNameAndValue[1].trim();
        BasicClientCookie cookie = new BasicClientCookie(cookieName,
                cookieValue);
        for (int i = 1; i < rawCookieParams.length; i++) {
            String rawCookieParamNameAndValue[] = rawCookieParams[i].trim()
                    .split("=");

            String paramName = rawCookieParamNameAndValue[0].trim();

            if (paramName.equalsIgnoreCase("secure")) {
                cookie.setSecure(true);
            } else {
                if (rawCookieParamNameAndValue.length != 2) {
                    // throw new Exception(
                    // "Invalid cookie: attribute not a flag or missing value.");
                    return null;
                }

                String paramValue = rawCookieParamNameAndValue[1].trim();

                if (paramName.equalsIgnoreCase("expires")) {
                    Date expiryDate = null;
                    try {
                        expiryDate = DateFormat.getDateTimeInstance(
                                DateFormat.FULL, DateFormat.FULL).parse(
                                paramValue);
                        cookie.setExpiryDate(expiryDate);
                    } catch (ParseException e) {
                    }
                } else if (paramName.equalsIgnoreCase("max-age")) {
                    long maxAge = Long.parseLong(paramValue);
                    Date expiryDate = new Date(System.currentTimeMillis()
                            + maxAge);
                    cookie.setExpiryDate(expiryDate);
                } else if (paramName.equalsIgnoreCase("domain")) {
                    cookie.setDomain(paramValue);
                } else if (paramName.equalsIgnoreCase("path")) {
                    cookie.setPath(paramValue);
                } else if (paramName.equalsIgnoreCase("comment")) {
                    cookie.setComment(paramValue);
                } else {
                    // throw new
                    // Exception("Invalid cookie: invalid attribute name.");
                }
            }
        }

        return cookie;
    }
}
