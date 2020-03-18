package com.example.xyzreader.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ShareCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.ImageView;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import com.example.xyzreader.ui.components.DrawInsetsFrameLayout;
import com.example.xyzreader.ui.components.GlideApp;
import com.example.xyzreader.ui.components.ImageLoaderHelper;
import com.example.xyzreader.ui.components.MaxWidthLinearLayout;
import com.example.xyzreader.ui.components.ObservableScrollView;
import android.support.annotation.NonNull;
import android.widget.TextView;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */

public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    public static final String LAYOUT_DECISION = "layout_decision";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private ObservableScrollView mScrollView;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;
    private View linearcontainer;
    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY = 1;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;
    private View mMaxWidthContainer;
    private AppCompatActivity activity;
    private Boolean layoutdecision;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */

    public ArticleDetailFragment() {
    }

    public static android.support.v4.app.Fragment newInstance(long itemId, boolean layout) {

        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        arguments.putBoolean(LAYOUT_DECISION, layout);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }
        if (getArguments().containsKey(LAYOUT_DECISION)) {
            layoutdecision = getArguments().getBoolean(LAYOUT_DECISION);
        }
        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);

        setHasOptionsMenu(true);
    }

    public static String getArticleDetailTag(final long articleId) {
        return ARG_ITEM_ID + articleId;
    }

    private void setTransparentStatusBar() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getActivity().getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            } else {
                getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.transparentSatusBar));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    getActivity().finishAfterTransition();
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }


    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public void onInflate(Context context, AttributeSet attrs,
                          Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        mDrawInsetsFrameLayout = new DrawInsetsFrameLayout(getActivityCast(), attrs);
        mMaxWidthContainer = new MaxWidthLinearLayout(getActivityCast(), attrs);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        if (layoutdecision) {
            mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        } else {
            mRootView = inflater.inflate(R.layout.fragment_article_detail2, container, false);
        }
        Log.d(TAG, "onCreateView: " + "hello");

        mMaxWidthContainer = mRootView.findViewById(R.id.maxwidthlayout_container);
        mDrawInsetsFrameLayout = mRootView.findViewById(R.id.container);

        setTransparentStatusBar();

        if (mDrawInsetsFrameLayout != null) {

            mRootView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    insets = mDrawInsetsFrameLayout.onApplyWindowInsets(insets);
                    insets.consumeSystemWindowInsets();
                    return insets;
                }
            });

            mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
                @Override
                public void onInsetsChanged(Rect insets) {
                    mTopInset = insets.top;
                }
            });
        }

        final Toolbar toolbar = (Toolbar) mRootView.findViewById(R.id.toolbar);

        activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        AppBarLayout appBarLayout = (AppBarLayout) mRootView.findViewById(R.id.appbar);

        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) mRootView.findViewById(R.id.collapsing_toolbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle("          xyz Reader");
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle("");
                    isShow = false;
                }
            }
        });

        toolbar.setTitle(null);
        toolbar.inflateMenu(R.menu.main);

        mStatusBarColorDrawable = new ColorDrawable(0);
        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        updateStatusBar();
     //   mRootView.invalidate();
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRootView.requestApplyInsets();
    }

    private void updateStatusBar() {

        if (mPhotoView != null) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            // * f !!

            mStatusBarColorDrawable.setColor(Color.argb(Color.alpha(mMutedColor),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9)));
        }
        if (mDrawInsetsFrameLayout != null) {
            mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
        } else {
            getActivity().getWindow().setStatusBarColor(mStatusBarColorDrawable.getColor());
        }

    }
    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

         TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
         TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);

         bylineView.setMovementMethod(new LinkMovementMethod());

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);

            /**authorView.setText(Html.fromHtml(
             //                    DateUtils.getRelativeTimeSpanString(
             //                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
             //                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
             //                            DateUtils.FORMAT_ABBREV_ALL).toString()
             //                            + " by <font color='#ffffff'>"
             //                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
             //                            + "</font>"));**/

                titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
             Date publishedDate = parsePublishedDate();
             if (!publishedDate.before(START_OF_EPOCH.getTime())) {
             bylineView.setText(Html.fromHtml(
             DateUtils.getRelativeTimeSpanString(
             publishedDate.getTime(),
             System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
             DateUtils.FORMAT_ABBREV_ALL).toString()
             + " by <font color='#ffffff'>"
             + mCursor.getString(ArticleLoader.Query.AUTHOR)
             + "</font>"));

             } else {
             // If date is before 1902, just show the string
             bylineView.setText(Html.fromHtml(
             outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
             + mCursor.getString(ArticleLoader.Query.AUTHOR)
             + "</font>"));

             }

            final String data = Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY).replaceAll("(\r\n|\n)", "<br />")).toString();
            final NestedScrollView scrollView = (NestedScrollView) mRootView.findViewById(R.id.nested_scrollview);

            if (scrollView != null) {
                scrollView.getViewTreeObserver()
                        .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                            Integer number = 40;
                            @Override
                            public void onScrollChanged() {
                                if (scrollView.getChildAt(0).getBottom()
                                        <= (scrollView.getHeight() + scrollView.getScrollY())) {
                                    new AsyncSupplierTextView(number,data).execute();
                                    number += 20;
                                } else {

                                }
                            }
                        });
            }
            new AsyncSupplierTextView(20,data).execute();


            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                        @Override
                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                            Bitmap bitmap = imageContainer.getBitmap();

                            if (bitmap != null) {

                                // Background Thread...
                                Palette p = Palette.generate(bitmap, 12);
                                mMutedColor = p.getDarkMutedColor(0xFF333333);

                                GlideApp.with(getActivity())
                                        .load(imageContainer.getBitmap())
                                        .centerCrop()
                                        .into(mPhotoView);
                                mRootView.findViewById(R.id.meta_bar)
                                        .setBackgroundColor(mMutedColor);
                                updateStatusBar();
                            }
                        }

                        @Override
                        public void onErrorResponse(VolleyError volleyError) {

                        }
                    });
        } else {
            mRootView.setVisibility(View.GONE);
               titleView.setText("N/A");
               bylineView.setText("N/A");
            //   bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished
            (@NonNull android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!isAdded()) {
            if (data != null) {
                data.close();
            }
            return;
        }

        mCursor = data;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {
        mCursor = null;
        bindViews();
    }

    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }


    private class AsyncSupplierTextView extends AsyncTask<Void, Void, String> {

        Integer mNumber;
        String mString;


        public AsyncSupplierTextView(Integer number,String string) {
            mNumber = number;
            mString = string;
        }

        @Override
        protected void onPreExecute(){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setCancelable(false);
            builder.setView(R.layout.progressdialog);
            mDialog = builder.create();
            mDialog.show();

        }

        @Override
        protected String doInBackground(Void... strings) {


            mString = mString.replaceAll("\\s+", " ");
            Pattern pattern = Pattern.compile("([A-Z] [^\\.?]*[\\.!?])");
            Matcher matcher = pattern.matcher(mString);

            String data = "";

            for (int i = 1; matcher.find(); i++) {
                if (i > mNumber) {
                    break;
                }
                data += matcher.group() + "\n";
              //  Log.d(TAG, "onPostExecute: " + i +" "+ matcher.group());
            }

            return data;
        }

        @Override
        protected void onPostExecute(String matcher) {
            super.onPostExecute(matcher);
            TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
            bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));
            bodyView.setText(matcher);
            mDialog.hide();
        }
    }

    private AlertDialog mDialog;

    @Override
    public void onPause(){
        super.onPause();
        if(mDialog!=null) mDialog.hide();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mDialog!=null) mDialog.hide();
    }

}
