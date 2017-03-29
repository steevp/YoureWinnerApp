package com.yourewinner.yourewinner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, PrivateMessageFragment.InboxRefreshListener {

    public final static String DRAWER_ITEM_ID = "DRAWER_ITEM_ID";
    public final static String AVATAR = "AVATAR";
    public final static String USERNAME = "USERNAME";
    public final static int RESULT_RELOAD = 666;

    private Forum mForum;
    private ProgressDialog mDialog;
    private PrefsManager mPrefs;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mDrawerView;
    private int mDrawerItemId;

    private TextView mUsernameView;
    private CircleImageView mAvatarView;
    private String mUsername;
    private String mAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPrefs = new PrefsManager(this);

        Config.loadTheme(this);
        mForum = Forum.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mDrawerItemId = savedInstanceState.getInt(DRAWER_ITEM_ID);
            mUsername = savedInstanceState.getString(USERNAME);
            mAvatar = savedInstanceState.getString(AVATAR);
        } else {
            mDrawerItemId = R.id.drawer_home;
            //mForum.setLogin(false);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerView = (NavigationView) findViewById(R.id.left_drawer);
        mDrawerView.setNavigationItemSelectedListener(this);

        View drawerHeader = getLayoutInflater().inflate(R.layout.navigation_header, null);
        mAvatarView = (CircleImageView) drawerHeader.findViewById(R.id.avatar);
        mUsernameView = (TextView) drawerHeader.findViewById(R.id.username);

        // Toggles the logout menu
        ToggleButton toggleLogout = (ToggleButton) drawerHeader.findViewById(R.id.toggle_logout);
        toggleLogout.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mDrawerView.getMenu().setGroupVisible(R.id.logout_group, mForum.getLogin() && b);
            }
        });

        mDrawerView.addHeaderView(drawerHeader);
        mDrawerView.getMenu().findItem(mDrawerItemId).setChecked(true);
        setupDrawer();

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
            intent.putExtra(TopicViewActivity.ARG_TOPIC_ID, topicID);
            startActivityForResult(intent, 1);
        }
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mDrawerItemId = intent.getIntExtra(DRAWER_ITEM_ID, R.id.drawer_home);
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
                //getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void setupDrawerHeader() {
        if (mAvatar.length() > 0) {
            Picasso.with(this).load(mAvatar).placeholder(R.drawable.no_avatar).fit().into(mAvatarView);
        } else {
            mAvatarView.setImageResource(R.drawable.no_avatar);
        }
        mUsernameView.setText(mUsername);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager sm = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView sv = (SearchView) menu.findItem(R.id.action_search).getActionView();
        sv.setSearchableInfo(sm.getSearchableInfo(getComponentName()));
        return true;
    }

    public void doWelcome() {
        final String username = mPrefs.getUsername();
        final String password = mPrefs.getPassword();
        if (mAvatar == null) {
            mAvatar = mPrefs.getAvatar();
        }
        if (mUsername == null) {
            mUsername = username;
        }
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
                        mPrefs.setUsername(user);
                        mPrefs.setPassword(pass);
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
        final long id = mForum.login(username, password, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                setThreadId(0);
                mForum.setLogin(true);

                Map<String, Object> r = (Map<String, Object>) result;

                final boolean canModerate = (boolean) r.get("can_moderate");
                mForum.setModerator(canModerate);

                mAvatar = (String) r.get("icon_url");
                mUsername = new String((byte[]) r.get("username"), StandardCharsets.UTF_8);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mAvatar.length() > 0) {
                            // Store avatar url in prefs
                            mPrefs.setAvatar(mAvatar);
                        }
                        mPrefs.setModerator(canModerate);
                        Map<String,String> cookies = mForum.getCookies();
                        mPrefs.setCookies(cookies);
                        setupDrawerHeader();
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "YOU'RE WINNER, " + username + " !", Toast.LENGTH_LONG).show();
                        selectItem(mDrawerItemId);
                        //checkNewMessages();
                    }
                });
            }

            @Override
            public void onError(long id, final XMLRPCException error) {
                setThreadId(0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        error.printStackTrace();
                        mForum.setLogin(false);
                        mForum.setModerator(false);
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
                        selectItem(mDrawerItemId);
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                setThreadId(0);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mForum.setLogin(false);
                        mForum.setModerator(false);
                        mDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
                        selectItem(mDrawerItemId);
                    }
                });
            }
        });
        setThreadId(id);
    }

    public void doLogout() {
        mPrefs.clearAllCookies();
        mAvatar = "";
        mUsername = "Guest";
        mForum.setLogin(false);
        mForum.setModerator(false);
        mForum.setUsername(mUsername);
        setupDrawerHeader();
        Toast.makeText(this, "Good-bye!", Toast.LENGTH_LONG).show();
        finish();
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

        return super.onOptionsItemSelected(item);
    }

    private void selectItem(int id) {

        Fragment fragment = null;
        Intent intent = null;
        switch (id) {
            case R.id.drawer_home:
                fragment = new HomeFragment();
                break;
            case R.id.drawer_news:
                fragment = new NewsFragment();
                break;
            case R.id.drawer_profile:
                intent = new Intent(this, ProfileViewActivity.class);
                intent.putExtra("username", mUsername);
                break;
            case R.id.drawer_messages:
                fragment = new InboxFragment();
                break;
            case R.id.drawer_browse:
                fragment = new SubForumsFragment();
                Bundle args = new Bundle();
                args.putInt(SubForumsFragment.ARG_PAGE, 1);
                fragment.setArguments(args);
                break;
            case R.id.drawer_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.drawer_search:
                intent = new Intent(this, SearchActivity.class);
                break;
            case R.id.drawer_logout:
                doLogout();
                break;
        }

        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
        } else if (intent != null) {
            startActivityForResult(intent, 1);
        }

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
    public void onInboxRefresh() {
        // Reload the inbox fragment
        Fragment fragment = new InboxFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_RELOAD) {
            // recreate activity to load new theme
            recreate();
        }
    }

    @Override
    protected void resumeThread() {
        doWelcome();
    }
}
