package com.yourewinner.yourewinner;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PrivateMessageFragment.InboxRefreshListener {

    public final static String DRAWER_ITEM_ID = "DRAWER_ITEM_ID";
    public final static String AVATAR = "AVATAR";
    public final static String USERNAME = "USERNAME";

    private Forum mForum;
    private ProgressDialog mDialog;
    private SharedPreferences mSharedPreferences;
    private String[] mNavigationItems;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mTitle;
    //private ListView mDrawerList;
    private NavigationView mDrawerView;
    private View mDrawerHeader;
    private int mDrawerItemId;

    private TextView mUsernameView;
    private CircleImageView mAvatarView;
    private String mUsername;
    private String mAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mDrawerItemId = savedInstanceState.getInt(DRAWER_ITEM_ID);
            mUsername = savedInstanceState.getString(USERNAME);
            mAvatar = savedInstanceState.getString(AVATAR);
        } else {
            mDrawerItemId = R.id.drawer_home;
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mTitle = getTitle().toString();

        mNavigationItems = getResources().getStringArray(R.array.navigation_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerView = (NavigationView) findViewById(R.id.left_drawer);
        mDrawerView.setNavigationItemSelectedListener(this);

        View drawerHeader = getLayoutInflater().inflate(R.layout.navigation_header, null);
        mAvatarView = (CircleImageView) drawerHeader.findViewById(R.id.avatar);
        mUsernameView = (TextView) drawerHeader.findViewById(R.id.username);

        mDrawerView.addHeaderView(drawerHeader);
        mDrawerView.getMenu().findItem(mDrawerItemId).setChecked(true);
        setupDrawer();

        mForum = Forum.getInstance();

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.login_message));
        mDialog.setCancelable(false);

        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (action.equals(Intent.ACTION_VIEW)) {
            handleUri(intent.getData());
        } else {
            doWelcome();
        }
    }

    private void handleUri(Uri uri) {
        final String topic = uri.getQueryParameter("topic");
        if (topic != null) {
            final String topicID = topic.split("\\.")[0];
            final Intent intent = new Intent(this, TopicViewActivity.class);
            intent.putExtra("topicID", topicID);
            startActivity(intent);
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mDrawerItemId = intent.getIntExtra(DRAWER_ITEM_ID, R.id.drawer_home);
        //mUsername = intent.getStringExtra(USERNAME);
        //mAvatar = intent.getStringExtra(AVATAR);
        mDrawerView.getMenu().findItem(mDrawerItemId).setChecked(true);
        selectItem(mDrawerItemId);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void setupDrawerHeader() {
        if (mAvatar.length() > 0) {
            Picasso.with(getApplicationContext()).load(mAvatar).placeholder(R.mipmap.no_avatar).fit().into(mAvatarView);
        } else {
            mAvatarView.setImageResource(R.mipmap.no_avatar);
        }
        mUsernameView.setText(mUsername);
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
                    selectItem(mDrawerItemId);
                }
            });

            alert.show();

        } else if (!mForum.getLogin()) {
            doLogin(username, password);
        } else {
            setupDrawerHeader();
            selectItem(mDrawerItemId);
        }
    }

    public void doLogin(final String username, final String password) {
        mDialog.show();
        mForum.login(username, password, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                mForum.setLogin(true);

                Map<String, Object> r = (Map<String, Object>) result;
                mAvatar = (String) r.get("icon_url");
                mUsername = new String((byte[]) r.get("username"), StandardCharsets.UTF_8);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupDrawerHeader();
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "YOU'RE WINNER, " + username + " !", Toast.LENGTH_LONG).show();
                        selectItem(mDrawerItemId);
                        checkNewMessages();
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
                        selectItem(mDrawerItemId);
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
                        selectItem(mDrawerItemId);
                    }
                });
            }
        });
    }

    private void checkNewMessages() {
        mForum.getInboxStat(new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String, Object> r = (Map<String, Object>) result;
                final int unreadCount = (int) r.get("inbox_unread_count");
                if (unreadCount > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Toast.makeText(getApplicationContext(), "You've got mail!", Toast.LENGTH_LONG).show();
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                                    .setSmallIcon(R.drawable.ic_message)
                                    .setContentTitle("You've got mail!")
                                    .setContentText(unreadCount + " unread message(s)")
                                    .setAutoCancel(true);
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra(DRAWER_ITEM_ID, R.id.drawer_messages);
                            //intent.putExtra(USERNAME, mUsername);
                            //intent.putExtra(AVATAR, mAvatar);
                            PendingIntent pi = PendingIntent.getActivity(
                                    getApplicationContext(),
                                    0,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                            builder.setContentIntent(pi);
                            NotificationManager notifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            notifyMgr.notify(001, builder.build());
                        }
                    });
                }
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    private void selectItem(int id) {

        Fragment fragment;
        Bundle args = new Bundle();
        Intent intent;
        switch (id) {
            case R.id.drawer_home:
                mTitle = "yourewinner.com";
                fragment = new HomeFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;
            case R.id.drawer_news:
                mTitle = getString(R.string.action_news);
                fragment = new NewsFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;
            case R.id.drawer_profile:
                intent = new Intent(this, ProfileViewActivity.class);
                intent.putExtra("username", mUsername);
                startActivity(intent);
                break;
            case R.id.drawer_messages:
                mTitle = getString(R.string.action_messages);
                fragment = new InboxFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;
            case R.id.drawer_browse:
                //mTitle = mNavigationItems[position];
                mTitle = getString(R.string.action_browse);
                fragment = new SubForumsFragment();
                args.putInt(SubForumsFragment.ARG_PAGE, 1);
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;
            case R.id.drawer_settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.drawer_search:
                intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            default:
                mTitle = "yourewinner.com";
                break;
        }

        //mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerView);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        item.setChecked(true);
        selectItem(item.getItemId());
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(DRAWER_ITEM_ID, mDrawerItemId);
        outState.putString(USERNAME, mUsername);
        outState.putString(AVATAR, mAvatar);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onInboxRefresh() {
        // Reload the inbox fragment
        Fragment fragment = new InboxFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }
}
