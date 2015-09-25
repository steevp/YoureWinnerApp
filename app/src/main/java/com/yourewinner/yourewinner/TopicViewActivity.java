package com.yourewinner.yourewinner;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
    Button mPage;
    Menu mOptionsMenu;

    private String topicTitle;
    private String topicID;
    private String boardID;
    private int page;
    private int lastPage;

    View mFooter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_view);

        mTopicViewList = (ListView) findViewById(R.id.topic_view_list);
        mTopicViewAdapter = new TopicViewAdapter(this, getLayoutInflater());
        mTopicViewList.setAdapter(mTopicViewAdapter);

        mForum = Forum.getInstance();

        mDialog = new ProgressDialog(this);
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.loading));

        mFooter = getLayoutInflater().inflate(R.layout.pagelinks, null);
        mPage = (Button) mFooter.findViewById(R.id.curpage);

        mTopicViewList.addHeaderView(mFooter);
        mTopicViewList.addFooterView(mFooter);

        topicID = getIntent().getStringExtra("topicID");
        page = 1;
        lastPage = 1;

        mTopicViewList.setOnItemClickListener(this);

        getTopic();
    }

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

                Integer totalPosts = (Integer) r.get("total_post_num");
                lastPage = (int) Math.ceil((double) totalPosts / 15);

                final Object[] posts = (Object[]) r.get("posts");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSupportActionBar().setTitle(forumName);
                        getSupportActionBar().setSubtitle(topicTitle);

                        mPage.setText(page + "/" + lastPage);
                        mTopicViewAdapter.updateData(posts);
                        mTopicViewList.setSelection(0);
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
            page++;
            getTopic();
        }
    }

    public void previousPage(View v) {
        if (page > 1) {
            page--;
            getTopic();
        }
    }

    public void setPage(int page) {
        if (page != this.page) {
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
                for (int i=0, size=checked.size();i<size;i++) {
                    int key = checked.keyAt(i);
                    if (checked.get(key)) {
                        Map<String,Object> post = (Map<String,Object>) mTopicViewAdapter.getItem(key - 1);
                        String postID = post.get("post_id").toString();
                        String ratingID = adapter.getItem(position).toString();
                        mForum.ratePost(postID, ratingID, new XMLRPCCallback() {
                            @Override
                            public void onResponse(long id, Object result) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();
                                        getTopic();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_topic_view, menu);
        mOptionsMenu = menu;
        MenuItem actionReply = mOptionsMenu.findItem((R.id.action_reply));
        if (mForum.getLogin()) {
            actionReply.setVisible(true);
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        MenuItem actionQuote = mOptionsMenu.findItem(R.id.action_quote);
        MenuItem actionEdit = mOptionsMenu.findItem(R.id.action_edit);
        MenuItem actionRate = mOptionsMenu.findItem(R.id.action_rate);

        int count = mTopicViewList.getCheckedItemCount();

        if (count > 0 && mForum.getLogin()) {
            actionQuote.setVisible(true);
            actionEdit.setVisible(true);
            actionRate.setVisible(true);
        } else {
            actionQuote.setVisible(false);
            actionEdit.setVisible(false);
            actionRate.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        Intent intent;
        if (id == R.id.action_settings) {
            intent = new Intent(this, MyPreferenceActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_reply) {
            intent = new Intent(this, ReplyTopicActivity.class);
            intent.putExtra("topicTitle", topicTitle);
            intent.putExtra("topicID", topicID);
            intent.putExtra("boardID", boardID);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_rate) {
            ratePost();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
