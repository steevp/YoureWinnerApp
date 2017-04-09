package com.yourewinner.yourewinner;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, PrivateMessageFragment.InboxRefreshListener {

    public final static String DRAWER_ITEM_ID = "DRAWER_ITEM_ID";
    public final static String AVATAR = "AVATAR";
    public final static String USERNAME = "USERNAME";
    public final static int RESULT_RELOAD = 666;
    public final static int LOGIN_AGAIN = 777;

    private Forum mForum;
    private PrefsManager mPrefs;
    private DrawerLayout mDrawerLayout;
    private AppBarLayout mAppBarLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mDrawerView;
    private int mDrawerItemId;

    private TextView mUsernameView;
    private CircleImageView mAvatarView;
    private String mUsername;
    private String mAvatar;
    private ImageView mBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPrefs = new PrefsManager(this);

        Config.loadTheme(this);
        mForum = Forum.getInstance();

        super.onCreate(savedInstanceState);
        if (mPrefs.getBanner()) {
            // Rotating banner
            setContentView(R.layout.activity_main);
        } else {
            // No banner
            setContentView(R.layout.activity_main_no_banner);
        }

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
        mAppBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);
        setupCollapsingToolbar();
        mDrawerView = (NavigationView) findViewById(R.id.left_drawer);
        mDrawerView.setNavigationItemSelectedListener(this);

        View drawerHeader = getLayoutInflater().inflate(R.layout.navigation_header, null);
        mAvatarView = (CircleImageView) drawerHeader.findViewById(R.id.avatar);
        mUsernameView = (TextView) drawerHeader.findViewById(R.id.username);

        mBanner = (ImageView) findViewById(R.id.banner);
        loadBanner();

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

        if (username.length() == 0 || password.length() == 0 || !mForum.getLogin()) {
            startLogin();
        } else {
            setupDrawerHeader();
            selectItem(mDrawerItemId);
        }
    }

    private void startLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    public void doLogout() {
        mPrefs.clearAllCookies();
        mAvatar = "";
        mUsername = "Guest";
        mForum.setLogin(false);
        mForum.setModerator(false);
        mForum.setUsername(mUsername);
        setupDrawerHeader();
        startLogin();
    }

    private void setupCollapsingToolbar() {
        if (mCollapsingToolbarLayout != null) {
            getSupportActionBar().setTitle(" ");
            mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                private boolean mCollapsed = false;
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (mCollapsed && verticalOffset == 0) {
                        Log.i("ywtag", "appbar expanded");
                        mCollapsed = false;
                        mCollapsingToolbarLayout.setTitle(" ");
                    } else if (!mCollapsed && Math.abs(verticalOffset) >= mAppBarLayout.getTotalScrollRange()) {
                        Log.i("ywtag", "appbar collapsed");
                        mCollapsed = true;
                        mCollapsingToolbarLayout.setTitle(getString(R.string.app_name));
                        loadBanner();
                    }
                }
            });
        }
    }

    private void loadBanner() {
        if (mBanner != null) {
            Glide.with(this)
                    .load("https://www.yourewinner.com/banners/steevbanner.php")
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(mBanner);
        }
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

        switch (resultCode) {
            case RESULT_RELOAD:
                if (data != null) {
                    mAvatar = data.getStringExtra(AVATAR);
                    mUsername = data.getStringExtra(USERNAME);
                }
                recreate();
                break;
            case LOGIN_AGAIN:
                startLogin();
                break;
        }
    }

    @Override
    protected void resumeThread() {
        doWelcome();
    }
}
