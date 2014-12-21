package com.abdennebi.photogift.activity;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.abdennebi.photogift.R;
import com.abdennebi.photogift.api.ApiCallbacks;
import com.abdennebi.photogift.api.ApiClient;
import com.abdennebi.photogift.api.retrofit.ApiClientImpl;
import com.abdennebi.photogift.application.ApplicationSession;
import com.abdennebi.photogift.application.BaseActivity;
import com.abdennebi.photogift.domain.Gift;
import com.abdennebi.photogift.domain.User;
import com.abdennebi.photogift.utils.IntentsUtils;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.SignInButton;

import static android.view.Gravity.RIGHT;
import static android.view.MenuItem.SHOW_AS_ACTION_ALWAYS;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.abdennebi.photogift.R.string.signin_dialog_message;
import static com.abdennebi.photogift.R.string.signin_dialog_no;
import static com.abdennebi.photogift.R.string.signin_dialog_yes;


public class MainActivity extends BaseActivity implements ApiCallbacks.ServiceCallback, GiftChainListFragment.OnArticleSelectedListener {

    private static final int REQ_CHOOSE_ACCOUNT = 55333;
    private static final int REQ_CONSENT = 55332;
    /**
     * Activity result code for image gallery select.
     */
    static final int REQUEST_CODE_GIFT_CREATE = 7000;
    private static final String TAG = "PhotoGift-MainActivity";
    SignInButton signIn;
    private ApplicationSession session;
    private ApiClient apiClient;
    private Boolean mImmediateSignIn = false;
    private AlertDialog mSignInDialog;
    private User user;

    private GiftChainListFragment giftChainListFragment;

    private GiftChainFragment giftChainFragment;

    /*============================================================================================*/
    /*==                                 Activity methods                                       ==*/
    /*============================================================================================*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.main_activity);

        android.support.v4.app.FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();

        giftChainListFragment = new GiftChainListFragment();

        fragmentTransaction.add(R.id.fragment_container, giftChainListFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        session = session();

        apiClient = new ApiClientImpl(session);

        signIn = (SignInButton) getLayoutInflater().inflate(R.layout.sign_in_button, null);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginSignInFlow();
            }
        });
        signIn.setSize(SignInButton.SIZE_STANDARD);

        // Hide sign in button and load user if we have cached account name and session id
        if (session.checkSessionState() == ApplicationSession.State.HAS_SESSION) {
            setProgressBarIndeterminateVisibility(true);
            apiClient.fetchCurrentUser(this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);

        getActionBar().setDisplayShowCustomEnabled(false);

//        menu.add(0, R.id.menu_item_search, 0, "Search").setIcon(R.drawable.ic_search_white_48dp)
//                .setShowAsAction(SHOW_AS_ACTION_ALWAYS);

        if (application().isAuthenticated()) {
            // Sign out Menu item

            menu.add(0, R.id.menu_item_new_gift, 0, "New Gift").setIcon(R.drawable.ic_add_white_48dp)
                    .setShowAsAction(SHOW_AS_ACTION_ALWAYS);

            menu.add(0, R.id.menu_item_sign_out, 0, getString(R.string.sign_out_menu_title))
                    .setShowAsAction(SHOW_AS_ACTION_NEVER);

            // Disconnect Menu Item
            menu.add(0, R.id.menu_item_disconnect, 0, getString(R.string.disconnect_menu_title))
                    .setShowAsAction(SHOW_AS_ACTION_NEVER);


        } else {
            ActionBar.LayoutParams params = new ActionBar.LayoutParams(WRAP_CONTENT, WRAP_CONTENT, RIGHT);
            getActionBar().setCustomView(signIn, params);
            getActionBar().setDisplayShowCustomEnabled(true);
        }

        menu.add(0, R.id.menu_item_top_givers, 0, "Top Givers").setIcon(R.drawable.ic_grade_white_48dp)
                .setShowAsAction(SHOW_AS_ACTION_ALWAYS);

        menu.add(0, R.id.menu_item_refresh, 0, getString(R.string.menu_home_title))
                .setIcon(R.drawable.ic_autorenew_white_48dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menu.add(0, R.id.menu_item_settings, 0, getString(R.string.settings_menu_title))
                .setShowAsAction(SHOW_AS_ACTION_NEVER);

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String syncFrequency = sharedPref.getString("sync_frequency", "");
//        pref_inappropriate

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_item_search:
                startSearchActivity();
                return true;

            case R.id.menu_item_new_gift:
                startCreateGiftActivity();
                return true;

            case R.id.menu_item_refresh:
                refreshContent();
                return true;

            case R.id.menu_item_disconnect:
                return true;

            case R.id.menu_item_sign_out:
                return true;

            case R.id.menu_item_top_givers:
                startTopGiversActivity();
                return true;
            case R.id.menu_item_settings:
                startSettingsActivity();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        // Result from the account chooser,
        switch (requestCode) {

            case REQ_CHOOSE_ACCOUNT:
                if (resultCode == RESULT_OK) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    session.storeAccountName(accountName);
                    mImmediateSignIn = true;
                    apiClient.fetchCurrentUser(this);
                }
                break;

            case REQ_CONSENT:
                if (resultCode == RESULT_OK) {
                    new CheckOrRetrieveCodeTask().execute();
                }
                break;

            case REQUEST_CODE_GIFT_CREATE:
                if (resultCode == Activity.RESULT_OK) {
                    Gift gift = (Gift) data.getSerializableExtra(IntentsUtils.Extra.CREATED_GIFT);
                    if (giftChainFragment != null && giftChainFragment.isVisible()) {
                        giftChainFragment.addGift(gift);
                    } else {
                        onArticleSelected(gift.giftChainId);
                    }
                } else {
                    Toast.makeText(this, "There were problem when creating a Gift ", Toast.LENGTH_LONG).show();
                }
                break;

            default:
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("SAVED_USER", user);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        onUserRetrieved((User) savedInstanceState.getParcelable("SAVED_USER"));
        invalidateOptionsMenu();
    }

    /*===========================================================================================*/
    /*==                        ApiCallbacks.ServiceCallback                                  ===*/
    /*===========================================================================================*/

