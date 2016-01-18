package com.yourewinner.yourewinner;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private Forum mForum;
    private SharedPreferences mSharedPreferences;
    private SearchView mSearchView;
    private ListView mPostList;
    private PostAdapter mPostAdapter;
    private String mQuery;
    private View mFooter;

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
        setContentView(R.layout.activity_search);

        mForum = Forum.getInstance();

        mPostList = (ListView) findViewById(R.id.post_list);
        mPostList.setOnItemClickListener(this);
        mPostAdapter = new PostAdapter(this, getLayoutInflater());
        mPostList.setAdapter(mPostAdapter);

        mFooter = (View) getLayoutInflater().inflate(R.layout.loading, null);
        mPostList.addFooterView(mFooter);

        handleIntent(getIntent());

        searchTopic();
    }

    public void searchTopic() {
        mForum.searchTopic(mQuery, 1, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String,Object> r = (Map<String,Object>) result;
                final Object[] topics = (Object[]) r.get("topics");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostList.removeFooterView(mFooter);
                        mPostAdapter.updateData(topics);
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostList.removeFooterView(mFooter);
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostList.removeFooterView(mFooter);
                    }
                });
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mQuery = intent.getStringExtra(SearchManager.QUERY);
            getSupportActionBar().setTitle("\"" + mQuery + "\"");
        }
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
}
