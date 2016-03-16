package com.yourewinner.yourewinner;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class SearchActivity extends AppCompatActivity
        implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {

    private Forum mForum;
    private SearchView mSearchView;
    private ListView mPostsList;
    private PostAdapter mPostAdapter;
    private SwipeRefreshLayout mSwipeContainer;
    private String mQuery;
    private View mFooter;

    private int lastCount = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean userScrolled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mForum = Forum.getInstance();

        mPostsList = (ListView) findViewById(R.id.posts_list);
        mPostsList.setOnItemClickListener(this);
        mPostsList.setOnScrollListener(this);
        mPostAdapter = new PostAdapter(this, getLayoutInflater());
        mPostsList.setAdapter(mPostAdapter);
        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeContainer.setOnRefreshListener(this);

        mFooter = (View) getLayoutInflater().inflate(R.layout.loading, null);

        lastCount = 0;
        currentPage = 1;
        userScrolled = false;
        isLoading = false;

        //searchTopic();
    }

    public void searchTopic() {
        mPostsList.removeFooterView(mFooter);

        isLoading = true;
        mPostsList.addFooterView(mFooter);

        mForum.searchTopic(mQuery, currentPage, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String, Object> r = (Map<String, Object>) result;
                final Object[] topics = (Object[]) r.get("topics");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostsList.removeFooterView(mFooter);
                        mPostAdapter.updateData(topics);
                        isLoading = false;
                        mSwipeContainer.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostsList.removeFooterView(mFooter);
                        isLoading = false;
                        mSwipeContainer.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                error.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostsList.removeFooterView(mFooter);
                        isLoading = false;
                        mSwipeContainer.setRefreshing(false);
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
        getMenuInflater().inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

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
            mQuery = intent.getStringExtra(SearchManager.QUERY).trim();
            if (mQuery.length() > 0) {
                getSupportActionBar().setTitle("\"" + mQuery + "\"");
                clearSearch();
                searchTopic();
            }
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

    @Override
    public void onRefresh() {
        clearSearch();
        searchTopic();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
            userScrolled = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final int lastItem = firstVisibleItem + visibleItemCount - mPostsList.getFooterViewsCount();
        if (userScrolled && lastItem == totalItemCount && totalItemCount > lastCount && !isLoading) {
            isLoading = true;
            lastCount = totalItemCount;
            addMoreItems();
        }
    }

    private void addMoreItems() {
        currentPage++;
        //mPostsList.addFooterView(mFooter);
        searchTopic();
    }

    private void clearSearch() {
        mPostAdapter.clear();
        lastCount = 0;
        currentPage = 1;
        userScrolled = false;
        isLoading = false;
    }
}
