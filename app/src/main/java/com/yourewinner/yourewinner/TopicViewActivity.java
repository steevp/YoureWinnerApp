package com.yourewinner.yourewinner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class TopicViewActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private Forum mForum;
    private CustomListView mTopicViewList;
    private TopicViewAdapter mTopicViewAdapter;
    private ProgressDialog mDialog;
    private TextView mHeaderPage;
    private TextView mFooterPage;
    private Menu mOptionsMenu;
    private ActionMode mActionMode;

    private String topicTitle;
    private String topicID;
    private String boardID;
    private boolean isSubscribed = false;
    private int page;
    private int lastPage;
    private int scrollTo = 0;

    private View mHeader;
    private View mFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mTopicViewList = (CustomListView) findViewById(R.id.topic_view_list);
        mTopicViewAdapter = new TopicViewAdapter(this, getLayoutInflater());
        mTopicViewList.setAdapter(mTopicViewAdapter);

        mForum = Forum.getInstance();

        mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.loading));

        mHeader = getLayoutInflater().inflate(R.layout.pagelinks, null);
        mFooter = getLayoutInflater().inflate(R.layout.pagelinks, null);
        mHeaderPage = (TextView) mHeader.findViewById(R.id.curpage);
        mFooterPage = (TextView) mFooter.findViewById(R.id.curpage);

        mTopicViewList.addHeaderView(mHeader);
        mTopicViewList.addFooterView(mFooter);

        topicID = getIntent().getStringExtra("topicID");
        page = 1;
        lastPage = 1;

        mTopicViewList.setOnItemClickListener(this);

        getTopicByUnread();
    };

    public void getTopic() {
        mDialog.show();
        mForum.getTopic(topicID, page, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                mDialog.dismiss();
                Map<String, Object> r = (Map<String, Object>) result;

                final String forumName = new String((byte[]) r.get("forum_name"));
                topicTitle = new String((byte[]) r.get("topic_title"));
                boardID = r.get("forum_id").toString();

                int totalPosts = (int) r.get("total_post_num");
                lastPage = (int) Math.ceil((double) totalPosts / 15);

                isSubscribed = (boolean) r.get("is_subscribed");

                final Object[] posts = (Object[]) r.get("posts");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSupportActionBar().setTitle(topicTitle);

                        MenuItem actionReply = mOptionsMenu.findItem((R.id.action_reply));
                        MenuItem actionSubscribe = mOptionsMenu.findItem(R.id.action_subscribe);
                        MenuItem actionUnsubscribe = mOptionsMenu.findItem(R.id.action_unsubscribe);

                        if (mForum.getLogin()) {
                            actionReply.setVisible(true);
                            if (isSubscribed) {
                                actionUnsubscribe.setVisible(true);
                                actionSubscribe.setVisible(false);
                            } else {
                                actionSubscribe.setVisible(true);
                                actionUnsubscribe.setVisible(false);
                            }
                        }

                        mHeader.setVisibility(View.VISIBLE);
                        mFooter.setVisibility(View.VISIBLE);

                        mHeaderPage.setText(page + "/" + lastPage);
                        mFooterPage.setText(page + "/" + lastPage);
                        mTopicViewAdapter.updateData(posts);
                        mTopicViewList.setSelection(scrollTo);
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                mDialog.dismiss();
                error.printStackTrace();
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                mDialog.dismiss();
                error.printStackTrace();
            }
        });
    }

    public void getTopicByUnread() {
        mDialog.show();
        mForum.getTopicByUnread(topicID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                mDialog.dismiss();
                Map<String, Object> r = (Map<String, Object>) result;

                final String forumName = new String((byte[]) r.get("forum_name"));
                topicTitle = new String((byte[]) r.get("topic_title"));
                boardID = r.get("forum_id").toString();

                int totalPosts = (int) r.get("total_post_num");
                lastPage = (int) Math.ceil((double) totalPosts / 15);

                isSubscribed = (boolean) r.get("is_subscribed");

                int position = (int) r.get("position");
                page = (int) Math.ceil((double) position / 15);
                scrollTo = position % 15;

                final Object[] posts = (Object[]) r.get("posts");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSupportActionBar().setTitle(topicTitle);
                        //mTopicViewList.addHeaderView(mFooter);
                        //mTopicViewList.addFooterView(mFooter);

                        MenuItem actionReply = mOptionsMenu.findItem((R.id.action_reply));
                        MenuItem actionSubscribe = mOptionsMenu.findItem(R.id.action_subscribe);
                        MenuItem actionUnsubscribe = mOptionsMenu.findItem(R.id.action_unsubscribe);

                        if (mForum.getLogin()) {
                            actionReply.setVisible(true);
                            if (isSubscribed) {
                                actionUnsubscribe.setVisible(true);
                                actionSubscribe.setVisible(false);
                            } else {
                                actionSubscribe.setVisible(true);
                                actionUnsubscribe.setVisible(false);
                            }
                        }

                        mHeader.setVisibility(View.VISIBLE);
                        mFooter.setVisibility(View.VISIBLE);

                        mHeaderPage.setText(page + "/" + lastPage);
                        mFooterPage.setText(page + "/" + lastPage);
                        mTopicViewAdapter.updateData(posts);
                        mTopicViewList.setSelection(scrollTo);
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {
                mDialog.dismiss();
                error.printStackTrace();
            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {
                mDialog.dismiss();
                error.printStackTrace();
            }
        });
    }

    public void nextPage(View v) {
        if (page < lastPage) {
            scrollTo = 0;
            mTopicViewList.clearChoices();
            page++;
            getTopic();
        }
    }

    public void previousPage(View v) {
        if (page > 1) {
            scrollTo = 0;
            mTopicViewList.clearChoices();
            page--;
            getTopic();
        }
    }

    public void goFirstPage(View v) {
        setPage(1);
    }

    public void goLastPage(View v) {
        setPage(lastPage);
    }

    public void setPage(int page) {
        if (page != this.page) {
            scrollTo = 0;
            mTopicViewList.clearChoices();
            this.page = page;
            getTopic();
        }
    }

    private void ratePost() {
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        ListView listView = new ListView(this);
        final RatingListAdapter adapter = new RatingListAdapter(this, getLayoutInflater());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SparseBooleanArray checked = mTopicViewList.getCheckedItemPositions();
                for (int i = 0, size = checked.size(); i < size; i++) {
                    final int key = checked.keyAt(i);
                    if (checked.get(key)) {
                        mDialog.show();
                        Map<String, Object> post = (Map<String, Object>) mTopicViewAdapter.getItem(key - 1);
                        String postID = post.get("post_id").toString();
                        String ratingID = adapter.getItem(position).toString();
                        mForum.ratePost(postID, ratingID, new XMLRPCCallback() {
                            @Override
                            public void onResponse(long id, Object result) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollTo = key;
                                        getTopic();
                                    }
                                });
                            }

                            @Override
                            public void onError(long id, XMLRPCException error) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Rating failed!", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }

                            @Override
                            public void onServerError(long id, XMLRPCServerException error) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Ratiing failed!", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        });
                    }
                }
                alert.dismiss();
            }
        });
        alert.setTitle(getString(R.string.rate_post));
        alert.setView(listView);
        alert.show();
    }

    private void viewRatings() {
        final SparseBooleanArray checked = mTopicViewList.getCheckedItemPositions();
        for (int i=0, size=checked.size(); i<size; i++) {
            final int key = checked.keyAt(i);
            if (checked.get(key)) {
                final Map<String,Object> post = (Map<String,Object>) mTopicViewAdapter.getItem(key - 1);
                final String postID = (String) post.get("post_id");
                mForum.viewRatings(postID, new XMLRPCCallback() {
                    @Override
                    public void onResponse(long id, Object result) {
                        final Map<String,Object> r = (Map<String,Object>) result;
                        if ((boolean) r.get("result")) {
                            final Object[] ratings = (Object[]) r.get("ratings");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final AlertDialog alert = new AlertDialog.Builder(TopicViewActivity.this).create();
                                    alert.setTitle("Ratings for this post");
                                    final ListView listView = new ListView(TopicViewActivity.this);
                                    final RatingViewAdapter adapter = new RatingViewAdapter(TopicViewActivity.this, getLayoutInflater(), ratings);
                                    listView.setAdapter(adapter);
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            alert.dismiss();
                                        }
                                    });
                                    alert.setView(listView);
                                    alert.show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(long id, XMLRPCException error) {

                    }

                    @Override
                    public void onServerError(long id, XMLRPCServerException error) {

                    }
                });
                break;
            }
        }
    }

    private void replyPost() {
        final Intent intent = new Intent(this, ReplyTopicActivity.class);
        intent.putExtra("topicTitle", topicTitle);
        intent.putExtra("topicID", topicID);
        intent.putExtra("boardID", boardID);
        startActivityForResult(intent, 69);
    }

    private void quotePost() {
        final Intent intent = new Intent(this, ReplyTopicActivity.class);
        intent.putExtra("topicTitle", topicTitle);
        intent.putExtra("topicID", topicID);
        intent.putExtra("boardID", boardID);
        final SparseBooleanArray checked = mTopicViewList.getCheckedItemPositions();
        for (int i=0, size=checked.size();i<size;i++) {
            final int key = checked.keyAt(i);
            if (checked.get(key)) {
                final Map<String,Object> post = (Map<String,Object>) mTopicViewAdapter.getItem(key - 1);
                final String postID = post.get("post_id").toString();
                intent.putExtra("postID", postID);
                break;
            }
        }
        intent.putExtra("quote", true);
        startActivityForResult(intent, 69);
    }

    private void editPost() {
        final SparseBooleanArray checked = mTopicViewList.getCheckedItemPositions();
        for (int i = 0, size = checked.size(); i < size; i++) {
            final int key = checked.keyAt(i);
            if (checked.get(key)) {
                final Map<String, Object> post = (Map<String, Object>) mTopicViewAdapter.getItem(key - 1);
                final String postID = post.get("post_id").toString();
                final Intent intent = new Intent(this, EditPostActivity.class);
                intent.putExtra("postID", postID);
                startActivityForResult(intent, 69);
                break;
            }
        }
    }

    public void selectPage(View v) {
        final AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setTitle("Jump to");
        ArrayList<String> arrayList = new ArrayList<String>(lastPage);
        for (int i=0;i<lastPage;i++) {
            arrayList.add(i, "Page " + (i + 1));
        }
        ListView listView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setPage(position + 1);
                alert.dismiss();
            }
        });
        alert.setView(listView);
        alert.show();
    }

    private void subscribeTopic() {
        mForum.subscribeTopic(topicID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isSubscribed = true;
                        MenuItem actionSubscribe = mOptionsMenu.findItem(R.id.action_subscribe);
                        MenuItem actionUnsubscribe = mOptionsMenu.findItem(R.id.action_unsubscribe);
                        actionSubscribe.setVisible(false);
                        actionUnsubscribe.setVisible(true);
                        Toast.makeText(getApplicationContext(), "Subscribed!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {

            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {

            }
        });
    }

    private void unsubscribeTopic() {
        mForum.unsubscribeTopic(topicID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isSubscribed = false;
                        MenuItem actionSubscribe = mOptionsMenu.findItem(R.id.action_subscribe);
                        MenuItem actionUnsubscribe = mOptionsMenu.findItem(R.id.action_unsubscribe);
                        actionSubscribe.setVisible(true);
                        actionUnsubscribe.setVisible(false);
                        Toast.makeText(getApplicationContext(), "Unsubscribed!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(long id, XMLRPCException error) {

            }

            @Override
            public void onServerError(long id, XMLRPCServerException error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_topic_view, menu);
        mOptionsMenu = menu;
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mActionMode == null) {
            TopicViewActionCallback callback = new TopicViewActionCallback();
            mTopicViewList.setOnItemCheckedListener(callback);
            mActionMode = startActionMode(callback);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_reply:
                replyPost();
                return true;
            case R.id.action_subscribe:
                subscribeTopic();
                return true;
            case R.id.action_unsubscribe:
                unsubscribeTopic();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //mTopicViewList.clearChoices();
            getTopic();
        }
    }

    private class TopicViewActionCallback implements ActionMode.Callback, CustomListView.OnItemCheckedListener {
        private MenuItem mViewRatings;
        private MenuItem mQuote;
        private MenuItem mEdit;
        private int mSelected;

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_topic_view_contextual, menu);
            mViewRatings = menu.findItem(R.id.action_view_rating);
            mQuote = menu.findItem(R.id.action_quote);
            mEdit = menu.findItem(R.id.action_edit);
            mSelected = 0;
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_rate:
                    ratePost();
                    return true;
                case R.id.action_quote:
                    quotePost();
                    return true;
                case R.id.action_edit:
                    editPost();
                    return true;
                case R.id.action_view_rating:
                    viewRatings();
                    return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mTopicViewList.clearChoices();
            mTopicViewList.requestLayout();
            mActionMode = null;
        }

        @Override
        public void onItemChecked(int position, boolean checked) {
            if (checked) {
                mSelected++;
            } else {
                mSelected--;
            }

            if (mForum.getLogin() && mSelected == 1) {
                mViewRatings.setVisible(true);
                mQuote.setVisible(true);
            } else {
                mViewRatings.setVisible(false);
                mQuote.setVisible(false);
            }

            if (mSelected == 1) {
                SparseBooleanArray checkStates = mTopicViewList.getCheckedItemPositions();
                for (int i=0, size=checkStates.size(); i<size; i++) {
                    int key = checkStates.keyAt(i);
                    if (checkStates.get(key)) {
                        Map<String,Object> post = (Map<String,Object>) mTopicViewAdapter.getItem(key - 1);
                        // Check if user can edit the post
                        boolean canEdit = (boolean) post.get("can_edit");
                        if (canEdit) {
                            mEdit.setVisible(true);
                        } else {
                            mEdit.setVisible(false);
                        }
                        String username = new String((byte[]) post.get("post_author_name"), StandardCharsets.UTF_8);
                        mActionMode.setTitle(username);
                        break;
                    }
                }
            } else {
                mEdit.setVisible(false);
            }

            if (mSelected > 1) {
                mActionMode.setTitle(mSelected + " selected");
            } else if (mSelected == 0) {
                mActionMode.finish();
            }
        }
    }
}
