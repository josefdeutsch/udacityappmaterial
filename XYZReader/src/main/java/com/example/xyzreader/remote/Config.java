package com.example.xyzreader.remote;

import android.support.v4.text.TextUtilsCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

public class Config {
    public static final URL BASE_URL;
    private static String TAG = Config.class.toString();

    static {
        URL url = null;
        try {
            url = new URL("https://go.udacity.com/xyz-reader-json" );
        } catch (IOException e) {
            Log.e(TAG, "Error fetching url");
            throw new RuntimeException(e);
        }

        BASE_URL = url;
    }
    public static boolean isRTL() {
        return TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }

}
