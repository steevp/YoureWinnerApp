package com.yourewinner.yourewinner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class TopicViewActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    Forum mForum;
    ListView mTopicViewList;
    TopicViewAdapter mTopicViewAdapter;
    ProgressDialog mDialog;
    TextView mHeaderPage;
    TextView mFooterPage;
    Menu mOptionsMenu;

    private String topicTitle;
    private String topicID;
    private String boardID;
    private boolean isSubscribed = false;
    private int page;
    private int lastPage;
    private int scrollTo = 0;

    View mHeader;
    View mFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mTopicViewList = (ListView) findViewById(R.id.topic_view_list);
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
    }

    public void getTopic() {
        //mTopicViewList.removeHeaderView(mFooter);
        //mTopicViewList.removeFooterView(mFooter);
        //mHeader.setVisibility(View.GONE);
        //mFooter.setVisibility(View.GONE);
        mDialog.show();
        mForum.getTopic(topicID, page, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                mDialog.dismiss();
                Map<String, Object> r = (Map<String, Object>) result;

                final String forumName = new String((byte[]) r.get("forum_name"));
                topicTitle = new String((byte[]) r.get("topic_title"));
                boardID = r.get("forum_id").toString();

                Integer totalPosts = (Integer) r.get("total_post_num");
                lastPage = (int) Math.ceil((double) totalPosts / 15);

                isSubscribed = (boolean) r.get("is_subscribed");

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

    public void getTopicByUnread() {
        //mTopicViewList.removeHeaderView(mFooter);
        //mTopicViewList.removeFooterView(mFooter);
        //mHeader.setVisibility(View.GONE);
        //mFooter.setVisibility(View.GONE);
        mDialog.show();
        mForum.getTopicByUnread(topicID, new XMLRPCCallback() {
            @Override
            public void onResponse(long id, Object result) {
                mDialog.dismiss();
                Map<String, Object> r = (Map<String, Object>) result;

                final String forumName = new String((byte[]) r.get("forum_name"));
                topicTitle = new String((byte[]) r.get("topic_title"));
                boardID = r.get("forum_id").toString();

                Integer totalPosts = (Integer) r.get("total_post_num");
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

    public void ratePost() {
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

    public void subscribeTopic() {
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

    public void unsubscribeTopic() {
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
        /*MenuItem actionReply = mOptionsMenu.findItem((R.id.action_reply));
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
        }*/
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MenuItem actionQuote = mOptionsMenu.findItem(R.id.action_quote);
        MenuItem actionEdit = mOptionsMenu.findItem(R.id.action_edit);
        MenuItem actionRate = mOptionsMenu.findItem(R.id.action_rate);
        MenuItem actionSelect = mOptionsMenu.findItem(R.id.action_select_all);
        MenuItem actionDeselect = mOptionsMenu.findItem(R.id.action_deselect_all);

        int count = mTopicViewList.getCheckedItemCount();
        Map<String,Object> r = (Map<String,Object>) mTopicViewAdapter.getItem(position-1);
        boolean canEdit = (boolean) r.get("can_edit");

        if (count == 1 && mForum.getLogin()) {
            actionQuote.setVisible(true);
            if (canEdit) {
                actionEdit.setVisible(true);
            } else {
                actionEdit.setVisible(false);
            }
            actionRate.setVisible(true);
            actionDeselect.setVisible(true);
        } else if (count > 1 && mForum.getLogin()) {
            actionQuote.setVisible(false);
            actionEdit.setVisible(false);
            actionRate.setVisible(true);
            actionDeselect.setVisible(true);
        } else {
            actionQuote.setVisible(false);
            actionEdit.setVisible(false);
            actionRate.setVisible(false);
            actionDeselect.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        MenuItem actionQuote = mOptionsMenu.findItem(R.id.action_quote);
        MenuItem actionEdit = mOptionsMenu.findItem(R.id.action_edit);
        MenuItem actionRate = mOptionsMenu.findItem(R.id.action_rate);
        MenuItem actionSelect = mOptionsMenu.findItem(R.id.action_select_all);
        MenuItem actionDeselect = mOptionsMenu.findItem(R.id.action_deselect_all);

        //noinspection SimplifiableIfStatement
        Intent intent;
        if (id == R.id.action_reply || id == R.id.action_quote) {
            intent = new Intent(this, ReplyTopicActivity.class);
            intent.putExtra("topicTitle", topicTitle);
            intent.putExtra("topicID", topicID);
            intent.putExtra("boardID", boardID);
            if (id == R.id.action_quote) {
                SparseBooleanArray checked = mTopicViewList.getCheckedItemPositions();
                String postID = null;
                for (int i=0, size=checked.size();i<size;i++) {
                    int key = checked.keyAt(i);
                    if (checked.get(key)) {
                        Map<String,Object> post = (Map<String,Object>) mTopicViewAdapter.getItem(key - 1);
                        postID = post.get("post_id").toString();
                        intent.putExtra("postID", postID);
                        break;
                    }
                }
                intent.putExtra("quote", true);
            }
            startActivityForResult(intent, 69);
            return true;
        } else if (id == R.id.action_edit) {
            SparseBooleanArray checked = mTopicViewList.getCheckedItemPositions();
            String postID = null;
            for (int i = 0, size = checked.size(); i < size; i++) {
                int key = checked.keyAt(i);
                if (checked.get(key)) {
                    Map<String, Object> post = (Map<String, Object>) mTopicViewAdapter.getItem(key - 1);
                    postID = post.get("post_id").toString();
                    intent = new Intent(this, EditPostActivity.class);
                    intent.putExtra("postID", postID);
                    startActivityForResult(intent, 69);
                    break;
                }
            }
            return true;
        } else if (id == R.id.action_subscribe) {
            subscribeTopic();
            return true;
        } else if (id == R.id.action_unsubscribe) {
            unsubscribeTopic();
            return true;
        } else if (id == R.id.action_rate) {
            ratePost();
            return true;
        } else if (id == R.id.action_deselect_all) {
            mTopicViewList.clearChoices();
            mTopicViewList.requestLayout();
            actionQuote.setVisible(false);
            actionEdit.setVisible(false);
            actionRate.setVisible(false);
            actionDeselect.setVisible(false);
            return true;
        } else if (id == R.id.action_select_all) {
            for (int i=1;i<mTopicViewAdapter.getCount()+1;i++) {
                mTopicViewList.setItemChecked(i, true);
            }
            mTopicViewList.requestLayout();
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
}
