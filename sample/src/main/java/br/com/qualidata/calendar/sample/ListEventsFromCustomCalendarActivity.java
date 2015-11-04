package br.com.qualidata.calendar.sample;

import android.app.Fragment;
import android.os.Bundle;

import java.util.Calendar;

import br.com.qualidata.calendar.db.CalendarDb;
import br.com.qualidata.calendar.eventloader.CustomCalendarEventLoader;
import br.com.qualidata.calendar.monthview.MonthByWeekFragment;

/**
 * Created by Ricardo on 03/11/2015.
 */
public class ListEventsFromCustomCalendarActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            br.com.qualidata.calendar.model.Calendar calendar = CalendarDb.with(this).getCalendarForId(1);

            Fragment f = new MonthByWeekFragment(Calendar.getInstance().getTimeInMillis(), false, new CustomCalendarEventLoader(calendar));
            getFragmentManager().beginTransaction()
                    .add(PARENT_VIEW_ID, f)
                    .commit();
        }
    }
}
