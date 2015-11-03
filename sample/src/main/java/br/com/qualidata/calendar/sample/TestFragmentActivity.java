package br.com.qualidata.calendar.sample;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.widget.FrameLayout;

import java.util.Calendar;

import br.com.qualidata.calendar.monthview.MonthByWeekFragment;

/**
 * Stub activity for testing fragments
 * See https://groups.google.com/forum/#!topic/robolectric/doUomaLr83Q and
 * http://blog.denevell.org/android-testing-fragments.html
 */
@SuppressWarnings("ALL")
public class TestFragmentActivity extends Activity {
    public static final int PARENT_VIEW_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final FrameLayout frameLayout = new FrameLayout( this );
        frameLayout.setId(PARENT_VIEW_ID);
        setContentView(frameLayout);

        Fragment f = new MonthByWeekFragment(Calendar.getInstance().getTimeInMillis(), false);
        getFragmentManager().beginTransaction()
                .add(PARENT_VIEW_ID, f, "month_frag")
                .commit();
    }
}
