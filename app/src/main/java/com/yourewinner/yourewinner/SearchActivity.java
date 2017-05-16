package com.yourewinner.yourewinner;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class SearchActivity extends AppCompatActivity
        implements PostsAdapter.OnItemClickedListener, SwipeRefreshLayout.OnRefreshListener {
    private Forum mForum;
    private SearchView mSearchView;
    private RecyclerView mRecyclerView;
    private PostsAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeContainer;
    private String mQuery;

    private int lastCount = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean userScrolled = false;

    private String mSearchUser;
    private boolean mSearchTitle;

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

        mRecyclerView = (RecyclerView) findViewById(R.id.posts_recycler);
        mLayoutManager = new LinearLayoutManager(mRecyclerView.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new PostsAdapter(mRecyclerView.getContext(), this);
        mRecyclerView.setAdapter(mAdapter);
        // Add divider
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(), mLayoutManager.getOrientation()));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolled = true;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = mLayoutManager.findFirstVisibleItemPosition() + mRecyclerView.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();

                if (userScrolled && lastVisibleItem == totalItemCount && totalItemCount > lastCount && !isLoading) {
                    isLoading = true;
                    lastCount = totalItemCount;
                    addMoreItems();
                }
            }
        });

        mSwipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeContainer.setOnRefreshListener(this);

        lastCount = 0;
        currentPage = 1;
        userScrolled = false;
        isLoading = false;

        mSearchUser = "";
        mSearchTitle = false;

        handleIntent(getIntent());
    }

    public void searchTopic() {
        isLoading = true;

        mForum.searchTopic(mQuery, currentPage, mSearchUser, mSearchTitle, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                Map<String, Object> r = (Map<String, Object>) result;
                final Object[] topics = (Object[]) r.get("topics");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setFooterEnabled(false);
                        mAdapter.updateData(topics);
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
                        mAdapter.setFooterEnabled(false);
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
                        mAdapter.setFooterEnabled(false);
                        isLoading = false;
                        mSwipeContainer.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showAdvanced() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = getLayoutInflater().inflate(R.layout.search_advanced, null);
        final EditText editText = (EditText) view.findViewById(R.id.search_user);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.search_title);
        editText.setText(mSearchUser);
        checkBox.setChecked(mSearchTitle);
        builder.setView(view);
        builder.setTitle("Advanced options");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String username = editText.getText().toString().trim();
                mSearchUser = username;
                mSearchTitle = checkBox.isChecked();
            }
        });
        builder.show();
        // Show keyboard
        editText.post(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
        MenuItem searchItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchItem.getActionView();
        if (!Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            // If we're not sent here with a Search action, expand the search bar
            MenuItemCompat.expandActionView(searchItem);
        }
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_advanced:
                showAdvanced();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            mQuery = intent.getStringExtra(SearchManager.QUERY).trim();
            if (mQuery.length() > 0) {
                getSupportActionBar().setTitle("\"" + mQuery + "\"");
                mSwipeContainer.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeContainer.setRefreshing(true);
                    }
                });
                clearSearch();
                searchTopic();
            }
        }
    }

    @Override
    public void onRefresh() {
        clearSearch();
        searchTopic();
    }

    private void addMoreItems() {
        currentPage++;
        mAdapter.setFooterEnabled(true);
        searchTopic();
    }

    private void clearSearch() {
        mAdapter.clear();
        lastCount = 0;
        currentPage = 1;
        userScrolled = false;
        isLoading = false;
    }

    @Override
    public void onItemClicked(Map<String, Object> item) {
        // Load topic
        String topicID = (String) item.get("topic_id");
        String boardID = (String) item.get("forum_id");
        Intent intent = new Intent(this, TopicViewActivity.class);
        intent.putExtra(TopicViewActivity.ARG_TOPIC_ID, topicID);
        intent.putExtra(TopicViewActivity.ARG_BOARD_ID, boardID);
        startActivity(intent);
    }
}
