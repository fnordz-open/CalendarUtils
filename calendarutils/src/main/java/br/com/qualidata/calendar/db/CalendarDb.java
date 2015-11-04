package br.com.qualidata.calendar.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import br.com.qualidata.calendar.model.Calendar;

/**
 * Created by Ricardo on 02/11/2015.
 */
public class CalendarDb extends DbHandler<Calendar> {

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
    public static final int CALENDAR_PROJECTION_SYNC_ID = 10;

    public static final String[] CALENDAR_PROJECTION_FIELDS = new String[]{
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.OWNER_ACCOUNT,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.VISIBLE,
            CalendarContract.Calendars.SYNC_EVENTS,
            CalendarContract.Calendars._SYNC_ID
    };

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

    protected CalendarDb(Context c) {
        super(c);
    }

    public void createCalendarIfNotExists(final @Nullable Long knownCalendarId, final @Nullable String knownSyncId,
                                          final @NonNull String calendarName, final @NonNull String accountName,
                                          @NonNull OnCreatedListener<Calendar> listener) {

        final WeakReference<OnCreatedListener<Calendar>> onCalendarCreatedListenerWeakReference = new WeakReference<>(listener);

        new AsyncTask<Void, Void, Calendar>() {

            private boolean alreadyExisted = false;
            private Exception exception;

            @Override
            protected Calendar doInBackground(Void... params) {
                synchronized (getContext()) {
                    if (knownCalendarId != null) {
                        if (hasCalendarForId(knownCalendarId)) {
                            alreadyExisted = true;
                            return getCalendarForId(knownCalendarId);
                        }
                    }

                    if (!TextUtils.isEmpty(knownSyncId)) {
                        if (hasCalendarForId(knownSyncId)) {
                            alreadyExisted = true;
                            return getCalendarForId(knownSyncId);
                        }
                    }

                    try {
                        return createLocalCalendar(calendarName, accountName, knownSyncId);
                    } catch (SecurityException e) {
                        exception = e;
                        return null;
                    }
                }
            }

            @Override
            protected void onPostExecute(Calendar calendar) {
                OnCreatedListener<Calendar> listener = onCalendarCreatedListenerWeakReference.get();
                if (listener != null) {
                    if (exception == null) {
                        listener.onCreated(calendar, alreadyExisted);
                    } else {
                        listener.onCreationError(exception);
                    }
                }
            }
        }.execute();

    }

