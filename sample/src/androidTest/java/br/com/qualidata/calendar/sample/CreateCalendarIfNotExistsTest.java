package br.com.qualidata.calendar.sample;

import android.app.Application;
import android.test.ApplicationTestCase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import br.com.qualidata.calendar.db.CalendarDb;
import br.com.qualidata.calendar.model.Calendar;

/**
 * Created by Ricardo on 03/11/2015.
 */
public class CreateCalendarIfNotExistsTest extends ApplicationTestCase<Application> {

    private static Calendar calendarIfNotExists;
    private static boolean expectedAlreadyExisted = false;

    public CreateCalendarIfNotExistsTest() {
        super(Application.class);
    }

    private void callCreateCalendarIfNotExists() throws InterruptedException {
        calendarIfNotExists = null;
        expectedAlreadyExisted = false;

        final CountDownLatch lock = new CountDownLatch(1);

        CalendarDb.with(getContext()).createCalendarIfNotExists(null, CalendarDbTest.UNIQUE_SYNC_ID, CalendarDbTest.DEFAULT_CALENDAR_NAME, CalendarDbTest.DEFAULT_CALENDAR_ACCOUNT_NAME, new CalendarDb.OnCalendarCreatedListener() {
            @Override
            public void onCalendarCreated(Calendar calendar, boolean alreadyExisted) {
                calendarIfNotExists = calendar;
                expectedAlreadyExisted = alreadyExisted;
                lock.countDown();
            }

            @Override
            public void onCalendarCreationError(Exception e) {
                lock.countDown();
            }
        });

        lock.await(1000, TimeUnit.MILLISECONDS);
    }

    public void testCreateCalendarIfNotExists() throws InterruptedException {
        callCreateCalendarIfNotExists();

        assertNotNull(calendarIfNotExists);
        assertTrue(calendarIfNotExists.getId() > 0);
        assertFalse(expectedAlreadyExisted);
    }

    public void testCreateCalendarIfNotExistsButItDoes() throws InterruptedException {
        callCreateCalendarIfNotExists();

        assertNotNull(calendarIfNotExists);
        assertTrue(calendarIfNotExists.getId() > 0);
        assertTrue(expectedAlreadyExisted);

        assertTrue(CalendarDb.with(getContext()).deleteCalendar(CalendarDbTest.UNIQUE_SYNC_ID, CalendarDbTest.DEFAULT_CALENDAR_ACCOUNT_NAME));
    }
}
