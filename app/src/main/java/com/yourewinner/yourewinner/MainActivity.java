package com.yourewinner.yourewinner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class MainActivity extends AppCompatActivity {

    Forum mForum;
    ListView mRecentPostsList;
    PostAdapter mPostAdapter;
    ProgressDialog mDialog;
    ProgressBar mProgress;
    SharedPreferences mSharedPreferences;
    SwipeRefreshLayout mSwipeContainer;

    ViewPager mViewPager;
    TabLayout mTabLayout;

    Boolean isLoading;
    Integer lastCount;
    Integer currentPage;

    View mFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final String theme = mSharedPreferences.getString("theme", "0");

        switch (theme) {
            case "0":
                setTheme(R.style.AppTheme);
                break;
            case "1":
                setTheme(R.style.GayPrideTheme);
                break;
            case "2":
                setTheme(R.style.StonerTheme);
                break;
            case "3":
                setTheme(R.style.DarkTheme);
                break;
            case "4":
                setTheme(R.style.LightTheme);
                break;
            default:
                setTheme(R.style.AppTheme);
                break;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mForum = Forum.getInstance();

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.login_message));
        mDialog.setCancelable(false);

        isLoading = false;
        lastCount = 0;
        currentPage = 1;

        doWelcome();
    }

    public void setupTabLayout() {
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new PostsViewPagerAdapter(getSupportFragmentManager(), MainActivity.this));
        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void doWelcome() {
        final String username = mSharedPreferences.getString("username", "");
        final String password = mSharedPreferences.getString("password", "");

        if (username.length() == 0 || password.length() == 0) {
            // Show sign in dialog
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final View input = getLayoutInflater().inflate(R.layout.dialog_signin, null);
            alert.setView(input);
            alert.setPositiveButton(R.string.signin, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText inputUsername = (EditText) input.findViewById(R.id.username);
                    EditText inputPassword = (EditText) input.findViewById(R.id.password);
                    String user = inputUsername.getText().toString();
                    String pass = inputPassword.getText().toString();
                    if (user.length() > 0 && pass.length() > 0) {
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString("username", user);
                        editor.putString("password", pass);
                        editor.commit();
                        doLogin(user, pass);
                    }
                }
            });

            alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //getRecent();
                }
            });

            alert.show();

        } else if (!mForum.getLogin()) {
            doLogin(username, password);
        }

        setupTabLayout();
    }

    public void doLogin(final String username, final String password) {
        mDialog.show();
        mForum.login(username, password, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                mForum.setLogin(true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "YOU'RE WINNER, " + username + " !", Toast.LENGTH_LONG).show();
                        //setupTabLayout();
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
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
                        //setupTabLayout();
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mForum.setLogin(false);
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
                        //setupTabLayout();
                    }
                });
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClassName(this, "com.yourewinner.yourewinner.MyPreferenceActivity");
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
