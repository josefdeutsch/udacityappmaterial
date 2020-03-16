package com.example.xyzreader.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Toast;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.remote.Config;
import com.example.xyzreader.ui.components.CursorFragmentPagerAdapter;

import java.util.List;
import java.util.Map;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor mCursor;
    private long mStartId;
    public static final String ARG_ITEM_ID = "item_id";
    private long mSelectedItemId;
    private int mSelectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int mTopInset;
    private static final String TAG = "ArticleDetailActivity";
    private ViewPager mPager;
    //private MyPagerAdapter mPagerAdapter;
    private MyPagerAdapter2 mPagerAdapter;
  //  private CursorFragmentPagerAdapter mPagerAdapter;
    // private FragmentPager mPagerAdapter;
    private View mUpButtonContainer;
    private View mUpButton;

    private int mCurrentPosition = 0;
    public final String SHOW_SWIPE_MESSAGE = "show_swipe_message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        getSupportLoaderManager().initLoader(0, null, this);
       // mPagerAdapter = new CursorPagerAdapter(getApplicationContext(), getSupportFragmentManager(), mCursor);
         mPagerAdapter = new MyPagerAdapter2(getSupportFragmentManager());
        //  mPagerAdapter = new FragmentPager(getSupportFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(1);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            private boolean snackShown = false;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {


                Log.d(TAG, "onPageSelected: " + position);
                mCursor.moveToPosition(position);

                mPagerAdapter.notifyDataSetChanged();
                /**
                 mCurrentPosition = position;
                 mSelectedItemId = mCursor.getLong(ArticleLoader.Query._ID);

                 mPager.setTag(ARG_ITEM_ID+mCursor.getLong(ArticleLoader.Query._ID));
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
                @Override public void onClick(View v) {
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
                 }**/
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                mUpButton.animate()
                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                        .setDuration(300);
            }
        });
        mPager.setAdapter(mPagerAdapter);
        mUpButtonContainer = findViewById(R.id.up_container);

        mUpButton = findViewById(R.id.action_up);
        mUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSupportNavigateUp();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
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

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
                mSelectedItemId = mStartId;
            }
        }
        ViewPager mViewPager;

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
                        for (int i = 0, count = mPager.getChildCount(); i <  count; i++) {
                            ViewCompat.dispatchApplyWindowInsets(mPager.getChildAt(i), insets);
                            if (insets.isConsumed()) {
                                consumed = true;
                            }
                        }
                        return consumed ? insets.consumeSystemWindowInsets() : insets;
                    }
                });
    }

    private void cleanFragments(FragmentManager childFragmentManager) {
        List<Fragment> childFragments = childFragmentManager.getFragments();
        if (childFragments != null && !childFragments.isEmpty()) {
            FragmentTransaction ft = childFragmentManager.beginTransaction();
            for (Fragment fragment : childFragments) {
                ft.remove(fragment);
            }
            ft.commit();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
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
            mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
            updateUpButtonPosition();
        }
    }

    private void updateUpButtonPosition() {
        int upButtonNormalBottom = mTopInset + mUpButton.getHeight();
        mUpButton.setTranslationY(Math.min(mSelectedItemUpButtonFloor - upButtonNormalBottom, 0));
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
                mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
                updateUpButtonPosition();
            }
        }
        public void swapCursor(Cursor cursor) {
            mCursor = cursor;
            notifyDataSetChanged();
        }
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        private SparseArray<Fragment> fragments = new SparseArray<>();
        private SparseArray<Integer> position = new SparseArray<>();

        public MyPagerAdapter(android.support.v4.app.FragmentManager fm, Cursor c) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
                mSelectedItemUpButtonFloor = fragment.getUpButtonFloor();
                updateUpButtonPosition();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            //   Log.d(TAG, "instantiateItem: "+position);
            mCursor.moveToPosition(position);
            fragments.put(position, ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID), true));
            Fragment fragment = (Fragment) super.instantiateItem(container, position);

            return fragment;
        }

        @Override
        public int getItemPosition(Object object) {

            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "getItem: " + position);
            if (fragments.size() == 0) return null;
            else return fragments.get(position);
        }

        @Override
        public int getCount() {
            if (mCursor != null) return mCursor.getCount();
            else return 0;
        }

        public void swapCursor(Cursor cursor) {
            mCursor = cursor;
            notifyDataSetChanged();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }

    }

    //Invert pager in RTL mode
    protected int getPositionToCursor(int position) {
        if (!Config.isRTL()) return position;
        else return (mCursor.getCount() - 1) - position;
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

    public class FragmentPager extends com.example.xyzreader.ui.components.FragmentStatePagerAdapter {

        public FragmentPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "getItem: " + position);
            mCursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID), true);
        }


        @Override
        public int getCount() {
            if (mCursor != null) return mCursor.getCount();
            else return 0;
        }

        public void swapCursor(Cursor cursor) {
            mCursor = cursor;
            notifyDataSetChanged();
        }
    }

}


