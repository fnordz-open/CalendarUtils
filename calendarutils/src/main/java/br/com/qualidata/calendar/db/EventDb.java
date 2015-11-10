package br.com.qualidata.calendar.db;

import android.content.ContentProviderOperation;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TimeZone;

import br.com.qualidata.calendar.model.CalendarEventModel;
import br.com.qualidata.calendar.model.Event;

import static br.com.qualidata.calendar.model.CalendarEventModel.*;

/**
 * Created by Ricardo on 04/11/2015.
 */
public class EventDb extends DbHandler<Event> {

    private static final String TAG = "EventDb";

    private static EventDb instance;

    public static EventDb with(Context c) {
        if (instance == null) {
            synchronized (EventDb.class) {
                if (instance == null) {
                    instance = new EventDb(c.getApplicationContext());
                }
            }
        }
        return instance;
    }

    protected EventDb(Context c) {
        super(c);
    }

    public int deleteAllEventsForCalendarId(long calendarId) throws SecurityException {
        return getContentResolver().delete(CalendarContract.Events.CONTENT_URI,
                CalendarContract.Events.CALENDAR_ID + " = ?",
                new String[]{Long.toString(calendarId)});
    }

    /**
     * Insert the event into the Calendar Content Provider.
     *
     * @param model The event model to save
     * @return The id of the inserted event. -1 if it was not inserted properly
     */
    public long insertEvent(@NonNull CalendarEventModel model) throws SecurityException{
        // It's a problem if we try to save a non-existent or invalid model or if we're
        // modifying an existing event and we have the wrong original model
        if (model == null) {
            Log.e(TAG, "Attempted to save null model.");
            return -1;
        }
        if (!model.isValid()) {
            Log.e(TAG, "Attempted to save invalid model.");
            return -1;
        }

        ContentValues values = getContentValuesFromModel(model);

        // Update the "hasAlarm" field for the event
        ArrayList<ReminderEntry> reminders = model.mReminders;
        int len = reminders.size();
        values.put(CalendarContract.Events.HAS_ALARM, (len > 0) ? 1 : 0);

        Uri newUri = getContentResolver().insert(CalendarContract.Events.CONTENT_URI, values);
        return ContentUris.parseId(newUri);
    }

    /**
     * Goes through an event model and fills in content values for saving. This
     * method will perform the initial collection of values from the model and
     * put them into a set of ContentValues. It performs some basic work such as
     * fixing the time on allDay events and choosing whether to use an rrule or
     * dtend.
     *
     * @param model The complete model of the event you want to save
     * @return values
     */
    ContentValues getContentValuesFromModel(CalendarEventModel model) {
        String title = model.getTitle();
        boolean isAllDay = model.mAllDay;
        String rrule = model.mRrule;
        String timezone = model.mTimezone;
        if (timezone == null) {
            timezone = TimeZone.getDefault().getID();
        }
        Time startTime = new Time(timezone);
        Time endTime = new Time(timezone);

        startTime.set(model.mStart);
        endTime.set(model.mEnd);
        //offsetStartTimeIfNecessary(startTime, endTime, rrule, model);

        ContentValues values = new ContentValues();

        long startMillis;
        long endMillis;
        long calendarId = model.mCalendarId;
        if (isAllDay) {
            // Reset start and end time, ensure at least 1 day duration, and set
            // the timezone to UTC, as required for all-day events.
            timezone = Time.TIMEZONE_UTC;
            startTime.hour = 0;
            startTime.minute = 0;
            startTime.second = 0;
            startTime.timezone = timezone;
            startMillis = startTime.normalize(true);

            endTime.hour = 0;
            endTime.minute = 0;
            endTime.second = 0;
            endTime.timezone = timezone;
            endMillis = endTime.normalize(true);
            if (endMillis < startMillis + DateUtils.DAY_IN_MILLIS) {
                // EditEventView#fillModelFromUI() should treat this case, but we want to ensure
                // the condition anyway.
                endMillis = startMillis + DateUtils.DAY_IN_MILLIS;
            }
        } else {
            startMillis = startTime.toMillis(true);
            endMillis = endTime.toMillis(true);
        }

        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timezone);
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.ALL_DAY, isAllDay ? 1 : 0);
        values.put(CalendarContract.Events.DTSTART, startMillis);
        values.put(CalendarContract.Events.RRULE, rrule);
        if (!TextUtils.isEmpty(rrule)) {
            //addRecurrenceRule(values, model);
        } else {
            values.put(CalendarContract.Events.DURATION, (String) null);
            values.put(CalendarContract.Events.DTEND, endMillis);
        }
        if (model.mDescription != null) {
            values.put(CalendarContract.Events.DESCRIPTION, model.mDescription.trim());
        } else {
            values.put(CalendarContract.Events.DESCRIPTION, (String) null);
        }
        if (model.mLocation != null) {
            values.put(CalendarContract.Events.EVENT_LOCATION, model.mLocation.trim());
        } else {
            values.put(CalendarContract.Events.EVENT_LOCATION, (String) null);
        }
        values.put(CalendarContract.Events.AVAILABILITY, model.mAvailability);
        values.put(CalendarContract.Events.HAS_ATTENDEE_DATA, model.mHasAttendeeData ? 1 : 0);

        int accessLevel = model.mAccessLevel;
        if (accessLevel > 0) {
            // For now the array contains the values 0, 2, and 3. We add one to match.
            // Default (0), Private (2), Public (3)
            accessLevel++;
        }
        values.put(CalendarContract.Events.ACCESS_LEVEL, accessLevel);
        values.put(CalendarContract.Events.STATUS, model.mEventStatus);
        if (model.isEventColorInitialized()) {
            if (model.getEventColor() == model.getCalendarColor()) {
                //values.put(CalendarContract.Events.EVENT_COLOR_KEY, NO_EVENT_COLOR);
            } else {
                values.put(CalendarContract.Events.EVENT_COLOR_KEY, model.getEventColorKey());
            }
        }
        return values;
    }
}
