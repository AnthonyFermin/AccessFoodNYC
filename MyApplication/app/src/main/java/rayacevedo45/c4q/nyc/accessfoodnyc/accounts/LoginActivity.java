package rayacevedo45.c4q.nyc.accessfoodnyc.accounts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Arrays;

import rayacevedo45.c4q.nyc.accessfoodnyc.MapsActivity;
import rayacevedo45.c4q.nyc.accessfoodnyc.ParseApplication;
import rayacevedo45.c4q.nyc.accessfoodnyc.ProfileActivity;
import rayacevedo45.c4q.nyc.accessfoodnyc.R;

public class LoginActivity extends Activity {

    //private static final String TAG = "LoginActivity";

    //private static final int RC_SIGN_IN = 0;

    // Is there a ConnectionResult resolution in progress?
    //private boolean mIsResolving = false;

    // Should we automatically resolve ConnectionResults when possible?
    //private boolean mShouldResolve = false;

    //private GoogleApiClient mGoogleApiClient;
    CallbackManager callbackManager;

    protected EditText usernameEditText;
    protected EditText passwordEditText;
    protected EditText usernameEditText2;
    protected EditText passwordEditText2;
    protected EditText emailField;
    protected Button loginButton;
    protected Button signUpButton;
    protected SignInButton mSignInButton;
    protected Button backButton;
    protected Button continueButton;
    protected LinearLayout layout;
    protected ParseApplication app;