    @Override
    public void onUserRetrieved(User user) {

        // We update the action bar to hide the signup button
        setProgressBarIndeterminateVisibility(false);

        if (user != null) {
            invalidateOptionsMenu();
            session().setUser(user);

            if (giftChainFragment != null && giftChainFragment.isVisible()) {
                giftChainFragment.refresh();
            }
        } else {
            Toast.makeText(this, "User was null", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void codeSignInRequired() {
        setProgressBarIndeterminateVisibility(false);
        // For now, we're just immediately popping a new consent.
        Log.e(TAG, "We need a new code");
        session.storeSessionId(null);
        user = null;
        if (mImmediateSignIn) {
            new CheckOrRetrieveCodeTask().execute();
        } else if (mSignInDialog == null || !mSignInDialog.isShowing()) {
            mSignInDialog = new AlertDialog.Builder(this)
                    .setNegativeButton(getString(signin_dialog_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Revert to signed out view
//                                    onSignedOut();
                        }
                    })
                    .setPositiveButton(getString(signin_dialog_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Begin sign in again
                            new CheckOrRetrieveCodeTask().execute();
                        }
                    })
                    .setMessage(getString(signin_dialog_message)).create();

            mSignInDialog.show();
        }
    }

    @Override
    public void onSignOut() {
        // We have signed out or disconnected, so should drop out local state.
        user = null;
        session.setCode(null);
        session.storeSessionId(null);
        session.storeAccountName(null);
        onUserRetrieved(null);
        setProgressBarIndeterminateVisibility(false);
    }

    /*============================================================================================*/
    /*==                          Listener.GiftChainListListener                                ==*/
    /*============================================================================================*/

    @Override
    public void onArticleSelected(Long giftChainId) {

        if (giftChainFragment == null) {
            giftChainFragment = new GiftChainFragment();
        }

        FragmentManager mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.remove(giftChainListFragment);
        fragmentTransaction.add(R.id.fragment_container, giftChainFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        giftChainFragment.loadGiftChain(giftChainId);


    }
    /*============================================================================================*/
    /*==                                         SignIn                                         ==*/
    /*============================================================================================*/

    /**
     * This class will only be instantiated as part of an explicit sign in click,
     * so we need to check the server state immediately.This means testing with the ID
     * token for a session, and if that doesn't work firing off a code retrieve task.
     */
    private class CheckOrRetrieveCodeTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            if (session.getAccountName() == null) {
                return null;
            }

            String code = null;

            try {

                code = session.getCodeSynchronous();

            } catch (UserRecoverableAuthException userEx) {

                startActivityForResult(userEx.getIntent(), REQ_CONSENT);

            } catch (GoogleAuthException gaEx) {
                Log.e(TAG, gaEx.getMessage());
            } catch (ApplicationSession.CodeException e) {
                Log.e(TAG, e.getMessage(), e);
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            return code;
        }

        @Override
        protected void onPostExecute(String code) {
            super.onPostExecute(code);
            if (code != null) {
                setProgressBarIndeterminateVisibility(true);
                session.setCode(code);
                apiClient.fetchCurrentUser(MainActivity.this);
            } else {
                Log.d(TAG, "Invalid user/code response");
            }
            mImmediateSignIn = false;
        }
    }

    /**
     * Begin the non-immediate sign in flow
     */
    private void beginSignInFlow() {

        ApplicationSession.State state = session.checkSessionState(true);

        if (state == ApplicationSession.State.UNAUTHENTICATED) {

            Intent intent = AccountPicker.newChooseAccountIntent(
                    null, null, new String[]{"com.google"},
                    false, null, null, null, null);

            startActivityForResult(intent, REQ_CHOOSE_ACCOUNT);

        } else {

            if (state == ApplicationSession.State.HAS_ACCOUNT) {
                mImmediateSignIn = true;
            } // else state == ApplicationSession.State.HAS_SESSION

            apiClient.fetchCurrentUser(MainActivity.this);

        }
    }
    /*============================================================================================*/
    /*==                                 Menu event handlers                                    ==*/
    /*============================================================================================*/

    private void startSearchActivity() {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    private void startTopGiversActivity() {
        Intent intent = new Intent(this, TopGiversActivity.class);
        startActivity(intent);
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void refreshContent() {
        if (giftChainFragment != null && giftChainFragment.isVisible()) {
            giftChainFragment.refresh();
        }

        if (giftChainListFragment.isVisible()) {
            giftChainListFragment.refresh();
        }
    }

    private void startCreateGiftActivity() {
        Intent intent = new Intent(this, GiftCreateActivity.class);
        Long giftChainId = null;
        if (giftChainFragment != null && giftChainFragment.isVisible()) {
            // We add a new Gift to the currently displayed Gift Chain
            giftChainId = giftChainFragment.getGiftChainId();
        }
        intent.putExtra(IntentsUtils.Extra.GIFT_CHAIN_ID, giftChainId);
        startActivityForResult(intent, REQUEST_CODE_GIFT_CREATE);
    }
}

