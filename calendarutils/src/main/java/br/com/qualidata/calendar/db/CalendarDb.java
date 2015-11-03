package br.com.qualidata.calendar.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.text.TextUtils;
import android.util.Log;

import java.util.jar.Manifest;

/**
 * Created by Ricardo on 02/11/2015.
 */
public class CalendarDb {

    private static final String LOG_TAG = "CalendarDb";

    public static final int CALENDAR_PROJECTION_ID = 0;
    public static final int CALENDAR_PROJECTION_NAME = 1;
    public static final int CALENDAR_PROJECTION_DISPLAY_NAME = 2;
    public static final int CALENDAR_PROJECTION_COLOR = 3;
    public static final int CALENDAR_PROJECTION_ACCOUNT_NAME = 4;
    public static final int CALENDAR_PROJECTION_ACCOUNT_TYPE = 5;
    public static final int CALENDAR_PROJECTION_OWNER_ACCOUNT = 6;
    public static final int CALENDAR_PROJECTION_IS_PRIMARY = 7;
    public static final int CALENDAR_PROJECTION_VISIBLE = 8;
    public static final int CALENDAR_PROJECTION_SYNC_EVENTS = 9;

    private static CalendarDb instance;

    public static CalendarDb with(Context c) {
        if (instance == null) {
            synchronized (CalendarDb.class) {
                if (instance == null) {
                    instance = new CalendarDb(c.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private final Context context;

    private CalendarDb(Context c) {
        context = c;
    }

//    public boolean hasCalendarForId(long calendarId) {
//
//    }

    public boolean deleteCalendar(long calendarId, @NonNull String accountName) {
        if (TextUtils.isEmpty(accountName)) {
            throw new NullPointerException("accountName is null");
        }

        Uri.Builder builder =
                CalendarContract.Calendars.CONTENT_URI.buildUpon()
                        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                        .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true");

        int deleted = context.getContentResolver().delete(ContentUris.withAppendedId(builder.build(), calendarId), null, null);
        if (deleted > 1) {
            throw new IllegalStateException("Unexpected number of calendars deleted for id " + calendarId + " and accountName '" + accountName + "': " + deleted);
        }
        return deleted == 1;
    }

    @RequiresPermission(allOf = {
            android.Manifest.permission.WRITE_CALENDAR,
            android.Manifest.permission.READ_CALENDAR})
    public long createLocalCalendar(@NonNull String calendarName, @NonNull String accountName) {
        if (TextUtils.isEmpty(calendarName)) {
            throw new NullPointerException("calendarName is null");
        } else if (TextUtils.isEmpty(accountName)) {
            throw new NullPointerException("accountName is null");
        }

        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Calendars.NAME, calendarName);
        cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, calendarName);
        cv.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        cv.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName);
        cv.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName);
        cv.put(CalendarContract.Calendars.IS_PRIMARY, 0);
        cv.put(CalendarContract.Calendars.VISIBLE, 0);
        cv.put(CalendarContract.Calendars.SYNC_EVENTS, 0);

        Uri.Builder builder =
                CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(
                CalendarContract.Calendars.ACCOUNT_NAME,
                accountName)
            .appendQueryParameter(
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.ACCOUNT_TYPE_LOCAL)
            .appendQueryParameter(
                CalendarContract.CALLER_IS_SYNCADAPTER,
                "true");

        try {
            Uri newUri = context.getContentResolver().insert(builder.build(), cv);
            if (newUri == null) {
                return -1;
            } else {
                return ContentUris.parseId(newUri);
            }
        } catch (SecurityException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return -1;
        }
    }

    public Cursor getCursorForAllCalendars() {
        Uri.Builder eventsUriBuilder = CalendarContract.Calendars.CONTENT_URI
                .buildUpon();
        Uri eventsUri = eventsUriBuilder.build();

        String[] cols = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.CALENDAR_COLOR,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CalendarContract.Calendars.OWNER_ACCOUNT,
                CalendarContract.Calendars.IS_PRIMARY,
                CalendarContract.Calendars.VISIBLE,
                CalendarContract.Calendars.SYNC_EVENTS
        };
        return context.getContentResolver().query(eventsUri, cols, null, null, CalendarContract.Calendars.OWNER_ACCOUNT + ", " + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME);
    }
}
