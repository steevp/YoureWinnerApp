package com.yourewinner.yourewinner;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yourewinner.yourewinner.wrapper.PostsWrapper;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class BoardViewActivity extends AppCompatActivity implements PostsAdapter.OnItemClickedListener {
    private Forum mForum;
    private RecyclerView mRecyclerView;
    private PostsAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private String mBoardID;
    private String mBoardName;
    private ArrayList<Map<String,Object>> mChildren;
    private LinearLayout mHeader;

    private int lastCount = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean userScrolled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final Intent intent = getIntent();
        mBoardID = intent.getStringExtra("boardID");
        mBoardName = intent.getStringExtra("boardName");
        mChildren = (ArrayList<Map<String,Object>>) intent.getSerializableExtra("children");

        getSupportActionBar().setTitle(mBoardName);

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

        //mHeader = (LinearLayout) getLayoutInflater().inflate(R.layout.board_header, null);
        mHeader = (LinearLayout) findViewById(R.id.board_header);
        populateHeader();

        lastCount = 0;
        currentPage = 1;
        userScrolled = false;
        isLoading = true;

        getBoard();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), NewTopicActivity.class);
                intent.putExtra(NewTopicActivity.ARG_BOARDID, mBoardID);
                intent.putExtra(NewTopicActivity.ARG_BOARDNAME, mBoardName);
                startActivity(intent);
            }
        });
    }

    public void populateHeader() {
        mHeader.removeAllViews();

        for (int i=0;i<mChildren.size();i++) {
            RelativeLayout headerItem = (RelativeLayout) getLayoutInflater().inflate(R.layout.board_header_item, mHeader, false);
            TextView headerTitle = (TextView) headerItem.findViewById(R.id.board_header_title);
            Map<String,Object> child = (Map<String,Object>) mChildren.get(i);
            final String boardName = new String((byte[]) child.get("forum_name"), Charset.forName("UTF-8"));
            headerTitle.setText(boardName);
            final String boardID = (String) child.get("forum_id");
            final Object[] c = (Object[]) child.get("child");
            final ArrayList<Map<String,Object>> children = new ArrayList<Map<String,Object>>();
            if (c != null) {
                for (int x=0;x<c.length;x++) {
                    children.add((Map<String,Object>) c[x]);
                }
            }
            headerItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BoardViewActivity.this, BoardViewActivity.class);
                    intent.putExtra("boardName", boardName);
                    intent.putExtra("boardID", boardID);
                    intent.putExtra("children", children);
                    startActivity(intent);
                    //getChildBoard(boardName, boardID);
                }
            });
            mHeader.addView(headerItem);
        }
    }

    public void getBoard() {
        mForum.getBoard(mBoardID, currentPage, new XMLRPCCallback() {
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
                        Toast.makeText(getApplicationContext(), "Error loading board!", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getApplicationContext(), "FUCK: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void addMoreItems() {
        currentPage++;
        mAdapter.setFooterEnabled(true);
        getBoard();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(PostsWrapper post) {
        // Load topic
        String topicID = post.getTopicId();
        Intent intent = new Intent(this, TopicViewActivity.class);
        intent.putExtra(TopicViewActivity.ARG_TOPIC_ID, topicID);
        intent.putExtra(TopicViewActivity.ARG_BOARD_ID, mBoardID);
        startActivity(intent);
    }
}
