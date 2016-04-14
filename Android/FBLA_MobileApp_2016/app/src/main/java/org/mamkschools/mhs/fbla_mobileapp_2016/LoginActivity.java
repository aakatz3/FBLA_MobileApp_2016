package org.mamkschools.mhs.fbla_mobileapp_2016;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.mamkschools.mhs.fbla_mobileapp_2016.lib.*;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {


    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mUserView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setTitle(R.string.title_activity_login);

        Constants.restorePrefs(getApplicationContext());

        if(Constants.AUTHCODE != null){
            startActivity(new Intent(getApplicationContext(), MainSwipeActivity.class));
        }


        // Set up the login form.
        mUserView = (AutoCompleteTextView) findViewById(R.id.username);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptAuth(true);
                    return true;
                }
                return false;
            }
        });

        Button mUsernameSignInButton = (Button) findViewById(R.id.username_sign_in_button);
        assert mUsernameSignInButton != null;
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAuth(true);
            }
        });

        Button mUsernameRegisterButton = (Button) findViewById(R.id.username_register_button);
        assert mUsernameRegisterButton != null;
        mUsernameRegisterButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptAuth(false);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    protected void onStop(){
        super.onStop();
        Constants.PREFS_RESTORED = false;
        Constants.savePrefs(getApplicationContext(), false);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptAuth(boolean login) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        //Check if internet is accessible
        if(!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(),
                    "Please connect to the internet in order to login", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check for a valid username.
        if (username.length() < 5) {
            mUserView.setError(getString(R.string.error_invalid_username));
            mUserView.requestFocus();
            return;
        }
        // Check for a valid password, if the user entered one.
        if (!login && password.length() < 8) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            mPasswordView.requestFocus();
            return;
        }
        showProgress(true);
        mAuthTask = new UserLoginTask(username, password, login);
        mAuthTask.execute((Void) null);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private Map<String, String> creds;
        JSONObject returnedJSON = null;
        private boolean isLogin;

        UserLoginTask(String username, String password, boolean loginChoice) {
            isLogin = loginChoice;
            creds = new HashMap<String,String>();
            creds.put("username", username);
            creds.put("password", password);
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            SecureAPI loginAPI = SecureAPI.getInstance(LoginActivity.this);

            try {
                returnedJSON = loginAPI.HTTPSPOST(isLogin ? Commands.Post.LOGIN : Commands.Post.REGISTER, creds);

                String status = returnedJSON.getString("status");
                String message = returnedJSON.getString("message");
                if(status.equals("error")) {
                    if(isLogin) {
                        return false;
                    } else {

                        return false;
                    }
                } else if(status.equals("success")) {
                    if(isLogin) {
                        Constants.AUTHCODE = returnedJSON.getJSONObject("data").getString("authcode");

                    }
                    return true;
                } else{
                    throw new IllegalStateException(status + ": " + message);
                }
            } catch (JSONException jse){
                if(Debug.DEBUG_MODE){
                    Debug.log(jse.getMessage());
                }
                return false;
            } catch (IllegalStateException ise){
                if(Debug.DEBUG_MODE){
                    Debug.log("Invalid message: " + ise.getMessage());
                }
                return false;
            } catch (Exception ex){
                Debug.log(ex.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if(success) {
                if(isLogin) {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("AUTHCODE", Constants.AUTHCODE);

                    editor.apply();
                    startActivity(new Intent(getApplicationContext(), MainSwipeActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), R.string.registration_complete, Toast.LENGTH_LONG).show();
                    mAuthTask = new UserLoginTask(creds.get("username"), creds.get("password"), true);
                    mAuthTask.execute((Void) null);
                }
            } else {

                if(isLogin) {
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                } else {
                    mUserView.setError(getString(R.string.error_user_exists));
                    mUserView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}