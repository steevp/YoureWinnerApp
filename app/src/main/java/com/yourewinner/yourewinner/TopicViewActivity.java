package com.yourewinner.yourewinner;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class TopicViewActivity extends AppCompatActivity {
    Forum mForum;
    ListView mTopicViewList;
    TopicViewAdapter mTopicViewAdapter;
    ProgressDialog mDialog;
    Button mPage;

    private String topicID;
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
        mDialog.setMessage("Loading...");

        mFooter = getLayoutInflater().inflate(R.layout.pagelinks, null);
        mPage = (Button) mFooter.findViewById(R.id.curpage);

        mTopicViewList.addHeaderView(mFooter);
        mTopicViewList.addFooterView(mFooter);

        topicID = getIntent().getStringExtra("topicID");
        page = 1;
        lastPage = 1;

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
                final String topicTitle = new String((byte[]) r.get("topic_title"));

                Integer totalPosts = (Integer) r.get("total_post_num");
                lastPage = (int) Math.ceil((double) totalPosts / 15);
                Log.d("yourewinner", "Last page " + lastPage);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_topic_view, menu);
        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
