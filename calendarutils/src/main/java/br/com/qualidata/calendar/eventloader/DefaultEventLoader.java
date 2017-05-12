package br.com.qualidata.calendar.eventloader;

import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.text.format.Time;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.qualidata.calendar.Utils;
import br.com.qualidata.calendar.model.Event;

public class DefaultEventLoader extends EventLoader<Cursor> {

    private static final String TAG = "DefaultEventLoader";

    // The minimum time between requeries of the data if the db is
    // changing
    private static final int LOADER_THROTTLE_DELAY = 500;

    // Selection and selection args for adding event queries
    private static final String WHERE_CALENDARS_VISIBLE = CalendarContract.Calendars.VISIBLE + "=1";
    private static final String INSTANCES_SORT_ORDER = CalendarContract.Instances.START_DAY + ","
            + CalendarContract.Instances.START_MINUTE + "," + CalendarContract.Instances.TITLE;

    private Uri mEventUri;

    protected boolean mHideDeclined;

    // disposable variable used for time calculations
    protected Time mTempTime = new Time();

    public DefaultEventLoader() {
        super();
    }

    @Override
    public void updateLoader() {
        if (!shouldLoad() || getLoader() == null) {
            return;
        }
        // Stop any previous loads while we update the uri
        stopLoader();

        // Start the loader again
        mEventUri = updateUri();

        CursorLoader loader = (CursorLoader)getLoader();
        loader.setUri(mEventUri);
        loader.startLoading();
        loader.onContentChanged();
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Started loader with uri: " + mEventUri);
        }
    }

    private Uri updateUri() {
        Pair<Integer, Integer> julianDays = mCallback.getCurrentFirstAndLastJulianDays();

        int mFirstLoadedJulianDay = julianDays.first;
        int mLastLoadedJulianDay = julianDays.second;

        // -1 to ensure we get all day events from any time zone
        mTempTime.setJulianDay(mFirstLoadedJulianDay - 1);
        long start = mTempTime.toMillis(true);
        // +1 to ensure we get all day events from any time zone
        mTempTime.setJulianDay(mLastLoadedJulianDay + 1);
        long end = mTempTime.toMillis(true);

        // Create a new uri with the updated times
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, start);
        ContentUris.appendId(builder, end);
        return builder.build();
    }

    @Override
    public
    @Nullable
    Pair<Integer, Integer> getLoadedFirstAndLastJulianDays() {
        List<String> pathSegments = mEventUri.getPathSegments();
        int size = pathSegments.size();
        if (size <= 2) {
            return null;
        }
        long first = Long.parseLong(pathSegments.get(size - 2));
        long last = Long.parseLong(pathSegments.get(size - 1));
        mTempTime.set(first);
        Integer firstJulianDay = Time.getJulianDay(first, mTempTime.gmtoff);
        mTempTime.set(last);
        Integer lastJulianDay = Time.getJulianDay(last, mTempTime.gmtoff);

        return new Pair<>(firstJulianDay, lastJulianDay);
    }

    protected String updateWhere() {
        // TODO fix selection/selection args after b/3206641 is fixed
        String where = WHERE_CALENDARS_VISIBLE;
        if (mHideDeclined || !showDetailsInMonth()) {
            where += " AND " + CalendarContract.Instances.SELF_ATTENDEE_STATUS + "!="
                    + CalendarContract.Attendees.ATTENDEE_STATUS_DECLINED;
        }
        return where;
    }

    protected String[] updateWhereSelectedArgs() {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        boolean prevHideDeclined = mHideDeclined;
        mHideDeclined = Utils.getHideDeclinedEvents(getActivity());
        if (prevHideDeclined != mHideDeclined && getLoader() != null) {
            CursorLoader cursorLoader = (CursorLoader)getLoader();
            cursorLoader.setSelection(updateWhere());
        }
    }

    @Override
    public Loader<Cursor> onCreateLoaderInternal(int id, Bundle args) {
        if (isMiniMonth()) {
            return null;
        }

        mEventUri = updateUri();
        String where = updateWhere();
        String[] whereSelectedArgs = updateWhereSelectedArgs();

        CursorLoader loader = new CursorLoader(
                getActivity(), mEventUri, Event.EVENT_PROJECTION, where,
                whereSelectedArgs, INSTANCES_SORT_ORDER);
        loader.setUpdateThrottle(LOADER_THROTTLE_DELAY);

        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Returning new loader with uri: " + mEventUri);
        }
        return loader;
    }

    @Override
    public void onLoadFinishedInternal(Loader<Cursor> loader, Cursor data) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "Found " + data.getCount() + " cursor entries for uri " + mEventUri);
        }
        CursorLoader cLoader = (CursorLoader) loader;
        Pair<Integer, Integer> julianDays;
        if (mEventUri == null) {
            mEventUri = cLoader.getUri();
            julianDays = mCallback.updateLoadedFirstAndLastJulianDays();
        } else {
            julianDays = getLoadedFirstAndLastJulianDays();
        }
        if (cLoader.getUri().compareTo(mEventUri) != 0) {
            // We've started a new query since this loader ran so ignore the
            // result
            return;
        }
        ArrayList<Event> events = new ArrayList<>();
        Event.buildEventsFromCursor(events, data, getActivity(), julianDays.first, julianDays.second);
        mCallback.onEventsLoaded(events);
    }
}
