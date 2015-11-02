package br.com.qualidata.calendar;

import android.app.Application;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.test.ApplicationTestCase;

import org.junit.Test;

import br.com.qualidata.calendar.db.CalendarDb;

/**
 *
 * Created by Ricardo on 02/11/2015.
 */
public class CalendarDbTest extends ApplicationTestCase<Application> {

    public CalendarDbTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
    }

    @Test
    public void testCalendarListing() {
        assertNotNull(getContext());

        Cursor c = CalendarDb.with(getContext()).getCursorForAllCalendars();
        assertNotNull(c);

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
            int isSync = c.getInt(CalendarDb.CALENDAR_PROJECTION_SYNC_EVENTS);
        }
    }

    public void testCreateCalendar() {
        long id = -1;
        try {
            id = CalendarDb.with(getContext()).createLocalCalendar("Test Calendar", "test@gmail.com");
        } catch (SecurityException e) {}

        Uri.Builder builder =
                CalendarContract.Calendars.CONTENT_URI.buildUpon()
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "test@gmail.com")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true");

        assertTrue(id > 0);
        int deleted = getContext().getContentResolver().delete(ContentUris.withAppendedId(builder.build(), id), null, null);
        assertTrue(deleted == 1);
    }
}