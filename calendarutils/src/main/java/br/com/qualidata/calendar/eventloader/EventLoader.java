package br.com.qualidata.calendar.eventloader;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.List;

import br.com.qualidata.calendar.model.Event;

public abstract class EventLoader<D> implements LoaderManager.LoaderCallbacks<D> {

    public interface EventLoaderCallback {
        void onCreateEventLoader();

        void onEventsLoaded(List<Event> events);

        Pair<Integer, Integer> getCurrentFirstAndLastJulianDays();

        Pair<Integer, Integer> updateLoadedFirstAndLastJulianDays();

    }

    private static final String TAG = "EventLoader";

    private final Runnable mUpdateLoader = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                updateLoader();
            }
        }
    };

    // Used to load the events when a delay is needed
    Runnable mLoadingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mIsDetached) {
                mLoader = startLoader();
            }
        }
    };

    private Loader<D> mLoader;

    private Activity mActivity;
    protected EventLoaderCallback mCallback;

    protected Handler mHandler;
    private volatile boolean mShouldLoad = true;

    private boolean mIsDetached;
    private boolean mIsMiniMonth;
    private boolean mShowDetailsInMonth = false;

    public EventLoader() {
        mHandler = new Handler();
    }

    public void setEventLoaderCallback(EventLoaderCallback callback) {
        this.mCallback = callback;
    }

    public abstract @Nullable Pair<Integer, Integer> getLoadedFirstAndLastJulianDays();

    public Loader<D> startLoader() {
        return mActivity.getLoaderManager().initLoader(0, null, this);
    }

    protected abstract void updateLoader();

    public final void stopLoader() {
        synchronized (mUpdateLoader) {
            mHandler.removeCallbacks(mUpdateLoader);
            if (mLoader != null) {
                mLoader.stopLoading();
                if (Log.isLoggable(TAG, Log.DEBUG)) {
                    Log.d(TAG, "Stopped loader from loading");
                }
            }
        }
    }

    public void onAttach(Activity activity) {
        this.mIsDetached = false;
        this.mActivity = activity;
    }

    public void onDetach() {
        this.mIsDetached = true;
        this.mActivity = null;
    }

    public void setIsMiniMonth(boolean value) {
        mIsMiniMonth = value;
    }

    public boolean isMiniMonth() {
        return mIsMiniMonth;
    }

    public void setShowDetailsInMonth(boolean value) {
        mShowDetailsInMonth = value;
    }

    public boolean showDetailsInMonth() {
        return mShowDetailsInMonth;
    }

    public void setShouldLoad(boolean value) {
        mShouldLoad = value;
    }

    public boolean shouldLoad() {
        return mShouldLoad;
    }

    public void forceLoad() {
        if (mLoader != null) {
            mLoader.forceLoad();
        }
    }

    public void onResume() {}

    @Override
    public final Loader<D> onCreateLoader(int id, Bundle args) {
        synchronized (mUpdateLoader) {
            mCallback.onCreateEventLoader();
            mLoader = onCreateLoaderInternal(id, args);
            return mLoader;
        }
    }

    protected abstract Loader<D> onCreateLoaderInternal(int id, Bundle args);

    @Override
    public final void onLoadFinished(Loader<D> loader, D data) {
        synchronized (mUpdateLoader) {
            onLoadFinishedInternal(loader, data);
        }
    }

    protected abstract void onLoadFinishedInternal(Loader<D> loader, D data);

    @Override
    public void onLoaderReset(Loader<D> loader) {}

    public Runnable getEventLoadingRunnable() {
        return mLoadingRunnable;
    }

    public Runnable getUpdateLoaderRunnable() {
        return mUpdateLoader;
    }

    public Loader getLoader() {
        return mLoader;
    }

    public Activity getActivity() {
        return mActivity;
    }
}
