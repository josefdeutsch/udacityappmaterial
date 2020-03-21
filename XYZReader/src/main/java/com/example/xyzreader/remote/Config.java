package com.example.xyzreader.remote;

import android.net.Uri;
import android.support.v4.text.TextUtilsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static com.example.xyzreader.data.ArticleLoader.Query.PUBLISHED_DATE;

public class Config {

    public static final String ERRORFETCHINGJSON = "Error fetching items JSON";
    public static final String ERRORPARSINGJSON = "Error parsing items JSON";
    public static final String JSONEXCEPTIONMOD = "Expected JSONArray";

    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;
    public static final int STAGGEREDGRIDCOLUMNCOUNT = 2;

    public static final String ILLEGALARGUMENTEXCEPMOD = "invalid orientation";
    public static final String ARG_ITEM_ID = "item_id";
    public static final String SHOW_SWIPE_MESSAGE = "show_swipe_message";

    public static final String PACKAGE = "package:";

    public static final String STATUSBARHEIGHT = "status_bar_height";
    public static final String DEFTYPE = "dimen";
    public static final String DEFPACKAGE = "android";

    public static final String RESOURCETYPE = "ResourceType";

    public static final String XYZREADERNAME = "xyzReader";
    public static final String TEXTTYPE = "text/plain";
    public static final String SAMPLETEXT = "Some sample text";

    public static final String HTMLREGEX = "(\r\n|\n)";
    public static final String HTMLNEWLINE = "<br />";
    public static final String BYFONTCOLOUR = " by <font color='#ffffff'>";
    public static final String ENDOFFONT = "</font>";

    public static final String NOTAVAILABLE = "N/A";

    public static final String JAVAREGEXPATTERNMATCHER = "([A-Z] [^\\.?]*[\\.!?])";
    public static final String CLEARWHITESPACEFULL = "\\s+";
    public static final String BLANKSPACE = " ";
    public static final String EMPTYNONNULL = "";
    public static final String NEWLINE = "\n";
    public static final String TYPEFACEASSETS = "Rosario-Regular.ttf";


    public static final URL BASE_URL;
    private static String TAG = Config.class.toString();

    static {
        URL url = null;
        try {
            url = new URL("https://go.udacity.com/xyz-reader-json");
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
