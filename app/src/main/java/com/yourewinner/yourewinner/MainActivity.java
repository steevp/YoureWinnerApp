package com.yourewinner.yourewinner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private Forum mForum;
    private ProgressDialog mDialog;
    private SharedPreferences mSharedPreferences;
    private String[] mNavigationItems;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mTitle;
    private ListView mDrawerList;

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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mTitle = getTitle().toString();

        mNavigationItems = getResources().getStringArray(R.array.navigation_items);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mNavigationItems));
        mDrawerList.setOnItemClickListener(this);
        setupDrawer();

        mForum = Forum.getInstance();

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.login_message));
        mDialog.setCancelable(false);

        doWelcome();
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
                getSupportActionBar().setTitle("Navigation");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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
                    selectItem(0);
                }
            });

            alert.show();

        } else if (!mForum.getLogin()) {
            doLogin(username, password);
        } else {
            selectItem(0);
        }
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
                        selectItem(0);
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
                        selectItem(0);
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
                        selectItem(0);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    private void selectItem(int position) {

        Fragment fragment;
        Bundle args = new Bundle();
        switch (position) {
            case 0:
                mTitle = "yourewinner.com";
                fragment = new HomeFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;
            case 1:
                mTitle = mNavigationItems[position];
                fragment = new InboxFragment();
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;
            case 2:
                mTitle = mNavigationItems[position];
                fragment = new SubForumsFragment();
                args.putInt(SubForumsFragment.ARG_PAGE, position);
                fragment.setArguments(args);
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
                break;
            case 3:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivityForResult(intent, 69);
                break;
            default:
                mTitle = mNavigationItems[position];
                break;
        }

        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        recreate();
    }
}
