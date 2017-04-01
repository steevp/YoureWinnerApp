package com.yourewinner.yourewinner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.nio.charset.Charset;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class LoginActivity extends AppCompatActivity {
    private Forum mForum;
    private EditText mUsername;
    private EditText mPassword;
    private PrefsManager mPrefs;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.login_message));
        mDialog.setCancelable(false);

        mUsername = (EditText) findViewById(R.id.username);
        mPassword = (EditText) findViewById(R.id.password);

        mForum = Forum.getInstance();
        mPrefs = new PrefsManager(this);

        setResult(MainActivity.LOGIN_AGAIN);
    }

    public void doLogin(View v) {
        final String username = mUsername.getText().toString().trim();
        final String password = mPassword.getText().toString().trim();

        if (username.length() > 0 && password.length() > 0) {
            mDialog.show();
            mForum.login(username, password, new XMLRPCCallback() {
                @Override
                public void onResponse(long id, Object result) {
                    mForum.setLogin(true);

                    @SuppressWarnings("unchecked")
                    Map<String, Object> r = (Map<String, Object>) result;

                    final boolean canModerate = (boolean) r.get("can_moderate");
                    mForum.setModerator(canModerate);

                    final String avatar = (String) r.get("icon_url");
                    final String displayName = new String((byte[]) r.get("username"), Charset.forName("UTF-8"));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (avatar.length() > 0) {
                                // Store avatar url in prefs
                                mPrefs.setAvatar(avatar);
                            }
                            mPrefs.setModerator(canModerate);
                            Map<String,String> cookies = mForum.getCookies();
                            mPrefs.setUsername(username);
                            mPrefs.setPassword(password);
                            mPrefs.setCookies(cookies);
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "YOU'RE WINNER, " + displayName + " !", Toast.LENGTH_LONG).show();
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(MainActivity.AVATAR, avatar);
                            resultIntent.putExtra(MainActivity.USERNAME, username);
                            setResult(MainActivity.RESULT_RELOAD, resultIntent);
                            finish();
                        }
                    });
                }

                @Override
                public void onError(long id, final XMLRPCException error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            error.printStackTrace();
                            mForum.setLogin(false);
                            mForum.setModerator(false);
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onServerError(long id, final XMLRPCServerException error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            error.printStackTrace();
                            mForum.setLogin(false);
                            mForum.setModerator(false);
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
    }
}
