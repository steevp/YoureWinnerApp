package com.yourewinner.yourewinner;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

public class NewTopicActivity extends AppCompatActivity {

    public final static String ARG_BOARDID = "ARG_BOARDID";
    public final static String ARG_BOARDNAME = "ARG_BOARDNAME";
    private String mBoardID;
    private String mBoardName;
    private Forum mForum;
    private EditText mSubject;
    private EditText mBody;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Config.loadTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_topic);

        mForum = Forum.getInstance();
        mBoardID = getIntent().getStringExtra(ARG_BOARDID);
        mBoardName = getIntent().getStringExtra(ARG_BOARDNAME);

        mDialog = new ProgressDialog(this);
        mDialog.setMessage(getString(R.string.loading));
        mDialog.setCancelable(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        getSupportActionBar().setTitle(mBoardName);

        mSubject = (EditText) findViewById(R.id.subject);
        mBody = (EditText) findViewById(R.id.body);
    }

    private void createNewTopic() {
        String subject = mSubject.getText().toString();
        String body = mBody.getText().toString();

        if (subject.length() > 0 && body.length() > 0) {
            mDialog.show();
            mForum.newTopic(mBoardID, subject, body, new XMLRPCCallback() {
                @Override
                public void onResponse(long id, Object result) {
                    Map<String,Object> r = (Map<String,Object>) result;
                    final String topicID = (String) r.get("topic_id");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(getApplicationContext(), TopicViewActivity.class);
                            intent.putExtra("topicID", topicID);
                            startActivity(intent);
                            mDialog.dismiss();
                            finish();
                        }
                    });
                }

                @Override
                public void onError(long id, XMLRPCException error) {
                    error.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed to create topic!", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                @Override
                public void onServerError(long id, XMLRPCServerException error) {
                    error.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "FUCK: yourewinner.com might be down!", Toast.LENGTH_LONG);
                        }
                    });
                }
            });
        } else {
            Toast.makeText(this, "Please fill in subject AND body!", Toast.LENGTH_LONG).show();
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
            createNewTopic();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
