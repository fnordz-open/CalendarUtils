package br.com.qualidata.calendar.sample;

import android.app.Application;
import android.database.Cursor;
import android.test.ApplicationTestCase;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import br.com.qualidata.calendar.db.CalendarDb;
import br.com.qualidata.calendar.model.Calendar;

/**
 * Created by Ricardo on 02/11/2015.
 */
public class CalendarDbTest extends ApplicationTestCase<Application> {

    protected static final String UNIQUE_SYNC_ID = "my_unique_sync_id";
    protected static final String DEFAULT_CALENDAR_NAME = "Testing Calendar";
    protected static final String DEFAULT_CALENDAR_ACCOUNT_NAME = "test@gmail.com";

    private static long insertedCalendarId;

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

        List<Calendar> calendars = CalendarDb.with(getContext()).getAllCalendars();
        assertNotNull(calendars);
        assertTrue(calendars.size() > 0);
    }

    public void testCreateCalendar() {
        long id = -1;
        Calendar calendar;
        try {
            calendar = CalendarDb.with(getContext()).createLocalCalendar(DEFAULT_CALENDAR_NAME, DEFAULT_CALENDAR_ACCOUNT_NAME, UNIQUE_SYNC_ID);
            assertNotNull(calendar);
            id = calendar.getId();
        } catch (SecurityException e) {
        }
        insertedCalendarId = id;
        assertTrue(insertedCalendarId > 0);
    }

    public void testCreatedCalendarExists() {
        boolean exists = CalendarDb.with(getContext()).hasCalendarForId(insertedCalendarId);
        assertTrue(exists);

        exists = CalendarDb.with(getContext()).hasCalendarForId(UNIQUE_SYNC_ID);
        assertTrue(exists);
    }

    public void testCalendarNotExists() {
        boolean exists = CalendarDb.with(getContext()).hasCalendarForId(Long.MAX_VALUE);
        assertFalse(exists);

        exists = CalendarDb.with(getContext()).hasCalendarForId(-1);
        assertFalse(exists);

        exists = CalendarDb.with(getContext()).hasCalendarForId(Long.MIN_VALUE);
        assertFalse(exists);

        exists = CalendarDb.with(getContext()).hasCalendarForId("some_non_existing_key");
        assertFalse(exists);

        exists = CalendarDb.with(getContext()).hasCalendarForId("");
        assertFalse(exists);
    }

    public void testDeleteCreatedCalendar() {
        assertTrue(insertedCalendarId > 0);
        boolean deleted = CalendarDb.with(getContext()).deleteCalendar(insertedCalendarId, DEFAULT_CALENDAR_ACCOUNT_NAME);
        assertTrue(deleted);

        assertFalse(CalendarDb.with(getContext()).deleteCalendar(UNIQUE_SYNC_ID, DEFAULT_CALENDAR_ACCOUNT_NAME));
    }

}
