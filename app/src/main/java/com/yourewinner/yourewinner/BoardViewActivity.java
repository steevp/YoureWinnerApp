package com.yourewinner.yourewinner;

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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class BoardViewActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {

    private Forum mForum;
    private SharedPreferences mSharedPreferences;
    private ListView mPostList;
    private PostAdapter mPostAdapter;
    private String mBoardID;
    private String mBoardName;
    private ArrayList<Map<String,Object>> mChildren;
    private LinearLayout mHeader;
    private View mFooter;

    private int lastCount = 0;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean userScrolled = false;

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
        setContentView(R.layout.activity_board_view);

        final Intent intent = getIntent();
        mBoardID = intent.getStringExtra("boardID");
        mBoardName = intent.getStringExtra("boardName");
        mChildren = (ArrayList<Map<String,Object>>) intent.getSerializableExtra("children");

        getSupportActionBar().setTitle(mBoardName);

        mForum = Forum.getInstance();
        mPostList = (ListView) findViewById(R.id.posts_list);
        mPostList.setOnItemClickListener(this);
        mPostList.setOnScrollListener(this);
        mPostAdapter = new PostAdapter(this, getLayoutInflater());
        mPostList.setAdapter(mPostAdapter);

        mHeader = (LinearLayout) getLayoutInflater().inflate(R.layout.board_header, null);
        populateHeader();

        mPostList.addHeaderView(mHeader);

        mFooter = (View) getLayoutInflater().inflate(R.layout.loading, null);
        mPostList.addFooterView(mFooter);

        lastCount = 0;
        currentPage = 1;
        userScrolled = false;
        isLoading = true;

        getBoard();
    }

    public void populateHeader() {
        mHeader.removeAllViews();

        for (int i=0;i<mChildren.size();i++) {
            RelativeLayout headerItem = (RelativeLayout) getLayoutInflater().inflate(R.layout.board_header_item, null);
            TextView headerTitle = (TextView) headerItem.findViewById(R.id.board_header_title);
            Map<String,Object> child = (Map<String,Object>) mChildren.get(i);
            final String boardName = new String((byte[]) child.get("forum_name"), StandardCharsets.UTF_8);
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
                    Intent intent = new Intent(getApplicationContext(), BoardViewActivity.class);
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
                        Log.d("ywdebug", "page " + currentPage);
                        mPostList.removeFooterView(mFooter);
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
                        mPostList.removeFooterView(mFooter);
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
                        mPostList.removeFooterView(mFooter);
                        isLoading = false;
                        Toast.makeText(getApplicationContext(), "FUCK: yourewinner.com might be down!", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public void getChildBoard(String boardName, String boardID) {
        mBoardName = boardName;
        getSupportActionBar().setTitle(mBoardName);
        mBoardID = boardID;
        mHeader.removeAllViews();
        mPostAdapter = new PostAdapter(this, getLayoutInflater());
        mPostList.setAdapter(mPostAdapter);
        getBoard();
    }

    public void addMoreItems() {
        currentPage++;
        mPostList.addFooterView(mFooter);
        getBoard();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Map<String,Object> topic = (Map<String,Object>) mPostAdapter.getItem(position-1);
        if (topic != null) {
            String topicID = (String) topic.get("topic_id");
            Intent intent = new Intent(this, TopicViewActivity.class);
            intent.putExtra("topicID", topicID);
            startActivity(intent);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
            userScrolled = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        final int lastItem = firstVisibleItem + visibleItemCount - mPostList.getFooterViewsCount();
        if (userScrolled && lastItem == totalItemCount && totalItemCount > lastCount && !isLoading) {
            isLoading = true;
            lastCount = totalItemCount;
            addMoreItems();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reply_topic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_reply) {
            Intent intent = new Intent(this, NewTopicActivity.class);
            intent.putExtra(NewTopicActivity.ARG_BOARDID, mBoardID);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}