package com.limemobile.app.sdk.http;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.cookie.Cookie;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;

public class HttpUtils {
    protected static final String COOKIE_DATE_FORMAT = "EEE, dd MMM yyyy hh:mm:ss z";

    public static String createUserAgentString(Context applicationContext) {
        String appName = "";
        String appVersion = "";
        int height = 0;
        int width = 0;
        DisplayMetrics display = applicationContext.getResources()
                .getDisplayMetrics();
        Configuration config = applicationContext.getResources()
                .getConfiguration();

        // Always send screen dimension for portrait mode
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            height = display.widthPixels;
            width = display.heightPixels;
        } else {
            width = display.widthPixels;
            height = display.heightPixels;
        }

        try {
            PackageInfo packageInfo = applicationContext.getPackageManager()
                    .getPackageInfo(applicationContext.getPackageName(),
                            PackageManager.GET_CONFIGURATIONS);
            appName = packageInfo.packageName;
            appVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignore) {
            // this should never happen, we are looking up ourself
        }

        // Tries to conform to default android UA string without the Safari /
        // webkit noise, plus adds the screen dimensions
        return String
                .format("%1$s/%2$s (%3$s; U; Android %4$s; %5$s-%6$s; %12$s Build/%7$s; %8$s) %9$dX%10$d %11$s %12$s",
                        appName, appVersion, System.getProperty("os.name",
                                "Linux"), Build.VERSION.RELEASE, config.locale
                                .getLanguage().toLowerCase(), config.locale
                                .getCountry().toLowerCase(), Build.ID,
                        Build.BRAND, width, height, Build.MANUFACTURER,
                        Build.MODEL);
    }

    public static Bundle getCookiesBundle(List<Cookie> cookies) {
        Bundle bundle = new Bundle();
        if (cookies == null) {
            return bundle;
        }
        Calendar calendar = Calendar.getInstance();
        Map<String, ArrayList<String>> cookiesMap = new HashMap<String, ArrayList<String>>();
        for (Cookie cookie : cookies) {
            String domain = cookie.getDomain();
            if (TextUtils.isEmpty(domain)) {
                domain = "";
            }
            StringBuilder builder = new StringBuilder();
            builder.append(cookie.getName());
            builder.append("=");
            builder.append(cookie.getValue());
            builder.append("; domain=");
            builder.append(cookie.getDomain());
            if (cookie.getExpiryDate() != null) {
                builder.append("; expires=");
                calendar.setTime(cookie.getExpiryDate());
                builder.append(new SimpleDateFormat(COOKIE_DATE_FORMAT)
                        .format(calendar.getTimeInMillis()));
            }
            builder.append("; path=");
            builder.append(cookie.getPath());
            builder.append("; version=");
            builder.append(cookie.getVersion());
            ArrayList<String> list = null;
            if (cookiesMap.containsKey(domain)) {
                list = cookiesMap.get(domain);
                list.add(builder.toString());
            } else {
                list = new ArrayList<String>();
                list.add(builder.toString());
                cookiesMap.put(domain, list);
            }
        }
        Set<String> keys = cookiesMap.keySet();
        for (String key : keys) {
            bundle.putStringArrayList(key, cookiesMap.get(key));
        }
        return bundle;
    }

    public static boolean isNetworkAvaliable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            final NetworkInfo net = connectivityManager.getActiveNetworkInfo();
            if (net != null && net.isAvailable() && net.isConnected()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
