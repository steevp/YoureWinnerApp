package com.yourewinner.yourewinner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    Forum mForum;
    ListView mRecentPostsList;
    PostAdapter mPostAdapter;
    ProgressDialog mDialog;
    ProgressBar mProgress;

    Boolean isLoading;
    Integer lastCount;
    Integer currentPage;

    View mFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mForum = Forum.getInstance();

        //mProgress = new ProgressBar(this);
        //mProgress.setIndeterminate(true);
        //mProgress.setVisibility(View.VISIBLE);

        mRecentPostsList = (ListView) findViewById(R.id.posts_list);

        mFooter = getLayoutInflater().inflate(R.layout.loading, null);

        //mRecentPostsList.addFooterView(loading);

        mPostAdapter = new PostAdapter(this, getLayoutInflater());
        mRecentPostsList.setAdapter(mPostAdapter);
        mRecentPostsList.setOnItemClickListener(this);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage("Loading...");
        mDialog.setCancelable(false);

        isLoading = false;
        lastCount = 0;
        currentPage = 1;

        mRecentPostsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                final int lastItem = firstVisibleItem + visibleItemCount - mRecentPostsList.getFooterViewsCount();
                if (lastItem == totalItemCount && totalItemCount != lastCount && !isLoading) {
                    isLoading = true;
                    lastCount = totalItemCount;
                    addMoreItems();
                }
            }
        });

        doLogin();
        //getRecent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void doLogin() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String username = prefs.getString("username", "");
        String password = prefs.getString("password", "");

        if (username.length() == 0 || password.length() == 0) {
            //TODO
        } else {
            mForum.login(username, password, new XMLRPCCallback() {
                @Override
                public void onResponse(long id, Object result) {
                    mForum.setLogin(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "YOU'RE WINNER, " + username + " !", Toast.LENGTH_LONG).show();
                            getRecent();
                        }
                    });
                }

                @Override
                public void onError(long id, XMLRPCException error) {
                    mForum.setLogin(false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
                            getRecent();
                        }
                    });
                }

                @Override
                public void onServerError(long id, XMLRPCServerException error) {
                    mForum.setLogin(false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
                            getRecent();
                        }
                    });
                }
            });
        }
    }

    public void getRecent() {
        mRecentPostsList.addFooterView(mFooter);

        mForum.getRecent(currentPage, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String, Object> r = (Map<String, Object>) result;
                final Object[] topics = (Object[]) r.get("topics");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mDialog.dismiss();
                        //loading.setVisibility(View.GONE);
                        mRecentPostsList.removeFooterView(mFooter);
                        mPostAdapter.updateData(topics);
                        isLoading = false;
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mDialog.dismiss();
                        //loading.setVisibility(View.GONE);
                        mRecentPostsList.removeFooterView(mFooter);
                        isLoading = false;
                        Toast.makeText(getApplicationContext(), "FUCK: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //mDialog.dismiss();
                        //loading.setVisibility(View.GONE);
                        mRecentPostsList.removeFooterView(mFooter);
                        isLoading = false;
                        Toast.makeText(getApplicationContext(), "FUCK: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void addMoreItems() {
        currentPage++;
        Log.d("yourewinner", "Loading more " + currentPage.toString());
        getRecent();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String,Object> topic = (Map<String,Object>) mPostAdapter.getItem(position);
        if (topic != null) {
            String topicID = (String) topic.get("topic_id");
            Intent intent = new Intent(this, TopicViewActivity.class);
            intent.putExtra("topicID", topicID);
            startActivity(intent);
        }
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
