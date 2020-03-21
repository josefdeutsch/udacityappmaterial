package com.example.xyzreader.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.ui.components.CursorFragmentPagerAdapter;
import java.util.Map;

import static com.example.xyzreader.remote.Config.ARG_ITEM_ID;
import static com.example.xyzreader.remote.Config.DEFPACKAGE;
import static com.example.xyzreader.remote.Config.DEFTYPE;
import static com.example.xyzreader.remote.Config.PACKAGE;
import static com.example.xyzreader.remote.Config.SHOW_SWIPE_MESSAGE;
import static com.example.xyzreader.remote.Config.STATUSBARHEIGHT;

public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ArticleDetailActivity";

    private Cursor mCursor;
    private long mStartId;
    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;
    private ViewPager mPager;
    private MyPagerAdapter2 mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;
    private int mTranslation;
    private int mCurrentPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_article_detail);
        getSupportLoaderManager().initLoader(0, null, this);
        setupViewPager();

        mUpButtonContainer = findViewById(R.id.up_container);

        setupUpButton();

        //bpplyTranslationToUpButtonContainer(Build.VERSION_CODES.KITKAT_WATCH);

        applyTranslationToUpButtonContainer(Build.VERSION_CODES.LOLLIPOP);
        verifyViewCompatWindowInsets(Build.VERSION_CODES.LOLLIPOP);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }
    }

    private void setupUpButton() {
        mUpButton = findViewById(R.id.action_up);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });
    }

    private void setupViewPager() {
        mPagerAdapter = new MyPagerAdapter2(getSupportFragmentManager());
        mPager = findViewById(R.id.pager);
        setupViewPagerViewPort();
        setupViewPagerListener();
        mPager.setAdapter(mPagerAdapter);
    }

    private void setupViewPagerViewPort() {
        mPager.setOffscreenPageLimit(1);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));
    }

    private void setupViewPagerListener() {
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private boolean snackShown = false;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                mCursor.moveToPosition(position);
                mPagerAdapter.notifyDataSetChanged();
                mCurrentPosition = position;
                mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);
                mPager.setTag(ARG_ITEM_ID + mCursor.getLong(ArticleLoader.Query._ID));

                setupSnackBar();
            }

            private void setupSnackBar() {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putBoolean(SHOW_SWIPE_MESSAGE, true);
                editor.apply();

                if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(SHOW_SWIPE_MESSAGE, true)) {
                    View vw = mPager.getRootView();
                    if (vw != null) {
                        vw = vw.findViewWithTag(ArticleDetailFragment.getArticleDetailTag(mCursor.getLong(ArticleLoader.Query._ID)));
                        if (vw != null) {
                            if (!snackShown) {
                                @SuppressWarnings("ResourceType") Snackbar snack = Snackbar.make(vw, R.string.swipe_message, Snackbar.LENGTH_LONG).setDuration(3000);
                                snack.setAction(R.string.dismiss, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                                        editor.putBoolean(SHOW_SWIPE_MESSAGE, false);
                                        editor.apply();
                                    }
                                });
                                snack.show();
                            } else {
                                editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                                editor.putBoolean(SHOW_SWIPE_MESSAGE, false);
                                editor.apply();
                            }
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mUpButton.animate()
                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                        .setDuration(300);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(getResources().getResourceName(R.string.translation), mTranslation);
        outState.putInt(getResources().getResourceName(R.string.windowinsets),mTopInset);
    }
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTranslation = savedInstanceState.getInt(getResources().getResourceName(R.string.translation));
        mTopInset = savedInstanceState.getInt(getResources().getResourceName(R.string.windowinsets));
    }

    @Override
    public void onResume() {
        super.onResume();
        mUpButtonContainer.setTranslationY(mTopInset);
        updateUpButtonPosition();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void verifyViewCompatWindowInsets(int buildversion) {
        if (Build.VERSION.SDK_INT >= buildversion) {
        ViewCompat.setOnApplyWindowInsetsListener(mPager,
                new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                  WindowInsetsCompat insets) {
                        insets = ViewCompat.onApplyWindowInsets(v, insets);
                        if (insets.isConsumed()) {
                            return insets;
                        }

                        boolean consumed = false;
                        for (int i = 0, count = mPager.getChildCount(); i < count; i++) {
                            ViewCompat.dispatchApplyWindowInsets(mPager.getChildAt(i), insets);
                            if (insets.isConsumed()) {
                                consumed = true;
                            }
                        }
                        return consumed ? insets.consumeSystemWindowInsets() : insets;
                    }
                });
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void applyTranslationToUpButtonContainer(int buildversion) {
        if (Build.VERSION.SDK_INT >= buildversion) {
            mUpButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                    view.onApplyWindowInsets(windowInsets);
                    mTopInset = windowInsets.getSystemWindowInsetTop();
                    mUpButtonContainer.setTranslationY(mTopInset);
                    updateUpButtonPosition();
                    return windowInsets;
                }
            });
        }
    }
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public void bpplyTranslationToUpButtonContainer(int buildversion) {
        if (Build.VERSION.SDK_INT >= buildversion) {
            mTopInset = getStatusBarHeight();
            mTranslation = mUpButton.getHeight();

            askpermission();
        }
    }

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE= 2323;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            hasPermission();
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void hasPermission() {
        if (Settings.canDrawOverlays(this)) {
            WindowManager manager = ((WindowManager) getApplicationContext()
                    .getSystemService(Context.WINDOW_SERVICE));
            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
            localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
            localLayoutParams.gravity = Gravity.TOP;
            localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|

                    // this is to enable the notification to receive touch events
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    // Draws over status bar
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

            localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            localLayoutParams.height = (int) (50 * getResources()
                    .getDisplayMetrics().scaledDensity);
            localLayoutParams.format = PixelFormat.RGBA_F16;

            CustomViewGroup view = new CustomViewGroup(this);

            manager.addView(view, localLayoutParams);

            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }



    @TargetApi(Build.VERSION_CODES.M)
    public void askpermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {

            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse(PACKAGE + getPackageName()));

            startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            handler.postDelayed(checkOverlaySetting, 1000);
            finish();
        }
    }

    Handler handler = new Handler();
    Runnable checkOverlaySetting = new Runnable() {
        @Override
        @TargetApi(Build.VERSION_CODES.M)
        public void run() {
            if (Settings.canDrawOverlays(ArticleDetailActivity.this)) {
                Intent i = new Intent(ArticleDetailActivity.this, ArticleDetailActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                return;
            }
            handler.postDelayed(this, 1000);
        }
    };

    public class CustomViewGroup extends ViewGroup {

        public CustomViewGroup(Context context) {
            super(context);
        }
        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return true;
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier(STATUSBARHEIGHT, DEFTYPE, DEFPACKAGE);
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        mCursor = cursor;
        mPagerAdapter.swapCursor(cursor);

        if (mStartId > 0) {
            // Select the start ID
            mCursor.moveToFirst();
            // TODO: optimize
            //  if(cursor != null && cursor.getCount() > 0)  {
            //     Log.i("haha","id: " + cursorLoader.getId());
            //     adapter.swapCursor(cursor);
            //  }
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    // Log.d(TAG, "onLoadFinished: " + "kdjshdkjsdhasd");
                    final int position = mCursor.getPosition();
                    Log.d(TAG, "onLoadFinished: " + position);
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }
        //
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mPagerAdapter.swapCursor(null);
    }

    public void onUpButtonFloorChanged(long itemId, ArticleDetailFragment fragment) {
        if (itemId == mSelectedItemId) {
            updateUpButtonPosition();
        }
    }

    private void updateUpButtonPosition() {
       int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
       mTranslation = mSelectedItemUpButtonFloor - upButtonNormalBottom;
       mUpButton.setTranslationY(Math.min(mTranslation,0));

    }

    private class MyPagerAdapter2 extends FragmentStatePagerAdapter {

        public MyPagerAdapter2(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return getFragment(position);
                case 1:
                    return getFragment(position);
                case 2:
                    return getFragment(position);
                case 3:
                    return getFragment(position);
                case 4:
                    return getFragment(position);
                case 5:
                    return getFragment(position);
                default:
                    return null;
            }
        }

        private Fragment getFragment(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID), true);
        }

        @Override
        public int getCount() {
            if (mCursor != null) return mCursor.getCount();
            else return 0;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
                updateUpButtonPosition();
            }
        }

        public void swapCursor(Cursor cursor) {
            mCursor = cursor;
            notifyDataSetChanged();
        }
    }




    public class CursorPagerAdapter extends CursorFragmentPagerAdapter {

        public CursorPagerAdapter(Context context, FragmentManager fm, Cursor cursor) {
            super(context, fm, cursor);
        }

        @Override
        public Fragment getItemByReference(int position) {

            if (mObjectMap.size() == 0) return null;
            else return getFragment(position);

        }

        public Fragment getFragment(int index) {
            Fragment fragment = null;
            if (mArrayList != null) {
                for (Map.Entry<Object, Integer> entry : mObjectMap.entrySet()) {
                    if (entry.getValue().equals(mArrayList.get(index))) {
                        fragment = (Fragment) entry.getKey();
                    }
                }
            }
            return fragment;
        }

        public Fragment getFragments(int index) {
            return fragments.get(mArrayList.get(index));
        }

        @Override
        public void updateUI(ArticleDetailFragment object) {
            {
                ArticleDetailFragment fragment = object;
                if (fragment != null) {
                    // mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
                    // updateUpButtonPosition();
                }
            }
        }

    }
}


