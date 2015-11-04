package br.com.qualidata.calendar.db;

import android.content.ContentResolver;
import android.content.Context;

import br.com.qualidata.calendar.model.Calendar;

/**
 * Created by Ricardo on 04/11/2015.
 */
public abstract class DbHandler<T> {

    public interface OnCreatedListener<T> {
        void onCreated(T item, boolean alreadyExisted);

        void onCreationError(Exception e);
    }

    private final Context context;

    protected DbHandler(Context c) {
        context = c;
    }

    protected Context getContext() {
        return context;
    }

    protected ContentResolver getContentResolver() {
        return context.getContentResolver();
    }
}
