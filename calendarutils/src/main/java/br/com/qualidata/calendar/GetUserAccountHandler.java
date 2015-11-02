package br.com.qualidata.calendar;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.GoogleAuthUtil;

import java.lang.ref.WeakReference;

import static com.google.android.gms.common.AccountPicker.*;

/**
 *
 * Created by Ricardo on 02/11/2015.
 */
public class GetUserAccountHandler {

    public static final int REQUEST_CODE_GET_USER_ACCOUNT = 1;

    public interface GetUserAccountCallback {
        void onGetUserAccountSuccess(String accountType, String accountName);
        void onGetUserAccountDenied();
        void onGetUserAccountFailed(Exception e);
    }

    private final GetUserAccountCallback callback;
    private final WeakReference<Activity> activityWeakReference;

    public GetUserAccountHandler(Activity activity, GetUserAccountCallback callback) {
        this.activityWeakReference = new WeakReference<>(activity);
        this.callback = callback;
    }

    public @Nullable Activity getActivity() {
        return activityWeakReference.get();
    }

    public void requestUserAccount() {
        try {
            Intent intent = newChooseAccountIntent(null, null,
                    new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);

            if (getActivity() == null) {
                callback.onGetUserAccountFailed(new NullPointerException("Activity is null"));
            } else {
                getActivity().startActivityForResult(intent, REQUEST_CODE_GET_USER_ACCOUNT);
            }
        } catch (ActivityNotFoundException e) {
            callback.onGetUserAccountFailed(e);
        }
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_GET_USER_ACCOUNT) {
            if (resultCode == Activity.RESULT_OK) {
                String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                String accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                callback.onGetUserAccountSuccess(accountType, accountName);
            } else {
                callback.onGetUserAccountDenied();
            }
            return true;
        }
        return false;
    }
}
