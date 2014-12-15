package com.limemobile.app.sdk.http.volley.internal;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import org.apache.http.conn.ssl.SSLSocketFactory;

import android.content.Context;
import android.util.Log;

public class CustomSSLSocketFactory {
    protected static final String TAG = "CustomSSLSocketFactory";

    public static SSLSocketFactory getSSLSocketFactory(
            final Context context) {
        SSLSocketFactory ret = null;

        try {
            // TODO替换自己的key
            final KeyStore ks = KeyStore.getInstance("BKS");
            // final InputStream inputStream =
            // context.getResources().openRawResource(R.raw.certs);
            // ks.load(inputStream,
            // context.getString(R.string.store_pass).toCharArray());
            // inputStream.close();
            ret = new SSLSocketFactory(ks);
        } catch (UnrecoverableKeyException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (KeyStoreException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (KeyManagementException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (NoSuchAlgorithmException ex) {
            Log.d(TAG, ex.getMessage());
        } catch (Exception ex) {
            Log.d(TAG, ex.getMessage());
        }

        return ret;
    }
}