    private LoginButton mButtonFacebookLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_login);

        app = new ParseApplication();


        callbackManager = CallbackManager.Factory.create();

        // Build GoogleApiClient with access to basic profile
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
//                    @Override
//                    public void onConnected(Bundle bundle) {
//                        // onConnected indicates that an account was selected on the device, that the selected
//                        // account has granted any requested permissions to our app and that we were able to
//                        // establish a service connection to Google Play services.
//                        Log.d(TAG, "onConnected:" + bundle);
//                        mShouldResolve = false;
//
//                        // Show the signed-in UI
//                        //showSignedInUI();
//                        Toast.makeText(getApplicationContext(), "Logged in via Google", Toast.LENGTH_SHORT).show();
//                        goToMapsActivity();
//                        //new GetIdTokenTask().execute();
//                    }
//
//                    @Override
//                    public void onConnectionSuspended(int i) {
//
//                    }
//                })
//                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
//                    @Override
//                    public void onConnectionFailed(ConnectionResult connectionResult) {
//
//                        if (!mIsResolving && mShouldResolve) {
//                            if (connectionResult.hasResolution()) {
//                                try {
//                                    connectionResult.startResolutionForResult(LoginActivity.this, RC_SIGN_IN);
//                                    mIsResolving = true;
//                                } catch (IntentSender.SendIntentException e) {
//                                    Log.e(TAG, "Could not resolve ConnectionResult.", e);
//                                    mIsResolving = false;
//                                    mGoogleApiClient.connect();
//                                }
//                            } else {
//                                // Could not resolve the connection result, show the user an
//                                // error dialog.
//                                //showErrorDialog(connectionResult);
//                            }
//                        } else {
//                            // Show the signed-out UI
//                            //showSignedOutUI();
//                        }
//                    }
//                })
//                .addApi(Plus.API)
//                .addScope(new Scope(Scopes.PROFILE))
//                .build();

        signUpButton = (Button)findViewById(R.id.signupButtonID);
        usernameEditText = (EditText)findViewById(R.id.usernameField);
        passwordEditText = (EditText)findViewById(R.id.passwordField);
        usernameEditText2 = (EditText)findViewById(R.id.usernameField2);
        passwordEditText2 = (EditText)findViewById(R.id.passwordField2);
        loginButton = (Button)findViewById(R.id.loginButton);
        emailField = (EditText) findViewById(R.id.emailFieldID);
        //mSignInButton = (SignInButton) findViewById(R.id.googleSigninID);
        backButton = (Button) findViewById(R.id.BackButtonID);
        continueButton = (Button) findViewById(R.id.ContinueButtonID);
        layout = (LinearLayout) findViewById(R.id.layoutID);
        mButtonFacebookLogin = (LoginButton) findViewById(R.id.login_button);

        mButtonFacebookLogin.setReadPermissions(Arrays.asList("public_profile", "user_friends"));

        mButtonFacebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                final AccessToken token = loginResult.getAccessToken();

                ParseFacebookUtils.logInInBackground(token, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (parseUser == null) {
                            Toast.makeText(getApplicationContext(), "Uh oh. The user cancelled the Facebook login.", Toast.LENGTH_SHORT).show();
                            Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                        } else if (parseUser.isNew()) {
                            Toast.makeText(getApplicationContext(), "User signed up and logged in through Facebook!", Toast.LENGTH_SHORT).show();
                            Log.d("MyApp", "User signed up and logged in through Facebook!");
                        } else {
                            
                            Toast.makeText(getApplicationContext(), "User logged in through Facebook!", Toast.LENGTH_SHORT).show();
                            Log.d("MyApp", "User logged in through Facebook!");
                        }
                    }
                });


            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                username = username.trim();
                password = password.trim();

                if (username.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(R.string.login_error_message)
                            .setTitle(R.string.login_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    setProgressBarIndeterminateVisibility(true);

                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            setProgressBarIndeterminateVisibility(false);

                            if (e == null) {
                                // Success!
                                goToProfileActivity();
                                //finish();
                            } else {
                                // Fail
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setMessage(e.getMessage())
                                        .setTitle(R.string.login_error_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            }
        });

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText2.getText().toString();
                String password = passwordEditText2.getText().toString();
                String email = emailField.getText().toString();

                username = username.trim();
                password = password.trim();
                email = email.trim();

                if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(R.string.signup_error_message)
                            .setTitle(R.string.signup_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else {
                    setProgressBarIndeterminateVisibility(true);

                    ParseUser newUser = new ParseUser();
                    newUser.setUsername(username);
                    newUser.setPassword(password);
                    newUser.setEmail(email);
                    newUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            setProgressBarIndeterminateVisibility(false);

                            if (e == null) {
                                // Success!
                                Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setMessage(e.getMessage())
                                        .setTitle(R.string.signup_error_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void setUpListeners(boolean isResumed) {
        if (isResumed) {
//            mSignInButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onSignInClicked();
//                }
//            });

        } else {
//            mSignInButton.setOnClickListener(null);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpListeners(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        setUpListeners(false);
    }


    @Override
    protected void onStart() {
        super.onStart();
        //mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
    public void showSignUpFields (View v){

            mSignInButton.setVisibility(View.GONE);
            usernameEditText.setVisibility(View.GONE);
            passwordEditText.setVisibility(View.GONE);
            usernameEditText2.setVisibility(View.VISIBLE);
            passwordEditText2.setVisibility(View.VISIBLE);
            emailField.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            signUpButton.setVisibility(View.GONE);
            backButton.setVisibility(View.VISIBLE);
            continueButton.setVisibility(View.VISIBLE);
            layout.setVisibility(View.GONE);


    }

    public void back (View v){
        mSignInButton.setVisibility(View.VISIBLE);
        usernameEditText.setVisibility(View.VISIBLE);
        passwordEditText.setVisibility(View.VISIBLE);
        usernameEditText2.setVisibility(View.GONE);
        passwordEditText2.setVisibility(View.GONE);
        emailField.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);
        signUpButton.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.GONE);
        continueButton.setVisibility(View.GONE);
        layout.setVisibility(View.VISIBLE);
    }

//    private void onSignInClicked() {
//        // User clicked the sign-in button, so begin the sign-in process and automatically
//        // attempt to resolve any errors that occur.
//        mShouldResolve = true;
//        mGoogleApiClient.connect();
//
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        //ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);

        //Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

//        if (requestCode == RC_SIGN_IN) {
//            // If the error resolution was not successful we should not resolve further.
//            if (resultCode != RESULT_OK) {
//                mShouldResolve = false;
//            }
//
//            mIsResolving = false;
//            mGoogleApiClient.connect();
//        }
    }

    private void goToMapsActivity() {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToProfileActivity() {
        Intent intent = new Intent(LoginActivity.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}


