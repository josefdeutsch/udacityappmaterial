package com.example.xyzreader.ui.components;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.ui.ArticleDetailFragment;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class CursorFragmentPagerAdapter extends FragmentStatePagerAdapter {

    protected boolean mDataValid;
    protected Cursor mCursor;
    protected Context mContext;
    protected SparseIntArray mItemPositions;
    protected ArrayList<Integer> mArrayList = new ArrayList<>();
    protected SparseArray<Fragment> fragments = new SparseArray<>();
    protected HashMap<Object, Integer> mObjectMap;
    protected int mRowIDColumn;
    public int rowId;


    public static final String COLUMNNAME = "_id";
    public static final String CURSOREXCEPTIONMOD = "this should only be called when the cursor is valid";

    public CursorFragmentPagerAdapter(Context context, FragmentManager fm, Cursor cursor) {
        super(fm);
        init(context, cursor);
    }

    void init(Context context, Cursor c) {
        mObjectMap = new HashMap<Object, Integer>();
        boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mContext = context;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow(COLUMNNAME) : -1;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemPosition(Object object) {
        Integer rowId = mObjectMap.get(object);
        if (rowId != null && mItemPositions != null) {
           int index = mItemPositions.get(rowId, POSITION_NONE);
            return mItemPositions.get(rowId, POSITION_NONE);
        }
        return POSITION_NONE;
    }

    public void setItemPositions() {
        mItemPositions = null;

        if (mDataValid) {
            int count = mCursor.getCount();
            mItemPositions = new SparseIntArray(count);
            mCursor.moveToPosition(-1);
            while (mCursor.moveToNext()) {
                int rowId = mCursor.getInt(mRowIDColumn);
                int cursorPos = mCursor.getPosition();
                mItemPositions.append(rowId, cursorPos);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {

        if (mDataValid) {
            // mCursor.moveToPosition(position);
            return getItemByReference(position);
        } else {
            return null;
        }
     /*   mCursor.moveToPosition(position);

        if (fragments.size() == 0) return null;
        else return fragments.get(position);*/
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mObjectMap.remove(object);

        super.destroyItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (!mDataValid) {
            throw new IllegalStateException(CURSOREXCEPTIONMOD);
        }

        //  if (!mCursor.moveToPosition(position)) {
        //    throw new IllegalStateException("couldn't move cursor to position " + position);
        //}
        mCursor.moveToPosition(position);

        rowId = mCursor.getInt(mRowIDColumn);
        mArrayList.add(rowId);
        //fragments.put(rowId, ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID), true));
    //    fragments.put(rowId, ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID), true));
        mObjectMap.put(ArticleDetailFragment.newInstance(mCursor.getLong(ArticleLoader.Query._ID), true), Integer.valueOf(rowId));
        Object obj = super.instantiateItem(container, position);
        return obj;
    }

    public abstract Fragment getItemByReference(int position);

    @Override
    public int getCount() {
        if (mDataValid) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            mRowIDColumn = newCursor.getColumnIndexOrThrow(COLUMNNAME);
            mDataValid = true;
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
        }
        setItemPositions();
        notifyDataSetChanged();

        return oldCursor;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        updateUI((ArticleDetailFragment) object);
    }

    public abstract void updateUI(ArticleDetailFragment object);
}