    public boolean hasCalendarForId(long calendarId) {
        if (calendarId < 0) {
            return false;
        }

        Cursor c = getContext().getContentResolver().query(ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarId),
                new String[]{CalendarContract.Calendars._ID}, null, null, null);
        boolean exists = false;
        if (c != null) {
            while (c.moveToNext()) {
                exists = true;
                break;
            }
            c.close();
        }
        return exists;
    }

    public boolean hasCalendarForId(String syncId) throws SecurityException {
        if (TextUtils.isEmpty(syncId)) {
            return false;
        }

        Cursor c = getContext().getContentResolver().query(CalendarContract.Calendars.CONTENT_URI,
                new String[]{CalendarContract.Calendars._ID}, CalendarContract.Calendars._SYNC_ID + " = ?", new String[]{syncId}, null);
        boolean exists = false;
        if (c != null) {
            while (c.moveToNext()) {
                exists = true;
                break;
            }
            c.close();
        }
        return exists;
    }

    public boolean deleteCalendar(long calendarId, @NonNull String accountName) {
        if (TextUtils.isEmpty(accountName)) {
            throw new NullPointerException("accountName is null");
        }

        Uri.Builder builder =
                CalendarContract.Calendars.CONTENT_URI.buildUpon()
                        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                        .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true");

        int deleted = getContext().getContentResolver().delete(ContentUris.withAppendedId(builder.build(), calendarId), null, null);
        if (deleted > 1) {
            throw new IllegalStateException("Unexpected number of calendars deleted for id " + calendarId + " and accountName '" + accountName + "': " + deleted);
        }
        return deleted == 1;
    }

    public boolean deleteCalendar(String syncId, @NonNull String accountName) {
        if (TextUtils.isEmpty(accountName)) {
            throw new NullPointerException("accountName is null");
        }

        Uri.Builder builder =
                CalendarContract.Calendars.CONTENT_URI.buildUpon()
                        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                        .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                        .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true");

        int deleted = getContext().getContentResolver().delete(builder.build(), CalendarContract.Calendars._SYNC_ID + " = ?", new String[]{syncId});
        if (deleted > 1) {
            throw new IllegalStateException("Unexpected number of calendars deleted for syncId " + syncId + " and accountName '" + accountName + "': " + deleted);
        }
        return deleted == 1;
    }

    @RequiresPermission(allOf = {
            android.Manifest.permission.WRITE_CALENDAR,
            android.Manifest.permission.READ_CALENDAR})
    public Calendar createLocalCalendar(@NonNull String calendarName, @NonNull String accountName) throws SecurityException {
        return createLocalCalendar(calendarName, accountName, "");
    }

    @RequiresPermission(allOf = {
            android.Manifest.permission.WRITE_CALENDAR,
            android.Manifest.permission.READ_CALENDAR})
    public
    @Nullable
    Calendar createLocalCalendar(@NonNull String calendarName, @NonNull String accountName, @Nullable String syncId) throws SecurityException {
        if (TextUtils.isEmpty(calendarName)) {
            throw new NullPointerException("calendarName is null");
        } else if (TextUtils.isEmpty(accountName)) {
            throw new NullPointerException("accountName is null");
        }

        String accountType = CalendarContract.ACCOUNT_TYPE_LOCAL;

        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Calendars.NAME, calendarName);
        cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, calendarName);
        cv.put(CalendarContract.Calendars.ACCOUNT_TYPE, accountType);
        cv.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName);
        cv.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName);

        boolean isPrimary = false;
        cv.put(CalendarContract.Calendars.IS_PRIMARY, isPrimary ? 1 : 0);

        boolean isVisible = false;
        cv.put(CalendarContract.Calendars.VISIBLE, isVisible ? 1 : 0);

        boolean syncEvents = false;
        cv.put(CalendarContract.Calendars.SYNC_EVENTS, syncEvents ? 1 : 0);

        if (!TextUtils.isEmpty(syncId)) {
            cv.put(CalendarContract.Calendars._SYNC_ID, syncId);
        }

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

        Uri newUri = getContext().getContentResolver().insert(builder.build(), cv);
        if (newUri == null) {
            return null;
        } else {
            long id = ContentUris.parseId(newUri);
            Calendar c = new Calendar(id, calendarName, calendarName);
            c.setAccountName(accountName);
            c.setAccountType(accountType);
            c.setIsPrimary(isPrimary);
            c.setIsVisible(isVisible);
            c.setSyncEvents(syncEvents);
            c.setOwnerAccount(accountName);
            return c;
        }
    }

    public @Nullable Calendar getCalendarForId(long calendarId) {
        List<Calendar> calendars = getCalendarsFromCursor(getContext().getContentResolver().query(
                ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarId),
                CALENDAR_PROJECTION_FIELDS,
                null, null, null)
        );

        if (calendars.size() != 1) {
            return null;
        } else {
            return calendars.get(0);
        }
    }

    public @Nullable Calendar getCalendarForId(String syncId) throws SecurityException {
        List<Calendar> calendars = getCalendarsFromCursor(getContext().getContentResolver().query(
                        CalendarContract.Calendars.CONTENT_URI,
                        CALENDAR_PROJECTION_FIELDS,
                        CalendarContract.Calendars._SYNC_ID + " = ?", new String[]{syncId}, null)
        );

        if (calendars.size() != 1) {
            return null;
        } else {
            return calendars.get(0);
        }
    }

    public List<Calendar> getAllCalendars() {
        Uri.Builder eventsUriBuilder = CalendarContract.Calendars.CONTENT_URI
                .buildUpon();
        Uri eventsUri = eventsUriBuilder.build();

        return getCalendarsFromCursor(getContext().getContentResolver().query(eventsUri, CALENDAR_PROJECTION_FIELDS, null, null, CalendarContract.Calendars.OWNER_ACCOUNT + ", " + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME));
    }

    private List<Calendar> getCalendarsFromCursor(Cursor c) {
        List<Calendar> calendars = new ArrayList<>();

        if (c != null) {
            while (c.moveToNext()) {
                long id = c.getLong(CalendarDb.CALENDAR_PROJECTION_ID);
                String name = c.getString(CalendarDb.CALENDAR_PROJECTION_NAME);
                String displayName = c.getString(CalendarDb.CALENDAR_PROJECTION_DISPLAY_NAME);
                int color = c.getInt(CalendarDb.CALENDAR_PROJECTION_COLOR);
                String accountName = c.getString(CalendarDb.CALENDAR_PROJECTION_ACCOUNT_NAME);
                String accountType = c.getString(CalendarDb.CALENDAR_PROJECTION_ACCOUNT_TYPE);
                String ownerAccount = c.getString(CalendarDb.CALENDAR_PROJECTION_OWNER_ACCOUNT);
                int isPrimary = c.getInt(CalendarDb.CALENDAR_PROJECTION_IS_PRIMARY);
                int isVisible = c.getInt(CalendarDb.CALENDAR_PROJECTION_VISIBLE);
                String syncId = c.getString(CalendarDb.CALENDAR_PROJECTION_SYNC_ID);
                int isSync = c.getInt(CalendarDb.CALENDAR_PROJECTION_SYNC_EVENTS);

                Calendar calendar = new Calendar(id, name, displayName);
                calendar.setAccountName(accountName);
                calendar.setAccountType(accountType);
                calendar.setOwnerAccount(ownerAccount);
                calendar.setIsPrimary(isPrimary == 1);
                calendar.setIsVisible(isVisible == 1);
                calendar.setSyncId(syncId);
                calendar.setSyncEvents(isSync == 1);
                calendar.setColor(color);

                calendars.add(calendar);
            }
            c.close();
        }
        return calendars;
    }
}
