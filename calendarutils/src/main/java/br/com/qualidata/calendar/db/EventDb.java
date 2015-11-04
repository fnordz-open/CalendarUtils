package br.com.qualidata.calendar.db;

import android.content.Context;
import android.provider.CalendarContract;

import br.com.qualidata.calendar.model.CalendarEventModel;
import br.com.qualidata.calendar.model.Event;

/**
 * Created by Ricardo on 04/11/2015.
 */
public class EventDb extends DbHandler<Event> {

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
}
