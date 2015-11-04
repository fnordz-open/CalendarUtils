package br.com.qualidata.calendar.eventloader;

import android.app.Activity;
import android.provider.CalendarContract;

import br.com.qualidata.calendar.model.Calendar;

public class CustomCalendarEventLoader extends DefaultEventLoader {

    private final Calendar calendar;

    public CustomCalendarEventLoader(Calendar calendar) {
        super();
        this.calendar = calendar;
    }

    @Override
    protected String updateWhere() {
        return CalendarContract.Instances.CALENDAR_ID + " = ?";
    }

    @Override
    protected String[] updateWhereSelectedArgs() {
        return new String[]{Long.toString(calendar.getId())};
    }
}
