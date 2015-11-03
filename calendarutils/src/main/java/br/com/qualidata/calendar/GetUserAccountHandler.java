package br.com.qualidata.calendar;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;

/**
 * Handler skeleton with helper functions to prompt the user to select one of his/her accounts in the device.
 * <p>This might be needed in apps that don't have a user logged in but still might require an account
 * (e.g., an e-mail address) for some specific purpose (e.g., creating a local calendar).</p>
 * <p>This handler was created with the Google Play Services in mind but the dependency is not
 * added. If you want to use it, add the following to this library's build.gradle file: </p>
 *
 * <pre>compile 'com.google.android.gms:play-services-base:8.1.0'</pre>
 *
 * <p>Then uncomment the {@link #newChooseAccountIntent()} method which uses this dependency.</p>
 * <p>Alternatively, you can implement your own account chooser dialog and provide it in this
 * method.</p>
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
            Intent intent = newChooseAccountIntent();

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

    private Intent newChooseAccountIntent() {
        //newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null);
        return null;
    }
}
