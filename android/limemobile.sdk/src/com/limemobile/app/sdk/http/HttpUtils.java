package com.limemobile.app.sdk.http;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.util.DisplayMetrics;

public class HttpUtils {
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
}
